package com.enderio.core.common.menu;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The filter slot of the limited item filter, from Ender IO 8.x. Unlike {@link ItemFilterSlot} it keeps the count of
 * the stack, which is the stock level of the entry, up to the max stack size of the item (f0b62d322).
 * <p>
 * Inventory Sorter matches the exact class name, so this class has its own entry in its slot blacklist.
 */
public class LimitedItemFilterSlot extends ItemFilterSlot {

    public LimitedItemFilterSlot(Supplier<ItemStack> item, Consumer<ItemStack> consumer, int pSlot, int pX, int pY) {
        super(item, consumer, pSlot, pX, pY);
    }

    @Override
    public ItemStack processResource(ItemStack resource) {
        return resource.copyWithCount(Math.min(resource.getCount(), resource.getMaxStackSize()));
    }
}
