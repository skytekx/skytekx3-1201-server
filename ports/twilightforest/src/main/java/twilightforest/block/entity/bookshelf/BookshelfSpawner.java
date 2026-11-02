package twilightforest.block.entity.bookshelf;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.block.ChiseledCanopyShelfBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Spawner logic of a {@link ChiseledCanopyShelfBlockEntity} in spawner mode: every spawn "uses up" one of the books on the
 * shelf, so a trapped shelf only releases as many mobs (Death Tomes by default) as it holds books.
 */
public abstract class BookshelfSpawner {
	public static final List<Pair<Integer, BooleanProperty>> SLOT_PROPERTIES_AND_INDEXES = List.of(
		Pair.of(0, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_0_OCCUPIED),
		Pair.of(1, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_1_OCCUPIED),
		Pair.of(2, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_2_OCCUPIED),
		Pair.of(3, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_3_OCCUPIED),
		Pair.of(4, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_4_OCCUPIED),
		Pair.of(5, BlockStateProperties.CHISELED_BOOKSHELF_SLOT_5_OCCUPIED));
	public int maxNearbyEntities = 4;
	public int spawnRange = 4;
	public int spawnCheckRange = 12;
	private int spawnDelay = 20;
	private SimpleWeightedRandomList<SpawnData> spawnPotentials = SimpleWeightedRandomList.empty();
	@Nullable
	private SpawnData nextSpawnData;
	private int minSpawnDelay = 200;
	private int maxSpawnDelay = 400;
	private int requiredPlayerRange = 8;

	public void setEntityId(EntityType<?> type, @Nullable Level level, RandomSource random, BlockPos pos) {
		this.getOrCreateNextSpawnData(level, random, pos)
			.getEntityToSpawn()
			.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
	}

	// 4.8 asks the 1.20.3+ trial spawner PlayerDetector.INCLUDING_CREATIVE_PLAYERS (line of sight required); 1.20.1 has no
	// PlayerDetector, so this is the same test written out: any non-spectator within range that can see the shelf.
	private boolean isNearPlayer(ServerLevel level, BlockPos pos) {
		Vec3 center = Vec3.atCenterOf(pos);
		for (ServerPlayer player : level.players()) {
			if (!player.isSpectator() && player.blockPosition().closerThan(pos, this.requiredPlayerRange) && inLineOfSight(level, center, player.getEyePosition())) {
				return true;
			}
		}
		return false;
	}

	private static boolean inLineOfSight(Level level, Vec3 pos, Vec3 targetPos) {
		BlockHitResult result = level.clip(new ClipContext(targetPos, pos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null));
		return result.getBlockPos().equals(BlockPos.containing(pos)) || result.getType() == HitResult.Type.MISS;
	}

	public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
		if (this.isNearPlayer(level, pos)) {
			if (this.spawnDelay == -1) {
				this.delay(level, pos);
			}

			if (this.spawnDelay > 0) {
				this.spawnDelay--;
			} else {
				List<Pair<Integer, BooleanProperty>> filledSlots = new ArrayList<>(SLOT_PROPERTIES_AND_INDEXES);
				filledSlots.removeIf(pair -> !state.getValue(pair.getSecond()));
				Collections.shuffle(filledSlots);

				for (Pair<Integer, BooleanProperty> filledSlot : filledSlots) {
					BooleanProperty property = filledSlot.getSecond();
					if (state.hasProperty(property) && state.getValue(property)) {
						if (this.attemptSpawnTome(filledSlot.getFirst(), level, pos, false, null, 0)) {
							this.delay(level, pos);
							break;
						}
					}
				}

				int fullSlots = 0;
				for (BooleanProperty property : ChiseledCanopyShelfBlock.SLOT_OCCUPIED_PROPERTIES) {
					if (state.hasProperty(property) && state.getValue(property)) {
						fullSlots++;
					}
				}

				if (fullSlots == 0) {
					level.setBlockAndUpdate(pos, state.setValue(ChiseledCanopyShelfBlock.SPAWNER, false));
				}
			}
		}
	}

	private void delay(Level level, BlockPos pos) {
		RandomSource randomsource = level.getRandom();
		if (this.maxSpawnDelay <= this.minSpawnDelay) {
			this.spawnDelay = this.minSpawnDelay;
		} else {
			this.spawnDelay = this.minSpawnDelay + randomsource.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
		}

		this.spawnPotentials.getRandom(randomsource).ifPresent(entry -> this.setNextSpawnData(level, pos, entry.getData()));
		this.broadcastEvent(level, pos, 1);
	}

	public void load(@Nullable Level level, BlockPos pos, CompoundTag tag) {
		this.spawnDelay = tag.getShort("Delay");
		boolean flag = tag.contains("SpawnData", 10);
		if (flag) {
			SpawnData spawndata = SpawnData.CODEC
				.parse(NbtOps.INSTANCE, tag.getCompound("SpawnData"))
				.resultOrPartial(error -> TwilightForestMod.LOGGER.warn("Death Tome Spawner: Invalid SpawnData: {}", error))
				.orElseGet(SpawnData::new);
			this.setNextSpawnData(level, pos, spawndata);
		}

		boolean flag1 = tag.contains("SpawnPotentials", 9);
		if (flag1) {
			ListTag listtag = tag.getList("SpawnPotentials", 10);
			this.spawnPotentials = SpawnData.LIST_CODEC
				.parse(NbtOps.INSTANCE, listtag)
				.resultOrPartial(error -> TwilightForestMod.LOGGER.warn("Death Tome Spawner: Invalid SpawnPotentials list: {}", error))
				.orElseGet(SimpleWeightedRandomList::empty);
		} else {
			this.spawnPotentials = SimpleWeightedRandomList.single(this.nextSpawnData != null ? this.nextSpawnData : new SpawnData());
		}

		if (tag.contains("MinSpawnDelay", 99)) {
			this.minSpawnDelay = tag.getShort("MinSpawnDelay");
			this.maxSpawnDelay = tag.getShort("MaxSpawnDelay");
		}

		if (tag.contains("MaxNearbyEntities", 99)) {
			this.maxNearbyEntities = tag.getShort("MaxNearbyEntities");
			this.requiredPlayerRange = tag.getShort("RequiredPlayerRange");
		}

		if (tag.contains("SpawnRange", 99)) {
			this.spawnRange = tag.getShort("SpawnRange");
		}

		if (tag.contains("SpawnCheckRange", 99)) {
			this.spawnCheckRange = tag.getShort("SpawnCheckRange");
		}
	}

	public CompoundTag save(CompoundTag tag) {
		tag.putShort("Delay", (short) this.spawnDelay);
		tag.putShort("MinSpawnDelay", (short) this.minSpawnDelay);
		tag.putShort("MaxSpawnDelay", (short) this.maxSpawnDelay);
		tag.putShort("MaxNearbyEntities", (short) this.maxNearbyEntities);
		tag.putShort("RequiredPlayerRange", (short) this.requiredPlayerRange);
		tag.putShort("SpawnRange", (short) this.spawnRange);
		tag.putShort("SpawnCheckRange", (short) this.spawnCheckRange);
		if (this.nextSpawnData != null) {
			tag.put(
				"SpawnData",
				SpawnData.CODEC
					.encodeStart(NbtOps.INSTANCE, this.nextSpawnData)
					.result()
					.orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
			);
		}

		tag.put("SpawnPotentials", SpawnData.LIST_CODEC.encodeStart(NbtOps.INSTANCE, this.spawnPotentials).result().orElseThrow());
		return tag;
	}

	public boolean onEventTriggered(Level level, int id) {
		if (id == 1) {
			if (level.isClientSide()) {
				this.spawnDelay = this.minSpawnDelay;
			}

			return true;
		} else {
			return false;
		}
	}

	protected void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData data) {
		this.nextSpawnData = data;
	}

	@Nullable
	public SpawnData getNextSpawnData() {
		return this.nextSpawnData;
	}

	private SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource random, BlockPos pos) {
		if (this.nextSpawnData == null) {
			this.setNextSpawnData(level, pos, this.spawnPotentials.getRandom(random).map(WeightedEntry.Wrapper::getData).orElseGet(SpawnData::new));
		}
		return this.nextSpawnData;
	}

	public abstract void broadcastEvent(Level level, BlockPos pos, int id);

	public boolean attemptSpawnTome(int slot, ServerLevel level, BlockPos pos, boolean fire, @Nullable LivingEntity assailant, int maxTries) {
		RandomSource random = level.getRandom();
		SpawnData data = this.getOrCreateNextSpawnData(level, random, pos);
		CompoundTag tag = data.getEntityToSpawn();
		BlockState shelf = level.getBlockState(pos);
		Direction facing = shelf.getValue(HorizontalDirectionalBlock.FACING);
		Optional<EntityType<?>> optional = EntityType.by(tag);
		//if the assigned entity doesn't exist or the bookshelf is blocked off, fail early
		if (optional.isEmpty() || !level.getBlockState(pos.relative(facing)).canBeReplaced()) {
			this.delay(level, pos);
			return false;
		}

		//pick random spot in front of the shelf
		double x = pos.relative(facing).getX() + (random.nextDouble() - random.nextDouble()) * 2.0D;
		double y = (double) pos.getY() + (random.nextDouble() - random.nextDouble());
		double z = pos.relative(facing).getZ() + (random.nextDouble() - random.nextDouble()) * 2.0D;

		//apply spawning logic like vanilla spawners do
		if (level.noCollision(optional.get().getAABB(x, y, z))) {
			//hostile mobs never leave the shelf on peaceful, so the book stays where it is
			boolean difficultyPreventsSpawn = !optional.get().getCategory().isFriendly() && level.getDifficulty() == Difficulty.PEACEFUL;

			BlockPos blockpos = BlockPos.containing(x, y, z);
			if (data.getCustomSpawnRules().isPresent()) {
				if (difficultyPreventsSpawn) {
					return false;
				}

				// SpawnData.CustomSpawnRules#isValidPosition is 1.20.3+; this is its body, as 1.20.1's BaseSpawner inlines it
				SpawnData.CustomSpawnRules rules = data.getCustomSpawnRules().get();
				boolean validPosition = rules.blockLightLimit().isValueInRange(level.getBrightness(LightLayer.BLOCK, blockpos))
					&& rules.skyLightLimit().isValueInRange(level.getBrightness(LightLayer.SKY, blockpos));
				if (!validPosition && !fire) {
					return false;
				}
			} else if (difficultyPreventsSpawn) {
				this.delay(level, pos);
				return false;
			}

			Entity entity = EntityType.loadEntityRecursive(tag, level, processed -> {
				processed.moveTo(x, y, z, processed.getYRot(), processed.getXRot());
				//set entity on fire if told to do so
				if (fire) {
					processed.setRemainingFireTicks(200);
				}

				//target whoever was responsible for spawning the mob
				if (assailant != null && processed instanceof Mob mob) {
					mob.setTarget(assailant);
				}

				return processed;
			});
			if (entity == null) {
				this.delay(level, pos);
				return false;
			}

			// EntityTypeTest.forExactClass is 1.20.3+: count entities of exactly this class, subclasses excluded
			Class<? extends Entity> entityClass = entity.getClass();
			int k = level.getEntitiesOfClass(entityClass, new AABB(pos).inflate(this.spawnCheckRange), nearby -> nearby.getClass() == entityClass && EntitySelector.NO_SPECTATORS.test(nearby)).size();
			if (k >= this.maxNearbyEntities && !fire) {
				this.delay(level, pos);
				return false;
			}

			entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), random.nextFloat() * 360.0F, 0.0F);
			if (entity instanceof Mob mob) {
				// NeoForge's EventHooks.finalizeMobSpawnSpawner, done the way Forge 47's BaseSpawner does it. The shelf is no
				// BaseSpawner, so the event carries no spawner reference.
				MobSpawnEvent.FinalizeSpawn event = ForgeEventFactory.onFinalizeSpawnSpawner(mob, level, level.getCurrentDifficultyAt(entity.blockPosition()), null, tag, null);
				if (event != null && data.getEntityToSpawn().size() == 1 && data.getEntityToSpawn().contains("id", 8)) {
					mob.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), event.getSpawnData(), event.getSpawnTag());
				}
			}

			if (!level.tryAddFreshEntityWithPassengers(entity)) {
				this.delay(level, pos);
				return false;
			}

			level.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
			if (entity instanceof Mob mob) {
				mob.spawnAnim();
			}

			//after mob is spawned, clear that book's spot from the shelf
			if (level.getBlockEntity(pos) instanceof ChiseledCanopyShelfBlockEntity be) {
				be.consumeBook(slot);
			}
			return true;
		} else if (maxTries != 0) {
			return this.attemptSpawnTome(slot, level, pos, fire, assailant, maxTries - 1);
		}
		return false;
	}
}
