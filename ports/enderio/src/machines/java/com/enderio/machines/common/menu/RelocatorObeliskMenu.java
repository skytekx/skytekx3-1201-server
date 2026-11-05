package com.enderio.machines.common.menu;

import com.enderio.machines.common.blockentity.RelocatorObeliskBlockEntity;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx: the Relocator Obelisk menu of 8.x (461cf0511): the capacitor slot first, the filter slot second.
 */
public class RelocatorObeliskMenu extends MachineMenu<RelocatorObeliskBlockEntity> {

    public RelocatorObeliskMenu(@Nullable RelocatorObeliskBlockEntity blockEntity, Inventory inventory, int pContainerId) {
        super(blockEntity, inventory, MachineMenus.RELOCATOR_OBELISK.get(), pContainerId);

        if (blockEntity != null) {
            if (blockEntity.requiresCapacitor()) {
                addSlot(new MachineSlot(blockEntity.getInventory(), blockEntity.getCapacitorSlot(), 12, 60));
            }

            addSlot(new MachineSlot(blockEntity.getInventory(), RelocatorObeliskBlockEntity.FILTER, 81, 31));
        }

        addInventorySlots(8, 84);
    }

    public static RelocatorObeliskMenu factory(@Nullable MenuType<RelocatorObeliskMenu> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buf.readBlockPos());
        if (entity instanceof RelocatorObeliskBlockEntity castBlockEntity) {
            return new RelocatorObeliskMenu(castBlockEntity, inventory, pContainerId);
        }

        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new RelocatorObeliskMenu(null, inventory, pContainerId);
    }
}
