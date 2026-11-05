package com.mrh0.createaddition.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mrh0.createaddition.index.CABlocks;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// SkyTekx: local fix for the Electric Pump (upstream 6cbf853). A change to a pipe makes Create's
// FluidPropagator.propagateChangedPipe wipe the pressure of the whole pipe network and then tell the
// pumps it found to spread their pressure again. Create 6.0.8 only counts a block entity as a pump
// there when its block is AllBlocks.MECHANICAL_PUMP, so the Electric Pump kept its own pressure but
// its pipes stayed without any, and no fluid moved until its speed changed. A test server showed this
// after a flip of the pump and after the pipe at its face was replaced. The wrap lets the Electric
// Pump through as well.
// Create is no Bukkit class, so Arclight does not patch the target.
@Mixin(value = FluidPropagator.class, remap = false)
public abstract class FluidPropagatorMixin {

	@WrapOperation(
			method = "propagateChangedPipe",
			at = @At(
					value = "INVOKE",
					target = "Lcom/tterrag/registrate/util/entry/BlockEntry;has(Lnet/minecraft/world/level/block/state/BlockState;)Z"
			)
	)
	private static boolean createaddition$findElectricPump(BlockEntry<?> entry, BlockState state, Operation<Boolean> original) {
		return original.call(entry, state) || CABlocks.ELECTRIC_PUMP.has(state);
	}
}
