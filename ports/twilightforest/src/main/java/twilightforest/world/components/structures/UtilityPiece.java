package twilightforest.world.components.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFStructurePieceTypes;

/**
 * A piece that places nothing. Structures add it to shape the Beardifier around them with its bounding box (it takes the structure's
 * terrain adaptation), to extend their protected area, and to mark space where features may or may not decorate.
 */
public class UtilityPiece extends StructurePiece {
	public final boolean allowFeatures;

	public UtilityPiece(int genDepth, BoundingBox boundingBox) {
		this(genDepth, boundingBox, true);
	}

	public UtilityPiece(int genDepth, BoundingBox boundingBox, boolean allowFeatures) {
		super(TFStructurePieceTypes.TFUtilityPiece.get(), genDepth, boundingBox);
		this.allowFeatures = allowFeatures;
	}

	public UtilityPiece(StructurePieceSerializationContext context, CompoundTag compoundTag) {
		super(TFStructurePieceTypes.TFUtilityPiece.get(), compoundTag);
		this.allowFeatures = compoundTag.getBoolean("allow_features");
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putBoolean("allow_features", this.allowFeatures);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
	}
}
