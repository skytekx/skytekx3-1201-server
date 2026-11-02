package twilightforest.components.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.CandelabraBlock;
import twilightforest.init.TFBlockEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The three candles held by a candelabra, from slot 0 to slot 2.
 * <p>
 * Backport of 4.8's CandelabraData data component ({@code twilightforest:candelabra_data}). 4.8 stores it in the block
 * entity as a {@code Candles} list of block ids (an empty slot is {@code minecraft:air}), and the 4.8 Lich Tower
 * templates carry that same NBT, so the format is kept as is. On items it lives in {@code BlockEntityTag.Candles},
 * which vanilla's {@link BlockItem} loads back into the block entity on placement.
 */
public record CandelabraData(Optional<Block> one, Optional<Block> two, Optional<Block> three) {
	public static final CandelabraData EMPTY = new CandelabraData(Optional.empty(), Optional.empty(), Optional.empty());
	public static final String CANDLES_TAG = "Candles";

	public CandelabraData(List<Block> blocks) {
		this(getItem(blocks, 0), getItem(blocks, 1), getItem(blocks, 2));
	}

	public CandelabraData(Block one, Block two, Block three) {
		this(List.of(one, two, three));
	}

	public static Optional<Block> getItem(List<Block> candles, int index) {
		if (index >= candles.size()) {
			return Optional.empty();
		} else {
			Block candle = candles.get(index);
			return candle == Blocks.AIR ? Optional.empty() : Optional.of(candle);
		}
	}

	/**
	 * A plain candle in every slot whose {@code has_candle_N} property is set. 4.3 candelabras were drawn by their block
	 * model with three plain candles and had no block entity; the overhauled block defaults to three candles in its state,
	 * so a block entity created without saved data (a 4.3 candelabra, or one set by command) matches what the state shows.
	 */
	public static CandelabraData fromState(BlockState state) {
		if (!(state.getBlock() instanceof CandelabraBlock)) return EMPTY;
		List<Block> candles = new ArrayList<>(3);
		for (int i = 0; i < CandelabraBlock.CANDLES.size(); i++) {
			candles.add(state.getValue(CandelabraBlock.CANDLES.get(i)) ? Blocks.CANDLE : Blocks.AIR);
		}
		return new CandelabraData(candles);
	}

	public boolean isEmpty() {
		return this.equals(EMPTY);
	}

	public CompoundTag save(CompoundTag tag) {
		if (!this.isEmpty()) {
			ListTag list = new ListTag();
			for (Block candle : this.ordered()) {
				list.add(StringTag.valueOf(BuiltInRegistries.BLOCK.getKey(candle).toString()));
			}
			tag.put(CANDLES_TAG, list);
		}
		return tag;
	}

	public List<Block> ordered() {
		return Stream.of(this.one, this.two, this.three).map(block -> block.orElse(Blocks.AIR)).toList();
	}

	/**
	 * Reads the {@code Candles} list. As with 4.8's codec on the defaulted block registry, an unknown id (a candle of a
	 * removed mod) reads as an empty slot. Entries past the third are ignored.
	 */
	public static CandelabraData load(@Nullable CompoundTag tag) {
		if (tag == null || !tag.contains(CANDLES_TAG, Tag.TAG_LIST)) return EMPTY;
		ListTag list = tag.getList(CANDLES_TAG, Tag.TAG_STRING);
		List<Block> candles = new ArrayList<>(3);
		for (int i = 0; i < Math.min(list.size(), 3); i++) {
			ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
			candles.add(id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(id));
		}
		return new CandelabraData(candles);
	}

	public static CandelabraData fromItem(ItemStack stack) {
		return load(BlockItem.getBlockEntityData(stack));
	}

	/**
	 * Puts the candles on a candelabra item. An empty candelabra gets no block entity tag, so it stacks with crafted ones.
	 */
	public void saveToItem(ItemStack stack) {
		BlockItem.setBlockEntityData(stack, TFBlockEntities.CANDELABRA.get(), this.save(new CompoundTag()));
	}
}
