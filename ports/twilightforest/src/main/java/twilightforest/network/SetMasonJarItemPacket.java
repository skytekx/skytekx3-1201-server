package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.block.entity.MasonJarBlockEntity;

import java.util.function.Supplier;

/**
 * Syncs the stack and item rotation of a {@link MasonJarBlockEntity} to the players tracking its chunk whenever the
 * contents change.
 */
public class SetMasonJarItemPacket {

	private final BlockPos pos;
	private final boolean empty;
	private final ItemStack stack;
	private final int rotation;

	public SetMasonJarItemPacket(BlockPos pos, ItemStack stack, int rotation) {
		this.pos = pos;
		this.empty = stack.isEmpty();
		this.stack = stack;
		this.rotation = rotation;
	}

	public SetMasonJarItemPacket(FriendlyByteBuf buf) {
		this.rotation = buf.readInt();
		this.pos = buf.readBlockPos();
		this.empty = buf.readBoolean();
		this.stack = this.empty ? ItemStack.EMPTY : buf.readItem();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.rotation);
		buf.writeBlockPos(this.pos);
		buf.writeBoolean(this.empty);
		if (!this.empty) buf.writeItem(this.stack);
	}

	public static class Handler {

		@SuppressWarnings("Convert2Lambda")
		public static boolean onMessage(SetMasonJarItemPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(new Runnable() {
				@Override
				public void run() {
					Level level = Minecraft.getInstance().level;
					if (level != null && level.getBlockEntity(message.pos) instanceof MasonJarBlockEntity blockEntity) {
						blockEntity.getItemHandler().setItem(message.stack);
						blockEntity.setItemRotation(message.rotation);
						blockEntity.setChanged();
					}
				}
			});

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
