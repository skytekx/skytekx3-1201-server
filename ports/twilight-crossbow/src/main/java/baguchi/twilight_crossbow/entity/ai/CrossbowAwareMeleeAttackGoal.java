package baguchi.twilight_crossbow.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.item.CrossbowItem;

/**
 * Melee attack that stands down while the mob holds a crossbow (upstream: an anonymous class inside SnowGuardianMixin;
 * moved out here because Mixin 0.8 does not copy anonymous inner classes of mixins into the target).
 */
public class CrossbowAwareMeleeAttackGoal extends MeleeAttackGoal {
    public CrossbowAwareMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    public boolean canUse() {
        return !this.mob.isHolding(stack -> stack.getItem() instanceof CrossbowItem) && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return !this.mob.isHolding(stack -> stack.getItem() instanceof CrossbowItem) && super.canContinueToUse();
    }
}
