package twilightforest.inventory;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Server side recipe book placement for the Uncrafting Table. Vanilla's {@link ServerPlaceRecipe} assumes the
 * crafting grid directly follows the result slot, but here slots 2 to 10 are the hidden uncrafting matrix,
 * so every grid lookup is shifted onto the assembly matrix (slots 11 to 19).
 */
public class UncrafterPlaceRecipe<C extends Container> extends ServerPlaceRecipe<C> implements UncraftingPlaceRecipe<Integer> {

	public UncrafterPlaceRecipe(RecipeBookMenu<C> menu) {
		super(menu);
	}

	@Override
	public void recipeClicked(ServerPlayer player, @Nullable Recipe<C> recipe, boolean placeAll) {
		if (recipe != null && player.getRecipeBook().contains(recipe)) {
			this.inventory = player.getInventory();
			if (this.tryClearGrid() || player.isCreative()) {
				this.stackedContents.clear();
				player.getInventory().fillStackedContents(this.stackedContents);
				this.menu.fillCraftSlotsStackedContents(this.stackedContents);
				if (this.stackedContents.canCraft(recipe, null)) {
					this.handleRecipeClicked(recipe, placeAll);
				} else {
					this.clearGrid();
					player.connection.send(new ClientboundPlaceGhostRecipePacket(player.containerMenu.containerId, recipe));
				}

				player.getInventory().setChanged();
			}
		}
	}

	@Override
	protected void handleRecipeClicked(Recipe<C> recipe, boolean placeAll) {
		boolean flag = this.menu.recipeMatches(recipe);
		int i = this.stackedContents.getBiggestCraftableStack(recipe, null);
		if (flag) {
			for (int slot = 0; slot < this.menu.getSize(); ++slot) {
				if (slot != this.menu.getResultSlotIndex()) {
					ItemStack itemstack = this.menu.getSlot(slot).getItem();
					if (!itemstack.isEmpty() && Math.min(i, itemstack.getMaxStackSize()) < itemstack.getCount() + 1) {
						return;
					}
				}
			}
		}

		int j1 = this.getStackSize(placeAll, i, flag);
		IntList intlist = new IntArrayList();
		if (this.stackedContents.canCraft(recipe, intlist, j1)) {
			int k = j1;

			for (int l : intlist) {
				int i1 = StackedContents.fromStackingIndex(l).getMaxStackSize();
				if (i1 < k) {
					k = i1;
				}
			}

			if (this.stackedContents.canCraft(recipe, intlist, k)) {
				this.clearGrid();
				this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), recipe, intlist.iterator(), k);
			}
		}
	}

	@Override
	protected int getStackSize(boolean placeAll, int maxPossible, boolean recipeMatches) {
		int i = 1;
		if (placeAll) {
			i = maxPossible;
		} else if (recipeMatches) {
			i = 64;

			for (int j = 0; j < this.menu.getGridWidth() * this.menu.getGridHeight(); ++j) {
				ItemStack itemStack = this.menu.getSlot(j + matrixOffset).getItem();
				if (!itemStack.isEmpty() && i > itemStack.getCount()) {
					i = itemStack.getCount();
				}
			}

			if (i < 64) {
				++i;
			}
		}

		return i;
	}

	private int getAmountOfFreeSlotsInInventory() {
		int i = 0;

		for (ItemStack itemstack : this.inventory.items) {
			if (itemstack.isEmpty()) {
				++i;
			}
		}

		return i;
	}

	private boolean tryClearGrid() {
		List<ItemStack> list = Lists.newArrayList();
		int i = this.getAmountOfFreeSlotsInInventory();

		if (i > 0) {
			// Uncrafting input slot
			ItemStack itemstack = this.menu.getSlot(0).getItem().copy();
			if (!itemstack.isEmpty()) list.add(itemstack);
		}

		for (int slotIndex = 0; slotIndex < this.menu.getGridWidth() * this.menu.getGridHeight(); ++slotIndex) {
			ItemStack itemstack = this.menu.getSlot(slotIndex + matrixOffset).getItem().copy();
			if (!itemstack.isEmpty()) {
				int k = this.inventory.getSlotWithRemainingSpace(itemstack);
				if (k == -1 && list.size() <= i) {
					for (ItemStack itemstack1 : list) {
						if (ItemStack.isSameItem(itemstack1, itemstack)
							&& itemstack1.getCount() != itemstack1.getMaxStackSize()
							&& itemstack1.getCount() + itemstack.getCount() <= itemstack1.getMaxStackSize()) {
							itemstack1.grow(itemstack.getCount());
							itemstack.setCount(0);
							break;
						}
					}

					if (!itemstack.isEmpty()) {
						if (list.size() >= i) {
							return false;
						}

						list.add(itemstack);
					}
				} else if (k == -1) {
					return false;
				}
			}
		}

		return true;
	}
}
