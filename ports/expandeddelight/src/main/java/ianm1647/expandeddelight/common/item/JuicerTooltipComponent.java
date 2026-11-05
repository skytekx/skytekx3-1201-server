package ianm1647.expandeddelight.common.item;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * The data of the juicer item's tooltip. Upstream nests it in the client class JuicerTooltip, here it lives
 * in common code, so that JuicerItem never names a client class.
 */
public record JuicerTooltipComponent(ItemStack drinkStack) implements TooltipComponent {
}
