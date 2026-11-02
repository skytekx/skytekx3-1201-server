package twilightforest.world;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFPortalBlock;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.data.tags.StructureTagGenerator;
import twilightforest.init.TFBlocks;
import twilightforest.init.custom.Restrictions;
import twilightforest.util.DiagonalSpiralIterator;
import twilightforest.util.LandmarkUtil;
import twilightforest.util.LegacyLandmarkPlacements;
import twilightforest.util.XZQuadrantIterator;
import twilightforest.world.registration.TFGenerationSettings;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class TFTeleporter implements ITeleporter {

	// destinationCoordinateCache is (src -> dest) [DestWorld, [SrcPos, DestPos]]
	private static final Map<ResourceLocation, Map<ColumnPos, PortalPosition>> destinationCoordinateCache = new HashMap<>();

	private final boolean locked;
	@Nullable
	private final BlockPos fixedDestination;

	public TFTeleporter(boolean locked) {
		this(locked, null);
	}

	/**
	 * @param fixedDestination if non-null, the destination-dimension position to search around instead of the scaled
	 *                         entity position (used to banish new players onto the surface, see CapabilityEvents)
	 */
	public TFTeleporter(boolean locked, @Nullable BlockPos fixedDestination) {
		this.locked = locked;
		this.fixedDestination = fixedDestination;
	}

	public static CompoundTag saveLinks() {
		CompoundTag tag = new CompoundTag();
		ListTag dcc = new ListTag();
		destinationCoordinateCache.forEach((rl, map) -> {
			CompoundTag ct = new CompoundTag();
			ListTag links = new ListTag();
			map.forEach((columnPos, portalPos) -> {
				CompoundTag link = new CompoundTag();
				CompoundTag column = new CompoundTag();
				column.putInt("x", columnPos.x());
				column.putInt("z", columnPos.z());
				link.put("column", column);
				CompoundTag portal = new CompoundTag();
				portal.putLong("time", portalPos.lastUpdateTime);
				portal.putLong("pos", portalPos.pos.asLong());
				link.put("portal", portal);
				links.add(link);
			});
			ct.put("links", links);
			ct.putString("name", rl.toString());
			dcc.add(ct);
		});
		tag.put("dest", dcc);
		return tag;
	}

	public static void loadLinks(CompoundTag tag) {
		tag.getList("dest", Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast).forEach(dest -> {
			ResourceLocation name = new ResourceLocation(dest.getString("name"));
			destinationCoordinateCache.putIfAbsent(name, Maps.newHashMapWithExpectedSize(4096));
			dest.getList("links", Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast).forEach(link -> {
				CompoundTag column = link.getCompound("column");
				CompoundTag portal = link.getCompound("portal");
				destinationCoordinateCache.get(name).put(new ColumnPos(column.getInt("x"), column.getInt("z")), new PortalPosition(BlockPos.of(portal.getLong("pos")), portal.getLong("time")));
			});
		});
	}

	@Nullable
	@Override
	public PortalInfo getPortalInfo(Entity entity, ServerLevel dest, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
		BlockPos destPos = this.getDestinationPos(entity, dest);
		PortalInfo info = placeInExistingPortal(dest, entity, destPos);

		if (info == null) {
			TwilightForestMod.LOGGER.debug("Did not find existing portal, making a new one.");
			info = this.createPosition(dest, entity, destPos);
		}

		if (info != null)
			return info;

		// Never fall back to the unscaled source position (#1968), use the surface of the scaled destination column
		return makePortalInfo(entity, Vec3.atCenterOf(dest.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, destPos)));
	}

	/**
	 * The position the search for an existing portal or a new portal spot starts from: the explicit destination if
	 * this teleporter was given one, otherwise the entity position scaled into the destination dimension.
	 */
	protected BlockPos getDestinationPos(Entity entity, ServerLevel dest) {
		return this.fixedDestination != null ? this.fixedDestination : getScaledDestination(entity, dest);
	}

	public static BlockPos getScaledDestination(Entity entity, ServerLevel dest) {
		double scale = getHorizontalScale(dest);
		return dest.getWorldBorder().clampToBounds(entity.blockPosition().getX() * scale, entity.blockPosition().getY(), entity.blockPosition().getZ() * scale);
	}

	@Nullable
	protected PortalInfo createPosition(ServerLevel dest, Entity entity, BlockPos destPos) {
		Vec3 safePos = moveToSafeCoords(dest, entity, destPos);
		BlockPos portalPos = this.makePortal(entity, dest, safePos);
		return placeInExistingPortal(dest, entity, portalPos);
	}

	@Nullable
	protected static PortalInfo placeInExistingPortal(ServerLevel destDim, Entity entity, BlockPos pos) {
		boolean flag = true;
		BlockPos blockpos;
		ColumnPos columnPos = new ColumnPos(entity.blockPosition().getX(), entity.blockPosition().getZ()); // Must be the position from the src dim

		PortalPosition portalPosition = destinationCoordinateCache.containsKey(destDim.dimension().location()) ? destinationCoordinateCache.get(destDim.dimension().location()).get(columnPos) : null;
		if (portalPosition != null) {
			blockpos = portalPosition.pos;
			portalPosition.lastUpdateTime = destDim.getGameTime();
			flag = false;
			// Validate that the Portal still exists
			TwilightForestMod.LOGGER.debug("Using cache, validating. {}", blockpos);
			if (blockpos == null || !destDim.getBlockState(blockpos).is(TFBlocks.TWILIGHT_PORTAL.get())) {
				// Portal was broken, we need to recreate it.
				TwilightForestMod.LOGGER.debug("Portal Invalid, recreating.");
				blockpos = null;
				destinationCoordinateCache.get(destDim.dimension().location()).remove(columnPos);
			}
		} else {
			blockpos = getPortalPosition(destDim, pos);
		}

		if (blockpos == null) {
			return null;
		} else {
			if (flag) {
				TwilightForestMod.LOGGER.debug("Caching Src Portal Blocks to {}", blockpos);
				destinationCoordinateCache.putIfAbsent(destDim.dimension().location(), Maps.newHashMapWithExpectedSize(4096));
				Map<BlockPos, Boolean> portalBlocks = new HashMap<>();
				portalBlocks.put(entity.blockPosition(), true);
				TFPortalBlock.recursivelyValidatePortal(entity.level(), entity.blockPosition(), portalBlocks, new MutableInt(0), entity.level().getBlockState(entity.blockPosition()));
				BlockPos finalBlockpos = blockpos;
				portalBlocks.forEach((blockPos, b) -> {
					if (b) {
						TwilightForestMod.LOGGER.debug("Caching {}", blockPos);
						destinationCoordinateCache.get(destDim.dimension().location()).put(new ColumnPos(blockPos.getX(), blockPos.getZ()), new PortalPosition(finalBlockpos, destDim.getGameTime()));
					}
				});
				// the last param is just an object for tracking, don't worry about it using columnPos instead of blockpos
				destDim.getChunkSource().addRegionTicket(TicketType.PORTAL, new ChunkPos(blockpos), 3, new BlockPos(columnPos.x(), blockpos.getY(), columnPos.z()));
			}

			// replace with our own placement logic: a random walkable block around the portal (#2014)
			BlockPos[] portalBorder = getBoundaryPositions(destDim, blockpos).toArray(new BlockPos[0]);
			BlockPos borderPos;
			if (portalBorder.length > 0) {
				borderPos = portalBorder[destDim.getRandom().nextInt(portalBorder.length)];
			} else {
				borderPos = blockpos;
			}

			double portalX = borderPos.getX() + 0.5;
			double portalY = borderPos.getY() + 1.0;
			double portalZ = borderPos.getZ() + 0.5;

			return makePortalInfo(entity, safePosInColumn(destDim, entity, portalX, portalY, portalZ));
		}
	}

	@Nullable
	private static BlockPos getPortalPosition(ServerLevel destDim, BlockPos pos) {
		int i = 200; // scan radius up to 200, and also un-inline this variable back into below
		double d0 = Double.MAX_VALUE;
		BlockPos result = null;

		for (int i1 = -i; i1 <= i; ++i1) {
			BlockPos blockpos2;

			for (int j1 = -i; j1 <= i; ++j1) {

				// skip positions outside current world border (MC-114796)
				if (!destDim.getWorldBorder().isWithinBounds(pos.offset(i1, 0, j1))) {
					continue;
				}

				ChunkPos chunkPos = new ChunkPos(pos.offset(i1, 0, j1));
				LevelChunk chunk = destDim.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);

				// skip chunks that aren't generated
				if (chunk == null || chunk.getFullStatus() == FullChunkStatus.INACCESSIBLE) {
					continue;
				}

				for (BlockPos blockpos1 = pos.offset(i1, getScanHeight(destDim, pos) - pos.getY(), j1); blockpos1.getY() >= destDim.getMinBuildHeight(); blockpos1 = blockpos2) {
					blockpos2 = blockpos1.below();

					// don't lookup state if inner condition would fail
					if (d0 >= 0.0D && blockpos1.distSqr(pos) >= d0) {
						continue;
					}

					// use our portal block
					if (isPortal(chunk.getBlockState(blockpos1))) {
						for (blockpos2 = blockpos1.below(); isPortal(chunk.getBlockState(blockpos2)); blockpos2 = blockpos2.below()) {
							blockpos1 = blockpos2;
						}

						float d1 = (float) blockpos1.distSqr(pos);
						if (d0 < 0.0D || d1 < d0) {
							d0 = d1;
							result = blockpos1;
							// restrict search radius to new distance
							i = Mth.ceil(Mth.sqrt(d1));
						}
					}
				}

				// mark unwatched chunks for unload
				//						if (!this.world.getPlayerChunkMap().contains(chunkPos.x, chunkPos.z)) {
				//							this.world.getChunkProvider().queueUnload(chunk);
				//						}
			}
		}
		return result;
	}

	private static int getScanHeight(ServerLevel world, BlockPos pos) {
		return getScanHeight(world, pos.getX(), pos.getZ());
	}

	private static int getScanHeight(ServerLevel world, int x, int z) {
		int worldHeight = world.getMaxBuildHeight() - 1;
		//FIXME find an alternative to getHighestSectionPosition, its marked for removal
		@SuppressWarnings("removal")
		int chunkHeight = world.getChunk(x >> 4, z >> 4).getHighestSectionPosition() + 15;
		return Math.min(worldHeight, chunkHeight);
	}

	private static boolean isPortal(BlockState state) {
		return state.getBlock() == TFBlocks.TWILIGHT_PORTAL.get();
	}

	// from the start point, builds a set of all directly adjacent non-portal blocks that can be stood on
	private static Set<BlockPos> getBoundaryPositions(ServerLevel world, BlockPos start) {
		Set<BlockPos> result = new HashSet<>(), checked = new HashSet<>();
		checked.add(start);
		checkAdjacent(world, start, checked, result);
		return result;
	}

	private static void checkAdjacent(ServerLevel world, BlockPos pos, Set<BlockPos> checked, Set<BlockPos> result) {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			BlockPos offset = pos.relative(facing);
			if (!checked.add(offset))
				continue;
			BlockState checkState = world.getBlockState(offset);
			if (isPortal(checkState)) {
				checkAdjacent(world, offset, checked, result);
			} else if (Block.isFaceFull(checkState.getCollisionShape(world, offset), Direction.UP) && world.getBlockState(offset.above()).getCollisionShape(world, offset.above()).isEmpty()) {
				result.add(offset);
			}
		}
	}

	protected static boolean isPortalAt(ServerLevel world, BlockPos pos) {
		return isPortal(world.getBlockState(pos));
	}

	// Scale the coords based on the dimension type coordinate_scale
	protected static double getHorizontalScale(ServerLevel destination) {
		ServerLevel tfDim = destination.getServer().getLevel(TFGenerationSettings.DIMENSION_KEY);
		double scale = tfDim == null ? 0.125D : tfDim.dimensionType().coordinateScale();
		return destination.dimension().equals(TFGenerationSettings.DIMENSION_KEY) ? 1F / scale : scale;
	}

	protected static Vec3 moveToSafeCoords(ServerLevel world, Entity entity, BlockPos pos) {
		// if we're in enforced progression mode, check the biomes for safety
		boolean checkProgression = LandmarkUtil.isProgressionEnforced(world);

		if (isSafeAround(world, pos, entity, checkProgression)) {
			TwilightForestMod.LOGGER.debug("Portal destination looks safe!");
			return safePosInColumn(world, entity, Vec3.atCenterOf(pos));
		}

		TwilightForestMod.LOGGER.debug("Portal destination looks unsafe, rerouting!");

		BlockPos newPos = scanIntoSafeBiomes(world, pos, entity, checkProgression);
		if (newPos != null) {
			TwilightForestMod.LOGGER.debug("Successfully found safe biome");
			return safePosInColumn(world, entity, Vec3.atCenterOf(newPos));
		}

		TwilightForestMod.LOGGER.warn("Did not find a safe portal spot.");

		return safePosInColumn(world, entity, Vec3.atCenterOf(pos));
	}

	@Nullable
	private static BlockPos scanIntoSafeBiomes(ServerLevel level, BlockPos pos, Entity entity, boolean checkProgression) {
		// Loops over the (slightly randomized) centers of the 17x17 biome cells around the destination, in approximate order of proximity
		Iterable<BlockPos> biomeCenterGrid = new DiagonalSpiralIterator<>(pos.getX() >> 4, pos.getZ() >> 4, false, 128, 16, LegacyLandmarkPlacements::getNearestCenterXZ);
		for (BlockPos biomeCenter : biomeCenterGrid) {
			// Precheck the biome at the cell center before scanning within the cell
			if (checkProgression && biomeUnsafe(level, biomeCenter, entity)) {
				continue;
			}

			// Searches every chunk in a 17x17 grid around the center, inside the biome cell.
			// XZQuadrantIterator counts its radius in steps, so 128 blocks / 16 blocks per step = 8 steps.
			Iterable<BlockPos> gridAroundCenter = new XZQuadrantIterator<>(biomeCenter.getX(), biomeCenter.getZ(), true, 128 / 16, 16, (x, z) -> new BlockPos(x, 4, z));
			for (BlockPos posInBiome : gridAroundCenter) {
				if (isSafeAround(level, posInBiome, entity, checkProgression)) {
					TwilightForestMod.LOGGER.debug("Found {} in biome-scanning for safe portal placement", posInBiome.toShortString());
					return posInBiome;
				}
			}
		}

		return null;
	}

	public static boolean isSafeAround(Level world, BlockPos pos, Entity entity, boolean checkProgression) {
		if (isUnsafe(world, pos, entity, checkProgression)) {
			return false;
		}

		for (Direction facing : Direction.Plane.HORIZONTAL) {
			if (isUnsafe(world, pos.relative(facing, 16), entity, checkProgression)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isUnsafe(Level world, BlockPos pos, Entity entity, boolean checkProgression) {
		if (!world.dimension().equals(TFGenerationSettings.DIMENSION_KEY)) {
			return false;
		}

		if (isOutsideBorder(world, pos)) {
			return true;
		}

		if (checkProgression && biomeUnsafe(world, pos, entity)) {
			return true;
		}

		return posOverlapsRestrictedStructureChunk(world, pos);
	}

	private static boolean isOutsideBorder(Level world, BlockPos pos) {
		return !world.getWorldBorder().isWithinBounds(pos);
	}

	/**
	 * True if any landmark structure has a piece reaching into the chunk at {@code pos}.
	 * Structure references are final once a chunk reaches {@link ChunkStatus#STRUCTURE_REFERENCES}, so the chunk is only
	 * generated that far instead of fully, which keeps rerouting scans cheap on the server.
	 */
	public static boolean posOverlapsRestrictedStructureChunk(Level destLevel, BlockPos pos) {
		Iterator<Holder<Structure>> landmarksInChunk = destLevel.registryAccess().registry(Registries.STRUCTURE)
			.flatMap(r -> r.getTag(StructureTagGenerator.LANDMARK))
			.map(HolderSet.ListBacked::iterator)
			.orElse(Collections.emptyIterator());

		ChunkAccess chunkAt = destLevel.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.STRUCTURE_REFERENCES);

		while (landmarksInChunk.hasNext()) {
			Holder<Structure> structureHolder = landmarksInChunk.next();
			if (!chunkAt.getReferencesForStructure(structureHolder.value()).isEmpty()) {
				return true;
			}
		}

		return false;
	}

	private static boolean biomeUnsafe(Level world, BlockPos pos, Entity entity) {
		return !Restrictions.isBiomeSafeFor(world.getBiome(pos).value(), entity);
	}

	protected BlockPos makePortal(Entity entity, ServerLevel world, Vec3 pos) {
		ServerLevel src = entity.level() instanceof ServerLevel serverLevel ? serverLevel : null;

		// ensure area is populated first
		loadSurroundingArea(world, pos);

		BlockPos spot = findPortalCoords(world, pos, blockPos -> isPortalAt(world, blockPos));
		String name = entity.getName().getString();

		if (spot != null) {
			TwilightForestMod.LOGGER.debug("Found existing portal for {} at {}", name, spot);
			cacheNewPortalCoords(src, spot, entity.blockPosition());
			return spot;
		}

		spot = findPortalCoords(world, pos, blockpos -> isIdealForPortal(world, blockpos));

		if (spot != null) {
			TwilightForestMod.LOGGER.debug("Found ideal portal spot for {} at {}", name, spot);
			cacheNewPortalCoords(src, this.makePortalAt(world, spot), entity.blockPosition());
			return spot;
		}

		TwilightForestMod.LOGGER.debug("Did not find ideal portal spot, shooting for okay one for {}", name);
		spot = findPortalCoords(world, pos, blockPos -> isOkayForPortal(world, blockPos));

		if (spot != null) {
			TwilightForestMod.LOGGER.debug("Found okay portal spot for {} at {}", name, spot);
			cacheNewPortalCoords(src, this.makePortalAt(world, spot), entity.blockPosition());
			return spot;
		}

		TwilightForestMod.LOGGER.debug("Did not even find an okay portal spot, just making a fallback one for {}", name);

		spot = findPortalCoords(world, pos, blockpos -> isOkayForFallbackPortal(world, blockpos), true);
		if (spot != null) {
			TwilightForestMod.LOGGER.debug("Found fallback portal spot for {} at {}", name, spot);
			cacheNewPortalCoords(src, this.makePortalAt(world, spot), entity.blockPosition());
			return spot;
		}

		// well I don't think we can actually just return and fail here
		TwilightForestMod.LOGGER.debug("Did not even find a fallback portal spot, just making a random one for {}", name);

		// on the surface of the scaled column, so it can neither cut into bedrock nor float in the void
		BlockPos horizontallyScaled = BlockPos.containing(entity.getX() * getHorizontalScale(world), entity.getY(), entity.getZ() * getHorizontalScale(world));
		spot = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, horizontallyScaled);
		cacheNewPortalCoords(src, this.makePortalAt(world, spot), entity.blockPosition());
		return spot;
	}

	protected static void loadSurroundingArea(ServerLevel world, Vec3 pos) {

		int x = Mth.floor(pos.x) >> 4;
		int z = Mth.floor(pos.z) >> 4;

		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				world.getChunk(x + dx, z + dz);
			}
		}
	}

	@Nullable
	protected static BlockPos findPortalCoords(ServerLevel world, Vec3 loc, Predicate<BlockPos> predicate) {
		return findPortalCoords(world, loc, predicate, false);
	}

	@Nullable
	protected static BlockPos findPortalCoords(ServerLevel world, Vec3 loc, Predicate<BlockPos> predicate, boolean makePortalInAir) {
		// adjust the height based on what world we're traveling to
		double yFactor = getYFactor(world);
		// modified copy of base Teleporter method:
		int entityX = Mth.floor(loc.x);
		int entityZ = Mth.floor(loc.z);

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		double spotWeight = -1D;
		BlockPos spot = null;

		int range = 16;
		for (int rx = entityX - range; rx <= entityX + range; rx++) {
			double xWeight = (rx + 0.5D) - loc.x;
			for (int rz = entityZ - range; rz <= entityZ + range; rz++) {
				double zWeight = (rz + 0.5D) - loc.z;

				for (int ry = getScanHeight(world, rx, rz); ry >= world.getMinBuildHeight(); ry--) {

					pos.set(rx, ry, rz);
					if (!makePortalInAir && !world.isEmptyBlock(pos)) {
						continue;
					}

					if (makePortalInAir) {
						while (ry > world.getMinBuildHeight() && world.isEmptyBlock(pos.set(rx, ry - 1, rz)) && predicate.test(pos)) {
							ry--;
						}
						pos.set(rx, ry, rz);
					} else {
						while (ry > world.getMinBuildHeight() && world.isEmptyBlock(pos.set(rx, ry - 1, rz))) {
							ry--;
						}
					}

					double yWeight = (ry + 0.5D) - loc.y * yFactor;
					double rPosWeight = xWeight * xWeight + yWeight * yWeight + zWeight * zWeight;

					if (spotWeight < 0.0D || rPosWeight < spotWeight) {
						// check from the "in ground" pos
						if (predicate.test(pos)) {
							spotWeight = rPosWeight;
							spot = pos.immutable();
						}
					}
				}
			}
		}

		return spot;
	}

	protected static double getYFactor(ServerLevel world) {
		return world.dimension().location().equals(Level.OVERWORLD.location()) ? 2.0 : 0.5;
	}

	private static void cacheNewPortalCoords(@Nullable ServerLevel srcDim, BlockPos pos, BlockPos srcPos) {
		// src/dest is backwards logic because we're caching the opposite direction
		if (srcDim == null)
			return;
		BlockPos exitPos = getPortalPosition(srcDim, srcPos);
		if (exitPos == null)
			return;
		TwilightForestMod.LOGGER.debug("Caching Dest Portal Blocks to {}", exitPos);
		destinationCoordinateCache.putIfAbsent(srcDim.dimension().location(), Maps.newHashMapWithExpectedSize(4096));
		destinationCoordinateCache.get(srcDim.dimension().location()).put(new ColumnPos(pos.getX(), pos.getZ()), new PortalPosition(exitPos, srcDim.getGameTime()));
		destinationCoordinateCache.get(srcDim.dimension().location()).put(new ColumnPos(pos.south().getX(), pos.south().getZ()), new PortalPosition(exitPos, srcDim.getGameTime()));
		destinationCoordinateCache.get(srcDim.dimension().location()).put(new ColumnPos(pos.east().getX(), pos.east().getZ()), new PortalPosition(exitPos, srcDim.getGameTime()));
		destinationCoordinateCache.get(srcDim.dimension().location()).put(new ColumnPos(pos.south().east().getX(), pos.south().east().getZ()), new PortalPosition(exitPos, srcDim.getGameTime()));
	}

	protected static boolean isIdealForPortal(ServerLevel world, BlockPos pos) {
		for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
			for (int potentialX = 0; potentialX < 4; potentialX++) {
				for (int potentialY = 0; potentialY < 6; potentialY++) {
					BlockPos tPos = pos.offset(potentialX - 1, potentialY, potentialZ - 1);
					BlockState state = world.getBlockState(tPos);

					// all blocks mustn't be bedrock, end portal frame, etc.; and other conditions for layers >= 0
					if (state.is(BlockTags.FEATURES_CANNOT_REPLACE) || potentialY == 0 && !state.is(BlockTags.DIRT) || potentialY >= 1 && !state.canBeReplaced()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected BlockPos makePortalAt(Level world, BlockPos pos) {
		// grass all around it
		BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

		world.setBlockAndUpdate(pos.west().north(), grass);
		world.setBlockAndUpdate(pos.north(), grass);
		world.setBlockAndUpdate(pos.east().north(), grass);
		world.setBlockAndUpdate(pos.east(2).north(), grass);

		world.setBlockAndUpdate(pos.west(), grass);
		world.setBlockAndUpdate(pos.east(2), grass);

		world.setBlockAndUpdate(pos.west().south(), grass);
		world.setBlockAndUpdate(pos.east(2).south(), grass);

		world.setBlockAndUpdate(pos.west().south(2), grass);
		world.setBlockAndUpdate(pos.south(2), grass);
		world.setBlockAndUpdate(pos.east().south(2), grass);
		world.setBlockAndUpdate(pos.east(2).south(2), grass);

		BlockPos[] positions = new BlockPos[4];
		positions[0] = pos.below();
		positions[1] = pos.east().below();
		positions[2] = pos.south().below();
		positions[3] = pos.east().south().below();

		// dirt under it, but only where it can't destroy bedrock or other important blocks (#2046)
		BlockState dirt = Blocks.DIRT.defaultBlockState();
		for (BlockPos blockpos : positions) {
			BlockState state = world.getBlockState(blockpos);
			if (state.is(BlockTags.DIRT) || state.is(BlockTags.REPLACEABLE) || state.isAir())
				world.setBlockAndUpdate(blockpos, dirt);
		}

		// portal in it
		BlockState portal = TFBlocks.TWILIGHT_PORTAL.get().defaultBlockState().setValue(TFPortalBlock.DISALLOW_RETURN, (this.locked || !TFConfig.COMMON_CONFIG.shouldReturnPortalBeUsable.get()));

		world.setBlock(pos, portal, Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(), portal, Block.UPDATE_CLIENTS);
		world.setBlock(pos.south(), portal, Block.UPDATE_CLIENTS);
		world.setBlock(pos.east().south(), portal, Block.UPDATE_CLIENTS);

		// meh, let's just make a bunch of air over it for 4 squares
		for (int dx = -1; dx <= 2; dx++) {
			for (int dz = -1; dz <= 2; dz++) {
				for (int dy = 1; dy <= 5; dy++) {
					world.removeBlock(pos.offset(dx, dy, dz), false);
				}
			}
		}

		// finally, "nature decorations"!
		world.setBlock(pos.west().north().above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);
		world.setBlock(pos.north().above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east().north().above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(2).north().above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);

		world.setBlock(pos.west().above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(2).above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);

		world.setBlock(pos.west().south().above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(2).south().above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);

		world.setBlock(pos.west().south(2).above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);
		world.setBlock(pos.south(2).above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east().south(2).above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);
		world.setBlock(pos.east(2).south(2).above(), randNatureBlock(world.random), Block.UPDATE_CLIENTS);

		return pos;
	}

	private static BlockState randNatureBlock(RandomSource random) {
		return ForgeRegistries.BLOCKS.tags().getTag(BlockTagGenerator.GENERATED_PORTAL_DECO).getRandomElement(random).get().defaultBlockState();
	}

	protected static boolean isOkayForPortal(ServerLevel world, BlockPos pos) {
		for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
			for (int potentialX = 0; potentialX < 4; potentialX++) {
				for (int potentialY = 0; potentialY < 6; potentialY++) {
					BlockPos tPos = pos.offset(potentialX - 1, potentialY, potentialZ - 1);
					BlockState state = world.getBlockState(tPos);

					// all blocks mustn't be bedrock, end portal frame, etc.; and other conditions for layers >= 0
					if (state.is(BlockTags.FEATURES_CANNOT_REPLACE) || potentialY == 0 && !state.isSolid() && !state.liquid() || potentialY >= 1 && !state.canBeReplaced()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// like isOkayForPortal, but the portal may hang in the air
	protected static boolean isOkayForFallbackPortal(ServerLevel world, BlockPos pos) {
		for (int potentialZ = 0; potentialZ < 4; potentialZ++) {
			for (int potentialX = 0; potentialX < 4; potentialX++) {
				for (int potentialY = 0; potentialY < 6; potentialY++) {
					BlockPos tPos = pos.offset(potentialX - 1, potentialY, potentialZ - 1);
					BlockState state = world.getBlockState(tPos);

					// all blocks mustn't be bedrock, end portal frame, etc.;
					if (state.is(BlockTags.FEATURES_CANNOT_REPLACE) || potentialY >= 1 && !state.canBeReplaced()) {
						return false;
					}
				}
			}
		}
		return true;
	}

	protected static Vec3 safePosInColumn(ServerLevel level, Entity entity, double x, double y, double z) {
		return safePosInColumn(level, entity, new Vec3(x, y, z));
	}

	/**
	 * Returns {@code pos} if the entity fits there, otherwise the same column on top of the motion-blocking (non-leaf)
	 * surface, so it is never placed inside blocks.
	 */
	protected static Vec3 safePosInColumn(ServerLevel level, Entity entity, Vec3 pos) {
		BlockPos blockPos = BlockPos.containing(pos);
		// collisions in unloaded chunks are ignored, so make sure the column is actually there before testing it
		level.getChunkAt(blockPos);

		AABB aabb = entity.getDimensions(entity.getPose()).makeBoundingBox(pos);

		if (level.noCollision(aabb)) {
			return pos;
		}

		int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos.getX(), blockPos.getZ());
		return pos.with(Direction.Axis.Y, height);
	}

	protected static PortalInfo makePortalInfo(Entity entity, Vec3 pos) {
		return new PortalInfo(pos, Vec3.ZERO, entity.getYRot(), entity.getXRot());
	}

	@Override
	public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
		entity.fallDistance = 0;
		return repositionEntity.apply(false);
	}

	static class PortalPosition {
		public final BlockPos pos;
		long lastUpdateTime;

		PortalPosition(BlockPos pos, long time) {
			this.pos = pos;
			this.lastUpdateTime = time;
		}
	}
}
