package twilightforest.item.travellers_gear;

import java.util.Set;

/**
 * The Traveller's Goggles. Backport of 4.8's TravellersGogglesItem; the Zoom ability, the All-Night Goggles ender mask
 * and the Item Display storage are added by their own features (travellers-goggles-modifiers,
 * travellers-goggles-item-display).
 */
public class TravellersGogglesItem extends TravellersArmorItem {

	public TravellersGogglesItem(Properties properties) {
		super(Type.HELMET, properties, 3, 12, Set.of());
	}
}
