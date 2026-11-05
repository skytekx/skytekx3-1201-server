package com.github.mekanismextramodules.module;

import com.github.mekanismextramodules.MekanismExtraModules;
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

public final class PhaseGuardModule implements ICustomModule<PhaseGuardModule> {
    private static final ResourceLocation ICON = ResourceLocation.fromNamespaceAndPath(MekanismExtraModules.MODID, "gui/hud/phase_guard_unit.png");

    @Override
    public void addHUDElements(IModule<PhaseGuardModule> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (!module.isEnabled()) {
            return;
        }
        long requiredEnergy = EnergyService.feToJoules(ProtectionService.currentPhaseGuardCost());
        boolean active = ProtectionService.canUsePhaseGuard(player) && module.canUseEnergy(player, stack, requiredEnergy);
        hudElementAdder.accept(IModuleHelper.INSTANCE.hudElement(
                ICON,
                Component.translatable(active
                        ? "hud.mekanism_extra_modules.phase_guard.active"
                        : "hud.mekanism_extra_modules.phase_guard.inactive"),
                active ? IHUDElement.HUDColor.REGULAR : IHUDElement.HUDColor.FADED
        ));
    }
}
