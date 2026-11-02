package baguchi.twilight_crossbow.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 1.20.1 replacement for the 1.21 {@code minecraft:charged_projectiles} data component.
 * <p>
 * A 1.20.1 crossbow stores its loaded ammo in its own NBT, exactly like vanilla's private
 * {@code CrossbowItem#addChargedProjectile/getChargedProjectiles/clearChargedProjectiles}: a {@code "Charged"} boolean
 * (see {@link net.minecraft.world.item.CrossbowItem#isCharged}) and a {@code "ChargedProjectiles"} list of saved stacks.
 * Using the same keys keeps vanilla code (tooltips, mob AI, {@code CrossbowItem#containsChargedProjectile}) working.
 */
public final class ChargedProjectiles {
    public static final String TAG_CHARGED_PROJECTILES = "ChargedProjectiles";

    private ChargedProjectiles() {
    }

    /** Sets the loaded projectiles, replacing any previous ones (1.21: {@code stack.set(CHARGED_PROJECTILES, ChargedProjectiles.of(list))}). */
    public static void set(ItemStack crossbow, List<ItemStack> projectiles) {
        ListTag list = new ListTag();
        for (ItemStack projectile : projectiles) {
            list.add(projectile.save(new CompoundTag()));
        }
        crossbow.getOrCreateTag().put(TAG_CHARGED_PROJECTILES, list);
    }

    public static List<ItemStack> get(ItemStack crossbow) {
        List<ItemStack> projectiles = new ArrayList<>();
        CompoundTag tag = crossbow.getTag();
        if (tag != null && tag.contains(TAG_CHARGED_PROJECTILES, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_CHARGED_PROJECTILES, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                projectiles.add(ItemStack.of(list.getCompound(i)));
            }
        }
        return projectiles;
    }

    public static void clear(ItemStack crossbow) {
        CompoundTag tag = crossbow.getTag();
        if (tag != null) {
            tag.put(TAG_CHARGED_PROJECTILES, new ListTag());
        }
    }

    public static boolean contains(ItemStack crossbow, Item item) {
        return get(crossbow).stream().anyMatch(stack -> stack.is(item));
    }
}
