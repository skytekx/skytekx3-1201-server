package twilightforest.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import twilightforest.block.CandelabraBlock;
import twilightforest.block.LightableBlock;
import twilightforest.components.item.CandelabraData;
import twilightforest.init.TFBlockEntities;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the three candles of a {@link CandelabraBlock}; the block state mirrors which slots are filled.
 * <p>
 * 4.3 candelabras had no block entity. A block entity created without saved data starts out with a plain candle in
 * every slot its {@code has_candle_N} state shows (see {@link CandelabraData#fromState}), and
 * {@link twilightforest.events.CandelabraEvents} gives 4.3 candelabras one when their chunk loads.
 */
public class CandelabraBlockEntity extends BlockEntity {

	private CandelabraData data;

	public CandelabraBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.CANDELABRA.get(), pos, state);
		this.data = CandelabraData.fromState(state);
	}

	public CandelabraData getCandles() {
		return this.data;
	}

	public void setData(CandelabraData data) {
		this.data = data;
	}

	public Block removeCandle(int index) {
		Block block = CandelabraData.getItem(this.data.ordered(), index).orElse(Blocks.AIR);
		this.setCandle(index, Blocks.AIR);
		return block;
	}

	public void setCandle(int index, Block block) {
		List<Block> list = new ArrayList<>(this.data.ordered());
		list.set(index, block);
		this.data = new CandelabraData(list);
		this.updateState(index);
		this.setChanged();
		if (this.getLevel() != null) {
			this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
		}
	}

	public Block getCandle(int index) {
		return CandelabraData.getItem(this.data.ordered(), index).orElse(Blocks.AIR);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		this.data.save(tag);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.data = CandelabraData.load(tag);
	}

	/**
	 * Syncs the {@code has_candle_N} properties with the stored candles and puts out a candelabra that has none left.
	 */
	public void updateState(int index) {
		Level level = this.getLevel();
		if (level != null && index >= 0 && index < 3) {
			BlockState blockstate = this.getBlockState();

			for (int i = 0; i < CandelabraBlock.CANDLES.size(); ++i) {
				boolean flag = !this.getCandle(i).defaultBlockState().isAir();
				BooleanProperty booleanproperty = CandelabraBlock.CANDLES.get(i);
				blockstate = blockstate.setValue(booleanproperty, flag);
			}

			if (CandelabraBlock.getCandleCount(blockstate) == 0 && blockstate.getValue(CandelabraBlock.LIGHTING) != LightableBlock.Lighting.NONE) {
				blockstate = blockstate.setValue(CandelabraBlock.LIGHTING, LightableBlock.Lighting.NONE);
			}

			level.setBlock(this.getBlockPos(), blockstate, Block.UPDATE_ALL);
			level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(blockstate));
		}
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
