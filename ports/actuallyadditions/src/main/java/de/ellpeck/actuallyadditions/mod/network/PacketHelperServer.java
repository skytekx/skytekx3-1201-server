/*
 * This file ("PacketHelperServer.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.network;

import de.ellpeck.actuallyadditions.mod.data.PlayerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

/**
 * A helper class for sending packets from the server to the client.
 * <p>
 * SkyTekx: the server half of the base's PacketHandlerHelper (upstream 8fd9afe8) and the laser
 * packet that the base sent from AssetUtil, the client render helper class (upstream main keeps
 * spawnLaserWithTimeServer here too).
 */
public final class PacketHelperServer {

    public static void syncPlayerData(Player player, boolean log) {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("Log", log);

        CompoundTag data = new CompoundTag();
        PlayerData.getDataFromPlayer(player).writeToNBT(data, false);
        compound.put("Data", data);

        if (player instanceof ServerPlayer) {
            PacketHandler.THE_NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new PacketServerToClient(compound, PacketHandler.SYNC_PLAYER_DATA));
        }
    }

    public static void spawnLaserWithTimeServer(Level world, double startX, double startY, double startZ, double endX, double endY, double endZ, int color, int maxAge, double rotationTime, float size, float alpha) {
        if (!world.isClientSide) {
            CompoundTag data = new CompoundTag();
            data.putDouble("StartX", startX);
            data.putDouble("StartY", startY);
            data.putDouble("StartZ", startZ);
            data.putDouble("EndX", endX);
            data.putDouble("EndY", endY);
            data.putDouble("EndZ", endZ);
            data.putInt("Color", color);
            data.putDouble("RotationTime", rotationTime);
            data.putFloat("Size", size);
            data.putInt("MaxAge", maxAge);
            data.putFloat("Alpha", alpha);
            PacketHandler.THE_NETWORK.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(startX, startY, startZ, 96, world.dimension())), new PacketServerToClient(data, PacketHandler.LASER_HANDLER));
        }
    }
}
