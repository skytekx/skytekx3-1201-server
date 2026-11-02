package baguchi.spiced_twilight.mixin.client;

import baguchi.spiced_twilight.client.SpicedModels;
import net.minecraft.client.model.HierarchicalModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twilightforest.client.model.entity.newmodels.NewHelmetCrabModel;
import twilightforest.entity.monster.HelmetCrab;

/**
 * Hiding helmet crabs only show their helmet. Jappa-pack model (4.8 keeps it in the same class as the default model).
 */
@Mixin(NewHelmetCrabModel.class)
public abstract class NewHelmetCrabModelMixin extends HierarchicalModel<HelmetCrab> {

    // remap = false: the typed setupAnim overload keeps its name in the reobfuscated TF jar (only the generic
    // bridge becomes m_6973_), so this target is the same in dev and production
    @Inject(method = "setupAnim(Ltwilightforest/entity/monster/HelmetCrab;FFFFF)V", at = @At("HEAD"), remap = false)
    private void spicedTwilight$hideAnim(HelmetCrab entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        SpicedModels.setupHelmetCrab(this, entity);
    }
}
