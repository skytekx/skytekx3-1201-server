package twilightforest.data.custom;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFRecipes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class DryingRecipeBuilder implements RecipeBuilder {
	private final Ingredient input;
	private final ItemStack result;
	private final int time;
	private final Map<String, CriterionTriggerInstance> criteria = new LinkedHashMap<>();

	private DryingRecipeBuilder(Ingredient input, ItemStack result, int dryingTime) {
		this.input = input;
		this.result = result;
		this.time = dryingTime;
	}

	public static DryingRecipeBuilder drying(ItemLike input, ItemLike result) {
		return drying(Ingredient.of(input), new ItemStack(result));
	}

	public static DryingRecipeBuilder drying(ItemLike input, ItemLike result, float dryingMinutes) {
		return drying(Ingredient.of(input), new ItemStack(result), dryingMinutes);
	}

	public static DryingRecipeBuilder drying(Ingredient input, ItemStack result) {
		return drying(input, result, 5);
	}

	public static DryingRecipeBuilder drying(Ingredient input, ItemStack result, float dryingMinutes) {
		return new DryingRecipeBuilder(input, result, (int) (20 * 60 * dryingMinutes));
	}

	@Override
	public DryingRecipeBuilder unlockedBy(String key, CriterionTriggerInstance criterion) {
		this.criteria.put(key, criterion);
		return this;
	}

	@Override
	public RecipeBuilder group(@Nullable String groupName) {
		return this;
	}

	@Override
	public Item getResult() {
		return this.result.getItem();
	}

	@Override
	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		this.ensureValid(id);
		// 4.8 adds the recipe_unlocked criterion first, so it leads the requirements list
		Advancement.Builder builder = Advancement.Builder.recipeAdvancement()
			.parent(ROOT_RECIPE_ADVANCEMENT)
			.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
			.rewards(AdvancementRewards.Builder.recipe(id))
			.requirements(RequirementsStrategy.OR);
		this.criteria.forEach(builder::addCriterion);
		consumer.accept(new Result(id, this.input, this.result, this.time, builder, id.withPrefix("recipes/drying/")));
	}

	private void ensureValid(ResourceLocation location) {
		if (this.criteria.isEmpty() && !location.getPath().equals("stale_bread")) {
			throw new IllegalStateException("No way of obtaining recipe " + location);
		}
	}

	public record Result(ResourceLocation id, Ingredient input, ItemStack result, int time, Advancement.Builder advancement, ResourceLocation advancementId) implements FinishedRecipe {

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("input", this.input.toJson());

			JsonObject resultJson = new JsonObject();
			resultJson.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(this.result.getItem())).toString());
			if (this.result.getCount() > 1) {
				resultJson.addProperty("count", this.result.getCount());
			}
			json.add("result", resultJson);

			json.addProperty("filter_time", this.time);
		}

		@Override
		public ResourceLocation getId() {
			return this.id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return TFRecipes.DRYING_SERIALIZER.get();
		}

		@Override
		public JsonObject serializeAdvancement() {
			return this.advancement.serializeToJson();
		}

		@Override
		public ResourceLocation getAdvancementId() {
			return this.advancementId;
		}
	}
}
