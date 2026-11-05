package com.enderio.machines.common.network;

import com.enderio.machines.common.blockentity.VatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;

import java.util.Optional;

/**
 * Moves the input tank of a Vat into its empty output tank. Sent by the Vat screen (1cb2e1420).
 */
public class VatMoveTankPacket extends VatTankPacket {

    public VatMoveTankPacket(BlockPos pos) {
        super(pos);
    }

    @Override
    protected void apply(VatBlockEntity vat) {
        vat.moveFluidToOutputTank();
    }

    public static class Handler extends PacketHandler<VatMoveTankPacket> {

        @Override
        public VatMoveTankPacket fromNetwork(FriendlyByteBuf buf) {
            return new VatMoveTankPacket(buf.readBlockPos());
        }

        @Override
        public void toNetwork(VatMoveTankPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.getPos());
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_SERVER);
        }
    }
}
