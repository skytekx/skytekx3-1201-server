package twilightforest.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFSounds;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class BrittleFlaskItem extends Item {

	public static final int DOSES = 3;

	public BrittleFlaskItem(Properties properties) {
		super(properties);
	}

	//empty flasks stack, filled ones do not
	@Override
	public int getMaxStackSize(ItemStack stack) {
		return PotionFlaskContents.of(stack).hasPotion() ? 1 : super.getMaxStackSize(stack);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return PotionFlaskContents.of(stack).hasPotion();
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return PotionUtils.getColor(stack);
	}

	//right-clicking a potion onto a flask in a slot
	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
		return action == ClickAction.SECONDARY && slot.allowModification(player) && this.tryFill(stack, other, player);
	}

	//right-clicking a held flask onto a potion in a slot (#2531)
	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		return action == ClickAction.SECONDARY && slot.allowModification(player) && this.tryFill(stack, slot.getItem(), player);
	}

	private boolean tryFill(ItemStack flask, ItemStack potionStack, Player player) {
		//only drinkable potions with an actual potion in them; splash and lingering potions or tipped arrows would hand out a free glass bottle
		if (!potionStack.is(Items.POTION) || PotionUtils.getPotion(potionStack) == Potions.EMPTY) return false;

		PotionFlaskContents flaskContents = PotionFlaskContents.of(flask);
		if ((!flaskContents.hasPotion() || flaskContents.holdsSameAs(potionStack)) && flaskContents.doses() < DOSES - flaskContents.breakage()) {
			PotionFlaskContents filled = flaskContents.tryAddDose(potionStack);
			if (!player.getAbilities().instabuild) {
				potionStack.shrink(1);
				if (!player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
					player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
				}
			}

			this.changeAndConsumeFlask(flask, player, filled::applyTo);
			player.playSound(TFSounds.FLASK_FILL.get(), (flaskContents.doses() + 1) * 0.25F, player.level().getRandom().nextFloat() * 0.1F + 0.9F);
			return true;
		}
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		PotionFlaskContents flaskContents = PotionFlaskContents.of(stack);

		if (flaskContents.hasPotion() && flaskContents.doses() > 0) {
			return ItemUtils.startUsingInstantly(level, player, hand);
		}

		return InteractionResultHolder.fail(player.getItemInHand(hand));
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		PotionFlaskContents flaskContents = PotionFlaskContents.of(stack);
		if (flaskContents.hasPotion()) {
			if (entity instanceof Player player) {
				if (!level.isClientSide()) {
					for (MobEffectInstance mobeffectinstance : PotionUtils.getMobEffects(stack)) {
						if (isHarmful(mobeffectinstance.getEffect(), entity) && mobeffectinstance.getAmplifier() > 0) {
							//custom harming death message for the advancement
							entity.hurt(TFDamageTypes.getDamageSource(level, TFDamageTypes.FAILED_CHALLENGE), (float) (6 << mobeffectinstance.getAmplifier()));
						} else if (mobeffectinstance.getEffect().isInstantenous()) {
							mobeffectinstance.getEffect().applyInstantenousEffect(player, player, player, mobeffectinstance.getAmplifier(), 1.0D);
						} else {
							player.addEffect(new MobEffectInstance(mobeffectinstance));
						}
					}
					//track after the effects are applied, so a dose that kills the player does not count towards the challenge
					if (!player.isCreative() && !player.isSpectator() && player instanceof ServerPlayer serverPlayer) {
						PotionFlaskTracking.trackDrink(serverPlayer, flaskContents.potion());
					}
				}
				player.awardStat(Stats.ITEM_USED.get(this));
				if (!player.getAbilities().instabuild) {
					this.changeAndConsumeFlask(stack, player, flask -> {
						PotionFlaskContents drunk = flaskContents.removeDose(this.canBreak());
						drunk.applyTo(flask);
						if (this.canBreak()) {
							if (drunk.breakage() >= DOSES) {
								flask.shrink(1);
								level.playSound(null, player, TFSounds.BRITTLE_FLASK_BREAK.get(), player.getSoundSource(), 1.5F, 0.7F);
							} else {
								level.playSound(null, player, TFSounds.BRITTLE_FLASK_CRACK.get(), player.getSoundSource(), 1.5F, 2.0F);
							}
						}
					});
				}
			}
		}
		return super.finishUsingItem(stack, level, entity);
	}

	/**
	 * Whether the effect hurts the drinker the way Instant Damage does: Harming for the living, Healing for the undead.
	 */
	private static boolean isHarmful(MobEffect effect, LivingEntity entity) {
		return entity.isInvertedHealAndHarm() ? effect == MobEffects.HEAL : effect == MobEffects.HARM;
	}

	private void changeAndConsumeFlask(ItemStack stack, Player player, Consumer<ItemStack> onDrink) {
		//if we have a stack of flasks, separate one when filling/emptying
		if (stack.getCount() > 1) {
			ItemStack copy = stack.copyWithCount(1);
			stack.shrink(1);

			onDrink.accept(copy);
			if (!player.getInventory().add(copy)) {
				player.drop(copy, false);
			}
		} else {
			//otherwise just use the existing stack. Having it jump around the inventory is weird
			onDrink.accept(stack);
		}
	}

	public boolean canBreak() {
		return true;
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		PotionFlaskContents contents = PotionFlaskContents.of(stack);
		return Optional.of(new Tooltip(contents.potion(), PotionUtils.getMobEffects(stack), PotionUtils.getColor(stack), contents.doses(), contents.breakage(), DOSES));
	}

	//copied from Item.getBarWidth, but reversed the "durability" check so it increments up, not down
	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F - Math.abs(Math.min(PotionFlaskContents.dosesOf(stack), DOSES) - DOSES) * 13.0F / DOSES);
	}

	/**
	 * The data the client-side flask tooltip draws: the potion's name and effects, then a bar of {@code maxDoses}
	 * segments with {@code doses} filled in the potion's colour and {@code breakage} greyed out.
	 */
	public record Tooltip(Potion potion, List<MobEffectInstance> effects, int color, int doses, int breakage, int maxDoses) implements TooltipComponent {
	}
}
