/*
 * This file ("ContainerBag.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory;

import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotFilter;
import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotImmovable;
import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotItemHandlerUnconditioned;
import de.ellpeck.actuallyadditions.mod.items.Sack;
import de.ellpeck.actuallyadditions.mod.network.gui.IButtonReactor;
import de.ellpeck.actuallyadditions.mod.sack.SackManager;
import de.ellpeck.actuallyadditions.mod.tile.FilterSettings;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

// SkyTekx: the menu of the Traveler's Sack of main (85725a0f, 38f48247, 669e8437, adce5984,
// 521cf822) under the base's menu id actuallyadditions:bag_container. Its filter is the one of the
// SackData, so filter slot changes save with the sack's inventory, and the pickup toggle goes into
// the stack's NBT key AutoInsert
public class SackContainer extends AbstractContainerMenu implements IButtonReactor {

    private final FilterSettings filter;
    private final ItemStackHandlerAA bagInventory;
    private final Inventory inventory;
    public boolean autoInsert;
    // SkyTekx: the hotbar slot and the sack stack of the moment the menu opened, as in ContainerDrill
    // and ContainerFilter. The base returned true from stillValid and wrote its options into whatever
    // stack was selected when it closed
    private final int selectedSlot;
    private final ItemStack sackStack;

    public static final int SIZE = 28;

    public static SackContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
        data.readUUID();
        boolean autoInsert = data.readBoolean();
        return new SackContainer(windowId, inv, new ItemStackHandlerAA(SIZE), autoInsert, new FilterSettings(Sack.FILTER_SLOTS, data.readInt()));
    }

    public SackContainer(int windowId, Inventory playerInventory, ItemStackHandlerAA handler, boolean autoInsert, FilterSettings filterIn) {
        super(ActuallyContainers.BAG_CONTAINER.get(), windowId);

        this.inventory = playerInventory;
        this.bagInventory = handler;
        this.filter = filterIn;
        this.autoInsert = autoInsert;
        this.selectedSlot = playerInventory.selected;
        this.sackStack = playerInventory.getSelected();

        // Filter slots
        for (int row = 0; row < 4; row++) {
            this.addSlot(new SlotFilter(this.filter, row, 155, 10 + row * 18));
        }

        // Sack inventory
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 7; col++) {
                this.addSlot(new SlotItemHandlerUnconditioned(this.bagInventory, col + row * 7, 10 + col * 18, 10 + row * 18) {
                    @Override
                    public boolean mayPlace(@Nonnull ItemStack stack) {
                        return !stack.is(ActuallyTags.Items.HOLDS_ITEMS) && SackContainer.this.filter.check(stack);
                    }
                });
            }
        }

        // Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 94 + row * 18));
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            if (i == playerInventory.selected) {
                this.addSlot(new SlotImmovable(playerInventory, i, 8 + i * 18, 152));
            } else {
                this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 152));
            }
        }
    }

    public FilterSettings getFilter() {
        return this.filter;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int slot) {
        int inventoryStart = this.bagInventory.getSlots() + 4;
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
                if (!this.filter.check(newStack) || !this.moveItemStackTo(newStack, 4, 32, false)) {
                    if (slot <= inventoryEnd) {
                        if (!this.moveItemStackTo(newStack, hotbarStart, hotbarEnd + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (slot >= inventoryEnd + 1 && slot < hotbarEnd + 1 && !this.moveItemStackTo(newStack, inventoryStart, inventoryEnd + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //

            } else if (!this.moveItemStackTo(newStack, inventoryStart, hotbarEnd + 1, false)) {
                return ItemStack.EMPTY;
            }

            if (newStack.isEmpty()) {
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
    public void clicked(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull Player player) {
        if (SlotFilter.checkFilter(this, slotId, player)) {
            return; //TODO: Check if this is correct, used to return ItemStack.EMPTY
        } else if (clickTypeIn == ClickType.SWAP && dragType == this.inventory.selected) {
            return; //TODO: Check if this is correct, used to return ItemStack.EMPTY
        } else {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    @Override
    public void removed(@Nonnull Player player) {
        if (isSack(this.sackStack)) {
            Sack.setAutoInsert(this.sackStack, this.autoInsert);
        }
        super.removed(player);
    }

    // SkyTekx: valid while the selected hotbar slot still holds the sack it was opened with
    @Override
    public boolean stillValid(@Nonnull Player player) {
        return this.inventory.selected == this.selectedSlot && this.inventory.getSelected() == this.sackStack && isSack(this.sackStack);
    }

    private static boolean isSack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Sack sack && !sack.isVoid;
    }

    // Button 0 toggles the pickup, the others are the filter buttons of FilterSettingsGui with the
    // offset 1 (79c8bcfb). The toggle goes into the stack at once (669e8437), and a filter toggle
    // marks the sack data for saving, since SackData only hears of slot changes
    @Override
    public void onButtonPressed(int buttonID, Player player) {
        if (buttonID == 0) {
            this.autoInsert = !this.autoInsert;
            if (isSack(this.sackStack)) {
                Sack.setAutoInsert(this.sackStack, this.autoInsert);
            }
        } else if (this.filter.onButtonPressed(buttonID - 1)) {
            SackManager.get().setDirty();
        }
    }
}
