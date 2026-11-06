package de.ellpeck.actuallyadditions.mod.util;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * SkyTekx: the loot table helper of upstream 02ca59fe. The 1.21 reloadable registries are the loot
 * data manager on 1.20.1. The ids of the tables that the code rolls live here, so that runtime code
 * never reaches into the data generators
 */
public final class LootTableUtil {

    /**
     * What a worm hoed out of a block of the worm_drop block tag drops (upstream 159fd67c)
     */
    public static final ResourceLocation WORM_DROP = new ResourceLocation(ActuallyAdditions.MODID, "worm_drop");

    private LootTableUtil() {
    }

    /**
     * The loot table of that id, or LootTable.EMPTY on a client level or for an unknown id
     */
    public static LootTable getLootFromTable(Level level, ResourceLocation resourceLocation) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return LootTable.EMPTY;
        }
        return server.getLootData().getLootTable(resourceLocation);
    }
}
