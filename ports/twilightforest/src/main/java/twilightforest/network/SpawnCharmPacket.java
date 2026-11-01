package twilightforest.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import twilightforest.TFConfig;
import twilightforest.entity.CharmEffect;
import twilightforest.init.TFEntities;

import java.util.function.Supplier;

/**
 * Sent to a player whose Charm of Life or Charm of Keeping just activated.
 * The client shows the charm, either as our orbiting {@link CharmEffect} or, with the client config option
 * {@code totemCharmAnimation}, like a popped Totem of Undying, and plays the charm's sound.
 */
public record SpawnCharmPacket(ItemStack charm, ResourceKey<SoundEvent> event) {

	public SpawnCharmPacket(FriendlyByteBuf buf) {
		this(buf.readItem(), buf.readResourceKey(Registries.SOUND_EVENT));
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeItem(this.charm());
		buf.writeResourceKey(this.event());
	}

	public static class Handler {

		@SuppressWarnings("Convert2Lambda")
		public static boolean onMessage(SpawnCharmPacket packet, Supplier<NetworkEvent.Context> ctx) {
			//ensure this is only done on clients as this uses client only code
			if (ctx.get().getDirection().getReceptionSide().isClient()) {
				ctx.get().enqueueWork(new Runnable() {
					@Override
					public void run() {
						Player player = Minecraft.getInstance().player;
						ClientLevel level = Minecraft.getInstance().level;
						if (player == null || level == null) return;
						Entity camera = Minecraft.getInstance().getCameraEntity();
						if (TFConfig.CLIENT_CONFIG.spawnCharmAnimationAsTotem.get()) {
							Minecraft.getInstance().gameRenderer.displayItemActivation(packet.charm());
							//prefer the camera pos over the player as the player position isnt quite synced to the client yet
							Minecraft.getInstance().particleEngine.createTrackingEmitter(camera != null ? camera : player, new ItemParticleOption(ParticleTypes.ITEM, packet.charm()), 20);
						} else {
							CharmEffect effect = new CharmEffect(TFEntities.CHARM_EFFECT.get(), level, player, packet.charm());
							effect.offset = (float) Math.PI;
							level.putNonPlayerEntity(effect.getId(), effect);
						}
						SoundEvent event = BuiltInRegistries.SOUND_EVENT.get(packet.event());
						if (camera != null && event != null) {
							level.playLocalSound(camera.getX(), camera.getY(), camera.getZ(), event, player.getSoundSource(), 1.5F, 1.0F, false);
						}
					}
				});
			}

			ctx.get().setPacketHandled(true);
			return true;
		}
	}
}
