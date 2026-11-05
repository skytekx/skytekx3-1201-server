package com.enderio.conduits.common.conduit.type.redstone;

import com.enderio.api.conduit.ConduitData;
import com.enderio.api.filter.ResourceFilter;
import com.enderio.api.misc.ColorControl;
import com.enderio.conduits.common.redstone.RedstoneInsertFilter;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RedstoneConduitData implements ConduitData<RedstoneConduitData> {

    private static final int ALL_SIDES_MASK = (1 << Direction.values().length) - 1;

    private boolean isActive = false;
    private final EnumMap<ColorControl, Integer> activeColors = new EnumMap<>(ColorControl.class);

    // Ender IO 8.0 (isStrongOutputSignal of the redstone connection config): the sides whose output is a strong signal, one
    // bit per Direction.get3DDataValue(). A strong signal also powers the neighbours of the block that the side faces.
    private byte strongSides = 0;

    // Server state, not saved: the strong output of each side at the last tick, so the ticker notifies the neighbours of the
    // block behind a side only when that output changes.
    private final int[] lastStrongOutput = new int[Direction.values().length];

    // region Serialization

    private static final String KEY_ACTIVE = "Active";
    private static final String KEY_COLORED_ACTIVE = "ColoredActive";
    private static final String KEY_ACTIVE_COLOR_MAP = "ActiveColors";
    private static final String KEY_STRONG_SIDES = "StrongSides";

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(KEY_ACTIVE, isActive);

        CompoundTag activeColorMap = new CompoundTag();
        for (Map.Entry<ColorControl, Integer> entry : activeColors.entrySet()) {
            activeColorMap.put(entry.getKey().name(), IntTag.valueOf(entry.getValue()));
        }

        nbt.put(KEY_ACTIVE_COLOR_MAP, activeColorMap);
        if (strongSides != 0) {
            nbt.putByte(KEY_STRONG_SIDES, strongSides);
        }
        return nbt;
    }

    /**
     * The screen data is the strong sides and nothing else.
     */
    @Override
    public CompoundTag serializeGuiNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte(KEY_STRONG_SIDES, strongSides);
        return nbt;
    }

    @Override
    public void applyGuiNBT(CompoundTag nbt) {
        // Take only the strong sides. The signal state is server state.
        if (nbt.contains(KEY_STRONG_SIDES, Tag.TAG_BYTE)) {
            strongSides = (byte) (nbt.getByte(KEY_STRONG_SIDES) & ALL_SIDES_MASK);
        }
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        isActive = nbt.getBoolean(KEY_ACTIVE);
        strongSides = (byte) (nbt.getByte(KEY_STRONG_SIDES) & ALL_SIDES_MASK);
        activeColors.clear();

        // Pre 6.1 compatibility - just populates with 15
        if (nbt.contains(KEY_COLORED_ACTIVE, Tag.TAG_LIST)) {
            ListTag list = nbt.getList(KEY_COLORED_ACTIVE, Tag.TAG_INT);
            for (Tag tag : list) {
                if (tag instanceof IntTag intTag) {
                    int intValue = intTag.getAsInt();
                    if (intValue < 0 || intValue >= ColorControl.values().length) {
                        continue;
                    }

                    ColorControl signalColor = ColorControl.values()[intValue];
                    activeColors.put(signalColor, 15);
                }
            }
        }

        // 6.1
        if (nbt.contains(KEY_ACTIVE_COLOR_MAP, Tag.TAG_COMPOUND)) {
            CompoundTag activeColorMap = nbt.getCompound(KEY_ACTIVE_COLOR_MAP);
            for (ColorControl color : ColorControl.values()) {
                if (activeColorMap.contains(color.name(), Tag.TAG_INT)) {
                    activeColors.put(color, activeColorMap.getInt(color.name()));
                } else {
                    activeColors.put(color, 0);
                }
            }
        }
    }

    // endregion

    public boolean isActive() {
        return isActive;
    }

    public boolean isActive(ColorControl color) {
        return activeColors.containsKey(color);
    }

    public int getSignal(ColorControl color) {
        return activeColors.getOrDefault(color, 0);
    }

    public void clearActive() {
        activeColors.clear();
        isActive = false;
    }

    public void setActiveColor(ColorControl color, int signal) {
        if (activeColors.containsKey(color)) {
            return;
        }

        isActive = true;
        activeColors.put(color, signal);
    }

    /**
     * The signal that an output (insert) side on this channel puts out, through its insert filter if it has one.
     */
    public int getOutputSignal(ColorControl channel, @Nullable ResourceFilter insertFilter) {
        if (insertFilter instanceof RedstoneInsertFilter redstoneInsertFilter) {
            return redstoneInsertFilter.getOutputSignal(this, channel);
        }
        return getSignal(channel);
    }

    public boolean isStrong(Direction side) {
        return (strongSides & (1 << side.get3DDataValue())) != 0;
    }

    public void setStrong(Direction side, boolean isStrong) {
        if (isStrong) {
            strongSides = (byte) (strongSides | (1 << side.get3DDataValue()));
        } else {
            strongSides = (byte) (strongSides & ~(1 << side.get3DDataValue()));
        }
    }

    /**
     * @return whether the side put out a strong signal at the last tick
     */
    public boolean hasStrongOutput(Direction side) {
        return lastStrongOutput[side.get3DDataValue()] > 0;
    }

    /**
     * Record the strong output of a side.
     * @return true when it differs from the output at the last tick
     */
    public boolean updateStrongOutput(Direction side, int output) {
        int index = side.get3DDataValue();
        if (lastStrongOutput[index] == output) {
            return false;
        }
        lastStrongOutput[index] = output;
        return true;
    }

    public RedstoneConduitData deepCopy() {
        RedstoneConduitData redstoneConduitData = new RedstoneConduitData();
        redstoneConduitData.isActive = isActive;
        redstoneConduitData.strongSides = strongSides;
        return redstoneConduitData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isActive, activeColors, strongSides);
    }
}
