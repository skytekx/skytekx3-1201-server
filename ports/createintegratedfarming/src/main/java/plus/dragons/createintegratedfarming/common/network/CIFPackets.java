/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createintegratedfarming.common.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import plus.dragons.createintegratedfarming.common.CIFCommon;

// SkyTekx: a Forge SimpleChannel in place of NeoForge's payload registrar. Both messages go from the
// server to the client. The channel accepts only its own protocol version, so a client without this
// build cannot join a server with it and the other way round.
public final class CIFPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final int ROOSTING_DISPLAY_SYNC = 0;
    public static final int CHICKEN_FOOD_SYNC = 1;
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            CIFCommon.asResource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private CIFPackets() {}

    public static void register() {
        CHANNEL.messageBuilder(RoostingDisplayPayload.class, ROOSTING_DISPLAY_SYNC, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RoostingDisplayPayload::encode)
                .decoder(RoostingDisplayPayload::decode)
                .consumerMainThread((payload, context) -> RoostingDisplayClientCache.accept(payload.snapshot()))
                .add();
        CHANNEL.messageBuilder(ChickenFoodSyncPacket.class, CHICKEN_FOOD_SYNC, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ChickenFoodSyncPacket::encode)
                .decoder(ChickenFoodSyncPacket::decode)
                .consumerMainThread((packet, context) -> packet.apply())
                .add();
    }
}
