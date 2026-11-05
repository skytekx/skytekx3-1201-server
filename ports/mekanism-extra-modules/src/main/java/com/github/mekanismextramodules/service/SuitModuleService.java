package com.github.mekanismextramodules.service;

import com.github.mekanismextramodules.registry.ExtraModuleRegistry;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SuitModuleService {
    private static final ResourceLocation HELMET = ResourceLocation.fromNamespaceAndPath("mekanism", "mekasuit_helmet");
    private static final ResourceLocation BODYARMOR = ResourceLocation.fromNamespaceAndPath("mekanism", "mekasuit_bodyarmor");
    private static final ResourceLocation PANTS = ResourceLocation.fromNamespaceAndPath("mekanism", "mekasuit_pants");
    private static final ResourceLocation BOOTS = ResourceLocation.fromNamespaceAndPath("mekanism", "mekasuit_boots");

    public static boolean hasFullMekaSuit(Player player) {
        return isItem(player.getItemBySlot(EquipmentSlot.HEAD), HELMET)
                && isItem(player.getItemBySlot(EquipmentSlot.CHEST), BODYARMOR)
                && isItem(player.getItemBySlot(EquipmentSlot.LEGS), PANTS)
                && isItem(player.getItemBySlot(EquipmentSlot.FEET), BOOTS);
    }

    public static boolean hasPhaseGuard(Player player) {
        return hasModule(player, ExtraModuleRegistry.PHASE_GUARD);
    }

    public static boolean hasPhaseGuardEnabled(Player player) {
        return hasEnabledModule(player, ExtraModuleRegistry.PHASE_GUARD);
    }

    public static boolean hasChaosAnchor(Player player) {
        return hasModule(player, ExtraModuleRegistry.CHAOS_ANCHOR);
    }

    public static boolean hasChaosAnchorEnabled(Player player) {
        return hasEnabledModule(player, ExtraModuleRegistry.CHAOS_ANCHOR);
    }

    public static boolean hasEmergencyRevivalEnabled(Player player) {
        return hasEnabledModule(player, ExtraModuleRegistry.EMERGENCY_REVIVAL);
    }

    public static boolean hasModule(Player player, Holder<ModuleData<?>> module) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
            if (container != null && container.has(module)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEnabledModule(Player player, Holder<ModuleData<?>> module) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            IModuleContainer container = IModuleHelper.INSTANCE.getModuleContainer(stack);
            if (container != null && container.hasEnabled(module)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isItem(ItemStack stack, ResourceLocation id) {
        return !stack.isEmpty() && id.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private SuitModuleService() {
    }
}
