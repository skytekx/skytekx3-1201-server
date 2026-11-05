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
import de.ellpeck.actuallyadditions.mod.data.PlayerData;
import de.ellpeck.actuallyadditions.mod.data.WorldData;
import de.ellpeck.actuallyadditions.mod.network.gui.IButtonReactor;
import de.ellpeck.actuallyadditions.mod.network.gui.INumberReactor;
import de.ellpeck.actuallyadditions.mod.network.gui.IStringReactor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.ArrayList;
import java.util.List;

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
    public static final IDataHandler GUI_BUTTON_TO_TILE_HANDLER = (compound, context) -> {
        Level world = context.getSender().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString("WorldID"))));
        BlockEntity tile = world.getBlockEntity(new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z")));

        if (tile instanceof IButtonReactor reactor) {
	        Entity entity = world.getEntity(compound.getInt("PlayerID"));
            if (entity instanceof Player) {
                reactor.onButtonPressed(compound.getInt("ButtonID"), (Player) entity);
            }
        }
    };
    public static final IDataHandler GUI_BUTTON_TO_CONTAINER_HANDLER = (compound, context) -> {
        Level world = context.getSender().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString("WorldID"))));
        Entity entity = world.getEntity(compound.getInt("PlayerID"));
        if (entity instanceof Player) {
            AbstractContainerMenu container = ((Player) entity).containerMenu;
            if (container instanceof IButtonReactor) {
                ((IButtonReactor) container).onButtonPressed(compound.getInt("ButtonID"), (Player) entity);
            }
        }
    };
    public static final IDataHandler GUI_NUMBER_TO_TILE_HANDLER = (compound, context) -> {
        Level world = context.getSender().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString("WorldID"))));
        BlockEntity tile = world.getBlockEntity(new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z")));

        if (tile instanceof INumberReactor reactor) {
	        reactor.onNumberReceived(compound.getDouble("Number"), compound.getInt("NumberID"), (Player) world.getEntity(compound.getInt("PlayerID")));
        }
    };
    public static final IDataHandler GUI_STRING_TO_TILE_HANDLER = (compound, context) -> {
        Level world = context.getSender().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString("WorldID"))));
        BlockEntity tile = world.getBlockEntity(new BlockPos(compound.getInt("X"), compound.getInt("Y"), compound.getInt("Z")));

        if (tile instanceof IStringReactor reactor) {
	        reactor.onTextReceived(compound.getString("Text"), compound.getInt("TextID"), (Player) world.getEntity(compound.getInt("PlayerID")));
        }
    };
    public static final IDataHandler SYNC_PLAYER_DATA = (compound, context) ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PacketHandlerClient.handlePlayerData(compound, context));
    public static final IDataHandler PLAYER_DATA_TO_SERVER = (compound, context) -> {
        Level world = context.getSender().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString("World"))));
        Player player = world.getServer().getPlayerList().getPlayer(compound.getUUID("UUID"));
        if (player != null) {
            PlayerData.PlayerSave data = PlayerData.getDataFromPlayer(player);

            int type = compound.getInt("Type");
            if (type == 0) {
                data.loadBookmarks(compound.getList("Bookmarks", 8));
            } else if (type == 1) {
                data.didBookTutorial = compound.getBoolean("DidBookTutorial");
            } else if (type == 2) {
                data.loadTrials(compound.getList("Trials", 8));

                if (compound.getBoolean("Achievement")) {
                    //TheAchievements.COMPLETE_TRIALS.get(player);
                }
            }
            WorldData.get(world).setDirty();

            if (compound.getBoolean("Log")) {
                ActuallyAdditions.LOGGER.info("Receiving changed Player Data for player " + player.getName() + ".");
            }
        } else {
            ActuallyAdditions.LOGGER.error("Tried to receive Player Data for UUID " + compound.getUUID("UUID") + ", but he doesn't seem to be present!");
        }
    };

    private static final String PROTOCOL_VERSION = Integer.toString(4);
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


        DATA_HANDLERS.add(LASER_HANDLER);
        DATA_HANDLERS.add(TILE_ENTITY_HANDLER);
        DATA_HANDLERS.add(GUI_BUTTON_TO_TILE_HANDLER);
        DATA_HANDLERS.add(GUI_STRING_TO_TILE_HANDLER);
        DATA_HANDLERS.add(GUI_NUMBER_TO_TILE_HANDLER);
        DATA_HANDLERS.add(SYNC_PLAYER_DATA);
        DATA_HANDLERS.add(GUI_BUTTON_TO_CONTAINER_HANDLER);
        DATA_HANDLERS.add(LASER_PARTICLE_HANDLER);
        DATA_HANDLERS.add(PLAYER_DATA_TO_SERVER);
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
