package twilightforest.item;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import twilightforest.entity.projectile.IceArrow;
import twilightforest.util.TwilightItemTier;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class IceBowItem extends BowItem {

	public IceBowItem(Properties props) {
		super(props);
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		AtomicBoolean badEnchant = new AtomicBoolean();
		EnchantmentHelper.getEnchantments(book).forEach((enchantment, integer) -> {
			if (Objects.equals(Enchantments.FLAMING_ARROWS, enchantment)) {
				badEnchant.set(true);
			}
		});

		return !badEnchant.get();
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return !Enchantments.FLAMING_ARROWS.equals(enchantment) && super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public AbstractArrow customArrow(AbstractArrow arrow) {
		return new IceArrow(arrow);
	}

	@Override
	public boolean isValidRepairItem(ItemStack toRepair, ItemStack repairWith) {
		// Same repair tag as the ice tools (twilightforest:repairs_ice_tools), as in 4.8 (upstream 2029c0e0a)
		return TwilightItemTier.ICE.getRepairIngredient().test(repairWith);
	}
}