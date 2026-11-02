package twilightforest.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Concentrated Essence Berry: using it pops it into an experience orb worth 6-19 XP.
 */
public class EssenceBerryItem extends Item {
	public EssenceBerryItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);
		// 1.21's ItemStack#consume(1, player): creative players keep the berry
		if (!player.getAbilities().instabuild)
			stack.shrink(1);
		if (!level.isClientSide()) {
			ExperienceOrb orb = new ExperienceOrb(level, player.getX(), player.getY() + 1, player.getZ(), 6 + level.getRandom().nextInt(14));
			level.addFreshEntity(orb);
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
