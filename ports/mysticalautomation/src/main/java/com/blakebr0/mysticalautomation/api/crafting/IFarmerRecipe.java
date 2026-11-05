package com.blakebr0.mysticalautomation.api.crafting;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/**
 * Used to represent a Farmer recipe for the recipe type
 * <p>
 * SkyTekx port: a {@code Recipe<Container>} on 1.20.1, which has no RecipeInput. The container holds the seeds, the
 * soil and the crux in this order.
 */
public interface IFarmerRecipe extends Recipe<Container> {
    /**
     * The required number of growth stages this plant must go through before being harvested
     * @return the number of growth stages
     */
    int getStages();

    /**
     * The list of all possible {@link FarmerResult}s for this recipe
     * @return the possible {@link FarmerResult}s
     */
    List<FarmerResult> getResults();

    /**
     * Returns an output/rolled list of outputs as a result of a successful harvest
     * @return the rolled {@link FarmerResult}s
     */
    List<ItemStack> getRolledResults();

    /**
     * Represents a possible result item and the associated chance of it rolling
     * <p>
     * SkyTekx port: the JSON and network forms live in the serializer, since 1.20.1 recipes have no codecs
     * @param stack the result item
     * @param chance the chance of getting this item
     */
    record FarmerResult(ItemStack stack, float chance) { }
}
