---
type: Mod
title: Expanded Delight port
description: The Forge 1.20.1 port of Expanded Delight 0.1.4 for NeoForge 1.21.1 that the pack bundles, what it adds, which upstream files stay out for license reasons, the fixes of the port and of its review, how its tags meet Farmer's Delight, Pam's Food Core and Mekanism, the worldgen and Skylands check, the tests of 2026-11-05 on Forge and Arclight, the fox food fix of 2026-11-06 and the operator steps.
tags: [modpack, mods, expandeddelight, farmersdelight, port]
timestamp: 2026-11-06
---

# Expanded Delight port

Expanded Delight is an addon for Farmer's Delight with cinnamon trees and a cinnamon wood set,
asparagus, sweet potatoes, chili peppers, peanuts and cranberries, a juicer, casks that ferment milk
into cheese wheels, salt and many dishes. Its author builds it for NeoForge 1.21.1 and Fabric only.
The 1.20.1 build on Modrinth is an older Fabric line with 44 items. So the pack bundles a port of
the newest NeoForge line to Forge 1.20.1, built from `ports/expandeddelight/`.

| Property | Value |
|---|---|
| Jar | File [local-mods/expandeddelight-0.1.4-skytekx.1.jar](../../local-mods/expandeddelight-0.1.4-skytekx.1.jar), 486,648 bytes, SHA-1 `2036aa13e77937afc7ba69b7f1a7c9ce8e9b21be` since f655eec, the build of 99183d0 with the fox food fix. The build of 2428130 with the review fixes (047ff2d) had 486,426 bytes and SHA-1 `45b288dd96173e620db5a6d1065250a96b584949`, the first bundled build of d9eb6cc had 485,909 bytes and SHA-1 `3e156033dbf6a5a09d199960614776023e15e69a` |
| Mod | The id stays `expandeddelight`, the version is `0.1.4-skytekx.1` and the authors field names the original author |
| Upstream | Branch `1.21-neo` of github.com/ianm1647/expandeddelight at `ea826400654702587e86c9e94544bfd74f065083` (2026-08-25, 0.1.4). The 1.20 branch at `8d8fe44d` served as the reference for the 1.20.1 game API |
| Needs | Farmer's Delight 1.20.1-1.3.4 or newer, which the index has (`modrinth.index.json`) |
| Sides | Required on both sides. The mod registers blocks, items, two boats, a menu and a recipe type |
| Source | Folder [ports/expandeddelight/](../../ports/expandeddelight), whose `UPSTREAM.md` lists every change and `NOTICE` the credits |
| Build | ForgeGradle 6.0.21, Parchment `2023.07.30-1.20.1`, Forge 47.1.70 as the NeoForged artifact and Java 17, like [SkyTekx DE Fixes](draconic-fixes.md). The jar is byte reproducible |
| License | MIT, with the MIT text of Farmer's Delight for the parts that follow it, see [license](#license) |

## Content

The port carries the whole content set of 0.1.4: 99 items and 45 blocks (`EDItems.java`,
`EDBlocks.java` in the port).

- The cinnamon wood set with logs, wood, planks, stairs, slab, fence, gate, door, trapdoor, pressure
  plate, button, signs, hanging signs, a cabinet in the style of Farmer's Delight and two boats.
  Stripping a cinnamon log with an axe gives two cinnamon sticks, cinnamon wood three.
- The crops asparagus, sweet potato, chili pepper and peanut, their wild forms, and the cranberry, a
  plant that grows on water like a lily pad. Crates and a cranberry bag store them.
- The juicer, a block with a menu and a recipe book that presses juices and jellies into bottles and
  jars. Five juicing recipes come with the mod.
- The cask. A milk bucket or a goat milk bucket turns it into a milk cask, which ferments by itself
  into a cheese cask, faster next to mushrooms and mycelium. Using the cheese cask gives two cheese
  or goat cheese wheels, which a knife cuts into slices.
- Goat milk in buckets and bottles from goats, salt ore in ocean biomes, salt and cinnamon dust from a
  crushing mallet on the cutting board, and 17 cooking pot dishes, sandwiches, cookies, pies and salads.
- Villager trades for farmers and the wandering trader, extra loot in village houses, a JEI plugin
  with the juicing and the cheese fermenting categories, and a KubeJS schema
  `event.recipes.expandeddelight.juicing(ingredients, result, experience, juicingtime, container)`.

## License

Expanded Delight declares MIT in its mods.toml, on Modrinth (project `e9V6wFcR`) and on CurseForge,
and its 1.20 branch carries a CC0 LICENSE. The license review of 2026-11-05 found material of
Farmer's Respite, which is All Rights Reserved, in the 1.21-neo tree and set conditions, which the
record [expandeddelight-0.1.4-skytekx.1.jar.md](../../modrinth-permissions/expandeddelight-0.1.4-skytekx.1.jar.md)
lists one by one.

- The juicer GUI sheets `textures/gui/juicer.png` and `textures/gui/jei/juicer.png`, built on the
  kettle of Farmer's Respite, the bottle slot icon copied from it and the menu class `JuicerMenu.java`,
  decompiled from its KettleMenu, never entered git. The vendoring commit eb734ad left them out.
- The juicer draws the author's own sheet `juicer_gui.png` of the 1.20 branch (commit 50e9c10, 2022)
  instead, and the port's `JuicerMenu` is written from the CookingPotMenu of Farmer's Delight, which is
  MIT, with the slot positions of that sheet.
- Three unused textures that copy vanilla or Farmer's Delight art stayed out as well.
- The jar carries the author's MIT text, the CC0 text, the MIT text of Farmer's Delight, the NeoForged
  MDK template license and a NOTICE that calls the build unofficial and names the upstream commits.
  The same files lie in [licenses/expandeddelight-0.1.4-skytekx.1/](../../licenses/expandeddelight-0.1.4-skytekx.1),
  which the pack ships as `overrides/licenses/`.
- MIT and CC0 do not ask for the source. The pack's gate does, because the record links
  `tree/master/ports/expandeddelight`: `tools/mrpack_permissions.py` refuses the jar until
  `origin/master` holds that folder and this build, see [publish](publish.md#release-steps).

## What the port changes

The file [ports/expandeddelight/UPSTREAM.md](../../ports/expandeddelight/UPSTREAM.md) lists each change
with its reason. In short:

- The 1.21.1 API became the Forge 1.20.1 one: item stack NBT instead of data components, the juicer's
  item handler through `getCapability`, Forge's `DeferredRegister`, a hand written recipe serializer,
  and `IExtensibleEnum` for the recipe book.
- The data moved to the 1.20.1 folder names and formats through the script
  `ports/expandeddelight/tools/convert_data_to_1_20_1.py`, and the c: tags became forge: tags.
- Upstream's mixin into `Goat#mobInteract` is gone. The port cancels Forge's interaction event when it
  milks a goat, so no mixin touches a class that Bukkit patches on Arclight. Log stripping goes
  through Forge's tool modification event, so the axe's own path does the work (sound, damage,
  statistics, axis). On Arclight no Bukkit event covers axe stripping at all, since the pack's
  `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar` has no mixin into `AxeItem` (review of
  2026-11-05, javadoc fixed in f409828). Cranberries plant through a vanilla
  `PlaceOnWaterBlockItem`, so Bukkit's `BlockPlaceEvent` covers them.
- Upstream bugs are fixed: crops that named themselves as seed, a juicer drink taken by hand that was
  neither saved nor synced, a chest boat that lost its items at every save, and the crushing mallet,
  whose stack size 1.20.1 refuses.
- The review of 2026-11-05 added four fixes over upstream. The juicer registers its default state
  in the constructor that the registry uses (3393d44), so `/setblock`, `/fill` and mods that place
  `defaultBlockState()` no longer make a waterlogged juicer. Seven language keys that showed raw got
  English names (9306195): the cranberry jelly sandwich, the root advancement, the two boats and the
  two wall signs. Hoppers and pipes on the sides put only valid containers into the juicer's
  container slot (c99d6cf), the rule of shift clicking in the menu. The peanut joined `forge:nuts`
  (2428130).
- Cranberries are fox food again (99183d0). Upstream adds them to the vanilla tag
  `minecraft:fox_food`, but the data conversion of 36ce58e dropped that file with the chicken, parrot
  and pig food tags, on the claim that all four need Minecraft 1.20.5. Only those three do. The 1.20.1
  client jar ships `data/minecraft/tags/items/fox_food.json` with sweet and glow berries, and javap of
  the Forge 47 `Fox` class shows that `isFood` reads `ItemTags.FOX_FOOD`. The port adds no chicken,
  parrot or pig food, so its asparagus and chili pepper seeds feed no chicken.
- Upstream's overrides of Farmer's Delight's dough and melon juice recipes stay out. The pack edits
  no recipes of other mods per mod, and on 1.20.1 those recipes have other ids.

## Pack fit

The port puts its items into the forge: tags that Farmer's Delight 1.20.1 and Pam's HarvestCraft 2
Food Core read: `forge:cheese`, `forge:milk` (goat milk), `forge:vegetables` (asparagus, sweet potato
and chili pepper), `forge:cookies`, `forge:crops/*`, `forge:dusts/salt` and `forge:ores/salt`. Pam's
recipes ask for `forge:cheese` seven times and `forge:milk` eleven times (counted in the recipes of
`pamhc2foodcore-1.20.4-1.0.5.jar`), so they take the new cheese and goat milk without glue. Since
2428130 the parent tag `forge:nuts` holds `#forge:nuts/peanut`, so Pam's cooking oil recipe
`cookingoilitem_x2_nuts` takes peanuts next to its sunflower seeds.

Since d8f433e two more layers tie it to the pack.

- The file [tag_fixes.js](../../kubejs/server_scripts/tag_fixes.js) puts every `forge:dusts/salt` into
  Pam's `forge:salt`, Pam's salt into `forge:dusts/salt`, the apple, sweet berry and glow berry juices
  and the two berry jellies of each mod into the tags of the other (`forge:juices/applejuice` and
  `forge:juices/apple`, `forge:jellies/sweetberryjelly` and `forge:jams/sweet_berry`, and so on). It
  names items of a mod only while that mod is loaded.
- [AlmostUnified](almostunified.md) has salt as a material, so `forge:dusts/salt` keeps one item, the
  Mekanism salt by the mod priority list. The salt of Expanded Delight and of Pam's mod still drop
  from salt rock and Pam's recipes, then recipes give the Mekanism salt and `unify_inventory.js` swaps
  the others in the inventories.

No other pack mod adds asparagus, sweet potatoes, chili peppers, peanuts, cranberries or cinnamon (the
index has no Thermal Cultivation, Croptopia or Pam's crops), so the crops need no unification.

## Worldgen and the Skylands

The biome modifiers add the cinnamon tree and the wild sweet potato to `#minecraft:is_jungle`, the wild
asparagus to `#forge:is_plains`, the wild chili pepper to `#forge:is_desert`, the wild peanuts to
`#minecraft:is_savanna`, the cranberries to `#forge:is_swamp` and the salt ore to
`#minecraft:is_ocean`. None of these tags holds `skytekx3:sky_void` or another biome of the Skylands
(the Skylands packs add their biomes only to the two `is_overworld` tags,
`skylands-datapack/data/*/tags/worldgen/biome/is_overworld.json`), so nothing of the mod generates in
the Skylands. The wild crop and cranberry patches also keep their `farmersdelight:biome_tag` filter for
`#minecraft:is_overworld`. Whether the islands should grow these crops too is open, see
[open items](../open-items.md).

The features only reach chunks that do not exist yet. In the current worlds they appear in new
terrain only.

## Test of 2026-11-05

These are the tests of the port with the first jar of d9eb6cc. The integration tests of the rebuilt
jar follow in [integration test](#integration-test).

A plain Forge 47.4.18 server with the jar, Farmer's Delight 1.20.1-1.3.4, JEI 15.74.0.233, KubeJS
2001.6.5 with Rhino and Architectury, all as the files of the index, `-Xmx3G`. The test server and its
logs stay in the session's scratch directory.

- The first boot stopped in the item registry: the crushing mallet asked for durability and a stack
  size, which 1.20.1 refuses. The fix is 86f6702. The second boot reached `Done` in 32.3 s with no
  ERROR or FATAL line, and the server stopped over RCON with exit code 0.
- All 99 recipe files of the jar loaded: 27 shapeless and 23 shaped crafting recipes, 3 smelting, 2
  blasting, 1 smoking and 2 campfire recipes, 17 cooking pot, 17 cutting board and 5 juicing recipes
  under `expandeddelight`, and 2 cutting board recipes under `minecraft`. The KubeJS schema added 2
  juicing recipes of a test script.
- Every one of the 99 items went into chests with `item replace` over RCON, and every one of the 45
  blocks stood after `setblock`, the cranberry plant on water and the door with both halves. The
  juicer, the signs, the hanging signs and the cabinet saved their block entities.
- Hoppers fed a juicer two apples, two sugar and two bottles, and the hopper below held two apple
  juices after 45 s, with the recipe counted in `RecipesUsed`. A juicer with melon juice inside dropped
  itself with the juice and its bottle in `BlockEntityTag`.
- Dispensed bone meal grew all four crops to age 7, the cranberries to age 3 and the sapling into a
  cinnamon tree. Random ticks, called through a KubeJS test command because forceloaded chunks
  without a player get none, fermented the milk cask and the goat milk cask into cheese casks.
- Chunks in a jungle, plains, desert, savanna, swamp and ocean (1,209 full chunks) held 77 cinnamon
  logs, 66 wild asparagus, 9 wild chili peppers, 137 wild peanuts, 21 wild sweet potatoes, 1,283 salt
  ores and 69 deepslate salt ores. The swamp area had only four swamp chunks and no cranberry patch,
  so `place feature expandeddelight:patch_cranberries` on a test pool placed 19 cranberry plants.
- The class load log of the dedicated server lists no class of the mod's `client` and `integration.jei`
  packages.
- A second boot added Mekanism 10.4.16.80, Pam's Food Core 1.0.5, AlmostUnified 0.11.0 and the pack's
  `unify.json` and tag glue. The merged tags held the items of all three mods, AlmostUnified replaced
  the salt of Expanded Delight and of Pam's mod with the Mekanism salt, and the salt rock recipe gave
  Mekanism salt. Its only ERROR lines were two of Mekanism about the ethene of Mekanism Generators,
  which that test lacked.

## Integration test

The jar of 047ff2d (SHA-1 `45b288dd`) on the same plain Forge 47.4.18 server, with Farmer's Delight,
JEI, KubeJS with Rhino and Architectury, Mekanism 10.4.16.80, Pam's Food Core 1.0.5, AlmostUnified
0.11.0 and the pack's tag glue, `-Xmx3G`. The logs stay in the session's scratch directory
(`bp2/nm/expandeddelight/server/console-int.log`).

- The boot reached `Done` in 9.0 s with 0 ERROR and 0 FATAL lines and stopped over RCON with exit
  code 0.
- The command `setblock 0 200 0 expandeddelight:juicer` gave a juicer that passed
  `execute if block 0 200 0 expandeddelight:juicer[facing=north,waterlogged=false]`.
- A hopper west of the juicer, facing it with four apples and three glass bottles, handed over the
  bottles into slot 3 and kept its apples. A hopper above it handed two apples into slot 0.
- A KubeJS check logged `forge:nuts=[expandeddelight:peanut, pamhc2foodcore:sunflowerseedsitem]`.

An Arclight copy with the repo's hardened `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar` (SHA-1
`70b44a84`) and the same mods and scripts, `-Xmx3G`, no plugin
(`bp2/nm/expandeddelight/arcserver/console-int.log`):

- The boot reached `Done` in 68.5 s with 0 ERROR and 0 FATAL lines and no failed mixin, KubeJS
  loaded 6 of 6 scripts with 0 errors, and the server stopped over RCON with exit code 0.
- The juicer from `/setblock` passed the same state check. Hoppers fed it two apples and two sugar
  from above and glass bottles from the side, the side hopper kept its four apples, and the hopper
  below received an apple juice, with `expandeddelight:juicing/apple_juice` counted in
  `RecipesUsed`.
- The tag check logged the same `forge:nuts` content.

Not verified: a client (the juicer screen, its recipe book, JEI, the boat, sign and tooltip rendering,
and the seven new names) and the hand interactions that need a player: milking a goat, filling and
emptying a cask, planting cranberries and stripping logs. The full pack of 98bf164 with the jar of
047ff2d booted on Forge 47.4.18 and on Arclight on 2026-11-06, see
[NeoForge ports](neoforge-ports.md#status).

### Fox food test of 2026-11-06

The jar of f655eec (SHA-1 `2036aa13`) on a plain Forge 47.4.18 server with only Farmer's Delight
1.20.1-1.3.4, `-Xmx3G` (`bp2/nm/newmods-fix-ed/server/console.log` in the session's scratch
directory). The boot reached `Done` in 49.0 s with 0 ERROR and 0 FATAL lines, and the server stopped
over RCON with exit code 0. The command `forge tags minecraft:item get minecraft:fox_food` listed
`expandeddelight:cranberries`, `minecraft:glow_berries` and `minecraft:sweet_berries`, and
`forge tags minecraft:item query expandeddelight:cranberries` named `forge:berries`,
`minecraft:fox_food` and `sereneseasons:autumn_crops`. The tag `minecraft:chicken_food` stayed unknown,
as in vanilla 1.20.1. Nobody fed or bred a fox by hand, which needs a player.

## Operator notes

The owner mirrors the server by hand, and the mod goes live with the next pack, because clients
without it cannot join.

- Server and clients update together, the mod is required on both sides. Clients need the same jar,
  which the next pack brings them.
- Stop the server over RCON and wait until the java process is gone. Then copy
  `local-mods/expandeddelight-0.1.4-skytekx.1.jar` into the server's `mods/` by hand and check its
  SHA-1 against `2036aa13e77937afc7ba69b7f1a7c9ce8e9b21be`. Farmer's Delight 1.20.1-1.3.4 is there
  already. On the Mac mini `macos/sync-to-mac.sh` mirrors `mods/` from the build machine.
- The first boot writes `config/expandeddelight-common.toml` with the recipe book switch of the juicer.
  Commit it before `tools/build_mrpack.py`, which takes overrides from git HEAD.
- The tag glue and the salt material come with git (`kubejs/server_scripts/tag_fixes.js`,
  `config/almostunified/unify.json`). A `/reload` applies the tags, AlmostUnified needs a restart.
- A clean boot keeps the accepted baseline of ERROR lines, see
  [logs and crashes](../server/logs-and-crashes.md#boot-error-baseline).
- The cinnamon trees, wild crops, cranberries and salt ore only generate in chunks that do not exist
  yet, so players find them beyond the explored area. A pregen with Chunky places them ahead of time.
- Before a public pack, merge and push the branch with `ports/expandeddelight/` to master and fetch,
  see [publish](publish.md).

Related: [NeoForge ports](neoforge-ports.md#expanded-delight), [mod set](mod-set.md#bundled-jars),
[AlmostUnified](almostunified.md), [KubeJS](kubejs.md), [publish](publish.md).
