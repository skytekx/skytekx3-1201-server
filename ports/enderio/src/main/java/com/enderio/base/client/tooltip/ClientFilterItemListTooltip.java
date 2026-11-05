package com.enderio.base.client.tooltip;

import com.enderio.base.common.item.filter.FilterItemListTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the entries of an item, limited or soul filter as items, from ClientEnderItemFilterTooltip of Ender IO 8.x
 * (824d70feb). The item decorations show the stock levels of a limited filter.
 */
public class ClientFilterItemListTooltip extends ClientSlotListTooltip {

    private final FilterItemListTooltip tooltip;

    public ClientFilterItemListTooltip(FilterItemListTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    protected int slotCount() {
        return tooltip.items().size();
    }

    @Override
    protected void renderSlotContent(int x, int y, int slotIndex, GuiGraphics guiGraphics, Font font) {
        ItemStack stack = tooltip.items().get(slotIndex);
        guiGraphics.renderItem(stack, x + 1, y + 1, slotIndex);
        guiGraphics.renderItemDecorations(font, stack, x + 1, y + 1);
    }
}
