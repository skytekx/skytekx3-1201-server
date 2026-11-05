package com.skytekx.defixes;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;

/**
 * SkyTekx DE Fixes: bug fixes for the unmodified Draconic Evolution and Brandon's Core 1.20.1 builds,
 * applied through the mixins in {@code skytekx_defixes.mixins.json}. This class only opens a network
 * channel that carries no messages. Forge compares the channels of both sides at login, so a client
 * without this mod cannot join a server with it and the other way round. That keeps the Draconic tool
 * tier, which sets the mining speed on both sides, the same on client and server.
 */
@Mod(SkyTekxDEFixes.MOD_ID)
public final class SkyTekxDEFixes {
    public static final String MOD_ID = "skytekx_defixes";
    private static final String PROTOCOL = "1";

    public SkyTekxDEFixes() {
        NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);
    }
}
