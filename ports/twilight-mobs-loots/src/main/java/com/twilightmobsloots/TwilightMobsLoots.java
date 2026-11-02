package com.twilightmobsloots;

import com.mojang.logging.LogUtils;
import com.twilightmobsloots.compat.CuriosCompat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

/**
 * Main class. The {@code @Mod} value must match the modId in META-INF/mods.toml.
 *
 * <p>Minecraft 1.20.1 / Forge backport of Twilight Mobs Loots 1.1.1 (1.21.1 NeoForge) by wdd1118 (MIT).
 */
@Mod(TwilightMobsLoots.MOD_ID)
public class TwilightMobsLoots {

    public static final String MOD_ID = "twilight_mobs_loots";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TwilightMobsLoots() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Blocks (48 floor + 48 wall), the shared block entity type, items and the creative tab.
        // Order matters: the block entity type supplier reads the registered blocks.
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModItems.CREATIVE_MODE_TABS.register(modEventBus);

        // Trophy drops for the Twilight Forest entity loot tables (game event bus).
        TFLootInjection.init();

        // Curios is optional: trophies only become curios when it is installed.
        CuriosCompat.init(modEventBus);

        // Client only: block entity renderer, item renderer (BEWLR), Curios renderers.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.twilightmobsloots.client.ClientSetup.init(modEventBus);
        }

        LOGGER.info("[{}] Twilight Mobs Loots loaded", MOD_ID);
    }
}
