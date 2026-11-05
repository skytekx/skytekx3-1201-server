package de.ellpeck.actuallyadditions.mod.blocks.blockhuds;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// SkyTekx: upstream 668f7ffe moved the look at overlays out of the common block classes into
// client only IBlockHud classes. Only client classes implement this interface, so it can be
// client only as a whole. BlockHuds maps the blocks to their overlay
@OnlyIn(Dist.CLIENT)
public interface IBlockHud {
    void displayHud(GuiGraphics guiGraphics, Minecraft minecraft, Player player, ItemStack stack, HitResult rayCast, Window resolution);
}
