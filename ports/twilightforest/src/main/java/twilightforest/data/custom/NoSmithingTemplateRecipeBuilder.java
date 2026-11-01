package twilightforest.data.custom;

import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFRecipes;

import java.util.function.Consumer;

public class NoSmithingTemplateRecipeBuilder {

	private final RecipeCategory category;
	private final Ingredient base;
	private final Ingredient addition;
	private CompoundTag additionalData = new CompoundTag();
	private final Advancement.Builder advancement = Advancement.Builder.recipeAdvancement();

	private NoSmithingTemplateRecipeBuilder(RecipeCategory category, Ingredient base, Ingredient addition) {
		this.category = category;
		this.base = base;
		this.addition = addition;
	}

	public static NoSmithingTemplateRecipeBuilder noTemplate(Ingredient base, Ingredient addition, RecipeCategory category) {
		return new NoSmithingTemplateRecipeBuilder(category, base, addition);
	}

	public NoSmithingTemplateRecipeBuilder unlocks(String key, CriterionTriggerInstance criterion) {
		this.advancement.addCriterion(key, criterion);
		return this;
	}

	public NoSmithingTemplateRecipeBuilder attachData(Consumer<CompoundTag> tag) {
		this.additionalData = Util.make(new CompoundTag(), tag);
		return this;
	}

	public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
		this.ensureValid(id);
		this.advancement.parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
				.addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
				.rewards(AdvancementRewards.Builder.recipe(id))
				.requirements(RequirementsStrategy.OR);
		consumer.accept(new Result(id, this.base, this.addition, this.additionalData, this.advancement, id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
	}

	private void ensureValid(ResourceLocation id) {
		if (this.advancement.getCriteria().isEmpty()) {
			throw new IllegalStateException("No way of obtaining recipe " + id);
		}
	}

	public record Result(ResourceLocation id, Ingredient base, Ingredient addition, CompoundTag additionalData, Advancement.Builder advancement, ResourceLocation advancementId) implements FinishedRecipe {

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("base", this.base().toJson());
			json.add("addition", this.addition().toJson());
			if (!this.additionalData().isEmpty()) {
				json.addProperty("additional_data", this.additionalData().toString());
			}
		}

		@Override
		public ResourceLocation getId() {
			return this.id();
		}

		@Override
		public RecipeSerializer<?> getType() {
			return TFRecipes.NO_TEMPLATE_SMITHING_SERIALIZER.get();
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			return this.advancement().serializeToJson();
		}

		@Nullable
		@Override
		public ResourceLocation getAdvancementId() {
			return this.advancementId();
		}
	}
}
