---
type: Feature
title: Aether-style fall
description: Falling off a Skylands island teleports the player down to the overworld.
tags: [content, skylands, kubejs, gameplay]
timestamp: 2026-06-29
---

# Aether-style fall

Like the Aether: drop off the edge of a [Skylands](skylands-worldgen.md) island and instead of dying in
the void, you **fall to the overworld**.

## Implementation
[`kubejs/server_scripts/skylands_fall.js`](../../kubejs/server_scripts/skylands_fall.js):

- Per-tick, a cheap `getY() >= 0` gate runs first (almost always true → near-zero cost).
- Only when a player in dimension `skytekx3:skylands` drops **below Y 0** (skylands `min_y=0`, no terrain
  below ~y12, void-death at y-64 → a comfortable buffer that still respects a player-built platform in
  0–255) does it `ServerPlayer.teleportTo(overworld, x, 300, z, yaw, pitch)` and message the player.
- Built on the native Mojmap API (same style as `combat_1_7.js` — see [KubeJS](../modpack/kubejs.md)).

## Testing
**Load-verified** (boot, 0 errors). The actual cross-dim teleport only fires on a real void-fall, so it
can't be exercised with no players online — verify in-world by jumping off an island.

Related: [skylands worldgen](skylands-worldgen.md), [KubeJS](../modpack/kubejs.md), [world reset](../server/world-reset.md).
