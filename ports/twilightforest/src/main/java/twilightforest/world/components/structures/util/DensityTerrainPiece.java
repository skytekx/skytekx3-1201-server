package twilightforest.world.components.structures.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * A piece of a landmark whose terrain TF 4.8 shapes with a {@link twilightforest.world.components.structures.CustomDensitySource}
 * terraformer (Hollow Hills, Hydra Lair, Yeti Cave and Labyrinth) instead of TF 4.3's block deformers.
 * <p>
 * Pieces built since the TF 4.8 terrain backport carry {@link #NBT_KEY}. Pieces saved by TF 4.3 lack it: their structure's
 * terraformer adds nothing, ChunkGeneratorTwilight keeps raising, hollowing and draining their chunks with the 4.3 deformers, and the
 * pieces keep the 4.3 feature heights, so a landmark that already has generated chunks stays consistent in the chunks generated after
 * the update.
 */
public interface DensityTerrainPiece {
	String NBT_KEY = "density_terrain";

	/**
	 * @return Whether the landmark's terrain comes from its structure's terraformer (TF 4.8) rather than TF 4.3's block deformers.
	 */
	boolean hasDensityTerrain();

	/**
	 * @return Whether the start's first piece shapes its terrain with the structure's terraformer
	 */
	static boolean isDensityShaped(StructureStart start) {
		return start.isValid() && start.getPieces().get(0) instanceof DensityTerrainPiece piece && piece.hasDensityTerrain();
	}

	static boolean readFlag(CompoundTag tag) {
		return tag.getBoolean(NBT_KEY);
	}

	static void writeFlag(CompoundTag tag, boolean densityTerrain) {
		if (densityTerrain)
			tag.putBoolean(NBT_KEY, true);
	}
}
