package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import twilightforest.item.recipe.ScepterRepairRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Shows a special scepter repair recipe in JEI's crafting category: an empty scepter plus one set of its repair items
 * gives the scepter back with one repair step of charges. Port of TF 4.8's ScepterRepairExtension.
 */
public class ScepterRepairExtension implements ICraftingCategoryExtension {

	private final ScepterRepairRecipe recipe;

	public ScepterRepairExtension(ScepterRepairRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		ItemStack scepter = new ItemStack(this.recipe.getScepter());
		scepter.setDamageValue(scepter.getMaxDamage());
		List<List<ItemStack>> inputs = new ArrayList<>();
		inputs.add(List.of(scepter));
		inputs.addAll(this.recipe.getRepairItems().stream().map(ingredient -> Arrays.stream(ingredient.getItems()).toList()).toList());

		craftingGridHelper.createAndSetInputs(builder, inputs, 0, 0);
		builder.setShapeless();

		ItemStack repairedScepter = new ItemStack(this.recipe.getScepter());
		repairedScepter.setDamageValue(scepter.getMaxDamage() - this.recipe.getRepairDurability());
		craftingGridHelper.createAndSetOutputs(builder, List.of(repairedScepter));
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return this.recipe.getId();
	}
}
