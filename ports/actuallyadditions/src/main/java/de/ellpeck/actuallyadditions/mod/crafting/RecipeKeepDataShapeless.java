/*
 * This file ("RecipeKeepDataShapeless.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.crafting;

import com.google.gson.JsonObject;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * SkyTekx: the shapeless recipe of upstream e0a8eed3 that copies the NBT of the ingredient marked with
 * TargetNBTIngredient onto its result, which the drill dye recipes of db9095e0 (#1459) use. On 1.20.1
 * the drill keeps its upgrades and its energy in that NBT. The 1.21 data components and the
 * 1.20.4 attachments it copies on main have no counterpart here
 */
public class RecipeKeepDataShapeless extends ShapelessRecipe {
    public static String NAME = "copy_nbt_shapeless";

    public RecipeKeepDataShapeless(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients) {
        super(id, group, category, result, ingredients);
    }

    public RecipeKeepDataShapeless(ShapelessRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.getIngredients());
    }

    @Nonnull
    @Override
    public RecipeSerializer<?> getSerializer() {
        return ActuallyRecipes.KEEP_DATA_SHAPELESS_RECIPE.get();
    }

    @Nonnull
    @Override
    public ItemStack assemble(@Nonnull CraftingContainer container, @Nonnull RegistryAccess registryAccess) {
        ItemStack result = super.assemble(container, registryAccess);

        TargetNBTIngredient donorIngredient = null;
        ItemStack datasource = ItemStack.EMPTY;
        NonNullList<Ingredient> ingredients = this.getIngredients();
        for (Ingredient ingredient : ingredients) {
            if (ingredient instanceof TargetNBTIngredient target) {
                donorIngredient = target;
                break;
            }
        }

        if (donorIngredient != null && !container.isEmpty()) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                final ItemStack item = container.getItem(i);
                if (!item.isEmpty() && donorIngredient.test(item)) {
                    datasource = item;
                    break;
                }
            }
        }

        // No donor in the grid gives no result, as on main. A donor without a tag (a drill that
        // never held energy or upgrades) gives a plain result, where main copies the default components
        if (datasource.isEmpty()) {
            ActuallyAdditions.LOGGER.info("AA.KeepDataShapeless missing TargetNBTIngredient");
            return ItemStack.EMPTY;
        }
        RecipeKeepDataShaped.copyData(datasource, result);

        return result;
    }

    public static class Serializer implements RecipeSerializer<RecipeKeepDataShapeless> {
        @Nonnull
        @Override
        public RecipeKeepDataShapeless fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json) {
            return new RecipeKeepDataShapeless(RecipeSerializer.SHAPELESS_RECIPE.fromJson(recipeId, json));
        }

        @Nullable
        @Override
        public RecipeKeepDataShapeless fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer) {
            ShapelessRecipe recipe = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(recipeId, buffer);
            return recipe == null ? null : new RecipeKeepDataShapeless(recipe);
        }

        @Override
        public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RecipeKeepDataShapeless recipe) {
            try {
                RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
            }
            catch (Exception e) {
                ActuallyAdditions.LOGGER.info("Failed to serialize {} Recipe to packet: {}", NAME, e.getMessage());
                throw e;
            }
        }
    }
}
