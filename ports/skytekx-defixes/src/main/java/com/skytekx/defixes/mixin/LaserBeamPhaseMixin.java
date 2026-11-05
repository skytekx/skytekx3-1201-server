package com.skytekx.defixes.mixin;

import com.brandon3055.draconicevolution.entity.guardian.control.LaserBeamPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Fix 2 (upstream 4683a54f): the Chaos Guardian's charged laser multiplies its power by
 * Float.MAX_VALUE / 5, upstream by MAX_VALUE / 100.
 */
@Mixin(value = LaserBeamPhase.class, remap = false)
public abstract class LaserBeamPhaseMixin {
    @ModifyConstant(method = "serverTick", constant = @Constant(floatValue = Float.MAX_VALUE / 5F))
    private float skytekx_defixes$beamPower(float factor) {
        return Float.MAX_VALUE / 100F;
    }
}
