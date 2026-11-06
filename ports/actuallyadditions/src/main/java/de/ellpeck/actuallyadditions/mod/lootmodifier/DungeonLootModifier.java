package de.ellpeck.actuallyadditions.mod.lootmodifier;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

// SkyTekx: upstream b9435301, the chest loot of the 1.12 DungeonLoot handler while villageAndDungeonLoot is on.
// On 1.21 BuiltInLootTables holds ResourceKeys, which never equal the queried ResourceLocation, so main adds
// nothing. On 1.20.1 both are ResourceLocations and the comparison works. Main's debug line on stdout for
// every drill core roll is left out
public class DungeonLootModifier extends LootModifier {
	public static final Supplier<Codec<DungeonLootModifier>> CODEC = Suppliers.memoize(() ->
			RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, DungeonLootModifier::new)));

	public DungeonLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		RandomSource random = context.getRandom();
		if (CommonConfig.Other.DUNGEON_LOOT.get()) {
			ResourceLocation lootTable = context.getQueriedLootTableId();
			boolean addCrystals = false;
			boolean addDrillCore = false;
			boolean addQuartz = false;
			boolean addBatWings = false;
			if (BuiltInLootTables.SIMPLE_DUNGEON.equals(lootTable)) {
				addCrystals = true;
				addDrillCore = true;
				addQuartz = true;
			} else if (BuiltInLootTables.ABANDONED_MINESHAFT.equals(lootTable)) {
				addCrystals = true;
				addDrillCore = true;
			} else if (BuiltInLootTables.VILLAGE_WEAPONSMITH.equals(lootTable)) {
				addDrillCore = true;
				addQuartz = true;
			} else if (BuiltInLootTables.STRONGHOLD_LIBRARY.equals(lootTable)) {
				addBatWings = true;
			} else if (BuiltInLootTables.IGLOO_CHEST.equals(lootTable)) {
				addBatWings = true;
			} else if (BuiltInLootTables.DESERT_PYRAMID.equals(lootTable)) {
				addDrillCore = true;
				addBatWings = true;
			} else if (BuiltInLootTables.NETHER_BRIDGE.equals(lootTable)) {
				addBatWings = true;
				addCrystals = true;
				addDrillCore = true;
			} else if (BuiltInLootTables.END_CITY_TREASURE.equals(lootTable)) {
				addBatWings = true;
				addCrystals = true;
				addDrillCore = true;
				addQuartz = true;
			} else if (BuiltInLootTables.WOODLAND_MANSION.equals(lootTable)) {
				addBatWings = true;
				addCrystals = true;
				addDrillCore = true;
				addQuartz = true;
			}

			if (addCrystals) {
				if (random.nextInt(5) == 0) {
					int count = random.nextInt(3) + 1;
					Item crystal = getRandomItem(random, ActuallyTags.Items.CRYSTALS, ActuallyItems.RESTONIA_CRYSTAL.get());
					generatedLoot.add(new ItemStack(crystal, count));
				}

				if (random.nextInt(15) == 0) {
					int count = random.nextInt(3) + 1;
					Item crystal = getRandomItem(random, ActuallyTags.Items.CRYSTAL_BLOCKS, ActuallyBlocks.RESTONIA_CRYSTAL.getItem());
					generatedLoot.add(new ItemStack(crystal, count));
				}
			}
			if (addDrillCore) {
				if (random.nextInt(10) == 0) {
					generatedLoot.add(new ItemStack(ActuallyItems.DRILL_CORE.get()));
				}
			}
			if (addQuartz) {
				if (random.nextInt(5) == 0) {
					int count = random.nextInt(5) + 1;
					generatedLoot.add(new ItemStack(ActuallyItems.BLACK_QUARTZ.get(), count));
				}
			}
			if (addBatWings) {
				if (random.nextInt(10) == 0) {
					int count = random.nextInt(2) + 1;
					generatedLoot.add(new ItemStack(ActuallyItems.BATS_WING.get(), count));
				}
			}
		}
		return generatedLoot;
	}

	private Item getRandomItem(RandomSource random, TagKey<Item> tagKey, Item defaultItem) {
		HolderSet.Named<Item> holderSet = BuiltInRegistries.ITEM.getTag(tagKey).orElse(null);
		if (holderSet != null) {
			Holder<Item> itemHolder = holderSet.getRandomElement(random).orElse(null);
			if (itemHolder != null) {
				return itemHolder.value();
			}
		}
		return defaultItem;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return ActuallyLootModifiers.DUNGEON_LOOT.get();
	}
}
