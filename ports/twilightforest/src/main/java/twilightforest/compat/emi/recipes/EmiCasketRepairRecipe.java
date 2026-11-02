package twilightforest.compat.emi.recipes;

import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import twilightforest.init.TFItems;
import twilightforest.item.KeepsakeCasketItem;

import java.util.List;

/**
 * The special Keepsake Casket repair recipe: a damaged casket plus a Charm of Keeping III takes one step of damage off.
 * 4.8 only shows it in JEI (CasketRepairExtension); since the JEI plugin stays off while EMI is installed, EMI gets the
 * same display here.
 */
public class EmiCasketRepairRecipe extends EmiCraftingRecipe {

	public EmiCasketRepairRecipe(ResourceLocation id) {
		super(List.of(EmiStack.of(casket(2)), EmiStack.of(TFItems.CHARM_OF_KEEPING_3.get())), EmiStack.of(casket(1)), id, true);
	}

	private static ItemStack casket(int damage) {
		ItemStack casket = new ItemStack(TFItems.KEEPSAKE_CASKET.get());
		KeepsakeCasketItem.setCasketDamage(casket, damage);
		return casket;
	}
}
