/*
 * This file ("MethodHandler.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.misc.apiimpl;

import de.ellpeck.actuallyadditions.api.internal.IAtomicReconstructor;
import de.ellpeck.actuallyadditions.api.internal.IMethodHandler;
import de.ellpeck.actuallyadditions.api.lens.Lens;
import de.ellpeck.actuallyadditions.api.recipe.CoffeeIngredient;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.BlockLaserRelay;
import de.ellpeck.actuallyadditions.mod.crafting.CoffeeIngredientRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.LaserRecipe;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityAtomicReconstructor;
import de.ellpeck.actuallyadditions.mod.util.AAFakePlayers;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MethodHandler implements IMethodHandler {

    @Override
    public boolean addEffectToStack(ItemStack stack, CoffeeIngredient ingredient) {
        boolean worked = false;
        if (ingredient != null) {
            MobEffectInstance[] effects = ingredient.getEffects();
            if (effects != null && effects.length > 0) {
                for (MobEffectInstance effect : effects) {
                    MobEffectInstance effectHas = this.getSameEffectFromStack(stack, effect);
                    if (effectHas != null) {
                        if (effectHas.getAmplifier() < ingredient.getMaxAmplifier() - 1) {
                            this.addEffectProperties(stack, effect, false, true);
                            worked = true;
                        }
                    } else {
                        this.addEffectToStack(stack, effect);
                        worked = true;
                    }
                }
            }
        }
        return worked;
    }

    // SkyTekx: upstream 971fdd65, the same as above for the data driven ingredient
    @Override
    public boolean addRecipeEffectToStack(ItemStack stack, CoffeeIngredientRecipe ingredient) {
        boolean worked = false;
        if (ingredient != null) {
            List<MobEffectInstance> effects = ingredient.getEffects();
            if (!effects.isEmpty()) {
                for (MobEffectInstance effect : effects) {
                    MobEffectInstance effectHas = this.getSameEffectFromStack(stack, effect);
                    if (effectHas != null) {
                        if (effectHas.getAmplifier() < ingredient.getMaxAmplifier() - 1) {
                            this.addEffectProperties(stack, effect, false, true);
                            worked = true;
                        }
                    } else {
                        this.addEffectToStack(stack, effect);
                        worked = true;
                    }
                }
            }
        }
        return worked;
    }

    // SkyTekx: the milk of the 1.12 MilkIngredient, which upstream main dropped
    @Override
    public void weakenEffectsOnStack(ItemStack stack, int extraSeconds) {
        MobEffectInstance[] effects = this.getEffectsFromStack(stack);
        if (effects != null && effects.length > 0) {
            this.clearEffectsFromStack(stack);
            for (MobEffectInstance effect : effects) {
                if (effect.getAmplifier() > 0) {
                    this.addEffectToStack(stack, new MobEffectInstance(effect.getEffect(), effect.getDuration() + extraSeconds, effect.getAmplifier() - 1));
                }
            }
        }
    }

    @Override
    public MobEffectInstance getSameEffectFromStack(ItemStack stack, MobEffectInstance effect) {
        MobEffectInstance[] effectsStack = this.getEffectsFromStack(stack);
        if (effectsStack != null && effectsStack.length > 0) {
            for (MobEffectInstance effectStack : effectsStack) {
                if (effect.getEffect() == effectStack.getEffect()) {
                    return effectStack;
                }
            }
        }
        return null;
    }

    @Override
    public void addEffectProperties(ItemStack stack, MobEffectInstance effect, boolean addDur, boolean addAmp) {
        // SkyTekx: upstream 90cecc8e. The base wiped the whole tag of the stack and wrote every effect
        // back once per effect it found, so effects doubled with each ingredient
        MobEffectInstance[] effects = this.getEffectsFromStack(stack);
        boolean effectUpdated = false;
        if (effects != null) {
            for (int i = 0; i < effects.length; i++) {
                if (effects[i].getEffect() == effect.getEffect()) {
                    effects[i] = new MobEffectInstance(effects[i].getEffect(),
                            effects[i].getDuration() + (addDur ? effect.getDuration() : 0),
                            effects[i].getAmplifier() + (addAmp ? effect.getAmplifier() > 0 ? effect.getAmplifier() : 1 : 0));
                    effectUpdated = true;
                    break;
                }
            }
        }
        if (!effectUpdated) {
            this.addEffectToStack(stack, effect);
        } else {
            this.clearEffectsFromStack(stack);
            for (MobEffectInstance updatedEffect : effects) {
                this.addEffectToStack(stack, updatedEffect);
            }
        }
    }

    // SkyTekx: removes the effect keys only, the other NBT of the stack (its damage) stays
    private void clearEffectsFromStack(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            int counter = tag.getInt("Counter");
            for (int i = 1; i <= counter; i++) {
                tag.remove(String.valueOf(i));
            }
            tag.remove("Counter");
        }
    }

    @Override
    public void addEffectToStack(ItemStack stack, MobEffectInstance effect) {
        CompoundTag tag = stack.getOrCreateTag();

        int prevCounter = tag.getInt("Counter");
        CompoundTag compound = new CompoundTag();
        // SkyTekx: upstream 56402e46 stores the effect by its registry name
        compound.putString("ID", BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect()).toString());
        compound.putInt("Duration", effect.getDuration());
        compound.putInt("Amplifier", effect.getAmplifier());

        int counter = prevCounter + 1;
        tag.put(counter + "", compound);
        tag.putInt("Counter", counter);

        stack.setTag(tag);
    }

    @Override
    public MobEffectInstance[] getEffectsFromStack(ItemStack stack) {
        ArrayList<MobEffectInstance> effects = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return null;
        }
        int counter = tag.getInt("Counter");
        while (counter > 0) {
            CompoundTag compound = tag.getCompound(counter + "");
            String id = compound.getString("ID");
            ResourceLocation effectID = id.isEmpty() ? new ResourceLocation("speed") : ResourceLocation.tryParse(id);
            MobEffect effect = effectID == null ? null : BuiltInRegistries.MOB_EFFECT.get(effectID);
            if (effect == null) {
                ActuallyAdditions.LOGGER.error("Unable to find effect with ID: {}, defaulting to speed", effectID);
                effect = MobEffects.MOVEMENT_SPEED;
            }
            effects.add(new MobEffectInstance(effect, compound.getInt("Duration"), compound.getByte("Amplifier")));
            counter--;
        }
        return !effects.isEmpty()
            ? effects.toArray(new MobEffectInstance[0])
            : null;
    }

    @Override
    public boolean invokeConversionLens(BlockState hitState, BlockPos hitBlock, IAtomicReconstructor tile) {
        if (hitBlock != null) {
            int range = 1;
            int rangeX = 0;
            int rangeY = 0;
            int rangeZ = 0;

            Direction facing = tile.getOrientation();
            if (facing != Direction.UP && facing != Direction.DOWN) {
                rangeY = range;

                if (facing == Direction.NORTH || facing == Direction.SOUTH) {
                    rangeX = range;
                } else {
                    rangeZ = range;
                }
            } else {
                rangeX = range;
                rangeZ = range;
            }

            //Converting the Blocks
            for (int reachX = -rangeX; reachX <= rangeX; reachX++) {
                for (int reachZ = -rangeZ; reachZ <= rangeZ; reachZ++) {
                    for (int reachY = -rangeY; reachY <= rangeY; reachY++) {
                        BlockPos pos = new BlockPos(hitBlock.getX() + reachX, hitBlock.getY() + reachY, hitBlock.getZ() + reachZ);
                        if (!tile.getWorldObject().isEmptyBlock(pos)) {
                            BlockState state = tile.getWorldObject().getBlockState(pos);
                            if (state.getBlock() instanceof BlockLaserRelay) {
                                continue;
                            }
                            Optional<LaserRecipe> recipe = LaserRecipe.getRecipeForStack(new ItemStack(state.getBlock()));
                            if (recipe.isPresent() && tile.getEnergy() >= recipe.get().getEnergy()) {
                                ItemStack output = recipe.get().getResultItem(tile.getWorldObject().registryAccess()).copy();
                                // SkyTekx: a conversion into a block is a placement and one into an item a
                                // break of the named fake player at the reconstructor, each with its event.
                                // A refused block stays as it is and costs no energy
                                if (!output.isEmpty() && tile.getWorldObject() instanceof ServerLevel level) {
                                    boolean converted;
                                    if (output.getItem() instanceof BlockItem) {
                                        Block toPlace = Block.byItem(output.getItem());
                                        BlockState state2Place = toPlace.defaultBlockState(); //.getStateForPlacement(tile.getWorldObject(), pos, facing, 0, 0, 0, output.getMetadata(), FakePlayerFactory.getMinecraft((WorldServer) tile.getWorldObject()), Hand.MAIN_HAND); //TODO
                                        converted = AAFakePlayers.placeBlock(level, tile.getPosition(), pos, state2Place, 2, facing.getOpposite(), output);
                                    } else {
                                        converted = AAFakePlayers.mayBreak(level, tile.getPosition(), pos);
                                        if (converted) {
                                            ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, output.copy());
                                            level.addFreshEntity(item);
                                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                        }
                                    }

                                    if (converted) {
                                        level.levelEvent(2001, pos, Block.getId(state));
                                        tile.extractEnergy(recipe.get().getEnergy());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Converting the Items
            AABB aabb = new AABB(tile.getPosition().getX(), tile.getPosition().getY(), tile.getPosition().getZ(), hitBlock.getX() + 1, hitBlock.getY() + 1, hitBlock.getZ() + 1);
            Vec3i dir = tile.getOrientation().getNormal();
            aabb = aabb.inflate(0.02, 0.02, 0.02).expandTowards(dir.getX(), dir.getY(), dir.getZ());
            List<ItemEntity> items = tile.getWorldObject().getEntitiesOfClass(ItemEntity.class, aabb);
            for (ItemEntity item : items) {
                ItemStack stack = item.getItem();
                if (item.isAlive() && StackUtil.isValid(stack) && !item.getPersistentData().getBoolean("aa_cnv")) {
                    Optional<LaserRecipe> recipe = LaserRecipe.getRecipeForStack(stack);
                    if (recipe.isPresent()) {
                        int itemsPossible = Math.min(tile.getEnergy() / recipe.get().getEnergy(), stack.getCount());

                        if (itemsPossible > 0) {
                            //recipe.transformHook(item.getItem(), null, item.blockPosition(), tile); //TODO empty method
                            item.discard();

                            if (stack.getCount() - itemsPossible > 0) {
                                ItemStack stackCopy = stack.copy();
                                stackCopy.shrink(itemsPossible);

                                ItemEntity inputLeft = new ItemEntity(tile.getWorldObject(), item.getX(), item.getY(), item.getZ(), stackCopy);
                                tile.getWorldObject().addFreshEntity(inputLeft);
                            }

                            ItemStack outputCopy = recipe.get().getResultItem(tile.getWorldObject().registryAccess()).copy();
                            outputCopy.setCount(itemsPossible);

                            ItemEntity newItem = new ItemEntity(tile.getWorldObject(), item.getX(), item.getY(), item.getZ(), outputCopy);
                            newItem.getPersistentData().putBoolean("aa_cnv", true);
                            tile.getWorldObject().addFreshEntity(newItem);

                            tile.extractEnergy(recipe.get().getEnergy() * itemsPossible);
                            break;
                        }
                    }
                }
            }
            return !hitState.isAir();
        }
        return false;
    }

    @Override
    public boolean invokeReconstructor(IAtomicReconstructor tile) {
        if (tile.getEnergy() >= TileEntityAtomicReconstructor.ENERGY_USE) {
            Direction sideToManipulate = tile.getOrientation();
            Lens currentLens = tile.getLens();
            if (currentLens.canInvoke(tile, sideToManipulate, TileEntityAtomicReconstructor.ENERGY_USE)) {
                tile.extractEnergy(TileEntityAtomicReconstructor.ENERGY_USE);

                int distance = currentLens.getDistance();
                for (int i = 0; i < distance; i++) {
                    BlockPos hitBlock = tile.getPosition().relative(sideToManipulate, i + 1);

                    if (currentLens.invoke(tile.getWorldObject().getBlockState(hitBlock), hitBlock, tile) || i >= distance - 1) {
                        TileEntityAtomicReconstructor.shootLaser(tile, tile.getWorldObject(), tile.getX(), tile.getY(), tile.getZ(), hitBlock.getX(), hitBlock.getY(), hitBlock.getZ(), currentLens);
                        break;
                    }
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean addCrusherRecipes(List<ItemStack> inputs, List<ItemStack> outputOnes, int outputOneAmounts, List<ItemStack> outputTwos, int outputTwoAmounts, int outputTwoChance) {
        boolean hasWorkedOnce = false;
/*        for (ItemStack input : inputs) {
            if (StackUtil.isValid(input) && CrusherRecipeRegistry.getRecipeFromInput(input) == null) {
                for (ItemStack outputOne : outputOnes) {
                    if (StackUtil.isValid(outputOne) && !CrusherRecipeRegistry.hasBlacklistedOutput(outputOne, ConfigStringListValues.CRUSHER_OUTPUT_BLACKLIST.getValue())) {
                        ItemStack outputOneCopy = outputOne.copy();
                        outputOneCopy.setCount(outputOneAmounts);

                        if (outputTwos.isEmpty()) {
                            ActuallyAdditionsAPI.addCrusherRecipe(input, outputOneCopy, StackUtil.getEmpty(), 0);
                            hasWorkedOnce = true;
                        } else {
                            for (ItemStack outputTwo : outputTwos) {
                                if (StackUtil.isValid(outputTwo) && !CrusherRecipeRegistry.hasBlacklistedOutput(outputTwo, ConfigStringListValues.CRUSHER_OUTPUT_BLACKLIST.getValue())) {
                                    ItemStack outputTwoCopy = outputTwo.copy();
                                    outputTwoCopy.setCount(outputTwoAmounts);

                                    ActuallyAdditionsAPI.addCrusherRecipe(input, outputOneCopy, outputTwoCopy, outputTwoChance);
                                    hasWorkedOnce = true;
                                }
                            }
                        }
                    }
                }
            }
        }*/ //TODO
        return hasWorkedOnce;
    }

    @Override
    public boolean addCrusherRecipes(List<ItemStack> inputs, ItemStack outputOne, int outputOneAmount, ItemStack outputTwo, int outputTwoAmount, int outputTwoChance) {
        boolean hasWorkedOnce = false;
/*        for (ItemStack input : inputs) {
            if (StackUtil.isValid(input) && CrusherRecipeRegistry.getRecipeFromInput(input) == null) {
                if (StackUtil.isValid(outputOne) && !CrusherRecipeRegistry.hasBlacklistedOutput(outputOne, ConfigStringListValues.CRUSHER_OUTPUT_BLACKLIST.getValue())) {
                    ItemStack outputOneCopy = outputOne.copy();
                    outputOneCopy.setCount(outputOneAmount);

                    if (!StackUtil.isValid(outputTwo)) {
                        ActuallyAdditionsAPI.addCrusherRecipe(input, outputOneCopy, StackUtil.getEmpty(), 0);
                        hasWorkedOnce = true;
                    } else if (StackUtil.isValid(outputTwo) && !CrusherRecipeRegistry.hasBlacklistedOutput(outputTwo, ConfigStringListValues.CRUSHER_OUTPUT_BLACKLIST.getValue())) {
                        ItemStack outputTwoCopy = outputTwo.copy();
                        outputTwoCopy.setCount(outputTwoAmount);

                        ActuallyAdditionsAPI.addCrusherRecipe(input, outputOneCopy, outputTwoCopy, outputTwoChance);
                        hasWorkedOnce = true;
                    }
                }
            }
        }*/
        return hasWorkedOnce;
    }

//    @Override
//    public IBookletPage generateTextPage(int id) {
//        return this.generateTextPage(id, 0);
//    }

//    @Override
//    public IBookletPage generatePicturePage(int id, ResourceLocation resLoc, int textStartY) {
//        return this.generatePicturePage(id, resLoc, textStartY, 0);
//    }

//    @Override
//    public IBookletPage generateCraftingPage(int id, IRecipe... recipes) {
//        return this.generateCraftingPage(id, 0, recipes);
//    }

//    @Override
//    public IBookletPage generateFurnacePage(int id, ItemStack input, ItemStack result) {
//        return this.generateFurnacePage(id, input, result, 0);
//    }

//    @Override
//    public IBookletChapter generateBookletChapter(String identifier, IBookletEntry entry, ItemStack displayStack, IBookletPage... pages) {
//        return this.generateBookletChapter(identifier, entry, displayStack, 0, pages);
//    }

//    @Override
//    public IBookletPage generateTextPage(int id, int priority) {
//        return new PageTextOnly(id, priority);
//    }

//    @Override
//    public IBookletPage generatePicturePage(int id, ResourceLocation resLoc, int textStartY, int priority) {
//        return new PagePicture(id, resLoc, textStartY, priority);
//    }

//    @Override
//    public IBookletPage generateCraftingPage(int id, int priority, IRecipe... recipes) {
//        return new PageCrafting(id, priority, recipes);
//    }

//    @Override
//    public IBookletPage generateFurnacePage(int id, ItemStack input, ItemStack result, int priority) {
//        return new PageFurnace(id, result, priority);
//    }

//    @Override
//    public IBookletChapter generateBookletChapter(String identifier, IBookletEntry entry, ItemStack displayStack, int priority, IBookletPage... pages) {
//        return new BookletChapter(identifier, entry, displayStack, priority, pages);
//    }

//    @Override
//    public IBookletChapter createTrial(String identifier, ItemStack displayStack, boolean textOnSecondPage) {
//        return new BookletChapterTrials(identifier, displayStack, textOnSecondPage);
//    }
}
