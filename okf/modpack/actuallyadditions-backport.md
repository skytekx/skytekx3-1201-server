---
type: Plan
title: Actually Additions backport
description: How the unreleased 1.20.1 Forge branch of Actually Additions becomes a release for the pack, which fixes and features of the newer NeoForge lines up to 1.3.26 it takes in which bundles and order, which upstream work stays out, and the license, savegame, Arclight and operator rules of the fork.
tags: [modpack, mods, actuallyadditions, backport]
timestamp: 2026-11-06
---

# Actually Additions backport

Actually Additions (mod id `actuallyadditions`) is new to the pack. Upstream never released a build
for Minecraft 1.20.1. Its branch `1.20.1` stopped at 226a439 on 2024-03-04 with `mod_version` 1.1.52
for Forge 47.1.3, and the mod went on as NeoForge builds for 1.20.4, 1.21 and 1.21.1, up to release
1.3.26 of 2026-06-19 (`ports/actuallyadditions/UPSTREAM.md`, Upstream table). This plan makes the
vendored copy of that branch in `ports/actuallyadditions/` release quality and then backports the
bug and dupe fixes, the items the branch lacks, the balance and quality of life changes and the new
models and textures of the newer lines, the same way as the
[Twilight Forest backport](twilightforest-backport.md). The fork keeps the mod id and every registry
id, NBT key, block state and saved data key of the base, so a world made with the base build loads
unchanged. Energy stays on Forge Energy. The work is done for SkyTekx, a modpack by ts4.party.

Effort is given in estimated human hours, the unit of the Twilight Forest plan. The plan holds 67
features in 14 bundles, 264.5 h in all, and no bundle is over 40 h. The machine readable plan is
`ports/actuallyadditions-backport-inventory.json`. Its `plan.bundles` holds each bundle with its
feature ids, hours, dependencies and a spec that names for every feature the upstream commits, the
files, the Forge 47 translation, the savegame notes, the new ids and the test. The list
`plan.excluded` holds 16 entries of upstream work left out with their reasons, and `critic_notes`
holds the review that corrected the first draft. The review checked that each of the 551 upstream
commits after the fork point 97532ff that is not a merge appears in a bundle spec or an exclusion. A
second check against the critic's commit list in the workflow scratchpad (`aacritic/all.txt`) found
none missing in the committed file. The file `ports/actuallyadditions-backport-carryover.json` will
record what a bundle hands to a later one. It does not exist yet, and until the release commits it
the hand overs collect in the workflow scratchpad (`bp2/actuallyadditions/carryover.json`).

| Property | Value |
|---|---|
| Upstream | The Actually Additions repository named in `ports/actuallyadditions/UPSTREAM.md`, MIT License |
| Base | Branch `1.20.1` at 226a439 (2024-03-04), `mod_version` 1.1.52 for Forge 47.1.3, never released. It registers 121 blocks and 107 further items (`UPSTREAM.md`, What the base contains) |
| Port source | Branch `main` at cd9d9c63 (2026-06-19), release 1.3.26 for NeoForge 1.21.1 |
| Intermediate steps | Branch `1.20.4` at 0ebbd811 (1.2.14, NeoForge 20.4) and branch `1.21` at d9b0a714 (1.3.6, NeoForge 21.1). The 1.20.4 port commit 63fbbb33 has the base as its parent and is the reference for every reverse mapping |
| Fork | Folder `ports/actuallyadditions/`, Java 17, ForgeGradle 6.0.54, Forge 47.1.3 userdev, Parchment 2023.09.03 (`UPSTREAM.md`, Build changes) |
| Target version | Version 1.1.52-skytekx.1 in `ActuallyAdditions-1.20.1-1.1.52-skytekx.1.jar`, the reobf jar from `build/libs/`, see [Version](#version) |
| Ships as | A new jar in `local-mods/` and `mods/`. The Modrinth project has no 1.20.1 file the index could point at |
| Build check | The `compileJava` call in `UPSTREAM.md`, run with `/opt/jdk17` and `-Xmx2g`, with upstream's compiler settings |
| Integrations | Patchouli 85 (`modrinth.index.json:2169`), Curios 5.14.1 (`modrinth.index.json:1014`), JEI 15.74.0.233 (`modrinth.index.json:1584`) and EMI 1.1.24-skytekx.2 from `local-mods/`, all optional |
| Dependents | None. Of the 264 `mods.toml` files in the Arclight pack boot copy of the workflow scratchpad (`packboot-arclight/mods/`), none names `actuallyadditions` (scan of 2026-11-05) |

## Status on 2026-11-05

The vendored base (1e786de), the reproducible Java 17 build of 1.1.52-skytekx.1 (1eccbee) and the
license and notice files in the jar (a6bcde1) are on the branch
`claude/twilight-forest-port-mods-j2owbq`. Three clean builds of that state gave the jar SHA-1
`c37140ce60ec6da5ac33ebea66b3f9fa3951f7fd` (`UPSTREAM.md`, build change 5).

The baseline bundle is done. Its five commits are 4f87826 (fork metadata and the InvTweaks
removal), 274cd3c (Patchouli, Curios and the Mixin setup), 9470a0d (the common config of `main`
and the client config), d5023b4 (the data generator fixes) and 5b06e38 (the boot smoke test), with
the review fix b285862. Two clean builds of 5b06e38 gave the jar SHA-1
`ba5e48f831da022f7eb45f763f1fdaf4db6e4794`. The review fix changes only a comment in `mods.toml`
and the text of `UPSTREAM.md`, and two clean builds of b285862 give
`342af9c288d7afb676105bf16b1466621edfa516` (4527513 bytes), which differs from `ba5e48f8` only in
`META-INF/mods.toml`.

The base jar `c37140ce` reaches `Done` alone on a dedicated Forge 47.4.18 server with 0 ERROR
lines, but it does not start in the Arclight pack. Immersive Engineering 10.2.0-183 ships the same
package `invtweaks.api.container`, and the module layer stops with a `ResolutionException` before
any mod loads (`UPSTREAM.md`, source change 8). So the OLD side of the savegame test is the base
a6bcde1 with only `src/main/java/invtweaks/` removed, jar SHA-1
`237e0fc65275a0204fe7008f144c395207e41205` (4557655 bytes). A copy and its build recipe sit in the
workflow scratchpad under `bp2/actuallyadditions/old-jar/`.

The Arclight pack boot copy of the workflow scratchpad ran three boots on 2026-11-05 with the
production jar `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar` (`packboot-logs/`, labels
`aa-ref-arclight`, `aa-old-arclight` and `aa-new-arclight`). The pack without the mod, the pack
with the OLD jar and the pack with the jar `ba5e48f8` each reached `Done` with 0 FATAL lines, no
crash report, no watchdog, a clean RCON stop and the same three ERROR signatures. They are the
AllTheLeaks line of the boot error baseline, a `RuntimeDistCleaner` line for `ModelBakery` and a
Nostalgic Tweaks line about its backup config, which every boot of the fresh test copy shows. No
boot logged a Mixin apply failure. The fork logs one WARN that the reference map
`actuallyadditions.refmap.json` could not be read, because the annotation processor writes it only
once a mixin exists. It goes away when the Item Tag filter brings the first mixin. The dev client
of the fork reaches the title screen and writes `actuallyadditions-client.toml`.

The data core bundle is done. Its four commits are 681664e (the `forge:` tags), c1c79a0 (a recipe
for every block and item of the base), 077c3cd (the advancements) and d4d05a7 (the JEI categories),
with the review fix 655b320 (`UPSTREAM.md`, source changes 9 to 12). The review fix lets the Lens
of the Killer take a real Sharpness V book and keeps the 3x3 slime block recipe off the slime balls
of Tinkers' Construct and Twilight Forest (see [Local divergences](#local-divergences)). The jar of
655b320 (SHA-1 `bb73c65ef81d17ef862f7231c28d66ebe67438d0`, 4605920 bytes, one build) reached `Done`
in 62.5 s on a dedicated Forge 47.4.18 server with AlmostUnified 0.11.0 and the pack config,
Farmer's Delight 1.3.4 and Patchouli 85, with 0 FATAL and 0 ERROR lines, a clean stop and an empty
`uncovered_recipes.txt` of AlmostUnified. Nobody has opened the JEI or EMI categories in a client
yet. This bundle makes the phantom placer and breaker, the Atomic Reconstructor and the Vertical
Digger craftable before the Arclight hardening gives them a fake player that Bukkit sees, so no jar
of this state may reach the live server.

The pack does not ship the mod yet. Most new config keys have no reader yet, because their blocks
keep reading the 1.12 enums until their features land (`UPSTREAM.md`, source change 6).

| Bundle | What it covers | Hours | Needs | Status |
|---|---|---|---|---|
| Baseline (`aa-baseline`) | Optional dependencies and pack format 15, removal of the bundled InvTweaks annotations, Patchouli and Curios at compile time, the Mixin setup, the port script, the config of `main`, the datagen baseline and the boot smoke test | 16.5 | Nothing | Done in 1e786de, 1eccbee and a6bcde1 (setup), 4f87826, 274cd3c, 9470a0d, d5023b4 and 5b06e38, review fix b285862 |
| Server safety (`aa-server-safety`) | Client classes out of common code, packet validation, menu validity with the drill upgrade dupe, player data sync | 18.5 | Baseline | Planned |
| Data core (`aa-data-core`) | Recipes, `forge:` tags, advancements and the JEI plugin shell | 21 | Baseline | Done in 681664e, c1c79a0, 077c3cd and d4d05a7, review fix 655b320 |
| Assets (`aa-assets`) | Retexture 2.0 and 2.1, removal of the 1.12 asset leftovers, block shapes, directional crystal clusters, lamps, block sounds and the vanilla stairs, slab and wall tags | 20 | Baseline, data core | Planned |
| Processing machines (`aa-processing-machines`) | Crushers, Powered Furnace, Coal Generator fuels, the other generators, Coffee Maker, Canola Press and Fermenting Barrel, Energizer and Enervator, Experience Solidifier, Firework Box, Automatic Feeder, Lava Factory | 33.5 | Server safety, data core, assets | Planned |
| World machines (`aa-world-machines`) | Breakers, placers and the Vertical Digger, phantom blocks and boosters, Fluid Collector and Placer, Farmer, Atomic Reconstructor and lenses, Display Stand and Empowerer, Greenhouse Glass | 28.5 | Server safety, data core, assets | Planned |
| Energy and relays (`aa-energy-relays`) | Laser relays, item interfaces, Battery Box and energy items, laser and particle rendering | 13.5 | Server safety, data core, assets | Planned |
| Machine contents (`aa-machine-retain`) | Machines keep energy, fluids and redstone mode when broken, reset recipes, house machines that drop nothing, drops with the wrong tool | 7.5 | Processing machines, world machines, energy and relays | Planned |
| Filters and sacks (`aa-filters-sacks`) | Sacks and the Void Sack, filters with the advanced item relay fix, the Item Tag filter, Ranged Collector, Crafting Table On A Stick with a key bind and Curios | 21.5 | Server safety, data core, energy and relays | Planned |
| Tools (`aa-tools`) | Drills, AIOTs, small item fixes, Engineer's Goggles | 19.5 | Server safety, data core, energy and relays | Planned |
| Arclight hardening (`aa-arclight-local`) | A named fake player with block events, a chunk guard for phantom blocks, an index of the relay networks, a throttle for the magnet ring | 15 | World machines, energy and relays, tools | Planned |
| World content (`aa-world-content`) | Crops, black quartz ore, wild crop patches, Engineer house and villager, loot modifiers, banner patterns | 25.5 | Data core, assets, machine contents | Planned |
| Manual and compat (`aa-ui-docs-compat`) | Manual on Patchouli, screen fixes, lang cleanup, Garden Cloche and Mekanism crusher recipes | 15 | Processing machines, world machines, energy and relays, machine contents, filters and sacks, tools, Arclight hardening, world content | Planned |
| Release (`aa-ship`) | Savegame test, pack wiring, licenses, README row and the final state of this page | 9 | Manual and compat, Arclight hardening | Planned |

The baseline comes first, then server safety and the data core. The assets wait for the data core,
because both edit the tag and recipe generators in `data/`. The three machine bundles (processing
machines, world machines, energy and relays) then run in parallel worktrees and are merged one at a
time. Machine contents waits for all three, because it edits the NBT handling of about fifteen of
their tiles. Filters and sacks wait for energy and relays, because their filter fix f46de7d9 also
changes the advanced item laser relay. The tools wait for energy and relays too, which rework the
energy items that the drills build on. The Arclight hardening waits for the tools, since it changes
the drill's area mining in `WorldUtil.breakExtraBlock`. World content waits for machine contents,
which handles the `StopDrop` key of the house machines. Manual and compat runs after every content
bundle so that the lang sweep comes last, and the release closes the plan (inventory,
`critic_notes`, Structure changes and Waves).

## Scope

The research for this backport rated the mod a good fit: a well known magitech mod with the Atomic
Reconstructor, the Empowerer, crystals, laser relays and canola oil power, rewritten upstream with
new models and textures. Its only library, Patchouli, and its optional Curios support are in the
pack (`bp2/discovery-result.json` in the workflow scratchpad, candidate `actually-additions`). Java
paths below are relative to the mod's root package folder in `src/main/java`, which holds `mod/`,
`data/` and `api/`, as in the inventory. Paths marked main are upstream's, all others the base's.

### Version

The fork keeps 1.1.52 as the base number and builds 1.1.52-skytekx.1. The code base is 1.1.52, and
`UPSTREAM.md` and `NOTICE` will state the content level as parity with upstream 1.3.26 (cd9d9c63)
except the exclusions. A base number of 1.3.26 would claim parity with a NeoForge 1.21.1 build that
has data components and 1.21 content, and it would point bug reports at the wrong changelog. The
alternative `1.3.26-skytekx.N` stays recorded in `UPSTREAM.md` (build change 4) for the release to
decide. Nothing in the pack depends on the version, see the property table.

### Baseline

- The setup stage already did the version, the reproducible jar and the license fields (1eccbee,
  a6bcde1, `UPSTREAM.md` build changes 1 to 9).
- The file `mods.toml` gets optional dependencies on Patchouli (`[1.20.1-81,)`), Curios (`[5.0,)`)
  and JEI (`[15.0,)`). Patchouli stays optional as upstream has it: the `neoforge.mods.toml` of
  `main` declares no Patchouli dependency, only the Modrinth publish block marks it required, and
  the manual warns in chat when Patchouli is missing (19cfbc92).
- The resource and data pack format becomes 15. The base still says 6 with a 1.16 comment
  (`src/main/resources/pack.mcmeta`).
- The folder `src/main/java/invtweaks/` goes, as 9bc38d2a does on 1.20.4. Its two annotation classes
  end up in the jar as the package `invtweaks.api.container`, their license is unchecked, and a
  second jar with the same package would stop Forge's module layer with a split package. The fully
  commented `ContainerGiantChest` that alone imports them goes too.
- Patchouli comes as the full jar at compile time, because the reconstructor page extends
  Patchouli's `PageSimpleProcessingRecipe` and uses `ClientBookRegistry`, which are not API (main
  `PatchouliPages.java:4`, `PageReconstructor.java:10-11`). Curios comes as its API jar.
- Mixins register through the manifest attribute `MixinConfigs`, the way
  `ports/enderio/build.gradle:342` does, because Forge 47 does not read the `[[mixins]]` block that
  `main` uses. The config targets Java 17 and Mixin 0.8.5. Its lists stay empty until the Item Tag
  filter and the goggles bring their mixins, so no refmap is written before that (274cd3c).
- A port script in the scratchpad rewrites the mechanical NeoForge names, the 1.21 data folder
  names, the recipe keys and the `c:` tags of one upstream commit and applies it with
  `git apply --3way`. Generated files under `src/generated` are never patched from upstream.
- The common config of `main` (36 keys with its two Vertical Digger lists, 9470a0d) replaces the
  base's 24 keys with upstream's camelCase names from the start, and a new client config holds
  `hideEnergyOverlay`. Values are read lazily, never in a static initialiser. The built in config
  screen of a831cbbd is NeoForge only.
- The data generators of the base first run untouched, and their diff to the committed
  `src/generated` (335 models, 120 blockstates, 109 recipes) stays in the scratchpad as the
  reference. Then the provider fixes land, among them item models for every item and the crushing
  recipe datagen of c1b23b52.
- The boot smoke test fixes only what stops a boot. Of the cited commits only f9897f06 applies:
  `mod/ActuallyAdditionsClient.java:97` registers `ClientRegistryHandler`, whose listeners take mod
  bus events, on the Forge bus. The fork deleted the class as upstream 2e394d9 did, since both
  listener bodies are commented out (5b06e38). In the pack the InvTweaks removal was the one boot
  fix the base needed, see the status.

### Server safety

- Client classes leave common code (668f7ffe, 844efe42 and the I18n commits). On the base the HUD
  interface `IHudDisplay` takes `GuiGraphics` and `Minecraft`, the laser relay tiles call
  `I18n.get`, and `PacketHandler` holds client handlers. Upstream ties this to dedicated server
  crashes (#1434). The same class loading hits Arclight's dedicated server when the verifier meets a
  client type.
- Packet validation is a local fix, since upstream still trusts the client. On the base the four GUI
  packets take the dimension, the position and the player from the packet
  (`mod/network/PacketHandler.java:92-171`), so any client can press buttons on any machine as any
  player and load chunks. The handler list serves both directions (`PacketHandler.java:195-203`), so
  a client can also send the index of a client handler. The fix keeps the channel and its NBT
  format, uses only the sender, checks the dimension, a loaded position within 8 blocks or the open
  menu, adds allow lists per direction and logs a WARN per refused packet. The protocol goes from 4
  to 5 once (`PacketHandler.java:175`).
- The drill screen dupes upgrades on the base. It writes them back into whatever hotbar slot is
  selected when it closes (`mod/inventory/ContainerDrill.java:130-136`, unchanged on `main`), and a
  client can change the slot by packet while the screen is open. The fix remembers the slot and the
  stack at open and closes the screen when either changes. The filter screen gets the same guard.
- The client never receives its player data on the base, because the clientbound handler reads the
  sender (`PacketHandler.java:134`), which is null on the client. The fix also stops writing the old
  manual keys `Bookmarks`, `Trials` and `DidTutorial` (c8e90aef) and still reads them.

### Data core

- Recipes grow from 109 generated files to the set of `main` (204), written in the 1.20.1 form by
  hand after the generator change. Conditions translate one to one to `forge:mod_loaded`,
  `forge:not` and `forge:tag_empty`.
- The base ships no `forge:` tag at all. The port adds the black quartz gem, ore and storage block
  tags, the twelve crystal storage blocks, the seeds and crops of rice, coffee, canola and flax,
  `forge:slimeballs` for the rice slimeball, the AIOT tool tags and the mod's own ore replaceable
  and lamp tags. The spec of `tags-forge` holds the full `c:` to `forge:` table for every tag that
  the data of `main` names.
- Advancements and the JEI plugin shell with the laser, mining lens and empowerer categories come
  here. The other categories land with their machines. EMI shows them through its JEI layer.
- The coffee branch of the advancement tree stays out of reach after this bundle. Coffee beans,
  canola, flax and rice have no source until the wild patches of the world content bundle land,
  because the plant generation of the base is commented out. So `pickup_coffee`,
  `craft_coffee_machine` and the recipes of the Coffee Maker, Canola Press, Fermenting Barrel and
  Oil Generator wait for that bundle (`UPSTREAM.md`, source change 11).

### Assets

- Retexture 2.0 (9cf9601e, 638 files) and 2.1 (65426282) with the crop, lens, crystal, battery,
  drill, AIOT and worm textures, the crate keeper model, mono sounds and the firework box texture.
  The changelog of 1.2.13 calls this new models and textures for almost every block and item.
- The 1.12 leftovers that ship in the jar and that no 1.20.1 loader reads go: `blockstates_old/`,
  `models_old/`, the asset folder `loot_tables/` and a 1.12 house structure with DataVersion 819.
- The block shapes of `main` with the rotation helper that upstream marks as taken from Mekanism
  (main `VoxelShapes.java:306-308`).
- Crystal clusters face the clicked side. The base already declares the property `facing` but always
  places them up (`mod/blocks/CrystalClusterBlock.java:32`, `58`).
- Lamps show their state, the lamp controller follows its rotation, and both get recipes. Casings,
  machines and crystals get their own sounds, and stairs, slabs and walls join the vanilla tags.

### Processing machines

- The crushers are finished with their recipes and the copper and deepslate ores (ef6e52f8,
  7c91da5a). The Powered Furnace becomes a JEI catalyst and lights up while it works.
- The Coal Generator burns anything with a furnace burn time (d5757a14, #1435), and tiny coal and
  tiny charcoal get one through `Item#getBurnTime`. The serializer `actuallyadditions:solid_fuel`
  stays registered so that third party datapacks still load.
- The Leaf Generator gets its config, the Bio Reactor a facing and a blockstate (822913f0), and the
  Heat Collector a recipe.
- The Coffee Maker reads its ingredients from the new recipe type
  `actuallyadditions:coffee_ingredient` (971fdd65) and gets the effect, water and automation fixes
  up to e6156bfc.
- The Canola Press, Fermenting Barrel, Energizer, Enervator, Experience Solidifier, Automatic Feeder
  and Lava Factory are fixed and get their recipes and JEI categories where upstream has them.
- The Firework Box gets its settings screen. A local clamp keeps every value in the slider range of
  `main` (main `GuiFireworkBox.java:48-61`), because the base stores any number a packet sends
  (`mod/tile/TileEntityFireworkBox.java:110-148`). The Experience Solidifier also refuses a negative
  button id (`mod/tile/TileEntityXPSolidifier.java:208`).

### World machines

- The Long-Range Breaker and the Vertical Digger mine again. On the base the drop helper always
  returns 0 (`mod/util/WorldUtil.java:293-305`). The Placer places again, and the Breaker breaks
  snow.
- The Vertical Digger keeps its saved layer below Y 0. The base uses Y -1 as not started and 0 as
  done (`mod/tile/TileEntityVerticalDigger.java:57`, `105-121`). The port uses the minimum build
  height as in ae6f1603 and marks the new format with an additive key. It does not copy 738f098b,
  which restarts every digger from the top at each chunk load.
- The phantom blocks and boosters work and drop their capabilities when the target changes. The
  Fluid Collector and Placer take from cauldrons and no longer waste fluid.
- The Farmer works with modded farmland, takes a compass for its area, handles nether wart, potatoes
  and carrots and leaves custom soil alone (b26c47dd, #1522).
- The Atomic Reconstructor no longer crashes its renderer or tooltip, takes a stack of lenses and
  gets the mining lens recipes for black quartz, ancient debris, deepslate and copper ores. The
  compat sets come for Mekanism and Immersive Engineering, which the pack has. The five other sets
  come only after their ids are checked against each mod's 1.20.1 jar.
- The emeradic empowering recipes ask for short grass, which is `minecraft:grass` on 1.20.1. The
  base has the same tall grass bug as upstream had before d8ad92c4. The Greenhouse Glass renders and
  has a recipe.

### Energy and relays

- Laser relays render right, fill Mekanism energy cubes that refuse a real transfer after a
  simulated one (9fe3fa1c), and the fluid relay rate comes from the config.
- The hopping item interface faces a direction again and moves items. The Battery Box renders and
  drops its capability on change, chargeable items show a bar, and JEI lists full and empty items as
  two entries.
- The laser and particle render types work with shader packs, on 1.20.1 through Oculus.

### Machine contents

- Generators, crushers, the display stand, the furnace, the reconstructor, the Coffee Maker and the
  Farmer keep their energy when broken, the fluid machines keep their fluids, and the redstone
  machines keep their mode (changelog 1.3.10). The 1.20.1 form is the loot function
  `minecraft:copy_nbt` into `BlockEntityTag`. Shapeless reset recipes clear an item.
- Machines in an Engineer house drop nothing (`StopDrop`, 74982c36). On 1.20.1 `playerWillDestroy`
  returns nothing, so the port suppresses the drops in `getDrops`. Blocks broken with the wrong tool
  still drop (632c3473).

### Filters and sacks

- The sacks pick items up, save their options and their filter, and the Void Sack gets its own menu
  and recipe. Filters match by damage and NBT, and the advanced item relay filters work (f46de7d9).
- The new item `actuallyadditions:item_tag` holds one item tag, set in its screen or by renaming it
  in an anvil at no cost, and every filter slot accepts it. Its string goes through the hardened
  packets, capped at 256 characters.
- The Ranged Collector picks up items again. The Crafting Table On A Stick opens with a key bind
  from the inventory or a Curios charm slot. Every Curios call sits in one compat class that runs
  only when Curios is loaded.

### Tools

- Drills get the range, attribute, highlight, placement, tooltip and dye fixes, and their screen
  refuses an upper augment without the lower one (75c3757e). The drill does not copy the cascade
  guard that `main` marks as taken from Apotheosis (main `DrillItem.java:257`).
- AIOTs till, flatten and strip, show their damage and join the tool tags. Water bowl, filler,
  teleport staff, ring of growth and worms get their fixes, among them no crash when a fake player
  hoes grass (a65dccf9).
- The Engineer's Goggles render on the head, and the advanced ones make mobs glow for the wearer
  only through a client mixin (3b297993). The base sets the real glowing flag from a client tick.

### Arclight hardening

All four parts are local, see [Arclight notes](#arclight-notes).

### World content

- Crops drop like on 1.12, harvest by right click, compost, and world generated plants survive a
  block update through a new block state property `persistent` (38d6404b). The Tiny Torch gets its
  fixes.
- Black quartz ore generates in stone and deepslate, size 6, count 8, between Y -25 and 45, behind
  the config switch `generateQuartz` (02422d65, ec4b79ff). The base generates nothing.
- Wild patches of canola, flax, coffee and rice generate with their own switches.
- The Engineer house joins five village house pools, and the Engineer villager works at an AA job
  block. The house is upstream's final structure with its DataVersion set to 3465 by a scratchpad
  script.
- Bats drop wings, and dungeon and other chests get crystals, drill cores, black quartz and wings,
  through global loot modifiers instead of the base's dead event handler.
- Four banner patterns come with their items and tags, with the textures under
  `assets/actuallyadditions/textures/entity/banner/` as 7245af2a moved them.

### Manual and compat

- The manual opens a Patchouli book with the category getting started and five entries, plus the
  reconstructor page type. The JSON is hand written from the generated files of `main`. Without
  Patchouli a chat line explains it.
- Screen fixes, the lang cleanup of the 131 entries marked work in progress, the Chinese translation
  and the removal of the eight 1.12 `.lang` files.
- Canola, coffee, flax and rice grow in the Garden Cloche of Immersive Engineering 10.2.0-183, and
  the Mekanism crusher turns a black quartz block into smooth black quartz, each behind a
  `forge:mod_loaded` condition.

### Release

- The savegame test, the jar in `local-mods/` and `mods/`, the permission record, the license text,
  the README row, the entry in [Mod set](mod-set.md), the carryover file and the final state of this
  page.

## Pack integration

- AlmostUnified 0.11.0 lists neither black quartz nor any crystal as a material
  (`config/almostunified/unify.json`, `materials` from line 39), so the new `forge:` tags unify
  nothing of the mod's own items.
- AlmostUnified does rewrite the compat outputs of the mining lens. Mekanism tin, lead and uranium
  ore and Immersive Engineering silver ore become the ores of the mod with the highest priority,
  Thermal where it has them. That is wanted. The crushing outputs stay vanilla because `minecraft`
  has the top priority.
- Tiny coal and tiny charcoal get no `forge:nuggets` tag, because AlmostUnified would unify them
  with the coal and charcoal materials.
- Farmer's Delight 1.3.4 also fills `forge:crops/rice`, so the mod's rice counts in its recipes,
  which is wanted. The flax seeds join `forge:crops/flax` as on `main`, and Supplementaries 3.1.44
  makes its rope, sack and doormat from that tag, so the seeds craft rope without growing flax. The
  owner can take them out of `forge:crops/flax` in `kubejs/server_scripts/tag_fixes.js` if that is
  unwanted (review of the data core bundle, not decided). Canola oil and the other three fluids
  have no `forge:` standard and keep the mod's own bucket tags.
- The new worldgen uses Forge biome modifiers on `#minecraft:is_overworld` and the `forge:` climate
  tags. The Skylands biome `skytekx3:sky_void` sits only in `#skytekx3:has_structure/sky_floaters`
  and two Dungeons Arise tags of the Skylands datapack. None of the tags the modifiers use holds it,
  and no village starts over the void, so no ore, patch or house lands there. The placed features
  keep the `minecraft:biome` filter of `main`. A pregenerated copy checked with `tools/void_scan.py`
  has to confirm it, see [Skylands worldgen](../content/skylands-worldgen.md).

## Excluded

| Upstream work | Reason |
|---|---|
| Port churn of NeoForge 20.4 (`neoforge-1204-port-churn`) | The port commit 63fbbb33 (227 files) and its follow ups only adapt APIs: capabilities, codecs, energy attachments, saved data and datagen helpers. Two of them, 9ec20f48 and f2596f97, are already in effect on the base. Each reverse mapping is read off 63fbbb33 when a feature needs it |
| Port churn of NeoForge 21.1 (`neoforge-121-port-churn`) | DeferredHolder registration, data components, payload networking and the like, from 23815b70 to d9b0a714. The fixes 8fd600dc and 758001e2 repair 1.21 regressions that the base does not have |
| New inventory format (`tile-inventory-format-change`) | Commit f40023cf writes machine inventories as one compound instead of the base's slot list. Porting it would empty every machine of an existing world |
| Structure updater (`structure-updater-blcs`) | The datagen helper `StructureUpdater.java` of 43a23883 carries a custom license whose permission does not reach this fork (`UPSTREAM.md`, License). A scratchpad script sets the DataVersion of the house instead |
| Storage crate (`crate-skeleton`) | Unfinished upstream content that upstream removed again (786d9d33, e9b5c7f8) |
| Enchantment rework of 1.21 (`enchantment-1-21-rework`) | The base already reads stored enchantment levels, and Forge 47 has no enchantment level event |
| Data driven systems of 1.21 (`data-driven-1-21-systems`) | Data maps, banner pattern JSON, the enchantment and armor material registries and `c:` tags. Their effects are ported by 1.20.1 means. The only newer vanilla id that `main` uses is short grass |
| Removal of unused items (`remove-unused-items`) | The items `dough` and `paper_cone` stay registered, since removing an id breaks existing worlds |
| Menu id rename (`menu-id-rename`) | The sack menu keeps the base id `bag_container` instead of `sack_container` |
| Removal of API and dead code (`api-and-dead-code-removal`) | No player effect. Only dead code that drags client classes into common code goes, in server safety |
| JEI 19 API (`jei-19-api`) | The pack runs JEI 15 |
| NeoForge config screen (`neoforge-config-screen`) | Commit a831cbbd uses a screen that Forge 47 lacks |
| Build, CI and release chores (`build-ci-and-release-chores`) | Gradle, CI, publishing, README, changelog and version bumps have no effect on the 1.20.1 jar |
| Code hygiene (`code-hygiene`) | Imports, logging, names and suppressions without player effect |
| Commits already on the base (`already-on-base`) | Commits 0e3fec76, 33f55411 and 226a4391 are in the snapshot |
| Datagen output sync (`generated-output-sync`) | The commits that only refresh `src/generated`. The fork writes its own 1.20.1 output |

## License conditions

- Code and assets are under the MIT License. Upstream commit 33f55411 replaced the old custom
  license on branch `1.20.1` on 2024-03-04, and 717199ea did the same on the newer lines, both
  citing upstream issue #1391. The same `LICENSE` with its 2019 copyright line sits on `main` at
  cd9d9c63 (`UPSTREAM.md`, License). The old per file headers that point to the former license are
  superseded and stay unchanged.
- The jar ships upstream's `LICENSE` as `LICENSE_actuallyadditions` and the SkyTekx `NOTICE` as
  `NOTICE_actuallyadditions` (a6bcde1, build change 8). The `mods.toml` field `credits` marks the
  build as modified and unofficial, and bug reports go to the SkyTekx tracker (build change 9).
- The textures of the retexture came into the MIT repository after the relicensing (9cf9601e is
  dated 2024-04-03), and no separate asset license exists on either ref. The Modrinth project lists
  the artist on its team and declares MIT. Nobody has verified this with the artist.
- Excerpts that upstream marks as taken from other mods with permission: the experience formulas of
  Ender IO in `TileEntityXPSolidifier` stay with their credit. The banner helper from Additional
  Banners leaves with the banner patterns bundle, and its `NOTICE` line goes with it. The empty
  Tinkers' Construct excerpt left with `ClientRegistryHandler`, which the boot smoke test deleted
  (5b06e38).
- The rotation helper in `VoxelShapes` is marked as taken from Mekanism, which is MIT. The comment
  stays, and `NOTICE` gets a Mekanism line with its copyright notice when the file lands.
- The snapshot already leaves out `RecipeHandler.java`, a commented copy of a class under CC
  BY-NC-ND 4.0 (`UPSTREAM.md`, Files left out). The baseline also deleted the fully commented
  `RecipeHelper.java`, whose header names a third party author and the MIT License without the
  notice text, and the InvTweaks annotations of unchecked license (4f87826).
- Patchouli, Curios and JEI are compile time dependencies only, and no class of them ships in the
  jar.
- The release adds a record in `modrinth-permissions/` (MIT, unofficial build, no 1.20.1 file on the
  Modrinth project, the project id checked against the Modrinth API) and the MIT text with its
  copyright line in `licenses/`, named after the jar like the other license files.

## Savegame notes

- No live world has the mod, so the savegame test is short. OLD is the base a6bcde1 with its one
  boot fix, the InvTweaks removal, as the jar `237e0fc65275a0204fe7008f144c395207e41205`, because
  the vendored base build `c37140ce` does not start in the Arclight pack (see the status). NEW is
  the final build. The test places one of every block, fills machines, tanks, relay networks,
  phantom bindings, sacks, drills and batteries, stops over RCON, swaps the jar and checks that
  everything loads with its contents.
- No registry id, NBT key, block state value or saved data key of the base is renamed or removed.
  The items `dough` and `paper_cone`, the menu `bag_container` and the recipe serializer
  `actuallyadditions:solid_fuel` stay registered. The machine inventories keep the base's slot list.
- New block state properties load with their default: the Bio Reactor and the hopping item interface
  take their default facing, and crops load with `persistent` false, the old behaviour. Crystal
  clusters keep their state, since the base already has the property.
- Additive NBT keys: the Vertical Digger gains a format marker and restarts from the top once, the
  sack data gains `Filter`, the Farmer gains its area offsets, and a broken machine item carries
  `BlockEntityTag` with only the kept keys. Firework Box values outside the slider ranges are
  clamped on load. Player data stops writing the old manual keys and still reads them.
- New ids include the item `item_tag`, the menus `void_sack_container` and `item_tag_container`, the
  recipe type `coffee_ingredient`, four banner pattern items with their patterns, the loot modifier
  serializers `bat_loot` and `dungeon_loot`, the point of interest and profession `engineer`, the
  ore and patch features with their biome modifiers and the house pool. Each spec lists its own
  under New ids.
- Worldgen reaches new chunks only. The config files `actuallyadditions-common.toml` and, on the
  client, `actuallyadditions-client.toml` are new to the pack, so nothing needs a migration.

## Arclight notes

- Upstream breaks blocks as the shared fake player `[Minecraft]`, and on the base the Breaker posts
  no break event at all (`mod/tile/TileEntityBreaker.java:115`). The Phantom Breaker, the conversion
  lens and the mining lens change blocks without any event on `main` too. The port adds one fake
  player per level with the fixed profile `[ActuallyAdditions]`, posts `BlockEvent.BreakEvent` and
  `EntityPlaceEvent` for every machine break and placement and reverts on a cancel. Arclight 1.0.6
  turns both into the Bukkit `BlockBreakEvent` and `BlockPlaceEvent` with the fake player (javap by
  the review on the production jar `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`, extract in
  the workflow scratchpad under `aacritic/arc/`).
- A claim plugin has to trust `[ActuallyAdditions]` for machines to work inside claims. The pack
  runs EssentialsX only. Fake players per owner would need a new key on every tile and go on the
  [open items](../open-items.md) at the release.
- A phantom block reads its target's block entity every tick before it checks the range (main
  `TileEntityPhantomface:141-170`), which loads the target chunk when the booster is gone. The port
  checks the range and `hasChunkAt` first.
- Every energy and fluid relay scans all networks and connections of the level in its tick
  (`mod/misc/apiimpl/LaserRelayConnectionHandler.java:71-120`, unchanged on `main`). A transient
  index by position replaces the scan, and the saved format stays.
- The magnet ring scans a 5 block box every tick for every ring. It scans every 4 ticks instead, and
  the pickup still goes through `ItemEntity.playerTouch`, so Bukkit pickup events fire.
- The anvil mixin injects at the head of `AnvilMenu.mayPickup` and an accessor reads
  `ItemCombinerMenu.inputSlots`. Arclight's own anvil mixin only touches `AnvilMenu.createResult`,
  so they do not collide (javap by the review). The goggles mixin is client only. No mixin uses
  `@Overwrite`.
- The sacks pick up items in Forge's `EntityItemPickupEvent`. Whether Arclight posts the Bukkit
  pickup event before or after it is not verified yet.

## Local divergences

These differ from upstream on purpose and must survive the next upstream sync.

- The version keeps the base number 1.1.52 plus `-skytekx.N`.
- The server validates every serverbound packet, see [Server safety](#server-safety).
- The drill and filter screens close when the selected slot changes, the Firework Box clamps its
  values, and the Experience Solidifier refuses negative button ids.
- The Vertical Digger marks its format with an additive key instead of resetting on every load.
- Machines break and place as the fake player `[ActuallyAdditions]` with block events, phantom
  blocks check the chunk first, relays use an index, and the magnet ring is throttled.
- The fork keeps the ids that `main` removed or renamed and the base's inventory format.
- Tags use `forge:` instead of `c:`, and tiny coal gets no nugget tag.
- The Lens of the Killer takes a `forge:partial_nbt` book with Sharpness V in `StoredEnchantments`,
  since the strict ingredient of the base and the 1.20.4 line matched no real book. The 3x3 slime
  block recipe takes rice slimeballs instead of every `forge:slimeballs` item, and the invisibility
  upgrade keeps `forge:glass/black` (`UPSTREAM.md`, source change 10).
- Patchouli stays optional, and the book is hand written without a Patchouli provider.
- Compat mining lens recipes ship only for mods whose 1.20.1 ids were checked.
- The Engineer house uses upstream's structure with a lowered DataVersion, without the structure
  updater.

## Operator notes

- Server and clients update together. The mod adds blocks and items, so a client without it cannot
  join, and the protocol bump refuses a client with another build.
- Mirror the jar by hand while the java process is gone: copy it from `local-mods/` into `mods/`.
  Then boot, and commit the new `config/actuallyadditions-common.toml` before
  `tools/build_mrpack.py`, which takes overrides from git HEAD. Edit that file only while the java
  process is gone.
- The ore, the crop patches and the Engineer houses only reach chunks that do not exist yet. The
  live world gets them in new areas only. Keep `/locate` sweeps off the live server.
- A boot is clean when it reaches `Done` with 0 FATAL lines, no crash report, no watchdog and only
  the two accepted ERROR lines, with no new ERROR line of the mod (see
  [Logs and crashes](../server/logs-and-crashes.md#boot-error-baseline)). Until the first mixin of
  the fork lands, Mixin also logs one WARN that `actuallyadditions.refmap.json` could not be read.
- Announce the new mod to the players, with the manual that needs Patchouli and the key bind of the
  Crafting Table On A Stick.

## History

- The first draft of the inventory had 13 bundles and 261.5 h. The review moved the machine contents
  into their own bundle after the three machine bundles, moved the block sounds into the assets
  bundle before them, moved the advanced item relay filter fix into the filters bundle, cut the JEI
  feature to the plugin shell with three categories and set the dependencies of the Arclight, assets
  and manual bundles (inventory, `critic_notes`, Structure changes).
- The draft also planned to port f2596f97, 82aaacf1, 9ec20f48 and 758001e2, which the review found
  already in effect on the base or not needed.

Related: [Mod set](mod-set.md), [Twilight Forest backport](twilightforest-backport.md),
[Ender IO backport](enderio-backport.md), [AlmostUnified](almostunified.md),
[Arclight](../server/arclight.md), [Logs and crashes](../server/logs-and-crashes.md).
