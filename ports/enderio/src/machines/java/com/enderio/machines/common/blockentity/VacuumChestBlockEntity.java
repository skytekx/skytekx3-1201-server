package com.enderio.machines.common.blockentity;

import com.enderio.api.filter.ItemStackFilter;
import com.enderio.api.filter.LimitingItemStackFilter;
import com.enderio.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.machines.common.blockentity.base.VacuumMachineBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.io.item.MachineInventoryLayout.Builder;
import com.enderio.machines.common.io.item.SingleSlotAccess;
import com.enderio.machines.common.menu.VacuumChestMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class VacuumChestBlockEntity extends VacuumMachineBlockEntity<ItemEntity> {

    public static final SingleSlotAccess FILTER_SLOT = new SingleSlotAccess();

    public VacuumChestBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState, ItemEntity.class);
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new VacuumChestMenu(this, inventory, containerId);
    }

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        return extractableGUISlot(MachineInventoryLayout.builder(), 27)
            .slot(slot -> slot.guiInsert().guiExtract().filter(this::acceptFilter))
            .slotAccess(FILTER_SLOT)
            .build();
    }

    private boolean acceptFilter(int slot, ItemStack itemStack) {
        return itemStack.getCapability(EIOCapabilities.FILTER)
            .map(filter -> filter instanceof ItemStackFilter)
            .orElse(false);
    }

    @Override
    public void handleEntity(ItemEntity entity) {
        // SkyTekx: a limited item filter lets in only what keeps the chest at its stock level (8.x 292bd84ed).
        if (getItemFilter() instanceof LimitingItemStackFilter limitingFilter) {
            handleLimitedEntity(entity, limitingFilter);
            return;
        }

        for (int i = 0; i < this.getInventory().getSlots(); i++) {
            ItemStack reminder = this.getInventory().insertItem(i, entity.getItem().copy(), false);
            if (reminder.isEmpty()) {
                entity.discard();
                return;
            } else {
                entity.getItem().setCount(reminder.getCount());
            }
        }
    }

    /**
     * Inserts at most the transfer limit of the filter and leaves the rest of the stack on the ground. Upstream sets the
     * entity count to the remainder of the limited stack and discards the entity when that was empty, which deletes the
     * items above the limit, so this counts what each slot took instead.
     */
    private void handleLimitedEntity(ItemEntity entity, LimitingItemStackFilter filter) {
        int allowance = filter.getTransferLimit(entity.getItem(), getInventory(), true);
        for (int i = 0; i < this.getInventory().getSlots() && allowance > 0; i++) {
            ItemStack item = entity.getItem();
            ItemStack offered = item.copyWithCount(Math.min(allowance, item.getCount()));
            ItemStack remainder = this.getInventory().insertItem(i, offered, false);
            int inserted = offered.getCount() - remainder.getCount();
            if (inserted <= 0) {
                continue;
            }

            allowance -= inserted;
            if (inserted >= item.getCount()) {
                entity.discard();
                return;
            }

            ItemStack rest = item.copy();
            rest.shrink(inserted);
            entity.setItem(rest);
        }
    }

    @Nullable
    private ResourceFilter getItemFilter() {
        return FILTER_SLOT.getItemStack(this).getCapability(EIOCapabilities.FILTER).resolve().orElse(null);
    }

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.VACUUM_CHEST_RANGE_COLOR.get();
    }

    @Override
    public Predicate<ItemEntity> getFilter() {
        ItemStack filterItemStack = FILTER_SLOT.getItemStack(this);
        LazyOptional<ResourceFilter> resourceFilter = filterItemStack.getCapability(EIOCapabilities.FILTER);

        return resourceFilter
            .map(filter -> {
                if (filter instanceof LimitingItemStackFilter limitingFilter) {
                    return (Predicate<ItemEntity>)(ItemEntity itemEntity) -> limitingFilter.test(itemEntity.getItem())
                        && limitingFilter.getTransferLimit(itemEntity.getItem(), getInventory(), true) > 0;
                }

                if (filter instanceof ItemStackFilter itemStackFilter) {
                    return (Predicate<ItemEntity>)(ItemEntity itemEntity) -> itemStackFilter.test(itemEntity.getItem());
                }

                return ITEM_ENTITY_FILTER_TRUE;
            })
            .orElse(ITEM_ENTITY_FILTER_TRUE);
    }

    // Slot config

    public Builder extractableGUISlot(Builder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.slot(slot -> slot.guiInsert().guiExtract().extract());
        }
        return builder;
    }
}
