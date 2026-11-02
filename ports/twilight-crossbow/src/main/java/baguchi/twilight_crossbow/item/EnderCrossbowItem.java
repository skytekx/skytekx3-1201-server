package baguchi.twilight_crossbow.item;

import baguchi.twilight_crossbow.entity.SeekerFireworkRocketEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import twilightforest.item.EnderBowItem;

public class EnderCrossbowItem extends TwilightCrossbowItem {
    public EnderCrossbowItem(Properties properties) {
        super(properties);
    }

    /**
     * Same marker as the Ender Bow: Twilight Forest's ToolEvents#onEnderBowHit swaps the shooter with whatever the
     * projectile hits.
     */
    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.getPersistentData().putBoolean(EnderBowItem.KEY, true);
        return arrow;
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            FireworkRocketEntity fireworkRocket = new SeekerFireworkRocketEntity(level, ammo, shooter, shooter.getX(), shooter.getEyeY() - 0.15F, shooter.getZ(), true);

            fireworkRocket.getPersistentData().putBoolean(EnderBowItem.KEY, true);

            return fireworkRocket;
        } else {
            return super.createProjectile(level, shooter, weapon, ammo, isCrit);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repairWith) {
        return repairWith.is(Items.ENDER_PEARL);
    }

    // 1.21 supportsEnchantment(...) && !PIERCING: no Piercing from the table or from books
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment != Enchantments.PIERCING && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return !EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.PIERCING) && super.isBookEnchantable(stack, book);
    }
}
