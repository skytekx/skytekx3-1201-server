package com.enderio.base.common.network;

import com.enderio.base.common.handler.TravelHandler;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.common.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

/**
 * SkyTekx: the short jump of the travel key (565ef4c3e, ServerboundRequestShortTravelPacket). The packet carries
 * nothing. The server finds the travel item itself, checks its cooldown and energy, and finds the target from its own
 * copy of the player, so a client cannot jump without a staff or pick the target.
 */
public record C2SRequestShortTravel() implements Packet {

    @Override
    public boolean isValid(NetworkEvent.Context context) {
        return context.getDirection() == NetworkDirection.PLAY_TO_SERVER && context.getSender() != null;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        ItemStack travelItem = TravelHandler.findTravelItem(player);
        if (travelItem.isEmpty()) {
            player.displayClientMessage(EIOLang.TRAVEL_ERROR_CANNOT_TELEPORT, true);
            return;
        }

        if (TravelHandler.shortTeleport(player.level(), player)) {
            TravelHandler.consumeTravelItem(player, travelItem);
        }
    }

    public static class Handler extends Packet.PacketHandler<C2SRequestShortTravel> {

        @Override
        public C2SRequestShortTravel fromNetwork(FriendlyByteBuf buf) {
            return new C2SRequestShortTravel();
        }

        @Override
        public void toNetwork(C2SRequestShortTravel packet, FriendlyByteBuf buf) {
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_SERVER);
        }
    }
}
