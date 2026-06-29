---
type: Reference
title: KubeJS scripts
description: Server-side recipe/tag/gameplay scripts — the modern CraftTweaker.
tags: [modpack, recipes, kubejs, scripting]
timestamp: 2026-06-29
---

# KubeJS scripts

`kubejs/server_scripts/` holds the gameplay scripting (ported from the 1.12.2 CraftTweaker `.zs`, which
is kept read-only in [`scripts-legacy-reference/`](../../scripts-legacy-reference) for reference).

| Script | Purpose |
|---|---|
| `recipe_fixes.js` | Recipe corrections / unification helpers (smelts, alternates). |
| `tag_fixes.js` | Tag normalisation. |
| `gameplay.js` | Gameplay tweaks (gear, balance). |
| `combat_1_7.js` | 1.7-style combat — restores the old swing, removes the attack cooldown via the attack-speed attribute. |
| `skylands_fall.js` | The [Aether-style void-fall](../content/aether-fall.md). |

## API note
These use the **native Mojmap** API (e.g. `combat_1_7.js`'s attribute pattern, `skylands_fall.js`'s
`ServerPlayer.teleportTo`). Mirror an existing script when adding a new one. Load-verify on boot
(0 errors), then test behaviour in-world — cross-dim / runtime effects only fire on the real event.

Related: [AlmostUnified](almostunified.md), [aether fall](../content/aether-fall.md).
