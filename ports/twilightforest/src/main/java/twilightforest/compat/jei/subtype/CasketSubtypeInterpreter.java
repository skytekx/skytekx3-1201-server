package twilightforest.compat.jei.subtype;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;
import twilightforest.item.KeepsakeCasketItem;

/**
 * Tells the damage levels of the Keepsake Casket apart, so JEI lists all three and the repair recipe can be looked up for each.
 */
public class CasketSubtypeInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {

	public static final CasketSubtypeInterpreter INSTANCE = new CasketSubtypeInterpreter();

	@Override
	public String apply(ItemStack stack, UidContext context) {
		return String.valueOf(KeepsakeCasketItem.getCasketDamage(stack));
	}
}
