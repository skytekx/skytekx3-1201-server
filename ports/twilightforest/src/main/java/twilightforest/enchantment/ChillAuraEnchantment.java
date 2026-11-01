package twilightforest.enchantment;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import twilightforest.enums.TwilightArmorMaterial;
import twilightforest.init.TFEnchantments;
import twilightforest.init.TFMobEffects;

import java.util.Map;

public class ChillAuraEnchantment extends LootOnlyEnchantment {

	public ChillAuraEnchantment(Rarity rarity) {
		super(rarity, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
				EquipmentSlot.LEGS, EquipmentSlot.FEET});
	}

	@Override
	public boolean canEnchant(ItemStack stack) {
		// Chill Aura contradicts the burning effect of Fiery armor
		if (stack.getItem() instanceof ArmorItem armorItem) {
			return armorItem.getMaterial() != TwilightArmorMaterial.ARMOR_FIERY;
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
	public void doPostHurt(LivingEntity user, Entity attacker, int level) {
		if (attacker instanceof LivingEntity entity) {
			boolean shouldHit = this.shouldHit(level, user.getRandom());
			doChillAuraEffect(entity, 200, level - 1, shouldHit);
			if (shouldHit) {
				// like Thorns, every proc costs the enchanted armor 2 durability
				damageEnchantedItem(this, user);
			}
		}
	}

	/**
	 * [VanillaCopy] of the durability part of ThornsEnchantment.doPostHurt: damages a random piece of equipment
	 * carrying the given enchantment by 2.
	 */
	public static void damageEnchantedItem(Enchantment enchantment, LivingEntity user) {
		Map.Entry<EquipmentSlot, ItemStack> entry = EnchantmentHelper.getRandomItemWith(enchantment, user);
		if (entry != null) {
			entry.getValue().hurtAndBreak(2, user, living -> living.broadcastBreakEvent(entry.getKey()));
		}
	}

	public static void doChillAuraEffect(LivingEntity victim, int duration, int amplifier, boolean shouldHit) {
		if (shouldHit && !victim.getType().is(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)) {
			if (!victim.getItemBySlot(EquipmentSlot.HEAD).is(ItemTags.FREEZE_IMMUNE_WEARABLES) &&
					!victim.getItemBySlot(EquipmentSlot.CHEST).is(ItemTags.FREEZE_IMMUNE_WEARABLES) &&
					!victim.getItemBySlot(EquipmentSlot.LEGS).is(ItemTags.FREEZE_IMMUNE_WEARABLES) &&
					!victim.getItemBySlot(EquipmentSlot.FEET).is(ItemTags.FREEZE_IMMUNE_WEARABLES)) {
				if (!(victim instanceof Player player) || !player.isCreative()) {
					victim.addEffect(new MobEffectInstance(TFMobEffects.FROSTY.get(), duration, amplifier));
				}
			}
		}
	}

	private boolean shouldHit(int level, RandomSource pRnd) {
		if (level <= 0) {
			return false;
		} else {
			return pRnd.nextFloat() < 0.15F * (float) level;
		}
	}

	@Override
	protected boolean checkCompatibility(Enchantment other) {
		return super.checkCompatibility(other) && other != TFEnchantments.FIRE_REACT.get() && other != Enchantments.THORNS;
	}
}
