package twilightforest.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import twilightforest.capabilities.CapabilityList;
import twilightforest.enchantment.RechargeScepterEffect;
import twilightforest.enchantment.RenewalEnchantment;
import twilightforest.util.TFItemStackUtils;

import javax.annotation.Nonnull;
import java.util.List;

public class FortificationWandItem extends Item {

	public FortificationWandItem(Properties properties) {
		super(properties);
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, @Nonnull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (stack.getDamageValue() == stack.getMaxDamage() && !player.getAbilities().instabuild) {
			return InteractionResultHolder.fail(stack);
		}

		if (!level.isClientSide()) {
			player.getCapability(CapabilityList.SHIELDS).ifPresent(cap -> {
				cap.replenishShields();
				if (!player.getAbilities().instabuild) {
					TFItemStackUtils.hurtButDontBreak(stack, 1, (ServerLevel) level, player);
				}
			});
		}

		if (!player.isCreative())
			player.getCooldowns().addCooldown(this, 1200);

		return InteractionResultHolder.success(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		RechargeScepterEffect.inventoryTick(stack, level, entity, isSelected);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	// only Renewal can be put on a scepter, with an enchanted book in an anvil
	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		return RenewalEnchantment.isRenewalBook(book);
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, level, tooltip, flags);
		tooltip.add(Component.translatable("item.twilightforest.scepter.desc", stack.getMaxDamage() - stack.getDamageValue()).withStyle(ChatFormatting.GRAY));
	}
}