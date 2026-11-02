package twilightforest.data.custom.structuredefinitions;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraftforge.common.data.ExistingFileHelper;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;
import twilightforest.init.custom.TemplateMarkerHandlers;
import twilightforest.world.components.processors.StateTransfiguringProcessor;
import twilightforest.world.components.structures.camp.CampPieces;
import twilightforest.world.components.structures.util.TemplateMarkerHandlerList;
import twilightforest.world.components.structures.util.TemplatePoolInstance;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CampStructureDefinitionGenerator extends StructureTemplateDefinitionProvider {
	// Needs the lookup of RegistryDataGenerator.getRegistryProvider(), which holds twilightforest:camp_marker_handlers
	public CampStructureDefinitionGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, TwilightForestMod.ID, existingFileHelper, "Camp");
	}

	@Override
	protected void generatePools() {
		// The future is already complete when generatePools() runs, so this runs synchronously
		this.lookupProvider.thenAccept(this::generatePoolsWithProvider);
	}

	private void generatePoolsWithProvider(HolderLookup.Provider provider) {
		Holder.Reference<TemplateMarkerHandlerList> campMarkers = this.getMarkers(provider, TemplateMarkerHandlers.CAMP_MARKER_HANDLERS);

		this.add("camp/campfire_east", CampPieces.START, this.weightedRigidTemplate(100, 1, null, campMarkers, null, null));
		this.add("camp/campfire_south", CampPieces.START, this.weightedRigidTemplate(100, 1, null, campMarkers, null, null));
		this.add("camp/campfire_west", CampPieces.START, this.weightedRigidTemplate(100, 1, null, campMarkers, null, null));

		this.configureTents(provider);

		this.configureRackPaths();

		this.configureBigPaths();

		this.configureSmallPaths();

		this.configureDeco(campMarkers);
	}

	private void configureTents(HolderLookup.Provider provider) {
		Holder.Reference<TemplateMarkerHandlerList> campMarkers = this.getMarkers(provider, TemplateMarkerHandlers.CAMP_MARKER_HANDLERS);

		int defaultWeight = 10;
		TemplatePoolInstance.ChooseRandomProcessors randomizedProcessors = new TemplatePoolInstance.ChooseRandomProcessors(List.of(
			SimpleWeightedRandomList.<StructureProcessor>builder()
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.LIGHT_GRAY_WOOL), AlwaysTrueTest.INSTANCE, Blocks.WHITE_WOOL.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.LIGHT_GRAY_WOOL), AlwaysTrueTest.INSTANCE, Blocks.LIGHT_GRAY_WOOL.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.LIGHT_GRAY_WOOL), AlwaysTrueTest.INSTANCE, Blocks.GRAY_WOOL.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.LIGHT_GRAY_WOOL), AlwaysTrueTest.INSTANCE, Blocks.BROWN_WOOL.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.LIGHT_GRAY_WOOL), AlwaysTrueTest.INSTANCE, Blocks.GREEN_WOOL.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.LIGHT_GRAY_WOOL), AlwaysTrueTest.INSTANCE, Blocks.BLUE_WOOL.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.LIGHT_GRAY_WOOL), AlwaysTrueTest.INSTANCE, Blocks.RED_WOOL.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.LIGHT_GRAY_WOOL), AlwaysTrueTest.INSTANCE, Blocks.LIME_WOOL.defaultBlockState()))), 1)
				.build(),
			SimpleWeightedRandomList.<StructureProcessor>builder()
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.RED_BED), AlwaysTrueTest.INSTANCE, Blocks.RED_BED.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.RED_BED), AlwaysTrueTest.INSTANCE, Blocks.LIGHT_BLUE_BED.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.RED_BED), AlwaysTrueTest.INSTANCE, Blocks.LIME_BED.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.RED_BED), AlwaysTrueTest.INSTANCE, Blocks.ORANGE_BED.defaultBlockState()))), defaultWeight)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(Blocks.RED_BED), AlwaysTrueTest.INSTANCE, Blocks.LIGHT_GRAY_BED.defaultBlockState()))), defaultWeight)
				.build()
		));

		this.add("camp/tent/solo_tent", CampPieces.TENT, this.weightedRigidTemplate(100, 1, 0, campMarkers, randomizedProcessors, null));
		this.add("camp/tent/duo_tent", CampPieces.TENT, this.weightedRigidTemplate(75, 1, 0, campMarkers, randomizedProcessors, null));
		this.add("camp/tent/open_tent", CampPieces.TENT, this.weightedRigidTemplate(75, 1, 0, campMarkers, randomizedProcessors, null));
		this.add("camp/tent/luxury_tent", CampPieces.TENT, this.weightedRigidTemplate(50, 1, 0, campMarkers, randomizedProcessors, null));
	}

	private void configureRackPaths() {
		// Paths of the rack_path pool lead on to drying racks only: their camp/deco jigsaws draw from camp/rack instead
		TemplatePoolInstance pathTemplateData = this.weightedPathTemplate(100, null, Map.of(CampPieces.DECO.toString(), CampPieces.RACK.toString()));

		this.add("camp/path/path_2x4", CampPieces.RACK_PATH, pathTemplateData);
		this.add("camp/path/path_3x4", CampPieces.RACK_PATH, pathTemplateData);
	}

	private void configureBigPaths() {
		TemplatePoolInstance pathTemplateData = this.weightedPathTemplate(25, null, null);

		this.addEmpty(CampPieces.MAIN_PATH, 300);

		this.add("camp/path/intersection_left", CampPieces.MAIN_PATH, pathTemplateData);
		this.add("camp/path/intersection_right", CampPieces.MAIN_PATH, pathTemplateData);
		this.add("camp/path/intersection_short", CampPieces.MAIN_PATH, pathTemplateData);
		this.add("camp/path/j_path", CampPieces.MAIN_PATH, pathTemplateData);
		this.add("camp/path/l_path", CampPieces.MAIN_PATH, pathTemplateData);
	}

	private void configureSmallPaths() {
		TemplatePoolInstance pathTemplateData = this.weightedPathTemplate(50, null, null);

		this.addEmpty(CampPieces.PATH, 300);

		this.addToAllPools("camp/path/path_2x4", pathTemplateData, CampPieces.MAIN_PATH, CampPieces.PATH);
		this.addToAllPools("camp/path/path_2x6", pathTemplateData, CampPieces.MAIN_PATH, CampPieces.PATH);
		this.addToAllPools("camp/path/path_2x7", pathTemplateData, CampPieces.MAIN_PATH, CampPieces.PATH);
		this.addToAllPools("camp/path/path_3x4", pathTemplateData, CampPieces.MAIN_PATH, CampPieces.PATH);
	}

	private void configureDeco(Holder.Reference<TemplateMarkerHandlerList> campMarkers) {
		TemplatePoolInstance twoLayerTemplateData = this.weightedRigidTemplate(100, 2, 0, campMarkers, null, null);
		TemplatePoolInstance berryTemplateData = this.weightedRigidTemplate(75, 2, 0, campMarkers, null, null);

		this.addToAllPools("camp/deco/double_drying_rack", twoLayerTemplateData, CampPieces.DECO, CampPieces.RACK);
		this.addToAllPools("camp/deco/long_drying_rack", twoLayerTemplateData, CampPieces.DECO, CampPieces.RACK);

		this.add("camp/deco/garden_1x3", CampPieces.DECO, twoLayerTemplateData);
		this.add("camp/deco/garden_2x4", CampPieces.DECO, twoLayerTemplateData);
		this.add("camp/deco/garden_straight", CampPieces.DECO, twoLayerTemplateData);
		this.add("camp/deco/garden_u", CampPieces.DECO, twoLayerTemplateData);

		this.add("camp/deco/berries_staked", CampPieces.DECO, berryTemplateData);
		this.add("camp/deco/berries_trellis", CampPieces.DECO, berryTemplateData);
		this.add("camp/deco/blackberry_wall", CampPieces.DECO, berryTemplateData);
		this.add("camp/deco/blueberry_wall", CampPieces.DECO, berryTemplateData);
		this.add("camp/deco/raspberry_wall", CampPieces.DECO, berryTemplateData);
		this.add("camp/deco/berries_support", CampPieces.DECO, berryTemplateData);

		this.add("camp/deco/compost_large", CampPieces.DECO, twoLayerTemplateData);
		this.add("camp/deco/compost_small", CampPieces.DECO, twoLayerTemplateData);

		this.add("camp/deco/repair_station", CampPieces.DECO, twoLayerTemplateData);

		this.add("camp/deco/pen", CampPieces.DECO, twoLayerTemplateData);

		TemplatePoolInstance.ChooseRandomProcessors randomizedWoods = new TemplatePoolInstance.ChooseRandomProcessors(List.of(
			SimpleWeightedRandomList.<StructureProcessor>builder()
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(TFBlocks.TWILIGHT_OAK_LOG.get()), AlwaysTrueTest.INSTANCE, TFBlocks.TWILIGHT_OAK_LOG.get().defaultBlockState()))), 100)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(TFBlocks.TWILIGHT_OAK_LOG.get()), AlwaysTrueTest.INSTANCE, TFBlocks.CANOPY_LOG.get().defaultBlockState()))), 90)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(TFBlocks.TWILIGHT_OAK_LOG.get()), AlwaysTrueTest.INSTANCE, Blocks.OAK_LOG.defaultBlockState()))), 80)
				.add(new StateTransfiguringProcessor(List.of(new ProcessorRule(new BlockMatchTest(TFBlocks.TWILIGHT_OAK_LOG.get()), AlwaysTrueTest.INSTANCE, Blocks.BIRCH_LOG.defaultBlockState()))), 70)
				.build()
		));
		TemplatePoolInstance lumberTemplateData = this.weightedRigidTemplate(100, 1, 0, campMarkers, randomizedWoods, null);

		this.add("camp/deco/lumber1", CampPieces.DECO, lumberTemplateData);
		this.add("camp/deco/lumber2", CampPieces.DECO, lumberTemplateData);
		this.add("camp/deco/lumber3", CampPieces.DECO, lumberTemplateData);

		this.addEmpty(CampPieces.DECO, 1000);
	}
}
