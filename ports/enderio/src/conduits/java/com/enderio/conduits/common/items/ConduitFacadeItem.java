package com.enderio.conduits.common.items;

import com.enderio.api.conduit.IFacadeItem;
import com.enderio.api.conduit.facade.FacadeType;
import com.enderio.base.common.util.PaintUtils;
import com.enderio.conduits.common.init.ConduitLang;
import com.enderio.core.client.item.IAdvancedTooltipProvider;
import com.enderio.core.common.util.TooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A conduit facade of Ender IO 7.1 (78e627263). The painting machine paints it like a painted block, so the paint is the block id
 * under BlockEntityTag.Paint, which {@link PaintUtils#getPaint(ItemStack)} reads. An unpainted facade cannot be placed.
 */
public class ConduitFacadeItem extends Item implements IFacadeItem, IAdvancedTooltipProvider {

    private final FacadeType type;

    public ConduitFacadeItem(Properties properties, FacadeType type) {
        super(properties);
        this.type = type;
    }

    @Override
    public BlockState getTexture(ItemStack stack) {
        @Nullable Block paint = PaintUtils.getPaint(stack);
        return paint == null ? Blocks.AIR.defaultBlockState() : paint.defaultBlockState();
    }

    @Override
    public FacadeType getFacadeType(ItemStack stack) {
        return type;
    }

    @Override
    public void addDetailedTooltips(ItemStack itemStack, @Nullable Player player, List<Component> tooltips) {
        if (type.doesHideConduits()) {
            tooltips.add(TooltipUtil.style(ConduitLang.TRANSPARENT_FACADE_TOOLTIP));
        }

        if (type.isBlastResistant()) {
            tooltips.add(TooltipUtil.style(ConduitLang.BLAST_RESIST_FACADE_TOOLTIP));
        }
    }
}
