package twilightforest.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import twilightforest.block.OminousCandleBlock;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFSounds;

public class ExanimateEssenceItem extends Item {
	public ExanimateEssenceItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		boolean flag = false;
		BlockState state = level.getBlockState(blockpos);
		// Only the 17 vanilla candles have an ominous form; another mod's CandleBlock subclass falls through to the fire
		if (state.getBlock() instanceof CandleBlock candleBlock && OminousCandleBlock.CANDLE_MAP.containsKey(candleBlock) && state.getValue(CandleBlock.LIT)) {
			this.playSound(level, blockpos);
			level.setBlockAndUpdate(blockpos, OminousCandleBlock.CANDLE_MAP.get(candleBlock).get().defaultBlockState().setValue(OminousCandleBlock.CANDLES, state.getValue(CandleBlock.CANDLES)));
			level.gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, blockpos);
			OminousCandleBlock.eruptFlameParticles(level, blockpos, level.getBlockState(blockpos));
			flag = true;
		} else {
			blockpos = blockpos.relative(context.getClickedFace());
			state = level.getBlockState(blockpos);
			BlockState fire = TFBlocks.OMINOUS_FIRE.get().defaultBlockState();

			if (state.canBeReplaced() && fire.canSurvive(level, blockpos)) {
				this.playSound(level, blockpos);
				level.setBlockAndUpdate(blockpos, fire);
				level.gameEvent(context.getPlayer(), GameEvent.BLOCK_PLACE, blockpos);
				flag = true;
			}
		}

		if (flag) {
			context.getItemInHand().shrink(1);
			return InteractionResult.sidedSuccess(level.isClientSide());
		} else {
			return InteractionResult.FAIL;
		}
	}

	private void playSound(Level level, BlockPos pos) {
		RandomSource randomsource = level.getRandom();
		level.playSound(null, pos, TFSounds.OMINOUS_FIRE.get(), SoundSource.BLOCKS, 1.5F, (randomsource.nextFloat() - randomsource.nextFloat()) * 0.2F + 0.75F);
	}
}
