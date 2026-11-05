package ianm1647.expandeddelight.common.block.entity.container;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.block.entity.JuicerBlockEntity;
import ianm1647.expandeddelight.common.crafting.JuicerRecipe;
import ianm1647.expandeddelight.common.registry.EDBlocks;
import ianm1647.expandeddelight.common.registry.EDMenuTypes;
import ianm1647.expandeddelight.common.tag.EDTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.Objects;

/**
 * The juicer menu of this port. Upstream's 1.21-neo JuicerMenu is not used, see NOTICE. This class is
 * written from Farmer's Delight's CookingPotMenu (MIT, Copyright (c) 2020 vectorwing) for the juicer's
 * two input slots, with the slot positions of the juicer sheet textures/gui/juicer_gui.png of the 1.20
 * branch (commit 50e9c10).
 */
public class JuicerMenu extends RecipeBookMenu<RecipeWrapper>
{
	public static final int INDEX_DRINK = JuicerBlockEntity.MEAL_DISPLAY_SLOT;
	public static final int INDEX_CONTAINER = JuicerBlockEntity.CONTAINER_SLOT;
	public static final int INDEX_OUTPUT = JuicerBlockEntity.OUTPUT_SLOT;
	public static final int INVENTORY_START = INDEX_OUTPUT + 1;
	public static final int INVENTORY_END = INVENTORY_START + 36;

	public final JuicerBlockEntity tileEntity;
	public final ItemStackHandler inventory;
	private final ContainerData juicerData;
	private final ContainerLevelAccess canInteractWithCallable;
	protected final Level level;

	public JuicerMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data), new SimpleContainerData(2));
	}

	public JuicerMenu(final int windowId, final Inventory playerInventory, final JuicerBlockEntity tileEntity, ContainerData juicerData) {
		super(EDMenuTypes.JUICER.get(), windowId);
		this.tileEntity = tileEntity;
		this.inventory = tileEntity.getInventory();
		this.juicerData = juicerData;
		this.level = playerInventory.player.level();
		this.canInteractWithCallable = ContainerLevelAccess.create(tileEntity.getLevel(), tileEntity.getBlockPos());

		// Ingredient slots, the column inside the juicer drawing of the sheet
		this.addSlot(new SlotItemHandler(this.inventory, 0, 48, 25));
		this.addSlot(new SlotItemHandler(this.inventory, 1, 48, 43));

		// Drink display, the large slot right of the arrow
		this.addSlot(new JuicerDrinkSlot(this.inventory, INDEX_DRINK, 116, 35));

		// Container input, the slot with the bottle drawn into the sheet
		this.addSlot(new SlotItemHandler(this.inventory, INDEX_CONTAINER, 80, 60));

		// Drink output
		this.addSlot(new JuicerResultSlot(playerInventory.player, tileEntity, this.inventory, INDEX_OUTPUT, 116, 60));

		// Main player inventory
		for (int row = 0; row < 3; ++row) {
			for (int column = 0; column < 9; ++column) {
				this.addSlot(new Slot(playerInventory, 9 + (row * 9) + column, 8 + (column * 18), 84 + (row * 18)));
			}
		}

		// Hotbar
		for (int column = 0; column < 9; ++column) {
			this.addSlot(new Slot(playerInventory, column, 8 + (column * 18), 142));
		}

		this.addDataSlots(juicerData);
	}

	private static JuicerBlockEntity getBlockEntity(final Inventory playerInventory, final FriendlyByteBuf data) {
		Objects.requireNonNull(playerInventory, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(data.readBlockPos());
		if (blockEntity instanceof JuicerBlockEntity juicer) {
			return juicer;
		}
		throw new IllegalStateException("Block entity is not correct! " + blockEntity);
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return stillValid(this.canInteractWithCallable, playerIn, EDBlocks.JUICER.get());
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotStackCopy = slotStack.copy();
			if (index == INDEX_OUTPUT) {
				if (!this.moveItemStackTo(slotStack, INVENTORY_START, INVENTORY_END, true)) {
					return ItemStack.EMPTY;
				}
			} else if (index > INDEX_OUTPUT) {
				if (slotStack.is(EDTags.VALID_JUICER_CONTAINERS) || slotStack.is(this.tileEntity.getContainer().getItem())) {
					if (!this.moveItemStackTo(slotStack, INDEX_CONTAINER, INDEX_OUTPUT, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(slotStack, 0, INDEX_DRINK, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(slotStack, INVENTORY_START, INVENTORY_END, false)) {
				return ItemStack.EMPTY;
			}

			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (slotStack.getCount() == slotStackCopy.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, slotStack);
		}
		return slotStackCopy;
	}

	public int getJuiceProgressionScaled() {
		int i = this.juicerData.get(0);
		int j = this.juicerData.get(1);
		return j != 0 && i != 0 ? i * 22 / j : 0;
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedContents helper) {
		for (int i = 0; i < this.inventory.getSlots(); i++) {
			helper.accountSimpleStack(this.inventory.getStackInSlot(i));
		}
	}

	@Override
	public void clearCraftingContent() {
		for (int i = 0; i < INDEX_DRINK; i++) {
			this.inventory.setStackInSlot(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean recipeMatches(Recipe<? super RecipeWrapper> recipe) {
		return recipe.matches(new RecipeWrapper(this.inventory), this.level);
	}

	@Override
	public int getResultSlotIndex() {
		return INDEX_CONTAINER;
	}

	@Override
	public int getGridWidth() {
		return 1;
	}

	@Override
	public int getGridHeight() {
		return 2;
	}

	@Override
	public int getSize() {
		// the inputs and the drink display, as getSize of the cooking pot counts its six inputs and the meal
		return JuicerRecipe.INPUT_SLOTS + 1;
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return ExpandedDelight.RECIPE_TYPE_JUICING;
	}

	@Override
	public boolean shouldMoveToInventory(int slot) {
		return slot < (this.getGridWidth() * this.getGridHeight());
	}
}
