package twilightforest.world.components.structures;

/**
 * A structure piece that picks the monster spawn list of its structure inside its bounding box. Where pieces overlap, the highest index
 * wins; a negative index means no structure spawns there.
 */
public interface SpawnIndexProvider {
	int getSpawnIndex();

	interface Deny extends SpawnIndexProvider {
		@Override
		default int getSpawnIndex() {
			return -1;
		}
	}
}
