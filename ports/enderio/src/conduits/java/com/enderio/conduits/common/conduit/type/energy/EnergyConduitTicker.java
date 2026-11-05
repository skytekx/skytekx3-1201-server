package com.enderio.conduits.common.conduit.type.energy;

import com.enderio.api.conduit.ColoredRedstoneProvider;
import com.enderio.api.conduit.ConduitGraph;
import com.enderio.api.conduit.ConduitNode;
import com.enderio.api.conduit.ConduitType;
import com.enderio.conduits.common.conduit.ConduitGraphObject;
import com.enderio.api.conduit.ticker.CapabilityAwareConduitTicker;
import com.enderio.conduits.common.tag.ConduitTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EnergyConduitTicker extends CapabilityAwareConduitTicker<EnergyConduitData, IEnergyStorage> {

    public EnergyConduitTicker() {
    }

    @Override
    public void tickGraph(
        ServerLevel level,
        ConduitType<EnergyConduitData> type,
        List<ConduitNode<EnergyConduitData>> loadedNodes,
        ConduitGraph<EnergyConduitData> graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

        super.tickGraph(level, type, loadedNodes, graph, coloredRedstoneProvider);

        // The insert targets are the same for every buffer, so look them up once, and only when a buffer holds energy.
        List<IEnergyStorage> bufferInserts = null;

        for (ConduitNode<EnergyConduitData> node : loadedNodes) {
            EnergyConduitData energyConduitData = node.getConduitData();
            IEnergyStorage energy = energyConduitData.getSelfCap()
                .resolve()
                .orElseThrow();
            if (energy.getEnergyStored() == 0) {
                energyConduitData.setCapacity(500);
                continue;
            }

            if (bufferInserts == null) {
                bufferInserts = findBufferInserts(level, loadedNodes);
            }

            int previousStored = energy.getEnergyStored();
            for (IEnergyStorage insert : bufferInserts) {
                extractEnergy(energy, List.of(List.of(insert)), 0, i -> {});
            }

            if (energy.getEnergyStored() == 0) {
                if (previousStored == energy.getMaxEnergyStored()) {
                    energyConduitData.setCapacity(Math.min(1_000_000_000, 2 * energyConduitData.getCapacity()));
                } else if (previousStored < energyConduitData.getCapacity() / 2) {
                    energyConduitData.setCapacity(Math.max(500, energyConduitData.getCapacity() / 2));
                }
            } else if (energy.getEnergyStored() > 0) {
                energyConduitData.setCapacity(Math.max(500, energy.getEnergyStored()));
            }
        }
    }

    /**
     * Every insert side of the loaded nodes, in node and direction order, then sorted by priority, highest first.
     * The sort is stable, so sides of one priority keep the order of 6.x.
     */
    private List<IEnergyStorage> findBufferInserts(ServerLevel level, List<ConduitNode<EnergyConduitData>> loadedNodes) {
        List<PrioritizedInsert<IEnergyStorage>> inserts = new ArrayList<>();
        for (ConduitNode<EnergyConduitData> otherNode : loadedNodes) {
            for (Direction dir: Direction.values()) {
                if (otherNode.getIOState(dir).map(ConduitGraphObject.IOState::isInsert).orElse(false)) {
                    BlockEntity be = level.getBlockEntity(otherNode.getPos().relative(dir));
                    if (be == null) {
                        continue;
                    }
                    Optional<IEnergyStorage> capability = be.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).resolve();
                    capability.ifPresent(insert -> inserts.add(new PrioritizedInsert<>(insert, otherNode.getConduitData().getPriority(dir))));
                }
            }
        }

        inserts.sort(PRIORITY_ORDER);
        return inserts.stream().map(PrioritizedInsert::target).toList();
    }

    @Override
    public void tickCapabilityGraph(
        ServerLevel level,
        ConduitType<EnergyConduitData> type,
        List<CapabilityConnection> inserts,
        List<CapabilityConnection> extracts,
        ConduitGraph<EnergyConduitData> graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

        List<List<IEnergyStorage>> insertGroups = groupByPriority(inserts.stream()
            .map(con -> new PrioritizedInsert<>(con.capability, con.data.castTo(EnergyConduitData.class).getPriority(con.direction)))
            .toList());

        for (CapabilityConnection extract : extracts) {
            IEnergyStorage extractHandler = extract.capability;

            EnergyConduitData.EnergySidedData sidedExtractData = extract.data.castTo(EnergyConduitData.class).compute(extract.direction);
            extractEnergy(extractHandler, insertGroups,
                sidedExtractData.rotatingIndex, i -> sidedExtractData.rotatingIndex = i);
        }
    }

    /**
     * Split the inserts into groups of one priority, highest priority first. Inside a group the inserts keep their order.
     */
    private static <T> List<List<T>> groupByPriority(List<PrioritizedInsert<T>> inserts) {
        List<PrioritizedInsert<T>> sorted = new ArrayList<>(inserts);
        sorted.sort(PRIORITY_ORDER);

        List<List<T>> groups = new ArrayList<>();
        List<T> group = null;
        int groupPriority = 0;
        for (PrioritizedInsert<T> insert : sorted) {
            if (group == null || insert.priority() != groupPriority) {
                group = new ArrayList<>();
                groups.add(group);
                groupPriority = insert.priority();
            }
            group.add(insert.target());
        }
        return groups;
    }

    /**
     * Push the energy of the extract handler into the insert groups, one group after the other, highest priority first.
     * Inside a group the round robin of 6.x applies, with its index arithmetic unchanged. The index belongs to the group in
     * which the energy ran out, so with a single priority (all sides at 0) this does exactly what 6.x did.
     */
    private void extractEnergy(IEnergyStorage extractHandler, List<List<IEnergyStorage>> insertGroups, int startingIndex, Consumer<Integer> rotationIndexSetter) {

        int availableForExtraction = extractHandler.extractEnergy(Integer.MAX_VALUE, true);
        if (availableForExtraction <= 0) {
            return;
        }

        boolean indexInAnyGroup = false;
        for (List<IEnergyStorage> inserts : insertGroups) {
            int groupStartingIndex = startingIndex;
            if (inserts.size() <= groupStartingIndex) {
                groupStartingIndex = 0;
            } else {
                indexInAnyGroup = true;
            }

            for (int j = groupStartingIndex; j < groupStartingIndex + inserts.size(); j++) {
                int insertIndex = j % inserts.size();
                IEnergyStorage insert = inserts.get(insertIndex);

                int inserted = insert.receiveEnergy(availableForExtraction, false);
                extractHandler.extractEnergy(inserted, false);

                if (inserted == availableForExtraction) {
                    rotationIndexSetter.accept(groupStartingIndex + (insertIndex) + 1);
                    return;
                }

                availableForExtraction -= inserted;
            }
        }

        if (!indexInAnyGroup) {
            rotationIndexSetter.accept(0);
        }
    }

    private static final Comparator<PrioritizedInsert<?>> PRIORITY_ORDER = Comparator.comparingInt((PrioritizedInsert<?> insert) -> insert.priority()).reversed();

    private record PrioritizedInsert<T>(T target, int priority) {}

    /**
     * This ensures consistent behaviour for FE/t caps and more.
     * @return how often the conduit should tick. 1 is every tick, 5 is every 5th tick, so 4 times a second
     */
    @Override
    public int getTickRate() {
        return 1;
    }

    @Override
    public Capability<IEnergyStorage> getCapability() {
        return ForgeCapabilities.ENERGY;
    }

    @Override
    public boolean canConnectTo(Level level, BlockPos conduitPos, Direction direction) {
        return super.canConnectTo(level, conduitPos, direction) && !level.getBlockState(conduitPos.relative(direction)).is(ConduitTags.Blocks.ENERGY_CABLE);
    }
}
