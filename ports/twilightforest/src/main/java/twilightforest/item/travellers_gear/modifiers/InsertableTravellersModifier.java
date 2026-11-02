package twilightforest.item.travellers_gear.modifiers;

import net.minecraft.world.item.ItemStack;

/**
 * A modifier that recipes and /tf travellers_gear can add to a piece of gear, and the grindstone remove. Backport of
 * 4.8's InsertableTravellersModifier.
 */
public interface InsertableTravellersModifier extends TravellersModifier {
	boolean addModifier(ItemStack stack);

	void removeModifier(ItemStack stack);
}
