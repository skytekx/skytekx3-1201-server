package ianm1647.expandeddelight.integration.kubejs.recipe;

import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.ItemComponents;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;

/**
 * The juicing schema with upstream's keys and order: event.recipes.expandeddelight.juicing(ingredients,
 * result, experience, juicingtime, container), the last three optional.
 */
public interface JuicingRecipeJS {
    RecipeKey<InputItem[]> INGREDIENTS = ItemComponents.UNWRAPPED_INPUT_ARRAY.key("ingredients");
    RecipeKey<OutputItem> RESULT = ItemComponents.OUTPUT.key("result");
    RecipeKey<Float> EXPERIENCE = NumberComponent.FLOAT.key("experience").optional(0f);
    RecipeKey<Integer> TIME = NumberComponent.INT.key("juicingtime").optional(200);
    RecipeKey<OutputItem> CONTAINER = ItemComponents.OUTPUT.key("container").defaultOptional();

    RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULT, EXPERIENCE, TIME, CONTAINER);

}
