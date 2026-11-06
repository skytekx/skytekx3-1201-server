/*
 * This file ("RenderReconstructorLens.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.ellpeck.actuallyadditions.api.lens.ILensItem;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ReconstructorRenderer implements BlockEntityRenderer<TileEntityAtomicReconstructor> {

    public ReconstructorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TileEntityAtomicReconstructor tile, float partialTicks, @Nonnull PoseStack matrices, @Nonnull MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        // SkyTekx: the guard of 713568d7. A renderer that runs after the block changed read the facing
        // of a state without one and crashed the client. As upstream it reads the state of the level,
        // which getOrientation reads too. A section that has not been rebuilt yet still renders the
        // block entity of a reconstructor that the client has just broken, while its cached state
        // is still the reconstructor
        if (tile.getLevel() == null || !tile.getLevel().getBlockState(tile.getBlockPos()).is(ActuallyBlocks.ATOMIC_RECONSTRUCTOR.get())) {
            return;
        }
        ItemStack stack = tile.inv.getStackInSlot(0);
        Direction direction = tile.getOrientation();
        float rot = 360.0f - direction.getOpposite().toYRot(); //Sigh...
        float pitch = 0;
        if (direction == Direction.UP) {
            pitch = 90;
        } else if (direction == Direction.DOWN) {
            pitch = -90;
        }

        // SkyTekx: the beam is a particle now, as in 337fbfb8, so the renderer only draws the lens
        if (stack.isEmpty() || !(stack.getItem() instanceof ILensItem)) {
            return;
        }

        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.5F);

        matrices.mulPose(Axis.YP.rotationDegrees(rot));
        matrices.mulPose(Axis.XP.rotationDegrees(pitch));

        matrices.translate(0.0F, 0.0F, -0.5F);

        matrices.scale(0.5F, 0.5F, 0.5F);
        int lightColor = LevelRenderer.getLightColor(tile.getLevel(), tile.getPosition().relative(direction));
        AssetUtil.renderItemInWorld(stack, lightColor, combinedOverlay, matrices, buffer);

        matrices.popPose();
    }

}
