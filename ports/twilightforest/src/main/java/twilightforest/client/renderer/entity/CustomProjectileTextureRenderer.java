package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import twilightforest.entity.projectile.TFThrowable;

/**
 * This renderer serves as a way to render item textures on a projectile without needing an actual item registered for it.
 * Consider using {@link net.minecraft.client.renderer.entity.ThrownItemRenderer} if your projectile is an existing item already.
 * <p>
 * Optionally scaled, drawn full bright, and flashing and pulsing like a creeper about to explode (the Lich's projectiles,
 * 4.8 93edc2bb2 and e65e3a651).
 */
public class CustomProjectileTextureRenderer extends EntityRenderer<TFThrowable> {

	private final ResourceLocation texture;
	private final float scale;
	private final boolean fullBright;
	private final boolean flashing;

	public CustomProjectileTextureRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture, float scale, boolean fullBright, boolean flashing) {
		super(ctx);
		this.texture = texture;
		this.scale = scale;
		this.fullBright = fullBright;
		this.flashing = flashing;
	}

	public CustomProjectileTextureRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture) {
		this(ctx, texture, 1.0F, false, false);
	}

	@Override
	protected int getBlockLightLevel(TFThrowable entity, BlockPos pos) {
		return this.fullBright ? 15 : super.getBlockLightLevel(entity, pos);
	}

	@Override
	public void render(TFThrowable entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		if (this.flashing) {
			stack.pushPose();
			float age = entity.tickCount + partialTicks;
			float f = (Mth.sin(age) + 1.0F) * 0.5F;
			float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
			f = Mth.clamp(f, 0.0F, 1.0F);
			f *= f;
			f *= f;
			float f2 = (1.0F + f * 0.4F) * f1;
			float f3 = (1.0F + f * 0.1F) / f1;
			stack.scale(f2, f3, f2);
			this.render(entity, entityYaw, partialTicks, stack, buffer, light, OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(false)));
			stack.popPose();
		} else {
			this.render(entity, entityYaw, partialTicks, stack, buffer, light, OverlayTexture.NO_OVERLAY);
		}
	}

	//[VanillaCopy] of DragonFireballRender.render, we just input our own texture stuff instead
	public void render(TFThrowable entity, float entityYaw, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlayTexture) {
		ms.pushPose();
		ms.scale(0.5F * this.scale, 0.5F * this.scale, 0.5F * this.scale);
		ms.mulPose(this.entityRenderDispatcher.cameraOrientation());
		ms.mulPose(Axis.YP.rotationDegrees(180.0F));
		PoseStack.Pose posestack$pose = ms.last();
		Matrix4f matrix4f = posestack$pose.pose();
		Matrix3f matrix3f = posestack$pose.normal();
		VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.texture));
		vertex(vertexconsumer, matrix4f, matrix3f, light, 0.0F, 0, 0, 1, overlayTexture);
		vertex(vertexconsumer, matrix4f, matrix3f, light, 1.0F, 0, 1, 1, overlayTexture);
		vertex(vertexconsumer, matrix4f, matrix3f, light, 1.0F, 1, 1, 0, overlayTexture);
		vertex(vertexconsumer, matrix4f, matrix3f, light, 0.0F, 1, 0, 0, overlayTexture);
		ms.popPose();
		super.render(entity, entityYaw, partialTicks, ms, buffer, light);
	}

	private static void vertex(VertexConsumer p_114090_, Matrix4f p_114091_, Matrix3f p_114092_, int p_114093_, float p_114094_, int p_114095_, int p_114096_, int p_114097_, int overlay) {
		p_114090_.vertex(p_114091_, p_114094_ - 0.5F, (float)p_114095_ - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float)p_114096_, (float)p_114097_).overlayCoords(overlay).uv2(p_114093_).normal(p_114092_, 0.0F, 1.0F, 0.0F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(TFThrowable entity) {
		return this.texture;
	}
}
