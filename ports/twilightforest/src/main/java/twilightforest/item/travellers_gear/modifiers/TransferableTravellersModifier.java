package twilightforest.item.travellers_gear.modifiers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * A modifier whose state can come from one of the recipe inputs, like the hotbar the Traveller's Belt carries over to
 * the Wings. Backport of 4.8's TransferableTravellersModifier.
 */
public interface TransferableTravellersModifier extends InsertableTravellersModifier {
	boolean transfer(ItemStack stack, List<Ingredient> input);
}
