/*
 * This file ("PacketClientToServer.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PacketClientToServer {

    private CompoundTag data;
    private IDataHandler handler;
    // SkyTekx: what the server read, so that a dropped packet gets one WARN naming its sender
    private int handlerId = -1;
    @Nullable
    private String readError;

    public PacketClientToServer() {

    }

    public PacketClientToServer(CompoundTag data, IDataHandler handler) {
        this.data = data;
        this.handler = handler;
    }

    // SkyTekx: only the handlers of PacketHandler's serverbound allow list decode. The base
    // accepted any index, also those of the clientbound handlers
    public static PacketClientToServer fromBytes(FriendlyByteBuf buffer) {
        PacketClientToServer message = new PacketClientToServer();
        try {
            message.data = buffer.readNbt();
            message.handlerId = buffer.readInt();
            message.handler = PacketHandler.serverboundHandler(message.handlerId);
        } catch (Exception e) {
            message.readError = e.toString();
        }
        return message;
    }

    public static void toBytes(PacketClientToServer message, FriendlyByteBuf buffer) {
        buffer.writeNbt(message.data);
        buffer.writeInt(PacketHandler.DATA_HANDLERS.indexOf(message.handler));
    }

    public static void handle(final PacketClientToServer message, final Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        ServerPlayer sender = context.getSender();
        if (message.readError != null) {
            PacketHandler.warnDropped(sender, "serverbound", "it could not be read (" + message.readError + ")");
        } else if (message.handler == null) {
            PacketHandler.warnDropped(sender, "serverbound", "the handler index " + message.handlerId + " is not a serverbound handler");
        } else if (message.data == null) {
            PacketHandler.warnDropped(sender, "serverbound", "it carries no data for the handler index " + message.handlerId);
        } else {
            PacketHandler.enqueueLogged(context, () -> message.handler.handleData(message.data, context),
                () -> "serverbound handler " + message.handlerId + " for " + PacketHandler.describe(sender));
        }
        context.setPacketHandled(true);
    }
}
