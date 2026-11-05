/*
 * Copyright (C) 2026  SkyTekx
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createintegratedfarming.common.ranching.roost.chicken;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createintegratedfarming.common.CIFCommon;

/**
 * SkyTekx: a Forge 1.20.1 stand-in for a synced NeoForge data map of the 1.21.1 build.
 * <p>
 * It reads {@code data/<namespace>/data_maps/<registry>/<name>.json} from every data pack in pack order,
 * in the NeoForge format: an object {@code "values"} whose keys are ids or {@code #tags}, an optional
 * {@code "replace": true} that drops the entries of the packs below, and an optional {@code "remove"}
 * list. Later entries win. Each value goes through the codec of its map. For items that is
 * {@link ChickenFoodItem#CODEC}, which takes {@code using_converts_to} in the 1.21.1 object form
 * {@code {"id": "minecraft:bowl"}} and as a plain item id. The server resolves tags when asked, after the
 * reload has bound them. Clients get the resolved map from
 * {@link plus.dragons.createintegratedfarming.common.network.ChickenFoodSyncPacket}.
 */
public final class ChickenFoodMap<T, V> {
    private final ResourceLocation file;
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final Registry<T> registry;
    private final Codec<V> codec;
    private volatile List<Entry<T, V>> serverEntries = List.of();
    private volatile Map<T, V> clientValues = Map.of();

    public ChickenFoodMap(ResourceLocation id, String registryFolder, ResourceKey<? extends Registry<T>> registryKey,
            Registry<T> registry, Codec<V> codec) {
        this.file = new ResourceLocation(id.getNamespace(), "data_maps/" + registryFolder + "/" + id.getPath() + ".json");
        this.registryKey = registryKey;
        this.registry = registry;
        this.codec = codec;
    }

    public Codec<V> codec() {
        return codec;
    }

    public Registry<T> registry() {
        return registry;
    }

    /** Looks the value up on the side the level belongs to. */
    public @Nullable V get(boolean clientSide, T value) {
        return clientSide ? clientValues.get(value) : getServer(value);
    }

    public @Nullable V getServer(T value) {
        var entries = serverEntries;
        if (entries.isEmpty())
            return null;
        Holder<T> holder = registry.wrapAsHolder(value);
        ResourceLocation id = registry.getKey(value);
        for (int i = entries.size() - 1; i >= 0; i--) {
            var entry = entries.get(i);
            boolean matches = entry.tag() != null ? holder.is(entry.tag()) : entry.id().equals(id);
            if (matches)
                return entry.value();
        }
        return null;
    }

    /** All registered values that have an entry, resolved on the server. */
    public Map<T, V> resolveServer() {
        Map<T, V> resolved = new IdentityHashMap<>();
        if (serverEntries.isEmpty())
            return resolved;
        for (T value : registry) {
            V food = getServer(value);
            if (food != null)
                resolved.put(value, food);
        }
        return resolved;
    }

    public void acceptClient(Map<T, V> values) {
        clientValues = Map.copyOf(values);
    }

    public void clearClient() {
        clientValues = Map.of();
    }

    public void reload(ResourceManager manager) {
        List<Entry<T, V>> entries = new ArrayList<>();
        for (Resource resource : manager.getResourceStack(file)) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject json = GsonHelper.parse(reader);
                if (GsonHelper.getAsBoolean(json, "replace", false))
                    entries.clear();
                JsonObject values = GsonHelper.getAsJsonObject(json, "values", new JsonObject());
                for (var element : values.entrySet()) {
                    Entry<T, V> entry = parseEntry(element.getKey(), element.getValue());
                    entries.removeIf(existing -> existing.sameKey(entry));
                    entries.add(entry);
                }
                for (JsonElement removed : GsonHelper.getAsJsonArray(json, "remove", new com.google.gson.JsonArray())) {
                    Entry<T, V> key = parseKey(removed.getAsString());
                    entries.removeIf(existing -> existing.sameKey(key));
                }
            } catch (Exception exception) {
                CIFCommon.LOGGER.error("Could not read {} from pack {}", file, resource.sourcePackId(), exception);
            }
        }
        serverEntries = List.copyOf(entries);
    }

    private Entry<T, V> parseEntry(String key, JsonElement json) {
        JsonElement value = json.isJsonObject() && json.getAsJsonObject().has("value")
                ? json.getAsJsonObject().get("value")
                : json;
        V parsed = codec.parse(JsonOps.INSTANCE, value).getOrThrow(false, error -> {});
        Entry<T, V> entry = parseKey(key);
        return new Entry<>(entry.id(), entry.tag(), parsed);
    }

    private Entry<T, V> parseKey(String key) {
        if (key.startsWith("#")) {
            @SuppressWarnings("unchecked")
            TagKey<T> tag = TagKey.create((ResourceKey<Registry<T>>) registryKey, new ResourceLocation(key.substring(1)));
            return new Entry<>(null, tag, null);
        }
        return new Entry<>(new ResourceLocation(key), null, null);
    }

    public CompoundTag encodeValue(V value) {
        Tag tag = codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, error -> {});
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("value", tag);
        return wrapper;
    }

    public V decodeValue(CompoundTag wrapper) {
        return codec.parse(NbtOps.INSTANCE, wrapper.get("value")).getOrThrow(false, error -> {});
    }

    private record Entry<T, V>(@Nullable ResourceLocation id, @Nullable TagKey<T> tag, @Nullable V value) {
        boolean sameKey(Entry<T, V> other) {
            return tag != null ? tag.equals(other.tag) : id != null && id.equals(other.id);
        }
    }
}
