package com.enderio.machines.common.network;

import com.enderio.machines.common.blockentity.VatBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;

import java.util.Optional;

/**
 * Voids the output tank of a Vat. Sent by the Vat screen (1cb2e1420).
 */
public class VatDumpTankPacket extends VatTankPacket {

    public VatDumpTankPacket(BlockPos pos) {
        super(pos);
    }

    @Override
    protected void apply(VatBlockEntity vat) {
        vat.dumpOutputTank();
    }

    public static class Handler extends PacketHandler<VatDumpTankPacket> {

        @Override
        public VatDumpTankPacket fromNetwork(FriendlyByteBuf buf) {
            return new VatDumpTankPacket(buf.readBlockPos());
        }

        @Override
        public void toNetwork(VatDumpTankPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.getPos());
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_SERVER);
        }
    }
}
