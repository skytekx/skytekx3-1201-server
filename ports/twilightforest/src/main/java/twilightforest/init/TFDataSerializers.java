package twilightforest.init;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.TwilightForestMod;

import java.util.ArrayList;
import java.util.List;

public class TFDataSerializers {

	public static final DeferredRegister<EntityDataSerializer<?>> DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, TwilightForestMod.ID);

	/**
	 * The serializer instance itself, so entities can create their {@link net.minecraft.network.syncher.EntityDataAccessor}s in static
	 * initialisers without depending on when the registry fires. Network ids are resolved through the registry entry below.
	 */
	public static final EntityDataSerializer<List<String>> STRING_LIST_SERIALIZER = new EntityDataSerializer<>() {
		@Override
		public void write(FriendlyByteBuf buf, List<String> strings) {
			buf.writeCollection(strings, FriendlyByteBuf::writeUtf);
		}

		@Override
		public List<String> read(FriendlyByteBuf buf) {
			return buf.readList(FriendlyByteBuf::readUtf);
		}

		@Override
		public List<String> copy(List<String> strings) {
			return new ArrayList<>(strings);
		}
	};

	public static final RegistryObject<EntityDataSerializer<List<String>>> STRING_LIST = DATA_SERIALIZERS.register("string_list", () -> STRING_LIST_SERIALIZER);
}
