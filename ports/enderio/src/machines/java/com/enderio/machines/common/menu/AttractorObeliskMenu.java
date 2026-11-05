package com.enderio.machines.common.menu;

import com.enderio.machines.common.blockentity.AttractorObeliskBlockEntity;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx: the Attractor Obelisk menu of 8.x (461cf0511): the capacitor slot first, the filter slot second.
 */
public class AttractorObeliskMenu extends MachineMenu<AttractorObeliskBlockEntity> {

    public AttractorObeliskMenu(@Nullable AttractorObeliskBlockEntity blockEntity, Inventory inventory, int pContainerId) {
        super(blockEntity, inventory, MachineMenus.ATTRACTOR_OBELISK.get(), pContainerId);

        if (blockEntity != null) {
            if (blockEntity.requiresCapacitor()) {
                addSlot(new MachineSlot(blockEntity.getInventory(), blockEntity.getCapacitorSlot(), 12, 60));
            }

            addSlot(new MachineSlot(blockEntity.getInventory(), AttractorObeliskBlockEntity.FILTER, 81, 31));
        }

        addInventorySlots(8, 84);
    }

    public static AttractorObeliskMenu factory(@Nullable MenuType<AttractorObeliskMenu> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buf.readBlockPos());
        if (entity instanceof AttractorObeliskBlockEntity castBlockEntity) {
            return new AttractorObeliskMenu(castBlockEntity, inventory, pContainerId);
        }

        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new AttractorObeliskMenu(null, inventory, pContainerId);
    }
}
