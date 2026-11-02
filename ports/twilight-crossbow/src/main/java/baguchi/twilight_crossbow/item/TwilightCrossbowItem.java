package baguchi.twilight_crossbow.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Shared base of the Twilight crossbows (1.20.1 backport).
 * <p>
 * In 1.21 {@code CrossbowItem} shoots through overridable instance methods ({@code shoot}, {@code createProjectile},
 * {@code customArrow}, {@code shootProjectile}), which the upstream items override. In 1.20.1 that pipeline is a chain
 * of private static methods, so this class carries an instance copy of it (vanilla 1.20.1 behaviour, 1.21 hook shape).
 * {@link baguchi.twilight_crossbow.mixin.CrossbowItemMixin} sends the static {@code CrossbowItem#performShooting}
 * here for our items, so players ({@code CrossbowItem#use}) and crossbow mobs ({@code CrossbowAttackMob}) both use it.
 */
public abstract class TwilightCrossbowItem extends CrossbowItem {
    private static final float MULTISHOT_SPREAD = 10.0F;

    public TwilightCrossbowItem(Properties properties) {
        super(properties);
    }

    /**
     * Instance version of the static vanilla {@code CrossbowItem#performShooting} (1.21: the instance {@code CrossbowItem#performShooting}).
     */
    public void shootLoadedProjectiles(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, float velocity, float inaccuracy) {
        if (shooter instanceof Player player && ForgeEventFactory.onArrowLoose(weapon, shooter.level(), player, 1, true) < 0) {
            return;
        }

        List<ItemStack> projectiles = ChargedProjectiles.get(weapon);
        if (level instanceof ServerLevel serverLevel && !projectiles.isEmpty()) {
            boolean creative = shooter instanceof Player player && player.getAbilities().instabuild;
            this.shoot(serverLevel, shooter, hand, weapon, projectiles, velocity, inaccuracy, shooter instanceof Player, creative);
        }

        // vanilla CrossbowItem#onCrossbowShot
        if (shooter instanceof ServerPlayer serverPlayer) {
            if (!level.isClientSide()) {
                CriteriaTriggers.SHOT_CROSSBOW.trigger(serverPlayer, weapon);
            }
            serverPlayer.awardStat(Stats.ITEM_USED.get(weapon.getItem()));
        }
        ChargedProjectiles.clear(weapon);
    }

    /**
     * 1.21 {@code ProjectileWeaponItem#shoot}: fans the loaded projectiles out over {@link #getProjectileSpread}.
     * The first projectile is the real ammo; the rest are copies that can only be picked up in creative mode.
     */
    protected void shoot(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, List<ItemStack> projectileItems, float velocity, float inaccuracy, boolean isCrit, boolean creative) {
        float f = this.getProjectileSpread(weapon);
        float f1 = projectileItems.size() == 1 ? 0.0F : 2.0F * f / (float) (projectileItems.size() - 1);
        float f2 = (float) ((projectileItems.size() - 1) % 2) * f1 / 2.0F;
        float f3 = 1.0F;

        for (int i = 0; i < projectileItems.size(); i++) {
            ItemStack itemstack = projectileItems.get(i);
            if (!itemstack.isEmpty()) {
                float f4 = f2 + f3 * (float) ((i + 1) / 2) * f1;
                f3 = -f3;
                Projectile projectile = this.createProjectile(level, shooter, weapon, itemstack, isCrit);
                if (projectile instanceof AbstractArrow arrow && (creative || i > 0)) {
                    arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
                this.shootProjectile(shooter, weapon, projectile, i, velocity, inaccuracy, f4);
                level.addFreshEntity(projectile);
                weapon.hurtAndBreak(this.getDurabilityUse(itemstack), shooter, entity -> entity.broadcastBreakEvent(hand));
                if (weapon.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * Total fan angle; vanilla Multishot spreads its three projectiles 10 degrees apart.
     */
    protected float getProjectileSpread(ItemStack weapon) {
        return EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, weapon) > 0 ? MULTISHOT_SPREAD : 0.0F;
    }

    protected int getDurabilityUse(ItemStack projectile) {
        return projectile.is(Items.FIREWORK_ROCKET) ? 3 : 1;
    }

    /**
     * 1.21 {@code CrossbowItem#createProjectile} (vanilla 1.20.1 {@code CrossbowItem#shootProjectile/getArrow}).
     */
    protected Projectile createProjectile(Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        if (ammo.is(Items.FIREWORK_ROCKET)) {
            return new FireworkRocketEntity(level, ammo, shooter, shooter.getX(), shooter.getEyeY() - 0.15F, shooter.getZ(), true);
        }

        ArrowItem arrowItem = ammo.getItem() instanceof ArrowItem item ? item : (ArrowItem) Items.ARROW;
        AbstractArrow arrow = this.customArrow(arrowItem.createArrow(level, ammo, shooter), ammo, weapon);
        if (isCrit) {
            arrow.setCritArrow(true);
        }
        arrow.setSoundEvent(SoundEvents.CROSSBOW_HIT);
        arrow.setShotFromCrossbow(true);
        int pierce = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, weapon);
        if (pierce > 0) {
            arrow.setPierceLevel((byte) pierce);
        }
        return arrow;
    }

    /**
     * 1.21 {@code ProjectileWeaponItem#customArrow(AbstractArrow, ItemStack, ItemStack)}.
     */
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        return arrow;
    }

    /**
     * 1.21 {@code CrossbowItem#shootProjectile}: crossbow mobs aim at their target, everyone else along the view vector.
     */
    protected void shootProjectile(LivingEntity shooter, ItemStack weapon, Projectile projectile, int index, float velocity, float inaccuracy, float angle) {
        if (shooter instanceof CrossbowAttackMob mob && mob.getTarget() != null) {
            mob.shootCrossbowProjectile(mob.getTarget(), weapon, projectile, angle);
        } else {
            Vec3 up = shooter.getUpVector(1.0F);
            Quaternionf rotation = new Quaternionf().setAngleAxis(angle * ((float) Math.PI / 180.0F), up.x, up.y, up.z);
            Vector3f direction = shooter.getViewVector(1.0F).toVector3f().rotate(rotation);
            projectile.shoot(direction.x(), direction.y(), direction.z(), velocity, inaccuracy);
        }

        shooter.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, shooter.getSoundSource(), 1.0F, getShotPitch(shooter.getRandom(), index));
    }

    private static float getShotPitch(RandomSource random, int index) {
        return index == 0 ? 1.0F : randomShotPitch((index & 1) == 1, random);
    }

    private static float randomShotPitch(boolean isHighPitched, RandomSource random) {
        float f = isHighPitched ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    /**
     * Vanilla {@code CrossbowItem#getPowerForTime} (private in 1.20.1).
     */
    protected static float chargePowerForTime(int useTime, ItemStack crossbowStack) {
        float f = (float) useTime / (float) getChargeDuration(crossbowStack);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    @Override
    public boolean useOnRelease(ItemStack stack) {
        return true;
    }
}
