# Upstream: Mystical Automation (1.20.1 port base)

This directory started as a plain source snapshot (`git archive`, no nested `.git`) of Mystical
Automation, an addon for Mystical Agriculture that adds machines for infusion, crafting, farming,
fertilizing and the altars. Upstream only builds it for NeoForge 1.21.1 and newer. SkyTekx ports it
to Forge 1.20.1, against the Mystical Agriculture and Cucumber builds that the pack already ships.

| Field | Value |
|---|---|
| Repository | https://github.com/BlakeBr0/MysticalAutomation |
| Ref | Branch `1.21` (upstream has no git tags) |
| Commit | `4149601b9c443eb7de5ebab6ecc9b6bdd7d6462d` ("Release 1.0.8") |
| Commit date | 2026-10-02T20:10:48-05:00 |
| Upstream version | `1.21.1-1.0.8` (NeoForge 21.1.18, Parchment 2024.07.28, Cucumber 8.0.16, Mystical Agriculture 8.0.19, Mystical Agradditions 8.0.7, JEI 19.16.1.156) |
| Released as | Modrinth version `HdlBF75n`, file `MysticalAutomation-1.21.1-1.0.8.jar`, SHA-1 `586be8e7f3fedc700e4bbf845a55c4758f12db7e` |
| Not used | Branch `26.1` (Minecraft 26.1, release line 2.0.x) |

The commit is the head of branch `1.21`. Every resource in the released 1.0.8 jar is byte identical
to `src/main/resources` at this commit, apart from the expanded `neoforge.mods.toml` (license review
of 2026-11-05).

Apart from this file, every file of the first snapshot commit is byte identical to `git archive
4149601` of the upstream repository (187 files, blob hashes and file modes checked). Changes on top of it are separate commits
in this repository's history (`git log -- ports/mysticalautomation`).

## License

- Code and assets: MIT License, `LICENSE` at the ref ("Copyright (c) 2025 BlakeBr0"), kept
  unchanged. Git history shows the file only in the initial commit `b9ae7d9` (2025-05-14). The
  README links it, `neoforge.mods.toml` says `license="MIT"`, and the Modrinth project
  `mystical-automation` (id `wtZsRhqk`) declares MIT.
- No Java file carries a license header or a third party attribution. There is no NOTICE, no
  separate asset license and no contributor agreement.
- The only commits by others are three translations merged by pull request (`zh_cn` in `f3237f9`,
  `pt_br` in `cb997b5`, `ja_jp` in `24b75e1`). They fall under the repository's MIT license through
  GitHub's inbound equals outbound terms (not separately confirmed).
- The 21 PNG textures carry Photoshop metadata from 2025 and no rights fields. No 16x16 block
  texture matches a vanilla texture. The side textures are references to
  `mysticalagriculture:block/machine_side`, which Mystical Agriculture 7.0.24 has as well. The GUI
  backgrounds were not compared pixel by pixel with vanilla (not verified).
- The CurseForge license field (project 1265613) could not be read, because CurseForge answered 403
  (not verified). Every other source says MIT.
- The Gradle wrapper scripts `gradlew` and `gradlew.bat` carry their Apache 2.0 headers. They do not
  go into the mod jar.

The dependencies Cucumber, Mystical Agriculture and Mystical Agradditions are MIT ("Copyright (c)
2018 BlakeBr0") and stay CDN entries of `modrinth.index.json`.

## Build

Upstream builds with NeoForge ModDevGradle 2.0.83 on Java 21 for NeoForge 21.1.18. The port replaces
that build with ForgeGradle for Forge 1.20.1 on Java 17.

Build check (from this directory):

```
JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew build -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain -q
```

The jar is `build/libs/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar`, which `reobfJar` rewrites in
place with SRG names. That jar is the one to ship.

- Gradle wrapper: `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` are the copies
  that the Twilight Forest, Create Crafts & Additions and Actually Additions ports use (wrapper jar
  SHA-256 `33ad4583...`, scripts with their Apache 2.0 headers). Upstream's wrapper is Gradle 8.9 for
  ModDevGradle. `gradle-wrapper.properties` names Gradle 8.1.1 and pins `distributionSha256Sum` to
  the official checksum of `gradle-8.1.1-bin.zip`.
- Plugins: `net.minecraftforge.gradle` 6.0.54 and `org.parchmentmc.librarian.forgegradle`
  1.2.0.7-dev-SNAPSHOT, pinned like in `../actuallyadditions` and `../enderio`. Forge 47.4.0 userdev
  with Parchment 2023.06.26, as in `../createaddition`, so the ForgeGradle caches are shared. The pack
  runs Forge 47.4.18.
- Dependencies: Cucumber 1.20.1-7.0.16, Mystical Agriculture 1.20.1-7.0.24 and Mystical Agradditions
  1.20.1-7.0.12 from maven.blakesmods.com, the builds of the pack's `modrinth.index.json`. The task
  `verifyPackJars` checks their SHA-1 against the index before `compileJava`. JEI 15.74.0.233, the
  build of the pack, is on the compile classpath with its API only.
- Project name: `settings.gradle` sets `rootProject.name = 'mysticalautomation'`.
- Version: `mod_version=1.0.8-skytekx.1`. The jar keeps upstream's name form
  `MysticalAutomation-<minecraft>-<version>`. Forge shows the version `1.0.8-skytekx.1`.
- Reproducible jar: the `jar` task sets `preserveFileTimestamps = false` and
  `reproducibleFileOrder = true`, and the manifest has no timestamp.
- License files in the jar: the `jar` task adds `LICENSE` (upstream's, unchanged) and `NOTICE`
  (SkyTekx's) to the jar root as `LICENSE_mysticalautomation` and `NOTICE_mysticalautomation`. MIT
  asks for the license text in every copy, and the official 1.0.8 jar carries none. The manifest's
  `Implementation-Vendor` reads `Mystical Automation (modified build: SkyTekx)`.
- Mod metadata: `META-INF/neoforge.mods.toml` became `META-INF/mods.toml` for Forge 47. It keeps
  `license="MIT"`, the mod id, the display name, the authors and the description, and adds a line
  that the build is unofficial. The field `issueTrackerURL` points to
  https://github.com/skytekx/skytekx3-1201-server/issues, `credits` names the unofficial port, and
  upstream's `updateJSONURL` (api.blakesmods.com, 1.21 updates) is gone. The dependencies are Forge
  47 or newer, Minecraft 1.20.1, Cucumber 7.0.16 up to 8 and Mystical Agriculture 7.0.24 up to 8,
  each loaded before this mod.
- Ignored files: the fork's `.gitignore` also ignores `*.log` and `.mixin.out`.

## Port to Forge 1.20.1

The port keeps upstream's packages, class names, registry ids, NBT keys, slot layouts, machine numbers
and recipe ids. Lines that differ from upstream carry a `SkyTekx port` comment that names the reason.

### API translation

| Upstream (NeoForge 1.21.1) | Port (Forge 47, Cucumber 7.0.16, Mystical Agriculture 7.0.24) |
|---|---|
| `DeferredRegister` with `DeferredHolder` | Forge `DeferredRegister` with `RegistryObject` |
| Block capabilities in `RegisterCapabilityHandler` | `BaseMachineTileEntity#getCapability` with `LazyOptional`s, the same sides (UP, DOWN, every other side like NORTH) |
| `CustomPacketPayload` and `StreamCodec`, registrar version "1" | A `SimpleChannel` `mysticalautomation:main` with protocol version "1", strict on both sides |
| `RegisterMenuScreensEvent` | `MenuScreens.register` in `FMLClientSetupEvent`, registered on the client dist only |
| `IContainerFactory` menus, `Player#openMenu(provider, pos)` | `IForgeMenuType.create` and `NetworkHooks.openScreen` |
| `ModConfigSpec`, config type STARTUP | `ForgeConfigSpec`, config type COMMON, same file name `mysticalautomation-common.toml` |
| `useItemOn`, `ItemInteractionResult` | `Block#use`, `InteractionResult` |
| `loadAdditional` and `saveAdditional` with `HolderLookup.Provider` | `load` and `saveAdditional` without it. A missing `Energy` tag leaves the energy at 0 instead of throwing |
| `Item.TooltipContext` | `BlockGetter`, the tooltip method marked `@OnlyIn(Dist.CLIENT)` as in Mystical Agriculture 7 |
| `ItemStack#getBurnTime`, `Math.clamp` | `ForgeHooks#getBurnTime`, `Mth#clamp` |
| `ResourceLocation.fromNamespaceAndPath`, `DeferredHolder.create` lookups | `new ResourceLocation`, `RegistryObject.create` lookups |
| `RecipeHolder`, `RecipeInput`, `CraftingInput` | Recipes that carry their id, `Container` inputs. The Crafter matches vanilla crafting recipes through `inventory/RecipeGrid`, a `CraftingContainer` view of its ghost grid |
| Codec and StreamCodec of `FarmerRecipe` | `RecipeSerializer#fromJson`, `fromNetwork` and `toNetwork`. The JSON keys stay upstream's |

### Mystical Agriculture 7 and Cucumber 7 in place of 8

- Machine upgrades: the machines implement Mystical Agriculture 7's `util.IUpgradeableMachine` and
  keep its `container.inventory.UpgradeItemStackHandler`, so the Machine Upgrades of 7.0.24 apply to
  them with a right click and fit their upgrade slot. Mystical Agriculture 8 moved these classes to
  `api.machine` and added `MachineUpgradeTier#getOperationTime`, `getFuelUsage` and
  `getFuelCapacity`. The port scales the base values in `util/MachineUpgrades` the way Mystical
  Agriculture 7's own Reprocessor does. No Mystical Agriculture 8 code is copied.
- Altar recipes: Mystical Agriculture 7's `IInfusionRecipe` and `IAwakeningRecipe` have no
  `getAltarIngredient`. Their `getIngredients` already start with the altar ingredient, the list that
  upstream builds from both methods, so the Altarnators count their inputs from it unchanged. The
  ghost grid goes to the recipe as a nine slot container with the altar item in slot 0, which
  Mystical Agriculture 7 tests before it matches the rest in any order.
- Enchanter recipes: Mystical Agriculture 7's `IEnchanterRecipe` lacks `getCount`,
  `getMaxResultEnchantmentLevel` and the `assemble` and `getRemainingItems` variants with a level, and
  its recipes match their two ingredients in a fixed order. `util/EnchanterRecipes` rebuilds these
  from `getIngredientCounts` and the recipe's own `assemble`, and looks the recipe up with the two
  ingredients in either order, as version 8 matches them.
- Cucumber 8 classes that Cucumber 7 lacks: `QuickMover` (copied unchanged to
  `com.blakebr0.mysticalautomation.util`), `ContainerDataBuilder` (see below), `CachedRecipe` for any
  input type (`util/CachedRecipe`, since Cucumber 7's takes only `Recipe<Container>`),
  `SidedInventoryWrapper` (Cucumber 7's `SidedItemStackHandlerWrapper` takes its place), the Clearable
  part of `BaseInventoryTileEntity` with `clearAdditional` (`tileentity/BaseMachineTileEntity`),
  `StackHelper#shrinkAndRetainContainer` (a private copy in the Enchanternator), `FuelWidget` and
  `ProgressArrowWidget` (see below) and `RecipeManagerLoadedEvent`. The ingredient cache of the
  Farmer slots is built on `TagsUpdatedEvent` with the cause `SERVER_DATA_LOAD`, as Mystical
  Agriculture 7 builds its own, from the recipe manager that Cucumber's `RecipeHelper` took from the
  same reload. The copies live in this mod's packages, because Forge's module layer rejects packages
  split across two jars. `LICENSE-Cucumber` carries their MIT notice.
- Tooltips: Mystical Agriculture 7 has no `tooltip.mysticalagriculture.chance`,
  `machine_fuel_usage` or `machine_scan_fuel_usage`. The port uses the keys
  `tooltip.mysticalautomation.chance`, `machine_fuel_usage` and `machine_scan_fuel_usage` with the
  texts of Mystical Agriculture 8 in every language file of this mod. `LICENSE-MysticalAgriculture`
  carries the MIT notice for these strings.
- Dynamic Farmer recipes: Mystical Agriculture 7's API has no `getConfigValues`, so the chance of
  Fertilized Essence comes from its config value `FERTILIZED_ESSENCE_DROP_CHANCE` directly. Cucumber
  7's `RecipeManagerLoadingEvent` adds the recipes, as upstream.

### Minecraft 1.20.1 limits

- Container data: 1.20.1 sends container data slots as shorts (`ClientboundContainerSetDataPacket`
  writes `writeShort`, checked in the Forge 47.4.0 jar), and Forge 47 has no NeoForge style payload
  for larger values. Energy, capacity and fuel of the machines exceed 32767, so the port's
  `ContainerDataBuilder` sends every value as two 16 bit halves, and the menus join them again.
- Fuel and arrow widgets: Cucumber 8 draws them from its widgets texture, whose flame and arrow sprites
  are pixel copies of the vanilla furnace sprites (compared with the vanilla 1.20.1 `furnace.png`,
  no pixel differs in the lit flame and the full arrow). Cucumber 7's widgets texture has neither.
  The GUI backgrounds of this mod already show the unlit flame and the empty arrow (no pixel differs
  from Cucumber 8's empty sprites), so the port's widgets draw only the lit part, straight from the
  vanilla furnace texture at runtime. No vanilla texture is copied into the jar. Cucumber 7's energy
  bar takes an `IEnergyStorage`, which `EnergyStorageView` builds from the synced values, and the
  port makes it unclickable, as Cucumber 8 does.
- Slot counts: 1.20.1 writes the count of a slot stack as a byte (`FriendlyByteBuf#writeItemStack`
  calls `writeByte`, checked in the Forge 47.4.0 jar), 1.21 as a VarInt. The input slots of the
  Infuser and the Enchanternator hold up to 512 items, and a count above 127 would reach the client as
  a negative or wrong number, so the slot would look empty. Their menus wrap the vanilla
  `ContainerSynchronizer` (`container/LargeStackSynchronizer`), which sends such a slot again with its
  full count (`SyncSlotCountPayload`) right after the vanilla packet. The client handler sits in
  `client/ClientPayloadHandlers`, which a dedicated server never loads. Hoppers and pipes still fill
  these slots to 64 only, as upstream, since Forge's `ItemStackHandler` caps automated insertion at
  the item's stack size.
- Farmer input: a 1.21 `CraftingInput` drops empty rows at its end, so upstream compares its size
  with the number of ingredients. The 1.20.1 container keeps the three slots, so `FarmerRecipe`
  counts the filled ones. It also reports `isSpecial`, which keeps the Farmer recipes out of the
  vanilla recipe book, which has no category for them and would log a warning for each.

### Data

- Folders: `recipe` became `recipes`, `loot_table` became `loot_tables`, `tags/item` became
  `tags/items` and `tags/block` became `tags/blocks`, the 1.20.1 names. Blockstates, models,
  textures, the Patchouli entries and the loot tables keep their content, since 1.20.1 reads the same
  forms. Mystical Agriculture 7.0.24 has every texture, item, block and book entry they name (checked
  against its jar).
- Recipes: `neoforge:conditions` became `conditions`, `neoforge:mod_loaded` became
  `forge:mod_loaded`, and Cucumber 7 reads the `cucumber:feature_flag` condition as Cucumber 8 does.
  Crafting results name their item with `item` instead of `id`, and so do the result stacks of the
  Farmer recipes. The common tags `c:ingots/iron` and `c:gems/diamond` became `forge:ingots/iron` and
  `forge:gems/diamond`, the forms AlmostUnified works with. Forge 47 has no tag for crafting tables,
  so the Crafter recipe asks for `minecraft:crafting_table`, the only entry NeoForge puts into
  `c:player_workstations/crafting_tables`.
- Tag `mysticalautomation:fertilizers`: upstream lists `#c:fertilizers`, which NeoForge fills with
  bone meal and Mystical Agriculture 8 with Fertilized Essence and Mystical Fertilizer. Forge 47 has
  no such common tag, so the port lists these three items and adds `#forge:fertilizers` as an
  optional entry for mods that define it.
- `pack.mcmeta` with `pack_format` 15, as in the Mystical Agriculture 7 jar.
- Translations: the three tooltip keys above, with the texts of Mystical Agriculture 8 in `en_us`,
  `ja_jp` and `pt_br`. Its `zh_cn` has no text for them, so Chinese clients show the English one,
  as they do upstream.

### Fixes over upstream

Upstream 1.0.8 behaves the same in these points on NeoForge. The fixes are SkyTekx's, since the pack
runs on a public server. Each is marked `SkyTekx:` in the source.

- Payloads: `SetFakeRecipeSlotPayload` set any slot of the open menu to the stack the client sent,
  and it checked only the upper bound of the index (upstream `9777e82`). A modified client could put
  any item into the player inventory or a machine input that way. Now only a ghost slot (`FakeSlot`)
  of a menu that the player may still use takes the stack. `InfuserSelectIndexPayload` and
  `EnchanternatorSelectLevelPayload` changed the machine at any position the client named, and
  loaded its chunk to find it. Now only the machine whose menu the player has open takes the change.

- Enchanternator: upstream enchanted at the highest level that the inputs allow, took the
  ingredients of one level only and kept the item to enchant, so every operation copied the item and
  gave the top level for the price of level 1. When the item already had the enchantment at that
  level, it still took the inputs and gave nothing. Now it enchants at the selected level, takes the
  ingredient counts of that level, as Mystical Agriculture's Enchanter does, takes the item to
  enchant, and does nothing when the recipe gives no result. The 26.1 line of upstream takes the item
  to enchant since `b100f44` but still enchants at the highest level.

- JEI recipe transfer: the four transfer handlers stopped at `index > views.size()` and then read
  `views.get(index)`, one step past the end for a recipe with fewer inputs than ghost slots, such as
  an Infusion Altar recipe with fewer than eight pedestals. They now clear the ghost slots past the
  inputs of the recipe.
- Fertilizer fuel: vanilla bone meal answers `CONSUME` on the server and the fertilizers of Mystical
  Agriculture answer `SUCCESS`. Upstream charged the full fuel usage for `SUCCESS` only, so bone meal
  cost the scan rate. Every use that consumes the item now costs the full fuel usage.

### Left out

- CraftTweaker integration (`compat/crafttweaker/FarmerCrafting.java`) and the CraftTweaker test
  scripts and test datapack under `src/test`. CraftTweaker is not in the pack, the build runs no
  game tests, and the test datapack is in the 1.21 format. Datapacks and KubeJS (`event.custom`) add
  Farmer recipes with the JSON form above.
