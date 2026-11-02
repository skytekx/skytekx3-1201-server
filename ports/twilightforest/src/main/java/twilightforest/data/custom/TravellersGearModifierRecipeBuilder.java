package twilightforest.data.custom;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFRecipes;
import twilightforest.item.recipe.travellers.TravellersGearModifierRecipe;
import twilightforest.item.recipe.travellers.TravellersGearModifierShapedRecipe;
import twilightforest.item.recipe.travellers.TravellersGearModifierShapelessRecipe;
import twilightforest.item.recipe.travellers.TravellersShapedPattern;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Datagen for the Traveller's modifier recipes, saved under 4.8's computed ids
 * ({@link TravellersGearModifierRecipe#computeId}). Backport of 4.8's TravellersGearComponentModifierBuilder; 4.8's
 * CartesianShaped/ShapelessRecipeBuilder, which splits a key listing several pieces of gear into one recipe per piece,
 * is left out: only Auto-Repair needs it, and CraftingGenerator loops over its four pieces instead.
 */
public final class TravellersGearModifierRecipeBuilder {

	private TravellersGearModifierRecipeBuilder() {
	}

	public static void shaped(Consumer<FinishedRecipe> consumer, ResourceKey<TravellersModifier> modifier, Map<Character, Ingredient> key, String... pattern) {
		shaped(consumer, modifier, false, key, pattern);
	}

	public static void shaped(Consumer<FinishedRecipe> consumer, ResourceKey<TravellersModifier> modifier, boolean isRotated, Map<Character, Ingredient> key, String... pattern) {
		TravellersShapedPattern shapedPattern = TravellersShapedPattern.of(key, List.of(pattern));
		consumer.accept(new ShapedResult(TravellersGearModifierRecipe.computeId(modifier, shapedPattern.ingredients(), isRotated), shapedPattern, modifier, isRotated));
	}

	public static void shapeless(Consumer<FinishedRecipe> consumer, ResourceKey<TravellersModifier> modifier, Ingredient... ingredients) {
		List<Ingredient> list = List.of(ingredients);
		consumer.accept(new ShapelessResult(TravellersGearModifierRecipe.computeId(modifier, list, false), list, modifier));
	}

	private record ShapedResult(ResourceLocation id, TravellersShapedPattern pattern, ResourceKey<TravellersModifier> modifier, boolean isRotated) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject json) {
			TravellersGearModifierShapedRecipe.Serializer.toJson(json, this.pattern, this.modifier, this.isRotated);
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return TFRecipes.MODIFIER_SHAPED_RECIPE_SERIALIZER.get();
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}
	}

	private record ShapelessResult(ResourceLocation id, List<Ingredient> ingredients, ResourceKey<TravellersModifier> modifier) implements FinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject json) {
			TravellersGearModifierShapelessRecipe.Serializer.toJson(json, this.ingredients, this.modifier);
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return TFRecipes.MODIFIER_SHAPELESS_RECIPE_SERIALIZER.get();
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementId() {
			return null;
		}
	}
}
