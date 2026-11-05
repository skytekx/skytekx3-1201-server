package com.github.mekanismextramodules.registry;

import com.github.mekanismextramodules.MekanismExtraModules;
import com.github.mekanismextramodules.module.EmergencyRevivalModule;
import com.github.mekanismextramodules.module.PhaseGuardModule;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ExtraModuleRegistry {
    private static final ResourceKey<CreativeModeTab> MEKANISM_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MekanismAPI.MEKANISM_MODID, MekanismAPI.MEKANISM_MODID));

    public static final DeferredRegister<ModuleData<?>> MODULES = DeferredRegister.create(MekanismAPI.MODULE_REGISTRY_NAME, MekanismExtraModules.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MekanismExtraModules.MODID);

    public static final DeferredItem<Item> PHASE_GUARD_ITEM = ITEMS.register("phase_guard_unit",
            () -> IModuleHelper.INSTANCE.createModuleItem(ExtraModuleRegistry::phaseGuardHolder, new Item.Properties()));
    public static final DeferredItem<Item> CHAOS_ANCHOR_ITEM = ITEMS.register("chaos_anchor_unit",
            () -> IModuleHelper.INSTANCE.createModuleItem(ExtraModuleRegistry::chaosAnchorHolder, new Item.Properties()));
    public static final DeferredItem<Item> EMERGENCY_REVIVAL_ITEM = ITEMS.register("emergency_revival_unit",
            () -> IModuleHelper.INSTANCE.createModuleItem(ExtraModuleRegistry::emergencyRevivalHolder, new Item.Properties()));

    public static final DeferredHolder<ModuleData<?>, ModuleData<PhaseGuardModule>> PHASE_GUARD = MODULES.register("phase_guard_unit",
            () -> new ModuleData<>(ModuleData.ModuleDataBuilder.customInstanced(PhaseGuardModule::new, PHASE_GUARD_ITEM).disabledByDefault().rendersHUD()));
    public static final DeferredHolder<ModuleData<?>, ModuleData<?>> CHAOS_ANCHOR = MODULES.register("chaos_anchor_unit",
            () -> new ModuleData<>(ModuleData.ModuleDataBuilder.marker(CHAOS_ANCHOR_ITEM).disabledByDefault()));
    public static final DeferredHolder<ModuleData<?>, ModuleData<EmergencyRevivalModule>> EMERGENCY_REVIVAL = MODULES.register("emergency_revival_unit",
            () -> new ModuleData<>(ModuleData.ModuleDataBuilder.customInstanced(EmergencyRevivalModule::new, EMERGENCY_REVIVAL_ITEM).disabledByDefault().rendersHUD()));

    public static void register(IEventBus bus) {
        MODULES.register(bus);
        ITEMS.register(bus);
        bus.addListener(ExtraModuleRegistry::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!MEKANISM_TAB.equals(event.getTabKey())) {
            return;
        }
        event.accept(PHASE_GUARD_ITEM.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        event.accept(CHAOS_ANCHOR_ITEM.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        event.accept(EMERGENCY_REVIVAL_ITEM.get().getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static void sendMekanismIMC() {
        MekanismIMC.addMekaSuitBodyarmorModules(PHASE_GUARD, CHAOS_ANCHOR, EMERGENCY_REVIVAL);
    }

    private static Holder<ModuleData<?>> phaseGuardHolder() {
        return PHASE_GUARD;
    }

    private static Holder<ModuleData<?>> chaosAnchorHolder() {
        return CHAOS_ANCHOR;
    }

    private static Holder<ModuleData<?>> emergencyRevivalHolder() {
        return EMERGENCY_REVIVAL;
    }

    private ExtraModuleRegistry() {
    }
}
