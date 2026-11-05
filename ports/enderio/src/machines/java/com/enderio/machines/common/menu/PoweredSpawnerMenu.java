package com.enderio.machines.common.menu;

import com.enderio.machines.common.blockentity.PoweredSpawnerBlockEntity;
import com.enderio.machines.common.blockentity.PoweredSpawnerMode;
import com.enderio.machines.common.init.MachineMenus;
import com.enderio.machines.common.io.item.SingleSlotAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.Nullable;

public class PoweredSpawnerMenu extends MachineMenu<PoweredSpawnerBlockEntity> {

    public PoweredSpawnerMenu(@Nullable PoweredSpawnerBlockEntity blockEntity, Inventory inventory, int pContainerId) {
        super(blockEntity, inventory, MachineMenus.POWERED_SPAWNER.get(), pContainerId);

        if (blockEntity != null) {
            if (blockEntity.requiresCapacitor()) {
                addSlot(new MachineSlot(blockEntity.getInventory(), blockEntity.getCapacitorSlot(), 12, 60));
            }

            // SkyTekx: the capture mode slots of 8.x (a90e5c512), at the slots of the capture screen texture.
            addSlot(new SpawnerSlot(blockEntity, PoweredSpawnerBlockEntity.INPUT, 54, 42));
            addSlot(new SpawnerSlot(blockEntity, PoweredSpawnerBlockEntity.OUTPUT, 105, 42));
        }

        addInventorySlots(8,84);
    }

    public static PoweredSpawnerMenu factory(@Nullable MenuType<PoweredSpawnerMenu> pMenuType, int pContainerId, Inventory inventory, FriendlyByteBuf buf) {
        BlockEntity entity = inventory.player.level().getBlockEntity(buf.readBlockPos());
        if (entity instanceof PoweredSpawnerBlockEntity castBlockEntity) {
            return new PoweredSpawnerMenu(castBlockEntity, inventory, pContainerId);
        }

        LogManager.getLogger().warn("couldn't find BlockEntity");
        return new PoweredSpawnerMenu(null, inventory, pContainerId);
    }

    /**
     * SkyTekx: a slot of capture mode. In spawn mode it is hidden and takes nothing from the player, also not by a shift
     * click, as the SpawnerSlot of 8.x and its canQuickInsertStack check (a90e5c512). The items in it stay.
     */
    public static class SpawnerSlot extends MachineSlot {
        private final PoweredSpawnerBlockEntity blockEntity;

        public SpawnerSlot(PoweredSpawnerBlockEntity blockEntity, SingleSlotAccess access, int xPosition, int yPosition) {
            super(blockEntity.getInventory(), access, xPosition, yPosition);
            this.blockEntity = blockEntity;
        }

        @Override
        public boolean isActive() {
            return blockEntity.getMode() == PoweredSpawnerMode.CAPTURE;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isActive() && super.mayPlace(stack);
        }

        @Override
        public boolean canQuickInsertStack() {
            return isActive() && super.canQuickInsertStack();
        }
    }
}
