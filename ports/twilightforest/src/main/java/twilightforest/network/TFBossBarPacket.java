package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.entity.boss.bar.ClientTFBossBar;
import twilightforest.entity.boss.bar.ServerTFBossBar;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Boss bar packets for {@link ServerTFBossBar} (backported from TF 4.8). Progress, name, property and removal updates use the
 * vanilla boss event packets; only adding a bar and changing its RGB colour or overlay need these.
 */
public abstract class TFBossBarPacket {
	protected final UUID id;

	protected TFBossBarPacket(ServerTFBossBar bossEvent) {
		this.id = bossEvent.getId();
	}

	protected TFBossBarPacket(FriendlyByteBuf buf) {
		this.id = buf.readUUID();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUUID(this.id);
	}

	/**
	 * Replaces vanilla's add operation of {@link net.minecraft.network.protocol.game.ClientboundBossEventPacket}, so the client
	 * creates a {@link ClientTFBossBar} that carries the RGB colour.
	 */
	public static class AddTFBossBarPacket extends TFBossBarPacket {
		private final Component name;
		private final float progress;
		private final int color;
		private final BossEvent.BossBarOverlay overlay;
		private final boolean darkenScreen;
		private final boolean playMusic;
		private final boolean createWorldFog;

		public AddTFBossBarPacket(ServerTFBossBar bossEvent) {
			super(bossEvent);
			this.name = bossEvent.getName();
			this.progress = bossEvent.getProgress();
			this.color = bossEvent.getBarColor();
			this.overlay = bossEvent.getOverlay();
			this.darkenScreen = bossEvent.shouldDarkenScreen();
			this.playMusic = bossEvent.shouldPlayBossMusic();
			this.createWorldFog = bossEvent.shouldCreateWorldFog();
		}

		public AddTFBossBarPacket(FriendlyByteBuf buf) {
			super(buf);
			this.name = buf.readComponent();
			this.progress = buf.readFloat();
			this.color = buf.readInt();
			this.overlay = buf.readEnum(BossEvent.BossBarOverlay.class);
			int i = buf.readUnsignedByte();
			this.darkenScreen = (i & 1) > 0;
			this.playMusic = (i & 2) > 0;
			this.createWorldFog = (i & 4) > 0;
		}

		@Override
		public void encode(FriendlyByteBuf buf) {
			super.encode(buf);
			buf.writeComponent(this.name);
			buf.writeFloat(this.progress);
			buf.writeInt(this.color);
			buf.writeEnum(this.overlay);
			// the same flags as ClientboundBossEventPacket.encodeProperties
			buf.writeByte((this.darkenScreen ? 1 : 0) | (this.playMusic ? 2 : 0) | (this.createWorldFog ? 4 : 0));
		}

		public static class Handler {

			@SuppressWarnings("Convert2Lambda")
			public static boolean onMessage(AddTFBossBarPacket packet, Supplier<NetworkEvent.Context> ctx) {
				//ensure this is only done on clients as this uses client only code
				if (ctx.get().getDirection().getReceptionSide().isClient()) {
					ctx.get().enqueueWork(new Runnable() {
						@Override
						public void run() {
							Minecraft.getInstance().gui.getBossOverlay().events.put(packet.id, new ClientTFBossBar(packet.id, packet.name, packet.progress, packet.color, packet.overlay, packet.darkenScreen, packet.playMusic, packet.createWorldFog));
						}
					});
				}

				ctx.get().setPacketHandled(true);
				return true;
			}
		}
	}

	/**
	 * Restyles a {@link ClientTFBossBar} mid-fight, e.g. when the Lich's shield breaks.
	 */
	public static class UpdateTFBossBarStylePacket extends TFBossBarPacket {
		private final int color;
		private final BossEvent.BossBarOverlay overlay;
		private final boolean allowLerp;

		public UpdateTFBossBarStylePacket(ServerTFBossBar bossEvent, boolean allowLerp) {
			super(bossEvent);
			this.color = bossEvent.getBarColor();
			this.overlay = bossEvent.getOverlay();
			this.allowLerp = allowLerp;
		}

		public UpdateTFBossBarStylePacket(FriendlyByteBuf buf) {
			super(buf);
			this.color = buf.readInt();
			this.overlay = buf.readEnum(BossEvent.BossBarOverlay.class);
			this.allowLerp = buf.readBoolean();
		}

		@Override
		public void encode(FriendlyByteBuf buf) {
			super.encode(buf);
			buf.writeInt(this.color);
			buf.writeEnum(this.overlay);
			buf.writeBoolean(this.allowLerp);
		}

		public static class Handler {

			@SuppressWarnings("Convert2Lambda")
			public static boolean onMessage(UpdateTFBossBarStylePacket packet, Supplier<NetworkEvent.Context> ctx) {
				//ensure this is only done on clients as this uses client only code
				if (ctx.get().getDirection().getReceptionSide().isClient()) {
					ctx.get().enqueueWork(new Runnable() {
						@Override
						public void run() {
							if (Minecraft.getInstance().gui.getBossOverlay().events.get(packet.id) instanceof ClientTFBossBar bossEvent) {
								bossEvent.setBarColor(packet.color);
								bossEvent.setOverlay(packet.overlay);
								// boss bars ease towards their progress over 100 milliseconds, which looks wrong when the bar starts tracking another value
								if (!packet.allowLerp) bossEvent.setSetTime(bossEvent.getSetTime() - 200L);
							}
						}
					});
				}

				ctx.get().setPacketHandled(true);
				return true;
			}
		}
	}
}
