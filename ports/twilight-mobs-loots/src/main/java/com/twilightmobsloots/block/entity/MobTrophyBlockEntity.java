package com.twilightmobsloots.block.entity;

import com.twilightmobsloots.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mob trophy block entity, modelled on Twilight Forest's {@code TrophyBlockEntity}.
 *
 * <p>Mob trophies do not animate, so {@link #getAnimationProgress} is always 0. Rendering is done by the
 * client {@code MobTrophyRenderer}; nothing is stored here.
 */
public class MobTrophyBlockEntity extends BlockEntity {

    public MobTrophyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOB_TROPHY.get(), pos, state);
    }

    /** Animation progress (always 0). */
    public float getAnimationProgress(float partialTick) {
        return 0.0F;
    }
}
