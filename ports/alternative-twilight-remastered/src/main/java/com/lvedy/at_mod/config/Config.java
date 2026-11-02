package com.lvedy.at_mod.config;

import net.minecraftforge.common.ForgeConfigSpec;

// 1.20.1 backport: NeoForge ModConfigSpec -> Forge ForgeConfigSpec (same keys, defaults and ranges, so at_mod_config.toml carries over).
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // 娜迦
    public static final ForgeConfigSpec.DoubleValue MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue NON_DAZED_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue DAZED_DAMAGE_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue DAZE_SPEED_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue PHASE2_HEALTH_RATIO;
    public static final ForgeConfigSpec.DoubleValue PHASE3_HEALTH_RATIO;
    public static final ForgeConfigSpec.DoubleValue VENOM_DAMAGE;
    public static final ForgeConfigSpec.IntValue VENOM_COUNT;
    public static final ForgeConfigSpec.DoubleValue VENOM_SPREAD;
    public static final ForgeConfigSpec.IntValue VENOM_LINGER_TICKS;
    public static final ForgeConfigSpec.BooleanValue MUSIC_ENABLED;
    public static final ForgeConfigSpec.DoubleValue MUSIC_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue MUSIC_VOLUME;

    // 长矛
    public static final ForgeConfigSpec.IntValue SPEAR_HIT_PROTECTION_TICKS;

    // 药水效果
    public static final ForgeConfigSpec.DoubleValue BLOOD_LUST_CURSE_DAMAGE_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue BLOOD_LUST_ATTACK_PER_LEVEL;
    public static final ForgeConfigSpec.DoubleValue BLOOD_LUST_SPEED_PER_LEVEL;

    // 巫妖仆从
    public static final ForgeConfigSpec.DoubleValue LICH_MINION_MAX_HEALTH;
    public static final ForgeConfigSpec.DoubleValue LICH_MINION_ARMOR;
    public static final ForgeConfigSpec.DoubleValue LICH_MINION_ARMOR_TOUGHNESS;

    // 巫妖
    public static final ForgeConfigSpec.IntValue LICH_BLOOD_LUST_CURSE_DURATION;
    public static final ForgeConfigSpec.DoubleValue LICH_PHASE3_MAX_HEALTH_BONUS;
    public static final ForgeConfigSpec.DoubleValue LICH_PHASE3_ARMOR_BONUS;
    public static final ForgeConfigSpec.DoubleValue LICH_PHASE3_ARMOR_TOUGHNESS_BONUS;
    public static final ForgeConfigSpec.DoubleValue LICH_PHASE2_TRANSITION_HP_LOSS_PER_MINION;
    public static final ForgeConfigSpec.DoubleValue LICH_PHASE2_TRANSITION_MAX_HP_LOSS_CAP;
    public static final ForgeConfigSpec.IntValue LICH_PHASE3_MAGIC_ORB_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue LICH_PHASE3_ORB_COUNT_THRESHOLD;
    public static final ForgeConfigSpec.IntValue LICH_PHASE3_ORB_COUNT_BEFORE_THRESHOLD;
    public static final ForgeConfigSpec.IntValue LICH_PHASE3_ORB_COUNT_AFTER_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue LICH_MUSIC_ENABLED;
    public static final ForgeConfigSpec.DoubleValue LICH_MUSIC_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue LICH_MUSIC_VOLUME;

    public static final ForgeConfigSpec.BooleanValue HYDRA_MUSIC_ENABLED;
    public static final ForgeConfigSpec.DoubleValue HYDRA_MUSIC_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue HYDRA_MUSIC_VOLUME;

    public static final ForgeConfigSpec.BooleanValue UR_GHAST_MUSIC_ENABLED;
    public static final ForgeConfigSpec.DoubleValue UR_GHAST_MUSIC_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue UR_GHAST_MUSIC_VOLUME;

    public static final ForgeConfigSpec.BooleanValue KNIGHT_PHANTOM_MUSIC_ENABLED;
    public static final ForgeConfigSpec.DoubleValue KNIGHT_PHANTOM_MUSIC_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue KNIGHT_PHANTOM_MUSIC_VOLUME;

    public static final ForgeConfigSpec.BooleanValue SNOW_QUEEN_MUSIC_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SNOW_QUEEN_MUSIC_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue SNOW_QUEEN_MUSIC_VOLUME;

    public static final ForgeConfigSpec.BooleanValue MINOSHROOM_MUSIC_ENABLED;
    public static final ForgeConfigSpec.DoubleValue MINOSHROOM_MUSIC_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue MINOSHROOM_MUSIC_VOLUME;

    public static final ForgeConfigSpec.BooleanValue ALPHA_YETI_MUSIC_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ALPHA_YETI_MUSIC_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue ALPHA_YETI_MUSIC_VOLUME;

    // 幻影骑士极光特效
    public static final ForgeConfigSpec.BooleanValue PHANTOM_AURORA_ENABLED;
    public static final ForgeConfigSpec.DoubleValue PHANTOM_AURORA_DETECTION_RADIUS;
    public static final ForgeConfigSpec.DoubleValue PHANTOM_AURORA_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<String> PHANTOM_AURORA_COLOR_HEX;

    static {
        BUILDER.push("naga");
        MAX_HEALTH = BUILDER
                .translation("config.at_mod.naga.maxHealth")
                .comment("Naga max health")
                .defineInRange("maxHealth", 210.0, 1.0, 10000.0);

        NON_DAZED_DAMAGE_MULTIPLIER = BUILDER
                .translation("config.at_mod.naga.nonDazedDamageMultiplier")
                .comment("Damage multiplier when Naga is NOT dazed (< 1 = damage reduction)")
                .defineInRange("nonDazedDamageMultiplier", 0.5, 0.0, 10000.0);

        DAZED_DAMAGE_MULTIPLIER = BUILDER
                .translation("config.at_mod.naga.dazedDamageMultiplier")
                .comment("Damage multiplier when Naga IS dazed (> 1 = damage increase)")
                .defineInRange("dazedDamageMultiplier", 1.5, 0.0, 10000.0);

        DAZE_SPEED_THRESHOLD = BUILDER
                .translation("config.at_mod.naga.dazeSpeedThreshold")
                .comment("Relative speed required between attacker and Naga to trigger daze")
                .defineInRange("dazeSpeedThreshold", 2.0, 0.1, 20.0);

        PHASE2_HEALTH_RATIO = BUILDER
                .translation("config.at_mod.naga.phase2HealthRatio")
                .comment("Phase 2 health threshold ratio (enters phase 2 when below this ratio of max health)")
                .defineInRange("phase2HealthRatio", 0.667, 0.0, 1.0);

        PHASE3_HEALTH_RATIO = BUILDER
                .translation("config.at_mod.naga.phase3HealthRatio")
                .comment("Phase 3 health threshold ratio (enters phase 3 when below this ratio of max health)")
                .defineInRange("phase3HealthRatio", 0.333, 0.0, 1.0);

        BUILDER.push("venom");
        VENOM_DAMAGE = BUILDER
                .translation("config.at_mod.naga.venom.damage")
                .comment("Venom's damage")
                .defineInRange("damage", 2D, 0, 200000);

        VENOM_COUNT = BUILDER
                .translation("config.at_mod.naga.venom.count")
                .comment("Number of venom projectiles fired per charge")
                .defineInRange("count", 3, 1, 20);

        VENOM_SPREAD = BUILDER
                .translation("config.at_mod.naga.venom.spread")
                .comment("Venom spread range around target (blocks)")
                .defineInRange("spread", 8.0, 0.0, 50.0);

        VENOM_LINGER_TICKS = BUILDER
                .translation("config.at_mod.naga.venom.lingerTicks")
                .comment("Venom lingering duration after landing (ticks, 20 ticks = 1 second)")
                .defineInRange("lingerTicks", 160, 20, 1200);
        BUILDER.pop();

        BUILDER.push("music");
        MUSIC_ENABLED = BUILDER
                .translation("config.at_mod.naga.music.enabled")
                .comment("Enable background music when a Naga is nearby")
                .define("enabled", true);

        MUSIC_DETECTION_RADIUS = BUILDER
                .translation("config.at_mod.naga.music.detectionRadius")
                .comment("Player-to-Naga distance (in blocks) at which the music starts/stops")
                .defineInRange("detectionRadius", 200.0, 4.0, 10000.0);

        MUSIC_VOLUME = BUILDER
                .translation("config.at_mod.naga.music.volume")
                .comment("Volume multiplier for the Naga background music (0.0 - 1.0)")
                .defineInRange("volume", 1.0, 0.0, 1.0);
        BUILDER.pop();
        BUILDER.pop();

        BUILDER.push("spear");
        SPEAR_HIT_PROTECTION_TICKS = BUILDER
                .translation("config.at_mod.spear.hitProtectionTicks")
                .comment("Ticks during which a spear cannot deal collision damage after its holder is hit (20 ticks = 1 second)")
                .defineInRange("hitProtectionTicks", 30, 0, 600);
        BUILDER.pop();

        BUILDER.push("bloodLustCurse");
        BLOOD_LUST_CURSE_DAMAGE_PER_LEVEL = BUILDER
                .translation("config.at_mod.effect.bloodLustCurse.damagePerLevel")
                .comment("Fraction of max health dealt as true damage per amplifier level when Blood Lust Curse expires (e.g. 0.2 = level I deals 20%)")
                .defineInRange("damagePerLevel", 0.2, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.push("bloodLust");
        BLOOD_LUST_ATTACK_PER_LEVEL = BUILDER
                .translation("config.at_mod.effect.bloodLust.attackPerLevel")
                .comment("Multiplied attack damage bonus per amplifier level (0.1 = +10% per level, applied as ADD_MULTIPLIED_TOTAL)")
                .defineInRange("attackPerLevel", 0.1, 0.0, 10.0);

        BLOOD_LUST_SPEED_PER_LEVEL = BUILDER
                .translation("config.at_mod.effect.bloodLust.speedPerLevel")
                .comment("Multiplied movement speed bonus per amplifier level (0.1 = +10% per level, applied as ADD_MULTIPLIED_TOTAL)")
                .defineInRange("speedPerLevel", 0.1, 0.0, 10.0);
        BUILDER.pop();

        BUILDER.push("lichMinion");
        LICH_MINION_MAX_HEALTH = BUILDER
                .translation("config.at_mod.lichMinion.maxHealth")
                .comment("Lich Minion max health (applied when summoned by the Lich)")
                .defineInRange("maxHealth", 40.0, 1.0, 10000.0);

        LICH_MINION_ARMOR = BUILDER
                .translation("config.at_mod.lichMinion.armor")
                .comment("Lich Minion armor (applied when summoned by the Lich)")
                .defineInRange("armor", 6.0, 0.0, 30.0);

        LICH_MINION_ARMOR_TOUGHNESS = BUILDER
                .translation("config.at_mod.lichMinion.armorToughness")
                .comment("Lich Minion armor toughness (applied when summoned by the Lich)")
                .defineInRange("armorToughness", 0.0, 0.0, 20.0);
        BUILDER.pop();

        BUILDER.push("lich");
        LICH_BLOOD_LUST_CURSE_DURATION = BUILDER
                .translation("config.at_mod.lich.bloodLustCurseDuration")
                .comment("Initial Blood Lust Curse duration in ticks when first applied by Lich shield break (20 ticks = 1 second)")
                .defineInRange("bloodLustCurseDuration", 1800, 20, 72000);

        LICH_PHASE3_MAX_HEALTH_BONUS = BUILDER
                .translation("config.at_mod.lich.phase3MaxHealthBonus")
                .comment("Max health bonus when Lich enters phase 3")
                .defineInRange("phase3MaxHealthBonus", 50.0, 0.0, 10000.0);

        LICH_PHASE3_ARMOR_BONUS = BUILDER
                .translation("config.at_mod.lich.phase3ArmorBonus")
                .comment("Armor bonus when Lich enters phase 3")
                .defineInRange("phase3ArmorBonus", 10.0, 0.0, 30.0);

        LICH_PHASE3_ARMOR_TOUGHNESS_BONUS = BUILDER
                .translation("config.at_mod.lich.phase3ArmorToughnessBonus")
                .comment("Armor toughness bonus when Lich enters phase 3")
                .defineInRange("phase3ArmorToughnessBonus", 5.0, 0.0, 20.0);

        LICH_PHASE2_TRANSITION_HP_LOSS_PER_MINION = BUILDER
                .translation("config.at_mod.lich.phase2TransitionHpLossPerMinion")
                .comment("Fraction of max health lost per minion cleared during phase 2 to phase 3 transition (0.1 = 10%)")
                .defineInRange("phase2TransitionHpLossPerMinion", 0.1, 0.0, 1.0);

        LICH_PHASE2_TRANSITION_MAX_HP_LOSS_CAP = BUILDER
                .translation("config.at_mod.lich.phase2TransitionMaxHpLossCap")
                .comment("Maximum fraction of max health that can be lost during phase 2 to phase 3 transition (0.5 = 50%)")
                .defineInRange("phase2TransitionMaxHpLossCap", 0.5, 0.0, 1.0);

        LICH_PHASE3_MAGIC_ORB_INTERVAL = BUILDER
                .translation("config.at_mod.lich.phase3MagicOrbInterval")
                .comment("Interval in ticks between magic orb attacks in phase 3 (20 ticks = 1 second)")
                .defineInRange("phase3MagicOrbInterval", 100, 40, 12000);

        LICH_PHASE3_ORB_COUNT_THRESHOLD = BUILDER
                .translation("config.at_mod.lich.phase3OrbCountThreshold")
                .comment("Health percentage threshold for increased orb count in phase 3 (e.g., 0.5 = 50% HP)")
                .defineInRange("phase3OrbCountThreshold", 0.5, 0.0, 1.0);

        LICH_PHASE3_ORB_COUNT_BEFORE_THRESHOLD = BUILDER
                .translation("config.at_mod.lich.phase3OrbCountBeforeThreshold")
                .comment("Number of magic orbs summoned when HP is above threshold")
                .defineInRange("phase3OrbCountBeforeThreshold", 3, 1, 12);

        LICH_PHASE3_ORB_COUNT_AFTER_THRESHOLD = BUILDER
                .translation("config.at_mod.lich.phase3OrbCountAfterThreshold")
                .comment("Number of magic orbs summoned when HP is below threshold")
                .defineInRange("phase3OrbCountAfterThreshold", 6, 1, 12);
        BUILDER.pop();

        BUILDER.push("lich_music");
        LICH_MUSIC_ENABLED = BUILDER
                .translation("config.at_mod.lich.music.enabled")
                .comment("Enable background music when a Lich is nearby")
                .define("enabled", true);

        LICH_MUSIC_DETECTION_RADIUS = BUILDER
                .translation("config.at_mod.lich.music.detectionRadius")
                .comment("Player-to-Lich distance (in blocks) at which the music starts/stops")
                .defineInRange("detectionRadius", 200.0, 4.0, 10000.0);

        LICH_MUSIC_VOLUME = BUILDER
                .translation("config.at_mod.lich.music.volume")
                .comment("Volume multiplier for the Lich background music (0.0 - 1.0)")
                .defineInRange("volume", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("hydra_music");
        HYDRA_MUSIC_ENABLED = BUILDER.comment("Enable background music when a Hydra is nearby").define("enabled", true);
        HYDRA_MUSIC_DETECTION_RADIUS = BUILDER.comment("Player-to-Hydra distance at which the music starts").defineInRange("detectionRadius", 200.0, 4.0, 10000.0);
        HYDRA_MUSIC_VOLUME = BUILDER.comment("Volume multiplier for the Hydra background music").defineInRange("volume", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("ur_ghast_music");
        UR_GHAST_MUSIC_ENABLED = BUILDER.comment("Enable background music when an Ur-Ghast is nearby").define("enabled", true);
        UR_GHAST_MUSIC_DETECTION_RADIUS = BUILDER.comment("Player-to-Ur-Ghast distance at which the music starts").defineInRange("detectionRadius", 200.0, 4.0, 10000.0);
        UR_GHAST_MUSIC_VOLUME = BUILDER.comment("Volume multiplier for the Ur-Ghast background music").defineInRange("volume", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("knight_phantom_music");
        KNIGHT_PHANTOM_MUSIC_ENABLED = BUILDER.comment("Enable background music when a Knight Phantom is nearby").define("enabled", true);
        KNIGHT_PHANTOM_MUSIC_DETECTION_RADIUS = BUILDER.comment("Player-to-Knight Phantom distance at which the music starts").defineInRange("detectionRadius", 200.0, 4.0, 10000.0);
        KNIGHT_PHANTOM_MUSIC_VOLUME = BUILDER.comment("Volume multiplier for the Knight Phantom background music").defineInRange("volume", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("snow_queen_music");
        SNOW_QUEEN_MUSIC_ENABLED = BUILDER.comment("Enable background music when a Snow Queen is nearby").define("enabled", true);
        SNOW_QUEEN_MUSIC_DETECTION_RADIUS = BUILDER.comment("Player-to-Snow Queen distance at which the music starts").defineInRange("detectionRadius", 200.0, 4.0, 10000.0);
        SNOW_QUEEN_MUSIC_VOLUME = BUILDER.comment("Volume multiplier for the Snow Queen background music").defineInRange("volume", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("minoshroom_music");
        MINOSHROOM_MUSIC_ENABLED = BUILDER.comment("Enable background music when a Minoshroom is nearby").define("enabled", true);
        MINOSHROOM_MUSIC_DETECTION_RADIUS = BUILDER.comment("Player-to-Minoshroom distance at which the music starts").defineInRange("detectionRadius", 200.0, 4.0, 10000.0);
        MINOSHROOM_MUSIC_VOLUME = BUILDER.comment("Volume multiplier for the Minoshroom background music").defineInRange("volume", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("alpha_yeti_music");
        ALPHA_YETI_MUSIC_ENABLED = BUILDER.comment("Enable background music when an Alpha Yeti is nearby").define("enabled", true);
        ALPHA_YETI_MUSIC_DETECTION_RADIUS = BUILDER.comment("Player-to-Alpha Yeti distance at which the music starts").defineInRange("detectionRadius", 200.0, 4.0, 10000.0);
        ALPHA_YETI_MUSIC_VOLUME = BUILDER.comment("Volume multiplier for the Alpha Yeti background music").defineInRange("volume", 1.0, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("phantom_aurora");
        PHANTOM_AURORA_ENABLED = BUILDER
                .translation("config.at_mod.phantomAurora.enabled")
                .comment("Enable the aurora post-processing effect around summoned Knight Phantoms")
                .define("enabled", true);
        PHANTOM_AURORA_DETECTION_RADIUS = BUILDER
                .translation("config.at_mod.phantomAurora.detectionRadius")
                .comment("Player-to-Phantom distance (in blocks) within which the effect activates")
                .defineInRange("detectionRadius", 80.0, 4.0, 1024.0);
        PHANTOM_AURORA_RADIUS = BUILDER
                .translation("config.at_mod.phantomAurora.radius")
                .comment("Aurora visual radius in blocks (max distance from anchor where blocks get tinted)")
                .defineInRange("radius", 40.0, 4.0, 256.0);
        PHANTOM_AURORA_COLOR_HEX = BUILDER
                .translation("config.at_mod.phantomAurora.color")
                .comment("Aurora base color as hex string #RRGGBB (default cyan-blue)")
                .define("color", "#8CD9FF");
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
