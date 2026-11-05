package probe;

import com.brandon3055.draconicevolution.blocks.tileentity.chest.TileDraconiumChest;
import com.brandon3055.draconicevolution.init.DEContent;
import com.brandon3055.draconicevolution.inventory.DraconiumChestMenu;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Test-only probe for SkyTekx DE Fixes. It is not part of the fix pack jar and never ships. Put its jar next
 * to the fix pack on a test server and run "deprobe" on the server console, then read the "[deprobe]" lines.
 */
@Mod("skytekx_deprobe")
public class Probe {
    private static final Logger LOG = LogUtils.getLogger();

    public Probe() {
        MinecraftForge.EVENT_BUS.addListener(this::commands);
    }

    private void commands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("deprobe").requires(s -> s.hasPermission(2)).executes(c -> {
            run(c.getSource());
            return 1;
        }));
    }

    private static void out(String msg, Object... args) {
        LOG.info("[deprobe] " + msg, args);
    }

    private static Item item(String id) {
        return ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
    }

    private static String items(Ingredient ingredient) {
        return Arrays.stream(ingredient.getItems()).map(s -> ForgeRegistries.ITEMS.getKey(s.getItem()).toString()).distinct().collect(Collectors.joining("|"));
    }

    private static void run(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        ServerLevel level = server.overworld();
        try { recipe(server); } catch (Throwable t) { out("recipe EXC {}", t.toString()); }
        try { tags(); } catch (Throwable t) { out("tags EXC {}", t.toString()); }
        try { tiers(); } catch (Throwable t) { out("tiers EXC {}", t.toString()); }
        try { occlusion(); } catch (Throwable t) { out("occlusion EXC {}", t.toString()); }
        try { lang(); } catch (Throwable t) { out("lang EXC {}", t.toString()); }
        try { chaosShape(level); } catch (Throwable t) { out("chaos EXC {}", t.toString()); }
        try { chest(level); } catch (Throwable t) { out("chest EXC {}", t.toString()); LOG.error("chest", t); }
        try { channel(); } catch (Throwable t) { out("channel EXC {}", t.toString()); }
        out("done");
    }

    private static void recipe(MinecraftServer server) {
        Recipe<?> recipe = server.getRecipeManager().byKey(new ResourceLocation("draconicevolution", "modules/item_chaotic_shield_capacity")).orElse(null);
        if (!(recipe instanceof ShapedRecipe shaped)) {
            out("recipe missing or not shaped: {}", recipe);
            return;
        }
        List<Ingredient> ing = shaped.getIngredients();
        out("recipe chaotic_shield_capacity {}x{} D(bottom middle)={} C(top middle)={}", shaped.getWidth(), shaped.getHeight(), items(ing.get(7)), items(ing.get(1)));
    }

    private static void tags() {
        List<TagKey<Item>> keys = List.of(ItemTags.PICKAXES, ItemTags.AXES, ItemTags.SHOVELS, ItemTags.SWORDS, ItemTags.HOES);
        for (String tool : List.of("pickaxe", "axe", "shovel", "sword", "hoe", "staff")) {
            for (String tier : List.of("wyvern", "draconic", "chaotic")) {
                Item item = item("draconicevolution:" + tier + "_" + tool);
                if (item == null || item == Items.AIR) continue;
                ItemStack stack = new ItemStack(item);
                String in = keys.stream().filter(stack::is).map(k -> k.location().getPath()).collect(Collectors.joining(","));
                out("tag {}_{} in [{}]", tier, tool, in);
            }
        }
    }

    private static void tiers() {
        for (String tier : List.of("wyvern", "draconic", "chaotic")) {
            Item item = item("draconicevolution:" + tier + "_pickaxe");
            Tier t = ((TieredItem) item).getTier();
            ItemStack stack = new ItemStack(item);
            out("tier {}_pickaxe level={} speed={} damageBonus={} enchant={} correctForObsidian={} correctForAncientDebris={}", tier, t.getLevel(), t.getSpeed(), t.getAttackDamageBonus(), t.getEnchantmentValue(),
                    item.isCorrectToolForDrops(stack, Blocks.OBSIDIAN.defaultBlockState()), item.isCorrectToolForDrops(stack, Blocks.ANCIENT_DEBRIS.defaultBlockState()));
        }
    }

    private static void occlusion() {
        for (String id : List.of("dislocator_receptacle", "creative_op_capacitor", "generator", "dislocator_pedestal")) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("draconicevolution", id));
            BlockState state = block.defaultBlockState();
            out("occlusion {} canOcclude={} lightBlock={} destroySpeed={} requiresTool={}", id, state.canOcclude(), state.getLightBlock(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO), state.getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO), state.requiresCorrectToolForDrops());
        }
    }

    private static void lang() {
        String key = "death.attack.draconicevolution.chaos_implosion";
        out("lang {} has={} value={}", key, Language.getInstance().has(key), Language.getInstance().getOrDefault(key));
    }

    private static void chaosShape(ServerLevel level) {
        BlockPos pos = new BlockPos(8, 250, 8);
        level.setBlock(pos, DEContent.CHAOS_CRYSTAL_PART.get().defaultBlockState(), 3);
        BlockState state = level.getBlockState(pos);
        try {
            out("chaos part getShape ok bounds={}", state.getShape(level, pos).bounds());
        } catch (Throwable t) {
            out("chaos part getShape EXC {}", t.toString());
        }
    }

    private static String stack(ItemStack s) {
        return s.isEmpty() ? "empty" : s.getCount() + "x" + ForgeRegistries.ITEMS.getKey(s.getItem());
    }

    private static int count(ServerPlayer player, Item item) {
        int n = player.containerMenu.getCarried().is(item) ? player.containerMenu.getCarried().getCount() : 0;
        for (ItemStack s : player.getInventory().items) {
            if (s.is(item)) n += s.getCount();
        }
        return n;
    }

    private static void clear(ServerPlayer player, AbstractContainerMenu menu) {
        player.getInventory().clearContent();
        menu.setCarried(ItemStack.EMPTY);
    }

    /**
     * Two players with the same Draconium Chest open. A puts logs into the grid, B touches the grid so that
     * both menus have computed a result, A takes one result, then B clicks the result slot with the given
     * click type. Stock Draconic Evolution hands B a second result for ingredients that A used.
     */
    private static void chestCase(ServerLevel level, BlockPos pos, String name, int logs, ClickType type, int button) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        level.setBlock(pos, DEContent.DRACONIUM_CHEST.get().defaultBlockState(), 3);
        TileDraconiumChest tile = (TileDraconiumChest) level.getBlockEntity(pos);
        ServerPlayer a = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("00000000-0000-0000-0000-00000000000a"), "probeA"));
        ServerPlayer b = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("00000000-0000-0000-0000-00000000000b"), "probeB"));
        DraconiumChestMenu menuA = (DraconiumChestMenu) tile.createMenu(101, a.getInventory(), a);
        DraconiumChestMenu menuB = (DraconiumChestMenu) tile.createMenu(102, b.getInventory(), b);
        a.containerMenu = menuA;
        b.containerMenu = menuB;
        clear(a, menuA);
        clear(b, menuB);
        Slot gridA0 = menuA.craftIn.getSlot(0);
        Slot gridB0 = menuB.craftIn.getSlot(0);
        Slot resA = menuA.craftResultSlot;
        Slot resB = menuB.craftResultSlot;
        gridA0.set(new ItemStack(Items.OAK_LOG, logs));
        ItemStack moved = gridB0.remove(64);
        gridB0.set(moved);
        String before = "resultA=" + stack(resA.getItem()) + " resultB=" + stack(resB.getItem());
        menuA.clicked(resA.index, 0, ClickType.PICKUP, a);
        int planksA = count(a, Items.OAK_PLANKS);
        String afterA = "grid0=" + stack(gridA0.getItem()) + " staleResultB=" + stack(resB.getItem());
        menuB.clicked(resB.index, button, type, b);
        int planksB = count(b, Items.OAK_PLANKS);
        int logsLeft = gridA0.getItem().is(Items.OAK_LOG) ? gridA0.getItem().getCount() : 0;
        int logsUsed = logs - logsLeft;
        int planks = planksA + planksB;
        String verdict = planks > 4 * logsUsed ? "DUPE" : "NO DUPE";
        out("chest {} logs={} {} | A took {} planks, {} | B {} took {} planks, grid0={} resultB={} | {} planks from {} logs -> {}",
                name, logs, before, planksA, afterA, type, planksB, stack(gridB0.getItem()), stack(resB.getItem()), planks, logsUsed, verdict);
        menuA.removed(a);
        menuB.removed(b);
        a.containerMenu = a.inventoryMenu;
        b.containerMenu = b.inventoryMenu;
        clear(a, a.inventoryMenu);
        clear(b, b.inventoryMenu);
    }

    private static void chest(ServerLevel level) {
        BlockPos pos = new BlockPos(0, 250, 0);
        chestCase(level, pos, "pickup", 1, ClickType.PICKUP, 0);
        chestCase(level, pos.east(2), "shift", 1, ClickType.QUICK_MOVE, 0);
        chestCase(level, pos.east(4), "swap", 1, ClickType.SWAP, 0);
        chestCase(level, pos.east(6), "pickup-two-logs", 2, ClickType.PICKUP, 0);
        chestCase(level, pos.east(8), "shift-two-logs", 2, ClickType.QUICK_MOVE, 0);

        // the chest keeps its stock save format: nothing of this mod in the block entity or the item data
        TileDraconiumChest tile = (TileDraconiumChest) level.getBlockEntity(pos);
        tile.craftingItems.setStackInSlot(0, new ItemStack(Items.OAK_LOG));
        CompoundTag saved = tile.saveWithoutMetadata();
        CompoundTag itemData = new CompoundTag();
        tile.writeToItemStack(itemData, true);
        out("chest saved keys={} mentionsDefixes={} itemDataMentionsDefixes={}", saved.getAllKeys(), saved.toString().contains("skytekx"), itemData.toString().contains("skytekx"));
    }

    private static void channel() throws Exception {
        // findTarget is package private in Forge 47, and the public check logs an ERROR line
        Method find = net.minecraftforge.network.NetworkRegistry.class.getDeclaredMethod("findTarget", ResourceLocation.class);
        find.setAccessible(true);
        Object target = find.invoke(null, new ResourceLocation("skytekx_defixes", "main"));
        out("channel skytekx_defixes:main registered={}", ((java.util.Optional<?>) target).isPresent());
    }
}
