package de.ellpeck.actuallyadditions.mod.blocks.blockhuds;

import com.mojang.blaze3d.platform.Window;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityVerticalDigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// SkyTekx: BlockVerticalDigger.displayHud of the base, moved here as upstream 668f7ffe does
@OnlyIn(Dist.CLIENT)
public class MinerHud implements IBlockHud {
    @Override
    public void displayHud(GuiGraphics guiGraphics, Minecraft minecraft, Player player, ItemStack stack, HitResult rayCast, Window resolution) {
        if (!(rayCast instanceof BlockHitResult)) {
            return;
        }
        BlockEntity tile = minecraft.level.getBlockEntity(((BlockHitResult) rayCast).getBlockPos());
        if (tile instanceof TileEntityVerticalDigger miner) {
            // SkyTekx: the states of the min build height, the base and main still test 0 and -1
            int minHeight = minecraft.level.getMinBuildHeight();
            String info = miner.checkY == minHeight
                    ? "Done Mining!"
                    : miner.checkY < minHeight
                    ? "Calculating positions..."
                    : "Mining at " + (miner.getBlockPos().getX() + miner.checkX) + ", " + miner.checkY + ", " + (miner.getBlockPos().getZ() + miner.checkZ) + ".";
            guiGraphics.drawString(minecraft.font, info, (int) (resolution.getGuiScaledWidth() / 2f + 5), (int) (resolution.getGuiScaledHeight() / 2f - 20), 0xFFFFFF);
        }
    }
}
