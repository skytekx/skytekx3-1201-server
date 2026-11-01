package twilightforest.init.custom;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;
import twilightforest.world.components.structures.markerhandler.*;
import twilightforest.world.components.structures.util.TemplateMarkerHandlerList;

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

	// TF 4.8 registers its first list here, twilightforest:camp_marker_handlers. It arrives with the Traveller camps, together with the
	// camp loot tables that list references
	public static void bootstrap(BootstapContext<TemplateMarkerHandlerList> context) {
	}
}
