package com.github.mekanismextramodules.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ExtraModulesConfig {
    public static final ModConfigSpec SERVER_SPEC;

    public static final ModConfigSpec.BooleanValue DEBUG;
    public static final ModConfigSpec.BooleanValue LOG_PROTECTION_EVENTS;
    public static final ModConfigSpec.BooleanValue REQUIRE_FULL_MEKASUIT;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_DIMENSIONS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_DIMENSIONS;

    public static final ModConfigSpec.BooleanValue PHASE_ENABLED;
    public static final ModConfigSpec.LongValue PHASE_ENERGY_COST_PER_TICK;
    public static final ModConfigSpec.BooleanValue PHASE_SET_INVULNERABLE_TIME;
    public static final ModConfigSpec.IntValue PHASE_INVULNERABILITY_TICKS;
    public static final ModConfigSpec.DoubleValue PHASE_MIN_HEALTH;
    public static final ModConfigSpec.BooleanValue PHASE_CLEAR_FIRE;
    public static final ModConfigSpec.BooleanValue PHASE_RESET_FALL_DISTANCE;

    public static final ModConfigSpec.BooleanValue CHAOS_ENABLED;
    public static final ModConfigSpec.BooleanValue CHAOS_ALLOW_MIXIN_PROTECTION;
    public static final ModConfigSpec.BooleanValue CHAOS_STRICT_DRACONIC_ONLY;

    public static final ModConfigSpec.BooleanValue REVIVAL_ENABLED;
    public static final ModConfigSpec.LongValue REVIVAL_ENERGY_COST;
    public static final ModConfigSpec.DoubleValue REVIVAL_HEALTH;
    public static final ModConfigSpec.DoubleValue REVIVAL_HEALTH_PERCENTAGE;
    public static final ModConfigSpec.IntValue REVIVAL_INVULNERABILITY_TICKS;
    public static final ModConfigSpec.BooleanValue REVIVAL_CLEAR_FIRE;
    public static final ModConfigSpec.BooleanValue REVIVAL_RESET_FALL_DISTANCE;
    public static final ModConfigSpec.BooleanValue REVIVAL_CURE_HARMFUL_EFFECTS;
    public static final ModConfigSpec.BooleanValue REVIVAL_APPLY_TOTEM_EFFECTS;
    public static final ModConfigSpec.IntValue REVIVAL_REGENERATION_DURATION;
    public static final ModConfigSpec.IntValue REVIVAL_REGENERATION_AMPLIFIER;
    public static final ModConfigSpec.IntValue REVIVAL_ABSORPTION_DURATION;
    public static final ModConfigSpec.IntValue REVIVAL_ABSORPTION_AMPLIFIER;
    public static final ModConfigSpec.IntValue REVIVAL_FIRE_RESISTANCE_DURATION;
    public static final ModConfigSpec.IntValue REVIVAL_FIRE_RESISTANCE_AMPLIFIER;
    public static final ModConfigSpec.BooleanValue REVIVAL_PLAY_TOTEM_ANIMATION;
    public static final ModConfigSpec.BooleanValue REVIVAL_SHOW_MESSAGE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        builder.comment("Enables additional diagnostic logging. Leave disabled during normal play.");
        DEBUG = builder.define("debug", false);
        builder.comment("Logs every blocked damage or death event to the server log.");
        LOG_PROTECTION_EVENTS = builder.define("logProtectionEvents", false);
        builder.comment("Requires all four MekaSuit armor pieces for every protection module.");
        REQUIRE_FULL_MEKASUIT = builder.define("requireFullMekaSuit", true);
        builder.comment("Dimensions where modules may work. Empty allows every dimension not listed below.");
        ALLOWED_DIMENSIONS = builder.defineListAllowEmpty("allowedDimensions", List.of(), ExtraModulesConfig::isString);
        builder.comment("Dimensions where protection modules never work. Entries here override allowedDimensions.");
        BLOCKED_DIMENSIONS = builder.defineListAllowEmpty("blockedDimensions", List.of(), ExtraModulesConfig::isString);
        builder.pop();

        builder.push("phaseGuard");
        builder.comment("Enables the Phase Guard Unit.");
        PHASE_ENABLED = builder.define("enabled", true);
        builder.comment("Forge Energy (FE) consumed each tick while Phase Guard is active. 20 ticks = 1 second.");
        PHASE_ENERGY_COST_PER_TICK = builder.defineInRange("energyCostPerTick", 500_000L, 0L, Long.MAX_VALUE);
        builder.comment("Applies vanilla invulnerability time when Phase Guard blocks damage.");
        PHASE_SET_INVULNERABLE_TIME = builder.define("setInvulnerableTime", true);
        builder.comment("Minimum invulnerability time maintained after protection. Only used when setInvulnerableTime is true.");
        PHASE_INVULNERABILITY_TICKS = builder.defineInRange("invulnerabilityTicks", 2, 0, 200);
        builder.comment("Minimum health maintained while Phase Guard is active. 2 health = 1 heart.");
        PHASE_MIN_HEALTH = builder.defineInRange("minHealth", 1.0D, 0.5D, 1024.0D);
        builder.comment("Extinguishes the player while Phase Guard is active.");
        PHASE_CLEAR_FIRE = builder.define("clearFire", true);
        builder.comment("Prevents accumulated fall damage while Phase Guard is active.");
        PHASE_RESET_FALL_DISTANCE = builder.define("resetFallDistance", true);
        builder.pop();

        builder.push("chaosAnchor");
        builder.comment("Enables the Chaos Anchor Unit and its optional Draconic Evolution integration.");
        CHAOS_ENABLED = builder.define("enabled", true);
        builder.comment("Allows the optional mixin that intercepts Chaos Guardian direct health writes and forced death.");
        CHAOS_ALLOW_MIXIN_PROTECTION = builder.define("allowMixinProtection", true);
        builder.comment("Restricts damage-source interception to identified Draconic Evolution attacks.");
        CHAOS_STRICT_DRACONIC_ONLY = builder.define("strictDraconicOnly", true);
        builder.pop();

        builder.push("emergencyRevival");
        builder.comment("Enables the Emergency Revival Unit.");
        REVIVAL_ENABLED = builder.define("enabled", true);
        builder.comment("Forge Energy (FE) consumed each time Emergency Revival prevents death.");
        REVIVAL_ENERGY_COST = builder.defineInRange("energyCost", 10_000_000L, 0L, Long.MAX_VALUE);
        builder.comment("Flat health restored after revival. 2 health = 1 heart.");
        REVIVAL_HEALTH = builder.defineInRange("reviveHealth", 4.0D, 1.0D, 1024.0D);
        builder.comment("Fraction of maximum health restored after revival. The greater of this value and reviveHealth is used. Set to 0 to disable.");
        REVIVAL_HEALTH_PERCENTAGE = builder.defineInRange("reviveHealthPercentage", 0.0D, 0.0D, 1.0D);
        builder.comment("Invulnerability time granted after revival.");
        REVIVAL_INVULNERABILITY_TICKS = builder.defineInRange("invulnerabilityTicks", 20, 0, 1200);
        builder.comment("Extinguishes the player after revival.");
        REVIVAL_CLEAR_FIRE = builder.define("clearFire", true);
        builder.comment("Clears accumulated fall distance after revival.");
        REVIVAL_RESET_FALL_DISTANCE = builder.define("resetFallDistance", true);
        builder.comment("Removes all active harmful effects after revival, including effects added by other mods.");
        REVIVAL_CURE_HARMFUL_EFFECTS = builder.define("cureHarmfulEffects", true);
        builder.comment("Applies the configurable regeneration, absorption, and fire resistance effects below.");
        REVIVAL_APPLY_TOTEM_EFFECTS = builder.define("applyTotemEffects", true);
        builder.comment("Regeneration duration in ticks. Set to 0 to disable this effect.");
        REVIVAL_REGENERATION_DURATION = builder.defineInRange("regenerationDuration", 160, 0, 72_000);
        builder.comment("Regeneration amplifier, where 0 is level I and 1 is level II.");
        REVIVAL_REGENERATION_AMPLIFIER = builder.defineInRange("regenerationAmplifier", 1, 0, 255);
        builder.comment("Absorption duration in ticks. Set to 0 to disable this effect.");
        REVIVAL_ABSORPTION_DURATION = builder.defineInRange("absorptionDuration", 100, 0, 72_000);
        builder.comment("Absorption amplifier, where 0 is level I and 1 is level II.");
        REVIVAL_ABSORPTION_AMPLIFIER = builder.defineInRange("absorptionAmplifier", 1, 0, 255);
        builder.comment("Fire resistance duration in ticks. Set to 0 to disable this effect.");
        REVIVAL_FIRE_RESISTANCE_DURATION = builder.defineInRange("fireResistanceDuration", 800, 0, 72_000);
        builder.comment("Fire resistance amplifier, where 0 is level I.");
        REVIVAL_FIRE_RESISTANCE_AMPLIFIER = builder.defineInRange("fireResistanceAmplifier", 0, 0, 255);
        builder.comment("Plays the vanilla Totem of Undying animation and sound after revival.");
        REVIVAL_PLAY_TOTEM_ANIMATION = builder.define("playTotemAnimation", true);
        builder.comment("Shows the Emergency Revival activation message above the hotbar.");
        REVIVAL_SHOW_MESSAGE = builder.define("showActivationMessage", true);
        builder.pop();

        SERVER_SPEC = builder.build();
    }

    private static boolean isString(Object value) {
        return value instanceof String;
    }

    private ExtraModulesConfig() {
    }
}
