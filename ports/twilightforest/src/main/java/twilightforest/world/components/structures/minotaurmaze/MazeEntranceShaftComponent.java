package twilightforest.world.components.structures.minotaurmaze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.world.components.structures.TFStructureComponentOld;
import twilightforest.world.components.structures.util.DensityTerrainPiece;
import twilightforest.world.registration.TFGenerationSettings;


public class MazeEntranceShaftComponent extends TFStructureComponentOld implements DensityTerrainPiece {
	// Whether the shaft has to cut through the mound LabyrinthStructure's terraformer raises over it (TF 4.8), see DensityTerrainPiece
	private final boolean densityTerrain;

	public MazeEntranceShaftComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(TFStructurePieceTypes.TFMMES.get(), nbt);
		this.densityTerrain = DensityTerrainPiece.readFlag(nbt);
	}

	public MazeEntranceShaftComponent(int i, RandomSource rand, int x, int y, int z) {
		this(i, rand, x, y, z, false);
	}

	public MazeEntranceShaftComponent(int i, RandomSource rand, int x, int y, int z, boolean densityTerrain) {
		super(TFStructurePieceTypes.TFMMES.get(), i, new BoundingBox(x, y, z, x + 6 - 1, y, z + 6 - 1).encapsulate(new BlockPos(x, TFGenerationSettings.SEALEVEL, z)));
		this.setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(rand));
		this.densityTerrain = densityTerrain;
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tagCompound) {
		super.addAdditionalSaveData(ctx, tagCompound);
		DensityTerrainPiece.writeFlag(tagCompound, this.densityTerrain);
	}

	@Override
	public boolean hasDensityTerrain() {
		return this.densityTerrain;
	}

	/**
	 * Initiates construction of the Structure Component picked, at the current Location of StructGen
	 */
	@Override
	public void addChildren(StructurePiece structurecomponent, StructurePieceAccessor list, RandomSource random) {
		// NO-OP
	}

	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		BlockPos.MutableBlockPos pos = chunkPosIn.getWorldPosition().mutable().setX(this.boundingBox.minX()).setZ(this.boundingBox.minZ());

		this.boundingBox.encapsulate(pos.setY(generator.getSeaLevel() - 9));

		this.generateBox(world, sbb, 0, 0, 0, 5, this.boundingBox.getYSpan(), 5, TFBlocks.MAZESTONE_BRICK.get().defaultBlockState(), AIR, true);
		// TF 4.8 (78a3dd526): the shaft opens through the terraformed mound, up to 11 above sea level
		this.generateAirBox(world, sbb, 1, 0, 1, 4, this.densityTerrain ? (generator.getSeaLevel() + 11) - this.boundingBox.minY() : this.boundingBox.getYSpan(), 4);
	}
}
