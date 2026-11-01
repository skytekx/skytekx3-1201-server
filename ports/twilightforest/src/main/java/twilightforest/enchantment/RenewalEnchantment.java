package twilightforest.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFEnchantments;

/**
 * Renewal: a scepter that runs out of charges recharges itself from the holder's inventory (see
 * {@link RechargeScepterEffect}). 4.8 defines it as a data-driven enchantment with a tick effect; on 1.20.1 it is a
 * classic loot-only enchantment, and the scepters run the effect from their inventoryTick.
 */
public class RenewalEnchantment extends LootOnlyEnchantment {

	public RenewalEnchantment(Rarity rarity) {
		super(rarity, TFEnchantments.SCEPTER, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
	}

	@Override
	public int getMinCost(int level) {
		return 5 + (level - 1) * 9;
	}

	@Override
	public int getMaxCost(int level) {
		return 20 + (level - 1) * 9;
	}

	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		return stack.is(ItemTagGenerator.SCEPTERS);
	}

	/**
	 * Scepters only take Renewal: 4.8 scepters have no enchantability, and Renewal is the only enchantment that lists them.
	 * On 1.20.1, Unbreaking and Mending would otherwise apply to any damageable item.
	 */
	public static boolean isRenewalBook(ItemStack book) {
		var enchantments = EnchantmentHelper.getEnchantments(book);
		return !enchantments.isEmpty() && enchantments.keySet().stream().allMatch(enchantment -> enchantment == TFEnchantments.RENEWAL.get());
	}
}
