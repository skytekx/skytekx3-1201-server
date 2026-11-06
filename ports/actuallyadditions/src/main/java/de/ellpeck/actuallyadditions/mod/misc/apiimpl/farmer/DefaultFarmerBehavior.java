/*
 * This file ("DefaultFarmerBehavior.java") is part of the Actually Additions mod for Minecraft.
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
import de.ellpeck.actuallyadditions.mod.util.AAFakePlayers;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.List;

public class DefaultFarmerBehavior implements IFarmerBehavior {

    // SkyTekx: soil that already holds the plant stays as it is, any FarmBlock as in b66af844 and,
    // through Forge's canSustainPlant, modded farmland that is no FarmBlock. Dirt is tilled with the
    // hoe item as in b26c47dd (#1522), so a modded soil turns into its own farmland (the rich soil of
    // Farmer's Delight into rich soil farmland) instead of vanilla farmland. The hoe plays the till
    // sound, and the energy is only used when the soil changed
    public static boolean defaultPlant(Level world, BlockPos pos, BlockState toPlant, IFarmer farmer, int use) {
        if (toPlant != null) {
            BlockPos farmland = pos.below();
            BlockState targetBlockstate = world.getBlockState(farmland);
            boolean sustains = toPlant.getBlock() instanceof IPlantable plantable && targetBlockstate.canSustainPlant(world, farmland, Direction.UP, plantable);
            if (!sustains && !(targetBlockstate.getBlock() instanceof FarmBlock) && (targetBlockstate.is(BlockTags.DIRT) || targetBlockstate.is(Blocks.GRASS_BLOCK))) {
                world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                if (useHoeAt(world, farmland).consumesAction()) {
                    farmer.extractEnergy(use);
                }
            }

            if (tryPlant(toPlant, world, pos)) {
                farmer.extractEnergy(use);
                return true;
            }
        }
        return false;
    }

    private static boolean tryPlant(BlockState toPlant, Level world, BlockPos pos) {
        if (toPlant.canSurvive(world, pos)) {
            // SkyTekx: the place sound of the plant, as in bfd8eb8f
            SoundType sound = toPlant.getSoundType(world, pos, null);
            world.setBlockAndUpdate(pos, toPlant);
            world.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            return true;
        }
        return false;
    }

    @Override
    public FarmerResult tryPlantSeed(ItemStack seed, Level world, BlockPos pos, IFarmer farmer) {
        int use = 350;
        if (farmer.getEnergy() >= use * 2) {
            BlockState toPlant = this.getPlantablePlantFromStack(seed, world, pos);
            if (toPlant == null && isPlantable(seed)) {
                // SkyTekx: the fallback of b23ba8f0 (#1464) for a seed or crop item whose block is no
                // IPlantable. Forge's IPlantable already covers potatoes and carrots on 1.20.1
                toPlant = ((BlockItem) seed.getItem()).getBlock().defaultBlockState();
            }
            if (defaultPlant(world, pos, toPlant, farmer, use)) {
                return FarmerResult.SUCCESS;
            }
        }
        return FarmerResult.FAIL;
    }

    private static boolean isPlantable(ItemStack seed) {
        if (seed.is(Tags.Items.SEEDS) && seed.getItem() instanceof BlockItem) {
            return true;
        } else return seed.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CropBlock;
    }

    @Override
    public FarmerResult tryHarvestPlant(ServerLevel world, BlockPos pos, IFarmer farmer) {
        int use = 250;
        if (farmer.getEnergy() >= use) {
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            if (block instanceof CropBlock) {
                if (((CropBlock) block).isMaxAge(state)) {
                    return this.doFarmerStuff(state, world, pos, farmer);
                }
            }
            else if (state.hasProperty(BlockStateProperties.AGE_7)) {
                if (state.getValue(BlockStateProperties.AGE_7) >= 7 && !(block instanceof StemBlock)) {
                    return this.doFarmerStuff(state, world, pos, farmer);
                }
            }
        }
        return FarmerResult.FAIL;
    }

    private FarmerResult doFarmerStuff(BlockState state, ServerLevel serverLevel, BlockPos pos, IFarmer farmer) {
        List<ItemStack> seeds = new ArrayList<>();
        List<ItemStack> other = new ArrayList<>();
        List<ItemStack> drops = state.getDrops((new LootParams.Builder(serverLevel))
                .withParameter(LootContextParams.ORIGIN, new Vec3(pos.getX(), pos.getY(), pos.getZ()))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY));
        if (drops.isEmpty())
            return FarmerResult.FAIL;
        for (ItemStack stack : drops) {
            // SkyTekx: the seeds of 690aa4bc go back into the seed slots too
            if (this.getPlantableFromStack(stack) != null || isPlantable(stack)) {
                seeds.add(stack);
            } else {
                other.add(stack);
            }
        }

        boolean addSeeds = true;
        if (!farmer.canAddToSeeds(seeds)) {
            other.addAll(seeds);
            addSeeds = false;
        }

        if (farmer.canAddToOutput(other)) {
            farmer.addToOutput(other);

            if (addSeeds) {
                farmer.addToSeeds(seeds);
            }

            serverLevel.levelEvent(2001, pos, Block.getId(state));
            serverLevel.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

            farmer.extractEnergy(250);
            return FarmerResult.SUCCESS;
        }
        return FarmerResult.FAIL;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    private BlockState getPlantablePlantFromStack(ItemStack stack, Level world, BlockPos pos) {
        if (StackUtil.isValid(stack)) {
            IPlantable plantable = this.getPlantableFromStack(stack);
            if (plantable != null) {
                BlockState state = plantable.getPlant(world, pos);
                if (state != null && state.getBlock() instanceof BonemealableBlock) {
                    return state;
                }
            }
        }
        return null;
    }

    private IPlantable getPlantableFromStack(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof IPlantable) {
            return (IPlantable) item;
        } else if (item instanceof BlockItem) {
            Block block = Block.byItem(item);
            if (block instanceof IPlantable) {
                return (IPlantable) block;
            }
        }
        return null;
    }

    private static ItemStack hoe = ItemStack.EMPTY;

    private static ItemStack getHoeStack() {
        if (hoe.isEmpty()) {
            hoe = new ItemStack(Items.DIAMOND_HOE);
            // SkyTekx: the hoe item now tills, and it would wear out and break after 1561 uses
            hoe.getOrCreateTag().putBoolean("Unbreakable", true);
        }
        return hoe;
    }

    public static InteractionResult useHoeAt(Level world, BlockPos pos) {

        // SkyTekx: the mod's own fake player of AAFakePlayers, standing at the soil it tills
        try (AAFakePlayers.Use use = AAFakePlayers.use((ServerLevel) world, pos.above())) {
            Player player = use.player();

            ItemStack itemstack = getHoeStack();

            if (!player.mayUseItemAt(pos.relative(Direction.UP), Direction.UP, itemstack)) {
                return InteractionResult.FAIL;
            } else {
//                UseOnContext dummyContext = new UseOnContext(world, player, InteractionHand.MAIN_HAND, itemstack, new BlockHitResult(new Vec3(0.5, 0.5, 0.5), Direction.UP, pos, false));
//                int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(dummyContext);
//                if (hook != 0) {
//                    return hook > 0
//                        ? InteractionResult.SUCCESS
//                        : InteractionResult.FAIL;
//                } TODO: Fire event for hoe use?

                if (world.isEmptyBlock(pos.above())) {
                    BlockState state = world.getBlockState(pos);
                    // SkyTekx: till with the hoe item as in b26c47dd (#1522). Forge's getToolModifiedState
                    // asks the block, so modded soil keeps its own farmland, and the tool modification
                    // event is posted. The base set vanilla farmland on every dirt
                    if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT_PATH) || state.is(BlockTags.DIRT)) {
                        UseOnContext context = new UseOnContext(world, player, InteractionHand.MAIN_HAND, itemstack, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                        return itemstack.useOn(context);
                    }
                }
                return InteractionResult.PASS;
            }
        }
    }
}
