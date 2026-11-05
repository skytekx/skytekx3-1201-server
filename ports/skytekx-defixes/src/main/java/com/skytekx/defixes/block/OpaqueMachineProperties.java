package com.skytekx.defixes.block;

import com.brandon3055.draconicevolution.init.DEContent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Fix 8 (upstream 51ab0afb, "Fixed Dislocator Receptacle light transparency"): the Dislocator Receptacle and
 * the Creative OP Capacitor are full opaque cubes, but use the shared {@code DEContent.MACHINE} properties,
 * which carry {@code noOcclusion()}. Light then passes through them and the faces of neighbouring blocks
 * are not culled. Upstream gives both {@code MACHINE_OPAQUE}, the same properties without noOcclusion,
 * which this builds the same way.
 */
public final class OpaqueMachineProperties {
    private OpaqueMachineProperties() {
    }

    public static BlockBehaviour.Properties replaceMachine(BlockBehaviour.Properties properties) {
        if (properties != DEContent.MACHINE) {
            return properties;
        }
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).sound(SoundType.METAL).strength(3.0F, 8F).requiresCorrectToolForDrops();
    }
}
