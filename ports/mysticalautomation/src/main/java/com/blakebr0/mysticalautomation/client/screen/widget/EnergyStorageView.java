package com.blakebr0.mysticalautomation.client.screen.widget;

import net.minecraftforge.energy.IEnergyStorage;

import java.util.function.IntSupplier;

/**
 * SkyTekx port: Cucumber 8's EnergyBarWidget reads two IntSuppliers, Cucumber 7's reads an IEnergyStorage. This read
 * only view hands the synced menu values to the Cucumber 7 widget.
 */
public record EnergyStorageView(IntSupplier energy, IntSupplier capacity) implements IEnergyStorage {
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return this.energy.getAsInt();
    }

    @Override
    public int getMaxEnergyStored() {
        return this.capacity.getAsInt();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
