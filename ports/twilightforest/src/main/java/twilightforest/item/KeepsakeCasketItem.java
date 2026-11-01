package twilightforest.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import twilightforest.block.KeepsakeCasketBlock;
import twilightforest.client.ISTER;

import java.util.function.Consumer;

/**
 * The Keepsake Casket item carries the casket's damage, so a broken casket keeps its damage, and the name and model show it.
 * <p>
 * 4.8 keeps the damage in the CASKET_DAMAGE data component. On 1.20.1 it is the block state tag that the casket has always
 * used ({@code BlockStateTag: {damage: "1"}}), so caskets from older worlds keep their damage and vanilla's
 * BlockItem places the right state. Undamaged caskets carry no tag, so they stack with new ones.
 */
public class KeepsakeCasketItem extends BlockItem {

	public KeepsakeCasketItem(Block block, Properties properties) {
		super(block, properties);
	}

	public static int getCasketDamage(ItemStack stack) {
		CompoundTag stateTag = stack.getTagElement(BLOCK_STATE_TAG);
		if (stateTag == null) return 0;
		String breakage = KeepsakeCasketBlock.BREAKAGE.getName();
		//a casket broken by hand in 4.3 stored its damage as an int, the loot table's CopyBlockState stores it as a string
		int damage;
		if (stateTag.get(breakage) instanceof NumericTag numeric) {
			damage = numeric.getAsInt();
		} else if (stateTag.contains(breakage, Tag.TAG_STRING)) {
			damage = KeepsakeCasketBlock.BREAKAGE.getValue(stateTag.getString(breakage)).orElse(0);
		} else {
			return 0;
		}
		return Mth.clamp(damage, 0, 2);
	}

	public static void setCasketDamage(ItemStack stack, int damage) {
		String breakage = KeepsakeCasketBlock.BREAKAGE.getName();
		if (damage > 0) {
			stack.getOrCreateTagElement(BLOCK_STATE_TAG).putString(breakage, String.valueOf(Mth.clamp(damage, 0, 2)));
		} else {
			CompoundTag stateTag = stack.getTagElement(BLOCK_STATE_TAG);
			if (stateTag != null) {
				stateTag.remove(breakage);
				if (stateTag.isEmpty()) stack.removeTagKey(BLOCK_STATE_TAG);
			}
		}
	}

	@Override
	public String getDescriptionId(ItemStack stack) {
		int damage = getCasketDamage(stack);
		if (damage > 0) {
			return "block.twilightforest." + (damage == 1 ? "chipped_" : "damaged_") + "keepsake_casket";
		}
		return super.getDescriptionId(stack);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(ISTER.CLIENT_ITEM_EXTENSION);
	}
}
