package de.ellpeck.actuallyadditions.mod.blocks.blockhuds;

import com.mojang.blaze3d.platform.Window;
import de.ellpeck.actuallyadditions.api.tile.IPhantomTile;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.util.StringUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// SkyTekx: BlockPhantom.displayHud of the base, moved here as upstream 668f7ffe does, with the
// Components of 3a767e18. The word wrapped lines come back with the phantom fix of ae6695a5
@OnlyIn(Dist.CLIENT)
public class PhantomHud implements IBlockHud {
    @Override
    public void displayHud(GuiGraphics guiGraphics, Minecraft minecraft, Player player, ItemStack stack, HitResult rayCast, Window resolution) {
        if (!(rayCast instanceof BlockHitResult)) {
            return;
        }
        BlockPos pos = ((BlockHitResult) rayCast).getBlockPos();
        BlockEntity tile = minecraft.level.getBlockEntity(pos);
        if (tile != null) {
            if (tile instanceof IPhantomTile phantom) {
                guiGraphics.drawString(minecraft.font, Component.translatable("tooltip." + ActuallyAdditions.MODID + ".blockPhantomRange.desc").append(": " + phantom.getRange()).withStyle(ChatFormatting.GOLD), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 - 40, ChatFormatting.WHITE.getColor());
                if (phantom.hasBoundPosition()) {
                    int distance = Mth.ceil(new Vec3(pos.getX(), pos.getY(), pos.getZ()).distanceTo(new Vec3(phantom.getBoundPosition().getX(), phantom.getBoundPosition().getY(), phantom.getBoundPosition().getZ())));
                    BlockState state = minecraft.level.getBlockState(phantom.getBoundPosition());
                    Block block = state.getBlock();
                    Item item = Item.byBlock(block);
                    String name = item.getName(new ItemStack(block)).getString();
                    StringUtil.drawSplitString(minecraft.font, StringUtil.localizeFormatted("tooltip." + ActuallyAdditions.MODID + ".phantom.blockInfo.desc", name, phantom.getBoundPosition().getX(), phantom.getBoundPosition().getY(), phantom.getBoundPosition().getZ(), distance), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 - 30, 200, 0xFFFFFF, true);

                    if (phantom.isBoundThingInRange()) {
                        StringUtil.drawSplitString(minecraft.font, ChatFormatting.DARK_GREEN + I18n.get("tooltip." + ActuallyAdditions.MODID + ".phantom.connectedRange.desc"), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 + 25, 200, 0xFFFFFF, true);
                    } else {
                        StringUtil.drawSplitString(minecraft.font, ChatFormatting.DARK_RED + I18n.get("tooltip." + ActuallyAdditions.MODID + ".phantom.connectedNoRange.desc"), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 + 25, 200, 0xFFFFFF, true);
                    }
                } else {
                    guiGraphics.drawString(minecraft.font, Component.translatable("tooltip." + ActuallyAdditions.MODID + ".phantom.notConnected.desc").withStyle(ChatFormatting.RED), resolution.getGuiScaledWidth() / 2 + 5, resolution.getGuiScaledHeight() / 2 + 25, ChatFormatting.WHITE.getColor());
                }
            }
        }
    }
}
