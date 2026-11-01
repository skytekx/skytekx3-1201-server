package twilightforest.client.renderer.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base for armor renderers that bake their {@link ModelPart}s once and reuse them,
 * instead of calling {@code EntityModelSet#bakeLayer} (a full deep copy of the layer) on every
 * {@code getHumanoidArmorModel} call, i.e. every frame for every armor piece that is drawn.
 * <p>
 * The cached parts are dropped on resource reload (see {@link ResourceReloadListener}) and rebaked lazily.
 */
public abstract class TFArmorRenderer implements IClientItemExtensions {
	private static final List<TFArmorRenderer> INSTANCES = new CopyOnWriteArrayList<>();
	private final Map<ModelLayerLocation, ModelPart> armorModels = new HashMap<>();

	protected TFArmorRenderer() {
		INSTANCES.add(this);
	}

	public void resetModelCache() {
		this.armorModels.clear();
	}

	public static void resetAllModelCache() {
		INSTANCES.forEach(TFArmorRenderer::resetModelCache);
	}

	protected ModelPart getModelPart(ModelLayerLocation layerLocation) {
		return this.armorModels.computeIfAbsent(layerLocation, location -> Minecraft.getInstance().getEntityModels().bakeLayer(location));
	}

	public static final class ResourceReloadListener implements ResourceManagerReloadListener {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
			TFArmorRenderer.resetAllModelCache();
		}
	}
}
