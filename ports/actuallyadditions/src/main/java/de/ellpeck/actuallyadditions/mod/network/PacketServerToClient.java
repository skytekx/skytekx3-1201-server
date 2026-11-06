/*
 * This file ("PacketServerToClient.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.network;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;


public class PacketServerToClient {

    private CompoundTag data;
    private IDataHandler handler;
    // SkyTekx: what the client read, for the WARN of a dropped packet
    private int handlerId = -1;
    @Nullable
    private String readError;

    public PacketServerToClient() {

    }

    public PacketServerToClient(CompoundTag data, IDataHandler handler) {
        this.data = data;
        this.handler = handler;
    }

    // SkyTekx: only the handlers of PacketHandler's clientbound allow list decode
    public static PacketServerToClient fromBytes(final FriendlyByteBuf buffer) {
        PacketServerToClient message = new PacketServerToClient();
        try {
            message.data = buffer.readNbt();
            message.handlerId = buffer.readInt();
            message.handler = PacketHandler.clientboundHandler(message.handlerId);
        } catch (Exception e) {
            message.readError = e.toString();
        }
        return message;
    }

    public static void toBytes(final PacketServerToClient message, FriendlyByteBuf buffer) {
        buffer.writeNbt(message.data);
        buffer.writeInt(PacketHandler.DATA_HANDLERS.indexOf(message.handler));
    }

    public static void handle(final PacketServerToClient message, final Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (message.readError != null) {
            ActuallyAdditions.LOGGER.warn("Dropped a clientbound packet that could not be read: {}", message.readError);
        } else if (message.handler == null) {
            ActuallyAdditions.LOGGER.warn("Dropped a clientbound packet: the handler index {} is not a clientbound handler", message.handlerId);
        } else if (message.data != null) {
            PacketHandler.enqueueLogged(context, () -> message.handler.handleData(message.data, context), null,
                () -> "clientbound handler " + message.handlerId);
        }
        context.setPacketHandled(true);
    }
}
