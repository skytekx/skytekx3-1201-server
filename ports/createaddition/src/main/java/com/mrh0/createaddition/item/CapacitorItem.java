package com.mrh0.createaddition.item;

import com.mrh0.createaddition.config.Config;
import com.mrh0.createaddition.util.Util;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Capacitor as an FE item (upstream 5c2b9ea and 51e4a38, release 1.6.0).
 * <p>
 * The energy lives in the stack NBT key {@code energy}. An empty capacitor carries no such key, and
 * no tag at all when nothing else is in it, so it stacks with the capacitors of older builds.
 * Unlike upstream (16), an empty capacitor stacks to 64 as before, and a charged one to 1. The
 * storage only charges and discharges a single capacitor, because upstream's storage treats a
 * whole stack as one item, which then splits into several charged capacitors.
 */
public class CapacitorItem extends Item {
    public static final String ENERGY_KEY = "energy";

    public CapacitorItem(Properties props) {
        super(props);
    }

    public static int getEnergy(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        return tag.getInt(ENERGY_KEY);
    }

    public static void setEnergy(ItemStack stack, int energy) {
        if (energy <= 0) {
            CompoundTag tag = stack.getTag();
            if (tag == null) return;
            tag.remove(ENERGY_KEY);
            // An empty tag does not stack with no tag, so drop it.
            if (tag.isEmpty()) stack.setTag(null);
        } else {
            stack.getOrCreateTag().putInt(ENERGY_KEY, Math.min(energy, Config.CAPACITOR_CAPACITY.get()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        stack.getCapability(ForgeCapabilities.ENERGY).ifPresent(es -> tooltip.add(Util.getTextComponent(es)));
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getEnergy(stack) > 0 ? 1 : super.getMaxStackSize(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEnergy(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(13, Math.round((float) getEnergy(stack) / Config.CAPACITOR_CAPACITY.get() * 13f));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x00BFFF;
    }

    // Forge 47 form of upstream's RegisterCapabilitiesEvent.registerItem. The provider is not
    // serializable, the storage reads and writes the stack tag itself, so no ForgeCaps tag appears.
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new CapacitorCapabilityProvider(stack);
    }

    private static class CapacitorCapabilityProvider implements ICapabilityProvider {
        private final LazyOptional<IEnergyStorage> energy;

        private CapacitorCapabilityProvider(ItemStack stack) {
            this.energy = LazyOptional.of(() -> new CapacitorEnergyStorage(stack));
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return ForgeCapabilities.ENERGY.orEmpty(cap, energy);
        }
    }

    public static class CapacitorEnergyStorage implements IEnergyStorage {
        private final ItemStack stack;

        public CapacitorEnergyStorage(ItemStack stack) {
            this.stack = stack;
        }

        // Local fix: only a single capacitor moves energy, see the class comment.
        private boolean isSingle() {
            return stack.getCount() == 1;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!isSingle()) return 0;
            int capacity = Config.CAPACITOR_CAPACITY.get();
            int stored = getEnergy(stack);
            int accepted = Math.min(maxReceive, Math.min(Config.CAPACITOR_CHARGE_RATE.get(), capacity - stored));
            if (accepted <= 0) return 0;
            if (!simulate) setEnergy(stack, stored + accepted);
            return accepted;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!isSingle()) return 0;
            int stored = getEnergy(stack);
            int extracted = Math.min(maxExtract, Math.min(Config.CAPACITOR_CHARGE_RATE.get(), stored));
            if (extracted <= 0) return 0;
            if (!simulate) setEnergy(stack, stored - extracted);
            return extracted;
        }

        @Override public int getEnergyStored() { return getEnergy(stack); }
        @Override public int getMaxEnergyStored() { return Config.CAPACITOR_CAPACITY.get(); }
        @Override public boolean canExtract() { return isSingle(); }
        @Override public boolean canReceive() { return isSingle(); }
    }
}
