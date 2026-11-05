/*
 * This file ("ContainerFilter.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory;

import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotFilter;
import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotImmovable;
import de.ellpeck.actuallyadditions.mod.items.DrillItem;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerFilter extends AbstractContainerMenu {

    public static final int SLOT_AMOUNT = 24;

    private final ItemStackHandlerAA filterInventory = new ItemStackHandlerAA(SLOT_AMOUNT);
    private final Inventory inventory;
    // SkyTekx: the hotbar slot and the filter stack of the moment the menu opened, as in
    // ContainerDrill. The base wrote the ghost items into whatever stack was selected at close
    private final int selectedSlot;
    private final ItemStack filterStack;

    public static ContainerFilter fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ContainerFilter(windowId, inv);
    }

    public ContainerFilter(int windowId, Inventory inventory) {
        super(ActuallyContainers.FILTER_CONTAINER.get(), windowId);
        this.inventory = inventory;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 6; j++) {
                this.addSlot(new SlotFilter(this.filterInventory, j + i * 6, 35 + j * 18, 10 + i * 18));
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 94 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            if (i == inventory.selected) {
                this.addSlot(new SlotImmovable(inventory, i, 8 + i * 18, 152));
            } else {
                this.addSlot(new Slot(inventory, i, 8 + i * 18, 152));
            }
        }

        this.selectedSlot = inventory.selected;
        this.filterStack = inventory.getSelected();
        if (SlotFilter.isFilter(this.filterStack)) {
            DrillItem.loadSlotsFromNBT(this.filterInventory, this.filterStack);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        int inventoryStart = SLOT_AMOUNT;
        int inventoryEnd = inventoryStart + 26;
        int hotbarStart = inventoryEnd + 1;
        int hotbarEnd = hotbarStart + 8;

        Slot theSlot = this.slots.get(slot);

        if (theSlot != null && theSlot.hasItem()) {
            ItemStack newStack = theSlot.getItem();
            ItemStack currentStack = newStack.copy();

            //Other Slots in Inventory excluded
            if (slot >= inventoryStart) {
                //Shift from Inventory
                //
                if (slot >= inventoryStart && slot <= inventoryEnd) {
                    if (!this.moveItemStackTo(newStack, hotbarStart, hotbarEnd + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slot >= inventoryEnd + 1 && slot < hotbarEnd + 1 && !this.moveItemStackTo(newStack, inventoryStart, inventoryEnd + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(newStack, inventoryStart, hotbarEnd + 1, false)) {
                return ItemStack.EMPTY;
            }

            if (!StackUtil.isValid(newStack)) {
                theSlot.set(ItemStack.EMPTY);
            } else {
                theSlot.setChanged();
            }

            if (newStack.getCount() == currentStack.getCount()) {
                return ItemStack.EMPTY;
            }
            theSlot.onTake(player, newStack);

            return currentStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
        if (SlotFilter.checkFilter(this, slotId, player)) {
            return; //TODO: Check if this is correct, used to return ItemStack.EMPTY
        } else if (clickTypeIn == ClickType.SWAP && dragType == this.inventory.selected) {
            return; //TODO: Check if this is correct, used to return ItemStack.EMPTY
        } else {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    @Override
    public void removed(Player player) {
        if (SlotFilter.isFilter(this.filterStack)) {
            DrillItem.writeSlotsToNBT(this.filterInventory, this.filterStack);
        }
        super.removed(player);
    }

    // SkyTekx: valid while the selected hotbar slot still holds the filter it was opened with
    @Override
    public boolean stillValid(Player player) {
        return this.inventory.selected == this.selectedSlot && this.inventory.getSelected() == this.filterStack && SlotFilter.isFilter(this.filterStack);
    }
}
