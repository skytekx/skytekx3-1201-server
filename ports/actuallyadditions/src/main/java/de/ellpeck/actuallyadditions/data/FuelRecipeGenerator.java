package de.ellpeck.actuallyadditions.data;

import com.google.gson.JsonObject;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.crafting.LiquidFuelRecipe;
import de.ellpeck.actuallyadditions.mod.fluids.InitFluids;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class FuelRecipeGenerator extends RecipeProvider {
    public FuelRecipeGenerator(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    public String getName() {
        return "Fuel " + super.getName();
    }

    @Override
    protected @Nullable CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe finishedRecipe, JsonObject advancementJson) {
        return null; //Nope...
    }

    @Override
    protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        // SkyTekx: upstream d5757a14 drops the solid fuel recipes, the Coal Generator burns anything
        // with a furnace burn time. The serializer actuallyadditions:solid_fuel stays registered
        // so that data packs which still carry such recipes load
        consumer.accept(new LiquidFuelRecipe.Result(new ResourceLocation(ActuallyAdditions.MODID, "liquid_fuel/canola_oil"),
            new FluidStack(InitFluids.CANOLA_OIL.get(), 50), 4000, 100));

        consumer.accept(new LiquidFuelRecipe.Result(new ResourceLocation(ActuallyAdditions.MODID, "liquid_fuel/refined_canola_oil"),
            new FluidStack(InitFluids.REFINED_CANOLA_OIL.get(), 50), 9600, 120));

        consumer.accept(new LiquidFuelRecipe.Result(new ResourceLocation(ActuallyAdditions.MODID, "liquid_fuel/crystallized_canola_oil"),
            new FluidStack(InitFluids.CRYSTALLIZED_OIL.get(), 50), 28000, 280));

        consumer.accept(new LiquidFuelRecipe.Result(new ResourceLocation(ActuallyAdditions.MODID, "liquid_fuel/empowered_canola_oil"),
            new FluidStack(InitFluids.EMPOWERED_OIL.get(), 50), 48000, 400));
    }
}
