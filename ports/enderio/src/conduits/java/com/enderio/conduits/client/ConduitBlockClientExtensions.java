package com.enderio.conduits.client;

import com.enderio.conduits.client.model.conduit.facades.ClientFacadeVisibility;
import com.enderio.conduits.common.conduit.block.ConduitBlockEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

import java.util.Optional;

/**
 * Client extensions of the conduit bundle block. Vanilla hit particles of a faced bundle would take the facade's sprite from the bundle
 * model but the bundle's block colour of tint index 0, a conduit channel colour, so a stone facade would crack in dark green. While a
 * facade is visible, its hit particles are spawned like those of ParticleEngine.crack on a full block, with the facade's own state, so
 * they take its sprite and its colour.
 */
public final class ConduitBlockClientExtensions implements IClientBlockExtensions {

    public static final ConduitBlockClientExtensions INSTANCE = new ConduitBlockClientExtensions();

    private ConduitBlockClientExtensions() {
    }

    @Override
    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
        if (!(target instanceof BlockHitResult hit) || !(level instanceof ClientLevel clientLevel) || !ClientFacadeVisibility.areFacadesVisible()) {
            return false;
        }

        BlockPos pos = hit.getBlockPos();
        Optional<BlockState> facade = level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit ? conduit.getBundle().getFacadeState()
            : Optional.empty();
        if (facade.isEmpty() || facade.get().getRenderShape() == RenderShape.INVISIBLE) {
            return false;
        }

        RandomSource random = level.getRandom();
        double x = pos.getX() + random.nextDouble() * 0.8 + 0.1;
        double y = pos.getY() + random.nextDouble() * 0.8 + 0.1;
        double z = pos.getZ() + random.nextDouble() * 0.8 + 0.1;
        switch (hit.getDirection()) {
            case DOWN -> y = pos.getY() - 0.1;
            case UP -> y = pos.getY() + 1.1;
            case NORTH -> z = pos.getZ() - 0.1;
            case SOUTH -> z = pos.getZ() + 1.1;
            case WEST -> x = pos.getX() - 0.1;
            case EAST -> x = pos.getX() + 1.1;
        }

        manager.add(new TerrainParticle(clientLevel, x, y, z, 0, 0, 0, facade.get(), pos).updateSprite(facade.get(), pos).setPower(0.2F).scale(0.6F));
        return true;
    }
}
