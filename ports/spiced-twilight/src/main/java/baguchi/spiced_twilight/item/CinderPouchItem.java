package baguchi.spiced_twilight.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFSounds;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A bundle that only takes Fire Beetle Powder and burns one powder per second to breathe fire.
 * <p>
 * 1.20.1 backport: upstream stores the powder in the 1.21 {@code minecraft:bundle_contents} data component. Here it is
 * kept in the stack's NBT the way the 1.20.1 vanilla bundle does it ({@code Items} list, 64 weight units, one per
 * powder), so the vanilla bundle tooltip still renders it.
 */
public class CinderPouchItem extends Item {
    private static final String TAG_ITEMS = "Items";
    public static final int MAX_WEIGHT = 64;
    private static final int BAR_COLOR = Mth.color(1F, 0.5F, 0F);

    public CinderPouchItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (stack.getCount() != 1 || action != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack itemstack = slot.getItem();
            if (itemstack.isEmpty()) {
                this.playRemoveOneSound(player);
                removeOne(stack).ifPresent(removed -> add(stack, slot.safeInsert(removed)));
            } else if (itemstack.is(ModItems.FIRE_BEETLE_POWDER.get())) {
                int space = (MAX_WEIGHT - getContentWeight(stack)) / getWeight(itemstack);
                int inserted = add(stack, slot.safeTake(itemstack.getCount(), space, player));
                if (inserted > 0) {
                    this.playInsertSound(player);
                }
            }

            return true;
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);

        if (getContentWeight(itemstack) > 0) {
            player.startUsingItem(usedHand);

            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        return oldStack.getItem() == newStack.getItem();
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || newStack.getItem() != oldStack.getItem();
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (remainingUseDuration % 20 == 0) {
            if (getContentWeight(stack) > 0) {
                consumeOne(stack);
            } else {
                livingEntity.stopUsingItem();
            }
        }

        if (!level.isClientSide() && remainingUseDuration > 10) {
            Entity target = this.getHeadLookTarget(livingEntity);
            if (target != null && target != livingEntity && livingEntity.attackable() && !target.fireImmune() && target.hurt(TFDamageTypes.getEntityDamageSource(level, TFDamageTypes.SCORCHED, livingEntity), 2.0F)) {
                target.setSecondsOnFire(10);
            }
        } else {
            Vec3 look = livingEntity.getLookAngle();
            double dist = 0.9 * 3;
            double px = livingEntity.getX() + look.x() * dist;
            double py = livingEntity.getY() + (double) livingEntity.getEyeHeight() + look.y() * dist;
            double pz = livingEntity.getZ() + look.z() * dist;

            for (int i = 0; i < 2; ++i) {
                double dx = look.x();
                double dy = look.y();
                double dz = look.z();
                double spread = (double) 5.0F + livingEntity.getRandom().nextDouble() * (double) 2.5F;
                double velocity = 0.15 + livingEntity.getRandom().nextDouble() * 0.15;
                dx += livingEntity.getRandom().nextGaussian() * 0.0075 * spread;
                dy += livingEntity.getRandom().nextGaussian() * 0.0075 * spread;
                dz += livingEntity.getRandom().nextGaussian() * 0.0075 * spread;
                dx *= velocity;
                dy *= velocity;
                dz *= velocity;
                level.addParticle(ParticleTypes.FLAME, px, py, pz, dx, dy, dz);
            }
        }
        livingEntity.playSound(TFSounds.FIRE_BEETLE_SHOOT.get(), livingEntity.getRandom().nextFloat() * 0.5F, livingEntity.getRandom().nextFloat() * 0.5F);
        livingEntity.gameEvent(GameEvent.PROJECTILE_SHOOT);
    }

    private @Nullable Entity getHeadLookTarget(LivingEntity living) {
        Entity pointedEntity = null;
        double range = 30.0F;
        double offset = 0.0F;
        Vec3 srcVec = new Vec3(living.getX(), living.getY() + (double) living.getEyeHeight(), living.getZ());
        Vec3 lookVec = living.getViewVector(1.0F);
        Vec3 destVec = srcVec.add(lookVec.x() * range, lookVec.y() * range, lookVec.z() * range);
        float var9 = 6.5F;
        List<Entity> possibleList = living.level().getEntities(living, living.getBoundingBox().move(lookVec.x() * offset, lookVec.y() * offset, lookVec.z() * offset).inflate(var9, var9, var9));
        double hitDist = 0.0F;
        if (living.isMultipartEntity()) {
            possibleList.removeAll(Arrays.asList((PartEntity<?>[]) Objects.requireNonNull(living.getParts())));
        }

        for (Entity possibleEntity : possibleList) {
            if (possibleEntity.isPickable() && possibleEntity != living && EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(EntitySelector.LIVING_ENTITY_STILL_ALIVE).test(possibleEntity)) {
                float borderSize = possibleEntity.getPickRadius();
                AABB collisionBB = possibleEntity.getBoundingBox().inflate(borderSize, borderSize, borderSize);
                Optional<Vec3> interceptPos = collisionBB.clip(srcVec, destVec);
                if (collisionBB.contains(srcVec)) {
                    if ((double) 0.0F < hitDist || hitDist == (double) 0.0F) {
                        pointedEntity = possibleEntity;
                        hitDist = 0.0F;
                    }
                } else if (interceptPos.isPresent()) {
                    double possibleDist = srcVec.distanceTo(interceptPos.get());
                    if (possibleDist < hitDist || hitDist == (double) 0.0F) {
                        pointedEntity = possibleEntity;
                        hitDist = possibleDist;
                    }
                }
            }
        }

        return pointedEntity;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getContentWeight(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.min(1 + 12 * getContentWeight(stack) / MAX_WEIGHT, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        NonNullList<ItemStack> list = NonNullList.create();
        getContents(stack).forEach(list::add);
        return Optional.of(new BundleTooltip(list, getContentWeight(stack)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.minecraft.bundle.fullness", getContentWeight(stack), MAX_WEIGHT).withStyle(ChatFormatting.GRAY));
        LoreItem.appendLore(tooltipComponents, "item.spiced_twilight.cinder_pouch.desc");
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        List<ItemStack> contents = getContents(stack).toList();
        if (!contents.isEmpty()) {
            stack.removeTagKey(TAG_ITEMS);
            ItemUtils.onContainerDestroyed(itemEntity, contents.stream());
        }
    }

    // ---- 1.20.1 NBT bundle storage (vanilla BundleItem layout) ----

    private static int getWeight(ItemStack stack) {
        return MAX_WEIGHT / stack.getMaxStackSize();
    }

    public static int getContentWeight(ItemStack pouch) {
        return getContents(pouch).mapToInt(stack -> getWeight(stack) * stack.getCount()).sum();
    }

    public static Stream<ItemStack> getContents(ItemStack pouch) {
        CompoundTag tag = pouch.getTag();
        if (tag == null) {
            return Stream.empty();
        }
        ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        return list.stream().map(CompoundTag.class::cast).map(ItemStack::of).filter(stack -> !stack.isEmpty());
    }

    /**
     * @return how many items of {@code inserted} went into the pouch
     */
    private static int add(ItemStack pouch, ItemStack inserted) {
        if (inserted.isEmpty() || !inserted.is(ModItems.FIRE_BEETLE_POWDER.get())) {
            return 0;
        }
        CompoundTag tag = pouch.getOrCreateTag();
        if (!tag.contains(TAG_ITEMS)) {
            tag.put(TAG_ITEMS, new ListTag());
        }

        int amount = Math.min(inserted.getCount(), (MAX_WEIGHT - getContentWeight(pouch)) / getWeight(inserted));
        if (amount <= 0) {
            return 0;
        }
        ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        Optional<CompoundTag> match = list.stream()
                .map(CompoundTag.class::cast)
                .filter(entry -> ItemStack.isSameItemSameTags(ItemStack.of(entry), inserted))
                .findFirst();
        if (match.isPresent()) {
            CompoundTag entry = match.get();
            ItemStack merged = ItemStack.of(entry);
            merged.grow(amount);
            merged.save(entry);
            list.remove(entry);
            list.add(0, entry);
        } else {
            CompoundTag entry = new CompoundTag();
            inserted.copyWithCount(amount).save(entry);
            list.add(0, entry);
        }
        return amount;
    }

    /**
     * Takes out the first stack (bundle click with an empty slot).
     */
    private static Optional<ItemStack> removeOne(ItemStack pouch) {
        CompoundTag tag = pouch.getTag();
        if (tag == null || !tag.contains(TAG_ITEMS)) {
            return Optional.empty();
        }
        ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) {
            return Optional.empty();
        }
        ItemStack removed = ItemStack.of(list.getCompound(0));
        list.remove(0);
        if (list.isEmpty()) {
            pouch.removeTagKey(TAG_ITEMS);
        }
        return Optional.of(removed);
    }

    /**
     * Burns a single powder (upstream: removeOneItem).
     */
    private static void consumeOne(ItemStack pouch) {
        CompoundTag tag = pouch.getTag();
        if (tag == null || !tag.contains(TAG_ITEMS)) {
            return;
        }
        ListTag list = tag.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) {
            return;
        }
        CompoundTag entry = list.getCompound(0);
        ItemStack first = ItemStack.of(entry);
        if (first.getCount() > 1) {
            first.shrink(1);
            first.save(entry);
        } else {
            list.remove(0);
            if (list.isEmpty()) {
                pouch.removeTagKey(TAG_ITEMS);
            }
        }
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
