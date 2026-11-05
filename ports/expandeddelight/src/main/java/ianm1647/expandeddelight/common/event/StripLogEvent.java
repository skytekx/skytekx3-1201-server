package ianm1647.expandeddelight.common.event;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.registry.EDBlocks;
import ianm1647.expandeddelight.common.registry.EDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.world.level.block.Block.popResource;

/**
 * Stripping a cinnamon log gives two cinnamon sticks, stripping cinnamon wood three.
 * <p>
 * Upstream does the stripping itself in a right click event: it damages the tool in the hand that uses an
 * item rather than the clicked hand, and turns every log upright. This port answers Forge's tool
 * modification event for the axe_strip action instead, so that the axe's own stripping (sound, damage,
 * statistics, axis) does the work. On Arclight no Bukkit event covers axe stripping, for these logs or
 * for vanilla ones, and the sticks drop only on the server after a stripping that is not simulated.
 */
@Mod.EventBusSubscriber(modid = ExpandedDelight.MODID)
public class StripLogEvent {

    @SubscribeEvent
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() != ToolActions.AXE_STRIP) {
            return;
        }
        BlockState state = event.getState();
        Block stripped;
        int sticks;
        if (state.is(EDBlocks.CINNAMON_LOG.get())) {
            stripped = EDBlocks.CINNAMON_STRIPPED_LOG.get();
            sticks = 2;
        } else if (state.is(EDBlocks.CINNAMON_WOOD.get())) {
            stripped = EDBlocks.CINNAMON_STRIPPED_WOOD.get();
            sticks = 3;
        } else {
            return;
        }
        event.setFinalState(stripped.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
        LevelAccessor level = event.getLevel();
        if (!event.isSimulated() && !level.isClientSide() && level instanceof net.minecraft.world.level.Level realLevel) {
            BlockPos pos = event.getPos();
            popResource(realLevel, pos, new ItemStack(EDItems.CINNAMON_STICK.get(), sticks));
        }
    }
}
