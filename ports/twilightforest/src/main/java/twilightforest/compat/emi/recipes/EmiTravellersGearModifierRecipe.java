package twilightforest.compat.emi.recipes;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import twilightforest.item.recipe.travellers.TravellersGearModifierRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Shows a Traveller's modifier recipe in EMI's crafting category; the output is the modified gear, computed from the
 * modifier definition. Backport of 4.8's EmiTravellersGearModifierRecipe. Unlike 4.8 it modifies a copy of the gear,
 * not the stack the input ingredient displays.
 */
public class EmiTravellersGearModifierRecipe extends EmiPatternCraftingRecipe {
	private final TravellersGearModifierRecipe recipe;
	private final int uniqueSeed = EmiUtil.RANDOM.nextInt();

	public EmiTravellersGearModifierRecipe(TravellersGearModifierRecipe recipe) {
		super(
			layoutInputs(recipe),
			// dummy output; actual result is generated dynamically
			EmiStack.EMPTY,
			recipe.getId(), recipe.isShapeless());
		this.recipe = recipe;
	}

	// 4.8 hands EMI the bare width x height list, which draws patterns narrower than 3 in the wrong slots; lay shaped
	// recipes out on the 3x3 grid like EMI's own shaped recipes
	private static List<EmiIngredient> layoutInputs(TravellersGearModifierRecipe recipe) {
		List<EmiIngredient> ingredients = recipe.getIngredients().stream().map(EmiIngredient::of).toList();
		if (recipe.isShapeless())
			return ingredients;
		List<EmiIngredient> grid = new ArrayList<>(Collections.nCopies(9, EmiStack.EMPTY));
		for (int y = 0; y < recipe.getHeight(); y++) {
			for (int x = 0; x < recipe.getWidth(); x++) {
				grid.set(x + y * 3, ingredients.get(x + y * recipe.getWidth()));
			}
		}
		return grid;
	}

	@Override
	public ResourceLocation getId() {
		return Objects.requireNonNull(super.getId()).withPrefix("/");
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot < this.input.size()) {
			return new SlotWidget(this.input.get(slot), x, y);
		}
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(
			rand -> EmiStack.of(this.recipe.applyModifier(Objects.requireNonNull(Minecraft.getInstance().level).registryAccess(), TravellersGearModifierRecipe.getModifiableArmorFromIngredients(this.recipe.getIngredients()).copy(), this.recipe.getIngredients())),
			this.uniqueSeed,
			x, y
		);
	}
}
