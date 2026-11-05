package com.github.mekanismextramodules.registry;

import com.github.mekanismextramodules.MekanismExtraModules;
import com.github.mekanismextramodules.module.EmergencyRevivalModule;
import com.github.mekanismextramodules.module.PhaseGuardModule;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IItemProvider;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ExtraModuleRegistry {
    private static final ResourceKey<CreativeModeTab> MEKANISM_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB,
            new ResourceLocation(MekanismAPI.MEKANISM_MODID, MekanismAPI.MEKANISM_MODID));

    public static final DeferredRegister<ModuleData<?>> MODULES = DeferredRegister.create(MekanismAPI.MODULE_REGISTRY_NAME, MekanismExtraModules.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MekanismExtraModules.MODID);

    private static final RegistryObject<ModuleData<PhaseGuardModule>> PHASE_GUARD_DATA = MODULES.register("phase_guard_unit",
            () -> new ModuleData<>(ModuleData.ModuleDataBuilder.custom(PhaseGuardModule::new, item(ExtraModuleRegistry.PHASE_GUARD_ITEM)).disabledByDefault().rendersHUD()));
    private static final RegistryObject<ModuleData<?>> CHAOS_ANCHOR_DATA = MODULES.register("chaos_anchor_unit",
            () -> new ModuleData<>(ModuleData.ModuleDataBuilder.marker(item(ExtraModuleRegistry.CHAOS_ANCHOR_ITEM)).disabledByDefault()));
    private static final RegistryObject<ModuleData<EmergencyRevivalModule>> EMERGENCY_REVIVAL_DATA = MODULES.register("emergency_revival_unit",
            () -> new ModuleData<>(ModuleData.ModuleDataBuilder.custom(EmergencyRevivalModule::new, item(ExtraModuleRegistry.EMERGENCY_REVIVAL_ITEM)).disabledByDefault().rendersHUD()));

    public static final IModuleDataProvider<?> PHASE_GUARD = ModuleProvider.of(PHASE_GUARD_DATA);
    public static final IModuleDataProvider<?> CHAOS_ANCHOR = ModuleProvider.of(CHAOS_ANCHOR_DATA);
    public static final IModuleDataProvider<?> EMERGENCY_REVIVAL = ModuleProvider.of(EMERGENCY_REVIVAL_DATA);

    public static final RegistryObject<Item> PHASE_GUARD_ITEM = ITEMS.register("phase_guard_unit",
            () -> IModuleHelper.INSTANCE.createModuleItem(PHASE_GUARD, new Item.Properties()));
    public static final RegistryObject<Item> CHAOS_ANCHOR_ITEM = ITEMS.register("chaos_anchor_unit",
            () -> IModuleHelper.INSTANCE.createModuleItem(CHAOS_ANCHOR, new Item.Properties()));
    public static final RegistryObject<Item> EMERGENCY_REVIVAL_ITEM = ITEMS.register("emergency_revival_unit",
            () -> IModuleHelper.INSTANCE.createModuleItem(EMERGENCY_REVIVAL, new Item.Properties()));

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

    /** Mekanism 10.4 names the module item through an {@link IItemProvider}, resolved after registration. */
    private static IItemProvider item(RegistryObject<Item> item) {
        return item::get;
    }

    private ExtraModuleRegistry() {
    }
}
