package twilightforest.world.components.structures.lichtowerrevamp;

import com.google.common.base.Suppliers;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import net.minecraft.Util;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.block.AbstractSkullCandleBlock;
import twilightforest.block.entity.SkullCandleBlockEntity;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFEntities;
import twilightforest.world.components.processors.*;
import twilightforest.world.components.structures.util.StructureTemplateDefinitions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

// TF 4.8 makes this a Beanification @Component that the pieces @Autowire; here it is a plain singleton
public final class LichTowerUtil {
	public static final LichTowerUtil INSTANCE = new LichTowerUtil();

	// TF 4.8's room spawners shrink mobs wider than this through the generic.scale attribute, which Minecraft 1.20.1 lacks
	private static final float ROOM_SPAWNER_MAX_WIDTH = 0.8f;

	private final StructureTemplateDefinitions structureTemplateDefinitions = StructureTemplateDefinitions.INSTANCE;

	private final Supplier<StructureProcessor> roomSpawners = Suppliers.memoize(() -> SpawnerProcessor.compile(2, ROOM_SPAWNER_MAX_WIDTH, Object2IntMaps.unmodifiable(fitsRoomSpawner(Util.make(new Object2IntArrayMap<>(), map -> {
		// 1/3 chance for any spider variant, 1/3 chance for skeleton, 1/3 chance for zombie
		map.put(EntityType.SPIDER, 1);
		map.put(EntityType.CAVE_SPIDER, 1);
		map.put(TFEntities.SWARM_SPIDER.get(), 1);
		map.put(TFEntities.HEDGE_SPIDER.get(), 1);
		map.put(EntityType.SKELETON, 4);
		map.put(EntityType.ZOMBIE, 4);
	})))));
	private final Supplier<StructureProcessor> centralSpawners = Suppliers.memoize(() -> SpawnerProcessor.compile(4, Object2IntMaps.unmodifiable(Util.make(new Object2IntArrayMap<>(), map -> {
		map.put(EntityType.SKELETON, 2);
		map.put(EntityType.ZOMBIE, 1);
		map.put(TFEntities.SWARM_SPIDER.get(), 1);
	}))));
	private final Supplier<List<Block>> STAIR_DECAY_BLOCKS = Suppliers.memoize(() -> List.of(
		TFBlocks.TWILIGHT_OAK_SLAB.get(),
		TFBlocks.CANOPY_SLAB.get(),
		TFBlocks.TWILIGHT_OAK_BANISTER.get(),
		TFBlocks.CANOPY_BANISTER.get()
	));
	private final Supplier<StructureProcessor[]> stairDecayProcessors = Suppliers.memoize(() -> {
		List<Block> filter = this.STAIR_DECAY_BLOCKS.get();
		return new StructureProcessor[]{
			new VerticalDecayProcessor(filter, 0.025f),
			new VerticalDecayProcessor(filter, 0.05f),
			new VerticalDecayProcessor(filter, 0.075f),
			new VerticalDecayProcessor(filter, 0.1f),
			new VerticalDecayProcessor(filter, 0.125f),
			new VerticalDecayProcessor(filter, 0.15f),
			new VerticalDecayProcessor(filter, 0.175f),
			new VerticalDecayProcessor(filter, 0.2f)
		};
	});
	private static final Supplier<StructureProcessor> UPDATE_MARKER = Suppliers.memoize(() -> UpdateMarkingProcessor.forBlocks(
		Blocks.BIRCH_FENCE,
		Blocks.POLISHED_ANDESITE_STAIRS,
		Blocks.STONE_BRICK_WALL,
		Blocks.MOSSY_STONE_BRICK_WALL,
		Blocks.COBBLESTONE_WALL,
		Blocks.MOSSY_COBBLESTONE_WALL,
		TFBlocks.WROUGHT_IRON_FENCE.get(),
		TFBlocks.CANOPY_FENCE.get(),
		TFBlocks.TWISTED_STONE_PILLAR.get()
	));

	private LichTowerUtil() {
	}

	// 1.20.1 cannot shrink a spawned mob, so room spawners leave out the mobs that 4.8 would shrink to fit the small rooms' doorways
	private static Object2IntMap<EntityType<?>> fitsRoomSpawner(Object2IntMap<EntityType<?>> weights) {
		weights.keySet().removeIf(type -> type.getWidth() > ROOM_SPAWNER_MAX_WIDTH);
		return weights;
	}

	public StructureProcessor getRoomSpawnerProcessor() {
		return this.roomSpawners.get();
	}

	public StructureProcessor getCentralBridgeSpawnerProcessor() {
		return this.centralSpawners.get();
	}

	public StructureProcessor[] getStairDecayProcessors() {
		return this.stairDecayProcessors.get();
	}

	@Nullable
	public ResourceLocation rollRandomRoom(RandomSource randomSource, int size) {
		ResourceLocation pool = switch (size) {
			case 0 -> LichTowerPieces.ROOM_3;
			case 1 -> LichTowerPieces.ROOM_5;
			case 2 -> LichTowerPieces.ROOM_7;
			case 3 -> LichTowerPieces.ROOM_9;
			default -> null;
		};

		if (pool == null) return null;

		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, pool);
	}

	@Nullable
	public ResourceLocation rollTowerGallery(RandomSource randomSource) {
		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, LichTowerPieces.GALLERY);
	}

	@Nullable
	public ResourceLocation rollGalleryRoof(RandomSource randomSource, BoundingBox box) {
		boolean odd = (Math.min(box.getXSpan(), box.getZSpan()) & 1) == 1;
		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, odd ? LichTowerPieces.GALLERY_ROOF_ODD : LichTowerPieces.GALLERY_ROOF_EVEN);
	}

	@Nullable
	public ResourceLocation rollRandomMobBridge(RandomSource randomSource) {
		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, LichTowerPieces.MOB_BRIDGE);
	}

	@Nullable
	public ResourceLocation rollRandomCover(RandomSource randomSource) {
		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, LichTowerPieces.DOOR_STOPPER);
	}

	@Nullable
	public ResourceLocation rollRandomDecor(RandomSource randomSource, boolean inCentralTower) {
		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, inCentralTower ? LichTowerPieces.CENTER_DECOR : LichTowerPieces.ROOM_DECOR);
	}

	public Iterable<ResourceLocation> shuffledCenterBridges(RandomSource randomSource) {
		return this.structureTemplateDefinitions.getShuffledSequence(randomSource, LichTowerPieces.BRIDGE_FROM_CENTRAL);
	}

	public Iterable<ResourceLocation> shuffledRoomBridges(RandomSource randomSource) {
		return this.structureTemplateDefinitions.getShuffledSequence(randomSource, LichTowerPieces.ROOM_BRIDGE);
	}

	public Iterable<ResourceLocation> shuffledEndBridges(RandomSource randomSource) {
		return this.structureTemplateDefinitions.getShuffledSequence(randomSource, LichTowerPieces.END_BRIDGE);
	}

	public Iterable<ResourceLocation> shuffledRoofs(RandomSource randomSource, int size, boolean sideRoof) {
		ResourceLocation pool = switch (size) {
			case 0 -> sideRoof ? LichTowerPieces.ROOM_3_SIDE_ROOF : LichTowerPieces.ROOM_3_ROOF;
			case 1 -> sideRoof ? LichTowerPieces.ROOM_5_SIDE_ROOF : LichTowerPieces.ROOM_5_ROOF;
			case 2 -> sideRoof ? LichTowerPieces.ROOM_7_SIDE_ROOF : LichTowerPieces.ROOM_7_ROOF;
			case 3 -> sideRoof ? LichTowerPieces.ROOM_9_SIDE_ROOF : LichTowerPieces.ROOM_9_ROOF;
			default -> null;
		};

		if (pool == null) return List.of();

		return this.structureTemplateDefinitions.getShuffledSequence(randomSource, pool);
	}

	public Iterable<ResourceLocation> shuffledBeards(RandomSource randomSource, int size) {
		ResourceLocation pool = switch (size) {
			case 1 -> LichTowerPieces.ROOM_5_BEARD;
			case 2 -> LichTowerPieces.ROOM_7_BEARD;
			case 3 -> LichTowerPieces.ROOM_9_BEARD;
			default -> null;
		};

		if (pool == null) return List.of();

		return this.structureTemplateDefinitions.getShuffledSequence(randomSource, pool);
	}

	@Nullable
	public ResourceLocation getTrim(RandomSource randomSource, int size) {
		ResourceLocation pool = switch (size) {
			case 1 -> LichTowerPieces.ROOM_5_TRIM;
			case 2 -> LichTowerPieces.ROOM_7_TRIM;
			case 3 -> LichTowerPieces.ROOM_9_TRIM;
			default -> null;
		};

		if (pool == null) return null;

		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, pool);
	}

	public Set<String> getLadderPlacementsForSize(int size) {
		return switch (size) {
			case 1 -> LichTowerPieces.ladderPlacements1;
			case 2 -> LichTowerPieces.ladderPlacements2;
			case 3 -> LichTowerPieces.ladderPlacements3;
			default -> Collections.emptySet();
		};
	}

	@Nullable
	public ResourceLocation getRoomUpwards(RandomSource random, int size, int ladderOffset) {
		if (size > 0 && size <= 3) {
			Int2ObjectMap<ResourceLocation> roomsForSize = LichTowerPieces.ladderRooms.get(size - 1);
			ResourceLocation roomsForLadderPlacement = roomsForSize.get(ladderOffset);
			return roomsForLadderPlacement == null ? null : this.structureTemplateDefinitions.getRandomTemplate(random, roomsForLadderPlacement);
		}

		return null;
	}

	@Nullable
	public ResourceLocation getFallbackRoof(RandomSource random, int size, boolean sideAttachment) {
		ResourceLocation pool = switch (size) {
			case 0 -> sideAttachment ? LichTowerPieces.ROOM_3_SIDE_ROOF_FALLBACK : LichTowerPieces.ROOM_3_ROOF_FALLBACK;
			case 1 -> sideAttachment ? LichTowerPieces.ROOM_5_SIDE_ROOF_FALLBACK : LichTowerPieces.ROOM_5_ROOF_FALLBACK;
			case 2 -> sideAttachment ? LichTowerPieces.ROOM_7_SIDE_ROOF_FALLBACK : LichTowerPieces.ROOM_7_ROOF_FALLBACK;
			case 3 -> sideAttachment ? LichTowerPieces.ROOM_9_SIDE_ROOF_FALLBACK : LichTowerPieces.ROOM_9_ROOF_FALLBACK;
			default -> null;
		};

		if (pool == null) return null;

		return this.structureTemplateDefinitions.getRandomTemplate(random, pool);
	}

	@Nullable
	public ResourceLocation getFallbackBeard(RandomSource random, int size) {
		ResourceLocation pool = switch (size) {
			case 1 -> LichTowerPieces.ROOM_5_BEARD_FALLBACK;
			case 2 -> LichTowerPieces.ROOM_7_BEARD_FALLBACK;
			case 3 -> LichTowerPieces.ROOM_9_BEARD_FALLBACK;
			default -> null;
		};

		if (pool == null) return null;

		return this.structureTemplateDefinitions.getRandomTemplate(random, pool);
	}

	public static void addDefaultProcessors(StructurePlaceSettings settings) {
		settings.addProcessor(JigsawNbtReplacementProcessor.INSTANCE)
			.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
			.addProcessor(StoneBricksVariants.INSTANCE)
			.addProcessor(CobbleVariants.INSTANCE)
			.addProcessor(InfestBlocksProcessor.INSTANCE)
			.addProcessor(UPDATE_MARKER.get());
	}

	// The jigsaw replacement and structure block removal that the yard pieces use
	public static void addJigsawProcessors(StructurePlaceSettings settings) {
		settings.addProcessor(JigsawNbtReplacementProcessor.INSTANCE)
			.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
	}

	@Nullable
	public ResourceLocation getKeepsakeCasketRoom(RandomSource random) {
		return this.structureTemplateDefinitions.getRandomTemplate(random, LichTowerPieces.ROOM_9_SPECIAL);
	}

	@Nullable
	public ResourceLocation getEnclosedCentralBridge(RandomSource random) {
		return this.structureTemplateDefinitions.getRandomTemplate(random, LichTowerPieces.BRIDGE_FROM_CENTRAL_FALLBACK);
	}

	@Nullable
	public ResourceLocation getDirectRoomAttachment(RandomSource random) {
		return this.structureTemplateDefinitions.getRandomTemplate(random, LichTowerPieces.ROOM_BRIDGE_FALLBACK);
	}

	@Nullable
	public ResourceLocation getDefaultBridgeStopper(RandomSource randomSource) {
		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, LichTowerPieces.DOOR_STOPPER_FALLBACK);
	}

	@Nullable
	public ResourceLocation rollGrave(RandomSource randomSource) {
		return this.structureTemplateDefinitions.getRandomTemplate(randomSource, LichTowerPieces.YARD_GRAVE);
	}

	/**
	 * Places a lit skull candle with the given amount of plain candles. TF 4.8 keeps the candle count in a {@code candles} block
	 * state; this fork keeps it (and the candle colour) in the skull candle's block entity, so it is written there instead.
	 */
	public static void placeSkullCandle(WorldGenLevel level, BlockPos pos, BlockState skullCandle, int candles, int flags) {
		level.setBlock(pos, skullCandle, flags);

		if (level.getBlockEntity(pos) instanceof SkullCandleBlockEntity skullCandleEntity) {
			// load() instead of the setters, which send block updates through the block entity's level that worldgen does not set
			CompoundTag tag = skullCandleEntity.saveWithoutMetadata();
			tag.putInt("CandleColor", AbstractSkullCandleBlock.CandleColors.PLAIN.getValue());
			tag.putInt("CandleAmount", candles);
			skullCandleEntity.load(tag);
		}
	}

	/**
	 * [VANILLA COPY] JigsawReplacementProcessor#processBlock of Minecraft 1.20.1, which drops the block entity NBT that a jigsaw's
	 * final_state may carry. Since 1.20.5 vanilla keeps it, and the 4.8 Lich Tower graves rely on that for their chests' loot tables
	 * ({@code canopy_chest[...]{"LootTable":"twilightforest:chests/casket_loot"}}), so this copy passes the parsed NBT on.
	 */
	private static final class JigsawNbtReplacementProcessor extends StructureProcessor {
		private static final JigsawNbtReplacementProcessor INSTANCE = new JigsawNbtReplacementProcessor();

		@Nullable
		@Override
		public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
			BlockState state = relativeBlockInfo.state();
			if (!state.is(Blocks.JIGSAW)) {
				return relativeBlockInfo;
			}

			CompoundTag jigsawNbt = relativeBlockInfo.nbt();
			if (jigsawNbt == null) {
				TwilightForestMod.LOGGER.warn("Jigsaw block at {} is missing nbt, will not replace", offset);
				return relativeBlockInfo;
			}

			String finalState = jigsawNbt.getString("final_state");
			BlockStateParser.BlockResult result;
			try {
				result = BlockStateParser.parseForBlock(level.holderLookup(Registries.BLOCK), finalState, true);
			} catch (CommandSyntaxException e) {
				throw new RuntimeException(e);
			}

			return result.blockState().is(Blocks.STRUCTURE_VOID) ? null : new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), result.blockState(), result.nbt());
		}

		@Override
		protected StructureProcessorType<?> getType() {
			// Never serialized, as the pieces add it in their constructors; would read back as the vanilla processor
			return StructureProcessorType.JIGSAW_REPLACEMENT;
		}
	}
}
