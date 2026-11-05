package com.enderio.base.common.item.filter;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The tooltip image of a filter that shows its entries as items, like a bundle (Ender IO 8.x 824d70feb).
 * It is common code: the client draws it with ClientFilterItemListTooltip, which only the client registers.
 */
public record FilterItemListTooltip(List<ItemStack> items) implements TooltipComponent {
}
