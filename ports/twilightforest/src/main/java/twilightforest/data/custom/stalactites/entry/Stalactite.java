package twilightforest.data.custom.stalactites.entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import twilightforest.TwilightForestMod;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A speleothem (stalactite or stalagmite) shape and the blocks it is made of.
 * <p>
 * TF 4.8 stores the blocks as {@code Either<List<Pair<Block, Integer>>, Block>}; this port keeps the weighted map that
 * BlockSpikeFeature turns into one weighted list per spike. The JSON format is 4.8's: {@code "ores"} is either a single block id
 * or a list of {@code {"block": <id>, "weight": <int>}}. {@link #fromLegacyJson} still reads the 4.3 {@code "blocks"} format.
 *
 * @param ores          The blocks of this speleothem with their weights, in datapack order. A single block is used as-is.
 * @param sizeVariation The shortest length as a fraction of maxLength (a lower number typically means longer speleothems)
 * @param maxLength     The longest this speleothem grows
 * @param weight        How often this speleothem is picked relative to the others of the same list
 */
public record Stalactite(Map<Block, Integer> ores, float sizeVariation, int maxLength, int weight) {
	// 1.20.1's defaulted block registry reads an unknown id (such as an ore of a mod that is not installed) as air instead of failing like 1.21.
	// Reject it, so the entry is skipped with an error instead of carving air spikes into the ceiling
	private static final Codec<Block> BLOCK_CODEC = ExtraCodecs.validate(BuiltInRegistries.BLOCK.byNameCodec(), block -> block == Blocks.AIR ? DataResult.error(() -> "Unknown block, or air") : DataResult.success(block));
	private static final Codec<Pair<Block, Integer>> WEIGHTED_BLOCK_CODEC = Codec.pair(BLOCK_CODEC.fieldOf("block").codec(), Codec.INT.fieldOf("weight").codec());
	private static final Codec<Map<Block, Integer>> ORES_CODEC = Codec.either(WEIGHTED_BLOCK_CODEC.listOf(), BLOCK_CODEC).xmap(
			either -> either.map(Stalactite::toOreMap, block -> Map.of(block, 1)),
			ores -> ores.size() == 1 ? Either.right(ores.keySet().iterator().next()) : Either.left(ores.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList())
	);

	public static final Codec<Stalactite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ORES_CODEC.fieldOf("ores").forGetter(Stalactite::ores),
			Codec.FLOAT.fieldOf("size_variation").forGetter(Stalactite::sizeVariation),
			Codec.INT.fieldOf("max_length").forGetter(Stalactite::maxLength),
			Codec.INT.fieldOf("weight").forGetter(Stalactite::weight)
	).apply(instance, Stalactite::new));

	public Stalactite(Block ore, float sizeVariation, int maxLength, int weight) {
		this(Map.of(ore, 1), sizeVariation, maxLength, weight);
	}

	private static Map<Block, Integer> toOreMap(List<Pair<Block, Integer>> ores) {
		// Keep the datapack's order so the weighted ore pick is deterministic for a given seed
		Map<Block, Integer> map = new LinkedHashMap<>();
		for (Pair<Block, Integer> ore : ores)
			map.put(ore.getFirst(), ore.getSecond());
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Reads a TF 4.3 stalactite entry, {@code {"blocks": [{"block": "iron_ore", "weight": 1}], "size_variation": 0.7, "max_length": 8, "weight": 24}},
	 * as found under the old {@code data/<namespace>/stalactites/entries/} path. Block ids without a namespace are vanilla ones.
	 */
	public static Stalactite fromLegacyJson(JsonElement json) {
		JsonObject jsonobject = GsonHelper.convertToJsonObject(json, "stalactite");
		float size = GsonHelper.getAsFloat(jsonobject, "size_variation");
		int maxLength = GsonHelper.getAsInt(jsonobject, "max_length");
		int weight = GsonHelper.getAsInt(jsonobject, "weight");

		JsonArray array = GsonHelper.getAsJsonArray(jsonobject, "blocks");
		Map<Block, Integer> map = new LinkedHashMap<>();
		for (JsonElement element : array) {
			JsonObject entry = GsonHelper.convertToJsonObject(element, "block entry");
			ResourceLocation blockId = ResourceLocation.tryParse(GsonHelper.getAsString(entry, "block"));
			Block block = blockId == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(blockId);
			if (block == Blocks.AIR) {
				TwilightForestMod.LOGGER.warn("Skipping unknown block {} in legacy stalactite entry", GsonHelper.getAsString(entry, "block"));
				continue;
			}
			map.put(block, GsonHelper.getAsInt(entry, "weight"));
		}

		return new Stalactite(Collections.unmodifiableMap(map), size, maxLength, weight);
	}

	@Override
	public String toString() {
		return String.format("Stalactite: Ore:%s, Length:%s, Variation:%s, Weight:%s", this.ores(), this.maxLength(), this.sizeVariation(), this.weight());
	}
}
