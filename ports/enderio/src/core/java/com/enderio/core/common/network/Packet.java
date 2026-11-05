package com.enderio.core.common.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public interface Packet {

    boolean isValid(NetworkEvent.Context context);

    void handle(NetworkEvent.Context context);


    abstract class PacketHandler<MSG extends Packet> implements BiConsumer<MSG, Supplier<NetworkEvent.Context>> {
        public abstract MSG fromNetwork(FriendlyByteBuf buf);

        public abstract void toNetwork(MSG packet, FriendlyByteBuf buf);

        @Override
        public void accept(MSG msg, Supplier<NetworkEvent.Context> context) {
            NetworkEvent.Context ctx = context.get();
            if (msg.isValid(context.get())) {
                ctx.enqueueWork(() -> msg.handle(ctx));
            } else {
                logPacketError(ctx, "didn't pass check and is invalid", msg);
            }
            context.get().setPacketHandled(true);
        }

        public abstract Optional<NetworkDirection> getDirection();
    }

    /**
     * The square of the distance within which a player may change a block through a packet, the same as for a vanilla container.
     */
    double MAX_BLOCK_DISTANCE_SQR = 8.0 * 8.0;

    /**
     * Whether a client packet may act on the block at the given position.
     * The block must be loaded, so the packet never loads a chunk, and within reach of the player, as for an open container.
     */
    static boolean canAccessBlock(ServerPlayer player, BlockPos pos) {
        return player.level().isLoaded(pos) && player.distanceToSqr(Vec3.atCenterOf(pos)) <= MAX_BLOCK_DISTANCE_SQR;
    }

    static void logPacketError(NetworkEvent.Context context, String error, Packet packet) {
        String sender;
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            sender = "the server";
        } else {
            sender = context.getSender().getName().getContents() + " with IP-Address " + context.getSender().getIpAddress();
        }
        LogManager.getLogger().warn("Packet {} from {}: {}", packet.getClass(),sender, error);
    }
}
