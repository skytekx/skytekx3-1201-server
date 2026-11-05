package com.enderio.conduits.common.conduit.facades;

import com.enderio.api.conduit.IFacadeItem;
import com.enderio.api.conduit.facade.FacadeType;
import com.enderio.api.integration.IntegrationManager;
import com.enderio.conduits.common.conduit.block.ConduitBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * The look and the type of a conduit facade, read from the facade item that a conduit bundle stores.
 * Ender IO 7.1 (78e627263) asks the ConduitFacadeProvider capability of the item. On 6.x an Ender IO facade item implements
 * {@link IFacadeItem}, and the facade items of other mods (AE2) come through {@link com.enderio.api.integration.Integration#getFacadeOf}
 * as basic facades.
 *
 * @param state the default state of the block the facade appears as
 * @param type  the type of the facade
 */
public record ConduitFacade(BlockState state, FacadeType type) {

    /**
     * @return the facade of this item, or empty if the item is no facade or cannot be placed yet (an unpainted conduit facade)
     */
    public static Optional<ConduitFacade> of(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        BlockState state;
        FacadeType type;
        if (stack.getItem() instanceof IFacadeItem facadeItem) {
            state = facadeItem.getTexture(stack);
            type = facadeItem.getFacadeType(stack);
        } else {
            Optional<BlockState> integrationState = IntegrationManager.findFirst(integration -> integration.getFacadeOf(stack));
            if (integrationState.isEmpty()) {
                return Optional.empty();
            }

            state = integrationState.get();
            type = FacadeType.BASIC;
        }

        //noinspection ConstantValue
        if (state == null || state.isAir() || state.getBlock() instanceof ConduitBlock) {
            // A conduit bundle painted onto a conduit bundle would ask itself for its light, sound and shape forever.
            return Optional.empty();
        }

        return Optional.of(new ConduitFacade(state.getBlock().defaultBlockState(), type));
    }
}
