package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.client.TravellersClientEvents;
import twilightforest.components.entity.TravellersWingsAttachment;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Whether a player glides gradually with the Gradual Glide modifier of the Traveller's Wings. The client decides it
 * from the sneak key and tells the server, which passes it on to the players tracking that player; the server also
 * sends {@code false} to everyone once the modifier stops working. Backport of 4.8's GradualGlidePacket (485d7e0a7).
 * <p>
 * Unlike 4.8, the server ignores the player id a client sends and only ever changes the sender's own state.
 */
public record GradualGlidePacket(boolean isGraduallyGliding, UUID playerUUID) {

	public GradualGlidePacket(FriendlyByteBuf buf) {
		this(buf.readBoolean(), buf.readUUID());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(this.isGraduallyGliding());
		buf.writeUUID(this.playerUUID());
	}

	public static class Handler {

		public static boolean onMessage(GradualGlidePacket message, Supplier<NetworkEvent.Context> ctx) {
			if (ctx.get().getDirection().getReceptionSide().isServer()) {
				ServerPlayer player = ctx.get().getSender();
				if (player != null) {
					ctx.get().enqueueWork(() -> {
						TravellersWingsAttachment attachment = TravellersWingsAttachment.get(player);
						if (attachment == null)
							return;
						attachment.isGraduallyGliding = message.isGraduallyGliding();
						TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new GradualGlidePacket(message.isGraduallyGliding(), player.getUUID()));
					});
				}
			} else {
				ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TravellersClientEvents.setGraduallyGliding(message.playerUUID(), message.isGraduallyGliding())));
			}

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
