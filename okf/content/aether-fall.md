---
type: Feature
title: Skylands travel
description: Fall off a Skylands island to reach the overworld, or climb past the overworld build ceiling to reach the Skylands, done with plain datapack functions.
tags: [content, skylands, datapack, gameplay]
timestamp: 2026-10-31
---

# Skylands travel

Players move between the overworld and the [Skylands](skylands-worldgen.md) vertically, in the style of
the Aether mod. There is no portal and no command. On 2026-06-30 04:24 the owner made this the primary
way to switch dimensions: flying or pillaring high enough in the overworld must land the player on a
dirt block at Y 0 in the Skylands, and falling out of the Skylands must lead back to the overworld
(`~/.claude/history.jsonl`, session 5bd3e1de). On 2026-10-31 16:58 they restated it: stacking dirt or any
other block high enough must take the player to the flying islands (session c0e872d8). The page keeps
its old file name `aether-fall.md` so that existing links work.

## How it works for players

| Direction | Trigger | Arrival |
|---|---|---|
| Down | Feet below Y 0 in the Skylands, after falling off an island | Overworld at the same x and z, Y 300, still falling |
| Up | Feet at Y 319 or higher in the overworld, by flying or pillaring, not while riding | Skylands at the same x and z, Y 1, standing on a new 3x3 dirt platform at Y 0 |

Both directions send an aqua chat line. The way down says "You fell from the Skylands...". The way up
announces the arrival in the Skylands. From the platform players build or fly up to the islands, which
float at roughly Y 28 to 90.

The two arrival heights sit inside the safe band on purpose. A player who arrives at Y 1 is above the
Skylands trigger, and a player who arrives at Y 300 is below the overworld trigger, so nobody bounces
back. A cooldown of 40 ticks backs this up.

## Implementation

The mechanic is plain datapack functions in the shared pack, so it behaves the same on Forge (Arclight),
vanilla and Spigot. It replaced the Java handler of the retired worldgen mod on 2026-07-12 (commit
4da4ee3).

| File in `skylands-datapack/data/` | Role |
|---|---|
| Tag `minecraft/tags/functions/load.json` | Runs `skytekx3:travel/load` on every datapack load |
| Tag `minecraft/tags/functions/tick.json` | Runs `skytekx3:travel/tick` every server tick |
| Function `skytekx3/functions/travel/load.mcfunction` | Creates the dummy objectives `sktx_pend` and `sktx_cd` |
| Function `skytekx3/functions/travel/tick.mcfunction` | Counts cooldowns down, retries pending platforms, starts ascend and descend |
| Function `skytekx3/functions/travel/descend.mcfunction` | Sets the cooldown, teleports to the overworld at Y 300, sends the message |
| Function `skytekx3/functions/travel/ascend.mcfunction` | Sets the cooldown and the platform budget, teleports to the Skylands at Y 1, sends the message, places the platform |
| Function `skytekx3/functions/travel/ensure_platform.mcfunction` | Fills the 3x3 dirt platform at Y 0 once the arrival chunk is loaded |
| Predicate `skytekx3/predicates/travel/in_skylands.json` | The entity is in `skytekx3:skylands` |
| Predicate `skytekx3/predicates/travel/in_skylands_void.json` | In the Skylands with feet at Y -0.1 or lower |
| Predicate `skytekx3/predicates/travel/at_overworld_ceiling.json` | In the overworld with feet at Y 319 or higher and no vehicle |

The tick function does four things in this order (`tick.mcfunction:2-5`):

1. It lowers `sktx_cd` by one for every player whose cooldown is still running.
2. It runs `ensure_platform` for every player with a pending platform (`sktx_pend` 1 or more).
3. It runs `descend` for players matched by `in_skylands_void`, unless their cooldown runs or a platform
   is still pending.
4. It runs `ascend` for players matched by `at_overworld_ceiling`, unless their cooldown runs.

The predicates use `location_check` on the player's feet position, so the check needs no hitbox math.
The overworld check skips riders so that Ad Astra rockets can fly past Y 319 to reach orbit (memory
`project_skytekx3_vanilla_skylands.md:20`).

### Platform retry loop

A `fill` does nothing in a chunk that is not loaded yet, and right after a cross dimension teleport the
arrival chunk often is not. So `ascend` sets `sktx_pend` to 100 and calls `ensure_platform` once. Each
call lowers the budget by one and, if the player is in the Skylands and the chunk at the player is
loaded, fills `~-1 0 ~-1 ~1 0 ~1` with dirt and clears the score. The tick function repeats the call
every tick until it succeeds or the budget of 100 ticks (5 seconds) runs out. While a platform is
pending, descend is blocked, so a player who drops below Y 0 before the platform exists is not thrown
back to the overworld at once.

The fill replaces whatever is in that 3x3 area at Y 0, including blocks a player placed there.

## Testing status

- The tick function loads and runs on the live Arclight server. An RCON test run on 2026-07-12 logged
  "Executed 4 command(s) from function 'skytekx3:travel/tick'" (`logs/2026-07-12-1.log.gz:3415`,
  23:29:23).
- The shared pack also booted on throwaway vanilla and BuildTools Spigot 1.20.1 servers on 2026-07-12
  ([log](../log.md), 2026-07-12 entry). The logs of those test servers were not kept.
- No player has joined since the rework. The last boot (`logs/2026-07-12-1.log.gz`, Done at 23:26:00)
  and `logs/latest.log` (up to 2026-07-13 15:45) contain no join line. So neither direction has been
  tested in game with the datapack functions. The memory note of the rework says the same
  (`project_skytekx3_vanilla_skylands.md:22`).
- The owner confirmed the way down on 2026-06-30 04:24 ("great"), but that was the older Java handler.

To test, join the server, jump off an island and fall below Y 0, then pillar or fly above Y 319 in the
overworld. Check that the dirt platform appears under you.

## Open points

- Fall damage: neither these functions nor the retired Java handler reset the fall distance or give
  slow falling, and the drop starts at Y 300. A survival player usually falls more than 200 blocks after
  the teleport. Whether that landing is meant to be survivable was never recorded or tested.
- Singleplayer: the `.mrpack` puts the datapacks under `overrides/world/datapacks/`, which only matches
  a server folder. A singleplayer save keeps its packs in `saves/<world>/datapacks/`, so a Prism
  singleplayer world has neither the Skylands nor this travel unless the player copies both packs in
  by hand (see [publish](../modpack/publish.md)).
- Both points are listed in [open items](../open-items.md).

## Mac mini

Nothing here depends on the host. The functions live in the world's `datapacks` folder. They move with
the world when it is copied to the Mac mini, and for a new world `tools/sync_skylands_datapack.sh` on
the Mac writes them into it. That script writes into `world/datapacks/`, so the new world must keep the
level name `world` (`server.properties:25`), see [macOS host](../server/macos.md) and
[world reset](../server/world-reset.md). After either, `/datapack list enabled` should show
`file/skytekx3_skylands` and `file/skytekx3_skylands_forge`.

## History

- 2026-06-27: mrpack 3.0.1 shipped the first Skylands datapack, with `/trigger sktx_skylands` and
  `/trigger sktx_overworld` commands that teleported onto a safe obsidian platform and stored the entry
  position (memory `project_skytekx2_modpack.md:30`).
- 2026-06-29 18:52: a KubeJS script, `kubejs/server_scripts/skylands_fall.js`, added the fall from the
  Skylands to the overworld (commit 573cc25). Commit 20bc022 (19:28) tried to fix its Rhino property
  access.
- 2026-06-29 20:10: the script never worked. Rhino resolved `level()`, `dimension()` and `location()`
  wrongly and threw a `TypeError` every tick a player was below Y 0, so players fell out of the world
  and the log took 126 such errors in one session. The fall moved into the skytekx-worldgen mod as the
  Forge tick handler `SkylandsFallHandler`, and the script was deleted (commit bf898e6).
- 2026-06-30 04:37: after the owner's ruling the handler also got the way up to a 3x3 dirt platform, and
  the `/trigger` functions, their function tags and the `sktx` objectives were removed (commit 2eceeb1).
- 2026-07-12: the vanilla rework replaced the Java handler with the datapack functions above (commit
  4da4ee3), and the handler was deleted from the mod (skytekx-worldgen commit 2b49bba, see
  [compat mod](compat-mod.md)). The thresholds, heights and messages stayed the same.

Related: [Skylands worldgen](skylands-worldgen.md), [world reset](../server/world-reset.md),
[KubeJS](../modpack/kubejs.md), [open items](../open-items.md).
