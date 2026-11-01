package twilightforest.util.datamaps;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;

/**
 * Loads a set of {@link DataMapType}s with the server's datapacks (registered through AddReloadListenerEvent).
 */
public class DataMapLoader extends SimplePreparableReloadListener<List<Runnable>> {
	private final List<DataMapType<?, ?>> types;

	public DataMapLoader(List<DataMapType<?, ?>> types) {
		this.types = types;
	}

	@Override
	protected List<Runnable> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return this.types.stream().map(type -> type.load(resourceManager)).toList();
	}

	@Override
	protected void apply(List<Runnable> loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
		loaded.forEach(Runnable::run);
	}
}
