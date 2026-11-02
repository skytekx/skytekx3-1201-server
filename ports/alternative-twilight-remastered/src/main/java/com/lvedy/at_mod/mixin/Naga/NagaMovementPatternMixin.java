package com.lvedy.at_mod.mixin.Naga;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.special.ModEntity.entity.NagaVenomEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import twilightforest.entity.ai.goal.NagaMovementPattern;
import twilightforest.entity.boss.Naga;

/**
 * 1.20.1 backport: the TF fork's NagaMovementPattern has the same fields and states as 4.8
 * ({@code naga}, {@code state}, {@code stateCounter}, private {@code transitionState()}); only {@code tick}
 * is a Minecraft (Goal) name and goes through the refmap.
 */
@Mixin(NagaMovementPattern.class)
public abstract class NagaMovementPatternMixin {

    @Shadow(remap = false) @Final private Naga naga;
    @Shadow(remap = false) private NagaMovementPattern.MovementState state;
    @Shadow(remap = false) private int stateCounter;

    @Unique
    private boolean at_mod$hasShot = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        // 原有逻辑：血量低于阈值时强制无晕冲撞
        if (this.naga.getHealth() <= this.naga.getMaxHealth() * Config.PHASE2_HEALTH_RATIO.get()) {
            if (this.state == NagaMovementPattern.MovementState.INTIMIDATE) {
                this.naga.setStunlessCharging(true);
            }
        }

        // 毒液发射逻辑：进入CHARGE或STUNLESS_CHARGE时发射毒液
        if ((this.state == NagaMovementPattern.MovementState.CHARGE
                || this.state == NagaMovementPattern.MovementState.STUNLESS_CHARGE)
                && !at_mod$hasShot) {
            at_mod$shootVenom();
            at_mod$hasShot = true;
        }

        // 非冲撞状态时重置标记
        if (this.state != NagaMovementPattern.MovementState.CHARGE
                && this.state != NagaMovementPattern.MovementState.STUNLESS_CHARGE) {
            at_mod$hasShot = false;
        }
    }

    @Unique
    private void at_mod$shootVenom() {
        if (this.naga.level().isClientSide()) return;

        LivingEntity target = this.naga.getTarget();
        if (target == null) return;

        // 从娜迦头部位置发射
        Vec3 headPos = this.naga.getEyePosition();
        int count = Config.VENOM_COUNT.get();
        double spread = Config.VENOM_SPREAD.get();

        for (int i = 0; i < count; i++) {
            NagaVenomEntity venom = new NagaVenomEntity(this.naga.level(), this.naga);
            venom.setPos(headPos.x, headPos.y, headPos.z);

            // 目标位置添加随机偏移，使毒液散开
            double targetX = target.getX() + (this.naga.getRandom().nextDouble() - 0.5) * spread;
            double targetY = target.getY();
            double targetZ = target.getZ() + (this.naga.getRandom().nextDouble() - 0.5) * spread;

            // 计算抛物线初速度
            Vec3 velocity = at_mod$calculateArcVelocity(headPos, targetX, targetY, targetZ);
            venom.setDeltaMovement(velocity);

            this.naga.level().addFreshEntity(venom);
        }
    }

    @Unique
    private Vec3 at_mod$calculateArcVelocity(Vec3 start, double targetX, double targetY, double targetZ) {
        double dx = targetX - start.x;
        double dy = targetY - start.y;
        double dz = targetZ - start.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        // 根据水平距离动态调整飞行时间（距离越远飞行越久）
        double flightTicks = Math.max(15, Math.min(40, horizontalDist * 1.5));
        double gravity = 0.05; // 与实体中的getGravity保持一致

        double vx = dx / flightTicks;
        double vz = dz / flightTicks;
        // vy = dy/T + 0.5*g*T，保证到达目标高度的同时产生向上的抛物线
        double vy = dy / flightTicks + 0.5 * gravity * flightTicks;

        return new Vec3(vx, vy, vz);
    }

    @Inject(method = "transitionState", at = @At("HEAD"), cancellable = true, remap = false)
    private void onTransitionState(CallbackInfo ci) {
        if (this.naga.getHealth() <= this.naga.getMaxHealth() * Config.PHASE3_HEALTH_RATIO.get()) {
            if (this.state == NagaMovementPattern.MovementState.STUNLESS_CHARGE) {
                this.stateCounter = 20;
                this.state = NagaMovementPattern.MovementState.INTIMIDATE;
                ci.cancel();
            }
        }
    }
}
