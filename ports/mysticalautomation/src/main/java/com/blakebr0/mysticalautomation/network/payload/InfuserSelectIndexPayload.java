package com.blakebr0.mysticalautomation.network.payload;

import com.blakebr0.mysticalautomation.tileentity.InfuserTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// SkyTekx port: a SimpleChannel message (NetworkHandler) for upstream's CustomPacketPayload and StreamCodec
public record InfuserSelectIndexPayload(BlockPos pos, int index) {
    public static InfuserSelectIndexPayload read(FriendlyByteBuf buffer) {
        return new InfuserSelectIndexPayload(buffer.readBlockPos(), buffer.readVarInt());
    }

    public static void write(InfuserSelectIndexPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.pos);
        buffer.writeVarInt(payload.index);
    }

    public static void handleServer(InfuserSelectIndexPayload payload, Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
        if (player == null)
            return;

        var level = player.level();
        var tile = level.getBlockEntity(payload.pos);

        if (tile instanceof InfuserTileEntity machine) {
            var selectedIndex = machine.getSelectedIndex();
            if (selectedIndex == payload.index) {
                machine.setSelectedIndex(0);
            } else {
                machine.setSelectedIndex(payload.index);
            }
        }
    }
}
