package com.skytekx.defixes.mixin;

import com.brandon3055.draconicevolution.blocks.CreativeOPSource;
import com.skytekx.defixes.block.OpaqueMachineProperties;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** Fix 8 (upstream 51ab0afb) for the Creative OP Capacitor, see {@link OpaqueMachineProperties}. */
@Mixin(value = CreativeOPSource.class, remap = false)
public abstract class CreativeOPSourceMixin {
    @ModifyArg(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/brandon3055/brandonscore/blocks/EntityBlockBCore;<init>(Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;)V"))
    private static BlockBehaviour.Properties skytekx_defixes$opaque(BlockBehaviour.Properties properties) {
        return OpaqueMachineProperties.replaceMachine(properties);
    }
}
