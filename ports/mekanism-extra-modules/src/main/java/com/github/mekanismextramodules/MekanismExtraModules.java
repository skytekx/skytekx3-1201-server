package com.github.mekanismextramodules;

import com.github.mekanismextramodules.config.ExtraModulesConfig;
import com.github.mekanismextramodules.registry.ExtraModuleRegistry;
import com.github.mekanismextramodules.service.ProtectionService;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MekanismExtraModules.MODID)
public final class MekanismExtraModules {
    public static final String MODID = "mekanism_extra_modules";
    public static final String MOD_NAME = "Mekanism: Extra Modules";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public MekanismExtraModules(IEventBus modBus, net.neoforged.fml.ModContainer modContainer) {
        ExtraModuleRegistry.register(modBus);
        modBus.addListener(this::enqueueIMC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ExtraModulesConfig.SERVER_SPEC);

        NeoForge.EVENT_BUS.register(ProtectionService.class);
        NeoForge.EVENT_BUS.addListener(this::logCompatSummary);
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        event.enqueueWork(ExtraModuleRegistry::sendMekanismIMC);
    }

    private void logCompatSummary(ServerStartedEvent event) {
        LOGGER.info("Compat loaded: mekanism={}, mekanismtools={}, draconicevolution={}",
                ModList.get().isLoaded("mekanism"),
                ModList.get().isLoaded("mekanismtools"),
                ModList.get().isLoaded("draconicevolution"));
    }
}
