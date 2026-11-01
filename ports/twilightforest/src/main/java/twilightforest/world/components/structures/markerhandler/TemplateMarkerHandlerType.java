package twilightforest.world.components.structures.markerhandler;

import com.mojang.serialization.Codec;

// TF 4.8 returns a MapCodec; DFU 6's Codec#dispatch takes a Codec and inlines a record codec's fields next to "type" all the same
@FunctionalInterface
public interface TemplateMarkerHandlerType {
	Codec<? extends TemplateMarkerHandler> getCodec();
}
