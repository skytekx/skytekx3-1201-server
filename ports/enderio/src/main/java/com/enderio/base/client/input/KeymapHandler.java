package com.enderio.base.client.input;

import com.enderio.EnderIO;
import com.enderio.base.common.config.BaseConfig;
import com.enderio.base.common.handler.TravelHandler;
import com.enderio.base.common.network.C2SToggleMagnet;
import com.enderio.core.common.network.CoreNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SkyTekx: the travel and electromagnet keys of 8.x (565ef4c3e). A tap of the travel key blinks like a crouching use of
 * the Staff of Travelling, holding it longer than travelKeyHoldDelayTicks shows the travel anchors and jumps to the
 * targeted one on release. The staff may sit in the inventory or a Curios slot. The magnet key toggles the first
 * electromagnet in a Curios slot or the inventory. The server checks the item, its cooldown and its energy again.
 */
@Mod.EventBusSubscriber(modid = EnderIO.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class KeymapHandler {

    private static int travelKeyTicks = 0;
    private static boolean travelTargetsVisible = false;

    private KeymapHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            travelKeyTicks = 0;
            travelTargetsVisible = false;
            return;
        }

        // Magnet
        if (KeyBindings.TOGGLE_MAGNET.consumeClick()) {
            CoreNetwork.sendToServer(new C2SToggleMagnet());
        }

        // Travel. The anchors show while the key is held.
        travelTargetsVisible = travelKeyTicks > BaseConfig.CLIENT.TRAVEL_KEY_HOLD_DELAY.get();

        boolean shouldTeleport = false;
        if (KeyBindings.TRAVEL_STAFF.isDown()) {
            travelKeyTicks++;
        } else {
            shouldTeleport = travelKeyTicks > 0;
            travelKeyTicks = 0;
        }

        if (shouldTeleport && !TravelHandler.findTravelItem(player).isEmpty()) {
            if (travelTargetsVisible) {
                TravelHandler.blockTeleport(level, player, true);
            } else {
                TravelHandler.shortTeleport(level, player, true);
            }
        }
    }

    /**
     * Whether the travel key is held long enough to show the travel anchors.
     */
    public static boolean showTravelTargets() {
        return travelTargetsVisible;
    }
}
