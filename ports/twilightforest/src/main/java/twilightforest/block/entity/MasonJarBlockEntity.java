package twilightforest.block.entity;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFBlockEntities;
import twilightforest.network.SetMasonJarItemPacket;
import twilightforest.network.TFPacketHandler;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The Mason Jar holds and displays a single stack. The stack is exposed as an item handler on the top face, and a
 * block item inside makes the jar give off that block's light.
 * <p>
 * The contents are saved as {@code item} (an {@link ItemStackHandler} tag) and on items as {@code BlockEntityTag.item},
 * which stands in for 4.8's {@code minecraft:container} component.
 */
public class MasonJarBlockEntity extends JarBlockEntity {
	public static final String TAG_ITEM = "item";
	public static final String TAG_ANGLE = "rotation";

	/**
	 * Light given off by the contents of each loaded mason jar, per level. It replaces NeoForge's
	 * {@code AuxiliaryLightManager}: the light engine asks for a block's emission on its own worker thread, where looking
	 * up the block entity is not safe, so the block entity publishes its value here instead.
	 */
	private static final Map<Level, Long2IntMap> CONTENT_LIGHT = new WeakHashMap<>();

	protected final MasonJarItemStackHandler item;
	private LazyOptional<IItemHandler> itemCapability;
	protected int itemRotation = 0;

	public MasonJarBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.MASON_JAR.get(), pos, state);
		this.item = new MasonJarItemStackHandler(this);
		this.itemCapability = LazyOptional.of(() -> this.item);
	}

	public MasonJarItemStackHandler getItemHandler() {
		return this.item;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put(TAG_ITEM, this.item.serializeNBT());
		tag.putInt(TAG_ANGLE, this.itemRotation);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.item.deserializeNBT(tag.getCompound(TAG_ITEM));
		this.itemRotation = tag.getInt(TAG_ANGLE);
		// Block entity data sent to clients arrives after onLoad; data read from disk (no level yet) is handled by onLoad
		if (this.level != null) this.updateContentLight(true);
	}

	@Override
	protected void saveToItemTag(CompoundTag blockEntityTag) {
		super.saveToItemTag(blockEntityTag);
		blockEntityTag.put(TAG_ITEM, this.item.serializeNBT());
	}

	/**
	 * Fills the jar from a loot table, merging rolled stacks of the same item into the one slot. Used by the Lich Tower.
	 *
	 * @return false when the loot table does not exist
	 */
	public boolean fillFromLootTable(ResourceLocation lootTableId, long seed, ServerLevel level) {
		LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);

		if (lootTable == LootTable.EMPTY) return false;

		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.getBlockPos())).create(LootContextParamSets.CHEST);

		lootTable.getRandomItemsRaw(new LootContext.Builder(params).withOptionalRandomSeed(seed).create(lootTableId), this::acceptLootTable);

		return true;
	}

	private void acceptLootTable(ItemStack stack) {
		MasonJarItemStackHandler jarInv = this.getItemHandler();
		if (jarInv.isEmpty()) {
			jarInv.setItem(stack);
		} else {
			ItemStack contained = jarInv.peekItem();
			// Merge stack in if there's already an item inside
			if (ItemStack.isSameItemSameTags(contained, stack)) {
				contained.setCount(Math.min(contained.getCount() + stack.getCount(), contained.getMaxStackSize()));
			}
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (this.level != null) {
			this.updateContentLight(true);
		}
		if (this.level instanceof ServerLevel serverLevel) {
			BlockPos pos = this.getBlockPos();
			TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_CHUNK.with(() -> serverLevel.getChunkAt(pos)), new SetMasonJarItemPacket(pos, this.item.getItem(), this.itemRotation));
		}
	}

	@Override
	public void onLoad() {
		super.onLoad();
		this.updateContentLight(false);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if (this.level != null) setContentLight(this.level, this.getBlockPos(), 0);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if (this.level != null) setContentLight(this.level, this.getBlockPos(), 0);
	}

	private void updateContentLight(boolean onlyIfChanged) {
		if (this.level == null) return;
		BlockPos pos = this.getBlockPos();
		int light = this.item.getItem().getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState().getLightEmission() : 0;
		int previous = setContentLight(this.level, pos, light);
		if (!onlyIfChanged ? light > 0 : light != previous) {
			this.level.getLightEngine().checkBlock(pos);
		}
	}

	/**
	 * @return the previous value
	 */
	private static int setContentLight(Level level, BlockPos pos, int light) {
		synchronized (CONTENT_LIGHT) {
			if (light > 0) {
				return CONTENT_LIGHT.computeIfAbsent(level, key -> new Long2IntOpenHashMap()).put(pos.asLong(), light);
			}
			Long2IntMap lights = CONTENT_LIGHT.get(level);
			if (lights == null) return 0;
			int previous = lights.remove(pos.asLong());
			if (lights.isEmpty()) CONTENT_LIGHT.remove(level);
			return previous;
		}
	}

	/**
	 * Light emitted by the contents of the mason jar at this position. Safe to call from the light engine's thread.
	 */
	public static int getContentLight(BlockGetter getter, BlockPos pos) {
		Level level = getter instanceof Level l ? l : getter instanceof LevelChunk chunk ? chunk.getLevel() : null;
		if (level == null) return 0;
		synchronized (CONTENT_LIGHT) {
			Long2IntMap lights = CONTENT_LIGHT.get(level);
			return lights == null ? 0 : lights.get(pos.asLong());
		}
	}

	public int getItemRotation() {
		return this.itemRotation;
	}

	public void setItemRotation(int itemRotation) {
		this.itemRotation = itemRotation;
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
		if (capability == ForgeCapabilities.ITEM_HANDLER && side == Direction.UP) {
			return this.itemCapability.cast();
		}
		return super.getCapability(capability, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		this.itemCapability.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		this.itemCapability = LazyOptional.of(() -> this.item);
	}

	/**
	 * @return the stack stored on a mason jar item, or {@link ItemStack#EMPTY}
	 */
	public static ItemStack getContents(ItemStack jar) {
		CompoundTag tag = BlockItem.getBlockEntityData(jar);
		if (tag == null || !tag.contains(TAG_ITEM, Tag.TAG_COMPOUND)) return ItemStack.EMPTY;
		ItemStackHandler handler = new ItemStackHandler(1);
		handler.deserializeNBT(tag.getCompound(TAG_ITEM));
		return handler.getStackInSlot(0);
	}

	public static class MasonJarItemStackHandler extends ItemStackHandler {
		protected final MasonJarBlockEntity jarEntity;

		public MasonJarItemStackHandler(MasonJarBlockEntity jarEntity) {
			super(1);
			this.jarEntity = jarEntity;
		}

		// Used for simple checks of what the one item is, without going through all the hoops. Used by the renderer and when saving contents to item
		public ItemStack getItem() {
			return this.stacks.get(0).copy();
		}

		// Peeks at the stored item, without cloning it
		private ItemStack peekItem() {
			return this.stacks.get(0);
		}

		// Used when syncing to client and when placing a jar that already has stored items
		public void setItem(ItemStack itemStack) {
			this.stacks.set(0, itemStack);
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			// Keep the single slot even if the saved tag carries another size
			super.deserializeNBT(nbt);
			if (this.stacks.size() != 1) {
				ItemStack first = this.stacks.isEmpty() ? ItemStack.EMPTY : this.stacks.get(0);
				this.setSize(1);
				this.stacks.set(0, first);
			}
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return stack.getItem().canFitInsideContainerItems();
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (simulate) return super.extractItem(slot, amount, true);
			ItemStack extractedStack = super.extractItem(slot, amount, false);
			if (!extractedStack.isEmpty()) {
				this.jarEntity.wobble(WobbleStyle.NEGATIVE);
				this.jarEntity.setChanged();
			}
			return extractedStack;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (simulate) return super.insertItem(slot, stack, true);
			ItemStack inserted = stack.copy();
			ItemStack returned = super.insertItem(slot, stack, false);
			if (!ItemStack.isSameItemSameTags(inserted, returned) || inserted.getCount() != returned.getCount()) {
				this.jarEntity.wobble(WobbleStyle.POSITIVE);
				this.jarEntity.setChanged();
			}
			return returned;
		}

		public boolean isEmpty() {
			return this.stacks.get(0).isEmpty();
		}
	}
}
