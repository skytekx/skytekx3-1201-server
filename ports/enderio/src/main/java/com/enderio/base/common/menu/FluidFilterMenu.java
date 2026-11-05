package com.enderio.base.common.menu;

import com.enderio.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.init.EIOMenus;
import com.enderio.base.common.network.FilterUpdatePacket;
import com.enderio.core.common.capability.FluidFilterCapability;
import com.enderio.core.common.menu.FluidFilterSlot;
import com.enderio.core.common.network.CoreNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class FluidFilterMenu extends AbstractFilterMenu {

    private final FluidFilterCapability capability;

    public FluidFilterMenu(MenuType<?> pMenuType, int pContainerId, Inventory inventory, ItemStack stack) {
        this(pMenuType, pContainerId, inventory, FilterAccess.hand(stack));
    }

    public FluidFilterMenu(MenuType<?> pMenuType, int pContainerId, Inventory inventory, FilterAccess access) {
        super(pMenuType, pContainerId, access);

        LazyOptional<ResourceFilter> resourceFilter = stack.getCapability(EIOCapabilities.FILTER);

        capability = resourceFilter.map(filter -> {
            if (filter instanceof FluidFilterCapability cap) {
                return cap;
            }
            throw new IllegalArgumentException();

        }).orElseThrow(IllegalArgumentException::new);

        for (int i = 0; i < capability.size(); i++) {
            int pSlot = i;
            addSlot(new FluidFilterSlot(fluidStack -> capability.setEntry(pSlot, fluidStack) ,i ,14 + ( i % 5) * 18, 35 + 20 * ( i / 5)));
        }
        addInventorySlots(14,119, inventory);
    }

    public FluidFilterMenu(int pContainerId, Inventory inventory, ItemStack stack) {
        this(EIOMenus.FLUID_FILTER.get(), pContainerId, inventory, stack);
    }

    public FluidFilterMenu(int pContainerId, Inventory inventory, FilterAccess access) {
        this(EIOMenus.FLUID_FILTER.get(), pContainerId, inventory, access);
    }

    public static FluidFilterMenu factory(@Nullable MenuType<FluidFilterMenu> pMenuType, int pContainerId, Inventory inventory, @Nullable FriendlyByteBuf buf) {
        return new FluidFilterMenu(EIOMenus.FLUID_FILTER.get(), pContainerId, inventory, FilterAccess.fromNetwork(inventory, buf));
    }

    public FluidFilterCapability getFilter() {
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
                    capability.setEntry(slotId, FluidStack.EMPTY);
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
