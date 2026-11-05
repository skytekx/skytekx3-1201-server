package com.skytekx.defixes.mixin;

import com.brandon3055.draconicevolution.blocks.tileentity.TileGrinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Fix 2 (upstream 4683a54f): the Mob Grinder's killing blow, Float.MAX_VALUE / 5, becomes MAX_VALUE / 100,
 * so damage multipliers cannot push it to infinity.
 * <p>
 * The kill box half of upstream 3ae9baa2 ("Fixed grinder kill box") is not needed on 1.20.1: there the box is
 * {@code new AABB(pos1, pos2)}, which ends at the corner of pos2, and is exactly the configured size. The box
 * grew by one block only in the 1.20.4 port, which switched to {@code AABB.encapsulatingFullBlocks}.
 */
@Mixin(value = TileGrinder.class, remap = false)
public abstract class TileGrinderMixin {
    @ModifyConstant(method = "attackTarget", constant = @Constant(floatValue = Float.MAX_VALUE / 5F))
    private float skytekx_defixes$killDamage(float damage) {
        return Float.MAX_VALUE / 100F;
    }
}
