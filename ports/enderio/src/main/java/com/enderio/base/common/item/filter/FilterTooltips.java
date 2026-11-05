package com.enderio.base.common.item.filter;

import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.common.capability.DamageFilterMode;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Optional;

/**
 * Tooltip lines and images of the filter items, from Ender IO 8.x (824d70feb, 0c79259d9).
 */
public final class FilterTooltips {

    private static final String HIDE_FLAGS_KEY = "HideFlags";

    private FilterTooltips() {
    }

    /**
     * @return whether the stack hides its additional tooltip, which also hides the filter contents as in 8.x
     */
    public static boolean isAdditionalTooltipHidden(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(HIDE_FLAGS_KEY, Tag.TAG_ANY_NUMERIC)
            && (tag.getInt(HIDE_FLAGS_KEY) & ItemStack.TooltipPart.ADDITIONAL.getMask()) != 0;
    }

    public static Component unconfiguredHint() {
        return EIOLang.FILTER_UNCONFIGURED_HINT.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
    }

    public static Component configured() {
        return EIOLang.FILTER_CONFIGURED.copy().withStyle(ChatFormatting.GRAY);
    }

    public static Component listMode(boolean inverted) {
        return (inverted ? EIOLang.BLACKLIST_FILTER : EIOLang.WHITELIST_FILTER).copy().withStyle(ChatFormatting.GRAY);
    }

    public static Component nbtMode(boolean nbt) {
        return (nbt ? EIOLang.NBT_FILTER : EIOLang.NO_NBT_FILTER).copy().withStyle(ChatFormatting.GRAY);
    }

    public static Component damageMode(DamageFilterMode mode) {
        return EIOLang.DAMAGE_FILTER.copy()
            .append(": ")
            .append(Component.translatable(mode.getTranslationKey()))
            .withStyle(ChatFormatting.GRAY);
    }

    /**
     * @return the bundle style image of the entries that are not empty, or nothing when all are empty
     */
    public static Optional<TooltipComponent> itemList(List<ItemStack> entries) {
        List<ItemStack> items = entries.stream().filter(stack -> !stack.isEmpty()).toList();
        return items.isEmpty() ? Optional.empty() : Optional.of(new FilterItemListTooltip(items));
    }

    /**
     * @return the bundle style image of the fluids that are not empty, or nothing when all are empty
     */
    public static Optional<TooltipComponent> fluidList(List<FluidStack> entries) {
        List<FluidStack> fluids = entries.stream().filter(stack -> !stack.isEmpty()).toList();
        return fluids.isEmpty() ? Optional.empty() : Optional.of(new FilterFluidListTooltip(fluids));
    }
}
