package twilightforest.network;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.client.DimensionSpecialEffectsManager;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.TwilightForestMod;
import twilightforest.client.TwilightForestRenderInfo;
import twilightforest.client.renderer.TFWeatherRenderer;

import java.util.List;
import java.util.function.Supplier;

/**
 * Sends every piece of a nearby locked structure to the client, each paired with whether that piece is protected.
 * Protected pieces arrive inflated by 4 so the protection rain also shows just outside their walls.
 */
public class StructureProtectionPacket {

	private final List<Pair<BoundingBox, Boolean>> boxes;

	public StructureProtectionPacket(List<Pair<BoundingBox, Boolean>> boxes) {
		this.boxes = boxes;
	}

	public StructureProtectionPacket(FriendlyByteBuf buf) {
		this.boxes = buf.readList(b -> Pair.of(new BoundingBox(
				b.readInt(), b.readInt(), b.readInt(),
				b.readInt(), b.readInt(), b.readInt()
		), b.readBoolean()));
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeCollection(this.boxes, (b, boxAndFlag) -> {
			BoundingBox box = boxAndFlag.getFirst();
			b.writeInt(box.minX());
			b.writeInt(box.minY());
			b.writeInt(box.minZ());
			b.writeInt(box.maxX());
			b.writeInt(box.maxY());
			b.writeInt(box.maxZ());
			b.writeBoolean(boxAndFlag.getSecond());
		});
	}

	public static class Handler {
		public static boolean onMessage(StructureProtectionPacket message, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				DimensionSpecialEffects info = DimensionSpecialEffectsManager.getForType(TwilightForestMod.prefix("renderer"));

				// add weather boxes if needed
				if (info instanceof TwilightForestRenderInfo) {
					TFWeatherRenderer.setProtectedBoxes(message.boxes);
				}
			});

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
