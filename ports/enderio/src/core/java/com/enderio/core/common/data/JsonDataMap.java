package com.enderio.core.common.data;

import com.enderio.core.EnderCore;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * SkyTekx: a Forge 1.20.1 stand-in for the NeoForge data maps of Ender IO 8.x (such as {@code enderio:vat_reagent} and
 * {@code enderio:range_extender}). A data map attaches a value to entries of a built-in registry (items, blocks, fluids
 * and so on) from datapack files. The files use the NeoForge location and format, so the data of 8.x carries over as is:
 * <pre>{@code
 * data/<namespace>/data_maps/<registry>/<name>.json
 * {
 *   "replace": false,                              // optional, true drops what lower datapacks defined
 *   "values": {
 *     "minecraft:gunpowder": { ... },              // a value for one registry entry
 *     "#forge:gunpowder": { ... },                 // a value for every entry of a tag
 *     "minecraft:sugar": { "value": { ... } }      // the long form, as NeoForge also accepts it
 *   },
 *   "remove": [ "minecraft:sugar", "#forge:dusts" ] // optional, entries that lose their value
 * }
 * }</pre>
 * {@code <registry>} is the path of the registry id ({@code item}, {@code block}), with its namespace in front for a
 * registry outside {@code minecraft}. Every datapack's copy of the file is read in pack order, and later values and
 * removals win over earlier ones. Ids and tags are resolved on the first lookup after a reload or a tag update, so a
 * value for an id that this game does not have (an item of a newer Minecraft version) is skipped with a debug line.
 * <p>
 * The server reads the files through the reload listener that {@link JsonDataMaps} adds on AddReloadListenerEvent, and
 * sends the resolved values to every client with {@link com.enderio.core.common.network.S2CDataMapSync} on
 * OnDatapackSyncEvent. A client keeps that copy apart from the server's values, so an integrated server never sees it.
 * {@link #get(Object)} picks the copy of the current logical side.
 * <p>
 * Create data maps with {@link #create} while the mod is constructed, before the first datapack load.
 */
public final class JsonDataMap<K, V> {

    private final ResourceLocation id;
    private final ResourceKey<? extends Registry<K>> registryKey;
    private final ValueSerializer<V> serializer;
    private final ResourceLocation file;

    // Server side: the parsed entries of all datapacks in pack order, resolved into serverValues on demand.
    private volatile List<Entry<K, V>> serverEntries = List.of();
    @Nullable
    private volatile Map<K, V> serverValues;

    // Client side: the values the server sent, by registry id.
    private volatile Map<ResourceLocation, V> syncedValues = Map.of();

    @Nullable
    private Registry<K> registry;

    private JsonDataMap(ResourceLocation id, ResourceKey<? extends Registry<K>> registryKey, ValueSerializer<V> serializer) {
        this.id = id;
        this.registryKey = registryKey;
        this.serializer = serializer;

        ResourceLocation registryId = registryKey.location();
        String folder = registryId.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)
            ? registryId.getPath()
            : registryId.getNamespace() + "/" + registryId.getPath();
        this.file = new ResourceLocation(id.getNamespace(), "data_maps/" + folder + "/" + id.getPath() + ".json");
    }

    /**
     * Creates and registers a data map. Call it during mod construction.
     *
     * @param id          the data map id, which also names the file
     * @param registryKey a built-in registry, such as {@code Registries.ITEM}
     * @param serializer  reads a value from JSON and writes it to the network
     * @throws IllegalArgumentException if the registry is not a built-in one or the id is taken
     */
    public static <K, V> JsonDataMap<K, V> create(ResourceLocation id, ResourceKey<? extends Registry<K>> registryKey, ValueSerializer<V> serializer) {
        if (!BuiltInRegistries.REGISTRY.containsKey(registryKey.location())) {
            throw new IllegalArgumentException("JSON data map " + id + " needs a built-in registry, " + registryKey.location() + " is not one");
        }

        JsonDataMap<K, V> dataMap = new JsonDataMap<>(id, registryKey, serializer);
        JsonDataMaps.register(dataMap);
        return dataMap;
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceKey<? extends Registry<K>> registryKey() {
        return registryKey;
    }

    public ValueSerializer<V> serializer() {
        return serializer;
    }

    /**
     * @return the datapack location of this data map, for example {@code enderio:data_maps/item/vat_reagent.json}
     */
    public ResourceLocation file() {
        return file;
    }

    /**
     * The value of a registry entry on the current logical side: the datapack values on the server, the values the
     * server sent on a client.
     * <p>
     * The logical side comes from {@link EffectiveSide}, which reports the server only for threads of the server
     * thread group. On a physical client, an integrated server worker thread (such as Worker-Main or
     * Util.backgroundExecutor) therefore reads the synced copy, which is empty until the host player has joined.
     * Server-only code, such as machine tasks and tick code, should call {@link #getServerValue(Object)} instead, and
     * keep this method for screens and JEI.
     *
     * @return the value, or null when the entry has none
     */
    @Nullable
    public V get(K key) {
        return isLogicalClient() ? getSyncedValue(key) : getServerValue(key);
    }

    public V getOrDefault(K key, V defaultValue) {
        V value = get(key);
        return value != null ? value : defaultValue;
    }

    @Nullable
    public V getServerValue(K key) {
        return getServerValues().get(key);
    }

    @Nullable
    public V getSyncedValue(K key) {
        ResourceLocation keyId = registry().getKey(key);
        return keyId != null ? syncedValues.get(keyId) : null;
    }

    /**
     * Server side: every registry entry that has a value, with ids and tags resolved against the current tags.
     */
    public Map<K, V> getServerValues() {
        Map<K, V> values = serverValues;
        if (values == null) {
            synchronized (this) {
                values = serverValues;
                if (values == null) {
                    values = resolve(serverEntries);
                    serverValues = values;
                }
            }
        }

        return values;
    }

    /**
     * Client side: the values the server sent last, by registry id. Empty until the server sent them.
     */
    public Map<ResourceLocation, V> getSyncedValues() {
        return syncedValues;
    }

    /**
     * The values for the sync packet: every entry with a value, by registry id.
     */
    public Map<ResourceLocation, V> getServerValuesById() {
        Registry<K> registry = registry();
        Map<ResourceLocation, V> values = new LinkedHashMap<>();
        getServerValues().forEach((key, value) -> values.put(registry.getKey(key), value));
        return values;
    }

    public void setSyncedValues(Map<ResourceLocation, V> values) {
        syncedValues = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public void clearSyncedValues() {
        syncedValues = Map.of();
    }

    /**
     * Drops the resolved server values, so the next lookup resolves the entries again (after the tags changed).
     */
    public synchronized void invalidate() {
        serverValues = null;
    }

    /**
     * Drops the server's datapack values, when the server stopped.
     */
    public synchronized void clearServerValues() {
        serverEntries = List.of();
        serverValues = null;
    }

    /**
     * @return a new reload listener that reads this data map from the server's datapacks
     */
    public PreparableReloadListener createReloadListener() {
        return new Loader();
    }

    private Registry<K> registry() {
        Registry<K> found = registry;
        if (found == null) {
            //noinspection unchecked
            found = (Registry<K>) BuiltInRegistries.REGISTRY.get(registryKey.location());
            if (found == null) {
                throw new IllegalStateException("Registry " + registryKey.location() + " of JSON data map " + id + " is missing");
            }

            registry = found;
        }

        return found;
    }

    private static boolean isLogicalClient() {
        // A dedicated server has no client side. On a physical client, the integrated server runs on its own thread group.
        return FMLEnvironment.dist.isClient() && EffectiveSide.get().isClient();
    }

    private Map<K, V> resolve(List<Entry<K, V>> entries) {
        Registry<K> registry = registry();
        Map<K, V> values = new HashMap<>();
        int skipped = 0;

        for (Entry<K, V> entry : entries) {
            Optional<TagKey<K>> tag = entry.target().left();
            if (tag.isPresent()) {
                Optional<HolderSet.Named<K>> holders = registry.getTag(tag.get());
                if (holders.isEmpty()) {
                    skipped++;
                    EnderCore.LOGGER.debug("JSON data map {}: skipping #{}, a tag that this game does not have", id, tag.get().location());
                    continue;
                }

                for (Holder<K> holder : holders.get()) {
                    entry.applyTo(values, holder.value());
                }
            } else {
                ResourceLocation entryId = entry.target().right().orElseThrow();
                Optional<K> value = registry.getOptional(entryId);
                if (value.isEmpty()) {
                    skipped++;
                    EnderCore.LOGGER.debug("JSON data map {}: skipping {}, an id that this game does not have", id, entryId);
                    continue;
                }

                entry.applyTo(values, value.get());
            }
        }

        EnderCore.LOGGER.debug("JSON data map {}: resolved {} entries into values for {} registry entries, skipped {}",
            id, entries.size(), values.size(), skipped);
        return Collections.unmodifiableMap(values);
    }

    // Runs on a worker thread during the reload: reads the files only, the registries are resolved later.
    private List<Entry<K, V>> load(ResourceManager resourceManager) {
        List<Entry<K, V>> loaded = new ArrayList<>();
        List<Resource> stack = resourceManager.getResourceStack(file);

        for (Resource resource : stack) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                if (GsonHelper.getAsBoolean(json, "replace", false)) {
                    loaded.clear();
                }

                for (Map.Entry<String, JsonElement> value : GsonHelper.getAsJsonObject(json, "values", new JsonObject()).entrySet()) {
                    try {
                        JsonElement element = value.getValue();
                        if (element.isJsonObject() && element.getAsJsonObject().has("value")) {
                            element = element.getAsJsonObject().get("value");
                        }

                        loaded.add(new Entry<>(parseTarget(value.getKey()), Optional.of(serializer.fromJson(element))));
                    } catch (JsonParseException | IllegalArgumentException e) {
                        EnderCore.LOGGER.error("JSON data map {}: skipping the invalid entry '{}' in data pack {}: {}",
                            id, value.getKey(), resource.sourcePackId(), e.getMessage());
                    }
                }

                if (json.has("remove")) {
                    for (String removal : removals(json.get("remove"))) {
                        try {
                            loaded.add(new Entry<>(parseTarget(removal), Optional.empty()));
                        } catch (JsonParseException e) {
                            EnderCore.LOGGER.error("JSON data map {}: skipping the invalid removal '{}' in data pack {}: {}",
                                id, removal, resource.sourcePackId(), e.getMessage());
                        }
                    }
                }
            } catch (IOException | RuntimeException e) {
                EnderCore.LOGGER.error("JSON data map {}: couldn't read {} in data pack {}", id, file, resource.sourcePackId(), e);
            }
        }

        EnderCore.LOGGER.debug("JSON data map {}: read {} entries from {} files", id, loaded.size(), stack.size());
        return loaded;
    }

    // NeoForge accepts a list of targets, or an object keyed by target (its values hold remover options this map has none of).
    private static List<String> removals(JsonElement json) {
        if (json.isJsonObject()) {
            return List.copyOf(json.getAsJsonObject().keySet());
        }

        List<String> removals = new ArrayList<>();
        for (JsonElement removal : GsonHelper.convertToJsonArray(json, "remove")) {
            removals.add(GsonHelper.convertToString(removal, "remove"));
        }

        return removals;
    }

    private Either<TagKey<K>, ResourceLocation> parseTarget(String target) {
        boolean isTag = target.startsWith("#");
        ResourceLocation location = ResourceLocation.tryParse(isTag ? target.substring(1) : target);
        if (location == null) {
            throw new JsonSyntaxException("'" + target + "' is not a valid id or tag");
        }

        return isTag ? Either.left(TagKey.create(registryKey, location)) : Either.right(location);
    }

    @Override
    public String toString() {
        return "JsonDataMap[" + id + "]";
    }

    /**
     * Reads a value of a data map from JSON and moves it over the network.
     */
    public interface ValueSerializer<V> {
        /**
         * @throws JsonParseException when the JSON is not a valid value
         */
        V fromJson(JsonElement json);

        void toNetwork(FriendlyByteBuf buf, V value);

        V fromNetwork(FriendlyByteBuf buf);
    }

    /**
     * A value that maps tags to decimal numbers, written as {@code {"#namespace:tag": 0.5}}. This is the value of the 8.x
     * {@code enderio:vat_reagent} data map, a strict map of {@code TagKey.hashedCodec} to {@code Codec.DOUBLE}.
     */
    public static <T> ValueSerializer<Map<TagKey<T>, Double>> tagToDouble(ResourceKey<? extends Registry<T>> tagRegistry) {
        return tagMap(tagRegistry, GsonHelper::convertToDouble, FriendlyByteBuf::writeDouble, FriendlyByteBuf::readDouble);
    }

    /**
     * A value that maps tags to whole numbers, written as {@code {"#namespace:tag": 16}}. This is the value of the 8.x
     * {@code enderio:range_extender} data map, a strict map of {@code TagKey.hashedCodec} to {@code Codec.INT}.
     */
    public static <T> ValueSerializer<Map<TagKey<T>, Integer>> tagToInteger(ResourceKey<? extends Registry<T>> tagRegistry) {
        return tagMap(tagRegistry, GsonHelper::convertToInt, FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
    }

    private static <T, N> ValueSerializer<Map<TagKey<T>, N>> tagMap(ResourceKey<? extends Registry<T>> tagRegistry,
        JsonNumberReader<N> jsonReader, BiConsumer<FriendlyByteBuf, N> networkWriter, Function<FriendlyByteBuf, N> networkReader) {

        return new ValueSerializer<>() {
            @Override
            public Map<TagKey<T>, N> fromJson(JsonElement json) {
                // Strict like the upstream codec: one bad key or number rejects the whole value.
                Map<TagKey<T>, N> values = new LinkedHashMap<>();
                for (Map.Entry<String, JsonElement> entry : GsonHelper.convertToJsonObject(json, "value").entrySet()) {
                    String key = entry.getKey();
                    ResourceLocation tag = key.startsWith("#") ? ResourceLocation.tryParse(key.substring(1)) : null;
                    if (tag == null) {
                        throw new JsonSyntaxException("Expected a tag like '#namespace:path', got '" + key + "'");
                    }

                    values.put(TagKey.create(tagRegistry, tag), jsonReader.read(entry.getValue(), key));
                }

                return Collections.unmodifiableMap(values);
            }

            @Override
            public void toNetwork(FriendlyByteBuf buf, Map<TagKey<T>, N> value) {
                buf.writeMap(value, (b, tag) -> b.writeResourceLocation(tag.location()), networkWriter::accept);
            }

            @Override
            public Map<TagKey<T>, N> fromNetwork(FriendlyByteBuf buf) {
                Map<TagKey<T>, N> values = buf.readMap(LinkedHashMap::new, b -> TagKey.create(tagRegistry, b.readResourceLocation()), networkReader::apply);
                return Collections.unmodifiableMap(values);
            }
        };
    }

    @FunctionalInterface
    private interface JsonNumberReader<N> {
        N read(JsonElement json, String name);
    }

    // An empty value removes what an earlier entry gave.
    private record Entry<K, V>(Either<TagKey<K>, ResourceLocation> target, Optional<V> value) {
        void applyTo(Map<K, V> values, K key) {
            if (value.isPresent()) {
                values.put(key, value.get());
            } else {
                values.remove(key);
            }
        }
    }

    private class Loader extends SimplePreparableReloadListener<List<Entry<K, V>>> {
        @Override
        protected List<Entry<K, V>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            return load(resourceManager);
        }

        @Override
        protected void apply(List<Entry<K, V>> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
            synchronized (JsonDataMap.this) {
                serverEntries = List.copyOf(entries);
                serverValues = null;
            }
        }

        @Override
        public String getName() {
            return "JsonDataMap[" + id + "]";
        }
    }
}
