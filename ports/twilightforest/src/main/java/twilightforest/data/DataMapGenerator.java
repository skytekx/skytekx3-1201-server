package twilightforest.data;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFDataMaps;
import twilightforest.util.datamaps.DataMapType;
import twilightforest.util.datamaps.EntityTransformation;
import twilightforest.util.datamaps.MagicMapBiomeColor;
import twilightforest.util.datamaps.OreMapOreColor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes TF's {@link DataMapType} files (TF 4.8's DataMapGenerator, for the data maps the 1.20.1 port has).
 */
public class DataMapGenerator implements DataProvider {
	private final PackOutput output;
	private final List<Builder<?, ?>> builders = new ArrayList<>();

	public DataMapGenerator(PackOutput output) {
		this.output = output;
	}

	protected void gather() {
		var magicMap = this.builder(TFDataMaps.MAGIC_MAP_BIOME_COLOR);
		magicMap.add(TFBiomes.FOREST, new MagicMapBiomeColor(MapColor.PLANT, 1));
		magicMap.add(TFBiomes.DENSE_FOREST, new MagicMapBiomeColor(MapColor.PLANT, 0));
		magicMap.add(TFBiomes.LAKE, new MagicMapBiomeColor(MapColor.WATER, 3));
		magicMap.add(TFBiomes.STREAM, new MagicMapBiomeColor(MapColor.WATER, 1));
		magicMap.add(TFBiomes.SWAMP, new MagicMapBiomeColor(MapColor.DIAMOND, 3));
		magicMap.add(TFBiomes.FIRE_SWAMP, new MagicMapBiomeColor(MapColor.NETHER, 1));
		magicMap.add(TFBiomes.CLEARING, new MagicMapBiomeColor(MapColor.GRASS, 2));
		magicMap.add(TFBiomes.OAK_SAVANNAH, new MagicMapBiomeColor(MapColor.GRASS, 0));
		magicMap.add(TFBiomes.HIGHLANDS, new MagicMapBiomeColor(MapColor.DIRT, 0));
		magicMap.add(TFBiomes.THORNLANDS, new MagicMapBiomeColor(MapColor.WOOD, 3));
		magicMap.add(TFBiomes.FINAL_PLATEAU, new MagicMapBiomeColor(MapColor.COLOR_LIGHT_GRAY, 2));
		magicMap.add(TFBiomes.FIREFLY_FOREST, new MagicMapBiomeColor(MapColor.EMERALD, 1));
		magicMap.add(TFBiomes.DARK_FOREST, new MagicMapBiomeColor(MapColor.COLOR_GREEN, 3));
		magicMap.add(TFBiomes.DARK_FOREST_CENTER, new MagicMapBiomeColor(MapColor.COLOR_ORANGE, 3));
		magicMap.add(TFBiomes.SNOWY_FOREST, new MagicMapBiomeColor(MapColor.SNOW, 1));
		magicMap.add(TFBiomes.GLACIER, new MagicMapBiomeColor(MapColor.ICE, 1));
		magicMap.add(TFBiomes.MUSHROOM_FOREST, new MagicMapBiomeColor(MapColor.COLOR_ORANGE, 0));
		magicMap.add(TFBiomes.DENSE_MUSHROOM_FOREST, new MagicMapBiomeColor(MapColor.COLOR_PINK, 0));
		magicMap.add(TFBiomes.ENCHANTED_FOREST, new MagicMapBiomeColor(MapColor.COLOR_CYAN, 2));
		magicMap.add(TFBiomes.SPOOKY_FOREST, new MagicMapBiomeColor(MapColor.COLOR_PURPLE, 0));

		var oreMap = this.builder(TFDataMaps.ORE_MAP_ORE_COLOR);
		oreMap.add(BlockTags.COPPER_ORES, new OreMapOreColor(MapColor.COLOR_ORANGE));
		oreMap.add(BlockTags.COAL_ORES, new OreMapOreColor(MapColor.COLOR_BLACK));
		oreMap.add(BlockTags.IRON_ORES, new OreMapOreColor(MapColor.RAW_IRON));
		oreMap.add(BlockTags.LAPIS_ORES, new OreMapOreColor(MapColor.LAPIS));
		oreMap.add(BlockTags.GOLD_ORES, new OreMapOreColor(MapColor.GOLD));
		oreMap.add(BlockTags.REDSTONE_ORES, new OreMapOreColor(MapColor.COLOR_RED));
		oreMap.add(BlockTags.DIAMOND_ORES, new OreMapOreColor(MapColor.DIAMOND));
		oreMap.add(BlockTags.EMERALD_ORES, new OreMapOreColor(MapColor.EMERALD));
		oreMap.add(BuiltInRegistries.BLOCK.getKey(Blocks.ANCIENT_DEBRIS), new OreMapOreColor(MapColor.TERRACOTTA_BROWN));

		var ominousFire = this.builder(TFDataMaps.OMINOUS_FIRE);
		ominousFire.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.VILLAGER), new EntityTransformation(EntityType.ZOMBIE_VILLAGER));
		ominousFire.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PIGLIN), new EntityTransformation(EntityType.ZOMBIFIED_PIGLIN));
		ominousFire.add(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.HORSE), new EntityTransformation(EntityType.ZOMBIE_HORSE));
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		this.builders.clear();
		this.gather();

		return CompletableFuture.allOf(this.builders.stream().map(builder -> {
			ResourceLocation file = builder.type.filePath();
			return DataProvider.saveStable(cache, builder.toJson(), this.output.getOutputFolder(PackOutput.Target.DATA_PACK).resolve(file.getNamespace()).resolve(file.getPath()));
		}).toArray(CompletableFuture[]::new));
	}

	protected <R, T> Builder<R, T> builder(DataMapType<R, T> type) {
		Builder<R, T> builder = new Builder<>(type);
		this.builders.add(builder);
		return builder;
	}

	@Override
	public String getName() {
		return "Twilight Forest Data Maps";
	}

	protected static class Builder<R, T> {
		private final DataMapType<R, T> type;
		private final Map<String, T> values = new LinkedHashMap<>();

		private Builder(DataMapType<R, T> type) {
			this.type = type;
		}

		public Builder<R, T> add(ResourceKey<R> key, T value) {
			return this.add(key.location(), value);
		}

		public Builder<R, T> add(ResourceLocation id, T value) {
			this.values.put(id.toString(), value);
			return this;
		}

		public Builder<R, T> add(TagKey<R> tag, T value) {
			this.values.put("#" + tag.location(), value);
			return this;
		}

		private JsonObject toJson() {
			JsonObject values = new JsonObject();
			this.values.forEach((key, value) -> values.add(key, this.type.codec().encodeStart(JsonOps.INSTANCE, value)
					.getOrThrow(false, error -> TwilightForestMod.LOGGER.error("Couldn't encode {} of data map {}: {}", key, this.type.id(), error))));

			JsonObject json = new JsonObject();
			json.add("values", values);
			return json;
		}
	}
}
