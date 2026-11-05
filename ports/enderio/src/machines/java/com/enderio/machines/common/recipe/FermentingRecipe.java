package com.enderio.machines.common.recipe;

import com.enderio.EnderIO;
import com.enderio.core.common.recipes.OutputStack;
import com.enderio.machines.common.blockentity.VatBlockEntity;
import com.enderio.machines.common.datamap.VatReagent;
import com.enderio.machines.common.init.MachineRecipes;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A Vat recipe: a fluid and two reagents ferment into another fluid. Ported from Ender IO 8.x (1cb2e1420, with the
 * fixes of 9aff4169f and e4645aa88). The reagents may sit in either slot. The output amount is multiplied by the
 * {@link VatReagent} factor of each reagent item.
 */
public class FermentingRecipe implements MachineRecipe<FermentingRecipe.Container> {

    private final ResourceLocation id;
    private final FluidInput input;
    private final TagKey<Item> firstReagent;
    private final TagKey<Item> secondReagent;
    private final FluidStack output;
    private final int ticks;

    public FermentingRecipe(ResourceLocation id, FluidInput input, TagKey<Item> firstReagent, TagKey<Item> secondReagent, FluidStack output, int ticks) {
        this.id = id;
        this.input = input;
        this.firstReagent = firstReagent;
        this.secondReagent = secondReagent;
        this.output = output;
        this.ticks = ticks;
    }

    public FluidInput getInput() {
        return input;
    }

    public TagKey<Item> getFirstReagent() {
        return firstReagent;
    }

    public TagKey<Item> getSecondReagent() {
        return secondReagent;
    }

    /**
     * @return the output for a multiplier of 1, do not modify.
     */
    public FluidStack getOutput() {
        return output;
    }

    public int getTicks() {
        return ticks;
    }

    @Override
    public int getBaseEnergyCost() {
        return 0;
    }

    @Override
    public List<OutputStack> craft(Container container, RegistryAccess registryAccess) {
        return List.of(OutputStack.of(getOutputFor(container.getItem(0), container.getItem(1), true)));
    }

    /**
     * The output for the given reagents, which may sit in either order.
     *
     * @param server true on the server, which reads the datapack values. Screens pass false and read what the server sent.
     */
    public FluidStack getOutputFor(ItemStack firstInput, ItemStack secondInput, boolean server) {
        // Use the modifier of each item for the reagent it fills (e4645aa88).
        double modifier;
        if (firstInput.is(firstReagent) && secondInput.is(secondReagent)) {
            modifier = getModifier(firstInput, firstReagent, server) * getModifier(secondInput, secondReagent, server);
        } else {
            modifier = getModifier(secondInput, firstReagent, server) * getModifier(firstInput, secondReagent, server);
        }

        // SkyTekx: upstream lets the multipliers push the output past the 8000 mB output tank (cloud seed is 9000 mB with
        // packed ice and 81000 mB with blue ice), and the recipe then waits forever for room. Cap it at the tank size.
        long amount = (long) (output.getAmount() * modifier);
        int capped = (int) Math.max(0, Math.min(amount, VatBlockEntity.TANK_CAPACITY));
        return new FluidStack(output.getFluid(), capped);
    }

    @Override
    public List<OutputStack> getResultStacks(RegistryAccess registryAccess) {
        return List.of(OutputStack.of(output.copy()));
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (!input.testWithAmount(container.getInputFluid())) {
            return false;
        }

        ItemStack firstInput = container.getItem(0);
        ItemStack secondInput = container.getItem(1);

        // Order independent check (e4645aa88).
        return (firstInput.is(firstReagent) && secondInput.is(secondReagent)) || (firstInput.is(secondReagent) && secondInput.is(firstReagent));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, Ingredient.of(firstReagent), Ingredient.of(secondReagent));
    }

    /**
     * The multiplier of the item for the reagent tag, 1 when the data map gives none.
     * Screens and JEI read the values the server sent.
     */
    public static double getModifier(ItemStack stack, TagKey<Item> reagent) {
        return getModifier(stack, reagent, false);
    }

    private static double getModifier(ItemStack stack, TagKey<Item> reagent, boolean server) {
        Map<TagKey<Item>, Double> map = server ? VatReagent.DATA_MAP.getServerValue(stack.getItem()) : VatReagent.DATA_MAP.get(stack.getItem());
        if (map != null) {
            return map.getOrDefault(reagent, 1D);
        }
        return 1;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return MachineRecipes.VAT_FERMENTING.serializer().get();
    }

    @Override
    public RecipeType<?> getType() {
        return MachineRecipes.VAT_FERMENTING.type().get();
    }

    /**
     * The two reagent slots and the fluid in the input tank. The fluid comes from a supplier at every check, so the
     * recipe never holds on to a tank object (9aff4169f). MachineFluidHandler replaces its tanks when it loads.
     */
    public static class Container extends RecipeWrapper {

        private final Supplier<FluidStack> inputFluid;

        public Container(IItemHandlerModifiable inv, Supplier<FluidStack> inputFluid) {
            super(inv);
            this.inputFluid = inputFluid;
        }

        public FluidStack getInputFluid() {
            return inputFluid.get();
        }
    }

    public static class Serializer implements RecipeSerializer<FermentingRecipe> {

        @Override
        public FermentingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            FluidInput input = FluidInput.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            // The upstream field names, kept for data compatibility with 8.x.
            TagKey<Item> firstReagent = TagKey.create(Registries.ITEM, new ResourceLocation(GsonHelper.getAsString(json, "left_reagent")));
            TagKey<Item> secondReagent = TagKey.create(Registries.ITEM, new ResourceLocation(GsonHelper.getAsString(json, "right_reagent")));

            JsonObject outputJson = GsonHelper.getAsJsonObject(json, "output");
            ResourceLocation fluidId = new ResourceLocation(GsonHelper.getAsString(outputJson, "fluid"));
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
            if (fluid == null || fluid == Fluids.EMPTY) {
                throw new JsonSyntaxException("Unknown output fluid '" + fluidId + "'");
            }

            int amount = GsonHelper.getAsInt(outputJson, "amount");
            if (amount <= 0) {
                throw new JsonSyntaxException("The output amount must be positive, got " + amount);
            }

            int ticks = GsonHelper.getAsInt(json, "ticks");
            if (ticks <= 0) {
                throw new JsonSyntaxException("The recipe needs a positive number of ticks, got " + ticks);
            }

            return new FermentingRecipe(recipeId, input, firstReagent, secondReagent, new FluidStack(fluid, amount), ticks);
        }

        @Override
        public @Nullable FermentingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            try {
                FluidInput input = FluidInput.fromNetwork(buffer);
                TagKey<Item> firstReagent = TagKey.create(Registries.ITEM, buffer.readResourceLocation());
                TagKey<Item> secondReagent = TagKey.create(Registries.ITEM, buffer.readResourceLocation());
                FluidStack output = FluidStack.readFromPacket(buffer);
                int ticks = buffer.readVarInt();
                return new FermentingRecipe(recipeId, input, firstReagent, secondReagent, output, ticks);
            } catch (Exception ex) {
                EnderIO.LOGGER.error("Error reading fermenting recipe from packet.", ex);
                throw ex;
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FermentingRecipe recipe) {
            try {
                recipe.input.toNetwork(buffer);
                buffer.writeResourceLocation(recipe.firstReagent.location());
                buffer.writeResourceLocation(recipe.secondReagent.location());
                recipe.output.writeToPacket(buffer);
                buffer.writeVarInt(recipe.ticks);
            } catch (Exception ex) {
                EnderIO.LOGGER.error("Error writing fermenting recipe to packet.", ex);
                throw ex;
            }
        }
    }
}
