package com.blakebr0.mysticalautomation.network.payload;

import com.blakebr0.mysticalautomation.container.slot.FakeSlot;
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

        // SkyTekx: upstream 9777e82 checks only the upper bound of the index, and any slot of the open menu could be
        // set, a player inventory or machine input slot too. A modified client could create items that way, so only
        // the ghost slots of a menu the player may still use take the stack
        if (payload.slot >= 0 && payload.slot < menu.slots.size() && menu.stillValid(player) && menu instanceof IFakeRecipeContainer fakeRecipeContainer) {
            var slot = menu.slots.get(payload.slot);

            if (slot instanceof FakeSlot) {
                fakeRecipeContainer.setFakeRecipeSlot(slot, payload.stack);
            }
        }
    }
}
