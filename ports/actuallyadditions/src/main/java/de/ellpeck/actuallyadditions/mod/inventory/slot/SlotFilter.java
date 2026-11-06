/*
 * This file ("SlotFilter.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.slot;

import de.ellpeck.actuallyadditions.mod.items.ItemFilter;
import de.ellpeck.actuallyadditions.mod.tile.FilterSettings;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotFilter extends SlotItemHandlerUnconditioned {

    public SlotFilter(ItemStackHandlerAA inv, int slot, int x, int y) {
        super(inv, slot, x, y);
    }

    public SlotFilter(FilterSettings inv, int slot, int x, int y) {
        this(inv.filterInventory, slot, x, y);
    }

    public static boolean checkFilter(AbstractContainerMenu container, int slotId, Player player) {
        if (slotId >= 0 && slotId < container.slots.size()) {
            Slot slot = container.getSlot(slotId);
            if (slot instanceof SlotFilter) {
                ((SlotFilter) slot).slotClick(container);
                return true;
            }
        }
        return false;
    }

    public static boolean isFilter(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemFilter;
    }

    // SkyTekx: the ghost item comes from the carried stack of the menu (85725a0f) and a filter taken
    // out goes back to the cursor (87601c1e). The base read the selected hotbar item, so a click
    // with an empty cursor put a copy of the held sack into the slot, and a filter taken out went
    // to the hotbar through setPickedItem, which overwrites the selected item of a full inventory
    private void slotClick(AbstractContainerMenu container) {
        ItemStack cursorItem = container.getCarried();
        ItemStack stackInSlot = this.getItem();

        if (!stackInSlot.isEmpty() && cursorItem.isEmpty()) {
            if (isFilter(stackInSlot)) {
                container.setCarried(stackInSlot);
            }

            this.set(ItemStack.EMPTY);
        } else if (!cursorItem.isEmpty()) {
            if (!isFilter(stackInSlot)) {
                ItemStack s = cursorItem.copy();
                s.setCount(1);
                this.set(s);

                if (isFilter(cursorItem)) {
                    cursorItem.shrink(1);
                }
            }
        }
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void set(ItemStack stack) {
        super.set(stack.copy());
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
