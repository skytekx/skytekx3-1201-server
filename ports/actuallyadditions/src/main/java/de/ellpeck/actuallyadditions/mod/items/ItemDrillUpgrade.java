/*
 * This file ("ItemDrillUpgrade.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemDrillUpgrade extends ItemBase {

    public final UpgradeType type;

    public ItemDrillUpgrade(UpgradeType type) {
        super(ActuallyItems.defaultProps().stacksTo(1));
        this.type = type;
    }

    public static int getSlotToPlaceFrom(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound != null) {
            return compound.getInt("SlotToPlaceFrom") - 1;
        }
        return -1;
    }

    // SkyTekx: the chat line of upstream ccfb8d16. The NBT key SlotToPlaceFrom keeps the base's slot
    // plus one, which getSlotToPlaceFrom takes off again. The off by one that b7875a09 (#1463) fixes
    // came with the 1.21 data component and never existed on this NBT form
    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide && this.type == UpgradeType.PLACER) {
            this.setSlotToPlaceFrom(stack, player.getInventory().selected);
            player.sendSystemMessage(Component.literal("Set the slot to place from to " + (player.getInventory().selected + 1)));
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    public void setSlotToPlaceFrom(ItemStack stack, int slot) {
        CompoundTag compound = stack.getOrCreateTag();
        compound.putInt("SlotToPlaceFrom", slot + 1);

        stack.setTag(compound);
    }

    public enum UpgradeType {
        SPEED,
        SPEED_II,
        SPEED_III,
        SILK_TOUCH,
        FORTUNE,
        FORTUNE_II,
        THREE_BY_THREE,
        FIVE_BY_FIVE,
        PLACER
    }

    // SkyTekx: the placer augment shows its hotbar slot (upstream b7875a09, #1463)
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltipComponents, @Nonnull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
        if (this.type == UpgradeType.PLACER) {
            tooltipComponents.add(Component.translatable("tooltip.actuallyadditions.placer_augment", getSlotToPlaceFrom(stack) + 1).withStyle(ChatFormatting.GRAY));
        }
    }
}
