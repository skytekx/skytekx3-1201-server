package twilightforest.data.custom;

import com.google.gson.JsonObject;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFRecipes;
import twilightforest.item.recipe.ScepterRepairRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScepterRecipeBuilder {
	private final Item scepter;
	private final int durability;
	private final List<Ingredient> repairItems = new ArrayList<>();

	private ScepterRecipeBuilder(Item scepter, int durability) {
		this.scepter = scepter;
		this.durability = durability;
	}

	public static ScepterRecipeBuilder repairFor(Item scepter, int durability) {
		return new ScepterRecipeBuilder(scepter, durability);
	}

	public ScepterRecipeBuilder addRepairIngredient(Ingredient item) {
		this.repairItems.add(item);
		return this;
	}

	public ScepterRecipeBuilder addRepairIngredient(ItemStack item) {
		this.repairItems.add(Ingredient.of(item));
		return this;
	}

	public ScepterRecipeBuilder addRepairIngredient(TagKey<Item> item) {
		this.repairItems.add(Ingredient.of(item));
		return this;
	}

	public ScepterRecipeBuilder addRepairIngredient(ItemLike item) {
		this.repairItems.add(Ingredient.of(item));
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		consumer.accept(new Result(id, this.scepter, List.copyOf(this.repairItems), this.durability, CraftingBookCategory.MISC));
	}

	private record Result(ResourceLocation id, Item scepter, List<Ingredient> repairItems, int durability, CraftingBookCategory category) implements FinishedRecipe {

		@Override
		public void serializeRecipeData(JsonObject json) {
			ScepterRepairRecipe.Serializer.toJson(json, this.scepter, this.repairItems, this.durability, this.category);
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return TFRecipes.SCEPTER_REPAIR_RECIPE.get();
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
