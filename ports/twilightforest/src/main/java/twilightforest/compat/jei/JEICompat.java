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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.client.UncraftingScreen;
import twilightforest.compat.RecipeViewerConstants;
import twilightforest.compat.jei.categories.CrumbleHornCategory;
import twilightforest.compat.jei.categories.JEIUncraftingCategory;
import twilightforest.compat.jei.categories.TransformationPowderCategory;
import twilightforest.compat.jei.extension.CasketRepairExtension;
import twilightforest.compat.jei.extension.MoonwormQueenExtension;
import twilightforest.compat.jei.renderers.*;
import twilightforest.compat.jei.subtype.CasketSubtypeInterpreter;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.init.TFMenuTypes;
import twilightforest.init.TFRecipes;
import twilightforest.inventory.UncraftingMenu;
import twilightforest.item.recipe.CasketRepairRecipe;
import twilightforest.item.recipe.MoonwormQueenRepairRecipe;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JeiPlugin
@SuppressWarnings("unused")
public class JEICompat implements IModPlugin {

	public static final IIngredientType<FakeEntityType> ENTITY_TYPE = () -> FakeEntityType.class;
	public static final IIngredientType<FakeItemEntity> FAKE_ITEM_ENTITY = () -> FakeItemEntity.class;

	static {
		// JEI only builds this plugin on the client. Drop the cached display entities whenever a screen closes,
		// otherwise they keep the level they were created in alive after a dimension change or disconnect (#2488)
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ScreenEvent.Closing.class, event -> EntityRenderer.clearEntityCache());
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get()) {
			registration.addRecipeCatalyst(new ItemStack(TFBlocks.UNCRAFTING_TABLE.get()), RecipeTypes.CRAFTING);
			registration.addRecipeCatalyst(new ItemStack(TFBlocks.UNCRAFTING_TABLE.get()), JEIUncraftingCategory.UNCRAFTING);
		}
		registration.addRecipeCatalyst(new ItemStack(TFItems.TRANSFORMATION_POWDER.get()), TransformationPowderCategory.TRANSFORMATION);
		registration.addRecipeCatalyst(new ItemStack(TFItems.CRUMBLE_HORN.get()), CrumbleHornCategory.CRUMBLE_HORN);
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.registerSubtypeInterpreter(TFItems.KEEPSAKE_CASKET.get(), CasketSubtypeInterpreter.INSTANCE);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
		registration.addRecipeTransferHandler(UncraftingMenu.class, TFMenuTypes.UNCRAFTING.get(), RecipeTypes.CRAFTING, 11, 9, 20, 36);
	}

	@Override
	public void registerIngredients(IModIngredientRegistration registration) {
		registration.register(ENTITY_TYPE, Collections.emptyList(), new EntityHelper(), new EntityRenderer(16));
		registration.register(FAKE_ITEM_ENTITY, Collections.emptyList(), new FakeItemEntityHelper(), new FakeItemEntityRenderer(16));
	}

	@Override
	public ResourceLocation getPluginUid() {
		return TwilightForestMod.prefix("jei_plugin");
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		registration.addRecipeCategories(new JEIUncraftingCategory(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new TransformationPowderCategory(registration.getJeiHelpers().getGuiHelper()));
		registration.addRecipeCategories(new CrumbleHornCategory(registration.getJeiHelpers().getGuiHelper()));
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		// the repair recipe is special, so JEI only lists it in the crafting category once something can draw it.
		// A data pack that removes the recipe also removes the display (#2320)
		registration.getCraftingCategory().addCategoryExtension(MoonwormQueenRepairRecipe.class, recipe -> new MoonwormQueenExtension());
		registration.getCraftingCategory().addCategoryExtension(CasketRepairRecipe.class, CasketRepairExtension::new);
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		RecipeManager manager = Objects.requireNonNull(Minecraft.getInstance().level).getRecipeManager();
		if (!TFConfig.COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get()) {
			registration.addRecipes(JEIUncraftingCategory.UNCRAFTING, RecipeViewerConstants.getAllUncraftingRecipes(manager));
		}
		registration.addRecipes(TransformationPowderCategory.TRANSFORMATION, manager.getAllRecipesFor(TFRecipes.TRANSFORM_POWDER_RECIPE.get()));
		registration.addRecipes(CrumbleHornCategory.CRUMBLE_HORN, manager.getAllRecipesFor(TFRecipes.CRUMBLE_RECIPE.get()));
	}

	@Override
	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
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
