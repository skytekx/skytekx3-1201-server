package twilightforest.init;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;
import twilightforest.item.recipe.*;

public class TFRecipes {
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, TwilightForestMod.ID);
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, TwilightForestMod.ID);

	public static final RegistryObject<RecipeSerializer<CasketRepairRecipe>> CASKET_REPAIR_RECIPE = RECIPE_SERIALIZERS.register("casket_repair_recipe", () -> new SimpleCraftingRecipeSerializer<>(CasketRepairRecipe::new));
	public static final RegistryObject<RecipeSerializer<EmperorsClothRecipe>> EMPERORS_CLOTH_RECIPE = RECIPE_SERIALIZERS.register("emperors_cloth_recipe", () -> new SimpleCraftingRecipeSerializer<>(EmperorsClothRecipe::new));
	public static final RegistryObject<RecipeSerializer<MagicMapCloningRecipe>> MAGIC_MAP_CLONING_RECIPE = RECIPE_SERIALIZERS.register("magic_map_cloning_recipe", () -> new SimpleCraftingRecipeSerializer<>(MagicMapCloningRecipe::new));
	public static final RegistryObject<RecipeSerializer<MazeMapCloningRecipe>> MAZE_MAP_CLONING_RECIPE = RECIPE_SERIALIZERS.register("maze_map_cloning_recipe", () -> new SimpleCraftingRecipeSerializer<>(MazeMapCloningRecipe::new));
	public static final RegistryObject<RecipeSerializer<MoonwormQueenRepairRecipe>> MOONWORM_QUEEN_REPAIR_RECIPE = RECIPE_SERIALIZERS.register("moonworm_queen_repair_recipe", () -> new SimpleCraftingRecipeSerializer<>(MoonwormQueenRepairRecipe::new));
	public static final RegistryObject<RecipeSerializer<UncraftingRecipe>> UNCRAFTING_SERIALIZER = RECIPE_SERIALIZERS.register("uncrafting", UncraftingRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<CrumbleRecipe>> CRUMBLE_SERIALIZER = RECIPE_SERIALIZERS.register("crumble_horn", CrumbleRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<TransformPowderRecipe>> TRANSFORMATION_SERIALIZER = RECIPE_SERIALIZERS.register("transformation_powder", TransformPowderRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<NoTemplateSmithingRecipe>> NO_TEMPLATE_SMITHING_SERIALIZER = RECIPE_SERIALIZERS.register("no_template_smithing", NoTemplateSmithingRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<DryingRecipe>> DRYING_SERIALIZER = RECIPE_SERIALIZERS.register("drying", DryingRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<EssenceRepairRecipe>> ESSENCE_REPAIR_RECIPE = RECIPE_SERIALIZERS.register("essence_repair_recipe", () -> new SimpleCraftingRecipeSerializer<>(EssenceRepairRecipe::new));
	public static final RegistryObject<RecipeSerializer<ScepterRepairRecipe>> SCEPTER_REPAIR_RECIPE = RECIPE_SERIALIZERS.register("scepter_repair", ScepterRepairRecipe.Serializer::new);

	public static final RegistryObject<RecipeType<UncraftingRecipe>> UNCRAFTING_RECIPE = RECIPE_TYPES.register("uncrafting", () -> RecipeType.simple(TwilightForestMod.prefix("uncrafting")));
	public static final RegistryObject<RecipeType<CrumbleRecipe>> CRUMBLE_RECIPE = RECIPE_TYPES.register("crumble_horn", () -> RecipeType.simple(TwilightForestMod.prefix("crumble_horn")));
	public static final RegistryObject<RecipeType<TransformPowderRecipe>> TRANSFORM_POWDER_RECIPE = RECIPE_TYPES.register("transformation_powder", () -> RecipeType.simple(TwilightForestMod.prefix("transformation_powder")));
	public static final RegistryObject<RecipeType<DryingRecipe>> DRYING_RECIPE = RECIPE_TYPES.register("drying", () -> RecipeType.simple(TwilightForestMod.prefix("drying")));
}
