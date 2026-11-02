package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.init.TFItems;

import java.util.List;

/**
 * Shows the special Moonworm Queen repair recipe in JEI's crafting category: a fully used queen plus one to four
 * Torchberries. All slots cycle in lockstep, so the result's durability always matches the number of berries shown.
 */
public class MoonwormQueenExtension implements ICraftingCategoryExtension {

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		List<List<ItemStack>> inputs = List.of(
			List.of(RecipeViewerConstants.DAMAGED_MOONWORM_QUEEN),
			List.of(new ItemStack(TFItems.TORCHBERRIES.get())),
			RecipeViewerConstants.BERRY_2_LIST,
			RecipeViewerConstants.BERRY_3_LIST,
			RecipeViewerConstants.BERRY_4_LIST);
		builder.setShapeless();

		craftingGridHelper.createAndSetInputs(builder, inputs, 0, 0);
		craftingGridHelper.createAndSetOutputs(builder, RecipeViewerConstants.MOONWORM_QUEEN_LIST)
			.addTooltipCallback((view, tooltip) -> tooltip.add(RecipeViewerConstants.MOONWORM_QUEEN_TOOLTIP));
	}
}
