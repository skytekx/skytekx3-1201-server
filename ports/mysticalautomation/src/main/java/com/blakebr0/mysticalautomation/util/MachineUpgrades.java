package com.blakebr0.mysticalautomation.util;

import com.blakebr0.mysticalagriculture.item.MachineUpgradeItem;
import com.blakebr0.mysticalagriculture.util.MachineUpgradeTier;
import net.minecraft.world.item.ItemStack;

/**
 * SkyTekx port: Mystical Agriculture 8 gives {@code MachineUpgradeTier} the methods getOperationTime, getFuelUsage
 * and getFuelCapacity and moves the upgrade classes into its API. Mystical Agriculture 7 keeps them in
 * {@code util}, {@code item} and {@code container.inventory} and scales the base values in each machine, as its
 * Reprocessor does. These helpers do the same for the machines of this mod, so they accept the Machine Upgrades of
 * Mystical Agriculture 7.
 */
public final class MachineUpgrades {
    private MachineUpgrades() { }

    public static int getOperationTime(MachineUpgradeTier tier, int base) {
        return (int) (base * tier.getOperationTimeMultiplier());
    }

    public static int getFuelUsage(MachineUpgradeTier tier, int base) {
        return (int) (base * tier.getFuelUsageMultiplier());
    }

    public static int getFuelCapacity(MachineUpgradeTier tier, int base) {
        return (int) (base * tier.getFuelCapacityMultiplier());
    }

    public static boolean isUpgrade(ItemStack stack) {
        return stack.getItem() instanceof MachineUpgradeItem;
    }
}
