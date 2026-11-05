package com.blakebr0.mysticalautomation.util;

import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
import com.blakebr0.mysticalautomation.compat.MysticalCompat;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * SkyTekx port: the parts of Mystical Agriculture 8's {@code IEnchanterRecipe} API that the Enchanternator uses and
 * that Mystical Agriculture 7's interface lacks, built on what 7.0.24 offers.
 * <p>
 * Mystical Agriculture 7's {@code EnchanterRecipe} matches its two ingredients in order, slot 0 against the first one
 * and slot 1 against the second, while version 8 matches them in any order. It reads the enchantment level of the
 * result from the counts in slots 0 and 1 and enchants the item in slot 2.
 */
public final class EnchanterRecipes {
    private EnchanterRecipes() { }

    /**
     * Finds the recipe for the two ingredients in either order, with the item to enchant in the third slot, as
     * Mystical Agriculture 8 matches it. The item to enchant has to be set, as version 8 requires
     */
    @Nullable
    public static IEnchanterRecipe find(Level level, ItemStack first, ItemStack second, ItemStack target) {
        if (target.isEmpty() || !MysticalCompat.RecipeTypes.ENCHANTER.isPresent())
            return null;

        var type = MysticalCompat.RecipeTypes.ENCHANTER.get();
        var manager = level.getRecipeManager();

        return manager.getRecipeFor(type, new SimpleContainer(first, second, target), level)
                .or(() -> manager.getRecipeFor(type, new SimpleContainer(second, first, target), level))
                .orElse(null);
    }

    /**
     * Mystical Agriculture 8's {@code IEnchanterRecipe#getCount}
     * @return the count of the ingredient with the given index for one enchantment level, or -1 if invalid
     */
    public static int getCount(IEnchanterRecipe recipe, int index) {
        var counts = recipe.getIngredientCounts();
        if (index < 0 || index >= counts.size() || index >= recipe.getIngredients().size())
            return -1;

        return counts.get(index);
    }

    /**
     * Mystical Agriculture 8's {@code IEnchanterRecipe#getMaxResultEnchantmentLevel}: for each ingredient the count of
     * the matching input stack divided by the count of one level, the lowest of these, at most the maximum level of the
     * enchantment
     * @param inputs the two ingredient stacks in any order, the item to enchant may follow
     */
    public static int getMaxResultEnchantmentLevel(IEnchanterRecipe recipe, List<ItemStack> inputs) {
        var ingredients = recipe.getIngredients();
        var taken = new boolean[2];
        var level = -1;

        for (var i = 0; i < ingredients.size(); i++) {
            var ingredient = ingredients.get(i);
            var count = getCount(recipe, i);
            if (ingredient.isEmpty() || count <= 0)
                continue;

            var found = false;

            for (var j = 0; j < 2 && j < inputs.size(); j++) {
                var stack = inputs.get(j);
                if (!taken[j] && !stack.isEmpty() && ingredient.test(stack)) {
                    taken[j] = true;
                    found = true;

                    var newLevel = stack.getCount() / count;
                    if (level < 0 || newLevel < level)
                        level = newLevel;

                    break;
                }
            }

            if (!found)
                return 0;
        }

        return Math.max(0, Math.min(level, recipe.getEnchantment().getMaxLevel()));
    }

    /**
     * Mystical Agriculture 8's {@code IEnchanterRecipe#assemble(CraftingInput, HolderLookup.Provider, int)}: the item to
     * enchant with the enchantment at the given level, or empty
     * @param inputs the two ingredient stacks in any order, then the item to enchant
     */
    public static ItemStack assemble(IEnchanterRecipe recipe, List<ItemStack> inputs, RegistryAccess access, int level) {
        var ingredients = recipe.getIngredients();
        var container = new SimpleContainer(3);
        var taken = new boolean[2];

        // Mystical Agriculture 7 reads the level from the counts in slots 0 and 1, so these get exactly the counts of
        // the requested level, in the order of the recipe
        for (var i = 0; i < ingredients.size() && i < 2; i++) {
            var ingredient = ingredients.get(i);
            var count = getCount(recipe, i);
            if (ingredient.isEmpty() || count <= 0)
                continue;

            for (var j = 0; j < 2 && j < inputs.size(); j++) {
                var stack = inputs.get(j);
                if (!taken[j] && !stack.isEmpty() && ingredient.test(stack)) {
                    taken[j] = true;
                    container.setItem(i, stack.copyWithCount(count * level));
                    break;
                }
            }
        }

        if (inputs.size() > 2) {
            container.setItem(2, inputs.get(2).copy());
        }

        return recipe.assemble(container, access);
    }
}
