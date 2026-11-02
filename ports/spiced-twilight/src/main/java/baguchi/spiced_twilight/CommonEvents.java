package baguchi.spiced_twilight;

import baguchi.spiced_twilight.api.HelmetCrabHideAccess;
import baguchi.spiced_twilight.entity.ModEntities;
import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.entity.monster.HelmetCrab;
import twilightforest.world.components.structures.start.TFStructureStart;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = SpicedTwilight.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    /**
     * Structures the Maze Slime Beetle joins the monster spawns of. Upstream's biome modifier names this structure tag
     * but NeoForge's add_spawns ignores it; see NOTICE.
     */
    public static final TagKey<Structure> MAZE_SLIME_BEETLE_SPAWN_STRUCTURES = TagKey.create(Registries.STRUCTURE, SpicedTwilight.prefix("labyrinth_maze_slime_spawn"));

    // NaturalSpawner re-checks the picked entry by identity (canSpawnMobAt), so every event must add the same instance
    private static final Supplier<MobSpawnSettings.SpawnerData> MAZE_SLIME_BEETLE_SPAWN = Suppliers.memoize(() -> new MobSpawnSettings.SpawnerData(ModEntities.MAZE_SLIME_BEETLE.get(), 3, 1, 2));

    // upstream: EntityTickEvent.Post driving HelmetCrabHideAttachment.tick
    @SubscribeEvent
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof HelmetCrab helmetCrab) {
            ((HelmetCrabHideAccess) helmetCrab).spicedTwilight$tickHide();
        }
    }

    // upstream: LivingIncomingDamageEvent
    @SubscribeEvent
    public static void onHurt(LivingAttackEvent event) {
        if (event.getEntity() instanceof HelmetCrab helmetCrab && !helmetCrab.level().isClientSide()) {
            // 1.20.1 backport: never swallow /kill or void damage
            if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return;
            }
            HelmetCrabHideAccess hide = (HelmetCrabHideAccess) helmetCrab;
            if (hide.spicedTwilight$isHiding()) {
                event.setCanceled(true);
            } else {
                float amount = event.getAmount();
                float hideChance = amount * 0.1F;

                if (hide.spicedTwilight$getHideChance() + hideChance < helmetCrab.getRandom().nextFloat()) {
                    hide.spicedTwilight$hide();
                } else {
                    hide.spicedTwilight$setHideChance(hide.spicedTwilight$getHideChance() + hideChance);
                }
            }
        }
    }

    /**
     * Adds the Maze Slime Beetle to the monster spawns inside the structures tagged
     * {@code #spiced_twilight:labyrinth_maze_slime_spawn} (the Labyrinth). Twilight Forest replaces the spawn list
     * inside its landmarks, so a biome modifier cannot reach them; this hooks the list after Twilight Forest built it.
     * Like Twilight Forest's own per-piece lookup, it only applies inside one of the structure's pieces (the rest of
     * the bounding box keeps its biome spawns) and leaves pieces without monster spawns (vaults, boss room) empty.
     */
    @SubscribeEvent
    public static void onPotentialSpawns(LevelEvent.PotentialSpawns event) {
        if (event.getMobCategory() != MobCategory.MONSTER || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (Holder<Structure> structure : level.registryAccess().registryOrThrow(Registries.STRUCTURE).getTagOrEmpty(MAZE_SLIME_BEETLE_SPAWN_STRUCTURES)) {
            StructureStart start = level.structureManager().getStructureWithPieceAt(event.getPos(), structure.value());
            if (start.isValid()) {
                // Twilight Forest empties the list in pieces that must not spawn monsters; keep it that way
                if (event.getSpawnerDataList().isEmpty()) {
                    return;
                }
                // a conquered landmark falls back to its biome's spawns
                if (!(start instanceof TFStructureStart tfStart && tfStart.isConquered())) {
                    event.addSpawnerData(MAZE_SLIME_BEETLE_SPAWN.get());
                }
                return;
            }
        }
    }
}
