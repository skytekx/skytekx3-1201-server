package twilightforest.util.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.material.MapColor;
import twilightforest.util.Codecs;

/**
 * The colour a biome is drawn with on the Magic Map ({@code twilightforest:magic_map_color} data map). Brightness is the
 * map colour shade, 0 (darkest) to 3.
 */
public record MagicMapBiomeColor(MapColor color, int brightness) {

	public static final Codec<MagicMapBiomeColor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codecs.COLOR_CODEC.fieldOf("map_color").forGetter(MagicMapBiomeColor::color),
			Codec.intRange(0, 3).fieldOf("brightness").forGetter(MagicMapBiomeColor::brightness)
	).apply(instance, MagicMapBiomeColor::new));

	public MagicMapBiomeColor(MapColor color) {
		this(color, 1);
	}
}
