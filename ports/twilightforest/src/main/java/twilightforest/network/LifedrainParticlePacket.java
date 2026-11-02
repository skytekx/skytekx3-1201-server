package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.client.particle.LifedrainTrail;

import java.util.function.Supplier;

/**
 * Tells clients to draw the Lifedrain Scepter's red trail from the user's hand to the drained target.
 * The server decides when the drain actually lands, so every player watching the user sees the same trail.
 */
public class LifedrainParticlePacket {

	private final int entityID;
	private final Vec3 victimPos;

	public LifedrainParticlePacket(int entityID, Vec3 victimPos) {
		this.entityID = entityID;
		this.victimPos = victimPos;
	}

	public LifedrainParticlePacket(FriendlyByteBuf buf) {
		this.entityID = buf.readInt();
		this.victimPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.entityID);
		buf.writeDouble(this.victimPos.x());
		buf.writeDouble(this.victimPos.y());
		buf.writeDouble(this.victimPos.z());
	}

	public static class Handler {

		@SuppressWarnings("Convert2Lambda")
		public static boolean onMessage(LifedrainParticlePacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(new Runnable() {
				@Override
				public void run() {
					ClientLevel level = Minecraft.getInstance().level;
					if (level != null && level.getEntity(message.entityID) instanceof LivingEntity living) {
						LifedrainTrail.makeRedMagicTrail(level, living, message.victimPos);
					}
				}
			});

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
