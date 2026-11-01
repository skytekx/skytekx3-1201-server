package twilightforest.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TwilightForestMod;
import twilightforest.util.datamaps.DataMapLoader;
import twilightforest.util.datamaps.DataMapType;
import twilightforest.util.datamaps.MagicMapBiomeColor;
import twilightforest.util.datamaps.OreMapOreColor;

import java.util.List;

/**
 * TF 4.8's NeoForge data maps, loaded from datapacks by {@link DataMapLoader}. See {@link DataMapType} for the file format.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class TFDataMaps {

	public static final DataMapType<Biome, MagicMapBiomeColor> MAGIC_MAP_BIOME_COLOR = new DataMapType<>(
			TwilightForestMod.prefix("magic_map_color"), Registries.BIOME, MagicMapBiomeColor.CODEC);

	public static final DataMapType<Block, OreMapOreColor> ORE_MAP_ORE_COLOR = new DataMapType<>(
			TwilightForestMod.prefix("ore_map_color"), Registries.BLOCK, OreMapOreColor.CODEC);

	private static final List<DataMapType<?, ?>> DATA_MAPS = List.of(MAGIC_MAP_BIOME_COLOR, ORE_MAP_ORE_COLOR);

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener(new DataMapLoader(DATA_MAPS));
	}

	// Tag entries are resolved lazily, so lookups made with the old tags have to be redone
	@SubscribeEvent
	public static void tagsUpdated(TagsUpdatedEvent event) {
		DATA_MAPS.forEach(DataMapType::invalidate);
	}
}
