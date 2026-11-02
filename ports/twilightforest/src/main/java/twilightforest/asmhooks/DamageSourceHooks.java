package twilightforest.asmhooks;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
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
	 * <p>
	 * {@code mobAttack(null)} is legal vanilla (the source simply has no attacker), and this hook runs for every
	 * mob and player melee hit in the pack, so a null attacker must fall through to the vanilla source.
	 */
	public static DamageSource getCustomDamageSource(DamageSource o, @Nullable LivingEntity entity) {
		return entity != null && entity.getMainHandItem().getItem() instanceof CustomDamageProvider customDamageType ? customDamageType.getDamageSource(entity) : o;
	}
}
