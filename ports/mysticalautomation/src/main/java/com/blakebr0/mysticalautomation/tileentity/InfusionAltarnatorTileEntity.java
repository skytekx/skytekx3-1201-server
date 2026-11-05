package com.blakebr0.mysticalautomation.tileentity;

import com.blakebr0.cucumber.energy.DynamicEnergyStorage;
import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.util.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.util.MachineUpgradeTier;
import com.blakebr0.mysticalautomation.block.InfusionAltarnatorBlock;
import com.blakebr0.mysticalautomation.compat.MysticalCompat;
import com.blakebr0.mysticalautomation.container.InfusionAltarnatorContainer;
import com.blakebr0.mysticalautomation.init.ModTileEntities;
import com.blakebr0.mysticalautomation.util.CachedRecipe;
import com.blakebr0.mysticalautomation.util.ContainerDataBuilder;
import com.blakebr0.mysticalautomation.util.GhostGridInputs;
import com.blakebr0.mysticalautomation.util.MachineUpgrades;
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

public class InfusionAltarnatorTileEntity extends BaseMachineTileEntity implements MenuProvider, IUpgradeableMachine {
    private static final int[] INPUT_SLOTS = IntStream.rangeClosed(0, 8).toArray();
    private static final int FUEL_SLOT = 9;
    private static final int OUTPUT_SLOT = 10;

    public static final int FUEL_TICK_MULTIPLIER = 20;
    public static final int OPERATION_TIME = 200;
    public static final int FUEL_USAGE = 20;
    public static final int FUEL_CAPACITY = 80000;

    private final BaseItemStackHandler inventory;
    private final BaseItemStackHandler recipeInventory;
    private final UpgradeItemStackHandler upgradeInventory;
    private final DynamicEnergyStorage energy;
    private final CachedRecipe<Container, IInfusionRecipe> recipe;

    private final ContainerData dataAccess;

    private MachineUpgradeTier tier;
    private int progress;
    private int fuelLeft;
    private int fuelItemValue;
    private boolean isRunning;
    private boolean isGridChanged = true;

    public InfusionAltarnatorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.INFUSION_ALTARNATOR.get(), pos, state);
        this.recipeInventory = createRecipeInventoryHandler(() -> {
            this.isGridChanged = true;
            this.setChangedFast();
        });
        this.inventory = createInventoryHandler(this.recipeInventory, this::setChanged);
        this.upgradeInventory = new UpgradeItemStackHandler();
        this.energy = new DynamicEnergyStorage(FUEL_CAPACITY, this::setChangedFast);
        this.recipe = new CachedRecipe<>(MysticalCompat.RecipeTypes.INFUSION.get());

        this.dataAccess = ContainerDataBuilder.builder()
                .sync(this.energy::getEnergyStored, this.energy::setEnergyStored)
                .sync(this.energy::getMaxEnergyStored, this.energy::setMaxEnergyStorage)
                .sync(() -> this.fuelLeft, value -> this.fuelLeft = value)
                .sync(() -> this.fuelItemValue, value -> this.fuelItemValue = value)
                .sync(() -> this.progress, value -> this.progress = value)
                .sync(this::getOperationTime, value -> {})
                .build();
    }

    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mysticalautomation.infusion_altarnator");
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new InfusionAltarnatorContainer(i, inventory, this.inventory, this.recipeInventory, this.upgradeInventory, this.dataAccess, this.getBlockPos());
    }

    @Override
    public UpgradeItemStackHandler getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.progress = tag.getInt("Progress");
        this.fuelLeft = tag.getInt("FuelLeft");
        this.fuelItemValue = tag.getInt("FuelItemValue");
        loadEnergy(this.energy, tag);
        this.recipeInventory.deserializeNBT(tag.getCompound("RecipeInventory"));
        this.upgradeInventory.deserializeNBT(tag.getCompound("UpgradeInventory"));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt("Progress", this.progress);
        tag.putInt("FuelLeft", this.fuelLeft);
        tag.putInt("FuelItemValue", this.fuelItemValue);
        tag.putInt("Energy", this.energy.getEnergyStored());
        tag.put("RecipeInventory", this.recipeInventory.serializeNBT());
        tag.put("UpgradeInventory", this.upgradeInventory.serializeNBT());
    }

    @Override
    protected void clearAdditional() {
        this.upgradeInventory.getStacks().clear();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfusionAltarnatorTileEntity tile) {
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
            if (recipe != null) {
                var inputs = tile.getInputResult(recipe);
                if (inputs.hasAll) {
                    // SkyTekx: the result comes from the items taken (getInputResult), not from the ghost grid
                    var result = recipe.assemble(inputs.craftingInput, level.registryAccess());
                    var output = tile.inventory.getStackInSlot(OUTPUT_SLOT);

                    if (!result.isEmpty() && StackHelper.canCombineStacks(result, output)) {
                        tile.isRunning = true;
                        tile.progress++;
                        tile.energy.extractEnergy(tile.getFuelUsage(), false);

                        if (tile.progress >= tile.getOperationTime()) {
                            int[] amounts = inputs.amounts;
                            for (int i = 0; i < amounts.length; i++) {
                                var amount = amounts[i];
                                var input = tile.inventory.getStackInSlot(INPUT_SLOTS[i]);

                                tile.inventory.setStackInSlot(INPUT_SLOTS[i], StackHelper.shrink(input, amount, amount == 1));
                            }

                            tile.inventory.setStackInSlot(OUTPUT_SLOT, StackHelper.combineStacks(output, result));

                            tile.progress = 0;
                        }

                        tile.setChangedFast();
                    }
                } else {
                    if (tile.progress > 0) {
                        tile.progress = 0;
                        tile.setChangedFast();
                    }
                }
            } else {
                if (tile.progress > 0) {
                    tile.progress = 0;
                    tile.setChangedFast();
                }
            }
        }

        if (wasRunning != tile.isRunning) {
            level.setBlock(pos, state.setValue(InfusionAltarnatorBlock.RUNNING, tile.isRunning), 3);

            tile.setChangedFast();
        }
    }

    public static BaseItemStackHandler createInventoryHandler() {
        return createInventoryHandler(createRecipeInventoryHandler(), null);
    }

    public static BaseItemStackHandler createInventoryHandler(BaseItemStackHandler recipeInventory, @Nullable Runnable onContentsChanged) {
        return BaseItemStackHandler.create(11, onContentsChanged, handler -> {
            handler.setCanInsert((slot, stack) -> {
                if (ArrayUtils.contains(INPUT_SLOTS, slot)) {
                    var recipeStack = recipeInventory.getStackInSlot(slot);
                    return !recipeStack.isEmpty() && StackHelper.areStacksEqual(recipeStack, stack);
                }

                return true;
            });

            handler.setOutputSlots(OUTPUT_SLOT);
            handler.setCanExtract(slot ->
                    slot == OUTPUT_SLOT || (slot == FUEL_SLOT && !FurnaceBlockEntity.isFuel(handler.getStackInSlot(slot)))
                    || (ArrayUtils.contains(INPUT_SLOTS, slot) && !recipeInventory.isItemValid(slot, handler.getStackInSlot(slot)))
            );
        });
    }

    public static BaseItemStackHandler createRecipeInventoryHandler() {
        return createRecipeInventoryHandler(null);
    }

    public static BaseItemStackHandler createRecipeInventoryHandler(@Nullable Runnable onContentsChanged) {
        return BaseItemStackHandler.create(9, onContentsChanged, handler -> {});
    }

    @Override
    public DynamicEnergyStorage getEnergy() {
        return this.energy;
    }

    @Nullable
    public IInfusionRecipe getActiveRecipe() {
        if (this.isGridChanged) {
            this.isGridChanged = false;
            return this.recipe.checkAndGet(this.toCraftingInput(), this.level);
        }

        return this.recipe.get();
    }

    private int getOperationTime() {
        return this.tier == null ? OPERATION_TIME : MachineUpgrades.getOperationTime(this.tier, OPERATION_TIME);
    }

    private int getFuelUsage() {
        return this.tier == null ? FUEL_USAGE : MachineUpgrades.getFuelUsage(this.tier, FUEL_USAGE);
    }

    // SkyTekx port: the nine ghost slots as they are, slot 0 the altar item, which Mystical Agriculture 7's
    // recipes test first before they match the rest in any order
    private Container toCraftingInput() {
        return this.recipeInventory.asRecipeWrapper();
    }

    private InputResult getInputResult(IInfusionRecipe recipe) {
        // SkyTekx: upstream tested the real inputs with Ingredient#test, which ignores NBT, and assembled the result
        // from the ghost grid, so an enchanted ghost on the altar slot over a plain input gave enchanted output with
        // the recipes that transfer NBT. Every filled ghost slot now takes one real item that equals it, item and NBT,
        // and the result comes from copies of the items taken, in the layout of the ghost grid: slot 0 the altar
        // item, which Mystical Agriculture 7's recipes test first before they match the rest in any order
        var taken = GhostGridInputs.take(this.inventory, INPUT_SLOTS, this.recipeInventory, GhostGridInputs.onePerGhost(this.recipeInventory));
        if (!taken.hasAll())
            return new InputResult(false, taken.amounts(), null);

        var input = taken.taken().asRecipeWrapper();
        if (!recipe.matches(input, this.level))
            return new InputResult(false, taken.amounts(), null);

        return new InputResult(true, taken.amounts(), input);
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

    private record InputResult(boolean hasAll, int[] amounts, @Nullable Container craftingInput) { }
}
