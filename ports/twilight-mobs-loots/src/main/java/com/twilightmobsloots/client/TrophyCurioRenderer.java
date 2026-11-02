package com.twilightmobsloots.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.twilightmobsloots.MobTrophyItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

/**
 * Curios slot renderer that copies vanilla {@code CustomHeadLayer.translateToHead}, so a trophy in a Curios
 * slot (head or trophy) looks exactly like a trophy worn in the vanilla head slot.
 *
 * <p>It renders through {@code ItemRenderer.renderStatic(stack, ItemDisplayContext.HEAD)}, which reaches the
 * item's own BEWLR ({@link MobTrophyISTER}), the same pipeline as wearing it. Transform sequence (player, so
 * without the villager offset):
 * <pre>
 * humanoid.head.translateAndRotate(pose)   // follow the head
 * translate(0, -0.25, 0)
 * mulPose(Y 180)
 * scale(0.625, -0.625, -0.625)
 * </pre>
 */
public class TrophyCurioRenderer implements ICurioRenderer {

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack, SlotContext slotContext, PoseStack pose,
            RenderLayerParent<T, M> renderLayerParent, MultiBufferSource buffer,
            int light, float limbSwing, float limbSwingAmount, float partialTick,
            float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(renderLayerParent.getModel() instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        Item item = stack.getItem();
        if (!(item instanceof MobTrophyItem) && !isTwilightForestTrophy(item)) {
            return;
        }
        pose.pushPose();
        humanoid.head.translateAndRotate(pose);
        pose.translate(0.0F, -0.25F, 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.scale(0.625F, -0.625F, -0.625F);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.HEAD, light, OverlayTexture.NO_OVERLAY,
                pose, buffer, slotContext.entity().level(), 0);
        pose.popPose();
    }

    /** Whether the item is a Twilight Forest trophy ({@code twilightforest:*_trophy}). */
    private static boolean isTwilightForestTrophy(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key.getNamespace().equals("twilightforest") && key.getPath().endsWith("_trophy");
    }
}
