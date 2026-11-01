package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;

import java.util.function.Supplier;

/**
 * Sends the data pack driven Questing Ram quest to a client, which needs it for the crosshair indicator.
 */
public class SyncQuestsPacket {

	private final QuestingRamContext ram;

	public SyncQuestsPacket(QuestingRamContext ram) {
		this.ram = ram;
	}

	public SyncQuestsPacket(FriendlyByteBuf buf) {
		this.ram = QuestingRamContext.fromNetwork(buf);
	}

	public void encode(FriendlyByteBuf buf) {
		this.ram.toNetwork(buf);
	}

	public static class Handler {

		public static boolean onMessage(SyncQuestsPacket message, Supplier<NetworkEvent.Context> ctx) {
			// clientbound only, a client must never be able to change the server's quest
			if (ctx.get().getDirection().getReceptionSide().isClient()) {
				ctx.get().enqueueWork(() -> QuestingRamCurrentContext.setClientContext(message.ram));
			}
			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
