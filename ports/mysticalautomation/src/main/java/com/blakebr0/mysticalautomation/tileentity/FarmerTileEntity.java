package com.blakebr0.mysticalautomation.tileentity;

import com.blakebr0.cucumber.energy.DynamicEnergyStorage;
import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.util.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.util.MachineUpgradeTier;
import com.blakebr0.mysticalautomation.block.FarmerBlock;
import com.blakebr0.mysticalautomation.container.FarmerContainer;
import com.blakebr0.mysticalautomation.crafting.recipe.FarmerRecipe;
import com.blakebr0.mysticalautomation.init.ModRecipeTypes;
import com.blakebr0.mysticalautomation.init.ModTileEntities;
import com.blakebr0.mysticalautomation.util.CachedRecipe;
import com.blakebr0.mysticalautomation.util.ContainerDataBuilder;
import com.blakebr0.mysticalautomation.util.MachineUpgrades;
import com.blakebr0.mysticalautomation.util.RecipeIngredientCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class FarmerTileEntity extends BaseMachineTileEntity implements MenuProvider, IUpgradeableMachine {
    private static final int[] INPUT_SLOTS = IntStream.rangeClosed(0, 2).toArray();
    private static final int FUEL_SLOT = 3;
    private static final int[] OUTPUT_SLOTS = IntStream.rangeClosed(4, 12).toArray();

    public static final int FUEL_TICK_MULTIPLIER = 20;
    public static final int OPERATION_TIME = 800;
    public static final int FUEL_USAGE = 20;
    public static final int FUEL_CAPACITY = 80000;

    private final BaseItemStackHandler inventory;
    private final UpgradeItemStackHandler upgradeInventory;
    private final DynamicEnergyStorage energy;
    private final CachedRecipe<Container, FarmerRecipe> recipe;

    private final ContainerData dataAccess;

    private MachineUpgradeTier tier;
    private int progress;
    private int stages;
    private int stageProgress;
    private int fuelLeft;
    private int fuelItemValue;
    private boolean isRunning;

    public FarmerTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.FARMER.get(), pos, state);
        this.inventory = createInventoryHandler(this::setChanged);
        this.upgradeInventory = new UpgradeItemStackHandler();
        this.energy = new DynamicEnergyStorage(FUEL_CAPACITY, this::setChangedFast);
        this.recipe = new CachedRecipe<>(ModRecipeTypes.FARMER.get());

        this.dataAccess = ContainerDataBuilder.builder()
                .sync(this.energy::getEnergyStored, this.energy::setEnergyStored)
                .sync(this.energy::getMaxEnergyStored, this.energy::setMaxEnergyStorage)
                .sync(() -> this.fuelLeft, value -> this.fuelLeft = value)
                .sync(() -> this.fuelItemValue, value -> this.fuelItemValue = value)
                .sync(() -> this.progress, value -> this.progress = value)
                .sync(this::getOperationTime, value -> {})
                .sync(() -> this.stages, value -> this.stages = value)
                .sync(() -> this.stageProgress, value -> this.stageProgress = value)
                .sync(this::getStageOperationTime, value -> {})
                .build();
    }

    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mysticalautomation.farmer");
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new FarmerContainer(i, inventory, this.inventory, this.upgradeInventory, this.dataAccess, this.getBlockPos());
    }

    @Override
    public UpgradeItemStackHandler getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.progress = tag.getInt("Progress");
        this.stages = tag.getInt("Stages");
        this.stageProgress = tag.getInt("StageProgress");
        this.fuelLeft = tag.getInt("FuelLeft");
        this.fuelItemValue = tag.getInt("FuelItemValue");
        loadEnergy(this.energy, tag);
        this.upgradeInventory.deserializeNBT(tag.getCompound("UpgradeInventory"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("Progress", this.progress);
        tag.putInt("Stages", this.stages);
        tag.putInt("StageProgress", this.stageProgress);
        tag.putInt("FuelLeft", this.fuelLeft);
        tag.putInt("FuelItemValue", this.fuelItemValue);
        tag.putInt("Energy", this.energy.getEnergyStored());
        tag.put("UpgradeInventory", this.upgradeInventory.serializeNBT());
    }

    @Override
    protected void clearAdditional() {
        this.upgradeInventory.getStacks().clear();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FarmerTileEntity tile) {
        if (tile.energy.getEnergyStored() < tile.energy.getMaxEnergyStored()) {
            var fuel = tile.inventory.getStackInSlot(FUEL_SLOT);

            if (tile.fuelLeft <= 0 && !fuel.isEmpty()) {
                tile.fuelItemValue = ForgeHooks.getBurnTime(fuel, null);

                if (tile.fuelItemValue > 0) {
                    tile.fuelLeft = tile.fuelItemValue *= FUEL_TICK_MULTIPLIER;
                    tile.inventory.setStackInSlot(FUEL_SLOT, StackHelper.shrink(fuel, 1, true));

                    tile.setChangedFast();
                }
            }

            if (tile.fuelLeft > 0) {
                var fuelPerTick = Math.min(Math.min(tile.fuelLeft, tile.getFuelUsage() * 2), tile.energy.getMaxEnergyStored() - tile.energy.getEnergyStored());

                tile.fuelLeft -= tile.energy.receiveEnergy(fuelPerTick, false);

                if (tile.fuelLeft <= 0)
                    tile.fuelItemValue = 0;

                tile.setChangedFast();
            }
        }

        var tier = tile.getMachineTier();

        if (tier != tile.tier) {
            tile.tier = tier;

            if (tier == null) {
                tile.energy.resetMaxEnergyStorage();
            } else {
                tile.energy.setMaxEnergyStorage(MachineUpgrades.getFuelCapacity(tier, FUEL_CAPACITY));
            }

            tile.setChangedFast();
        }

        var wasRunning = tile.isRunning;
        tile.isRunning = false;

        if (tile.energy.getEnergyStored() >= tile.getFuelUsage()) {
            var recipe = tile.getActiveRecipe();
            // SkyTekx: while the outputs cannot take every result of the recipe the Farmer waits with its progress
            // kept, instead of rolling results that it would drop
            var waiting = recipe != null && !tile.canFitResults(recipe);
            if (recipe != null && !waiting) {
                tile.isRunning = true;
                tile.stages = recipe.getStages();
                tile.energy.extractEnergy(tile.getFuelUsage(), false);

                if (tile.stageProgress >= tile.getStageOperationTime()) {
                    tile.progress++;

                    if (tile.progress >= tile.getOperationTime()) {
                        var results = recipe.getRolledResults();
                        var outputs = tile.getOutputs();

                        for (var result : results) {
                            addToOutputs(outputs, result, tile.inventory.getSlotLimit(OUTPUT_SLOTS[0]));
                        }

                        tile.setOutputs(outputs);

                        tile.reset();
                    }
                } else {
                    tile.stageProgress++;
                }

                tile.setChangedFast();
            } else if (recipe == null) {
                if (tile.progress > 0 || tile.stageProgress > 0 || tile.stages > 0) {
                    tile.reset();
                    tile.setChangedFast();
                }
            }
        }

        if (wasRunning != tile.isRunning) {
            level.setBlock(pos, state.setValue(FarmerBlock.RUNNING, tile.isRunning), 3);

            tile.setChangedFast();
        }
    }

    public static BaseItemStackHandler createInventoryHandler() {
        return createInventoryHandler(null);
    }

    public static BaseItemStackHandler createInventoryHandler(@Nullable Runnable onContentsChanged) {
        return BaseItemStackHandler.create(16, onContentsChanged, handler -> {
            handler.setCanInsert((slot, stack) -> switch (slot) {
                case 0 -> RecipeIngredientCache.INSTANCE.isValidInput(RecipeIngredientCache.Key.FARMER_SEEDS, stack);
                case 1 -> RecipeIngredientCache.INSTANCE.isValidInput(RecipeIngredientCache.Key.FARMER_SOIL, stack);
                case 2 -> RecipeIngredientCache.INSTANCE.isValidInput(RecipeIngredientCache.Key.FARMER_CRUX, stack);
                default -> true;
            });

            for (var slot : INPUT_SLOTS) {
                handler.addSlotLimit(slot, 1);
            }

            handler.setOutputSlots(OUTPUT_SLOTS);
            handler.setCanExtract(slot ->
                    ArrayUtils.contains(OUTPUT_SLOTS, slot) || (slot == FUEL_SLOT && !FurnaceBlockEntity.isFuel(handler.getStackInSlot(slot)))
            );
        });
    }

    @Override
    public DynamicEnergyStorage getEnergy() {
        return this.energy;
    }

    private void reset() {
        this.progress = 0;
        this.stages = 0;
        this.stageProgress = 0;
    }

    @Nullable
    private FarmerRecipe getActiveRecipe() {
        return this.recipe.checkAndGet(this.toCraftingInput(), this.level);
    }

    private int getOperationTime() {
        return this.tier == null ? OPERATION_TIME : MachineUpgrades.getOperationTime(this.tier, OPERATION_TIME);
    }

    private int getFuelUsage() {
        return this.tier == null ? FUEL_USAGE : MachineUpgrades.getFuelUsage(this.tier, FUEL_USAGE);
    }

    private int getStageOperationTime() {
        return this.tier == null ? OPERATION_TIME * this.stages : MachineUpgrades.getOperationTime(this.tier, OPERATION_TIME) * this.stages;
    }

    // SkyTekx port: the three input slots as a recipe Container, the 1.20.1 form of upstream's CraftingInput
    private Container toCraftingInput() {
        return this.inventory.toRecipeInventory(INPUT_SLOTS[0], INPUT_SLOTS.length);
    }

    // SkyTekx: upstream merged part of a result into a matching output stack and then put a copy of the whole result
    // into the next empty slot, so a result of 3 melon slices against a slot of 62 gave 2 + 3, and it dropped what
    // found no room. The outputs now take each result once, in slot order as upstream, and canFitResults keeps the
    // Farmer from finishing an operation whose results could not all fit
    private ItemStack[] getOutputs() {
        var outputs = new ItemStack[OUTPUT_SLOTS.length];

        for (int i = 0; i < OUTPUT_SLOTS.length; i++) {
            outputs[i] = this.inventory.getStackInSlot(OUTPUT_SLOTS[i]).copy();
        }

        return outputs;
    }

    private void setOutputs(ItemStack[] outputs) {
        for (int i = 0; i < OUTPUT_SLOTS.length; i++) {
            if (!ItemStack.matches(outputs[i], this.inventory.getStackInSlot(OUTPUT_SLOTS[i]))) {
                this.inventory.setStackInSlot(OUTPUT_SLOTS[i], outputs[i]);
            }
        }
    }

    private boolean canFitResults(FarmerRecipe recipe) {
        var outputs = this.getOutputs();
        var limit = this.inventory.getSlotLimit(OUTPUT_SLOTS[0]);

        // every result at its full count, the most one operation can roll
        for (var result : recipe.getResults()) {
            if (addToOutputs(outputs, result.stack(), limit) > 0)
                return false;
        }

        return true;
    }

    // adds the stack to the outputs and gives back the count that found no room
    private static int addToOutputs(ItemStack[] outputs, ItemStack stack, int slotLimit) {
        var remaining = stack.getCount();
        var limit = Math.min(stack.getMaxStackSize(), slotLimit);

        for (int i = 0; i < outputs.length && remaining > 0; i++) {
            var stackInSlot = outputs[i];

            if (stackInSlot.isEmpty()) {
                var insertSize = Math.min(remaining, limit);

                outputs[i] = stack.copyWithCount(insertSize);
                remaining -= insertSize;
            } else if (StackHelper.areStacksEqual(stackInSlot, stack)) {
                var insertSize = Math.min(remaining, Math.max(0, limit - stackInSlot.getCount()));

                stackInSlot.grow(insertSize);
                remaining -= insertSize;
            }
        }

        return remaining;
    }

    @Override
    protected boolean canInsertStackSided(int slot, ItemStack stack, @Nullable Direction direction) {
        if (direction == null)
            return true;
        if (ArrayUtils.contains(INPUT_SLOTS, slot) && direction == Direction.UP)
            return true;
        if (slot == FUEL_SLOT && direction == Direction.NORTH)
            return FurnaceBlockEntity.isFuel(stack);

        return false;
    }
}
