package twilightforest.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.TwilightForestMod;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.entity.monster.Kobold;
import twilightforest.network.AreaProtectionPacket;
import twilightforest.network.EnforceProgressionStatusPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.LandmarkUtil;
import twilightforest.util.WorldUtil;
import twilightforest.world.components.chunkgenerators.ChunkGeneratorTwilight;
import twilightforest.world.components.structures.util.ProgressionPiece;
import twilightforest.world.components.structures.util.ProgressionStructure;
import twilightforest.world.registration.TFGenerationSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A class to store events relating to progression
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class ProgressionEvents {

	/**
	 * Check if the player is trying to break a block in a structure that's considered unbreakable for progression reasons
	 */
	@SubscribeEvent
	public static void breakBlock(BlockEvent.BreakEvent event) {
		Player player = event.getPlayer();
		BlockPos pos = event.getPos();

		if (!(event.getLevel() instanceof Level level) || level.isClientSide()) return;

		if (isBlockProtectedFromBreaking(level, pos) && isAreaProtected(level, player, pos)) {
			event.setCanceled(true);
		}
	}

	/**
	 * Check if the player is trying to place a block in a structure that's considered inaccessible for progression reasons.
	 * Every right-click on a block inside a protected piece is refused, so nothing gets placed or used there.
	 * The client has already predicted the placement, so resend the inventory to undo the item it thinks it used up.
	 * Runs before {@link #onPlayerRightClick} (as in 4.8, where it is registered first), whose banned interactions it already covers,
	 * so a click only sends one area protection packet.
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void preventLockedAreaBlockPlacing(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;

		Player player = event.getEntity();
		if (isAreaProtected(level, player, event.getPos())) {
			event.setCanceled(true);
			player.inventoryMenu.sendAllDataToRemote();
		}
	}

	/**
	 * Check if the player is trying to place a multi-block (beds, doors, ...) that intersects a structure that's considered inaccessible for progression reasons
	 */
	@SubscribeEvent
	public static void preventLockedAreaMultiblocks(BlockEvent.EntityMultiPlaceEvent event) {
		Entity entity = event.getEntity();

		if (!(event.getLevel() instanceof ServerLevel level) || !(entity instanceof Player player)) return;

		for (BlockSnapshot snapshot : event.getReplacedBlockSnapshots()) {
			BlockPos pos = snapshot.getPos();

			if (isBlockProtectedFromBreaking(level, pos) && isAreaProtected(level, player, pos)) {
				event.setCanceled(true);
				player.inventoryMenu.sendAllDataToRemote();
				break;
			}
		}
	}

	/**
	 * Stop the player from interacting with blocks that could produce treasure or open doors in a protected area
	 */
	@SubscribeEvent
	public static void onPlayerRightClick(PlayerInteractEvent.RightClickBlock event) {

		Player player = event.getEntity();
		Level level = player.level();

		if (!level.isClientSide() && isBlockProtectedFromInteraction(level, event.getPos()) && isAreaProtected(level, player, event.getPos())) {
			event.setUseBlock(Event.Result.DENY);
		}
	}

	private static boolean isBlockProtectedFromInteraction(Level level, BlockPos pos) {
		return level.getBlockState(pos).is(BlockTagGenerator.STRUCTURE_BANNED_INTERACTIONS);
	}

	private static boolean isBlockProtectedFromBreaking(Level level, BlockPos pos) {
		return !level.getBlockState(pos).is(BlockTagGenerator.PROGRESSION_ALLOW_BREAKING);
	}

	/**
	 * Return if the area at the coordinates is considered protected for that player.
	 * Currently, if we return true, we also send the area protection packet here.
	 */
	private static boolean isAreaProtected(Level level, Player player, BlockPos pos) {

		if (player.getAbilities().instabuild || player.isSpectator() ||
				!LandmarkUtil.isProgressionEnforced(level) || player instanceof FakePlayer) {
			return false;
		}

		ChunkGeneratorTwilight chunkGenerator = WorldUtil.getChunkGenerator(level);

		if (chunkGenerator != null) {
			Optional<StructureStart> struct = LandmarkUtil.locateNearestLandmarkStart(level, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
			if (struct.isPresent()) {
				StructureStart structureStart = struct.get();
				// only the pieces themselves are protected (not the whole start box), and only those that opt into protection
				if (structureStart.getPieces().stream().anyMatch(structurePiece -> structurePiece.getBoundingBox().isInside(pos) && ProgressionPiece.isPieceProtected(structurePiece)) && structureStart.getStructure() instanceof ProgressionStructure structureHints) {
					if (!structureHints.doesPlayerHaveRequiredAdvancements(player)/* && chunkGenerator.isBlockProtected(pos)*/) {
						// send protection packet
						List<BoundingBox> boxes = new ArrayList<>();
						structureStart.getPieces().forEach(piece -> {
							if (piece.getBoundingBox().isInside(pos))
								boxes.add(piece.getBoundingBox());
						});

						sendAreaProtectionPacket(level, pos, boxes);

						// send a hint monster?
						structureHints.trySpawnHintMonster(level, player, pos);

						return true;
					}
				}
			}
		}
		return false;
	}

	private static void sendAreaProtectionPacket(Level level, BlockPos pos, List<BoundingBox> sbb) {
		PacketDistributor.TargetPoint targetPoint = new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 64, level.dimension());
		TFPacketHandler.CHANNEL.send(PacketDistributor.NEAR.with(() -> targetPoint), new AreaProtectionPacket(sbb, pos));
	}

	@SubscribeEvent
	public static void livingAttack(LivingAttackEvent event) {
		LivingEntity living = event.getEntity();
		// cancel attacks in protected areas
		if (!living.level().isClientSide() && living instanceof Enemy && event.getSource().getEntity() instanceof Player && !(living instanceof Kobold)
				&& isAreaProtected(living.level(), (Player) event.getSource().getEntity(), new BlockPos(living.blockPosition()))) {

			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void playerPortals(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
			if (TFGenerationSettings.usesTwilightChunkGenerator((ServerLevel) player.level())) {
				sendEnforcedProgressionStatus(player, LandmarkUtil.isProgressionEnforced(player.level()));
			}
		}
	}

	@SubscribeEvent
	public static void playerLogsIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (!event.getEntity().level().isClientSide() && event.getEntity() instanceof ServerPlayer player) {
			sendEnforcedProgressionStatus(player, LandmarkUtil.isProgressionEnforced(event.getEntity().level()));
		}
	}

	private static void sendEnforcedProgressionStatus(ServerPlayer player, boolean isEnforced) {
		TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new EnforceProgressionStatusPacket(isEnforced));
	}
}
