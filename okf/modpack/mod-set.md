---
type: Reference
title: Mod set
description: How the mod list is composed — Modrinth index (source of truth) + local-mods overrides.
tags: [modpack, mods, modrinth]
timestamp: 2026-06-29
---

# Mod set

## Source of truth
[`modrinth.index.json`](../../modrinth.index.json) is the **manifest** — every Modrinth-resolvable mod
(213 files at v3.0.4), each with its env (`client` / `server` `required|optional|unsupported`), hashes,
and download URL. To add or remove a Modrinth mod, **edit the index**, not `mods/`.

## Derived vs tracked
- `mods/` — **derived**, gitignored. Rebuilt from the index by `tools/resolve_all.py` (needs the
  Modrinth PAT, see [secrets](../server/secrets.md)).
- `client-only-mods/` — derived, gitignored (split out of `mods/` for the server).
- `local-mods/` — **tracked**. Mods built from source / not on Modrinth (Twilight Forest,
  [Nyx](../content/nyx.md), …). These are part of the pack and ride into the `.mrpack` as overrides.

## ⚠️ Known gap
A few non-Modrinth jars are referenced but not yet bundled in `local-mods/` nor given a manual index
entry: `skytekx-worldgen` (provides the [Skylands `surface_spread`](../content/skylands-worldgen.md)
placement!), `alltheleaks`, `ba_bt`, `cataclysm_spellbooks`. A fresh clone won't have them until they
are added to `local-mods/`. Fix before relying on a clean rebuild.

Related: [AlmostUnified](almostunified.md), [publish](publish.md), [Arclight](../server/arclight.md).
