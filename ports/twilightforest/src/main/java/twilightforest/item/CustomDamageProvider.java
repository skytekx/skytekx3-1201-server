package twilightforest.item;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Items whose melee hits use their own damage source instead of the vanilla mob or player attack source.
 * Picked up by {@link twilightforest.asmhooks.DamageSourceHooks} for the item in the attacker's main hand.
 */
public interface CustomDamageProvider {
	DamageSource getDamageSource(LivingEntity attacker);
}
