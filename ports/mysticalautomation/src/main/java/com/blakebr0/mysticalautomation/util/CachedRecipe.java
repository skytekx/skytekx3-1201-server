package com.blakebr0.mysticalautomation.util;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * SkyTekx port: Cucumber 8's {@code com.blakebr0.cucumber.inventory.CachedRecipe} (Cucumber, MIT License,
 * Copyright (c) 2018 BlakeBr0) takes any recipe input type. Cucumber 7's takes only {@code Recipe<Container>}, which
 * vanilla crafting recipes ({@code Recipe<CraftingContainer>}) are not, so the machines use this generic form.
 */
public class CachedRecipe<C extends Container, T extends Recipe<C>> {
    private final RecipeType<T> type;
    private T recipe;

    public CachedRecipe(RecipeType<T> type) {
        this.type = type;
    }

    /**
     * Checks if the provided inventory matches the cached recipe. If it doesn't match, it will try to find a new recipe
     * @param inventory the recipe inventory
     * @param level the level
     * @return does this recipe match the inventory
     */
    public boolean check(C inventory, Level level) {
        if (this.recipe != null && this.recipe.matches(inventory, level))
            return true;

        this.recipe = level.getRecipeManager()
                .getRecipeFor(this.type, inventory, level)
                .orElse(null);

        return this.recipe != null;
    }

    public boolean exists() {
        return this.recipe != null;
    }

    public T get() {
        return this.recipe;
    }

    public T checkAndGet(C inventory, Level level) {
        if (this.check(inventory, level))
            return this.recipe;

        return null;
    }
}
