package twilightforest.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.fml.ModList;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.client.UncraftingScreen;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.compat.jei.categories.CrumbleHornCategory;
import twilightforest.compat.jei.categories.DryingCategory;
import twilightforest.compat.jei.categories.JEIUncraftingCategory;
import twilightforest.compat.jei.categories.OminousFireCategory;
import twilightforest.compat.jei.categories.TransformationPowderCategory;
import twilightforest.compat.jei.extension.CasketRepairExtension;
import twilightforest.compat.jei.extension.ExanimateEssenceRepairExtension;
import twilightforest.compat.jei.extension.MoonwormQueenExtension;
import twilightforest.compat.jei.extension.ScepterRepairExtension;
import twilightforest.compat.jei.extension.TravellersGearModifierExtension;
import twilightforest.compat.jei.extension.TravellersVestGlovesMergeExtension;
import twilightforest.compat.jei.renderers.*;
import twilightforest.compat.jei.subtype.CasketSubtypeInterpreter;
import twilightforest.compat.jei.util.OminousFireRecipe;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.init.TFMenuTypes;
import twilightforest.init.TFRecipes;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.inventory.UncraftingMenu;
import twilightforest.item.recipe.CasketRepairRecipe;
import twilightforest.item.recipe.EssenceRepairRecipe;
import twilightforest.item.recipe.MoonwormQueenRepairRecipe;
import twilightforest.item.recipe.ScepterRepairRecipe;
import twilightforest.item.recipe.travellers.TravellersGearModifierRecipe;
import twilightforest.item.recipe.travellers.TravellersVestGlovesMergeRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JeiPlugin
@SuppressWarnings("unused")
public class JEICompat implements IModPlugin {

	public static final IIngredientType<FakeEntityType> ENTITY_TYPE = () -> FakeEntityType.class;
	public static final IIngredientType<FakeItemEntity> FAKE_ITEM_ENTITY = () -> FakeItemEntity.class;

	// The cached display entities are dropped whenever a screen closes by TFClientEvents (#2488), for EMI's widgets too

	public static boolean isEmiInstalled() {
		//Skip handling if both EMI and JEI are loaded as otherwise some things behave strangely.
		//EMI shows the Twilight Forest recipes through its own plugin (TFEmiCompat) then
		return ModList.get().isLoaded("emi");
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if (isEmiInstalled()) return;
		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get()) {
			registration.addRecipeCatalyst(new ItemStack(TFBlocks.UNCRAFTING_TABLE.get()), RecipeTypes.CRAFTING);
			registration.addRecipeCatalyst(new ItemStack(TFBlocks.UNCRAFTING_TABLE.get()), JEIUncraftingCategory.UNCRAFTING);
		}
		registration.addRecipeCatalyst(new ItemStack(TFItems.TRANSFORMATION_POWDER.get()), TransformationPowderCategory.TRANSFORMATION);
		registration.addRecipeCatalyst(new ItemStack(TFItems.EXANIMATE_ESSENCE.get()), OminousFireCategory.OMINOUS_FIRE);
		registration.addRecipeCatalyst(new ItemStack(TFItems.CRUMBLE_HORN.get()), CrumbleHornCategory.CRUMBLE_HORN);

		for (var block : BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTagGenerator.DRYING_RACKS)) {
			registration.addRecipeCatalyst(new ItemStack(block.value()), DryingCategory.DRYING);
		}
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		if (isEmiInstalled()) return;
		registration.registerSubtypeInterpreter(TFItems.KEEPSAKE_CASKET.get(), CasketSubtypeInterpreter.INSTANCE);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		if (isEmiInstalled()) return;
		registration.addRecipeTransferHandler(UncraftingMenu.class, TFMenuTypes.UNCRAFTING.get(), RecipeTypes.CRAFTING, 11, 9, 20, 36);
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		if (isEmiInstalled()) return;
		registration.register(ENTITY_TYPE, Collections.emptyList(), new EntityHelper(), new EntityRenderer(16));
		registration.register(FAKE_ITEM_ENTITY, Collections.emptyList(), new FakeItemEntityHelper(), new FakeItemEntityRenderer(16));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return TwilightForestMod.prefix("jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		if (isEmiInstalled()) return;
		registration.addRecipeCategories(new JEIUncraftingCategory(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new TransformationPowderCategory(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new OminousFireCategory(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new CrumbleHornCategory(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new DryingCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		if (isEmiInstalled()) return;
		// the repair recipe is special, so JEI only lists it in the crafting category once something can draw it.
		// A data pack that removes the recipe also removes the display (#2320)
		registration.getCraftingCategory().addCategoryExtension(MoonwormQueenRepairRecipe.class, recipe -> new MoonwormQueenExtension());
		registration.getCraftingCategory().addCategoryExtension(ScepterRepairRecipe.class, ScepterRepairExtension::new);
		registration.getCraftingCategory().addCategoryExtension(CasketRepairRecipe.class, CasketRepairExtension::new);
		registration.getCraftingCategory().addCategoryExtension(EssenceRepairRecipe.class, ExanimateEssenceRepairExtension::new);
		registration.getCraftingCategory().addCategoryExtension(TravellersVestGlovesMergeRecipe.class, recipe -> new TravellersVestGlovesMergeExtension());
		// JEI 15.2 has no grindstone category, so 4.8's GrindstoneTravellersRecipesGetter display has nowhere to go
		registration.getCraftingCategory().addCategoryExtension(TravellersGearModifierRecipe.class, TravellersGearModifierExtension::new);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		if (isEmiInstalled()) return;
		RecipeManager manager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get()) {
			registration.addRecipes(JEIUncraftingCategory.UNCRAFTING, RecipeViewerConstants.getAllUncraftingRecipes(manager));
		}
		registration.addRecipes(TransformationPowderCategory.TRANSFORMATION, manager.getAllRecipesFor(TFRecipes.TRANSFORM_POWDER_RECIPE.get()));
		registration.addRecipes(OminousFireCategory.OMINOUS_FIRE, RecipeViewerConstants.getOminousFireRecipes().stream().map(info -> new OminousFireRecipe(new FakeEntityType(info.input()), new FakeEntityType(info.output()))).toList());
		registration.addRecipes(CrumbleHornCategory.CRUMBLE_HORN, manager.getAllRecipesFor(TFRecipes.CRUMBLE_RECIPE.get()));
		// Stale Bread stays a secret, as in 4.8
		registration.addRecipes(DryingCategory.DRYING, manager.getAllRecipesFor(TFRecipes.DRYING_RECIPE.get()).stream().filter(recipe -> !recipe.getResult().is(TFItems.STALE_BREAD.get())).toList());
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
		if (isEmiInstalled()) return;
		registration.addRecipeClickArea(UncraftingScreen.class, 34, 33, 27, 20, JEIUncraftingCategory.UNCRAFTING);
		registration.addRecipeClickArea(UncraftingScreen.class, 115, 33, 27, 20, RecipeTypes.CRAFTING);
		// JEI's own recipe book handler only covers the vanilla screens, so keep its overlays off the open recipe book.
		// The book starts up to 149 pixels left of the GUI and its category tabs stick out up to 32 pixels further.
		registration.addGuiContainerHandler(UncraftingScreen.class, new IGuiContainerHandler<UncraftingScreen>() {
			@Override
			public List<Rect2i> getGuiExtraAreas(UncraftingScreen screen) {
				if (!screen.getRecipeBookComponent().isVisible()) return List.of();
				return List.of(new Rect2i(screen.getGuiLeft() - 182, screen.getGuiTop(), 182, screen.getYSize()));
			}
		});
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		// the painting variants differ only by NBT, which JEI does not tell apart, so it would list one arbitrary variant (4.8 3ae3c3575)
		jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, List.of(new ItemStack(TFItems.MAGIC_PAINTING.get())));
	}
}
