package twilightforest.client.model.item;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.CompositeModel;
import net.minecraftforge.client.model.DynamicFluidContainerModel;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import net.minecraftforge.client.model.geometry.StandaloneGeometryBakingContext;
import net.minecraftforge.client.model.geometry.UnbakedGeometryHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import twilightforest.TwilightForestMod;
import twilightforest.item.travellers_gear.TravellersArmorItem;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Item model of a piece of Traveller's Gear ({@code twilightforest:travellers_gear} loader). It draws the base texture,
 * or the broken one once the piece is broken, and layers one overlay sprite per inserted modifier on top (read from the
 * {@code modifier_directory} or {@code broken_modifier_directory}), plus the gloves overlay on a Vest merged with Gloves.
 * Every combination is baked once on first use. Backport of 4.8's TravellersGearItemModel to Forge 47's geometry API.
 */
public class TravellersGearItemModel implements IUnbakedGeometry<TravellersGearItemModel> {

	private static final Function<Float, Transformation> TRANSFORM = f -> new Transformation(null, null, new Vector3f(1.0F + f), null);
	private final List<ResourceLocation> modifiers;
	private final String directory;
	private final boolean broken;
	private final String brokenDirectory;
	private final boolean showGloves;

	TravellersGearItemModel(List<ResourceLocation> modifiers, String directory, boolean broken, String brokenDirectory, boolean showGloves) {
		this.modifiers = modifiers;
		this.directory = directory;
		this.broken = broken;
		this.brokenDirectory = brokenDirectory;
		this.showGloves = showGloves;
	}

	public TravellersGearItemModel withModifiers(List<ResourceLocation> modifiers, boolean broken, boolean showGloves) {
		return new TravellersGearItemModel(modifiers, this.directory, broken, this.brokenDirectory, showGloves);
	}

	@Override
	public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides, ResourceLocation modelLocation) {

		TextureAtlasSprite sprite = this.getSprite(context, spriteGetter);

		// We need to disable GUI 3D and block lighting for this to render properly
		var itemContext = StandaloneGeometryBakingContext.builder(context).withGui3d(false).withUseBlockLight(false).build(modelLocation);
		var modelBuilder = CompositeModel.Baked.builder(itemContext, sprite, new TravellersGearItemModel.Overrides(overrides, this, baker, itemContext), context.getTransforms());

		var normalRenderTypes = DynamicFluidContainerModel.getLayerRenderTypes(false);

		if (sprite != null) {
			// Base texture
			var unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0, sprite.contents());
			var quads = UnbakedGeometryHelper.bakeElements(unbaked, $ -> sprite, modelState, modelLocation);
			modelBuilder.addQuads(normalRenderTypes, quads);
		}

		int layers = 1;
		for (ResourceLocation modifier : this.modifiers) {
			TextureAtlasSprite modSprite = spriteGetter.apply(ForgeHooksClient.getBlockMaterial(modifier.withPrefix("item/" + (this.broken ? this.brokenDirectory : this.directory))));
			if (!modSprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
				var unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0, modSprite.contents());
				var quads = UnbakedGeometryHelper.bakeElements(unbaked, $ -> modSprite, new SimpleModelState(modelState.getRotation().compose(TRANSFORM.apply(layers * 0.001F)), modelState.isUvLocked()), modelLocation);
				modelBuilder.addQuads(normalRenderTypes, quads);
				layers++;
			}
		}

		if (this.showGloves) {
			TextureAtlasSprite modSprite = spriteGetter.apply(ForgeHooksClient.getBlockMaterial(TwilightForestMod.prefix("item/" + (this.broken ? this.brokenDirectory : this.directory) + "gloves")));
			if (!modSprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
				var unbaked = UnbakedGeometryHelper.createUnbakedItemElements(0, modSprite.contents());
				var quads = UnbakedGeometryHelper.bakeElements(unbaked, $ -> modSprite, new SimpleModelState(modelState.getRotation().compose(TRANSFORM.apply(layers * 0.001F)), modelState.isUvLocked()), modelLocation);
				modelBuilder.addQuads(normalRenderTypes, quads);
			}
		}

		return modelBuilder.build();
	}

	@Nullable
	private TextureAtlasSprite getSprite(IGeometryBakingContext context, Function<Material, TextureAtlasSprite> spriteGetter) {
		Material baseLocation = context.hasMaterial("base") ? context.getMaterial("base") : null;
		if (this.broken) {
			Material brokenLocation = context.hasMaterial("broken") ? context.getMaterial("broken") : null;
			return brokenLocation != null ? spriteGetter.apply(brokenLocation) : baseLocation != null ? spriteGetter.apply(baseLocation) : null;
		} else {
			return baseLocation != null ? spriteGetter.apply(baseLocation) : null;
		}
	}

	public static final class Loader implements IGeometryLoader<TravellersGearItemModel> {
		public static final TravellersGearItemModel.Loader INSTANCE = new TravellersGearItemModel.Loader();

		private Loader() {
		}

		@Override
		public TravellersGearItemModel read(JsonObject object, JsonDeserializationContext context) {
			return new TravellersGearItemModel(List.of(), GsonHelper.getAsString(object, "modifier_directory"), false, GsonHelper.getAsString(object, "broken_modifier_directory"), false);
		}
	}

	private static final class Overrides extends ItemOverrides {
		private static final ResourceLocation OVERRIDE_LOCATION = TwilightForestMod.prefix("travellers_gear_override");
		private final Map<String, BakedModel> possibleCombos = Maps.newHashMap();
		private final ItemOverrides nested;
		private final TravellersGearItemModel parent;
		private final ModelBaker baker;
		private final IGeometryBakingContext owner;

		private Overrides(ItemOverrides nested, TravellersGearItemModel parent, ModelBaker baker, IGeometryBakingContext owner) {
			this.nested = nested;
			this.parent = parent;
			this.baker = baker;
			this.owner = owner;
		}

		@Nullable
		@Override
		public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
			BakedModel overridden = this.nested.resolve(originalModel, stack, level, entity, seed);
			if (overridden != originalModel)
				return overridden;

			// the modifier definitions are a synced datapack registry, read from the level the stack is drawn in
			List<ResourceLocation> modifiers = level == null ? List.of() : TravellersArmorItem.getInsertedModifierIds(level.registryAccess(), stack);
			boolean broken = TravellersArmorItem.isTravellersArmorAndBroken(stack);
			boolean gloves = TravellersArmorItem.hasGloves(stack);
			if (modifiers.isEmpty() && !broken && !gloves)
				return originalModel;

			String key = ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath() + this.getModifiersSuffix(modifiers, broken, gloves);
			return this.possibleCombos.computeIfAbsent(key, k -> this.parent.withModifiers(modifiers, broken, gloves).bake(this.owner, this.baker, Material::sprite, BlockModelRotation.X0_Y0, this, OVERRIDE_LOCATION));
		}

		private String getModifiersSuffix(List<ResourceLocation> modifiers, boolean broken, boolean gloves) {
			StringBuilder ret = new StringBuilder();
			if (gloves) ret.append("_gloves");
			if (broken) ret.append("_broken");
			for (ResourceLocation modifier : modifiers) {
				ret.append("_").append(modifier.toLanguageKey());
			}
			return ret.toString();
		}
	}
}
