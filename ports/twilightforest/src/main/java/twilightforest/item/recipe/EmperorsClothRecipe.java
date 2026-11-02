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
import twilightforest.util.ArmorUtil;

public class EmperorsClothRecipe extends CustomRecipe {

	public EmperorsClothRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		boolean foundCloth = false;
		boolean foundItem = false;

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.is(TFItems.EMPERORS_CLOTH.get()) && !foundCloth) {
					foundCloth = true;
				} else if (!foundItem) {
					if (stack.is(ItemTagGenerator.EMPERORS_CLOTH_APPLICABLE) && !stack.hasCraftingRemainingItem() && !ArmorUtil.hasEmperorsCloth(stack)) {
						foundItem = true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			}
		}

		return foundCloth && foundItem;
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		ItemStack item = ItemStack.EMPTY;

		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty() && stack.is(ItemTagGenerator.EMPERORS_CLOTH_APPLICABLE) && item.isEmpty()) {
				item = stack;
			}
		}

		ItemStack copy = item.copy();
		ArmorUtil.applyEmperorsCloth(copy);
		return copy;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return TFRecipes.EMPERORS_CLOTH_RECIPE.get();
	}
}
