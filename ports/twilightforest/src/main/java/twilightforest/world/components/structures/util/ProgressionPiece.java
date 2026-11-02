package twilightforest.world.components.structures.util;

import net.minecraft.world.level.levelgen.structure.StructurePiece;

// TODO:
//  Iterate structure pieces that do have protections and combine into a single VoxelShape.
//  Use that VoxelShape as a "cache" for whether a player has entered a protected part of the structure
//  instead of continuously iterating the List<StructurePiece> for ProgressionPiece impls (all pieces).
//  It should be computed once for when a StructureStart is loaded from WorldGen or Disk.
//  REQUIRED: A way to determine if a BlockPosition is inside of a VoxelShape
public interface ProgressionPiece {
	/**
	 * Does this component fall under block protection when progression is turned on, default true
	 */
	default boolean isComponentProtected() {
		return true;
	}

	/**
	 * Pieces that do not implement {@link ProgressionPiece} (vanilla template pieces and the like) are always protected.
	 */
	static boolean isPieceProtected(StructurePiece piece) {
		return !(piece instanceof ProgressionPiece progressionPiece) || progressionPiece.isComponentProtected();
	}
}
