package com.github.mekanismextramodules;

import com.github.mekanismextramodules.config.ExtraModulesConfig;
import com.github.mekanismextramodules.registry.ExtraModuleRegistry;
import com.github.mekanismextramodules.service.ProtectionService;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MekanismExtraModules.MODID)
public final class MekanismExtraModules {
    public static final String MODID = "mekanism_extra_modules";
    public static final String MOD_NAME = "Mekanism: Extra Modules";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public MekanismExtraModules() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ExtraModuleRegistry.register(modBus);
        modBus.addListener(this::enqueueIMC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ExtraModulesConfig.SERVER_SPEC);

        MinecraftForge.EVENT_BUS.register(ProtectionService.class);
        MinecraftForge.EVENT_BUS.addListener(this::logCompatSummary);
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
