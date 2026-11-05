package com.blakebr0.mysticalautomation.network.payload;

import com.blakebr0.mysticalautomation.util.RecipeIngredientCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// SkyTekx port: a SimpleChannel message (NetworkHandler) for upstream's CustomPacketPayload and StreamCodec
public record ReloadIngredientCachePayload(Map<RecipeIngredientCache.Key, Map<Item, List<Ingredient>>> caches) {
    public static ReloadIngredientCachePayload read(FriendlyByteBuf buffer) {
        var caches = new HashMap<RecipeIngredientCache.Key, Map<Item, List<Ingredient>>>();
        var types = buffer.readVarInt();

        for (var i = 0; i < types; i++) {
            var key = buffer.readEnum(RecipeIngredientCache.Key.class);
            var items = buffer.readVarInt();

            caches.put(key, new HashMap<>());

            for (var j = 0; j < items; j++) {
                var item = ForgeRegistries.ITEMS.getValue(buffer.readResourceLocation());
                var ingredients = buffer.readVarInt();

                for (var k = 0; k < ingredients; k++) {
                    var cache = caches.get(key).computeIfAbsent(item, l -> new ArrayList<>());
                    var ingredient = Ingredient.fromNetwork(buffer);

                    cache.add(ingredient);
                }
            }
        }

        return new ReloadIngredientCachePayload(caches);
    }

    public static void write(ReloadIngredientCachePayload payload, FriendlyByteBuf buffer) {
        buffer.writeVarInt(payload.caches.size());

        for (var entry : payload.caches.entrySet()) {
            var key = entry.getKey();
            var caches = entry.getValue();

            buffer.writeEnum(key);
            buffer.writeVarInt(caches.size());

            for (var cache : caches.entrySet()) {
                var item = ForgeRegistries.ITEMS.getKey(cache.getKey());
                var ingredients = cache.getValue();

                buffer.writeResourceLocation(item);
                buffer.writeVarInt(ingredients.size());

                for (var ingredient : ingredients) {
                    ingredient.toNetwork(buffer);
                }
            }
        }
    }

    public static void handleClient(ReloadIngredientCachePayload payload, Supplier<NetworkEvent.Context> context) {
        RecipeIngredientCache.INSTANCE.setCaches(payload.caches);
    }
}
