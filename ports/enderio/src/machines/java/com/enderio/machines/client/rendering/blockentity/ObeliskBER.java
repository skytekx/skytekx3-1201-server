package com.enderio.machines.client.rendering.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Renders an item that turns to the player above an obelisk.
 * SkyTekx: any block entity, as 84bfe2477 widened it for the Mind Killer.
 */
public class ObeliskBER<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private final Item obeliskRenderItem;

    public ObeliskBER(BlockEntityRendererProvider.Context context, Item renderItem) {
        this.obeliskRenderItem = renderItem;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.75, 0.5);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        Minecraft minecraft = Minecraft.getInstance();
        Vec3 position = minecraft.player.position();
        float f1 = (float) (Mth.atan2(position.z - blockEntity.getBlockPos().getZ() - 0.5D, position.x - blockEntity.getBlockPos().getX() - 0.5D) * 180.0f / Math.PI + 90);
        poseStack.mulPose(Axis.YP.rotationDegrees(-f1));
        ItemStack stack = new ItemStack(obeliskRenderItem);
        BakedModel bakedmodel = minecraft.getItemRenderer().getModel(stack, blockEntity.getLevel(), null, 0);
        if (stack.getItem() instanceof BlockItem) { // Blocks are in iso, let's correct that (84bfe2477)
            poseStack.mulPose(Axis.YP.rotationDegrees(45));
            poseStack.mulPose(Axis.XP.rotationDegrees(-30));
            poseStack.scale(1.2f, 1.2f, 1.2f);
        }
        minecraft.getItemRenderer()
            .render(stack, ItemDisplayContext.GUI, true, poseStack, buffer, packedLight, packedOverlay, bakedmodel);
        poseStack.popPose();
    }
}
