package com.enderio.base.common.network;

import com.enderio.base.common.integrations.curios.CuriosCompat;
import com.enderio.base.common.item.tool.ElectromagnetItem;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.common.network.Packet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Optional;

/**
 * SkyTekx: the electromagnet toggle key (565ef4c3e, ServerboundToggleMagnetPacket). The packet carries nothing. The
 * server toggles the first electromagnet in a Curios slot, else the first one in the inventory, and shows the new
 * state in the action bar.
 */
public record C2SToggleMagnet() implements Packet {

    @Override
    public boolean isValid(NetworkEvent.Context context) {
        return context.getDirection() == NetworkDirection.PLAY_TO_SERVER && context.getSender() != null;
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        ItemStack magnet = findMagnet(player);
        if (magnet.getItem() instanceof ElectromagnetItem electromagnet) {
            boolean enabled = electromagnet.toggle(magnet);
            player.displayClientMessage(enabled ? EIOLang.ELECTROMAGNET_ON : EIOLang.ELECTROMAGNET_OFF, true);
        }
    }

    private static ItemStack findMagnet(ServerPlayer player) {
        List<ItemStack> curios = CuriosCompat.getActiveCurios(player, stack -> stack.getItem() instanceof ElectromagnetItem);
        if (!curios.isEmpty()) {
            return curios.get(0);
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.getItem() instanceof ElectromagnetItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public static class Handler extends Packet.PacketHandler<C2SToggleMagnet> {

        @Override
        public C2SToggleMagnet fromNetwork(FriendlyByteBuf buf) {
            return new C2SToggleMagnet();
        }

        @Override
        public void toNetwork(C2SToggleMagnet packet, FriendlyByteBuf buf) {
        }

        @Override
        public Optional<NetworkDirection> getDirection() {
            return Optional.of(NetworkDirection.PLAY_TO_SERVER);
        }
    }
}
