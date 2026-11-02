package baguchi.spiced_twilight.mixin;

import baguchi.spiced_twilight.util.LichCompat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.ai.goal.LichAbsorbMinionsGoal;
import twilightforest.entity.boss.Lich;
import twilightforest.entity.monster.LichMinion;
import twilightforest.init.TFEntities;
import twilightforest.init.TFSounds;
import twilightforest.item.LifedrainScepterItem;
import twilightforest.util.EntityUtil;

import java.util.List;

/**
 * 40% of the time the Lich turns a minion into a Skeleton Druid (healing half the minion's health) instead of popping
 * it. Falls through to Twilight Forest's own absorb logic otherwise.
 * <p>
 * 1.20.1 backport: upstream injects right after {@code super.tick()}; {@code Goal#tick} is empty, so HEAD is the same
 * point and does not depend on how the fork's tick body looks.
 */
@Mixin(LichAbsorbMinionsGoal.class)
public abstract class LichAbsorbMinionsGoalMixin {

    @Shadow(remap = false)
    @Final
    private Lich lich;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void spicedTwilight$convertMinion(CallbackInfo ci) {
        if (LichCompat.getTeleportInvisibility(this.lich) <= 0) {
            if (this.lich.getScepterTimeLeft() <= 0) {
                List<LichMinion> minions = this.lich.level().getEntitiesOfClass(LichMinion.class, this.lich.getBoundingBox().inflate(32.0D, 16.0D, 32.0D)).stream().filter((m) -> m.master == this.lich).toList();
                if (!minions.isEmpty() && this.lich.getRandom().nextFloat() < 0.4F) {
                    LichMinion minion = minions.get(0);
                    // shatter before converting: the converted minion is discarded and no longer tracked
                    LifedrainScepterItem.animateTargetShatter((ServerLevel) this.lich.level(), minion);
                    minion.convertTo(TFEntities.SKELETON_DRUID.get(), true);
                    SoundEvent deathSound = EntityUtil.getDeathSound(minion);
                    if (deathSound != null) {
                        this.lich.level().playSound((Player) null, minion.blockPosition(), deathSound, SoundSource.HOSTILE, 1.0F, minion.getVoicePitch());
                    }

                    this.lich.playSound(TFSounds.LICH_POP_MOB.get(), 3.0F, 0.4F + this.lich.getRandom().nextFloat() * 0.2F);
                    minion.playSound(TFSounds.LICH_POP_MOB.get(), 3.0F, 0.4F + this.lich.getRandom().nextFloat() * 0.2F);
                    this.lich.makeMagicTrail(minion.getEyePosition(), this.lich.getEyePosition(), 1.0F, 0.5F, 0.5F);
                    this.lich.heal(minion.getHealth() / 2);
                    this.lich.swing(InteractionHand.MAIN_HAND);
                    this.lich.setPopCooldown(40);
                    this.lich.gameEvent(GameEvent.ENTITY_DIE);
                    ci.cancel();
                }

            }
        }
    }
}
