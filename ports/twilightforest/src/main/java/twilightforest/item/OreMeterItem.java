package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import twilightforest.components.item.OreScannerComponent;
import twilightforest.components.item.OreScannerData;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.init.TFSounds;

import java.util.List;

/**
 * The Ore Meter, finished in 4.8 (1514b923b and follow-ups up to 1076dd0cc).
 * <ul>
 *     <li>Right click: scans a square of whole chunks around the player, spread over a few seconds on the server.
 *     The results are shown in the Ore Meter HUD ({@link twilightforest.client.renderer.TFOverlays}) while it is held.</li>
 *     <li>Sneak + right click on air: cycles the scan radius from 0 to {@link #MAX_CHUNK_SEARCH_RANGE} chunks.</li>
 *     <li>Sneak + right click on a block in {@code #twilightforest:ore_meter_targetable}: only that block is counted.</li>
 *     <li>Left click on air: wipes the results and the targeted block ({@link twilightforest.network.WipeOreMeterPacket}).</li>
 * </ul>
 * 4.8 keeps its state in data components; here each one is a key of the stack NBT, named after the 4.8 component.
 */
public class OreMeterItem extends Item {
	public static final int MAX_CHUNK_SEARCH_RANGE = 2;
	public static final int LOAD_TIME = 50;

	/** The scan in progress ({@link OreScannerComponent}). Server only: {@link #getShareTag} keeps it off the network. */
	public static final String SCANNER_TAG = "ore_scanner";
	/** Ticks the scan in progress has run, present only while scanning. */
	public static final String LOADING_TAG = "ore_loading";
	/** The last finished scan ({@link OreScannerData}). */
	public static final String DATA_TAG = "ore_data";
	/** Scan radius in chunks, 1 when absent. */
	public static final String RANGE_TAG = "ore_range";
	/** Id of the only block to count, from sneak + right clicking a targetable block. */
	public static final String FILTER_TAG = "ore_filter";

	public OreMeterItem(Properties properties) {
		super(properties);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean held) {
		if (level.isClientSide()) return;

		CompoundTag tag = stack.getTag();
		if (tag == null) return;

		if (!tag.contains(SCANNER_TAG, Tag.TAG_COMPOUND)) {
			// The scan can be lost while the meter still says it is loading, e.g. when a creative inventory edit
			// sends back the client's copy of the stack, which never carries the scan. Don't stay stuck loading.
			tag.remove(LOADING_TAG);
			return;
		}

		OreScannerComponent scan = OreScannerComponent.load(tag.getCompound(SCANNER_TAG));

		if (scan == null || scan.isEmpty() || !scan.isIn(level)) {
			tag.remove(SCANNER_TAG);
			tag.remove(LOADING_TAG);
			return;
		}

		scan.tickScan(level);

		if (!scan.isFinished()) {
			tag.putInt(LOADING_TAG, scan.getTickProgress());
			tag.put(SCANNER_TAG, scan.save());
			return;
		}

		// Scanning completed, save results to item
		tag.put(DATA_TAG, OreScannerData.create(scan.getResults(getFilter(stack)), scan.centerChunkPos(), scan.getVolume(level), scan.getRange()).save());

		tag.remove(LOADING_TAG);
		tag.remove(SCANNER_TAG);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		// FakePlayers should never be allowed to use the Ore Meter.
		// Only checked on the server: 4.8 checks on both sides, so its client never played the crackle for the user.
		if (!level.isClientSide() && player.getClass() != ServerPlayer.class) return InteractionResultHolder.fail(stack);

		//if we're in the "loading" state don't try to run any logic
		if (isLoading(stack)) return InteractionResultHolder.pass(stack);

		//if we're not crouching, put the ore meter into its "loading" state
		if (!player.isSecondaryUseActive()) {
			return beginScanning(level, player, stack);
		} else {
			return toggleRange(level, player, stack);
		}
	}

	private static InteractionResultHolder<ItemStack> beginScanning(Level level, Player player, ItemStack stack) {
		if (!level.isClientSide()) {
			int range = getRange(stack);

			OreScannerComponent data = OreScannerComponent.scanFromCenter(level.dimension(), player.blockPosition(), range, getScanTime(range));
			CompoundTag tag = stack.getOrCreateTag();
			tag.put(SCANNER_TAG, data.save());
			tag.putInt(LOADING_TAG, 0);
		}

		level.playSound(player, player.blockPosition(), TFSounds.ORE_METER_CRACKLE.get(), SoundSource.PLAYERS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);

		return InteractionResultHolder.pass(stack);
	}

	private static InteractionResultHolder<ItemStack> toggleRange(Level level, Player player, ItemStack stack) {
		//if we're crouching and not targeting a block, change the ore meter range instead
		HitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
		if (result.getType() == HitResult.Type.MISS) {
			if (!level.isClientSide()) {
				int newRange = Mth.positiveModulo(getRange(stack) + 1, MAX_CHUNK_SEARCH_RANGE + 1);

				stack.getOrCreateTag().putInt(RANGE_TAG, newRange);
				player.displayClientMessage(Component.translatable("misc.twilightforest.ore_meter_new_range", newRange), true);
				level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.25F, 0.75F + (newRange * 0.1F));
			}
			return InteractionResultHolder.success(stack);
		}

		return InteractionResultHolder.pass(stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack stack = context.getItemInHand();
		Player player = context.getPlayer();
		Level level = context.getLevel();
		//if we're crouching and targeting a block, attempt to save the block as the focused block
		if (player != null && context.isSecondaryUseActive()) {
			BlockState state = level.getBlockState(context.getClickedPos());
			if (state.is(BlockTagGenerator.ORE_METER_TARGETABLE)) {
				stack.getOrCreateTag().putString(FILTER_TAG, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
				if (!level.isClientSide()) {
					player.displayClientMessage(Component.translatable("misc.twilightforest.ore_meter_set_block", state.getBlock().getName()), true);
				}
				level.playSound(player, player.blockPosition(), TFSounds.ORE_METER_TARGET_BLOCK.get(), SoundSource.PLAYERS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
				return InteractionResult.sidedSuccess(level.isClientSide());
			}
		}
		return super.useOn(context);
	}

	//don't make the player hand spazz out when the NBT changes
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || newStack.getItem() != oldStack.getItem();
	}

	/**
	 * The scan in progress is only needed on the server, and it changes every tick while the meter is loading,
	 * so it is not sent to clients (4.8 does not network-sync the ore_scanner component either).
	 */
	@Nullable
	@Override
	public CompoundTag getShareTag(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(SCANNER_TAG)) {
			tag = tag.copy();
			tag.remove(SCANNER_TAG);
		}
		return tag;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		Block block = getFilter(stack);

		if (block != null)
			tooltip.add(Component.translatable("misc.twilightforest.ore_meter_targeted_block", block.getName()).withStyle(ChatFormatting.GRAY));

		super.appendHoverText(stack, level, tooltip, flag);
	}

	/**
	 * 50 base ticks plus 25 additional ticks for each range increment.
	 * Range 0: 50 ticks, range 1: 75 ticks, range 2: 100 ticks.
	 * It's not an exponent growth to match the increase in chunks, but range is capped at 2 anyway.
	 */
	public static int getScanTime(int range) {
		return LOAD_TIME + range * 25;
	}

	public static boolean isLoading(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null && tag.contains(LOADING_TAG, Tag.TAG_ANY_NUMERIC);
	}

	public static int getLoadProgress(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null ? Math.max(tag.getInt(LOADING_TAG), 0) : 0;
	}

	public static int getRange(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null && tag.contains(RANGE_TAG, Tag.TAG_ANY_NUMERIC))
			return Mth.clamp(tag.getInt(RANGE_TAG), 0, MAX_CHUNK_SEARCH_RANGE);

		return 1;
	}

	@Nullable
	public static Block getFilter(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null || !tag.contains(FILTER_TAG, Tag.TAG_STRING)) return null;

		ResourceLocation id = ResourceLocation.tryParse(tag.getString(FILTER_TAG));
		Block block = id != null ? BuiltInRegistries.BLOCK.getOptional(id).orElse(null) : null;
		return block == Blocks.AIR ? null : block;
	}

	public static boolean hasScanData(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null && tag.contains(DATA_TAG, Tag.TAG_COMPOUND);
	}

	@Nullable
	public static OreScannerData getScanData(ItemStack stack) {
		return hasScanData(stack) ? OreScannerData.load(stack.getTag().getCompound(DATA_TAG)) : null;
	}

	/**
	 * @return whether there is anything for a left click to wipe
	 */
	public static boolean hasScanDataOrFilter(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		return tag != null && (tag.contains(DATA_TAG) || tag.contains(FILTER_TAG));
	}

	/**
	 * Clears the last scan results and the targeted block. A scan in progress keeps running.
	 */
	public static void wipe(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag != null) {
			tag.remove(DATA_TAG);
			tag.remove(FILTER_TAG);
			if (tag.isEmpty()) stack.setTag(null);
		}
	}
}
