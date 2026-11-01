package twilightforest.client;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import twilightforest.inventory.UncraftingPlaceRecipe;

import java.util.List;

/**
 * Recipe book for the Uncrafting Table. Ghost recipes have to land on the assembly matrix and the result slot,
 * not on the hidden uncrafting matrix that directly follows the result slot.
 */
public class UncraftingRecipeBookComponent extends RecipeBookComponent implements UncraftingPlaceRecipe<Ingredient> {

	@Override
	public void setupGhostRecipe(Recipe<?> recipe, List<Slot> slots) {
		ItemStack itemstack = recipe.getResultItem(this.minecraft.level.registryAccess());
		this.ghostRecipe.setRecipe(recipe);
		this.ghostRecipe.addIngredient(Ingredient.of(itemstack), slots.get(1).x, slots.get(1).y);
		this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, recipe.getIngredients().iterator(), 0);
	}
}
