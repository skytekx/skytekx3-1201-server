package com.enderio.base.client.input;

import com.enderio.EnderIO;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnderIO.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class KeyBindings {
    public static final String CATEGORY = "keybinding.enderio.category";

    public static final KeyMapping MODE_CHANGE = new KeyMapping(
        "keybinding.enderio.probe_mode",
        KeyConflictContext.IN_GAME, 
        InputConstants.getKey(InputConstants.KEY_Y, -1),
        CATEGORY
    );

    // SkyTekx: the keys of 8.x (565ef4c3e), handled by KeymapHandler. Upstream binds them to G and M, which mods of the
    // pack already use (G: Curios, Ars Nouveau, Simple Voice Chat, LittleTiles, GuideME. M: Xaero's World Map, Simple
    // Voice Chat, LittleTiles), so they ship unbound. They sit in the 6.x Ender IO category next to the probe key.
    public static final KeyMapping TRAVEL_STAFF = new KeyMapping(
        "key.enderio.travel_staff",
        KeyConflictContext.IN_GAME,
        InputConstants.UNKNOWN,
        CATEGORY
    );

    public static final KeyMapping TOGGLE_MAGNET = new KeyMapping(
        "key.enderio.toggle_magnet",
        KeyConflictContext.IN_GAME,
        InputConstants.UNKNOWN,
        CATEGORY
    );
    
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(MODE_CHANGE);
        event.register(TRAVEL_STAFF);
        event.register(TOGGLE_MAGNET);
    }
}
