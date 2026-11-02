package baguchi.twilight_crossbow;

import baguchi.twilight_crossbow.init.ModEntities;
import baguchi.twilight_crossbow.init.ModItems;
import baguchi.twilight_crossbow.init.ModLootModifiers;
import baguchi.twilight_crossbow.init.ModRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import twilightforest.init.TFCreativeTabs;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TwilightCrossBow.MODID)
public class TwilightCrossBow {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "twilight_crossbow";

    public TwilightCrossBow() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEM_REGISTRY.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
        // Register the items to the Twilight Forest creative tabs
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == TFCreativeTabs.EQUIPMENT.getKey()) {
            event.accept(ModItems.TRIPLE_CROSSBOW);
            event.accept(ModItems.ICE_CROSSBOW);
            event.accept(ModItems.SEEKER_CROSSBOW);
            event.accept(ModItems.ENDER_CROSSBOW);
        }

        if (event.getTabKey() == TFCreativeTabs.ITEMS.getKey()) {
            event.accept(ModItems.CROSSBOW_UPGRADE_SMITHING_TEMPLATE);
        }
    }

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(MODID, path);
    }
}
