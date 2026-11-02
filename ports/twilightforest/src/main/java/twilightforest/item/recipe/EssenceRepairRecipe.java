package twilightforest.item.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;

/**
 * One damaged scepter (#twilightforest:scepters) and one Exanimate Essence give the scepter back fully charged.
 */
public class EssenceRepairRecipe extends CustomRecipe {

	public EssenceRepairRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		boolean scepter = false;
		boolean essence = false;

		for (int i = 0; i < input.getContainerSize(); ++i) {
			ItemStack stackInQuestion = input.getItem(i);
			if (!stackInQuestion.isEmpty()) {
				if (stackInQuestion.is(ItemTagGenerator.SCEPTERS) && stackInQuestion.isDamaged()) {
					if (scepter) return false;
					scepter = true;
				} else if (stackInQuestion.is(TFItems.EXANIMATE_ESSENCE.get())) {
					if (essence) return false;
					essence = true;
				} else {
					return false;
				}
			}
		}
		return scepter && essence;
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess access) {
		ItemStack scepter = null;
		for (int i = 0; i < input.getContainerSize(); ++i) {
			ItemStack itemstack = input.getItem(i);
			if (!itemstack.isEmpty()) {
				if (itemstack.is(ItemTagGenerator.SCEPTERS)) {
					if (scepter == null) {
						scepter = itemstack;
					} else {
						//Only accept 1 scepter
						return ItemStack.EMPTY;
					}
				}
			}
		}

		if (scepter != null && scepter.isDamaged()) {
			ItemStack repaired = scepter.copyWithCount(1);
			repaired.setDamageValue(0);
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
		return TFRecipes.ESSENCE_REPAIR_RECIPE.get();
	}
}
