package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;
import twilightforest.entity.TFPart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Syncs the position, size and synched data of every {@link TFPart} of a multipart entity. Parts are keyed by their entity id rather
 * than their index, so when the client and server part arrays differ the unknown parts are skipped instead of the data shifting onto
 * the wrong part or running past the end of the list.
 */
public class UpdateTFMultipartPacket {

	private final int entityId;
	private final Map<Integer, PartDataHolder> data;

	public UpdateTFMultipartPacket(FriendlyByteBuf buf) {
		this.entityId = buf.readInt();
		this.data = new HashMap<>();
		int id;
		while ((id = buf.readInt()) > 0) {
			this.data.put(id, PartDataHolder.decode(buf));
		}
	}

	public UpdateTFMultipartPacket(Entity entity) {
		this.entityId = entity.getId();
		this.data = new HashMap<>();
		PartEntity<?>[] parts = entity.getParts();
		if (parts != null) {
			for (PartEntity<?> part : parts) {
				if (part instanceof TFPart<?> tfPart) {
					this.data.put(tfPart.getId(), tfPart.writeData());
				}
			}
		}
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeInt(this.entityId);
		this.data.forEach((id, data) -> {
			buf.writeInt(id);
			data.encode(buf);
		});
		buf.writeInt(-1);
	}

	public static class Handler {

		@SuppressWarnings("Convert2Lambda")
		public static boolean onMessage(UpdateTFMultipartPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(new Runnable() {
				@Override
				public void run() {
					Level world = Minecraft.getInstance().level;
					if (world == null)
						return;
					Entity ent = world.getEntity(message.entityId);
					if (ent != null && ent.isMultipartEntity()) {
						PartEntity<?>[] parts = ent.getParts();
						if (parts == null)
							return;
						for (PartEntity<?> part : parts) {
							if (part instanceof TFPart<?> tfPart) {
								PartDataHolder data = message.data.get(tfPart.getId());
								if (data != null)
									tfPart.readData(data);
							}
						}
					}
				}
			});
			ctx.get().setPacketHandled(true);
			return true;
		}
	}

	public record PartDataHolder(double x,
								 double y,
								 double z,
								 float yRot,
								 float xRot,
								 float width,
								 float height,
								 boolean fixed,
								 @Nullable List<SynchedEntityData.DataValue<?>> data) {


		public void encode(FriendlyByteBuf buffer) {
			buffer.writeDouble(this.x);
			buffer.writeDouble(this.y);
			buffer.writeDouble(this.z);
			buffer.writeFloat(this.yRot);
			buffer.writeFloat(this.xRot);
			buffer.writeFloat(this.width);
			buffer.writeFloat(this.height);
			buffer.writeBoolean(this.fixed);
			if (this.data != null) {
				for (SynchedEntityData.DataValue<?> datavalue : this.data) {
					datavalue.write(buffer);
				}
			}
			buffer.writeByte(255);
		}

		static PartDataHolder decode(FriendlyByteBuf buffer) {
			return new PartDataHolder(
					buffer.readDouble(),
					buffer.readDouble(),
					buffer.readDouble(),
					buffer.readFloat(),
					buffer.readFloat(),
					buffer.readFloat(),
					buffer.readFloat(),
					buffer.readBoolean(),
					unpack(buffer)
			);
		}

		private static List<SynchedEntityData.DataValue<?>> unpack(FriendlyByteBuf buf) {
			List<SynchedEntityData.DataValue<?>> list = new ArrayList<>();

			int i;
			while((i = buf.readUnsignedByte()) != 255) {
				list.add(SynchedEntityData.DataValue.read(buf, i));
			}

			return list;
		}

	}
}
