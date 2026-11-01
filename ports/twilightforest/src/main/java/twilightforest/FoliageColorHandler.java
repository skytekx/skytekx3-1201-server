package twilightforest;

import com.google.common.collect.MapMaker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.biome.Biome;
import twilightforest.world.components.BiomeGrassColors;
import twilightforest.init.TFBiomes;

import java.util.HashMap;
import java.util.Map;

public final class FoliageColorHandler {

	// Concurrent + weak keys: the foliage colour resolver is called from chunk meshing worker threads (Embeddium/Sodium),
	// which corrupted the plain WeakHashMap. Cleared when the client world changes (#2361), see TFClientEvents#clearFoliageColorCache.
	private static final Map<Biome, Handler> HANDLES = new MapMaker().weakKeys().makeMap();

	/**
	 * Drops the cached biome handlers. They are looked up through the current client level's biome registry,
	 * so a handler resolved while switching worlds (or with no level loaded) must not outlive that world.
	 */
	public static void clearCache() {
		HANDLES.clear();
	}

	public static int get(int o, Biome biome, double x, double z) {
		Handler handler = HANDLES.get(biome);
		if (handler == null) {
			handler = Handler.REGISTRY.getOrDefault(
					Minecraft.getInstance().level == null ? null :
							Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.BIOME).getResourceKey(biome).orElse(null),
					Handler.DEFAULT);
			HANDLES.put(biome, handler);
		}
		return handler.apply(o, x, z);
	}

	@FunctionalInterface
	private interface Handler {

		Map<ResourceKey<Biome>, Handler> REGISTRY = new HashMap<>() {{
			put(TFBiomes.SPOOKY_FOREST, (o, x, z) -> {
				double noise = (Biome.TEMPERATURE_NOISE.getValue(x * 0.0225D, z * 0.0225D, false) + 1D) / 2D;
				return BiomeGrassColors.blendColors(0xFF0101, 0x49FF01, noise > 0.6D ? noise * 0.2D : noise);
			});
			put(TFBiomes.ENCHANTED_FOREST, (o, x, z) -> (o & 0xFFFF00) + BiomeGrassColors.getEnchantedColor((int) x, (int) z));
			put(TFBiomes.DARK_FOREST_CENTER, (o, x, z) -> {
				double noise = (Biome.TEMPERATURE_NOISE.getValue(x * 0.0225D, z * 0.0225D, false) + 1D) / 2D;
				return noise < -0.1D ? 0xF9821E : 0xE94E14;
			});
			put(TFBiomes.DARK_FOREST, (o, x, z) -> ((FoliageColor.get(0.7F, 0.8F) & 0xFEFEFE) + 0x1E0E4E) / 2);
			put(TFBiomes.SWAMP, (o, x, z) -> ((FoliageColor.get(0.8F, 0.9F) & 0xFEFEFE) + 0x4E0E4E) / 2);
		}};

		Handler DEFAULT = (o, x, z) -> o;

		int apply(int o, double x, double z);


	}

}
