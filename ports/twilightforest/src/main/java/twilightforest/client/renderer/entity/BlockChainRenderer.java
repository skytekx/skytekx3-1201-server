package twilightforest.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.ChainModel;
import twilightforest.client.model.entity.SpikeBlockModel;
import twilightforest.entity.ChainBlock;

public class BlockChainRenderer extends EntityRenderer<ChainBlock> {

	private static final ResourceLocation textureLoc = TwilightForestMod.getModelTexture("blockgoblin.png");
	private final Model model;
	private final Model chainModel;

	public BlockChainRenderer(EntityRendererProvider.Context manager) {
		super(manager);
		model = new SpikeBlockModel(manager.bakeLayer(TFModelLayers.CHAIN_BLOCK));
		chainModel = new ChainModel(manager.bakeLayer(TFModelLayers.CHAIN));
	}

	@Override
	public void render(ChainBlock chainBlock, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		super.render(chainBlock, yaw, partialTicks, stack, buffer, light);

		stack.pushPose();
		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBufferDirect(buffer, this.model.renderType(textureLoc), false, chainBlock.isFoil());

		stack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, chainBlock.yRotO, chainBlock.getYRot()) - 90.0F));
		stack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, chainBlock.xRotO, chainBlock.getXRot())));

		stack.scale(-1.0F, -1.0F, 1.0F);
		this.model.renderToBuffer(stack, ivertexbuilder, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		stack.popPose();

		// the chain is no longer made of part entities: lay links from the block back to the thrower every 1.5 blocks
		Entity owner = chainBlock.getOwner();
		if (owner != null) {
			stack.pushPose();
			stack.translate(0.0D, chainBlock.getBbHeight() * 0.5D, 0.0D);
			Vec3 xyz = owner.getEyePosition(partialTicks).subtract(chainBlock.getPosition(partialTicks).add(0.0D, owner.getBbHeight() * 0.5D, 0.0D));
			double linkSpacing = 1.5D; // distance between two chain links, in blocks
			double links = xyz.length() / linkSpacing;
			Vec3 offset = xyz.normalize().scale(-linkSpacing);
			int ownerLight = this.entityRenderDispatcher.getPackedLightCoords(owner, partialTicks);
			for (int i = 1; i < links; i++) {
				renderChain(chainBlock, xyz.add(offset.scale(links - i)), stack, buffer, Math.max(light, ownerLight), this.chainModel);
			}
			stack.popPose();
		}
	}

	public static void renderChain(Entity entity, Vec3 offset, PoseStack stack, MultiBufferSource buffer, int light, Model chainModel) {
		stack.pushPose();
		VertexConsumer vertexConsumer;
		if (entity instanceof ChainBlock block) {
			vertexConsumer = ItemRenderer.getFoilBufferDirect(buffer, chainModel.renderType(textureLoc), false, block.isFoil());
		} else {
			vertexConsumer = buffer.getBuffer(chainModel.renderType(textureLoc));
		}

		stack.translate(offset.x(), offset.y(), offset.z());

		stack.scale(-1.0F, -1.0F, 1.0F);
		chainModel.renderToBuffer(stack, vertexConsumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		stack.popPose();
	}

	@Override
	public ResourceLocation getTextureLocation(ChainBlock entity) {
		return textureLoc;
	}
}
