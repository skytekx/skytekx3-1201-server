package baguchi.twilight_crossbow.item;

import baguchi.twilight_crossbow.entity.SeekerFireworkRocketEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import twilightforest.entity.projectile.SeekerArrow;
import twilightforest.util.TwilightItemTier;

public class SeekerCrossBowItem extends TwilightCrossbowItem {
    public SeekerCrossBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        return new SeekerArrow(arrow);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            return new SeekerFireworkRocketEntity(level, ammo, shooter, shooter.getX(), shooter.getEyeY() - 0.15F, shooter.getZ(), true);
        } else {
            return super.createProjectile(level, shooter, weapon, ammo, isCrit);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repairWith) {
        // 1.21 TFToolMaterials.STEELEAF == 4.3 TwilightItemTier.STEELEAF (twilightforest:repairs_steeleaf_tools)
        return TwilightItemTier.STEELEAF.getRepairIngredient().test(repairWith);
    }
}
