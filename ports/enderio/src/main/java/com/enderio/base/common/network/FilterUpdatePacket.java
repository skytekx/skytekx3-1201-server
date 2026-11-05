package com.enderio.base.common.network;

import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.menu.AbstractFilterMenu;
import com.enderio.core.common.capability.IFilterCapability;
import com.enderio.core.common.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

public record FilterUpdatePacket(boolean nbt, boolean inverted) implements Packet {

    public FilterUpdatePacket(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public boolean isValid(NetworkEvent.Context context) {
        return context.getSender() != null;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        // The filter of the open filter menu, which is the main hand stack or a filter in a conduit slot (Ender IO 8.x
        // 04e69b8e6). Before, this always changed the main hand, also without an open menu, and it would have changed the main
        // hand instead of a filter opened from a conduit.
        ServerPlayer player = context.getSender();
        if (player == null || !(player.containerMenu instanceof AbstractFilterMenu filterMenu) || !filterMenu.stillValid(player)) {
            return;
        }

        ItemStack filterStack = filterMenu.getFilterStack();
        filterStack.getCapability(EIOCapabilities.FILTER)
            .ifPresent(filter -> {
                if (filter instanceof IFilterCapability<?> capability) {
                    capability.setNbt(nbt);
                    capability.setInverted(inverted);
                }
            });
    }

    protected void write(FriendlyByteBuf writeInto) {
        writeInto.writeBoolean(nbt);
        writeInto.writeBoolean(inverted);
    }

    public static class Handler extends Packet.PacketHandler<FilterUpdatePacket> {

        @Override
        public FilterUpdatePacket fromNetwork(FriendlyByteBuf buf) {
            return new FilterUpdatePacket(buf);
        }

        @Override
        public void toNetwork(FilterUpdatePacket packet, FriendlyByteBuf buf) {
            packet.write(buf);
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_SERVER);
        }
    }
}
