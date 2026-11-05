package com.enderio.conduits.common.integrations;

import com.enderio.api.conduit.IFacadeItem;
import com.enderio.api.integration.Integration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class ConduitSelfIntegration implements Integration {

    @Override
    public Optional<BlockState> getFacadeOf(ItemStack stack) {
        if (stack.getItem() instanceof IFacadeItem facadeItem) {
            // An unpainted conduit facade has no texture yet and is no facade.
            BlockState texture = facadeItem.getTexture(stack);
            //noinspection ConstantValue
            if (texture != null && !texture.isAir()) {
                return Optional.of(texture);
            }
        }
        return Optional.empty();
    }
}
