package twilightforest.world.components.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.init.custom.StructureSpeleothemConfigs;

public class YetiCaveComponent extends HollowHillComponent {

	public YetiCaveComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(ctx, TFStructurePieceTypes.TFYeti.get(), nbt);
	}

	public YetiCaveComponent(int i, int x, int y, int z, ResourceKey<StructureSpeleothemConfig> speleothemConfig) {
		super(TFStructurePieceTypes.TFYeti.get(), i, 2, x, y, z, speleothemConfig);
	}

	@Override
	protected ResourceKey<StructureSpeleothemConfig> getDefaultSpeleothemConfig() {
		return StructureSpeleothemConfigs.YETI_CAVE;
	}

	/**
	 * Add in all the blocks we're adding.
	 */
	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox writeableBounds, ChunkPos chunkPosIn, BlockPos blockPos) {
		StructureSpeleothemConfig speleothemConfig = this.getSpeleothemConfig(world);

		int maxRadius = 24;

		// Lattice placement from TF 4.8 (4c99eec1c, cfadbd358). The heights are world heights matching the 4.3 cave carved by
		// ChunkGeneratorTwilight.deformTerrainForYetiLair: the ceiling slopes down from y 16 and the ice floor up from y -4, by offset / 6
		BlockPos center = this.getLocatorPosition();
		for (BlockPos.MutableBlockPos dest : speleothemConfig.latticeIterator(writeableBounds, 0)) {
			int xDist = Math.abs(dest.getX() - center.getX());
			int zDist = Math.abs(dest.getZ() - center.getZ());
			int minDelta = Math.min(xDist, zDist);

			if (xDist <= maxRadius && zDist <= maxRadius) {
				if (speleothemConfig.shouldDoAStalactite(rand))
					this.generateSpeleothem(world, speleothemConfig, dest.above(15 - minDelta / 6), writeableBounds, true);

				// The stock yeti_cave config has no stalagmites; a datapack can add some
				if (speleothemConfig.shouldDoAStalagmite(rand))
					this.generateSpeleothem(world, speleothemConfig, dest.above(-4 + minDelta / 6), writeableBounds, false);
			}
		}

		// spawn alpha yeti
		final BlockState yetiSpawner = TFBlocks.ALPHA_YETI_BOSS_SPAWNER.get().defaultBlockState();
		this.setBlockStateRotated(world, yetiSpawner, this.radius, 1, this.radius, Rotation.NONE, writeableBounds);
	}
}
