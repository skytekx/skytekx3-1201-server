/*
 * This file ("PacketHelperClient.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * SkyTekx: the client half of the base's PacketHandlerHelper, renamed as upstream 8fd9afe8 does.
 * The server half (syncPlayerData) is PacketHelperServer. Only screens and other client code
 * call these methods.
 */
@OnlyIn(Dist.CLIENT)
public final class PacketHelperClient {

    @OnlyIn(Dist.CLIENT)
    public static void sendButtonPacket(BlockEntity tile, int buttonId) {
        CompoundTag compound = new CompoundTag();
        BlockPos pos = tile.getBlockPos();
        compound.putInt("X", pos.getX());
        compound.putInt("Y", pos.getY());
        compound.putInt("Z", pos.getZ());
        compound.putString("WorldID", tile.getLevel().dimension().location().toString());
        compound.putInt("PlayerID", Minecraft.getInstance().player.getId());
        compound.putInt("ButtonID", buttonId);
        PacketHandler.THE_NETWORK.sendToServer(new PacketClientToServer(compound, PacketHandler.GUI_BUTTON_TO_TILE_HANDLER));
    }

    /**
     * SkyTekx: a button of the open menu, such as the sack buttons of 85725a0f, which build this
     * packet in each screen on main. The server presses it on the sender's open menu while that
     * menu is still valid (PacketHandler.GUI_BUTTON_TO_CONTAINER_HANDLER).
     */
    @OnlyIn(Dist.CLIENT)
    public static void sendContainerButtonPacket(int buttonId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        CompoundTag compound = new CompoundTag();
        compound.putInt("ButtonID", buttonId);
        compound.putInt("PlayerID", mc.player.getId());
        compound.putString("WorldID", mc.level.dimension().location().toString());
        PacketHandler.THE_NETWORK.sendToServer(new PacketClientToServer(compound, PacketHandler.GUI_BUTTON_TO_CONTAINER_HANDLER));
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendPlayerDataToServer(boolean log, int type) {
        CompoundTag compound = new CompoundTag();
        compound.putBoolean("Log", log);
        compound.putInt("Type", type);

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            compound.putString("World", player.level().dimension().location().toString());
            compound.putUUID("UUID", player.getUUID());

            // SkyTekx: no booklet data any more (upstream c8e90aef), and the server ignores the packet
            PacketHandler.THE_NETWORK.sendToServer(new PacketClientToServer(compound, PacketHandler.PLAYER_DATA_TO_SERVER));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void sendNumberPacket(BlockEntity tile, double number, int id) {
        CompoundTag compound = new CompoundTag();
        compound.putInt("X", tile.getBlockPos().getX());
        compound.putInt("Y", tile.getBlockPos().getY());
        compound.putInt("Z", tile.getBlockPos().getZ());
        compound.putString("WorldID", tile.getLevel().dimension().location().toString());
        compound.putInt("PlayerID", Minecraft.getInstance().player.getId());
        compound.putInt("NumberID", id);
        compound.putDouble("Number", number);
        PacketHandler.THE_NETWORK.sendToServer(new PacketClientToServer(compound, PacketHandler.GUI_NUMBER_TO_TILE_HANDLER));
    }
}
