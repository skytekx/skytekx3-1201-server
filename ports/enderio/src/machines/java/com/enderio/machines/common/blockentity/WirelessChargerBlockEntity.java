package com.enderio.machines.common.blockentity;

import com.enderio.api.capacitor.CapacitorModifier;
import com.enderio.api.capacitor.QuadraticScalable;
import com.enderio.api.io.IIOConfig;
import com.enderio.api.io.IOMode;
import com.enderio.api.io.energy.EnergyIOMode;
import com.enderio.base.common.integrations.curios.CuriosCompat;
import com.enderio.core.common.network.slot.BooleanNetworkDataSlot;
import com.enderio.core.common.network.slot.IntegerNetworkDataSlot;
import com.enderio.machines.common.blockentity.base.PoweredMachineBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.datamap.RangeExtender;
import com.enderio.machines.common.io.energy.IMachineEnergyStorage;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.menu.WirelessChargerMenu;
import com.enderio.machines.common.tag.MachineTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * SkyTekx: the Wireless Charger of 8.x (4ad73fff0, Curios support of e65b3feb0) on the 6.x machine base. While it has
 * power and a capacitor it charges the energy items in the inventories and Curios slots of the players in its range.
 * An antenna on top raises the max range by its value in the {@code enderio:range_extender} data map.
 * <p>
 * Differences from upstream:
 * <ul>
 *     <li>The range uses the 6.x range and visibility data slots instead of the 8.x ActionRange. The client may change
 *     both, so the range is clamped to the max range on the server.</li>
 *     <li>Upstream reads the block above and sends a block update from onLoad. That is the chunk load hazard of
 *     4dd6e5e09, so the antenna is read on the first server tick and on a neighbour change, and nothing here calls
 *     setChanged or sendBlockUpdated from setLevel or onLoad. The max range reaches the client through a data
 *     slot.</li>
 *     <li>Items charge every {@code chargeInterval} ticks with the charge speed times the interval, so a charger near
 *     several players sends fewer item updates. The upkeep is still paid each tick.</li>
 *     <li>Spectators are skipped, as the 6.x electromagnet skips them.</li>
 * </ul>
 */
public class WirelessChargerBlockEntity extends PoweredMachineBlockEntity {

    public static final QuadraticScalable CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY, MachinesConfig.COMMON.ENERGY.WIRELESS_CHARGER_CAPACITY);
    public static final QuadraticScalable USAGE = new QuadraticScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.ENERGY.WIRELESS_CHARGER_USAGE);

    // The max range on the server: the base range plus the bonus of the antenna above, read on the first tick.
    private int maxRange;

    // The max range that the server synced, read by the client.
    private int clientMaxRange;

    private boolean needsRangeUpdate = true;
    private int ticksSinceCharge;

    public WirelessChargerBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(EnergyIOMode.Input, CAPACITY, USAGE, type, worldPosition, blockState);

        maxRange = getBaseRange();
        range = maxRange;

        rangeVisibleDataSlot = new BooleanNetworkDataSlot(this::isRangeVisible, b -> this.rangeVisible = b);
        addClientDataSlot(rangeVisibleDataSlot);

        // Server to client only. It goes before the range so that a sync never sees a range above the max.
        addDataSlot(new IntegerNetworkDataSlot(this::getMaxRange, r -> this.clientMaxRange = r));

        // On the server the range comes from a client, so clamp it. The client takes the server's value as it is.
        rangeDataSlot = new IntegerNetworkDataSlot(this::getRange, r -> this.range = level instanceof ServerLevel ? Mth.clamp(r, 0, getMaxRange()) : r) {
            @Override
            public void updateServerCallback() {
                setChanged();
            }
        };
        addClientDataSlot(rangeDataSlot);
    }

    private static int getBaseRange() {
        return MachinesConfig.COMMON.WIRELESS_CHARGER_RANGE.get();
    }

    @Override
    protected IIOConfig createIOConfig() {
        // The default of 8.x is pull on every side, which still lets the player change each side.
        // The level is not set yet, so setMode only stores the modes.
        IIOConfig config = super.createIOConfig();
        for (Direction side : Direction.values()) {
            config.setMode(side, IOMode.PULL);
        }

        return config;
    }

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
            .capacitor()
            .build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new WirelessChargerMenu(this, pPlayerInventory, pContainerId);
    }

    @Override
    protected boolean isActive() {
        return hasEnergy() && canAct();
    }

    @Override
    public void serverTick() {
        if (needsRangeUpdate) {
            needsRangeUpdate = false;
            updateMaxRange(false);
        }

        boolean active = isActive();
        updateMachineState(MachineState.ACTIVE, active);
        super.serverTick();

        if (active) {
            getEnergyStorage().consumeEnergy(MachinesConfig.COMMON.ENERGY.WIRELESS_CHARGER_UPKEEP.get(), false);

            int interval = MachinesConfig.COMMON.WIRELESS_CHARGER_INTERVAL.get();
            if (++ticksSinceCharge >= interval) {
                ticksSinceCharge = 0;
                chargeItems(interval);
            }
        } else {
            ticksSinceCharge = 0;
        }
    }

    /**
     * Charges the energy items of the players in range: the inventory of each player first, then the Curios slots.
     * One call hands out up to the charge speed times the interval, so the throughput matches a charge every tick.
     */
    private void chargeItems(int interval) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        IMachineEnergyStorage energyStorage = getEnergyStorage();
        int available = Math.min(energyStorage.getEnergyStored(), (int) Math.min(Integer.MAX_VALUE, (long) energyStorage.getMaxEnergyUse() * interval));
        if (available <= 0) {
            return;
        }

        AABB bounds = getBounds();
        int remaining = available;
        for (Player player : serverLevel.players()) {
            if (player.isSpectator() || !bounds.intersects(player.getBoundingBox())) {
                continue;
            }

            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize() && remaining > 0; i++) {
                remaining -= chargeStack(inventory.getItem(i), remaining);
            }

            if (remaining > 0) {
                for (ItemStack stack : CuriosCompat.getActiveCurios(player, s -> !s.isEmpty())) {
                    remaining -= chargeStack(stack, remaining);
                    if (remaining <= 0) {
                        break;
                    }
                }
            }

            if (remaining <= 0) {
                break;
            }
        }

        if (remaining < available) {
            energyStorage.takeEnergy(available - remaining);
        }
    }

    private static int chargeStack(ItemStack stack, int maxReceive) {
        if (stack.isEmpty()) {
            return 0;
        }

        IEnergyStorage storage = stack.getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        if (storage == null || !storage.canReceive()) {
            return 0;
        }

        return Math.max(0, storage.receiveEnergy(maxReceive, false));
    }

    @Override
    public void neighborChanged(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.neighborChanged(state, level, pos, neighbor);
        if (this.level != null && !this.level.isClientSide() && worldPosition.above().equals(neighbor)) {
            // An antenna was placed on top or taken away.
            updateMaxRange(true);
        }
    }

    /**
     * Reads the antenna above and keeps the range within the new max range. Called from the server tick and from
     * neighbour changes only, never from setLevel or onLoad.
     *
     * @param antennaChanged whether the block above just changed. Then, as upstream does, a range bonus moves the range
     *                       to the new max range.
     */
    private void updateMaxRange(boolean antennaChanged) {
        int extension = getRangeExtension();
        maxRange = getBaseRange() + extension;

        int newRange = range;
        if (antennaChanged && extension > 0) {
            newRange = maxRange;
        }

        newRange = Mth.clamp(newRange, 0, maxRange);
        if (newRange != range) {
            range = newRange;
            if (antennaChanged) {
                setChanged();
            }
        }
    }

    private int getRangeExtension() {
        if (level == null) {
            return 0;
        }

        BlockState above = level.getBlockState(worldPosition.above());
        if (!above.is(MachineTags.Blocks.RANGE_EXTENDER)) {
            return 0;
        }

        Map<TagKey<Block>, Integer> values = RangeExtender.DATA_MAP.getServerValue(above.getBlock());
        if (values == null) {
            return 0;
        }

        return Math.max(0, values.getOrDefault(MachineTags.Blocks.RANGE_EXTENDER, 0));
    }

    @Override
    public int getMaxRange() {
        if (level != null && level.isClientSide()) {
            return clientMaxRange;
        }

        return maxRange;
    }

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.WIRELESS_CHARGER_RANGE_COLOR.get();
    }

    /**
     * The area in which players get their items charged.
     */
    public AABB getBounds() {
        return new AABB(worldPosition).inflate(range);
    }
}
