/*
 * This file ("NetherWartFarmerBehavior.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.misc.apiimpl.farmer;

import de.ellpeck.actuallyadditions.api.farmer.FarmerResult;
import de.ellpeck.actuallyadditions.api.farmer.IFarmerBehavior;
import de.ellpeck.actuallyadditions.api.internal.IFarmer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;

import java.util.List;

public class NetherWartFarmerBehavior implements IFarmerBehavior {

    @Override
    public FarmerResult tryPlantSeed(ItemStack seed, Level world, BlockPos pos, IFarmer farmer) {
        int use = 500;
        // SkyTekx: the check of 124cac11. The base cast the item Items.NETHER_WART to IPlantable,
        // which it is not, so a nether wart in a seed slot threw at every try. The block is the
        // IPlantable, and a soil that the wart survives on counts too
        if (farmer.getEnergy() >= use && seed.is(Items.NETHER_WART)) {
            BlockState belowState = world.getBlockState(pos.below());
            BlockState wartState = Blocks.NETHER_WART.defaultBlockState();
            if (belowState.canSustainPlant(world, pos.below(), Direction.UP, (IPlantable) Blocks.NETHER_WART) || wartState.canSurvive(world, pos)) {
                world.setBlock(pos, wartState, 2);
                farmer.extractEnergy(use);
                return FarmerResult.SUCCESS;
            }
        }
        return FarmerResult.FAIL;
    }

    @Override
    public FarmerResult tryHarvestPlant(ServerLevel world, BlockPos pos, IFarmer farmer) {
        int use = 500;
        if (farmer.getEnergy() >= use) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof NetherWartBlock) {
                if (state.getValue(BlockStateProperties.AGE_3) >= 3) {
                    List<ItemStack> drops = state.getDrops(new LootParams.Builder(world)
                        .withParameter(LootContextParams.ORIGIN, new Vec3(pos.getX(), pos.getY(), pos.getZ()))
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY));
                    if (!drops.isEmpty()) {
                        boolean toInput = farmer.canAddToSeeds(drops);
                        if (toInput || farmer.canAddToOutput(drops)) {
                            world.levelEvent(2001, pos, Block.getId(state));
                            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

                            if (toInput) {
                                farmer.addToSeeds(drops);
                            } else {
                                farmer.addToOutput(drops);
                            }

                            farmer.extractEnergy(use);
                            return FarmerResult.SUCCESS;
                        }
                    }
                }
                return FarmerResult.FAIL;
            }
        }
        return FarmerResult.FAIL;
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
