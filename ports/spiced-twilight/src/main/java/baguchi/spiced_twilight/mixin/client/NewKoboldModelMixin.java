package baguchi.spiced_twilight.mixin.client;

import baguchi.spiced_twilight.api.ISpicedArmor;
import baguchi.spiced_twilight.client.SpicedModels;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.model.entity.newmodels.NewKoboldModel;
import twilightforest.entity.monster.Kobold;

/**
 * Baby kobold head and armor anchors for CustomTFArmorLayer. Jappa-pack model (4.8 keeps it in the same class as the default model).
 */
@Mixin(NewKoboldModel.class)
public abstract class NewKoboldModelMixin extends HumanoidModel<Kobold> implements ISpicedArmor<Kobold> {
    public NewKoboldModelMixin(ModelPart root) {
        super(root);
    }

    // remap = false: the typed setupAnim overload keeps its name in the reobfuscated TF jar (only the generic
    // bridge becomes m_6973_), so this target is the same in dev and production
    @Inject(method = "setupAnim(Ltwilightforest/entity/monster/Kobold;FFFFF)V", at = @At("TAIL"), remap = false)
    private void spicedTwilight$babyHead(Kobold entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        SpicedModels.setupKoboldHead(this.head, entity);
    }

    @Override
    public void translateToHead(Kobold kobold, ModelPart modelPart, PoseStack poseStack) {
        SpicedModels.translateToHead(modelPart, poseStack);
    }

    @Override
    public void translateToChest(Kobold kobold, ModelPart modelPart, PoseStack poseStack) {
        SpicedModels.translateToChest(modelPart, poseStack);
    }

    @Override
    public void translateToChestPat(Kobold kobold, ModelPart modelPart, PoseStack poseStack) {
        SpicedModels.translateToChestPat(modelPart, poseStack);
    }

    @Override
    public void translateToLeg(Kobold kobold, ModelPart modelPart, PoseStack poseStack) {
        SpicedModels.translateToLeg(modelPart, poseStack);
    }

    @Override
    public Iterable<ModelPart> headPartArmors() {
        return ImmutableList.of(this.head);
    }

    @Override
    public Iterable<ModelPart> bodyPartArmors() {
        return ImmutableList.of(this.body);
    }

    @Override
    public Iterable<ModelPart> rightHandArmors() {
        return ImmutableList.of(this.rightArm);
    }

    @Override
    public Iterable<ModelPart> leftHandArmors() {
        return ImmutableList.of(this.leftArm);
    }

    @Override
    public Iterable<ModelPart> rightLegPartArmors() {
        return ImmutableList.of(this.rightLeg);
    }

    @Override
    public Iterable<ModelPart> leftLegPartArmors() {
        return ImmutableList.of(this.leftLeg);
    }
}
