package twilightforest.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.JarBlock;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFBlocks;
import twilightforest.util.HolidayEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * Block entity of the firefly and cicada jars (and, through {@link MasonJarBlockEntity}, the mason jar). It stores the
 * swappable log lid and drives the decorated-pot style wobble.
 * <p>
 * 4.3 placed these jars without a block entity. Such jars get one when their chunk loads (see
 * {@link twilightforest.events.JarEvents}), starting out with the block's default lid, so the block state stays
 * unchanged.
 * <p>
 * On items the lid is kept as {@code BlockEntityTag.lid}, which vanilla's {@link BlockItem} copies back into the block
 * entity on placement. It stands in for 4.8's {@code twilightforest:jar_lid} data component.
 */
public class JarBlockEntity extends BlockEntity {
	public static final Map<Item, BooleanSupplier> REGISTERED_LOG_LIDS = new HashMap<>();
	public static final String TAG_LID = "lid";
	public static final int EVENT_POT_WOBBLES = 1;

	public static void addLid(Item item, BooleanSupplier supplier) {
		REGISTERED_LOG_LIDS.put(item, supplier);
	}

	public static void addLid(Item item) {
		addLid(item, () -> true);
	}

	/**
	 * The logs (and the Halloween pumpkin) that can be put on a jar as its lid. Called during common setup.
	 */
	public static void registerDefaultLids() {
		addLid(TFBlocks.MANGROVE_LOG.get().asItem());
		addLid(TFBlocks.CANOPY_LOG.get().asItem());
		addLid(TFBlocks.DARK_LOG.get().asItem());
		addLid(TFBlocks.MINING_LOG.get().asItem());
		addLid(TFBlocks.SORTING_LOG.get().asItem());
		addLid(TFBlocks.TIME_LOG.get().asItem());
		addLid(TFBlocks.TRANSFORMATION_LOG.get().asItem());
		addLid(TFBlocks.TWILIGHT_OAK_LOG.get().asItem());
		addLid(Items.ACACIA_LOG);
		addLid(Items.BIRCH_LOG);
		addLid(Items.CHERRY_LOG);
		addLid(Items.DARK_OAK_LOG);
		addLid(Items.JUNGLE_LOG);
		addLid(Items.MANGROVE_LOG);
		addLid(Items.OAK_LOG);
		addLid(Items.SPRUCE_LOG);
		addLid(Items.CRIMSON_STEM);
		addLid(Items.WARPED_STEM);
		addLid(TFBlocks.STRIPPED_MANGROVE_LOG.get().asItem());
		addLid(TFBlocks.STRIPPED_CANOPY_LOG.get().asItem());
		addLid(TFBlocks.STRIPPED_DARK_LOG.get().asItem());
		addLid(TFBlocks.STRIPPED_MINING_LOG.get().asItem());
		addLid(TFBlocks.STRIPPED_SORTING_LOG.get().asItem());
		addLid(TFBlocks.STRIPPED_TIME_LOG.get().asItem());
		addLid(TFBlocks.STRIPPED_TRANSFORMATION_LOG.get().asItem());
		addLid(TFBlocks.STRIPPED_TWILIGHT_OAK_LOG.get().asItem());
		addLid(Items.STRIPPED_ACACIA_LOG);
		addLid(Items.STRIPPED_BIRCH_LOG);
		addLid(Items.STRIPPED_CHERRY_LOG);
		addLid(Items.STRIPPED_DARK_OAK_LOG);
		addLid(Items.STRIPPED_JUNGLE_LOG);
		addLid(Items.STRIPPED_MANGROVE_LOG);
		addLid(Items.STRIPPED_OAK_LOG);
		addLid(Items.STRIPPED_SPRUCE_LOG);
		addLid(Items.STRIPPED_CRIMSON_STEM);
		addLid(Items.STRIPPED_WARPED_STEM);
		addLid(TFBlocks.CINDER_LOG.get().asItem());
		addLid(Items.BAMBOO_BLOCK);
		addLid(Items.STRIPPED_BAMBOO_BLOCK);
		addLid(Items.PUMPKIN, HolidayEvent::isHalloweenWeek);
	}

	@NotNull
	public Item lid = TFBlocks.TWILIGHT_OAK_LOG.get().asItem();
	public long wobbleStartedAtTick;
	@Nullable
	public WobbleStyle lastWobbleStyle;

	public JarBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
		if (blockState.getBlock() instanceof JarBlock jarBlock) this.lid = jarBlock.getDefaultLid();
	}

	public JarBlockEntity(BlockPos pos, BlockState state) {
		this(TFBlockEntities.JAR.get(), pos, state);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putString(TAG_LID, BuiltInRegistries.ITEM.getKey(this.lid).toString());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		// A missing tag keeps the block's default lid; an unknown id (a removed mod's log) falls back to Twilight Oak
		if (tag.contains(TAG_LID, Tag.TAG_STRING)) this.lid = parseLid(tag.getString(TAG_LID));
	}

	/**
	 * @return the jar as an item, carrying its lid (and the mason jar's contents) in {@code BlockEntityTag}
	 */
	public ItemStack getJarAsItem() {
		ItemStack stack = new ItemStack(this.getBlockState().getBlock());
		this.saveToItemTag(stack.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG));
		return stack;
	}

	protected void saveToItemTag(CompoundTag blockEntityTag) {
		blockEntityTag.putString(TAG_LID, BuiltInRegistries.ITEM.getKey(this.lid).toString());
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	public void wobble(WobbleStyle style) {
		if (this.level != null && !this.level.isClientSide()) {
			this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), EVENT_POT_WOBBLES, style.ordinal());
		}
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		if (this.level != null && id == EVENT_POT_WOBBLES && type >= 0 && type < WobbleStyle.values().length) {
			this.wobbleStartedAtTick = this.level.getGameTime();
			this.lastWobbleStyle = WobbleStyle.values()[type];
			return true;
		} else {
			return super.triggerEvent(id, type);
		}
	}

	public static Item parseLid(String id) {
		ResourceLocation location = ResourceLocation.tryParse(id);
		Item item = location == null ? Items.AIR : BuiltInRegistries.ITEM.get(location);
		return item == Items.AIR ? TFBlocks.TWILIGHT_OAK_LOG.get().asItem() : item;
	}

	/**
	 * @return the lid stored on a jar item, or {@code null} when the item does not carry one
	 */
	@Nullable
	public static Item getLid(ItemStack jar) {
		CompoundTag tag = BlockItem.getBlockEntityData(jar);
		return tag != null && tag.contains(TAG_LID, Tag.TAG_STRING) ? parseLid(tag.getString(TAG_LID)) : null;
	}

	/**
	 * Stores a lid on a jar item, the 1.20.1 form of 4.8's {@code twilightforest:jar_lid} component.
	 */
	public static ItemStack setLid(ItemStack jar, @Nullable Item lid) {
		if (lid != null) jar.getOrCreateTagElement(BlockItem.BLOCK_ENTITY_TAG).putString(TAG_LID, BuiltInRegistries.ITEM.getKey(lid).toString());
		return jar;
	}

	/**
	 * 1.20.1 has no {@code DecoratedPotBlockEntity.WobbleStyle} (added in 1.20.3), so the jar keeps its own copy.
	 */
	public enum WobbleStyle {
		POSITIVE(7),
		NEGATIVE(10);

		public final int duration;

		WobbleStyle(int duration) {
			this.duration = duration;
		}
	}
}
