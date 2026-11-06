package de.ellpeck.actuallyadditions.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ResultContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// SkyTekx: the accessor of upstream 6f567725. The refmap maps inputSlots and resultSlots to their
// SRG names. The result slots are a local addition for AnvilMixin.clearItemTagResult
@Mixin(ItemCombinerMenu.class)
public interface ItemCombinerMenuAccessor {
    @Accessor
    Container getInputSlots();

    @Accessor
    ResultContainer getResultSlots();
}
