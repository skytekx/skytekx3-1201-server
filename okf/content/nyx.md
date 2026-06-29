---
type: Mod
title: Nyx
description: Ellpeck's night/lunar content mod, forked and ported to 1.20.1 for SkyTekx 3.
resource: https://github.com/skytekx/nyx
tags: [content, mod, nyx, worldgen]
timestamp: 2026-06-29
---

# Nyx

A night/lunar content mod (lunar events, star/meteor materials). Forked to
**[github.com/skytekx/nyx](https://github.com/skytekx/nyx)** (MIT, from `Ellpeck/Nyx`) and **ported to
1.20.1** for this pack.

## Facts
- Modid `nyx` (`de.ellpeck.nyx`), **side = BOTH** (a real server+client content mod, not a client stub).
- Jar: `local-mods/nyx-1.20.1-1.0.0.jar` (tracked) — also present in the client mods dir.
- [`modrinth.index.json`](../modpack/mod-set.md) entry: env **`client: required` + `server: required`**.
- Registers lunar blocks/fluids/items: `nyx:lunar_water`, `nyx:star_air`, `nyx:crystal`,
  `nyx:meteor_rock`, … Boot log confirms `de.ellpeck.nyx.Nyx` loads.

## History
Started life as a client-only stub; the full port (server + client) replaced it on 2026-06-29 — the old
8,974-byte stub was retired. See the [log](../log.md).

Related: [mod set](../modpack/mod-set.md).
