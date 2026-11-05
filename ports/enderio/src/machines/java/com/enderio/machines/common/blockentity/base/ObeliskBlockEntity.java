package com.enderio.machines.common.blockentity.base;

import com.enderio.api.capacitor.ICapacitorScalable;
import com.enderio.api.filter.EntityFilter;
import com.enderio.api.io.IIOConfig;
import com.enderio.api.io.IOMode;
import com.enderio.api.io.energy.EnergyIOMode;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.particle.RangeParticleData;
import com.enderio.core.common.network.slot.BooleanNetworkDataSlot;
import com.enderio.core.common.network.slot.IntegerNetworkDataSlot;
import com.enderio.machines.common.blockentity.MachineState;
import com.enderio.machines.common.io.SidedFixedIOConfig;
import com.enderio.machines.common.io.item.MachineInventory;
import com.enderio.machines.common.io.item.SingleSlotAccess;
import com.enderio.machines.common.obelisk.IObeliskManagerCapability;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;

/**
 * Base of the area obelisks.
 * SkyTekx: the obelisk framework of 8.x on the 6.x base. The block entity registers itself with its level's obelisk
 * manager (75bb8b250, f216165c6), the range can follow a capacitor scaled max range (461cf0511) and an obelisk can pay a
 * per tick upkeep (461cf0511). Nothing here calls setChanged, sendBlockUpdated or a neighbour lookup from setLevel or
 * onLoad, see 4dd6e5e09.
 *
 * @param <T> the obelisk class itself, which its manager stores
 */
public abstract class ObeliskBlockEntity<T extends ObeliskBlockEntity<T>> extends PoweredMachineBlockEntity {

    private @Nullable AABB aabb;
    public static SingleSlotAccess FILTER = new SingleSlotAccess();

    // The max range that the server synced, read by the client for capacitor scaled ranges.
    private int clientMaxRange;

    // The max range seen by the last server tick, or -1 before the first tick.
    private int lastMaxRange = -1;

    public ObeliskBlockEntity(EnergyIOMode energyIOMode, ICapacitorScalable capacity, ICapacitorScalable usageRate, BlockEntityType<?> type,
        BlockPos worldPosition, BlockState blockState) {
        super(energyIOMode, capacity, usageRate, type, worldPosition, blockState);

        rangeVisibleDataSlot = new BooleanNetworkDataSlot(this::isRangeVisible, b -> this.rangeVisible = b);
        addClientDataSlot(rangeVisibleDataSlot);
        range = 4;

        // Server to client only. It goes before the range so that a sync never sees a range above the max.
        addDataSlot(new IntegerNetworkDataSlot(this::getMaxRange, r -> this.clientMaxRange = r));

        // On the server the range comes from a client, so clamp it. The client takes the server's value as it is.
        rangeDataSlot = new IntegerNetworkDataSlot(this::getRange, r -> this.range = level instanceof ServerLevel ? Mth.clamp(r, 0, getMaxRange()) : r) {
            @Override
            public void updateServerCallback() {
                updateLocations();
                setChanged();
            }
        };
        addClientDataSlot(rangeDataSlot);
    }

    /**
     * The obelisk manager of the given level that tracks this kind of obelisk, or null for an obelisk that needs none.
     * Each kind needs its own non-generic capability, see {@link IObeliskManagerCapability}.
     */
    @Nullable
    protected abstract IObeliskManagerCapability<T> getObeliskManager(ServerLevel level);

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);

        // Only in memory work here: no setChanged, block update or chunk access during a chunk load.
        updateAabb();
        if (level instanceof ServerLevel serverLevel) {
            IObeliskManagerCapability<T> manager = getObeliskManager(serverLevel);
            if (manager != null) {
                manager.register(self());
            }
        }

        updateFilterState();
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            IObeliskManagerCapability<T> manager = getObeliskManager(serverLevel);
            if (manager != null) {
                manager.unregister(self());
            }
        }

        super.setRemoved();
    }

    @Override
    protected boolean isActive() {
        return canAct()
            && getEnergyStorage().getEnergyStored() >= getPerTickEnergyCost()
            && (!requiresFilter() || getEntityFilter() != null);
    }

    /**
     * Whether the obelisk does nothing without an entity filter. 8.x obelisks require one, the 6.x Aversion Obelisk
     * acts on every mob without one.
     */
    public boolean requiresFilter() {
        return false;
    }

    @Nullable
    protected EntityFilter getEntityFilter() {
        // FILTER is shared by every obelisk layout. An obelisk without a filter slot (the inhibitor) may run before any
        // layout has set its index.
        MachineInventory inventory = getInventory();
        if (inventory == null || FILTER.getIndex() < 0 || FILTER.getIndex() >= inventory.getSlots()) {
            return null;
        }

        if (FILTER.getItemStack(inventory).getCapability(EIOCapabilities.FILTER).orElse(null) instanceof EntityFilter entityFilter) {
            return entityFilter;
        }

        return null;
    }

    /**
     * The energy that the obelisk draws each tick while it is active, 8.x scales it with the range (461cf0511).
     * 0 keeps the 6.x way of the Aversion Obelisk, which pays per cancelled spawn.
     */
    public int getPerTickEnergyCost() {
        return 0;
    }

    /**
     * The upkeep of 8.x: the max energy use scaled by the share of the max range in use.
     */
    protected final int getRangeScaledEnergyCost() {
        int maxRange = getMaxRange();
        if (maxRange <= 0) {
            return 0;
        }

        return Mth.ceil(getEnergyStorage().getMaxEnergyUse() * ((float) Mth.clamp(getRange(), 0, maxRange) / maxRange));
    }

    /**
     * A max range that scales with the capacitor. The client has no capacitor data, so it reads the value that the
     * server synced.
     */
    protected final int getCapacitorScaledMaxRange(ICapacitorScalable maxRange) {
        if (level != null && level.isClientSide()) {
            return clientMaxRange;
        }

        return Math.max(0, maxRange.scaleI(this::getCapacitorData).get());
    }

    protected void updateFilterState() {
        updateMachineState(MachineState.NO_ENTITY_FILTER, requiresFilter() && getEntityFilter() == null);
    }

    @Override
    protected void onInventoryContentsChanged(int slot) {
        super.onInventoryContentsChanged(slot);
        updateFilterState();
    }

    @SuppressWarnings("unused")
    public void setRangedActionData(int range, boolean rangeVisible) {
        if (level != null && level.isClientSide) {
            clientUpdateSlot(rangeVisibleDataSlot, rangeVisible);
            clientUpdateSlot(rangeDataSlot, range);
        } else {
            internalSetRangedActionData(range, rangeVisible);
        }
    }

    private void internalSetRangedActionData(int actionRange, boolean actionRangeVisible) {
        range = Mth.clamp(actionRange, 0, getMaxRange());
        rangeVisible = actionRangeVisible;
        updateLocations();
        setChanged();
    }

    @Override
    public void serverTick() {
        followMaxRange();
        updateMachineState(MachineState.ACTIVE, isActive());
        super.serverTick();

        int upkeep = getPerTickEnergyCost();
        if (upkeep > 0 && isActive()) {
            getEnergyStorage().consumeEnergy(upkeep, false);
        }
    }

    /**
     * Whether the range follows a change of the max range, for an obelisk whose max range scales with its capacitor.
     * Such an obelisk starts with a range of 0, which means not set, like DEFAULT_RANGE of 8.x (461cf0511).
     */
    protected boolean rangeFollowsMaxRange() {
        return false;
    }

    /**
     * Follows a change of the max range, as updateCapacitorData of 8.x does (461cf0511): a range of 0 (not set) or at
     * the old max grows to the new max, any other range stays inside the new max. The first tick after a load or
     * placement keeps a stored range in bounds and fills a range of 0. Only obelisks that opt in with
     * {@link #rangeFollowsMaxRange()} change their range here. The first tick of every obelisk also moves it in its
     * manager, which catches a range that a placed item's NBT loaded after setLevel. This runs from the server tick,
     * never from setLevel or onLoad, and changes the range without setChanged or a block update (4dd6e5e09). The data
     * slot sync of the same tick marks the block entity changed.
     */
    private void followMaxRange() {
        int maxRange = getMaxRange();
        boolean firstTick = lastMaxRange < 0;
        if (!firstTick && maxRange == lastMaxRange) {
            return;
        }

        int newRange = range;
        if (rangeFollowsMaxRange()) {
            if (range <= 0 || (!firstTick && range == lastMaxRange)) {
                newRange = maxRange;
            } else {
                newRange = Math.min(range, maxRange);
            }

            newRange = Math.max(newRange, 0);
        }

        lastMaxRange = maxRange;
        if (firstTick || newRange != range) {
            range = newRange;
            updateLocations();
        }
    }

    @Override
    public void clientTick() {
        if (level instanceof ClientLevel clientLevel) {
            if (!rangeVisible) {
                return;
            }

            BlockPos pos = getBlockPos();
            if (clientLevel.isClientSide()) {
                clientLevel.addAlwaysVisibleParticle(new RangeParticleData(range, getColor()), true, pos.getX(), pos.getY(), pos.getZ(), 0, 0, 0);
            }
        }

        super.clientTick();
    }

    public abstract String getColor();

    public @Nullable AABB getAABB() {
        return aabb;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateLocations();
    }

    private void updateAabb() {
        aabb = new AABB(getBlockPos()).inflate(getRange());
    }

    /**
     * Recomputes the area and moves the obelisk in its manager. Never calls setChanged.
     */
    protected void updateLocations() {
        updateAabb();

        if (level instanceof ServerLevel serverLevel) {
            IObeliskManagerCapability<T> manager = getObeliskManager(serverLevel);
            if (manager != null) {
                manager.update(self());
            }
        }
    }

    @Override
    protected IIOConfig createIOConfig() {
        return new SidedFixedIOConfig(dir -> IOMode.PULL);
    }
}
