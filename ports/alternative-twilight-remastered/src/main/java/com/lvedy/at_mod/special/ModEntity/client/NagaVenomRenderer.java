package com.lvedy.at_mod.special.ModEntity.client;

import com.lvedy.at_mod.special.ModEntity.entity.NagaVenomEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class NagaVenomRenderer extends EntityRenderer<NagaVenomEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/misc/white.png");

    public NagaVenomRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(NagaVenomEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // 不渲染任何模型，所有视觉效果通过粒子在实体tick中实现
    }

    @Override
    public ResourceLocation getTextureLocation(NagaVenomEntity entity) {
        return TEXTURE;
    }
}
