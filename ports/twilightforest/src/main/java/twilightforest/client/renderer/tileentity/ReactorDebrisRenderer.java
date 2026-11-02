package twilightforest.client.renderer.tileentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import twilightforest.block.entity.ReactorDebrisBlockEntity;

/**
 * Draws Carminite Reactor debris as a box with the size and per-face textures stored in its block entity
 * (backported from 4.8, 67bbcd9fb).
 */
public class ReactorDebrisRenderer implements BlockEntityRenderer<ReactorDebrisBlockEntity> {

	public ReactorDebrisRenderer(BlockEntityRendererProvider.Context context) {
	}

	private enum Axis {
		X, Y, Z
	}

	private record QuadRenderInfo(VertexConsumer builder, Matrix4f matrix, Matrix3f normalMatrix, int light, int overlay) {  // Reduce copy-paste, thanks to TFC for idea
		private void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
			// 1.20.1 vertex consumers need the elements in format order (NEW_ENTITY: position, colour, uv, overlay, light, normal).
			// Unlike 1.21, 1.20.1 keeps the camera rotation in the PoseStack and sets the diffuse lights up in view space,
			// so the normal has to go through the pose's normal matrix or the shading changes as the camera turns.
			this.builder.vertex(this.matrix, x, y, z)
				.color(1.0F, 1.0F, 1.0F, 1.0F)
				.uv(u, v)
				.overlayCoords(this.overlay)
				.uv2(this.light)
				.normal(this.normalMatrix, nx, ny, nz)
				.endVertex();
		}
	}

	@Override
	public void render(ReactorDebrisBlockEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
		if (entity.getLevel() == null) return;

		poseStack.pushPose();
		VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS));
		Matrix4f matrix = poseStack.last().pose();
		QuadRenderInfo info = new QuadRenderInfo(builder, matrix, poseStack.last().normal(), light, overlay);

		renderBlock(info, entity);
		poseStack.popPose();
	}

	private void renderBlock(QuadRenderInfo info, ReactorDebrisBlockEntity entity) {
		float minX = entity.minPos.x;
		float minY = entity.minPos.y;
		float minZ = entity.minPos.z;
		float maxX = entity.maxPos.x;
		float maxY = entity.maxPos.y;
		float maxZ = entity.maxPos.z;

		TextureAtlasSprite[] sprites = new TextureAtlasSprite[entity.textures.length];
		for (int i = 0; i < sprites.length; i++) {
			sprites[i] = getSprite(entity.textures[i]);
		}

		renderSide(info, sprites[0], Axis.X, minX, minY, minZ, maxY, maxZ, -1);
		renderSide(info, sprites[1], Axis.X, maxX, minY, maxZ, maxY, minZ, 1);
		renderSide(info, sprites[2], Axis.Y, minY, minX, maxZ, maxX, minZ, -1);
		renderSide(info, sprites[3], Axis.Y, maxY, minX, minZ, maxX, maxZ, 1);
		renderSide(info, sprites[4], Axis.Z, minZ, minX, minY, maxX, maxY, -1);
		renderSide(info, sprites[5], Axis.Z, maxZ, maxX, minY, minX, maxY, 1);

		// Duplication for inner side because portal is transparent
		renderSide(info, sprites[0], Axis.X, minX, minY, maxZ, maxY, minZ, 1);
		renderSide(info, sprites[1], Axis.X, maxX, minY, minZ, maxY, maxZ, -1);
		renderSide(info, sprites[2], Axis.Y, minY, minX, minZ, maxX, maxZ, 1);
		renderSide(info, sprites[3], Axis.Y, maxY, minX, maxZ, maxX, minZ, -1);
		renderSide(info, sprites[4], Axis.Z, minZ, maxX, minY, minX, maxY, 1);
		renderSide(info, sprites[5], Axis.Z, maxZ, minX, minY, maxX, maxY, -1);
	}

	private void renderSide(QuadRenderInfo info, TextureAtlasSprite sprite, Axis axis, float c1, float min1, float min2, float max1, float max2, float n) {
		float u0 = Mth.lerp(min1, sprite.getU0(), sprite.getU1());
		float v0 = Mth.lerp(min2, sprite.getV0(), sprite.getV1());
		float u1 = Mth.lerp(max1, sprite.getU0(), sprite.getU1());
		float v1 = Mth.lerp(max2, sprite.getV0(), sprite.getV1());

		switch (axis) {
			case X -> {
				info.vertex(c1, min1, min2, u0, v0, n, 0, 0);
				info.vertex(c1, min1, max2, u0, v1, n, 0, 0);
				info.vertex(c1, max1, max2, u1, v1, n, 0, 0);
				info.vertex(c1, max1, min2, u1, v0, n, 0, 0);
			}
			case Y -> {
				info.vertex(min1, c1, min2, u0, v0, 0, n, 0);
				info.vertex(min1, c1, max2, u0, v1, 0, n, 0);
				info.vertex(max1, c1, max2, u1, v1, 0, n, 0);
				info.vertex(max1, c1, min2, u1, v0, 0, n, 0);
			}
			case Z -> {
				info.vertex(min1, min2, c1, u0, v0, 0, 0, n);
				info.vertex(min1, max2, c1, u0, v1, 0, 0, n);
				info.vertex(max1, max2, c1, u1, v1, 0, 0, n);
				info.vertex(max1, min2, c1, u1, v0, 0, 0, n);
			}
		}
	}

	/**
	 * Looks the sprite up in the block atlas every time instead of caching it like upstream does, so the debris does
	 * not keep stale sprites after a resource reload.
	 */
	public static TextureAtlasSprite getSprite(@Nullable ResourceLocation location) {
		return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
			.apply(location == null ? ReactorDebrisBlockEntity.DEFAULT_TEXTURE : location);
	}
}
