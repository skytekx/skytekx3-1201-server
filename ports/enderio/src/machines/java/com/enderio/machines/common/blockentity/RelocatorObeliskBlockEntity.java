package com.enderio.machines.common.blockentity;

import com.enderio.api.capacitor.CapacitorModifier;
import com.enderio.api.capacitor.LinearScalable;
import com.enderio.api.capacitor.QuadraticScalable;
import com.enderio.api.filter.EntityFilter;
import com.enderio.api.io.energy.EnergyIOMode;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.machines.common.blockentity.base.ObeliskBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.menu.RelocatorObeliskMenu;
import com.enderio.machines.common.obelisk.IObeliskManagerCapability;
import com.enderio.machines.common.obelisk.RelocatorObeliskManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx: the Relocator Obelisk of 8.x (f216165c6, 461cf0511) on the 6.x obelisk base. Natural spawns of the
 * filtered mobs in its area appear next to the obelisk instead. It needs a soul filter and pays a per tick upkeep.
 */
public class RelocatorObeliskBlockEntity extends ObeliskBlockEntity<RelocatorObeliskBlockEntity> {

    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY, MachinesConfig.COMMON.ENERGY.RELOCATOR_CAPACITY);
    private static final LinearScalable ENERGY_USAGE = new LinearScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.ENERGY.RELOCATOR_USAGE);
    private static final LinearScalable RANGE = new LinearScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.RELOCATOR_RANGE);

    public RelocatorObeliskBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(EnergyIOMode.Input, ENERGY_CAPACITY, ENERGY_USAGE, type, worldPosition, blockState);
        // 0 means not set, the first tick with a capacitor grows it to the max range (8.x DEFAULT_RANGE).
        range = 0;
    }

    @Override
    protected @Nullable IObeliskManagerCapability<RelocatorObeliskBlockEntity> getObeliskManager(ServerLevel level) {
        return RelocatorObeliskManager.getManager(level).resolve().orElse(null);
    }

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
            .inputSlot((integer, itemStack) -> itemStack.getCapability(EIOCapabilities.FILTER).orElse(null) instanceof EntityFilter)
            .slotAccess(FILTER)
            .capacitor()
            .build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new RelocatorObeliskMenu(this, pPlayerInventory, pContainerId);
    }

    @Override
    public boolean requiresFilter() {
        return true;
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
        return MachinesConfig.CLIENT.BLOCKS.RELOCATOR_RANGE_COLOR.get();
    }

    /**
     * Moves a filtered mob that spawns naturally in the area to a random spot next to the obelisk.
     * The mob is not in the level yet during FinalizeSpawn, NaturalSpawner adds it after the event. So the position is
     * set with moveTo and not teleportTo, which would also run the Bukkit teleport handling on Arclight. Like upstream,
     * the new spot is not checked for collisions.
     */
    public boolean handleSpawnEvent(MobSpawnEvent.FinalizeSpawn event) {
        AABB aabb = getAABB();
        if (level == null || aabb == null || !isActive()) {
            return false;
        }

        Mob mob = event.getEntity();
        EntityFilter filter = getEntityFilter();
        if (filter == null || !filter.test(mob)) {
            return false;
        }

        if (!aabb.contains(event.getX(), event.getY(), event.getZ())) {
            return false;
        }

        RandomSource random = level.getRandom();
        double x = getBlockPos().getX() + (random.nextDouble() - random.nextDouble()) * 5 + 0.5D;
        double y = getBlockPos().getY() + random.nextInt(3) - 1;
        double z = getBlockPos().getZ() + (random.nextDouble() - random.nextDouble()) * 5 + 0.5D;

        EntityTeleportEvent teleportEvent = new EntityTeleportEvent(mob, x, y, z);
        if (MinecraftForge.EVENT_BUS.post(teleportEvent)) {
            return false;
        }

        mob.moveTo(teleportEvent.getTargetX(), teleportEvent.getTargetY(), teleportEvent.getTargetZ());
        return true;
    }
}
