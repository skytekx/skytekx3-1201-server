package com.blakebr0.mysticalautomation.network.payload;

import com.blakebr0.mysticalautomation.tileentity.InfuserTileEntity;
import com.blakebr0.mysticalautomation.container.InfuserContainer;
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

        // SkyTekx: upstream changes the machine at any position the client names, and getBlockEntity loads its chunk
        // first. Only the machine whose menu the player has open, and may still use, takes the change
        if (!(player.containerMenu instanceof InfuserContainer menu) || !menu.getBlockPos().equals(payload.pos) || !menu.stillValid(player))
            return;

        var level = player.level();
        if (!level.isLoaded(payload.pos))
            return;

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
