package com.enderio.core.common.network;

import com.enderio.core.common.blockentity.EnderBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class C2SDataSlotChange implements Packet {

    private final BlockPos pos;

    // You shouldn't really send null, but its "technically" valid.
    @Nullable
    private final FriendlyByteBuf updateData;

    public C2SDataSlotChange(BlockPos pos, FriendlyByteBuf updateData) {
        this.pos = pos;
        this.updateData = updateData;
    }

    public C2SDataSlotChange(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        updateData = new FriendlyByteBuf(buf.copy());
    }

    @Override
    public boolean isValid(NetworkEvent.Context context) {
        return context.getSender() != null && updateData != null;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || updateData == null) {
            return;
        }

        // Only a block in reach, and never one in an unloaded chunk.
        if (!Packet.canAccessBlock(player, pos)) {
            Packet.logPacketError(context, "changes a data slot at " + pos.toShortString() + ", which is out of reach or not loaded", this);
            return;
        }

        BlockEntity be = player.serverLevel().getBlockEntity(pos);
        if (be instanceof EnderBlockEntity enderBlockEntity) {
            try {
                enderBlockEntity.serverHandleBufferChange(updateData);
            } catch (RuntimeException e) {
                Packet.logPacketError(context, "invalid data slot change at " + pos.toShortString() + ": " + e.getMessage(), this);
            }
        }
    }

    protected void write(FriendlyByteBuf writeInto) {
        writeInto.writeBlockPos(pos);
        writeInto.writeBytes(updateData);
    }

    public static class Handler extends PacketHandler<C2SDataSlotChange> {

        @Override
        public C2SDataSlotChange fromNetwork(FriendlyByteBuf buf) {
            return new C2SDataSlotChange(buf);
        }

        @Override
        public void toNetwork(C2SDataSlotChange packet, FriendlyByteBuf buf) {
            packet.write(buf);
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_SERVER);
        }
    }
}