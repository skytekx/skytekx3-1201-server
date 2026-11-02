package twilightforest.compat.emi.recipes;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import twilightforest.TwilightForestMod;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.compat.emi.TFEmiCategories;
import twilightforest.compat.emi.widget.EmiItemEntityWidget;
import twilightforest.item.recipe.CrumbleRecipe;

import java.util.List;

// 4.8 reads the crumble_horn data map; here the crumbles are still 4.3's twilightforest:crumble_horn recipes
public class EmiCrumbleHornRecipe implements EmiRecipe {
	private static final int WIDTH = RecipeViewerConstants.GENERIC_RECIPE_WIDTH;
	//height is adjusted slightly to allow 2 entries per page
	private static final int HEIGHT = RecipeViewerConstants.GENERIC_RECIPE_HEIGHT - 8;

	private final ResourceLocation id;
	private final Block input;
	private final Block output;

	public EmiCrumbleHornRecipe(CrumbleRecipe recipe) {
		// keyed by the recipe rather than the input block like 4.8, so two recipes for different states of one block stay apart
		this.id = TwilightForestMod.prefix("/crumbling/" + recipe.getId().getNamespace() + "/" + recipe.getId().getPath());
		this.input = recipe.input().getBlock();
		this.output = recipe.result().getBlock();
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return TFEmiCategories.CRUMBLE_HORN;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return List.of(EmiStack.of(this.input));
	}

	@Override
	public List<EmiStack> getOutputs() {
		return List.of(EmiStack.of(this.output));
	}

	@Override
	public int getDisplayWidth() {
		return WIDTH;
	}

	@Override
	public int getDisplayHeight() {
		return HEIGHT;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 44, 15);
		if (this.output != Blocks.AIR) {
			widgets.addSlot(this.getInputs().get(0), 14, 14);
			widgets.addSlot(this.getOutputs().get(0), 76, 10).large(true).recipeContext(this);
		} else {
			widgets.addSlot(this.getInputs().get(0), 14, 14).recipeContext(this);
			widgets.add(new EmiItemEntityWidget(this.input, 76, 10));
		}
	}
}
