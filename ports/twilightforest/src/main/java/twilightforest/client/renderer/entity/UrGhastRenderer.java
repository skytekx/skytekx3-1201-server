package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.entity.UrGhastModel;
import twilightforest.entity.boss.UrGhast;

public class UrGhastRenderer extends MobRenderer<UrGhast, UrGhastModel> {

	private final ResourceLocation textureLocClosed = TwilightForestMod.getModelTexture("towerboss.png");
	private final ResourceLocation textureLocOpen   = TwilightForestMod.getModelTexture("towerboss_openeyes.png");
	private final ResourceLocation textureLocAttack = TwilightForestMod.getModelTexture("towerboss_fire.png");
	private final float scale;

	public UrGhastRenderer(EntityRendererProvider.Context manager, UrGhastModel model, float shadowSize, float scale) {
		super(manager, model, shadowSize);
		this.scale = scale;
	}

	@Override
	public ResourceLocation getTextureLocation(UrGhast entity) {
		return switch (entity.isCharging() || entity.isDeadOrDying() ? 2 : entity.getAttackStatus()) {
			case 1 -> textureLocOpen;
			case 2 -> textureLocAttack;
			default -> textureLocClosed;
		};
	}

	// [VanillaCopy] of the Carminite Ghastguard renderer's scaling, which the Ur-Ghast used before it stopped extending the Ghastguard
	@Override
	protected void scale(UrGhast ghast, PoseStack stack, float partialTicks) {
		int attackTimer = ghast.getAttackTimer();
		int prevAttackTimer = ghast.getPrevAttackTimer();
		float scaleVariable = (prevAttackTimer + (attackTimer - prevAttackTimer) * partialTicks) / 20.0F;
		if (scaleVariable < 0.0F) {
			scaleVariable = 0.0F;
		}

		scaleVariable = 1.0F / (scaleVariable * scaleVariable * scaleVariable * scaleVariable * scaleVariable * 2.0F + 1.0F);
		float yScale = (this.scale + scaleVariable) / 2.0F;
		float xzScale = (this.scale + 1.0F / scaleVariable) / 2.0F;
		stack.scale(xzScale, yScale, xzScale);
	}

	@Override
	public boolean shouldRender(UrGhast ghast, Frustum camera, double camX, double camY, double camZ) {
		if (ghast.deathTime > UrGhast.DEATH_ANIMATION_DURATION / 3) return false;
		return super.shouldRender(ghast, camera, camX, camY, camZ);
	}

	@Override
	protected float getFlipDegrees(UrGhast ghast) { //Prevent the body from keeling over
		return ghast.isDeadOrDying() ? 0.0F : super.getFlipDegrees(ghast);
	}
}
