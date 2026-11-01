package twilightforest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import twilightforest.init.TFLandmark;

import org.jetbrains.annotations.Nullable;
import twilightforest.network.MagicMapPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.LandmarkUtil;
import twilightforest.util.LegacyLandmarkPlacements;

import java.util.HashMap;
import java.util.Map;

public class TFMagicMapData extends MapItemSavedData {
	private static final Map<String, TFMagicMapData> CLIENT_DATA = new HashMap<>();
	// The top bit of a feature id byte (in the saved "features" array and in MagicMapPacket) flags a conquered landmark
	private static final int CONQUERED_FLAG = 0b1000_0000;

	// Keyed by the decoration's packed map coordinates, so a landmark that changes (e.g. gets conquered) replaces its old marker
	public final Int2ObjectMap<TFMapDecoration> tfDecorations = new Int2ObjectLinkedOpenHashMap<>();

	public TFMagicMapData(int x, int z, byte scale, boolean trackpos, boolean unlimited, boolean locked, ResourceKey<Level> dim) {
		super(x, z, scale, trackpos, unlimited, locked, dim);
	}

	public static TFMagicMapData load(CompoundTag nbt) {
		MapItemSavedData data = MapItemSavedData.load(nbt);
		final boolean trackingPosition = !nbt.contains("trackingPosition", 1) || nbt.getBoolean("trackingPosition");
		final boolean unlimitedTracking = nbt.getBoolean("unlimitedTracking");
		final boolean locked = nbt.getBoolean("locked");
		TFMagicMapData tfdata = new TFMagicMapData(data.centerX, data.centerZ, data.scale, trackingPosition, unlimitedTracking, locked, data.dimension);

		tfdata.colors = data.colors;
		tfdata.bannerMarkers.putAll(data.bannerMarkers);
		tfdata.decorations.putAll(data.decorations);
		tfdata.frameMarkers.putAll(data.frameMarkers);
		tfdata.trackedDecorationCount = data.trackedDecorationCount;

		byte[] featureStorage = nbt.getByteArray("features");
		if (featureStorage.length > 0) {
			tfdata.deserializeFeatures(featureStorage);
		}

		return tfdata;
	}

	@Override
	public CompoundTag save(CompoundTag cmp) {
		cmp = super.save(cmp);

		if (!this.tfDecorations.isEmpty()) {
			cmp.putByteArray("features", serializeFeatures());
		}

		return cmp;
	}

	/**
	 * Checks existing features against the feature cache changes wrong ones
	 */
	public void checkExistingFeatures(Level world) {
		IntArrayList toRemove = new IntArrayList();
		Int2ObjectLinkedOpenHashMap<TFMapDecoration> toAdd = new Int2ObjectLinkedOpenHashMap<>();

		for (Int2ObjectMap.Entry<TFMapDecoration> entry : this.tfDecorations.int2ObjectEntrySet()) {
			TFMapDecoration coord = entry.getValue();
			int worldX = (coord.getX() << this.scale - 1) + this.centerX;
			int worldZ = (coord.getY() << this.scale - 1) + this.centerZ;

			int trueId = TFMapDecoration.ICONS_FLIPPED.getInt(LegacyLandmarkPlacements.pickLandmarkAtBlock(worldX, worldZ, (ServerLevel) world));
			if (coord.featureId != trueId) {
				toRemove.add(entry.getIntKey());
				toAdd.put(entry.getIntKey(), new TFMapDecoration(trueId, coord.getX(), coord.getY(), coord.getRot(), LandmarkUtil.isConquered(world, worldX, worldZ)));
			}
		}

		for (int packedCoords : toRemove)
			this.tfDecorations.remove(packedCoords);
		this.tfDecorations.putAll(toAdd);
	}

	public void deserializeFeatures(byte[] arr) {
		this.tfDecorations.clear();

		for (int i = 0; i < arr.length / 3; ++i) {
			byte featureInfo = arr[i * 3];
			byte mapX = arr[i * 3 + 1];
			byte mapZ = arr[i * 3 + 2];
			byte mapRotation = 8;
			this.putMapData(new TFMapDecoration(featureInfo & 0b0111_1111, mapX, mapZ, mapRotation, (featureInfo & CONQUERED_FLAG) != 0));
		}
	}

	public byte[] serializeFeatures() {
		byte[] storage = new byte[this.tfDecorations.size() * 3];

		int i = 0;
		for (TFMapDecoration featureCoord : this.tfDecorations.values()) {
			storage[i * 3] = (byte) (featureCoord.conquered ? featureCoord.featureId | CONQUERED_FLAG : featureCoord.featureId);
			storage[i * 3 + 1] = featureCoord.getX();
			storage[i * 3 + 2] = featureCoord.getY();
			i++;
		}

		return storage;
	}

	/**
	 * Adds the decoration, replacing any decoration at the same map position.
	 *
	 * @return true if the map's decorations changed
	 */
	public boolean putMapData(TFMapDecoration info) {
		return !info.equals(this.tfDecorations.put(packCoordBytes(info.getX(), info.getY()), info));
	}

	private static int packCoordBytes(byte x, byte y) {
		return (x & 0xFF) | ((y & 0xFF) << 8);
	}

	// [VanillaCopy] Adapted from World.getMapData
	@Nullable
	public static TFMagicMapData getMagicMapData(Level world, String name) {
		if (world.isClientSide) return CLIENT_DATA.get(name);
		else return ((ServerLevel)world).getServer().overworld().getDataStorage().get(TFMagicMapData::load, name);
	}

	// [VanillaCopy] Adapted from World.registerMapData
	public static void registerMagicMapData(Level world, TFMagicMapData data, String id) {
		if (world.isClientSide) CLIENT_DATA.put(id, data);
		else ((ServerLevel)world).getServer().overworld().getDataStorage().set(id, data);
	}

	@Nullable
	@Override
	public Packet<?> getUpdatePacket(int mapId, Player player) {
		Packet<?> packet = super.getUpdatePacket(mapId, player);
		return packet instanceof ClientboundMapItemDataPacket mapItemDataPacket ? TFPacketHandler.CHANNEL.toVanillaPacket(new MagicMapPacket(this, mapItemDataPacket), NetworkDirection.PLAY_TO_CLIENT) : packet;
	}

	public static class TFMapDecoration extends MapDecoration {

		private static final Int2ObjectArrayMap<TFLandmark> ICONS = new Int2ObjectArrayMap<>(){{
			defaultReturnValue(TFLandmark.NOTHING);
			put(0, TFLandmark.NOTHING);
			put(1, TFLandmark.SMALL_HILL);
			put(2, TFLandmark.MEDIUM_HILL);
			put(3, TFLandmark.LARGE_HILL);
			put(4, TFLandmark.HEDGE_MAZE);
			put(5, TFLandmark.NAGA_COURTYARD);
			put(6, TFLandmark.LICH_TOWER);
			put(7, TFLandmark.ICE_TOWER);
			put(9, TFLandmark.QUEST_GROVE);
			put(12, TFLandmark.HYDRA_LAIR);
			put(13, TFLandmark.LABYRINTH);
			put(14, TFLandmark.DARK_TOWER);
			put(15, TFLandmark.KNIGHT_STRONGHOLD);
			put(17, TFLandmark.YETI_CAVE);
			put(18, TFLandmark.TROLL_CAVE);
			put(19, TFLandmark.FINAL_CASTLE);
		}};
		private static final Object2IntArrayMap<TFLandmark> ICONS_FLIPPED = new Object2IntArrayMap<>(){{
			ICONS.forEach((k, v) -> put(v, k.intValue()));
		}};

		@OnlyIn(Dist.CLIENT)
		public static class RenderContext {
			private static final RenderType MAP_ICONS = RenderType.text(TwilightForestMod.prefix("textures/gui/mapicons.png"));
			private static final RenderType VANILLA_ICONS = RenderType.text(new ResourceLocation("textures/map/map_icons.png"));
			public static PoseStack stack;
			public static MultiBufferSource buffer;
			public static int light;
		}

		final int featureId;
		final boolean conquered;

		public TFMapDecoration(TFLandmark featureId, byte xIn, byte yIn, byte rotationIn, boolean conquered) {
			this(ICONS_FLIPPED.getInt(featureId), xIn, yIn, rotationIn, conquered);
		}

		public TFMapDecoration(int featureId, byte xIn, byte yIn, byte rotationIn, boolean conquered) {
			super(Type.TARGET_X, xIn, yIn, rotationIn, Component.translatable("map.magic.text"));
			this.featureId = featureId;
			this.conquered = conquered;
		}

		@Override
		@OnlyIn(Dist.CLIENT)
		public boolean render(int idx) {
			// TODO: Forge needs to pass in the ms and buffers, but for now this works
			if (ICONS.get(featureId).isStructureEnabled) {
				RenderContext.stack.pushPose();
				RenderContext.stack.translate(0.0F + getX() / 2.0F + 64.0F, 0.0F + getY() / 2.0F + 64.0F, -0.02F);
				RenderContext.stack.mulPose(Axis.ZP.rotationDegrees(getRot() * 360 / 16.0F));
				RenderContext.stack.scale(4.0F, 4.0F, 3.0F);
				RenderContext.stack.translate(-0.125D, 0.125D, 0.0D);
				float f1 = featureId % 8.0F / 8.0F;
				float f2 = featureId / 8 / 8.0F;
				float f3 = (featureId % 8 + 1) / 8.0F;
				float f4 = (featureId / 8 + 1) / 8.0F;
				Matrix4f matrix4f1 = RenderContext.stack.last().pose();
				VertexConsumer ivertexbuilder1 = RenderContext.buffer.getBuffer(RenderContext.MAP_ICONS);
				ivertexbuilder1.vertex(matrix4f1, -1.0F, 1.0F, idx * -0.001F).color(255, 255, 255, 255).uv(f1, f2).uv2(RenderContext.light).endVertex();
				ivertexbuilder1.vertex(matrix4f1, 1.0F, 1.0F, idx * -0.001F).color(255, 255, 255, 255).uv(f3, f2).uv2(RenderContext.light).endVertex();
				ivertexbuilder1.vertex(matrix4f1, 1.0F, -1.0F, idx * -0.001F).color(255, 255, 255, 255).uv(f3, f4).uv2(RenderContext.light).endVertex();
				ivertexbuilder1.vertex(matrix4f1, -1.0F, -1.0F, idx * -0.001F).color(255, 255, 255, 255).uv(f1, f4).uv2(RenderContext.light).endVertex();
				RenderContext.stack.popPose();

				if (this.conquered) {
					this.renderConqueredMarker(idx);
				}
			}
			return true;
		}

		// TF 4.8 ConqueredMapIconRenderer: a half-size vanilla red X over one corner of the landmark icon
		@OnlyIn(Dist.CLIENT)
		private void renderConqueredMarker(int idx) {
			int icon = Type.RED_X.getIcon();
			float uMin = (icon % 16) / 16.0F;
			float vMin = (icon / 16) / 16.0F;
			float uMax = (icon % 16 + 1) / 16.0F;
			float vMax = (icon / 16 + 1) / 16.0F;
			RenderContext.stack.pushPose();
			RenderContext.stack.translate(0.0F + getX() / 2.0F + 64.0F, 0.0F + getY() / 2.0F + 64.0F, -0.02F);
			RenderContext.stack.mulPose(Axis.ZP.rotationDegrees(getRot() * 360 / 16.0F));
			RenderContext.stack.scale(2.0F, 2.0F, 3.0F);
			RenderContext.stack.translate(-1.0F, -1.0F, 0.0F);
			Matrix4f matrix4f = RenderContext.stack.last().pose();
			// just in front of this landmark's icon, still behind the next decoration (player markers come after landmarks)
			float depth = idx * -0.001F - 0.0005F;
			VertexConsumer vertexConsumer = RenderContext.buffer.getBuffer(RenderContext.VANILLA_ICONS);
			vertexConsumer.vertex(matrix4f, -1.0F, 1.0F, depth).color(255, 255, 255, 255).uv(uMin, vMin).uv2(RenderContext.light).endVertex();
			vertexConsumer.vertex(matrix4f, 1.0F, 1.0F, depth).color(255, 255, 255, 255).uv(uMax, vMin).uv2(RenderContext.light).endVertex();
			vertexConsumer.vertex(matrix4f, 1.0F, -1.0F, depth).color(255, 255, 255, 255).uv(uMax, vMax).uv2(RenderContext.light).endVertex();
			vertexConsumer.vertex(matrix4f, -1.0F, -1.0F, depth).color(255, 255, 255, 255).uv(uMin, vMax).uv2(RenderContext.light).endVertex();
			RenderContext.stack.popPose();
		}

		@Override
		public boolean equals(Object o) {
			if (super.equals(o) && o instanceof TFMapDecoration tfMapDecoration) {
				return this.featureId == tfMapDecoration.featureId && this.conquered == tfMapDecoration.conquered;
			}

			return false;
		}

		@Override
		public int hashCode() {
			return (super.hashCode() * 31 + featureId) * 31 + Boolean.hashCode(this.conquered);
		}
	}
}
