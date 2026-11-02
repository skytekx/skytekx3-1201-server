package com.twilightmobsloots.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.twilightmobsloots.MobHead;
import com.twilightmobsloots.TwilightMobsLoots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import twilightforest.client.model.TFModelLayers;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 3D head model of one mob trophy. Upstream implements Twilight Forest 4.8's {@code TrophyBlockModel}
 * interface; the 1.20.1 fork has no such interface (its boss trophies extend {@code GenericTrophyModel}),
 * so this class keeps the same two methods on its own.
 *
 * <p>Like Twilight Forest's boss trophies only the head part of the mob model is drawn:
 * <ul>
 *     <li>bake the mob's Twilight Forest model layer and take its {@code "head"} part; mobs whose head is
 *         an empty shell (the Yeti's face is on its body) fall back to {@code "body"};</li>
 *     <li>measure the part's bounding box (parent frame, pivots included) through reflection and scale by
 *         <b>height</b> to about half a block ({@link #TARGET_SIZE}), so horns and ears do not shrink it;</li>
 *     <li>offsets are block units: translation happens before the mirrored scale, so model units are
 *         divided by 16 first;</li>
 *     <li>{@link #setupRotationsForTrophy} turns the head by yaw (GUI spin), xRot adds a slight tilt;</li>
 *     <li>{@link #renderTrophy} binds the entity texture and draws only that part.</li>
 * </ul>
 *
 * <h3>Coordinates</h3>
 * The pipeline is {@code translate(offset); scale(s); part.render()}, and {@code part.render} translates
 * by the part pivot {@code /16} before drawing the cubes, so a vertex ends up at
 * <pre>
 * finalX = offsetX + s * (pivot.x + cubeX) / 16
 * finalY = -(offsetY + s * (pivot.y + cubeY) / 16)   // the renderer mirrors with scale(-1,-1,1)
 * finalZ = offsetZ + s * (pivot.z + cubeZ) / 16
 * </pre>
 * Hence {@code s = TARGET_SIZE / H}, the horizontal offsets centre the box ({@code -s * centre / 16}) and
 * {@code offsetY = -BASE_Y - s * rmaxY / 16} puts the lowest point {@value #BASE_Y} above the base.
 *
 * <h3>1.20.1 fork specifics</h3>
 * <ul>
 *     <li>Entity textures live in {@code twilightforest:textures/model/} (4.8: {@code textures/entity/}).</li>
 *     <li>4.8 picks the classic or the Jappa model inside one layer ({@code checkForPack}). The fork keeps
 *         two layers: {@code new_<mob>} holds the classic model, {@code <mob>} the Jappa model used when the
 *         Jappa models resource pack is enabled. The same choice is made here, per resource reload.</li>
 *     <li>{@code ModelPart.cubes}/{@code children} are read through their SRG names so the reflection also
 *         works in the obfuscated production runtime.</li>
 * </ul>
 */
public class MobTrophyBlockModel {

    /** Target head height in model units (16 = one block), about half a block like the boss trophies. */
    private static final float TARGET_SIZE = 8.0F;

    /** Height of the lowest point above the current frame origin (block units): 0 = on the ground / head. */
    private static final float BASE_Y = 0.0F;

    /** {@code ModelPart.cubes} (SRG f_104212_) and {@code ModelPart.children} (SRG f_104213_). */
    private static final Field CUBES_FIELD = ObfuscationReflectionHelper.findField(ModelPart.class, "f_104212_");
    private static final Field CHILDREN_FIELD = ObfuscationReflectionHelper.findField(ModelPart.class, "f_104213_");

    /** Marker file of Twilight Forest's Jappa models resource pack (same check as the fork's renderers). */
    private static final ResourceLocation JAPPA_MARKER = new ResourceLocation("twilightforest", "jappa_models.marker");

    private final ModelPart head;
    private final ResourceLocation texture;
    /** Render the viewer's skin (Armored Giant / Giant Miner) instead of a fixed texture. */
    private final boolean playerSkin;

    /** Scale and offsets that fit any head to the trophy size (block units). */
    private final float scale;
    /** Horizontal centre of the head box (model units), used to re-centre the head when it turns. */
    private final float centerX;
    private final float centerZ;
    private final float offsetY;
    /** Full box max Y (model units), to recompute offsetY when worn (HEAD context scales up). */
    private final float maxY;

    public MobTrophyBlockModel(ModelPart head, ResourceLocation texture, boolean playerSkin) {
        this.head = head;
        this.texture = texture;
        this.playerSkin = playerSkin;

        // Full box (children included, rotations ignored): decides the scale height and vertical placement.
        float[] fullBounds = newBounds();
        collectBounds(head, 0.0F, 0.0F, 0.0F, fullBounds);
        float height = fullBounds[4] - fullBounds[1];
        // Scale by height: small models grow, tall ones shrink, horns and ears no longer shrink the head.
        this.scale = height > 0.0F ? TARGET_SIZE / height : 1.0F;
        this.maxY = fullBounds[4];
        // The lowest point (parent-frame max Y, lowest after mirroring) sits on the frame origin.
        this.offsetY = -BASE_Y - this.scale * fullBounds[4] / 16.0F;

        // Centre horizontally on the head shell's own cubes: rotated children (antennae, horns) skew the
        // unrotated box (beetle antennae addBox(0,0,0,10,1,1) all reach +x, centerX=3.25, trophy "leans
        // right"); the shell itself is usually symmetric. Without solid shell cubes use the full box.
        float[] coreBounds = newBounds();
        collectDirectCubes(head, 0.0F, 0.0F, 0.0F, coreBounds);
        if (coreBounds[3] > coreBounds[0] && coreBounds[5] > coreBounds[2]) {
            this.centerX = (coreBounds[0] + coreBounds[3]) * 0.5F;
            this.centerZ = (coreBounds[2] + coreBounds[5]) * 0.5F;
        } else {
            this.centerX = (fullBounds[0] + fullBounds[3]) * 0.5F;
            this.centerZ = (fullBounds[2] + fullBounds[5]) * 0.5F;
        }
    }

    public void setupRotationsForTrophy(float anim, float yaw, float xRot, float zRot) {
        this.head.yRot = yaw * Mth.DEG_TO_RAD;
        this.head.xRot = xRot * Mth.DEG_TO_RAD;
    }

    public void renderTrophy(PoseStack pose, MultiBufferSource buffer, int light, int overlay,
                             ItemDisplayContext context) {
        // Turn the head around the frame's vertical axis. A head pivot is often off the head centre
        // (beetles pivot behind the head, spiders and wolves high up), so turning only head.yRot with a
        // fixed offset would swing the head off the base. Recompute the horizontal offset per yaw so the
        // box centre stays on the frame origin. Translate first, then scale.
        float yaw = this.head.yRot;
        float cos = Mth.cos(yaw);
        float sin = Mth.sin(yaw);
        float dcx = this.centerX - this.head.x;
        float dcz = this.centerZ - this.head.z;
        float ox = -this.scale * (this.head.x + dcx * cos + dcz * sin) / 16.0F;
        float oz = -this.scale * (this.head.z - dcx * sin + dcz * cos) / 16.0F;
        // Worn: CustomHeadLayer shrinks to 62.5%, multiply by 1.6 to restore the size. offsetY must grow
        // with it or the head base sinks below the top of the head.
        float s = this.scale * (context == ItemDisplayContext.HEAD ? 1.6F : 1.0F);
        float oy = context == ItemDisplayContext.HEAD ? -BASE_Y - s * this.maxY / 16.0F : this.offsetY;
        pose.translate(ox, oy, oz);
        pose.scale(s, s, s);
        ResourceLocation tex = this.playerSkin ? currentPlayerSkin() : this.texture;
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull(tex));
        this.head.render(pose, vc, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** The local player's skin (the default skin when not in a world). */
    private static ResourceLocation currentPlayerSkin() {
        AbstractClientPlayer player = Minecraft.getInstance().player;
        return player != null ? player.getSkinTextureLocation() : DefaultPlayerSkin.getDefaultSkin();
    }

    /** Bakes all 48 head models at once; mobs without a solid part are skipped. */
    public static Map<MobHead, MobTrophyBlockModel> createTrophyRenderers(EntityModelSet models) {
        boolean jappa = jappaModelsLoaded();
        ImmutableMap.Builder<MobHead, MobTrophyBlockModel> builder = ImmutableMap.builder();
        for (MobHead head : MobHead.values()) {
            ModelLayerLocation layer = LAYERS.get(head);
            ResourceLocation texture = TEXTURES.get(head);
            // Giant trophies use the current player's skin, no fixed texture.
            boolean playerSkin = head == MobHead.ARMORED_GIANT || head == MobHead.GIANT_MINER;
            if (layer == null || (!playerSkin && texture == null)) {
                TwilightMobsLoots.LOGGER.warn("Mob trophy [{}] has no model layer or texture, not rendered", head.getSerializedName());
                continue;
            }
            ModelPart root = bake(models, head, layer, jappa);
            ModelPart part = selectMeshPart(root, head);
            if (part != null) {
                builder.put(head, new MobTrophyBlockModel(part, texture, playerSkin));
            }
        }
        return builder.build();
    }

    /** Bakes the classic layer unless the Jappa pack is on, falling back to the base layer. */
    private static ModelPart bake(EntityModelSet models, MobHead head, ModelLayerLocation layer, boolean jappa) {
        ModelLayerLocation classic = CLASSIC_LAYERS.get(head);
        if (!jappa && classic != null) {
            try {
                return models.bakeLayer(classic);
            } catch (IllegalArgumentException e) {
                TwilightMobsLoots.LOGGER.debug("Mob trophy [{}]: no classic layer {}, using {}", head.getSerializedName(), classic, layer);
            }
        }
        return models.bakeLayer(layer);
    }

    private static boolean jappaModelsLoaded() {
        return Minecraft.getInstance().getResourceManager().listPacks()
                .anyMatch(pack -> pack.getResource(PackType.CLIENT_RESOURCES, JAPPA_MARKER) != null);
    }

    /**
     * Picks the "head" part of a model root, in order:
     * <ol>
     *     <li>{@code head}: most mobs;</li>
     *     <li>{@code body}: when the head is an empty shell (Yeti, face on the body);</li>
     *     <li>the <b>root itself</b> when it has solid cubes (cube mobs such as the Harbinger/Roving Cube);</li>
     *     <li>the <b>largest solid direct child</b> otherwise.</li>
     * </ol>
     * Returns null when nothing is solid; the caller then skips the mob.
     */
    private static ModelPart selectMeshPart(ModelPart root, MobHead head) {
        if (root.hasChild("head") && hasSolidMesh(root.getChild("head"))) {
            return root.getChild("head");
        }
        if (root.hasChild("body") && hasSolidMesh(root.getChild("body"))) {
            return root.getChild("body");
        }
        if (hasSolidMesh(root)) {
            return root;
        }
        ModelPart largest = null;
        float largestSize = 0.0F;
        for (ModelPart child : getChildren(root).values()) {
            if (!hasSolidMesh(child)) {
                continue;
            }
            float[] b = newBounds();
            collectBounds(child, 0.0F, 0.0F, 0.0F, b);
            float size = (b[3] - b[0]) * (b[4] - b[1]) * (b[5] - b[2]);
            if (size > largestSize) {
                largestSize = size;
                largest = child;
            }
        }
        if (largest == null) {
            TwilightMobsLoots.LOGGER.warn("Mob trophy [{}]: the model has no solid part, not rendered", head.getSerializedName());
        }
        return largest;
    }

    /** Whether the part (with its descendants) has a solid mesh: positive extent on all three axes. */
    private static boolean hasSolidMesh(ModelPart part) {
        float[] bounds = newBounds();
        collectBounds(part, 0.0F, 0.0F, 0.0F, bounds);
        return bounds[3] > bounds[0] && bounds[4] > bounds[1] && bounds[5] > bounds[2];
    }

    // ===== mob -> Twilight Forest model layer / entity texture (data driven) =====

    /**
     * Mob -> model layer of the trophy head. Mostly the entity id; the Dwarf Rabbit uses the bunny layer and
     * the giants the vanilla player head layer. In the fork these are the Jappa model layers for the mobs
     * that also have a classic layer in {@link #CLASSIC_LAYERS}.
     */
    private static final EnumMap<MobHead, ModelLayerLocation> LAYERS = new EnumMap<>(MobHead.class);
    /**
     * Mob -> the fork's classic model layer ({@code twilightforest:new_<mob>}), used by the fork's renderers
     * unless the Jappa models pack is enabled. Referenced by name so a later fork without these layers falls
     * back to {@link #LAYERS} instead of failing to link.
     */
    private static final EnumMap<MobHead, ModelLayerLocation> CLASSIC_LAYERS = new EnumMap<>(MobHead.class);
    /** Mob -> trophy texture (the zombies, Death Tome and Hostile Wolf use vanilla textures). */
    private static final EnumMap<MobHead, ResourceLocation> TEXTURES = new EnumMap<>(MobHead.class);

    static {
        // Layers
        LAYERS.put(MobHead.REDCAP, TFModelLayers.REDCAP);
        LAYERS.put(MobHead.REDCAP_SAPPER, TFModelLayers.REDCAP);
        LAYERS.put(MobHead.KOBOLD, TFModelLayers.KOBOLD);
        LAYERS.put(MobHead.BLOCKCHAIN_GOBLIN, TFModelLayers.BLOCKCHAIN_GOBLIN);
        LAYERS.put(MobHead.LOWER_GOBLIN_KNIGHT, TFModelLayers.LOWER_GOBLIN_KNIGHT);
        LAYERS.put(MobHead.UPPER_GOBLIN_KNIGHT, TFModelLayers.UPPER_GOBLIN_KNIGHT);
        LAYERS.put(MobHead.LOYAL_ZOMBIE, TFModelLayers.LOYAL_ZOMBIE);
        LAYERS.put(MobHead.RISING_ZOMBIE, TFModelLayers.RISING_ZOMBIE);
        LAYERS.put(MobHead.LICH_MINION, TFModelLayers.LICH_MINION);
        LAYERS.put(MobHead.MINOTAUR, TFModelLayers.MINOTAUR);
        LAYERS.put(MobHead.MAZE_SLIME, TFModelLayers.MAZE_SLIME);
        LAYERS.put(MobHead.PINCH_BEETLE, TFModelLayers.PINCH_BEETLE);
        LAYERS.put(MobHead.SLIME_BEETLE, TFModelLayers.SLIME_BEETLE);
        LAYERS.put(MobHead.HELMET_CRAB, TFModelLayers.HELMET_CRAB);
        LAYERS.put(MobHead.HARBINGER_CUBE, TFModelLayers.HARBINGER_CUBE);
        LAYERS.put(MobHead.ROVING_CUBE, TFModelLayers.ROVING_CUBE);
        LAYERS.put(MobHead.ADHERENT, TFModelLayers.ADHERENT);
        LAYERS.put(MobHead.DEATH_TOME, TFModelLayers.DEATH_TOME);
        LAYERS.put(MobHead.CARMINITE_BROODLING, TFModelLayers.CARMINITE_BROODLING);
        LAYERS.put(MobHead.CARMINITE_GHASTGUARD, TFModelLayers.CARMINITE_GHASTGUARD);
        LAYERS.put(MobHead.CARMINITE_GHASTLING, TFModelLayers.CARMINITE_GHASTLING);
        LAYERS.put(MobHead.CARMINITE_GOLEM, TFModelLayers.CARMINITE_GOLEM);
        LAYERS.put(MobHead.TOWERWOOD_BORER, TFModelLayers.TOWERWOOD_BORER);
        LAYERS.put(MobHead.SKELETON_DRUID, TFModelLayers.SKELETON_DRUID);
        LAYERS.put(MobHead.TROLL, TFModelLayers.TROLL);
        LAYERS.put(MobHead.HEDGE_SPIDER, TFModelLayers.HEDGE_SPIDER);
        LAYERS.put(MobHead.KING_SPIDER, TFModelLayers.KING_SPIDER);
        LAYERS.put(MobHead.SWARM_SPIDER, TFModelLayers.SWARM_SPIDER);
        LAYERS.put(MobHead.FIRE_BEETLE, TFModelLayers.FIRE_BEETLE);
        LAYERS.put(MobHead.MOSQUITO_SWARM, TFModelLayers.MOSQUITO_SWARM);
        LAYERS.put(MobHead.TINY_BIRD, TFModelLayers.TINY_BIRD);
        LAYERS.put(MobHead.WINTER_WOLF, TFModelLayers.WINTER_WOLF);
        LAYERS.put(MobHead.HOSTILE_WOLF, TFModelLayers.HOSTILE_WOLF);
        LAYERS.put(MobHead.MIST_WOLF, TFModelLayers.MIST_WOLF);
        LAYERS.put(MobHead.STABLE_ICE_CORE, TFModelLayers.STABLE_ICE_CORE);
        LAYERS.put(MobHead.UNSTABLE_ICE_CORE, TFModelLayers.UNSTABLE_ICE_CORE);
        LAYERS.put(MobHead.ICE_CRYSTAL, TFModelLayers.ICE_CRYSTAL);
        LAYERS.put(MobHead.WRAITH, TFModelLayers.WRAITH);
        LAYERS.put(MobHead.YETI, TFModelLayers.YETI);
        LAYERS.put(MobHead.RAVEN, TFModelLayers.RAVEN);
        LAYERS.put(MobHead.BIGHORN_SHEEP, TFModelLayers.BIGHORN_SHEEP);
        LAYERS.put(MobHead.BOAR, TFModelLayers.BOAR);
        LAYERS.put(MobHead.DEER, TFModelLayers.DEER);
        LAYERS.put(MobHead.DWARF_RABBIT, TFModelLayers.BUNNY);
        LAYERS.put(MobHead.SQUIRREL, TFModelLayers.SQUIRREL);
        LAYERS.put(MobHead.PENGUIN, TFModelLayers.PENGUIN);
        // Giants: the vanilla player head layer (with the player's skin)
        LAYERS.put(MobHead.ARMORED_GIANT, ModelLayers.PLAYER_HEAD);
        LAYERS.put(MobHead.GIANT_MINER, ModelLayers.PLAYER_HEAD);

        // Classic layers of the fork (TFClientSetup uses them when the Jappa pack is off)
        CLASSIC_LAYERS.put(MobHead.REDCAP, classic("new_redcap"));
        CLASSIC_LAYERS.put(MobHead.REDCAP_SAPPER, classic("new_redcap"));
        CLASSIC_LAYERS.put(MobHead.KOBOLD, classic("new_kobold"));
        CLASSIC_LAYERS.put(MobHead.BLOCKCHAIN_GOBLIN, classic("new_blockchain_goblin"));
        CLASSIC_LAYERS.put(MobHead.LOWER_GOBLIN_KNIGHT, classic("new_lower_goblin_knight"));
        CLASSIC_LAYERS.put(MobHead.UPPER_GOBLIN_KNIGHT, classic("new_upper_goblin_knight"));
        CLASSIC_LAYERS.put(MobHead.MINOTAUR, classic("new_minotaur"));
        CLASSIC_LAYERS.put(MobHead.PINCH_BEETLE, classic("new_pinch_beetle"));
        CLASSIC_LAYERS.put(MobHead.SLIME_BEETLE, classic("new_slime_beetle"));
        CLASSIC_LAYERS.put(MobHead.HELMET_CRAB, classic("new_helmet_crab"));
        CLASSIC_LAYERS.put(MobHead.TROLL, classic("new_troll"));
        CLASSIC_LAYERS.put(MobHead.FIRE_BEETLE, classic("new_fire_beetle"));
        CLASSIC_LAYERS.put(MobHead.TINY_BIRD, classic("new_tiny_bird"));
        CLASSIC_LAYERS.put(MobHead.RAVEN, classic("new_raven"));
        CLASSIC_LAYERS.put(MobHead.BIGHORN_SHEEP, classic("new_bighorn_sheep"));
        CLASSIC_LAYERS.put(MobHead.BOAR, classic("new_boar"));
        CLASSIC_LAYERS.put(MobHead.DEER, classic("new_deer"));
        CLASSIC_LAYERS.put(MobHead.SQUIRREL, classic("new_squirrel"));

        // Textures
        TEXTURES.put(MobHead.REDCAP, tf("redcap.png"));
        TEXTURES.put(MobHead.REDCAP_SAPPER, tf("redcapsapper.png"));
        TEXTURES.put(MobHead.KOBOLD, tf("kobold.png"));
        TEXTURES.put(MobHead.BLOCKCHAIN_GOBLIN, tf("blockgoblin.png"));
        TEXTURES.put(MobHead.LOWER_GOBLIN_KNIGHT, tf("doublegoblin.png"));
        TEXTURES.put(MobHead.UPPER_GOBLIN_KNIGHT, tf("doublegoblin.png"));
        TEXTURES.put(MobHead.LOYAL_ZOMBIE, mc("zombie/zombie.png"));
        TEXTURES.put(MobHead.RISING_ZOMBIE, mc("zombie/zombie.png"));
        TEXTURES.put(MobHead.LICH_MINION, mc("zombie/zombie.png"));
        TEXTURES.put(MobHead.MINOTAUR, tf("minotaur.png"));
        TEXTURES.put(MobHead.MAZE_SLIME, tf("mazeslime.png"));
        TEXTURES.put(MobHead.PINCH_BEETLE, tf("pinchbeetle.png"));
        TEXTURES.put(MobHead.SLIME_BEETLE, tf("slimebeetle.png"));
        TEXTURES.put(MobHead.HELMET_CRAB, tf("helmetcrab.png"));
        TEXTURES.put(MobHead.HARBINGER_CUBE, tf("cubeofannihilation.png"));
        TEXTURES.put(MobHead.ROVING_CUBE, tf("cubeofannihilation.png"));
        TEXTURES.put(MobHead.ADHERENT, tf("adherent.png"));
        TEXTURES.put(MobHead.DEATH_TOME, mc("enchanting_table_book.png"));
        TEXTURES.put(MobHead.CARMINITE_BROODLING, tf("towerbroodling.png"));
        TEXTURES.put(MobHead.CARMINITE_GHASTGUARD, tf("towerghast.png"));
        TEXTURES.put(MobHead.CARMINITE_GHASTLING, tf("towerghast.png"));
        TEXTURES.put(MobHead.CARMINITE_GOLEM, tf("carminitegolem.png"));
        TEXTURES.put(MobHead.TOWERWOOD_BORER, tf("towertermite.png"));
        TEXTURES.put(MobHead.SKELETON_DRUID, tf("skeletondruid.png"));
        TEXTURES.put(MobHead.TROLL, tf("troll.png"));
        TEXTURES.put(MobHead.HEDGE_SPIDER, tf("hedgespider.png"));
        TEXTURES.put(MobHead.KING_SPIDER, tf("kingspider.png"));
        TEXTURES.put(MobHead.SWARM_SPIDER, tf("swarmspider.png"));
        TEXTURES.put(MobHead.FIRE_BEETLE, tf("firebeetle.png"));
        TEXTURES.put(MobHead.MOSQUITO_SWARM, tf("mosquitoswarm.png"));
        TEXTURES.put(MobHead.TINY_BIRD, tf("tinybirdred.png"));
        TEXTURES.put(MobHead.WINTER_WOLF, tf("winterwolf.png"));
        TEXTURES.put(MobHead.HOSTILE_WOLF, mc("wolf/wolf.png"));
        TEXTURES.put(MobHead.MIST_WOLF, tf("mistwolf.png"));
        TEXTURES.put(MobHead.STABLE_ICE_CORE, tf("iceshooter.png"));
        TEXTURES.put(MobHead.UNSTABLE_ICE_CORE, tf("iceexploder.png"));
        TEXTURES.put(MobHead.ICE_CRYSTAL, tf("icecrystal.png"));
        TEXTURES.put(MobHead.WRAITH, tf("ghost.png"));
        TEXTURES.put(MobHead.YETI, tf("yeti2.png"));
        TEXTURES.put(MobHead.RAVEN, tf("raven.png"));
        TEXTURES.put(MobHead.BIGHORN_SHEEP, tf("bighorn.png"));
        TEXTURES.put(MobHead.BOAR, tf("wildboar.png"));
        TEXTURES.put(MobHead.DEER, tf("wilddeer.png"));
        TEXTURES.put(MobHead.DWARF_RABBIT, tf("bunnywhite.png"));
        TEXTURES.put(MobHead.SQUIRREL, tf("squirrel2.png"));
        TEXTURES.put(MobHead.PENGUIN, tf("penguin.png"));
    }

    /** Twilight Forest entity texture; the 1.20.1 fork keeps them in {@code textures/model/}. */
    private static ResourceLocation tf(String name) {
        return new ResourceLocation("twilightforest", "textures/model/" + name);
    }

    /** Vanilla entity texture. */
    private static ResourceLocation mc(String name) {
        return new ResourceLocation("minecraft", "textures/entity/" + name);
    }

    /** A Twilight Forest model layer by name (layer "main"). */
    private static ModelLayerLocation classic(String path) {
        return new ModelLayerLocation(new ResourceLocation("twilightforest", path), "main");
    }

    // ===== bounding boxes (reflection on private ModelPart fields) =====

    private static float[] newBounds() {
        return new float[] {
                Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE,
                -Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE
        };
    }

    /**
     * Full box: cube coordinates plus every pivot on the way (the part's own pivot included), child
     * rotations <b>ignored</b>. Used for the scale height and vertical placement.
     */
    private static void collectBounds(ModelPart part, float tx, float ty, float tz, float[] bounds) {
        float ox = tx + part.x;
        float oy = ty + part.y;
        float oz = tz + part.z;
        for (ModelPart.Cube cube : getCubes(part)) {
            bounds[0] = Math.min(bounds[0], cube.minX + ox);
            bounds[1] = Math.min(bounds[1], cube.minY + oy);
            bounds[2] = Math.min(bounds[2], cube.minZ + oz);
            bounds[3] = Math.max(bounds[3], cube.maxX + ox);
            bounds[4] = Math.max(bounds[4], cube.maxY + oy);
            bounds[5] = Math.max(bounds[5], cube.maxZ + oz);
        }
        for (ModelPart child : getChildren(part).values()) {
            collectBounds(child, ox, oy, oz, bounds);
        }
    }

    /** Only the part's own cubes (the head shell), no children: used for horizontal centring. */
    private static void collectDirectCubes(ModelPart part, float tx, float ty, float tz, float[] bounds) {
        float ox = tx + part.x;
        float oy = ty + part.y;
        float oz = tz + part.z;
        for (ModelPart.Cube cube : getCubes(part)) {
            bounds[0] = Math.min(bounds[0], cube.minX + ox);
            bounds[1] = Math.min(bounds[1], cube.minY + oy);
            bounds[2] = Math.min(bounds[2], cube.minZ + oz);
            bounds[3] = Math.max(bounds[3], cube.maxX + ox);
            bounds[4] = Math.max(bounds[4], cube.maxY + oy);
            bounds[5] = Math.max(bounds[5], cube.maxZ + oz);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ModelPart.Cube> getCubes(ModelPart part) {
        try {
            return (List<ModelPart.Cube>) CUBES_FIELD.get(part);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read ModelPart.cubes", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ModelPart> getChildren(ModelPart part) {
        try {
            return (Map<String, ModelPart>) CHILDREN_FIELD.get(part);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot read ModelPart.children", e);
        }
    }
}
