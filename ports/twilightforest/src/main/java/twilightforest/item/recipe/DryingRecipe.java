package twilightforest.item.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.Level;
import twilightforest.init.TFRecipes;

/**
 * A Drying Rack turns its single item into the result once it has hung there for {@link #getDryingTime()} ticks.
 * The JSON is TF 4.8's: {@code input} (a non-empty ingredient), {@code result} (1.20.1 item stack JSON) and {@code filter_time}.
 * The rack offers its item as slot 0 of a one-slot container.
 */
public class DryingRecipe extends SingleItemRecipe {

	private final int dryingTime;

	public DryingRecipe(ResourceLocation id, Ingredient ingredient, ItemStack result, int dryingTime) {
		super(TFRecipes.DRYING_RECIPE.get(), TFRecipes.DRYING_SERIALIZER.get(), id, "", ingredient, result);
		this.dryingTime = dryingTime;
	}

	@Override
	public boolean matches(Container container, Level level) {
		return this.ingredient.test(container.getItem(0));
	}

	public Ingredient getInput() {
		return this.ingredient;
	}

	public ItemStack getResult() {
		return this.result;
	}

	public int getDryingTime() {
		return this.dryingTime;
	}

	@Override
	public boolean isSpecial() {
		return true;
	}

	public static class Serializer implements RecipeSerializer<DryingRecipe> {

		@Override
		public DryingRecipe fromJson(ResourceLocation id, JsonObject json) {
			Ingredient input = Ingredient.fromJson(GsonHelper.isArrayNode(json, "input") ? GsonHelper.getAsJsonArray(json, "input") : GsonHelper.getAsJsonObject(json, "input"), false);
			ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
			int dryingTime = GsonHelper.getAsInt(json, "filter_time");
			return new DryingRecipe(id, input, result, dryingTime);
		}

		@Override
		public DryingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
			Ingredient input = Ingredient.fromNetwork(buffer);
			ItemStack result = buffer.readItem();
			int dryingTime = buffer.readInt();
			return new DryingRecipe(id, input, result, dryingTime);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, DryingRecipe recipe) {
			recipe.ingredient.toNetwork(buffer);
			buffer.writeItem(recipe.result);
			buffer.writeInt(recipe.dryingTime);
		}
	}
}
