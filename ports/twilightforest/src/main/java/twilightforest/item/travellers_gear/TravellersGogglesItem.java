package twilightforest.item.travellers_gear;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import twilightforest.init.custom.TravellersModifiersManager;

import java.util.Set;

/**
 * The Traveller's Goggles. Backport of 4.8's TravellersGogglesItem: All-Night Goggles keep Endermen calm when looked at.
 * The built-in Zoom ability is an item default (TFItems), the Item Display storage comes with its own feature
 * (travellers-goggles-item-display).
 */
public class TravellersGogglesItem extends TravellersArmorItem {

	public TravellersGogglesItem(Properties properties) {
		super(Type.HELMET, properties, 3, 12, Set.of());
	}

	@Override
	public boolean isEnderMask(ItemStack stack, Player player, EnderMan enderman) {
		return TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ALL_NIGHT_GOGGLES_MODIFIER);
	}
}
