package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.inventory.ItemTagContainer;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

// SkyTekx: the Item Tag of upstream 6f567725 and 79b2084e. It holds one item tag, and every filter
// slot of the mod matches the items in that tag (FilterSettings.check). The tag id sits in the
// stack's NBT key tag as a string, the 1.20.1 form of 1.20.4's ItemTagAttachment, which saved a
// compound with that key, and of main's data component ITEM_TAG. It is set by renaming the item in
// an anvil (CommonEvents.onAnvilEvent) or in its own screen, which main has but never opens
public class ItemTag extends ItemBase {
    public static final String TAG_KEY = "tag";
    public static final int MAX_TAG_LENGTH = 256;

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, inventory, entity) -> new ItemTagContainer(id, inventory), Component.translatable("container.actuallyadditions.item_tag")));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        Optional<ResourceLocation> tag = getTagLocation(stack);
        if (tag.isPresent()) {
            tooltip.add(Component.literal("Tag: " + tag.get()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("tooltip.actuallyadditions.item_tag.no_tag").withStyle(ChatFormatting.RED));
        }

        super.appendHoverText(stack, level, tooltip, flag);
    }

    /**
     * The tag id in the stack's NBT, if it holds a valid one.
     */
    public static Optional<ResourceLocation> getTagLocation(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains(TAG_KEY, Tag.TAG_STRING)) {
            return Optional.empty();
        }
        return Optional.ofNullable(ResourceLocation.tryParse(nbt.getString(TAG_KEY)));
    }

    public static Optional<TagKey<Item>> getTag(ItemStack stack) {
        return getTagLocation(stack).map(location -> TagKey.create(Registries.ITEM, location));
    }

    public static void setTag(ItemStack stack, ResourceLocation tag) {
        stack.getOrCreateTag().putString(TAG_KEY, tag.toString());
    }

    public static void clearTag(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt != null) {
            nbt.remove(TAG_KEY);
            if (nbt.isEmpty()) {
                stack.setTag(null);
            }
        }
    }
}
