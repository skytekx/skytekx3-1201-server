package com.github.mekanismextramodules.service;

import com.github.mekanismextramodules.registry.ExtraModuleRegistry;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class SuitModuleService {
    private static final ResourceLocation HELMET = new ResourceLocation("mekanism", "mekasuit_helmet");
    private static final ResourceLocation BODYARMOR = new ResourceLocation("mekanism", "mekasuit_bodyarmor");
    private static final ResourceLocation PANTS = new ResourceLocation("mekanism", "mekasuit_pants");
    private static final ResourceLocation BOOTS = new ResourceLocation("mekanism", "mekasuit_boots");

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

    // SkyTekx port: Mekanism 10.4 has no IModuleContainer, its IModuleHelper reads the modules of a stack directly.
    public static boolean hasModule(Player player, IModuleDataProvider<?> module) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (IModuleHelper.INSTANCE.load(stack, module) != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasEnabledModule(Player player, IModuleDataProvider<?> module) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (IModuleHelper.INSTANCE.isEnabled(stack, module)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isItem(ItemStack stack, ResourceLocation id) {
        return !stack.isEmpty() && id.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    private SuitModuleService() {
    }
}
