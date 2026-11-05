package com.mrh0.createaddition.blocks.tesla_coil;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.config.Config;
import com.mrh0.createaddition.energy.BaseElectricBlockEntity;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CADamageTypes;
import com.mrh0.createaddition.index.CAEffects;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.index.CASounds;
import com.mrh0.createaddition.network.IObserveTileEntity;
import com.mrh0.createaddition.network.ObservePacket;
import com.mrh0.createaddition.network.TimeRemainingPacket;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.sound.CASoundScapes;
import com.mrh0.createaddition.util.CALang;
import com.mrh0.createaddition.util.Util;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TeslaCoilBlockEntity extends BaseElectricBlockEntity implements IHaveGoggleInformation, IObserveTileEntity {

	private Optional<ChargingRecipe> recipeCache = Optional.empty();

	private final ItemStackHandler inputInv;
	private int chargeAccumulator;
	protected int poweredTimer = 0;

	public TeslaCoilBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		inputInv = new ItemStackHandler(1);
	}

	@Override
	public int getCapacity() {
		return Util.max(Config.TESLA_COIL_CAPACITY.get(), Config.TESLA_COIL_CHARGE_RATE.get(), Config.TESLA_COIL_RECIPE_CHARGE_RATE.get());
	}

	@Override
	public int getMaxIn() {
		return Config.TESLA_COIL_MAX_INPUT.get();
	}

	@Override
	public int getMaxOut() {
		return 0;
	}

	public BeltProcessingBehaviour processingBehaviour;

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);
		processingBehaviour =
			new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> TeslaCoilBeltCallbacks.onItemReceived(s, i, this))
				.whileItemHeld((s, i) -> TeslaCoilBeltCallbacks.whenItemHeld(s, i, this));
		behaviours.add(processingBehaviour);
	}

	@Override
	public boolean isEnergyInput(Direction side) {
		return side != getBlockState().getValue(TeslaCoilBlock.FACING).getOpposite();
	}

	@Override
	public boolean isEnergyOutput(Direction side) {
		return false;
	}

	public int getConsumption() {
		return Config.TESLA_COIL_CHARGE_RATE.get();
	}

	protected float getItemCharge(IEnergyStorage energy) {
		if (energy == null) return 0f;
		return (float) energy.getEnergyStored() / (float) energy.getMaxEnergyStored();
	}

	protected BeltProcessingBehaviour.ProcessingResult onCharge(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
		BeltProcessingBehaviour.ProcessingResult res = chargeCompundAndStack(transported, handler);
		return res;
	}

	private void doDmg() {
		localEnergy.internalConsumeEnergy(Config.TESLA_COIL_HURT_ENERGY_REQUIRED.get());
		BlockPos origin = getBlockPos().relative(getBlockState().getValue(TeslaCoilBlock.FACING).getOpposite());
		List<LivingEntity> ents = getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(Config.TESLA_COIL_HURT_RANGE.get()));
		boolean zapped = false;
		for(LivingEntity e : ents) {
			if(e == null) return;

			boolean allChain = true;
			for(ItemStack armor : e.getArmorSlots()) {
				if(armor.is(Items.CHAINMAIL_BOOTS)) continue;
				if(armor.is(Items.CHAINMAIL_LEGGINGS)) continue;
				if(armor.is(Items.CHAINMAIL_CHESTPLATE)) continue;
				if(armor.is(Items.CHAINMAIL_HELMET)) continue;
				allChain = false;
				break;
			}
			if(allChain) continue;

			int dmg = Config.TESLA_COIL_HURT_DMG_MOB.get();
			int time = Config.TESLA_COIL_HURT_EFFECT_TIME_MOB.get();
			if(e instanceof Player) {
				dmg = Config.TESLA_COIL_HURT_DMG_PLAYER.get();
				time = Config.TESLA_COIL_HURT_EFFECT_TIME_PLAYER.get();
			}

			if(dmg > 0) {
				e.hurt(CADamageTypes.teslaCoil(level), dmg);
				if (!zapped) {
					if (Config.AUDIO_ENABLED.get()) level.playSound(null, worldPosition, CASounds.LOUD_ZAP.get(), SoundSource.BLOCKS, 0.6f, 1f);
					zapped = true;
				}
			}
			if(time > 0) e.addEffect(new MobEffectInstance(CAEffects.SHOCKING.get(), time));
		}
	}

	int dmgTick = 0;
	int zapTimer = 200;

	@Override
	public void tick() {
		super.tick();
		if(level == null) return;

		if(level.isClientSide()) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::tickAudio);
			return;
		}
		int signal = getLevel().getBestNeighborSignal(getBlockPos());
		if(signal > 0 && localEnergy.getEnergyStored() >= Config.TESLA_COIL_HURT_ENERGY_REQUIRED.get())
			poweredTimer = 10;

		dmgTick++;
		if((dmgTick%=Config.TESLA_COIL_HURT_FIRE_COOLDOWN.get()) == 0 && localEnergy.getEnergyStored() >= Config.TESLA_COIL_HURT_ENERGY_REQUIRED.get() && signal > 0)
			doDmg();

		if(poweredTimer > 0) {
			if (zapTimer == 0) {
				if (Config.AUDIO_ENABLED.get()) level.playSound(null, worldPosition, CASounds.LITTLE_ZAP.get(), SoundSource.BLOCKS, 0.1f, 1f);
				zapTimer = level.random.nextInt(100, 300);
			}
			zapTimer--;

			if(!isPoweredState())
				CABlocks.TESLA_COIL.get().setPowered(level, getBlockPos(), true);
			poweredTimer--;
		}
		else
			if(isPoweredState())
				CABlocks.TESLA_COIL.get().setPowered(level, getBlockPos(), false);
	}

	@OnlyIn(Dist.CLIENT)
	public void tickAudio() {
		if (!isPoweredState()) return;
		if (Config.AUDIO_ENABLED.get()) CASoundScapes.play(CASoundScapes.AmbienceGroup.TESLA, worldPosition, 1f);
	}

	public boolean isPoweredState() {
		return getBlockState().getValue(TeslaCoilBlock.POWERED);
	}

	protected BeltProcessingBehaviour.ProcessingResult chargeCompundAndStack(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {

		ItemStack stack = transported.stack;
		if(stack == null) return BeltProcessingBehaviour.ProcessingResult.PASS;
		if(chargeStack(stack, transported, handler)) {
			poweredTimer = 10;
			return BeltProcessingBehaviour.ProcessingResult.HOLD;
		}
		else if(chargeRecipe(stack, transported, handler)) {
			// Only show the powered state while the recipe really draws energy (upstream 7cb0ef9).
			if (energyRemoved > 0) poweredTimer = 10;
			return BeltProcessingBehaviour.ProcessingResult.HOLD;
		}
		return BeltProcessingBehaviour.ProcessingResult.PASS;
	}

	protected boolean chargeStack(ItemStack stack, TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
		if(!stack.getCapability(ForgeCapabilities.ENERGY).isPresent()) return false;
		IEnergyStorage es = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
		if(es.receiveEnergy(1, true) != 1) return false;
		if(localEnergy.getEnergyStored() < stack.getCount()) return false;
		localEnergy.internalConsumeEnergy(es.receiveEnergy(Math.min(getConsumption(), localEnergy.getEnergyStored()), false));
		return true;
	}

	// The energy of the last charging tick and the last 20 of them, for the powered state and the
	// charge time left on the goggles (upstream 7cb0ef9).
	private int energyRemoved = 0;
	private final int[] chargeRateHistory = new int[20];
	private int chargeRateIndex = 0;
	private int chargeRateSamples = 0;

	private boolean chargeRecipe(ItemStack stack, TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
		if(this.getLevel() == null) return false;
		if(!inputInv.getStackInSlot(0).is(stack.getItem())) {
			inputInv.setStackInSlot(0, stack);
			recipeCache = find(new RecipeWrapper(inputInv), this.getLevel());
			chargeAccumulator = 0;
			Arrays.fill(chargeRateHistory, 0);
			chargeRateIndex = 0;
			chargeRateSamples = 0;
		}
		if(recipeCache.isPresent()) {
			ChargingRecipe recipe = recipeCache.get();
			energyRemoved = localEnergy.internalConsumeEnergy(Util.min(Config.TESLA_COIL_RECIPE_CHARGE_RATE.get(), recipe.getEnergy() - chargeAccumulator, recipe.getMaxChargeRate()));
			chargeRateHistory[chargeRateIndex] = energyRemoved;
			chargeRateIndex = (chargeRateIndex + 1) % chargeRateHistory.length;
			if (chargeRateSamples < chargeRateHistory.length) chargeRateSamples++;
			chargeAccumulator += energyRemoved;
			if(chargeAccumulator >= recipe.getEnergy()) {
				TransportedItemStack remainingStack = transported.copy();
				TransportedItemStack result = transported.copy();
				result.stack = recipe.getResultItem(this.getLevel().registryAccess()).copy();
				remainingStack.stack.shrink(1);
				List<TransportedItemStack> outList = new ArrayList<>();
				outList.add(result);
				handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(outList, remainingStack));
				chargeAccumulator = 0;
				if (Config.AUDIO_ENABLED.get()) level.playSound(null, worldPosition, CASounds.LITTLE_ZAP.get(), SoundSource.BLOCKS, 0.1f, 1f);
			}
			return true;
		}
		return false;
	}

	public Optional<ChargingRecipe> find(RecipeWrapper wrapper, Level world) {
		return world.getRecipeManager().getRecipeFor(CARecipes.CHARGING_TYPE.get(), wrapper, world);
	}

	// The charge time left comes from the server as a TimeRemainingPacket, -1 while the coil gets no energy
	// (upstream 54cf0c9 and 7cb0ef9). Times of a second or less are not shown.
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (level == null) return false;
		ObservePacket.send(worldPosition, 0);
		int timeRemaining = TimeRemainingPacket.clientTimeRemaining;
		if (timeRemaining == 0 || timeRemaining > 0 && timeRemaining <= 20) return false;
		String timeStr = timeRemaining == -1 ? "\u221E" : Util.formatTime(timeRemaining);
		CALang.builder().add(Component.translatable(CreateAddition.MODID + ".tooltip.charging.info").withStyle(ChatFormatting.WHITE)).forGoggles(tooltip);
		CALang.builder().add(Component.literal(" ").append(Component.translatable(CreateAddition.MODID + ".tooltip.charging.time_remaining").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(" " + timeStr).withStyle(ChatFormatting.AQUA))).forGoggles(tooltip);
		return true;
	}

	@Override
	public void onObserved(ServerPlayer player, ObservePacket pkt) {
		int timeRemaining = 0;
		if (recipeCache.isPresent() && chargeRateSamples > 0) {
			ChargingRecipe recipe = recipeCache.get();
			int totalRate = 0;
			for (int rate : chargeRateHistory) totalRate += rate;
			int avgChargeRate = totalRate / chargeRateSamples;
			if (avgChargeRate == 0) {
				TimeRemainingPacket.send(-1, player);
				return;
			}
			timeRemaining = (recipe.getEnergy() - chargeAccumulator) / avgChargeRate;
		}
		TimeRemainingPacket.send(timeRemaining, player);
	}
}
