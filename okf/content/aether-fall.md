---
type: Feature
title: Aether-style travel
description: Fall off a Skylands island to reach the overworld, climb past the overworld build ceiling to ascend.
tags: [content, skylands, datapack, gameplay]
timestamp: 2026-07-12
---

# Aether-style travel

Like the Aether, both directions, and the **primary** way to switch dimensions:

- **Descend**: drop off a [Skylands](skylands-worldgen.md) island below Y 0 and you fall into the
  overworld sky at the same x/z (arrive at Y 300, keep falling).
- **Ascend**: fly or pillar past the overworld build ceiling (Y 319) and you arrive in the Skylands
  standing on a 3x3 dirt platform at the void floor (feet Y 1).

## Implementation
Pure vanilla **datapack functions** in the shared pack (`skylands-datapack/data/skytekx3/functions/travel/`),
so the mechanic works identically on Forge, unmodded Spigot, vanilla and singleplayer. This replaced the
Java `SkylandsFallHandler` when the worldgen mod was retired.

- Detection via entity **predicates** (`location_check`, exact feet Y, no hitbox error): `in_skylands_void`
  (y below 0), `at_overworld_ceiling` (y at or above 319, riders excluded so Ad Astra rockets pass through).
- `sktx_cd` scoreboard: 40 tick cooldown after any teleport (anti retrigger belt and braces, the primary
  guard is threshold separation: arrivals land at Y 1 and Y 300).
- `sktx_pend` scoreboard: platform retry loop. `setblock` fails in unloaded chunks, so `ensure_platform`
  retries each tick (budget 100) with `if loaded` until the arrival chunk is server loaded, always before
  the client can start falling.

## Testing
Verified on the throwaway Spigot 1.20.1 and vanilla test servers (functions load, tick runs clean,
scoreboards created) and on the live Arclight boot. The actual cross dim teleports need a real player,
verify in world by jumping off an island and by pillaring above Y 319.

Related: [skylands worldgen](skylands-worldgen.md), [world reset](../server/world-reset.md).
