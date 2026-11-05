package de.ellpeck.actuallyadditions.mod.blocks.blockhuds;

import com.mojang.blaze3d.platform.Window;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityLavaFactoryController;
import de.ellpeck.actuallyadditions.mod.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// SkyTekx: BlockLavaFactoryController.displayHud of the base, moved here as upstream 668f7ffe does.
// The overlay text itself comes back with the lava factory fix of 6886aca6
@OnlyIn(Dist.CLIENT)
public class LavaFactoryControllerHud implements IBlockHud {
    @Override
    public void displayHud(GuiGraphics guiGraphics, Minecraft minecraft, Player player, ItemStack stack, HitResult rayCast, Window resolution) {
        if (!(rayCast instanceof BlockHitResult)) {
            return;
        }

        TileEntityLavaFactoryController factory = (TileEntityLavaFactoryController) minecraft.level.getBlockEntity(((BlockHitResult) rayCast).getBlockPos());
        if (factory != null) {
            int state = factory.isMultiblock();
            if (state == TileEntityLavaFactoryController.NOT_MULTI) {
                StringUtil.drawSplitString(minecraft.font, I18n.get("tooltip." + ActuallyAdditions.MODID + ".factory.notPart.desc"), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 + 5, 200, 0xFFFFFF, true);
            } else if (state == TileEntityLavaFactoryController.HAS_AIR || state == TileEntityLavaFactoryController.HAS_LAVA) {
                StringUtil.drawSplitString(minecraft.font, I18n.get("tooltip." + ActuallyAdditions.MODID + ".factory.works.desc"), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 + 5, 200, 0xFFFFFF, true);
            }
        }
    }
}
