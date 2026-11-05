package com.enderio.base.common.network;

import com.enderio.api.travel.ITravelTarget;
import com.enderio.base.common.handler.TravelHandler;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.base.common.travel.TravelSavedData;
import com.enderio.core.common.network.Packet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

public class RequestTravelPacket implements Packet {
    private final BlockPos pos;

    public RequestTravelPacket(BlockPos pos) {
        this.pos = pos;
    }


    public RequestTravelPacket(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
    }

    protected void write(FriendlyByteBuf writeInto) {
        writeInto.writeBlockPos(pos);
    }

    @Override
    public boolean isValid(NetworkEvent.Context context) {
        return context.getDirection() == NetworkDirection.PLAY_TO_SERVER;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerHandler.handle(context, pos);
    }

    public static class Handler extends PacketHandler<RequestTravelPacket> {

        @Override
        public RequestTravelPacket fromNetwork(FriendlyByteBuf buf) {
            return new RequestTravelPacket(buf);
        }

        @Override
        public void toNetwork(RequestTravelPacket packet, FriendlyByteBuf buf) {
            packet.write(buf);
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_SERVER);
        }
    }

    public static class ServerHandler {
        static void handle(NetworkEvent.Context context, BlockPos pos) {
            var player = context.getSender();

            if (player == null) {
                return;
            }

            TravelSavedData travelData = TravelSavedData.getTravelData(player.level());
            Optional<ITravelTarget> target = travelData.getTravelTarget(pos);

            // SkyTekx: the travel key (565ef4c3e) asks for an anchor jump without an anchor under the player. Then it
            // needs a travel item that the server finds itself, which pays for the jump like a use of the item.
            boolean canBlockTeleport = TravelHandler.canBlockTeleport(player);
            ItemStack travelItem = canBlockTeleport || player.isSpectator() ? ItemStack.EMPTY : TravelHandler.findTravelItem(player);

            // These errors should only ever be triggered if there's some form of desync
            if (!canBlockTeleport && travelItem.isEmpty()) {
                player.displayClientMessage(EIOLang.TRAVEL_ERROR_CANNOT_TELEPORT, true);
                return;
            }
            if (target.isEmpty()) {
                player.displayClientMessage(EIOLang.TRAVEL_ERROR_INVALID_DESTINATION, true);
                return;
            }

            // The client picks the target, so check the travel rules of TravelHandler again. A target that takes no teleports,
            // such as a hidden anchor, can only be reached as an elevator, straight above or below the anchor the player stands on.
            BlockPos anchorPos = player.blockPosition().below();
            boolean isElevatorJump = canBlockTeleport && target.get().getPos().getX() == anchorPos.getX() && target.get().getPos().getZ() == anchorPos.getZ();
            if (!target.get().canTeleportTo() && !(isElevatorJump && target.get().canJumpTo())) {
                player.displayClientMessage(EIOLang.TRAVEL_ERROR_INVALID_DESTINATION, true);
                return;
            }

            // Eventually change the packet structure to include what teleport method was used so this range can be selected correctly.
            // SkyTekx: a jump without an anchor under the player can only be an item jump, so it gets the item range.
            int range = canBlockTeleport ? Math.max(target.get().getBlock2BlockRange(), target.get().getItem2BlockRange()) : target.get().getItem2BlockRange();
            if (pos.distSqr(player.getOnPos()) > range * range) {
                player.displayClientMessage(EIOLang.TRAVEL_ERROR_TOO_FAR, true);
                return;
            }

            boolean teleported = TravelHandler.blockTeleportTo(player.level(), player, target.get(), false);
            if (teleported && !canBlockTeleport) {
                TravelHandler.consumeTravelItem(player, travelItem);
            }
        }
    }
}
