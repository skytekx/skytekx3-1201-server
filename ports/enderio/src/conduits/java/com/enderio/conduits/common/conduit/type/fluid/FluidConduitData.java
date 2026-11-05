package com.enderio.conduits.common.conduit.type.fluid;

import com.enderio.EnderIO;
import com.enderio.api.conduit.ConduitData;
import com.enderio.api.conduit.ConduitType;
import com.enderio.conduits.ConduitNBTKeys;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class FluidConduitData implements ConduitData<FluidConduitData> {

    public boolean isMultiFluid;

    @Nullable
    private Fluid lockedFluid = null;
    private boolean shouldReset = false;

    // Ender IO 8.x 6ecc1c788: the insert priority of each side. Only the conduits whose type supports priority use it.
    private final Map<Direction, FluidSidedData> fluidSidedData = new EnumMap<>(Direction.class);

    public FluidConduitData(boolean isMultiFluid) {
        this.isMultiFluid = isMultiFluid;
    }

    @Nullable
    public Fluid lockedFluid() {
        return lockedFluid;
    }

    public void setLockedFluid(@Nullable Fluid lockedFluid) {
        this.lockedFluid = lockedFluid;
    }

    public boolean shouldReset() {
        return shouldReset;
    }

    public void setShouldReset(boolean shouldReset) {
        this.shouldReset = shouldReset;
    }

    public FluidSidedData compute(Direction direction) {
        return fluidSidedData.computeIfAbsent(direction, dir -> new FluidSidedData());
    }

    /**
     * @return the insert priority of a side, without creating side data for it
     */
    public int getPriority(Direction direction) {
        @Nullable FluidSidedData sidedData = fluidSidedData.get(direction);
        return sidedData != null ? sidedData.getPriority() : 0;
    }

    @Override
    public void onCreated(ConduitType<?> type, Level level, BlockPos pos, @Nullable Player player) {
        if (type instanceof FluidConduitType fluidType) {
            this.isMultiFluid = fluidType.isMultiFluid();
            if (this.isMultiFluid) {
                // remove locked fluid
                setShouldReset(true);
            }
        }
    }

    @Override
    public void onConnectTo(FluidConduitData otherData) {
        if (lockedFluid != null) {
            if (otherData.lockedFluid != null && lockedFluid != otherData.lockedFluid) {
                EnderIO.LOGGER.warn("incompatible fluid conduits merged");
            }
            otherData.setLockedFluid(lockedFluid);
        } else if (otherData.lockedFluid != null) {
            setLockedFluid(otherData.lockedFluid);
        }
    }

    @Override
    public boolean canConnectTo(FluidConduitData otherData) {
        return lockedFluid == null || otherData.lockedFluid == null || lockedFluid == otherData.lockedFluid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isMultiFluid, shouldReset, lockedFluid, fluidSidedData);
    }

    // region Serialization

    private static final String SHOULD_RESET = "ShouldReset";

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        if (!isMultiFluid) {
            if (lockedFluid != null) {
                nbt.putString(ConduitNBTKeys.FLUID, ForgeRegistries.FLUIDS.getKey(lockedFluid).toString());
            } else {
                nbt.putString(ConduitNBTKeys.FLUID, "null");
            }
        }
        for (Direction direction : Direction.values()) {
            @Nullable FluidSidedData sidedData = fluidSidedData.get(direction);
            if (sidedData != null) {
                nbt.put(direction.name(), sidedData.toNbt());
            }
        }
        return nbt;
    }

    @Override
    public CompoundTag serializeGuiNBT() {
        CompoundTag nbt = serializeNBT();
        nbt.putBoolean(SHOULD_RESET, shouldReset);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains(ConduitNBTKeys.FLUID) && !isMultiFluid) {
            String fluid = nbt.getString(ConduitNBTKeys.FLUID);
            if (fluid.equals("null") || ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluid)) == Fluids.EMPTY) {
                setLockedFluid(null);
            } else {
                setLockedFluid(ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluid)));
            }
        } else {
            setLockedFluid(null);
        }
        if (nbt.contains(SHOULD_RESET)) {
            shouldReset = nbt.getBoolean(SHOULD_RESET);
        }
        fluidSidedData.clear();
        for (Direction direction : Direction.values()) {
            if (nbt.contains(direction.name(), Tag.TAG_COMPOUND)) {
                fluidSidedData.put(direction, FluidSidedData.fromNbt(nbt.getCompound(direction.name())));
            }
        }
    }

    /**
     * The screen can ask for a reset of the locked fluid and change the priorities, nothing else. The locked fluid itself is
     * set by the ticker, so the client's view of it is ignored, and a stale screen cannot cancel a reset that is still pending.
     */
    @Override
    public void applyGuiNBT(CompoundTag nbt) {
        if (nbt.getBoolean(SHOULD_RESET)) {
            shouldReset = true;
        }
        for (Direction direction : Direction.values()) {
            if (nbt.contains(direction.name(), Tag.TAG_COMPOUND)) {
                CompoundTag sidedTag = nbt.getCompound(direction.name());
                if (sidedTag.contains(FluidSidedData.KEY_PRIORITY, Tag.TAG_INT)) {
                    compute(direction).setPriority(sidedTag.getInt(FluidSidedData.KEY_PRIORITY));
                }
            }
        }
    }

    public static class FluidSidedData {
        public static final int MIN_PRIORITY = -9999;
        public static final int MAX_PRIORITY = 9999;

        private static final String KEY_PRIORITY = "Priority";

        private int priority = 0;

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = Mth.clamp(priority, MIN_PRIORITY, MAX_PRIORITY);
        }

        @Override
        public int hashCode() {
            return Objects.hash(priority);
        }

        private CompoundTag toNbt() {
            CompoundTag nbt = new CompoundTag();
            nbt.putInt(KEY_PRIORITY, priority);
            return nbt;
        }

        private static FluidSidedData fromNbt(CompoundTag nbt) {
            FluidSidedData sidedData = new FluidSidedData();
            if (nbt.contains(KEY_PRIORITY, Tag.TAG_INT)) {
                sidedData.setPriority(nbt.getInt(KEY_PRIORITY));
            }
            return sidedData;
        }
    }

    // endregion
}
