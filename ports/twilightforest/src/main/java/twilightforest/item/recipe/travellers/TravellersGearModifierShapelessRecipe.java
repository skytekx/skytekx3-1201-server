package twilightforest.item.recipe.travellers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import twilightforest.init.TFRecipes;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * A shapeless Traveller's modifier recipe ({@code twilightforest:travellers_gear_modifier_shapeless_recipe}). Backport
 * of 4.8's TravellersGearModifierShapelessRecipe, with the same JSON: {@code ingredients} and {@code modifier_key}.
 */
public class TravellersGearModifierShapelessRecipe extends TravellersGearModifierRecipe {
	protected final NonNullList<Ingredient> ingredients;

	public TravellersGearModifierShapelessRecipe(ResourceLocation id, NonNullList<Ingredient> ingredients, ResourceKey<TravellersModifier> travellersModifier) {
		super(id, travellersModifier);
		this.ingredients = ingredients;
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		if (!super.matches(input, level))
			return false;
		List<ItemStack> nonEmptyItems = new ArrayList<>(this.ingredients.size());
		for (ItemStack item : input.getItems()) {
			if (!item.isEmpty())
				nonEmptyItems.add(item);
		}
		if (nonEmptyItems.size() != this.ingredients.size())
			return false;
		return RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return this.ingredients.size() <= width * height;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return this.ingredients;
	}

	@Override
	public int getWidth() {
		return this.ingredients.size() > 4 ? 3 : 2;
	}

	@Override
	public int getHeight() {
		return this.ingredients.size() > 4 ? 3 : 2;
	}

	@Override
	public boolean isShapeless() {
		return true;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return TFRecipes.MODIFIER_SHAPELESS_RECIPE_SERIALIZER.get();
	}

	public static class Serializer implements RecipeSerializer<TravellersGearModifierShapelessRecipe> {

		@Override
		public TravellersGearModifierShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
			NonNullList<Ingredient> ingredients = NonNullList.create();
			for (JsonElement element : GsonHelper.getAsJsonArray(json, "ingredients")) {
				Ingredient ingredient = Ingredient.fromJson(element, false);
				// 4.8 reads the list with Ingredient.CODEC_NONEMPTY
				if (ingredient.isEmpty())
					throw new JsonParseException("Empty ingredient in Traveller's modifier recipe " + id);
				ingredients.add(ingredient);
			}
			if (ingredients.isEmpty())
				throw new JsonParseException("No ingredients for Traveller's modifier recipe " + id);
			ResourceKey<TravellersModifier> modifier = ResourceKey.create(TravellersModifiersManager.TRAVELLERS_MODIFIERS_KEY, new ResourceLocation(GsonHelper.getAsString(json, "modifier_key")));
			return new TravellersGearModifierShapelessRecipe(id, ingredients, modifier);
		}

		@Override
		public TravellersGearModifierShapelessRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
			int size = buffer.readVarInt();
			NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
			ingredients.replaceAll(ignored -> Ingredient.fromNetwork(buffer));
			return new TravellersGearModifierShapelessRecipe(id, ingredients, buffer.readResourceKey(TravellersModifiersManager.TRAVELLERS_MODIFIERS_KEY));
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, TravellersGearModifierShapelessRecipe recipe) {
			buffer.writeVarInt(recipe.ingredients.size());
			for (Ingredient ingredient : recipe.ingredients) {
				ingredient.toNetwork(buffer);
			}
			buffer.writeResourceKey(recipe.travellersModifierKey);
		}

		public static void toJson(JsonObject json, List<Ingredient> ingredients, ResourceKey<TravellersModifier> modifier) {
			JsonArray array = new JsonArray();
			ingredients.forEach(ingredient -> array.add(ingredient.toJson()));
			json.add("ingredients", array);
			json.addProperty("modifier_key", modifier.location().toString());
		}
	}
}
