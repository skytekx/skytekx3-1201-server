package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.entity.HydraModel;
import twilightforest.entity.boss.Hydra;

public class HydraRenderer extends MobRenderer<Hydra, HydraModel> {

	private static final ResourceLocation textureLoc = TwilightForestMod.getModelTexture("hydra4.png");

	public HydraRenderer(EntityRendererProvider.Context manager, HydraModel modelbase, float shadowSize) {
		super(manager, modelbase, shadowSize);
	}

	@Override
	protected float getFlipDegrees(Hydra entity) {
		return 0F;
	}

	// [VanillaCopy] of LivingEntityRenderer.setupRotations without the spin attack and sleeping poses; an upside-down Hydra is only
	// lifted by 6 blocks instead of its 12 block tall bounding box
	@Override
	protected void setupRotations(Hydra entity, PoseStack stack, float ageInTicks, float rotationYaw, float partialTicks) {
		if (this.isShaking(entity)) {
			rotationYaw += (float) (Math.cos((double) entity.tickCount * 3.25D) * Math.PI * 0.4F);
		}

		if (!entity.hasPose(Pose.SLEEPING)) {
			stack.mulPose(Axis.YP.rotationDegrees(180.0F - rotationYaw));
		}

		if (entity.deathTime > 0) {
			float f = ((float) entity.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
			f = Mth.sqrt(f);
			if (f > 1.0F) {
				f = 1.0F;
			}

			stack.mulPose(Axis.ZP.rotationDegrees(f * this.getFlipDegrees(entity)));
		} else if (isEntityUpsideDown(entity)) {
			stack.translate(0.0F, 6.0F, 0.0F);
			stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}
	}

	@Override
	public ResourceLocation getTextureLocation(Hydra entity) {
		return textureLoc;
	}
}
