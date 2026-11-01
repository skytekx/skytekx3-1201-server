package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.init.TFItems;
import twilightforest.item.OreMeterItem;

import java.util.function.Supplier;

/**
 * Sent by the client when the player left clicks air with an Ore Meter that holds scan results or a targeted block.
 * Left clicks on air never reach the server on their own, so the server wipes its copy of the stack here.
 * Backport of 4.8's WipeOreMeterPacket.
 */
public record WipeOreMeterPacket(InteractionHand hand) {

	public WipeOreMeterPacket(FriendlyByteBuf buf) {
		this(buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(this.hand() == InteractionHand.MAIN_HAND);
	}

	public static class Handler {

		public static boolean onMessage(WipeOreMeterPacket message, Supplier<NetworkEvent.Context> ctx) {
			ServerPlayer player = ctx.get().getSender();

			if (player != null) {
				ctx.get().enqueueWork(() -> {
					ItemStack heldStack = player.getItemInHand(message.hand());
					// Ensure we're not modifying data on some random item because of a desync or a forged packet
					if (heldStack.is(TFItems.ORE_METER.get())) {
						OreMeterItem.wipe(heldStack);
					}
				});
			}

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
