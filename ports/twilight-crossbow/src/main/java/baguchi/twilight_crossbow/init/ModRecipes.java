package baguchi.twilight_crossbow.init;

import baguchi.twilight_crossbow.TwilightCrossBow;
import baguchi.twilight_crossbow.recipe.RebuildSmithingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TwilightCrossBow.MODID);
    // Upstream also creates an empty RecipeType register; it never registers a type (the recipe is a vanilla minecraft:smithing recipe), so it is left out.

    public static final RegistryObject<RecipeSerializer<RebuildSmithingRecipe>> REBUILD_TEMPLATE_SMITHING_SERIALIZER = RECIPE_SERIALIZERS.register("rebuild_template_smithing", RebuildSmithingRecipe.Serializer::new);

}
