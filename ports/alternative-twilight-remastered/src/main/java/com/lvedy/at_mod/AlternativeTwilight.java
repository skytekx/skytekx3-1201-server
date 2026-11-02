package com.lvedy.at_mod;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModEffects;
import com.lvedy.at_mod.register.ModEntityTypes;
import com.lvedy.at_mod.register.ModItem;
import com.lvedy.at_mod.register.ModSounds;
import com.lvedy.at_mod.register.ModTabs;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.Locale;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AlternativeTwilight.MODID)
public class AlternativeTwilight {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "at_mod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation prefix(String name) {
        return new ResourceLocation(MODID, name.toLowerCase(Locale.ROOT));
    }

    // 1.20.1 backport: Forge 47 hands the mod bus out through FMLJavaModLoadingContext instead of constructor injection.
    // The upstream MDK leftovers (example_block, its item and the "HELLO" log listeners) are not ported.
    public AlternativeTwilight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the Deferred Registers to the mod event bus
        ModItem.register(modEventBus);
        ModTabs.register(modEventBus);
        ModEntityTypes.register(modEventBus);
        ModSounds.register(modEventBus);
        ModEffects.register(modEventBus);

        // Register our mod's ForgeConfigSpec so that FML can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "at_mod_config.toml");
    }
}
