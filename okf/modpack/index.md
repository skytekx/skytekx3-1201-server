---
type: Index
title: Modpack
description: Mod set composition, recipe-consistency engine, and the publish pipeline.
tags: [modpack, mods]
timestamp: 2026-06-29
---

# Modpack

What ships in the pack and how its recipes are kept conflict-free.

- **[Mod set](mod-set.md)** — `modrinth.index.json` (source of truth) + `local-mods/` (non-Modrinth jars).
- **[AlmostUnified](almostunified.md)** — material unification (the modern UniDict).
- **[KubeJS scripts](kubejs.md)** — recipe / tag / loot / gameplay tweaks (the modern CraftTweaker).
- **[Publish](publish.md)** — build + upload the `.mrpack` to Modrinth.

## Recipe-consistency engine
The original 1.12.2 layered conflict-prevention ported 1:1 in concept:
UniDict → **[AlmostUnified](almostunified.md)**, NoMoreRecipeConflict → **Polymorph**,
CraftTweaker → **[KubeJS](kubejs.md)**. Recipe viewer is **EMI**.

Custom content (Nyx, the Skylands dimension) lives under [content/](../content/index.md).
