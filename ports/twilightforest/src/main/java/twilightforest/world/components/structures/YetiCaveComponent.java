package twilightforest.world.components.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.init.custom.StructureSpeleothemConfigs;

public class YetiCaveComponent extends HollowHillComponent {
	/**
	 * Half the width of the stone hill that YetiCaveStructure's terraformer raises around the cave: its exterior trapezoid reaches
	 * up to (not including) 40 blocks from the center at sea level.
	 */
	public static final int HILL_RADIUS = 39;
	/**
	 * How far the terraformer's roof (y 26, 4.8's {@code 21 + riser}) lies above the top of the cave piece, plus one block for what
	 * players put on it.
	 */
	public static final int ROOF_ABOVE_PIECE = 8;

	public YetiCaveComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(ctx, TFStructurePieceTypes.TFYeti.get(), nbt);
	}

	public YetiCaveComponent(int i, int x, int y, int z, ResourceKey<StructureSpeleothemConfig> speleothemConfig) {
		this(i, x, y, z, speleothemConfig, false);
	}

	public YetiCaveComponent(int i, int x, int y, int z, ResourceKey<StructureSpeleothemConfig> speleothemConfig, boolean densityTerrain) {
		super(TFStructurePieceTypes.TFYeti.get(), i, 2, x, y, z, speleothemConfig, densityTerrain);

		if (densityTerrain) {
			// TF 4.8 (37f62ed62): the box reaches one block deeper, getWorldY() compensates so the cave's contents keep their heights
			this.boundingBox = new BoundingBox(
				this.boundingBox.minX(),
				this.boundingBox.minY() - 1,
				this.boundingBox.minZ(),
				this.boundingBox.maxX(),
				this.boundingBox.maxY(),
				this.boundingBox.maxZ()
			);
		}
	}

	@Override
	protected ResourceKey<StructureSpeleothemConfig> getDefaultSpeleothemConfig() {
		return StructureSpeleothemConfigs.YETI_CAVE;
	}

	@Override
	protected int getWorldY(int pY) {
		// TF 4.8 (37f62ed62) bandaid for the box lowered by one; caves saved by TF 4.3 have the original box
		return super.getWorldY(this.densityTerrain ? pY + 1 : pY);
	}

	/**
	 * The terraformer of a density-shaped Yeti Cave raises a stone hill with the lair's roof above this piece's box, and TF 4.8 sets
	 * yeti_cave's terrain_adaptation to none (0ceb8258d), which drops the 12-block margin that bury added to the start's box. Since
	 * progression protection is per piece, this utility piece covers the hill, its roof and the floor, so the lair stays protected
	 * (and free of biome spawns) like the whole bury-inflated start box was in TF 4.3. It builds nothing and takes the structure's
	 * terrain adaptation, none, so it adds no beard either.
	 */
	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource random) {
		super.addChildren(parent, list, random);

		if (this.densityTerrain) {
			BlockPos center = this.getLocatorPosition();

			list.addPiece(new UtilityPiece(this.getGenDepth() + 1, new BoundingBox(
				center.getX() - HILL_RADIUS,
				this.boundingBox.minY() - 1, // The floor the terraformer leaves under the cave
				center.getZ() - HILL_RADIUS,
				center.getX() + HILL_RADIUS,
				this.boundingBox.maxY() + ROOF_ABOVE_PIECE,
				center.getZ() + HILL_RADIUS
			)));
		}
	}

	/**
	 * Add in all the blocks we're adding.
	 */
	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox writeableBounds, ChunkPos chunkPosIn, BlockPos blockPos) {
		StructureSpeleothemConfig speleothemConfig = this.getSpeleothemConfig(world);

		int maxRadius = 24;
		BlockPos center = this.getLocatorPosition();

		// Lattice placement from TF 4.8 (4c99eec1c, cfadbd358)
		int latticeY;
		if (this.densityTerrain) {
			// TF 4.8 (0ceb8258d, 13712dc34): drain the water and ice the floor of the cave the terraformer carves; the lattice sits at the
			// raised cave's floor level (the piece starts 4 blocks higher than in 4.3)
			int bottomYRelative = this.getWorldY(4);

			drainWater(writeableBounds, world, 6, Blocks.CAVE_AIR.defaultBlockState(), center.getX(), center.getZ(), maxRadius + 10, Blocks.PACKED_ICE.defaultBlockState(), bottomYRelative);

			latticeY = bottomYRelative;
		} else {
			// The heights are world heights matching the 4.3 cave carved by ChunkGeneratorTwilight.deformTerrainForYetiLair: the ceiling
			// slopes down from y 16 and the ice floor up from y -4, by offset / 6
			latticeY = 0;
		}

		for (BlockPos.MutableBlockPos dest : speleothemConfig.latticeIterator(writeableBounds, latticeY)) {
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

	/**
	 * TF 4.8 (0ceb8258d, 13712dc34): drains the water the terrain fill leaves in a density-shaped Yeti Cave (a square of the given
	 * radius, from {@code yStart} down to {@code maxDepth} below it) and turns the dirt and snow under it into {@code undergroundBlock}.
	 */
	public static void drainWater(BoundingBox chunkBox, CommonLevelAccessor level, int maxDepth, BlockState airState, int xCenter, int zCenter, double radius, BlockState undergroundBlock, int yStart) {
		int minY = yStart - maxDepth;

		for (int z = chunkBox.minZ(); z <= chunkBox.maxZ(); z++) {
			int dZ = Mth.abs(zCenter - z);
			for (int x = chunkBox.minX(); x <= chunkBox.maxX(); x++) {
				int dX = Mth.abs(xCenter - x);

				if (Math.max(dX, dZ) >= radius) {
					continue;
				}

				int maxY = Math.min(yStart, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1);

				boolean crossedFloor = false;

				for (int y = maxY; y >= minY; y--) {
					BlockPos posChecked = new BlockPos(x, y, z);

					BlockState stateAt = level.getBlockState(posChecked);

					if (stateAt.getFluidState().is(FluidTags.WATER)) {
						level.setBlock(posChecked, airState, Block.UPDATE_ALL);
					} else {
						crossedFloor = true;
					}

					if (crossedFloor) {
						if (stateAt.is(Blocks.DIRT) || stateAt.is(Blocks.SNOW_BLOCK)) {
							level.setBlock(posChecked, undergroundBlock, Block.UPDATE_ALL);
						}
					}
				}
			}
		}
	}
}
