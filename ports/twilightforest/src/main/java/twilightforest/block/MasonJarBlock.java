package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.JarBlockEntity;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.init.TFSounds;

/**
 * A jar that holds one stack: right-click with an item to put it in, with an empty hand to take it out, or sneak with
 * an empty hand to read what is inside. Hoppers and pipes reach the stack through the top face, comparators read how
 * full it is, and a light-emitting block item inside lights up the jar.
 */
public class MasonJarBlock extends JarBlock {
	private static final int SLOT = 0;
	private static final int ALL = Integer.MAX_VALUE;

	public MasonJarBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MasonJarBlockEntity(pos, state);
	}

	@Override
	@Nullable
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (super.useItemOn(stack, state, level, pos, player, hand, hit) != null)
			return InteractionResult.sidedSuccess(level.isClientSide());
		if (!(level.getBlockEntity(pos) instanceof MasonJarBlockEntity jar))
			return InteractionResult.PASS;
		if (!(level instanceof ServerLevel server))
			return InteractionResult.SUCCESS;

		MasonJarBlockEntity.MasonJarItemStackHandler handler = jar.getItemHandler();
		if (stack.isEmpty()) handleEmptyHand(server, pos, player, hand, jar, handler);
		else handleInsert(server, pos, player, hand, jar, handler, stack);
		return InteractionResult.CONSUME;
	}

	private static void handleEmptyHand(ServerLevel server, BlockPos pos, Player player, InteractionHand hand, MasonJarBlockEntity jar, MasonJarBlockEntity.MasonJarItemStackHandler handler) {
		ItemStack preview = handler.extractItem(SLOT, ALL, true);
		if (preview.isEmpty()) {
			wiggle(server, pos, jar);
			return;
		}
		if (player.isSecondaryUseActive()) {
			player.displayClientMessage(Component.literal(preview.getHoverName().getString() + " x" + preview.getCount()), true);
			wiggle(server, pos, jar);
			return;
		}
		ItemStack extracted = handler.extractItem(SLOT, ALL, false);
		player.setItemInHand(hand, extracted);
		server.playSound(null, pos, TFSounds.JAR_REMOVE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
		server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	private static void handleInsert(ServerLevel server, BlockPos pos, Player player, InteractionHand hand, MasonJarBlockEntity jar, MasonJarBlockEntity.MasonJarItemStackHandler handler, ItemStack stack) {
		// Simulate insert first; if nothing would go in then just wiggle
		if (handler.insertItem(SLOT, stack, true).getCount() >= stack.getCount()) {
			wiggle(server, pos, jar);
			return;
		}

		jar.setItemRotation(RotationSegment.convertToSegment(player.getYRot() + 180.0F));
		player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
		ItemStack before = stack.copy();
		ItemStack remainder = handler.insertItem(SLOT, stack, false);
		player.setItemInHand(hand, player.getAbilities().instabuild ? before : remainder);
		float filledRatio = (float) (before.getCount() - remainder.getCount()) / (float) before.getMaxStackSize();
		server.playSound(null, pos, TFSounds.JAR_INSERT.get(), SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * filledRatio);
		server.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	private static void wiggle(ServerLevel server, BlockPos pos, MasonJarBlockEntity jar) {
		server.playSound(null, pos, TFSounds.JAR_WIGGLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
		jar.wobble(JarBlockEntity.WobbleStyle.NEGATIVE);
	}

	@Override
	@Deprecated
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	@Deprecated
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof MasonJarBlockEntity jarBlockEntity) {
			ItemStack itemstack = jarBlockEntity.getItemHandler().getItem();
			float fullness = itemstack.isEmpty() ? 0.0F : (float) itemstack.getCount() / (float) itemstack.getMaxStackSize();
			// Mth.lerpDiscrete(fullness, 0, 15), which 1.20.1 does not have yet
			return Mth.floor(fullness * 14.0F) + (fullness > 0.0F ? 1 : 0);
		}
		return 0;
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
		return Math.max(super.getLightEmission(state, level, pos), MasonJarBlockEntity.getContentLight(level, pos));
	}
}
