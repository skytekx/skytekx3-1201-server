package twilightforest.util;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.init.TFSounds;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

public class EntityUtil {

	public static <T extends Mob & EnforcedHomePoint> BlockPos bossChestLocation(T boss) {
		return !boss.isRestrictionPointValid(boss.level().dimension()) ? boss.blockPosition() : boss.getRestrictionPoint().pos().below();
	}

	public static boolean canDestroyBlock(Level world, BlockPos pos, Entity entity) {
		return canDestroyBlock(world, pos, world.getBlockState(pos), entity);
	}

	public static boolean canDestroyBlock(Level world, BlockPos pos, BlockState state, Entity entity) {
		float hardness = state.getDestroySpeed(world, pos);
		return hardness >= 0f && hardness < 50f && !state.isAir()
				&& !(world.getBlockEntity(pos) instanceof Container)
				&& state.getBlock().canEntityDestroy(state, world, pos, entity)
				&& (/* rude type limit */!(entity instanceof LivingEntity)
				|| ForgeEventFactory.onEntityDestroyBlock((LivingEntity) entity, pos, state));
	}

	/**
	 * [VanillaCopy] Entity.pick
	 */
	public static BlockHitResult rayTrace(Entity entity, double range) {
		Vec3 position = entity.getEyePosition(1.0F);
		Vec3 look = entity.getViewVector(1.0F);
		Vec3 dest = position.add(look.x * range, look.y * range, look.z * range);
		return entity.level().clip(new ClipContext(position, dest, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
	}

	public static BlockHitResult rayTrace(Player player) {
		return rayTrace(player, null);
	}

	public static BlockHitResult rayTrace(Player player, @Nullable DoubleUnaryOperator modifier) {
		double range = player.getAttribute(ForgeMod.BLOCK_REACH.get()).getValue();
		return rayTrace(player, modifier == null ? range : modifier.applyAsDouble(range));
	}

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
	private static final Method LivingEntity_getDeathSound = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_5592_");
	private static final MethodHandle handle_LivingEntity_getDeathSound;
	private static final Method HangingEntity_setDirection = ObfuscationReflectionHelper.findMethod(HangingEntity.class, "m_6022_", Direction.class);
	private static final MethodHandle handle_HangingEntity_setDirection;

	static {
		MethodHandle tmp_handle_LivingEntity_getDeathSound = null;
		MethodHandle tmp_handle_HangingEntity_setDirection = null;

		try {
			tmp_handle_LivingEntity_getDeathSound = LOOKUP.unreflect(LivingEntity_getDeathSound);
			tmp_handle_HangingEntity_setDirection = LOOKUP.unreflect(HangingEntity_setDirection);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		handle_LivingEntity_getDeathSound = tmp_handle_LivingEntity_getDeathSound;
		handle_HangingEntity_setDirection = tmp_handle_HangingEntity_setDirection;
	}

	@Nullable
	public static SoundEvent getDeathSound(LivingEntity living) {
		SoundEvent sound = null;
		if (handle_LivingEntity_getDeathSound != null) {
			try {
				sound = (SoundEvent) handle_LivingEntity_getDeathSound.invokeExact(living);
			} catch (Throwable e) {
				// FAIL SILENTLY
			}
		}
		return sound;
	}

	public static void killLavaAround(Entity entity) {
		AABB bounds = entity.getBoundingBox().inflate(9D);
		for (double x = bounds.minX; x < bounds.maxX; x++) {
			for (double z = bounds.minZ; z < bounds.maxZ; z++) {
				for (double y = bounds.minY; y < bounds.maxY; y++) {
					BlockPos pos = BlockPos.containing(x, y, z);
					BlockState state = entity.level().getBlockState(pos);
					if (state.is(Blocks.LAVA)) {
						entity.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
					}
				}
			}
		}
	}

	//copy of Mob.doHurtTarget, allows for using a custom DamageSource instead of the generic Mob Attack one
	public static boolean properlyApplyCustomDamageSource(Mob entity, Entity victim, DamageSource source) {
		return properlyApplyCustomDamageSource(entity, victim, source, null);
	}

	/**
	 * @param flingSound if not null, a hit victim is also flung upwards in the attacker's direction and this sound plays (a charging
	 *                   Minotaur or Minoshroom)
	 */
	public static boolean properlyApplyCustomDamageSource(Mob entity, Entity victim, DamageSource source, @Nullable SoundEvent flingSound) {
		float f = (float)entity.getAttributeValue(Attributes.ATTACK_DAMAGE);
		float f1 = (float)entity.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
		if (victim instanceof LivingEntity) {
			f += EnchantmentHelper.getDamageBonus(entity.getMainHandItem(), ((LivingEntity)victim).getMobType());
			f1 += (float)EnchantmentHelper.getKnockbackBonus(entity);
		}

		int i = EnchantmentHelper.getFireAspect(entity);
		if (i > 0) {
			victim.setSecondsOnFire(i * 4);
		}

		boolean flag = victim.hurt(source, f);
		if (flag) {
			if (f1 > 0.0F && victim instanceof LivingEntity) {
				((LivingEntity)victim).knockback(f1 * 0.5F, Mth.sin(entity.getYRot() * ((float)Math.PI / 180F)), -Mth.cos(entity.getYRot() * ((float)Math.PI / 180F)));
				entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
			}

			if (flingSound != null) {
				victim.push(entity.getDirection().getStepX(), 0.35D, entity.getDirection().getStepZ());
				entity.playSound(flingSound, 1.0F, 1.0F);
			}

			if (victim instanceof Player player) {
				entity.maybeDisableShield(player, entity.getMainHandItem(), player.isUsingItem() ? player.getUseItem() : ItemStack.EMPTY);
			}

			entity.doEnchantDamageEffects(entity, victim);
			entity.setLastHurtMob(victim);
		}

		return flag;
	}

	// [VanillaCopy] with modifications: StructureTemplate.createEntityIgnoreException
	@Nullable
	private static <T extends Entity> T createEntityIgnoreException(EntityType<T> type, ServerLevelAccessor levelAccessor) {
		try {
			return type.create(levelAccessor.getLevel());
		} catch (Exception exception) {
			return null;
		}
	}

	public static void tryHangPainting(WorldGenLevel world, BlockPos pos, Direction direction, @Nullable ResourceKey<PaintingVariant> chosenPainting) {
		if (chosenPainting == null) return;

		Painting painting = createEntityIgnoreException(EntityType.PAINTING, world);

		painting.setPos(pos.getX(), pos.getY(), pos.getZ());
		try {
			handle_HangingEntity_setDirection.invoke(painting, direction);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
		painting.setVariant(ForgeRegistries.PAINTING_VARIANTS.getHolder(chosenPainting).get());

		if (checkValidPaintingPosition(world, painting))
			world.addFreshEntity(painting);
	}

	// TF 4.8 variant that takes a variant holder and reports whether the painting was hung
	public static boolean tryHangPainting(WorldGenLevel world, BlockPos pos, Direction direction, @Nullable Holder<PaintingVariant> chosenPainting) {
		if (chosenPainting == null) return false;

		Painting painting = createEntityIgnoreException(EntityType.PAINTING, world);

		if (painting == null) return false;

		painting.setPos(pos.getX(), pos.getY(), pos.getZ());
		try {
			handle_HangingEntity_setDirection.invoke(painting, direction);
		} catch (Throwable throwable) {
			throwable.printStackTrace();

			return false;
		}
		painting.setVariant(chosenPainting);

		if (checkValidPaintingPosition(world, painting)) {
			world.addFreshEntity(painting);
			return true;
		}

		return false;
	}

	/**
	 * TF 4.8's LichBossRoom.placePainting(): tries the paintings of the tag that fit in width x height blocks, in random order and with a
	 * bias towards the larger ones, until one hangs or limitTries more attempts have failed. Width and height are in blocks; 1.20.1's
	 * PaintingVariant measures in pixels, 16 per block.
	 */
	public static boolean placePainting(BlockPos pos, RandomSource random, int limitTries, TagKey<PaintingVariant> paintings, WorldGenLevel genLevel, int width, int height, Direction dir) {
		int maxArea = width * height;

		// 1.20.1's Util.shuffle() only takes an ObjectArrayList; same algorithm as 1.21's List overload
		ObjectArrayList<Holder<PaintingVariant>> paintingsOfSizeOrSmaller = new ObjectArrayList<>(getPaintingsOfSizeOrSmaller(genLevel, paintings, width, height));
		Util.shuffle(paintingsOfSizeOrSmaller, random);
		for (Holder<PaintingVariant> paintingHolder : paintingsOfSizeOrSmaller) {
			PaintingVariant painting = paintingHolder.value();
			int area = (painting.getWidth() / 16) * (painting.getHeight() / 16);
			if (random.nextInt(maxArea) <= area) {
				if (tryHangPainting(genLevel, pos, dir, paintingHolder) || limitTries-- <= 0) {
					return true;
				}
			}
		}
		return false;
	}

	// Paintings of the tag no larger than width x height blocks
	public static List<Holder<PaintingVariant>> getPaintingsOfSizeOrSmaller(WorldGenLevel level, TagKey<PaintingVariant> paintings, int width, int height) {
		List<Holder<PaintingVariant>> valid = new ArrayList<>();

		for (Holder<PaintingVariant> art : level.registryAccess().registryOrThrow(Registries.PAINTING_VARIANT).getTagOrEmpty(paintings)) {
			if (art.value().getWidth() <= width * 16 && art.value().getHeight() <= height * 16) {
				valid.add(art);
			}
		}

		return valid;
	}

	@Nullable
	public static ResourceKey<PaintingVariant> getPaintingOfSize(RandomSource rand, int minSize) {
		return getPaintingOfSize(rand, minSize, minSize, false);
	}

	@Nullable
	public static ResourceKey<PaintingVariant> getPaintingOfSize(RandomSource rand, int width, int height, boolean exactMeasurements) {
		List<ResourceKey<PaintingVariant>> valid = new ArrayList<>();

		for (PaintingVariant art : ForgeRegistries.PAINTING_VARIANTS.tags().getTag(PaintingVariantTags.PLACEABLE)) {
			if (exactMeasurements) {
				if (art.getWidth() == width && art.getHeight() == height) {
					valid.add(ResourceKey.create(Registries.PAINTING_VARIANT, Objects.requireNonNull(ForgeRegistries.PAINTING_VARIANTS.getKey(art))));
				}
			} else {
				if (art.getWidth() >= width || art.getHeight() >= height) {
					valid.add(ResourceKey.create(Registries.PAINTING_VARIANT, Objects.requireNonNull(ForgeRegistries.PAINTING_VARIANTS.getKey(art))));
				}
			}
		}

		if (valid.size() > 0) {
			return valid.get(rand.nextInt(valid.size()));
		} else {
			return null;
		}
	}

	public static boolean checkValidPaintingPosition(WorldGenLevel world, @Nullable Painting painting) {
		if (painting == null) {
			return false;
		}

		final AABB largerBox = painting.getBoundingBox();

		if (!world.noCollision(painting, largerBox)) {
			return false;
		} else {
			List<Entity> collidingEntities = getEntitiesInAABB(world, largerBox);

			for (Entity entityOnList : collidingEntities) {
				if (entityOnList instanceof HangingEntity) {
					return false;
				}
			}

			return true;
		}
	}

	public static List<Entity> getEntitiesInAABB(WorldGenLevel world, AABB boundingBox) {
		List<Entity> list = Lists.newArrayList();
		int i = Mth.floor((boundingBox.minX - 2) / 16.0D);
		int j = Mth.floor((boundingBox.maxX + 2) / 16.0D);
		int k = Mth.floor((boundingBox.minZ - 2) / 16.0D);
		int l = Mth.floor((boundingBox.maxZ + 2) / 16.0D);

		for (int i1 = i; i1 <= j; ++i1) {
			for (int j1 = k; j1 <= l; ++j1) {
				ChunkAccess chunk = world.getChunk(i1, j1, ChunkStatus.STRUCTURE_STARTS);
				if (chunk instanceof ProtoChunk proto) {
					proto.getEntities().forEach(nbt -> {
						Entity entity = EntityType.loadEntityRecursive(nbt, world.getLevel(), e -> e);
						if (entity != null && boundingBox.intersects(entity.getBoundingBox())) {
							list.add(entity);
						}
					});
				}
			}
		}

		return list;
	}

	/**
	 * Turns a living entity into another entity type, carrying over as much of its data as the new type can read.
	 * [VanillaCopy]-ish port of TF 4.8's EntityUtil.convertEntity (used by ominous fire kills).
	 *
	 * @return true if the entity was converted
	 */
	@SuppressWarnings("unchecked")
	public static boolean convertEntity(LivingEntity oldEntity, EntityType<?> newType) {
		if (!(oldEntity.level() instanceof ServerLevel level)) return false;
		Entity newEntity = newType.create(level);
		if (newEntity == null) return false;
		if (!(newEntity instanceof LivingEntity living) || ForgeEventFactory.canLivingConvert(oldEntity, (EntityType<? extends LivingEntity>) living.getType(), timer -> {})) {
			List<Entity> passengerSave = oldEntity.getPassengers();
			if (oldEntity instanceof Mob mob && newEntity instanceof Mob newMob) {
				newEntity = mob.convertTo((EntityType<? extends Mob>) newMob.getType(), true);
				if (newEntity == null) return false;
			} else {
				newEntity.copyPosition(oldEntity);

				// 4.8 also moves the old mob's equipment over here, but that branch is unreachable: two mobs always take convertTo above
				if (newEntity instanceof Mob mob) {
					ForgeEventFactory.onFinalizeSpawn(mob, level, level.getCurrentDifficultyAt(oldEntity.blockPosition()), MobSpawnType.CONVERSION, null, null);
				}

				level.addFreshEntity(newEntity);
				oldEntity.discard();
			}
			try { // try copying what can be copied
				UUID uuid = newEntity.getUUID();
				CompoundTag old = oldEntity.saveWithoutId(new CompoundTag());
				// Deliberate fix over 4.8, which copies these too: the old mob's attribute base values would replace the new
				// type's (a zombie villager kept the villager's 0.5 movement speed), and its equipment lists, already emptied
				// by convertTo, would wipe the gear convertTo just moved over, so a piglin's weapon vanished instead of dropping
				old.remove("Attributes");
				old.remove("ArmorItems");
				old.remove("HandItems");
				old.remove("ArmorDropChances");
				old.remove("HandDropChances");
				CompoundTag merged = newEntity.saveWithoutId(new CompoundTag());
				for (String key : old.getAllKeys()) {
					Tag value = old.get(key);
					if (value != null) merged.put(key, value);
				}
				newEntity.load(merged);
				newEntity.setUUID(uuid);
				if (newEntity instanceof LivingEntity newLiving) {
					newLiving.setHealth(newLiving.getMaxHealth());
				}
			} catch (Exception e) {
				TwilightForestMod.LOGGER.warn("Couldn't transform entity NBT data", e);
			}

			if (oldEntity instanceof Saddleable saddleable && saddleable.isSaddled() && !(newEntity instanceof Saddleable)) {
				newEntity.spawnAtLocation(Items.SADDLE);
			}

			if (newEntity instanceof Mob mob) {
				mob.spawnAnim();
				mob.spawnAnim();

				for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
					ItemStack itemstack = mob.getItemBySlot(equipmentslot).copyAndClear();
					mob.spawnAtLocation(itemstack);
				}
			}

			if (!passengerSave.isEmpty()) {
				for (Entity entity : passengerSave) {
					entity.startRiding(newEntity, true);
				}
			}

			if (newEntity instanceof LivingEntity newLiving) ForgeEventFactory.onLivingConvert(oldEntity, newLiving);
			level.playSound(null, newEntity.blockPosition(), TFSounds.POWDER_USE.get(), newEntity.getSoundSource());
			return true;
		}
		return false;
	}

	// 4.8 keeps this profile in the twilightforest:zombified_player data attachment; here it lives in the zombie's persistent data
	private static final String ZOMBIFIED_PLAYER_TAG = "twilightforest:zombified_player";

	/**
	 * Marks a zombie as the risen remains of a player killed by ominous fire.
	 */
	public static void setZombifiedPlayer(Entity zombie, GameProfile profile) {
		CompoundTag tag = new CompoundTag();
		tag.put("id", NbtUtils.createUUID(profile.getId()));
		tag.putString("name", profile.getName());
		zombie.getPersistentData().put(ZOMBIFIED_PLAYER_TAG, tag);
	}

	/**
	 * @return the profile (uuid and name) of the player whose remains this zombie is, or null if it is an ordinary zombie
	 */
	@Nullable
	public static GameProfile getZombifiedPlayer(Entity zombie) {
		if (!zombie.getPersistentData().contains(ZOMBIFIED_PLAYER_TAG, Tag.TAG_COMPOUND)) return null;
		CompoundTag tag = zombie.getPersistentData().getCompound(ZOMBIFIED_PLAYER_TAG);
		if (!tag.hasUUID("id")) return null;
		return new GameProfile(tag.getUUID("id"), tag.getString("name"));
	}
}
