package com.skytekx.defixes.mixin;

import com.brandon3055.draconicevolution.blocks.ChaosCrystal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChaosCrystal.class)
public abstract class ChaosCrystalMixin {
    /**
     * Fix 2 (upstream 4683a54f, "Insta-kill damage no longer sometimes bugs out"): placing a Chaos Crystal
     * outside creative hurts the player for Float.MAX_VALUE. Any damage multiplier (Apothic Attributes is in
     * the pack) turns that into infinity, which can leave the entity half dead. Upstream uses MAX_VALUE / 100.
     */
    @ModifyConstant(method = "setPlacedBy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)V",
            constant = @Constant(floatValue = Float.MAX_VALUE))
    private float skytekx_defixes$implosionDamage(float damage) {
        return Float.MAX_VALUE / 100F;
    }

    /**
     * Fix 3 (upstream 8ec180ab, "Fixed potential chaos crystal crash"): a crystal part whose parent position
     * is not set yet crashed in getShape with a NullPointerException. Upstream returns the unmoved shape then,
     * which a zero offset gives here.
     */
    @Redirect(method = "getShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;subtract(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos skytekx_defixes$nullSafeParentOffset(BlockPos parentPos, Vec3i pos) {
        return parentPos == null ? BlockPos.ZERO : parentPos.subtract(pos);
    }
}
