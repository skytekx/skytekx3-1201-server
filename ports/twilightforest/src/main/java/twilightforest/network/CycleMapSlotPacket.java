package twilightforest.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.components.item.ItemDisplayContents;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;

import java.util.function.Supplier;

/**
 * Sent by the client when the Item Display map cycle key is pressed; the server shows the next map stored in the
 * player's goggles, or none after the last one. Backport of 4.8's CycleMapSlotPacket (031090fd4).
 */
public class CycleMapSlotPacket {
	public static final CycleMapSlotPacket INSTANCE = new CycleMapSlotPacket();

	private CycleMapSlotPacket() {
	}

	@SuppressWarnings("unused")
	public static CycleMapSlotPacket decode(FriendlyByteBuf buf) {
		return INSTANCE;
	}

	@SuppressWarnings("unused")
	public void encode(FriendlyByteBuf buf) {
	}

	public static class Handler {

		public static boolean onMessage(CycleMapSlotPacket message, Supplier<NetworkEvent.Context> ctx) {
			ServerPlayer player = ctx.get().getSender();
			if (player != null)
				ctx.get().enqueueWork(() -> cycleMapSlot(player));
			ctx.get().setPacketHandled(true);
			return true;
		}

		private static void cycleMapSlot(ServerPlayer player) {
			ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
			ItemDisplayContents contents = TravellersGearData.ITEM_DISPLAY.get(headStack);
			if (contents == null || contents.isEmpty() || !TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.ITEM_DISPLAY_MODIFIER))
				return;

			ItemDisplayContents.Mutable mutable = new ItemDisplayContents.Mutable(contents);
			int oldIndex = mutable.chosenMapSlot();
			int newIndex = mutable.cycleChosenMapSlot();

			if (oldIndex != newIndex) {
				TravellersGearData.ITEM_DISPLAY.set(headStack, mutable.toImmutable());
				player.getInventory().setChanged();
				player.playNotifySound(newIndex == -1 ? TFSounds.CYCLE_MAPS_EMPTY.get() : TFSounds.CYCLE_MAPS.get(), player.getSoundSource(), 1F, 1F);
			}
		}
	}
}
