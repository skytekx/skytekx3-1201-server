package twilightforest.world.components.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.jetbrains.annotations.NotNull;
import twilightforest.init.TFEntities;
import twilightforest.init.TFLandmark;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.init.custom.StructureSpeleothemConfigs;
import twilightforest.loot.TFLootTables;
import twilightforest.world.components.feature.BlockSpikeFeature;

public class HollowHillComponent extends TFStructureComponentOld {
	private static final float CHEST_SPAWN_CHANCE = 0.025f;
	private static final float SPAWNER_SPAWN_CHANCE = 0.025f;
	private static final float SPECIAL_SPAWN_CHANCE = CHEST_SPAWN_CHANCE + SPAWNER_SPAWN_CHANCE;

	private final int hillSize;
	final int radius;
	final int hdiam;

	// Settings for placing features inside (Stalactites, Stalagmites, Chests, & Spawners): the lattice spacing and the stalactite chances and lists.
	// Only the id is kept, the entry is looked up in the world's registries when a chunk is decorated
	protected final ResourceKey<StructureSpeleothemConfig> speleothemConfigKey;

	public HollowHillComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		this(ctx, TFStructurePieceTypes.TFHill.get(), nbt);
	}

	public HollowHillComponent(StructurePieceSerializationContext ctx, StructurePieceType piece, CompoundTag nbt) {
		super(piece, nbt);
		this.hillSize = nbt.getInt("hillSize");
		this.radius = ((this.hillSize * 2 + 1) * 8) - 6;
		this.hdiam = (this.hillSize * 2 + 1) * 16;

		// Pieces saved before the speleothem configs existed have no config_id, they keep their structure's config
		this.speleothemConfigKey = StructureSpeleothemConfigs.readKey(nbt, this.getDefaultSpeleothemConfig());
	}

	public HollowHillComponent(StructurePieceType piece, int i, int size, int x, int y, int z, ResourceKey<StructureSpeleothemConfig> speleothemConfigKey) {
		super(piece, i, x, y, z);

		this.setOrientation(Direction.SOUTH);

		// get the size of this hill?
		this.hillSize = size;
		this.radius = ((hillSize * 2 + 1) * 8) - 6;
		this.hdiam = (hillSize * 2 + 1) * 16;

		// can we determine the size here?
		this.boundingBox = TFLandmark.getComponentToAddBoundingBox(x, y, z, -radius, -(3 + hillSize), -radius, radius * 2, radius / (hillSize == 1 ? 2 : hillSize), radius * 2, Direction.SOUTH, true);

		this.speleothemConfigKey = speleothemConfigKey;
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tagCompound) {
		super.addAdditionalSaveData(ctx, tagCompound);
		tagCompound.putInt("hillSize", hillSize);
		StructureSpeleothemConfigs.writeKey(tagCompound, this.speleothemConfigKey);
	}

	/**
	 * @return The speleothem config of this piece's structure, used for pieces saved without a config_id
	 */
	protected ResourceKey<StructureSpeleothemConfig> getDefaultSpeleothemConfig() {
		return StructureSpeleothemConfigs.getHollowHillKey(this.hillSize);
	}

	protected StructureSpeleothemConfig getSpeleothemConfig(WorldGenLevel world) {
		return StructureSpeleothemConfigs.resolve(world.registryAccess(), this.speleothemConfigKey);
	}

	/**
	 * Add in all the blocks we're adding.
	 */
	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource rand, BoundingBox writeableBounds, ChunkPos chunkPosIn, BlockPos blockPos) {
		BlockPos locatorPos = this.getLocatorPosition();
		float shortenedRadiusSq = this.radius * this.radius * 0.85f;
		StructureSpeleothemConfig speleothemConfig = this.getSpeleothemConfig(world);

		// Use two rectangle-grid lattices to simulate a triangular-grid lattice, simulating an optimal hexagonal-packing pattern for filling this structure
		// with stalactites, stalagmites, chests, and spawners

		// RectangleLatticeIterator enables for approximately-even spacing across chunks
		for (BlockPos.MutableBlockPos latticePos : speleothemConfig.latticeIterator(writeableBounds, 0)) {
			int distSq = getDistSqFromCenter(locatorPos, latticePos);

			if (distSq > shortenedRadiusSq) continue;

			this.setFeatures(world, rand, writeableBounds, latticePos, distSq, speleothemConfig);
		}
	}

	private void setFeatures(WorldGenLevel world, RandomSource rand, BoundingBox writeableBounds, BlockPos.MutableBlockPos pos, int distSq, StructureSpeleothemConfig speleothemConfig) {
		rand.setSeed(rand.nextLong() ^ pos.asLong());
		this.placeCeilingFeature(world, rand, pos, distSq, speleothemConfig);
		this.placeFloorFeature(world, rand, writeableBounds, pos, distSq, speleothemConfig);
	}

	private void placeFloorFeature(WorldGenLevel world, RandomSource rand, BoundingBox writeableBounds, BlockPos.MutableBlockPos pos, int distSq, StructureSpeleothemConfig speleothemConfig) {

		int floorY = this.getFloorY(distSq);

		float floatChance = rand.nextFloat();

		if (floatChance < SPECIAL_SPAWN_CHANCE) {
			// Random direction for offset from lattice-grid, this isn't applied to stalagmites to reduce chances of burying these
			float angle = rand.nextFloat() * Mth.TWO_PI;
			int x = Math.round(Mth.cos(angle) * Mth.SQRT_OF_TWO) + pos.getX();
			int z = Math.round(Mth.sin(angle) * Mth.SQRT_OF_TWO) + pos.getZ();
			pos.set(x, floorY, z);

			if (floatChance < SPAWNER_SPAWN_CHANCE) {
				setSpawnerInWorld(world, writeableBounds, this.getMobID(rand), v -> {}, pos.above());
			} else {
				this.placeTreasureAtWorldPosition(world, this.getTreasureType(), false, writeableBounds, pos.above());
			}

			world.setBlock(pos.below(), Blocks.COBBLESTONE.defaultBlockState(), 50);
			world.setBlock(pos, Blocks.COBBLESTONE.defaultBlockState(), 50);
		} else if (speleothemConfig.shouldDoAStalagmite(rand)) {
			pos.setY(floorY);

			int ceilingY = this.getCeilingY(distSq);

			int forcedMaxHeight = ceilingY - floorY + 4; // Limit height of Stalagmites, plus a little leeway, so they do not poke out through the topsoil

			BlockSpikeFeature.startSpike(world, pos, speleothemConfig.getStalagmite(rand), rand, false, forcedMaxHeight);
		}
	}

	private void placeCeilingFeature(WorldGenLevel world, RandomSource rand, BlockPos.MutableBlockPos pos, int distSq, StructureSpeleothemConfig speleothemConfig) {
		if (!speleothemConfig.shouldDoAStalactite(rand)) return;

		BlockPos ceiling = pos.atY(this.getCeilingY(distSq));

		// There's no generational defect from over-generating Stalactites, such as poking through the ground with Stalagmites.
		// Plus the ore girth would be diminished as well. Thus, no max height is set for stalactites unlike stalagmites.
		// The config's ore_chance decides between a stone and an ore stalactite, the weights pick the ore
		BlockSpikeFeature.startSpike(world, ceiling, speleothemConfig.getStalactite(rand), rand, true);
	}

	private int getCeilingY(int distSq) {
		return this.getWorldY(Mth.ceil(this.getCeilingHeight(Mth.sqrt(distSq))));
	}

	private int getFloorY(int distSq) {
		return this.getWorldY(Mth.floor(this.getFloorHeight(Mth.sqrt(distSq)) + 0.25f));
	}

	@NotNull
	private TFLootTables getTreasureType() {
		return this.hillSize == 3 ? TFLootTables.LARGE_HOLLOW_HILL : (this.hillSize == 2 ? TFLootTables.MEDIUM_HOLLOW_HILL : TFLootTables.SMALL_HOLLOW_HILL);
	}

	/**
	 * Generate a random stalactite/stalagmite from this piece's speleothem config, at a world position
	 */
	protected void generateSpeleothem(WorldGenLevel world, StructureSpeleothemConfig speleothemConfig, BlockPos pos, BoundingBox sbb, boolean hanging) {
		// are the coordinates in our bounding box?
		if (sbb.isInside(pos) && world.getBlockState(pos).getBlock() != Blocks.SPAWNER) {
			// generate an RNG for this stalactite
			RandomSource stalRNG = RandomSource.create(world.getSeed() + (long) pos.getX() * pos.getZ());

			// make the actual stalactite
			BlockSpikeFeature.startSpike(world, pos, speleothemConfig.getSpeleothem(hanging, stalRNG), stalRNG, hanging);
		}
	}

	private float getFloorHeight(float dist) {
		return (this.hillSize * 2) - Mth.cos(dist / this.hdiam * Mth.PI) * (this.hdiam / 20f) + 1;
	}

	private float getCeilingHeight(float dist) {
		return Mth.cos(dist / this.hdiam * Mth.PI) * (this.hdiam / 4f);
	}

	/**
	 * Gets the id of a mob appropriate to the current hill size.
	 */
	protected EntityType<?> getMobID(RandomSource rand) {
		return getMobID(rand, this.hillSize);
	}

	/**
	 * Gets the id of a mob appropriate to the specified hill size.
	 *
	 */
	protected EntityType<?> getMobID(RandomSource rand, int level) {
		if (level == 1) {
			return getLevel1Mob(rand);
		}
		if (level == 2) {
			return getLevel2Mob(rand);
		}
		if (level == 3) {
			return getLevel3Mob(rand);
		}

		return EntityType.SPIDER;
	}

	/**
	 * Returns a mob string appropriate for a level 1 hill
	 */
	public EntityType<?> getLevel1Mob(RandomSource rand) {
		return switch (rand.nextInt(10)) {
			case 3, 4, 5 -> EntityType.SPIDER;
			case 6, 7 -> EntityType.ZOMBIE;
			case 8 -> EntityType.SILVERFISH;
			case 9 -> TFEntities.REDCAP.get();
			default -> TFEntities.SWARM_SPIDER.get();
		};
	}

	/**
	 * Returns a mob string appropriate for a level 2 hill
	 */
	public EntityType<?> getLevel2Mob(RandomSource rand) {
		return switch (rand.nextInt(10)) {
			case 3, 4, 5 -> EntityType.ZOMBIE;
			case 6, 7 -> EntityType.SKELETON;
			case 8 -> TFEntities.SWARM_SPIDER.get();
			case 9 -> EntityType.CAVE_SPIDER;
			default -> TFEntities.REDCAP.get();
		};
	}

	/**
	 * Returns a mob string appropriate for a level 3 hill.  The level 3 also has 2 mid-air wraith spawners.
	 */
	public EntityType<?> getLevel3Mob(RandomSource rand) {
		return switch (rand.nextInt(11)) {
			case 0 -> TFEntities.SLIME_BEETLE.get();
			case 1 -> TFEntities.FIRE_BEETLE.get();
			case 2 -> TFEntities.PINCH_BEETLE.get();
			case 3, 4, 5 -> EntityType.SKELETON;
			case 6, 7, 8 -> EntityType.CAVE_SPIDER;
			case 9 -> EntityType.CREEPER;
			default -> TFEntities.WRAITH.get();
		};
	}

	public static int getDistSqFromCenter(BlockPos center, BlockPos to) {
		int x = to.getX() - center.getX();
		int z = to.getZ() - center.getZ();

		return x * x + z * z;
	}
}
