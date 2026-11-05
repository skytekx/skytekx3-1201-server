package com.blakebr0.mysticalautomation.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModConfigs {
    public static final ForgeConfigSpec COMMON;

    public static final ForgeConfigSpec.BooleanValue INFUSER_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CRAFTER_ENABLED;
    public static final ForgeConfigSpec.BooleanValue FARMER_ENABLED;
    public static final ForgeConfigSpec.BooleanValue FARMER_DYNAMIC_MYSTICAL_AGRICULTURE_RECIPES;
    public static final ForgeConfigSpec.BooleanValue FERTILIZER_ENABLED;
    public static final ForgeConfigSpec.BooleanValue INFUSION_ALTARNATOR_ENABLED;
    public static final ForgeConfigSpec.BooleanValue AWAKENING_ALTARNATOR_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ENCHANTERNATOR_ENABLED;

    // common
    static {
        final var common = new ForgeConfigSpec.Builder();

        common.comment("Machine configuration options").push("Machines");

        {
            common.push("Infuser");
            INFUSER_ENABLED = common
                    .comment("Should the Infuser be enabled?")
                    .define("enabled", true);
            common.pop();

            common.push("Crafter");
            CRAFTER_ENABLED = common
                    .comment("Should the Crafter be enabled?")
                    .define("enabled", true);
            common.pop();

            common.push("Farmer");
            FARMER_ENABLED = common
                    .comment("Should the Farmer be enabled?")
                    .define("enabled", true);
            FARMER_DYNAMIC_MYSTICAL_AGRICULTURE_RECIPES = common
                    .comment("Should recipes be generated dynamically for all Mystical Agriculture crops?")
                    .define("dynamicMysticalAgricultureRecipes", true);
            common.pop();

            common.push("Fertilizer");
            FERTILIZER_ENABLED = common
                    .comment("Should the Fertilizer be enabled?")
                    .define("enabled", true);
            common.pop();

            common.push("Infusion Altarnator");
            INFUSION_ALTARNATOR_ENABLED = common
                    .comment("Should the Infusion Altarnator be enabled?")
                    .define("enabled", true);
            common.pop();

            common.push("Awakening Altarnator");
            AWAKENING_ALTARNATOR_ENABLED = common
                    .comment("Should the Awakening Altar be enabled?")
                    .define("enabled", true);
            common.pop();

            common.push("Enchanternator");
            ENCHANTERNATOR_ENABLED = common
                    .comment("Should the Enchanternator be enabled?")
                    .define("enabled", true);
            common.pop();
        }

        common.pop();

        COMMON = common.build();
    }
}
