---
type: Log
title: Change log
description: Chronological history of major SkyTekx 3 milestones.
tags: [log, history]
timestamp: 2026-06-29
---

# Change log

Oldest → newest.

## 2026-06-27 — scaffold
Arclight 1.20.1 / Forge 47 / Java 17 hybrid stood up. The 1.12.2 recipe-consistency strategy was
ported: UniDict → [AlmostUnified](modpack/almostunified.md), NoMoreRecipeConflict → Polymorph,
CraftTweaker → [KubeJS](modpack/kubejs.md). Mod set assembled from
[`modrinth.index.json`](modpack/mod-set.md).

## 2026-06-29 — Nyx + worldgen overhaul + all-dimension regen (v3.0.4)
- **[Nyx](content/nyx.md)** fully ported to 1.20.1 (server + client + index, env `client`+`server` required).
- **[Skylands worldgen](content/skylands-worldgen.md)**: fixed the y0 void-floor structure leak
  (`min_surface_y` 16 → 36) and wired many more floating + ground structures.
- **[Aether-style fall](content/aether-fall.md)**: dropping off a Skylands island teleports you to the overworld.
- **[All dimensions regenerated](server/world-reset.md)** (same seed) + Chunky pregen of overworld + skylands.
- [AlmostUnified](modpack/almostunified.md) mod priorities finalised.
- [Published](modpack/publish.md) Modrinth v3.0.4 (project itself still in `draft`).
