package twilightforest;

import com.mojang.datafixers.util.Pair;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.commands.Commands;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.advancements.TFAdvancements;
import twilightforest.block.TFPortalBlock;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFBlocks;
import twilightforest.init.custom.Enforcement;
import twilightforest.network.MissingAdvancementToastPacket;
import twilightforest.network.StructureProtectionClearPacket;
import twilightforest.network.StructureProtectionPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.LandmarkUtil;
import twilightforest.util.PlayerHelper;
import twilightforest.util.WorldUtil;
import twilightforest.world.components.chunkgenerators.ChunkGeneratorTwilight;
import twilightforest.world.components.structures.util.AdvancementLockedStructure;
import twilightforest.world.components.structures.util.ProgressionPiece;
import twilightforest.world.registration.TFGenerationSettings;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class TFTickHandler {

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		Player eventPlayer = event.player;

		if (!(eventPlayer instanceof ServerPlayer player)) return;
		if (!(player.level() instanceof ServerLevel world)) return;

		// check for portal creation, at least if it's not disabled
		if (!TFConfig.COMMON_CONFIG.disablePortalCreation.get() && event.phase == TickEvent.Phase.END && player.tickCount % (TFConfig.COMMON_CONFIG.checkPortalDestination.get() ? 100 : 20) == 0) {
			// skip players below the required permission level
			int requiredPermission = TFConfig.COMMON_CONFIG.portalCreationPermission.get();
			boolean legacyAdminOnly = TFConfig.COMMON_CONFIG.adminOnlyPortals.get();
			if (legacyAdminOnly) requiredPermission = Math.max(requiredPermission, Commands.LEVEL_MODERATORS);
			if (world.getServer().getProfilePermissions(player.getGameProfile()) >= requiredPermission) {
				// reduce range to 4.0 if config is set to admins/owners only
				checkForPortalCreation(player, world, legacyAdminOnly || requiredPermission >= Commands.LEVEL_ADMINS ? 4.0F : 32.0F);
			}
		}

		// check the player for being in a forbidden progression area, only every 20 ticks
		if (event.phase == TickEvent.Phase.END && player.tickCount % 20 == 0 && LandmarkUtil.isProgressionEnforced(world) && TFGenerationSettings.usesTwilightChunkGenerator(world) && !player.isCreative() && !player.isSpectator()) {
			Enforcement.enforceBiomeProgression(player, world);
		}

		// check and send nearby forbidden structures, every 100 ticks or so
		if (event.phase == TickEvent.Phase.END && player.tickCount % 100 == 0 && LandmarkUtil.isProgressionEnforced(world)) {
			if (TFGenerationSettings.usesTwilightChunkGenerator(world)) {
				if (player.isCreative() || player.isSpectator()) {
					sendAllClearPacket(player);
				} else {
					checkForLockedStructuresSendPacket(player, world);
				}
			}
		}
	}

	private static void sendStructureProtectionPacket(Player player, List<Pair<BoundingBox, Boolean>> sbbData) {
		if (player instanceof ServerPlayer) {
			TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new StructureProtectionPacket(sbbData));
		}
	}

	private static void sendAllClearPacket(Player player) {
		if (player instanceof ServerPlayer) {
			TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new StructureProtectionClearPacket());
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	private static boolean checkForLockedStructuresSendPacket(Player player, ServerLevel world) {
		ChunkGeneratorTwilight chunkGenerator = WorldUtil.getChunkGenerator(world);
		if (chunkGenerator == null)
			return false;

		ChunkPos chunkPlayer = player.chunkPosition();
		return LandmarkUtil.locateNearestLandmarkStart(world, chunkPlayer.x, chunkPlayer.z).map(structureStart -> {
			if (structureStart.getStructure() instanceof AdvancementLockedStructure advancementLockedStructure && !advancementLockedStructure.doesPlayerHaveRequiredAdvancements(player)) {
				// per piece: protected pieces are inflated by 4 so the protection rain also shows around their walls,
				// unprotected pieces are sent as-is so the client can cut them out of the rain
				List<Pair<BoundingBox, Boolean>> boundingBoxesData = structureStart.getPieces().stream()
						.map(piece -> {
							boolean isProtected = ProgressionPiece.isPieceProtected(piece);
							return Pair.of(isProtected ? piece.getBoundingBox().inflatedBy(4) : piece.getBoundingBox(), isProtected);
						})
						.toList();

				sendStructureProtectionPacket(player, boundingBoxesData);
				return true;
			}

			sendAllClearPacket(player);
			return false;
		}).orElse(false);
	}

	private static void checkForPortalCreation(ServerPlayer player, Level world, float rangeToCheck) {
		if (world.dimension().location().equals(new ResourceLocation(TFConfig.COMMON_CONFIG.originDimension.get()))
				|| TFGenerationSettings.isTwilightPortalDestination(world)
				|| TFConfig.COMMON_CONFIG.allowPortalsInOtherDimensions.get()) {

			List<ItemEntity> itemList = world.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(rangeToCheck));
			ItemEntity qualified = null;

			for (ItemEntity entityItem : itemList) {
				if (entityItem.getItem().is(ItemTagGenerator.PORTAL_ACTIVATOR) &&
						TFBlocks.TWILIGHT_PORTAL.get().canFormPortal(world.getBlockState(entityItem.blockPosition())) &&
						Objects.equals(entityItem.getOwner(), player)) {

					qualified = entityItem;
					break;
				}
			}

			if (qualified == null) return;

			if (!player.isCreative() && !player.isSpectator() && TFConfig.getPortalLockingAdvancement(player) != null) {
				Advancement requirement = PlayerHelper.getAdvancement(player, Objects.requireNonNull(TFConfig.getPortalLockingAdvancement(player)));
				if (requirement != null && !PlayerHelper.doesPlayerHaveRequiredAdvancement(player, requirement)) {
					player.displayClientMessage(TFPortalBlock.PORTAL_UNWORTHY, true);

					if (!TFPortalBlock.isPlayerNotifiedOfRequirement(player)) {
						// .doesPlayerHaveRequiredAdvancement null-checks already, so we can skip null-checking the `requirement`
						DisplayInfo info = requirement.getDisplay();
						TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), info == null ? new MissingAdvancementToastPacket(Component.translatable(".ui.advancement.no_title"), new ItemStack(TFBlocks.TWILIGHT_PORTAL_MINIATURE_STRUCTURE.get())) : new MissingAdvancementToastPacket(info.getTitle(), info.getIcon()));

						TFPortalBlock.playerNotifiedOfRequirement(player);
					}

					return; // Item qualifies, but the player doesn't
				}
			}

			Random rand = new Random();
			for (int i = 0; i < 2; i++) {
				double vx = rand.nextGaussian() * 0.02D;
				double vy = rand.nextGaussian() * 0.02D;
				double vz = rand.nextGaussian() * 0.02D;

				world.addParticle(ParticleTypes.EFFECT, qualified.getX(), qualified.getY() + 0.2, qualified.getZ(), vx, vy, vz);
			}

			if (TFBlocks.TWILIGHT_PORTAL.get().tryToCreatePortal(world, qualified.blockPosition(), qualified, player))
				TFAdvancements.MADE_TF_PORTAL.trigger(player);

		}
	}

}
