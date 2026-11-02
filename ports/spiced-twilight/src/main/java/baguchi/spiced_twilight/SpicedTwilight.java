package baguchi.spiced_twilight;

import baguchi.spiced_twilight.entity.ModEntities;
import baguchi.spiced_twilight.item.ModCreativeTabs;
import baguchi.spiced_twilight.item.ModItems;
import baguchi.spiced_twilight.loot.ModLootModifiers;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SpicedTwilight.MODID)
public class SpicedTwilight {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "spiced_twilight";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public SpicedTwilight() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModLootModifiers.LOOT_MODIFIERS.register(modEventBus);
    }

    public static ResourceLocation prefix(String path) {
        return new ResourceLocation(MODID, path);
    }
}
