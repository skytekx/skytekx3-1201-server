package twilightforest.world.components.feature.trees;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import twilightforest.init.TFFeatures;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class RedCanopyMushroomFeature extends CanopyMushroomFeature {
    public static final int VANILLA_CAP = 0;
    public static final int SMOOTH_CAP = 1;
    public static final int SPHEROID_CAP = 2;
    public static final int FLAT_CAP = 3;

    /**
     * The cap style is fixed per Feature instance instead of being rolled into a field in place(), which raced between worldgen threads.
     * The 33/33/33/1 roll moved into the twilightforest:weighted_list configured feature mushroom/red_canopy_mushroom:
     * 0: Vanilla, 33% Chance, styled big red mushroom cap,
     * 1: Smooth, 33% Chance, kinda like the ones on a mushroom castle,
     * 2: Spheroid, 33% Chance, same style of cap as you'd find in TF version 1.17, for example
     * 3: Flat, 1% chance, same as the brown mushroom one, these used to be in older versions of TF
     */
    private final int enumHead;

    public RedCanopyMushroomFeature(Codec<HugeMushroomFeatureConfiguration> featureConfigurationCodec, int enumHead) {
        super(featureConfigurationCodec);
        this.enumHead = enumHead;
    }

    /**
     * Backs the legacy twilightforest:canopy_red_mushroom feature id that 4.3 placed directly and that datapacks may still reference.
     * It rolls the cap style per placement with the old 33/33/33/1 odds and delegates to the matching fixed-cap feature,
     * so nothing is stored on the Feature instance that every worldgen thread shares.
     */
    public static class AnyCap extends Feature<HugeMushroomFeatureConfiguration> {
        public AnyCap(Codec<HugeMushroomFeatureConfiguration> featureConfigurationCodec) {
            super(featureConfigurationCodec);
        }

        @Override
        public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> context) {
            int altHeads = context.random().nextInt(100) + 1; //Roll the dice on the style of cap
            Feature<HugeMushroomFeatureConfiguration> variant;
            if (altHeads <= 33) {
                variant = TFFeatures.CANOPY_RED_VANILLA_MUSHROOM.get();
            } else if (altHeads <= 66) {
                variant = TFFeatures.CANOPY_RED_SMOOTH_MUSHROOM.get();
            } else if (altHeads <= 99) {
                variant = TFFeatures.CANOPY_RED_SPHEROID_MUSHROOM.get();
            } else {
                variant = TFFeatures.CANOPY_RED_FLAT_MUSHROOM.get();
            }
            return variant.place(context);
        }
    }

    @Override
    protected int getTreeHeight(RandomSource random) {
        return super.getTreeHeight(random) + 3;
    }

    @Override
    protected int getBranches(RandomSource random) {
        return 3;
    }

    @Override
    protected double getLength(RandomSource random) {
        return 10 + random.nextInt(2);
    }

    @Override
    protected void makeCap(LevelAccessor levelAccessor, RandomSource random, BlockPos pos, int height, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration featureConfiguration) {
        if (this.enumHead == VANILLA_CAP) {
            this.makeVanillaCap(levelAccessor, random, pos, height, mutableBlockPos, featureConfiguration);
        } else if (this.enumHead == SMOOTH_CAP) {
            this.makeSmoothCap(levelAccessor, random, pos, height, mutableBlockPos, featureConfiguration);
        } else if (this.enumHead == SPHEROID_CAP) {
            this.makeSpheroidCap(levelAccessor, random, pos, height, mutableBlockPos, featureConfiguration);
        } else super.makeCap(levelAccessor, random, pos, height, mutableBlockPos, featureConfiguration);
    }

    //Pretty much a 1:1 vanilla copy of the big red mushroom cap code
    protected void makeVanillaCap(LevelAccessor levelAccessor, RandomSource random, BlockPos pos, int height, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration featureConfiguration) {
        for(int y = height - 3; y <= height; ++y) {
            int j = y < height ? featureConfiguration.foliageRadius : featureConfiguration.foliageRadius - 1;
            int k = featureConfiguration.foliageRadius - 2;

            for(int x = -j; x <= j; ++x) {
                for(int z = -j; z <= j; ++z) {
                    boolean xIsMin = x == -j;
                    boolean xIsMax = x == j;
                    boolean zIsMin = z == -j;
                    boolean zIsMax = z == j;
                    boolean xMinMax = xIsMin || xIsMax;
                    boolean zMinMax = zIsMin || zIsMax;
                    if (y >= height || xMinMax != zMinMax) {
                        mutableBlockPos.setWithOffset(pos, x, y, z);
                        if (this.isReplaceable(levelAccessor, mutableBlockPos)) {
                            BlockState blockstate = featureConfiguration.capProvider.getState(random, pos);

                            if (blockstate.hasProperty(HugeMushroomBlock.WEST) && blockstate.hasProperty(HugeMushroomBlock.EAST) && blockstate.hasProperty(HugeMushroomBlock.NORTH) && blockstate.hasProperty(HugeMushroomBlock.SOUTH) && blockstate.hasProperty(HugeMushroomBlock.UP)) {
                                blockstate = blockstate
                                        .setValue(HugeMushroomBlock.UP, y >= height - 1)
                                        .setValue(HugeMushroomBlock.WEST, x < -k)
                                        .setValue(HugeMushroomBlock.EAST, x > k)
                                        .setValue(HugeMushroomBlock.NORTH, z < -k)
                                        .setValue(HugeMushroomBlock.SOUTH, z > k);
                            }

                            this.setBlock(levelAccessor, mutableBlockPos, blockstate);
                        }
                    }
                }
            }
        }
    }

    protected void makeSmoothCap(LevelAccessor levelAccessor, RandomSource random, BlockPos pos, int height, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration featureConfiguration) {
        for(int y = height - 2; y <= height + 1; ++y) {
            int j = featureConfiguration.foliageRadius - Math.max(0, y - (height - 1)) + 1;

            for(int x = -j; x <= j; ++x) {
                for(int z = -j; z <= j; ++z) {
                    if (isInsideSmoothShape(height, j, x, y, z)) {
                        mutableBlockPos.setWithOffset(pos, x, y, z);
                        if (this.isReplaceable(levelAccessor, mutableBlockPos)) {
                            BlockState blockstate = featureConfiguration.capProvider.getState(random, pos);

                            if (blockstate.hasProperty(HugeMushroomBlock.WEST) && blockstate.hasProperty(HugeMushroomBlock.EAST) && blockstate.hasProperty(HugeMushroomBlock.NORTH) && blockstate.hasProperty(HugeMushroomBlock.SOUTH) && blockstate.hasProperty(HugeMushroomBlock.UP)) {
                                blockstate = blockstate
                                        .setValue(HugeMushroomBlock.UP, !isInsideSmoothShape(height, j - (y > height - 2 ? 1 : 0), x, y + 1, z))
                                        .setValue(HugeMushroomBlock.WEST, !isInsideSmoothShape(height, j, x - 1, y, z) && x < 0)
                                        .setValue(HugeMushroomBlock.EAST, !isInsideSmoothShape(height, j, x + 1, y, z) && x > 0)
                                        .setValue(HugeMushroomBlock.NORTH, !isInsideSmoothShape(height, j, x, y, z - 1) && z < 0)
                                        .setValue(HugeMushroomBlock.SOUTH, !isInsideSmoothShape(height, j, x, y, z + 1) && z > 0);
                            }

                            this.setBlock(levelAccessor, mutableBlockPos, blockstate);
                        }
                    }
                }
            }
        }
    }

    /**
     * Don't know if there's a cleaner way of doing this? Feel free to try, but this works for now.
     * Most of this code is just for the blockStates to be correct, this would be a lot shorter otherwise and wouldn't be a method.
     */
    private static boolean isInsideSmoothShape(int height, int j, int x, int y, int z) {
        int i = y - (height - 2);
        if (i == 4 || Math.abs(x) > j || Math.abs(z) > j) return false;
        if (i >= 2) return true;

        boolean xIsMin = x == -j;
        boolean xIsMax = x == j;
        boolean zIsMin = z == -j;
        boolean zIsMax = z == j;
        boolean xMinMax = xIsMin || xIsMax;
        boolean zMinMax = zIsMin || zIsMax;

        if (i == 1 && ((xMinMax && Math.abs(z) == j - 1) || (zMinMax && Math.abs(x) == j - 1))) return false;

        return xMinMax != zMinMax || (Math.abs(x) == Math.abs(z) && Math.abs(x) == j - 1);
    }

    protected void makeSpheroidCap(LevelAccessor levelAccessor, RandomSource random, BlockPos pos, int height, BlockPos.MutableBlockPos mutableBlockPos, HugeMushroomFeatureConfiguration featureConfiguration) {
        for(int y = height - 2; y <= height; ++y) {
            int j = y == height - 1 ? featureConfiguration.foliageRadius + 2 : featureConfiguration.foliageRadius + 1;

            for(int x = -j; x <= j; ++x) {
                for(int z = -j; z <= j; ++z) {
                    double distance = Math.sqrt(x * x + z * z);
                    double maxDistance = (double)j + 0.1D;
                    if (distance <= maxDistance) {
                        mutableBlockPos.setWithOffset(pos, x, y, z);
                        if (this.isReplaceable(levelAccessor, mutableBlockPos)) {
                            BlockState blockstate = featureConfiguration.capProvider.getState(random, pos);

                            if (blockstate.hasProperty(HugeMushroomBlock.WEST) && blockstate.hasProperty(HugeMushroomBlock.EAST) && blockstate.hasProperty(HugeMushroomBlock.NORTH) && blockstate.hasProperty(HugeMushroomBlock.SOUTH) && blockstate.hasProperty(HugeMushroomBlock.UP)) {
                                blockstate = blockstate
                                        .setValue(HugeMushroomBlock.UP, y > height - 2 && (y == height || distance > maxDistance - 1D))
                                        .setValue(HugeMushroomBlock.WEST,  Math.sqrt((x - 1) * (x - 1) + z * z) > maxDistance)
                                        .setValue(HugeMushroomBlock.EAST, Math.sqrt((x + 1) * (x + 1) + z * z) > maxDistance)
                                        .setValue(HugeMushroomBlock.NORTH, Math.sqrt(x * x + (z - 1) * (z - 1)) > maxDistance)
                                        .setValue(HugeMushroomBlock.SOUTH, Math.sqrt(x * x + (z + 1) * (z + 1)) > maxDistance);
                            }

                            this.setBlock(levelAccessor, mutableBlockPos, blockstate);
                        }
                    }
                }
            }
        }
    }
}
