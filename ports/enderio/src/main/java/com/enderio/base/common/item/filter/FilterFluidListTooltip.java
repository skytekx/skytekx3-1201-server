package com.enderio.base.common.item.filter;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * The tooltip image of a fluid filter that shows its fluids, like a bundle (Ender IO 8.x 824d70feb).
 * It is common code: the client draws it with ClientFilterFluidListTooltip, which only the client registers.
 */
public record FilterFluidListTooltip(List<FluidStack> fluids) implements TooltipComponent {
}
