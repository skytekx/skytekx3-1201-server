package com.enderio.core.client;

import com.enderio.core.EnderCore;
import com.enderio.core.common.data.JsonDataMaps;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SkyTekx: drops the data map values that a server sent when the client leaves it.
 */
@Mod.EventBusSubscriber(modid = EnderCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientDataMapEvents {

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        JsonDataMaps.clearSyncedValues();
    }
}
