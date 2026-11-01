package twilightforest.world.components.feature.trees.treeplacers;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import twilightforest.util.FeatureLogic;
import twilightforest.util.VoxelBresenhamIterator;
import twilightforest.init.TFFeatureModifiers;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BranchingTrunkPlacer extends TrunkPlacer {
    // prevent_exposed_root is optional so tree configs written against 4.3 keep loading unchanged
    public static final Codec<BranchingTrunkPlacer> CODEC = RecordCodecBuilder.create(instance ->
            trunkPlacerParts(instance).and(instance.group(
                    Codec.intRange(0, 24).fieldOf("branch_start_offset_down").forGetter(o -> o.branchDownwardOffset),
                    BranchesConfig.CODEC.fieldOf("branch_config").forGetter(o -> o.branchesConfig),
                    Codec.BOOL.fieldOf("perpendicular_branches").forGetter(o -> o.perpendicularBranches),
                    Codec.BOOL.optionalFieldOf("prevent_exposed_root", false).forGetter(o -> o.preventExposedRoot)
            )).apply(instance, BranchingTrunkPlacer::new)
    );

    private final int branchDownwardOffset;
    private final BranchesConfig branchesConfig;
    private final boolean perpendicularBranches;
    private final boolean preventExposedRoot;

    public BranchingTrunkPlacer(int baseHeight, int randomHeightA, int randomHeightB, int branchDownwardOffset, BranchesConfig branchesConfig, boolean perpendicularBranches) {
        this(baseHeight, randomHeightA, randomHeightB, branchDownwardOffset, branchesConfig, perpendicularBranches, false);
    }

    public BranchingTrunkPlacer(int baseHeight, int randomHeightA, int randomHeightB, int branchDownwardOffset, BranchesConfig branchesConfig, boolean perpendicularBranches, boolean preventExposedRoot) {
        super(baseHeight, randomHeightA, randomHeightB);
        this.branchDownwardOffset = branchDownwardOffset;
        this.branchesConfig = branchesConfig;
        this.perpendicularBranches = perpendicularBranches;
        this.preventExposedRoot = preventExposedRoot;
    }

    @Override
    protected TrunkPlacerType<BranchingTrunkPlacer> type() {
        return TFFeatureModifiers.TRUNK_BRANCHING.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader worldReader, BiConsumer<BlockPos, BlockState> worldPlacer, RandomSource random, int height, BlockPos startPos, TreeConfiguration treeConfig) {
        List<FoliagePlacer.FoliageAttachment> leafAttachments = Lists.newArrayList();

        // Sink the trunk one block into the ground when the ground next to it is open, so the root of a tree on a slope is not left hanging in the air
        if (this.preventExposedRoot) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (worldReader.isStateAtPosition(startPos.below().relative(direction), BlockBehaviour.BlockStateBase::canBeReplaced)) {
                    worldPlacer.accept(startPos.below(), treeConfig.trunkProvider.getState(random, startPos.below()));
                    break;
                }
            }
        }

        for (int y = 0; y <= height; y++) { // Keep building upwards until we cannot, and then adjust height if we run into something
            if (!placeLog(worldReader, worldPlacer, random, startPos.above(y), treeConfig)) {
                height = y;
                break;
            }
        }

        leafAttachments.add(new FoliagePlacer.FoliageAttachment(startPos.above(height), 0, false));

        int numBranches = this.branchesConfig.branchCount() + random.nextInt(this.branchesConfig.randomAddBranches() + 1);
        float offset = random.nextFloat();
        for (int b = 0; b < numBranches; b++) {
            buildBranch(worldReader, worldPlacer, startPos, leafAttachments, height - this.branchDownwardOffset + b, this.branchesConfig.length(), this.branchesConfig.spacingYaw() * b + offset, this.branchesConfig.downwardsPitch(), random, treeConfig, this.perpendicularBranches);
        }

        return leafAttachments;
    }

    private void buildBranch(LevelSimulatedReader worldReader, BiConsumer<BlockPos, BlockState> worldPlacer, BlockPos pos, List<FoliagePlacer.FoliageAttachment> leafBlocks, int height, double length, double angle, double tilt, RandomSource treeRNG, TreeConfiguration treeConfig, boolean perpendicularBranches) {
        BlockPos src = pos.above(height);
        BlockPos dest = FeatureLogic.translate(src, length, angle, tilt);

        if (perpendicularBranches) {
            drawBresenhamBranch(worldReader, worldPlacer, treeRNG, src, new BlockPos(dest.getX(), src.getY(), dest.getZ()), treeConfig);

            int max = Math.max(src.getY(), dest.getY());

            for (int i = Math.min(src.getY(), dest.getY()); i < max + 1; i++) {
                placeWood(worldReader, worldPlacer, treeRNG, new BlockPos(dest.getX(), i, dest.getZ()), treeConfig);
            }
        } else {
            drawBresenhamBranch(worldReader, worldPlacer, treeRNG, src, dest, treeConfig);
        }

        placeWood(worldReader, worldPlacer, treeRNG, dest.east(), treeConfig);
        placeWood(worldReader, worldPlacer, treeRNG, dest.west(), treeConfig);
        placeWood(worldReader, worldPlacer, treeRNG, dest.south(), treeConfig);
        placeWood(worldReader, worldPlacer, treeRNG, dest.north(), treeConfig);

        leafBlocks.add(new FoliagePlacer.FoliageAttachment(dest, 0, false));
    }

    /**
     * Draws a line from {x1, y1, z1} to {x2, y2, z2}
     * This takes all variables for setting Branch
     */
    private void drawBresenhamBranch(LevelSimulatedReader worldReader, BiConsumer<BlockPos, BlockState> worldPlacer, RandomSource random, BlockPos from, BlockPos to, TreeConfiguration config) {
        for (BlockPos pixel : new VoxelBresenhamIterator(from, to)) {
            placeWood(worldReader, worldPlacer, random, pixel, config);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    protected boolean placeWood(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos pos, TreeConfiguration config) {
        return this.placeWood(level, blockSetter, random, pos, config, Function.identity());
    }

    protected boolean placeWood(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos pos, TreeConfiguration config, Function<BlockState, BlockState> propertySetter) {
        if (this.validTreePos(level, pos)) {
            BlockStateProvider branchProvider = this.branchesConfig.branchProvider() != null ? this.branchesConfig.branchProvider() : config.trunkProvider;
            blockSetter.accept(pos, propertySetter.apply(branchProvider.getState(random, pos)));
            return true;
        } else return false;
    }
}
