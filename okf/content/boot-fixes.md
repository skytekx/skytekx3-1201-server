---
type: Reference
title: Boot fixes
description: How the server boot went from 152 errors to 2, with the skytekx3_fixes override datapack, the jar patches of tools/patch_mods.py, two config fixes and a hand edited jar.
tags: [content, server, datapack, mods, boot]
timestamp: 2026-11-04
---

# Boot fixes

On 2026-06-30 04:15 the owner asked for every error and warning at server boot to be resolved
(`~/.claude/history.jsonl`, session 5bd3e1de). Until then a boot logged 152 ERRORs, almost all of them
third-party data files that do not parse on 1.20.1. Three commits brought that down to 3: 52f4f19
(05:26, to about 27), 518aa19 (06:21, to 10) and af5860e (07:05, to 3). It stayed at 3 until the jar
patch of 2026-11-04 removed the error of the Simply Jetpacks 2 guide book, see
[jar patches](#jar-patches). The warnings were not cleared. The last boot on the build machine
(2026-07-12 23:25) still logged 304 WARN lines, and the boots on the Mac mini of 2026-10-31 logged 344
to 387, or 1,658 at the first boot of the new world. The counts per boot and the accepted errors are on
[logs and crashes](../server/logs-and-crashes.md#boot-error-baseline).

The fixes come in five kinds:

| Kind | Where | Survives a mod update |
|---|---|---|
| Override datapack `skytekx3_fixes` | Folder `world/datapacks/skytekx3_fixes/`, tracked in git | Yes, as long as the paths stay the same |
| Jar patches | Script `tools/patch_mods.py`, applied to jars in `mods/`, and by `tools/build_mrpack.py` to the one bundled jar that the pack ships patched | No, run the script again after any new download |
| Config fixes | Files `config/apotheosis/enchantments.cfg` and `config/collective.json5` | Yes |
| Bird's Nests crash guard | The [compat mod](compat-mod.md), `local-mods/skytekx-compat-2.0.0.jar` | Yes |
| Hand edited Aether Genesis jar | Jar `local-mods/aether_genesis-1.20.1-0.0.1-neoforge.jar` | No, a rebuild from source loses it |

Installing missing dependencies did the rest. SlashBlade Resharped, Re:Avaritia (removed again later)
and Medieval Embroidery came in with 518aa19, and The One Probe and a source build of Aether Genesis
with af5860e.

## The override datapack

The pack `skytekx3_fixes` exists only inside the world, at `world/datapacks/skytekx3_fixes/`. It has no
source folder, and `tools/sync_skylands_datapack.sh` does not touch it. Since 3c2662e `tools/build_mrpack.py` ships it in
the `.mrpack` next to the two Skylands packs. Git ignores `world/`, but the pack was force added, so it
is tracked: 148 files, 147 data files plus `pack.mcmeta` (pack format 15). The table lists the 125 boot
fixes. 4 files add [missing structure pools](#missing-structure-pools), and the other 18 are
[gameplay overrides](#gameplay-overrides).

| Override | Count | What it replaces |
|---|---:|---|
| Loot tables | 99 | Tables that name items or types missing on 1.20.1, replaced with valid tables with empty pools: mekanism_extras 60, decocraft 28, dragnlivestock 3, irons_spellbooks 2, and one each for aether_villages, ancient_aether, galosphere, lost_aether_content, mocreatures and naturalist |
| Advancements | 14 | Malformed advancements, replaced with one `minecraft:impossible` criterion: galosphere 9, dragnlivestock 3, aether_redux 1, industrialforegoing 1 |
| Tags | 8 | Tags with a missing required entry, rewritten with `replace: true` and every entry `required: false`: five `forge` tags (`ungrown_ender_lilly_states`, `pink_items`, `generator_eur`, `frosty_gen_tag`, `rabbit_hutches`), `galosphere:experimental_blocks`, `bibliocraft:potion_shelf_potions` and `aether_villages:collections/ancient_aether_biomes` |
| Recipes | 2 | The Create recipes `silver_ingot_compat_galosphere` for smelting and blasting, switched off with a `forge:false` condition |
| Farming for Blockheads compat | 1 | The Thermal Cultivation seeds entry, made silent |
| Biome modifier | 1 | The Mo' Creatures modifier `wyvern_nests`, which names the tag `mocreatures:worldgen/biome/is_wyvern_lair` that does not exist, set to `forge:none` |

### What the overrides change in game

Each override was checked on 2026-10-31 against the last boot before the pack existed
(`logs/2026-06-30-19.log.gz`, started 03:33 on 2026-06-30). That boot logged 152 ERRORs, 101 of them
"Couldn't parse element". All 99 loot tables and all 14 advancements were among the parse failures, so
the originals never loaded. An empty loot table and an advancement that cannot be earned behave the
same as a file that failed to load. These 113 overrides only remove log noise.

The 8 tags are different. That same boot logged a "Couldn't load tag" error for each of them, and a
tag with a missing required entry does not load at all. With the overrides they load and hold the
entries that exist. So these tags now work where they were empty before.

Until 2026-11-04 the pack also held a `book.json` for the Simply Jetpacks 2 guide book, set to
`use_resource_pack: true`. It could not work. The boot of 2026-07-12 still logs "Failed to load book
simplyjetpacks:guidebook" (23:25:43), and so does every later boot. Patchouli reads book definitions
only from mod jars, never from datapacks, so the file in the fixes pack was never read. The file is
deleted, and a jar patch that the pack ships too fixes the book now, see [jar patches](#jar-patches).

The last boot on the build machine (`logs/2026-07-12-1.log.gz`, up to Done) has no "Couldn't parse
element" and no "Couldn't load tag" line left.

The Mo' Creatures biome modifier came in on 2026-11-04 and only removes log noise too. The tag of the
mod is `mocreatures:is_wyvern_lair`, so the modifier matched no biome, and every boot and data reload
warned "Not all defined tags for registry ResourceKey[minecraft:root / minecraft:worldgen/biome] are
present in data pack: mocreatures:worldgen/biome/is_wyvern_lair", 35 times from 2026-10-31 to
2026-11-03. The four wyvern lair biomes list `mocreatures:wyvern_nest` in their own features, so the
nests still generate. Correcting the tag instead would add the feature a second time, which can stop
the server with a feature order cycle. Biome modifiers load only at a server start.

Since the mod update of 2026-10-31 (see [mod set](../modpack/mod-set.md#update-of-2026-10-31)) three
overridden mods run newer builds: DragNs Livestock Overhaul 3.9.4, Iron's Spellbooks 3.16.3 and
Naturalist 2.0.9. An override with empty pools also empties a table that a new build has repaired. The
next boot test should show whether their 6 loot tables and 3 advancements still need the overrides.

### Gameplay overrides

Two groups of files in the pack change the game on purpose:

- 16 biome modifiers of Railcraft and Immersive Engineering are set to `forge:none`. They turn off the
  ores that duplicate the preferred ores of AlmostUnified, see
  [AlmostUnified](../modpack/almostunified.md#one-ore-per-metal).
- 2 worldgen files under `data/hexxit_gear/worldgen/` make the Hexbiscus generate as single flowers
  like in SkyTekx 2 (9a784372), in 1 of 32 chunks since 68c55501. On 2026-06-27 21:46 the owner asked
  for no three Hexxit flowers next to each other, on 2026-11-03 16:18 said that they spawn too often
  and at 17:13 chose 1 in every 32 chunks.

| | Hexxit Gear 1.0.1 | SkyTekx 2, Hexxit Gear 2.8.2 | Override |
|---|---|---|---|
| Chance per chunk | 1 in 16 | 1 in 55 on average (`Hexbiscus Chance=55`) | 1 in 32, the owner's choice |
| Flowers per hit | A patch of 4 tries within 2 blocks | 1 | 1 |
| Ground | Heightmap `MOTION_BLOCKING`, so the tree tops in a forest | The top solid block below the leaves | Heightmap `MOTION_BLOCKING_NO_LEAVES` |
| May replace | Air | Any replaceable block, like grass | Air, short grass and fern |

The configured feature `hexbiscus` keeps the type `minecraft:flower` with 1 try and no spread, and the
placed feature `hexbiscus_placed` keeps the biome modifier of the mod. Both files parsed with the
codecs of the live server before they went in. Worldgen files load only at a server start, and only
new chunks change. Chunks generated before keep their Hexbiscus. At 16:20 the 441 chunks loaded around
the owner in the overworld held 11 Hexbiscus in 8 chunks. The flowers of Hexxit Additions are not
changed.

### Missing structure pools

Since 2026-11-04 four files under `worldgen/template_pool/` define pools that a structure piece names
but its mod never shipped. The jigsaw code skips such a jigsaw with the warning "Empty or non-existent
pool" (Repurposed Structures writes "nonexistent"), the jigsaw block turns into its final state and the
piece it should have attached is missing. Each file is a copy of the pool that the sibling jigsaws of
the same piece use. For Repurposed Structures it is the plain pool of the same village type, which is
what the zombie wells of its overworld villages name.

| File | Named by | Copy of | What comes back |
|---|---|---|---|
| `dungeons_arise/.../mechanical_nest/mechanical_nest_decoration.json` | One of nine decoration jigsaws in `mechanical_nest_towers_1` | `mechanical_nest_decorations` (88 elements) | One library decoration per tower, instead of a gray carpet. 1 of the 21 library pieces holds a chest with the loot table `mechanical_nest_normal` |
| `dungeons_arise/.../underworld/foundry/foundry_corridor_gears.json` | Two of six gear jigsaws in `foundry_corridor_crossing_2` | `foundry_corridor_deco` | Two gears in the corridor crossing, instead of cut sandstone |
| `repurposed_structures/.../villages/crimson/zombie/well_bottoms.json` and the `warped` one | The bottom jigsaw of the zombie town center `meeting_point_1` of the crimson and the warped village | `villages/crimson/well_bottoms` and `villages/warped/well_bottoms`, which no piece used before | The lava basin below the well, instead of one blackstone block. The town center has a weight of 1 in 204 |

The Mechanical Nest typo logged 344 warnings from 2026-10-31 18:14 to 2026-11-01 01:05, all during
the four Skylands pregens of those hours. The last of them (2026-11-01 00:59, radius 2000) made the
Skylands of the live world and logged 34, so 34 tower pieces there lack their library decoration. The
other three typos never showed in the logs, since no Foundry and no zombie nether village had
generated. Template pools load only at a server start, and only structures in new chunks change.
Nests generated before keep the gap.

A scan of all jars on 2026-11-04 found more names of missing pools, but none of them matters. Most sit
in pieces that no template pool places: the old Mushroom Village of When Dungeons Arise, pieces of
Orion Structures and Hexxit Additions, the big dungeon of Quark, and the camp and Lich Tower templates
of Twilight Forest, which the mod assembles in its own code. The spiral stairs terminator of YUNG's
Better Strongholds is in a pool, but no piece targets its only jigsaw. The pool `minecraft:` comes
from an empty pool field on the receiving jigsaws of five Iron's Spellbooks pieces, so nothing is
missing there either.

### Pack order

Commit 52f4f19 says the pack had to be enabled last, because the Skylands pack sat in the middle of the
order and lost to mod packs loaded after it. In the current `world/level.dat` (checked 2026-10-31) the
fixes pack is no longer last. It sits at index 270 of 279. After it come packs that the server appended
when their mods were installed later (SlashBlade, Medieval Embroidery, The One Probe, Aether Genesis and
its built in packs), and then `skytekx3_skylands_forge` at 278. None of these later packs is known to
ship a file at one of the 125 override paths, and the current boot shows no parse error, so this is
harmless today. A fresh world puts the three file packs after all mod packs, in the last three of 280
places (read from `level.dat` on the Mac mini. The message of 1c6f8d4 counts from 0 and says 277 to
279). The same order problem does break the ChocoCraft overrides, see
[Skylands worldgen](skylands-worldgen.md#known-defect-the-chococraft-overrides-lose). The commands to
move a pack to the end are on [world reset](../server/world-reset.md).

On 2026-11-04 the `level.dat` on the Mac mini listed 296 packs, with the fixes pack at index 273
(counted from 0) and the two Skylands packs right after it. The 20 packs after those are mods installed
since the world was created, among them Falling Meteors, Elementary Staffs, the Twilight Forest add-ons
and GraveStone. None of them ships a file at a path of the three file packs, but a new override for one
of these mods would lose. Mo' Creatures (33), When Dungeons Arise (68) and Repurposed Structures (23)
come before the fixes pack, so the overrides of 2026-11-04 win.

## Jar patches

The script [tools/patch_mods.py](../../tools/patch_mods.py) edits mod jars in `mods/` in place (776
lines). It finds each jar by a glob next to the script, so the repo path does not matter, and it is
idempotent: a second run prints `noop` for every jar. Three of the patches change class files. The
script adds new constants at the end of the constant pool, so every index the class uses keeps its
meaning, and it patches only when it finds exactly the instructions it expects. A new build of the mod
is left alone with a `skip` line.

| Mod and jar | Patch | Why |
|---|---|---|
| Doom Angel Ring (`doomangelring*.jar`) | Adds `minVersion: 0.8` to `doomangelring.forge.mixins.json` | Mixin logs an ERROR for a config without `minVersion` |
| Aether Redux (`aether-redux*.jar`) | Drops `GreenAercloudBlockMixin`, `OrangeTreeMixin` and `PurpleAercloudBlockMixin` | Compat mixins whose target mods are not installed |
| Mekanism Extras (`mekanism_extras*.jar`) | Drops `integration.mekaf.MixinAdvancedFactory` and `integration.mekmm.MixinMoreMachineFactory` | Same |
| Deep Aether (`deep_aether*.jar`) | Drops `AerwhaleKingMixin` | Same |
| Quark (`Quark*.jar`) | Drops `lootr.ConfigManagerMixin` from `quark_integrations.mixins.json` | Lootr is not installed |
| Ad Astra (`ad_astra*.jar`) | Deletes `data/minecraft/loot_tables/loot.json` | Its short path made Bird's Nests throw, see [compat mod](compat-mod.md) |
| Twilight Forest Dungeons & Villages (`tf_dnv-*.jar`) | Adds `minVersion: 0.8` to `mixins.tf_dnv.json` | Mixin ERROR as above |
| Traverse (`traverse-forge*.jar`) | Adds `minVersion: 0.8` to the mixin config of the nested terraform wood API jar | Mixin ERROR as above |
| TiCEX (`*[Tt]icex*.jar`) | Drops the `ticex:catalyst_*` part and station slot from `blitz_gun` and the four `singular_gem` armour pieces, and 7 catalyst stats from the reconstruction material | TaCz is not installed and Re:Avaritia did not satisfy the gem tools, so these tools failed to load |
| TiCEX, summoned swords | Rewrites the Utf8 constant `onHitEntity` of `SummonedSwordMixin` to `m_5790_`. It is the handler name and the target of its `@Inject` at once | The mixin has `remap = false` and names the target by its source name, but SlashBlade's `EntityAbstractSummonedSword` has the runtime name `m_5790_`. Every boot logged "Mixin apply failed", and the projectile hit modifiers of a Tinkers tool never fired for the summoned swords of its owner |
| CoFH Core (`cofh_core*.jar`) | Moves `MultiPlayerGameModeMixin` from the common list to the client list | A client class does not exist on a dedicated server |
| Medieval Embroidery (`Medieval_Embroidery*.jar`) | Rewrites 5 entity type tags with optional entries | They name a griffin entity that is never registered |
| Asgard Shield Reloaded (`AsgardShieldReloaded*.jar`) | In `EventHandler`, turns both reads of `Player.swingingArm` (`f_20912_`) into calls of `getUsedItemHand()` (`m_7655_`), two instructions of three bytes each | Not a boot fix. The field stays null until a player swings after a login or a respawn, so a hit on such a player threw `Invalid hand null` and did no damage. All 37 hits between players on 2026-10-31 from 22:37 on failed this way |
| BiblioCraft (`bibliocraft-*.jar`) | Three classes in `content/printingtable/`, see [BiblioCraft cloning cost](#bibliocraft-cloning-cost) | The recipe `bibliocraft:enchanted_book_cloning_in_printing_table` failed to parse ("Failed to decode from JSON"), so the printing table could not clone enchanted books |
| Simply Jetpacks 2 (`SimplyJetpacks2-*.jar`) | Sets `use_resource_pack: true` in the `book.json` of the guide book and copies the 146 files of its `en_us` and `ru_ru` folders from `data/` to `assets/`. The pack ships this jar patched, as `SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar` | Patchouli 1.20 refuses a book without the flag ("Failed to load book simplyjetpacks:guidebook") and reads book contents only from resource packs. The recipe of the book warned that the book does not exist |

A test boot of 2026-11-04 with the TiCEX, BiblioCraft and Simply Jetpacks 2 patches logged none of
their errors. KubeJS counted 9 failed recipes instead of 10, and `debug.log` shows the line "Mixing
SummonedSwordMixin from ticex.slashblade.mixins.json into
mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword". A second test boot the same day showed
the same.

On that second boot a temporary KubeJS script checked the TiCEX patch in play. A fake player owned a
summoned sword and held a TiCEX blade with Fiery 2. The sword hit a husk through `doForceHitEntity`,
and the husk burned for 200 ticks, the 10 seconds of Fiery 2. The same hit with a diamond sword in the
owner's hand set no fire. A blade with Fiery 2, Freezing 1, Decay 1 and Necrotic 1 left the husk with
460 frozen ticks, which keep it frozen for the 8 seconds of Freezing 1, and with Wither for 120 ticks.
Freezing put the fire of Fiery out, as its module does. That happened on a direct hit and on a hit by
a sword that flew 3 blocks through the entity tick. No hook threw. Hits by the summoned swords of a
real player and the healing of Necrotic were not tested.

Fourteen jars are patched. Eleven of them come from the index, and exactly these eleven no longer
match their SHA-1 in `modrinth.index.json` (checked 2026-11-04). The other three, Traverse, BiblioCraft
and Simply Jetpacks 2, are bundled jars. Their copies in `mods/` differ from the ones in `local-mods/`,
which keeps the upstream files.

Players get the unpatched jars, except Simply Jetpacks 2. The list `PACK_RULES` in `patch_mods.py` holds
the rules for jars that clients need patched, each with a suffix for the file name in the pack. The
script `tools/build_mrpack.py` reads it from git HEAD and applies it to the bundled jars while it writes
the `.mrpack`, so the pack and the server get the same bytes. The pack ships Simply Jetpacks 2 as
`mods/SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar`, while `local-mods/` and the server's `mods/` keep
the upstream name. Packs up to 3.1.2 installed the Modrinth file at
`mods/SimplyJetpacks2-1.20.1-2.11.0.1.jar`. A launcher that updates such an instance removes that
file, because the new index no longer lists it (the update logic of Prism Launcher and the Modrinth
App, not tested with this pack). With a name of its own the patched jar does not depend on whether a
launcher removes the old files before or after it unpacks the overrides. Should a launcher keep the old
file, Forge 47.4.18 loads only one of two files with the same mod id and version, the first by
lowercase file name, and that is the patched one. It logs the choice at DEBUG only (read from the
bytecode of `UniqueModListBuilder` and `ModsFolderLocator`, not tested). The build refuses a rule that
changes nothing, so a jar that upstream has fixed never ships under the patched name, and it refuses an
index that still lists a jar that `PACK_RULES` ships from `local-mods/`.

So that the pack can carry the patch, Simply Jetpacks 2 left the index on 2026-11-04 and is bundled
now, with its MIT license text in `licenses/` and its permission note in `modrinth-permissions/` under
the shipped name, which both permission checks use. A client with the jar from Modrinth still joins,
but it logs its own "Failed to load book" error, shows the crafted guide as "Guide Book" with the
tooltip "Invalid book: simplyjetpacks:guidebook", and a right click opens nothing. The other patches
are not needed on clients. Both patched handlers of Asgard Shield Reloaded return early on the client,
the BiblioCraft cost is charged on the server, and a client without the TiCEX patch only keeps logging
the mixin warning.

Run the script after anything that downloads jars into `mods/`:

```sh
python3 tools/patch_mods.py
```

The docstring says that `tools/resolve_all.py` rebuilds `mods/` from `modrinth.index.json`. It does not:
it resolves a hard coded list of slugs to the newest release. See [tools](../modpack/tools.md).

### BiblioCraft cloning cost

BiblioCraft Legacy (Expanded) 1.3.1 decodes the `experience_cost` of the recipe with
`EnchantmentLevelsNumberProvider.NUMBER_PROVIDER_CODEC`. That codec is lazy and needs itself for the
required field `normal_cost`, so no JSON can ever decode with it. The mod also registers its number
provider type `bibliocraft:enchantment_levels` with a Gson serializer, the way loot tables read
number providers. The patch uses that path.

- The recipe serializer's `fromJson` decodes the cost with the Gson of loot predicates, through a new
  static method `skytekx$experienceCost`.
- The Gson serializer of the number provider read `normal_cost` and `treasure_cost` with
  `GsonHelper.getAsJsonObject`, which throws for the plain numbers 1.0 and 2.0 of the recipe. It now
  reads them with `getNonNull`, like the values of `overrides`.
- The recipe serializer's `toNetwork` used the broken codec too. It now writes the flag for "no cost"
  and nothing after it, which every client reads, with or without the patch. The cost is computed on
  the server (`getExperienceLevelCost` takes a `ServerLevel`), so on clients only the experience note
  of the recipe in JEI is missing.
- The treasure check asked for the tag `minecraft:enchantment/treasure`, which no jar and no datapack
  defines, so treasure enchantments cost `normal_cost`. It now asks `Enchantment.isTreasureOnly()`,
  so treasure enchantments such as Mending and Frost Walker cost 2 levels per enchantment level, as
  the recipe says. This goes beyond the parse fix.

A KubeJS probe on a test server on 2026-11-04 got 5 levels for Sharpness V, 3 for Unbreaking III, 2
for Mending I, 4 for Frost Walker II and 7 for Sharpness V with Mending I. Its network round trip
wrote 15 bytes and read all of them back, into a copy with the same ingredient, result and duration
(600) but no cost.

## Config fixes

Commit 52f4f19 also changed two config files:

- In `config/apotheosis/enchantments.cfg` the Dimensional Doors enchantment `dimdoors:string_theory` had
  empty power functions, which made it impossible to obtain. They are now `Min Power Function=15` and
  `Max Power Function=50`.
- In `config/collective.json5` the key `enableUpdateChecker` is now false.

## Hand edited Aether Genesis jar

Aether Genesis has no release on Modrinth, so commit af5860e built it from the GitHub branch
`1.20.1-develop` and ships the jar without embedded libraries in `local-mods/`. The commit says the
client only mixins were moved into the client list. In the shipped jar the accessor
`client.accessor.SoundEngineAccessor` sits in the client list of `aether_genesis.mixins.json`. In the
source checkout `~/git/aether-genesis` (commit 6343dd5, no source change) it is still in the common
list, and the boot of 2026-06-30 06:54 warned about it ("@Mixin target
net.minecraft.client.sounds.SoundEngine was not found"). The shipped jar's SHA-1 (`5fa6d537...`) matches
none of the jars in the checkout's `build/libs/`. So the jar was edited after the build, and
`patch_mods.py` has no rule for it. A rebuild from source brings the warning back.

## On a new host

The script `macos/sync-to-mac.sh` mirrors `mods/` to the Mac mini as it is, with the patched jars.
Since 7a47a22 it also mirrors `world/datapacks/skytekx3_fixes/` straight into the Mac's world, so a new
world there gets the pack when the sync runs before its first start. On any other host, copy the folder
into a new world's `datapacks/` by hand, as was done for the Halloween map the owner asked for on
2026-10-31 16:56. The script `tools/sync_skylands_datapack.sh` only writes the two Skylands packs. Run `patch_mods.py` only if jars are downloaded again, and never fill
`mods/` with `resolve_all.py`. See [macOS host](../server/macos.md),
[world reset](../server/world-reset.md) and [deploy](../server/deploy.md).

A server built from the `.mrpack` gets the fixes pack since 3.0.6 (3c2662e) and, in a pack built
with `PACK_RULES`, the patched Simply Jetpacks 2, but none of the other jar patches. It boots with the
errors they silence until `tools/patch_mods.py` runs on its `mods/`.

Related: [logs and crashes](../server/logs-and-crashes.md), [mod set](../modpack/mod-set.md),
[compat mod](compat-mod.md), [world reset](../server/world-reset.md).
