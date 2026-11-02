package baguchi.spiced_twilight.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.monster.Kobold;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFItems;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Baby kobolds and biome specific kobold armor.
 * <p>
 * 1.20.1 backport: upstream mirrors the baby flag into a synced NeoForge attachment. Here it is a synced entity data
 * value, so clients get it on tracking and refresh the hitbox through {@link #onSyncedDataUpdated}. 1.20.1 still
 * halves a baby's size through {@code LivingEntity#getScale}, so only the eye height needs an override.
 */
@Mixin(Kobold.class)
public abstract class KoboldMixin extends Monster {
    @Unique
    private static final EntityDataAccessor<Boolean> SPICED_TWILIGHT_BABY = SynchedEntityData.defineId(Kobold.class, EntityDataSerializers.BOOLEAN);
    // 1.21 keys attribute modifiers by id; upstream uses minecraft:baby
    @Unique
    private static final UUID SPICED_TWILIGHT_BABY_DAMAGE_ID = UUID.fromString("5b0f38a8-9ec5-4f53-b8b5-1f7d4a3e6c21");
    @Unique
    private static final AttributeModifier SPICED_TWILIGHT_BABY_DAMAGE = new AttributeModifier(
            SPICED_TWILIGHT_BABY_DAMAGE_ID, "Baby kobold damage", -0.25, AttributeModifier.Operation.MULTIPLY_BASE
    );
    // not in the fork yet (4.8 worldgen); a key matches nothing until the biome exists
    @Unique
    private static final ResourceKey<Biome> SPICED_TWILIGHT_HIGHLANDS_UNDERGROUND = ResourceKey.create(Registries.BIOME, new ResourceLocation("twilightforest", "highlands_underground"));

    protected KoboldMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void spicedTwilight$defineBaby(CallbackInfo ci) {
        this.getEntityData().define(SPICED_TWILIGHT_BABY, false);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void spicedTwilight$saveBaby(CompoundTag tag, CallbackInfo ci) {
        if (this.isBaby()) {
            tag.putBoolean("IsBaby", true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void spicedTwilight$loadBaby(CompoundTag tag, CallbackInfo ci) {
        if (tag.getBoolean("IsBaby")) {
            this.setBaby(true);
        }
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(baby);
        this.getEntityData().set(SPICED_TWILIGHT_BABY, baby);
        if (this.level() != null && !this.level().isClientSide) {
            AttributeInstance attributeinstance = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attributeinstance != null) {
                attributeinstance.removeModifier(SPICED_TWILIGHT_BABY_DAMAGE_ID);
                if (baby) {
                    attributeinstance.addTransientModifier(SPICED_TWILIGHT_BABY_DAMAGE);
                }
            }
        }
    }

    @Override
    public boolean isBaby() {
        return this.getEntityData().get(SPICED_TWILIGHT_BABY);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (SPICED_TWILIGHT_BABY.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    // upstream: EntityDimensions#withEyeHeight(0.45F) on the halved kobold dimensions
    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return this.isBaby() ? 0.45F : super.getStandingEyeHeight(pose, dimensions);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        if (!this.isBaby()) {
            Holder<Biome> biome = this.level().getBiome(this.blockPosition());
            if (biome.is(TFBiomes.SNOWY_FOREST) || biome.is(TFBiomes.GLACIER)) {
                if (this.random.nextFloat() < 0.5F) {
                    this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TFItems.ARCTIC_HELMET.get()));
                }
                if (this.random.nextFloat() < 0.2F) {
                    this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TFItems.ARCTIC_CHESTPLATE.get()));
                }
                if (this.random.nextFloat() < 0.25F) {
                    this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TFItems.ARCTIC_LEGGINGS.get()));
                    this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TFItems.ARCTIC_BOOTS.get()));
                }

            } else if (biome.is(TFBiomes.UNDERGROUND)) {
                if (this.random.nextFloat() < 0.1) {
                    this.setItemSlot(EquipmentSlot.HEAD, Items.IRON_HELMET.getDefaultInstance());
                }
            } else if (biome.is(SPICED_TWILIGHT_HIGHLANDS_UNDERGROUND)) {
                if (this.random.nextFloat() < 0.25) {
                    this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TFItems.IRONWOOD_HELMET.get()));
                }
            } else {
                if (this.random.nextFloat() < 0.025) {
                    if (this.random.nextBoolean()) {
                        this.setItemSlot(EquipmentSlot.FEET, Items.LEATHER_BOOTS.getDefaultInstance());
                    } else {
                        this.setItemSlot(EquipmentSlot.HEAD, Items.LEATHER_HELMET.getDefaultInstance());
                    }
                }
                if (this.random.nextFloat() < 0.025) {
                    if (this.random.nextBoolean()) {
                        // upstream puts the boots into the HEAD slot (typo), where they can't render or be worn
                        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TFItems.STEELEAF_BOOTS.get()));
                    } else {
                        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TFItems.STEELEAF_HELMET.get()));
                    }
                }
            }
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag dataTag) {
        RandomSource randomsource = level.getRandom();
        if (this.random.nextFloat() < 0.05F) {
            this.setBaby(true);
        }
        this.populateDefaultEquipmentSlots(randomsource, difficulty);
        this.populateDefaultEquipmentEnchantments(randomsource, difficulty);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
    }
}
