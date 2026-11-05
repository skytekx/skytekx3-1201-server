package com.blakebr0.mysticalautomation.network.payload;

import com.blakebr0.mysticalautomation.client.ClientPayloadHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * SkyTekx port: Minecraft 1.20.1 writes the count of a slot stack as a byte (FriendlyByteBuf#writeItemStack), 1.21
 * as a VarInt. The Infuser and the Enchanternator hold up to 512 items per input slot, which a byte cannot carry.
 * After each vanilla slot update with more than 127 items, this message sends the slot again with its full count.
 */
public record SyncSlotCountPayload(int containerId, int slot, ItemStack stack, int count) {
    public static SyncSlotCountPayload read(FriendlyByteBuf buffer) {
        return new SyncSlotCountPayload(buffer.readByte(), buffer.readVarInt(), buffer.readItem(), buffer.readVarInt());
    }

    public static void write(SyncSlotCountPayload payload, FriendlyByteBuf buffer) {
        buffer.writeByte(payload.containerId);
        buffer.writeVarInt(payload.slot);
        buffer.writeItem(payload.stack.copyWithCount(1));
        buffer.writeVarInt(payload.count);
    }

    public static void handleClient(SyncSlotCountPayload payload, Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPayloadHandlers.syncSlotCount(payload));
    }
}
