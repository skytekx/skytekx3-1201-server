package twilightforest.entity.boss;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFConfig;
import twilightforest.init.TFSounds;
import twilightforest.loot.TFLootTables;
import twilightforest.network.ParticlePacket;
import twilightforest.network.TFPacketHandler;

import java.util.List;

public interface IBossLootBuffer {
	int CONTAINER_SIZE = 27;

	default ItemStack getItem(int slot) {
		return this.getItemStacks().get(slot);
	}

	default void setItem(int slot, ItemStack stack) {
		this.getItemStacks().set(slot, stack);
		if (!stack.isEmpty() && stack.getCount() > 64) {
			stack.setCount(64);
		}
	}

	default void addDeathItemsSaveData(CompoundTag tag) {
		ContainerHelper.saveAllItems(tag, this.getItemStacks());
	}

	default void readDeathItemsSaveData(CompoundTag tag) {
		ContainerHelper.loadAllItems(tag, this.getItemStacks());
	}

	static <T extends LivingEntity & IBossLootBuffer> void saveDropsIntoBoss(T boss, LootParams params, ServerLevel serverLevel) {
		if (TFConfig.COMMON_CONFIG.bossDropChests.get() && canDropLoot(serverLevel)) {
			LootTable table = serverLevel.getServer().getLootData().getLootTable(boss.getLootTable());
			ObjectArrayList<ItemStack> stacks = table.getRandomItems(params);
			boss.fill(boss, params, table);

			//If our loot stack size is bigger than the inventory, drop everything else outside it. Don't want to lose any loot now do we?
			if (stacks.size() > CONTAINER_SIZE) {
				for (ItemStack stack : stacks.subList(CONTAINER_SIZE, stacks.size())) {
					ItemEntity item = new ItemEntity(serverLevel, boss.getX(), boss.getY(), boss.getZ(), stack);
					item.setExtendedLifetime();
					item.setNoPickUpDelay();
					serverLevel.addFreshEntity(item);
				}
			}
		}
	}

	static <T extends LivingEntity & IBossLootBuffer> void depositDropsIntoChest(T boss, BlockState chest, BlockPos pos, ServerLevel serverLevel) {
		if (TFConfig.COMMON_CONFIG.bossDropChests.get() && canDropLoot(serverLevel)) {
			BlockPos chestPos = placeLootChest(serverLevel, chest, pos);
			if (chestPos != null && serverLevel.getBlockEntity(chestPos) instanceof Container container) {
				for (int i = 0; i < CONTAINER_SIZE && i < container.getContainerSize(); i++) {
					container.setItem(i, boss.getItem(i));
				}
			} else {
				//No room for a chest anywhere above the drop spot: drop the loot into the world instead of losing it
				Vec3 dropPos = Vec3.atCenterOf(pos);
				for (int i = 0; i < CONTAINER_SIZE; i++) {
					dropLootItem(serverLevel, dropPos, boss.getItem(i));
				}
				chestPos = pos;
			}
			celebrateAt(boss, Vec3.atCenterOf(chestPos), serverLevel);
		}
	}

	/**
	 * Bosses honour the doMobLoot gamerule like every other mob: with it off they leave neither a loot chest nor any drops.
	 */
	static boolean canDropLoot(Level level) {
		return level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
	}

	/**
	 * Drops boss loot as an item entity that does not despawn early and can be picked up immediately. Unlike
	 * {@link net.minecraft.world.level.block.Block#popResource}, this ignores the doTileDrops gamerule: boss loot is mob loot, which
	 * {@link #canDropLoot} already gates on doMobLoot. Empty stacks are skipped.
	 */
	static void dropLootItem(ServerLevel serverLevel, Vec3 pos, ItemStack stack) {
		if (stack.isEmpty()) return;
		ItemEntity item = new ItemEntity(serverLevel, pos.x(), pos.y(), pos.z(), stack);
		item.setExtendedLifetime();
		item.setNoPickUpDelay();
		serverLevel.addFreshEntity(item);
	}

	/**
	 * Places {@code chest} at {@code pos}, or reuses one that is already there. If that spot holds a block that pistons cannot push
	 * or another block entity, each position above it is tried in turn, so the chest never wipes out a spawner or another chest.
	 * Like upstream, unbreakable blocks such as bedrock still count as replaceable here: they keep {@link PushReaction#NORMAL}.
	 *
	 * @return the position of the placed chest, or null if there was no room for one up to the build limit
	 */
	@Nullable
	static BlockPos placeLootChest(ServerLevel serverLevel, BlockState chest, BlockPos pos) {
		BlockPos.MutableBlockPos chestPos = pos.mutable();
		for (int y = pos.getY(); y < serverLevel.getMaxBuildHeight(); y++) {
			chestPos.setY(y);
			BlockState state = serverLevel.getBlockState(chestPos);
			if ((state.is(chest.getBlock()) ||
				((state.canBeReplaced() || state.getPistonPushReaction() != PushReaction.BLOCK) && serverLevel.getBlockEntity(chestPos) == null && serverLevel.setBlock(chestPos, chest, TFLootTables.DEFAULT_PLACE_FLAG))) &&
				serverLevel.getBlockEntity(chestPos) instanceof Container) {
				return chestPos.immutable();
			}
		}
		return null;
	}

	static <T extends LivingEntity & IBossLootBuffer> void celebrateAt(T boss, Vec3 vec3, ServerLevel serverLevel) {
		serverLevel.playSound(null, vec3.x, vec3.y, vec3.z, TFSounds.BOSS_CHEST_APPEAR.get(), boss.getSoundSource(), 128.0F, (boss.getRandom().nextFloat() - boss.getRandom().nextFloat()) * 0.175F + 0.5F);

		ParticlePacket particlePacket = new ParticlePacket();
		for (int i = 0; i < 40; i++) {
			double x = (boss.getRandom().nextDouble() - 0.5D) * 0.075D * i;
			double y = (boss.getRandom().nextDouble() - 0.5D) * 0.075D * i;
			double z = (boss.getRandom().nextDouble() - 0.5D) * 0.075D * i;
			particlePacket.queueParticle(ParticleTypes.POOF, false, vec3.add(x, y, z), Vec3.ZERO);
		}
		TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> boss), particlePacket);
	}

	default <T extends LivingEntity & IBossLootBuffer> void fill(T boss, LootParams context, LootTable table) {
		ObjectArrayList<ItemStack> items = table.getRandomItems(context);
		RandomSource randomsource = boss.getRandom();
		List<Integer> list = this.getAvailableSlots(randomsource);
		table.shuffleAndSplitItems(items, list.size(), randomsource);

		for (ItemStack itemstack : items) {
			if (list.isEmpty()) {
				return;
			}

			if (itemstack.isEmpty()) {
				this.setItem(list.remove(list.size() - 1), ItemStack.EMPTY);
			} else {
				this.setItem(list.remove(list.size() - 1), itemstack);
			}
		}
	}

	default List<Integer> getAvailableSlots(RandomSource random) {
		ObjectArrayList<Integer> arrayList = new ObjectArrayList<>();
		for (int i = 0; i < CONTAINER_SIZE; ++i) arrayList.add(i);
		Util.shuffle(arrayList, random);
		return arrayList;
	}

	NonNullList<ItemStack> getItemStacks();
}
