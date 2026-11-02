package com.lvedy.at_mod.mixin.Naga;

import com.lvedy.at_mod.config.Config;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.ai.goal.NagaMovementPattern;
import twilightforest.entity.boss.Naga;

import java.util.Objects;

/**
 * 1.20.1 backport: targets the TF fork's Naga. Its movement goal getter is {@code getMovementAI()}
 * (4.8: getMovementPattern()); isStunlessCharging/isDazed and NagaMovementPattern#doDaze match 4.8.
 */
@Mixin(Naga.class)
public abstract class NagaMixin {

    @Shadow(remap = false) public abstract boolean isStunlessCharging();
    @Shadow(remap = false) public abstract NagaMovementPattern getMovementAI();

    @Inject(method = "hurt", at = @At("HEAD"))
    public void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Naga naga = (Naga) (Object) this;
        if (this.isStunlessCharging()) {
            Entity attacker = source.getDirectEntity();
            if (attacker == null) {
                attacker = source.getEntity();
            }

            if (attacker != null) {
                Vec3 attacker_speed = attacker.getDeltaMovement();
                Vec3 naga_speed = naga.getDeltaMovement();
                double speed = attacker_speed.subtract(naga_speed).length();
                if (speed > Config.DAZE_SPEED_THRESHOLD.get()) {
                    Vec3 nagaLook = naga.getLookAngle();
                    Vec3 toAttacker = attacker.position().subtract(naga.position()).normalize();
                    double dot = nagaLook.dot(toAttacker);
                    if (dot > 0) { // Frontal attack
                        NagaMovementPattern pattern = this.getMovementAI();
                        if (pattern != null) {
                            pattern.doDaze();
                        }
                    }
                }
            }
        }
    }

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float atmod$modifyHurtAmount(float amount) {
        Naga naga = (Naga) (Object) this;
        double mult = naga.isDazed()
                ? Config.DAZED_DAMAGE_MULTIPLIER.get()
                : Config.NON_DAZED_DAMAGE_MULTIPLIER.get();
        return (float) (amount * mult);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onNaga(EntityType<?> type, Level level, CallbackInfo ci) {
        Naga naga = (Naga) (Object) this;
        double maxHealth;
        try {
            maxHealth = Config.MAX_HEALTH.get();
        } catch (IllegalStateException configNotLoaded) {
            return; // 1.20.1: an entity built before the common config loads keeps TF's default health
        }
        Objects.requireNonNull(naga.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(maxHealth);
        naga.setHealth((float) maxHealth);
    }
}
