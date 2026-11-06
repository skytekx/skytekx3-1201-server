package de.ellpeck.actuallyadditions.mod.blocks.blockhuds;

import com.mojang.blaze3d.platform.Window;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityLavaFactoryController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// SkyTekx: BlockLavaFactoryController.displayHud of the base, moved here as upstream 668f7ffe does.
// Upstream 6886aca6 draws the text with drawWordWrap, where the base called the empty
// StringUtil.drawSplitString and so showed nothing
@OnlyIn(Dist.CLIENT)
public class LavaFactoryControllerHud implements IBlockHud {
    @Override
    public void displayHud(GuiGraphics guiGraphics, Minecraft minecraft, Player player, ItemStack stack, HitResult rayCast, Window resolution) {
        if (!(rayCast instanceof BlockHitResult blockHit) || minecraft.level == null) {
            return;
        }

        if (minecraft.level.getBlockEntity(blockHit.getBlockPos()) instanceof TileEntityLavaFactoryController factory) {
            int state = factory.isMultiblock();
            if (state == TileEntityLavaFactoryController.NOT_MULTI) {
                guiGraphics.drawWordWrap(minecraft.font, Component.translatable("tooltip." + ActuallyAdditions.MODID + ".factory.notPart.desc"), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 + 5, 200, 0xFFFFFF);
            } else if (state == TileEntityLavaFactoryController.HAS_AIR || state == TileEntityLavaFactoryController.HAS_LAVA) {
                guiGraphics.drawWordWrap(minecraft.font, Component.translatable("tooltip." + ActuallyAdditions.MODID + ".factory.working.desc"), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 + 5, 200, 0xFFFFFF);
            }
        }
    }
}
