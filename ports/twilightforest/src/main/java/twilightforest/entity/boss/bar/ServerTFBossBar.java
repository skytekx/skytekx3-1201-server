package twilightforest.entity.boss.bar;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.network.TFBossBarPacket;
import twilightforest.network.TFPacketHandler;

/**
 * A boss bar with an RGB colour instead of one of the seven vanilla {@link BossBarColor}s (backported from TF 4.8).
 * <p>
 * Players get the bar through {@link TFBossBarPacket.AddTFBossBarPacket}, which creates a {@code ClientTFBossBar} on the client.
 * Progress, name, property and removal updates keep using the vanilla boss event packets, which work on it unchanged, while
 * {@link #updateStyle} restyles the bar mid-fight. The vanilla colour stays {@link BossBarColor#WHITE}, the tintable bar texture.
 */
public class ServerTFBossBar extends ServerBossEvent {
	private int color;

	public ServerTFBossBar(Component name, int color, BossBarOverlay overlay) {
		super(name, BossBarColor.WHITE, overlay);
		this.color = color;
	}

	public int getBarColor() {
		return this.color;
	}

	@Override
	public void addPlayer(ServerPlayer player) {
		if (this.players.add(player) && this.visible) {
			this.sendAddPacket(player);
		}
	}

	/**
	 * Vanilla re-sends its own add packet when a hidden bar is shown again, which would replace our bar with a plain white one on
	 * the client, so ours is sent instead.
	 */
	@Override
	public void setVisible(boolean visible) {
		if (visible != this.visible) {
			this.visible = visible;
			for (ServerPlayer player : this.players) {
				if (visible) this.sendAddPacket(player);
				else player.connection.send(ClientboundBossEventPacket.createRemovePacket(this.getId()));
			}
		}
	}

	private void sendAddPacket(ServerPlayer player) {
		TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TFBossBarPacket.AddTFBossBarPacket(this));
	}

	/**
	 * Changes the colour and overlay of the bar for everyone who sees it.
	 *
	 * @param allowLerp whether the client keeps easing the bar towards its new progress. Without it, the bar jumps straight to the
	 *                  progress it was last given, for example when it starts tracking a different value.
	 */
	public void updateStyle(int color, BossBarOverlay overlay, boolean allowLerp) {
		boolean change = false;
		if (this.color != color) {
			this.color = color;
			change = true;
		}
		if (this.overlay != overlay) {
			this.overlay = overlay;
			change = true;
		}
		if (change) {
			TFBossBarPacket.UpdateTFBossBarStylePacket packet = new TFBossBarPacket.UpdateTFBossBarStylePacket(this, allowLerp);
			this.players.forEach(player -> TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet));
		}
	}
}
