package twilightforest.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFConfig;
import twilightforest.client.ISTER;
import twilightforest.compat.curios.CuriosCompat;
import twilightforest.init.TFItems;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * The Mystic Crown: a wearable copy of the Lich's crown, crafted from Crown Splinters.
 * Worn on the head it gives armor, lets temporary Fortification shields last longer and
 * sometimes spares scepter durability; the effects live at the respective call sites.
 * <p>
 * With Curios installed it also fits a Curios head slot (without the armor) and still counts as worn there, and the
 * {@code mysticCrownScepterBonuses} option adds stronger scepter effects. Both come from the Mystic Crown Compat addon
 * (MIT, by 741741), folded in here instead of its mixins.
 */
public class MysticCrownItem extends Item implements Equipable {
	// Same id as the vanilla helmet armor modifier (4.8 uses minecraft:armor.head), so it never stacks with a helmet slot modifier
	private static final UUID HEAD_ARMOR_MODIFIER_UUID = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");

	private final Multimap<Attribute, AttributeModifier> headModifiers;

	public MysticCrownItem(Properties properties) {
		super(properties);
		this.headModifiers = ImmutableMultimap.of(Attributes.ARMOR, new AttributeModifier(HEAD_ARMOR_MODIFIER_UUID, "Armor modifier", 2.0D, AttributeModifier.Operation.ADDITION));
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.HEAD;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		return this.swapWithEquipmentSlot(this, level, player, hand);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		return slot == EquipmentSlot.HEAD ? this.headModifiers : super.getDefaultAttributeModifiers(slot);
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return ModList.get().isLoaded("curios") ? CuriosCompat.setupMysticCrownCapability(stack) : super.initCapabilities(stack, nbt);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(ISTER.CLIENT_ITEM_EXTENSION);
	}

	/**
	 * Whether the entity wears a Mystic Crown, either in its head slot or, with Curios installed and the
	 * {@code mysticCrownCurios} option on, in a Curios slot. Every crown effect checks this.
	 */
	public static boolean isWornBy(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).is(TFItems.MYSTIC_CROWN.get())
				|| (TFConfig.mysticCrownCurios && ModList.get().isLoaded("curios") && CuriosCompat.isMysticCrownEquipped(entity));
	}

	/**
	 * Whether the entity wears a Mystic Crown and the {@code mysticCrownScepterBonuses} option is on, which
	 * turns on the stronger scepter effects of Mystic Crown Compat 1.1.0.
	 */
	public static boolean empowersScepters(LivingEntity entity) {
		return TFConfig.mysticCrownScepterBonuses && isWornBy(entity);
	}
}
