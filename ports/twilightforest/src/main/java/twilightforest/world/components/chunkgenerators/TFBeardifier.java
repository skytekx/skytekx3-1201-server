package twilightforest.world.components.chunkgenerators;

import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.jetbrains.annotations.Nullable;
import twilightforest.world.components.structures.CustomDensitySource;

import java.util.ArrayList;
import java.util.List;

/**
 * The structure terraforming of TF 4.8 for {@link ChunkGeneratorTwilight}, which fills the Twilight Forest's terrain noise itself.
 * <p>
 * TF 4.8 generates terrain with vanilla's NoiseBasedChunkGenerator and patches the vanilla {@link Beardifier} with tf-asm
 * transformers so that every {@link CustomDensitySource} terraformer is added to it (WorldgenHooks.gatherCustomTerrain and
 * getCustomDensity). TF 4.3 owns the fill loop of the Twilight Forest dimension, so no coremod is needed: this class evaluates the
 * vanilla Beardifier (structure beards, jigsaw junctions and Forge's per-piece PieceBeardifierModifier boxes) plus the
 * terraformers of the structures in the chunk, and ChunkGeneratorTwilight adds the result to the terrain density of each block.
 */
public final class TFBeardifier {
	/**
	 * TF 4.8 adds the Beardifier to {@code twilightforest:forested_terrain}, a density that grows by 1/6 per block downwards through
	 * the surface and is clamped to [-0.1, 0.5], so beards of a few tenths decide whether a block is solid. TF 4.3's 1.16-style warp
	 * noise is not normalised: through the surface of the common Twilight Forest biomes (scale 0.05) it falls by about 13 per block
	 * (2/32 per noise cell * base_factor 2.5 * 96 / (0.9 * 0.05 + 0.1) / 8 blocks per cell), and positive values are multiplied by 4.
	 * {@link #toBeardDensity} undoes the x4, scales the noise to 4.8's slope and applies 4.8's clamp, so beards and terraformers move
	 * the surface by the same amounts as in 4.8.
	 */
	private static final double WARP_NOISE_TO_DENSITY = 1.0 / 78.0;

	private final Beardifier beardifier;
	private final DensityFunction[] customDensities;
	private final MutableFunctionContext context = new MutableFunctionContext();

	private TFBeardifier(Beardifier beardifier, DensityFunction[] customDensities) {
		this.beardifier = beardifier;
		this.customDensities = customDensities;
	}

	/**
	 * @return The terraforming for the chunk, or null when no structure in it adapts the terrain, in which case the chunk's terrain
	 * is left exactly as TF 4.3 generates it.
	 */
	@Nullable
	public static TFBeardifier forStructuresInChunk(StructureManager structureManager, ChunkPos chunkPos) {
		if (structureManager.startsForStructure(chunkPos, structure -> structure.terrainAdaptation() != TerrainAdjustment.NONE || structure instanceof CustomDensitySource).isEmpty())
			return null;

		return new TFBeardifier(Beardifier.forStructuresInChunk(structureManager, chunkPos), gatherCustomTerrain(structureManager, chunkPos));
	}

	// TF 4.8 WorldgenHooks.gatherCustomTerrain
	private static DensityFunction[] gatherCustomTerrain(StructureManager structureManager, ChunkPos chunkPos) {
		List<DensityFunction> customStructureTerraforms = new ArrayList<>(4);

		for (StructureStart structureStart : structureManager.startsForStructure(chunkPos, s -> s instanceof CustomDensitySource))
			if (structureStart.getStructure() instanceof CustomDensitySource customDensitySource)
				customStructureTerraforms.add(customDensitySource.getStructureTerraformer(chunkPos, structureStart));

		return customStructureTerraforms.toArray(new DensityFunction[0]);
	}

	/**
	 * The density that the structures add at a block: the vanilla Beardifier plus every structure terraformer (TF 4.8 WorldgenHooks.getCustomDensity).
	 * Not thread safe, one instance serves one chunk fill.
	 */
	public double compute(int blockX, int blockY, int blockZ) {
		this.context.set(blockX, blockY, blockZ);

		double density = this.beardifier.compute(this.context);

		for (DensityFunction customDensity : this.customDensities)
			density += customDensity.compute(this.context);

		return density;
	}

	/**
	 * @param warpNoise The terrain noise ChunkGeneratorTwilight interpolated for the block.
	 * @return The terrain density with the structures' terraforming added; the block is solid when it is positive.
	 */
	public double applyTo(double warpNoise, int blockX, int blockY, int blockZ) {
		return toBeardDensity(warpNoise) + this.compute(blockX, blockY, blockZ);
	}

	/**
	 * Converts TF 4.3's warp noise into the density range TF 4.8 adds its Beardifier to. Both steps keep the sign, so a block that no
	 * structure adds density to stays solid or empty exactly as before.
	 */
	public static double toBeardDensity(double warpNoise) {
		double linearNoise = warpNoise > 0 ? warpNoise * 0.25 : warpNoise;
		return Mth.clamp(linearNoise * WARP_NOISE_TO_DENSITY, -0.1, 0.5);
	}

	private static final class MutableFunctionContext implements DensityFunction.FunctionContext {
		private int blockX, blockY, blockZ;

		private void set(int blockX, int blockY, int blockZ) {
			this.blockX = blockX;
			this.blockY = blockY;
			this.blockZ = blockZ;
		}

		@Override
		public int blockX() {
			return this.blockX;
		}

		@Override
		public int blockY() {
			return this.blockY;
		}

		@Override
		public int blockZ() {
			return this.blockZ;
		}
	}
}
