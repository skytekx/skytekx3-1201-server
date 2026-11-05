package com.enderio.base.common.menu;

import com.enderio.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOMenus;
import com.enderio.base.common.network.FilterUpdatePacket;
import com.enderio.core.common.capability.DamageFilterMode;
import com.enderio.core.common.capability.ItemFilterCapability;
import com.enderio.core.common.menu.ItemFilterSlot;
import com.enderio.core.common.network.CoreNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFilterMenu extends AbstractContainerMenu {

    /**
     * Menu button ids for the damage modes: this id plus the ordinal of the mode. The screen sends them through
     * the vanilla container button packet, which checks the container id and stillValid on the server.
     */
    public static final int DAMAGE_MODE_BUTTON_ID = 16;

    /**
     * Filters with more slots than this use the 8.x layout of rows of 9 (EnderItemFilterMenu),
     * smaller ones keep the 6.x layout of rows of 5.
     */
    public static final int MAX_SMALL_LAYOUT_SIZE = 10;

    private final ItemStack stack;
    private final ItemFilterCapability capability;

    public ItemFilterMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, ItemStack stack) {
        super(pMenuType, pContainerId);
        this.stack = stack;

        LazyOptional<ResourceFilter> resourceFilter = stack.getCapability(EIOCapabilities.FILTER);

        capability = resourceFilter.map(filter -> {
            if (filter instanceof ItemFilterCapability cap) {
                return cap;
            }
            throw new IllegalArgumentException();

        }).orElseThrow(IllegalArgumentException::new);

        if (isBigLayout(capability.size())) {
            // 8.x EnderItemFilterMenu: rows of 9 from y 27, the player inventory at 45 + rows * 18.
            for (int i = 0; i < capability.size(); i++) {
                int pSlot = i;
                addSlot(new ItemFilterSlot(() -> capability.getEntry(pSlot), stack2 -> capability.setEntry(pSlot, stack2), i, 14 + (i % 9) * 18, 27 + 18 * (i / 9)));
            }
            addInventorySlots(14, 45 + rowCount(capability.size()) * 18, inventory);
        } else {
            for (int i = 0; i < capability.size(); i++) {
                int pSlot = i;
                addSlot(new ItemFilterSlot(() -> capability.getEntry(pSlot), stack2 -> capability.setEntry(pSlot, stack2) ,i ,14 + ( i % 5) * 18, 35 + 20 * ( i / 5)));
            }
            addInventorySlots(14,119, inventory);
        }
    }

    public static boolean isBigLayout(int size) {
        return size > MAX_SMALL_LAYOUT_SIZE;
    }

    public static int rowCount(int size) {
        return (size + 8) / 9;
    }

    public ItemFilterMenu(int pContainerId, Inventory inventory, ItemStack stack) {
        this(EIOMenus.ITEM_FILTER.get(), pContainerId, inventory, stack);
    }

    public static ItemFilterMenu factory(@Nullable MenuType<ItemFilterMenu> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        return new ItemFilterMenu(EIOMenus.ITEM_FILTER.get(), pContainerId, inventory, inventory.player.getMainHandItem());
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return pPlayer.getMainHandItem().equals(stack);
    }

    public void addInventorySlots(int xPos, int yPos, Inventory inventory) {

        // The slot that holds the opened filter is locked, so the stack the menu edits cannot be moved or duplicated.

        // Hotbar
        for (int x = 0; x < 9; x++) {
            Slot ref = new Slot(inventory, x, xPos + x * 18, yPos + 58) {

                @Override
                public boolean mayPickup(Player player) {
                    return !this.getItem().equals(ItemFilterMenu.this.stack);
                }
            };
            this.addSlot(ref);
        }

        // Inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                Slot ref = new Slot(inventory, x + y * 9 + 9, xPos + x * 18, yPos + y * 18) {

                    @Override
                    public boolean mayPickup(Player player) {
                        return !this.getItem().equals(ItemFilterMenu.this.stack);
                    }
                };
                this.addSlot(ref);
            }
        }

    }

    public ItemFilterCapability getFilter() {
        return capability;
    }

    public void setNbt(Boolean nbt) {
        CoreNetwork.sendToServer(new FilterUpdatePacket(nbt, capability.isInvert()));
        capability.setNbt(nbt);
    }

    public void setInverted(Boolean inverted) {
        CoreNetwork.sendToServer(new FilterUpdatePacket(capability.isNbt(), inverted));
        capability.setInverted(inverted);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int modeId = id - DAMAGE_MODE_BUTTON_ID;
        if (modeId >= 0 && modeId < DamageFilterMode.values().length) {
            if (capability.canFilterByDamage()) {
                capability.setDamageMode(DamageFilterMode.byId(modeId));
            }
            return true;
        }

        return super.clickMenuButton(player, id);
    }

    @Override
    public void doClick(int slotId, int button, ClickType clickType, Player player) {
        // A number key must not move the opened filter out of its slot either.
        if (clickType == ClickType.SWAP && player.getInventory().getItem(button).equals(stack)) {
            return;
        }

        if (slotId >= 0 && slotId < capability.size()) {
            // Only allow PICKUP (click) or QUICK_MOVE (shift + click) events.
            if (clickType != ClickType.PICKUP && clickType != ClickType.QUICK_MOVE) {
                return;
            }

            // A click with an empty hand clears the entry.
            ItemStack carriedItem = this.getCarried();
            if (carriedItem.isEmpty()) {
                if (!capability.getEntry(slotId).isEmpty()) {
                    capability.setEntry(slotId, ItemStack.EMPTY);
                }
                return;
            }

            // A click with an item sets the entry from it. The carried stack stays untouched.
            if (clickType == ClickType.PICKUP) {
                this.slots.get(slotId).safeInsert(carriedItem);
            }
            return;
        }

        super.doClick(slotId, button, clickType, player);
    }
}
