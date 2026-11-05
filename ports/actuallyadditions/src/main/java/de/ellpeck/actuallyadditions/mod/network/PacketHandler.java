/*
 * This file ("PacketHandler.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.network;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.network.gui.IButtonReactor;
import de.ellpeck.actuallyadditions.mod.network.gui.INumberReactor;
import de.ellpeck.actuallyadditions.mod.network.gui.IStringReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class PacketHandler {

    public static final List<IDataHandler> DATA_HANDLERS = new ArrayList<>();
    // SkyTekx: the four clientbound handlers hand their work to the client only PacketHandlerClient
    // through DistExecutor, as upstream 844efe42 and 8fd9afe8 split them. The base held the client
    // code in anonymous classes here, with @OnlyIn handleData methods that a dedicated server
    // strips
    public static final IDataHandler LASER_HANDLER = (compound, context) ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PacketHandlerClient.handleLaser(compound, context));
    public static final IDataHandler TILE_ENTITY_HANDLER = (compound, context) ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PacketHandlerClient.handleTileUpdate(compound, context));
    public static final IDataHandler LASER_PARTICLE_HANDLER = (compound, context) ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PacketHandlerClient.handleLaserParticle(compound, context));
    // SkyTekx: the four GUI handlers below are the only serverbound ones a client may name besides
    // PLAYER_DATA_TO_SERVER (see SERVERBOUND). The base took WorldID, X, Y, Z and PlayerID from the
    // packet, so a client could press buttons on any machine in any dimension as any player, load
    // chunks through getBlockEntity and throw a NullPointerException with an unknown WorldID or a
    // ClassCastException with a PlayerID of another entity. Upstream main still trusts all five
    // (ServerPayloadHandler.handleButtonToTile and its siblings), so this is a local deviation.
    // Now the sender is the only player, WorldID and PlayerID must name the sender's level and the
    // sender, and the block must sit in a loaded chunk within 8 blocks of the sender
    public static final IDataHandler GUI_BUTTON_TO_TILE_HANDLER = (compound, context) -> {
        ServerPlayer player = guiSender(compound, context, "button to tile");
        if (player != null && guiTarget(compound, player, "button to tile") instanceof IButtonReactor reactor) {
            reactor.onButtonPressed(compound.getInt("ButtonID"), player);
        }
    };
    public static final IDataHandler GUI_BUTTON_TO_CONTAINER_HANDLER = (compound, context) -> {
        ServerPlayer player = guiSender(compound, context, "button to container");
        if (player != null) {
            AbstractContainerMenu container = player.containerMenu;
            if (container instanceof IButtonReactor reactor && container.stillValid(player)) {
                reactor.onButtonPressed(compound.getInt("ButtonID"), player);
            }
        }
    };
    public static final IDataHandler GUI_NUMBER_TO_TILE_HANDLER = (compound, context) -> {
        ServerPlayer player = guiSender(compound, context, "number to tile");
        if (player != null && guiTarget(compound, player, "number to tile") instanceof INumberReactor reactor) {
            double number = compound.getDouble("Number");
            if (Double.isFinite(number)) {
                reactor.onNumberReceived(number, compound.getInt("NumberID"), player);
            } else {
                warnDropped(player, "number to tile", "the number is not finite");
            }
        }
    };
    public static final IDataHandler GUI_STRING_TO_TILE_HANDLER = (compound, context) -> {
        ServerPlayer player = guiSender(compound, context, "string to tile");
        if (player != null && guiTarget(compound, player, "string to tile") instanceof IStringReactor reactor) {
            reactor.onTextReceived(compound.getString("Text"), compound.getInt("TextID"), player);
        }
    };
    public static final IDataHandler SYNC_PLAYER_DATA = (compound, context) ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PacketHandlerClient.handlePlayerData(compound, context));
    // SkyTekx: a no op that keeps its index. The base let any client overwrite the bookmarks, the
    // tutorial flag and the trials of any online player named by UUID. Main reads nothing from
    // this packet any more (ServerPayloadHandler.handleSyncPlayer has every branch commented out,
    // c8e90aef drops the booklet data), and no client code sends it
    public static final IDataHandler PLAYER_DATA_TO_SERVER = (compound, context) -> {
        ServerPlayer player = context.getSender();
        if (player != null && compound.hasUUID("UUID") && !player.getUUID().equals(compound.getUUID("UUID"))) {
            warnDropped(player, "player data", "it names the UUID " + compound.getUUID("UUID") + " and not the sender");
        }
    };

    // SkyTekx: DATA_HANDLERS stays the one index list of the wire format, so the indices of the
    // base do not change. These sets say which side may receive each handler. The base decoded any
    // index in both directions, so a client could name a clientbound handler: on a dedicated
    // server its stripped @OnlyIn handleData threw an AbstractMethodError, on a LAN host it ran
    // client code with the guest's NBT. Handlers that later changes add go through
    // addServerbound or addClientbound
    private static final Set<IDataHandler> SERVERBOUND = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Set<IDataHandler> CLIENTBOUND = Collections.newSetFromMap(new IdentityHashMap<>());

    // SkyTekx: the reach of TileEntityBase.canPlayerUse, which every machine menu checks in
    // stillValid. A packet for a block farther away cannot come from an open screen of it
    private static final double MAX_GUI_DISTANCE_SQ = 64.0D;

    // SkyTekx: 5 since the packet hardening, so that a client of the base (4) is refused at login
    private static final String PROTOCOL_VERSION = Integer.toString(5);
    public static final SimpleChannel THE_NETWORK = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ActuallyAdditions.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void init() {
        THE_NETWORK.messageBuilder(PacketServerToClient.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .decoder(PacketServerToClient::fromBytes)
            .encoder(PacketServerToClient::toBytes)
            .consumerNetworkThread(PacketServerToClient::handle).add();

        THE_NETWORK.messageBuilder(PacketClientToServer.class, 1, NetworkDirection.PLAY_TO_SERVER)
            .decoder(PacketClientToServer::fromBytes)
            .encoder(PacketClientToServer::toBytes)
            .consumerNetworkThread(PacketClientToServer::handle).add();


        // The order is the wire format: indices 0 to 8 as on the base
        addClientbound(LASER_HANDLER);
        addClientbound(TILE_ENTITY_HANDLER);
        addServerbound(GUI_BUTTON_TO_TILE_HANDLER);
        addServerbound(GUI_STRING_TO_TILE_HANDLER);
        addServerbound(GUI_NUMBER_TO_TILE_HANDLER);
        addClientbound(SYNC_PLAYER_DATA);
        addServerbound(GUI_BUTTON_TO_CONTAINER_HANDLER);
        addClientbound(LASER_PARTICLE_HANDLER);
        addServerbound(PLAYER_DATA_TO_SERVER);
    }

    /**
     * Appends a handler that clients may send to the server.
     */
    public static void addServerbound(IDataHandler handler) {
        DATA_HANDLERS.add(handler);
        SERVERBOUND.add(handler);
    }

    /**
     * Appends a handler that the server may send to clients.
     */
    public static void addClientbound(IDataHandler handler) {
        DATA_HANDLERS.add(handler);
        CLIENTBOUND.add(handler);
    }

    /**
     * @return the serverbound handler with this index, or null for an unknown or clientbound index
     */
    @Nullable
    public static IDataHandler serverboundHandler(int index) {
        IDataHandler handler = index >= 0 && index < DATA_HANDLERS.size() ? DATA_HANDLERS.get(index) : null;
        return SERVERBOUND.contains(handler) ? handler : null;
    }

    /**
     * @return the clientbound handler with this index, or null for an unknown or serverbound index
     */
    @Nullable
    public static IDataHandler clientboundHandler(int index) {
        IDataHandler handler = index >= 0 && index < DATA_HANDLERS.size() ? DATA_HANDLERS.get(index) : null;
        return CLIENTBOUND.contains(handler) ? handler : null;
    }

    /**
     * Runs the work of a received packet on the main thread and logs a WARN if it throws. The
     * base enqueued the work without looking at the returned future, so a failing handler left no
     * trace. Upstream main disconnects the player instead.
     */
    static void enqueueLogged(NetworkEvent.Context context, Runnable work, Supplier<String> what) {
        CompletableFuture<Void> future;
        try {
            future = context.enqueueWork(work);
        } catch (RuntimeException e) {
            future = CompletableFuture.failedFuture(e);
        }
        future.exceptionally(e -> {
            ActuallyAdditions.LOGGER.warn("The {} failed: {}", what.get(), e.toString());
            return null;
        });
    }

    /**
     * Logs one WARN line for a dropped serverbound packet, naming the player who sent it.
     */
    static void warnDropped(@Nullable ServerPlayer player, String packet, String reason) {
        ActuallyAdditions.LOGGER.warn("Dropped the {} packet of {}: {}", packet, describe(player), reason);
    }

    static String describe(@Nullable ServerPlayer player) {
        return player == null
            ? "an unknown sender"
            : player.getGameProfile().getName() + " (" + player.getStringUUID() + ")";
    }

    /**
     * @return the sender of a GUI packet, or null if the packet must be dropped
     */
    @Nullable
    private static ServerPlayer guiSender(CompoundTag compound, NetworkEvent.Context context, String packet) {
        ServerPlayer player = context.getSender();
        if (player == null || player.isSpectator()) {
            // A spectator may look at a machine screen but not use it, as vanilla's container clicks
            return null;
        }
        if (compound.contains("PlayerID", Tag.TAG_ANY_NUMERIC) && compound.getInt("PlayerID") != player.getId()) {
            warnDropped(player, packet, "it names the entity id " + compound.getInt("PlayerID") + " and not the sender's " + player.getId());
            return null;
        }
        if (compound.contains("WorldID", Tag.TAG_STRING)) {
            ResourceLocation dimension = ResourceLocation.tryParse(compound.getString("WorldID"));
            if (!player.level().dimension().location().equals(dimension)) {
                warnDropped(player, packet, "it names the dimension " + quote(compound.getString("WorldID")) + " and the sender is in " + player.level().dimension().location());
                return null;
            }
        }
        return player;
    }

    /**
     * @return the block entity a GUI packet targets in the sender's level, or null if the packet
     * must be dropped. Checks the distance before it touches the chunk, so a packet cannot load
     * or generate chunks
     */
    @Nullable
    private static BlockEntity guiTarget(CompoundTag compound, ServerPlayer player, String packet) {
        BlockPos pos = new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z"));
        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) > MAX_GUI_DISTANCE_SQ) {
            warnDropped(player, packet, "it targets " + pos.toShortString() + ", more than 8 blocks away from the sender");
            return null;
        }
        Level level = player.level();
        if (!level.isLoaded(pos)) {
            warnDropped(player, packet, "it targets " + pos.toShortString() + ", which is not loaded");
            return null;
        }
        return level.getBlockEntity(pos);
    }

    private static String quote(String text) {
        String shortened = text.length() > 64 ? text.substring(0, 64) + "..." : text;
        return "'" + shortened.replaceAll("\\p{Cntrl}", "?") + "'";
    }

    public static void sendTo(Object msg, ServerPlayer player) {
        if (!(player instanceof FakePlayer)) {
            THE_NETWORK.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public static void sendToServer(Object msg) {
        THE_NETWORK.sendToServer(msg);
    }

    public static void send(Object msg, PacketDistributor.PacketTarget target) {
        THE_NETWORK.send(target, msg);
    }
}
