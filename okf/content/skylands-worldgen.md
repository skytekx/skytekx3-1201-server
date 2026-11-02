---
type: Feature
title: Skylands worldgen
description: The skytekx3:skylands floating islands dimension, a pure vanilla datapack generator that also runs on unmodded Spigot.
tags: [content, worldgen, skylands, datapack]
timestamp: 2026-11-02
---

# Skylands worldgen

The Skylands are a separate dimension, `skytekx3:skylands`, with floating islands above an empty void.
Players get there by climbing above the overworld build limit and leave by falling off an island, see
[Skylands travel](aether-fall.md). The dimension replaces SB-Skylands, the skyblock plugin of SkyTekx 2,
which is a 1.12 Bukkit plugin with no port (memory `project_skytekx2_modpack.md:12` and `:30`).

Since 2026-07-12 the whole generator is plain datapack JSON with no mod code. The owner asked for it on
2026-07-12 20:39: "Please look for a more elegant version, it is extremely important to me that the same
world generator also works on an unmodded Spigot server." The older design that needed a mod is
summarised under [History](#history).

Owner quotes on this page come from `~/.claude/history.jsonl` (session 5bd3e1de, and session c0e872d8
for 2026-10-31) and are cited by local time.

## Owner requirements

- Smaller islands, a little further apart, so players can build bridges (2026-06-28 18:33).
- Island tops must not be flat (2026-06-28 18:33).
- Different biomes on different islands (2026-06-28 18:33).
- No water or lava may fall into the void. Liquids only appear in contained features (2026-06-28 19:58,
  which replaced the bedrock catch basin idea of 18:33 and 18:46).
- Small lakes on islands are fine as long as they never leak (2026-06-28 20:35).
- No structure may sit on the y=0 floor above the void (2026-06-29 08:26 and 08:30, 2026-06-30 04:06,
  2026-07-12 20:36), and nothing may be placed between the islands (2026-10-31 16:57).
- Many structures, flying ones above all, and common friendly structures such as villages and
  adventure houses (2026-06-29 11:15 to 22:03, 2026-06-30 03:57).
- Chocobos are common and live only in the Skylands (2026-06-30 03:55 and 04:01).
- The same generator must work on an unmodded Spigot server (2026-07-12 20:39).
- A new map with a light Halloween theme, not the world that is already generated
  (2026-10-31 16:56).
- Sleeping in the Skylands turns night into day in the overworld (2026-11-01 20:11, 2026-11-02 13:31,
  13:45, 14:04 and 14:07).
- Hostile mobs in the Skylands are weaker than in the overworld, because everybody builds a home there
  (2026-11-02 13:39). At 13:48 the owner asked for no hostile mobs there at all, and at 13:51 said that
  levels 1 to 5 would be fine but not level 50. At 14:16 the owner chose weak mobs over none.
- Nether portals work in the Skylands (2026-11-02 14:54 and 14:59).
- Standing rule since 2026-06-30 01:20: decisions that improve speed and reliability win
  (memory `skytekx3-performance-priority.md`).

## Where the files live

| Path | Role |
|---|---|
| Source folder [skylands-datapack/](../../skylands-datapack) | Shared source pack, vanilla safe (74 files, pack_format 15) |
| Source folder [skylands-datapack-forge/](../../skylands-datapack-forge) | Forge only source pack with the mod structures (32 files) |
| World copy `world/datapacks/skytekx3_skylands/` | Runtime copy of the shared pack that the server reads |
| World copy `world/datapacks/skytekx3_skylands_forge/` | Runtime copy of the Forge pack |
| Script [tools/sync_skylands_datapack.sh](../../tools/sync_skylands_datapack.sh) | Mirrors both sources into the world copies |

Edit only the two source folders, then run the sync script (see [Edit workflow](#edit-workflow)).

## Terrain

The dimension type (`data/skytekx3/dimension_type/skylands.json`) has `min_y` 0 and height 256, uses
the overworld sky, lets beds work, disables respawn anchors and allows raids. Its `fixed_time` of 6000
holds the sun at noon. The noise settings
(`data/skytekx3/worldgen/noise_settings/skylands.json`) turn aquifers and ore veins off and set the sea
level to -64, so there is no sea and no free water from aquifers.

The terrain is the one of June 2026 again since 2026-10-31 (commit 9a4dc12). `final_density` is the
function of commit 0f78e9d, unchanged:

```
final_density = island_blob(x, 2y, z) + min( gradient(Y 12: -2 to Y 28: -0.42),
                                             gradient(Y 90: -0.42 to Y 128: -2) )
```

The 3D noise `skytekx3:island_blob` (first octave -6, amplitudes 1 and 0.5) is read with `xz_scale` 1
and `y_scale` 2. Between Y 28 and Y 90 a block is solid where the noise is above 0.42. The two gradients
close the band, so no natural block is solid at Y 12 or below or at Y 128 or above. The noise settings
keep `legacy_random_source` true, so a seed gives the same islands as in June.

From 2026-07-12 to 2026-10-31 a 2D island mask with a guaranteed core slab shaped the islands (174fbf6).
That made them bigger, farther apart and flatter, which the owner rejected on 2026-10-31, see
[changes of 2026-10-31](#changes-of-2026-10-31).

The biome gate follows the terrain and does not shape it. The density function
`skytekx3:skylands/island_column` takes the June density of a column at the 28 heights Y 16, 20 and so
on up to Y 124, keeps the highest value, adds 0.8 and clamps the result to -2 and 2. The router reads it
as `continents`, with a gradient that forces the void below Y 8:

```
continents = min( island_column, gradient(Y 4: -1 to Y 8: 2) )
```

- A column with island terrain reads at least about 0.8, above the 0.5 that separates `sky_void` from
  the island biomes. The margin of 0.3 covers a thin sliver of terrain between two sampled heights,
  which the sampling can miss by about 0.15 at most. A block takes its biome from a quart corner up to
  about 6 blocks away, so without a margin the island rims would read `sky_void`.
- The constant was 1.1 from 2026-10-31 23:01 and is 0.8 since 2026-11-01 (commit 33a52d5). Throwaway
  worlds of radius 1000 with the production seed were counted per 4 by 4 column. With 0.5, 7.40% of
  all columns had terrain but read `sky_void`, so those rims lost their trees and snow. With 0.8 that
  share is 0.16%, and 42.58% of all columns read an island biome without terrain. With 1.1 the world
  of 23:13 read `sky_void` in only 0.51% of its columns. Over the void an island biome only changes
  weather and sky color, because the [void floor](#the-void-floor) keeps structures off it, and no
  test world had a block below Y 13.
- `island_column` is a `flat_cache`, so a chunk computes it 25 times when it fills its biomes. The July
  note that `flat_cache` gave terraces 4 blocks high was about the mask inside `final_density`. The gate
  only feeds the biome source, which works on quart corners anyway.
- Carvers read the biome at Y 0 of a chunk's corner, which the gradient makes `sky_void`. So `sky_void`
  carries the same three carvers as the 14 island biomes, in the same order (cb91870). Caves and canyons
  then start in every chunk with the same seeds as in June. In the void they carve nothing, because air
  is not replaceable.

Design notes kept in memory `project_skytekx3_vanilla_skylands.md`:

- The gate goes through `continents` because aquifer generation reads `erosion` and `depth`.
- The island biomes are tiling temperature and humidity ranges, not climate points. Points left a void
  halo around island edges.

## Biomes

The multi noise biome source in `data/skytekx3/dimension/skylands.json` has 15 entries:

- The biome `skytekx3:sky_void` covers continentalness -2.0 to 0.5. It has no features and no mob
  spawns. Since 2026-10-31 it has the three carvers of the island biomes, see [terrain](#terrain).
- Fourteen island biomes cover continentalness 0.5 to 2.0 and tile the temperature and humidity plane:
  badlands, birch forest, dark forest, desert, flower forest, forest, jungle, plains, savanna, snowy
  taiga, sunflower plains, swamp, taiga and windswept hills (all in the `skytekx3` namespace).

So the line between island and void is decided by continentalness alone. The island biomes carry the
vanilla overworld ores and three contained liquid features from the shared pack:
`skytekx3:contained_spring_water` and `skytekx3:contained_spring_lava` (spring features with no open
side that need stone around them) and `skytekx3:water_pool` (a small pond patch on solid ground, about
one chunk in four). These replace free springs and lakes.

Since 2026-11-02 (425d6113) none of the 15 biomes has precipitation, so it neither rains nor snows in
the Skylands, and thunderstorms bring no natural lightning there. The dimension still shares the
overworld weather, so its sky turns a little grey while it rains in the overworld, as over a vanilla
desert.

The 14 island biomes are also added to `#minecraft:is_overworld` and `#forge:is_overworld`. That is how
mod ores, features and mob spawns that target the overworld reach the islands, which the pack
description calls keeping "modded biomes and ores". The side effect is that any structure aimed at
`#minecraft:is_overworld` may also start on an island. Battle Towers and strongholds are excluded since
2026-10-31, see [changes of 2026-10-31](#changes-of-2026-10-31).

## No structures over the void

Vanilla checks the biome at a structure's start position before it places the structure
(`Structure.findValidGenerationPoint` and `isValidBiome`, checked in bytecode against Forge
1.20.1-47.4.18 per memory `project_skytekx3_vanilla_skylands.md`). The void biome is in almost no
`has_structure` tag, so every structure start above the void is rejected, modded ones included, with no
extra code.

Since 2026-10-31 the gate is the June terrain itself (see [terrain](#terrain)). A start above the
void lands at Y 0 by `WORLD_SURFACE_WG`, where the gradient makes the biome `sky_void` in every
column, so it is rejected even next to an island. The biome check cannot stop a structure that starts
on an island and reaches past its edge. Its jigsaw children and `terrain_matching` pieces over the
void follow the heightmap down to Y 0, and AE2 sinks a meteorite toward the lowest column nearby.
Since 2026-11-01 skytekx-compat stops all of these, see [the void floor](#the-void-floor).

The only tags that list `skytekx3:sky_void` are these three, and they only hold structures with a fixed
start height in the air:

- The tag `#skytekx3:has_structure/sky_floaters` (the sky floaters).
- The tag `#dungeons_arise:has_structure/keep_kayra_biomes`.
- The tag `#dungeons_arise:has_structure/mechanical_nest_biomes`.

Hard rule: never add `sky_void` to `#minecraft:is_overworld`, `#forge:is_overworld` or another mod's
`has_structure` tag. Tag chains would bring the y=0 bug back, for example through AE2 meteorites
(memory `project_skytekx3_vanilla_skylands.md`).

Five jigsaw placers of other mods never call the vanilla method that skytekx-compat redirects, so
their children over a void column would still land at Y 0. These are `ModifiedJigsawPlacement` of
When Dungeons Arise, `PieceLimitedJigsawManager` of Repurposed Structures, the placers of YUNG's API
and Cataclysm, and `ExtendedJigsawPlacement` of Structure Gel. The biome tags leave only two of their
structures able to start on an island: the lighthouse of When Dungeons Arise, whose pieces are all
rigid, and the exempt Keep Kayra. Keep the island biomes out of these mods' structure tags, and run
the void scan of [tools](../modpack/tools.md#region-scans) on a test world after any biome tag change.

## Structure sets

Every set is `minecraft:random_spread` with linear spread. Spacing and separation are in chunks.

| Set | Pack | Spacing and separation | Salt | Content |
|---|---|---|---|---|
| Sky floaters, `skytekx3:skylands_floaters` | Shared | 32 and 8 | 5318411 | Only `skytekx3:sky_balloon` |
| Sky floaters, `skytekx3:skylands_floaters` | Forge, overrides the shared one | 32 and 8 | 5318411 | The balloon plus 21 `skytekx3:sky_*` floaters |
| Island ground, `skytekx3:skylands_ground` | Forge | 14 and 5 | 7654321 | Ten Dungeons Arise surface structures |
| Villages, `minecraft:villages` | Shared | 24 and 8 (vanilla is 34 and 8) | 10387312 | The five vanilla village types |
| Swamp huts, `minecraft:swamp_huts` | Shared | 32 and 8 (vanilla values) | 14357620 | The swamp hut |
| Dungeons Arise majors, `dungeons_arise:major_structures` | Forge | 60 and 50 (as in the jar) | 88371663 | 27 of the jar's 31 structures |
| Dungeons Arise minors, `dungeons_arise:minor_structures` | Forge | 35 and 25 (as in the jar) | 342415935 | Only `abandoned_temple`, 1 of 6 |

The floaters are jigsaw structures with a fixed absolute start height and no projection to the
heightmap, so they always hang in the air. The balloon starts at Y 165. It was converted from the Ruins
hot air balloon template into a vanilla structure file (`data/skytekx3/structures/sky_balloon.nbt`,
commit 8afbfe1), so it is the only floater on vanilla and Spigot. The Forge pack adds 21 more with start
heights from Y 126 to 210 (`skylands-datapack-forge/data/skytekx3/worldgen/structure/`):

- From Dungeons Arise start pools (18): aviary, bandit towers, blimp, coliseum, corsair, galley, giant
  mushroom, heavenly challenger, heavenly conqueror, heavenly rider, illager fort, lighthouse,
  monastery, pirate ship, Shiraz palace, thornborn towers, windmill and wishing well.
- From Cataclysm start pools (3): abandoned temple, factory (ancient factory) and spire (abandoned
  spire).

The island ground set holds the Dungeons Arise mushroom house, merchant campsite, illager campsite,
wishing well, fishing hut, lighthouse, small prairie house, mushroom village, jungle tree house and
bathhouse. The Forge pack removes nine of them from the Dungeons Arise sets (four majors and five
minors), so they only generate through the ground set. The tenth, the small prairie house, is defined
in the Dungeons Arise jar, but no structure set in the jar places it, so it only exists through this set.
The shared pack adds the 14 island biomes to eleven Dungeons
Arise `has_structure` tags (nine ground structures plus keep_kayra and mechanical_nest). The illager
campsite lost its tag on 2026-10-31 and no longer generates on the islands.

Villages generate in matching island biomes: plains and sunflower plains, desert, savanna, snowy taiga
and taiga. Swamp huts generate in the swamp island. The village_snowy tag was missing before the
2026-07-12 rework (commit 4da4ee3).

The Forge pack also overrides two Botania placed features. Mystical flowers drop from count 2 at rarity
1 in 16 to count 1 at rarity 1 in 40, and mystical mushrooms move from an underground height range to
the surface heightmap at rarity 1 in 40. The reason for this change is not recorded (it arrived with
commit 0f78e9d).

### Structure sets are global

Structure sets apply in every dimension, not just the Skylands. Two side effects follow (code reader,
verified against `mods/DungeonsArise-1.20.x-2.1.58-release.jar`):

- The villages set at spacing 24 also applies to the overworld, which gives about twice the vanilla
  village density there.
- The ten ground structures keep their native Dungeons Arise biome tags (forest, plains, meadow and
  beach biomes), so in the overworld they now spawn on the 14 and 5 grid instead of the native 60 and 50
  or 35 and 25 grids. The small prairie house had no grid at all before.

This has been true since the first Skylands datapack (0f78e9d) and was never written down before.
Whether the denser overworld is wanted is an open owner decision.

## Pack split

Since Minecraft 1.19.3 a single registry reference that cannot be resolved stops the server from
booting (memory `project_skytekx3_vanilla_skylands.md`). The generator is therefore split in two:

- The shared pack holds the dimension, terrain, biomes, tags, contained features, the balloon, the
  village and swamp hut sets and the [travel functions](aether-fall.md). Its registry entries only use
  the `minecraft` and `skytekx3` namespaces. It was booted on vanilla and on BuildTools Spigot 1.20.1
  with zero registry errors on 2026-07-12 (okf/log.md, 2026-07-12 entry). The logs of those test servers
  were not kept.
- The Forge pack holds everything that names a mod: the 21 floaters and the full floaters set, the
  island ground set, the two Dungeons Arise set overrides and the Botania features.

The shared pack still carries five Forge biome modifier files (the chocobo ones below). Vanilla and
Spigot ignore them because their registry only exists on Forge, so they do not break those servers.

## Chocobos

The owner wants chocobos to be common in the Skylands (2026-06-30 03:55) and the whole chocobo mod to
spawn only there (2026-06-30 04:01). The shared pack does this with five files (commit daba422):

- The modifier `skytekx3:add_skylands_chocobos` (type `forge:add_spawns`) adds `chococraft:chocobo` at
  weight 30, in groups of 3 to 5, to the 14 island biomes.
- Overrides of ChocoCraft's own `add_plains_chocobos`, `add_mountain_chocobos` and
  `add_nether_chocobos` set them to `forge:none`.
- An override of `add_gysahl_green` points the chocobo food plant at the 14 island biomes.

The file `config/chococraft-common.toml` sets `chocoboSpawnWeight = 20` (raised from 10 in commit c2d3c5a).

### Known defect: the ChocoCraft overrides lose

A later data pack wins over an earlier one. In this world's `level.dat` (checked 2026-10-31,
`Data.DataPacks.Enabled`, 279 packs, 0-based index) `file/skytekx3_skylands` sits at 119 and
`mod:chococraft` at 140. The jar ships the same four files at the same paths
(`data/chococraft/forge/biome_modifier/`), so the jar's versions load:

- Chocobos are added to `#forge:is_plains`, `#forge:is_mountain` and `#minecraft:is_nether`.
- Gysahl greens are added to `#minecraft:is_overworld`, which includes the islands.

So chocobos most likely still spawn in the overworld and the Nether. Nobody has checked this in game.
The Skylands spawn itself works, because `add_skylands_chocobos` has its own path. The lesson that a
mid order pack loses to mods loaded after it was written down an hour after this change (commit d5e1449,
see [boot fixes](boot-fixes.md)).

Fix candidate, not done: move the five chocobo files into `skylands-datapack-forge/`, which loads last
(index 278), run the sync script, restart, and check in game that chocobos no longer spawn in overworld
plains, mountains or the Nether. This also makes the shared pack free of mod references. If the Mac mini
deploy copies this world and its `level.dat`, the defect moves with it until the fix lands. A fresh world
loads the file packs after all mod packs (commit 1f8f4ef), which should let the overrides win there.
Nobody has checked that in game.

## Sleep and hostile mobs

A KubeJS server script and two config entries set the rules for living in the Skylands. All of them went
live on the Mac mini on 2026-11-02 without a restart.

Sleep comes from `kubejs/server_scripts/skylands_sleep.js`. With its fixed time the dimension never
counts as day, so beds work there at any hour. When everybody sleeps, vanilla sets the time of the
Skylands only, and that time follows the overworld, so the night in the overworld went on. The script
moves the overworld to the next morning and ends its rain once enough players in the Skylands sleep, one
tick before vanilla wakes them. At day in the overworld the sleepers get the vanilla message instead. It
runs in a level tick, because Arclight replaces the Forge sleeping time check when a player lies down.
The rule `playersSleepingPercentage` is 0 since 14:10, so one sleeping player is enough in every
dimension. With the default of 100, a second player who was awake in the Skylands kept the night going,
which is why sleeping there still failed at 14:03. At 15:11:03 one of two players in the Skylands slept,
and the overworld stood at time 126, early morning, six seconds later.

Hostile mobs stay, but weak. The Skylands stay at noon without counting as day, so zombies and skeletons
do not burn there and gather around the homes up to the mob cap. A minute after they came back, 59
zombies, skeletons, spiders and creepers stood in the loaded part of the Skylands.
L2Hostility sets a mob's level from the base of its dimension, a random variation, the level of the
nearest player times a scale, and 0.003 levels per block of distance from the origin
(`config/l2_configs/l2hostility-common.toml`). The Skylands had no entry and used the default for mod
dimensions, base 20, variation 16 and scale 1.5. A player's level grows by 10 for each dimension the
player has visited, so mobs there reached about level 50. Since 14:26
`skylands-datapack-forge/data/skytekx3/l2hostility_config/difficulty/skylands.json` gives the Skylands
base 0, variation 1, scale 0 and no traits. Mobs sampled near the homes at 14:27 had levels 0 to 3, and
2,000 blocks out the distance alone adds 6. Since 14:28 `dimensionIDBlackList` in
`config/infernalmobs.cfg` keeps Infernal Mobs elites out of the Skylands.

L2Hostility reads its difficulty files only when the datapacks load, and Infernal Mobs reads its config
only at startup. One-shot KubeJS scripts therefore put the same values into the running server at
14:26:47 and 14:28:02, and the files take over at the next start. A `/reload` was avoided, because it
makes every client rebuild its EMI index. From 13:51 to 14:26 `kubejs/server_scripts/skylands_no_mobs.js`
refused every hostile mob in the Skylands and removed 70 on its first load. It went when the owner chose
weak mobs over none.

## Nether portals

Vanilla lights a nether portal only in the overworld and the nether, and Arclight checks the same with
its own level keys. A complete obsidian frame in the Skylands with a 2 by 3 inside therefore stayed shut
on 2026-11-02 at 14:54. Since 15:05 `kubejs/server_scripts/skylands_portals.js` opens such a frame one
tick after flint and steel or a fire charge was used on it, so the portal replaces the fire as it does in
the overworld. The portal then leads to the nether at an eighth of the coordinates, which vanilla does on
its own.

In the nether vanilla always leads to the overworld. The script keeps the dimension of the last portal a
player stood in outside the nether in the player data, under `skytekx3PortalOrigin`. A player who came
from the Skylands goes back there after 60 ticks in a nether portal, before vanilla would send the player
to the overworld after 80. The player lands at eight times the coordinates, in the closest portal within
128 blocks or in a new one that vanilla builds there. Everybody else still goes to the overworld. Players
in creative mode cross after one tick, before the script can act, so they land in the overworld. No
player has used it yet.

## Edit workflow

1. Edit `skylands-datapack/` or `skylands-datapack-forge/`. Anything that names a mod belongs in the
   Forge pack.
2. Run `tools/sync_skylands_datapack.sh`. It runs `rsync -a --delete` from each source into its world
   copy, so edits made directly under `world/datapacks/` are wiped. It does not touch
   `world/datapacks/skytekx3_fixes`.
3. Restart the server. Worldgen registries (dimension, noise, biomes, structure sets, biome modifiers)
   only rebind on a restart, not on `/reload` (commit c2d3c5a and okf/server/performance.md).
4. Remember that tag and spacing changes only affect chunks generated afterwards. To redo existing
   terrain use the Skylands only variant in [world reset](../server/world-reset.md), then pregenerate.
5. Commit the sources and the world copies. The world copies are tracked although `world/` is ignored
   (`.gitignore:2`), so new files there need `git add -f`.

The script `tools/build_mrpack.py` ships both source packs into the `.mrpack` as
`overrides/world/datapacks/skytekx3_skylands` and `overrides/world/datapacks/skytekx3_skylands_forge`,
from git HEAD since ce31f6f. Earlier on 2026-10-31 both world copies matched their sources
(`diff -rq` empty). The changes of 2026-10-31 below are not synced into this machine's world (checked
18:16).

## Checking a change

These tricks come from the 2026-07-12 work (memory `project_skytekx3_vanilla_skylands.md`):

- Over RCON the output of `say` is invisible. Use `execute if biome ...` or `execute if block ...`,
  which answer "Test passed" or "Test failed".
- Mind jigsaw rotation. A structure can extend in the negative direction from its anchor.
- Replacing air with air is a no op, so a fill with glass can count the non air blocks in a region.
- Locate one structure at a time. A sweep of `/locate` over every modded structure on the live server
  held the server thread for 60 seconds and tripped the watchdog on 2026-06-29 19:41 (see
  [logs and crashes](../server/logs-and-crashes.md)).

## Performance

Denser structures cost chunk generation time. Skylands pregeneration ran at 107 to 144 chunks per
second before the floaters were added and at 58 to 83 afterwards (Chunky lines in the server logs,
2026-06-29 and 2026-06-30). The latest pregen after the 2026-07-12 wipe covered radius 1000 (16,129
chunks) in 3 minutes 31 seconds at a median of 83.2 chunks per second. The density was raised on
purpose on 2026-06-30 (commit 801edb4) so that structures act as stepping stones between islands. The
profiles are in [performance](../server/performance.md).

On the Mac mini the June terrain generated at 135 to 171 chunks per second on 2026-10-31 from 23:02.
With the void floor of skytekx-compat 2.1.0 the pregen of 2026-11-01 ran at a median of 145.5 (radius
2000, 63,001 chunks in 7 minutes 13 seconds), so the mixins cost no time that shows in these rates.

The log of the last boot on the build machine shows 33 runtime warnings "Empty or non-existent pool:
dungeons_arise:mechanical_nest/mechanical_nest_decoration" (logs/2026-07-12-1.log.gz). They come from
the Dungeons Arise jar and have not caused a crash.

## Changes of 2026-10-31

On 2026-10-31 at 16:56 the owner asked for a new map with a light Halloween theme, and at 16:57 for a
check that nothing is placed between the islands. Two commits of 18:15 answer that:

- Commit 5ec6b72 gives all 14 island biomes two vegetation features from the shared pack:
  `skytekx3:halloween_pumpkins` (pumpkin patches of 40 tries on dirt, in 1 of 8 chunks) and
  `skytekx3:halloween_jack_o_lanterns` (jack o'lantern patches of 6 tries, in 1 of 32 chunks). They only
  use the `minecraft` and `skytekx3` namespaces, so the shared pack still loads on Spigot.
- Commit 1f8f4ef makes the Forge pack remove the 14 island biomes from the BrassAmber BattleTowers tags
  `ba_bt:has_structure/core_tower_biomes` and `land_tower_biomes` and from
  `minecraft:has_structure/stronghold`, through the Forge `remove` list. All three tags reach the
  islands through `#minecraft:is_overworld`. Its message reports that a scan of the old Skylands found a
  Battle Tower foundation reaching Y 1, and that a stronghold would be pushed to the bottom of the
  dimension.
- The same commit drops the shared pack's file that added the island biomes to
  `dungeons_arise:has_structure/illager_campsite_biomes`. The scan had found campsite tents at Y 1 to 4.
  The jar's own tag only names plains, meadow, sunflower plains and `#minecraft:is_hill`, so the illager
  campsite of the island ground set no longer generates on the islands, while the overworld keeps it.

The commit message also records a check on the Mac mini: a locate per tag in `skytekx3:skylands` found
no island biome left in them. It adds that a fresh world loads the three file packs after all mod
packs, in the last three of 280 places, which the remove lists need. The message counts from 0 and
says 277 to 279. The changes are not synced into
this machine's `world/datapacks/` (checked 18:16).

The Skylands of the Halloween world were then pregenerated to radius 3000 around the spawn on the Mac
mini (142,129 chunks) and read with the region scans of [Tools](../modpack/tools.md#region-scans). Of
268 structure starts none had a piece below Y 24. Below Y 24 the scan found 2,133 blocks. Most belong to
island bottoms, geodes and eight small islands that the terrain noise places low. Two leaks were left,
and two commits of 18:52 close them.

- Commit 0cf350a removes the Forbidden & Arcanus feature `forbidden_arcanus:petrified_root` from the 14
  island biomes with a `forge:remove_features` biome modifier in the Forge pack
  (`data/skytekx3/forge/biome_modifier/no_petrified_roots_on_islands.json`). Forbidden & Arcanus adds it
  to `#minecraft:is_overworld` at the `underground_ores` step. The feature looks up through up to 12
  blocks of air for a stone ceiling and places the root one to two blocks below it, so under an island
  the root floats. The scan found 124 such columns between Y 18 and 26. Forge applies removals after
  additions, so the pack order does not matter, and the overworld keeps its roots.
- Commit 7b51eb5 changes the floater `skytekx3:sky_windmill`. The fields of the Dungeons Arise windmill
  are `terrain_matching` pieces. Over the void they land below the jigsaw bounds and are dropped, which
  happened at 8 of the 10 windmills. At the other 2 an island lay under the windmill, so its fields
  landed on that island about 60 blocks lower, and field blocks past the island edge fell to Y 1. The
  structure now starts on the jigsaw at the top of the windmill's lower part
  (`start_jigsaw_name` `dungeons_arise:illager_windmill_1`). That centers the jigsaw bounds on the
  windmill, so `max_distance_from_center` 34 still holds both windmill parts and their spawners but no
  field below the base. The start height of 171 keeps the build at Y 139 to 202 as before. In vanilla
  1.20.1 the bounds are a box of that distance around the start in all three axes
  (`JigsawPlacement`, read in bytecode). The Dungeons Arise pools stay as they are, so overworld
  windmills keep their fields.

The Skylands were then wiped on the Mac mini at 18:54 and generated again with both fixes, to the same
radius 3000 (142,129 chunks from 18:55 to 19:15). The same scans at 19:20 found this.

- The same 268 structure starts, none with a piece below Y 24. The fixed scan of 4ec39e3 counts 296
  on the same files, because it also reads the 28 starts in chunks at the edge that are not full yet.
  None of them has a piece below Y 24 either.
- 1,826 blocks below Y 24 instead of 2,133. All 307 root blocks and the two field blocks at Y 1 are
  gone, and no petrified root is left anywhere in the dimension.
- The float scan finds eight bodies that hang from an island (island bottoms and amethyst geodes) and
  two that float free. Both are small islands of stone, dirt and grass between Y 22 and 37, which the
  first run had too. The other six small islands of the first run no longer reach below Y 24, since only
  their roots did.
- All 10 windmills have both parts and their 33 mob pieces, with the lowest box at Y 139 and no field
  piece.

### June terrain returns

At 22:18 and 22:22 the owner reported that the islands no longer looked like in June. The fix of July
had made them far bigger, farther apart and flatter, and the owner ruled that a fix must not change the
size, shape or spacing of the islands. Commits 9a4dc12, cb91870 and 9af7fae of 22:59 bring back the
June density, gate the biomes by the terrain and drop the island mask, see [terrain](#terrain).

On the Mac mini the Skylands of the Halloween world were wiped at 23:01 (backup
`~/skytekx3-backups/skylands-july-terrain-2026-10-31-2301.tgz`, 187,838,175 bytes) and pregenerated
with Chunky around the spawn to radius 3000. At 23:17:24, after 114,315 of 142,129 chunks (80.43%), the
server ran out of its 9 GB heap while it placed a structure piece in the Skylands
(`crash-reports/crash-2026-10-31_23.17.24-server.txt`). Launchd restarted it at 23:17:31, since the
job has `KeepAlive` true. Chunky does not resume a task after a restart here, so the rest of that world
generated only where players went. A pregen of radius 1000 on the build machine with an 8 GB heap kept
the old generation at about 3.0 GB after every full collection from 3,592 to 16,129 chunks, so the
heap does not grow with the number of chunks.

### The void floor

At 23:10 the owner reported, with two screenshots, that the Skylands still generated things between
the islands, especially flowers in the void. A scan of the June terrain at 23:13 found 60 of 528
structure starts with pieces below Y 13 and 54,386 blocks below Y 13. Most came from the merchant
camps of Dungeons Arise (29 of 29 starts), plains villages (11 of 21), taiga villages (9 of 15) and
AE2 meteorites (7 of 16). The camps brought the rose bushes that looked like flowers in the void. The
biome check of [no structures over the void](#no-structures-over-the-void) cannot catch these, because
they start on an island:

- A village street or house is a jigsaw child. Vanilla puts it at the first free height under its
  parent's connector, which over the void is Y 0.
- A merchant camp and a village street are `terrain_matching` pieces. Where they overhang an island
  edge, their blocks follow the heightmap of their own column down to Y 0.
- AE2 puts a meteorite at the mean height of the columns around it and sinks it toward the lowest one,
  which next to the void is Y 0.

skytekx-compat 2.1.0 adds three mixins that act only in this dimension, see
[SkyTekx Compat](compat-mod.md#skylands-void-floor). A jigsaw child over a void column is rejected
like a dead end. A block of a `terrain_matching` piece over a void column is dropped. A start is
cancelled when its first piece reaches below Y 13 or hangs over a void column, and a meteorite when
the void lies within 15 blocks of its chunk. Structures whose biomes include `sky_void` are left
alone: the floaters and the Keep Kayra and Mechanical Nest of When Dungeons Arise.

Version 2.1.1 closed three gaps that the review of 2.1.0 found. A swamp hut, desert pyramid or jungle
temple is also cancelled when any column under its box is void. The swamp hut sinks to the mean
height of its footprint and the desert pyramid to its lowest column, which counts a void column as
Y 0, and the swamp hut fills its stilts down to the ground. A meteorite is also cancelled when its sky
stone, which reaches 8 blocks below its center, would reach below Y 13. The exemption for `sky_void`
reads the structure's own biomes instead of the biome test its caller passes, so `/place structure`
gets the same checks as the worldgen.

Version 2.1.2 closes the two gaps that a review of 2.1.1 found. AE2's decay pass copies the lowest sky
stone one block further down, so a meteorite now counts from 9 blocks below its center. The start
check only sees the noise terrain, so a stilt over a column that is empty under the hut, below an
overhang or where a carver cut the island, still reached Y 2. A column that a structure piece fills
downward is now left out when the fill would not meet island ground at Y 13 or higher.

The mod was tested on a throwaway copy of the server on the build machine, with the production seed
and Chunky pregens around the spawn. The counts include the starts in chunks at the edge that are not
full yet.

| Test | Mod and gate | Full chunks | Starts | Floaters | Blocks below Y 13 |
|---|---|---|---|---|---|
| 1 | first jar, gate 0.5 | 6,561 | 63 | 9 | 2,569, all of one meteorite at Y 9 |
| 2 | meteorite rule, gate 0.5 | 16,129 | 123 | 16 | 0 |
| 3 | gate 0.8 | 16,129 | 126 | 16 | 0 |
| 4 | floaters exempt (db56da3), gate 0.8 | 16,129 | 135 | 25 | 0 |
| 5 | 2.1.1 (630fca4), gate 0.8 | 16,129 | 135 | 25 | 0 |
| 6 | 2.1.2 (26a493d), gate 0.8 | 16,129 | 135 | 25 | 0 |

Test 3 held one Keep Kayra and no Mechanical Nest. The world of 23:13 held 13 and 12 in 82,948 full
chunks, which makes about 2.5 and 2.3 per 16,129. The start check had also cancelled floaters whose
center hung over the void, so commit db56da3 of the mod exempts them. Test 4 held 3 Keep Kayra and 1
Mechanical Nest. Below Y 24 its float scan found 18 bodies that hang from islands (island bottoms and
amethyst geodes) and 5 specks of one or two blocks (clay, bone block, tin ore), but no part of a
structure.

Test 5 ran version 2.1.1 on the same seed and radius at 01:53 on 2026-11-01. Its starts match test 4
kind by kind, with 3 Keep Kayra, 1 Mechanical Nest and the one meteorite at Y 71, so the new rules
cancelled no start of the natural generation there. Then `/place structure` was tried over RCON in
the 16 by 16 chunks around chunk 19, 24 (blocks 304, 384), which sorted into 73 interior, 31 rim and
112 void chunks by 9 probe columns each. A swamp hut went into all 10 interior chunks tried, into 2 of
10 rim chunks and into none of 3 void chunks. A plains village over the void failed, and a sky balloon
over the void was placed, as intended. After these placements the scan still found no block below
Y 13, so neither rim hut dropped its stilts into the void. An AE2 meteorite failed in all 6 interior
chunks tried. No chunk of the area has a 3 by 3 chunk neighbourhood without a void probe column, so
the 15 block rule most likely rejected them.

Test 6 ran version 2.1.2 at 02:41, after the Skylands of the test world were deleted and generated
again from the same seed (3 minutes 18 seconds, median 85.8 chunks per second against 84.7 in test
5). Its starts, floaters, float scans and biome shares match test 5, and the `/place structure` set
of test 5 gave the same results. For the stilts, the 15 columns where a swamp hut can put a stilt
were carved out in chosen chunks before a hut was placed by command, once with 2.1.1 in the world
of test 5 and once with 2.1.2 in the same chunks of the new world. Carving from Y 13 to 127 removes
the blocks while the noise still reports terrain. With 2.1.1 the four stilts then reached Y 2, with
44 oak logs below Y 13 in each of 2 chunks. With 2.1.2 the same chunks held only the 16 logs of the
hut's corner posts. Carving only from Y 40 up leaves pits whose ground the stilts reach, and both
versions placed the same 48 logs in those columns, posts included. The scan after these placements
found no block below Y 13.

### Deploy of 2026-11-01

The server on the Mac mini stopped at 00:58:03 after a warning in chat. The Skylands of the Halloween
world were packed into `~/skytekx3-backups/skylands-gate-1.1-2026-11-01-0058.tgz` (352,266,314 bytes,
403 region files) and wiped. No player had spent more than about 5 minutes in any of their chunks
(`InhabitedTime`). skytekx-compat 2.1.0 of db56da3 replaced 2.0.0, which went to
`~/skytekx3-backups/jars-orig/`, the gate of 0.8 (33a52d5) was synced, and EssentialsXSpawn came in,
see [plugins](../modpack/plugins.md#spawn). The server was up at 00:58:59 with the 3 accepted boot
errors and logged "SkyTekx compat: birdsnests loot-table guard and Skylands void floor active".

Chunky then generated the Skylands around the spawn (308, 392) to radius 2000, 63,001 chunks in 7
minutes 13 seconds up to 01:06:21. Its rate lines show a median of 145.5 chunks per second and at most
200.1. A watch script read the heap during the run and would have paused Chunky if the old generation
had stayed above 7.5 GB. It stayed at about 3.4 to 3.7 GB. Two players joined at 01:00 while it ran.
The log holds 45 errors "No key selector" of `SculkSensorBlockEntity` from the pregen. The pregen of
23:02 without the mod logged 102 of them, so the mod does not cause them.

The region scans of [Tools](../modpack/tools.md#region-scans) read all 63,001 full chunks at 01:11 and
found this.

- 426 structure starts of 67 kinds, 69 of them in chunks at the edge that are not full yet. None has a
  piece below Y 13. Only floaters start in `sky_void` (16 starts). The merchant camps reach down to
  Y 29, and the one meteorite lies at Y 71.
- 93 floaters of 23 kinds, among them 11 Keep Kayra and 8 Mechanical Nests. Per 16,129 chunks that
  makes about 23.8 floaters, 2.8 Keep Kayra and 2.0 Mechanical Nests, close to test 4 (25 floaters)
  and to the world of 23:13 (2.5 and 2.3).
- No natural block below Y 13. 42.62% of the columns have terrain and an island biome, 42.66% an island
  biome without terrain, 14.43% `sky_void` without terrain and 0.29% `sky_void` with terrain.
- 10,446 blocks below Y 24. They form 69 bodies that hang from an island and 12 that float free. The
  free ones are 5 small islands of stone, dirt and grass between Y 21 and 39 (744 to 16,429 blocks),
  which the terrain noise places low, and 7 specks of one or two blocks (smooth basalt, clay, bone
  block, tin ore).

The Skylands beyond radius 2000 generate when players get there. A pregen to radius 3000 can follow
when nobody is online.

At 02:51 the server restarted after a warning in chat and ran skytekx-compat 2.1.2 from 02:52:03,
see [SkyTekx Compat](compat-mod.md#history). The Skylands were not generated again. Version 2.1.2 only
changes chunks that generate later, and the scan of 01:11 had found no block below Y 13 in the
pregenerated radius. Since 06:11 the server runs 2.1.3, whose worldgen code is that of 2.1.2. It only
adds the mixins for Infernal Mobs and for Falling Meteors, whose meteors fall in the overworld and
never in the Skylands.

## Open rulings

These owner rulings are tracked in [open items](../open-items.md). Only the first one has been
implemented, on 2026-10-31.

- On 2026-06-29 11:40 the owner ruled out Battle Towers with the explosion golem in the Skylands. On
  11:52 they called Battle Towers essential, and on 12:01 they named BrassAmber BattleTowers as the
  successor. The jar `ba_bt-1.20.1-3.0.0-beta3.2.1.jar` is installed on the server only. Its land and
  core towers use the biome tag `#minecraft:is_overworld`, which includes the 14 island biomes, so
  towers could start on islands, and a scan found one whose foundation reached Y 1. Commit 1f8f4ef
  removed the island biomes from both tags.
- The same prompt asks to turn off mob explosion damage to blocks in the Skylands, so that buildings
  stay intact. No gamerule, script, datapack or config does this. The vanilla `mobGriefing` rule is
  global, so a Skylands only rule needs a KubeJS explosion handler or a mod.
- On 2026-06-29 19:30 the owner wanted the structure at overworld x 0, y 110, z 451 in the Skylands too.
  Which structure that is was never recorded.

## History

- 2026-06-27: the first Skylands datapack shipped in mrpack 3.0.1 with 8 files, the dimension plus
  `/trigger` teleport functions. The owner found the islands too high and too stacked (21:41).
- 2026-06-28: a day of generator iterations with 22 short server runs (logs 2026-06-28-1 to -22, where
  file 1 is the newest).
  Bedrock catch basins under falling liquids were tried and then replaced by contained liquids only.
- 2026-06-29: structures used a custom placement type, `skytekx:surface_spread`, from the source built
  skytekx-worldgen mod (commit 0f78e9d). It was a `random_spread` variant that kept a candidate chunk
  only when the `WORLD_SURFACE_WG` height above the floor reached `min_surface_y` (default 16). It only
  checked the start chunk's center column, so a structure whose footprint reached a void column could
  still land on y=0. Raising `min_surface_y` from 16 to 36 fixed most cases. Floaters stayed on plain
  `random_spread` with a fixed start height.
- 2026-06-29 and 2026-06-30: floaters arrived in three batches. Commit 63ec22f added six reused Dungeons
  Arise pools at spacing 44. Commit 8afbfe1 converted the Ruins balloon and added seven more. Commit
  ac261f1 dropped the Ruins mod (its writer thread threw an exception every few minutes) and added eight
  more.
- 2026-06-30, night: a thinning pass (b07466b) removed six raw Dungeons Arise majors that duplicated
  floaters and the two heaviest Cataclysm builds (acropolis, soul_black_smith) from the Skylands tags.
  The owner ruled at 03:48 that removed structures must keep generating in their native dimensions,
  which they do. At 03:57 they asked for common friendly structures, so commit 801edb4 (04:18) made the
  sets denser: floaters from spacing 44 to 32, island ground from 18 to 14 and villages from 34 to 24.
- 2026-06-30 04:18: a keep_kayra was found at Y 0, so the Dungeons Arise major and minor sets also moved
  to `surface_spread` (e375d22). The rule of that time said that every set reaching a Skylands biome
  must use `surface_spread`. It has been void since 2026-07-12. Do not apply it.
- Mrpack 3.0.4 shipped a datapack whose island ground, villages and swamp hut sets needed
  `skytekx:surface_spread`, without bundling the mod that registers it (checked in the 3.0.4 `.mrpack`
  on 2026-10-31). A server built from 3.0.4 could not load the Skylands. The mod entered local-mods only
  in bf898e6, after that build.
- 2026-07-12: the vanilla rework (commits 174fbf6, 4da4ee3, dc8b470). The five `surface_spread` sets went
  back to `random_spread` with the same salts, the island mask and void biome replaced the placement
  check, the packs were split, and the mod shrank to [skytekx-compat](compat-mod.md). The Skylands were
  wiped (backup `~/skylands-prewipe-2026-07-12.tgz`, 126,854,054 bytes) and pregenerated to radius 1000.
- 2026-10-31: Battle Towers, strongholds and the illager campsite left the islands (1f8f4ef), and
  Halloween pumpkin patches arrived (5ec6b72), see [changes of 2026-10-31](#changes-of-2026-10-31).
- 2026-10-31, 23:01: the June terrain came back (9a4dc12, cb91870, 9af7fae), and the Skylands of the
  Halloween world were wiped and generated again, see [changes of 2026-10-31](#changes-of-2026-10-31).
- 2026-11-01, 00:58: the void floor of skytekx-compat 2.1.0 and the gate of 0.8 went live, and the
  Skylands were wiped and generated again to radius 2000, see
  [deploy of 2026-11-01](#deploy-of-2026-11-01).
- 2026-11-01, 02:51: skytekx-compat 2.1.2 replaced 2.1.0 on the Mac mini. It also keeps swamp hut stilts
  and the sky stone of meteorites out of the void.
- 2026-11-02: the Skylands became dry at 13:13, had no hostile mobs from 13:51, got sleep that turns the
  overworld to day at 14:10, weak mobs instead of none at 14:26 and nether portals at 15:05, see
  [biomes](#biomes), [sleep and hostile mobs](#sleep-and-hostile-mobs) and
  [nether portals](#nether-portals).

Related: [Skylands travel](aether-fall.md), [world reset](../server/world-reset.md),
[performance](../server/performance.md), [mod set](../modpack/mod-set.md), [decisions](../decisions.md).
