/*
 * This file ("TileEntityFluidCollector.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;

import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.fluids.AATank;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerFluidCollector;
import de.ellpeck.actuallyadditions.mod.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityFluidCollector extends TileEntityBase implements ISharingFluidHandler, MenuProvider {

    public boolean isPlacer;
    public final AATank tank = new AATank(8 * FluidType.BUCKET_VOLUME) {
        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!TileEntityFluidCollector.this.isPlacer) {
                return 0;
            }
            return super.fill(resource, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (TileEntityFluidCollector.this.isPlacer) {
                return FluidStack.EMPTY;
            }
            return super.drain(maxDrain, action);
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (TileEntityFluidCollector.this.isPlacer) {
                return FluidStack.EMPTY;
            }
            return super.drain(resource, action);
        }
    };
    public final LazyOptional<IFluidHandler> lazyTank = LazyOptional.of(() -> this.tank);
    private int lastTankAmount;
    private int currentTime;
    private int lastCompare;

    public TileEntityFluidCollector(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TileEntityFluidCollector(BlockPos pos, BlockState state) {
        this(ActuallyBlocks.FLUID_COLLECTOR.getTileEntityType(), pos, state);
        this.isPlacer = false;
    }

    @Override
    public boolean isRedstoneToggle() {
        return true;
    }

    @Override
    public void activateOnPulse() {
        this.doWork();
    }

    // TODO: [port] big old check on this entire functionality, I've not worked with fluids before
    private void doWork() {
        BlockState state = this.level.getBlockState(this.worldPosition);
        Direction sideToManipulate = WorldUtil.getDirectionByPistonRotation(state);
        BlockPos coordsBlock = this.worldPosition.relative(sideToManipulate);

        BlockState stateToBreak = this.level.getBlockState(coordsBlock);

        // SkyTekx: the collector empties a full water or lava cauldron, as in ea1a072c (#1440). Only
        // the collector, main also lets the Fluid Placer take from a cauldron
        if (!this.isPlacer && stateToBreak.getBlock() instanceof AbstractCauldronBlock cauldron) {
            if (cauldron.isFull(stateToBreak)) {
                if (stateToBreak.getBlock() == Blocks.WATER_CAULDRON) {
                    if (this.tank.fillInternal(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.SIMULATE) >= FluidType.BUCKET_VOLUME) {
                        this.tank.fillInternal(new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                        this.level.setBlockAndUpdate(coordsBlock, Blocks.CAULDRON.defaultBlockState());
                        this.level.playSound(null, coordsBlock, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return;
                    }
                } else if (stateToBreak.getBlock() == Blocks.LAVA_CAULDRON) {
                    if (this.tank.fillInternal(new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.SIMULATE) >= FluidType.BUCKET_VOLUME) {
                        this.tank.fillInternal(new FluidStack(Fluids.LAVA, FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                        this.level.setBlockAndUpdate(coordsBlock, Blocks.CAULDRON.defaultBlockState());
                        this.level.playSound(null, coordsBlock, SoundEvents.BUCKET_FILL_LAVA, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return;
                    }
                }
            }
        }

        Block blockToBreak = stateToBreak.getBlock();
        if (!this.isPlacer && FluidType.BUCKET_VOLUME <= this.tank.getCapacity() - this.tank.getFluidAmount()) {
            if (blockToBreak instanceof LiquidBlock && stateToBreak.getFluidState().isSource() && ((LiquidBlock) blockToBreak).getFluid() != null) {
                if (this.tank.fillInternal(new FluidStack(((LiquidBlock) blockToBreak).getFluid(), FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.SIMULATE) >= FluidType.BUCKET_VOLUME) {
                    this.tank.fillInternal(new FluidStack(((LiquidBlock) blockToBreak).getFluid(), FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                    this.level.setBlockAndUpdate(coordsBlock, Blocks.AIR.defaultBlockState());
                }
            }
        } else if (this.isPlacer && blockToBreak.defaultBlockState().canBeReplaced()) {
            if (this.tank.getFluidAmount() >= FluidType.BUCKET_VOLUME) {
                FluidStack stack = this.tank.getFluid();
                Block fluid = stack.getFluid().defaultFluidState().createLegacyBlock().getBlock();
                // SkyTekx: a fluid without a block, such as the potions and tea of Create, stays in the
                // tank. The check for null was always true, so the placer set air and voided a bucket
                if (!fluid.defaultBlockState().isAir()) {
                    BlockPos offsetPos = this.worldPosition.relative(sideToManipulate);
                    // SkyTekx: no bucket goes into a source block that is already there, as in f524eccc.
                    // The base drained a bucket into a water source in front of it
                    boolean placeable = !stateToBreak.getFluidState().isSource() && blockToBreak.defaultBlockState().canBeReplaced();
                    if (placeable) {
                        this.tank.drainInternal(FluidType.BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
                        // TODO: [port] validate this check is still valid.
                        if (this.level.dimensionType().ultraWarm() && stack.getFluid().is(FluidTags.WATER)) {
                            this.level.playSound(null, offsetPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.8F);

                            if (this.level instanceof ServerLevel) {
                                for (int l = 0; l < 8; ++l) {
                                    ((ServerLevel) this.level).sendParticles(ParticleTypes.SMOKE, offsetPos.getX() + Math.random(), offsetPos.getY() + Math.random(), offsetPos.getZ() + Math.random(), 1, 0.0D, 0.0D, 0.0D, 0);
                                }
                            }
                        } else {
                            this.level.setBlock(offsetPos, fluid.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int getComparatorStrength() {
        float calc = (float) this.tank.getFluidAmount() / (float) this.tank.getCapacity() * 15F;
        return (int) calc;
    }

    @Override
    public LazyOptional<IFluidHandler> getFluidHandler(Direction facing) {
        return this.lazyTank;
    }

    @Override
    public void writeSyncableNBT(CompoundTag compound, NBTType type) {
        super.writeSyncableNBT(compound, type);
        if (type != NBTType.SAVE_BLOCK) {
            compound.putInt("CurrentTime", this.currentTime);
        }
        this.tank.writeToNBT(compound);
    }

    @Override
    public void readSyncableNBT(CompoundTag compound, NBTType type) {
        super.readSyncableNBT(compound, type);
        if (type != NBTType.SAVE_BLOCK) {
            this.currentTime = compound.getInt("CurrentTime");
        }
        this.tank.readFromNBT(compound);
    }

    public static <T extends BlockEntity> void clientTick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TileEntityFluidCollector tile) {
            tile.clientTick();
        }
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TileEntityFluidCollector tile) {
            tile.serverTick();
        }
    }

    // SkyTekx: the work timer lives in the instance tick as in e4fdc6d7, so that the Fluid Placer,
    // whose ticker calls TileEntityFluidPlacer::serverTick, runs it too. On the base it never placed
    @Override
    protected void serverTick() {
        super.serverTick();
        if (!this.isRedstonePowered && !this.isPulseMode) {
            if (this.currentTime > 0) {
                this.currentTime--;
                if (this.currentTime <= 0) {
                    this.doWork();
                }
            } else {
                this.currentTime = 15;
            }
        }

        if (this.lastCompare != this.getComparatorStrength()) {
            this.lastCompare = this.getComparatorStrength();

            this.setChanged();
        }

        if (this.lastTankAmount != this.tank.getFluidAmount() && this.sendUpdateWithInterval()) {
            this.lastTankAmount = this.tank.getFluidAmount();
        }
    }

    @Override
    public int getMaxFluidAmountToSplitShare() {
        return this.tank.getFluidAmount();
    }

    @Override
    public boolean doesShareFluid() {
        return !this.isPlacer;
    }

    @Override
    public Direction[] getFluidShareSides() {
        return Direction.values();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(isPlacer ? "container.actuallyadditions.fluidPlacer" : "container.actuallyadditions.fluidCollector");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new ContainerFluidCollector(windowId, playerInventory, this);
    }
}
