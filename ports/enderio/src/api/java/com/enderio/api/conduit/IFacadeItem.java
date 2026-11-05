package com.enderio.api.conduit;

import com.enderio.api.conduit.facade.FacadeType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IFacadeItem {

    /**
     * @return the block state the facade appears as, or air while the facade cannot be placed (an unpainted conduit facade)
     */
    BlockState getTexture(ItemStack stack);

    /**
     * @return the type of the facade, which decides whether it hides the conduits and resists explosions
     */
    default FacadeType getFacadeType(ItemStack stack) {
        return FacadeType.BASIC;
    }
}
