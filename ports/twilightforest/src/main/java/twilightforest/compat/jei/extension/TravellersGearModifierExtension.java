package twilightforest.compat.jei.extension;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;
import twilightforest.item.recipe.travellers.TravellersGearModifierRecipe;
import twilightforest.item.travellers_gear.TravellersArmorItem;
import twilightforest.item.travellers_gear.modifiers.TravellersModifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Shows a Traveller's modifier recipe in JEI's crafting category, with the modified gear as output. Backport of 4.8's
 * TravellersGearModifierExtension. JEI 15.2 has no setOutputSlotBackground; the render-only output slot sits at 4.8's
 * position over the output the crafting category already draws.
 */
public class TravellersGearModifierExtension implements ICraftingCategoryExtension {

	private final TravellersGearModifierRecipe recipe;

	public TravellersGearModifierExtension(TravellersGearModifierRecipe recipe) {
		this.recipe = recipe;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		List<List<ItemStack>> inputs = new ArrayList<>(this.recipe.getIngredients().stream().map(ingredient -> Arrays.stream(ingredient.getItems()).toList()).toList());
		List<ItemStack> outputs = new ArrayList<>();
		for (Ingredient ingredient : this.recipe.getIngredients()) {
			for (ItemStack stack : ingredient.getItems()) {
				if (TravellersArmorItem.isTravellersGear(stack) && stack.getItem() instanceof TravellersModifiable modifiable && modifiable.getModifierSlots() > 0) {
					outputs.add(this.recipe.applyModifier(Objects.requireNonNull(Minecraft.getInstance().level).registryAccess(), stack.copy(), this.recipe.getIngredients()));
				}
			}
		}

		outputs.removeIf(ItemStack::isEmpty);
		if (outputs.isEmpty())
			return;

		if (this.recipe.isShapeless()) builder.setShapeless();
		craftingGridHelper.createAndSetInputs(builder, inputs, this.recipe.getWidth(), this.recipe.getHeight());
		// output slot; use RENDER_ONLY to prevent displaying modifier recipes when using the "Show Recipe" key
		builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 95, 19).addItemStacks(outputs);
	}

	@Override
	public int getWidth() {
		return this.recipe.getWidth();
	}

	@Override
	public int getHeight() {
		return this.recipe.getHeight();
	}

	@Nullable
	@Override
	public ResourceLocation getRegistryName() {
		return this.recipe.getId();
	}
}
