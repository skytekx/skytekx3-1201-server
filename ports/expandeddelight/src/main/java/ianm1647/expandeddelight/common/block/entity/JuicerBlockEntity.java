package ianm1647.expandeddelight.common.block.entity;

import com.google.common.collect.Lists;
import ianm1647.expandeddelight.common.block.JuicerBlock;
import ianm1647.expandeddelight.common.block.entity.container.JuicerMenu;
import ianm1647.expandeddelight.common.block.entity.inventory.JuicerItemHandler;
import ianm1647.expandeddelight.common.crafting.JuicerRecipe;
import ianm1647.expandeddelight.common.registry.EDBlockEntityTypes;
import ianm1647.expandeddelight.common.registry.EDItems;
import ianm1647.expandeddelight.common.registry.EDRecipeTypes;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * The juicer. Upstream's 1.21.1 class follows Farmer's Delight's CookingPotBlockEntity (MIT), and this
 * 1.20.1 form follows the Forge 1.20.1 cooking pot of Farmer's Delight 1.3.4: item handler capability
 * through getCapability, NBT instead of data components, and the drink of a juicer item kept in its
 * BlockEntityTag.
 */
public class JuicerBlockEntity extends SyncedBlockEntity implements MenuProvider, Nameable, RecipeHolder {
   public static final int MEAL_DISPLAY_SLOT = 2;
   public static final int CONTAINER_SLOT = 3;
   public static final int OUTPUT_SLOT = 4;
   public static final int INVENTORY_SIZE = 5;
   private final ItemStackHandler inventory = this.createHandler();
   private final LazyOptional<IItemHandler> inputHandler;
   private final LazyOptional<IItemHandler> outputHandler;
   public int juiceTime;
   private int juiceTimeTotal;
   private ItemStack containerStack;
   private Component customName;
   protected final ContainerData juicerData;
   private final Object2IntOpenHashMap<ResourceLocation> usedRecipeTracker;
   private final RecipeManager.CachedCheck<RecipeWrapper, JuicerRecipe> quickCheck;

   public JuicerBlockEntity(BlockPos pos, BlockState state) {
      super(EDBlockEntityTypes.JUICER.get(), pos, state);
      this.inputHandler = LazyOptional.of(() -> new JuicerItemHandler(this.inventory, Direction.UP));
      this.outputHandler = LazyOptional.of(() -> new JuicerItemHandler(this.inventory, Direction.DOWN));
      this.containerStack = ItemStack.EMPTY;
      this.juicerData = this.createIntArray();
      this.usedRecipeTracker = new Object2IntOpenHashMap<>();
      this.quickCheck = RecipeManager.createCheck(EDRecipeTypes.JUICING.get());
   }

   // the top takes ingredients, every other side (and no side) gives the output, as upstream's capability
   @Override
   @Nonnull
   public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
      if (cap == ForgeCapabilities.ITEM_HANDLER) {
         return side == Direction.UP ? this.inputHandler.cast() : this.outputHandler.cast();
      }
      return super.getCapability(cap, side);
   }

   @Override
   public void invalidateCaps() {
      super.invalidateCaps();
      this.inputHandler.invalidate();
      this.outputHandler.invalidate();
   }

   @Nullable
   private static CompoundTag getInventoryTag(ItemStack juicerStack) {
      if (!juicerStack.is(EDItems.JUICER.get())) {
         return null;
      }
      CompoundTag compound = juicerStack.getTagElement("BlockEntityTag");
      if (compound == null) {
         return null;
      }
      CompoundTag inventoryTag = compound.getCompound("Inventory");
      return inventoryTag.contains("Items", 9) ? inventoryTag : null;
   }

   public static ItemStack getDrinkFromItem(ItemStack juicerStack) {
      CompoundTag inventoryTag = getInventoryTag(juicerStack);
      if (inventoryTag == null) {
         return ItemStack.EMPTY;
      }
      ItemStackHandler handler = new ItemStackHandler();
      handler.deserializeNBT(inventoryTag);
      return handler.getSlots() > MEAL_DISPLAY_SLOT ? handler.getStackInSlot(MEAL_DISPLAY_SLOT) : ItemStack.EMPTY;
   }

   public static void takeServingFromItem(ItemStack juicerStack) {
      CompoundTag inventoryTag = getInventoryTag(juicerStack);
      if (inventoryTag == null) {
         return;
      }
      ItemStackHandler handler = new ItemStackHandler();
      handler.deserializeNBT(inventoryTag);
      if (handler.getSlots() > MEAL_DISPLAY_SLOT) {
         handler.getStackInSlot(MEAL_DISPLAY_SLOT).shrink(1);
         CompoundTag compound = juicerStack.getOrCreateTagElement("BlockEntityTag");
         compound.put("Inventory", handler.serializeNBT());
      }
   }

   public static ItemStack getContainerFromItem(ItemStack juicerStack) {
      if (!juicerStack.is(EDItems.JUICER.get())) {
         return ItemStack.EMPTY;
      }
      CompoundTag compound = juicerStack.getTagElement("BlockEntityTag");
      return compound != null ? ItemStack.of(compound.getCompound("Container")) : ItemStack.EMPTY;
   }

   @Override
   public void load(CompoundTag compound) {
      super.load(compound);
      this.inventory.deserializeNBT(compound.getCompound("Inventory"));
      this.juiceTime = compound.getInt("JuiceTime");
      this.juiceTimeTotal = compound.getInt("JuiceTimeTotal");
      this.containerStack = ItemStack.of(compound.getCompound("Container"));
      if (compound.contains("CustomName", 8)) {
         this.customName = Component.Serializer.fromJson(compound.getString("CustomName"));
      }

      CompoundTag compoundRecipes = compound.getCompound("RecipesUsed");
      for (String key : compoundRecipes.getAllKeys()) {
         ResourceLocation recipeId = ResourceLocation.tryParse(key);
         if (recipeId != null) {
            this.usedRecipeTracker.put(recipeId, compoundRecipes.getInt(key));
         }
      }

   }

   @Override
   public void saveAdditional(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.putInt("JuiceTime", this.juiceTime);
      // upstream writes juiceTime here as well
      compound.putInt("JuiceTimeTotal", this.juiceTimeTotal);
      compound.put("Container", this.containerStack.save(new CompoundTag()));
      if (this.customName != null) {
         compound.putString("CustomName", Component.Serializer.toJson(this.customName));
      }

      compound.put("Inventory", this.inventory.serializeNBT());
      CompoundTag compoundRecipes = new CompoundTag();
      this.usedRecipeTracker.forEach((recipeId, craftedAmount) -> {
         compoundRecipes.putInt(recipeId.toString(), craftedAmount);
      });
      compound.put("RecipesUsed", compoundRecipes);
   }

   private CompoundTag writeItems(CompoundTag compound) {
      super.saveAdditional(compound);
      compound.put("Container", this.containerStack.save(new CompoundTag()));
      compound.put("Inventory", this.inventory.serializeNBT());
      return compound;
   }

   public CompoundTag writeDrink(CompoundTag compound) {
      if (this.getDrink().isEmpty()) {
         return compound;
      } else {
         ItemStackHandler drops = new ItemStackHandler(INVENTORY_SIZE);

         for(int i = 0; i < INVENTORY_SIZE; ++i) {
            drops.setStackInSlot(i, i == MEAL_DISPLAY_SLOT ? this.inventory.getStackInSlot(i) : ItemStack.EMPTY);
         }

         if (this.customName != null) {
            compound.putString("CustomName", Component.Serializer.toJson(this.customName));
         }

         compound.put("Container", this.containerStack.save(new CompoundTag()));
         compound.put("Inventory", drops.serializeNBT());
         return compound;
      }
   }

   public static void juicingTick(Level level, BlockPos pos, BlockState state, JuicerBlockEntity juicer) {
      boolean didInventoryChange = false;
      if (juicer.hasInput()) {
         Optional<JuicerRecipe> recipe = juicer.getMatchingRecipe(new RecipeWrapper(juicer.inventory));
         if (recipe.isPresent() && juicer.canJuice(recipe.get())) {
            didInventoryChange = juicer.processJuicing(recipe.get(), juicer);
         } else {
            juicer.juiceTime = 0;
         }
      } else if (juicer.juiceTime > 0) {
         juicer.juiceTime = Mth.clamp(juicer.juiceTime - 2, 0, juicer.juiceTimeTotal);
      }

      ItemStack drinkStack = juicer.getDrink();
      if (!drinkStack.isEmpty()) {
         if (!juicer.doesDrinkHaveContainer(drinkStack)) {
            juicer.moveDrinkToOutput();
            didInventoryChange = true;
         } else if (!juicer.inventory.getStackInSlot(CONTAINER_SLOT).isEmpty()) {
            juicer.useStoredContainersOnDrink();
            didInventoryChange = true;
         }
      }

      if (didInventoryChange) {
         juicer.inventoryChanged();
      }

   }

   private Optional<JuicerRecipe> getMatchingRecipe(RecipeWrapper inventoryWrapper) {
      if (this.level == null) {
         return Optional.empty();
      } else {
         return this.hasInput() ? this.quickCheck.getRecipeFor(inventoryWrapper, this.level) : Optional.empty();
      }
   }

   public ItemStack getContainer() {
      ItemStack mealStack = this.getDrink();
      return !mealStack.isEmpty() && !this.containerStack.isEmpty() ? this.containerStack : mealStack.getCraftingRemainingItem();
   }

   private boolean hasInput() {
      for(int i = 0; i < JuicerRecipe.INPUT_SLOTS; ++i) {
         if (!this.inventory.getStackInSlot(i).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   protected boolean canJuice(JuicerRecipe recipe) {
      if (this.hasInput() && this.level != null) {
         ItemStack resultStack = recipe.assemble(new RecipeWrapper(this.inventory), this.level.registryAccess());
         if (resultStack.isEmpty()) {
            return false;
         } else {
            ItemStack storedMealStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
            if (storedMealStack.isEmpty()) {
               return true;
            } else if (!ItemStack.isSameItem(storedMealStack, resultStack)) {
               return false;
            } else if (storedMealStack.getCount() + resultStack.getCount() <= this.inventory.getSlotLimit(MEAL_DISPLAY_SLOT)) {
               return true;
            } else {
               return storedMealStack.getCount() + resultStack.getCount() <= resultStack.getMaxStackSize();
            }
         }
      } else {
         return false;
      }
   }

   private boolean processJuicing(JuicerRecipe recipe, JuicerBlockEntity juicer) {
      if (this.level == null) {
         return false;
      } else {
         ++this.juiceTime;
         this.juiceTimeTotal = recipe.getJuiceTime();
         if (this.juiceTime < this.juiceTimeTotal) {
            return false;
         } else {
            this.juiceTime = 0;
            this.containerStack = recipe.getOutputContainer();
            ItemStack resultStack = recipe.assemble(new RecipeWrapper(this.inventory), this.level.registryAccess());
            ItemStack storedDrinkStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
            if (storedDrinkStack.isEmpty()) {
               this.inventory.setStackInSlot(MEAL_DISPLAY_SLOT, resultStack.copy());
            } else if (ItemStack.isSameItem(storedDrinkStack, resultStack)) {
               storedDrinkStack.grow(resultStack.getCount());
            }

            juicer.setRecipeUsed(recipe);

            for(int i = 0; i < JuicerRecipe.INPUT_SLOTS; ++i) {
               ItemStack slotStack = this.inventory.getStackInSlot(i);
               if (slotStack.hasCraftingRemainingItem()) {
                  this.ejectIngredientRemainder(slotStack.getCraftingRemainingItem());
               }
               if (!slotStack.isEmpty()) {
                  slotStack.shrink(1);
               }
            }

            return true;
         }
      }
   }

   protected void ejectIngredientRemainder(ItemStack remainderStack) {
      Direction direction = this.getBlockState().getValue(JuicerBlock.FACING).getCounterClockWise();
      double x = (double)this.worldPosition.getX() + 0.5 + (double)direction.getStepX() * 0.25;
      double y = (double)this.worldPosition.getY() + 0.7;
      double z = (double)this.worldPosition.getZ() + 0.5 + (double)direction.getStepZ() * 0.25;
      ItemUtils.spawnItemEntity(this.level, remainderStack, x, y, z, (double)((float)direction.getStepX() * 0.08F), 0.25, (double)((float)direction.getStepZ() * 0.08F));
   }

   @Override
   public void setRecipeUsed(@Nullable Recipe<?> recipe) {
      if (recipe != null) {
         ResourceLocation recipeID = recipe.getId();
         this.usedRecipeTracker.addTo(recipeID, 1);
      }

   }

   @Override
   @Nullable
   public Recipe<?> getRecipeUsed() {
      return null;
   }

   @Override
   public void awardUsedRecipes(Player player, List<ItemStack> items) {
      List<Recipe<?>> usedRecipes = this.getUsedRecipesAndPopExperience(player.level(), player.position());
      player.awardRecipes(usedRecipes);
      this.usedRecipeTracker.clear();
   }

   public List<Recipe<?>> getUsedRecipesAndPopExperience(Level level, Vec3 pos) {
      List<Recipe<?>> list = Lists.newArrayList();

      for (Object2IntMap.Entry<ResourceLocation> entry : this.usedRecipeTracker.object2IntEntrySet()) {
         level.getRecipeManager().byKey(entry.getKey()).ifPresent((recipe) -> {
            list.add(recipe);
            if (recipe instanceof JuicerRecipe juicerRecipe) {
               splitAndSpawnExperience((ServerLevel)level, pos, entry.getIntValue(), juicerRecipe.getExperience());
            }
         });
      }

      return list;
   }

   private static void splitAndSpawnExperience(ServerLevel level, Vec3 pos, int craftedAmount, float experience) {
      int expTotal = Mth.floor((float)craftedAmount * experience);
      float expFraction = Mth.frac((float)craftedAmount * experience);
      if (expFraction != 0.0F && Math.random() < (double)expFraction) {
         ++expTotal;
      }

      ExperienceOrb.award(level, pos, expTotal);
   }

   public ItemStackHandler getInventory() {
      return this.inventory;
   }

   public ItemStack getDrink() {
      return this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
   }

   public NonNullList<ItemStack> getDroppableInventory() {
      NonNullList<ItemStack> drops = NonNullList.create();

      for(int i = 0; i < INVENTORY_SIZE; ++i) {
         if (i != MEAL_DISPLAY_SLOT) {
            drops.add(this.inventory.getStackInSlot(i));
         }
      }

      return drops;
   }

   private void moveDrinkToOutput() {
      ItemStack drinkStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
      ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
      int drinkCount = Math.min(drinkStack.getCount(), drinkStack.getMaxStackSize() - outputStack.getCount());
      if (outputStack.isEmpty()) {
         this.inventory.setStackInSlot(OUTPUT_SLOT, drinkStack.split(drinkCount));
      } else if (outputStack.getItem() == drinkStack.getItem()) {
         drinkStack.shrink(drinkCount);
         outputStack.grow(drinkCount);
      }

   }

   private void useStoredContainersOnDrink() {
      ItemStack drinkStack = this.inventory.getStackInSlot(MEAL_DISPLAY_SLOT);
      ItemStack containerInputStack = this.inventory.getStackInSlot(CONTAINER_SLOT);
      ItemStack outputStack = this.inventory.getStackInSlot(OUTPUT_SLOT);
      if (this.isContainerValid(containerInputStack) && outputStack.getCount() < outputStack.getMaxStackSize()) {
         int smallerStackCount = Math.min(drinkStack.getCount(), containerInputStack.getCount());
         int drinkCount = Math.min(smallerStackCount, drinkStack.getMaxStackSize() - outputStack.getCount());
         if (outputStack.isEmpty()) {
            containerInputStack.shrink(drinkCount);
            this.inventory.setStackInSlot(OUTPUT_SLOT, drinkStack.split(drinkCount));
         } else if (outputStack.getItem() == drinkStack.getItem()) {
            drinkStack.shrink(drinkCount);
            containerInputStack.shrink(drinkCount);
            outputStack.grow(drinkCount);
         }
      }

   }

   public ItemStack useHeldItemOnDrink(ItemStack container) {
      if (this.isContainerValid(container) && !this.getDrink().isEmpty()) {
         container.shrink(1);
         ItemStack serving = this.getDrink().split(1);
         // upstream changes the stack without telling the block entity, so the change is neither saved nor synced
         this.inventoryChanged();
         return serving;
      } else {
         return ItemStack.EMPTY;
      }
   }

   private boolean doesDrinkHaveContainer(ItemStack stack) {
      return !this.containerStack.isEmpty() || stack.hasCraftingRemainingItem();
   }

   public boolean isContainerValid(ItemStack containerItem) {
      if (containerItem.isEmpty()) {
         return false;
      } else {
         return !this.containerStack.isEmpty() ? ItemStack.isSameItem(this.containerStack, containerItem) : ItemStack.isSameItem(this.getDrink(), containerItem);
      }
   }

   @Override
   public Component getName() {
      return this.customName != null ? this.customName : Component.translatable("expandeddelight.container.juicer");
   }

   @Override
   public Component getDisplayName() {
      return this.getName();
   }

   @Override
   @Nullable
   public Component getCustomName() {
      return this.customName;
   }

   public void setCustomName(Component name) {
      this.customName = name;
   }

   @Override
   public AbstractContainerMenu createMenu(int id, Inventory player, Player entity) {
      return new JuicerMenu(id, player, this, this.juicerData);
   }

   @Override
   public CompoundTag getUpdateTag() {
      return this.writeItems(new CompoundTag());
   }

   private ItemStackHandler createHandler() {
      return new ItemStackHandler(INVENTORY_SIZE) {
         @Override
         protected void onContentsChanged(int slot) {
            JuicerBlockEntity.this.inventoryChanged();
         }
      };
   }

   private ContainerData createIntArray() {
      return new ContainerData() {
         @Override
         public int get(int index) {
            return switch (index) {
               case 0 -> JuicerBlockEntity.this.juiceTime;
               case 1 -> JuicerBlockEntity.this.juiceTimeTotal;
               default -> 0;
            };
         }

         @Override
         public void set(int index, int value) {
            switch (index) {
               case 0 -> JuicerBlockEntity.this.juiceTime = value;
               case 1 -> JuicerBlockEntity.this.juiceTimeTotal = value;
            }

         }

         @Override
         public int getCount() {
            return 2;
         }
      };
   }
}
