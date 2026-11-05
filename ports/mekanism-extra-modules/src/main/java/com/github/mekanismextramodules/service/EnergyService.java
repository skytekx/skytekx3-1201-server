package com.github.mekanismextramodules.service;

import com.github.mekanismextramodules.registry.ExtraModuleRegistry;
import mekanism.api.energy.IEnergyConversionHelper;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import net.minecraft.core.Holder;
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
        long amountJoules = feToJoules(amountFE);
        ModuleStack moduleStack = findModuleStack(player, moduleFor(context));
        if (moduleStack == null || !moduleStack.module().canUseEnergy(player, moduleStack.stack(), amountJoules)) {
            return 0L;
        }
        if (simulate) {
            return amountFE;
        }
        moduleStack.module().useEnergy(player, moduleStack.stack(), amountJoules);
        return amountFE;
    }

    public static long feToJoules(long amountFE) {
        return amountFE <= 0 ? 0L : IEnergyConversionHelper.INSTANCE.feConversion().convertFrom(amountFE);
    }

    private static ModuleStack findModuleStack(ServerPlayer player, Holder<ModuleData<?>> moduleData) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.HEAD, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
            if (container != null && container.has(moduleData)) {
                IModule<?> module = container.get(moduleData);
                return new ModuleStack(stack, module);
            }
        }
        return null;
    }

    private static Holder<ModuleData<?>> moduleFor(EnergyContext context) {
        return switch (context) {
            case EMERGENCY_REVIVAL -> ExtraModuleRegistry.EMERGENCY_REVIVAL;
            case PHASE_GUARD -> ExtraModuleRegistry.PHASE_GUARD;
        };
    }

    private EnergyService() {
    }

    private record ModuleStack(ItemStack stack, IModule<?> module) {
    }
}
