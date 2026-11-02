package twilightforest.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Emperor's Cloth helpers. TF 4.8 stores the flag as the {@code twilightforest:emperors_cloth} data component;
 * 1.20.1 has no data components, so the flag is a boolean stack NBT tag under the key TF used before the
 * component port (1.20.4, upstream c5e562b57). Keep every read and write of that key in this class.
 */
public final class ArmorUtil {

	public static final String EMPERORS_CLOTH_TAG = "twilightforest_emperors_cloth_applied";

	private ArmorUtil() {
	}

	public static boolean hasEmperorsCloth(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null && tag.contains(EMPERORS_CLOTH_TAG);
	}

	public static void applyEmperorsCloth(ItemStack stack) {
		stack.getOrCreateTag().putBoolean(EMPERORS_CLOTH_TAG, true);
	}

	/**
	 * Removes the flag; the stack tag is dropped entirely once it becomes empty, so washed armor stacks again
	 * with untouched armor.
	 */
	public static void removeEmperorsCloth(ItemStack stack) {
		stack.removeTagKey(EMPERORS_CLOTH_TAG);
	}

	public static float getShroudedArmorPercentage(LivingEntity entity) {
		int shroudedArmor = 0;
		int nonShroudedArmor = 0;

		for (ItemStack stack : entity.getArmorSlots()) {
			if (!stack.isEmpty() && hasEmperorsCloth(stack)) {
				shroudedArmor++;
			}

			nonShroudedArmor++;
		}

		return nonShroudedArmor > 0 && shroudedArmor > 0 ? (float) shroudedArmor / (float) nonShroudedArmor : 0.0F;
	}
}
