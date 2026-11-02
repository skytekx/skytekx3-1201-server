package com.lvedy.at_mod.mixin.Lich;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twilightforest.entity.boss.Lich;
import twilightforest.entity.monster.LichMinion;
import twilightforest.entity.projectile.LichBolt;
import twilightforest.entity.projectile.LichBomb;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 1.20.1 backport notes: targets the TF fork's Lich ({@code hurt} and {@code customServerAiStep} are Minecraft
 * overrides and go through the refmap; {@code getPhase}, {@code setMinionsToSummon} and {@code setShieldStrength}
 * are Twilight Forest's own names, so they are not remapped). Attribute modifiers are keyed by name-based UUIDs of
 * the upstream ids, the colored ENTITY_EFFECT particle uses the 1.20.1 "count 0 = color" packet form, and the
 * summoned minion goes through {@link ForgeEventFactory#onFinalizeSpawn} like TF's own LichMinionsGoal.
 */
@Mixin(Lich.class)
public abstract class LichMixin {

    @Shadow(remap = false) public abstract int getPhase();
    @Shadow(remap = false) public abstract void setMinionsToSummon(int minionsToSummon);

    // fixed name-based (v3) UUIDs derived from the upstream modifier ids at_mod:phase3_health_bonus / _armor_bonus / _armor_toughness_bonus
    @Unique
    private static final UUID PHASE3_HEALTH_ID = UUID.fromString("784b74c3-4343-384e-be46-31ccfec26587");
    @Unique
    private static final UUID PHASE3_ARMOR_ID = UUID.fromString("c49a9d0f-7aa3-3e56-afde-097441507fe2");
    @Unique
    private static final UUID PHASE3_ARMOR_TOUGHNESS_ID = UUID.fromString("5047595b-9037-3953-b4ca-3ac41376ae5f");

    @Unique
    private boolean atmod$phase3AttributesApplied = false;
    @Unique
    private int atmod$magicOrbCooldown = 0;
    @Unique
    private float atmod$pendingHealthDeductionRatio = 0.0F;

    @Unique
    private final Map<ThrowableProjectile, Vec3> atmod$stasisProjectilesTargetPos = new HashMap<>();
    @Unique
    private final Map<ThrowableProjectile, Integer> atmod$stasisProjectilesTicksLeft = new HashMap<>();
    @Unique
    private final Map<ThrowableProjectile, Integer> atmod$stasisProjectilesTotalTicks = new HashMap<>();

    @Inject(method = "customServerAiStep", at = @At("TAIL"))
    private void atmod$phase3Logic(CallbackInfo ci) {
        Lich self = (Lich) (Object) this;
        if (self.level().isClientSide()) return;

        int phase = this.getPhase();

        // 进入三阶段时应用属性加成
        if (phase == 3 && !atmod$phase3AttributesApplied) {
            atmod$applyPhase3Attributes(self);
            atmod$phase3AttributesApplied = true;
            atmod$magicOrbCooldown = Config.LICH_PHASE3_MAGIC_ORB_INTERVAL.get();
        }

        if (phase == 3) {
            // 应用延迟的百分比生命值扣除 (在三阶段属性提升和回满血后执行)
            if (atmod$pendingHealthDeductionRatio > 0) {
                float damageAmount = self.getMaxHealth() * atmod$pendingHealthDeductionRatio;
                float newHealth = Math.max(1.0F, self.getHealth() - damageAmount);
                self.setHealth(newHealth);
                atmod$pendingHealthDeductionRatio = 0;
            }

            atmod$handleMagicOrbSkill(self);
        }
    }

    @Unique
    private void atmod$handleMagicOrbSkill(Lich self) {
        // 捕获周围停滞的法球（处理玩家重进游戏导致的Map数据丢失）
        List<ThrowableProjectile> nearbyProjectiles = self.level().getEntitiesOfClass(ThrowableProjectile.class, self.getBoundingBox().inflate(32.0), p ->
            (p instanceof LichBolt || p instanceof LichBomb) && p.getOwner() == self && p.isNoGravity() && p.getDeltaMovement().lengthSqr() < 0.01
        );

        for (ThrowableProjectile p : nearbyProjectiles) {
            if (!atmod$stasisProjectilesTargetPos.containsKey(p)) {
                LivingEntity target = self.getTarget();
                if (target != null) {
                    atmod$stasisProjectilesTargetPos.put(p, target.getBoundingBox().getCenter());
                    atmod$stasisProjectilesTicksLeft.put(p, 40);
                    atmod$stasisProjectilesTotalTicks.put(p, 40);
                } else {
                    p.discard(); // 如果没有目标，清除无用停滞法球
                }
            }
        }

        // 处理处于静止状态的法球
        Iterator<Map.Entry<ThrowableProjectile, Vec3>> iterator = atmod$stasisProjectilesTargetPos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ThrowableProjectile, Vec3> entry = iterator.next();
            ThrowableProjectile projectile = entry.getKey();
            Vec3 targetPos = entry.getValue();

            if (!projectile.isAlive()) {
                iterator.remove();
                atmod$stasisProjectilesTicksLeft.remove(projectile);
                atmod$stasisProjectilesTotalTicks.remove(projectile);
                continue;
            }

            Integer ticksLeft = atmod$stasisProjectilesTicksLeft.get(projectile);
            Integer totalTicks = atmod$stasisProjectilesTotalTicks.get(projectile);

            if (ticksLeft == null || totalTicks == null) {
                iterator.remove();
                atmod$stasisProjectilesTicksLeft.remove(projectile);
                atmod$stasisProjectilesTotalTicks.remove(projectile);
                continue;
            }

            // 计算绘制进度 (前一半时间用于绘制完整线条)
            float drawProgress = Math.min(1.0F, (float) (totalTicks - ticksLeft) / (totalTicks / 2.0F));

            // 绘制轨迹粒子（使用记录的目标位置）
            atmod$drawTrajectoryParticles(projectile, targetPos, drawProgress);

            // 保持静止
            projectile.setDeltaMovement(Vec3.ZERO);
            projectile.setNoGravity(true);

            if (ticksLeft > 0) {
                atmod$stasisProjectilesTicksLeft.put(projectile, ticksLeft - 1);
            } else {
                // 发射法球：朝向记录的目标位置
                Vec3 dir = targetPos.subtract(projectile.position()).normalize();
                projectile.setNoGravity(false);
                projectile.shoot(dir.x, dir.y, dir.z, 0.75F, 0.1F); // 稍微提高一点速度

                iterator.remove();
                atmod$stasisProjectilesTicksLeft.remove(projectile);
                atmod$stasisProjectilesTotalTicks.remove(projectile);
            }
        }

        // 技能冷却
        if (atmod$magicOrbCooldown > 0) {
            atmod$magicOrbCooldown--;
        } else if (self.getTarget() != null) {
            // 释放技能：召唤3个法球
            atmod$spawnMagicOrbs(self);
            atmod$magicOrbCooldown = Config.LICH_PHASE3_MAGIC_ORB_INTERVAL.get();
        }
    }

    @Unique
    private void atmod$spawnMagicOrbs(Lich self) {
        LivingEntity target = self.getTarget();
        if (target == null) return;

        Vec3 targetPos = target.getBoundingBox().getCenter();

        // 根据当前生命值百分比确定召唤法球数量
        float healthPercent = self.getHealth() / self.getMaxHealth();
        int orbCount;
        if (healthPercent < Config.LICH_PHASE3_ORB_COUNT_THRESHOLD.get().floatValue()) {
            orbCount = Config.LICH_PHASE3_ORB_COUNT_AFTER_THRESHOLD.get();
        } else {
            orbCount = Config.LICH_PHASE3_ORB_COUNT_BEFORE_THRESHOLD.get();
        }

        for (int i = 0; i < orbCount; i++) {
            ThrowableProjectile projectile;
            boolean isBolt = self.getRandom().nextBoolean();
            if (isBolt) {
                projectile = new LichBolt(self.level(), self);
            } else {
                projectile = new LichBomb(self.level(), self);
            }

            // 寻找无遮挡的生成位置 (使用 orbCount 来均匀分布角度)
            Vec3 spawnPos = atmod$findValidSpawnPos(self, targetPos, i, orbCount);

            projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            projectile.setDeltaMovement(Vec3.ZERO);
            projectile.setNoGravity(true);

            self.level().addFreshEntity(projectile);

            atmod$stasisProjectilesTargetPos.put(projectile, targetPos);
            atmod$stasisProjectilesTicksLeft.put(projectile, 40);
            atmod$stasisProjectilesTotalTicks.put(projectile, 40);
        }
    }

    @Unique
    private Vec3 atmod$findValidSpawnPos(Lich self, Vec3 targetPos, int index, int total) {
        Level level = self.level();
        double baseAngle = index * (2 * Math.PI / total);

        // 尝试多个半径和角度寻找最佳位置，半径至少10格
        for (double radius : new double[]{10.0, 12.0, 14.0}) {
            for (int attempt = 0; attempt < 8; attempt++) {
                double angle = baseAngle + (attempt * Math.PI / 4);
                double x = self.getX() + Math.cos(angle) * radius;
                double y = self.getY() + 2.0 + self.getRandom().nextDouble() * 2.0;
                double z = self.getZ() + Math.sin(angle) * radius;
                Vec3 candidate = new Vec3(x, y, z);

                // 检查生成点是否在方块内
                if (!level.getBlockState(BlockPos.containing(x, y, z)).isAir()) continue;

                // 检查到目标的视线
                ClipContext context = new ClipContext(
                    candidate, targetPos,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    self
                );
                if (level.clip(context).getType() == HitResult.Type.MISS) {
                    return candidate;
                }
            }
        }

        // 如果找不到完美的，返回一个至少离巫妖10格的默认位置
        double x = self.getX() + Math.cos(baseAngle) * 10.0;
        double y = self.getY() + 2.5;
        double z = self.getZ() + Math.sin(baseAngle) * 10.0;
        return new Vec3(x, y, z);
    }

    @Unique
    private void atmod$drawTrajectoryParticles(ThrowableProjectile projectile, Vec3 targetPos, float progress) {
        if (projectile.level() instanceof ServerLevel serverLevel) {
            Vec3 start = projectile.position();
            Vec3 dir = targetPos.subtract(start).normalize();

            // 计算轨迹终点：从法球开始，穿过目标，直到撞击方块或达到最大距离（如64格）
            ClipContext context = new ClipContext(
                start, start.add(dir.scale(64.0)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                projectile
            );
            HitResult hitResult = serverLevel.clip(context);
            Vec3 endPos = hitResult.getLocation();
            double totalDist = endPos.distanceTo(start);

            // 逐渐绘制粒子效果：根据进度决定绘制长度
            double currentMaxDist = totalDist * progress;
            boolean bolt = projectile instanceof LichBolt;
            for (double d = 0; d < currentMaxDist; d += 0.6) {
                Vec3 particlePos = start.add(dir.scale(d));
                if (bolt) {
                    // 1.20.1: ENTITY_EFFECT takes its color from the velocity; a count-0 packet sends (dx, dy, dz) * speed
                    // as that velocity, which gives upstream's ColorParticleOption (0.3, 0.7, 1.0)
                    serverLevel.sendParticles(ParticleTypes.ENTITY_EFFECT,
                        particlePos.x, particlePos.y, particlePos.z,
                        0, 0.3F, 0.7F, 1.0F, 1.0F);
                } else {
                    ParticleOptions particle = ParticleTypes.FLAME;
                    serverLevel.sendParticles(
                        particle,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0
                    );
                }
            }
        }
    }

    @Unique
    private void atmod$applyPhase3Attributes(Lich self) {
        AttributeInstance maxHealth = self.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.getModifier(PHASE3_HEALTH_ID) == null) {
            maxHealth.addPermanentModifier(new AttributeModifier(
                    PHASE3_HEALTH_ID,
                    "at_mod:phase3_health_bonus",
                    Config.LICH_PHASE3_MAX_HEALTH_BONUS.get(),
                    AttributeModifier.Operation.ADDITION
            ));
            self.setHealth(self.getMaxHealth());
        }

        AttributeInstance armor = self.getAttribute(Attributes.ARMOR);
        if (armor != null && armor.getModifier(PHASE3_ARMOR_ID) == null) {
            armor.addPermanentModifier(new AttributeModifier(
                    PHASE3_ARMOR_ID,
                    "at_mod:phase3_armor_bonus",
                    Config.LICH_PHASE3_ARMOR_BONUS.get(),
                    AttributeModifier.Operation.ADDITION
            ));
        }

        AttributeInstance armorToughness = self.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughness != null && armorToughness.getModifier(PHASE3_ARMOR_TOUGHNESS_ID) == null) {
            armorToughness.addPermanentModifier(new AttributeModifier(
                    PHASE3_ARMOR_TOUGHNESS_ID,
                    "at_mod:phase3_armor_toughness_bonus",
                    Config.LICH_PHASE3_ARMOR_TOUGHNESS_BONUS.get(),
                    AttributeModifier.Operation.ADDITION
            ));
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void atmod$limitPhase2Damage(DamageSource src, float damage, CallbackInfoReturnable<Boolean> cir) {
        Lich self = (Lich) (Object) this;
        if (self.level().isClientSide()) return;

        // 二阶段伤害限制为1
        if (this.getPhase() == 2 && damage > 1.0F) {
            // 递归调用 hurt，但伤害限制为1
            cir.setReturnValue(self.hurt(src, 1.0F));
            return;
        }

        // 检测周围仆从数量，如果>=3则进入3阶段
        if (this.getPhase() == 2) {
            AABB searchBox = self.getBoundingBox().inflate(32.0);
            List<LichMinion> nearbyMinions = self.level().getEntitiesOfClass(
                    LichMinion.class,
                    searchBox,
                    minion -> minion.master == self && minion.isAlive()
            );

            int count = nearbyMinions.size();
            if (count >= 3) {

                // 1. 清除场上全部仆从
                for (LichMinion minion : nearbyMinions) {
                    minion.discard(); // 使用 discard 而不是 kill，避免触发死亡掉落或逻辑
                }

                // 2. 获得嗜血效果：每只仆从增加一级，持续10000小时
                int duration = 10000 * 3600 * 20; // 10000小时
                int amplifier = count - 1; // count只仆从 = level count (amplifier count-1)
                self.addEffect(new MobEffectInstance(
                        ModEffects.BLOOD_LUST.get(),
                        duration,
                        amplifier,
                        false,
                        true,
                        true
                ));

                // 3. 记录扣除生命值的比例：每只10%，最高50%
                double lossPerMinion = Config.LICH_PHASE2_TRANSITION_HP_LOSS_PER_MINION.get();
                double maxLossCap = Config.LICH_PHASE2_TRANSITION_MAX_HP_LOSS_CAP.get();
                atmod$pendingHealthDeductionRatio = (float) Math.min(maxLossCap, count * lossPerMinion);

                // 4. 设置为0进入三阶段 (这会触发原版的属性加成和满血逻辑)
                this.setMinionsToSummon(0);

                // 限制本次伤害为1.0
                cir.setReturnValue(self.hurt(src, 1.0F));
            }
        }
    }

    @Inject(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Ltwilightforest/entity/boss/Lich;setShieldStrength(I)V",
                    shift = At.Shift.AFTER,
                    remap = false
            )
    )
    private void atmod$onShieldBreak(DamageSource src, float damage, CallbackInfoReturnable<Boolean> cir) {
        Lich self = (Lich) (Object) this;
        if (self.level().isClientSide()) return;

        // 召唤一只 LichMinion（属性由 LichMinionSpawnHandler 统一应用）
        LichMinion minion = new LichMinion(self.level(), self);
        minion.moveTo(self.getX(), self.getY(), self.getZ(), self.getYRot(), 0.0F);
        if (self.level() instanceof ServerLevel serverLevel) {
            ForgeEventFactory.onFinalizeSpawn(
                    minion,
                    serverLevel,
                    serverLevel.getCurrentDifficultyAt(self.blockPosition()),
                    MobSpawnType.MOB_SUMMONED,
                    null,
                    null
            );
        }

        self.level().addFreshEntity(minion);

        // 给破盾者施加嗜血诅咒
        if (src.getEntity() instanceof LivingEntity attacker) {
            MobEffectInstance existing = attacker.getEffect(ModEffects.BLOOD_LUST_CURSE.get());
            int newLevel;
            int duration;

            if (existing == null) {
                newLevel = 0;
                duration = Config.LICH_BLOOD_LUST_CURSE_DURATION.get();
            } else {
                newLevel = existing.getAmplifier() + 1;
                duration = existing.getDuration(); // 保留剩余时间
            }

            attacker.addEffect(new MobEffectInstance(
                    ModEffects.BLOOD_LUST_CURSE.get(),
                    duration,
                    newLevel,
                    false,
                    true,
                    true
            ));
        }
    }
}
