package twilightforest.item.recipe.travellers;

import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import twilightforest.init.TFRecipes;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

/**
 * A shaped Traveller's modifier recipe ({@code twilightforest:travellers_gear_modifier_shaped_recipe}). Backport of
 * 4.8's TravellersGearModifierShapedRecipe, with the same JSON: {@code pattern} (key and rows), {@code modifier_key}
 * and {@code is_rotated}.
 */
public class TravellersGearModifierShapedRecipe extends TravellersGearModifierRecipe {
	protected final TravellersShapedPattern pattern;
	protected final boolean isRotated;

	public TravellersGearModifierShapedRecipe(ResourceLocation id, TravellersShapedPattern pattern, ResourceKey<TravellersModifier> travellersModifier, boolean isRotated) {
		super(id, travellersModifier);
		this.pattern = pattern;
		this.isRotated = isRotated;
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		if (!super.matches(input, level))
			return false;
		return this.pattern.matches(input);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return this.getHeight() <= height && this.getWidth() <= width;
	}

	@Override
	public int getWidth() {
		return this.pattern.width();
	}

	@Override
	public int getHeight() {
		return this.pattern.height();
	}

	@Override
	public boolean isShapeless() {
		return false;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return this.pattern.ingredients();
	}

	public TravellersShapedPattern getPattern() {
		return this.pattern;
	}

	public boolean isRotated() {
		return this.isRotated;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return TFRecipes.MODIFIER_SHAPED_RECIPE_SERIALIZER.get();
	}

	public static class Serializer implements RecipeSerializer<TravellersGearModifierShapedRecipe> {

		@Override
		public TravellersGearModifierShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
			TravellersShapedPattern pattern = TravellersShapedPattern.fromJson(GsonHelper.getAsJsonObject(json, "pattern"));
			ResourceKey<TravellersModifier> modifier = ResourceKey.create(TravellersModifiersManager.TRAVELLERS_MODIFIERS_KEY, new ResourceLocation(GsonHelper.getAsString(json, "modifier_key")));
			return new TravellersGearModifierShapedRecipe(id, pattern, modifier, GsonHelper.getAsBoolean(json, "is_rotated"));
		}

		@Override
		public TravellersGearModifierShapedRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
			TravellersShapedPattern pattern = TravellersShapedPattern.fromNetwork(buffer);
			ResourceKey<TravellersModifier> modifier = buffer.readResourceKey(TravellersModifiersManager.TRAVELLERS_MODIFIERS_KEY);
			return new TravellersGearModifierShapedRecipe(id, pattern, modifier, buffer.readBoolean());
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, TravellersGearModifierShapedRecipe recipe) {
			recipe.pattern.toNetwork(buffer);
			buffer.writeResourceKey(recipe.travellersModifierKey);
			buffer.writeBoolean(recipe.isRotated);
		}

		public static void toJson(JsonObject json, TravellersShapedPattern pattern, ResourceKey<TravellersModifier> modifier, boolean isRotated) {
			json.add("pattern", pattern.toJson());
			json.addProperty("modifier_key", modifier.location().toString());
			json.addProperty("is_rotated", isRotated);
		}
	}
}
