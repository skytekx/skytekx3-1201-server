package twilightforest.entity.passive.quest.ram;

import net.minecraft.world.level.Level;

/**
 * Holds the Questing Ram quest currently in effect. 4.8 keeps one Beanification component for this; here the logical
 * server (filled by the reload listener) and the logical client (filled by {@link twilightforest.network.SyncQuestsPacket})
 * keep separate copies, so in singleplayer the client's synced copy never replaces the server's tag-based one.
 */
public final class QuestingRamCurrentContext {

	private static volatile QuestingRamContext serverContext = QuestingRamContext.FALLBACK;
	private static volatile QuestingRamContext clientContext = QuestingRamContext.FALLBACK;

	private QuestingRamCurrentContext() {
	}

	public static QuestingRamContext getContext(Level level) {
		return level.isClientSide() ? getClientContext() : getServerContext();
	}

	public static QuestingRamContext getServerContext() {
		return serverContext;
	}

	public static QuestingRamContext getClientContext() {
		return clientContext;
	}

	public static void setServerContext(QuestingRamContext context) {
		serverContext = context;
	}

	public static void setClientContext(QuestingRamContext context) {
		clientContext = context;
	}
}
