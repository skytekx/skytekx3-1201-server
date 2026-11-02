package baguchi.twilight_crossbow.client;

import baguchi.twilight_crossbow.entity.IceFireworkRocketEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

// Kept from upstream; like upstream, ClientRegistrar renders both rockets with the vanilla FireworkEntityRenderer.
public class IceFireworkEntityRenderer extends EntityRenderer<IceFireworkRocketEntity> {
    private final ItemRenderer itemRenderer;

    public IceFireworkEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(IceFireworkRocketEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        if (entity.isShotAtAngle()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }

        this.itemRenderer
                .renderStatic(
                        entity.getItem(),
                        ItemDisplayContext.GROUND,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        buffer,
                        entity.level(),
                        entity.getId()
                );
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation getTextureLocation(IceFireworkRocketEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
