package com.enderio.conduits.client;

import com.enderio.conduits.client.model.conduit.facades.ClientFacadeVisibility;
import com.enderio.conduits.common.conduit.block.ConduitBlock;
import com.enderio.conduits.common.conduit.block.ConduitBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/**
 * The ghost pass of Ender IO 7.1 (78e627263): while the local player holds an item that hides the facades, the conduit model leaves them
 * out and this pass draws them translucent over the conduits, after the tripwire layer. Forge 47 has no AddSectionGeometryEvent, so the
 * section renderer of 397c648f5 is not ported. Embeddium fires this stage too.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ConduitFacadeRendering {

    private static final int GHOST_ALPHA = 85;

    private ConduitFacadeRendering() {
    }

    @SubscribeEvent
    public static void renderFacades(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS || ClientFacadeVisibility.areFacadesVisible()) {
            return;
        }

        var minecraft = Minecraft.getInstance();
        @Nullable ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        @Nullable Long2ObjectMap<BlockState> facades = ConduitBlockEntity.FACADES.get(level.dimension());
        if (facades == null || facades.isEmpty()) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        RenderType renderType = Sheets.translucentCullBlockSheet();
        VertexConsumer ghost = new GhostVertexConsumer(bufferSource.getBuffer(renderType));
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        double maxDistance = minecraft.options.getEffectiveRenderDistance() * 16.0;
        RandomSource random = RandomSource.create();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (Long2ObjectMap.Entry<BlockState> entry : facades.long2ObjectEntrySet()) {
            pos.set(entry.getLongKey());
            BlockState facade = entry.getValue();
            if (!level.isLoaded(pos) || !(level.getBlockState(pos).getBlock() instanceof ConduitBlock)) {
                continue;
            }

            if (!pos.closerToCenterThan(camera, maxDistance) || !event.getFrustum().isVisible(new AABB(pos))) {
                continue;
            }

            BakedModel model = minecraft.getBlockRenderer().getBlockModel(facade);
            // A facade block that keeps data in its own block entity would find the conduit's block entity here (bceadbded).
            ModelData modelData = facade.getBlock() instanceof EntityBlock ? ModelData.EMPTY
                : model.getModelData(level, pos, facade, ModelData.EMPTY);
            int color = minecraft.getBlockColors().getColor(facade, level, pos, 0);
            float red = color == -1 ? 1.0F : (color >> 16 & 255) / 255.0F;
            float green = color == -1 ? 1.0F : (color >> 8 & 255) / 255.0F;
            float blue = color == -1 ? 1.0F : (color & 255) / 255.0F;
            int light = LightTexture.pack(level.getBrightness(LightLayer.BLOCK, pos), level.getBrightness(LightLayer.SKY, pos));

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            random.setSeed(facade.getSeed(pos));
            for (RenderType facadeType : model.getRenderTypes(facade, random, modelData)) {
                minecraft.getBlockRenderer()
                    .getModelRenderer()
                    .renderModel(poseStack.last(), ghost, facade, model, red, green, blue, light, OverlayTexture.NO_OVERLAY, modelData, facadeType);
            }
            poseStack.popPose();
        }

        bufferSource.endBatch(renderType);
    }

    /**
     * Writes every vertex with the ghost alpha.
     */
    private static final class GhostVertexConsumer extends VertexConsumerWrapper {

        private GhostVertexConsumer(VertexConsumer parent) {
            super(parent);
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            parent.color(red, green, blue, GHOST_ALPHA);
            return this;
        }
    }
}
