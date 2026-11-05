package com.enderio.machines.common.menu;

import com.enderio.machines.common.blockentity.WirelessChargerBlockEntity;
import com.enderio.machines.common.init.MachineMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx: the Wireless Charger menu of 8.x (4ad73fff0), only the capacitor slot. The range and its visibility change
 * through the client data slots of the block entity, as on the other 6.x ranged machines.
 */
public class WirelessChargerMenu extends MachineMenu<WirelessChargerBlockEntity> {

    public WirelessChargerMenu(@Nullable WirelessChargerBlockEntity blockEntity, Inventory inventory, int pContainerId) {
        super(blockEntity, inventory, MachineMenus.WIRELESS_CHARGER.get(), pContainerId);

        if (blockEntity != null && blockEntity.requiresCapacitor()) {
            addSlot(new MachineSlot(blockEntity.getInventory(), blockEntity.getCapacitorSlot(), 12, 60));
        }

        addInventorySlots(8, 84);
    }

    public static WirelessChargerMenu factory(@Nullable MenuType<WirelessChargerMenu> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buf.readBlockPos());
        if (entity instanceof WirelessChargerBlockEntity castBlockEntity) {
            return new WirelessChargerMenu(castBlockEntity, inventory, pContainerId);
        }

        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new WirelessChargerMenu(null, inventory, pContainerId);
    }
}
