package com.enderio.machines.common.datamap;

import com.enderio.EnderIO;
import com.enderio.core.common.data.JsonDataMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.Map;

/**
 * The range bonus that a block gives the machine below it: for a block, the extra range per range extender tag it is
 * in. The Wireless Charger reads the value for {@code #enderio:range_extender} from the block above it. Read from
 * {@code data/<namespace>/data_maps/block/range_extender.json}, the 8.x NeoForge data map format, through the SkyTekx
 * {@link JsonDataMap} shim (upstream 4ad73fff0).
 */
public class RangeExtender {
    public static final JsonDataMap<Block, Map<TagKey<Block>, Integer>> DATA_MAP = JsonDataMap.create(EnderIO.loc("range_extender"), Registries.BLOCK,
        JsonDataMap.tagToInteger(Registries.BLOCK));

    /**
     * Loads the class, so the data map exists before the first datapack load.
     */
    public static void register() {}
}
