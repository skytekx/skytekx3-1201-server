package com.blakebr0.mysticalautomation.crafting;

import com.blakebr0.cucumber.event.RecipeManagerLoadingEvent;
import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.config.ModConfigs;
import com.blakebr0.mysticalautomation.MysticalAutomation;
import com.blakebr0.mysticalautomation.compat.MysticalCompat;
import com.blakebr0.mysticalautomation.crafting.recipe.FarmerRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public final class DynamicRecipeManager {
    public static final DynamicRecipeManager INSTANCE = new DynamicRecipeManager();

    @SubscribeEvent
    public void onRecipeManagerLoading(RecipeManagerLoadingEvent event) {
        if (com.blakebr0.mysticalautomation.config.ModConfigs.FARMER_DYNAMIC_MYSTICAL_AGRICULTURE_RECIPES.get()) {
            for (var crop : MysticalAgricultureAPI.getCropRegistry().getCrops()) {
                if (crop.isEnabled() && !"inferium".equals(crop.getName())) {
                    var id = MysticalAutomation.resource("farmer/mysticalagriculture/" + crop.getNameWithSuffix("seeds"));
                    var recipe = createResourceSeedRecipe(id, crop);

                    if (recipe != null) {
                        event.addRecipe(recipe);
                    }
                }
            }
        }
    }

    // SkyTekx port: 1.20.1 recipes carry their id, which upstream's RecipeHolder holds
    private static FarmerRecipe createResourceSeedRecipe(ResourceLocation id, Crop crop) {
        var seeds = crop.getSeedsItem();
        if (seeds == null)
            return null;

        var farmland = crop.getTier().getFarmland();
        if (farmland == null)
            return null;

        var essence = crop.getEssenceItem();
        if (essence == null)
            return null;

        var crux = crop.getCruxBlock() != null ? Ingredient.of(crop.getCruxBlock()) : Ingredient.EMPTY;
        var stages = crop.getCropBlock() != null ? crop.getCropBlock().getMaxAge() : 7;

        // SkyTekx port: Mystical Agriculture 7 has no getConfigValues() in its API, the value is its own config
        var fertilizedEssenceChance = ModConfigs.FERTILIZED_ESSENCE_DROP_CHANCE.get().floatValue();

        var results = List.of(
                new FarmerRecipe.FarmerResult(new ItemStack(crop.getEssenceItem()), 1.0F),
                new FarmerRecipe.FarmerResult(new ItemStack(MysticalCompat.Items.FERTILIZED_ESSENCE.get()), fertilizedEssenceChance)
        );

        return new FarmerRecipe(id, Ingredient.of(seeds), Ingredient.of(farmland), crux, stages, results);
    }
}
