package baguchi.twilight_crossbow.init;

import baguchi.twilight_crossbow.TwilightCrossBow;
import baguchi.twilight_crossbow.item.EnderCrossbowItem;
import baguchi.twilight_crossbow.item.IceCrossBowItem;
import baguchi.twilight_crossbow.item.SeekerCrossBowItem;
import baguchi.twilight_crossbow.item.TripleCrossBowItem;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModItems {
    public static final DeferredRegister<Item> ITEM_REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, TwilightCrossBow.MODID);

    // 1.20.1 crossbows keep their loaded projectiles in the "Charged"/"ChargedProjectiles" NBT, so the 1.21
    // CHARGED_PROJECTILES default component is not needed. durability() also caps the stack size at 1.
    public static final RegistryObject<Item> TRIPLE_CROSSBOW = ITEM_REGISTRY.register("triple_crossbow", () -> new TripleCrossBowItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(412)));
    public static final RegistryObject<Item> ICE_CROSSBOW = ITEM_REGISTRY.register("ice_crossbow", () -> new IceCrossBowItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(412)));
    public static final RegistryObject<Item> SEEKER_CROSSBOW = ITEM_REGISTRY.register("seeker_crossbow", () -> new SeekerCrossBowItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(412)));
    public static final RegistryObject<Item> ENDER_CROSSBOW = ITEM_REGISTRY.register("ender_crossbow", () -> new EnderCrossbowItem(new Item.Properties().rarity(Rarity.UNCOMMON).durability(412)));
    // Upstream keeps a Knight Crossbow commented out (KnightCrossBowItem + a getChargeDuration mixin); it is not ported.


    private static final ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
    private static final ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;

    private static final Component CROSSBOW_UPGRADE_APPLIES_TO = Component.translatable(
                    Util.makeDescriptionId("item", TwilightCrossBow.prefix("smithing_template.crossbow_upgrade.applies_to"))
            )
            .withStyle(DESCRIPTION_FORMAT);
    private static final Component CROSSBOW_UPGRADE_INGREDIENTS = Component.translatable(
                    Util.makeDescriptionId("item", TwilightCrossBow.prefix("smithing_template.crossbow_upgrade.ingredients"))
            )
            .withStyle(DESCRIPTION_FORMAT);
    private static final Component CROSSBOW_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(
            Util.makeDescriptionId("item", TwilightCrossBow.prefix("smithing_template.crossbow_upgrade.base_slot_description"))
    );
    private static final Component CROSSBOW_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(
            Util.makeDescriptionId("item", TwilightCrossBow.prefix("smithing_template.crossbow_upgrade.additions_slot_description"))
    );
    private static final Component CROSSBOW_UPGRADE = Component.translatable(
                    Util.makeDescriptionId("upgrade", TwilightCrossBow.prefix("crossbow_upgrade"))
            )
            .withStyle(TITLE_FORMAT);

    private static final ResourceLocation EMPTY_SLOT_BOW = TwilightCrossBow.prefix("item/empty_slot_bow");

    public static final RegistryObject<Item> CROSSBOW_UPGRADE_SMITHING_TEMPLATE = ITEM_REGISTRY.register("crossbow_upgrade_smithing_template", ModItems::createCrossbowUpgradeTemplate);


    public static SmithingTemplateItem createCrossbowUpgradeTemplate() {
        return new SmithingTemplateItem(
                CROSSBOW_UPGRADE_APPLIES_TO,
                CROSSBOW_UPGRADE_INGREDIENTS,
                CROSSBOW_UPGRADE,
                CROSSBOW_UPGRADE_BASE_SLOT_DESCRIPTION,
                CROSSBOW_UPGRADE_ADDITIONS_SLOT_DESCRIPTION,
                createCrossbowUpgradeIconList(),
                createEmptyMaterialList()
        );
    }

    private static List<ResourceLocation> createCrossbowUpgradeIconList() {
        return List.of(
                EMPTY_SLOT_BOW
        );
    }

    private static List<ResourceLocation> createEmptyMaterialList() {
        return List.of();
    }

}
