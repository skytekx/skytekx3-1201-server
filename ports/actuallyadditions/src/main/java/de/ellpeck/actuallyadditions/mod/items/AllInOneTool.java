package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.api.ActuallyTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nonnull;
import java.util.List;

public class AllInOneTool extends DiggerItem {
    // SkyTekx: the actions of upstream 4d3ba552 and 9e0ce035. IForgeBlock#getToolModifiedState asks the
    // stack in the hand for the action, so without HOE_TILL, SHOVEL_FLATTEN and AXE_STRIP the hoe,
    // shovel and axe uses below never changed a block
    private static final List<ToolAction> ACTIONS = List.of(
        ToolActions.AXE_DIG,
        ToolActions.HOE_DIG,
        ToolActions.PICKAXE_DIG,
        ToolActions.SHOVEL_DIG,
        ToolActions.HOE_TILL,
        ToolActions.SHOVEL_FLATTEN,
        ToolActions.AXE_STRIP
    );

    private final Tier tier;

    // SkyTekx: the damage and speed that upstream 9a418c1c and 906aa08e (#1424) restore on 1.21 come
    // from DiggerItem on 1.20.1: 4 plus the attack damage bonus of the tier, and -2 attack speed, under
    // the base attack UUIDs
    public AllInOneTool(Tier tier) {
        super(
            4.0f,
            -2f,
            tier,
                ActuallyTags.Blocks.MINEABLE_WITH_AIO,
            new Properties()
                .durability(tier.getUses() * 4)
                
        );

        this.tier = tier;
    }

    @Override
    public boolean canPerformAction(@Nonnull ItemStack stack, @Nonnull ToolAction toolAction) {
        if (ACTIONS.contains(toolAction))
            return true;
        return super.canPerformAction(stack, toolAction);
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        // How, no idea, possible, most likely :cry:
        if (context.getPlayer() == null) {
            return InteractionResult.FAIL;
        }

        // Player sneaking? Act as a shovel, else strip like an axe and otherwise till like a hoe
        if (context.getPlayer().isCrouching()) {
            return Items.IRON_SHOVEL.useOn(context);
        }
        // SkyTekx: upstream 9e0ce035 with the corrected check of 6686f1f4. The axe answers CONSUME on
        // the server, so the check asks consumesAction() rather than for SUCCESS, or the server would
        // try the hoe on the block the axe just stripped
        InteractionResult tmp = Items.IRON_AXE.useOn(context);
        if (tmp.consumesAction())
            return tmp;
        return Items.IRON_HOE.useOn(context);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment) || enchantment.category.canEnchant(Items.DIAMOND_SWORD);
    }
}
