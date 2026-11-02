package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;
import twilightforest.init.TFBlocks;
import twilightforest.loot.TFLootTables;
import twilightforest.world.components.structures.markerhandler.*;
import twilightforest.world.components.structures.util.TemplateMarkerHandlerList;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Template marker handlers turn the DATA structure blocks of a jigsaw template into content. The handler types are a static registry
 * (twilightforest:template_marker_handler_type); single handlers and handler lists are datapack registries, read from
 * {@code data/<namespace>/twilight/template_marker_handler/} and {@code data/<namespace>/twilight/template_marker_handler_list/}.
 * A template pool entry references a list through its {@code marker_handlers} field.
 */
public class TemplateMarkerHandlers {
	public static final ResourceKey<Registry<TemplateMarkerHandlerType>> TEMPLATE_MARKER_HANDLER_TYPE_KEY = ResourceKey.createRegistryKey(TwilightForestMod.prefix("template_marker_handler_type"));
	public static final ResourceKey<Registry<TemplateMarkerHandler>> TEMPLATE_MARKER_HANDLER_KEY = ResourceKey.createRegistryKey(TwilightForestMod.namedRegistry("template_marker_handler"));
	public static final ResourceKey<Registry<TemplateMarkerHandlerList>> TEMPLATE_MARKER_HANDLER_LIST_KEY = ResourceKey.createRegistryKey(TwilightForestMod.namedRegistry("template_marker_handler_list"));

	public static final DeferredRegister<TemplateMarkerHandlerType> TEMPLATE_MARKER_HANDLER_TYPES = DeferredRegister.create(TEMPLATE_MARKER_HANDLER_TYPE_KEY, TwilightForestMod.ID);
	// Only worldgen reads the types, so like NeoForge's default for TF 4.8 the registry is neither synced to clients nor saved with the world
	public static final Supplier<IForgeRegistry<TemplateMarkerHandlerType>> REGISTRY = TEMPLATE_MARKER_HANDLER_TYPES.makeRegistry(() -> new RegistryBuilder<TemplateMarkerHandlerType>().disableSaving().disableSync());

	public static final Codec<TemplateMarkerHandlerType> TYPE_CODEC = ExtraCodecs.lazyInitializedCodec(() -> REGISTRY.get().getCodec());
	public static final Codec<TemplateMarkerHandler> DISPATCH_CODEC = TYPE_CODEC.dispatch("type", TemplateMarkerHandler::getType, TemplateMarkerHandlerType::getCodec);
	public static final Codec<Holder<TemplateMarkerHandler>> HOLDER_CODEC = RegistryFileCodec.create(TEMPLATE_MARKER_HANDLER_KEY, DISPATCH_CODEC);

	public static final RegistryObject<TemplateMarkerHandlerType> BLOCK_PLACEMENT = TEMPLATE_MARKER_HANDLER_TYPES.register("block_placement", () -> () -> BlockPlaceMarkerHandler.CODEC);
	public static final RegistryObject<TemplateMarkerHandlerType> HANDLER_SWITCH = TEMPLATE_MARKER_HANDLER_TYPES.register("handler_switch", () -> () -> SwitchMarkerHandler.CODEC);
	public static final RegistryObject<TemplateMarkerHandlerType> ROTATION = TEMPLATE_MARKER_HANDLER_TYPES.register("rotation", () -> () -> RotationMarkerHandler.CODEC);
	public static final RegistryObject<TemplateMarkerHandlerType> DRYING_RACK = TEMPLATE_MARKER_HANDLER_TYPES.register("drying_rack", () -> () -> DryingRackMarkerHandler.CODEC);
	public static final RegistryObject<TemplateMarkerHandlerType> PAINTING = TEMPLATE_MARKER_HANDLER_TYPES.register("painting", () -> () -> PaintingMarkerHandler.CODEC);
	public static final RegistryObject<TemplateMarkerHandlerType> LOOT = TEMPLATE_MARKER_HANDLER_TYPES.register("loot", () -> () -> LootMarkerHandler.CODEC);

	public static final ResourceKey<TemplateMarkerHandlerList> CAMP_MARKER_HANDLERS = ResourceKey.create(TEMPLATE_MARKER_HANDLER_LIST_KEY, TwilightForestMod.prefix("camp_marker_handlers"));

	public static void bootstrap(BootstapContext<TemplateMarkerHandlerList> context) {
		BlockPlaceMarkerHandler campfireSeat = new BlockPlaceMarkerHandler(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
			.add(TFBlocks.TWILIGHT_OAK_SLAB.get().defaultBlockState(), 1)
			.add(Blocks.AIR.defaultBlockState(), 3)
			.build()));

		DryingRackMarkerHandler armorRack = new DryingRackMarkerHandler(BlockStateProvider.simple(TFBlocks.CANOPY_DRYING_RACK.get()), TFLootTables.CAMP_ARMOR_RACK);

		DryingRackMarkerHandler birchDryingRack = new DryingRackMarkerHandler(BlockStateProvider.simple(TFBlocks.BIRCH_DRYING_RACK.get()), TFLootTables.CAMP_DRYING_RACK);

		PaintingMarkerHandler painting = new PaintingMarkerHandler(PaintingVariantTags.PLACEABLE);

		// TF 4.8 fills a decorated pot here. 1.20.1 decorated pots have no inventory (LootMarkerHandler would place an empty pot and report the
		// marker unhandled), so the tent pot is an upright barrel holding the same loot
		LootMarkerHandler tentPot = new LootMarkerHandler(BlockStateProvider.simple(Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP)), TFLootTables.CAMP_POT);

		Map<String, Holder<TemplateMarkerHandler>> keyedHandlers = Map.of(
			"twilight_oak_slab", Holder.direct(campfireSeat),
			"camp_armor_rack", Holder.direct(armorRack),
			"birch_drying_rack", Holder.direct(birchDryingRack),
			"painting", Holder.direct(painting),
			"tent_pot", Holder.direct(tentPot)
		);
		context.register(CAMP_MARKER_HANDLERS, TemplateMarkerHandlerList.of(
			new RotationMarkerHandler(Holder.direct(new SwitchMarkerHandler(keyedHandlers)))
		));
	}
}
