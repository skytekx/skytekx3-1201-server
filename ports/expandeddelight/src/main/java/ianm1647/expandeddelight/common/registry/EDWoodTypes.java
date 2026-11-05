package ianm1647.expandeddelight.common.registry;

import ianm1647.expandeddelight.ExpandedDelight;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

public class EDWoodTypes {
    public static final BlockSetType CINNAMON_SET = BlockSetType.register(new BlockSetType(ExpandedDelight.MODID + ":cinnamon"));
    public static final WoodType CINNAMON = WoodType.register(new WoodType(ExpandedDelight.MODID + ":cinnamon", CINNAMON_SET));
}
