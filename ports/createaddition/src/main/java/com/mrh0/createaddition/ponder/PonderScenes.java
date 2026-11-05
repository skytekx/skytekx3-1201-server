package com.mrh0.createaddition.ponder;

import com.mrh0.createaddition.blocks.connector.SmallConnectorBlockEntity;
import com.mrh0.createaddition.blocks.connector.base.AbstractConnectorBlock;
import com.mrh0.createaddition.blocks.connector.base.ConnectorMode;
import com.mrh0.createaddition.blocks.electric_pump.ElectricPumpBlockEntity;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerBlock;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceBlockEntity;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlock;
import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.WireType;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CAFluids;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.util.CALang;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class PonderScenes {
	public static void electricMotor(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("electric_motor", CALang.translateDirect("ponder.electric_motor.header").getString());
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);

		BlockPos motor = util.grid().at(3, 1, 2);

		for (int i = 0; i < 3; i++) {
			scene.idle(5);
			scene.world().showSection(util.select().position(1 + i, 1, 2), Direction.DOWN);
		}

		scene.idle(10);
		scene.effects().rotationDirectionIndicator(motor);
		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.electric_motor.text_1").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(motor));
		scene.idle(50);


		scene.rotateCameraY(90);
		scene.idle(20);

		Vec3 blockSurface = util.vector().blockSurface(motor, Direction.EAST);
		AABB point = new AABB(blockSurface, blockSurface);
		AABB expanded = point.inflate(1 / 16f, 1 / 5f, 1 / 5f);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, blockSurface, point, 1);
		scene.idle(1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, blockSurface, expanded, 60);
		scene.overlay().showControls(blockSurface, Pointing.DOWN, 60).scroll();
		scene.idle(20);

		scene.addKeyframe();
		scene.overlay().showText(70)
			.text(CALang.translateDirect("ponder.electric_motor.text_2").getString())
			.placeNearTarget()
			.pointAt(blockSurface);
		scene.idle(10);
		scene.world().modifyKineticSpeed(util.select().fromTo(1, 1, 2, 3, 1, 2), f -> (4 * f));
		scene.effects().rotationSpeedIndicator(motor);
		scene.idle(70);

		scene.addKeyframe();
		scene.overlay().showText(70)
		.text(CALang.translateDirect("ponder.electric_motor.text_3").getString())
		.placeNearTarget()
		.pointAt(blockSurface);
		scene.idle(80);

		scene.overlay().showText(70)
		.text(CALang.translateDirect("ponder.electric_motor.text_4").getString())
		.placeNearTarget()
		.pointAt(blockSurface);
		scene.idle(80);
		scene.markAsFinished();


		scene.rotateCameraY(-90);
	}

	public static void alternator(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("alternator", CALang.translateDirect("ponder.alternator.header").getString());
		scene.configureBasePlate(1, 0, 4);
		scene.world().showSection(util.select().layer(0), Direction.UP);

		BlockPos generator = util.grid().at(3, 1, 2);

		for (int i = 0; i < 6; i++) {
			scene.idle(5);
			scene.world().showSection(util.select().position(i, 1, 2), Direction.DOWN);
			//scene.world().showSection(util.select().position(i, 2, 2), Direction.DOWN);
		}

		scene.idle(10);
		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.alternator.text_2").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(generator));
		scene.idle(60);

		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.alternator.text_1").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(generator));
		scene.idle(60);


		scene.overlay().showText(50)
		.text(CALang.translateDirect("ponder.alternator.text_3").getString())
		.placeNearTarget()
		.pointAt(util.vector().topOf(generator));
		scene.idle(60);
		scene.markAsFinished();
	}

	public static void rollingMill(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("rolling_mill", CALang.translateDirect("ponder.rolling_mill.header").getString());
		scene.configureBasePlate(1, 0, 4);
		scene.world().showSection(util.select().layer(0), Direction.UP);

		BlockPos mill = util.grid().at(3, 1, 2);

		for (int i = 0; i < 6; i++) {
			scene.idle(5);
			scene.world().showSection(util.select().position(i, 1, 2), Direction.DOWN);
		}

		scene.idle(10);
		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.rolling_mill.text_1").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(mill));
		scene.idle(60);

		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.rolling_mill.text_2").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(mill));
		scene.idle(60);

		scene.addKeyframe();
		scene.overlay().showControls(util.vector().topOf(mill), Pointing.DOWN, 50).rightClick();
		scene.overlay().showText(50)
		.text(CALang.translateDirect("ponder.rolling_mill.text_3").getString())
		.placeNearTarget()
		.pointAt(util.vector().topOf(mill));
		scene.idle(60);
		scene.markAsFinished();
	}

	public static void automateRollingMill(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("automated_rolling_mill", CALang.translateDirect("ponder.automated_rolling_mill.header").getString());
		scene.configureBasePlate(1, 0, 4);
		scene.world().showSection(util.select().layer(0), Direction.UP);

		BlockPos mill = util.grid().at(3, 2, 2);
		BlockPos in = util.grid().at(3, 2, 3);
		BlockPos out = util.grid().at(3, 2, 1);

		//BlockPos entryBeltPos = util.grid().at(3, 1, 4);
		//BlockPos exitBeltPos = util.grid().at(3, 1, 0);

		for (int i = 0; i < 3; i++) {
			scene.idle(5);
			scene.world().showSection(util.select().position(i, 1, 4), Direction.DOWN);
		}

		for (int i = 5; i >= 0; i--) {
			scene.idle(5);
			scene.world().showSection(util.select().position(3, 1, i), Direction.DOWN);
			//scene.world().showSection(util.select().position(3, 2, i), Direction.DOWN);
			scene.world().showSection(util.select().position(4, 1, i), Direction.DOWN);
			scene.world().showSection(util.select().position(4, 2, i), Direction.DOWN);
		}

		scene.world().showSection(util.select().position(mill), Direction.DOWN);

		scene.addKeyframe();
		scene.overlay().showText(50)
		.text(CALang.translateDirect("ponder.automated_rolling_mill.text_1").getString())
		.placeNearTarget()
		.pointAt(util.vector().topOf(mill));
		scene.idle(60);

		scene.idle(5);
		scene.world().showSection(util.select().position(in), Direction.NORTH);
		scene.idle(5);
		scene.world().showSection(util.select().position(out), Direction.SOUTH);
		scene.idle(20);
		scene.markAsFinished();
	}

	public static void ccMotor(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("cc_electric_motor", CALang.translateDirect("ponder.cc_electric_motor.header").getString());
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);

		BlockPos motor = util.grid().at(2, 1, 2);
		BlockPos computer = util.grid().at(1, 1, 2);

		scene.idle(5);
		scene.world().showSection(util.select().position(motor), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(util.select().position(computer), Direction.DOWN);

		scene.idle(10);
		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.cc_electric_motor.text_1").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(computer));
		scene.idle(60);

		scene.idle(10);
		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.cc_electric_motor.text_2").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(computer));
		scene.idle(60);

		scene.addKeyframe();
		scene.idle(10);
		scene.overlay().showText(150)
			.text(CALang.translateDirect("ponder.cc_electric_motor.text_3").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(computer));
		scene.idle(160);
		scene.markAsFinished();
	}

	public static void teslaCoil(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("tesla_coil", CALang.translateDirect("ponder.tesla_coil.header").getString());
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		scene.world().setBlock(util.grid().at(3, 2, 2), Blocks.WATER.defaultBlockState(), false);

		BlockPos depotPos = util.grid().at(2, 1, 2);
		scene.world().showSection(util.select().position(2, 1, 2), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(util.select().position(2, 3, 2), Direction.DOWN);
		scene.idle(5);
		Vec3 topOf = util.vector().topOf(depotPos);
		scene.overlay().showText(50)
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.tesla_coil.text_1").getString())
			.placeNearTarget()
			.pointAt(topOf);
		scene.idle(60);

		scene.world().createItemOnBeltLike(depotPos, Direction.NORTH, AllItems.CHROMATIC_COMPOUND.asStack());
		scene.idle(10);
		scene.world().setBlock(util.grid().at(2, 3, 2), CABlocks.TESLA_COIL.getDefaultState().setValue(TeslaCoilBlock.FACING, Direction.UP).setValue(TeslaCoilBlock.POWERED, true), false);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.tesla_coil.text_2").getString())
			.placeNearTarget()
			.pointAt(topOf);
		scene.idle(80);
		scene.markAsFinished();
	}

	public static void teslaCoilHurt(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("tesla_coil_hurt", CALang.translateDirect("ponder.tesla_coil_hurt.header").getString());
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		//scene.world().setBlock(util.grid().at(3, 2, 2), Blocks.WATER.defaultBlockState(), false);

		BlockPos teslacoil = util.grid().at(2, 1, 2);
		BlockPos lever = util.grid().at(2, 1, 1);
		scene.world().showSection(util.select().position(teslacoil), Direction.DOWN);
		scene.idle(5);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.tesla_coil_hurt.text_1").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(teslacoil));
		scene.idle(80);
		scene.world().showSection(util.select().position(lever), Direction.SOUTH);

		scene.idle(5);
		scene.overlay().showText(50)
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.tesla_coil_hurt.text_2").getString())
			.placeNearTarget()
			.pointAt(util.vector().centerOf(lever));
		scene.idle(60);
		scene.world().setBlock(lever, Blocks.LEVER.defaultBlockState().setValue(LeverBlock.POWERED, true).setValue(LeverBlock.FACING, Direction.SOUTH).setValue(LeverBlock.FACE, AttachFace.FLOOR), false);
		scene.idle(5);
		scene.world().setBlock(teslacoil, CABlocks.TESLA_COIL.getDefaultState().setValue(TeslaCoilBlock.FACING, Direction.DOWN).setValue(TeslaCoilBlock.POWERED, true), false);
		scene.idle(5);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.tesla_coil_hurt.text_3").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(teslacoil));
		scene.idle(80);
		scene.markAsFinished();
	}

	public static void liquidBlazeBurner(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("liquid_blaze_burner", CALang.translateDirect("ponder.liquid_blaze_burner.header").getString());
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);
		//scene.world().setBlock(util.grid().at(3, 2, 2), Blocks.WATER.defaultBlockState(), false);

		BlockPos burner = util.grid().at(2, 1, 2);
		BlockPos[] blocks = {
				util.grid().at(1, 1, 2),
				util.grid().at(0, 1, 2),
				util.grid().at(0, 2, 2),
				util.grid().at(0, 3, 2)
		};
		scene.world().showSection(util.select().position(burner), Direction.DOWN);
		scene.idle(5);
		scene.overlay().showText(50)
		.attachKeyFrame()
		.text(CALang.translateDirect("ponder.liquid_blaze_burner.text_1").getString())
		.placeNearTarget()
		.pointAt(util.vector().topOf(burner));
		scene.idle(10);
		scene.overlay().showControls(util.vector().topOf(burner), Pointing.DOWN, 40)
				.rightClick()
				.withItem(new ItemStack(CAItems.STRAW.get()));
		scene.world().setBlock(burner, CABlocks.LIQUID_BLAZE_BURNER.getDefaultState().setValue(LiquidBlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.SMOULDERING), false);
		scene.idle(60);
		scene.overlay().showText(50)
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.liquid_blaze_burner.text_2").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(burner));
		scene.idle(10);
		scene.overlay().showControls(util.vector().topOf(burner), Pointing.DOWN, 40)
				.rightClick()
				.withItem(new ItemStack(CAFluids.BIOETHANOL.getBucket().get()));
		scene.idle(60);
		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.liquid_blaze_burner.text_3").getString())
			.placeNearTarget()
			.pointAt(util.vector().topOf(burner));
		scene.idle(10);

		for (int i = 0; i < blocks.length; i++) {
			scene.idle(5);
			scene.world().showSection(util.select().position(blocks[i]), Direction.EAST);
		}
		scene.idle(20);
		scene.markAsFinished();
	}


	public static void modularAccumulator(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("modular_accumulator", CALang.translateDirect("ponder.modular_accumulator.header").getString());
		scene.configureBasePlate(0, 0, 4);
		scene.showBasePlate();
		scene.idle(15);

		BlockPos cIn = new BlockPos(1, 3, 1);
		BlockPos cOut = new BlockPos(2, 3, 2);

		var accumulator = util.select().fromTo(1, 1, 1, 2, 2, 2);
		//scene.world().showSection(accumulator, Direction.EAST);
		ElementLink<WorldSectionElement> accumulatorLink = scene.world().showIndependentSection(accumulator, Direction.EAST);
		scene.idle(15);
		scene.overlay().showOutline(PonderPalette.GREEN, accumulatorLink, accumulator, 50);

		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.modular_accumulator.text_1").getString())
			.placeNearTarget()
			.pointAt(util.vector().centerOf(cIn));
		scene.idle(60);
		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.modular_accumulator.text_2").getString())
			.placeNearTarget()
			.pointAt(util.vector().centerOf(cIn));
		scene.idle(60);
		scene.world().showSection(util.select().position(cIn), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(util.select().position(cOut), Direction.DOWN);

		scene.idle(15);
		scene.overlay().showControls(util.vector().centerOf(cIn), Pointing.DOWN, 0).rightClick()
				.withItem(new ItemStack(AllItems.WRENCH.get()));
		scene.world().setBlock(cIn, CABlocks.SMALL_CONNECTOR.getDefaultState().setValue(AbstractConnectorBlock.FACING, Direction.DOWN).setValue(AbstractConnectorBlock.MODE, ConnectorMode.Push), false);
		scene.overlay().showText(50)
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.modular_accumulator.text_3").getString())
			.placeNearTarget()
			.pointAt(util.vector().centerOf(cIn));

		scene.idle(60);
		scene.overlay().showControls(util.vector().centerOf(cOut), Pointing.DOWN, 40)
				.rightClick()
				.withItem(new ItemStack(AllItems.WRENCH.get()));
		scene.world().setBlock(cOut, CABlocks.SMALL_CONNECTOR.getDefaultState().setValue(AbstractConnectorBlock.FACING, Direction.DOWN).setValue(AbstractConnectorBlock.MODE, ConnectorMode.Pull), false);
		scene.overlay().showText(50)
			.text(CALang.translateDirect("ponder.modular_accumulator.text_4").getString())
			.placeNearTarget()
			.pointAt(util.vector().centerOf(cOut));
		scene.idle(60);

		scene.overlay().showText(110)
		.text(CALang.translateDirect("ponder.modular_accumulator.text_5").getString())
		.placeNearTarget()
		.pointAt(util.vector().centerOf(cOut));
		scene.idle(120);
		scene.markAsFinished();
	}

	public static void peiTransfer(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("pei_transfer", CALang.translateDirect("ponder.pei_transfer.header").getString());
		scene.configureBasePlate(0, 0, 6);
		scene.scaleSceneView(0.95f);
		scene.setSceneOffsetY(-1);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos bearing = util.grid().at(5, 1, 2);
		scene.world().showSection(util.select().position(bearing), Direction.DOWN);
		scene.idle(5);
		ElementLink<WorldSectionElement> contraption =
			scene.world().showIndependentSection(util.select().fromTo(5, 2, 2, 6, 4, 2), Direction.DOWN);
		scene.world().configureCenterOfRotation(contraption, util.vector().centerOf(bearing));
		scene.idle(10);
		scene.world().rotateBearing(bearing, 360, 70);
		scene.world().rotateSection(contraption, 0, 360, 0, 70);
		scene.overlay().showText(60)
			.pointAt(util.vector().topOf(bearing.above(2)))
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.pei_transfer.text_1").getString());

		scene.idle(70);
		BlockPos pei = util.grid().at(4, 2, 2);
		scene.world().showSectionAndMerge(util.select().position(pei), Direction.EAST, contraption);
		scene.idle(13);
		scene.effects().superGlue(pei, Direction.EAST, true);

		scene.overlay().showText(80)
			.pointAt(util.vector().topOf(pei))
			.colored(PonderPalette.GREEN)
			.placeNearTarget()
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.pei_transfer.text_2").getString());
		scene.idle(90);

		BlockPos pei2 = pei.west(2);
		scene.world().showSection(util.select().position(pei2), Direction.DOWN);
		scene.overlay().showOutlineWithText(util.select().position(pei.west()), 50)
			.colored(PonderPalette.RED)
			.placeNearTarget()
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.pei_transfer.text_3").getString());
		scene.idle(55);

		scene.world().rotateBearing(bearing, 360, 60);
		scene.world().rotateSection(contraption, 0, 360, 0, 60);
		scene.idle(20);

		scene.overlay().showText(40)
			.placeNearTarget()
			.pointAt(util.vector().of(3, 3, 2.5))
			.text(CALang.translateDirect("ponder.pei_transfer.text_4").getString());
		scene.idle(35);

		Selection both = util.select().fromTo(2, 2, 2, 4, 2, 2);
		Class<PortableEnergyInterfaceBlockEntity> peiClass = PortableEnergyInterfaceBlockEntity.class;

		scene.world().modifyBlockEntityNBT(both, peiClass, nbt -> {
			nbt.putFloat("Distance", 1);
			nbt.putFloat("Timer", 40);
		});

		scene.idle(20);
		scene.overlay().showOutline(PonderPalette.GREEN, pei, util.select().fromTo(5, 3, 2, 6, 4, 2), 80);
		scene.idle(10);

		scene.overlay().showOutlineWithText(util.select().position(pei2), 70)
			.placeNearTarget()
			.colored(PonderPalette.GREEN)
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.pei_transfer.text_5").getString());

		scene.idle(80);

		BlockPos connector = util.grid().at(2, 3, 2);
		scene.world().showSection(util.select().position(connector), Direction.DOWN);
		scene.overlay().showText(70)
			.placeNearTarget()
			.pointAt(util.vector().centerOf(connector))
			.attachKeyFrame()
			.text(CALang.translateDirect("ponder.pei_transfer.text_6").getString());
		scene.idle(80);

		scene.overlay().showText(120)
			.placeNearTarget()
			.pointAt(util.vector().centerOf(pei2))
			.text(CALang.translateDirect("ponder.pei_transfer.text_7").getString());
		scene.world().modifyBlockEntityNBT(both, peiClass, nbt -> nbt.putFloat("Timer", 9));

		scene.idle(15);
		scene.world().rotateBearing(bearing, 270, 120);
		scene.world().rotateSection(contraption, 0, 270, 0, 120);
		scene.markAsFinished();
	}

	public static void peiRedstone(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("pei_redstone", CALang.translateDirect("ponder.pei_redstone.header").getString());
		scene.configureBasePlate(0, 0, 5);
		scene.setSceneOffsetY(-1);

		Class<PortableEnergyInterfaceBlockEntity> peiClass = PortableEnergyInterfaceBlockEntity.class;
		Selection peis = util.select().fromTo(1, 1, 3, 1, 3, 3);
		scene.world().modifyBlockEntityNBT(peis, peiClass, nbt -> {
			nbt.putFloat("Distance", 1);
			nbt.putFloat("Timer", 40);
		});

		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);
		scene.world().showSection(util.select().layer(1), Direction.DOWN);
		scene.idle(5);

		ElementLink<WorldSectionElement> contraption =
			scene.world().showIndependentSection(util.select().layersFrom(2), Direction.DOWN);
		BlockPos bearing = util.grid().at(3, 1, 3);
		scene.world().configureCenterOfRotation(contraption, util.vector().topOf(bearing));
		scene.idle(20);
		scene.world().modifyBlockEntityNBT(peis, peiClass, nbt -> nbt.putFloat("Timer", 9));
		scene.idle(20);
		scene.world().rotateBearing(bearing, 360 * 3 + 270, 240 + 60);
		scene.world().rotateSection(contraption, 0, 360 * 3 + 270, 0, 240 + 60);
		scene.idle(20);

		scene.world().toggleRedstonePower(util.select().fromTo(1, 1, 1, 1, 1, 2));
		scene.effects().indicateRedstone(util.grid().at(1, 1, 1));

		scene.idle(10);

		scene.overlay().showOutlineWithText(util.select().position(1, 1, 3), 120)
			.colored(PonderPalette.RED)
			.text(CALang.translateDirect("ponder.pei_redstone.text_1").getString());

		scene.idle(20);
		scene.markAsFinished();
	}

	public static void electricPumpflow(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("electric_pump_flow", "Fluid Transportation using Electric Pumps");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.world().multiplyKineticSpeed(util.select().everywhere(), -1);
		scene.idle(5);

		BlockPos pumpPos = util.grid().at(2, 1, 1);
		Selection tank1 = util.select().fromTo(0, 2, 3, 0, 1, 3);
		Selection tank2 = util.select().fromTo(4, 2, 3, 4, 1, 3);
		Selection pipes = util.select().fromTo(3, 1, 3, 1, 1, 1);
		Selection pump = util.select().position(pumpPos);
		BlockPos connectorPos = pumpPos.above();
		Selection connector = util.select().position(connectorPos);
		BlockPos connector2Pos = connectorPos.offset(2, 0, -1);
		BlockPos accumulatorPos = connector2Pos.below();
		Selection connector2 = util.select().position(connector2Pos);
		Selection accumulator = util.select().position(accumulatorPos);

		scene.world().setBlock(pumpPos, AllBlocks.FLUID_PIPE.get()
			.getAxisState(Axis.X), false);

		scene.world().showSection(tank1, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(tank2, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(pipes, Direction.NORTH);
		scene.idle(5);
		
		scene.world().destroyBlock(pumpPos);
		scene.world().restoreBlocks(pump);
		scene.world().modifyBlock(pumpPos, s -> s.setValue(PumpBlock.FACING, s.getValue(PumpBlock.FACING).getOpposite()), false);
		scene.world().setKineticSpeed(pump, 0);

		scene.idle(15);

		scene.overlay().showText(60)
			.text("Electric Pumps govern the flow of their attached pipe networks")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(pumpPos));

		scene.idle(70);
		scene.world().showSection(connector, Direction.DOWN);
		scene.world().showSection(connector2, Direction.DOWN);
		scene.world().showSection(accumulator, Direction.DOWN);
		scene.world().modifyBlockEntity(connectorPos, SmallConnectorBlockEntity.class, be -> {
			Level level = be.getLevel();
			if (level != null)
				IWireNode.connect(level, connectorPos, be.getAvailableNode(), connector2Pos, 0, WireType.COPPER);
		});
		scene.idle(15);
		scene.world().setKineticSpeed(pump, 64);
		scene.world().modifyBlockEntity(pumpPos, ElectricPumpBlockEntity.class, be -> {
			be.getBehaviour(ScrollValueBehaviour.TYPE).setValue(64);
			be.setActive(true);
		});
		scene.world().propagatePipeChange(pumpPos);
		scene.effects().rotationDirectionIndicator(pumpPos.north());
		scene.idle(15);

		scene.overlay().showText(60)
			.text("Their arrow indicates the direction of flow")
			.attachKeyFrame()
			.placeNearTarget()
			.pointAt(util.vector().topOf(pumpPos)
				.subtract(0.5f, 0.125f, 0));

		AABB bb1 = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.25, .25, 0)
			.move(0, 0, .25);
		AABB bb2 = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.25, .25, 1.25);
		scene.idle(65);

		Object in = new Object();
		Object out = new Object();

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, in, bb1.move(util.vector().centerOf(3, 1, 3)), 3);
		scene.idle(2);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, in, bb2.move(util.vector().centerOf(3, 1, 2)), 50);
		scene.idle(10);

		scene.overlay().showText(50)
			.text("The network behind is now pulling fluids...")
			.attachKeyFrame()
			.placeNearTarget()
			.colored(PonderPalette.INPUT)
			.pointAt(util.vector().centerOf(3, 1, 2));

		scene.idle(60);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, bb1.move(util.vector().centerOf(1, 1, 1)
			.add(0, 0, -.5)), 3);
		scene.idle(2);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, bb2.move(util.vector().centerOf(1, 1, 2)), 50);
		scene.idle(10);

		scene.overlay().showText(50)
			.text("...while the network in front is transferring it outward")
			.placeNearTarget()
			.colored(PonderPalette.OUTPUT)
			.pointAt(util.vector().centerOf(1, 1, 2));

		scene.idle(25);

		scene.overlay().showControls(util.vector().topOf(pumpPos), Pointing.DOWN, 40).rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.idle(7);
		scene.world().modifyBlock(pumpPos, s -> s.setValue(PumpBlock.FACING, Direction.EAST), true);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.pointAt(util.vector().centerOf(2, 1, 1))
			.placeNearTarget()
			.text("A Wrench can be used to reverse the direction");
		scene.world().propagatePipeChange(pumpPos);
		scene.idle(40);

		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, in, bb1.move(util.vector().centerOf(3, 1, 3)), 3);
		scene.idle(2);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.INPUT, in, bb2.move(util.vector().centerOf(3, 1, 2)), 30);
		scene.idle(15);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, bb1.move(util.vector().centerOf(1, 1, 1)
			.add(0, 0, -.5)), 3);
		scene.idle(2);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.OUTPUT, out, bb2.move(util.vector().centerOf(1, 1, 2)), 30);
		scene.idle(25);

	}

	public static void connector(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("connector", "Transferring Energy using Connectors");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		BlockPos alternator = util.grid().at(4, 1, 2);
		BlockPos motor = util.grid().at(1, 1, 1);
		BlockPos pull = alternator.above();
		BlockPos push = motor.above();
		BlockPos relay = util.grid().at(2, 3, 3);

		Selection largeCog = util.select().position(5, 0, 1);
		Selection alternatorKinetics = util.select().fromTo(4, 1, 2, 5, 1, 2);
		Selection motorKinetics = util.select().fromTo(1, 1, 0, 1, 1, 1);

		// The structure is saved with its wires attached, take them down until the relay is shown.
		scene.world().modifyBlockEntity(relay, SmallConnectorBlockEntity.class, be -> {
			Level level = be.getLevel();
			if (level == null) return;
			IWireNode.disconnect(level, relay, pull);
			IWireNode.disconnect(level, relay, push);
		});
		scene.world().setKineticSpeed(largeCog, 32);
		scene.world().setKineticSpeed(alternatorKinetics, -64);
		scene.idle(5);

		scene.world().showSection(alternatorKinetics.add(largeCog), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(motorKinetics, Direction.DOWN);
		scene.idle(10);

		scene.overlay().showText(60)
			.text("The Alternator is a producer of energy...")
			.placeNearTarget()
			.pointAt(util.vector().topOf(alternator));
		scene.idle(70);

		scene.overlay().showText(60)
			.text("...while the Electric Motor is a consumer of energy")
			.placeNearTarget()
			.pointAt(util.vector().topOf(motor));
		scene.idle(70);

		scene.world().showSection(util.select().position(pull), Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("Producers need a connector in Pull mode, which pulls energy out of the block it is attached to")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(pull));
		scene.idle(90);

		scene.world().showSection(util.select().position(push), Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("Consumers need a connector in Push mode, which pushes energy into the block it is attached to")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(push));
		scene.idle(90);

		scene.overlay().showControls(util.vector().topOf(push), Pointing.DOWN, 50).rightClick()
			.withItem(AllItems.WRENCH.asStack());
		scene.overlay().showText(50)
			.text("Use a Wrench on a connector to cycle its mode")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(push));
		scene.idle(60);

		scene.world().showSection(util.select().fromTo(2, 1, 3, 2, 2, 3), Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(util.select().position(relay), Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text("Connectors in None mode do not interact with the block they are attached to...")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(relay));
		scene.idle(80);

		scene.overlay().showText(70)
			.text("...they only relay energy through their wires, so they can be placed on any block")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(relay));
		scene.idle(80);

		scene.overlay().showText(110)
			.attachKeyFrame()
			.text("Connect two connectors with a wire by right-clicking both using a Copper Spool")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(relay));
		scene.idle(10);

		scene.overlay().showControls(util.vector().topOf(relay), Pointing.DOWN, 20).rightClick()
			.withItem(CAItems.COPPER_SPOOL.asStack());
		scene.idle(25);
		scene.overlay().showControls(util.vector().topOf(pull), Pointing.DOWN, 20).rightClick()
			.withItem(CAItems.COPPER_SPOOL.asStack());
		scene.idle(5);
		scene.world().modifyBlockEntity(relay, SmallConnectorBlockEntity.class, be -> {
			Level level = be.getLevel();
			if (level != null)
				IWireNode.connect(level, relay, 1, pull, 0, WireType.COPPER);
		});
		scene.idle(20);

		scene.overlay().showControls(util.vector().topOf(relay), Pointing.DOWN, 20).rightClick()
			.withItem(CAItems.COPPER_SPOOL.asStack());
		scene.idle(25);
		scene.overlay().showControls(util.vector().topOf(push), Pointing.DOWN, 20).rightClick()
			.withItem(CAItems.COPPER_SPOOL.asStack());
		scene.idle(5);
		scene.world().modifyBlockEntity(relay, SmallConnectorBlockEntity.class, be -> {
			Level level = be.getLevel();
			if (level != null)
				IWireNode.connect(level, relay, 0, push, 0, WireType.COPPER);
		});
		scene.idle(25);

		scene.world().setKineticSpeed(motorKinetics, -32);
		scene.effects().rotationDirectionIndicator(motor.north());
		scene.overlay().showText(60)
			.attachKeyFrame()
			.text("Energy now flows from the Alternator to the Electric Motor")
			.placeNearTarget()
			.pointAt(util.vector().topOf(motor));
		scene.idle(70);
		scene.markAsFinished();
	}

	public static void connectorRemove(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("connector_remove", "Removing Wires using an Empty Spool");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();

		BlockPos motor = util.grid().at(1, 1, 1);
		BlockPos push = motor.above();
		BlockPos relay = util.grid().at(2, 3, 3);

		Selection largeCog = util.select().position(5, 0, 1);
		Selection motorKinetics = util.select().fromTo(1, 1, 0, 1, 1, 1);

		scene.world().setKineticSpeed(largeCog, 32);
		scene.world().setKineticSpeed(util.select().fromTo(4, 1, 2, 5, 1, 2), -64);
		scene.world().setKineticSpeed(motorKinetics, -32);
		scene.idle(5);

		scene.world().showSection(util.select().layersFrom(1).add(largeCog), Direction.DOWN);
		scene.idle(20);

		scene.overlay().showText(90)
			.text("Right-click both ends of a wire with an Empty Spool to remove it")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(push));
		scene.idle(20);

		scene.overlay().showControls(util.vector().topOf(push), Pointing.DOWN, 20).rightClick()
			.withItem(CAItems.SPOOL.asStack());
		scene.idle(25);
		scene.overlay().showControls(util.vector().topOf(relay), Pointing.DOWN, 20).rightClick()
			.withItem(CAItems.SPOOL.asStack());
		scene.idle(5);
		scene.world().modifyBlockEntity(relay, SmallConnectorBlockEntity.class, be -> {
			Level level = be.getLevel();
			if (level != null)
				IWireNode.disconnect(level, relay, push);
		});
		scene.idle(20);
		scene.world().setKineticSpeed(motorKinetics, 0);
		scene.idle(30);

		scene.overlay().showText(60)
			.text("The wire is returned as a filled Spool")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(relay));
		scene.idle(70);
		scene.markAsFinished();
	}

	public static void servoMotor(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("servo_motor", "Rotating Blocks to precise Angles using the Servo Motor");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(5);

		BlockPos servo = util.grid().at(2, 1, 1);
		BlockPos connector = util.grid().at(2, 1, 2);
		Selection minLever = util.select().position(1, 1, 1);
		Selection maxLever = util.select().position(3, 1, 1);
		Vec3 minLeverVec = util.vector().centerOf(1, 1, 1).add(0, -.25, 0);
		Vec3 maxLeverVec = util.vector().centerOf(3, 1, 1).add(0, -.25, 0);

		scene.world().showSection(util.select().position(servo), Direction.DOWN);
		scene.idle(15);

		scene.overlay().showText(60)
			.text("The Servo Motor uses redstone to rotate attached blocks to a precise angle")
			.placeNearTarget()
			.pointAt(util.vector().topOf(servo));
		scene.idle(70);

		scene.world().showSection(util.select().position(connector)
			.add(util.select().fromTo(4, 1, 3, 4, 2, 3)), Direction.DOWN);
		scene.idle(15);
		scene.overlay().showText(50)
			.text("It requires a source of energy (fe) to operate")
			.placeNearTarget()
			.pointAt(util.vector().centerOf(connector));
		scene.idle(60);

		ElementLink<WorldSectionElement> observer =
			scene.world().showIndependentSection(util.select().position(servo.above()), Direction.DOWN);
		scene.world().configureCenterOfRotation(observer, util.vector().centerOf(servo));
		scene.idle(15);

		scene.world().showSection(minLever, Direction.DOWN);
		scene.idle(5);
		scene.world().showSection(maxLever, Direction.DOWN);
		scene.idle(15);

		// Max side 15, min side 0: 0° -> 90°
		scene.addKeyframe();
		scene.overlay().showControls(maxLeverVec, Pointing.DOWN, 30).rightClick();
		scene.idle(7);
		rotateServo(scene, servo, observer, 90, 30);
		turnLever(scene, maxLever, 0, 15);
		scene.overlay().showText(60)
			.text("Redstone power on this side turns the Servo towards its Max Angle...")
			.placeNearTarget()
			.pointAt(maxLeverVec);
		scene.idle(70);

		// Max side 0: back to 0°
		scene.overlay().showControls(maxLeverVec, Pointing.DOWN, 30).rightClick().whileSneaking();
		scene.idle(7);
		rotateServo(scene, servo, observer, -90, 30);
		turnLever(scene, maxLever, 15, 0);
		scene.idle(10);

		// Min side 15: 0° -> -90°
		scene.addKeyframe();
		scene.overlay().showControls(minLeverVec, Pointing.DOWN, 30).rightClick();
		scene.idle(7);
		rotateServo(scene, servo, observer, -90, 30);
		turnLever(scene, minLever, 0, 15);
		scene.overlay().showText(60)
			.text("...while power on the opposite side turns it towards its Min Angle")
			.placeNearTarget()
			.pointAt(minLeverVec);
		scene.idle(70);

		// Both sides 15, net 0: -90° -> 0°
		scene.addKeyframe();
		scene.overlay().showControls(maxLeverVec, Pointing.DOWN, 30).rightClick();
		scene.idle(7);
		rotateServo(scene, servo, observer, 90, 30);
		turnLever(scene, maxLever, 0, 15);
		scene.overlay().showText(80)
			.text("The signals from both sides are summed, with the Min side counting as negative, so equal signals cancel each other out")
			.placeNearTarget()
			.pointAt(util.vector().topOf(servo.above()));
		scene.idle(90);

		// Min side 5, net 10: 0° -> 60°
		scene.addKeyframe();
		scene.overlay().showControls(minLeverVec, Pointing.DOWN, 30).rightClick().whileSneaking();
		scene.idle(7);
		rotateServo(scene, servo, observer, 60, 20);
		turnLever(scene, minLever, 15, 5);
		scene.overlay().showText(70)
			.text("How far the Servo turns depends on the strength of the summed signal")
			.placeNearTarget()
			.pointAt(util.vector().topOf(servo.above()));
		scene.idle(80);

		scene.rotateCameraY(90);
		scene.idle(20);

		Vec3 maxAngleSlot = util.vector().blockSurface(servo, Direction.EAST).add(0, -2 / 16f, 0);
		AABB point = new AABB(maxAngleSlot, maxAngleSlot);
		AABB expanded = point.inflate(1 / 16f, 1 / 5f, 1 / 5f);

		scene.addKeyframe();
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, maxAngleSlot, point, 1);
		scene.idle(1);
		scene.overlay().chaseBoundingBoxOutline(PonderPalette.WHITE, maxAngleSlot, expanded, 80);
		scene.overlay().showControls(maxAngleSlot, Pointing.DOWN, 80).scroll();
		scene.overlay().showText(80)
			.text("Scroll on the sides of the Servo to adjust its Max and Min Angles between 0° and 180°")
			.placeNearTarget()
			.pointAt(maxAngleSlot);
		scene.idle(90);

		// Max angle 90° -> 180°, same net 10: 60° -> 120°
		rotateServo(scene, servo, observer, 60, 20);
		scene.idle(20);
		scene.overlay().showText(70)
			.text("With the Max Angle raised to 180°, the same signal now turns it twice as far")
			.placeNearTarget()
			.pointAt(util.vector().topOf(servo.above()));
		scene.idle(80);
		scene.markAsFinished();
	}

	private static void rotateServo(CreateSceneBuilder scene, BlockPos servo,
			ElementLink<WorldSectionElement> attached, float angle, int duration) {
		scene.world().rotateBearing(servo, angle, duration);
		scene.world().rotateSection(attached, 0, angle, 0, duration);
	}

	// Steps an Analog Lever one power level every 2 ticks.
	private static void turnLever(CreateSceneBuilder scene, Selection lever, int from, int to) {
		int step = Integer.signum(to - from);
		for (int state = from + step; state != to + step; state += step) {
			final int power = state;
			scene.idle(2);
			scene.world().modifyBlockEntityNBT(lever, AnalogLeverBlockEntity.class, nbt -> nbt.putInt("State", power));
		}
	}
}
