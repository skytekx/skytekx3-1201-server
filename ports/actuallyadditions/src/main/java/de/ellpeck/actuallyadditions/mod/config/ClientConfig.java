package de.ellpeck.actuallyadditions.mod.config;

import net.minecraftforge.common.ForgeConfigSpec;

// SkyTekx: the ClientConfig of upstream main (9c6859cd, 12527cd1) on ForgeConfigSpec. Only the
// physical client registers it (ActuallyAdditions constructor)
public class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec CLIENT_CONFIG;
    public static ForgeConfigSpec.BooleanValue HIDE_ENERGY_OVERLAY;


    static {
        HIDE_ENERGY_OVERLAY = BUILDER.comment("If true, when looking at blocks the Energy Overlay will be hidden.")
                .define("hideEnergyOverlay", false);

        CLIENT_CONFIG = BUILDER.build();
    }
}
