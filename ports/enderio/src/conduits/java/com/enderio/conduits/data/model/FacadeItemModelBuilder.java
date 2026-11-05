package com.enderio.conduits.data.model;

import com.enderio.EnderIO;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Writes the enderio:facades_item model of a conduit facade (Ender IO 7.1, 78e627263).
 */
public class FacadeItemModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    private ResourceLocation model = EnderIO.loc("block/conduit_facade");

    public static <T extends ModelBuilder<T>> FacadeItemModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new FacadeItemModelBuilder<>(parent, existingFileHelper);
    }

    protected FacadeItemModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(EnderIO.loc("facades_item"), parent, existingFileHelper);
    }

    public FacadeItemModelBuilder<T> model(String name) {
        this.model = EnderIO.loc("block/" + name);
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);
        JsonObject modelJson = new JsonObject();
        modelJson.addProperty("parent", model.toString());
        json.add("model", modelJson);
        return json;
    }
}
