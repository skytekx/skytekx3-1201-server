package com.enderio.base.common.menu;

import com.enderio.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOMenus;
import com.enderio.base.common.network.FilterUpdatePacket;
import com.enderio.core.common.capability.DamageFilterMode;
import com.enderio.core.common.capability.LimitedItemFilterCapability;
import com.enderio.core.common.menu.LimitedItemFilterSlot;
import com.enderio.core.common.network.CoreNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

/**
 * The menu of the limited item filter, from LimitedItemFilterMenu of Ender IO 8.x (1a85694c2) in the style of the 6.x
 * {@link ItemFilterMenu}: it edits the filter in the main hand, whose inventory slot stays locked, or a filter in a conduit
 * slot (see {@link FilterAccess}).
 */
public class LimitedItemFilterMenu extends AbstractFilterMenu {

    public static final int ROWS = 2;

    private final LimitedItemFilterCapability capability;

    public LimitedItemFilterMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, ItemStack stack) {
        this(pMenuType, pContainerId, inventory, FilterAccess.hand(stack));
    }

    public LimitedItemFilterMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory inventory, FilterAccess access) {
        super(pMenuType, pContainerId, access);

        LazyOptional<ResourceFilter> resourceFilter = stack.getCapability(EIOCapabilities.FILTER);

        capability = resourceFilter.map(filter -> {
            if (filter instanceof LimitedItemFilterCapability cap) {
                return cap;
            }
            throw new IllegalArgumentException();

        }).orElseThrow(IllegalArgumentException::new);

        // 8.x layout: two rows of 9 from y 27, the player inventory at 45 + 2 * 18.
        for (int i = 0; i < capability.size(); i++) {
            int pSlot = i;
            addSlot(new LimitedItemFilterSlot(() -> capability.getEntry(pSlot), stack2 -> capability.setEntry(pSlot, stack2), i, 14 + (i % 9) * 18, 27 + 18 * (i / 9)));
        }
        addInventorySlots(14, 45 + ROWS * 18, inventory);
    }

    public LimitedItemFilterMenu(int pContainerId, Inventory inventory, ItemStack stack) {
        this(EIOMenus.LIMITED_ITEM_FILTER.get(), pContainerId, inventory, stack);
    }

    public LimitedItemFilterMenu(int pContainerId, Inventory inventory, FilterAccess access) {
        this(EIOMenus.LIMITED_ITEM_FILTER.get(), pContainerId, inventory, access);
    }

    public static LimitedItemFilterMenu factory(@Nullable MenuType<LimitedItemFilterMenu> pMenuType, int pContainerId, Inventory inventory, @Nullable FriendlyByteBuf buf) {
        return new LimitedItemFilterMenu(EIOMenus.LIMITED_ITEM_FILTER.get(), pContainerId, inventory, FilterAccess.fromNetwork(inventory, buf));
    }

    public LimitedItemFilterCapability getFilter() {
        return capability;
    }

    public void setNbt(Boolean nbt) {
        CoreNetwork.sendToServer(new FilterUpdatePacket(nbt, false));
        capability.setNbt(nbt);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int modeId = id - ItemFilterMenu.DAMAGE_MODE_BUTTON_ID;
        if (modeId >= 0 && modeId < DamageFilterMode.values().length) {
            capability.setDamageMode(DamageFilterMode.byId(modeId));
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
            ItemStack current = capability.getEntry(slotId);
            if (carriedItem.isEmpty()) {
                if (!current.isEmpty()) {
                    capability.setEntry(slotId, ItemStack.EMPTY);
                }
                return;
            }

            // 8.x: a click with the same item raises the stock level by the carried count, up to one stack.
            if (this.slots.get(slotId) instanceof LimitedItemFilterSlot limitedSlot) {
                if (!current.isEmpty() && ItemStack.isSameItem(carriedItem, current)) {
                    limitedSlot.setResource(current.copyWithCount(current.getCount() + carriedItem.getCount()));
                    return;
                }

                // A click with another item sets the entry from it with the carried count. The carried stack stays untouched.
                if (clickType == ClickType.PICKUP) {
                    limitedSlot.safeInsert(carriedItem);
                }
            }
            return;
        }

        super.doClick(slotId, button, clickType, player);
    }
}
