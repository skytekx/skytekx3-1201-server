package com.blakebr0.mysticalautomation.util;

import com.blakebr0.cucumber.helper.RecipeHelper;
import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.init.ModRecipeTypes;
import com.blakebr0.mysticalautomation.network.NetworkHandler;
import com.blakebr0.mysticalautomation.network.payload.ReloadIngredientCachePayload;
import com.google.common.base.Stopwatch;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class RecipeIngredientCache {
    public static final RecipeIngredientCache INSTANCE = new RecipeIngredientCache();

    private final Map<Key, Map<Item, List<Ingredient>>> caches;

    private RecipeIngredientCache() {
        this.caches = new HashMap<>();
    }

    @SubscribeEvent
    public void onDatapackSyncEvent(OnDatapackSyncEvent event) {
        var payload = new ReloadIngredientCachePayload(this.caches);
        var player = event.getPlayer();

        // send the new caches to the client
        if (player != null) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), payload);
        } else {
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), payload);
        }
    }

    // SkyTekx port: Cucumber 7 has no RecipeManagerLoadedEvent. As Mystical Agriculture 7 does for its own
    // ingredient cache, the cache is built once the tags of the new server data are bound, from the recipe manager
    // that Cucumber's RecipeHelper took from the same reload
    @SubscribeEvent
    public void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD)
            return;

        var stopwatch = Stopwatch.createStarted();

        this.caches.clear();
        this.cacheFarmerRecipes();

        MysticalAutomation.LOGGER.info("Recipe ingredient caching done in {} ms", stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
    }

    // called on the client by ReloadIngredientCacheMessage
    public void setCaches(Map<Key, Map<Item, List<Ingredient>>> caches) {
        this.caches.clear();
        this.caches.putAll(caches);
    }

    public boolean isValidInput(Key key, ItemStack stack) {
        var cache = this.caches.getOrDefault(key, Collections.emptyMap()).get(stack.getItem());
        return cache != null && cache.stream().anyMatch(i -> i.test(stack));
    }

    private void cacheFarmerRecipes() {
        this.caches.put(Key.FARMER_SEEDS, new HashMap<>());
        this.caches.put(Key.FARMER_SOIL, new HashMap<>());
        this.caches.put(Key.FARMER_CRUX, new HashMap<>());

        if (RecipeHelper.getRecipeManager() == null)
            return;

        var recipes = RecipeHelper.getRecipes(ModRecipeTypes.FARMER.get()).values();
        for (var recipe : recipes) {
            var ingredients = recipe.getIngredients();
            for (var i = 0; i < 3; i++) {
                var ingredient = ingredients.get(i);
                var items = new HashSet<>();
                for (var stack : ingredient.getItems()) {
                    var item = stack.getItem();
                    if (items.contains(item))
                        continue;

                    var cache = switch (i) {
                        case 0 -> this.caches.get(Key.FARMER_SEEDS);
                        case 1 -> this.caches.get(Key.FARMER_SOIL);
                        case 2 -> this.caches.get(Key.FARMER_CRUX);
                        default -> throw new IllegalStateException("Unexpected value: " + i);
                    };

                    items.add(item);
                    cache.computeIfAbsent(item, c -> new ArrayList<>()).add(ingredient);
                }
            }
        }
    }

    public enum Key implements StringRepresentable {
        FARMER_SEEDS("farmer_seeds"),
        FARMER_SOIL("farmer_soil"),
        FARMER_CRUX("farmer_crux");

        public final String name;

        Key(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
