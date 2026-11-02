package baguchi.twilight_crossbow.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.entity.monster.SnowGuardian;

/**
 * Crossbow hold/charge arm poses for Snow Guardians (their renderer uses Twilight Forest's NoopModel, a HumanoidModel).
 */
@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> {

    @Shadow
    public HumanoidModel.ArmPose leftArmPose;

    @Shadow
    public HumanoidModel.ArmPose rightArmPose;

    @Inject(method = "prepareMobModel(Lnet/minecraft/world/entity/LivingEntity;FFF)V", at = @At("HEAD"))
    private void twilight_crossbow$prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick, CallbackInfo ci) {
        ItemStack itemstack = entity.getItemInHand(InteractionHand.MAIN_HAND);
        if (entity instanceof SnowGuardian snowGuardian) {
            this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            this.leftArmPose = HumanoidModel.ArmPose.EMPTY;

            if (itemstack.getItem() instanceof CrossbowItem) {
                if (snowGuardian.isUsingItem()) {
                    if (entity.getMainArm() == HumanoidArm.RIGHT) {
                        this.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                    } else {
                        this.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                    }
                } else {
                    if (entity.getMainArm() == HumanoidArm.RIGHT) {
                        this.rightArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                    } else {
                        this.leftArmPose = HumanoidModel.ArmPose.CROSSBOW_HOLD;
                    }
                }
            }
        }
    }
}
