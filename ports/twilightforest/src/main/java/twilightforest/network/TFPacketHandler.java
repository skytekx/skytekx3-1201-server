package twilightforest.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import twilightforest.TwilightForestMod;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TFPacketHandler {
	// Bump this number every time theres a breaking change, to ensure people dont mess things up when joining on the wrong version
	// SkyTekx backport: the "-skytekx.N" suffix keeps stock TF 4.3 clients (protocol "2") out at the handshake instead of letting
	// them fail to decode the changed packets later. Bump N (2-skytekx.2, ...) whenever a backported packet changes its wire format.
	// .1: UpdateTFMultipartPacket is keyed by part entity id and always carries the part's synched data.
	// .2: SpawnCharmPacket is registered after ParticlePacket, which shifts the discriminator of every later packet.
	// .3: SyncUncraftingTableConfigPacket carries disableIngredientSwitching after allowShapeless (4.8 f5ef5a835).
	// .4: UpdateDeathTimePacket syncs a dying boss's deathTime to players that start tracking it (4.8 5b63f08d4).
	// .5: AddTFBossBarPacket and UpdateTFBossBarStylePacket carry the RGB boss bars (4.8 15ab0d12a).
	// .6: StructureProtectionPacket carries a list of per-piece boxes with a protected flag instead of one start box (4.8 262daec10, 48ba590a9).
	// .7: SyncQuestsPacket (data pack driven Questing Ram quest, 4.8 02875d8a3) is registered before SyncUncraftingTableConfigPacket.
	// .8: MagicMapPacket feature ids carry a conquered flag in their top bit (4.8 e67ac0be2).
	// .9: WipeOreMeterPacket (client to server) for the finished Ore Meter.
	// .10: the synced twilightforest:magic_paintings registry gains author, back_texture, local_lighting and the 4.8 opacity modifier fields.
	// .11: LifedrainParticlePacket draws the Lifedrain Scepter trail for everyone watching the user (4.8 31a235c1c).
	// .12: SetMasonJarItemPacket syncs the contents of a Mason Jar (4.8 9eaa6db1e).
	// .13: SyncDataMapPacket syncs the ominous fire data map to clients for the recipe viewers (4.8 TFDataMaps synced(...)).
	// .14: TravellersWingsStatePacket syncs the Traveller's Wings animation state (4.8 ab58315ba).
	// .15: PerformDoubleJumpPacket, PerformSidestepPacket (client to server) and GradualGlidePacket (both ways) for the Traveller's Wings modifiers (4.8 485d7e0a7).
	// .16: GogglesZoomPacket (both ways) syncs the Traveller's Goggles zoom (4.8 98ad4b10b).
	// .17: SwapHotbarPacket (client to server) for the Traveller's Belt Swap Hotbar.
	private static final String PROTOCOL_VERSION = "2-skytekx.17";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			TwilightForestMod.prefix("channel"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	@SuppressWarnings("UnusedAssignment")
	public static void init() {
		int id = 0;
		CHANNEL.registerMessage(id++, AreaProtectionPacket.class, AreaProtectionPacket::encode, AreaProtectionPacket::new, AreaProtectionPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, CreateMovingCicadaSoundPacket.class, CreateMovingCicadaSoundPacket::encode, CreateMovingCicadaSoundPacket::new, CreateMovingCicadaSoundPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, EnforceProgressionStatusPacket.class, EnforceProgressionStatusPacket::encode, EnforceProgressionStatusPacket::new, EnforceProgressionStatusPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, MagicMapPacket.class, MagicMapPacket::encode, MagicMapPacket::new, MagicMapPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, MazeMapPacket.class, MazeMapPacket::encode, MazeMapPacket::new, MazeMapPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, MissingAdvancementToastPacket.class, MissingAdvancementToastPacket::encode, MissingAdvancementToastPacket::new, MissingAdvancementToastPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, ParticlePacket.class, ParticlePacket::encode, ParticlePacket::new, ParticlePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, SpawnCharmPacket.class, SpawnCharmPacket::encode, SpawnCharmPacket::new, SpawnCharmPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, SpawnFallenLeafFromPacket.class, SpawnFallenLeafFromPacket::encode, SpawnFallenLeafFromPacket::new, SpawnFallenLeafFromPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, StructureProtectionClearPacket.class, StructureProtectionClearPacket::encode, StructureProtectionClearPacket::new, StructureProtectionClearPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, StructureProtectionPacket.class, StructureProtectionPacket::encode, StructureProtectionPacket::new, StructureProtectionPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, SyncQuestsPacket.class, SyncQuestsPacket::encode, SyncQuestsPacket::new, SyncQuestsPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, SyncUncraftingTableConfigPacket.class, SyncUncraftingTableConfigPacket::encode, SyncUncraftingTableConfigPacket::new, SyncUncraftingTableConfigPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, ThrowPlayerPacket.class, ThrowPlayerPacket::encode, ThrowPlayerPacket::new, ThrowPlayerPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UncraftingGuiPacket.class, UncraftingGuiPacket::encode, UncraftingGuiPacket::new, UncraftingGuiPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateFeatherFanFallPacket.class, UpdateFeatherFanFallPacket::encode, UpdateFeatherFanFallPacket::new, UpdateFeatherFanFallPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateGamerulePacket.class, UpdateGamerulePacket::encode, UpdateGamerulePacket::new, UpdateGamerulePacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateShieldPacket.class, UpdateShieldPacket::encode, UpdateShieldPacket::new, UpdateShieldPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateTFMultipartPacket.class, UpdateTFMultipartPacket::encode, UpdateTFMultipartPacket::new, UpdateTFMultipartPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, UpdateThrownPacket.class, UpdateThrownPacket::encode, UpdateThrownPacket::new, UpdateThrownPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateDeathTimePacket.class, UpdateDeathTimePacket::encode, UpdateDeathTimePacket::new, UpdateDeathTimePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, TFBossBarPacket.AddTFBossBarPacket.class, TFBossBarPacket.AddTFBossBarPacket::encode, TFBossBarPacket.AddTFBossBarPacket::new, TFBossBarPacket.AddTFBossBarPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, TFBossBarPacket.UpdateTFBossBarStylePacket.class, TFBossBarPacket.UpdateTFBossBarStylePacket::encode, TFBossBarPacket.UpdateTFBossBarStylePacket::new, TFBossBarPacket.UpdateTFBossBarStylePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, WipeOreMeterPacket.class, WipeOreMeterPacket::encode, WipeOreMeterPacket::new, WipeOreMeterPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		CHANNEL.registerMessage(id++, LifedrainParticlePacket.class, LifedrainParticlePacket::encode, LifedrainParticlePacket::new, LifedrainParticlePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, SetMasonJarItemPacket.class, SetMasonJarItemPacket::encode, SetMasonJarItemPacket::new, SetMasonJarItemPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, SyncDataMapPacket.class, SyncDataMapPacket::encode, SyncDataMapPacket::new, SyncDataMapPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, TravellersWingsStatePacket.class, TravellersWingsStatePacket::encode, TravellersWingsStatePacket::new, TravellersWingsStatePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, PerformDoubleJumpPacket.class, PerformDoubleJumpPacket::encode, PerformDoubleJumpPacket::new, PerformDoubleJumpPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		CHANNEL.registerMessage(id++, PerformSidestepPacket.class, PerformSidestepPacket::encode, PerformSidestepPacket::new, PerformSidestepPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_SERVER));
		CHANNEL.registerMessage(id++, GradualGlidePacket.class, GradualGlidePacket::encode, GradualGlidePacket::new, GradualGlidePacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, GogglesZoomPacket.class, GogglesZoomPacket::encode, GogglesZoomPacket::new, GogglesZoomPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, SwapHotbarPacket.class, SwapHotbarPacket::encode, SwapHotbarPacket::decode, SwapHotbarPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_SERVER));
	}
}
