package com.enderio.machines.common.blockentity;

import com.enderio.EnderIO;
import com.enderio.api.capability.StoredEntityData;
import com.enderio.api.capacitor.CapacitorModifier;
import com.enderio.api.capacitor.QuadraticScalable;
import com.enderio.api.io.energy.EnergyIOMode;
import com.enderio.base.common.init.EIOItems;
import com.enderio.core.common.network.slot.BooleanNetworkDataSlot;
import com.enderio.core.common.network.slot.EnumNetworkDataSlot;
import com.enderio.core.common.network.slot.ResourceLocationNetworkDataSlot;
import com.enderio.machines.common.MachineNBTKeys;
import com.enderio.machines.common.blockentity.base.PoweredMachineBlockEntity;
import com.enderio.machines.common.blockentity.task.IMachineTask;
import com.enderio.machines.common.blockentity.task.SpawnerCaptureTask;
import com.enderio.machines.common.blockentity.task.SpawnerMachineTask;
import com.enderio.machines.common.blockentity.task.host.MachineTaskHost;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.io.item.SingleSlotAccess;
import com.enderio.machines.common.lang.MachineLang;
import com.enderio.machines.common.menu.PoweredSpawnerMenu;
import com.enderio.machines.common.tag.MachineTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
// TODO: I want to revisit the powered spawner and task
//       But there's not enough time before alpha, so just porting as-is.
public class PoweredSpawnerBlockEntity extends PoweredMachineBlockEntity {

    // SkyTekx: the capture mode slots of 8.x (a90e5c512), after the capacitor slot.
    public static final SingleSlotAccess INPUT = new SingleSlotAccess();
    public static final SingleSlotAccess OUTPUT = new SingleSlotAccess();

    public static final QuadraticScalable CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY, MachinesConfig.COMMON.ENERGY.POWERED_SPAWNER_CAPACITY);
    public static final QuadraticScalable USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.ENERGY.POWERED_SPAWNER_USAGE);
    public static final ResourceLocation NO_MOB = EnderIO.loc("no_mob");
    private StoredEntityData entityData = StoredEntityData.empty();
    private SpawnerBlockedReason reason = SpawnerBlockedReason.NONE;
    private final MachineTaskHost taskHost;

    // SkyTekx: the mode of 8.x (a90e5c512), saved under the optional key SpawnerMode. Without the key it is SPAWN.
    private PoweredSpawnerMode mode = PoweredSpawnerMode.SPAWN;
    private final EnumNetworkDataSlot<PoweredSpawnerMode> modeDataSlot;

    // SkyTekx: whether a Mind Killer sits on top (84bfe2477). Upstream reads it in onLoad. That is the chunk load hazard
    // of 4dd6e5e09, so it is read on the first server tick and on a change of the block above.
    private boolean mindKiller = false;
    private boolean needsMindKillerCheck = true;


    public PoweredSpawnerBlockEntity(BlockEntityType type, BlockPos worldPosition, BlockState blockState) {
        super(EnergyIOMode.Input, CAPACITY, USAGE, type, worldPosition, blockState);

        rangeVisibleDataSlot = new BooleanNetworkDataSlot(this::isRangeVisible, b -> this.rangeVisible = b);
        addClientDataSlot(rangeVisibleDataSlot);

        addDataSlot(new ResourceLocationNetworkDataSlot(() -> this.getEntityType().orElse(NO_MOB), rl -> {
            setEntityType(rl);
            EnderIO.LOGGER.info("UPDATED ENTITY TYPE.");
        }));
        range = 4;

        taskHost = new MachineTaskHost(this, this::hasEnergy) {
            @Override
            protected @Nullable IMachineTask getNewTask() {
                return createTask();
            }

            @Override
            protected @Nullable IMachineTask loadTask(CompoundTag nbt) {
                IMachineTask task = createTask();
                if (task != null) {
                    task.deserializeNBT(nbt);
                }
                return task;
            }
        };

        // The screen changes the mode. The enum slot refuses an unknown ordinal, and the setter refuses null.
        modeDataSlot = new EnumNetworkDataSlot<>(PoweredSpawnerMode.class, this::getMode, this::applyMode);
        addClientDataSlot(modeDataSlot);

        updateMachineState(new MachineState(MachineStateType.ERROR, this.reason.component), false);
    }

    public PoweredSpawnerMode getMode() {
        return mode;
    }

    /**
     * Set the mode. On the client this asks the server to change it.
     */
    public void setMode(PoweredSpawnerMode mode) {
        if (level != null && level.isClientSide()) {
            clientUpdateSlot(modeDataSlot, mode);
        } else {
            applyMode(mode);
        }
    }

    private void applyMode(@Nullable PoweredSpawnerMode mode) {
        if (mode == null || mode == this.mode) {
            return;
        }

        this.mode = mode;

        // The client only takes the value that the server synced.
        if (level != null && !level.isClientSide()) {
            // The running task sees the new mode and completes, then the next tick starts a task of the new mode.
            taskHost.newTaskAvailable();
            setChanged();
        }
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new PoweredSpawnerMenu(this, pPlayerInventory, pContainerId);
    }

    @Override
    public void serverTick() {
        if (needsMindKillerCheck) {
            needsMindKillerCheck = false;
            updateMindKiller();
        }

        super.serverTick();

        if (canAct()) {
            taskHost.tick();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        taskHost.onLevelReady();
    }

    @Override
    public void neighborChanged(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.neighborChanged(state, level, pos, neighbor);
        if (this.level != null && !this.level.isClientSide() && worldPosition.above().equals(neighbor)) {
            // A Mind Killer was placed on top or taken away.
            updateMindKiller();
        }
    }

    private void updateMindKiller() {
        if (level != null) {
            mindKiller = level.getBlockState(worldPosition.above()).is(MachineTags.Blocks.MIND_KILLER);
        }
    }

    /**
     * Whether the spawned mobs get no AI, because a Mind Killer sits on top.
     */
    public boolean hasMindKiller() {
        return mindKiller;
    }

    // region Inventory

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        // SkyTekx: the input and output slots of capture mode go after the capacitor slot, so the capacitor of an
        // existing spawner stays in slot 0. MachineInventory sizes itself from this layout and ignores the saved size.
        return MachineInventoryLayout.builder()
            .capacitor()
            .inputSlot((i, stack) -> stack.is(EIOItems.EMPTY_SOUL_VIAL.get()))
            .slotAccess(INPUT)
            .outputSlot()
            .slotAccess(OUTPUT)
            .build();
    }

    @Override
    protected void onInventoryContentsChanged(int slot) {
        super.onInventoryContentsChanged(slot);
        taskHost.newTaskAvailable();
    }

    // endregion

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.POWERED_SPAWNER_RANGE_COLOR.get();
    }

    @Override
    public int getMaxRange() {
        return 3;
    }

    // region Task

    public float getSpawnProgress() {
        return taskHost.getProgress();
    }

    @Override
    protected boolean isActive() {
        return canAct() && hasEnergy() && taskHost.hasTask();
    }

    @Nullable
    private IMachineTask createTask() {
        // A new task starts without the reason of the last one, which may belong to the other mode.
        setReason(SpawnerBlockedReason.NONE);

        if (mode == PoweredSpawnerMode.CAPTURE) {
            if (!SpawnerCaptureTask.hasEmptyVial(this)) {
                setReason(SpawnerBlockedReason.INPUT_EMPTY);
                return null;
            }

            // A capture task that cannot run (unknown or blacklisted soul, occupied output) has set the reason. The
            // spawner holds no task until its inventory or mode changes.
            SpawnerCaptureTask task = new SpawnerCaptureTask(this, this.getEnergyStorage(), this.getEntityType());
            return task.isCompleted() ? null : task;
        }

        return new SpawnerMachineTask(this, this.getEnergyStorage(), this.getEntityType());
    }

    // endregion

    public Optional<ResourceLocation> getEntityType() {
        return entityData.getEntityType();
    }

    public void setEntityType(ResourceLocation entityType) {
        entityData = StoredEntityData.of(entityType);
    }

    public StoredEntityData getEntityData() {
        return entityData;
    }

    public void setReason(SpawnerBlockedReason reason) {
        // SkyTekx: NONE is no error. Before a90e5c512 a successful spawn showed an error state named NONE.
        if (this.reason != SpawnerBlockedReason.NONE) {
            updateMachineState(new MachineState(MachineStateType.ERROR, this.reason.component), false);
        }

        if (reason != SpawnerBlockedReason.NONE) {
            updateMachineState(new MachineState(MachineStateType.ERROR, reason.component), true);
        }

        this.reason = reason;
    }

    // region Serialization

    @Override
    public void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put(MachineNBTKeys.ENTITY_STORAGE, entityData.serializeNBT());
        if (mode != PoweredSpawnerMode.SPAWN) {
            pTag.putString(MachineNBTKeys.SPAWNER_MODE, mode.getSerializedName());
        }
        taskHost.save(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        entityData.deserializeNBT(pTag.getCompound(MachineNBTKeys.ENTITY_STORAGE));

        // The mode decides which task loads, so read it first. A spawner saved before the mode existed stays in SPAWN.
        PoweredSpawnerMode savedMode = PoweredSpawnerMode.byName(pTag.getString(MachineNBTKeys.SPAWNER_MODE));
        mode = savedMode != null ? savedMode : PoweredSpawnerMode.SPAWN;

        taskHost.load(pTag);
    }

    // endregion

    public enum SpawnerBlockedReason {
        TOO_MANY_MOB(MachineLang.TOO_MANY_MOB),
        TOO_MANY_SPAWNER(MachineLang.TOO_MANY_SPAWNER),
        UNKNOWN_MOB(MachineLang.UNKNOWN),
        OTHER_MOD(MachineLang.OTHER_MOD),
        DISABLED(MachineLang.DISABLED),
        // SkyTekx: the capture mode reasons of 8.x (a90e5c512).
        INPUT_EMPTY(MachineLang.TOOLTIP_INPUT_EMPTY),
        OUTPUT_FULL(MachineLang.TOOLTIP_OUTPUT_FULL),
        NONE(Component.literal("NONE"));

        private final MutableComponent component;

        SpawnerBlockedReason(MutableComponent component) {
            this.component = component;
        }

        public MutableComponent getComponent() {
            return component;
        }
    }
}
