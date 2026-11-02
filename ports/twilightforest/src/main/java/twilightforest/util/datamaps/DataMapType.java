package twilightforest.util.datamaps;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1.20.1 stand-in for TF 4.8's NeoForge data maps: a value attached to registry entries by a datapack file. The file uses
 * the NeoForge location and format, so 4.8 datapacks carry over unchanged:
 * <pre>{@code
 * data/<namespace>/data_maps/<registry>/<path>.json
 * {
 *   "replace": false,                                  // optional, true drops what lower datapacks defined
 *   "values": {
 *     "minecraft:ancient_debris": { ... },             // a value for one entry
 *     "#minecraft:coal_ores": { ... },                 // a value for every entry of a tag
 *     "minecraft:stone": { "value": { ... } }          // the long form is accepted too
 *   },
 *   "remove": [ "minecraft:stone", "#minecraft:logs" ] // optional
 * }
 * }</pre>
 * {@code <registry>} is the registry id's path ({@code block}, {@code worldgen/biome}), prefixed with its namespace for a
 * non-vanilla registry. Files from all datapacks are applied in pack order; a later value or removal for an entry
 * overrides an earlier one. Tags are resolved on lookup, so they always reflect the loaded tags.
 * <p>
 * Loaded server side only, by {@link DataMapLoader}. Data maps listed in {@code TFDataMaps.SYNCED_DATA_MAPS} are also sent
 * to clients (4.8 marks them {@code synced}) by {@link twilightforest.network.SyncDataMapPacket}; a client reads its copy
 * through {@link #getSyncedValues()}, which is kept apart from the server's entries so a singleplayer client never replaces them.
 */
public final class DataMapType<R, T> {
	private final ResourceLocation id;
	private final ResourceKey<? extends Registry<R>> registryKey;
	private final Codec<T> codec;

	private volatile List<Entry<R, T>> entries = List.of();
	private final Map<ResourceLocation, Optional<T>> resolved = new ConcurrentHashMap<>();
	private volatile Map<ResourceLocation, T> syncedValues = Map.of();

	public DataMapType(ResourceLocation id, ResourceKey<? extends Registry<R>> registryKey, Codec<T> codec) {
		this.id = id;
		this.registryKey = registryKey;
		this.codec = codec;
	}

	public ResourceLocation id() {
		return this.id;
	}

	public ResourceKey<? extends Registry<R>> registryKey() {
		return this.registryKey;
	}

	public Codec<T> codec() {
		return this.codec;
	}

	/**
	 * @return the datapack path of this data map, e.g. {@code twilightforest:data_maps/block/ore_map_color.json}
	 */
	public ResourceLocation filePath() {
		ResourceLocation registry = this.registryKey.location();
		String folder = registry.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE) ? registry.getPath() : registry.getNamespace() + "/" + registry.getPath();
		return new ResourceLocation(this.id.getNamespace(), "data_maps/" + folder + "/" + this.id.getPath() + ".json");
	}

	@Nullable
	public T get(Holder<R> holder) {
		Optional<ResourceKey<R>> key = holder.unwrapKey();
		if (key.isEmpty()) return null;
		return this.resolved.computeIfAbsent(key.get().location(), id -> this.resolve(holder, id)).orElse(null);
	}

	@Nullable
	public T get(Registry<R> registry, ResourceLocation id) {
		Optional<T> known = this.resolved.get(id);
		if (known != null) return known.orElse(null);
		return registry.getHolder(ResourceKey.create(this.registryKey, id)).map(this::get).orElse(null);
	}

	private Optional<T> resolve(Holder<R> holder, ResourceLocation id) {
		Optional<T> value = Optional.empty();
		for (Entry<R, T> entry : this.entries) {
			if (entry.key().map(tag -> holder.is(tag), entryId -> entryId.equals(id)))
				value = entry.value();
		}
		return value;
	}

	/**
	 * Server side: the value of every entry of the registry that has one, with tags resolved, in registry order.
	 */
	public Map<ResourceLocation, T> resolveAll(Registry<R> registry) {
		Map<ResourceLocation, T> values = new LinkedHashMap<>();
		registry.holders().forEach(holder -> {
			T value = this.get(holder);
			if (value != null)
				values.put(holder.key().location(), value);
		});
		return values;
	}

	/**
	 * Client side: the values the server last synced, keyed by registry id. Empty until the server sent them.
	 */
	public Map<ResourceLocation, T> getSyncedValues() {
		return this.syncedValues;
	}

	public void setSyncedValues(Map<ResourceLocation, T> values) {
		this.syncedValues = Collections.unmodifiableMap(new LinkedHashMap<>(values));
	}

	/**
	 * Forgets resolved lookups, for example after the tags changed.
	 */
	public void invalidate() {
		this.resolved.clear();
	}

	/**
	 * Reads this data map from every datapack. Runs during the reload's preparation; the returned task swaps the result in.
	 */
	Runnable load(ResourceManager resourceManager) {
		ResourceLocation file = this.filePath();
		List<Entry<R, T>> loaded = new ArrayList<>();

		for (Resource resource : resourceManager.getResourceStack(file)) {
			try (Reader reader = resource.openAsReader()) {
				JsonObject json = GsonHelper.parse(reader);

				if (GsonHelper.getAsBoolean(json, "replace", false))
					loaded.clear();

				for (Map.Entry<String, JsonElement> value : GsonHelper.getAsJsonObject(json, "values", new JsonObject()).entrySet()) {
					Either<TagKey<R>, ResourceLocation> key = this.parseKey(value.getKey());
					JsonElement element = value.getValue();
					if (element.isJsonObject() && element.getAsJsonObject().has("value"))
						element = element.getAsJsonObject().get("value");

					DataResult<T> decoded = this.codec.parse(JsonOps.INSTANCE, element);
					Optional<T> result = decoded.result();
					if (key == null || result.isEmpty()) {
						TwilightForestMod.LOGGER.error("Skipping invalid entry '{}' of data map {} in data pack {}: {}", value.getKey(), this.id, resource.sourcePackId(),
								key == null ? "not a valid id or tag" : decoded.error().map(DataResult.PartialResult::message).orElse("unknown error"));
						continue;
					}

					loaded.add(new Entry<>(key, result));
				}

				for (JsonElement removal : GsonHelper.getAsJsonArray(json, "remove", new JsonArray())) {
					Either<TagKey<R>, ResourceLocation> key = this.parseKey(GsonHelper.convertToString(removal, "remove"));
					if (key != null)
						loaded.add(new Entry<>(key, Optional.empty()));
				}
			} catch (IOException | RuntimeException e) {
				TwilightForestMod.LOGGER.error("Couldn't read data map {} from {} in data pack {}", this.id, file, resource.sourcePackId(), e);
			}
		}

		return () -> {
			this.entries = List.copyOf(loaded);
			this.invalidate();
			TwilightForestMod.LOGGER.debug("Loaded {} entries for data map {}", loaded.size(), this.id);
		};
	}

	@Nullable
	private Either<TagKey<R>, ResourceLocation> parseKey(String key) {
		boolean tag = key.startsWith("#");
		ResourceLocation location = ResourceLocation.tryParse(tag ? key.substring(1) : key);
		if (location == null) return null;
		return tag ? Either.left(TagKey.create(this.registryKey, location)) : Either.right(location);
	}

	// An empty value removes whatever an earlier entry assigned
	private record Entry<R, T>(Either<TagKey<R>, ResourceLocation> key, Optional<T> value) {
	}
}
