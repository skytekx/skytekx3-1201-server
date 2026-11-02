package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.components.entity.TravellersGogglesZoomAttachment;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Whether a player zooms with their Traveller's Goggles. The client sends it to the server whenever its zoom state
 * changes; the server checks the goggles, plays the zoom sound for everyone around and passes the state on to the players
 * tracking the zooming player, whose clients flip the goggles down. Backport of 4.8's GogglesZoomPacket (98ad4b10b,
 * 33ce94581, 76a760cc6).
 */
public record GogglesZoomPacket(boolean isUsingZoom, UUID playerUUID) {

	public GogglesZoomPacket(FriendlyByteBuf buf) {
		this(buf.readBoolean(), buf.readUUID());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(this.isUsingZoom());
		buf.writeUUID(this.playerUUID());
	}

	public static class Handler {

		public static boolean onMessage(GogglesZoomPacket message, Supplier<NetworkEvent.Context> ctx) {
			NetworkEvent.Context context = ctx.get();
			if (context.getDirection().getReceptionSide().isServer()) {
				// 4.8 looks the player up by the UUID in the packet; the sender is the only player a client may speak for
				ServerPlayer player = context.getSender();
				if (player != null)
					context.enqueueWork(() -> updateZoomState(player, message.isUsingZoom()));
			} else {
				context.enqueueWork(() -> {
					ClientLevel level = Minecraft.getInstance().level;
					Player player = level == null ? null : level.getPlayerByUUID(message.playerUUID());
					if (player != null)
						TravellersGogglesZoomAttachment.setUsingZoom(player, message.isUsingZoom());
				});
			}
			context.setPacketHandled(true);
			return true;
		}

		private static void updateZoomState(ServerPlayer player, boolean isUsingZoom) {
			// 4.8 drops a stop as well when the goggles no longer zoom (taken off or broken), which left the goggles
			// flipped down for the other players; only starting to zoom needs working goggles
			if (isUsingZoom && !TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ZOOM_ABILITY)
				|| TravellersGogglesZoomAttachment.isUsingZoom(player) == isUsingZoom)
				return;

			TravellersGogglesZoomAttachment.setUsingZoom(player, isUsingZoom);
			player.playSound(isUsingZoom ? TFSounds.GOGGLES_ZOOM_IN.get() : TFSounds.GOGGLES_ZOOM_OUT.get(), 1.0F, 1.0F);
			TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new GogglesZoomPacket(isUsingZoom, player.getUUID()));
		}
	}
}
