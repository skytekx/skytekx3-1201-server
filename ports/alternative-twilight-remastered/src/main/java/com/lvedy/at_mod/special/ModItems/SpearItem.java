package com.lvedy.at_mod.special.ModItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.lvedy.at_mod.AlternativeTwilight;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IArmPoseTransformer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 1.20.1 backport notes:
 * <ul>
 *   <li>1.21's ItemAttributeModifiers component becomes {@link #getDefaultAttributeModifiers}; the reach bonus
 *   (entity_interaction_range) is Forge's {@link ForgeMod#ENTITY_REACH}.</li>
 *   <li>1.21's Tool component (sword-efficient blocks at 1.5x, 2 durability per mined block) becomes
 *   {@link #getDestroySpeed} and {@link #mineBlock}.</li>
 *   <li>NeoForge's enum extension for the arm pose becomes Forge's {@code HumanoidModel.ArmPose.create}.</li>
 * </ul>
 */
public class SpearItem extends TieredItem {

    public static final ResourceKey<DamageType> SPEAR_DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE, AlternativeTwilight.prefix("spear"));

    // 玩家持矛被打后的保护期截止 gameTime（写入玩家 PersistentData）
    public static final String PROTECT_UNTIL_KEY = "AtmodSpearProtectUntil";

    // name-based UUID of the upstream modifier id at_mod:range_modifier
    private static final UUID RANGE_MODIFIER_UUID = UUID.fromString("3746d1d6-3615-3233-8aeb-fdbf719f6203");

    // 保存右键时的突进速度
    private final float dashSpeed;
    private final double attackDamage;
    private final double attackSpeed;
    private final double range;
    private Multimap<Attribute, AttributeModifier> mainHandModifiers;

    public SpearItem(Tier tier, Properties properties, double damage, double speed, double range, float dashSpeed) {
        super(tier, properties);
        this.attackDamage = damage;
        this.attackSpeed = speed;
        this.range = range;
        this.dashSpeed = dashSpeed;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getDefaultAttributeModifiers(slot);
        }
        // built lazily: ForgeMod.ENTITY_REACH is a RegistryObject that only resolves after registration
        if (this.mainHandModifiers == null) {
            this.mainHandModifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
                    .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", this.attackDamage, AttributeModifier.Operation.ADDITION))
                    .put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", this.attackSpeed, AttributeModifier.Operation.ADDITION))
                    .put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(RANGE_MODIFIER_UUID, "Spear reach", this.range, AttributeModifier.Operation.ADDITION))
                    .build();
        }
        return this.mainHandModifiers;
    }

    // Tool component: sword-efficient blocks mine at 1.5, everything else at the default 1.0
    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, BlockState state) {
        return state.is(BlockTags.SWORD_EFFICIENT) ? 1.5F : 1.0F;
    }

    // Tool component: 2 durability per mined block that is not instantly broken
    @Override
    public boolean mineBlock(@NotNull ItemStack stack, Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull LivingEntity miner) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, miner, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public boolean canAttackBlock(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull Entity entity) {
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return super.onLeftClickEntity(stack, player, entity);
    }

    // 设置玩家手持长矛时的动画为“矛”专属动画（手臂微微抬起并固定）
    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // 确保在服务端运行，玩家正手持长矛且速度大于1
        if (!level.isClientSide() && isSelected && entity instanceof Player player && player.getDeltaMovement().length() > 1.0) {
            // 攻击冷却中（蓄力未满）不造成伤害
            if (player.getAttackStrengthScale(0F) < 1.0F) {
                return;
            }
            // 被击保护期内不造成伤害
            long protectUntil = player.getPersistentData().getLong(PROTECT_UNTIL_KEY);
            if (level.getGameTime() < protectUntil) {
                return;
            }

            AABB boundingBox = player.getBoundingBox().inflate(1.6);
            List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, boundingBox, e -> e != player && e.isAlive());

            if (!targets.isEmpty()) {
                float totalDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                var damageRegistry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
                var damageHolder = damageRegistry.getHolderOrThrow(SPEAR_DAMAGE);
                DamageSource spearSource = new DamageSource(damageHolder, player);

                for (LivingEntity target : targets) {
                    if (target.invulnerableTime == 0) {
                        float speed = (float) player.getDeltaMovement().length();
                        target.hurt(spearSource, (float) Math.max(1, totalDamage * Math.pow(speed - 1, 2)));
                        target.invulnerableTime = 10; // 给目标 10 tick 无敌时间
                        stack.hurtAndBreak(2, player, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
                    }
                }
            }
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
        // 攻击冷却中禁止冲刺
        if (player.getAttackStrengthScale(0F) < 1.0F) {
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }
        // 使用保存的 dashSpeed 作为初速度
        Vec3 vec1 = player.getForward().scale(this.dashSpeed);
        player.setDeltaMovement(vec1);
        player.getCooldowns().addCooldown(this, 100);
        return super.use(level, player, usedHand);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(ClientExtensions.INSTANCE);
    }

    @OnlyIn(Dist.CLIENT)
    public static class ClientExtensions implements IClientItemExtensions {
        static final ClientExtensions INSTANCE = new ClientExtensions();

        // Forge 47 extends the ArmPose enum through ArmPose.create (NeoForge: META-INF/enumextensions.json + EnumProxy)
        public static final HumanoidModel.ArmPose SPEAR_POSE = HumanoidModel.ArmPose.create("AT_MOD_SPEAR_POSE", false,
                (IArmPoseTransformer) (model, entity, arm) -> {
                    if (arm == HumanoidArm.RIGHT) {
                        model.rightArm.xRot = -0.5F; // 向前略微抬起
                        model.rightArm.yRot = -0.1F;
                        model.rightArm.zRot = 0.0F;
                    } else {
                        model.leftArm.xRot = -0.5F;
                        model.leftArm.yRot = 0.1F;
                        model.leftArm.zRot = 0.0F;
                    }
                });

        @Override
        public HumanoidModel.ArmPose getArmPose(@NotNull LivingEntity entityLiving, @NotNull InteractionHand hand, @NotNull ItemStack itemStack) {
            return SPEAR_POSE;
        }
    }
}
