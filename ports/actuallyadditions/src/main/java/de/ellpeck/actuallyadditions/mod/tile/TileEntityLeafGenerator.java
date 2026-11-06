/*
 * This file ("TileEntityLeafGenerator.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;

import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperServer;
import de.ellpeck.actuallyadditions.mod.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TileEntityLeafGenerator extends TileEntityBase implements ISharingEnergyProvider, IEnergyDisplay {

    // SkyTekx: how many refused leaves one cycle passes over before it gives up
    private static final int BREAK_TRIES = 8;

    public final CustomEnergyStorage storage = new CustomEnergyStorage(35000, 0, 450);
    public final LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.of(() -> this.storage);
    private int nextUseCounter;
    private int oldEnergy;

    public TileEntityLeafGenerator(BlockPos pos, BlockState state) {
        super(ActuallyBlocks.LEAF_GENERATOR.getTileEntityType(), pos, state);
    }

    @Override
    public void writeSyncableNBT(CompoundTag compound, NBTType type) {
        super.writeSyncableNBT(compound, type);
        this.storage.writeToNBT(compound);
    }

    @Override
    public void readSyncableNBT(CompoundTag compound, NBTType type) {
        super.readSyncableNBT(compound, type);
        this.storage.readFromNBT(compound);
    }

    public static <T extends BlockEntity> void clientTick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TileEntityLeafGenerator tile) {
            tile.clientTick();
        }
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TileEntityLeafGenerator tile) {
            tile.serverTick();

            if (!tile.isRedstonePowered) {

                // SkyTekx: upstream 5182ac8b. The base read the 1.12 config enums, which nothing loads,
                // so the energy per leaf was 0 and the generator never ate a leaf. Leaves count by
                // class or by the minecraft:leaves tag
                if (tile.nextUseCounter >= CommonConfig.Machines.LEAF_GENERATOR_COOLDOWN.get()) {
                    tile.nextUseCounter = 0;

                    int energyProduced = CommonConfig.Machines.LEAF_GENERATOR_CF_PER_LEAF.get();
                    if (energyProduced > 0 && energyProduced <= tile.storage.getMaxEnergyStored() - tile.storage.getEnergyStored()) {
                        int range = CommonConfig.Machines.LEAF_GENERATOR_AREA.get();
                        // SkyTekx: a local change. Only the leaves are copied out of the mutable cursor,
                        // where the base and main collected every position of the cube first
                        List<BlockPos> breakPositions = BlockPos.betweenClosedStream(
                                pos.offset(-range, -range, -range),
                                pos.offset(range, range, range)).filter(blockPos -> {
                            BlockState offsetState = level.getBlockState(blockPos);
                            return offsetState.getBlock() instanceof LeavesBlock || offsetState.is(BlockTags.LEAVES);
                        }).map(BlockPos::immutable).collect(Collectors.toList());

                        // SkyTekx: a local change. The leaf goes only when the break event of the fake
                        // player at the generator passes, so a protection mod or, on Arclight, a Bukkit
                        // claim plugin can keep it. A refused leaf makes the next shuffled one the
                        // candidate, up to BREAK_TRIES of them, and a cycle without a leaf makes no energy
                        Collections.shuffle(breakPositions);
                        BlockPos theCoord = null;
                        for (int i = 0; i < Math.min(BREAK_TRIES, breakPositions.size()); i++) {
                            if (WorldUtil.fireFakeBreakEvent(tile, level, breakPositions.get(i))) {
                                theCoord = breakPositions.get(i);
                                break;
                            }
                        }

                        if (theCoord != null) {
                            level.levelEvent(2001, theCoord, Block.getId(level.getBlockState(theCoord)));

                            level.setBlockAndUpdate(theCoord, Blocks.AIR.defaultBlockState());

                            tile.storage.receiveEnergyInternal(energyProduced, false);

                            PacketHelperServer.spawnLaserWithTimeServer(level, pos.getX(), pos.getY(), pos.getZ(), theCoord.getX(), theCoord.getY(), theCoord.getZ(), 0x3EA34A, 25, 0, 0.075F, 0.8F);
                        }
                    }
                } else {
                    tile.nextUseCounter++;
                }
            }

            if (tile.oldEnergy != tile.storage.getEnergyStored() && tile.sendUpdateWithInterval()) {
                tile.oldEnergy = tile.storage.getEnergyStored();
            }
        }
    }

    @Override
    public CustomEnergyStorage getEnergyStorage() {
        return this.storage;
    }

    @Override
    public boolean needsHoldShift() {
        return false;
    }

    @Override
    public int getEnergyToSplitShare() {
        return this.storage.getEnergyStored();
    }

    @Override
    public boolean doesShareEnergy() {
        return true;
    }

    @Override
    public Direction[] getEnergyShareSides() {
        return Direction.values();
    }

    @Override
    public boolean canShareTo(BlockEntity tile) {
        return true;
    }

    @Override
    public LazyOptional<IEnergyStorage> getEnergyStorage(Direction facing) {
        return this.lazyEnergy;
    }
}
