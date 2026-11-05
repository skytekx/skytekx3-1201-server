package com.enderio.machines.common.network;

import com.enderio.core.common.network.Packet;
import com.enderio.machines.common.blockentity.VatBlockEntity;
import com.enderio.machines.common.menu.VatMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * A button of the Vat screen that acts on the tanks of the Vat at a position. Validated like the hardened 6.x client
 * packets (053cc7a, c6a76d2): the block must be loaded and in reach, and the player must have this Vat's menu open.
 */
public abstract class VatTankPacket implements Packet {

    protected final BlockPos pos;

    protected VatTankPacket(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public boolean isValid(NetworkEvent.Context context) {
        return context.getSender() != null;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) {
            return;
        }

        if (!Packet.canAccessBlock(player, pos)) {
            Packet.logPacketError(context, "acts on a Vat at " + pos.toShortString() + ", which is out of reach or not loaded", this);
            return;
        }

        if (!(player.serverLevel().getBlockEntity(pos) instanceof VatBlockEntity vat)
            || !(player.containerMenu instanceof VatMenu menu)
            || menu.getBlockEntity() != vat) {
            Packet.logPacketError(context, "acts on a Vat at " + pos.toShortString() + " without its menu open", this);
            return;
        }

        apply(vat);
    }

    protected abstract void apply(VatBlockEntity vat);
}
