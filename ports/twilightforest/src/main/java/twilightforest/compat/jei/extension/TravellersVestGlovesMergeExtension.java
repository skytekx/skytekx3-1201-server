package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.TFItems;

import java.util.List;

/**
 * Shows the special Traveller's Vest and Gloves merge recipe in JEI's crafting category. Backport of 4.8's
 * TravellersVestGlovesMergeExtension.
 */
public class TravellersVestGlovesMergeExtension implements ICraftingCategoryExtension {

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		craftingGridHelper.createAndSetInputs(builder, List.of(List.of(new ItemStack(TFItems.TRAVELLERS_VEST.get())), List.of(new ItemStack(TFItems.TRAVELLERS_GLOVES.get()))), 0, 0);
		builder.setShapeless();
		ItemStack vestWithGloves = new ItemStack(TFItems.TRAVELLERS_VEST.get());
		TravellersGearData.setGloves(vestWithGloves);
		craftingGridHelper.createAndSetOutputs(builder, List.of(vestWithGloves));
	}
}
