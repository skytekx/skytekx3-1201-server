package com.enderio.base.common.config.client;

import net.minecraftforge.common.ForgeConfigSpec;

public class BaseClientConfig {
    public final ForgeConfigSpec.ConfigValue<Boolean> MACHINE_PARTICLES;
    public final ForgeConfigSpec.IntValue TRAVEL_KEY_HOLD_DELAY;

    public BaseClientConfig(ForgeConfigSpec.Builder builder) {
        // SkyTekx: the travel key of 8.x (565ef4c3e).
        builder.push("behavior");
        TRAVEL_KEY_HOLD_DELAY = builder.comment("Ticks before the Staff of Travelling key counts as held, which shows the travel anchors and jumps to the targeted one on release")
            .defineInRange("travelKeyHoldDelayTicks", 8, 1, 200);
        builder.pop();

        builder.push("visual");
        MACHINE_PARTICLES = builder.comment("Enable machine particles").define("machineParticles", true);
        builder.pop();
    }
}
