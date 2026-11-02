package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFItems;
import twilightforest.item.recipe.EssenceRepairRecipe;

import java.util.Arrays;
import java.util.List;

/**
 * Shows the special Exanimate Essence repair recipe in JEI's crafting category: any empty scepter plus one essence gives
 * it back fully charged. Port of TF 4.8's ExanimateEssenceRepairExtension.
 */
public class ExanimateEssenceRepairExtension implements ICraftingCategoryExtension {

	private final EssenceRepairRecipe recipe;

	public ExanimateEssenceRepairExtension(EssenceRepairRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		List<ItemStack> scepters = Arrays.stream(Ingredient.of(ItemTagGenerator.SCEPTERS).getItems()).map(ItemStack::copy).toList();
		List<ItemStack> emptyScepters = scepters.stream().map(stack -> {
			ItemStack empty = stack.copy();
			empty.setDamageValue(empty.getMaxDamage());
			return empty;
		}).toList();

		List<IRecipeSlotBuilder> inputSlots = craftingGridHelper.createAndSetInputs(builder, List.of(emptyScepters, List.of(new ItemStack(TFItems.EXANIMATE_ESSENCE.get()))), 0, 0);
		builder.setShapeless();
		IRecipeSlotBuilder outputSlot = craftingGridHelper.createAndSetOutputs(builder, scepters);

		// 4.8 keeps the output scepter in step with the input through JEI 19's onDisplayedIngredientsUpdate; on JEI 15 a
		// focus link does the same: both slots cycle together and follow a focused scepter
		builder.createFocusLink(inputSlots.get(0), outputSlot);
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return this.recipe.getId();
	}
}
