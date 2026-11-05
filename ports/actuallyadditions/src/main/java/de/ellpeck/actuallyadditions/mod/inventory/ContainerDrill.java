/*
 * This file ("ContainerDrill.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory;

import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotImmovable;
import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotItemHandlerUnconditioned;
import de.ellpeck.actuallyadditions.mod.items.DrillItem;
import de.ellpeck.actuallyadditions.mod.items.ItemDrillUpgrade;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerDrill extends AbstractContainerMenu {

    public static final int SLOT_AMOUNT = 5;

    // SkyTekx: every change of the upgrade slots is written into the drill right away (see
    // saveUpgrades), so that no copy of the drill with stale upgrades can exist while the menu is
    // open. The base wrote them only when the menu closed
    private final ItemStackHandlerAA drillInventory = new ItemStackHandlerAA(SLOT_AMOUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            ContainerDrill.this.saveUpgrades();
        }
    };
    private final Inventory inventory;
    // SkyTekx: the hotbar slot and the drill stack of the moment the menu opened. The base read and
    // wrote inventory.getSelected() each time, so a client that switched the selected slot by
    // packet while the menu was open got the remaining upgrades written into a second drill, while
    // the first one kept every upgrade the player had taken out
    private final int selectedSlot;
    private final ItemStack drillStack;
    private boolean upgradesLoaded;

    public static ContainerDrill fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ContainerDrill(windowId, inv);
    }

    public ContainerDrill(int windowId, Inventory inventory) {
        super(ActuallyContainers.DRILL_CONTAINER.get(), windowId);
        this.inventory = inventory;

        for (int i = 0; i < SLOT_AMOUNT; i++) {
            this.addSlot(new SlotItemHandlerUnconditioned(this.drillInventory, i, 44 + i * 18, 19) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof ItemDrillUpgrade;
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 58 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            if (i == inventory.selected) {
                this.addSlot(new SlotImmovable(inventory, i, 8 + i * 18, 116));
            } else {
                this.addSlot(new Slot(inventory, i, 8 + i * 18, 116));
            }
        }

        this.selectedSlot = inventory.selected;
        this.drillStack = inventory.getSelected();
        if (isDrill(this.drillStack)) {
            DrillItem.loadSlotsFromNBT(this.drillInventory, this.drillStack);
        }
        this.upgradesLoaded = true;
    }

    private static boolean isDrill(ItemStack stack) {
        return StackUtil.isValid(stack) && stack.getItem() instanceof DrillItem;
    }

    /**
     * Writes the upgrade slots into the drill that the menu was opened with, and only into it.
     */
    private void saveUpgrades() {
        if (this.upgradesLoaded && isDrill(this.drillStack)) {
            DrillItem.writeSlotsToNBT(this.drillInventory, this.drillStack);
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        int inventoryStart = 5;
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
                if (newStack.getItem() instanceof ItemDrillUpgrade) {
                    if (!this.moveItemStackTo(newStack, 0, 5, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //

                else if (slot >= inventoryStart && slot <= inventoryEnd) {
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
        if (clickTypeIn == ClickType.SWAP && dragType == this.inventory.selected) {
            return; //TODO: Check if this is correct, used to return ItemStack.EMPTY
        } else {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    @Override
    public void removed(Player player) {
        this.saveUpgrades();
        super.removed(player);
    }

    // SkyTekx: the menu belongs to one drill in one hotbar slot. Once the selected slot changes or
    // that slot holds another stack (or none, after the drill was dropped), the server closes the
    // menu and ignores the clicks of the client
    @Override
    public boolean stillValid(Player player) {
        return this.inventory.selected == this.selectedSlot && this.inventory.getSelected() == this.drillStack && isDrill(this.drillStack);
    }
}
