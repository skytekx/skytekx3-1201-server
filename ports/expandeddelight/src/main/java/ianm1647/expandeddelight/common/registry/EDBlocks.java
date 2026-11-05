package ianm1647.expandeddelight.common.registry;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.block.*;
import ianm1647.expandeddelight.common.world.tree.EDTreeGrowers;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.block.WildCropBlock;

import java.util.function.Supplier;

public class EDBlocks {

    public static final DeferredRegister<Block> BLOCKS;

    public static Supplier<Block> CINNAMON_SAPLING;
    public static Supplier<Block> CINNAMON_LEAVES;
    public static Supplier<Block> CINNAMON_LOG;
    public static Supplier<Block> CINNAMON_WOOD;
    public static Supplier<Block> CINNAMON_STRIPPED_LOG;
    public static Supplier<Block> CINNAMON_STRIPPED_WOOD;
    public static Supplier<Block> CINNAMON_PLANKS;
    public static Supplier<Block> CINNAMON_STAIRS;
    public static Supplier<Block> CINNAMON_SLAB;
    public static Supplier<Block> CINNAMON_FENCE;
    public static Supplier<Block> CINNAMON_FENCE_GATE;
    public static Supplier<Block> CINNAMON_DOOR;
    public static Supplier<Block> CINNAMON_TRAPDOOR;
    public static Supplier<Block> CINNAMON_PRESSURE_PLATE;
    public static Supplier<Block> CINNAMON_BUTTON;
    public static Supplier<Block> CINNAMON_SIGN;
    public static Supplier<Block> CINNAMON_WALL_SIGN;
    public static Supplier<Block> CINNAMON_CEILING_HANGING_SIGN;
    public static Supplier<Block> CINNAMON_WALL_HANGING_SIGN;
    public static Supplier<Block> CINNAMON_CABINET;

    public static Supplier<Block> JUICER;
    public static Supplier<Block> CASK;
    public static Supplier<Block> MILK_CASK;
    public static Supplier<Block> GOAT_MILK_CASK;
    public static Supplier<Block> CHEESE_CASK;
    public static Supplier<Block> GOAT_CHEESE_CASK;

    public static Supplier<Block> CHEESE_WHEEL;
    public static Supplier<Block> GOAT_CHEESE_WHEEL;

    public static Supplier<Block> ASPARAGUS_CRATE;
    public static Supplier<Block> SWEET_POTATO_CRATE;
    public static Supplier<Block> CHILI_PEPPER_CRATE;
    public static Supplier<Block> CRANBERRY_BAG;

    public static Supplier<Block> WILD_ASPARAGUS;
    public static Supplier<Block> WILD_SWEET_POTATO;
    public static Supplier<Block> WILD_CHILI_PEPPER;
    public static Supplier<Block> WILD_PEANUTS;

    public static Supplier<Block> SALT_ORE;
    public static Supplier<Block> DEEPSLATE_SALT_ORE;

    public static Supplier<Block> ASPARAGUS_CROP;
    public static Supplier<Block> SWEET_POTATO_CROP;
    public static Supplier<Block> CHILI_PEPPER_CROP;
    public static Supplier<Block> PEANUT_CROP;

    public static Supplier<Block> CRANBERRY_PLANT;
    public static Supplier<Block> CRANBERRY_COBBLER;
    public static Supplier<Block> HONEYED_GOAT_CHEESE_TART;

    static {
        BLOCKS = DeferredRegister.create(Registries.BLOCK, ExpandedDelight.MODID);

        CINNAMON_SAPLING = BLOCKS.register("cinnamon_sapling",
                () -> new SaplingBlock(EDTreeGrowers.CINNAMON, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));
        CINNAMON_LEAVES = BLOCKS.register("cinnamon_leaves",
                () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)));
        CINNAMON_LOG = BLOCKS.register("cinnamon_log",
                () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
        CINNAMON_WOOD = BLOCKS.register("cinnamon_wood",
                () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD)));
        CINNAMON_STRIPPED_LOG = BLOCKS.register("stripped_cinnamon_log",
                () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
        CINNAMON_STRIPPED_WOOD = BLOCKS.register("stripped_cinnamon_wood",
                () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));
        CINNAMON_PLANKS = BLOCKS.register("cinnamon_planks",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
        CINNAMON_STAIRS = BLOCKS.register("cinnamon_stairs",
                () -> new StairBlock(CINNAMON_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)));
        CINNAMON_SLAB = BLOCKS.register("cinnamon_slab",
                () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)));
        CINNAMON_FENCE = BLOCKS.register("cinnamon_fence",
                () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)));
        CINNAMON_FENCE_GATE = BLOCKS.register("cinnamon_fence_gate",
                () -> new FenceGateBlock(EDWoodTypes.CINNAMON, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)));
        CINNAMON_DOOR = BLOCKS.register("cinnamon_door",
                () -> new DoorBlock(EDWoodTypes.CINNAMON.setType(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)));
        CINNAMON_TRAPDOOR = BLOCKS.register("cinnamon_trapdoor",
                () -> new TrapDoorBlock(EDWoodTypes.CINNAMON.setType(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)));
        CINNAMON_PRESSURE_PLATE = BLOCKS.register("cinnamon_pressure_plate",
                () -> new PressurePlateBlock(EDWoodTypes.CINNAMON.setType(), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)));
        CINNAMON_BUTTON = BLOCKS.register("cinnamon_button",
                () -> new ButtonBlock(EDWoodTypes.CINNAMON.setType(), 30, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON)));
        CINNAMON_SIGN = BLOCKS.register("cinnamon_sign", CinnamonStandingSignBlock::new);
        CINNAMON_WALL_SIGN = BLOCKS.register("cinnamon_wall_sign", CinnamonWallSignBlock::new);
        CINNAMON_CEILING_HANGING_SIGN = BLOCKS.register("cinnamon_ceiling_hanging_sign", CinnamonCeilingHangingSignBlock::new);
        CINNAMON_WALL_HANGING_SIGN = BLOCKS.register("cinnamon_wall_hanging_sign", CinnamonWallHangingSignBlock::new);
        CINNAMON_CABINET = BLOCKS.register("cinnamon_cabinet", CinnamonCabinetBlock::new);

        JUICER = BLOCKS.register("juicer",
                () -> new JuicerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(0.5F, 1.0F).sound(SoundType.BAMBOO_WOOD)));
        CASK = BLOCKS.register("cask",
                    () -> new CaskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F, 1.0F).sound(SoundType.CHERRY_WOOD)));
        MILK_CASK = BLOCKS.register("milk_cask",
                    () -> new MilkCaskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F, 1.0F).sound(SoundType.CHERRY_WOOD)));
        CHEESE_CASK = BLOCKS.register("cheese_cask",
                    () -> new CheeseCaskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F, 1.0F).sound(SoundType.CHERRY_WOOD)));
        GOAT_MILK_CASK = BLOCKS.register("goat_milk_cask",
                () -> new MilkCaskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F, 1.0F).sound(SoundType.CHERRY_WOOD)));
        GOAT_CHEESE_CASK = BLOCKS.register("goat_cheese_cask",
                () -> new CheeseCaskBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.0F, 1.0F).sound(SoundType.CHERRY_WOOD)));
        CHEESE_WHEEL = BLOCKS.register("cheese_wheel",
                    () -> new PieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE), EDItems.CHEESE_SLICE));
        GOAT_CHEESE_WHEEL = BLOCKS.register("goat_cheese_wheel",
                () -> new PieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE), EDItems.GOAT_CHEESE_SLICE));

        ASPARAGUS_CRATE = BLOCKS.register("asparagus_crate",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        SWEET_POTATO_CRATE = BLOCKS.register("sweet_potato_crate",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        CHILI_PEPPER_CRATE = BLOCKS.register("chili_pepper_crate",
                () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
        CRANBERRY_BAG = BLOCKS.register("cranberry_bag",
                () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.RED_WOOL)));

        WILD_ASPARAGUS  = BLOCKS.register("wild_asparagus",
                () -> new WildCropBlock(MobEffects.ABSORPTION, 6, BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS)));
        WILD_SWEET_POTATO  = BLOCKS.register("wild_sweet_potato",
                () -> new WildCropBlock(MobEffects.OOZING, 6, BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS)));
        WILD_CHILI_PEPPER  = BLOCKS.register("wild_chili_pepper",
                () -> new WildCropBlock(MobEffects.CONFUSION, 6, BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS)));
        WILD_PEANUTS  = BLOCKS.register("wild_peanuts",
                () -> new WildCropBlock(MobEffects.DAMAGE_RESISTANCE, 6, BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS)));

        SALT_ORE = BLOCKS.register("salt_ore",
                () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(3.0f, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
        DEEPSLATE_SALT_ORE = BLOCKS.register("deepslate_salt_ore",
                () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                        .strength(4.5f, 3.0f).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));

        CRANBERRY_PLANT = BLOCKS.register("cranberry_plant",
                () -> new CranberryPlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_PAD).mapColor(MapColor.PLANT).sound(SoundType.LILY_PAD)));
        CRANBERRY_COBBLER = BLOCKS.register("cranberry_cobbler",
                () -> new PieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE), EDItems.CRANBERRY_COBBLER_SLICE));
        HONEYED_GOAT_CHEESE_TART = BLOCKS.register("honeyed_goat_cheese_tart",
                () -> new PieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE), EDItems.HONEYED_GOAT_CHEESE_TART_SLICE));

        ASPARAGUS_CROP = BLOCKS.register("asparagus_crop", EDCropBlock::new);
        SWEET_POTATO_CROP = BLOCKS.register("sweet_potato_crop", EDCropBlock::new);
        CHILI_PEPPER_CROP = BLOCKS.register("chili_pepper_crop", EDCropBlock::new);
        PEANUT_CROP = BLOCKS.register("peanut_crop", EDCropBlock::new);
    }
}
