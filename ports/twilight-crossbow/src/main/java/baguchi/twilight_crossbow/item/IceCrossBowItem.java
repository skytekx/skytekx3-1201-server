package baguchi.twilight_crossbow.item;

import baguchi.twilight_crossbow.entity.IceFireworkRocketEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import twilightforest.entity.projectile.IceArrow;
import twilightforest.util.TwilightItemTier;

public class IceCrossBowItem extends TwilightCrossbowItem {
    public IceCrossBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        // The fork's IceArrow takes over the owner, pickup rule, pickup item and on-hit effects of the arrow it replaces.
        return new IceArrow(arrow);
    }

    @Override
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            return new IceFireworkRocketEntity(level, ammo, shooter, shooter.getX(), shooter.getEyeY() - 0.15F, shooter.getZ(), true);
        } else {
            return super.createProjectile(level, shooter, weapon, ammo, isCrit);
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repairWith) {
        // 1.21 TFToolMaterials.ICE == 4.3 TwilightItemTier.ICE (twilightforest:repairs_ice_tools)
        return TwilightItemTier.ICE.getRepairIngredient().test(repairWith);
    }
}
