package com.enderio.machines.common.blockentity;

import com.enderio.EnderIO;
import com.enderio.api.UseOnly;
import com.enderio.core.common.network.CoreNetwork;
import com.enderio.core.common.network.slot.FluidStackNetworkDataSlot;
import com.enderio.core.common.network.slot.ResourceLocationNetworkDataSlot;
import com.enderio.core.common.recipes.OutputStack;
import com.enderio.machines.common.block.ProgressMachineBlock;
import com.enderio.machines.common.blockentity.base.MachineBlockEntity;
import com.enderio.machines.common.blockentity.task.CraftingMachineTask;
import com.enderio.machines.common.blockentity.task.host.CraftingMachineTaskHost;
import com.enderio.machines.common.init.MachineRecipes;
import com.enderio.machines.common.io.fluid.IFluidItemInteractive;
import com.enderio.machines.common.io.fluid.MachineFluidHandler;
import com.enderio.machines.common.io.fluid.MachineFluidTank;
import com.enderio.machines.common.io.fluid.MachineTankLayout;
import com.enderio.machines.common.io.fluid.TankAccess;
import com.enderio.machines.common.io.item.MachineInventory;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.io.item.MultiSlotAccess;
import com.enderio.machines.common.menu.VatMenu;
import com.enderio.machines.common.network.VatDumpTankPacket;
import com.enderio.machines.common.network.VatMoveTankPacket;
import com.enderio.machines.common.recipe.FermentingRecipe;
import com.enderio.machines.common.recipe.RecipeCaches;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Vat: ferments the fluid of its input tank with two reagents into the fluid of its output tank. It needs no power.
 * Ported from Ender IO 8.x (1cb2e1420, 9aff4169f, e4645aa88) onto the 6.x machine base.
 */
public class VatBlockEntity extends MachineBlockEntity implements IFluidItemInteractive {

    public static final int TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;
    public static final TankAccess INPUT_TANK = new TankAccess();
    public static final TankAccess OUTPUT_TANK = new TankAccess();
    public static final MultiSlotAccess REAGENTS = new MultiSlotAccess();

    // The Vat puts its output into a tank, so its task has no output slots.
    private static final MultiSlotAccess NO_OUTPUT_SLOTS = new MultiSlotAccess();
    private static final ResourceLocation EMPTY = EnderIO.loc("");

    private final CraftingMachineTaskHost<FermentingRecipe, FermentingRecipe.Container> craftingTaskHost;

    @UseOnly(LogicalSide.CLIENT)
    private ResourceLocation clientRecipeId = EMPTY;

    private boolean updateModel = false;

    public VatBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);

        // Sync both tanks and the active recipe for the screen. The client cannot change these slots.
        addDataSlot(new FluidStackNetworkDataSlot(() -> INPUT_TANK.getFluid(this), f -> INPUT_TANK.setFluid(this, f)));
        addDataSlot(new FluidStackNetworkDataSlot(() -> OUTPUT_TANK.getFluid(this), f -> OUTPUT_TANK.setFluid(this, f)));
        addDataSlot(new ResourceLocationNetworkDataSlot(this::getRecipeId, id -> clientRecipeId = id));

        craftingTaskHost = new CraftingMachineTaskHost<>(this, () -> true, MachineRecipes.VAT_FERMENTING.type().get(),
            new FermentingRecipe.Container(getInventoryNN(), () -> INPUT_TANK.getFluid(this)), this::createTask);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new VatMenu(this, playerInventory, containerId);
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (canAct()) {
            craftingTaskHost.tick();
        }

        updateMachineState(MachineState.ACTIVE, isActive());
        updatePoweredState();
    }

    // Show the active state like the powered machines do, with the same one tick delay against flicker.
    private void updatePoweredState() {
        if (level == null) {
            return;
        }

        BlockState blockState = getBlockState();
        if (blockState.hasProperty(ProgressMachineBlock.POWERED) && blockState.getValue(ProgressMachineBlock.POWERED) != isActive()) {
            if (updateModel) {
                level.setBlock(getBlockPos(), blockState.setValue(ProgressMachineBlock.POWERED, isActive()), Block.UPDATE_ALL);
            }
            updateModel = true;
        } else {
            updateModel = false;
        }
    }

    protected boolean isActive() {
        return canAct() && craftingTaskHost.hasTask();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        craftingTaskHost.onLevelReady();
    }

    @Override
    public InteractionResult onBlockEntityUsed(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            if (handleFluidItemInteraction(player, hand, stack, this, INPUT_TANK) || handleFluidItemInteraction(player, hand, stack, this, OUTPUT_TANK)) {
                player.getInventory().setChanged();
                return InteractionResult.CONSUME;
            }
        }

        return super.onBlockEntityUsed(state, level, pos, player, hand, hit);
    }

    // region Inventory

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
            .inputSlot(2, this::acceptSlotInput)
            .slotAccess(REAGENTS)
            .build();
    }

    // Only items that can take part in a recipe with the other reagent (e4645aa88).
    private boolean acceptSlotInput(int slot, ItemStack stack) {
        return RecipeCaches.FERMENTING.hasValidRecipeIf(getInventoryNN(), REAGENTS, slot, stack);
    }

    @Override
    protected void onInventoryContentsChanged(int slot) {
        super.onInventoryContentsChanged(slot);
        craftingTaskHost.newTaskAvailable();
    }

    // endregion

    // region Fluid Storage

    @Override
    public MachineTankLayout getTankLayout() {
        return MachineTankLayout.builder()
            .tank(INPUT_TANK, TANK_CAPACITY, true, false, stack -> true)
            .tank(OUTPUT_TANK, TANK_CAPACITY, false, true, stack -> true)
            .build();
    }

    @Override
    protected MachineFluidHandler createFluidHandler(MachineTankLayout layout) {
        return new MachineFluidHandler(getIOConfig(), layout) {
            @Override
            protected void onContentsChanged(int slot) {
                craftingTaskHost.newTaskAvailable();
                setChanged();
            }

            // SkyTekx: the 6.x handler drains the first tank here without the canExtract check that drain(FluidStack) has,
            // which would let pipes and buckets pull the input tank. Drain the output tank only.
            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                FluidStack output = OUTPUT_TANK.getFluid(this);
                if (maxDrain <= 0 || output.isEmpty()) {
                    return FluidStack.EMPTY;
                }

                return drain(new FluidStack(output, maxDrain), action);
            }
        };
    }

    public MachineFluidTank getInputTank() {
        return INPUT_TANK.getTank(this);
    }

    public MachineFluidTank getOutputTank() {
        return OUTPUT_TANK.getTank(this);
    }

    /**
     * Moves the input tank into the empty output tank. A client sends the request to the server.
     */
    public void moveFluidToOutputTank() {
        if (level != null && level.isClientSide()) {
            CoreNetwork.sendToServer(new VatMoveTankPacket(getBlockPos()));
            return;
        }

        if (OUTPUT_TANK.isEmpty(this) && !INPUT_TANK.isEmpty(this)) {
            OUTPUT_TANK.setFluid(this, INPUT_TANK.getFluid(this).copy());
            INPUT_TANK.setFluid(this, FluidStack.EMPTY);
            craftingTaskHost.newTaskAvailable();
            setChanged();
        }
    }

    /**
     * Voids the output tank. A client sends the request to the server.
     */
    public void dumpOutputTank() {
        if (level != null && level.isClientSide()) {
            CoreNetwork.sendToServer(new VatDumpTankPacket(getBlockPos()));
            return;
        }

        if (!OUTPUT_TANK.isEmpty(this)) {
            OUTPUT_TANK.setFluid(this, FluidStack.EMPTY);
            craftingTaskHost.newTaskAvailable();
            setChanged();
        }
    }

    // endregion

    // region Crafting Task

    public float getCraftingProgress() {
        return craftingTaskHost.getProgress();
    }

    /**
     * The id of the recipe in progress, an empty path when there is none. A client reads the synced id.
     */
    public ResourceLocation getRecipeId() {
        if (level != null && level.isClientSide()) {
            return clientRecipeId;
        }

        CraftingMachineTask<FermentingRecipe, FermentingRecipe.Container> task = craftingTaskHost.getCurrentTask();
        if (craftingTaskHost.hasTask() && task != null && task.getRecipe() != null) {
            return task.getRecipe().getId();
        }

        return EMPTY;
    }

    protected CraftingMachineTask<FermentingRecipe, FermentingRecipe.Container> createTask(Level level, FermentingRecipe.Container container,
        @Nullable FermentingRecipe recipe) {
        return new VatCraftingTask(level, getInventoryNN(), container, recipe);
    }

    private class VatCraftingTask extends CraftingMachineTask<FermentingRecipe, FermentingRecipe.Container> {

        VatCraftingTask(@NotNull Level level, MachineInventory inventory, FermentingRecipe.Container container, @Nullable FermentingRecipe recipe) {
            super(level, inventory, container, NO_OUTPUT_SLOTS, recipe);
        }

        @Override
        protected void consumeInputs(FermentingRecipe recipe) {
            REAGENTS.get(0).getItemStack(inventory).shrink(1);
            REAGENTS.get(1).getItemStack(inventory).shrink(1);

            INPUT_TANK.getTank(getFluidHandlerNN()).drain(recipe.getInput().amount(), IFluidHandler.FluidAction.EXECUTE);
            setChanged();
        }

        @Override
        protected boolean placeOutputs(List<OutputStack> outputs, boolean simulate) {
            // SkyTekx: build the output again from the reagents that are consumed now. The task fixes its outputs when it
            // starts, so swapping a reagent for one with a lower multiplier during the task would keep the higher output.
            FermentingRecipe recipe = getRecipe();
            FluidStack output = recipe != null ? recipe.craft(container, level.registryAccess()).get(0).getFluid() : outputs.get(0).getFluid();

            // Fill only when the whole amount fits, so a partial fill never repeats every tick (9aff4169f, GH-1196).
            MachineFluidTank outputTank = OUTPUT_TANK.getTank(getFluidHandlerNN());
            if (outputTank.fill(output, IFluidHandler.FluidAction.SIMULATE) != output.getAmount()) {
                return false;
            }

            if (!simulate) {
                outputTank.fill(output, IFluidHandler.FluidAction.EXECUTE);
                setChanged();
            }

            return true;
        }

        @Override
        protected int makeProgress(int remainingProgress) {
            return 1; // The Vat needs no power.
        }

        @Override
        protected int getProgressRequired(FermentingRecipe recipe) {
            return recipe.getTicks();
        }
    }

    // endregion

    // region Serialization

    @Override
    public void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        craftingTaskHost.save(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        craftingTaskHost.load(pTag);
    }

    // endregion
}
