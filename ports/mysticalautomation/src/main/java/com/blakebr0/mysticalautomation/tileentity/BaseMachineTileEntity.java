package com.blakebr0.mysticalautomation.tileentity;

import com.blakebr0.cucumber.energy.DynamicEnergyStorage;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.cucumber.inventory.SidedItemStackHandlerWrapper;
import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;
import com.blakebr0.cucumber.util.TriFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx port: the parts of the machines that NeoForge and Cucumber 8 provide upstream.
 * <ul>
 *     <li>Upstream registers the sided item handlers and the energy storage of every machine in
 *     {@code RegisterCapabilityHandler} (NeoForge block capabilities). On Forge 1.20.1 the block entity hands out
 *     its capabilities itself. The sides are upstream's: UP, DOWN, and every other side (or none) like NORTH.</li>
 *     <li>Cucumber 8's {@code BaseInventoryTileEntity} implements {@link Clearable} with a {@code clearAdditional}
 *     hook for the upgrade inventory (upstream a2a6c60). Cucumber 7's does not, so this class adds both.</li>
 * </ul>
 */
public abstract class BaseMachineTileEntity extends BaseInventoryTileEntity implements Clearable {
    private static final Direction[] SIDES = new Direction[] { Direction.UP, Direction.DOWN, Direction.NORTH };

    private LazyOptional<IItemHandlerModifiable>[] inventoryCapabilities;
    private LazyOptional<IEnergyStorage> energyCapability;

    public BaseMachineTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract DynamicEnergyStorage getEnergy();

    /**
     * Upstream's rule for automated insertion from the given side, which upstream hands to Cucumber 8's
     * {@code SidedInventoryWrapper}. Here it goes to Cucumber 7's {@link SidedItemStackHandlerWrapper}.
     */
    protected abstract boolean canInsertStackSided(int slot, ItemStack stack, @Nullable Direction direction);

    protected static void loadEnergy(DynamicEnergyStorage energy, CompoundTag tag) {
        if (tag.contains("Energy")) {
            energy.deserializeNBT(tag.get("Energy"));
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!this.isRemoved()) {
            if (cap == ForgeCapabilities.ENERGY) {
                return this.getEnergyCapability().cast();
            }

            if (cap == ForgeCapabilities.ITEM_HANDLER) {
                var capabilities = this.getInventoryCapabilities();

                if (side == Direction.UP) {
                    return capabilities[0].cast();
                } else if (side == Direction.DOWN) {
                    return capabilities[1].cast();
                } else {
                    return capabilities[2].cast();
                }
            }
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        if (this.inventoryCapabilities != null) {
            for (var capability : this.inventoryCapabilities) {
                capability.invalidate();
            }

            this.inventoryCapabilities = null;
        }

        if (this.energyCapability != null) {
            this.energyCapability.invalidate();
            this.energyCapability = null;
        }
    }

    @Override
    public void clearContent() {
        this.getInventory().getStacks().clear();
        this.clearAdditional();
    }

    protected void clearAdditional() { }

    private LazyOptional<IItemHandlerModifiable>[] getInventoryCapabilities() {
        if (this.inventoryCapabilities == null) {
            BaseItemStackHandler inventory = this.getInventory();
            TriFunction<Integer, ItemStack, Direction, Boolean> canInsert = this::canInsertStackSided;
            this.inventoryCapabilities = SidedItemStackHandlerWrapper.create(inventory, SIDES, canInsert, null);
        }

        return this.inventoryCapabilities;
    }

    private LazyOptional<IEnergyStorage> getEnergyCapability() {
        if (this.energyCapability == null) {
            this.energyCapability = LazyOptional.of(this::getEnergy);
        }

        return this.energyCapability;
    }
}
