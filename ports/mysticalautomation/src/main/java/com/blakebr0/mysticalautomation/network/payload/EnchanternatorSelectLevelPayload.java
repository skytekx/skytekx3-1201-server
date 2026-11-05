package com.blakebr0.mysticalautomation.network.payload;

import com.blakebr0.mysticalautomation.tileentity.EnchanternatorTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// SkyTekx port: a SimpleChannel message (NetworkHandler) for upstream's CustomPacketPayload and StreamCodec
public record EnchanternatorSelectLevelPayload(BlockPos pos, int level) {
    public static EnchanternatorSelectLevelPayload read(FriendlyByteBuf buffer) {
        return new EnchanternatorSelectLevelPayload(buffer.readBlockPos(), buffer.readVarInt());
    }

    public static void write(EnchanternatorSelectLevelPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.pos);
        buffer.writeVarInt(payload.level);
    }

    public static void handleServer(EnchanternatorSelectLevelPayload payload, Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
        if (player == null)
            return;

        var level = player.level();
        var tile = level.getBlockEntity(payload.pos);

        if (tile instanceof EnchanternatorTileEntity machine) {
            var selectedLevel = machine.getSelectedLevel();
            if (selectedLevel == payload.level) {
                machine.setSelectedLevel(0);
            } else {
                machine.setSelectedLevel(payload.level);
            }
        }
    }
}
