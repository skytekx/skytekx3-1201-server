package com.blakebr0.mysticalautomation.container;

import com.blakebr0.mysticalautomation.network.NetworkHandler;
import com.blakebr0.mysticalautomation.network.payload.SyncSlotCountPayload;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

/**
 * SkyTekx port: wraps the vanilla synchronizer of a server side menu. Vanilla 1.20.1 sends slot stacks with a byte
 * count, so after each slot it sends with more than 127 items this sends the full count in a
 * {@link SyncSlotCountPayload}. The client applies both in order, the full count last.
 */
public record LargeStackSynchronizer(ContainerSynchronizer delegate, ServerPlayer player) implements ContainerSynchronizer {
    public static ContainerSynchronizer wrap(ContainerSynchronizer synchronizer, Player player) {
        if (player instanceof ServerPlayer serverPlayer && !(synchronizer instanceof LargeStackSynchronizer)) {
            return new LargeStackSynchronizer(synchronizer, serverPlayer);
        }

        return synchronizer;
    }

    @Override
    public void sendInitialData(AbstractContainerMenu menu, NonNullList<ItemStack> items, ItemStack carried, int[] dataSlots) {
        this.delegate.sendInitialData(menu, items, carried, dataSlots);

        for (int i = 0; i < items.size(); i++) {
            this.sendCount(menu, i, items.get(i));
        }
    }

    @Override
    public void sendSlotChange(AbstractContainerMenu menu, int slot, ItemStack stack) {
        this.delegate.sendSlotChange(menu, slot, stack);
        this.sendCount(menu, slot, stack);
    }

    @Override
    public void sendCarriedChange(AbstractContainerMenu menu, ItemStack stack) {
        this.delegate.sendCarriedChange(menu, stack);
    }

    @Override
    public void sendDataChange(AbstractContainerMenu menu, int index, int value) {
        this.delegate.sendDataChange(menu, index, value);
    }

    private void sendCount(AbstractContainerMenu menu, int slot, ItemStack stack) {
        if (stack.getCount() > Byte.MAX_VALUE) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(this::player), new SyncSlotCountPayload(menu.containerId, slot, stack, stack.getCount()));
        }
    }
}
