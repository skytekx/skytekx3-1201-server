package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.entity.LichModel;
import twilightforest.client.renderer.TFRenderTypes;
import twilightforest.entity.boss.Lich;

public class LichRenderer extends HumanoidMobRenderer<Lich, LichModel> {

	public static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("twilightlich64.png");
	private static final RenderType CLONE_EYES = RenderType.eyes(TwilightForestMod.getModelTexture("twilightlich64_eyes.png"));

	private final float baseShadowRadius;

	public LichRenderer(EntityRendererProvider.Context manager, LichModel modelbiped, float shadowSize) {
		super(manager, modelbiped, shadowSize);
		this.baseShadowRadius = shadowSize;
		this.addLayer(new ShieldLayer<>(this));
		// shadow clones glare with red eyes (4.8 4fe947319), hidden while they are invisible after a teleport
		this.addLayer(new EyesLayer<>(this) {
			@Override
			public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Lich lich, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
				if (lich.isShadowClone() && !lich.isInvisible()) super.render(poseStack, buffer, packedLight, lich, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
			}

			@Override
			public RenderType renderType() {
				return CLONE_EYES;
			}
		});
	}

	/**
	 * Shadow clones are drawn see-through (4.8 7c465f29d).
	 */
	@Nullable
	@Override
	protected RenderType getRenderType(Lich lich, boolean bodyVisible, boolean translucent, boolean glowing) {
		if (lich.isShadowClone() && bodyVisible) return TFRenderTypes.SHADOW_CLONE;
		else return super.getRenderType(lich, bodyVisible, translucent, glowing);
	}

	@Override
	protected boolean isShaking(Lich lich) {
		return super.isShaking(lich) || (lich.isDeadOrDying() && lich.deathTime <= Lich.DEATH_ANIMATION_POINT_A);
	}

	@Override
	public void render(Lich lich, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
		// 1.20.1 has no per-entity shadow radius hook; the dispatcher reads this field right after render()
		this.shadowRadius = lich.isShadowClone() || lich.deathTime > Lich.DEATH_ANIMATION_POINT_A ? 0.0F : this.baseShadowRadius;

		stack.pushPose();
		stack.scale(1.125F, 1.125F, 1.125F);
		if (lich.deathTime > 0) {
			if (lich.deathTime > Lich.DEATH_ANIMATION_POINT_A) {
				// the crown drops to the floor
				stack.translate(0.0D, -1.8D * Math.pow(Math.min(((float) (lich.deathTime - Lich.DEATH_ANIMATION_POINT_A) + partialTicks) / (float) (Lich.DEATH_ANIMATION_POINT_B - Lich.DEATH_ANIMATION_POINT_A), 1.0D), 3.0D), 0.0D);
			} else {
				float time = (float) lich.deathTime + partialTicks;
				stack.translate(Math.sin(time * time) * 0.01D, 0.0D, Math.cos(time * time) * 0.01D);
			}
		}
		super.render(lich, entityYaw, partialTicks, stack, buffer, packedLight);
		stack.popPose();
	}

	@Override
	protected float getFlipDegrees(Lich lich) { //Prevent the body from keeling over
		return lich.isDeadOrDying() ? 0.0F : super.getFlipDegrees(lich);
	}

	@Override
	public ResourceLocation getTextureLocation(Lich lich) {
		return TEXTURE;
	}
}
