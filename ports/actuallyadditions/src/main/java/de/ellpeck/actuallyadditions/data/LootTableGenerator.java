package de.ellpeck.actuallyadditions.data;


import com.google.common.collect.ImmutableSet;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.fluids.InitFluids;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import de.ellpeck.actuallyadditions.mod.util.LootTableUtil;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LootTableGenerator extends LootTableProvider {
    public LootTableGenerator(PackOutput packOutput) {
        super(packOutput, Set.of(), List.of(
                new SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK),
                new SubProviderEntry(ItemWorm::new, LootContextParamSets.BLOCK)
        ));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, @Nonnull ValidationContext validationtracker) {
        map.forEach((name, table) -> table.validate(validationtracker));
    }

    public static class Blocks extends BlockLootSubProvider {

        protected Blocks() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            CopyNbtFunction.Builder copyEnergy = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Energy", "BlockEntityTag.Energy");
            CopyNbtFunction.Builder copyPulseMode = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("IsPulseMode", "BlockEntityTag.IsPulseMode");
            // SkyTekx: upstream c1e274db, f05b284e and 2b8884da keep the contents as data components. On 1.20.1
            // copy_nbt takes the same keys from the block entity into BlockEntityTag, which BlockItem merges
            // back on placement. A single tank writes its FluidStack at the root of the block entity
            // (FluidName, Amount and an optional Tag), the Fermenting Barrel keeps its two tanks in "tanks".
            // A missing key, such as Tag, is skipped. StopDrop, Redstone and the work timers stay behind
            CopyNbtFunction.Builder copyFluid = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
                .copy("FluidName", "BlockEntityTag.FluidName")
                .copy("Amount", "BlockEntityTag.Amount")
                .copy("Tag", "BlockEntityTag.Tag");
            CopyNbtFunction.Builder copyInputTank = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("tanks.inputTank", "BlockEntityTag.tanks.inputTank");
            CopyNbtFunction.Builder copyOutputTank = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("tanks.outputTank", "BlockEntityTag.tanks.outputTank");
            CopyNbtFunction.Builder copyCoffeeCache = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Cache", "BlockEntityTag.Cache");

            //Special Drops
            dropNBT(ActuallyBlocks.ATOMIC_RECONSTRUCTOR, $ -> $.apply(copyEnergy).apply(copyPulseMode));
            dropKeepEnergy(ActuallyBlocks.DISPLAY_STAND);
            dropKeepEnergy(ActuallyBlocks.COAL_GENERATOR);
            dropNBT(ActuallyBlocks.OIL_GENERATOR, $ -> $.apply(copyEnergy).apply(copyFluid));
            dropKeepEnergy(ActuallyBlocks.LEAF_GENERATOR);
            dropKeepEnergy(ActuallyBlocks.CRUSHER);
            dropKeepEnergy(ActuallyBlocks.CRUSHER_DOUBLE);
            dropKeepEnergy(ActuallyBlocks.POWERED_FURNACE);
            dropNBT(ActuallyBlocks.COFFEE_MACHINE, $ -> $.apply(copyEnergy).apply(copyFluid).apply(copyCoffeeCache));
            dropNBT(ActuallyBlocks.FLUID_COLLECTOR, $ -> $.apply(copyFluid).apply(copyPulseMode));
            dropNBT(ActuallyBlocks.FLUID_PLACER, $ -> $.apply(copyFluid).apply(copyPulseMode));
            dropKeepPulseMode(ActuallyBlocks.BREAKER);
            dropKeepPulseMode(ActuallyBlocks.PLACER);
            dropKeepPulseMode(ActuallyBlocks.DROPPER);
            dropNBT(ActuallyBlocks.CANOLA_PRESS, $ -> $.apply(copyEnergy).apply(copyFluid));
            dropNBT(ActuallyBlocks.FERMENTING_BARREL, $ -> $.apply(copyInputTank).apply(copyOutputTank));
            // SkyTekx: only the energy, as on main. The work area of the compass stays with the block
            dropKeepEnergy(ActuallyBlocks.FARMER);

            this.dropSelf(ActuallyBlocks.BATTERY_BOX.get());
            this.dropSelf(ActuallyBlocks.ITEM_INTERFACE_HOPPING.get());
            this.dropSelf(ActuallyBlocks.BIOREACTOR.get());
            this.dropSelf(ActuallyBlocks.EMPOWERER.get());
            this.dropSelf(ActuallyBlocks.TINY_TORCH.get());
            this.dropSelf(ActuallyBlocks.SHOCK_SUPPRESSOR.get());
            this.dropSelf(ActuallyBlocks.PLAYER_INTERFACE.get());
            this.dropSelf(ActuallyBlocks.ITEM_INTERFACE.get());
            this.dropSelf(ActuallyBlocks.FIREWORK_BOX.get());
            this.dropSelf(ActuallyBlocks.VERTICAL_DIGGER.get());
            this.dropSelf(ActuallyBlocks.ENERGIZER.get());
            this.dropSelf(ActuallyBlocks.ENERVATOR.get());
            this.dropSelf(ActuallyBlocks.LAVA_FACTORY_CONTROLLER.get());
            this.dropSelf(ActuallyBlocks.PHANTOM_ITEMFACE.get());
            this.dropSelf(ActuallyBlocks.PHANTOM_PLACER.get());
            this.dropSelf(ActuallyBlocks.PHANTOM_LIQUIFACE.get());
            this.dropSelf(ActuallyBlocks.PHANTOM_ENERGYFACE.get());
            this.dropSelf(ActuallyBlocks.PHANTOM_REDSTONEFACE.get());
            this.dropSelf(ActuallyBlocks.PHANTOM_BREAKER.get());
            this.dropSelf(ActuallyBlocks.FEEDER.get());
            this.dropSelf(ActuallyBlocks.HEAT_COLLECTOR.get());
            this.dropSelf(ActuallyBlocks.GREENHOUSE_GLASS.get());
            this.dropSelf(ActuallyBlocks.PHANTOM_BOOSTER.get());
            this.dropSelf(ActuallyBlocks.RANGED_COLLECTOR.get());
            this.dropSelf(ActuallyBlocks.LONG_RANGE_BREAKER.get());
            this.dropSelf(ActuallyBlocks.XP_SOLIDIFIER.get());
            this.dropSelf(ActuallyBlocks.LASER_RELAY.get());
            this.dropSelf(ActuallyBlocks.LASER_RELAY_ADVANCED.get());
            this.dropSelf(ActuallyBlocks.LASER_RELAY_EXTREME.get());
            this.dropSelf(ActuallyBlocks.LASER_RELAY_FLUIDS.get());
            this.dropSelf(ActuallyBlocks.LASER_RELAY_ITEM.get());
            this.dropSelf(ActuallyBlocks.LASER_RELAY_ITEM_ADVANCED.get());
            this.dropSelf(ActuallyBlocks.ETHETIC_GREEN_BLOCK.get());
            this.dropSelf(ActuallyBlocks.ETHETIC_WHITE_BLOCK.get());
            this.dropSelf(ActuallyBlocks.ETHETIC_GREEN_STAIRS.get());
            this.dropSelf(ActuallyBlocks.ETHETIC_WHITE_STAIRS.get());
            this.dropSelf(ActuallyBlocks.ETHETIC_GREEN_SLAB.get());
            this.dropSelf(ActuallyBlocks.ETHETIC_WHITE_SLAB.get());
            this.dropSelf(ActuallyBlocks.ETHETIC_GREEN_WALL.get());
            this.dropSelf(ActuallyBlocks.ETHETIC_WHITE_WALL.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ.get());
            this.dropSelf(ActuallyBlocks.SMOOTH_BLACK_QUARTZ.get());
            this.dropSelf(ActuallyBlocks.CHISELED_BLACK_QUARTZ.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_PILLAR.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_BRICK.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_WALL.get());
            this.dropSelf(ActuallyBlocks.SMOOTH_BLACK_QUARTZ_WALL.get());
            this.dropSelf(ActuallyBlocks.CHISELED_BLACK_QUARTZ_WALL.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_PILLAR_WALL.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_BRICK_WALL.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_STAIR.get());
            this.dropSelf(ActuallyBlocks.SMOOTH_BLACK_QUARTZ_STAIR.get());
            this.dropSelf(ActuallyBlocks.CHISELED_BLACK_QUARTZ_STAIR.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_PILLAR_STAIR.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_BRICK_STAIR.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_SLAB.get());
            this.dropSelf(ActuallyBlocks.SMOOTH_BLACK_QUARTZ_SLAB.get());
            this.dropSelf(ActuallyBlocks.CHISELED_BLACK_QUARTZ_SLAB.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_PILLAR_SLAB.get());
            this.dropSelf(ActuallyBlocks.BLACK_QUARTZ_BRICK_SLAB.get());
            this.dropSelf(ActuallyBlocks.LAMP_WHITE.get());
            this.dropSelf(ActuallyBlocks.LAMP_ORANGE.get());
            this.dropSelf(ActuallyBlocks.LAMP_MAGENTA.get());
            this.dropSelf(ActuallyBlocks.LAMP_LIGHT_BLUE.get());
            this.dropSelf(ActuallyBlocks.LAMP_YELLOW.get());
            this.dropSelf(ActuallyBlocks.LAMP_LIME.get());
            this.dropSelf(ActuallyBlocks.LAMP_PINK.get());
            this.dropSelf(ActuallyBlocks.LAMP_GRAY.get());
            this.dropSelf(ActuallyBlocks.LAMP_LIGHT_GRAY.get());
            this.dropSelf(ActuallyBlocks.LAMP_CYAN.get());
            this.dropSelf(ActuallyBlocks.LAMP_PURPLE.get());
            this.dropSelf(ActuallyBlocks.LAMP_BLUE.get());
            this.dropSelf(ActuallyBlocks.LAMP_BROWN.get());
            this.dropSelf(ActuallyBlocks.LAMP_GREEN.get());
            this.dropSelf(ActuallyBlocks.LAMP_RED.get());
            this.dropSelf(ActuallyBlocks.LAMP_BLACK.get());
            this.dropSelf(ActuallyBlocks.LAMP_CONTROLLER.get());
            this.dropSelf(ActuallyBlocks.ENDER_CASING.get());
            this.dropSelf(ActuallyBlocks.IRON_CASING.get());
            //this.dropSelf(ActuallyBlocks.IRON_CASING_SNOW.get());
            this.dropSelf(ActuallyBlocks.LAVA_FACTORY_CASING.get());
            this.dropSelf(ActuallyBlocks.WOOD_CASING.get());

            this.dropSelf(ActuallyBlocks.ENORI_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.RESTONIA_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.PALIS_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.DIAMATINE_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.VOID_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.EMERADIC_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.EMPOWERED_ENORI_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.EMPOWERED_RESTONIA_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.EMPOWERED_PALIS_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.EMPOWERED_DIAMATINE_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.EMPOWERED_VOID_CRYSTAL.get());
            this.dropSelf(ActuallyBlocks.EMPOWERED_EMERADIC_CRYSTAL.get());

            // SkyTekx: upstream 09592c6f, a cluster drops two to eight shards, itself with silk touch
            this.registerCrystal(ActuallyBlocks.ENORI_CRYSTAL_CLUSTER, ActuallyItems.ENORI_CRYSTAL_SHARD);
            this.registerCrystal(ActuallyBlocks.RESTONIA_CRYSTAL_CLUSTER, ActuallyItems.RESTONIA_CRYSTAL_SHARD);
            this.registerCrystal(ActuallyBlocks.PALIS_CRYSTAL_CLUSTER, ActuallyItems.PALIS_CRYSTAL_SHARD);
            this.registerCrystal(ActuallyBlocks.DIAMATINE_CRYSTAL_CLUSTER, ActuallyItems.DIAMATINE_CRYSTAL_SHARD);
            this.registerCrystal(ActuallyBlocks.VOID_CRYSTAL_CLUSTER, ActuallyItems.VOID_CRYSTAL_SHARD);
            this.registerCrystal(ActuallyBlocks.EMERADIC_CRYSTAL_CLUSTER, ActuallyItems.EMERADIC_CRYSTAL_SHARD);

            // SkyTekx: upstream abbc1ae9 and 005b3ccc, the ore drops black quartz with the ore fortune bonus
            // and itself only with silk touch
            add(ActuallyBlocks.BLACK_QUARTZ_ORE.get(), createOreDrop(ActuallyBlocks.BLACK_QUARTZ_ORE.getBlock(), ActuallyItems.BLACK_QUARTZ.get()));

            // SkyTekx: upstream 506d481d (changelog 1.3.11), the drop counts of 1.12. A ripe crop drops
            // between min and min + bonus of its crop and one or two seeds, each with the uniform fortune bonus
            addCrop(ActuallyBlocks.CANOLA, ActuallyItems.CANOLA, 2, 3, ActuallyItems.CANOLA_SEEDS);
            addCrop(ActuallyBlocks.RICE, ActuallyItems.RICE, 1, 2, ActuallyItems.RICE_SEEDS);
            addCrop(ActuallyBlocks.FLAX, () -> Items.STRING, 2, 4, ActuallyItems.FLAX_SEEDS);
            addCrop(ActuallyBlocks.COFFEE, ActuallyItems.COFFEE_BEANS, 2, 2, ActuallyItems.COFFEE_BEANS);
        }

        private void addCrop(Supplier<Block> block, Supplier<Item> item, int min, int bonus, Supplier<Item> seed) {
            LootItemBlockStatePropertyCondition.Builder cropCondition = LootItemBlockStatePropertyCondition.hasBlockStateProperties(block.get()).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7));
            LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool().add(LootItem
                    .lootTableItem(item.get()).when(cropCondition)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, min + bonus)))
                    .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
                    .otherwise(LootItem.lootTableItem(seed.get())))
                )
                .withPool(LootPool.lootPool().add(LootItem.lootTableItem(seed.get()).when(cropCondition)
                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                    .apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))));

            add(block.get(), applyExplosionDecay(block.get(), table));
        }

        private void dropNBT(Supplier<Block> blockSupplier, Consumer<LootPool.Builder> lootFunctionProvider) {
            LootPool.Builder lootpool = LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(blockSupplier.get()));

            lootFunctionProvider.accept(lootpool);

            add(blockSupplier.get(), LootTable.lootTable().withPool(applyExplosionCondition(ActuallyBlocks.ATOMIC_RECONSTRUCTOR.get(), lootpool)));
        }
        private void dropKeepPulseMode(Supplier<Block> blockSupplier) {
            dropNBT(blockSupplier, $ -> $.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("IsPulseMode", "BlockEntityTag.IsPulseMode")));
        }
        private void dropKeepEnergy(Supplier<Block> blockSupplier) {
            dropNBT(blockSupplier, $ -> $.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Energy", "BlockEntityTag.Energy")));
        }

        private void registerCrystal(Supplier<? extends Block> crystalCluster, Supplier<? extends Item> crystalShard) {
            this.add(crystalCluster.get(), block ->
                    this.createSingleItemTableWithSilkTouch(block, crystalShard.get(), UniformGenerator.between(2.0F, 8.0F)));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            final Set<Block> ignoreForNow = ImmutableSet.of(
                InitFluids.CANOLA_OIL.getBlock(),
                InitFluids.REFINED_CANOLA_OIL.getBlock(),
                InitFluids.CRYSTALLIZED_OIL.getBlock(),
                InitFluids.EMPOWERED_OIL.getBlock()
            );

            return ActuallyBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get).filter(e -> !ignoreForNow.contains(e)).collect(Collectors.toList());
        }
    }

    /**
     * SkyTekx: the worm drop of upstream 159fd67c (class name of c8045ff5), which Worm#onHoe rolls
     */
    public static class ItemWorm implements LootTableSubProvider {
        @Override
        public void generate(@Nonnull BiConsumer<ResourceLocation, LootTable.Builder> output) {
            output.accept(
                    LootTableUtil.WORM_DROP,
                    LootTable.lootTable()
                            .withPool(
                                    LootPool.lootPool()
                                            .add(LootItem.lootTableItem(ActuallyItems.WORM.get())
                                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                                            .when(LootItemRandomChanceCondition.randomChance(0.05F))
                            )
            );
        }
    }
}
