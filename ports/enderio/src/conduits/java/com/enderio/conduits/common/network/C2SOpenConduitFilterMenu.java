package com.enderio.conduits.common.network;

import com.enderio.api.conduit.ConduitType;
import com.enderio.api.conduit.SlotType;
import com.enderio.base.common.menu.AbstractFilterMenu;
import com.enderio.conduits.common.conduit.block.ConduitBlockEntity;
import com.enderio.conduits.common.menu.ConduitFilterAccess;
import com.enderio.conduits.common.menu.ConduitMenu;
import com.enderio.core.common.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;

/**
 * Open the menu of the filter in a slot of the connection that the open conduit menu shows, from
 * ServerboundOpenConduitFilterMenu of Ender IO 8.x (04e69b8e6). Like upstream the client names only its container and the slot.
 * The conduit, the conduit type and the side come from the server's conduit menu.
 */
public record C2SOpenConduitFilterMenu(int containerId, SlotType slotType) implements Packet {

    public C2SOpenConduitFilterMenu(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readEnum(SlotType.class));
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

        if (!(player.containerMenu instanceof ConduitMenu conduitMenu) || conduitMenu.containerId != containerId || !conduitMenu.stillValid(player)) {
            return;
        }

        ConduitBlockEntity conduit = conduitMenu.getBlockEntity();
        ConduitType<?> conduitType = conduitMenu.getConduitType();
        if (conduit == null || conduitType == null) {
            return;
        }

        // Only a conduit in reach, and never one in an unloaded chunk.
        if (!Packet.canAccessBlock(player, conduit.getBlockPos())) {
            Packet.logPacketError(context, "opens a filter of a conduit at " + conduit.getBlockPos().toShortString() + ", which is out of reach or not loaded", this);
            return;
        }

        // The conduit type must be in the bundle, the slot must belong to it and hold a filter with a menu.
        Optional<ConduitFilterAccess> access = ConduitFilterAccess.of(conduit, conduitType, conduitMenu.getDirection(), slotType);
        if (access.isEmpty()) {
            return;
        }

        AbstractFilterMenu.open(player, access.get());
    }

    protected void write(FriendlyByteBuf writeInto) {
        writeInto.writeVarInt(containerId);
        writeInto.writeEnum(slotType);
    }

    public static class Handler extends Packet.PacketHandler<C2SOpenConduitFilterMenu> {

        @Override
        public C2SOpenConduitFilterMenu fromNetwork(FriendlyByteBuf buf) {
            return new C2SOpenConduitFilterMenu(buf);
        }

        @Override
        public void toNetwork(C2SOpenConduitFilterMenu packet, FriendlyByteBuf buf) {
            packet.write(buf);
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_SERVER);
        }
    }
}
