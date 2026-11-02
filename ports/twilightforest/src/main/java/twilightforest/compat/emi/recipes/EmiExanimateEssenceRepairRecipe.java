package twilightforest.compat.emi.recipes;

import dev.emi.emi.api.recipe.EmiPatternCraftingRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.GeneratedSlotWidget;
import dev.emi.emi.api.widget.SlotWidget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFItems;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * The special Exanimate Essence repair recipe: any empty scepter plus one essence gives it back fully charged. 4.8 only
 * shows it in JEI (ExanimateEssenceRepairExtension); since the JEI plugin stays off while EMI is installed, EMI gets the
 * same display here. Input and output slots share their random seed, so they always show the same scepter.
 */
public class EmiExanimateEssenceRepairRecipe extends EmiPatternCraftingRecipe {

	private final List<ItemStack> scepters;

	public EmiExanimateEssenceRepairRecipe(ResourceLocation id) {
		this(Arrays.stream(Ingredient.of(ItemTagGenerator.SCEPTERS).getItems()).map(ItemStack::copy).toList(), id);
	}

	private EmiExanimateEssenceRepairRecipe(List<ItemStack> scepters, ResourceLocation id) {
		super(List.of(EmiIngredient.of(scepters.stream().map(EmiExanimateEssenceRepairRecipe::empty).map(EmiStack::of).toList()), EmiStack.of(TFItems.EXANIMATE_ESSENCE.get())), EmiStack.EMPTY, id, true);
		this.scepters = scepters;
	}

	private static ItemStack empty(ItemStack scepter) {
		ItemStack empty = scepter.copy();
		empty.setDamageValue(empty.getMaxDamage());
		return empty;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return this.scepters.stream().map(EmiStack::of).toList();
	}

	@Override
	public SlotWidget getInputWidget(int slot, int x, int y) {
		if (slot == 0) {
			return new GeneratedSlotWidget(random -> EmiStack.of(empty(this.pick(random))), this.unique, x, y);
		}
		if (slot == 1) {
			return new SlotWidget(EmiStack.of(TFItems.EXANIMATE_ESSENCE.get()), x, y);
		}
		return new SlotWidget(EmiStack.EMPTY, x, y);
	}

	@Override
	public SlotWidget getOutputWidget(int x, int y) {
		return new GeneratedSlotWidget(random -> EmiStack.of(this.pick(random)), this.unique, x, y);
	}

	private ItemStack pick(Random random) {
		return this.scepters.isEmpty() ? ItemStack.EMPTY : this.scepters.get(random.nextInt(this.scepters.size())).copy();
	}
}
