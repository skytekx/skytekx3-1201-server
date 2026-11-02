package twilightforest.components.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Stack NBT of the Traveller's Gear. TF 4.8 keeps this state in data components; 1.20.1 has none, so each component
 * becomes a stack NBT key named after the 4.8 component id. Keep every read and write of these keys in this class, so
 * the Traveller's Gear features ported later (the modifier framework and the per-piece modifiers) add their keys here
 * and the names stay stable for existing items.
 * <p>
 * The parts a piece is made of ({@code has_travellers_chestplate}, {@code has_travellers_wings} and so on) are fixed per
 * item in 4.8 too, so they are item properties ({@link twilightforest.item.travellers_gear.TravellersArmorItem.Part})
 * rather than NBT. Only the gloves can be added to a stack: the vest gains them through the vest and gloves merge recipe.
 */
public final class TravellersGearData {

	/**
	 * 4.8 {@code twilightforest:has_travellers_gloves}: set on a Traveller's Vest that has been merged with a pair of
	 * Traveller's Gloves. The gloves item has the part built in.
	 */
	public static final String HAS_GLOVES = "twilightforest:has_travellers_gloves";

	private TravellersGearData() {
	}

	public static boolean hasGlovesTag(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null && tag.getBoolean(HAS_GLOVES);
	}

	public static void setGloves(ItemStack stack) {
		stack.getOrCreateTag().putBoolean(HAS_GLOVES, true);
	}
}
