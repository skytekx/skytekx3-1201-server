package com.github.mekanismextramodules.service;

import com.github.mekanismextramodules.registry.ExtraModuleRegistry;
import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class EnergyService {
    public static boolean tryConsumeEnergy(ServerPlayer player, long amountFE, EnergyContext context) {
        if (amountFE <= 0) {
            return true;
        }
        return consumeFromMekaSuit(player, amountFE, context, false) >= amountFE;
    }

    public static long consumeFromMekaSuit(ServerPlayer player, long amountFE, EnergyContext context, boolean simulate) {
        if (amountFE <= 0) {
            return 0L;
        }
        FloatingLong amountJoules = feToJoules(amountFE);
        IModule<?> module = findModule(player, moduleFor(context));
        // SkyTekx port: a Mekanism 10.4 module knows the suit piece it was loaded from, so it draws from that stack.
        if (module == null || !module.canUseEnergy(player, amountJoules)) {
            return 0L;
        }
        if (simulate) {
            return amountFE;
        }
        module.useEnergy(player, amountJoules);
        return amountFE;
    }

    /** SkyTekx port: Mekanism 10.4 counts Joules as {@link FloatingLong}, Mekanism 10.7 as long. */
    public static FloatingLong feToJoules(long amountFE) {
        return amountFE <= 0 ? FloatingLong.ZERO : IEnergyConversionHelper.INSTANCE.feConversion().convertFrom(amountFE);
    }

    private static IModule<?> findModule(ServerPlayer player, IModuleDataProvider<?> moduleData) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            IModule<?> module = IModuleHelper.INSTANCE.load(stack, moduleData);
            if (module != null) {
                return module;
            }
        }
        return null;
    }

    private static IModuleDataProvider<?> moduleFor(EnergyContext context) {
        return switch (context) {
            case EMERGENCY_REVIVAL -> ExtraModuleRegistry.EMERGENCY_REVIVAL;
            case PHASE_GUARD -> ExtraModuleRegistry.PHASE_GUARD;
        };
    }

    private EnergyService() {
    }
}
