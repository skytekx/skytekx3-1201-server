package twilightforest.entity.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFSounds;

import java.util.UUID;

public class LoyalZombie extends TamableAnimal {

	// Own baby flag instead of AgeableMob's age, so a baby loyal zombie never grows up
	private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(LoyalZombie.class, EntityDataSerializers.BOOLEAN);
	// Same id as the vanilla zombie's baby speed boost (4.8 uses the matching minecraft:baby id)
	private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
	private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.5D, AttributeModifier.Operation.MULTIPLY_BASE);
	private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	// Mystic Crown Compat: summoned by a Zombie Scepter under an empowering Mystic Crown
	private boolean crownSummoned;

	public LoyalZombie(EntityType<? extends LoyalZombie> type, Level world) {
		super(type, world);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, true));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
		this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, true));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.getEntityData().define(DATA_BABY_ID, false);
	}

	@Override
	public Animal getBreedOffspring(ServerLevel level, AgeableMob animal) {
		return null;
	}

	public static AttributeSupplier.Builder registerAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 40.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ARMOR, 3.0D);
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		// Mystic Crown Compat: crown-summoned zombies last three times as long and may wear armor, but hit for 4 instead of 7
		boolean success = entity.hurt(this.damageSources().mobAttack(this), this.crownSummoned ? 4.0F : 7.0F);

		if (success) {
			entity.push(0.0D, 0.2D, 0.0D);
			// knockback from the held item (the crown's Knockback II stick), as Mob#doHurtTarget applies it
			int knockback = EnchantmentHelper.getKnockbackBonus(this);
			if (knockback > 0 && entity instanceof LivingEntity living) {
				living.knockback(knockback * 0.5F, Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), -Mth.cos(this.getYRot() * Mth.DEG_TO_RAD));
				this.setDeltaMovement(this.getDeltaMovement().multiply(0.6D, 1.0D, 0.6D));
			}
		}

		return success;
	}

	/**
	 * Mystic Crown Compat: dresses a zombie summoned under an empowering Mystic Crown. A baby gets a full set of
	 * leather armor and a Knockback II stick, a grown zombie has a 50% chance of one random leather armor piece.
	 * The gear never drops, so the scepter is no source of free items.
	 */
	public void equipCrownGear(RandomSource random) {
		this.crownSummoned = true;
		if (this.isBaby()) {
			for (EquipmentSlot slot : ARMOR_SLOTS) {
				this.setItemSlot(slot, new ItemStack(leatherArmorFor(slot)));
			}
			ItemStack stick = new ItemStack(Items.STICK);
			stick.enchant(Enchantments.KNOCKBACK, 2);
			this.setItemSlot(EquipmentSlot.MAINHAND, stick);
		} else if (random.nextFloat() < 0.5F) {
			EquipmentSlot slot = ARMOR_SLOTS[random.nextInt(ARMOR_SLOTS.length)];
			this.setItemSlot(slot, new ItemStack(leatherArmorFor(slot)));
		}
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			this.setDropChance(slot, 0.0F);
		}
	}

	private static Item leatherArmorFor(EquipmentSlot slot) {
		return switch (slot) {
			case HEAD -> Items.LEATHER_HELMET;
			case CHEST -> Items.LEATHER_CHESTPLATE;
			case LEGS -> Items.LEATHER_LEGGINGS;
			default -> Items.LEATHER_BOOTS;
		};
	}

	@Override
	public void aiStep() {
		// once our damage boost effect wears out, start to decay
		// the effect here is that we die shortly after our 60 second lifespan
		if (!this.level().isClientSide() && this.getEffect(MobEffects.DAMAGE_BOOST) == null) {
			if (this.tickCount % 20 == 0) {
				this.hurt(TFDamageTypes.getDamageSource(this.level(), TFDamageTypes.EXPIRED), 2);
			}
		}

		super.aiStep();
	}

	@Override
	public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand hand) {
		//feeding a loyal zombie rotten flesh will refresh its death timer, allowing your minions to stick around for longer
		if (this.getOwner() != null && this.getOwner().is(player) && player.getItemInHand(hand).is(Items.ROTTEN_FLESH)) {
			this.removeEffect(MobEffects.DAMAGE_BOOST);
			this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));
			this.heal(1.0F);
			this.playSound(SoundEvents.ZOMBIE_INFECT, this.getSoundVolume(), this.getVoicePitch());
			if (!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
			return InteractionResult.sidedSuccess(this.level().isClientSide());
		}

		return super.interactAt(player, vec3, hand);
	}

	/**
	 * [VanillaCopy] {@link Wolf#wantsToAttack(LivingEntity, LivingEntity)} ()}, substituting with our class
	 */
	@Override
	public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
		if (!(target instanceof Creeper) && !(target instanceof Ghast)) {
			if (target instanceof LoyalZombie zombie) {
				return !zombie.isTame() || zombie.getOwner() != owner;
			} else if (target instanceof Player pTarget && owner instanceof Player pOwner && !pOwner.canHarmPlayer(pTarget)) {
				return false;
			} else if (target instanceof AbstractHorse horse && horse.isTamed()) {
				return false;
			} else {
				return !(target instanceof TamableAnimal animal) || !animal.isTame();
			}
		} else {
			return false;
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putBoolean("IsBaby", this.isBaby());
		compound.putBoolean("CrownSummoned", this.crownSummoned);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.setBaby(compound.getBoolean("IsBaby"));
		this.crownSummoned = compound.getBoolean("CrownSummoned");
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return !this.isTame();
	}

	@Override
	public double getMyRidingOffset() {
		return -0.35D;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TFSounds.LOYAL_ZOMBIE_AMBIENT.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TFSounds.LOYAL_ZOMBIE_HURT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TFSounds.LOYAL_ZOMBIE_DEATH.get();
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		playSound(TFSounds.LOYAL_ZOMBIE_STEP.get(), 0.15F, 1.0F);
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	protected void dropExperience() {
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (DATA_BABY_ID.equals(key)) {
			this.refreshDimensions();
		}

		super.onSyncedDataUpdated(key);
	}

	@Override
	public boolean isBaby() {
		return this.getEntityData().get(DATA_BABY_ID);
	}

	@Override
	public void setBaby(boolean baby) {
		this.getEntityData().set(DATA_BABY_ID, baby);
		if (!this.level().isClientSide()) {
			AttributeInstance attributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
			if (attributeinstance != null) {
				attributeinstance.removeModifier(SPEED_MODIFIER_BABY_UUID);
				if (baby) {
					attributeinstance.addTransientModifier(SPEED_MODIFIER_BABY);
				}
			}
		}
	}

	// LivingEntity already halves the hitbox of babies through getScale(); 4.8 pins the baby eye height like the vanilla baby zombie
	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
		return this.isBaby() ? 0.93F : super.getStandingEyeHeight(pose, dimensions);
	}
}
