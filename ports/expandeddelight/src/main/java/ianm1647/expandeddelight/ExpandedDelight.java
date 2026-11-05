package ianm1647.expandeddelight;

import ianm1647.expandeddelight.common.EDCompostables;
import ianm1647.expandeddelight.common.registry.*;
import ianm1647.expandeddelight.common.world.tree.EDTreePlacers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ExpandedDelight.MODID)
public class ExpandedDelight
{
    public static final String MODID = "expandeddelight";
    public static final Logger LOGGER = LogManager.getLogger();

    // The juicer's recipe book. NeoForge 1.21.1 adds the enum value through META-INF/enumextensions.json,
    // Forge 1.20.1 through IExtensibleEnum, early enough for RecipeBookSettings to see it.
    public static final RecipeBookType RECIPE_TYPE_JUICING = RecipeBookType.create("EXPANDEDDELIGHT_JUICING");

    public ExpandedDelight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, EDConfig.COMMON_CONFIG);
        EDBlocks.BLOCKS.register(modEventBus);
        EDItems.ITEMS.register(modEventBus);
        EDBlockEntityTypes.TILES.register(modEventBus);
        EDMenuTypes.MENU_TYPES.register(modEventBus);
        EDRecipeTypes.RECIPE_TYPES.register(modEventBus);
        EDRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        EDTabs.CREATIVE_TABS.register(modEventBus);
        EDEntityTypes.ENTITIES.register(modEventBus);
        EDLootFunctions.LOOT_FUNCTIONS.register(modEventBus);

        EDTreePlacers.FOLIAGE_PLACERS.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        // NeoForge 1.21.1 reads data/neoforge/data_maps/item/compostables.json, Forge 1.20.1 has no data maps
        event.enqueueWork(EDCompostables::register);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("Expanded Delight is starting...");
    }
}
