package twilightforest.client.renderer.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import twilightforest.block.entity.spawner.SinisterSpawnerBlockEntity;

// [VANILLA COPY] SpawnerRenderer (Type bound changed to SinisterSpawnerBlockEntity)
// The wider render bounding box 4.8 sets here lives on SinisterSpawnerBlockEntity, where Forge 1.20.1 reads it
@OnlyIn(Dist.CLIENT)
public class SinisterSpawnerRenderer implements BlockEntityRenderer<SinisterSpawnerBlockEntity> {
	private final EntityRenderDispatcher entityRenderer;

	public SinisterSpawnerRenderer(BlockEntityRendererProvider.Context context) {
		this.entityRenderer = context.getEntityRenderer();
	}

	@Override
	public void render(SinisterSpawnerBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
		Level level = blockEntity.getLevel();
		if (level != null) {
			poseStack.pushPose();
			poseStack.translate(0.5F, 0.0F, 0.5F);
			BaseSpawner basespawner = blockEntity.getSpawner();
			Entity entity = basespawner.getOrCreateDisplayEntity(level, level.getRandom(), blockEntity.getBlockPos());
			if (entity != null) {
				float scale = 0.53125F;
				float size = Math.max(entity.getBbWidth(), entity.getBbHeight());
				if ((double) size > 1.0D) {
					scale /= size;
				}

				poseStack.translate(0.0F, 0.4F, 0.0F);
				poseStack.mulPose(Axis.YP.rotationDegrees((float) Mth.lerp(partialTick, basespawner.getoSpin(), basespawner.getSpin()) * 10.0F));
				poseStack.translate(0.0F, -0.2F, 0.0F);
				poseStack.mulPose(Axis.XP.rotationDegrees(-30.0F));
				poseStack.scale(scale, scale, scale);
				this.entityRenderer.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTick, poseStack, bufferSource, packedLight);
			}

			poseStack.popPose();
		}
	}
}
