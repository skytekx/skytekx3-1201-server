package baguchi.spiced_twilight.mixin;

import baguchi.spiced_twilight.entity.goal.FireBeetleBreathGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.ai.goal.BreathAttackGoal;
import twilightforest.entity.monster.FireBeetle;

@Mixin(FireBeetle.class)
public abstract class FireBeetleMixin extends Monster {

    protected FireBeetleMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void spicedTwilight$replaceBreathGoal(CallbackInfo ci) {

        this.goalSelector.removeAllGoals(goal -> {
            return goal instanceof BreathAttackGoal<?>;
        });

        this.goalSelector.addGoal(2, new FireBeetleBreathGoal<>((FireBeetle) (Object) this, 5.0F, 40, 0.1F));
    }
}
