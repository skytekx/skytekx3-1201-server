package twilightforest.client.model.block.connected;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Generic connected-texture ("CTM") geometry loader, registered as {@code twilightforest:connected_texture_block}.
 * <p>
 * Model json format:
 * <ul>
 *     <li>{@code textures}: {@code overlay_texture} (unconnected), {@code overlay_connected} (CTM sheet), optional {@code base_texture} (an unconnected layer underneath) and {@code particle}</li>
 *     <li>{@code base}: optional {@code tint_index} and {@code emissivity} for the base layer</li>
 *     <li>{@code connected_texture}: optional {@code faces} (defaults to all), {@code tint_index}, {@code emissivity} and {@code render_disabled_faces} (defaults to true) for the overlay layer</li>
 *     <li>{@code connectable_blocks}: block ids, or {@code #tag} ids, this model connects its texture to</li>
 * </ul>
 */
public class ConnectedTextureModelLoader implements IGeometryLoader<UnbakedConnectedTextureModel> {
	public static final ConnectedTextureModelLoader INSTANCE = new ConnectedTextureModelLoader();

	public ConnectedTextureModelLoader() {
	}

	@Override
	public UnbakedConnectedTextureModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
		JsonObject baseTextureInfo = GsonHelper.getAsJsonObject(jsonObject, "base", new JsonObject());
		int baseTintIndex = GsonHelper.getAsInt(baseTextureInfo, "tint_index", -1);
		int baseEmissivity = GsonHelper.getAsInt(baseTextureInfo, "emissivity", 0);

		JsonObject overlayInfo = GsonHelper.getAsJsonObject(jsonObject, "connected_texture", new JsonObject());
		int tintIndex = GsonHelper.getAsInt(overlayInfo, "tint_index", -1);
		int emissivity = GsonHelper.getAsInt(overlayInfo, "emissivity", 0);
		boolean renderDisabled = GsonHelper.getAsBoolean(overlayInfo, "render_disabled_faces", true);
		EnumSet<Direction> faces = this.parseEnabledFaces(overlayInfo);

		List<Block> connectables = new ArrayList<>();
		List<TagKey<Block>> connectableTags = new ArrayList<>();
		this.parseConnectableBlocks(jsonObject, connectables, connectableTags);
		return new UnbakedConnectedTextureModel(faces, renderDisabled, List.copyOf(connectables), List.copyOf(connectableTags), baseTintIndex, baseEmissivity, tintIndex, emissivity);
	}

	private EnumSet<Direction> parseEnabledFaces(JsonObject object) {
		if (!object.has("faces")) {
			return EnumSet.allOf(Direction.class);
		} else {
			EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);

			for (JsonElement element : GsonHelper.getAsJsonArray(object, "faces")) {
				Direction face = Direction.byName(element.getAsString());
				if (face == null) {
					throw new JsonParseException("Invalid face: " + element.getAsString());
				}

				faces.add(face);
			}

			return faces;
		}
	}

	// 4.8 resolves tags into blocks here, but on 1.20.1 models bake before (and independent of) the world's tags being bound,
	// so tag entries are kept as TagKeys and matched at render time instead.
	private void parseConnectableBlocks(JsonObject object, List<Block> blocks, List<TagKey<Block>> tags) {
		if (object.has("connectable_blocks")) {
			for (JsonElement element : GsonHelper.getAsJsonArray(object, "connectable_blocks")) {
				String entry = element.getAsString();
				if (entry.startsWith("#")) {
					ResourceLocation tag = ResourceLocation.tryParse(entry.substring(1));
					if (tag != null) {
						tags.add(TagKey.create(Registries.BLOCK, tag));
					} else {
						throw new JsonParseException("Invalid block tag: " + entry);
					}
				} else {
					ResourceLocation id = ResourceLocation.tryParse(entry);
					Block block = id != null ? BuiltInRegistries.BLOCK.get(id) : Blocks.AIR;
					if (block == Blocks.AIR) {
						throw new JsonParseException("Invalid block: " + entry);
					}
					blocks.add(block);
				}
			}
		}
	}
}
