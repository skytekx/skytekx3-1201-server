package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFItems;
import twilightforest.item.KeepsakeCasketItem;
import twilightforest.item.recipe.CasketRepairRecipe;

import java.util.List;

/**
 * Shows the casket repair recipe as a damaged casket and a Charm of Keeping III crafting into a less damaged casket.
 */
public class CasketRepairExtension implements ICraftingCategoryExtension {

	private final CasketRepairRecipe recipe;

	public CasketRepairExtension(CasketRepairRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		ItemStack casket = new ItemStack(TFItems.KEEPSAKE_CASKET.get());
		KeepsakeCasketItem.setCasketDamage(casket, 2);
		List<List<ItemStack>> inputs = List.of(List.of(casket), List.of(new ItemStack(TFItems.CHARM_OF_KEEPING_3.get())));

		craftingGridHelper.createAndSetInputs(builder, inputs, 0, 0);
		builder.setShapeless();

		ItemStack repairedCasket = new ItemStack(TFItems.KEEPSAKE_CASKET.get());
		KeepsakeCasketItem.setCasketDamage(repairedCasket, 1);
		craftingGridHelper.createAndSetOutputs(builder, List.of(repairedCasket));
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return this.recipe.getId();
	}
}
