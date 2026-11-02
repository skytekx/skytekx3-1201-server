package twilightforest;

import com.mojang.authlib.EnvironmentParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.exceptions.*;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import twilightforest.network.SyncUncraftingTableConfigPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.PlayerHelper;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TFConfig {

	public static Common COMMON_CONFIG;
	public static Client CLIENT_CONFIG;

	// Baked copies of config values that are read on hot paths (every Level#isRainingAt call, every magic tree
	// core tick, the client weather tick), so those paths don't go through ForgeConfigSpec on each call.
	// They are refreshed on every ModConfigEvent.Loading and ModConfigEvent.Reloading of our configs.
	// Names follow TF 4.8's config.TFConfig. (upstream c50ef588a, 84ccdeec6, 333678333, a2b9fa06b)

	// --- CLIENT ---
	private static int clientCloudBlockPrecipitationDistance = -1;

	// --- COMMON ---
	public static int commonCloudBlockPrecipitationDistance = 32;

	// -- Magic Trees --
	public static boolean disableTimeCore = false;
	public static int timeCoreRange = 16;
	public static boolean disableTransformationCore = false;
	public static int transformationCoreRange = 16;
	public static boolean disableMiningCore = false;
	public static int miningCoreRange = 16;
	public static boolean disableSortingCore = false;
	public static int sortingCoreRange = 16;

	// -- Bosses --
	public static MultiplayerFightAdjuster multiplayerFightAdjuster = MultiplayerFightAdjuster.NONE;

	// -- Mystic Crown -- (read every tick while temporary Fortification shields count down)
	public static boolean mysticCrownCurios = true;
	public static boolean mysticCrownScepterBonuses = true;

	public static class Common {

		public Common(ForgeConfigSpec.Builder builder) {
			builder.
					comment("Settings that are not reversible without consequences.").
					push("Dimension Settings");
			{
				DIMENSION.newPlayersSpawnInTF = builder.
						translation(config + "spawn_in_tf").
						comment("If true, players spawning for the first time will spawn in the Twilight Forest.").
						define("newPlayersSpawnInTF", false);
				DIMENSION.portalForNewPlayerSpawn = builder.
						translation(config + "portal_for_new_player").
						comment("If true, the return portal will spawn for new players that were sent to the TF if `spawn_in_tf` is true.").
						define("portalForNewPlayer", false);
			}
			builder.pop();
			originDimension = builder.
					translation(config + "origin_dimension").
					comment("The dimension you can always travel to the Twilight Forest from, as well as the dimension you will return to. Defaults to the overworld. (domain:regname).").
					define("originDimension", "minecraft:overworld");
			allowPortalsInOtherDimensions = builder.
					translation(config + "portals_in_other_dimensions").
					comment("Allow portals to the Twilight Forest to be made outside of the 'origin' dimension. May be considered an exploit.").
					define("allowPortalsInOtherDimensions", false);
			adminOnlyPortals = builder.
					translation(config + "admin_portals").
					comment("""
							Legacy option, prefer `portalCreationPermission`.
							Allow portals only for admins (Operators). This severely reduces the range in which the mod usually scans for valid portal conditions, and it scans near ops only.
							If true, this requires at least permission level 1 on top of `portalCreationPermission`.""").
					define("adminOnlyPortals", false);
			portalCreationPermission = builder.
					translation(config + "portal_permission").
					comment("""
							Allows people with the specified permission or higher to create portals. This is based off of Vanilla's permission system.
							You can read about them here: https://minecraft.wiki/w/Permission_level
							At level 3 (admins) or higher the mod only scans for valid portal conditions in a small range around permitted players.""").
					defineInRange("portalCreationPermission", Commands.LEVEL_ALL, Commands.LEVEL_ALL, Commands.LEVEL_OWNERS);
			disablePortalCreation = builder.
					translation(config + "portals").
					comment("Disable Twilight Forest portal creation entirely. Provided for server operators looking to restrict action to the dimension.").
					define("disablePortalCreation", false);
			checkPortalDestination = builder.
					translation(config + "check_portal_destination").
					comment("""
							Determines if new portals should be pre-checked for safety. If enabled, portals will fail to form rather than redirect to a safe alternate destination.
							Note that enabling this also reduces the rate at which portal formation checks are performed.""").
					define("checkPortalDestination", false);
			portalLightning = builder.
					translation(config + "portal_lighting").
					comment("Set this true if you want the lightning that zaps the portal to not set things on fire. For those who don't like fun.").
					define("portalLightning", false);
			shouldReturnPortalBeUsable = builder.
					translation(config + "portal_return").
					comment("If false, the return portal will require the activation item.").
					define("shouldReturnPortalBeUsable", true);
			portalAdvancementLock = builder.
					translation(config + "portal_unlocked_by_advancement").
					comment("Use a valid advancement resource location as a string. For example, using the string \"minecraft:story/mine_diamond\" will lock the portal behind the \"Diamonds!\" advancement. Invalid/Empty Advancement resource IDs will leave the portal entirely unlocked.").
					define("portalUnlockedByAdvancement", "");
			maxPortalSize = builder.
					translation(config + "max_portal_size").
					comment("The max amount of water spaces the mod will check for when creating a portal. Very high numbers may cause issues.").
					defineInRange("maxPortalSize", 64, 4, Integer.MAX_VALUE);
			casketUUIDLocking = builder.
					worldRestart().
					translation(config + "casket_uuid_locking").
					comment("If true, Keepsake Caskets that are spawned when a player dies will not be accessible by other players. Use this if you dont want people taking from other people's death caskets. NOTE: server operators will still be able to open locked caskets.")
					.define("uuid_locking", false);
			disableSkullCandles = builder.
					translation(config + "disable_skull_candles").
					comment("If true, disables the ability to make Skull Candles by right clicking a vanilla skull with a candle. Turn this on if you're having mod conflict issues for some reason.").
					define("skull_candles", false);
			defaultItemEnchants = builder.
					translation(config + "default_item_enchantments").
					worldRestart().
					comment("""
							If false, items that come enchanted when you craft them (such as ironwood or steeleaf gear) will not show this way in the creative inventory.
							Please note that this doesnt affect the crafting recipes themselves, you will need a datapack to change those.""").
					define("default_item_enchantments", true);

			bossDropChests = builder.
					translation(config + "boss_drop_chests").
					comment("""
							If true, Twilight Forest's bosses will put their drops inside of a chest where they originally spawned instead of dropping the loot directly.
							The Knight Phantoms share one chest, filled by the last knight to fall; if false, each knight drops its own loot and the last one also drops the trophy.""").
					define("boss_drop_chests", true);

			cloudBlockPrecipitationDistanceCommon = builder.
					translation(config + "cloud_block_precipitation_distance_server").
					comment("""
							Dictates how many blocks down from a cloud block should the game logic check for handling weather related code.
							Lower if experiencing low tick rate. Set to 0 to turn all cloud precipitation logic off.""").
					defineInRange("cloudBlockPrecipitationDistance", 32, 0, Integer.MAX_VALUE);

			multiplayerFightAdjuster = builder.
					worldRestart().
					translation(config + "multiplayer_fight_adjuster").
					comment("""
							Determines how bosses should adjust to multiplayer fights. There are 4 possible values that can be put here:
							NONE: doesnt do anything when multiple people participate in a bossfight. Bosses will act the same as they do in singleplayer or solo fights.
							MORE_LOOT: adds additional drops to a boss' loot table based on how many players participated in the fight. These are fully controlled through the entity's loot table, using the `twilightforest:multiplayer_addition` loot function and the `twilightforest:multiplayer_rolls` number provider. Note that these will only do things to entities that are included in the `twilightforest:multiplayer_inclusive_entities` tag.
							MORE_HEALTH: increases the health of each boss in that tag by 10 hearts on Easy, 20 on Normal and 30 on Hard for each additional player nearby when it spawns.
							MORE_LOOT_AND_HEALTH: does both of the above functions for each boss.""").
					defineEnum("multiplayerFightAdjuster", MultiplayerFightAdjuster.NONE);

			// The two Mystic Crown options come from the Mystic Crown Compat addon (MIT, by 741741).
			mysticCrownCurios = builder.
					translation(config + "mystic_crown_curios").
					comment("If true and Curios is installed, the Mystic Crown can also be worn in a Curios head slot, where it counts as worn for all of its effects.").
					define("mysticCrownCurios", true);
			mysticCrownScepterBonuses = builder.
					translation(config + "mystic_crown_scepter_bonuses").
					comment("""
							If true, a worn Mystic Crown also empowers the scepters:
							the Twilight Scepter has a 10% chance to fire an extra Lich bomb, and the wearer takes no damage from Lich bombs;
							the Lifedrain Scepter drains twice as much health;
							temporary Fortification shields no longer run out over time, they only break from hits;
							Zombie Scepter zombies keep their strength for 3 minutes instead of 1, may wear leather armor (baby zombies get a full set and a Knockback II stick) and hit for 4 damage instead of 7.
							If false, the crown keeps only its normal effects: a 5% chance to save a scepter charge, 50% longer temporary shields and a 10% chance to summon a baby zombie.""").
					define("mysticCrownScepterBonuses", true);

			builder.
					comment("Settings for all things related to the uncrafting table.").
					push("Uncrafting Table");
			{
				UNCRAFTING_STUFFS.uncraftingXpCostMultiplier = builder.
						worldRestart().
						translation(config + "uncrafting_xp_cost").
						comment("""
								Multiplies the total XP cost of uncrafting an item and rounds up.
								Higher values means the recipe will cost more to uncraft, lower means less. Set to 0 to disable the cost altogether.
								Note that this only affects reversed crafting recipes, uncrafting recipes will still use the same cost as they normally would.""").
						defineInRange("uncraftingXpCostMultiplier", 1.0D, 0.0D, Double.MAX_VALUE);
				UNCRAFTING_STUFFS.repairingXpCostMultiplier = builder.
						worldRestart().
						translation(config + "repairing_xp_cost").
						comment("""
								Multiplies the total XP cost of repairing an item and rounds up.
								Higher values means the recipe will cost more to repair, lower means less. Set to 0 to disable the cost altogether.""").
						defineInRange("repairingXpCostMultiplier", 1.0D, 0.0D, Double.MAX_VALUE);
				UNCRAFTING_STUFFS.disableUncraftingRecipes = builder.
						worldRestart().
						translation(config + "uncrafting_recipes").
						comment("""
								If you don't want to disable uncrafting altogether, and would rather disable certain recipes, this is for you.
								To add a recipe, add the mod id followed by the name of the recipe. You can check this in things like JEI.
								Example: "twilightforest:firefly_particle_spawner" will disable uncrafting the particle spawner into a firefly jar, firefly, and poppy.
								If an item has multiple crafting recipes and you wish to disable them all, add the item to the "twilightforest:banned_uncraftables" item tag.
								If you have a problematic ingredient, like infested towerwood for example, add the item to the "twilightforest:banned_uncrafting_ingredients" item tag.""").
						defineList("disableUncraftingRecipes", List.of("twilightforest:giant_log_to_oak_log"), s -> s instanceof String);
				UNCRAFTING_STUFFS.reverseRecipeBlacklist = builder.
						worldRestart().
						translation(config + "uncrafting_recipes_flip").
						comment("If true, this will invert the above uncrafting recipe list from a blacklist to a whitelist.").
						define("flipRecipeList", false);
				UNCRAFTING_STUFFS.blacklistedUncraftingModIds = builder.
						worldRestart().
						translation(config + "uncrafting_mod_ids").
						comment("""
								Here, you can disable all items from certain mods from being uncrafted.
								Input a valid mod id to disable all uncrafting recipes from that mod.
								Example: "twilightforest" will disable all uncrafting recipes from this mod.""").
						defineList("blacklistedUncraftingModIds", new ArrayList<>(), s -> s instanceof String);
				UNCRAFTING_STUFFS.flipUncraftingModIdList = builder.
						worldRestart().
						translation(config + "uncrafting_mod_id_flip").
						comment("If true, this will invert the above option from a blacklist to a whitelist.").
						define("flipIdList", false);
				UNCRAFTING_STUFFS.allowShapelessUncrafting = builder.
						worldRestart().
						translation(config + "shapeless_uncrafting").
						comment("""
								If true, the uncrafting table will also be allowed to uncraft shapeless recipes.
								The table was originally intended to only take shaped recipes, but this option remains for people who wish to keep the functionality.""").
						define("enableShapelessCrafting", false);
				UNCRAFTING_STUFFS.disableIngredientSwitching = builder.
						worldRestart().
						translation(config + "ingredient_switching").
						comment("""
								If true, the uncrafting table will no longer allow you to switch between ingredients if a recipe uses a tag for crafting.
								This will remove the functionality for ALL RECIPES!
								If you want to prevent certain ingredients from showing up in the first place, use the "twilightforest:banned_uncrafting_ingredients" tag.""").
						define("disableIngredientSwitching", false);
				UNCRAFTING_STUFFS.disableUncraftingOnly = builder.
						worldRestart().
						translation(config + "disable_uncrafting").
						comment("""
								Disables the uncrafting function of the uncrafting table. Recommended as a last resort if there's too many things to change about its behavior (or you're just lazy, I dont judge).
								Do note that special uncrafting recipes are not disabled as the mod relies on them for other things.""").
						define("disableUncrafting", false);

				UNCRAFTING_STUFFS.disableEntireTable = builder.
						worldRestart().
						translation(config + "disable_uncrafting_table").
						comment("""
								Disables any usage of the uncrafting table, as well as prevents it from showing up in loot or crafted.
								Please note that table has more uses than just uncrafting, you can read about them here! http://benimatic.com/tfwiki/index.php?title=Uncrafting_Table
								It is highly recommended to keep the table enabled as the mod has special uncrafting exclusive recipes, but the option remains for people that dont want the table to be functional at all.
								If you are looking to just prevent normal crafting recipes from being reversed, consider using the 'disableUncrafting' option instead.""").
						define("disableUncraftingTable", false);
			}
			builder.pop();

			builder.
					comment("Settings for all things related to the magic trees.").
					push("Magic Trees");
			{
				MAGIC_TREES.disableTime = builder.
						worldRestart().
						translation(config + "disable_time").
						comment("If true, prevents the Timewood Core from functioning.").
						define("disableTimeCore", false);

				MAGIC_TREES.timeRange = builder.
						worldRestart().
						translation(config + "time_range").
						comment("""
								Defines the radius at which the Timewood Core works. Can be a number anywhere between 1 and 128.
								Set to 0 to prevent the Timewood Core from functioning.""")
						.defineInRange("timeCoreRange", 16, 0, 128);

				MAGIC_TREES.disableTransformation = builder.
						worldRestart().
						translation(config + "disable_transformation").
						comment("If true, prevents the Transformation Core from functioning.").
						define("disableTransformationCore", false);

				MAGIC_TREES.transformationRange = builder.
						worldRestart().
						translation(config + "transformation_range").
						comment("""
								Defines the radius at which the Transformation Core works. Can be a number anywhere between 1 and 128.
								Set to 0 to prevent the Transformation Core from functioning.""")
						.defineInRange("transformationCoreRange", 16, 0, 128);

				MAGIC_TREES.disableMining = builder.
						worldRestart().
						translation(config + "disable_mining").
						comment("If true, prevents the Minewood Core from functioning.").
						define("disableMiningCore", false);

				MAGIC_TREES.miningRange = builder.
						worldRestart().
						translation(config + "mining_range").
						comment("""
								Defines the radius at which the Minewood Core works. Can be a number anywhere between 1 and 128.
								Set to 0 to prevent the Minewood Core from functioning.""")
						.defineInRange("miningCoreRange", 16, 0, 128);

				MAGIC_TREES.disableSorting = builder.
						worldRestart().
						translation(config + "disable_sorting").
						comment("If true, prevents the Sortingwood Core from functioning.").
						define("disableSortingCore", false);

				MAGIC_TREES.sortingRange = builder.
						worldRestart().
						translation(config + "sorting_range").
						comment("""
								Defines the radius at which the Sortingwood Core works. Can be a number anywhere between 1 and 128.
								Set to 0 to prevent the Sortingwood Core from functioning.""")
						.defineInRange("sortingCoreRange", 16, 0, 128);
			}
			builder.pop();

			builder.
					comment("We recommend downloading the Shield Parry mod for parrying, but these controls remain for without.").
					push("Shield Parrying");
			{
				SHIELD_INTERACTIONS.parryNonTwilightAttacks = builder.
						translation(config + "parry_non_twilight").
						comment("Set to true to parry non-Twilight projectiles.").
						define("parryNonTwilightAttacks", false);
				SHIELD_INTERACTIONS.shieldParryTicks = builder.
						translation(config + "parry_window").
						comment("The amount of ticks after raising a shield that makes it OK to parry a projectile.").
						defineInRange("shieldParryTicksArrow", 40, 0, Integer.MAX_VALUE);
			}
			builder.pop();
		}

		public final Dimension DIMENSION = new Dimension();

		public static class Dimension {

			public ForgeConfigSpec.BooleanValue newPlayersSpawnInTF;
			public ForgeConfigSpec.BooleanValue portalForNewPlayerSpawn;

		}

		public final ForgeConfigSpec.ConfigValue<String> originDimension;
		public final ForgeConfigSpec.BooleanValue allowPortalsInOtherDimensions;
		public final ForgeConfigSpec.BooleanValue adminOnlyPortals;
		public final ForgeConfigSpec.IntValue portalCreationPermission;
		public final ForgeConfigSpec.BooleanValue disablePortalCreation;
		public final ForgeConfigSpec.BooleanValue checkPortalDestination;
		public final ForgeConfigSpec.BooleanValue portalLightning;
		public final ForgeConfigSpec.BooleanValue shouldReturnPortalBeUsable;
		public final ForgeConfigSpec.ConfigValue<String> portalAdvancementLock;
		public final ForgeConfigSpec.IntValue maxPortalSize;
		public final ForgeConfigSpec.BooleanValue casketUUIDLocking;
		public final ForgeConfigSpec.BooleanValue disableSkullCandles;
		public final ForgeConfigSpec.BooleanValue defaultItemEnchants;
		public final ForgeConfigSpec.BooleanValue bossDropChests;
		public final ForgeConfigSpec.IntValue cloudBlockPrecipitationDistanceCommon;
		public final ForgeConfigSpec.EnumValue<MultiplayerFightAdjuster> multiplayerFightAdjuster;
		public final ForgeConfigSpec.BooleanValue mysticCrownCurios;
		public final ForgeConfigSpec.BooleanValue mysticCrownScepterBonuses;

		public final MagicTrees MAGIC_TREES = new MagicTrees();

		public static class MagicTrees {
			public ForgeConfigSpec.BooleanValue disableTime;
			public ForgeConfigSpec.IntValue timeRange;
			public ForgeConfigSpec.BooleanValue disableTransformation;
			public ForgeConfigSpec.IntValue transformationRange;
			public ForgeConfigSpec.BooleanValue disableMining;
			public ForgeConfigSpec.IntValue miningRange;
			public ForgeConfigSpec.BooleanValue disableSorting;
			public ForgeConfigSpec.IntValue sortingRange;
		}

		public final UncraftingStuff UNCRAFTING_STUFFS = new UncraftingStuff();

		public static class UncraftingStuff {
			public ForgeConfigSpec.DoubleValue uncraftingXpCostMultiplier;
			public ForgeConfigSpec.DoubleValue repairingXpCostMultiplier;
			public ForgeConfigSpec.BooleanValue allowShapelessUncrafting;
			public ForgeConfigSpec.BooleanValue disableIngredientSwitching;
			public ForgeConfigSpec.BooleanValue disableUncraftingOnly;
			public ForgeConfigSpec.BooleanValue disableEntireTable;
			public ForgeConfigSpec.ConfigValue<List<? extends String>> disableUncraftingRecipes;
			public ForgeConfigSpec.BooleanValue reverseRecipeBlacklist;
			public ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistedUncraftingModIds;
			public ForgeConfigSpec.BooleanValue flipUncraftingModIdList;
		}

		public final ShieldInteractions SHIELD_INTERACTIONS = new ShieldInteractions();
		@Nullable
		public ResourceLocation portalLockingAdvancement;

		public static class ShieldInteractions {
			public ForgeConfigSpec.BooleanValue parryNonTwilightAttacks;
			public ForgeConfigSpec.IntValue shieldParryTicks;
		}

	}

	public static class Client {

		public Client(ForgeConfigSpec.Builder builder) {
			silentCicadas = builder.
					translation(config + "silent_cicadas").
					comment("Make cicadas silent for those having sound library problems, or otherwise finding them annoying.").
					define("silentCicadas", false);
			silentCicadasOnHead = builder.
					translation(config + "silent_cicadas_on_head").
					comment("Make cicadas silent when sitting on your head. If the above option is already true, this won't have any effect.").
					define("silentCicadasOnHead", false);
			firstPersonEffects = builder.
					translation(config + "first_person_effects").
					comment("Controls whether various effects from the mod are rendered while in first-person view, including the screen shaking while a Magic Beanstalk is growing. Turn this off if you find them distracting.").
					define("firstPersonEffects", true);
			rotateTrophyHeadsGui = builder.
					translation(config + "animate_trophyitem").
					comment("Rotate trophy heads on item model. Has no performance impact at all. For those who don't like fun.").
					define("rotateTrophyHeadsGui", true);
			disableOptifineNagScreen = builder.
					translation(config + "optifine").
					comment("Disable the nag screen when Optifine is installed.").
					define("disableOptifineNagScreen", false);
			disableLockedBiomeToasts = builder.
					translation(config + "locked_toasts").
					comment("Disables the toasts that appear when a biome is locked. Not recommended if you're not familiar with progression.").
					define("disableLockedBiomeToasts", false);
			showQuestRamCrosshairIndicator = builder.
					translation(config + "ram_indicator").
					comment("Renders a little check mark or x above your crosshair depending on if fed the Quest Ram that color of wool. Turn this off if you find it intrusive.").
					define("questRamWoolIndicator", true);
			showFortificationShieldIndicator = builder.
					translation(config + "shield_indicator").
					comment("Renders how many fortification shields are currently active on your player above your armor bar. Turn this off if you find it intrusive or other mods render over/under it.").
					define("fortificationShieldIndicator", true);
			showFortificationShieldIndicatorInCreative = builder.
					translation(config + "shield_indicator_creative").
					comment("Enables the fortification shield indicator in creative for debugging.").
					define("fortificationShieldIndicatorInCreative", false);
			cloudBlockPrecipitationDistanceClient = builder.
					translation(config + "cloud_block_precipitation_distance").
					comment("""
							Renders precipitation underneath cloud blocks. -1 sets it to be synced with the common config.
							Set this to a lower number if you're experiencing poor performance, or set it to 0 if you wish to turn it off""").
					defineInRange("cloudBlockPrecipitationDistance", -1, -1, Integer.MAX_VALUE);
			giantSkinUUIDs = builder.
					translation(config + "giant_skin_uuid_list").
					comment("""
							List of player UUIDs whose skins the giants of Twilight Forest should use.
							If left empty, the giants will appear the same as the player viewing them does.""").
					defineListAllowEmpty("giantSkinUUIDs", new ArrayList<>(), s -> s instanceof String);
			auroraBiomes = builder.
					translation(config + "aurora_biomes").
					comment("Defines which biomes the aurora shader effect will appear in. Leave the list empty to disable the effect.")
					.defineList("auroraBiomes", List.of("twilightforest:glacier"), s -> s instanceof String);
			prettifyOreMeterGui = builder.
					translation(config + "prettify_ore_meter_gui").
					comment("Lines up the dashes and percentages in the Ore Meter GUI.").
					define("prettifyOreMeterGui", true);
			spawnCharmAnimationAsTotem = builder.
					translation(config + "totem_charm_animation").
					comment("If true, Twilight Forest charm items will display similar to the totem of undying when used.").
					define("totemCharmAnimation", false);
		}

		public final ForgeConfigSpec.BooleanValue silentCicadas;
		public final ForgeConfigSpec.BooleanValue silentCicadasOnHead;
		public final ForgeConfigSpec.BooleanValue firstPersonEffects;
		public final ForgeConfigSpec.BooleanValue rotateTrophyHeadsGui;
		public final ForgeConfigSpec.BooleanValue disableOptifineNagScreen;
		public final ForgeConfigSpec.BooleanValue disableLockedBiomeToasts;
		public final ForgeConfigSpec.BooleanValue showQuestRamCrosshairIndicator;
		public final ForgeConfigSpec.BooleanValue showFortificationShieldIndicator;
		public final ForgeConfigSpec.BooleanValue showFortificationShieldIndicatorInCreative;
		public final ForgeConfigSpec.IntValue cloudBlockPrecipitationDistanceClient;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> giantSkinUUIDs;
		public final ForgeConfigSpec.ConfigValue<List<? extends String>> auroraBiomes;
		private final List<Holder<Biome>> validAuroraBiomes = new ArrayList<>();
		public final ForgeConfigSpec.BooleanValue prettifyOreMeterGui;
		public final ForgeConfigSpec.BooleanValue spawnCharmAnimationAsTotem;
	}

	private static final String config = "config." + TwilightForestMod.ID;

	/**
	 * How bosses in {@code #twilightforest:multiplayer_inclusive_entities} adapt to group fights (backported from TF 4.8, #1987).
	 */
	public enum MultiplayerFightAdjuster {
		NONE(false, false),
		MORE_LOOT(true, false),
		MORE_HEALTH(false, true),
		MORE_LOOT_AND_HEALTH(true, true);

		private final boolean moreLoot;
		private final boolean moreHealth;

		MultiplayerFightAdjuster(boolean loot, boolean health) {
			this.moreLoot = loot;
			this.moreHealth = health;
		}

		public boolean adjustsLootRolls() {
			return this.moreLoot;
		}

		public boolean adjustsHealth() {
			return this.moreHealth;
		}
	}

	/**
	 * A magic tree core works unless it is disabled through its boolean option or its range is set to 0.
	 * Takes the baked values (e.g. {@link #disableTimeCore}, {@link #timeCoreRange}) so the core tick stays off ForgeConfigSpec.
	 */
	public static boolean isMagicTreeCoreEnabled(boolean disabled, int range) {
		return !disabled && range > 0;
	}

	public static int getClientCloudBlockPrecipitationDistance() {
		return clientCloudBlockPrecipitationDistance == -1 ? commonCloudBlockPrecipitationDistance : clientCloudBlockPrecipitationDistance;
	}

	private static void rebakeCommonOptions(Common config) {
		commonCloudBlockPrecipitationDistance = config.cloudBlockPrecipitationDistanceCommon.get();

		// Tree Cores
		disableTimeCore = config.MAGIC_TREES.disableTime.get();
		timeCoreRange = config.MAGIC_TREES.timeRange.get();
		disableTransformationCore = config.MAGIC_TREES.disableTransformation.get();
		transformationCoreRange = config.MAGIC_TREES.transformationRange.get();
		disableMiningCore = config.MAGIC_TREES.disableMining.get();
		miningCoreRange = config.MAGIC_TREES.miningRange.get();
		disableSortingCore = config.MAGIC_TREES.disableSorting.get();
		sortingCoreRange = config.MAGIC_TREES.sortingRange.get();

		multiplayerFightAdjuster = config.multiplayerFightAdjuster.get();

		mysticCrownCurios = config.mysticCrownCurios.get();
		mysticCrownScepterBonuses = config.mysticCrownScepterBonuses.get();
	}

	private static void rebakeClientOptions(Client config) {
		clientCloudBlockPrecipitationDistance = config.cloudBlockPrecipitationDistanceClient.get();
	}

	@Nullable
	public static ResourceLocation getPortalLockingAdvancement(Player player) {
		//only run assigning logic if the config has an advancement set and the RL is null
		if (!COMMON_CONFIG.portalAdvancementLock.get().isEmpty() && COMMON_CONFIG.portalLockingAdvancement == null) {

			if (!ResourceLocation.isValidResourceLocation(COMMON_CONFIG.portalAdvancementLock.get()) || PlayerHelper.getAdvancement(player, ResourceLocation.tryParse(COMMON_CONFIG.portalAdvancementLock.get())) == null) {
				//if the RL is not a valid advancement fail us
				TwilightForestMod.LOGGER.fatal("The portal locking advancement is not a valid advancement! Setting to null!");
				COMMON_CONFIG.portalAdvancementLock.set("");
			} else {
				COMMON_CONFIG.portalLockingAdvancement = ResourceLocation.tryParse(COMMON_CONFIG.portalAdvancementLock.get());
				TwilightForestMod.LOGGER.debug("Portal Locking Advancement reloaded.");
			}
		}
		//always return the RL, even if its null. We can use this to run logic less often
		return COMMON_CONFIG.portalLockingAdvancement;
	}

	//Forge's biome registry doesn't contain biomes done via datapacks, so we have to use registryaccess
	//Holders are cached so the client tick doesn't have to look the camera's biome up in the registry every tick
	public static List<Holder<Biome>> getValidAuroraBiomes(RegistryAccess access) {
		if (CLIENT_CONFIG.validAuroraBiomes.isEmpty() && !CLIENT_CONFIG.auroraBiomes.get().isEmpty()) {
			CLIENT_CONFIG.auroraBiomes.get().forEach(s -> {
				Optional<? extends Holder<Biome>> holder = Optional.ofNullable(ResourceLocation.tryParse(s)).flatMap(key -> access.registryOrThrow(Registries.BIOME).getHolder(ResourceKey.create(Registries.BIOME, key)));
				if (holder.isEmpty()) {
					TwilightForestMod.LOGGER.warn("Biome {} in Twilight Forest's validAuroraBiomes config option is not a valid biome. Skipping!", s);
				} else {
					CLIENT_CONFIG.validAuroraBiomes.add(holder.get());
				}
			});
		}
		return CLIENT_CONFIG.validAuroraBiomes;
	}

	@SubscribeEvent
	public static void onConfigReload(final ModConfigEvent event) {
		if (Objects.equals(event.getConfig().getModId(), TwilightForestMod.ID)) {
			// This listener sees Loading as well as Reloading, so the baked values are also set on startup
			// and not only when a config is changed while the game is running. (upstream 333678333)
			if (!(event instanceof ModConfigEvent.Unloading)) {
				if (event.getConfig().getType() == ModConfig.Type.COMMON) {
					rebakeCommonOptions(COMMON_CONFIG);
				} else if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
					rebakeClientOptions(CLIENT_CONFIG);
				}
			}

			if (event.getConfig().getType() == ModConfig.Type.COMMON) {
				//resends uncrafting settings to all players when the config is reloaded. This ensures all players have matching configs so things dont desync.
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				if (server != null && server.isDedicatedServer()) {
					TFPacketHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncUncraftingTableConfigPacket(
							COMMON_CONFIG.UNCRAFTING_STUFFS.uncraftingXpCostMultiplier.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.repairingXpCostMultiplier.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.allowShapelessUncrafting.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.disableIngredientSwitching.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingOnly.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingRecipes.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.reverseRecipeBlacklist.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.blacklistedUncraftingModIds.get(),
							COMMON_CONFIG.UNCRAFTING_STUFFS.flipUncraftingModIdList.get()));
				}
				//sets cached portal locking advancement to null just in case it changed
				COMMON_CONFIG.portalLockingAdvancement = null;
			} else if (event.getConfig().getType() == ModConfig.Type.CLIENT) {
				CLIENT_CONFIG.validAuroraBiomes.clear();
			}
		}
	}

	@SubscribeEvent
	public static void onConfigReload(final ModConfigEvent.Reloading event) {
		if (Objects.equals(event.getConfig().getModId(), TwilightForestMod.ID) && event.getConfig().getType() == ModConfig.Type.CLIENT) {
			TFConfig.reloadGiantSkins();
		}
	}

	//damn forge events
	@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
	public static class ConfigSync {
		//sends uncrafting settings to a player on a server when they log in. This prevents desyncs when the configs dont match up between the player and the server.
		@SubscribeEvent
		public static void syncConfigOnLogin(PlayerEvent.PlayerLoggedInEvent event) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null && server.isDedicatedServer() && event.getEntity() instanceof ServerPlayer player) {
				TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncUncraftingTableConfigPacket(
						COMMON_CONFIG.UNCRAFTING_STUFFS.uncraftingXpCostMultiplier.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.repairingXpCostMultiplier.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.allowShapelessUncrafting.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.disableIngredientSwitching.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingOnly.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.disableEntireTable.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.disableUncraftingRecipes.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.reverseRecipeBlacklist.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.blacklistedUncraftingModIds.get(),
						COMMON_CONFIG.UNCRAFTING_STUFFS.flipUncraftingModIdList.get()));
			}
		}
	}

	public static final List<GameProfile> GAME_PROFILES = new ArrayList<>();

	public static void reloadGiantSkins() {
		if (!TFConfig.CLIENT_CONFIG.giantSkinUUIDs.get().isEmpty()) {
			new Thread() {
				@Override
				public void run() {
					GAME_PROFILES.clear();

					YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY);
					String baseUrl = EnvironmentParser.getEnvironmentFromProperties().orElse(YggdrasilEnvironment.PROD.getEnvironment()).getSessionHost() + "/session/minecraft/";
					boolean requireSecure = false;
					for (String stringUUID : TFConfig.CLIENT_CONFIG.giantSkinUUIDs.get()) {
						try {
							UUID uuid = UUID.fromString(stringUUID);

							URL url = HttpAuthenticationService.constantURL(baseUrl + "profile/" + uuid);
							url = HttpAuthenticationService.concatenateURL(url, "unsigned=" + !requireSecure);

							final MinecraftProfilePropertiesResponse response = ObjectMapper.create().readValue(service.performGetRequest(url), MinecraftProfilePropertiesResponse.class);

							if (StringUtils.isNotBlank(response.getError())) {
								if ("UserMigratedException".equals(response.getCause())) {
									throw new UserMigratedException(response.getErrorMessage());
								} else if ("ForbiddenOperationException".equals(response.getError())) {
									throw new InvalidCredentialsException(response.getErrorMessage());
								} else if ("InsufficientPrivilegesException".equals(response.getError())) {
									throw new InsufficientPrivilegesException(response.getErrorMessage());
								} else if ("multiplayer.access.banned".equals(response.getError())) {
									throw new UserBannedException();
								} else {
									throw new AuthenticationException(response.getErrorMessage());
								}
							}

							if (response.getId() != null) {
								final GameProfile result = new GameProfile(response.getId(), response.getName());
								if (response.getProperties() != null)
									result.getProperties().putAll(response.getProperties());
								GAME_PROFILES.add(result);
							}
						} catch (IllegalArgumentException e) {
							TwilightForestMod.LOGGER.error("\"{}\" is not a valid UUID!", stringUUID);
						} catch (AuthenticationException | IOException e) {
							e.printStackTrace();
						}
					}
					super.run();
				}
			}.start();
		}
	}
}
