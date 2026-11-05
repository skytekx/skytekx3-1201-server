package com.enderio.api.filter;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * An item filter that also limits how many items may move, for the limited item filter backported from Ender IO 8.x,
 * where ItemFilter.test takes an ItemFilterContext with the target inventory and the direction.
 * <p>
 * {@link #test(Object)} keeps the meaning of {@link ItemStackFilter}: whether a stack matches the filter at all.
 * Users that only call test treat a limiting filter as a plain allow list.
 */
public interface LimitingItemStackFilter extends ItemStackFilter {

    /**
     * @param stack   the stack that would move
     * @param handler the inventory that the stack moves into when {@code insert} is true, or out of when it is false
     * @param insert  true for an insertion into the handler, false for an extraction from it
     * @return the largest number of items of the stack that may move, 0 or less when none may move
     */
    int getTransferLimit(ItemStack stack, IItemHandler handler, boolean insert);
}
