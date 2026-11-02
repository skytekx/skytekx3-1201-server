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

	private PlayerHooks() {
	}

	/**
	 * Efficient Eater: the movement and jump exhaustion of a player in a Traveller's Vest with the modifier is divided
	 * by the modifier's value (halved with the stock definition).
	 * <p>
	 * Injection Points (before every {@code causeFoodExhaustion} call):<br/>
	 * {@code net.minecraft.world.entity.player.Player#checkMovementStatistics(double, double, double)} (in
	 * ServerPlayer in 1.21)<br/>
	 * {@code net.minecraft.world.entity.player.Player#jumpFromGround()}
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
}
