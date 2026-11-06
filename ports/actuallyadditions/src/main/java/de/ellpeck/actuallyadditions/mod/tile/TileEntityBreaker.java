/*
 * This file ("TileEntityBreaker.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;

import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerBreaker;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA.IAcceptor;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import de.ellpeck.actuallyadditions.mod.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class TileEntityBreaker extends TileEntityInventoryBase implements MenuProvider {

    public boolean isPlacer;
    private int currentTime;

    public TileEntityBreaker(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state, slots);
    }

    public TileEntityBreaker(BlockPos pos, BlockState state) {
        super(ActuallyBlocks.BREAKER.getTileEntityType(), pos, state, 9);
        this.isPlacer = false;
    }

    @Override
    public void writeSyncableNBT(CompoundTag compound, NBTType type) {
        super.writeSyncableNBT(compound, type);
        if (type != NBTType.SAVE_BLOCK) {
            compound.putInt("CurrentTime", this.currentTime);
        }
    }

    @Override
    public void readSyncableNBT(CompoundTag compound, NBTType type) {
        super.readSyncableNBT(compound, type);
        if (type != NBTType.SAVE_BLOCK) {
            this.currentTime = compound.getInt("CurrentTime");
        }
    }

    public static <T extends BlockEntity> void clientTick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TileEntityBreaker tile) {
            tile.clientTick();
        }
    }

    public static <T extends BlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof TileEntityBreaker tile) {
            tile.serverTick();
        }
    }

    // SkyTekx: the work timer lives in the instance tick as in a117dd53, so that the Placer, whose
    // ticker calls TileEntityPlacer::serverTick, runs it too. On the base the Placer never placed
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
    }

    @Override
    public IAcceptor getAcceptor() {
        return (slot, stack, automation) -> !automation;
    }

    private void doWork() {
        Direction side = WorldUtil.getDirectionByPistonRotation(this.getBlockState());
        BlockPos breakCoords = this.worldPosition.relative(side);
        BlockState stateToBreak = this.level.getBlockState(breakCoords);
        Block blockToBreak = stateToBreak.getBlock();

        // SkyTekx: the break path of 217fc733 (snow, PR #1515) with the drop chance of e91c1569 and
        // 902e8064. The base asked the fake player with an empty hand, which can harvest nothing that
        // needs a tool. Fluids stay out as on 1.12, main dropped that check with IFluidBlock
        if (!this.isPlacer && blockToBreak != Blocks.AIR && !(blockToBreak instanceof LiquidBlock) && !(blockToBreak instanceof IFluidBlock) && stateToBreak.getDestroySpeed(this.level, breakCoords) >= 0.0F) {
            List<ItemStack> drops = Block.getDrops(stateToBreak, (ServerLevel) this.level, breakCoords, this.level.getBlockEntity(breakCoords));
            float chance = WorldUtil.fireFakeHarvestEventsForDropChance(this, this.level, breakCoords);

            if (chance > 0 && this.level.random.nextFloat() <= chance) {
                if (StackUtil.canAddAll(this.inv, drops, false)) {
                    this.level.levelEvent(2001, breakCoords, Block.getId(this.level.getBlockState(breakCoords)));
                    this.level.setBlockAndUpdate(breakCoords, Blocks.AIR.defaultBlockState());
                    StackUtil.addAll(this.inv, drops, false);
                    this.setChanged();
                }
            }
        } else if (this.isPlacer) {
            int slot = StackUtil.findFirstFilled(this.inv);
            if (slot == -1) {
                return;
            }
            this.inv.setStackInSlot(slot, WorldUtil.useItemAtSide(side, this.level, this.worldPosition, this.inv.getStackInSlot(slot)));
        }
    }

    @Override
    public boolean isRedstoneToggle() {
        return true;
    }

    @Override
    public void activateOnPulse() {
        this.doWork();
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return Component.translatable(isPlacer ? "container.actuallyadditions.placer" : "container.actuallyadditions.breaker");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new ContainerBreaker(windowId, playerInventory, this);
    }
}
