package twilightforest.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFSounds;

import java.util.UUID;

/**
 * A 45 slot chest that can be locked to the player who died into it. The Keepsake Casket extends it.
 * Lid handling follows the vanilla chest (ContainerOpenersCounter on the server, ChestLidController on the client).
 */
public class SkullChestBlockEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {
	private static final int SIZE = 9 * 5;
	private static final String OWNER_TAG = "owner";
	//4.3 caskets stored only the owner's UUID and a name cut down to 12 characters
	private static final String LEGACY_OWNER_UUID_TAG = "deadPlayer";
	private static final String LEGACY_OWNER_NAME_TAG = "playerName";

	public NonNullList<ItemStack> contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
		@Override
		protected void onOpen(Level level, BlockPos pos, BlockState state) {
			level.playSound(null, pos, SkullChestBlockEntity.this.getOpenSound(), SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
		}

		@Override
		protected void onClose(Level level, BlockPos pos, BlockState state) {
			level.playSound(null, pos, SkullChestBlockEntity.this.getCloseSound(), SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
		}

		@Override
		protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {
			level.blockEvent(pos, state.getBlock(), 1, openCount);
		}

		@Override
		protected boolean isOwnContainer(Player player) {
			return player.containerMenu instanceof ChestMenu menu && menu.getContainer() == SkullChestBlockEntity.this;
		}
	};
	private final ChestLidController chestLidController = new ChestLidController();
	/**
	 * The player this chest is locked to. Only they (and operators) can open it while it is set.
	 */
	@Nullable
	public GameProfile owner;

	public SkullChestBlockEntity(BlockPos pos, BlockState state) {
		this(TFBlockEntities.SKULL_CHEST.get(), pos, state);
	}

	protected SkullChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected SoundEvent getOpenSound() {
		return TFSounds.SKULL_CHEST_OPEN.get();
	}

	protected SoundEvent getCloseSound() {
		return TFSounds.SKULL_CHEST_CLOSE.get();
	}

	public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, SkullChestBlockEntity te) {
		te.chestLidController.tickLid();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.contents) {
			if (!itemstack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public NonNullList<ItemStack> getItems() {
		return this.contents;
	}

	@Override
	public void setItems(NonNullList<ItemStack> items) {
		// Because NonNullList is a very incomplete List as a whole, not even proxying half of the conveniences of List to its delegate.
		// We hereby curse it to merge stacks instead.

		// Due to some outside usages we will be doing a pull-overwrite operation here instead.
		int limit = Math.min(this.contents.size(), items.size());

		for (int i = 0; i < limit; i++) {
			ItemStack stack = items.get(i);
			//noinspection ConstantConditions
			if (stack != null) { // No, it is very easily possible for NonNullList to have null.
				this.contents.set(i, items.get(i));
				items.set(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	protected Component getDefaultName() {
		return this.getBlockState().getBlock().getName();
	}

	@Override
	protected AbstractContainerMenu createMenu(int id, Inventory player) {
		return new ChestMenu(MenuType.GENERIC_9x5, id, player, this, 5);
	}

	@Override
	public int getContainerSize() {
		return SIZE;
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		if (!this.trySaveLootTable(tag)) {
			ContainerHelper.saveAllItems(tag, this.contents);
		}
		if (this.owner != null) {
			tag.put(OWNER_TAG, NbtUtils.writeGameProfile(new CompoundTag(), this.owner));
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.contents = NonNullList.withSize(SIZE, ItemStack.EMPTY);
		if (!this.tryLoadLootTable(tag)) {
			ContainerHelper.loadAllItems(tag, this.contents);
		}
		this.owner = null;
		if (tag.contains(OWNER_TAG, Tag.TAG_COMPOUND)) {
			this.owner = NbtUtils.readGameProfile(tag.getCompound(OWNER_TAG));
		} else if (tag.hasUUID(LEGACY_OWNER_UUID_TAG)) {
			String name = tag.contains(LEGACY_OWNER_NAME_TAG, Tag.TAG_STRING) ? tag.getString(LEGACY_OWNER_NAME_TAG) : null;
			this.owner = new GameProfile(tag.getUUID(LEGACY_OWNER_UUID_TAG), name);
		}
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		if (id == 1) {
			this.chestLidController.shouldBeOpen(type > 0);
			return true;
		} else {
			return super.triggerEvent(id, type);
		}
	}

	@Override
	public void startOpen(Player player) {
		if (!this.remove && !player.isSpectator() && this.getLevel() != null) {
			this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	@Override
	public void stopOpen(Player player) {
		if (!this.remove && !player.isSpectator() && this.getLevel() != null) {
			this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	public void recheckOpen() {
		if (!this.remove && this.getLevel() != null) {
			this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
		}
	}

	@Override
	public float getOpenNess(float partialTicks) {
		return this.chestLidController.getOpenness(partialTicks);
	}

	/**
	 * Compared by UUID, so a renamed player keeps access and chests loaded from 4.3 (which only know the UUID) still work.
	 */
	public boolean isOwner(Player player) {
		if (this.owner == null) return false;
		UUID id = this.owner.getId();
		return id != null ? id.equals(player.getGameProfile().getId()) : player.getGameProfile().getName().equals(this.owner.getName());
	}

	public String getOwnerName() {
		if (this.owner == null) return "";
		return this.owner.getName() != null && !this.owner.getName().isEmpty() ? this.owner.getName() : String.valueOf(this.owner.getId());
	}

	//if we have a dead player set, then only that player can open the chest
	@Override
	public boolean stillValid(Player player) {
		if (this.owner != null) {
			if (player.hasPermissions(3) || this.isOwner(player)) {
				return super.stillValid(player);
			} else {
				return false;
			}
		} else {
			return super.stillValid(player);
		}
	}

	@Override
	public boolean canOpen(Player player) {
		if (this.owner != null) {
			if (player.hasPermissions(3) || this.isOwner(player)) {
				return super.canOpen(player);
			} else {
				this.displayLockedInfo(player);
				return false;
			}
		} else {
			return super.canOpen(player);
		}
	}

	public void displayLockedInfo(Player player) {
		player.playNotifySound(TFSounds.SKULL_CHEST_LOCKED.get(), SoundSource.BLOCKS, 0.5F, 0.5F);
		player.displayClientMessage(Component.translatable("block.twilightforest.skull_chest.locked", this.getOwnerName()).withStyle(ChatFormatting.RED), true);
	}

	//no hoppers or pipes: a locked chest must only give its items to its owner
	@Override
	protected IItemHandler createUnSidedHandler() {
		return new EmptyHandler();
	}

	//remove stored player when chest is broken
	@Override
	public void setRemoved() {
		this.owner = null;
		super.setRemoved();
	}
}
