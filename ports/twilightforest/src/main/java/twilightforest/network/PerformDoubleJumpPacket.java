package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.item.travellers_gear.TravellersGearLogic;

import java.util.function.Supplier;

/**
 * Sent by the client when its player performed a Double Jump (Traveller's Wings modifier). The jump itself is client
 * side movement; the server repeats it for its own state (sound for the others, particles, wing animation, fall
 * distance) and counts requests it has to reject. Backport of 4.8's PerformDoubleJumpPacket (485d7e0a7).
 */
public record PerformDoubleJumpPacket() {

	public PerformDoubleJumpPacket(FriendlyByteBuf buf) {
		this();
	}

	public void encode(FriendlyByteBuf buf) {
	}

	public static class Handler {

		public static boolean onMessage(PerformDoubleJumpPacket message, Supplier<NetworkEvent.Context> ctx) {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				ctx.get().enqueueWork(() -> {
					if (!TravellersGearLogic.performDoubleJump(player))
						TravellersGearLogic.handleDoubleJumpAbuse(player);
				});
			}

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
