package com.enderio.core.common.capability;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * The damage modes of the advanced item filters, ported from Ender IO 8.x
 * (content/filters/item/general/DamageFilterMode).
 * A filter stack keeps the mode by its serialized name under the optional NBT key DamageMode.
 * A missing or unknown name reads as {@link #IGNORE}, which matches every stack as 6.x filters always did.
 */
public enum DamageFilterMode {
    IGNORE("ignore", stack -> true),
    UP_TO_25("up_to_25", stack -> stack.getDamageValue() <= stack.getMaxDamage() * 0.25f),
    MORE_THAN_25("more_than_25", stack -> stack.getDamageValue() > stack.getMaxDamage() * 0.25f),
    UP_TO_50("up_to_50", stack -> stack.getDamageValue() <= stack.getMaxDamage() * 0.5f),
    MORE_THAN_50("more_than_50", stack -> stack.getDamageValue() > stack.getMaxDamage() * 0.5f),
    UP_TO_75("up_to_75", stack -> stack.getDamageValue() <= stack.getMaxDamage() * 0.75f),
    MORE_THAN_75("more_than_75", stack -> stack.getDamageValue() > stack.getMaxDamage() * 0.75f),
    NOT_DAMAGED("not_damaged", stack -> !stack.isDamaged()),
    ONLY_DAMAGED("only_damaged", ItemStack::isDamaged),
    IS_DAMAGEABLE("is_damageable", ItemStack::isDamageableItem),
    NOT_DAMAGEABLE("not_damageable", stack -> !stack.isDamageableItem());

    private final String name;
    private final Predicate<ItemStack> predicate;

    DamageFilterMode(String name, Predicate<ItemStack> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    public boolean test(ItemStack stack) {
        return predicate.test(stack);
    }

    public String getSerializedName() {
        return name;
    }

    /**
     * @return the translation key, the same key as the 8.x EnumLangMap uses
     */
    public String getTranslationKey() {
        return "enderio.damage_filter_mode." + name;
    }

    /**
     * @return the next mode, or the previous one when {@code forward} is false, wrapping around at both ends
     */
    public DamageFilterMode cycle(boolean forward) {
        DamageFilterMode[] values = values();
        int index = ordinal() + (forward ? 1 : -1) + values.length;
        return values[index % values.length];
    }

    public static DamageFilterMode byName(String name) {
        for (DamageFilterMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }

        return IGNORE;
    }

    public static DamageFilterMode byId(int id) {
        DamageFilterMode[] values = values();
        return id >= 0 && id < values.length ? values[id] : IGNORE;
    }
}
