package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.RisingZombieModel;
import twilightforest.entity.monster.RisingZombie;

public class RisingZombieRenderer extends TFBipedRenderer<RisingZombie, RisingZombieModel> {

	private static final float SHADOW_RADIUS = 0.5F;

	public RisingZombieRenderer(EntityRendererProvider.Context context) {
		super(context, new RisingZombieModel(context.bakeLayer(TFModelLayers.RISING_ZOMBIE)), new RisingZombieModel(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)), new RisingZombieModel(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)), SHADOW_RADIUS, "textures/entity/zombie/zombie.png");
	}

	@Override
	public void render(RisingZombie entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		// 1.20.1 has no per-entity getShadowRadius; the dispatcher reads the field right after render(), so grow it with the zombie here
		this.shadowRadius = SHADOW_RADIUS * (entity.getRisingTicks() / (float) RisingZombie.RISE_TICKS);
		super.render(entity, entityYaw, partialTicks, stack, buffer, light);
	}

	@Override
	protected @Nullable RenderType getRenderType(RisingZombie entity, boolean bodyVisible, boolean translucent, boolean glowing) {
		// Render normally for spectators instead of very translucent
		if (translucent) return this.model.renderType(this.getTextureLocation(entity));
		return super.getRenderType(entity, bodyVisible, translucent, glowing);
	}

	@Override
	protected void setupRotations(RisingZombie entity, PoseStack stack, float bob, float yBodyRot, float partialTick) {
		super.setupRotations(entity, stack, bob, yBodyRot, partialTick);
		int tick = entity.getRisingTicks();
		stack.translate(0.0F, -(80.0F - Math.min(80.0F, tick)) / 80.0F, 0.0F);
		stack.translate(0.0F, -(40.0F - Math.min(40.0F, Math.max(0.0F, tick - 80.0F))) / 40.0F, 0.0F);
		final float yOff = 1.0F;
		stack.translate(0.0F, yOff, 0.0F);
		stack.mulPose(Axis.XP.rotationDegrees(120.0F * (80.0F - Math.min(80.0F, tick)) / 80.0F));
		stack.mulPose(Axis.XP.rotationDegrees(-30.0F * (40.0F - Math.min(40.0F, Math.max(0.0F, tick - 80.0F))) / 40.0F));
		stack.translate(0.0F, -yOff, 0.0F);
	}

	@Override
	protected float getFlipDegrees(RisingZombie entity) {
		return 0.0F;
	}
}
