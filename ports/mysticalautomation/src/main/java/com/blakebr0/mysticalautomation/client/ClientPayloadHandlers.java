package com.blakebr0.mysticalautomation.client;

import com.blakebr0.mysticalautomation.network.payload.SyncSlotCountPayload;
import net.minecraft.client.Minecraft;

// SkyTekx port: client side handling of the port's own messages, kept apart so a dedicated server never loads it
public final class ClientPayloadHandlers {
    private ClientPayloadHandlers() { }

    public static void syncSlotCount(SyncSlotCountPayload payload) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;

        var menu = player.containerMenu;
        if (menu.containerId != payload.containerId() || payload.slot() < 0 || payload.slot() >= menu.slots.size())
            return;

        menu.getSlot(payload.slot()).set(payload.stack().copyWithCount(payload.count()));
    }
}
