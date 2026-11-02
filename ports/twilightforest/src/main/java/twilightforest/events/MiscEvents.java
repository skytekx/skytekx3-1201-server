package twilightforest.events;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.TwilightForestMod;
import twilightforest.block.SinisterSpawnerBlock;
import twilightforest.compat.curios.CuriosCompat;
import twilightforest.entity.monster.DeathTome;
import twilightforest.entity.passive.Bighorn;
import twilightforest.entity.passive.DwarfRabbit;
import twilightforest.entity.passive.Squirrel;
import twilightforest.entity.passive.TinyBird;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFEntities;
import twilightforest.network.CreateMovingCicadaSoundPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.ArmorUtil;

@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class MiscEvents {

	@SubscribeEvent
	public static void addPrey(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof Mob mob) {
			EntityType<?> type = mob.getType();
			if (type == EntityType.CAT) {
				mob.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>((TamableAnimal) mob, DwarfRabbit.class, true, null));
				mob.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>((TamableAnimal) mob, Squirrel.class, true, null));
				mob.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>((TamableAnimal) mob, TinyBird.class, true, null));
			} else if (type == EntityType.OCELOT) {
				mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mob, DwarfRabbit.class, true));
				mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mob, Squirrel.class, true));
				mob.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mob, TinyBird.class, true));
			} else if (type == EntityType.FOX) {
				mob.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(mob, DwarfRabbit.class, true));
				mob.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(mob, Squirrel.class, true));
			} else if (type == EntityType.WOLF) {
				mob.targetSelector.addGoal(7, new NonTameRandomTargetGoal<>((TamableAnimal) mob, DwarfRabbit.class, true, null));
				mob.targetSelector.addGoal(7, new NonTameRandomTargetGoal<>((TamableAnimal) mob, Squirrel.class, true, null));
				mob.targetSelector.addGoal(7, new NonTameRandomTargetGoal<>((TamableAnimal) mob, Bighorn.class, true, null));
			}
		}
	}

	@SubscribeEvent
	public static void armorChanged(LivingEquipmentChangeEvent event) {
		LivingEntity living = event.getEntity();

		// from what I can see, vanilla doesnt have a hook for this in the item class. So this will have to do.
		// we only have to check equipping, when its unequipped the sound instance handles the rest

		//if we have a cicada in our curios slot, dont try to run this
		if (ModList.get().isLoaded("curios")) {
			if (CuriosCompat.isCicadaEquipped(living)) {
				return;
			}
		}

		if (living != null && !living.level().isClientSide() && event.getSlot() == EquipmentSlot.HEAD && event.getTo().is(TFBlocks.CICADA.get().asItem())) {
			TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> living), new CreateMovingCicadaSoundPacket(living.getId()));
		}
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		ItemStack stack = player.getItemInHand(event.getHand());
		if (stack.getItem() instanceof SpawnEggItem spawnEggItem && spawnEggItem.getType(stack.getTag()) == TFEntities.DEATH_TOME.get()) {
			BlockPos pos = event.getPos();
			BlockState state = event.getLevel().getBlockState(pos);
			if (state.getBlock() instanceof LecternBlock && !state.getValue(BlockStateProperties.HAS_BOOK)) {
				event.setCanceled(true);
				event.getLevel().playSound(null, pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);

				if (event.getLevel() instanceof ServerLevel serverLevel) {
					DeathTome tome = TFEntities.DEATH_TOME.get().spawn(serverLevel, stack, player, pos.below(), MobSpawnType.SPAWN_EGG, true, false);
					if (tome != null) {
						if (!player.getAbilities().instabuild) stack.shrink(1);
						serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
						tome.setOnLectern(true);
					}
				}

			}
		}
	}

	/**
	 * Spawn eggs set a sinister spawner's mob even when the player sneaks, as in 4.8. A sneaking player holding an item
	 * skips Block#use, and 1.20.1's SpawnEggItem#useOn would then spawn the mob next to the spawner, so the egg is
	 * handled here, before both.
	 */
	@SubscribeEvent
	public static void useSpawnEggOnSinisterSpawner(PlayerInteractEvent.RightClickBlock event) {
		InteractionResult result = SinisterSpawnerBlock.useSpawnEgg(event.getLevel(), event.getPos(), event.getEntity(), event.getItemStack());
		if (result != InteractionResult.PASS) {
			event.setCancellationResult(result);
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void washOffCloth(PlayerInteractEvent.RightClickBlock event) {
		BlockState state = event.getLevel().getBlockState(event.getPos());
		if (!state.is(Blocks.WATER_CAULDRON) || state.getValue(LayeredCauldronBlock.LEVEL) <= 0) return;
		ItemStack stack = event.getItemStack();
		if (ArmorUtil.hasEmperorsCloth(stack)) {
			LayeredCauldronBlock.lowerFillLevel(state, event.getLevel(), event.getPos());
			ArmorUtil.removeEmperorsCloth(stack);
			event.getEntity().awardStat(Stats.CLEAN_ARMOR);
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	/**
	 * Shrouded armor does not count as armor cover while invisible, so it no longer gives the wearer away to mobs.
	 * TF 4.8 patches LivingEntity#getVisibilityPercent to subtract the shrouded share from getArmorCoverPercentage();
	 * Forge 47 fires this event at the end of that method, so the same factor is corrected here instead.
	 */
	@SubscribeEvent
	public static void hideShroudedArmor(LivingEvent.LivingVisibilityEvent event) {
		LivingEntity entity = event.getEntity();
		if (!entity.isInvisible()) return;
		float shrouded = ArmorUtil.getShroudedArmorPercentage(entity);
		if (shrouded <= 0.0F) return;
		// vanilla multiplied the visibility by 0.7 * max(cover, 0.1); swap that cover for (cover - shrouded)
		float cover = entity.getArmorCoverPercentage();
		event.modifyVisibility(Math.max(cover - shrouded, 0.1F) / Math.max(cover, 0.1F));
	}
}
