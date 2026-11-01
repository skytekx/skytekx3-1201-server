package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Boss death animations run on the client off {@link LivingEntity#deathTime}. A player that starts tracking a boss that is already
 * dying would otherwise start the animation from zero, so the server sends its current timer along.
 */
public class UpdateDeathTimePacket {

	private final int entityID;
	private final int deathTime;

	public UpdateDeathTimePacket(int entityID, int deathTime) {
		this.entityID = entityID;
		this.deathTime = deathTime;
	}

	public UpdateDeathTimePacket(FriendlyByteBuf buf) {
		this.entityID = buf.readInt();
		this.deathTime = buf.readInt();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.entityID);
		buf.writeInt(this.deathTime);
	}

	public static class Handler {

		public static boolean onMessage(UpdateDeathTimePacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ClientLevel level = Minecraft.getInstance().level;
				if (level != null && level.getEntity(message.entityID) instanceof LivingEntity living) {
					living.deathTime = message.deathTime;
				}
			});

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
