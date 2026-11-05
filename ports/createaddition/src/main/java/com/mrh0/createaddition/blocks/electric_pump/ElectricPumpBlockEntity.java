package com.mrh0.createaddition.blocks.electric_pump;

import java.util.List;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.config.Config;
import com.mrh0.createaddition.energy.InternalEnergyStorage;
import com.mrh0.createaddition.sound.CASoundScapes;
import com.mrh0.createaddition.util.CALang;
import com.mrh0.createaddition.util.Util;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

// Backported from the 1.21.1 line (upstream 6cbf853, 9dd1b46 and the block entity part of 562d383).
// The pump sets its own speed with a scroll value and runs on FE. It has no kinetic connection, so it
// never joins a kinetic network and adds no stress.
public class ElectricPumpBlockEntity extends PumpBlockEntity {
	public static final float[][] PARTIAL_OFFSETS = {
			{8 / 16f, 4 / 16f, 8 / 16f},  // bottom face at y=2
			{8 / 16f, 12 / 16f, 8 / 16f}  // bottom face at y=10
	};

	public static final float[] PARTIAL_PHASE_OFFSETS = {6f, 0f};

	protected float pumpSpeed;
	protected ScrollValueBehaviour speedBehaviour;
	protected final InternalEnergyStorage energy;
	private LazyOptional<IEnergyStorage> lazyEnergy;

	private boolean active = false;

	public ElectricPumpBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		energy = new InternalEnergyStorage(Config.ELECTRIC_PUMP_CAPACITY.get(), Config.ELECTRIC_PUMP_MAX_INPUT.get(), 0);
		lazyEnergy = LazyOptional.of(() -> energy);
		setLazyTickRate(20);
	}

	// Upstream registers the storage for every side through RegisterCapabilitiesEvent, Forge 47 asks
	// the block entity.
	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.ENERGY) return lazyEnergy.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		lazyEnergy.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		lazyEnergy = LazyOptional.of(() -> energy);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		super.addBehaviours(behaviours);

		CenteredSideValueBoxTransform slot =
				new CenteredSideValueBoxTransform((pump, side) -> side.getAxis() != pump.getValue(ElectricPumpBlock.FACING).getAxis());

		speedBehaviour = new ScrollValueBehaviour(CreateLang.translateDirect("generic.speed"), this, slot);
		speedBehaviour.between(0, Config.ELECTRIC_PUMP_RPM_RANGE.get());
		speedBehaviour.value = 32;
		behaviours.add(speedBehaviour);
	}

	protected void applySpeed() {
		float target = active ? pumpSpeed : 0;
		if (target == speed) return;
		float prevSpeed = speed;
		speed = target;
		onSpeedChanged(prevSpeed);

		sendData();
	}

	// KineticBlockEntity.validateKinetics zeroes the speed of a block entity without a source whose
	// generated speed is 0, so the pump reports its own speed while it runs.
	@Override
	public float getGeneratedSpeed() {
		return active ? pumpSpeed : 0;
	}

	@Override
	public void attachKinetics() {
		updateSpeed = false;
	}

	@Override
	public void setSource(BlockPos source) {
	}

	@Override
	public float calculateStressApplied() {
		return 0f;
	}

	@Override
	public float calculateAddedStressCapacity() {
		return 0f;
	}

	// Upstream writes the unit as a lightning sign per tick. This fork keeps fe/t, as on the goggles of
	// the motor, the alternator and the connectors.
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		CALang.builder().add(Component.translatable(CreateAddition.MODID + ".tooltip.energy.consumption").withStyle(ChatFormatting.GRAY)).forGoggles(tooltip);
		CALang.builder().add(Component.literal(" " + Util.format(getEnergyConsumptionRate(pumpSpeed)) + "fe/t ")
				.withStyle(ChatFormatting.AQUA).append(CreateLang.translateDirect("gui.goggles.at_current_speed").withStyle(ChatFormatting.DARK_GRAY))).forGoggles(tooltip);
		return true;
	}

	public static int getEnergyConsumptionRate(float speed) {
		float magnitude = Math.abs(speed);
		if (magnitude <= 0) return 0;
		float effectiveSpeed = Math.max(magnitude, 32);
		return (int) Math.max(1, Math.round(Config.ELECTRIC_PUMP_FE_RPM.get() * effectiveSpeed / 256d));
	}

	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		energy.read(tag);
		active = tag.getBoolean("Active");
	}

	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		super.write(tag, clientPacket);
		energy.write(tag);
		tag.putBoolean("Active", active);
	}

	// As upstream, schematics keep the stored FE, since Create saves writeSafe into them. A printed
	// pump can only spend it on itself, because the storage extracts nothing.
	@Override
	public void writeSafe(CompoundTag tag) {
		super.writeSafe(tag);
		energy.write(tag);
		tag.putBoolean("Active", active);
	}

	@Override
	public void tick() {
		super.tick();

		float behaviourSpeed = speedBehaviour.getValue();
		if (behaviourSpeed != pumpSpeed) {
			pumpSpeed = behaviourSpeed;
			applySpeed();
		}

		if (level.isClientSide()) return;

		// Kept as upstream, without hysteresis. A pump that gets a little less FE than it draws runs on
		// what each tick brings and stops again, so it switches every one to three ticks. Each switch
		// runs PumpBlockEntity.updatePressureChange, two propagateChangedPipe searches of the pipe
		// network, and sends the block entity to the clients.
		int con = getEnergyConsumptionRate(pumpSpeed);
		boolean powered = getBlockState().getValue(ElectricPumpBlock.POWERED);
		boolean shouldRun = !powered && con > 0 && energy.getEnergyStored() >= con;

		if (shouldRun) energy.internalConsumeEnergy(con);

		if (shouldRun != active) {
			active = shouldRun;
			applySpeed();
		}
	}

	public float getPumpSpeed() {
		return pumpSpeed;
	}

	public int getEnergyConsumption() {
		return getEnergyConsumptionRate(pumpSpeed);
	}

	public boolean isActive() {
		return active;
	}

	// Used by upstream's ponder scene (562d383).
	public void setActive(boolean active) {
		this.active = active;
		applySpeed();
	}

	@Override
	public void tickAudio() {
		if (!active) return;
		if (Config.AUDIO_ENABLED.get())
			CASoundScapes.play(CASoundScapes.AmbienceGroup.DYNAMO, worldPosition, 1);
	}

	public static float getPulseScale(boolean active, float pumpSpeed, float renderTime, float phaseOffsetTicks) {
		if (!active) return 1f;
		float rate = Math.max(32f, Math.abs(pumpSpeed)) / 32f;
		return 1f + Mth.sin((renderTime + phaseOffsetTicks) * 0.1f * rate) * 0.1f;
	}

	public static Quaternionf getFacingRotation(Direction facing) {
		return new Quaternionf().rotateTo(0, 1, 0, facing.getStepX(), facing.getStepY(), facing.getStepZ());
	}
}
