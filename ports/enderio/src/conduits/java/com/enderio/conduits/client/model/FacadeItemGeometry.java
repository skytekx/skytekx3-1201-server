package com.enderio.conduits.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

/**
 * The item model of a conduit facade (Ender IO 7.1, 78e627263). The nested model is the look of an unpainted facade.
 */
public class FacadeItemGeometry implements IUnbakedGeometry<FacadeItemGeometry> {

    private final BlockModel facadeModel;

    public FacadeItemGeometry(BlockModel facadeModel) {
        this.facadeModel = facadeModel;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
        ItemOverrides overrides, ResourceLocation modelLocation) {
        return new FacadeItemModel(facadeModel.bake(baker, spriteGetter, modelState, modelLocation));
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        facadeModel.resolveParents(modelGetter);
    }

    public static class Loader implements IGeometryLoader<FacadeItemGeometry> {
        @Override
        public FacadeItemGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
            BlockModel model = deserializationContext.deserialize(jsonObject.get("model"), BlockModel.class);
            return new FacadeItemGeometry(model);
        }
    }
}
