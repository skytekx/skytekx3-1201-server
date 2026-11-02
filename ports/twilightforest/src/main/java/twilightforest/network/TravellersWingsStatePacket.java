package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.components.entity.TravellersWingsAttachment;

import java.util.function.Supplier;

/**
 * Syncs the Traveller's Wings animation state of a living entity to the players tracking it, so the procedural wing
 * animation matches between clients. Backport of 4.8's TravellersWingsStatePacket (ab58315ba).
 */
public class TravellersWingsStatePacket {

	private final int entityId;
	private final TravellersWingsAttachment.WingState state;
	private final boolean sidestepLeft;
	private final int doubleJumpTimer;
	private final int sidestepTimer;

	public TravellersWingsStatePacket(int entityId, TravellersWingsAttachment.WingState state, boolean sidestepLeft, int doubleJumpTimer, int sidestepTimer) {
		this.entityId = entityId;
		this.state = state;
		this.sidestepLeft = sidestepLeft;
		this.doubleJumpTimer = doubleJumpTimer;
		this.sidestepTimer = sidestepTimer;
	}

	public TravellersWingsStatePacket(FriendlyByteBuf buf) {
		this.entityId = buf.readInt();
		this.state = buf.readEnum(TravellersWingsAttachment.WingState.class);
		this.sidestepLeft = buf.readBoolean();
		this.doubleJumpTimer = buf.readInt();
		this.sidestepTimer = buf.readInt();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.entityId);
		buf.writeEnum(this.state);
		buf.writeBoolean(this.sidestepLeft);
		buf.writeInt(this.doubleJumpTimer);
		buf.writeInt(this.sidestepTimer);
	}

	public static class Handler {

		public static boolean onMessage(TravellersWingsStatePacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				ClientLevel level = Minecraft.getInstance().level;
				if (level != null && level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
					TravellersWingsAttachment attachment = TravellersWingsAttachment.get(livingEntity);
					if (attachment != null) {
						attachment.state = message.state;
						attachment.sidestepLeft = message.sidestepLeft;
						attachment.doubleJumpTimer = message.doubleJumpTimer;
						attachment.sidestepTimer = message.sidestepTimer;
					}
				}
			});

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
