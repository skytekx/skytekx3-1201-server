package com.enderio.machines.common.menu;

import com.enderio.machines.common.blockentity.InhibitorObeliskBlockEntity;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx: the Inhibitor Obelisk menu of 8.x (f216165c6), only the capacitor slot.
 */
public class InhibitorObeliskMenu extends MachineMenu<InhibitorObeliskBlockEntity> {

    public InhibitorObeliskMenu(@Nullable InhibitorObeliskBlockEntity blockEntity, Inventory inventory, int pContainerId) {
        super(blockEntity, inventory, MachineMenus.INHIBITOR_OBELISK.get(), pContainerId);

        if (blockEntity != null && blockEntity.requiresCapacitor()) {
            addSlot(new MachineSlot(blockEntity.getInventory(), blockEntity.getCapacitorSlot(), 12, 60));
        }

        addInventorySlots(8, 84);
    }

    public static InhibitorObeliskMenu factory(@Nullable MenuType<InhibitorObeliskMenu> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buf.readBlockPos());
        if (entity instanceof InhibitorObeliskBlockEntity castBlockEntity) {
            return new InhibitorObeliskMenu(castBlockEntity, inventory, pContainerId);
        }

        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new InhibitorObeliskMenu(null, inventory, pContainerId);
    }
}
