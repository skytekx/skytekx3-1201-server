package com.enderio.machines.common.blockentity;

import com.enderio.api.capacitor.CapacitorModifier;
import com.enderio.api.capacitor.LinearScalable;
import com.enderio.api.capacitor.QuadraticScalable;
import com.enderio.api.io.energy.EnergyIOMode;
import com.enderio.machines.common.blockentity.base.ObeliskBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.menu.InhibitorObeliskMenu;
import com.enderio.machines.common.obelisk.IObeliskManagerCapability;
import com.enderio.machines.common.obelisk.InhibitorObeliskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx: the Inhibitor Obelisk of 8.x (f216165c6, d8261275b, a0189250c, 8254bda11, 71328eba8, 461cf0511) on the
 * 6.x obelisk base. It stops teleports into and out of its area. It pays a per tick upkeep and nothing per cancelled
 * teleport, as upstream does since 461cf0511.
 */
public class InhibitorObeliskBlockEntity extends ObeliskBlockEntity<InhibitorObeliskBlockEntity> {

    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY, MachinesConfig.COMMON.ENERGY.INHIBITOR_CAPACITY);
    private static final LinearScalable ENERGY_USAGE = new LinearScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.ENERGY.INHIBITOR_USAGE);
    private static final LinearScalable RANGE = new LinearScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.INHIBITOR_RANGE);

    public InhibitorObeliskBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(EnergyIOMode.Input, ENERGY_CAPACITY, ENERGY_USAGE, type, worldPosition, blockState);
        // 0 means not set, the first tick with a capacitor grows it to the max range (8.x DEFAULT_RANGE).
        range = 0;
    }

    @Override
    protected @Nullable IObeliskManagerCapability<InhibitorObeliskBlockEntity> getObeliskManager(ServerLevel level) {
        return InhibitorObeliskManager.getManager(level).resolve().orElse(null);
    }

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
            .capacitor()
            .build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new InhibitorObeliskMenu(this, pPlayerInventory, pContainerId);
    }

    @Override
    public int getMaxRange() {
        return getCapacitorScaledMaxRange(RANGE);
    }

    @Override
    protected boolean rangeFollowsMaxRange() {
        return true;
    }

    @Override
    public int getPerTickEnergyCost() {
        return getRangeScaledEnergyCost();
    }

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.INHIBITOR_RANGE_COLOR.get();
    }

    public boolean handleTeleportEvent(EntityTeleportEvent event) {
        AABB aabb = getAABB();
        if (aabb != null && isActive() && (aabb.contains(event.getTargetX(), event.getTargetY(), event.getTargetZ())
            || aabb.contains(event.getPrevX(), event.getPrevY(), event.getPrevZ()))) {
            event.setCanceled(true);
            return true;
        }

        return false;
    }
}
