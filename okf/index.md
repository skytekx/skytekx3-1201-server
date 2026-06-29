---
type: Project
title: SkyTekx 3
description: Minecraft 1.20.1 / Forge 47 / Arclight hybrid modpack + server — the gen-3 SkyTekx rebuild.
tags: [skytekx, minecraft, modpack, overview]
timestamp: 2026-06-29
---

# SkyTekx 3

Gen-3 rebuild of the SkyTekx community modpack (1.7.10 → 1.12.2 → **1.20.1**), run as a
**Forge 47 + Bukkit hybrid** on **Arclight** so the EssentialsX / LuckPerms / Multiverse / WorldEdit
plugin layer rides alongside the Forge mods. Ships a bespoke **custom Skylands floating-islands dimension**.

This bundle is the knowledge base for agents working on the pack. Three subsystems:

- **[Server](server/index.md)** — running, deploying, and operating the Arclight server.
- **[Modpack](modpack/index.md)** — mod set, recipe-consistency engine, and the publish pipeline.
- **[Content](content/index.md)** — the custom mods/features (Nyx, Skylands worldgen, Aether fall).

See also [`../README.md`](../README.md) (self-hosting) and [`../AGENTS.md`](../AGENTS.md) (conventions),
plus the [change log](log.md).

## At a glance
- MC 1.20.1 · Forge 47.x · Arclight · **Java 17 only** (crashes on 21).
- Recipe engine: AlmostUnified + Polymorph + KubeJS. Viewer: EMI.
- Source of truth for mods: `modrinth.index.json` (+ `local-mods/` for non-Modrinth jars).
- Published: Modrinth `skytekx3` v3.0.4.
