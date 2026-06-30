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

## 2026-06-29 — worldgen performance profile
Spark-profiled live chunk generation (overworld + Skylands) on the running server. Finding: lag is
**vanilla-noise-bound**, not memory and not a single mod. Vanilla terrain (~50–62 %) + JVM/GC overhead
(~30 %) dominate; the worst mod is Starlight (<1–3 %, itself a perf mod) and the custom Skylands worldgen
is invisible. No content mod is worth cutting — pregen wider instead. Full writeup:
[server/performance.md](server/performance.md).

## 2026-06-30 — Skylands structure-density follow-up
`/locate` sweep + a dense-region Spark profile found the Skylands **over-saturated**: one structure per
~210 blocks, fed by four overlapping layers (22 floaters + dense `skylands_ground` at spacing 18 + a
**redundant** raw-DA surface layer that duplicates 6 floaters + villages/swamp + two heavy Cataclysm
builds that do generate there). Per-structure spike verdict: **Cataclysm is not a CPU cost** (its code is
~0 %, all vanilla jigsaw assembly), but saturated regions generate ~2.4× slower and **triple GC**. Decisive
thinning is specified (widen `skylands_ground` 18→32, drop the raw-DA + Cataclysm Skylands biome tags) in
[server/performance.md §6](server/performance.md); left for the pack owner to apply (gameplay edit).

## 2026-06-30 — Skylands thinned (light touch, applied)
Owner chose the lowest-regret slice and it is now live. Removed the 6 raw-DA structures that duplicate a
sky floater (`aviary`, `bandit_towers`, `thornborn_towers`, `heavenly_conqueror`, `heavenly_challenger`,
`heavenly_rider`) and the 2 heaviest Cataclysm builds (`acropolis`, `soul_black_smith`) from the Skylands
by deleting their `has_structure` biome tags in both datapack copies (16 files). Kept the 22 floaters, the
`skylands_ground` DA layer, the villages and `sky_abandoned_temple`. Tag-only edit, so only not-yet-generated
chunks change and it is fully reversible. Verified after a restart: all 8 no longer `/locate` in the
Skylands but still generate in their native dimensions (End, Nether, overworld ocean or badlands), so
nothing was deleted from the game, only un-duplicated out of the sky. The bigger `skylands_ground` spacing
widen stays available for a later pass. Details: [server/performance.md §6.4](server/performance.md).

## 2026-06-30 — Skylands denser + chocobos + y0 dungeon fix
Reversed course on thinning per the owner: a densely-populated skylands actually helps island-to-island
traversal (structures are stepping-stones and landmarks). Tuned `skylands_floaters` 44→32,
`skylands_ground` 18→14 and `minecraft:villages` 34→24 so friendly content (adventure houses + villager
houses) is common. Made the whole chocobo mod skylands-only: a `forge:add_spawns` modifier adds
`chococraft:chocobo` (weight 30) to the 14 skylands biomes, chococraft's overworld/mountain/nether
spawn modifiers are overridden to `forge:none`, and gysahl green is retargeted to the skylands. FIXED a
y0 leak: `dungeons_arise:major_structures` and `minor_structures` were still `minecraft:random_spread`
(projecting DA dungeons onto the y0 void floor — a player found a keep at Y0), now converted to
`skytekx:surface_spread` (min_surface_y 36) like every other skylands set. Details:
[content/skylands-worldgen.md](content/skylands-worldgen.md).

## 2026-06-30 — Boot-log cleanup (third-party parse spam)
Added the `skytekx3_fixes` datapack (enabled LAST so it outranks the mod datapacks) that neutralises broken
third-party data: 14 malformed advancements and 99 loot tables (items/types absent in 1.20.1) replaced with
valid no-op/empty overrides. Fixed two configs: apotheosis dimdoors:string_theory power range (was inverted
and unobtainable) and collective update checker off. Deleted a stray `minecraft:loot` table from the ad_astra
jar (a 4-char path that crashed Bird's Nests' unguarded substring). Boot ERRORs dropped 152 to ~27. KEY
LESSON: a world datapack that sits mid-order (like skytekx3_skylands) loses override priority to mods loaded
after it, so content overrides must live in a dedicated pack enabled `last`.

## 2026-06-30 — Boot-log cleanup part 2 (152 to 10 errors)
Resolved most of the remaining third-party parse spam by INSTALLING the missing optional dependencies the
mods expect, not only neutering. Installed SlashBlade:Resharped, Re:Avaritia and Medieval Embroidery (added
to modrinth.index.json), which TiCEX and DragNs Livestock expected. Bird's Nests' unguarded-substring crash
is fixed durably by a mixin in the skytekx worldgen mod (BirdsnestsDecayLeafFixMixin), patched from our own
mod so it survives re-resolves. Jar/config fixes that mods/ would overwrite on re-resolve live in
tools/patch_mods.py (run it AFTER tools/resolve_all.py): mixin minVersion additions, orphan compat-mixin
removals for absent mods, the ticex reconstruction-material catalyst trim, Medieval Embroidery broken
predator tags, and the ad_astra stray loot table. The 10 remaining errors are benign or irreducible: 5
TiCEX tools for content not fully present (Avaritia gem mismatch, TaCz guns intentionally absent), a cofhcore
client class on a dedicated server, an optional The One Probe integration, the simplyjetpacks Patchouli book
(old format), an AllTheLeaks reflection, and the EssentialsX/Bukkit hybrid warning (kept by choice). Boot:
152 to 10 ERRORs, 589 to ~311 WARNs, 0 FATAL.

## 2026-06-30 — Boot-log cleanup part 3 (down to 3 benign errors)
Installed the rest of the deps the owner asked for. The One Probe (Modrinth) fixes the arsmagicalegacy
TOPCompat error. BUILT Aether: Genesis from its GitHub 1.20.1-develop branch (no Modrinth release) by
authenticating the GitHub Packages maven with GITHUB_TOKEN, shipped the no-embeds jar in local-mods; it
provides the swet_jelly items aether-redux's tag needed. Made the ticex singular_gem and blitz_gun tools
load by dropping their unregistered catalyst part + station slot (Re:Avaritia and TaCz integrations are
absent or incompatible). Moved the cofhcore and aether_genesis client-only mixins into their client arrays
(they errored on a dedicated server). All jar fixes live in tools/patch_mods.py. Boot is now 152 to 3
ERRORs (98% down): only the simplyjetpacks Patchouli book (pre-1.20 format, mod-author fix), an AllTheLeaks
reflection into Citadel (benign, not Nyx), and the accepted EssentialsX warning remain.

## 2026-06-30 — Removed Re:Avaritia
Re:Avaritia was installed to satisfy ticex's gem-tinkers integration, but ticex 0.6.0 is code-incompatible
with Re:Avaritia 1.4.0 (its Avaritia module targets a different build), so the gem catalyst never registered
and the mod added nothing usable. Removed from mods + modrinth.index.json. The ticex singular_gem tools stay
loadable (their fix drops the catalyst part, independent of Avaritia). Removing a mod from the live world
logs one-time "Unidentified mapping" errors on the first boot that clear on the second. Boot stays at 3
benign errors.
