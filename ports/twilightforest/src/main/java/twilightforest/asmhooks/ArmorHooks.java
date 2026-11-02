package twilightforest.asmhooks;

import net.minecraft.world.item.ItemStack;
import twilightforest.util.ArmorUtil;

/**
 * Hooks called from {@code META-INF/asm/armor.js}. They only take common classes so that loading this class can
 * never pull client code onto a dedicated server; the injection points themselves are client-only renderers.
 * <p>
 * The 4.8 mob-detection hook (ArmorHooks.modifyArmorVisibility on LivingEntity#getVisibilityPercent) is not a
 * coremod here: Forge 47 fires LivingEvent.LivingVisibilityEvent from that method, see
 * {@link twilightforest.events.MiscEvents#hideShroudedArmor}.
 */
@SuppressWarnings("unused")
public final class ArmorHooks {

	private ArmorHooks() {
	}

	/**
	 * Injection Points:<br/>
	 * {@code net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer#renderArmorPiece} (after the
	 * {@code instanceof ArmorItem} check)<br/>
	 * {@code net.minecraft.client.renderer.entity.layers.ElytraLayer#shouldRender} (Forge-added, every return)
	 */
	public static boolean cancelArmorRendering(boolean o, ItemStack stack) {
		if (o && ArmorUtil.hasEmperorsCloth(stack)) {
			return false;
		}
		return o;
	}

	/**
	 * Injection Point:<br/>
	 * {@code net.minecraft.client.renderer.entity.layers.CapeLayer#render} (after {@code chest.is(Items.ELYTRA)}).
	 * A shrouded elytra is not drawn, so the cape it would normally replace has to render again.
	 */
	public static boolean fixCapeRendering(boolean o, ItemStack stack) {
		return o && !ArmorUtil.hasEmperorsCloth(stack);
	}
}
