package twilightforest.asmhooks;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import twilightforest.item.CustomDamageProvider;

/**
 * Hooks called from {@code META-INF/asm/damagesources.js} (4.8's tf-asm DamageSourcesTransformer).
 */
@SuppressWarnings("unused")
public final class DamageSourceHooks {

	private DamageSourceHooks() {
	}

	/**
	 * Injection Points (before every return):<br/>
	 * {@code net.minecraft.world.damagesource.DamageSources#mobAttack(LivingEntity)}<br/>
	 * {@code net.minecraft.world.damagesource.DamageSources#playerAttack(Player)}
	 * <p>
	 * 1.20.1 has no {@code LivingEntity#getWeaponItem}; 1.21 defaults it to the main hand item.
	 */
	public static DamageSource getCustomDamageSource(DamageSource o, LivingEntity entity) {
		return entity.getMainHandItem().getItem() instanceof CustomDamageProvider customDamageType ? customDamageType.getDamageSource(entity) : o;
	}
}
