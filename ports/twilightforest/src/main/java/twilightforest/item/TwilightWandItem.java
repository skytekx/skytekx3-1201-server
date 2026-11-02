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
import twilightforest.enchantment.RechargeScepterEffect;
import twilightforest.enchantment.RenewalEnchantment;
import twilightforest.entity.projectile.LichBomb;
import twilightforest.entity.projectile.TwilightWandBolt;
import twilightforest.init.TFSounds;
import twilightforest.util.TFItemStackUtils;

import java.util.List;

public class TwilightWandItem extends Item {

	public TwilightWandItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (stack.getDamageValue() == stack.getMaxDamage() && !player.getAbilities().instabuild) {
			return InteractionResultHolder.fail(player.getItemInHand(hand));
		} else {
			player.playSound(TFSounds.TWILIGHT_SCEPTER_USE.get(), 1.0F, (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);

			if (!level.isClientSide()) {
				level.addFreshEntity(new TwilightWandBolt(level, player));
				// Mystic Crown Compat: the empowering crown adds a Lich bomb to 10% of the shots (its wearer is immune to the blast, see EntityEvents)
				if (MysticCrownItem.empowersScepters(player) && level.getRandom().nextFloat() < 0.1F) {
					LichBomb bomb = new LichBomb(level, player);
					bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
					level.addFreshEntity(bomb);
				}
				// the Mystic Crown spares the charge 5% of the time
				if (!player.getAbilities().instabuild && (!MysticCrownItem.isWornBy(player) || level.getRandom().nextFloat() > 0.05F)) {
					TFItemStackUtils.hurtButDontBreak(stack, 1, (ServerLevel) level, player);
				}
			}

			return InteractionResultHolder.success(player.getItemInHand(hand));
		}
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
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		tooltip.add(Component.translatable("item.twilightforest.scepter.desc", stack.getMaxDamage() - stack.getDamageValue()).withStyle(ChatFormatting.GRAY));
	}
}