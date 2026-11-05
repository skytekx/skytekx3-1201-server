package com.enderio.machines.common.item;

import com.enderio.core.CoreNBTKeys;
import com.enderio.machines.common.block.CapacitorBankBlock;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CapacitorBankItem extends BlockItem {

    public CapacitorBankItem(CapacitorBankBlock pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new BlockEntityEnergyStorage(stack, ((CapacitorBankBlock)getBlock()).getTier().getStorageCapacity());
    }

    private static class BlockEntityEnergyStorage implements IEnergyStorage, ICapabilityProvider {

        private final ItemStack container;
        private final int capacity;
        BlockEntityEnergyStorage(ItemStack container, int capacity) {
            this.container = container;
            this.capacity = capacity;
        }

        // Every item of a stack shares the one tag, and every bank placed from the stack gets the stored value.
        // So the stored value counts per item, and a transfer is split evenly across the items of the stack.

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int count = container.getCount();
            if (count <= 0 || maxReceive <= 0) {
                return 0;
            }

            int stored = getEnergyStored();
            int received = Math.max(0, Math.min(maxReceive / count, getMaxEnergyStored() - stored));
            if (!simulate && received > 0) {
                setEnergyStored(stored + received);
            }

            return received * count;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int count = container.getCount();
            if (count <= 0 || maxExtract <= 0) {
                return 0;
            }

            int stored = getEnergyStored();
            int extracted = Math.max(0, Math.min(maxExtract / count, stored));
            if (!simulate && extracted > 0) {
                setEnergyStored(stored - extracted);
            }

            return extracted * count;
        }

        @Override
        public int getEnergyStored() {
            return Optional.ofNullable(container.getTag())
                .filter(nbt -> nbt.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND))
                .map(nbt -> nbt.getCompound(BLOCK_ENTITY_TAG))
                .filter(nbt -> nbt.contains(CoreNBTKeys.ENERGY, Tag.TAG_COMPOUND))
                .map(nbt -> nbt.getCompound(CoreNBTKeys.ENERGY))
                .filter(nbt -> nbt.contains(CoreNBTKeys.ENERGY_STORED, Tag.TAG_INT))
                .map(nbt -> nbt.getInt(CoreNBTKeys.ENERGY_STORED))
                .orElse(0);
        }

        public void setEnergyStored(int stored) {
            CompoundTag nbt = container.getOrCreateTag();
            CompoundTag blockEntityTag = null;
            if (nbt.contains(BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
                blockEntityTag = nbt.getCompound(BLOCK_ENTITY_TAG);
            } else {
                blockEntityTag = new CompoundTag();
                nbt.put(BLOCK_ENTITY_TAG, blockEntityTag);
            }
            CompoundTag energyTag = null;
            if (blockEntityTag.contains(CoreNBTKeys.ENERGY, Tag.TAG_COMPOUND)) {
                energyTag = blockEntityTag.getCompound(CoreNBTKeys.ENERGY);
            } else {
                energyTag = new CompoundTag();
                blockEntityTag.put(CoreNBTKeys.ENERGY, energyTag);
            }
            energyTag.putInt(CoreNBTKeys.ENERGY_STORED, stored);
        }

        @Override
        public int getMaxEnergyStored() {
            return capacity;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return true;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ENERGY) {
                return LazyOptional.of(() -> this).cast();
            }

            return LazyOptional.empty();
        }
    }
}
