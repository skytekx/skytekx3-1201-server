package com.github.mekanismextramodules.module;

import com.github.mekanismextramodules.MekanismExtraModules;
import com.github.mekanismextramodules.config.ExtraModulesConfig;
import com.github.mekanismextramodules.service.EnergyService;
import com.github.mekanismextramodules.service.ProtectionService;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleContainer;
import mekanism.api.gear.IModuleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class EmergencyRevivalModule implements ICustomModule<EmergencyRevivalModule> {
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(MekanismExtraModules.MODID, "gui/hud/emergency_revival_unit.png");

    @Override
    public void addHUDElements(IModule<EmergencyRevivalModule> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (!module.isEnabled()) {
            return;
        }
        if (!ProtectionService.canUseEmergencyRevival(player)) {
            return;
        }
        long requiredEnergy = EnergyService.feToJoules(ExtraModulesConfig.REVIVAL_ENERGY_COST.get());
        boolean charged = module.canUseEnergy(player, stack, requiredEnergy);
        hudElementAdder.accept(IModuleHelper.INSTANCE.hudElement(
                ICON,
                Component.translatable(charged
                        ? "hud.mekanism_extra_modules.emergency_revival.ready"
                        : "hud.mekanism_extra_modules.emergency_revival.no_energy"),
                charged ? IHUDElement.HUDColor.REGULAR : IHUDElement.HUDColor.FADED
        ));
    }
}
