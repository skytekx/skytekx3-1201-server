package com.enderio.conduits.client;

import com.enderio.api.misc.ColorControl;
import com.enderio.base.common.util.PaintUtils;
import com.enderio.conduits.client.model.conduit.facades.ClientFacadeVisibility;
import com.enderio.conduits.common.conduit.block.ConduitBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Colors of the conduit bundle and the facade items (Ender IO 7.1, 78e627263). The conduit model moves the tint indices of the facade
 * quads below -1, so they do not collide with the channel colors of the conduit quads, and they are colored by the facade's block with the
 * real level and position, which keeps biome tints.
 */
public class ConduitFacadeColor implements BlockColor, ItemColor {

    private static final int WHITE = 0xFFFFFF;

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (tintIndex >= 0) {
            return tintIndex < ColorControl.values().length ? ColorControl.values()[tintIndex].getColor() : WHITE;
        }

        if (level != null && pos != null && level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit
            && ClientFacadeVisibility.areFacadesVisible()) {
            Optional<BlockState> facadeState = conduit.getBundle().getFacadeState();
            if (facadeState.isPresent()) {
                int color = Minecraft.getInstance().getBlockColors().getColor(facadeState.get(), level, pos, unmoveTintIndex(tintIndex));
                if (color != -1) {
                    return color;
                }
            }
        }

        return WHITE;
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        @Nullable Block paint = PaintUtils.getPaint(stack);
        if (paint != null) {
            return Minecraft.getInstance().getItemColors().getColor(paint.asItem().getDefaultInstance(), tintIndex);
        }

        return WHITE;
    }

    public static int moveTintIndex(int original) {
        return original < 0 ? original : -original - 2;
    }

    public static int unmoveTintIndex(int moved) {
        return -moved - 2;
    }
}
