package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.item.travellers_gear.TravellersArmorBeltItem;

import java.util.function.Supplier;

/**
 * Sent by the client when the Swap Hotbar key is pressed; the server swaps the player's hotbar with the one stored in
 * their belt or wings. Backport of 4.8's SwapHotbarPacket. 4.8 then calls ServerPlayer#broadcastToPlayer, which only
 * answers whether the player can see themselves; the inventory syncs with the open menu every tick anyway.
 */
public class SwapHotbarPacket {
	public static final SwapHotbarPacket INSTANCE = new SwapHotbarPacket();

	private SwapHotbarPacket() {
	}

	@SuppressWarnings("unused")
	public static SwapHotbarPacket decode(FriendlyByteBuf buf) {
		return INSTANCE;
	}

	@SuppressWarnings("unused")
	public void encode(FriendlyByteBuf buf) {
	}

	public static class Handler {

		public static boolean onMessage(SwapHotbarPacket message, Supplier<NetworkEvent.Context> ctx) {
			ServerPlayer player = ctx.get().getSender();
			if (player != null)
				ctx.get().enqueueWork(() -> TravellersArmorBeltItem.travellersTrySwapHotbar(player));
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
