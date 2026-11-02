package twilightforest.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import twilightforest.TwilightForestMod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Draws entities and item entities in recipe viewer GUIs, shared by the JEI and EMI plugins (4.8 keeps this in
 * twilightforest.util.entities). Client only.
 * <p>
 * The drawing itself is the 1.20.1 code the JEI renderers already used: it goes through the model view stack, which
 * 1.20.1's entity rendering still reads, instead of 4.8's 1.21 GuiGraphics-only path.
 */
public class EntityRenderingUtil {

	private static final Set<EntityType<?>> IGNORED_ENTITIES = new HashSet<>();
	// The entities are created in the client level, so they are dropped whenever a screen closes (TFClientEvents) and
	// never outlive the level they were made for (#2488)
	private static final Map<EntityType<?>, Entity> ENTITY_MAP = new HashMap<>();

	public static void clearEntityCache() {
		if (!ENTITY_MAP.isEmpty()) ENTITY_MAP.clear();
	}

	@Nullable
	public static Entity fetchEntity(EntityType<?> type, @Nullable Level level) {
		if (level != null && !IGNORED_ENTITIES.contains(type)) {
			// players cannot be created using the type, but we can use the client player
			// side effect is it renders armor/items
			if (type == EntityType.PLAYER) {
				return Minecraft.getInstance().player;
			}
			return ENTITY_MAP.computeIfAbsent(type, t -> {
				Entity created = t.create(level);
				if (created != null) {
					created.setYRot(0.0F);
					created.setYHeadRot(0.0F);
					created.setYBodyRot(0.0F);
					created.hasImpulse = false;
					if (created instanceof Mob mob) {
						mob.setNoAi(true);
					}
				}
				return created;
			});
		}
		return null;
	}

	/**
	 * Draws the entity in a {@code size} by {@code size} square at the origin of the current GUI pose.
	 */
	public static void renderEntity(GuiGraphics graphics, EntityType<?> type, int size) {
		Level level = Minecraft.getInstance().level;
		if (level == null || IGNORED_ENTITIES.contains(type)) return;

		Entity entity = fetchEntity(type, level);
		// only can draw living entities, plus non-living ones don't get recipes anyways
		if (entity instanceof LivingEntity livingEntity) {
			// scale down large mobs, but don't scale up small ones
			int scale = size / 2;
			float height = entity.getBbHeight();
			float width = entity.getBbWidth();
			if (height > 2.25F || width > 2.25F) {
				scale = (int) (20 / Math.max(height, width));
			}
			// catch exceptions drawing the entity to be safe, any caught exceptions blacklist the entity
			try {
				PoseStack modelView = RenderSystem.getModelViewStack();
				modelView.pushPose();
				modelView.mulPoseMatrix(graphics.pose().last().pose());
				renderTheEntity(size / 2, size - 2, scale, livingEntity);
				modelView.popPose();
				RenderSystem.applyModelViewMatrix();
			} catch (Exception e) {
				TwilightForestMod.LOGGER.error("Error drawing entity " + BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
				IGNORED_ENTITIES.add(type);
				ENTITY_MAP.remove(type);
			}
		} else {
			// not living, so might as well skip next time
			IGNORED_ENTITIES.add(type);
			ENTITY_MAP.remove(type);
		}
	}

	//[VanillaCopy] of InventoryScreen.renderEntityInInventory, with added rotations and some other modified values
	private static void renderTheEntity(int x, int y, int scale, LivingEntity entity) {
		PoseStack posestack = RenderSystem.getModelViewStack();
		posestack.pushPose();
		posestack.translate(x, y, 1050.0D);
		if (entity.getType() == EntityType.GHAST) posestack.translate(0.0D, -8.5D, 0.0D);
		posestack.scale(1.0F, 1.0F, -1.0F);
		RenderSystem.applyModelViewMatrix();
		PoseStack posestack1 = new PoseStack();
		posestack1.translate(0.0D, 0.0D, 1000.0D);
		posestack1.scale((float) scale, (float) scale, (float) scale);
		Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
		Quaternionf quaternion1 = Axis.XP.rotationDegrees(20.0F);
		quaternion.mul(quaternion1);
		posestack1.mulPose(quaternion);
		posestack1.mulPose(Axis.XN.rotationDegrees(35.0F));
		posestack1.mulPose(Axis.YN.rotationDegrees(145.0F));
		float f2 = entity.yBodyRot;
		float f3 = entity.getYRot();
		float f4 = entity.getXRot();
		float f5 = entity.yHeadRotO;
		float f6 = entity.yHeadRot;
		entity.yBodyRot = 0.0F;
		entity.setYRot(0.0F);
		entity.setXRot(0.0F);
		entity.yHeadRot = entity.getYRot();
		entity.yHeadRotO = entity.getYRot();
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		quaternion1.conjugate();
		entityrenderdispatcher.overrideCameraOrientation(quaternion1);
		entityrenderdispatcher.setRenderShadow(false);
		MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderSystem.runAsFancy(() -> entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880));
		multibuffersource$buffersource.endBatch();
		entityrenderdispatcher.setRenderShadow(true);
		entity.yBodyRot = f2;
		entity.setYRot(f3);
		entity.setXRot(f4);
		entity.yHeadRotO = f5;
		entity.yHeadRot = f6;
		posestack.popPose();
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();
	}

	/**
	 * Draws the stack as a bobbing, spinning item entity in a 32 by 32 square at the origin of the current GUI pose.
	 */
	public static void renderItemEntity(GuiGraphics graphics, ItemStack stack, @Nullable Level level, float bobOffset) {
		if (!(fetchEntity(EntityType.ITEM, level) instanceof ItemEntity item)) return;
		item.setItem(stack);

		PoseStack modelView = RenderSystem.getModelViewStack();
		modelView.pushPose();
		modelView.mulPoseMatrix(graphics.pose().last().pose());

		PoseStack posestack = RenderSystem.getModelViewStack();
		posestack.pushPose();
		posestack.translate(16.0D, 32.0D, 1050.0D);
		posestack.scale(1.0F, 1.0F, -1.0F);
		RenderSystem.applyModelViewMatrix();
		PoseStack posestack1 = new PoseStack();
		posestack1.translate(0.0D, 0.0D, 1000.0D);
		posestack1.scale(50.0F, 50.0F, 50.0F);
		Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
		Quaternionf quaternion1 = Axis.XP.rotationDegrees(20.0F);
		quaternion.mul(quaternion1);
		posestack1.mulPose(quaternion);
		posestack1.mulPose(Axis.XN.rotationDegrees(35.0F));
		posestack1.mulPose(Axis.YN.rotationDegrees(145.0F));
		Lighting.setupForEntityInInventory();
		quaternion1.conjugate();
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		RenderSystem.runAsFancy(() -> render(item, Minecraft.getInstance().getDeltaFrameTime(), posestack1, buffer, 15728880, bobOffset));
		buffer.endBatch();
		posestack.popPose();
		RenderSystem.applyModelViewMatrix();
		Lighting.setupFor3DItems();

		modelView.popPose();
		RenderSystem.applyModelViewMatrix();
	}

	//[VanillaCopy] of ItemEntityRenderer.render. I have to add my own bob offset and ticker since using the vanilla method has issues
	private static void render(ItemEntity entity, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light, float bobOffset) {
		stack.pushPose();
		ItemStack itemstack = entity.getItem();
		BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(itemstack, entity.level(), null, entity.getId());
		float f1 = Mth.sin((Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() + partialTicks) / 10.0F + bobOffset) * 0.1F + 0.1F;
		float f2 = bakedmodel.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
		stack.translate(0.0D, f1 + 0.25F * f2, 0.0D);
		float f3 = getSpin(partialTicks, bobOffset);
		stack.mulPose(Axis.YP.rotation(f3));

		stack.pushPose();

		Minecraft.getInstance().getItemRenderer().render(itemstack, ItemDisplayContext.GROUND, false, stack, buffer, light, OverlayTexture.NO_OVERLAY, bakedmodel);
		stack.popPose();


		stack.popPose();
	}

	private static float getSpin(float partialTicks, float bobOffset) {
		return (Objects.requireNonNull(Minecraft.getInstance().level).getGameTime() + partialTicks) / 20.0F + bobOffset;
	}

	public static List<Component> getMobTooltip(EntityType<?> type) {
		List<Component> components = new ArrayList<>();
		components.add(type.getDescription());
		if (Minecraft.getInstance().options.advancedItemTooltips) {
			components.add(Component.literal(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString()).withStyle(ChatFormatting.DARK_GRAY));
		}
		return components;
	}

	public static String getModIdForTooltip(String modId) {
		return ModList.get().getModContainerById(modId)
			.map(ModContainer::getModInfo)
			.map(IModInfo::getDisplayName)
			.orElseGet(() -> StringUtils.capitalize(modId));
	}
}
