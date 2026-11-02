package twilightforest.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.spawner.SinisterSpawnerBlockEntity;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.init.TFParticleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SinisterSpawnerBlock extends BaseEntityBlock {
	public SinisterSpawnerBlock(Properties properties) {
		super(properties);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SinisterSpawnerBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return createTickerHelper(blockEntityType, TFBlockEntities.SINISTER_SPAWNER.get(), level.isClientSide() ? SinisterSpawnerBlockEntity::clientTick : SinisterSpawnerBlockEntity::serverTick);
	}

	@Override
	public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
		return 15 + randomSource.nextInt(15) + randomSource.nextInt(15);
	}

	// [VANILLA COPY] SpawnerBlock#appendHoverText (4.8 calls Spawner.appendHoverText, which 1.20.1 does not have)
	@Override
	public void appendHoverText(ItemStack stack, @Nullable BlockGetter getter, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, getter, tooltip, flag);
		Optional<Component> entityName = getSpawnEntityDisplayName(stack);
		if (entityName.isPresent()) {
			tooltip.add(entityName.get());
		} else {
			tooltip.add(CommonComponents.EMPTY);
			tooltip.add(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
			tooltip.add(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
		}
	}

	private static Optional<Component> getSpawnEntityDisplayName(ItemStack stack) {
		CompoundTag tag = BlockItem.getBlockEntityData(stack);
		if (tag != null && tag.contains("SpawnData", Tag.TAG_COMPOUND)) {
			ResourceLocation entityId = ResourceLocation.tryParse(tag.getCompound("SpawnData").getCompound("entity").getString("id"));
			if (entityId != null) {
				return BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).map(type -> Component.translatable(type.getDescriptionId()).withStyle(ChatFormatting.GRAY));
			}
		}

		return Optional.empty();
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext collisionContext) {
		return Shapes.empty();
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	@Override
	@SuppressWarnings("deprecation")
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		ItemStack stack = player.getItemInHand(hand);

		if (level.getBlockEntity(pos) instanceof SinisterSpawnerBlockEntity entity) {
			if (player.isCreative()) {
				List<ParticleOptions> particleOptions = this.particlesFromItem(stack);

				if (!particleOptions.isEmpty()) {
					entity.getSpawner().setParticles(particleOptions, true);

					return InteractionResult.sidedSuccess(level.isClientSide());
				}
			}

			// 4.8's block entity implements Spawner, so spawn eggs set its mob like a vanilla spawner's.
			// 1.20.1's SpawnEggItem only knows minecraft:spawner, so the egg is handled here instead.
			if (stack.getItem() instanceof SpawnEggItem egg && player.getAbilities().mayBuild) {
				if (!level.isClientSide()) {
					EntityType<?> type = egg.getType(stack.getTag());
					entity.setEntityId(type, level.getRandom());
					level.sendBlockUpdated(pos, state, state, 3);
					level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
					if (!player.getAbilities().instabuild) {
						stack.shrink(1);
					}
				}

				return InteractionResult.sidedSuccess(level.isClientSide());
			}
		}

		return super.use(state, level, pos, player, hand, hitResult);
	}

	private List<ParticleOptions> particlesFromItem(ItemStack stack) {
		if (stack.is(TFItems.NAGA_TROPHY.get()) || stack.is(TFBlocks.NAGA_BOSS_SPAWNER.get().asItem())) {
			return List.of(ParticleTypes.SMOKE, ParticleTypes.CRIT);
		} else if (stack.is(TFItems.LICH_TROPHY.get()) || stack.is(TFBlocks.LICH_BOSS_SPAWNER.get().asItem())) {
			return List.of(ParticleTypes.SMOKE, TFParticleType.OMINOUS_FLAME.get());
		} else if (stack.is(TFItems.MINOSHROOM_TROPHY.get()) || stack.is(TFBlocks.MINOSHROOM_BOSS_SPAWNER.get().asItem())) {
			return List.of(ParticleTypes.SMOKE, ParticleTypes.CRIMSON_SPORE);
		} else if (stack.is(TFItems.HYDRA_TROPHY.get()) || stack.is(TFBlocks.HYDRA_BOSS_SPAWNER.get().asItem())) {
			return List.of(ParticleTypes.SMOKE, ParticleTypes.FLAME);
		} else if (stack.is(TFItems.KNIGHT_PHANTOM_TROPHY.get()) || stack.is(TFBlocks.KNIGHT_PHANTOM_BOSS_SPAWNER.get().asItem())) {
			return List.of(ParticleTypes.SMOKE, TFParticleType.OMINOUS_FLAME.get());
		} else if (stack.is(TFItems.UR_GHAST_TROPHY.get()) || stack.is(TFBlocks.UR_GHAST_BOSS_SPAWNER.get().asItem())) {
			return List.of(ParticleTypes.SMOKE, DustParticleOptions.REDSTONE);
		} else if (stack.is(TFItems.ALPHA_YETI_TROPHY.get()) || stack.is(TFBlocks.ALPHA_YETI_BOSS_SPAWNER.get().asItem())) {
			return List.of(TFParticleType.SNOW.get(), ParticleTypes.FALLING_WATER);
		} else if (stack.is(TFItems.SNOW_QUEEN_TROPHY.get()) || stack.is(TFBlocks.SNOW_QUEEN_BOSS_SPAWNER.get().asItem())) {
			return List.of(TFParticleType.SNOW.get(), TFParticleType.SNOW_WARNING.get());
		} else if (stack.is(TFBlocks.FINAL_BOSS_BOSS_SPAWNER.get().asItem())) {
			return List.of(TFParticleType.ANNIHILATE.get());
		} else if (stack.is(TFItems.QUEST_RAM_TROPHY.get())) {
			return List.of(TFParticleType.TRANSFORMATION_PARTICLE.get());
		}

		return List.of();
	}

	@Override
	@SuppressWarnings("deprecation")
	public List<ItemStack> getDrops(BlockState state, LootParams.Builder paramBuilder) {
		List<ItemStack> drops = super.getDrops(state, paramBuilder);

		if (paramBuilder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof SinisterSpawnerBlockEntity entity) {
			LootTable lootTable = entity.getLootTable();
			if (lootTable != null) {
				LootParams params = paramBuilder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
				List<ItemStack> allDrops = new ArrayList<>(drops);
				allDrops.addAll(lootTable.getRandomItems(params));
				return allDrops;
			}
		}

		return drops;
	}
}
