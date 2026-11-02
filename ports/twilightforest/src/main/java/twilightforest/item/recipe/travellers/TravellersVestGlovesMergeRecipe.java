package twilightforest.item.recipe.travellers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.TFItems;
import twilightforest.init.TFRecipes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shapeless Traveller's Vest + Traveller's Gloves: the vest keeps everything it had and wears the gloves from then on,
 * so a single chest slot shows both. Backport of 4.8's TravellersVestGlovesMergeRecipe.
 */
public class TravellersVestGlovesMergeRecipe extends CustomRecipe {

	public TravellersVestGlovesMergeRecipe(ResourceLocation id, CraftingBookCategory category) {
		super(id, category);
	}

	@Override
	public boolean matches(CraftingContainer container, Level level) {
		return this.resolve(container).filter(pair -> !TravellersGearData.hasGlovesTag(pair.vest())).isPresent();
	}

	@Override
	public ItemStack assemble(CraftingContainer container, RegistryAccess access) {
		return this.resolve(container).map(pair -> {
			ItemStack vest = pair.vest().copyWithCount(1);
			TravellersGearData.setGloves(vest);
			return vest;
		}).orElse(ItemStack.EMPTY);
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return TFRecipes.TRAVELLERS_VEST_GLOVES_MERGE_RECIPE.get();
	}

	private Optional<InputPair> resolve(CraftingContainer container) {
		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < container.getContainerSize(); i++) {
			ItemStack stack = container.getItem(i);
			if (!stack.isEmpty()) items.add(stack);
		}
		if (items.size() != 2) return Optional.empty();

		Optional<ItemStack> vest = items.stream().filter(s -> s.is(TFItems.TRAVELLERS_VEST.get())).findFirst();
		Optional<ItemStack> gloves = items.stream().filter(s -> s.is(TFItems.TRAVELLERS_GLOVES.get())).findFirst();

		return vest.flatMap(v -> gloves.map(g -> new InputPair(v, g)));
	}

	private record InputPair(ItemStack vest, ItemStack gloves) {}
}
