package twilightforest.asmhooks;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;

/**
 * Hooks called from {@code META-INF/asm/exhaustion.js} (4.8's tf-asm ReduceMovementFoodExhaustionTransformer). They
 * only take common classes, so loading this class never pulls client code onto a dedicated server.
 */
@SuppressWarnings("unused")
public final class PlayerHooks {

	/**
	 * The player whose {@code jumpFromGround} is running on this thread, so its {@code causeFoodExhaustion} knows the
	 * exhaustion is the jump's.
	 */
	private static final ThreadLocal<Player> JUMPING = new ThreadLocal<>();

	private PlayerHooks() {
	}

	/**
	 * Efficient Eater: the movement and jump exhaustion of a player in a Traveller's Vest with the modifier is divided
	 * by the modifier's value (halved with the stock definition).
	 * <p>
	 * Injection Point (before every {@code causeFoodExhaustion} call):<br/>
	 * {@code net.minecraft.world.entity.player.Player#checkMovementStatistics(double, double, double)} (in
	 * ServerPlayer in 1.21)
	 * <p>
	 * The jump exhaustion goes through {@link #getJumpFoodExhaustion} instead.
	 */
	public static float getFoodExhaustion(float f, Player player) {
		ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
		// 4.8 reads the component first; a stack NBT component is decoded on every read, so the cheap check goes first
		if (!TravellersArmorItem.isTravellersGear(chestStack))
			return f;
		Float divisor = TravellersGearData.EFFICIENT_EATER.get(chestStack);
		if (!TravellersModifiersManager.isModifierActive(player, chestStack, TravellersModifiersManager.EFFICIENT_EATER_MODIFIER) || divisor == null)
			return f;
		return f * (1 / divisor);
	}

	/**
	 * Marks {@code player} as jumping until {@link #endJump}.
	 * <p>
	 * Injection Point (at the head):<br/>
	 * {@code net.minecraft.world.entity.player.Player#jumpFromGround()}
	 */
	public static void beginJump(Player player) {
		JUMPING.set(player);
	}

	/**
	 * Injection Point (before every return):<br/>
	 * {@code net.minecraft.world.entity.player.Player#jumpFromGround()}
	 */
	public static void endJump() {
		JUMPING.remove();
	}

	/**
	 * Efficient Eater for the jump exhaustion: the first exhaustion a jumping player causes is reduced like
	 * {@link #getFoodExhaustion}. It runs inside {@code causeFoodExhaustion}, after Arclight has replaced the argument
	 * of the call in {@code jumpFromGround} with the Spigot world config's jump exhaustion.
	 * <p>
	 * Injection Point (at the head, replacing the argument):<br/>
	 * {@code net.minecraft.world.entity.player.Player#causeFoodExhaustion(float)}
	 */
	public static float getJumpFoodExhaustion(float f, Player player) {
		if (JUMPING.get() != player)
			return f;
		JUMPING.remove();
		return getFoodExhaustion(f, player);
	}
}
