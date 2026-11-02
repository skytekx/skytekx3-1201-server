package twilightforest.compat.emi.recipes;

import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import twilightforest.TwilightForestMod;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.compat.emi.widget.OrderedSlotWidget;
import twilightforest.init.TFItems;
import twilightforest.item.recipe.MoonwormQueenRepairRecipe;

import java.util.List;

/**
 * The special Moonworm Queen repair recipe: a fully used queen plus one to four Torchberries. Only listed while a data
 * pack provides the recipe (#2320).
 */
public class EmiMoonwormQueenRecipe extends TFEmiRecipe<MoonwormQueenRepairRecipe> {

	public EmiMoonwormQueenRecipe(MoonwormQueenRepairRecipe recipe) {
		super(VanillaEmiRecipeCategories.CRAFTING, recipe, TwilightForestMod.prefix("/moonworm_queen_repairing"), RecipeViewerConstants.GENERIC_RECIPE_WIDTH + 2, RecipeViewerConstants.GENERIC_RECIPE_HEIGHT);
	}

	@Override
	protected void addAdditionalInputs(List<EmiIngredient> inputs) {
		inputs.add(EmiStack.of(TFItems.MOONWORM_QUEEN.get()));
	}

	@Override
	protected void addAdditionalOutputs(List<EmiStack> outputs) {
		outputs.add(EmiStack.of(TFItems.MOONWORM_QUEEN.get()));
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 60, 18);
		widgets.addTexture(EmiTexture.SHAPELESS, 97, 0);
		widgets.addSlot(EmiStack.of(RecipeViewerConstants.DAMAGED_MOONWORM_QUEEN), 0, 0);
		widgets.addSlot(EmiStack.of(TFItems.TORCHBERRIES.get()), 18, 0);

		widgets.add(new OrderedSlotWidget(RecipeViewerConstants.BERRY_2_LIST.stream().map(EmiStack::of).toList(), 36, 0, 1000L));
		widgets.add(new OrderedSlotWidget(RecipeViewerConstants.BERRY_3_LIST.stream().map(EmiStack::of).toList(), 0, 18, 1000L));
		widgets.add(new OrderedSlotWidget(RecipeViewerConstants.BERRY_4_LIST.stream().map(EmiStack::of).toList(), 18, 18, 1000L));

		for (int i = 5; i < 9; ++i) {
			widgets.addSlot(EmiStack.EMPTY, i % 3 * 18, i / 3 * 18);
		}

		widgets.add(new OrderedSlotWidget(RecipeViewerConstants.MOONWORM_QUEEN_LIST.stream().map(EmiStack::of).toList(), 92, 14, 1000L)
			.appendTooltip(RecipeViewerConstants.MOONWORM_QUEEN_TOOLTIP).large(true).recipeContext(this));
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}
}
