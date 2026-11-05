package com.mrh0.createaddition.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

// SkyTekx: backport of upstream 6af9b4f (1.21.1 line). The invoker target is remapped through
// createaddition.refmap.json.
@Mixin(FireBlock.class)
public interface FireBlockInvoker {
    @Invoker("getStateForPlacement")
    BlockState invokeSpreadPlacement(BlockGetter level, BlockPos pos);
}
