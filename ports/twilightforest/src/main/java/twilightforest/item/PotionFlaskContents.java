package twilightforest.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * What a potion flask holds: the potion (with any custom effects and colour it was brewed with), the doses left and how
 * often a brittle flask has cracked.
 * <p>
 * Backport of 4.8's {@code PotionFlaskComponent}. 1.20.1 has no data components, so this is read from and written back
 * to the flask's stack NBT. The keys stay the ones 4.3 already used ({@code Potion}, {@code Uses}, {@code Breakage}),
 * plus vanilla's {@code CustomPotionEffects}/{@code CustomPotionColor} for the parts of 4.8's {@code PotionContents}
 * that 4.3 did not keep, so {@link PotionUtils} can read a flask like a potion. Whether a flask can break is a property
 * of the item ({@link BrittleFlaskItem#canBreak()}) rather than of the stack.
 * <p>
 * A flask with nothing in it and no cracks carries none of these keys, just like a freshly made one, so empty flasks
 * stack with each other again after being drunk dry.
 */
public record PotionFlaskContents(Potion potion, @Nullable Tag customEffects, @Nullable Tag customColor, int doses, int breakage) {

	public static final PotionFlaskContents EMPTY = new PotionFlaskContents(Potions.EMPTY, null, null, 0, 0);

	private static final String USES = "Uses";
	private static final String BREAKAGE = "Breakage";
	//4.3's WIP flasks stopped refilling once cracked. 4.8 lets a cracked flask refill up to its remaining capacity instead.
	private static final String LEGACY_REFILLABLE = "Refillable";

	/**
	 * Reads the flask's contents. The custom effect and colour tags are shared with the stack, not copied; they are only
	 * copied when written to another stack.
	 */
	public static PotionFlaskContents of(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null) return EMPTY;
		return new PotionFlaskContents(
			PotionUtils.getPotion(tag),
			tag.get(PotionUtils.TAG_CUSTOM_POTION_EFFECTS),
			tag.get(PotionUtils.TAG_CUSTOM_POTION_COLOR),
			tag.getInt(USES),
			tag.getInt(BREAKAGE));
	}

	/**
	 * Cheap read of just the dose count, for item model properties.
	 */
	public static int dosesOf(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null ? tag.getInt(USES) : 0;
	}

	/**
	 * Cheap read of just the breakage, for item model properties.
	 */
	public static int breakageOf(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null ? tag.getInt(BREAKAGE) : 0;
	}

	@Nullable
	private static Tag copyOrNull(@Nullable CompoundTag tag, String key) {
		if (tag == null) return null;
		Tag value = tag.get(key);
		return value != null ? value.copy() : null;
	}

	/**
	 * @return true if the flask holds a potion. Doses and breakage do not count.
	 */
	public boolean hasPotion() {
		return this.potion() != Potions.EMPTY;
	}

	/**
	 * Equivalent of 4.8's {@code PotionContents#equals}: the same potion with the same custom effects and colour.
	 */
	public boolean holdsSameAs(ItemStack potionStack) {
		CompoundTag tag = potionStack.getTag();
		return this.potion() == PotionUtils.getPotion(potionStack)
			&& Objects.equals(this.customEffects(), tag != null ? tag.get(PotionUtils.TAG_CUSTOM_POTION_EFFECTS) : null)
			&& Objects.equals(this.customColor(), tag != null ? tag.get(PotionUtils.TAG_CUSTOM_POTION_COLOR) : null);
	}

	public PotionFlaskContents tryAddDose(ItemStack potionStack) {
		CompoundTag tag = potionStack.getTag();
		return new PotionFlaskContents(PotionUtils.getPotion(potionStack),
			copyOrNull(tag, PotionUtils.TAG_CUSTOM_POTION_EFFECTS),
			copyOrNull(tag, PotionUtils.TAG_CUSTOM_POTION_COLOR),
			this.doses() + 1, this.breakage());
	}

	public PotionFlaskContents removeDose(boolean breakable) {
		int newDose = this.doses() - 1;
		boolean emptied = newDose < 1;
		return new PotionFlaskContents(
			emptied ? Potions.EMPTY : this.potion(),
			emptied ? null : this.customEffects(),
			emptied ? null : this.customColor(),
			newDose, breakable ? this.breakage() + 1 : this.breakage());
	}

	/**
	 * Writes these contents to the flask, leaving any unrelated NBT (custom names and the like) alone.
	 */
	public void applyTo(ItemStack flask) {
		if (this.hasPotion()) {
			PotionUtils.setPotion(flask, this.potion());
		} else {
			flask.removeTagKey(PotionUtils.TAG_POTION);
		}
		this.putOrRemove(flask, PotionUtils.TAG_CUSTOM_POTION_EFFECTS, this.customEffects());
		this.putOrRemove(flask, PotionUtils.TAG_CUSTOM_POTION_COLOR, this.customColor());
		if (this.doses() != 0) {
			flask.getOrCreateTag().putInt(USES, this.doses());
		} else {
			flask.removeTagKey(USES);
		}
		if (this.breakage() != 0) {
			flask.getOrCreateTag().putInt(BREAKAGE, this.breakage());
		} else {
			flask.removeTagKey(BREAKAGE);
		}
		flask.removeTagKey(LEGACY_REFILLABLE);
	}

	private void putOrRemove(ItemStack flask, String key, @Nullable Tag value) {
		if (value != null) {
			flask.getOrCreateTag().put(key, value.copy());
		} else {
			flask.removeTagKey(key);
		}
	}
}
