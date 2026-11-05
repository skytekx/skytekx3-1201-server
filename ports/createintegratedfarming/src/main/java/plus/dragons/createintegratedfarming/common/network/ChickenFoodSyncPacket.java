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

package plus.dragons.createintegratedfarming.common.network;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createintegratedfarming.common.ranching.roost.chicken.ChickenFoodMap;
import plus.dragons.createintegratedfarming.common.registry.CIFDataMaps;

/**
 * SkyTekx: sends the resolved chicken food maps to clients (id 1 of {@link CIFPackets}), as NeoForge syncs
 * the two data maps of the 1.21.1 build. Clients use them to predict feeding by hand.
 */
public final class ChickenFoodSyncPacket {
    private static final int MAX_ENTRIES = 65536;
    private final Map<ResourceLocation, net.minecraft.nbt.CompoundTag> items;
    private final Map<ResourceLocation, net.minecraft.nbt.CompoundTag> fluids;

    private ChickenFoodSyncPacket(Map<ResourceLocation, net.minecraft.nbt.CompoundTag> items,
            Map<ResourceLocation, net.minecraft.nbt.CompoundTag> fluids) {
        this.items = items;
        this.fluids = fluids;
    }

    public static ChickenFoodSyncPacket create() {
        return new ChickenFoodSyncPacket(encodeAll(CIFDataMaps.CHICKEN_FOOD_ITEMS), encodeAll(CIFDataMaps.CHICKEN_FOOD_FLUIDS));
    }

    private static <T, V> Map<ResourceLocation, net.minecraft.nbt.CompoundTag> encodeAll(ChickenFoodMap<T, V> map) {
        Map<ResourceLocation, net.minecraft.nbt.CompoundTag> encoded = new java.util.LinkedHashMap<>();
        map.resolveServer().forEach((key, value) -> encoded.put(map.registry().getKey(key), map.encodeValue(value)));
        return encoded;
    }

    public void encode(FriendlyByteBuf buffer) {
        write(buffer, items);
        write(buffer, fluids);
    }

    private static void write(FriendlyByteBuf buffer, Map<ResourceLocation, net.minecraft.nbt.CompoundTag> values) {
        buffer.writeVarInt(values.size());
        values.forEach((id, value) -> {
            buffer.writeResourceLocation(id);
            buffer.writeNbt(value);
        });
    }

    public static ChickenFoodSyncPacket decode(FriendlyByteBuf buffer) {
        return new ChickenFoodSyncPacket(read(buffer), read(buffer));
    }

    private static Map<ResourceLocation, net.minecraft.nbt.CompoundTag> read(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_ENTRIES)
            throw new IllegalArgumentException("Invalid chicken food entry count: " + size);
        Map<ResourceLocation, net.minecraft.nbt.CompoundTag> values = new java.util.LinkedHashMap<>();
        for (int i = 0; i < size; i++)
            values.put(buffer.readResourceLocation(), buffer.readNbt());
        return values;
    }

    public void apply() {
        apply(CIFDataMaps.CHICKEN_FOOD_ITEMS, items);
        apply(CIFDataMaps.CHICKEN_FOOD_FLUIDS, fluids);
    }

    private static <T, V> void apply(ChickenFoodMap<T, V> map, Map<ResourceLocation, net.minecraft.nbt.CompoundTag> values) {
        Map<T, V> decoded = new IdentityHashMap<>();
        values.forEach((id, value) -> {
            if (value == null || !map.registry().containsKey(id))
                return;
            decoded.put(map.registry().get(id), map.decodeValue(value));
        });
        map.acceptClient(decoded);
    }
}
