---
type: Feature
title: Old blocks of Chocolate Quest
description: The five old decoration blocks of the Chocolate Quest Builder Village become the normal blocks everywhere, through a template pool and five loot tables in the fixes pack, the unify script for items and the offline tool replace_blocks.py for the existing world.
tags: [content, worldgen, datapacks, tools, kubejs]
timestamp: 2026-11-05
---

# Old blocks of Chocolate Quest

Chocolate Quest Golden Apple Edition (`mods/chocolate_quest-1.3.1-forge-1.20.1.jar`) builds its Builder
Village from five decoration blocks that look like vanilla blocks. Each village holds 48 old
cobblestone, 21 old iron blocks, 10 old diamond ores, 5 old glass and 3 old obsidian. The mod's loot
tables let them drop only themselves, and no recipe, no Fortune and no melting recipe takes them. Of
the 16 structure files in the jar only `data/chocolate_quest/structures/builder_village.nbt` holds them.

On 2026-11-04 at 17:33 the owner asked to replace the old blocks everywhere and to deploy the change.
The balance change was accepted with the request: every Builder Village now gives 21 real iron blocks
and 10 real diamond ores. The iron block, the diamond ore, the glass and the obsidian also melt in the
smeltery of Tinkers' Construct. Its jar `mods/TConstruct-1.20.1-3.12.1.231.jar` melts them through the
Forge tags `storage_blocks/iron`, `ores/diamond`, `glass/silica` and `obsidian`, in the recipes
`metal/iron/block.json`, `diamond/ore_singular.json`, `glass/block.json` and `obsidian/block.json` under
`data/tconstruct/recipes/smeltery/melting/`. Nobody tried that in a smeltery yet.

The production world on the Mac mini has not been converted yet.

## The blocks

| Old block | Becomes | Per village | Hardness, old and new |
|---|---|---:|---|
| Old cobblestone, `chocolate_quest:old_cobblestone` | Cobblestone | 48 | 1 and 2 |
| Old iron block, `chocolate_quest:old_iron_block` | Block of iron | 21 | 1 and 5 |
| Old diamond ore, `chocolate_quest:old_diamond_ore` | Diamond ore | 10 | 1 and 3 |
| Old glass, `chocolate_quest:old_glass` | Glass | 5 | 1 and 0.3 |
| Old obsidian, `chocolate_quest:old_obsidian` | Obsidian | 3 | 1 and 50 |

The five old blocks have no block states, emit no light and block all of it (light block 15, read from
the live block states on the test server). The four opaque replacements block light the same way.
Glass lets the sky light through, so light changes only where old glass was.

## How the change works

| Path of a block or item | Part | Where |
|---|---|---|
| New villages | A copy of the mod's template pool with a rule processor that turns each old block into its replacement | File `world/datapacks/skytekx3_fixes/data/chocolate_quest/worldgen/template_pool/builder_village.json` |
| Old blocks that still turn up, such as ones from the creative menu | Five loot tables at the mod's own paths, so an old block drops what its replacement drops | Folder `world/datapacks/skytekx3_fixes/data/chocolate_quest/loot_tables/blocks/` |
| Items that players carry or find | The unify script swaps the five old items for the replacements | Script `kubejs/server_scripts/unify_inventory.js` |
| The existing world | An offline tool for a stopped server that rewrites chunks, entities, player data and saved data | Script `tools/replace_blocks.py` |

The fixes pack must load after `mod:chocolate_quest` for the pool and the loot tables to win. On the
test server it sits at index 272 of 295 enabled packs and Chocolate Quest at 110, both counted from 0.

### Template pool

The pool keeps the single element of the mod, its fallback `minecraft:empty` and its rigid projection.
After the `minecraft:block_ignore` processor for structure blocks it gets a `minecraft:rule` processor
with one rule per old block: input `block_match`, location `always_true` and the replacement as output.

Minecraft saves the processors of every piece inside the structure start. A village whose start exists
before the change therefore keeps the old list for the chunks that generate later. The tool appends the
same rule processor to every saved Builder Village start, so those chunks come out right too.

### Loot tables

Four tables are copies of the vanilla tables of the server jar
(`libraries/net/minecraft/server/1.20.1-20230612.114412/server-1.20.1-20230612.114412-extra.jar`) with
only `random_sequence` changed. The diamond ore table gives the ore with Silk Touch and otherwise one
diamond with the Fortune bonus and explosion decay. The old glass table points at
`minecraft:blocks/glass`, so it drops what glass drops on this server. Quark replaces that table, and
with its Glass Shard module on (`config/quark-common.toml:523`) glass without Silk Touch drops 2 to 4
glass shards.

The loot command on the test server, with each block set by hand:

| Block and tool | Drops |
|---|---|
| Old diamond ore, iron pickaxe, twice | One diamond each time |
| Old diamond ore, Fortune III, four times | 2, 1, 1 and 4 diamonds |
| Old diamond ore, Silk Touch | One diamond ore |
| Old glass, iron pickaxe, three times | 2, 3 and 4 glass shards |
| Old glass, Silk Touch | One glass |
| Glass for comparison, iron pickaxe twice and Silk Touch once | 2 and 4 glass shards, then one glass |
| Old cobblestone, wooden pickaxe | One cobblestone |
| Old iron block, stone pickaxe | One block of iron |
| Old obsidian, diamond pickaxe, with and without Silk Touch | One obsidian each time |

Every answer for an old block named its table, for example "Dropped 1 [Diamond] from loot table
chocolate_quest:blocks/old_diamond_ore".

### Unify script

The script maps the five old ids to their replacements before it asks AlmostUnified, and swaps them at
the same moments as the other variants, see [AlmostUnified](../modpack/almostunified.md#inventory-converter).
Stacks with NBT stay as they are, as for every variant, so a renamed old block keeps its old id until
the tool converts the world. A probe on the test server called the script on a chest with 5 old
obsidian, 62 obsidian, 3 old glass, 64 old iron blocks, 12 old cobblestone, 2 old diamond ores and one
renamed old glass. It swapped 86 items. The obsidian topped up the stack of 62 to 64 and 3 stayed in the
first slot, and the renamed glass stayed old.

## The tool

```sh
python3 tools/replace_blocks.py --dry-run world   # Counts per file and id, writes nothing
python3 tools/replace_blocks.py world             # Rewrites, only while the server is stopped
python3 tools/replace_blocks.py --dry-run world   # Must report 0 to change
```

It needs Python 3.9 or newer and the standard library only. It walks the whole world folder, so every
dimension is included.

- In region chunks it renames the old blocks in the palette of every section. A rename can leave the
  same block twice in a palette, so it merges such entries and packs the block data again at the bit
  width of the shorter palette. It replaces every NBT string that equals an old id, such as items in
  chests, shulker boxes and other block entities. Every saved start of `chocolate_quest:builder_village`
  gets the rule processor.
- A chunk where old glass became glass loses its `isLightOn` flag. Starlight 1.1.2, the light engine
  of the server, then drops the stored light of that chunk when it loads it and lights it from scratch
  (`SaveUtil.loadLightHookReal` reads light only when the flag is there). Vanilla would run its light
  step again on top of the stored light.
- In entity chunks it replaces the same strings, which covers item entities, item frames, armor stands
  and minecarts.
- In the other NBT files (`.dat`, `.dat_old` and `.nbt`) it replaces them in player data with
  inventory, ender chest and curios, in saved data under `data/` such as backpacks, and in mod files
  such as the GraveStone deaths.
- It leaves `level.dat` and `level.dat_old` alone, because their Forge registry snapshot must keep the
  ids of the mod, which stays installed. It also skips `poi/`, the `datapacks/` folder, the `uid.dat`
  of each dimension (16 bytes, the world UUID of Arclight), compound keys and strings that only contain
  an old id, such as a sign text. The summary counts what it skipped, the files with other names that
  it passes over and the NBT files that do not read, and it lists the latter. With `--verbose` it lists
  the passed over files too. The converted test world gave 69 passed over files and no NBT file that
  does not read.
- Chunks without a change keep their bytes, their place in the file and their timestamp. A changed
  chunk goes back to its own sectors when it fits and to the end of the file otherwise. A chunk of
  256 sectors or more goes to a `c.X.Z.mcc` file as in vanilla, and a stale one is deleted.
- Every changed file is read back and checked, written to a temporary file in the same folder and
  renamed over the original.
- It refuses with exit code 2 while a server holds `world/session.lock` and holds the lock itself while
  it runs. A second run changes nothing and counts the starts that are already patched.

### Tests

The test server `skytest-oldblocks` ran the pack with Arclight on the build machine on 2026-11-04.
Before the run the world held 305 old blocks: three natural villages, one fully generated, one with only
its start saved and one partly generated, a village placed by command, test blocks with two light boxes
under roofs of old glass, and one old block each in the Nether, the End and the Skylands. Old items lay
in a chest, a shulker box in the chest, an item frame, an armor stand, a chest minecart, an item entity
and command storage, and in synthetic player, backpack and death files. Chunks in all five storage
forms held old blocks: zlib, gzip, uncompressed, a small and a big `.mcc` file.

| Check | Result |
|---|---|
| Dry run and run | 305 blocks, 26 strings with 236 items, 3 starts, 6 chunks to relight, 6 merged palette entries, 15 chunks in 14 files, 0 errors |
| Time of the run | 65.7 MB in 0.46 s with 42 MB of memory |
| Second dry run | 0 to change, 3 starts already patched |
| Files after the run | 11,334 unchanged chunks identical in bytes, place and timestamp, the compression of every chunk kept, the small `.mcc` file gone and its chunk inline |
| Run while the server was up | Refused with exit code 2, in both modes |
| Boot after the run | 0 FATAL and 1 ERROR, the AllTheLeaks line of every boot, no chunk or entity error and 9 failed recipes as before |
| Blocks after the boot | 305 of 305 positions hold the replacement, 0 old blocks in any probed chunk |
| Villages after the boot | All five villages, the four from before the run and one placed with the new pool after it, hold 21 iron blocks and 5 glass each, also in the parts that generated after the run |
| Light after the boot | Sky light 15 under the converted roof, 0 in the control box whose chunk kept its flag |
| Items after the boot | Converted in the chest, the shulker box, the item frame, the armor stand, the minecart, the item entity and command storage, and a renamed stack kept its name |
| After the stop | The start of the village that had only its start was saved again with the rule processor, and the final dry run found 0 to change |

The only old ids left are the 10 registry entries in each of `level.dat` and `level.dat_old` and the
sign text.

The same tool ran under Python 3.14.3 and under Python 3.9.25 (the container image `python:3.9-slim`)
on a copy of the seven region files of the old world on the build machine that hold Builder Villages,
with synthetic player and data files and four chunks in other storage forms. Both gave the same output
and the same content in all 6,490 chunks, and 6,470 unchanged chunks kept their bytes, place and
timestamp. The refusal under a held lock works under 3.9 too. The Python 3.9.6 of macOS on the Mac mini
ran the deploy of 2026-11-05, see [Deploy](#deploy).

### Run time

A dry run of the old world on the build machine (531.8 MB, 57,422 chunks, 6 built villages and one
start) took 3.1 s on the idle M1 Max and 5.5 s with a test server running next to it, which is 6 to
10 s per GB. A real run adds the rewrite of each changed region file: 9 changed files with about 90 MB
took 1.5 s including the reads. The two big region folders of the Halloween world hold about 1.4 GB, so
one pass on the Mac mini should take well under a minute. That was not measured there.

## Deploy

Stop the server and wait until its java process is gone. Clone the world, copy the tool, the unify
script and the six datapack files, then run the dry run, the real run and a second dry run. Check the
pack order in `level.dat`, start the server and check the boot log for chunk errors. To undo the run,
stop the server and put the clone back. The service commands of the Mac mini are on
[Mac mini](../server/macos.md).

The deploy of 3.1.3 did this on the Mac mini on 2026-11-05. The server stopped at 00:44:13 with nobody
online, and the clone of the world went to `~/skytekx3-backups/before-313-2026-11-05/` with the dry
runs and the run as `dry1.txt`, `run.txt` and `dry2.txt`. The system Python 3.9.6 of macOS ran the tool.

| Check | Result |
|---|---|
| Dry run | 595 files and 135,369 chunks, 1,125.9 MB in 6.2 s, 13 chunks in 14 files to change, 0 errors |
| Run | The same counts in 5.6 s: 144 cobblestone, 63 iron blocks, 20 diamond ores, 15 glass and 9 obsidian, 9 strings with 58 old diamond ores, 5 village starts, 3 chunks to relight, 7 merged palette entries |
| Second dry run | 0 to change, 5 starts already patched, no temporary file left |
| Passed over | 81 files with other names, no NBT file that does not read |
| Pack order | `mod:chocolate_quest` at 110, the fixes pack at 272, so the overrides win |
| Boot | Done at 00:46:19, 0 FATAL, no chunk or entity error, no line about Chocolate Quest |

Three villages held their old blocks, and one had lost its ten diamond ores already. The 58 old
diamond ores were eight in the inventory of one player, counted in the player file and in its
`.dat_old`, the same eight in five grave records of that player, and two items on the ground. They are
normal diamond ore now.

Related: [Boot fixes](boot-fixes.md), [AlmostUnified](../modpack/almostunified.md),
[Tools](../modpack/tools.md), [World reset and pregen](../server/world-reset.md).
