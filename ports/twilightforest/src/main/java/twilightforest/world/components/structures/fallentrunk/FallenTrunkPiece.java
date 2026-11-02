package twilightforest.world.components.structures.fallentrunk;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFEntities;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.loot.TFLootTables;
import twilightforest.world.components.structures.UtilityPiece;
import twilightforest.world.components.structures.type.FallenTrunkStructure;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FallenTrunkPiece extends StructurePiece {
	public static final BlockStateProvider DEFAULT_LOG = BlockStateProvider.simple(TFBlocks.TWILIGHT_OAK_LOG.get());

	public static final int ERODED_LENGTH = 2;
	protected static final float MOSS_CHANCE = 0.44F;
	protected static final List<EntityType<?>> SPAWNER_MONSTERS = List.of(TFEntities.SWARM_SPIDER.get(), TFEntities.HOSTILE_WOLF.get(), EntityType.CAVE_SPIDER);
	public static final int TERRAFORM_PIECE_SIZE = 10;
	public static final int FEATURE_PIECE_SIZE = 3;
	protected final BlockStateProvider log;
	public final int length;
	public final int radius;
	// 1.20.1 has no LOOT_TABLE registry, so the loot table is a plain id
	protected final ResourceLocation chestLootTable;
	private final long holeSeed;
	protected final Hole hole;
	// The hole in world coordinates. It only depends on the hole and the orientation, so it is built once instead of for every log.
	// Volatile because chunks that share the trunk generate on different worker threads
	@Nullable
	private volatile Set<BlockPos> absoluteHoleBlockPos;

	public FallenTrunkPiece(int length, int radius, BlockStateProvider log, ResourceLocation chestLootTable, Direction orientation, BoundingBox boundingBox, long seed) {
		super(TFStructurePieceTypes.TFFallenTrunk.get(), 0, boundingBox);
		this.length = length;
		this.radius = radius;
		this.log = log;
		this.chestLootTable = chestLootTable;
		this.holeSeed = seed;
		this.hole = new Hole(this, RandomSource.create(this.holeSeed));
		this.setOrientation(orientation);
	}

	public FallenTrunkPiece(StructurePieceSerializationContext context, CompoundTag tag) {
		super(TFStructurePieceTypes.TFFallenTrunk.get(), tag);
		this.length = tag.getInt("length");
		this.radius = tag.getInt("radius");

		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, context.registryAccess());
		this.log = BlockStateProvider.CODEC.parse(ops, tag.getCompound("log")).result().orElse(DEFAULT_LOG);
		ResourceLocation lootTable = tag.contains("chest_loot_table", Tag.TAG_STRING) ? ResourceLocation.tryParse(tag.getString("chest_loot_table")) : null;
		this.chestLootTable = lootTable != null ? lootTable : TFLootTables.FALLEN_TRUNK_LOOT.lootTable;
		// 4.8 reads this back with getInt, which truncates the saved long, so a reloaded start would cut a different hole and mounds
		// into the chunks that had not generated yet
		this.holeSeed = tag.getLong("hole_seed");
		this.hole = new Hole(this, RandomSource.create(this.holeSeed));
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putInt("length", this.length);
		tag.putInt("radius", this.radius);
		tag.put("log", BlockStateProvider.CODEC.encodeStart(NbtOps.INSTANCE, this.log).resultOrPartial(TwilightForestMod.LOGGER::error).orElseGet(CompoundTag::new));
		tag.putString("chest_loot_table", this.chestLootTable.toString());
		tag.putLong("hole_seed", this.holeSeed);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
		// Widens the structure start over the terraformed area, so every chunk the mounds and the flattening reach gets the terraformer
		StructurePiece terraformingPiece = new UtilityPiece(0, this.boundingBox.inflatedBy(TERRAFORM_PIECE_SIZE));
		list.addPiece(terraformingPiece);

		// Keeps surface features such as trees out of the trunk
		StructurePiece featurePiece = new UtilityPiece(0, this.boundingBox.inflatedBy(FEATURE_PIECE_SIZE), false);
		list.addPiece(featurePiece);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource randomSource,
							BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
		RandomSource random = RandomSource.create(pos.asLong());
		if (this.radius == FallenTrunkStructure.radiuses.get(0))
			this.generateSmallFallenTrunk(level, random, box, pos, random.nextBoolean());
		if (this.radius == FallenTrunkStructure.radiuses.get(1))
			this.generateFallenTrunk(level, random, box, pos, random.nextBoolean(), false);
		if (this.radius == FallenTrunkStructure.radiuses.get(2))
			this.generateFallenTrunk(level, random, box, pos, false, random.nextBoolean());
	}

	private void generateSmallFallenTrunk(WorldGenLevel level, RandomSource random, BoundingBox box, BlockPos pos, boolean hasHole) {
		for (int dx = 0; dx <= 3; dx++) {
			for (int dy = 0; dy <= 3; dy++) {
				if (Math.abs(dx - 1.5) + Math.abs(dy - 1.5) != 2)
					continue;

				this.generateTrunkRod(level, random, box, pos, dx, dy, hasHole);
			}
		}
	}

	private void generateFallenTrunk(WorldGenLevel level, RandomSource random, BoundingBox box, BlockPos pos, boolean hasHole, boolean hasSpawnerAndChests) {
		this.generateTrunk(level, random, box, pos, hasHole);
		if (hasSpawnerAndChests)
			this.generateSpawnerAndChests(level, random, box);
	}

	private void generateTrunk(WorldGenLevel level, RandomSource random, BoundingBox box, BlockPos pos, boolean hasHole) {
		int hollow = this.radius / 2;
		int diameter = this.radius * 2;

		for (int dx = 0; dx <= diameter; dx++) {
			for (int dy = 0; dy <= diameter; dy++) {
				int dist = this.getDist(dx, dy);
				if (dist > this.radius || dist <= hollow)
					continue;

				this.generateTrunkRod(level, random, box, pos, dx, dy, hasHole);
			}
		}
	}

	private void generateTrunkRod(WorldGenLevel level, RandomSource random, BoundingBox box, BlockPos pos, int dx, int dy, boolean hasHole) {
		this.generateTrunkMainRod(level, random, box, pos, dx, dy, hasHole);
		this.generateErodedEnds(level, random, box, pos, dx, dy, hasHole);
	}

	private void generateTrunkMainRod(WorldGenLevel level, RandomSource random, BoundingBox box, BlockPos pos, int dx, int dy, boolean hasHole) {
		for (int dz = ERODED_LENGTH; dz < this.length - 1 - ERODED_LENGTH; dz++) {
			BlockPos offsetPos = pos.offset(dx, dy, dz);
			this.placeLog(level, this.getLogState(random, offsetPos), dx, dy, dz, box, random, hasHole);
		}
	}

	private void generateErodedEnds(WorldGenLevel level, RandomSource random, BoundingBox box, BlockPos pos, int dx, int dy, boolean hasHole) {
		for (int dz = ERODED_LENGTH - 1; dz >= 0; dz--) {
			if (random.nextBoolean())
				break;

			BlockPos offsetPos = pos.offset(dx, dy, dz);
			this.placeLog(level, this.getLogState(random, offsetPos), dx, dy, dz, box, random, hasHole);
		}

		for (int dz = this.length - 1 - ERODED_LENGTH; dz < this.length - 1; dz++) {
			if (random.nextBoolean())
				break;

			BlockPos offsetPos = pos.offset(dx, dy, dz);
			this.placeLog(level, this.getLogState(random, offsetPos), dx, dy, dz, box, random, hasHole);
		}
	}

	private void generateSpawnerAndChests(WorldGenLevel level, RandomSource random, BoundingBox box) {
		BlockPos spawnerPos = new BlockPos(this.radius, 2, (this.length - 1) / 2);
		this.placeBlock(level, Blocks.SPAWNER.defaultBlockState(), spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ(), box);
		BlockPos worldSpawnerPos = this.getWorldPos(spawnerPos.getX(), spawnerPos.getY(), spawnerPos.getZ());

		// Don't use main random for spawner due to desyncs in random.next() calls between chunks, causing 2 chests or other bugs
		RandomSource spawnerRandom = RandomSource.create(random.nextLong());

		if (box.isInside(worldSpawnerPos.getX(), worldSpawnerPos.getY(), worldSpawnerPos.getZ()) && level.getBlockEntity(worldSpawnerPos) instanceof SpawnerBlockEntity spawner)
			spawner.setEntityId(Util.getRandom(SPAWNER_MONSTERS, spawnerRandom), spawnerRandom);

		Direction orientation = this.getOrientation().getClockWise();
		if (this.getMirror() == Mirror.LEFT_RIGHT)
			orientation = orientation.getOpposite();


		Set<Vec3i> possibleChestsOffsets = new HashSet<>();
		for (int i = 0; i <= 6; i++) {
			possibleChestsOffsets.add(new Vec3i(2, 1, -3 + i));
		}
		possibleChestsOffsets.add(new Vec3i(1, 0, -3));
		possibleChestsOffsets.add(new Vec3i(1, 0, 2));
		for (Vec3i vec3i : possibleChestsOffsets.stream().toList()) {
			possibleChestsOffsets.add(new Vec3i(-vec3i.getX(), vec3i.getY(), vec3i.getZ()));
		}

		Vec3i chestOffset = Util.getRandom(possibleChestsOffsets.stream().toList(), random);
		BlockPos chestSpawnerPos = spawnerPos.offset(chestOffset);

		BlockState chestState = TFBlocks.TWILIGHT_OAK_CHEST.get().defaultBlockState().setValue(ChestBlock.FACING, chestOffset.getX() < 0 ? orientation : orientation.getOpposite());
		BlockPos chestPos = this.getWorldPos(chestSpawnerPos.getX(), chestSpawnerPos.getY(), chestSpawnerPos.getZ());
		RandomSource chestRandom = RandomSource.create(random.nextLong());
		this.createChest(level, box, chestRandom, chestPos, this.chestLootTable, chestState);
	}

	private int getDist(int dx, int dy) {
		int ax = Math.abs(dx - this.radius);
		int az = Math.abs(dy - this.radius);
		return (int) (Math.max(ax, az) + (Math.min(ax, az) * 0.5));
	}

	private BlockState getLogState(RandomSource random, BlockPos pos) {
		return this.log.getState(random, pos).trySetValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
	}

	private void placeLog(WorldGenLevel level, BlockState blockstate, int x, int y, int z, BoundingBox boundingbox, RandomSource random, boolean hasHole) {
		// getBlock() returns air outside the chunk we are generating, we create new random to call random.next() constant amount of times in this function
		RandomSource randomChild = RandomSource.create(random.nextLong());
		if (hasHole && z > ERODED_LENGTH && z < this.length - 1 - ERODED_LENGTH - 1 && this.getAllAbsoluteHoleBlockPos().contains(this.getWorldPos(x, y, z)))
			return;
		BlockState blockState = this.getBlock(level, x, y, z, boundingbox);
		if (blockState.is(BlockTags.REPLACEABLE_BY_TREES) || blockState.is(BlockTags.FLOWERS) || blockState.isAir() || randomChild.nextBoolean()) {
			this.placeBlock(level, blockstate, x, y, z, boundingbox);
			if (randomChild.nextFloat() <= MOSS_CHANCE && boundingbox.isInside(this.getWorldPos(x, y + 1, z)) && this.getBlock(level, x, y + 1, z, boundingbox).is(BlockTags.REPLACEABLE)) {
				this.placeBlock(level, TFBlocks.MOSS_PATCH.get().defaultBlockState(), x, y + 1, z, boundingbox);
				level.blockUpdated(this.getWorldPos(x, y + 1, z), TFBlocks.MOSS_PATCH.get());  // to connect moss patches
				level.getChunk(this.getWorldPos(x, y + 1, z)).markPosForPostprocessing(this.getWorldPos(x, y + 1, z));
			}
		}
	}

	@Override
	public Direction getOrientation() {
		return Objects.requireNonNull(super.getOrientation());  // orientation is always not null, just to remove warnings
	}

	protected int getSideLength() {
		return this.radius == 1 ? 2 : this.radius * 2 - 1;
	}

	protected int convertXYtoLength(int x, int y) {
		return this.convertXYtoLength(this.getSideLength(), x, y);
	}

	protected int convertXYtoLength(int sideLength, int x, int y) {
		int length = 0;

		if (x == 0) {
			length = y;
		} else if (y == sideLength + 1) {
			length = sideLength + x;
		} else if (x == sideLength + 1) {
			length = 3 * sideLength - y + 1;
		} else if (y == 0) {
			length = 4 * sideLength - x;
		}

		return length - 1;
	}

	public int[] convertLengthToXY(int length) {
		return this.convertLengthToXY(this.getSideLength(), length);
	}

	public int[] convertLengthToXY(int sideLength, int length) {
		if (length < sideLength) {
			return new int[] {0, length + 1};  // Left edge
		} else if (length < 2 * sideLength) {
			return new int[] {length + 1 - sideLength, sideLength + 1};  // Top edge
		} else if (length < 3 * sideLength) {
			return new int[] {sideLength + 1, 3 * sideLength - length};  // Right edge
		} else {
			return new int[] {4 * sideLength - (length + 1), 0};  // Bottom edge
		}
	}

	private Set<BlockPos> getAllAbsoluteHoleBlockPos() {
		Set<BlockPos> absoluteHoleBlockPos = this.absoluteHoleBlockPos;
		if (absoluteHoleBlockPos == null) {
			int zOffset = ERODED_LENGTH + 1;  // hole z coordinate has offset because of eroded ends
			Set<BlockPos> holeBlockPos = new HashSet<>();
			for (int xy = 0; xy < this.hole.sizeXY; xy++) {
				for (int z = 0; z < this.hole.sizeZ; z++) {
					if (this.hole.isInHole(xy, z)) {
						int[] separatedXY = this.convertLengthToXY(xy);
						holeBlockPos.add(this.getWorldPos(separatedXY[0], separatedXY[1], z + zOffset).immutable());
					}
				}
			}
			absoluteHoleBlockPos = Collections.unmodifiableSet(holeBlockPos);
			this.absoluteHoleBlockPos = absoluteHoleBlockPos;
		}
		return absoluteHoleBlockPos;
	}

	// returns all blockPos adjacent hole
	private Set<BlockPos> getAllForbiddenMoundBaseBlockPos() {
		Set<BlockPos> allAbsoluteHoleBlockPos = this.getAllAbsoluteHoleBlockPos();
		Set<BlockPos> forbiddenBlockPos = new HashSet<>(allAbsoluteHoleBlockPos);
		for (BlockPos blockPos : allAbsoluteHoleBlockPos) {
			for (Direction direction : Direction.values()) {
				forbiddenBlockPos.add(blockPos.relative(direction).atY(this.boundingBox.minY()));
			}
		}

		return forbiddenBlockPos;
	}

	public Set<BlockPos> getAllPotentialBaseMoundBlockPos(boolean isOnRightSide) {
		Set<BlockPos> potentialBlockPos = new HashSet<>();
		// It's difficult to calculate the size of the mound because of different biases. So, use just "safe" distance (determined experimentally)
		int forbiddenLength = Mth.positiveCeilDiv(this.length, 4) + ERODED_LENGTH;
		for (int offset = forbiddenLength; offset <= this.length - forbiddenLength; offset++) {
			if (isOnRightSide)
				potentialBlockPos.add(this.getWorldPos(Math.min(this.boundingBox.getXSpan(), this.boundingBox.getZSpan()) - 1, 0, offset).immutable());
			else
				potentialBlockPos.add(this.getWorldPos(0, 0, offset).immutable());
		}
		return potentialBlockPos;
	}

	public Set<BlockPos> getAllowedBaseMoundBlockPos(boolean isOnRightSide) {
		Set<BlockPos> allowedBlockPos = this.getAllPotentialBaseMoundBlockPos(isOnRightSide);
		allowedBlockPos.removeAll(this.getAllForbiddenMoundBaseBlockPos());
		return allowedBlockPos;
	}
}
