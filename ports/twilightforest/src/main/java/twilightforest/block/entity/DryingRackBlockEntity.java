package twilightforest.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.DryingRackBlock;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFRecipes;
import twilightforest.item.recipe.DryingRecipe;

public class DryingRackBlockEntity extends BlockEntity {

	public static final int DEFAULT_DRYING_TIME = 20 * 60 * 5; //5 Minutes
	private ItemStack stack = ItemStack.EMPTY;
	private final RecipeManager.CachedCheck<Container, DryingRecipe> quickCheck = RecipeManager.createCheck(TFRecipes.DRYING_RECIPE.get());
	// TF 4.8 hands this out through NeoForge's RegisterCapabilitiesEvent; Forge 47 asks the block entity for it
	private LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new DryingRackHandler(this));

	protected boolean drying;
	protected int dryTime;
	protected int totalDryTime;

	public DryingRackBlockEntity(BlockPos pos, BlockState blockState) {
		super(TFBlockEntities.DRYING_RACK.get(), pos, blockState);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity entity) {
		if (!state.getValue(DryingRackBlock.WATERLOGGED) && !level.isClientSide()) {
			if (!entity.getTheItem().isEmpty()) {
				Container input = new SimpleContainer(entity.getTheItem());
				DryingRecipe recipe = entity.quickCheck.getRecipeFor(input, level).orElse(null);
				boolean recipeExists = recipe != null;
				entity.updateDryingTime(recipeExists);
				if (recipeExists) {
					entity.dryTime++;

					if (entity.dryTime >= entity.totalDryTime) {
						entity.setTheItem(recipe.assemble(input, level.registryAccess()));
						setChanged(level, pos, state);
					}
				}
			} else {
				entity.updateDryingTime(false);
			}
		}
	}

	private void updateDryingTime(boolean drying) {
		boolean wasDrying = this.drying;
		this.drying = drying;
		if (wasDrying != drying) {
			this.setChanged();
		}
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if (this.level != null) {
			this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
		}
	}

	public ItemStack getTheItem() {
		return this.stack;
	}

	public void setTheItem(ItemStack newItem) {
		boolean updateDryTime = newItem.isEmpty() || !ItemStack.isSameItemSameTags(this.stack, newItem);
		this.stack = newItem;
		if (this.stack.getCount() > 1) {
			this.stack.setCount(1);
		}
		if (updateDryTime) {
			this.totalDryTime = this.getDryingTime();
			this.dryTime = 0;
			this.setChanged();

			if (this.level != null && !this.level.isClientSide()) {
				if (newItem.isEmpty() || this.getBlockState().getValue(DryingRackBlock.WATERLOGGED)) {
					this.drying = false;
				} else {
					this.drying = this.quickCheck.getRecipeFor(new SimpleContainer(newItem), this.level).isPresent();
				}
			}
		}
	}

	public ItemStack takeTheItem() {
		ItemStack theItem = this.getTheItem();
		this.setTheItem(ItemStack.EMPTY);
		return theItem;
	}

	/**
	 * Hangs an item rolled from a loot table on the rack, as a structure's drying rack marker does. The loot table is a plain id here;
	 * TF 4.8's ResourceKey&lt;LootTable&gt; has the same JSON form.
	 */
	public boolean fillFromLootTable(ResourceLocation lootTableId, long seed, ServerLevel level) {
		LootTable lootTable = level.getServer().getLootData().getLootTable(lootTableId);

		if (lootTable == LootTable.EMPTY) return false;

		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.getBlockPos())).create(LootContextParamSets.CHEST);

		lootTable.getRandomItemsRaw(new LootContext.Builder(params).withOptionalRandomSeed(seed).create(lootTableId), lootStack -> this.stack = lootStack);

		return true;
	}

	private int getDryingTime() {
		Level level = this.getLevel();
		if (level == null)
			return DEFAULT_DRYING_TIME;
		return this.quickCheck.getRecipeFor(new SimpleContainer(this.getTheItem()), level).map(DryingRecipe::getDryingTime).orElse(DEFAULT_DRYING_TIME);
	}

	public boolean isDrying() {
		return this.drying;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (!this.stack.isEmpty()) {
			tag.put("item", this.stack.save(new CompoundTag()));
		}
		tag.putInt("dry_time", this.dryTime);
		tag.putInt("total_dry_time", this.totalDryTime);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains("item", Tag.TAG_COMPOUND)) {
			this.stack = ItemStack.of(tag.getCompound("item"));
		} else {
			this.stack = ItemStack.EMPTY;
		}
		this.dryTime = tag.getInt("dry_time");
		this.totalDryTime = tag.getInt("total_dry_time");

		if (tag.contains("drying")) {
			this.drying = tag.getBoolean("drying");
		}
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.putBoolean("drying", this.drying);

		if (!this.stack.isEmpty()) {
			tag.put("item", this.stack.save(new CompoundTag()));
		}

		return tag;
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER) {
			return this.itemHandler.cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		this.itemHandler.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		this.itemHandler = LazyOptional.of(() -> new DryingRackHandler(this));
	}

	public record DryingRackHandler(DryingRackBlockEntity inventory) implements IItemHandlerModifiable {
		@Override
		public int getSlots() {
			return 1;
		}

		@Override
		public @NotNull ItemStack getStackInSlot(int slot) {
			return this.inventory.getTheItem();
		}

		@Override
		public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if (!this.inventory.getTheItem().isEmpty()) {
				return stack;
			}

			ItemStack copyStack = stack.copy();
			ItemStack splitOut = copyStack.split(1);
			if (!simulate) {
				this.inventory.setTheItem(splitOut);
			}

			return copyStack;
		}

		@Override
		public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (this.inventory.drying) {
				return ItemStack.EMPTY;
			}

			return simulate ? this.inventory.getTheItem() : this.inventory.takeTheItem();
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, @NotNull ItemStack stack) {
			return true;
		}

		@Override
		public void setStackInSlot(int slot, @NotNull ItemStack stack) {
			this.inventory.setTheItem(stack);
		}
	}
}
