package com.twilightmobsloots.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.twilightmobsloots.MobHead;
import com.twilightmobsloots.MobTrophyItem;
import com.twilightmobsloots.TwilightMobsLoots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * Mob trophy item renderer (client only).
 *
 * <p>In the GUI: the flat silver icon plus a slowly turning 3D head; everywhere else (hand, ground, head):
 * only the 3D head. Everything goes through {@link MobTrophyRenderer#render}, so the item head matches the
 * placed block. The transforms follow Twilight Forest's own trophy ISTER.
 */
public class MobTrophyISTER extends BlockEntityWithoutLevelRenderer {

    /** Flat item icon drawn under the head in inventories (registered as an additional model). */
    public static final ResourceLocation SILVER_ICON_MODEL =
            new ResourceLocation(TwilightMobsLoots.MOD_ID, "item/trophy_silver");

    private final Map<MobHead, MobTrophyBlockModel> trophies;
    private final ItemRenderer itemRenderer;

    public MobTrophyISTER(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
        this.trophies = MobTrophyBlockModel.createTrophyRenderers(models);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffer, int light, int overlay) {
        if (!(stack.getItem() instanceof MobTrophyItem trophy)) {
            return;
        }
        MobTrophyBlockModel model = this.trophies.get(trophy.getMobHead());
        if (model == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        boolean paused = mc.isPaused();
        float time = paused ? 0.0F
                : (float) (mc.level != null ? mc.level.getGameTime() : 0L) + mc.getDeltaFrameTime();

        if (context == ItemDisplayContext.GUI) {
            // 1) flat silver icon, drawn into the shared GUI buffer and flushed right away
            pose.pushPose();
            Lighting.setupForFlatItems();
            pose.translate(0.5F, 0.5F, -1.5F);
            MultiBufferSource.BufferSource guiBuffer = mc.renderBuffers().bufferSource();
            BakedModel icon = mc.getModelManager().getModel(SILVER_ICON_MODEL);
            this.itemRenderer.render(stack, ItemDisplayContext.GUI, false, pose, guiBuffer,
                    0xF000F0, OverlayTexture.NO_OVERLAY, icon);
            pose.popPose();
            guiBuffer.endBatch();
            Lighting.setupFor3DItems();

            // 2) the head turns around Y above the icon (stops while paused)
            pose.pushPose();
            pose.translate(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.XP.rotationDegrees(30.0F));
            pose.mulPose(Axis.YN.rotationDegrees(paused ? -45.0F : time % 360.0F));
            pose.translate(-0.5F, -0.5F, -0.5F);
            pose.translate(0.0F, 0.25F, 0.0F);
            MobTrophyRenderer.render(null, 180.0F, model, time, pose, buffer,
                    0xF000F0, ItemDisplayContext.GUI);
            pose.popPose();
        } else if (context == ItemDisplayContext.HEAD) {
            // worn: yaw 0 cancels CustomHeadLayer's 180 degree turn; MobTrophyBlockModel scales by 1.6 to
            // undo the layer's 0.625 shrink
            MobTrophyRenderer.render(null, 0.0F, model, time, pose, buffer, light, context);
        } else {
            // hand / dropped item: Twilight Forest's pipeline with yaw 180
            MobTrophyRenderer.render(null, 180.0F, model, time, pose, buffer, light, context);
        }
    }
}
