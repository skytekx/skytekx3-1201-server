package twilightforest.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import twilightforest.init.TFDamageTypes;

public class CustomDamageSwordItem extends SwordItem implements CustomDamageProvider {

	private final ResourceKey<DamageType> damageType;
	private final boolean showAttributeTooltip;

	public CustomDamageSwordItem(ResourceKey<DamageType> damageType, Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		this(damageType, tier, attackDamageModifier, attackSpeedModifier, true, properties);
	}

	/**
	 * @param showAttributeTooltip false hides the attack damage and speed lines, like 4.8's
	 *                             {@code ItemAttributeModifiers.withTooltip(false)}
	 */
	public CustomDamageSwordItem(ResourceKey<DamageType> damageType, Tier tier, int attackDamageModifier, float attackSpeedModifier, boolean showAttributeTooltip, Properties properties) {
		super(tier, attackDamageModifier, attackSpeedModifier, properties);
		this.damageType = damageType;
		this.showAttributeTooltip = showAttributeTooltip;
	}

	@Override
	public DamageSource getDamageSource(LivingEntity attacker) {
		return TFDamageTypes.getEntityDamageSource(attacker.level(), this.damageType, attacker);
	}

	@Override
	public int getDefaultTooltipHideFlags(ItemStack stack) {
		return this.showAttributeTooltip ? super.getDefaultTooltipHideFlags(stack) : ItemStack.TooltipPart.MODIFIERS.getMask();
	}

	// In 4.8 this item (stale bread) is in no #minecraft:swords or #minecraft:enchantable/* tag, so neither the
	// enchanting table nor the anvil offers it anything. On 1.20.1 every SwordItem matches the WEAPON, BREAKABLE
	// and VANISHABLE enchantment categories, so these three overrides mirror 4.8 by refusing all enchantments.
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}
}
