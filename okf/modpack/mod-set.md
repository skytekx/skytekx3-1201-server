---
type: Reference
title: Mod set
description: How the mod list is composed — Modrinth index (source of truth) + local-mods overrides.
tags: [modpack, mods, modrinth]
timestamp: 2026-11-02
---

# Mod set

## Source of truth
[`modrinth.index.json`](../../modrinth.index.json) is the **manifest** — every Modrinth-resolvable mod
(213 files at v3.0.4), each with its env (`client` / `server` `required|optional|unsupported`), hashes,
and download URL. To add or remove a Modrinth mod, **edit the index**, not `mods/`.

## Derived vs tracked
- `mods/` — **derived**, gitignored. Rebuilt from the index by `tools/resolve_all.py` (needs the
  Modrinth PAT, see [secrets](../server/secrets.md)). Caveat: `resolve_all.py` resolves its own
  hard-coded `SLUGS` list and writes to the original build host's paths. It does not read
  `modrinth.index.json`, so entries added only to the index (such as the TF addons) are not picked up.
  To rebuild `mods/` exactly from the index, download every `files[].downloads[0]` whose `env.server`
  is not `unsupported`, check `sha1`/`sha512`/`fileSize`, add `local-mods/*.jar`, then run
  `tools/patch_mods.py`.
- `client-only-mods/` — derived, gitignored (split out of `mods/` for the server).
- `local-mods/` — **tracked**. Mods built from source / not on Modrinth (Twilight Forest,
  [Nyx](../content/nyx.md), …). These are part of the pack and ride into the `.mrpack` as overrides.

## Bundling a non-Modrinth jar (redistribution)
A jar in `local-mods/` is redistributed inside the `.mrpack`, so Modrinth review requires an
attribution for each one. [`modrinth-permissions/`](../../modrinth-permissions/README.md) holds one
`<jar>.md` per bundled jar (permission reason, link, license, explanation) and the overview table.
- Only bundle a jar whose license allows redistribution (open license, or a stated modpack permission).
  Check the CurseForge project license, the source repository LICENSE and the jar's mods.toml. MDK
  leftovers such as `license="All rights reserved"` in an LGPL project are common. CurseForge's
  `allowModDistribution` flag only covers its own launcher API.
- Keep the jar byte-identical to the upstream file and record the file id and SHA-1 in its `.md`.
- If a mod is on Modrinth, put it in the index instead (even when All Rights Reserved), because the
  file then comes from the Modrinth CDN.
- All Rights Reserved without written permission stays out (e.g. Twilight Spellbooks, see the
  [TF backport](twilightforest-backport.md#pending-permission)).

The TF addons Twilight Construct, Twilight Tweaks and The King of Twilight are bundled this way. The
other TF addons are index entries; see the [TF backport](twilightforest-backport.md#in-the-pack-2026-11-01).

A backport built from source (a 1.21.1-only mod ported to 1.20.1) cannot be byte-identical to an upstream
file. Keep its source in `ports/<mod>/` with the upstream LICENSE and a NOTICE (upstream URL, commit,
changes), make the jar build reproducible, and record the build command and SHA-1 in its
`modrinth-permissions/` file instead. Example: Twilight Mobs Loots (`ports/twilight-mobs-loots/`).

## ⚠️ Known gap
A few non-Modrinth jars are referenced but not yet bundled in `local-mods/` nor given a manual index
entry: `alltheleaks`, `ba_bt`, `cataclysm_spellbooks`. A fresh clone won't have them until they
are added to `local-mods/`. Fix before relying on a clean rebuild.

Note: the former `skytekx-worldgen` mod is retired. Skylands worldgen is
[pure vanilla datapack](../content/skylands-worldgen.md) now; the slim `skytekx-compat` jar in
`local-mods/` only carries compat mixins (Birds Nests loot crash guard).

Related: [AlmostUnified](almostunified.md), [publish](publish.md), [Arclight](../server/arclight.md).
