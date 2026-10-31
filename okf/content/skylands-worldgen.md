---
type: Feature
title: Skylands worldgen
description: The skytekx3:skylands floating islands dimension, a pure vanilla datapack generator that also runs on unmodded Spigot.
tags: [content, worldgen, skylands, datapack]
timestamp: 2026-10-31
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
the overworld sky, lets beds work, disables respawn anchors and allows raids. The noise settings
(`data/skytekx3/worldgen/noise_settings/skylands.json`) turn aquifers and ore veins off and set the sea
level to -64, so there is no sea and no free water from aquifers.

A single 2D island mask drives the shape. The density function `skytekx3:skylands/island_mask` is
`cache_2d` over a `shifted_noise` of the noise `skytekx3:island_mask` (first octave -7, amplitudes 1,
0.7 and 0.35) with `xz_scale` 0.25 and `y_scale` 0, so every column has one mask value. The noise
router uses that one reference three times:

1. As the terrain gate. The 3D noise `skytekx3:island_blob` (first octave -6, amplitudes 1 and 0.5)
   only counts where the mask is above 0.3.
2. As a guaranteed core slab of solid ground at about Y 52 to 77 where the mask is high. Without it the
   blob noise alone leaves about two thirds of the island columns empty (memory
   `project_skytekx3_vanilla_skylands.md`).
3. As the `continents` climate channel, so the biome source reads the mask as continentalness.

A vertical falloff keeps the islands in a band, with density fading below about Y 28 and above about
Y 90. In short:

```
final_density = max( min(island_blob, (mask - 0.3) * 6),
                     clamp((mask - 0.3) * 12.5, 0, 1) * slab(rises Y 38..56, falls Y 72..94) )
              + falloff(rises Y 12..28, falls Y 90..128)
```

Design notes kept in memory `project_skytekx3_vanilla_skylands.md`:

- The mask goes through `continents` because aquifer generation reads `erosion` and `depth`.
- It uses `cache_2d`. The first try with `flat_cache` produced terraces 4 blocks high.
- The island biomes are tiling temperature and humidity ranges, not climate points. Points left a void
  halo around island edges.

## Biomes

The multi noise biome source in `data/skytekx3/dimension/skylands.json` has 15 entries:

- The biome `skytekx3:sky_void` covers continentalness -2.0 to 0.5. It has no features, no carvers and
  no mob spawns.
- Fourteen island biomes cover continentalness 0.5 to 2.0 and tile the temperature and humidity plane:
  badlands, birch forest, dark forest, desert, flower forest, forest, jungle, plains, savanna, snowy
  taiga, sunflower plains, swamp, taiga and windswept hills (all in the `skytekx3` namespace).

So the line between island and void is decided by continentalness alone. The island biomes carry the
vanilla overworld ores and three contained liquid features from the shared pack:
`skytekx3:contained_spring_water` and `skytekx3:contained_spring_lava` (spring features with no open
side that need stone around them) and `skytekx3:water_pool` (a small pond patch on solid ground, about
one chunk in four). These replace free springs and lakes.

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

The only tags that list `skytekx3:sky_void` are these three, and they only hold structures with a fixed
start height in the air:

- The tag `#skytekx3:has_structure/sky_floaters` (the sky floaters).
- The tag `#dungeons_arise:has_structure/keep_kayra_biomes`.
- The tag `#dungeons_arise:has_structure/mechanical_nest_biomes`.

Hard rule: never add `sky_void` to `#minecraft:is_overworld`, `#forge:is_overworld` or another mod's
`has_structure` tag. Tag chains would bring the y=0 bug back, for example through AE2 meteorites
(memory `project_skytekx3_vanilla_skylands.md`).

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

Related: [Skylands travel](aether-fall.md), [world reset](../server/world-reset.md),
[performance](../server/performance.md), [mod set](../modpack/mod-set.md), [decisions](../decisions.md).
