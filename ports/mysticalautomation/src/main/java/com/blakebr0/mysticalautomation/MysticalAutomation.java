package com.blakebr0.mysticalautomation;

import com.blakebr0.mysticalautomation.client.ModMenuScreens;
import com.blakebr0.mysticalautomation.config.ModConfigs;
import com.blakebr0.mysticalautomation.crafting.DynamicRecipeManager;
import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.init.ModCreativeModeTabs;
import com.blakebr0.mysticalautomation.init.ModItems;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import com.blakebr0.mysticalautomation.init.ModRecipeSerializers;
import com.blakebr0.mysticalautomation.init.ModRecipeTypes;
import com.blakebr0.mysticalautomation.init.ModTileEntities;
import com.blakebr0.mysticalautomation.network.NetworkHandler;
import com.blakebr0.mysticalautomation.util.RecipeIngredientCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MysticalAutomation.MOD_ID)
public final class MysticalAutomation {
    public static final String MOD_ID = "mysticalautomation";
    public static final String NAME = "Mystical Automation";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public MysticalAutomation() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.REGISTRY.register(bus);
        ModItems.REGISTRY.register(bus);
        ModCreativeModeTabs.REGISTRY.register(bus);
        ModMenuTypes.REGISTRY.register(bus);
        ModTileEntities.REGISTRY.register(bus);
        ModRecipeTypes.REGISTRY.register(bus);
        ModRecipeSerializers.REGISTRY.register(bus);

        bus.register(this);

        // SkyTekx port: Forge has no block capability event, the machines hand out their capabilities themselves
        // (BaseMachineTileEntity), and the network channel registers in common setup

        if (FMLEnvironment.dist == Dist.CLIENT) {
            bus.register(new ModMenuScreens());
        }

        // SkyTekx port: Forge 47 has no STARTUP config type. A COMMON config loads at mod construction too, before
        // recipes, tags and creative tabs read the feature flags, and keeps the file name
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModConfigs.COMMON, "mysticalautomation-common.toml");
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);

        MinecraftForge.EVENT_BUS.register(DynamicRecipeManager.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RecipeIngredientCache.INSTANCE);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
