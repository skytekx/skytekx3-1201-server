package twilightforest.entity.passive.quest.ram;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import twilightforest.loot.TFLootTables;

import java.util.Map;

/**
 * The Questing Ram's quest: which item counts as which of the 16 colours, and which loot table is dropped once all
 * of them were fed. Loaded from {@code data/<namespace>/twilight/quests/questing_ram.json} by
 * {@link twilightforest.entity.passive.quest.QuestReloadListener} and synced to clients for the crosshair indicator.
 */
public record QuestingRamContext(Map<DyeColor, Ingredient> questItems, ResourceLocation lootTable) {

	public static final QuestingRamContext FALLBACK = new QuestingRamContext(ImmutableMap.<DyeColor, Ingredient>builder()
		.put(DyeColor.WHITE, Ingredient.of(Items.WHITE_WOOL))
		.put(DyeColor.LIGHT_GRAY, Ingredient.of(Items.LIGHT_GRAY_WOOL))
		.put(DyeColor.GRAY, Ingredient.of(Items.GRAY_WOOL))
		.put(DyeColor.BLACK, Ingredient.of(Items.BLACK_WOOL))
		.put(DyeColor.RED, Ingredient.of(Items.RED_WOOL))
		.put(DyeColor.ORANGE, Ingredient.of(Items.ORANGE_WOOL))
		.put(DyeColor.YELLOW, Ingredient.of(Items.YELLOW_WOOL))
		.put(DyeColor.GREEN, Ingredient.of(Items.GREEN_WOOL))
		.put(DyeColor.LIME, Ingredient.of(Items.LIME_WOOL))
		.put(DyeColor.BLUE, Ingredient.of(Items.BLUE_WOOL))
		.put(DyeColor.CYAN, Ingredient.of(Items.CYAN_WOOL))
		.put(DyeColor.LIGHT_BLUE, Ingredient.of(Items.LIGHT_BLUE_WOOL))
		.put(DyeColor.PURPLE, Ingredient.of(Items.PURPLE_WOOL))
		.put(DyeColor.MAGENTA, Ingredient.of(Items.MAGENTA_WOOL))
		.put(DyeColor.PINK, Ingredient.of(Items.PINK_WOOL))
		.put(DyeColor.BROWN, Ingredient.of(Items.BROWN_WOOL)).build(),
		TFLootTables.QUESTING_RAM_REWARDS);

	// 1.20.1 has no Ingredient codec, so go through Forge's JSON (de)serializer, which also understands custom ingredient types
	private static final Codec<Ingredient> INGREDIENT_CODEC = ExtraCodecs.JSON.flatXmap(json -> {
		try {
			return DataResult.success(Ingredient.fromJson(json, false));
		} catch (JsonParseException e) {
			return DataResult.error(e::getMessage);
		}
	}, ingredient -> DataResult.success(ingredient.toJson()));

	public static final Codec<QuestingRamContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ExtraCodecs.validate(Codec.unboundedMap(DyeColor.CODEC, INGREDIENT_CODEC), QuestingRamContext::validate).fieldOf("items").forGetter(QuestingRamContext::questItems),
		ResourceLocation.CODEC.fieldOf("reward").forGetter(QuestingRamContext::lootTable)
	).apply(instance, QuestingRamContext::new));

	private static DataResult<Map<DyeColor, Ingredient>> validate(Map<DyeColor, Ingredient> map) {
		int colorFlags = 0;
		for (var color : map.keySet()) {
			colorFlags |= (1 << color.getId());
		}
		if (Integer.bitCount(colorFlags) == 16) {
			return DataResult.success(map);
		}
		return DataResult.error(() -> "Questing Ram quest must contain all 16 dye colors");
	}

	/**
	 * Network form for {@link twilightforest.network.SyncQuestsPacket}. Ingredients use the vanilla recipe sync format,
	 * so tags arrive already resolved into their items.
	 */
	public void toNetwork(FriendlyByteBuf buf) {
		buf.writeMap(this.questItems(), FriendlyByteBuf::writeEnum, (buffer, ingredient) -> ingredient.toNetwork(buffer));
		buf.writeResourceLocation(this.lootTable());
	}

	public static QuestingRamContext fromNetwork(FriendlyByteBuf buf) {
		// keep the server's entry order, the first matching colour wins when an item fits several ingredients
		Map<DyeColor, Ingredient> items = buf.readMap(Maps::newLinkedHashMapWithExpectedSize, buffer -> buffer.readEnum(DyeColor.class), Ingredient::fromNetwork);
		return new QuestingRamContext(ImmutableMap.copyOf(items), buf.readResourceLocation());
	}

	/**
	 * The item to show for a colour that still has to be fed (Jade and The One Probe): the first item of that colour's
	 * quest ingredient, or the matching wool if the ingredient has none.
	 */
	public ItemStack getDisplayStack(DyeColor color, ItemLike fallback) {
		Ingredient ingredient = this.questItems().get(color);
		if (ingredient != null) {
			for (ItemStack stack : ingredient.getItems()) {
				if (!stack.isEmpty()) {
					return stack;
				}
			}
		}
		return new ItemStack(fallback);
	}
}
