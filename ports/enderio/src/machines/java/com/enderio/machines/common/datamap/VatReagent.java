package com.enderio.machines.common.datamap;

import com.enderio.EnderIO;
import com.enderio.core.common.data.JsonDataMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Map;

/**
 * The reagent multipliers of the Vat: for an item, the factor that each reagent tag it fills applies to the output of a
 * fermenting recipe. Read from {@code data/<namespace>/data_maps/item/vat_reagent.json}, the 8.x NeoForge data map
 * format, through the SkyTekx {@link JsonDataMap} shim (upstream 1cb2e1420 and 6bc3ec343).
 */
public class VatReagent {
    public static final JsonDataMap<Item, Map<TagKey<Item>, Double>> DATA_MAP = JsonDataMap.create(EnderIO.loc("vat_reagent"), Registries.ITEM,
        JsonDataMap.tagToDouble(Registries.ITEM));

    /**
     * Loads the class, so the data map exists before the first datapack load.
     */
    public static void register() {}
}
