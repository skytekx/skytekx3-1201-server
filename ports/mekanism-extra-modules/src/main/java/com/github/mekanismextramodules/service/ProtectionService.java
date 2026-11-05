package com.github.mekanismextramodules.service;

import com.github.mekanismextramodules.MekanismExtraModules;
import com.github.mekanismextramodules.config.ExtraModulesConfig;
import com.github.mekanismextramodules.data.ExtraModulesPlayerData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.ModList;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ProtectionService {
    /**
     * SkyTekx port: players whose death went through, that is whose LivingDeathEvent nobody canceled. Runtime state
     * of the server thread only, never saved, cleared on respawn and logout.
     */
    private static final Set<UUID> DIED = new HashSet<>();

    // SkyTekx port: Forge 47 has one PlayerTickEvent with a phase and a side, NeoForge 21 has PlayerTickEvent.Post.
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player) || player.isRemoved()) {
            return;
        }
        // SkyTekx port: a dead player keeps ticking until respawn. Without this check a suit that survives death
        // (keepInventory, or a death that bypassed Phase Guard such as /kill) would draw energy and set the
        // dead player's health back to minHealth. The check keys on a death that went through, not on health, so a
        // player whose health a mod wrote to 0 without a death is still rescued, as upstream does.
        if (player.isAlive()) {
            DIED.remove(player.getUUID());
        } else if (DIED.contains(player.getUUID())) {
            return;
        }
        updatePhaseGuard(player);
    }

    /**
     * SkyTekx port: Forge 47 has no EntityInvulnerabilityCheckEvent. LivingAttackEvent is the first hook in
     * {@code Player.hurt}, after vanilla's own {@code isInvulnerableTo} check of {@code ServerPlayer.hurt},
     * so canceling it at the highest priority makes the player invulnerable before any other listener sees
     * the hit, as the NeoForge invulnerability check did.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || isCommandKill(event.getSource())) {
            return;
        }
        if (isPhaseGuardActive(player)) {
            event.setCanceled(true);
            if (ExtraModulesConfig.PHASE_SET_INVULNERABLE_TIME.get()) {
                player.invulnerableTime = Math.max(player.invulnerableTime, ExtraModulesConfig.PHASE_INVULNERABILITY_TICKS.get());
            }
            logProtection("Blocked damage from {}", event.getSource().getMsgId());
        }
    }

    /**
     * SkyTekx port: a mod that calls {@code actuallyHurt} directly skips {@code Player.hurt} and LivingAttackEvent.
     * Upstream's invulnerability check also stops that path at its first line. In Forge 47 the first hook of
     * {@code actuallyHurt} is LivingHurtEvent, which fires before armor, absorption hearts and Mekanism's own MekaSuit
     * absorption (a LivingHurtEvent listener that draws suit energy). Canceling it at the highest priority makes
     * {@code ForgeHooks.onLivingHurt} return 0, and {@code actuallyHurt} returns before any of them.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || isCommandKill(event.getSource())) {
            return;
        }
        if (isPhaseGuardActive(player)) {
            event.setCanceled(true);
            stabilizePhaseGuard(player);
        }
    }

    // SkyTekx port: Forge 47's LivingDamageEvent is NeoForge 21's LivingDamageEvent.Pre, the damage after armor
    // and absorption, just before health drops. It stays as the last backstop.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || isCommandKill(event.getSource())) {
            return;
        }
        if (isPhaseGuardActive(player)) {
            event.setAmount(0.0F);
            stabilizePhaseGuard(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player) || isCommandKill(event.getSource())) {
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

    // SkyTekx port: runs last and only for a death that no listener canceled, so the tick handler can tell a real
    // death from a health of 0.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeathNotCanceled(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DIED.add(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        DIED.remove(event.getOriginal().getUUID());
        ExtraModulesPlayerData.copyForRespawn(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        DIED.remove(event.getEntity().getUUID());
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
                    .filter(effect -> effect.getEffect().getCategory() == MobEffectCategory.HARMFUL)
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

    private static void addEffect(ServerPlayer player, MobEffect effect, int duration, int amplifier) {
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

    /**
     * /kill must always work, so server operators can still remove a protected player. Taken from upstream
     * db62401 (2026-10-27). In 1.20.1 {@code LivingEntity.kill()} hurts with {@code genericKill()}.
     */
    private static boolean isCommandKill(DamageSource source) {
        return source.is(DamageTypes.GENERIC_KILL);
    }

    private static void logProtection(String message, Object... args) {
        if (ExtraModulesConfig.LOG_PROTECTION_EVENTS.get()) {
            MekanismExtraModules.LOGGER.info(message, args);
        }
    }

    private ProtectionService() {
    }
}
