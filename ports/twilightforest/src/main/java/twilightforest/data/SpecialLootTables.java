package twilightforest.data;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import twilightforest.init.TFBlocks;
import twilightforest.loot.TFLootTables;

import java.util.function.BiConsumer;

public class SpecialLootTables implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
        consumer.accept(TFLootTables.CICADA_SQUISH_DROPS, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(Items.GRAY_DYE))));
        consumer.accept(TFLootTables.FIREFLY_SQUISH_DROPS, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(Items.GLOWSTONE_DUST))));
        consumer.accept(TFLootTables.MOONWORM_SQUISH_DROPS, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(Items.LIME_DYE))));

        consumer.accept(TFLootTables.MOONWORM_FAILED_TO_PLACE_DROPS, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(TFBlocks.MOONWORM.get().asItem()))));

        consumer.accept(TFLootTables.KNIGHT_PHANTOM_DEFEATED, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(TFBlocks.KNIGHT_PHANTOM_TROPHY.get().asItem()))));

        consumer.accept(TFLootTables.LIFEDRAIN_SCEPTER_KILL_BONUS, LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH)).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
        ));
    }
}
