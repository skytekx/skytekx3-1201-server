package twilightforest.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.server.ServerLifecycleHooks;
import twilightforest.world.components.chunkgenerators.ChunkGeneratorTwilight;
import twilightforest.world.registration.TFGenerationSettings;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class WorldUtil {
	private WorldUtil() {}

	/**
	 * The Overworld seed from the save's WorldOptions. The legacy biome layers and the landmark grid of the Twilight Forest dimension are seeded with it.
	 * Only valid while a server is running.
	 */
	public static long getOverworldSeed() {
		return Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer()).getWorldData().worldGenOptions().seed();
	}

	/**
	 * Inclusive of edges
	 */
	public static Iterable<BlockPos> getAllAround(BlockPos center, int range) {
		return BlockPos.betweenClosed(center.offset(-range, -range, -range), center.offset(range, range, range));
	}

	/**
	 * Floors both corners of the bounding box to integers
	 * Inclusive of edges
	 */
	public static Iterable<BlockPos> getAllInBB(AABB bb) {
		return BlockPos.betweenClosed((int) bb.minX, (int) bb.minY, (int) bb.minZ, (int) bb.maxX, (int) bb.maxY, (int) bb.maxZ);
	}

	public static BlockPos randomOffset(RandomSource random, BlockPos pos, int range) {
		return randomOffset(random, pos, range, range, range);
	}

	public static BlockPos randomOffset(RandomSource random, BlockPos pos, int rx, int ry, int rz) {
		int dx = random.nextInt(rx * 2 + 1) - rx;
		int dy = random.nextInt(ry * 2 + 1) - ry;
		int dz = random.nextInt(rz * 2 + 1) - rz;
		return pos.offset(dx, dy, dz);
	}

	public static <T> T getRandomElementWithWeights(List<Pair<T, Float>> list, RandomSource rng) {
		float totalWeight = (float) list.stream().mapToDouble(Pair::getSecond).sum();
		float randomValue = rng.nextFloat() * totalWeight;

		for (Pair<T, Float> pair : list) {
			randomValue -= pair.getSecond();
			if (randomValue < 0) {
				return pair.getFirst();
			}
		}
		return Util.getRandom(list, rng).getFirst(); // This line should never be reached if the input list is valid
	}

	@Nullable
	public static ChunkGeneratorTwilight getChunkGenerator(LevelAccessor level) {
		if (level.getChunkSource() instanceof ServerChunkCache chunkSource && chunkSource.chunkMap.generator() instanceof ChunkGeneratorTwilight chunkGenerator)
			return chunkGenerator;

		return null;
	}

	public static int getGeneratorSeaLevel(LevelAccessor level) {
		return level.getChunkSource() instanceof ServerChunkCache chunkSource
				? chunkSource.chunkMap.generator().getSeaLevel()
				: TFGenerationSettings.SEALEVEL; // TFGenerationSettings.SEALEVEL Should only ever hit if this method is called on client
	}

	public static int getSeaLevel(ChunkGenerator generator) {
		if (generator instanceof ChunkGeneratorTwilight) {
			return generator.getSeaLevel();
		} else return TFGenerationSettings.SEALEVEL;
	}

	public static int getBaseHeight(LevelAccessor level, int x, int z, Heightmap.Types type) {
		if (level.getChunkSource() instanceof ServerChunkCache chunkSource) {
			return chunkSource.chunkMap.generator().getBaseHeight(x, z, type, level, chunkSource.randomState());
		} else {
			return level.getHeight(type, x, z);
		}
	}
}
