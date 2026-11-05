package com.mrh0.createaddition.recipe.charging;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Create's processing recipe serializer, which Sequenced Assembly needs for its steps, with the
 * legacy JSON form of the 1.20.1 line on top. A recipe may name its input as "input" (one
 * ingredient) or "ingredients" (Create's list) and its output as "result" (one item stack) or
 * "results" (Create's list of processing outputs), in any mix, and its rate as "maxChargeRate"
 * or "max_charge_rate". The energy is always "energy". The network form is Create's, followed by
 * the energy and the max charge rate (ChargingRecipe#writeAdditional).
 */
public class ChargingRecipeSerializer extends ProcessingRecipeSerializer<ChargingRecipe> {

	private static final String INGREDIENT_KEY = "input";
	private static final String SEQUENCED_ASSEMBLY_INGREDIENT_KEY = "ingredients";
	private static final String RESULT_KEY = "result";
	private static final String SEQUENCED_ASSEMBLY_RESULT_KEY = "results";

	public ChargingRecipeSerializer(ProcessingRecipeBuilder.ProcessingRecipeFactory<ChargingRecipe> factory) {
		super(factory);
	}

	@Override
	protected ChargingRecipe readFromJson(ResourceLocation recipeId, JsonObject json) {
		// Create's own form, as KubeJS and the datagen of the 1.21.1 line write it.
		if(!json.has(INGREDIENT_KEY) && !json.has(RESULT_KEY))
			return super.readFromJson(recipeId, json);

		NonNullList<Ingredient> ingredients = NonNullList.create();
		if(json.has(INGREDIENT_KEY))
			ingredients.add(Ingredient.fromJson(json.get(INGREDIENT_KEY)));
		else
			for(JsonElement je : GsonHelper.getAsJsonArray(json, SEQUENCED_ASSEMBLY_INGREDIENT_KEY))
				ingredients.add(Ingredient.fromJson(je));

		NonNullList<ProcessingOutput> results = NonNullList.create();
		if(json.has(RESULT_KEY))
			results.add(new ProcessingOutput(ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, RESULT_KEY)), 1f));
		else
			for(JsonElement je : GsonHelper.getAsJsonArray(json, SEQUENCED_ASSEMBLY_RESULT_KEY))
				results.add(ProcessingOutput.deserialize(je));

		ChargingRecipe recipe = new ProcessingRecipeBuilder<>(getFactory(), recipeId)
				.withItemIngredients(ingredients)
				.withItemOutputs(results)
				.build();
		recipe.readAdditional(json);
		return recipe;
	}
}
