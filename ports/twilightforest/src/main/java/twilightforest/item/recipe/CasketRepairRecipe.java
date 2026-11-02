package twilightforest.item.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;
import twilightforest.item.KeepsakeCasketItem;

/**
 * A damaged Keepsake Casket and a Charm of Keeping III craft into the casket with one level of damage less,
 * the same repair that using the charm on a placed casket does.
 */
public class CasketRepairRecipe extends CustomRecipe {

	public CasketRepairRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		boolean casket = false;
		boolean charm = false;

		for (int i = 0; i < input.getContainerSize(); ++i) {
			ItemStack stackInQuestion = input.getItem(i);
			if (!stackInQuestion.isEmpty()) {
				if (stackInQuestion.is(TFItems.KEEPSAKE_CASKET.get()) && KeepsakeCasketItem.getCasketDamage(stackInQuestion) > 0) {
					if (casket) return false;
					casket = true;
				} else if (stackInQuestion.is(TFItems.CHARM_OF_KEEPING_3.get())) {
					if (charm) return false;
					charm = true;
				} else {
					return false;
				}
			}
		}
		return casket && charm;
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess access) {
		ItemStack casket = null;
		for (int i = 0; i < input.getContainerSize(); ++i) {
			ItemStack itemstack = input.getItem(i);
			if (!itemstack.isEmpty()) {
				if (itemstack.is(TFItems.KEEPSAKE_CASKET.get())) {
					if (casket == null) {
						casket = itemstack;
					} else {
						//Only accept 1 casket
						return ItemStack.EMPTY;
					}
				}
			}
		}

		if (casket != null && KeepsakeCasketItem.getCasketDamage(casket) > 0) {
			ItemStack repaired = casket.copyWithCount(1);
			KeepsakeCasketItem.setCasketDamage(repaired, KeepsakeCasketItem.getCasketDamage(casket) - 1);
			return repaired;
		}

		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return TFRecipes.CASKET_REPAIR_RECIPE.get();
	}
}
