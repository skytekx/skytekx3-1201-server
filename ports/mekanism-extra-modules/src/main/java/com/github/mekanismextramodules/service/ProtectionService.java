package com.github.mekanismextramodules.service;

import com.github.mekanismextramodules.MekanismExtraModules;
import com.github.mekanismextramodules.config.ExtraModulesConfig;
import com.github.mekanismextramodules.data.ExtraModulesPlayerData;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ProtectionService {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        updatePhaseGuard(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isPhaseGuardActive(player)) {
            event.setInvulnerable(true);
            if (!event.getOriginalInvulnerability()) {
                if (ExtraModulesConfig.PHASE_SET_INVULNERABLE_TIME.get()) {
                    player.invulnerableTime = Math.max(player.invulnerableTime, ExtraModulesConfig.PHASE_INVULNERABILITY_TICKS.get());
                }
                logProtection("Blocked damage from {}", event.getSource().getMsgId());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isPhaseGuardActive(player)) {
            event.setNewDamage(0.0F);
            stabilizePhaseGuard(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (isPhaseGuardActive(player)) {
            event.setCanceled(true);
            stabilizePhaseGuard(player);
            logProtection("Canceled death from {}", event.getSource().getMsgId());
            return;
        }
        if (tryEmergencyRevival(player, event.getSource())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        ExtraModulesPlayerData.copyForRespawn(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ExtraModulesPlayerData.setPhaseActive(event.getEntity(), false);
    }

    public static boolean canUsePhaseGuard(Player player) {
        if (!ExtraModulesConfig.PHASE_ENABLED.get()) {
            return false;
        }
        if (!SuitModuleService.hasPhaseGuardEnabled(player)) {
            return false;
        }
        if (!isDimensionAllowed(player)) {
            return false;
        }
        return !ExtraModulesConfig.REQUIRE_FULL_MEKASUIT.get() || SuitModuleService.hasFullMekaSuit(player);
    }

    public static long currentPhaseGuardCost() {
        return ExtraModulesConfig.PHASE_ENERGY_COST_PER_TICK.get();
    }

    public static boolean isPhaseGuardActive(ServerPlayer player) {
        return ExtraModulesPlayerData.isPhaseActive(player) && canUsePhaseGuard(player);
    }

    public static boolean canUseChaosAnchor(Player player) {
        if (!ExtraModulesConfig.CHAOS_ENABLED.get()
                || !ExtraModulesConfig.CHAOS_ALLOW_MIXIN_PROTECTION.get()
                || !ModList.get().isLoaded("draconicevolution")
                || !SuitModuleService.hasChaosAnchorEnabled(player)) {
            return false;
        }
        if (!isDimensionAllowed(player)) {
            return false;
        }
        return !ExtraModulesConfig.REQUIRE_FULL_MEKASUIT.get() || SuitModuleService.hasFullMekaSuit(player);
    }

    public static boolean isChaosAnchorActive(ServerPlayer player) {
        return canUseChaosAnchor(player) && isPhaseGuardActive(player);
    }

    public static boolean canUseEmergencyRevival(Player player) {
        if (!ExtraModulesConfig.REVIVAL_ENABLED.get() || !SuitModuleService.hasEmergencyRevivalEnabled(player)) {
            return false;
        }
        if (!isDimensionAllowed(player)) {
            return false;
        }
        return !ExtraModulesConfig.REQUIRE_FULL_MEKASUIT.get() || SuitModuleService.hasFullMekaSuit(player);
    }

    public static boolean tryEmergencyRevival(ServerPlayer player, DamageSource source) {
        if (!canUseEmergencyRevival(player)) {
            return false;
        }
        if (!EnergyService.tryConsumeEnergy(player, ExtraModulesConfig.REVIVAL_ENERGY_COST.get(), EnergyContext.EMERGENCY_REVIVAL)) {
            return false;
        }
        float restoredHealth = (float) Math.max(
                ExtraModulesConfig.REVIVAL_HEALTH.get(),
                player.getMaxHealth() * ExtraModulesConfig.REVIVAL_HEALTH_PERCENTAGE.get()
        );
        player.setHealth(Math.min(player.getMaxHealth(), restoredHealth));
        stabilizeRevival(player);
        if (ExtraModulesConfig.REVIVAL_CURE_HARMFUL_EFFECTS.get()) {
            player.getActiveEffects().stream()
                    .filter(effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                    .map(MobEffectInstance::getEffect)
                    .toList()
                    .forEach(player::removeEffect);
        }
        if (ExtraModulesConfig.REVIVAL_APPLY_TOTEM_EFFECTS.get()) {
            addEffect(player, MobEffects.REGENERATION, ExtraModulesConfig.REVIVAL_REGENERATION_DURATION.get(), ExtraModulesConfig.REVIVAL_REGENERATION_AMPLIFIER.get());
            addEffect(player, MobEffects.ABSORPTION, ExtraModulesConfig.REVIVAL_ABSORPTION_DURATION.get(), ExtraModulesConfig.REVIVAL_ABSORPTION_AMPLIFIER.get());
            addEffect(player, MobEffects.FIRE_RESISTANCE, ExtraModulesConfig.REVIVAL_FIRE_RESISTANCE_DURATION.get(), ExtraModulesConfig.REVIVAL_FIRE_RESISTANCE_AMPLIFIER.get());
        }
        if (ExtraModulesConfig.REVIVAL_PLAY_TOTEM_ANIMATION.get()) {
            player.level().broadcastEntityEvent(player, (byte) 35);
        }
        if (ExtraModulesConfig.REVIVAL_SHOW_MESSAGE.get()) {
            player.displayClientMessage(Component.translatable("message.mekanism_extra_modules.emergency_revival.triggered"), true);
        }
        logProtection("Emergency Revival triggered for {} from {}", player.getGameProfile().getName(), source.getMsgId());
        return true;
    }

    public static void stabilizePhaseGuard(Player player) {
        float minHealth = ExtraModulesConfig.PHASE_MIN_HEALTH.get().floatValue();
        if (player.getHealth() < minHealth) {
            player.setHealth(minHealth);
        }
        if (ExtraModulesConfig.PHASE_CLEAR_FIRE.get()) {
            player.clearFire();
        }
        if (ExtraModulesConfig.PHASE_RESET_FALL_DISTANCE.get()) {
            player.fallDistance = 0.0F;
        }
    }

    private static void stabilizeRevival(Player player) {
        if (ExtraModulesConfig.REVIVAL_CLEAR_FIRE.get()) {
            player.clearFire();
        }
        if (ExtraModulesConfig.REVIVAL_RESET_FALL_DISTANCE.get()) {
            player.fallDistance = 0.0F;
        }
        player.invulnerableTime = Math.max(player.invulnerableTime, ExtraModulesConfig.REVIVAL_INVULNERABILITY_TICKS.get());
    }

    private static void addEffect(ServerPlayer player, Holder<MobEffect> effect, int duration, int amplifier) {
        if (duration > 0) {
            player.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }

    public static void updatePhaseGuard(ServerPlayer player) {
        boolean wasActive = ExtraModulesPlayerData.isPhaseActive(player);
        boolean shouldBeActive = false;
        if (canUsePhaseGuard(player)) {
            long cost = currentPhaseGuardCost();
            shouldBeActive = EnergyService.tryConsumeEnergy(player, cost, EnergyContext.PHASE_GUARD);
        }
        ExtraModulesPlayerData.setPhaseActive(player, shouldBeActive);
        if (shouldBeActive) {
            stabilizePhaseGuard(player);
        }
        if (wasActive != shouldBeActive) {
            if (ExtraModulesConfig.DEBUG.get()) {
                MekanismExtraModules.LOGGER.debug("Phase Guard {} for {}", shouldBeActive ? "active" : "inactive", player.getGameProfile().getName());
            }
        }
    }

    public static boolean isDimensionAllowed(Player player) {
        String dimension = player.level().dimension().location().toString();
        if (ExtraModulesConfig.BLOCKED_DIMENSIONS.get().contains(dimension)) {
            return false;
        }
        return ExtraModulesConfig.ALLOWED_DIMENSIONS.get().isEmpty() || ExtraModulesConfig.ALLOWED_DIMENSIONS.get().contains(dimension);
    }

    private static void logProtection(String message, Object... args) {
        if (ExtraModulesConfig.LOG_PROTECTION_EVENTS.get()) {
            MekanismExtraModules.LOGGER.info(message, args);
        }
    }

    private ProtectionService() {
    }
}
