package twilightforest.enchantment;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import twilightforest.enums.TwilightArmorMaterial;
import twilightforest.init.TFEnchantments;

import javax.annotation.Nullable;

public class FireReactEnchantment extends LootOnlyEnchantment {

	public FireReactEnchantment(Rarity rarity) {
		super(rarity, EnchantmentCategory.ARMOR, new EquipmentSlot[]{
				EquipmentSlot.HEAD, EquipmentSlot.CHEST,
				EquipmentSlot.LEGS, EquipmentSlot.FEET
		});
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		// Fire React contradicts the freezing effect of Yeti armor
		if (stack.getItem() instanceof ArmorItem armorItem) {
			return armorItem.getMaterial() != TwilightArmorMaterial.ARMOR_YETI;
		}
		return super.canEnchant(stack);
	}

	@Override
	public int getMinCost(int level) {
		return 5 + (level - 1) * 9;
	}

	@Override
	public int getMaxCost(int level) {
		return this.getMinCost(level) + 15;
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public void doPostHurt(LivingEntity user, @Nullable Entity attacker, int level) {
		RandomSource random = user.getRandom();
		// 4.8 gates both the ignite and the durability cost only on the 0.15 x level chance,
		// so the armor still pays for a proc against a fire-immune attacker
		if (attacker != null && level > 0 && random.nextFloat() < 0.15F * (float) level) {
			if (!attacker.fireImmune()) {
				attacker.setSecondsOnFire(2 + (random.nextInt(level) * 3));
			}
			// like Thorns, every proc costs the enchanted armor 2 durability
			ChillAuraEnchantment.damageEnchantedItem(this, user);
		}
	}

	public static boolean shouldHit(int level, RandomSource random, Entity attacker) {
		if (level <= 0 || attacker.fireImmune()) {
			return false;
		} else {
			return random.nextFloat() < 0.15F * (float)level;
		}
	}

	@Override
	protected boolean checkCompatibility(Enchantment other) {
		return super.checkCompatibility(other) && other != TFEnchantments.CHILL_AURA.get() && other != Enchantments.THORNS;
	}
}
