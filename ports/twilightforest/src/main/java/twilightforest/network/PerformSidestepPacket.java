package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.item.travellers_gear.TravellersGearLogic;

import java.util.function.Supplier;

/**
 * Sent by the client when its player performed a Sidestep (Traveller's Wings modifier) to the left or the right. The
 * server repeats it for its own state (cooldown, sound for the others, wing animation) and counts requests it has to
 * reject. Backport of 4.8's PerformSidestepPacket (485d7e0a7).
 */
public record PerformSidestepPacket(boolean isLeftStepSide) {

	public PerformSidestepPacket(FriendlyByteBuf buf) {
		this(buf.readBoolean());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(this.isLeftStepSide());
	}

	public static class Handler {

		public static boolean onMessage(PerformSidestepPacket message, Supplier<NetworkEvent.Context> ctx) {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				ctx.get().enqueueWork(() -> {
					if (!TravellersGearLogic.tryPerformSidestep(player, message.isLeftStepSide()))
						TravellersGearLogic.handleSidestepAbuse(player);
				});
			}

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
