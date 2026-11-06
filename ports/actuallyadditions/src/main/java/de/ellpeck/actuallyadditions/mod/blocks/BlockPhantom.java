/*
 * This file ("BlockPhantom.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.blocks;

import de.ellpeck.actuallyadditions.mod.blocks.base.BlockContainerBase;
import de.ellpeck.actuallyadditions.mod.items.ItemPhantomConnector;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomBreaker;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomEnergyface;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomItemface;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomLiquiface;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomPlacer;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPhantomRedstoneface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;


public class BlockPhantom extends BlockContainerBase implements IHudDisplay {

    public final Type type;

    public BlockPhantom(Type type) {
        super(ActuallyBlocks.defaultPickProps(4.5F, 10.0F));
        this.type = type;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return this.type == Type.REDSTONEFACE;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        if (this.type == Type.REDSTONEFACE) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileEntityPhantomRedstoneface) {
                return ((TileEntityPhantomRedstoneface) tile).providesWeak[side.ordinal()];
            }
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        if (this.type == Type.REDSTONEFACE) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileEntityPhantomRedstoneface) {
                return ((TileEntityPhantomRedstoneface) tile).providesStrong[side.ordinal()];
            }
        }
        return 0;
    }

    @Override
    public boolean shouldDropInventory(Level world, BlockPos pos) {
        return this.type == Type.PLACER || this.type == Type.BREAKER;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        switch (this.type) {
            case PLACER:
                return new TileEntityPhantomPlacer(pos, state);
            case BREAKER:
                return new TileEntityPhantomBreaker(pos, state);
            case LIQUIFACE:
                return new TileEntityPhantomLiquiface(pos, state);
            case ENERGYFACE:
                return new TileEntityPhantomEnergyface(pos, state);
            case REDSTONEFACE:
                return new TileEntityPhantomRedstoneface(pos, state);
            default:
                return new TileEntityPhantomItemface(pos, state);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> entityType) {
        // SkyTekx: the Phantom Breaker ticks with the placer's ticker, as in ccfb8d16. Its own ticker
        // never ran the work timer, so on the base it broke nothing
        switch (this.type) {
            case PLACER:
            case BREAKER:
                return level.isClientSide? TileEntityPhantomPlacer::clientTick : TileEntityPhantomPlacer::serverTick;
            case LIQUIFACE:
                return level.isClientSide? TileEntityPhantomLiquiface::clientTick : TileEntityPhantomLiquiface::serverTick;
            case ENERGYFACE:
                return level.isClientSide? TileEntityPhantomEnergyface::clientTick : TileEntityPhantomEnergyface::serverTick;
            case REDSTONEFACE:
                return level.isClientSide? TileEntityPhantomRedstoneface::clientTick : TileEntityPhantomRedstoneface::serverTick;
            default:
                return level.isClientSide? TileEntityPhantomItemface::clientTick : TileEntityPhantomItemface::serverTick;
        }
    }

    // SkyTekx: the Phantom Placer and Breaker open their screens, as in ccfb8d16, and a redstone toggle
    // ends the click (5ce4f9a7). Unlike main the block passes the click on when the player holds a
    // Phantom Connector or when nothing opens, so the connector binds without sneaking and blocks
    // can still be placed against a phantomface
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (this.tryToggleRedstone(world, pos, player)) {
            return InteractionResult.SUCCESS;
        }
        if (player.getItemInHand(handIn).getItem() instanceof ItemPhantomConnector) {
            return InteractionResult.PASS;
        }
        if (world.getBlockEntity(pos) instanceof MenuProvider) {
            return this.openGui(world, player, pos, TileEntityPhantomPlacer.class);
        }
        return InteractionResult.PASS;
    }

    public enum Type {
        ITEMFACE,
        PLACER,
        BREAKER,
        LIQUIFACE,
        ENERGYFACE,
        REDSTONEFACE
    }
}
