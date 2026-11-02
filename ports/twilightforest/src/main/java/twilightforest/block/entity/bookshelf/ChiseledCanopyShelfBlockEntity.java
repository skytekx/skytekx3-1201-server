package twilightforest.block.entity.bookshelf;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.ChiseledCanopyShelfBlock;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFBlocks;

public class ChiseledCanopyShelfBlockEntity extends ChiseledBookShelfBlockEntity {

	private final BookshelfSpawner spawner = new BookshelfSpawner() {
		@Override
		public void broadcastEvent(Level level, BlockPos pos, int id) {
			level.blockEvent(pos, TFBlocks.CHISELED_CANOPY_BOOKSHELF.get(), id, 0);
		}

		@Override
		public void setNextSpawnData(@Nullable Level level, BlockPos pos, SpawnData data) {
			super.setNextSpawnData(level, pos, data);
			if (level != null) {
				BlockState blockstate = level.getBlockState(pos);
				level.sendBlockUpdated(pos, blockstate, blockstate, 4);
			}
		}
	};

	public ChiseledCanopyShelfBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, ChiseledCanopyShelfBlockEntity te) {
		if (!level.isClientSide() && state.getValue(ChiseledCanopyShelfBlock.SPAWNER)) {
			te.spawner.serverTick((ServerLevel) level, pos, state);
		}
	}

	// the vanilla constructor hardwires BlockEntityType.CHISELED_BOOKSHELF; saving, syncing and ticking all go through getType()
	@Override
	public BlockEntityType<?> getType() {
		return TFBlockEntities.CHISELED_CANOPY_BOOKSHELF.get();
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.spawner.load(this.level, this.worldPosition, tag);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		this.spawner.save(tag);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag compoundtag = this.saveWithoutMetadata();
		compoundtag.remove("SpawnPotentials");
		return compoundtag;
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		return this.spawner.onEventTriggered(this.level, id) || super.triggerEvent(id, type);
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	// 4.8 implements the 1.20.3+ Spawner interface so SpawnEggItem calls this; on 1.20.1 ChiseledCanopyShelfBlock#use does
	public void setEntityId(EntityType<?> type, RandomSource random) {
		this.spawner.setEntityId(type, this.level, random, this.worldPosition);
		if (this.level != null) {
			this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(ChiseledCanopyShelfBlock.SPAWNER, true));
		}
		this.setChanged();
	}

	public BookshelfSpawner getSpawner() {
		return this.spawner;
	}

	/**
	 * Removes the book a spawn just used up. Goes around the spawner lock in {@link #removeItem}; vanilla 1.20.1's
	 * {@code setItem(slot, ItemStack.EMPTY)}, which 4.8 calls here, is a no-op because an empty stack is not a bookshelf book.
	 */
	public void consumeBook(int slot) {
		super.removeItem(slot, 1);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		if (this.getBlockState().getValue(ChiseledCanopyShelfBlock.SPAWNER)) return ItemStack.EMPTY;
		return super.removeItem(slot, amount);
	}

	@Override
	public boolean canTakeItem(Container target, int slot, ItemStack stack) {
		return !this.getBlockState().getValue(ChiseledCanopyShelfBlock.SPAWNER) && super.canTakeItem(target, slot, stack);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return !this.getBlockState().getValue(ChiseledCanopyShelfBlock.SPAWNER) && super.canPlaceItem(slot, stack);
	}

	// NeoForge registers the wrapper as a block capability (#2368); Forge 47's chiseled shelf already exposes an item handler,
	// built by this factory
	@Override
	protected IItemHandler createUnSidedHandler() {
		return new ChiseledCanopyBookshelfWrapper(this);
	}
}
