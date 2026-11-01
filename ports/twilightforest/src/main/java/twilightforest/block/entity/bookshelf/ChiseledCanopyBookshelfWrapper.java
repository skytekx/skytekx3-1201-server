package twilightforest.block.entity.bookshelf;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.wrapper.InvWrapper;
import twilightforest.block.ChiseledCanopyShelfBlock;

/**
 * Item handler of the Chiseled Canopy Bookshelf: hoppers and pipes may not pull books out of a shelf in spawner mode
 * (inserting is already refused by {@link ChiseledCanopyShelfBlockEntity#canPlaceItem}).
 */
public class ChiseledCanopyBookshelfWrapper extends InvWrapper {
	public ChiseledCanopyBookshelfWrapper(ChiseledCanopyShelfBlockEntity inv) {
		super(inv);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (((ChiseledCanopyShelfBlockEntity) this.getInv()).getBlockState().getValue(ChiseledCanopyShelfBlock.SPAWNER)) return ItemStack.EMPTY;
		return super.extractItem(slot, amount, simulate);
	}
}
