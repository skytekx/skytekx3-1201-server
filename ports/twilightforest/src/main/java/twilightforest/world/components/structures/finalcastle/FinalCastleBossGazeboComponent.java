package twilightforest.world.components.structures.finalcastle;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFStructurePieceTypes;
import twilightforest.util.BoundingBoxUtils;
import twilightforest.util.RotationUtil;
import twilightforest.world.components.structures.TFStructureComponentOld;
import twilightforest.world.components.structures.TwilightJigsawPiece;

import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public class FinalCastleBossGazeboComponent extends TFStructureComponentOld {

	// Template pool of the gazebo on the keep's roof (template_definition final_castle/temp/gazebo), so modpacks can supply their own boss arena
	public static final ResourceLocation GAZEBO_TEMP_POOL = TwilightForestMod.prefix("final_castle/temp/gazebo");

	// Tag of the Interaction entity that removes the gazebo's WIP text when punched (EntityEvents#onAttackEvent)
	public static final String INTERACTION_TAG = "final_castle_wip";

	// False for gazebos saved before the gazebo came from the template pool, and for gazebos generated with Twilight Tweaks loaded:
	// they keep building their force field and text themselves (unless Twilight Tweaks' coremod replaces that with its boss spawner)
	private final boolean templateHooks;

	@SuppressWarnings("unused")
	public FinalCastleBossGazeboComponent(StructurePieceSerializationContext ctx, CompoundTag nbt) {
		super(TFStructurePieceTypes.TFFCBoGaz.get(), nbt);
		this.templateHooks = nbt.getBoolean(FinalCastleTemplateHooks.NBT_KEY);
	}

	public FinalCastleBossGazeboComponent(int i, TFStructureComponentOld keep, int x, int y, int z) {
		super(TFStructurePieceTypes.TFFCBoGaz.get(), i, x, y, z);
		this.spawnListIndex = -1; // no monsters

		this.setOrientation(keep.getOrientation());
		this.boundingBox = new BoundingBox(keep.getBoundingBox().minX() + 14, keep.getBoundingBox().maxY() + 2, keep.getBoundingBox().minZ() + 14, keep.getBoundingBox().maxX() - 14, keep.getBoundingBox().maxY() + 13, keep.getBoundingBox().maxZ() - 14);
		// Twilight Tweaks replaces the gazebo with its final boss spawner (coremod on postProcess); the 4.8 template would cage that spawner in violet force field
		this.templateHooks = !ModList.get().isLoaded("twilighttweaks");
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tagCompound) {
		super.addAdditionalSaveData(ctx, tagCompound);
		tagCompound.putBoolean(FinalCastleTemplateHooks.NBT_KEY, this.templateHooks);
	}

	@Override
	public void addChildren(StructurePiece parent, StructurePieceAccessor list, RandomSource rand) {
		this.deco = new StructureTFDecoratorCastle();
		this.deco.blockState = TFBlocks.VIOLET_CASTLE_RUNE_BRICK.get().defaultBlockState();

		this.deco.fenceState = TFBlocks.VIOLET_FORCE_FIELD.get().defaultBlockState();

		if (this.templateHooks) {
			TwilightJigsawPiece templatePiece = FinalCastleTemplateHooks.initializeTemplateFromPool(GAZEBO_TEMP_POOL, this.getWorldPos(10, -1, 10), this.rotation.rotation().rotate(FrontAndTop.UP_SOUTH), "twilightforest:final_castle/final_boss", rand, this.genDepth + 1);
			if (templatePiece != null) {
				list.addPiece(templatePiece);
			}
		}
	}

	@Override
	public void postProcess(WorldGenLevel world, StructureManager manager, ChunkGenerator generator, RandomSource randomIn, BoundingBox sbb, ChunkPos chunkPosIn, BlockPos blockPos) {
		// Keep this the first static field read of the method: Twilight Tweaks' coremod inserts its boss spawner placement (and a return) right before it
		BlockState state = TFBlocks.VIOLET_FORCE_FIELD.get().defaultBlockState();

		// Since TF 4.8 the gazebo, its WIP text included, is placed by the template from GAZEBO_TEMP_POOL
		if (this.templateHooks)
			return;

		// walls
		for (Rotation rotation : RotationUtil.ROTATIONS) {
			this.fillBlocksRotated(world, sbb, 0, 0, 0, 0, 10, 20, state, rotation);
		}

		// roof
		this.generateBox(world, sbb, 1, 11, 0, 19, 11, 20, state, state, false);
		this.generateBox(world, sbb, 0, 11, 0, 0, 11, 20, state, state, false);
		this.generateBox(world, sbb, 20, 11, 0, 20, 11, 20, state, state, false);

		this.willBeAddingFinalBossSoon(world, sbb);

		// placeBlock(world, TFBlocks.boss_spawner_final_boss.get().defaultBlockState(), 10, 1, 10, sbb);
	}

	private void willBeAddingFinalBossSoon(WorldGenLevel world, BoundingBox sbb) {
		// this.placeSignAtCurrentPosition(world, 10, 0, 10, sbb, "Final Boss Here", "You win!", "discord.gg/6v3z26B");

		// old message - revised 9/25/2022
		// setInvisibleTextEntity(world, 10, 0, 10, sbb, "Final Boss Here", true, 2.3f);
		// setInvisibleTextEntity(world, 10, 0, 10, sbb, "You win!", true, 2.0f);
		// setInvisibleTextEntity(world, 10, 0, 10, sbb, "You can join the Twilight Forest Discord server to follow",true, 1.0f);
		// setInvisibleTextEntity(world, 10, 0, 10, sbb, "the latest updates on this castle and other content at:",true, 0.7f);

		ImmutableList.Builder<Vec3> positionAccumulator = ImmutableList.builder();

		// Places Display Text entities, configured to only rotate-sync with camera (billboarding) except with locked vertical angle: It will only rotate sideways.
		this.setInvisibleTextEntity(world, 10, 0, 10, sbb, "Final Castle WIP.", true, 2.3f, positionAccumulator::add, Display.BillboardConstraints.VERTICAL);
		this.setInvisibleTextEntity(world, 10, 0, 10, sbb, "Join our Discord server to", true, 1.3f, positionAccumulator::add, Display.BillboardConstraints.VERTICAL);
		this.setInvisibleTextEntity(world, 10, 0, 10, sbb, "follow development of the mod:", true, 1.0f, positionAccumulator::add, Display.BillboardConstraints.VERTICAL);
		this.setInvisibleTextEntity(world, 10, 0, 10, sbb, "discord.experiment115.com", true, 0.7f, positionAccumulator::add, Display.BillboardConstraints.VERTICAL);

		// The hitbox for the Interaction entity capturing Player Punches
		AABB punchBox = BoundingBoxUtils.vectorsMinMax(positionAccumulator.build(), 1);
		if (punchBox != null) {
			double width = (punchBox.getXsize() + punchBox.getZsize()) * 0.5;
			double height = punchBox.getYsize();

			Interaction interaction = new Interaction(EntityType.INTERACTION, world.getLevel());

			// IMPORTANT: Punching this box has to produce behavior of discarding the text
			// Executed via EntityEvents#onAttackEvent using AttackEntityEvent
			interaction.addTag(INTERACTION_TAG);

			interaction.setHeight((float) height);
			interaction.setWidth((float) width);
			Vec3 center = punchBox.getCenter();
			interaction.setPos(center.x, punchBox.minY, center.z);
			world.addFreshEntity(interaction);
		}
	}
}
