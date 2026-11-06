package de.ellpeck.actuallyadditions.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ItemCombinerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// SkyTekx: the accessor of upstream 6f567725. The refmap maps inputSlots to its SRG name
@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {
    @Accessor
    Container getInputSlots();
}
