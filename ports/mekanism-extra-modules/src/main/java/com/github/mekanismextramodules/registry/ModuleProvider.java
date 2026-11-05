package com.github.mekanismextramodules.registry;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

/**
 * SkyTekx port: Mekanism 10.4 takes an {@link IModuleDataProvider} where Mekanism 10.7 takes a
 * {@code Holder<ModuleData<?>>}. This wraps the registry object of a module, so the module item can be
 * created before the module registry is filled. The id comes from the registry object, never from the
 * module data itself.
 */
public final class ModuleProvider<MODULE extends ICustomModule<MODULE>> implements IModuleDataProvider<MODULE> {
    private final RegistryObject<? extends ModuleData<?>> data;

    private ModuleProvider(RegistryObject<? extends ModuleData<?>> data) {
        this.data = data;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static ModuleProvider<?> of(RegistryObject<? extends ModuleData<?>> data) {
        return new ModuleProvider(data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModuleData<MODULE> getModuleData() {
        return (ModuleData<MODULE>) data.get();
    }

    @Override
    public ResourceLocation getRegistryName() {
        return data.getId();
    }
}
