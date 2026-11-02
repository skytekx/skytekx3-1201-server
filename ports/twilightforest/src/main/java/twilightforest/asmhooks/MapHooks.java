package twilightforest.asmhooks;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;

/**
 * Hooks called from {@code META-INF/asm/goggles_maps.js}. Backport of the Item Display part of 4.8's MapHooks; they
 * only take common classes, so loading this class never pulls client code onto a dedicated server.
 */
@SuppressWarnings("unused")
public final class MapHooks {

	private MapHooks() {
	}

	/**
	 * Item Display: the map the goggles of the inventory's player show counts as being in that inventory, so the map
	 * keeps the player's marker and its holder entry. 4.8 UpdateMapsInGogglesTransformer.
	 * <p>
	 * Injection Point:<br/>
	 * {@code net.minecraft.world.level.saveddata.maps.MapItemSavedData#tickCarriedBy(Player, ItemStack)}<br/>
	 * Targets: every {@code Inventory#contains(ItemStack)} (1.21: {@code Inventory#contains(Predicate)}); {@code o} is
	 * its result.
	 */
	public static boolean updateMapsInGoggles(Inventory inventory, ItemStack stack, boolean o) {
		if (o) return true;
		Player player = inventory.player;
		ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
		// the item check first: the hook runs for every carried map that is not in the inventory
		if (!TravellersArmorItem.isTravellersGear(headStack) || !TravellersModifiersManager.isModifierActive(player, headStack, TravellersModifiersManager.ITEM_DISPLAY_MODIFIER)) return false;
		ItemDisplayContents contents = TravellersGearData.ITEM_DISPLAY.get(headStack);
		if (contents == null || contents.isEmpty()) return false;
		int activeMapSlot = contents.findActiveMapSlot();
		if (activeMapSlot < 0) return false;

		ItemStack map = contents.getItem(activeMapSlot);
		// 1.20.1's Inventory#contains(ItemStack) compares item and tag; 4.8 compares item and components here
		return !map.isEmpty() && ItemStack.isSameItemSameTags(stack, map);
	}
}
