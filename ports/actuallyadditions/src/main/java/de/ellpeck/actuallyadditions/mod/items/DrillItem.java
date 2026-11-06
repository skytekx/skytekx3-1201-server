/*
 * This file ("ItemDrill.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerDrill;
import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityInventoryBase;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA;
import de.ellpeck.actuallyadditions.mod.util.ItemUtil;
import de.ellpeck.actuallyadditions.mod.util.Util;
import de.ellpeck.actuallyadditions.mod.util.WorldUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DrillItem extends ItemEnergy {

    public static final int HARVEST_LEVEL = 4;
    private static final int ENERGY_USE = 100;
    private static final List<ToolAction> ACTIONS = List.of(ToolActions.SHOVEL_DIG, ToolActions.PICKAXE_DIG);

    // SkyTekx: the drill attributes of upstream 0ebbd811 and 8977b3ba (#1461). They are built once and
    // carry the base attack UUIDs of Item, so the tooltip shows the attack damage and attack speed the
    // player gets (9 and 1 when charged) instead of a raw -2.5 modifier under a random UUID each call
    private final Multimap<Attribute, AttributeModifier> attributesPowered;
    private final Multimap<Attribute, AttributeModifier> attributesUnpowered;

    // SkyTekx: true while this thread breaks the area of a drill. Another mod's BreakEvent listener
    // that breaks more blocks for the player (a vein miner, for example) calls onBlockStartBreak again
    // through ServerPlayerGameMode#destroyBlock, and those nested calls break only their own block
    private static final ThreadLocal<Boolean> BREAKING_AREA = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public DrillItem() {
        super(ActuallyItems.defaultProps().defaultDurability(0).stacksTo(1), 250000, 1000);
        this.attributesPowered = ImmutableMultimap.of(
            Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0F, AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.0F, AttributeModifier.Operation.ADDITION));
        this.attributesUnpowered = ImmutableMultimap.of(
            Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 0.1F, AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.0F, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public boolean canPerformAction(@Nonnull ItemStack stack, @Nonnull ToolAction toolAction) {
        return ACTIONS.contains(toolAction);
    }

    // SkyTekx: upstream 424427fd makes the drill explicitly unbreakable. It has no durability on
    // 1.20.1 either, and this keeps every damage path of ItemStack#isDamageableItem off it
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState pBlock) {
        Tier tier = Tiers.NETHERITE; //Use Nettherite as the tier as it has the same harvest level as the drill
        if (net.minecraftforge.common.TierSortingRegistry.isTierSorted(tier)) {
            return net.minecraftforge.common.TierSortingRegistry.isCorrectTierForDrops(tier, pBlock) && pBlock.is(ActuallyTags.Blocks.MINEABLE_WITH_DRILL);
        }
        if (HARVEST_LEVEL < 3 && pBlock.is(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        } else if (HARVEST_LEVEL < 2 && pBlock.is(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else {
            return HARVEST_LEVEL < 1 && pBlock.is(BlockTags.NEEDS_STONE_TOOL) ? false : pBlock.is(ActuallyTags.Blocks.MINEABLE_WITH_DRILL);
        }
    }

    /**
     * Gets all of the Slots from NBT
     *
     * @param stack The Drill
     */
    public static void loadSlotsFromNBT(IItemHandlerModifiable slots, ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();
        TileEntityInventoryBase.loadSlots(slots, compound);
    }

    /**
     * Writes all of the Slots to NBT
     *
     * @param slots The Slots
     * @param stack The Drill
     */
    public static void writeSlotsToNBT(IItemHandler slots, ItemStack stack) {
        CompoundTag compound = stack.getOrCreateTag();

        TileEntityInventoryBase.saveSlots(slots, compound);
        stack.setTag(compound);
    }

    // SkyTekx: the placer augment of upstream 7e7fd0d8, 846172e6 and 9bd1fa63. The base swapped a copy
    // of the hotbar stack into the hand and placed it with the drill's own context, so BlockItem placed
    // and shrank the drill stack of that context. Now the hotbar stack places itself with a context of
    // its own, ItemStack#useOn posts the Forge place events (which Bukkit plugins see on Arclight), and
    // the stack in the hotbar slot shrinks
    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();

        ItemStack stack = context.getItemInHand();
        ItemStack upgrade = this.getHasUpgradeAsStack(stack, ItemDrillUpgrade.UpgradeType.PLACER);
        if (!upgrade.isEmpty() && player != null) {
            int slot = ItemDrillUpgrade.getSlotToPlaceFrom(upgrade);
            if (slot >= 0 && slot < Inventory.getSelectionSize()) {
                ItemStack equip = player.getInventory().getItem(slot);
                if (!equip.isEmpty() && equip != stack) {
                    //Places the Block into the World
                    BlockHitResult result = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), context.getClickedPos(), context.isInside());
                    return equip.useOn(new UseOnContext(level, player, hand, equip, result));
                }
            }
        }

        return super.useOn(context);
    }

    /**
     * Checks if a certain Upgrade is installed and returns it as an ItemStack
     *
     * @param stack   The Drill
     * @param upgrade The Upgrade to be checked
     * @return The Upgrade, if it's installed
     */
    public ItemStack getHasUpgradeAsStack(ItemStack stack, ItemDrillUpgrade.UpgradeType upgrade) {
        ItemStackHandlerAA inv = readUpgradeSlots(stack);
        if (inv != null) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack slotStack = inv.getStackInSlot(i);
                if (!slotStack.isEmpty() && slotStack.getItem() instanceof ItemDrillUpgrade drillUpgrade) {
                    if (drillUpgrade.type == upgrade) {
                        return slotStack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * SkyTekx: the upgrade slots in the NBT key Items, or null for a drill without a tag. Unlike
     * loadSlotsFromNBT this never adds an empty tag to a drill it only reads
     */
    @Nullable
    private static ItemStackHandlerAA readUpgradeSlots(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound == null || !compound.contains("Items")) {
            return null;
        }
        ItemStackHandlerAA inv = new ItemStackHandlerAA(ContainerDrill.SLOT_AMOUNT);
        TileEntityInventoryBase.loadSlots(inv, compound);
        return inv;
    }

    /**
     * Gets all of the Upgrades that are installed in the Drill (upstream d84518fe)
     *
     * @param drill The Drill
     * @return A List of all Upgrades
     */
    public static List<ItemDrillUpgrade.UpgradeType> getUpgrades(@Nonnull ItemStack drill) {
        List<ItemDrillUpgrade.UpgradeType> upgrades = new ArrayList<>();
        ItemStackHandlerAA inv = readUpgradeSlots(drill);
        if (inv != null) {
            for (int i = 0; i < inv.getSlots(); i++) {
                ItemStack slotStack = inv.getStackInSlot(i);
                if (!slotStack.isEmpty() && slotStack.getItem() instanceof ItemDrillUpgrade drillUpgrade) {
                    upgrades.add(drillUpgrade.type);
                }
            }
        }
        return upgrades;
    }

    /**
     * Gets all of the Upgrades that are installed in the Drill as a String (upstream d84518fe)
     *
     * @param drill The Drill
     * @return A String of all Upgrades
     */
    public static String getUpgradesString(@Nonnull ItemStack drill) {
        List<ItemDrillUpgrade.UpgradeType> upgrades = getUpgrades(drill);
        StringBuilder sb = new StringBuilder();
        for (ItemDrillUpgrade.UpgradeType upgrade : upgrades) {
            String name = switch (upgrade) {
                case SPEED -> "Speed";
                case SPEED_II -> "Speed II";
                case SPEED_III -> "Speed III";
                case SILK_TOUCH -> "Silk Touch";
                case FORTUNE -> "Fortune";
                case FORTUNE_II -> "Fortune II";
                case THREE_BY_THREE -> "3x3";
                case FIVE_BY_FIVE -> "5x5";
                case PLACER -> "Placer";
            };
            sb.append(name).append(", ");
        }

        if (sb.length() > 2) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide && player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ContainerDrill(id, inv), Component.translatable("container." + ActuallyAdditions.MODID + ".drill")));
//            player.openGui(ActuallyAdditions.INSTANCE, GuiHandler.GuiTypes.DRILL.ordinal(), world, (int) player.posX, (int) player.posY, (int) player.posZ);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, player.getItemInHand(hand));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entityHit, InteractionHand hand) {
        int use = this.getEnergyUsePerBlock(stack);
        if (!(entityHit instanceof Player) || !((Player) entityHit).isCreative()) {
            if (this.getEnergyStored(stack) >= use) {
                this.extractEnergyInternal(stack, use, false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.getEnergyStored(stack) >= ENERGY_USE
                    ? this.attributesPowered
                    : this.attributesUnpowered;
        }
        return super.getAttributeModifiers(slot, stack);
    }


    // SkyTekx: upstream d9961a39. The base answered 1.0 for every block outside the drill tag and gave
    // the upgrade speed only to blocks on the extra mining whitelist, so a charged drill mined stone and
    // ores at hand speed
    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        return this.getEnergyStored(stack) >= this.getEnergyUsePerBlock(stack)
                ? (this.hasExtraWhitelist(state.getBlock()) || state.is(ActuallyTags.Blocks.MINEABLE_WITH_DRILL))
                ? this.getEfficiencyFromUpgrade(stack)
                : 1.0F
                : 0.1F;
    }

    // SkyTekx: 1.20.1 keeps Item#onBlockStartBreak, which main moved into a BreakEvent listener for
    // 1.21 (32625541, 92dfcb47). The trace of d36cb9c7, cf8e56ab and 2fbf2984 replaces the base's
    // stub, which always answered the bottom face of 0, 0, 0, so the 3x3 and 5x5 areas were always
    // laid flat whatever face the player mined
    @Override
    public boolean onBlockStartBreak(@Nonnull ItemStack stack, @Nonnull BlockPos pos, @Nonnull Player player) {
        if (BREAKING_AREA.get()) {
            return false;
        }
        BREAKING_AREA.set(Boolean.TRUE);
        try {
            return this.breakArea(stack, pos, player);
        } finally {
            BREAKING_AREA.set(Boolean.FALSE);
        }
    }

    private boolean breakArea(ItemStack stack, BlockPos pos, Player player) {
        boolean toReturn = false;
        int use = this.getEnergyUsePerBlock(stack);
        List<ItemDrillUpgrade.UpgradeType> upgrades = getUpgrades(stack);

        if (this.getEnergyStored(stack) >= use) {
            //Enchants the Drill depending on the Upgrades it has
            if (upgrades.contains(ItemDrillUpgrade.UpgradeType.SILK_TOUCH)) {
                ItemUtil.addEnchantment(stack, Enchantments.SILK_TOUCH, 1);
            } else {
                if (upgrades.contains(ItemDrillUpgrade.UpgradeType.FORTUNE)) {
                    ItemUtil.addEnchantment(stack, Enchantments.BLOCK_FORTUNE, upgrades.contains(ItemDrillUpgrade.UpgradeType.FORTUNE_II)
                            ? 3
                            : 1);
                }
            }

            //Block hit
            HitResult ray = player.pick(Util.getReachDistance(player), 1f, false);
            if (ray instanceof BlockHitResult trace) {
                //Breaks the Blocks
                if (!player.isShiftKeyDown() && upgrades.contains(ItemDrillUpgrade.UpgradeType.THREE_BY_THREE)) {
                    if (upgrades.contains(ItemDrillUpgrade.UpgradeType.FIVE_BY_FIVE)) {
                        toReturn = this.breakBlocks(stack, 2, player.level(), pos, trace.getDirection(), player);
                    } else {
                        toReturn = this.breakBlocks(stack, 1, player.level(), pos, trace.getDirection(), player);
                    }
                } else {
                    toReturn = this.breakBlocks(stack, 0, player.level(), pos, trace.getDirection(), player);
                }
            }

            //Removes Enchantments added above
            ItemUtil.removeEnchantment(stack, Enchantments.SILK_TOUCH);
            ItemUtil.removeEnchantment(stack, Enchantments.BLOCK_FORTUNE);
        }
        return toReturn;
    }

    // SkyTekx: upstream ed8ea0e6. The drop check knows the current power and otherwise follows the
    // Netherite tier on the drill tag (isCorrectToolForDrops(BlockState) above), replacing the 1.12
    // block list of the base
    @Override
    public boolean isCorrectToolForDrops(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        return this.getEnergyStored(stack) >= this.getEnergyUsePerBlock(stack) && super.isCorrectToolForDrops(stack, state);
    }

    /**
     * Gets the Energy that is used per Block broken
     *
     * @param stack The Drill
     * @return The Energy use per Block
     */
    public int getEnergyUsePerBlock(ItemStack stack) {
        int use = ENERGY_USE;
        List<ItemDrillUpgrade.UpgradeType> upgrades = getUpgrades(stack);

        //Speed
        if (upgrades.contains(ItemDrillUpgrade.UpgradeType.SPEED)) {
            use += 50;
            if (upgrades.contains(ItemDrillUpgrade.UpgradeType.SPEED_II)) {
                use += 75;
                if (upgrades.contains(ItemDrillUpgrade.UpgradeType.SPEED_III)) {
                    use += 175;
                }
            }
        }

        //Silk Touch
        if (upgrades.contains(ItemDrillUpgrade.UpgradeType.SILK_TOUCH)) {
            use += 100;
        }

        //Fortune
        if (upgrades.contains(ItemDrillUpgrade.UpgradeType.FORTUNE)) {
            use += 40;
            if (upgrades.contains(ItemDrillUpgrade.UpgradeType.FORTUNE_II)) {
                use += 80;
            }
        }

        //Size
        if (upgrades.contains(ItemDrillUpgrade.UpgradeType.THREE_BY_THREE)) {
            use += 10;
            if (upgrades.contains(ItemDrillUpgrade.UpgradeType.FIVE_BY_FIVE)) {
                use += 30;
            }
        }

        return use;
    }

    /**
     * Checks if a certain Upgrade is applied
     *
     * @param stack   The Drill
     * @param upgrade The Upgrade to be checked
     * @return Is the Upgrade applied?
     */
    public boolean getHasUpgrade(ItemStack stack, ItemDrillUpgrade.UpgradeType upgrade) {
        return !this.getHasUpgradeAsStack(stack, upgrade).isEmpty();
    }

    /**
     * Gets the Mining Speed of the Drill
     *
     * @param stack The Drill
     * @return The Mining Speed depending on the Speed Upgrades
     */
    public float getEfficiencyFromUpgrade(ItemStack stack) {
        float efficiency = 8.0F;
        List<ItemDrillUpgrade.UpgradeType> upgrades = getUpgrades(stack);

        if (upgrades.contains(ItemDrillUpgrade.UpgradeType.SPEED)) {
            if (upgrades.contains(ItemDrillUpgrade.UpgradeType.SPEED_II)) {
                if (upgrades.contains(ItemDrillUpgrade.UpgradeType.SPEED_III)) {
                    efficiency += 37.0F;
                } else {
                    efficiency += 25.0F;
                }
            } else {
                efficiency += 8.0F;
            }
        }
        if (upgrades.contains(ItemDrillUpgrade.UpgradeType.THREE_BY_THREE)) {
            efficiency *= 0.5F;
            if (upgrades.contains(ItemDrillUpgrade.UpgradeType.FIVE_BY_FIVE)) {
                efficiency *= 0.35F;
            }
        }
        return efficiency;
    }

    /**
     * Breaks Blocks in a certain Radius
     * Has to be called on both Server and Client
     *
     * @param stack  The Drill
     * @param radius The Radius to break Blocks in (0 means only 1 Block will be broken!)
     * @param world  The World
     * @param player The Player who breaks the Blocks
     */
    public boolean breakBlocks(ItemStack stack, int radius, Level world, BlockPos aPos, Direction side, Player player) {
        int xRange = radius;
        int yRange = radius;
        int zRange = 0;

        //Corrects Blocks to hit depending on Side of original Block hit
        if (side.getAxis() == Direction.Axis.Y) {
            zRange = radius;
            yRange = 0;
        }
        if (side.getAxis() == Direction.Axis.X) {
            xRange = 0;
            zRange = radius;
        }

        //Not defined later because main Block is getting broken below
        BlockState state = world.getBlockState(aPos);
        float mainHardness = state.getDestroySpeed(world, aPos);

        //Break Middle Block first
        int use = this.getEnergyUsePerBlock(stack);
        if (this.getEnergyStored(stack) >= use) {
            if (!this.tryHarvestBlock(world, aPos, false, stack, player, use)) {
                return false;
            }
        } else {
            return false;
        }

        if (radius == 2 && side.getAxis() != Direction.Axis.Y) {
            aPos = aPos.above();
            BlockState theState = world.getBlockState(aPos);
            if (theState.getDestroySpeed(world, aPos) <= mainHardness + 5.0F) {
                this.tryHarvestBlock(world, aPos, true, stack, player, use);
            }
        }

        //Break Blocks around
        if (radius > 0 && mainHardness >= 0.2F) {
            for (int xPos = aPos.getX() - xRange; xPos <= aPos.getX() + xRange; xPos++) {
                for (int yPos = aPos.getY() - yRange; yPos <= aPos.getY() + yRange; yPos++) {
                    for (int zPos = aPos.getZ() - zRange; zPos <= aPos.getZ() + zRange; zPos++) {
                        if (!(aPos.getX() == xPos && aPos.getY() == yPos && aPos.getZ() == zPos)) {
                            if (this.getEnergyStored(stack) >= use) {
                                //Only break Blocks around that are (about) as hard or softer
                                BlockPos thePos = new BlockPos(xPos, yPos, zPos);
                                BlockState theState = world.getBlockState(thePos);
                                if (theState.getDestroySpeed(world, thePos) <= mainHardness + 5.0F) {
                                    this.tryHarvestBlock(world, thePos, true, stack, player, use);
                                }
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Generate a list of block positions that can be broken taking radius, poker and side into account
     * (upstream f390ffd4, for the block outlines of ClientEvents#renderBlockHighlight)
     * @param stack The Drill
     * @param radius The Radius to break Blocks in (0 means only 1 Block will be broken!)
     * @param world The World
     * @param aPos The position of the block being broken
     * @param side The side of the block being broken
     * @param player The Player who breaks the Blocks
     * @return A list of block positions that can be broken
     */
    public List<BlockPos> gatherBreakingPositions(ItemStack stack, int radius, Level world, BlockPos aPos, Direction side, Player player) {
        int energyStored = this.getEnergyStored(stack);
        List<BlockPos> positions = new ArrayList<>();

        int xRange = radius;
        int yRange = radius;
        int zRange = 0;

        //Corrects Blocks to hit depending on Side of original Block hit
        if (side.getAxis() == Direction.Axis.Y) {
            zRange = radius;
            yRange = 0;
        }
        if (side.getAxis() == Direction.Axis.X) {
            xRange = 0;
            zRange = radius;
        }

        //Not defined later because main Block is getting broken below
        BlockState state = world.getBlockState(aPos);
        float mainHardness = state.getDestroySpeed(world, aPos);

        //Break Middle Block first
        int use = this.getEnergyUsePerBlock(stack);
        if (energyStored < use) {
            return positions;
        }

        if (radius == 2 && side.getAxis() != Direction.Axis.Y) {
            aPos = aPos.above();
            BlockState theState = world.getBlockState(aPos);
            if (theState.getDestroySpeed(world, aPos) <= mainHardness + 5.0F) {
                positions.add(aPos.immutable());
            }
        }

        //Break Blocks around
        if (radius > 0 && mainHardness >= 0.2F) {
            for (int xPos = aPos.getX() - xRange; xPos <= aPos.getX() + xRange; xPos++) {
                for (int yPos = aPos.getY() - yRange; yPos <= aPos.getY() + yRange; yPos++) {
                    for (int zPos = aPos.getZ() - zRange; zPos <= aPos.getZ() + zRange; zPos++) {
                        if (!(aPos.getX() == xPos && aPos.getY() == yPos && aPos.getZ() == zPos)) {
                            if (energyStored >= use) {
                                //Only break Blocks around that are (about) as hard or softer
                                BlockPos thePos = new BlockPos(xPos, yPos, zPos);
                                BlockState theState = world.getBlockState(thePos);
                                if (theState.getDestroySpeed(world, thePos) <= mainHardness + 5.0F) {
                                    energyStored -= use;
                                    positions.add(thePos.immutable());
                                }
                            } else {
                                return positions;
                            }
                        }
                    }
                }
            }
        }
        return positions;
    }

    /**
     * Tries to harvest a certain Block
     * Breaks the Block, drops Particles etc.
     * Has to be called on both Server and Client
     *
     * @param world   The World
     * @param isExtra If the Block is the Block that was looked at when breaking or an additional Block
     * @param stack   The Drill
     * @param player  The Player breaking the Blocks
     * @param use     The Energy that should be extracted per Block
     */
    private boolean tryHarvestBlock(Level world, BlockPos pos, boolean isExtra, ItemStack stack, Player player, int use) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        float hardness = state.getDestroySpeed(world, pos);

        boolean canHarvest = (ForgeHooks.isCorrectToolForDrops(state, player) || this.isCorrectToolForDrops(stack, state)) && (!isExtra || this.getDestroySpeed(stack, world.getBlockState(pos)) > 1.0F);
        if (hardness >= 0.0F && (!isExtra || canHarvest && !state.hasBlockEntity())) {
            if (!player.isCreative()) {
                this.extractEnergyInternal(stack, use, false);
            }
            //Break the Block
            // SkyTekx: isExtra tells WorldUtil#breakExtraBlock whether the vanilla break already posted
            // the break event of this block
            return WorldUtil.breakExtraBlock(stack, world, player, pos, isExtra);
        }
        return false;
    }

    private boolean hasExtraWhitelist(Block block) {
        if (block != null) {
            ResourceLocation location = ForgeRegistries.BLOCKS.getKey(block);
            if (location != null) {
                String name = location.toString();
                if (name != null) {
                    for (String s : CommonConfig.ItemSettings.DRILL_EXTRA_MINING_WHITELIST.get()) {
                        if (s != null && s.equals(name)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldCauseBlockBreakReset(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack) {
        return !ItemStack.isSameItem(newStack, oldStack);
    }

    // SkyTekx: the augment list of upstream d84518fe and the placer slot of b7875a09 (#1463). Client
    // only like the tooltip of ItemEnergy, which the dedicated server strips
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        super.appendHoverText(stack, level, tooltip, flagIn);

        String augmentList = getUpgradesString(stack);
        tooltip.add(Component.translatable("tooltip.actuallyadditions.drill.augments", augmentList).withStyle(ChatFormatting.GRAY));

        ItemStack placer = this.getHasUpgradeAsStack(stack, ItemDrillUpgrade.UpgradeType.PLACER);
        if (!placer.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.actuallyadditions.placer_augment", ItemDrillUpgrade.getSlotToPlaceFrom(placer) + 1).withStyle(ChatFormatting.GRAY));
        }
    }
}
