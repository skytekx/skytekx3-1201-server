package twilightforest.client.model.block.carpet;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class RoyalRagsModelLoader implements IGeometryLoader<UnbakedRoyalRagsModel> {
	// FIXME (upstream): Generalize alongside the connected texture (castle door) models
	public static final RoyalRagsModelLoader INSTANCE = new RoyalRagsModelLoader();

	public RoyalRagsModelLoader() {
	}

	@Override
	public UnbakedRoyalRagsModel read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
		return new UnbakedRoyalRagsModel();
	}
}
