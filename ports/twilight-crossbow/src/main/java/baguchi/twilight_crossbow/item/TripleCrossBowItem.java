package baguchi.twilight_crossbow.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads three projectiles per ammo draw (nine with Multishot) and fans them out like the Triple Bow.
 */
public class TripleCrossBowItem extends TwilightCrossbowItem {
    private static final float TRIPLE_SPREAD = 10.0F;

    public TripleCrossBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        int i = this.getUseDuration(stack) - timeLeft;
        float f = chargePowerForTime(i, stack);
        if (f >= 1.0F && !isCharged(stack) && tryLoadProjectiles(entityLiving, stack)) {
            setCharged(stack, true);
            SoundSource soundsource = entityLiving instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            level.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundsource, 1.0F, 1.0F / (level.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }

    }

    /**
     * 1.21 {@code ProjectileWeaponItem#draw} with three times the projectile count. Only the first projectile uses up
     * ammo; the copies are free (and can only be picked up in creative mode, see {@link #shoot}).
     */
    protected static List<ItemStack> draw(ItemStack weapon, ItemStack ammo, LivingEntity shooter) {
        if (ammo.isEmpty()) {
            return List.of();
        } else {
            int multishot = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, weapon) > 0 ? 3 : 1;
            int i = multishot * 3;
            boolean creative = shooter instanceof Player player && player.getAbilities().instabuild;
            List<ItemStack> list = new ArrayList<>(i);
            ItemStack itemstack1 = ammo.copyWithCount(1);

            for (int j = 0; j < i; ++j) {
                if (j == 0 && !creative) {
                    list.add(ammo.split(1));
                    if (ammo.isEmpty() && shooter instanceof Player player) {
                        player.getInventory().removeItem(ammo);
                    }
                } else {
                    list.add(itemstack1.copy());
                }
            }

            return list;
        }
    }

    private static boolean tryLoadProjectiles(LivingEntity shooter, ItemStack crossbowStack) {
        ItemStack ammo = shooter.getProjectile(crossbowStack);
        if (ammo.isEmpty() && shooter instanceof Player player && player.getAbilities().instabuild) {
            ammo = new ItemStack(Items.ARROW);
        }
        List<ItemStack> list = draw(crossbowStack, ammo, shooter);
        if (!list.isEmpty()) {
            ChargedProjectiles.set(crossbowStack, list);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Upstream fans the projectiles over 10 degrees on top of Multishot's spread.
     */
    @Override
    protected float getProjectileSpread(ItemStack weapon) {
        return TRIPLE_SPREAD + super.getProjectileSpread(weapon);
    }
}
