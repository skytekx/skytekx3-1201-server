package com.blakebr0.mysticalautomation.network.payload;

import com.blakebr0.mysticalautomation.util.IFakeRecipeContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// SkyTekx port: a SimpleChannel message (NetworkHandler) for upstream's CustomPacketPayload and StreamCodec
public record SetFakeRecipeSlotPayload(int slot, ItemStack stack) {
    public static SetFakeRecipeSlotPayload read(FriendlyByteBuf buffer) {
        return new SetFakeRecipeSlotPayload(buffer.readVarInt(), buffer.readItem());
    }

    public static void write(SetFakeRecipeSlotPayload payload, FriendlyByteBuf buffer) {
        buffer.writeVarInt(payload.slot);
        buffer.writeItem(payload.stack);
    }

    public static void handleServer(SetFakeRecipeSlotPayload payload, Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
        if (player == null)
            return;

        var menu = player.containerMenu;

        if (payload.slot < menu.slots.size() && menu instanceof IFakeRecipeContainer fakeRecipeContainer) {
            var slot = menu.slots.get(payload.slot);

            fakeRecipeContainer.setFakeRecipeSlot(slot, payload.stack);
        }
    }
}
