package com.enderio.conduits.common.network;

import com.enderio.core.common.network.CoreNetwork;

public class ConduitNetwork {
    public static void register() {
        CoreNetwork.registerPacket(new C2SSetConduitConnectionState.Handler(), C2SSetConduitConnectionState.class);
        CoreNetwork.registerPacket(new C2SSetConduitExtendedData.Handler(), C2SSetConduitExtendedData.class);
        CoreNetwork.registerPacket(new CountFilterPacket.Handler(), CountFilterPacket.class);
        CoreNetwork.registerPacket(new DoubleChannelPacket.Handler(), DoubleChannelPacket.class);
        CoreNetwork.registerPacket(new TimerFilterPacket.Handler(), TimerFilterPacket.class);
        CoreNetwork.registerPacket(new ConduitSelectionPacket.Handler(), ConduitSelectionPacket.class);
        CoreNetwork.registerPacket(new C2SSyncProbeState.Handler(), C2SSyncProbeState.class);
        // SkyTekx: appended last, so the ids of the packets above stay as they are.
        CoreNetwork.registerPacket(new C2SOpenConduitFilterMenu.Handler(), C2SOpenConduitFilterMenu.class);
    }

}
