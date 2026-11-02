package baguchi.spiced_twilight.mixin;

import baguchi.spiced_twilight.api.HelmetCrabHideAccess;
import baguchi.spiced_twilight.entity.goal.HidingGoal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.monster.HelmetCrab;

/**
 * 1.20.1 backport: the hide state that upstream keeps in a synced NeoForge attachment lives here. The hide flag is a
 * synced entity data value (sent to every tracking client like the attachment sync), cooldown and hide chance stay on
 * the server and are saved under {@code SpicedTwilightHide}.
 */
@Mixin(HelmetCrab.class)
public abstract class HelmetCrabMixin extends Monster implements HelmetCrabHideAccess {
    @Unique
    private static final EntityDataAccessor<Boolean> SPICED_TWILIGHT_HIDE = SynchedEntityData.defineId(HelmetCrab.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private int spicedTwilight$hideCooldown;
    @Unique
    private float spicedTwilight$hideChance;

    protected HelmetCrabMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void spicedTwilight$defineHide(CallbackInfo ci) {
        this.getEntityData().define(SPICED_TWILIGHT_HIDE, false);
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    private void spicedTwilight$addHidingGoal(CallbackInfo ci) {
        this.goalSelector.addGoal(0, new HidingGoal((HelmetCrab) (Object) this));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void spicedTwilight$saveHide(CompoundTag tag, CallbackInfo ci) {
        CompoundTag hide = new CompoundTag();
        hide.putBoolean("hide", this.spicedTwilight$isHiding());
        hide.putFloat("hide_chance", this.spicedTwilight$hideChance);
        hide.putInt("hide_cooldown", this.spicedTwilight$hideCooldown);
        tag.put("SpicedTwilightHide", hide);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void spicedTwilight$loadHide(CompoundTag tag, CallbackInfo ci) {
        CompoundTag hide = tag.getCompound("SpicedTwilightHide");
        this.spicedTwilight$setHiding(hide.getBoolean("hide"));
        this.spicedTwilight$hideChance = hide.getFloat("hide_chance");
        this.spicedTwilight$hideCooldown = hide.getInt("hide_cooldown");
    }

    @Override
    public boolean spicedTwilight$isHiding() {
        return this.getEntityData().get(SPICED_TWILIGHT_HIDE);
    }

    @Override
    public void spicedTwilight$setHiding(boolean hide) {
        this.getEntityData().set(SPICED_TWILIGHT_HIDE, hide);
    }

    @Override
    public int spicedTwilight$getHideCooldown() {
        return this.spicedTwilight$hideCooldown;
    }

    @Override
    public void spicedTwilight$setHideCooldown(int cooldown) {
        this.spicedTwilight$hideCooldown = cooldown;
    }

    @Override
    public float spicedTwilight$getHideChance() {
        return this.spicedTwilight$hideChance;
    }

    @Override
    public void spicedTwilight$setHideChance(float hideChance) {
        this.spicedTwilight$hideChance = hideChance;
    }
}
