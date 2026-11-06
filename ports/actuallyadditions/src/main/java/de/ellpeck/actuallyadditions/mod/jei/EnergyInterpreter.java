package de.ellpeck.actuallyadditions.mod.jei;

import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.world.item.ItemStack;

/**
 * SkyTekx: the subtype interpreter of upstream c86a2905 and 3cf1815c (#1485) on the JEI 15 API, whose
 * interpreters return a string. It is the legacy string of main: a full energy item is "charged", any
 * other one "uncharged", so JEI lists an empty and a charged entry and a part charged stack looks up
 * the empty one. The charge comes from the NBT key Energy, which the client stack carries.
 */
public class EnergyInterpreter implements IIngredientSubtypeInterpreter<ItemStack> {
	public static final EnergyInterpreter INSTANCE = new EnergyInterpreter();

	private EnergyInterpreter() {

	}

	@Override
	public String apply(ItemStack ingredient, UidContext context) {
		if (ingredient.getItem() instanceof ItemEnergy item && ItemEnergy.getEnergyFromTag(ingredient) >= item.maxPower) {
			return "charged";
		}
		return "uncharged";
	}
}
