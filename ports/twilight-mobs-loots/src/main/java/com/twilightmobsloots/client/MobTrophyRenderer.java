package com.twilightmobsloots.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.twilightmobsloots.MobHead;
import com.twilightmobsloots.block.AbstractMobTrophyBlock;
import com.twilightmobsloots.block.MobTrophyWallBlock;
import com.twilightmobsloots.block.entity.MobTrophyBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Mob trophy block entity renderer, following Twilight Forest's trophy renderer pipeline:
 *
 * <pre>
 * pose.pushPose();
 * if (direction == null) translate(0.5, 0, 0.5);                          // floor trophy
 * else                   translate(0.5 - dx*0.249, 0.25, 0.5 - dz*0.249);  // wall trophy
 * pose.scale(-1, -1, 1);
 * model.setupRotationsForTrophy(anim*4.5, yaw, 0, 0);
 * model.renderTrophy(pose, buffer, light, NO_OVERLAY, ctx);
 * pose.popPose();
 * </pre>
 *
 * <p>Yaw: wall trophies use {@code (get2DDataValue(facing)+2)*4}, floor trophies the 16-step ROTATION.
 */
public class MobTrophyRenderer implements BlockEntityRenderer<MobTrophyBlockEntity> {

    private final Map<MobHead, MobTrophyBlockModel> trophies;

    public MobTrophyRenderer(BlockEntityRendererProvider.Context context) {
        this.trophies = MobTrophyBlockModel.createTrophyRenderers(context.getModelSet());
    }

    @Override
    public void render(MobTrophyBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffer,
                       int light, int overlay) {
        BlockState state = be.getBlockState();
        float anim = be.getAnimationProgress(partialTick);
        boolean isWall = state.getBlock() instanceof MobTrophyWallBlock;
        Direction direction = isWall ? state.getValue(MobTrophyWallBlock.FACING) : null;
        float yaw = 22.5F * (isWall ? wallRotation(direction) : state.getValue(BlockStateProperties.ROTATION_16));
        MobHead head = ((AbstractMobTrophyBlock) state.getBlock()).getMobHead();
        MobTrophyBlockModel model = this.trophies.get(head);
        if (model != null) {
            render(direction, yaw, model, anim, pose, buffer, light, ItemDisplayContext.NONE);
        }
    }

    /** Shared render entry point, also used for the item forms (GUI, hand, head). */
    public static void render(@Nullable Direction direction, float yaw, MobTrophyBlockModel model, float anim,
                              PoseStack pose, MultiBufferSource buffer, int light, ItemDisplayContext context) {
        pose.pushPose();
        if (direction == null) {
            pose.translate(0.5F, 0.0F, 0.5F);
        } else {
            pose.translate(0.5F - direction.getStepX() * 0.249F, 0.25F,
                    0.5F - direction.getStepZ() * 0.249F);
        }
        pose.scale(-1.0F, -1.0F, 1.0F);
        model.setupRotationsForTrophy(anim * 4.5F, yaw,
                context == ItemDisplayContext.GUI ? 0.35F : 0.0F, 0.0F);
        model.renderTrophy(pose, buffer, light, OverlayTexture.NO_OVERLAY, context);
        pose.popPose();
    }

    /**
     * 16-step rotation of a wall trophy (times 22.5 degrees gives the yaw), same as Twilight Forest's
     * {@code (get2DDataValue(facing)+2)*4}: NORTH=0, SOUTH=180, WEST=270, EAST=90 degrees.
     */
    private static int wallRotation(Direction direction) {
        return switch (direction) {
            case SOUTH -> 8;
            case WEST -> 12;
            case EAST -> 4;
            default -> 0;
        };
    }
}
