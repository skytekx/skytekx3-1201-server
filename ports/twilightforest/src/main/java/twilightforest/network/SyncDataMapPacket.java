package twilightforest.network;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFDataMaps;
import twilightforest.util.datamaps.DataMapType;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Sends the resolved values of a synced data map to a client, like NeoForge does for the data maps 4.8 registers with
 * {@code synced(...)}. Clients only need them for display: the recipe viewers list the ominous fire transformations.
 */
public class SyncDataMapPacket {

	@Nullable
	private final Payload<?> payload;

	private SyncDataMapPacket(@Nullable Payload<?> payload) {
		this.payload = payload;
	}

	public static <R, T> SyncDataMapPacket create(DataMapType<R, T> type, RegistryAccess access) {
		return new SyncDataMapPacket(new Payload<>(type, type.resolveAll(access.registryOrThrow(type.registryKey()))));
	}

	public SyncDataMapPacket(FriendlyByteBuf buf) {
		ResourceLocation id = buf.readResourceLocation();
		DataMapType<?, ?> type = TFDataMaps.getSyncedDataMap(id);
		if (type == null) {
			// both sides run the same mod version (the channel's protocol check), so this only happens with a broken install
			TwilightForestMod.LOGGER.warn("Ignoring values for unknown synced data map {}", id);
			buf.skipBytes(buf.readableBytes());
			this.payload = null;
		} else {
			this.payload = Payload.read(type, buf);
		}
	}

	public void encode(FriendlyByteBuf buf) {
		if (this.payload != null) {
			this.payload.write(buf);
		}
	}

	private record Payload<T>(DataMapType<?, T> type, Map<ResourceLocation, T> values) {

		private static <T> Payload<T> read(DataMapType<?, T> type, FriendlyByteBuf buf) {
			return new Payload<>(type, buf.readMap(FriendlyByteBuf::readResourceLocation, valueBuf -> valueBuf.readJsonWithCodec(type.codec())));
		}

		private void write(FriendlyByteBuf buf) {
			buf.writeResourceLocation(this.type.id());
			buf.writeMap(this.values, FriendlyByteBuf::writeResourceLocation, (valueBuf, value) -> valueBuf.writeJsonWithCodec(this.type.codec(), value));
		}

		private void apply() {
			this.type.setSyncedValues(this.values);
		}
	}

	public static class Handler {

		public static boolean onMessage(SyncDataMapPacket message, Supplier<NetworkEvent.Context> ctx) {
			// clientbound only; the client copy is kept apart from the server's, so this is safe in singleplayer too
			if (ctx.get().getDirection().getReceptionSide().isClient() && message.payload != null) {
				ctx.get().enqueueWork(message.payload::apply);
			}
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
