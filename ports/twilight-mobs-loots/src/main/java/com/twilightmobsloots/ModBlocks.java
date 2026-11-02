package com.twilightmobsloots;

import com.twilightmobsloots.block.MobTrophyBlock;
import com.twilightmobsloots.block.MobTrophyWallBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Block registry: one floor trophy ({@code <mob>_trophy}) and one wall trophy ({@code <mob>_wall_trophy})
 * per {@link MobHead}. {@link MobTrophyItem} binds both (StandingAndWallBlockItem).
 */
public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TwilightMobsLoots.MOD_ID);

    private static final EnumMap<MobHead, RegistryObject<MobTrophyBlock>> FLOOR = new EnumMap<>(MobHead.class);
    private static final EnumMap<MobHead, RegistryObject<MobTrophyWallBlock>> WALL = new EnumMap<>(MobHead.class);

    static {
        for (MobHead head : MobHead.values()) {
            String name = head.getSerializedName();
            FLOOR.put(head, BLOCKS.register(name + "_trophy", () -> new MobTrophyBlock(head, blockProperties())));
            WALL.put(head, BLOCKS.register(name + "_wall_trophy", () -> new MobTrophyWallBlock(head, blockProperties())));
        }
    }

    private static BlockBehaviour.Properties blockProperties() {
        return BlockBehaviour.Properties.of()
                .strength(0.0F)
                .noOcclusion()
                .sound(SoundType.STONE);
    }

    public static RegistryObject<MobTrophyBlock> floor(MobHead head) {
        return FLOOR.get(head);
    }

    public static RegistryObject<MobTrophyWallBlock> wall(MobHead head) {
        return WALL.get(head);
    }

    /** All 96 trophy blocks (48 floor + 48 wall), for the block entity type. */
    public static Block[] allBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (MobHead head : MobHead.values()) {
            blocks.add(FLOOR.get(head).get());
            blocks.add(WALL.get(head).get());
        }
        return blocks.toArray(new Block[0]);
    }
}
