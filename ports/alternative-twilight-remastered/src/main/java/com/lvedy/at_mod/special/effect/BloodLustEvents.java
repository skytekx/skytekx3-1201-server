package com.lvedy.at_mod.special.effect;

import com.lvedy.at_mod.AlternativeTwilight;
import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = AlternativeTwilight.MODID)
public class BloodLustEvents {

    public static final ResourceKey<DamageType> BLOOD_CURSE_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, AlternativeTwilight.prefix("blood_curse"));

    // 1.20.1 backport: the expiry damage is applied at the start of the entity's next tick instead of inside
    // MobEffectEvent.Expired. The event fires while LivingEntity#tickEffects iterates the active effects; hurting
    // the entity there (totem of undying, mods that add effects on hurt) changes that map mid-iteration, the
    // vanilla code swallows the ConcurrentModificationException and the curse stays, expiring (and hitting) again.
    // Only the server thread touches this map.
    private static final Map<LivingEntity, Float> PENDING_CURSE_DAMAGE = new WeakHashMap<>();

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;
        if (instance.getEffect() != ModEffects.BLOOD_LUST_CURSE.get()) return;

        int level = instance.getAmplifier() + 1;
        double maxHealth = entity.getAttributeValue(Attributes.MAX_HEALTH);
        float damage = (float) (maxHealth * Config.BLOOD_LUST_CURSE_DAMAGE_PER_LEVEL.get() * level);
        if (damage <= 0F) return;

        PENDING_CURSE_DAMAGE.merge(entity, damage, Float::sum);
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || PENDING_CURSE_DAMAGE.isEmpty()) return;

        Float damage = PENDING_CURSE_DAMAGE.remove(entity);
        if (damage == null) return;

        DamageSource source = new DamageSource(
                entity.level().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(BLOOD_CURSE_DAMAGE));
        entity.hurt(source, damage);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;

        LivingEntity killer = victim.getKillCredit();
        if (killer == null || killer == victim) return;

        MobEffectInstance curse = killer.getEffect(ModEffects.BLOOD_LUST_CURSE.get());
        if (curse == null) return;

        if (!wasTargetingKiller(victim, killer)) return;

        int curseLevel = curse.getAmplifier();
        int curseDuration = curse.getDuration();

        killer.removeEffect(ModEffects.BLOOD_LUST_CURSE.get());
        if (curseLevel > 0) {
            killer.addEffect(new MobEffectInstance(
                    ModEffects.BLOOD_LUST_CURSE.get(),
                    curseDuration,
                    curseLevel - 1,
                    curse.isAmbient(),
                    curse.isVisible(),
                    curse.showIcon()));
        }

        MobEffectInstance lust = killer.getEffect(ModEffects.BLOOD_LUST.get());
        int newLustLevel = (lust == null) ? 0 : lust.getAmplifier() + 1;
        killer.addEffect(new MobEffectInstance(
                ModEffects.BLOOD_LUST.get(),
                curseDuration,
                newLustLevel,
                false,
                true,
                true));
    }

    /**
     * 是否 victim 把 killer 当作攻击目标。
     * <p>
     * - {@link Mob}：用 {@link Targeting#getTarget()}（AI 实时目标），最精确。
     * - 非 Mob（玩家等）：用 {@code killer.getLastHurtByMob() == victim}，
     *   表示 killer 此前被 victim 主动伤害过——这才证明 victim 攻击过 killer。
     * <p>
     * 注意：不能用 {@code victim.getLastHurtByMob() == killer} 作兜底，
     * 因为这只表示 victim 被 killer 打过（任何被反击致死的中立生物都成立）。
     */
    private static boolean wasTargetingKiller(LivingEntity victim, LivingEntity killer) {
        if (victim instanceof Targeting targeting) {
            return targeting.getTarget() == killer;
        }
        return killer.getLastHurtByMob() == victim;
    }
}
