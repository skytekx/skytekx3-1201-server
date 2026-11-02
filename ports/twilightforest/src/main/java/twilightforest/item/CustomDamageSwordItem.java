package twilightforest.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
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
}
