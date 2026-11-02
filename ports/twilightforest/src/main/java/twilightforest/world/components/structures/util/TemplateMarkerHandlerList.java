package twilightforest.world.components.structures.util;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import twilightforest.init.custom.TemplateMarkerHandlers;
import twilightforest.world.components.structures.markerhandler.TemplateMarkerHandler;

import java.util.Arrays;
import java.util.List;

// Ordered marker handlers of a template pool entry; the first handler that handles a data marker ends the list
public record TemplateMarkerHandlerList(List<TemplateMarkerHandler> markerHandlers) {
	public static final Codec<TemplateMarkerHandlerList> CODEC = TemplateMarkerHandlers.DISPATCH_CODEC.listOf().xmap(TemplateMarkerHandlerList::new, TemplateMarkerHandlerList::markerHandlers);
	public static final Codec<Holder<TemplateMarkerHandlerList>> HOLDER_CODEC = RegistryFileCodec.create(TemplateMarkerHandlers.TEMPLATE_MARKER_HANDLER_LIST_KEY, CODEC);

	public static TemplateMarkerHandlerList of(TemplateMarkerHandler... markerHandlers) {
		return new TemplateMarkerHandlerList(Arrays.asList(markerHandlers));
	}
}
