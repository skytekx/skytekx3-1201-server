package twilightforest.item.travellers_gear;

import java.util.Set;

/**
 * The Traveller's Belt and the Traveller's Wings, the two leg pieces. Backport of 4.8's TravellersArmorBeltItem; the
 * Swap Hotbar storage it carries in 4.8 is added by its own feature (travellers-belt-swap-hotbar).
 */
public class TravellersArmorBeltItem extends TravellersArmorItem {

	public TravellersArmorBeltItem(Type type, Properties properties, int insertableModifierSlots, int durability, Set<Part> parts) {
		super(type, properties, insertableModifierSlots, durability, parts);
	}
}
