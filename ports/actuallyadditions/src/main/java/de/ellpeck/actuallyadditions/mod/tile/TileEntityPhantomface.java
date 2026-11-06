/*
 * This file ("TileEntityPhantomface.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;

import de.ellpeck.actuallyadditions.api.tile.IPhantomTile;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.blocks.BlockPhantom;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public abstract class TileEntityPhantomface extends TileEntityInventoryBase implements IPhantomTile {
    public static final int RANGE = 16;
    public BlockPos boundPosition;
    public BlockPhantom.Type type;
    public int range;
    private int rangeBefore;
    private BlockPos boundPosBefore;
    private Block boundBlockBefore;
    private int lastStrength;
    // SkyTekx: the capabilities this face hands out, per capability and side (index 6 for no side)
    private final Map<Capability<?>, LazyOptional<?>[]> forwardedCaps = new IdentityHashMap<>();
    // SkyTekx: the block entity whose handlers forwardedCaps holds. A target chunk that unloads and
    // loads again makes a new block entity at the same place, and the handlers of the old one must
    // not be handed out any more
    private BlockEntity forwardedTarget;

    public TileEntityPhantomface(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0);
    }


    // SkyTekx: a local change. The phantom blocks read their target through the loaded chunk itself.
    // Level#getBlockEntity and #getBlockState go through ServerChunkCache#getChunk with load, which
    // loads a missing chunk and, on every miss of its small cache, adds the one tick UNKNOWN ticket of
    // Forge at the FULL level. A face that read its target in every tick therefore kept the target
    // chunk loaded for good, and loaded it again when it was gone. getChunkNow neither loads a chunk
    // nor adds a ticket, so a target that nothing else keeps loaded can unload
    @Nullable
    public static LevelChunk loadedChunk(Level level, BlockPos pos) {
        return level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    @Nullable
    public static BlockEntity loadedBlockEntity(Level level, BlockPos pos) {
        LevelChunk chunk = loadedChunk(level, pos);
        return chunk == null
            ? null
            : chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
    }

    public static int upgradeRange(int defaultRange, Level world, BlockPos pos) {
        int newRange = defaultRange;
        for (int i = 0; i < 3; i++) {
            Block block = world.getBlockState(pos.above(1 + i)).getBlock();
            if (block == ActuallyBlocks.PHANTOM_BOOSTER.get()) {
                newRange = newRange * 2;
            } else {
                break;
            }
        }
        return newRange;
    }

    @Override
    public void writeSyncableNBT(CompoundTag compound, NBTType type) {
        super.writeSyncableNBT(compound, type);
        if (type != NBTType.SAVE_BLOCK) {
            compound.putInt("Range", this.range);
            if (this.boundPosition != null) {
                compound.putInt("xOfTileStored", this.boundPosition.getX());
                compound.putInt("yOfTileStored", this.boundPosition.getY());
                compound.putInt("zOfTileStored", this.boundPosition.getZ());
            }
        }
    }

    @Override
    public void readSyncableNBT(CompoundTag compound, NBTType type) {
        super.readSyncableNBT(compound, type);
        if (type != NBTType.SAVE_BLOCK) {
            int x = compound.getInt("xOfTileStored");
            int y = compound.getInt("yOfTileStored");
            int z = compound.getInt("zOfTileStored");
            this.range = compound.getInt("Range");
            if (!(x == 0 && y == 0 && z == 0)) {
                this.boundPosition = new BlockPos(x, y, z);
                this.setChanged();
            }
        }
    }

    @Override
    protected void serverTick() {
        super.serverTick();
        this.range = upgradeRange(RANGE, this.level, this.getBlockPos());

        if (!this.hasBoundPosition()) {
            this.boundPosition = null;
        }

        if (this.doesNeedUpdateSend()) {
            this.onUpdateSent();
        }

        // SkyTekx: pipes that cached a forwarded handler drop it once the target block entity is gone,
        // even while nobody asks the face. A removed target needs no look into its chunk, and an
        // unloaded chunk counts as gone and is not loaded for the look
        if (this.forwardedTarget != null && (this.forwardedTarget.isRemoved() || this.boundPosition == null || loadedBlockEntity(this.level, this.boundPosition) != this.forwardedTarget)) {
            this.invalidateForwardedCaps();
        }

        int strength = this.getComparatorStrength();
        if (this.lastStrength != strength) {
            this.lastStrength = strength;

            this.setChanged();
        }
    }

    @Override
    protected void clientTick() {
        super.clientTick();
        if (this.boundPosition != null) {
            this.renderParticles();
        }
    }

    // SkyTekx: the block at the target only counts while its chunk is loaded, see loadedChunk
    protected boolean doesNeedUpdateSend() {
        return this.boundPosition != this.boundPosBefore || this.boundBlockChanged() || this.rangeBefore != this.range;
    }

    private boolean boundBlockChanged() {
        if (this.boundPosition != null) {
            LevelChunk chunk = loadedChunk(this.level, this.boundPosition);
            return chunk != null && chunk.getBlockState(this.boundPosition).getBlock() != this.boundBlockBefore;
        }
        return false;
    }

    protected void onUpdateSent() {
        this.rangeBefore = this.range;
        this.boundPosBefore = this.boundPosition;
        LevelChunk boundChunk = this.boundPosition == null
            ? null
            : loadedChunk(this.level, this.boundPosition);
        this.boundBlockBefore = boundChunk == null
            ? null
            : boundChunk.getBlockState(this.boundPosition).getBlock();

        // SkyTekx: the target, its block or the range changed, so pipes drop what they cached
        this.invalidateForwardedCaps();

        if (this.boundPosition != null) {
            this.level.updateNeighborsAt(this.worldPosition, this.boundBlockBefore != null ? this.boundBlockBefore : this.getBlockState().getBlock());
        }

        this.sendUpdate();
        this.setChanged();
    }

    // SkyTekx: a local change. The face looked at the block entity of its target every tick, before
    // the range check, and Level#getBlockEntity loads a missing chunk on the server thread. A face out
    // of range, after its booster was taken away or when it was bound from far away, kept the target
    // chunk loaded and loaded it again each tick. The face now checks itself, then whether the chunk
    // is loaded, and only then the block entity, both through loadedChunk. A target in an unloaded
    // chunk stays bound and is checked once the chunk is back
    @Override
    public boolean hasBoundPosition() {
        if (this.boundPosition != null) {
            if (this.getBlockPos().equals(this.boundPosition)) {
                this.boundPosition = null;
                return false;
            }
            LevelChunk chunk = loadedChunk(this.level, this.boundPosition);
            if (chunk == null) {
                return true;
            }
            if (chunk.getBlockEntity(this.boundPosition, LevelChunk.EntityCreationType.IMMEDIATE) instanceof IPhantomTile) {
                this.boundPosition = null;
                return false;
            }
            return true;
        }
        return false;
    }

    // SkyTekx: common, since it names no client class and the common clientTick calls it. As a
    // client only method a dedicated server stripped it while clientTick kept the call
    public void renderParticles() {
        if (this.level.random.nextInt(2) == 0) {
            double d1 = this.boundPosition.getY() + this.level.random.nextFloat();
            int i1 = this.level.random.nextInt(2) * 2 - 1;
            int j1 = this.level.random.nextInt(2) * 2 - 1;
            double d4 = (this.level.random.nextFloat() - 0.5D) * 0.125D;
            double d2 = this.boundPosition.getZ() + 0.5D + 0.25D * j1;
            double d5 = this.level.random.nextFloat() * 1.0F * j1;
            double d0 = this.boundPosition.getX() + 0.5D + 0.25D * i1;
            double d3 = this.level.random.nextFloat() * 1.0F * i1;
            this.level.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }
    }

    // SkyTekx: the range first and the chunk second, so a target out of range or unloaded is never read
    @Override
    public boolean isBoundThingInRange() {
        return this.boundPosition != null && this.boundPosition.distSqr(this.getBlockPos()) <= this.range * this.range && loadedChunk(this.level, this.boundPosition) != null && this.hasBoundPosition();
    }

    @Override
    public BlockPos getBoundPosition() {
        return this.boundPosition;
    }

    @Override
    public void setBoundPosition(BlockPos pos) {
        this.boundPosition = pos;
    }

    //@Override
    public int getGuiID() {
        return -1;
    }

    @Override
    public int getRange() {
        return this.range;
    }

    protected abstract boolean isCapabilitySupported(Capability<?> capability);

    // SkyTekx: the Forge 47 form of the capability invalidation of b8e284d9 and ae6695a5. The base
    // returned the target's own LazyOptional, so a pipe that cached it kept using the old target
    // after the face was bound elsewhere or its range dropped. The face now hands out a LazyOptional
    // of its own for the target's handler, invalidates it when the target, the target's block or the
    // range changes and when the face goes away, and follows the target's invalidation. An
    // invalidated one is never handed out again
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (this.isCapabilitySupported(capability) && this.isBoundThingInRange()) {
            BlockEntity tile = loadedBlockEntity(this.level, this.getBoundPosition());
            // SkyTekx: handlers of another or a removed block entity are never handed out again
            if (tile != this.forwardedTarget || tile != null && tile.isRemoved()) {
                this.invalidateForwardedCaps();
                this.forwardedTarget = tile;
            }
            if (tile != null && !tile.isRemoved()) {
                return this.forwardCapability(capability, side, tile.getCapability(capability, side));
            }
        }

        return LazyOptional.empty();
    }

    private <T> LazyOptional<T> forwardCapability(Capability<T> capability, @Nullable Direction side, LazyOptional<T> target) {
        if (!target.isPresent()) {
            return LazyOptional.empty();
        }
        LazyOptional<?>[] bySide = this.forwardedCaps.computeIfAbsent(capability, c -> new LazyOptional<?>[7]);
        int index = side == null
            ? 6
            : side.ordinal();
        LazyOptional<?> known = bySide[index];
        if (known != null && known.isPresent()) {
            return known.cast();
        }
        T handler = target.orElseThrow(IllegalStateException::new);
        LazyOptional<T> forwarded = LazyOptional.of(() -> handler);
        target.addListener(invalid -> forwarded.invalidate());
        bySide[index] = forwarded;
        return forwarded;
    }

    protected void invalidateForwardedCaps() {
        this.forwardedTarget = null;
        if (!this.forwardedCaps.isEmpty()) {
            List<LazyOptional<?>> toInvalidate = new ArrayList<>();
            for (LazyOptional<?>[] bySide : this.forwardedCaps.values()) {
                for (LazyOptional<?> optional : bySide) {
                    if (optional != null) {
                        toInvalidate.add(optional);
                    }
                }
            }
            this.forwardedCaps.clear();
            toInvalidate.forEach(LazyOptional::invalidate);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.invalidateForwardedCaps();
    }

    // SkyTekx: the tick asks this every tick. The output of a chest or a furnace reads its block entity
    // through the level and so renews the chunk ticket, so the face only asks a target whose blocks
    // tick, which a ticket of the face's own reads never makes them do
    @Override
    public int getComparatorStrength() {
        if (this.isBoundThingInRange()) {
            BlockPos pos = this.getBoundPosition();
            LevelChunk chunk = loadedChunk(this.level, pos);
            BlockState state = chunk == null
                ? null
                : chunk.getBlockState(pos);

            if (state != null && state.hasAnalogOutputSignal() && this.level.shouldTickBlocksAt(pos)) {
                return state.getAnalogOutputSignal(this.level, pos);
            }
        }
        return 0;
    }
}
