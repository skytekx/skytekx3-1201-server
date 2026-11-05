package com.blakebr0.mysticalautomation.tileentity;

import com.blakebr0.cucumber.energy.DynamicEnergyStorage;
import com.blakebr0.cucumber.helper.StackHelper;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;
import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
import com.blakebr0.mysticalagriculture.container.inventory.UpgradeItemStackHandler;
import com.blakebr0.mysticalagriculture.util.IUpgradeableMachine;
import com.blakebr0.mysticalagriculture.util.MachineUpgradeTier;
import com.blakebr0.mysticalautomation.block.EnchanternatorBlock;
import com.blakebr0.mysticalautomation.container.EnchanternatorContainer;
import com.blakebr0.mysticalautomation.init.ModTileEntities;
import com.blakebr0.mysticalautomation.util.ContainerDataBuilder;
import com.blakebr0.mysticalautomation.util.EnchanterRecipes;
import com.blakebr0.mysticalautomation.util.MachineUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class EnchanternatorTileEntity extends BaseMachineTileEntity implements MenuProvider, IUpgradeableMachine {
    private static final int[] INPUT_SLOTS = IntStream.rangeClosed(0, 2).toArray();
    private static final int FUEL_SLOT = 3;
    private static final int OUTPUT_SLOT = 4;

    public static final int FUEL_TICK_MULTIPLIER = 20;
    public static final int OPERATION_TIME = 200;
    public static final int FUEL_USAGE = 20;
    public static final int FUEL_CAPACITY = 80000;

    private final BaseItemStackHandler inventory;
    private final BaseItemStackHandler recipeInventory;
    private final UpgradeItemStackHandler upgradeInventory;
    private final DynamicEnergyStorage energy;
    private IEnchanterRecipe recipe;

    private final ContainerData dataAccess;

    private MachineUpgradeTier tier;
    private int progress;
    private int selectedLevel;
    private int fuelLeft;
    private int fuelItemValue;
    private boolean isRunning;
    private boolean isGridChanged = true;

    public EnchanternatorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntities.ENCHANTERNATOR.get(), pos, state);
        this.recipeInventory = createRecipeInventoryHandler(() -> {
            this.isGridChanged = true;
            this.setChangedFast();
        });
        this.inventory = createInventoryHandler(this.recipeInventory, this::setChanged);
        this.upgradeInventory = new UpgradeItemStackHandler();
        this.energy = new DynamicEnergyStorage(FUEL_CAPACITY, this::setChangedFast);

        this.dataAccess = ContainerDataBuilder.builder()
                .sync(this.energy::getEnergyStored, this.energy::setEnergyStored)
                .sync(this.energy::getMaxEnergyStored, this.energy::setMaxEnergyStorage)
                .sync(() -> this.fuelLeft, value -> this.fuelLeft = value)
                .sync(() -> this.fuelItemValue, value -> this.fuelItemValue = value)
                .sync(() -> this.progress, value -> this.progress = value)
                .sync(this::getOperationTime, value -> {})
                .sync(() -> this.selectedLevel, value -> this.selectedLevel = value)
                .build();
    }

    @Override
    public BaseItemStackHandler getInventory() {
        return this.inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mysticalautomation.enchanternator");
    }

    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new EnchanternatorContainer(i, inventory, this.inventory, this.recipeInventory, this.upgradeInventory, this.dataAccess, this.getBlockPos());
    }

    @Override
    public UpgradeItemStackHandler getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        this.progress = tag.getInt("Progress");
        this.selectedLevel = tag.getInt("SelectedLevel");
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
        tag.putInt("SelectedLevel", this.selectedLevel);
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

    public static void tick(Level level, BlockPos pos, BlockState state, EnchanternatorTileEntity tile) {
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
            if (recipe != null && tile.selectedLevel > 0) {
                // SkyTekx: upstream enchants at the highest level the inputs allow but takes the ingredients of one
                // level and keeps the item to enchant. This enchants at the selected level, takes the ingredients of
                // that level, as Mystical Agriculture's Enchanter does, and takes the item to enchant
                var inputs = tile.getInputResult(recipe, tile.selectedLevel);
                var maxLevel = EnchanterRecipes.getMaxResultEnchantmentLevel(recipe, tile.toCraftingInput());
                if (inputs.hasAll && maxLevel >= tile.selectedLevel) {
                    var result = EnchanterRecipes.assemble(recipe, tile.toCraftingInput(), level.registryAccess(), tile.selectedLevel);
                    var output = tile.inventory.getStackInSlot(OUTPUT_SLOT);

                    // SkyTekx: an item that already has the enchantment at this level, or an incompatible one, gives
                    // no result. Upstream would still take the inputs
                    if (!result.isEmpty() && StackHelper.canCombineStacks(result, output)) {
                        tile.isRunning = true;
                        tile.progress++;
                        tile.energy.extractEnergy(tile.getFuelUsage(), false);

                        if (tile.progress >= tile.getOperationTime()) {
                            int[] amounts = inputs.amounts;
                            for (int i = 0; i < amounts.length; i++) {
                                var amount = amounts[i];
                                var input = tile.inventory.getStackInSlot(INPUT_SLOTS[i]);

                                tile.inventory.setStackInSlot(INPUT_SLOTS[i], shrinkAndRetainContainer(input, amount));
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
            level.setBlock(pos, state.setValue(EnchanternatorBlock.RUNNING, tile.isRunning), 3);

            tile.setChangedFast();
        }
    }

    public static BaseItemStackHandler createInventoryHandler() {
        return createInventoryHandler(createRecipeInventoryHandler(), null);
    }

    public static BaseItemStackHandler createInventoryHandler(BaseItemStackHandler recipeInventory, @Nullable Runnable onContentsChanged) {
        return BaseItemStackHandler.create(5, onContentsChanged, handler -> {
            for (var slot : INPUT_SLOTS) {
                handler.addSlotLimit(slot, 512);
            }

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
            );
        });
    }

    public static BaseItemStackHandler createRecipeInventoryHandler() {
        return createRecipeInventoryHandler(null);
    }

    public static BaseItemStackHandler createRecipeInventoryHandler(@Nullable Runnable onContentsChanged) {
        return BaseItemStackHandler.create(3, onContentsChanged, handler -> {});
    }

    @Override
    public DynamicEnergyStorage getEnergy() {
        return this.energy;
    }

    public int getSelectedLevel() {
        return this.selectedLevel;
    }

    public void setSelectedLevel(int level) {
        this.selectedLevel = Mth.clamp(level, 0, 5);
        this.setChangedFast();
    }

    @Nullable
    public IEnchanterRecipe getActiveRecipe() {
        if (this.isGridChanged) {
            this.isGridChanged = false;

            if (this.level == null)
                return null;

            // to show the proper ghost item as the result, we need to both pretend to have the maximum number of materials
            // to account for all requirements
            this.recipe = EnchanterRecipes.find(
                    this.level,
                    this.recipeInventory.getStackInSlot(0).copyWithCount(512),
                    this.recipeInventory.getStackInSlot(1).copyWithCount(512),
                    this.recipeInventory.getStackInSlot(2)
            );
        }

        return this.recipe;
    }

    private int getOperationTime() {
        return this.tier == null ? OPERATION_TIME : MachineUpgrades.getOperationTime(this.tier, OPERATION_TIME);
    }

    private int getFuelUsage() {
        return this.tier == null ? FUEL_USAGE : MachineUpgrades.getFuelUsage(this.tier, FUEL_USAGE);
    }

    // SkyTekx port: the three input stacks, the 1.20.1 form of upstream's ShapelessCraftingInput (Cucumber 8)
    private List<ItemStack> toCraftingInput() {
        return List.of(
                this.inventory.getStackInSlot(INPUT_SLOTS[0]),
                this.inventory.getStackInSlot(INPUT_SLOTS[1]),
                this.inventory.getStackInSlot(INPUT_SLOTS[2])
        );
    }

    private InputResult getInputResult(IEnchanterRecipe recipe, int enchantmentLevel) {
        var amounts = new int[INPUT_SLOTS.length];
        var remaining = new int[INPUT_SLOTS.length];

        for (int i = 0; i < INPUT_SLOTS.length; i++) {
            remaining[i] = this.inventory.getStackInSlot(INPUT_SLOTS[i]).getCount();
        }

        var ingredients = recipe.getIngredients();
        var required = 0;

        for (int i = 0; i < ingredients.size(); i++) {
            var ingredient = ingredients.get(i);
            if (ingredient.isEmpty())
                continue;

            var amount = EnchanterRecipes.getCount(recipe, i) * enchantmentLevel;

            required += amount;

            // SkyTekx: the ingredients come from the two ingredient slots, the third holds the item to enchant
            for (int j = 0; j < INPUT_SLOTS.length - 1; j++) {
                var slot = INPUT_SLOTS[j];
                var stack = this.inventory.getStackInSlot(slot);
                if (remaining[j] >= amount && ingredient.test(stack)) {
                    remaining[j] -= amount;
                    amounts[j] += amount;
                    break;
                }
            }
        }

        // SkyTekx: the item to enchant, as upstream b100f44 of the 26.1 line takes it too
        {
            required += 1;

            var slot = INPUT_SLOTS[2];
            var stack = this.inventory.getStackInSlot(slot);

            if (remaining[slot] >= 1 && !stack.isEmpty()) {
                remaining[slot] -= 1;
                amounts[slot] += 1;
            }
        }

        return new InputResult(Arrays.stream(amounts).sum() == required, amounts);
    }

    // SkyTekx port: Cucumber 8's StackHelper#shrinkAndRetainContainer, which Cucumber 7 lacks
    private static ItemStack shrinkAndRetainContainer(ItemStack stack, int amount) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        var remaining = stack.getCraftingRemainingItem();
        var result = StackHelper.shrink(stack, amount, false);

        if (!remaining.isEmpty() && StackHelper.areStacksEqual(remaining, result)) {
            result.grow(Math.min(remaining.getCount(), result.getMaxStackSize()));
        }

        return result;
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

    private record InputResult(boolean hasAll, int[] amounts) { }
}
