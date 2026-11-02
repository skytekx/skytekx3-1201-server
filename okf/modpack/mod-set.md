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
(225 files at v3.1.0), each with its env (`client` / `server` `required|optional|unsupported`), hashes,
and download URL. To add or remove a Modrinth mod, **edit the index**, not `mods/`.

## Derived vs tracked
- `mods/` — **derived**, gitignored. Assembled from the index plus `local-mods/` as in
  [README step 4](../../README.md#4--clone-the-pack), with no token: copy `local-mods/*.jar`, download
  every `files[].downloads[0]` whose `env.server` is not `unsupported` and check its `sha1`, then run
  `tools/patch_mods.py`. `tools/resolve_all.py` is legacy: it needs the Modrinth PAT, resolves its own
  hard-coded `SLUGS` list into the original build host's paths and does not read
  `modrinth.index.json`, so entries added only to the index (such as the TF addons) are not picked up.
- `client-only-mods/` — derived, gitignored (split out of `mods/` for the server).
- `local-mods/` — **tracked**. Mods built from source / not on Modrinth (the Twilight Forest fork from
  `ports/twilightforest/`, [Nyx](../content/nyx.md), …). These are part of the pack and ride into the `.mrpack` as overrides.

## Bundling a non-Modrinth jar (redistribution)
A jar in `local-mods/` is redistributed inside the `.mrpack`, so Modrinth review requires an
attribution for each one. [`modrinth-permissions/`](../../modrinth-permissions/README.md) holds one
`<jar>.md` per bundled jar (permission reason, link, license, explanation) and the overview table.
- Only bundle a jar whose license allows redistribution (open license, or a stated modpack permission).
  Check the CurseForge project license, the source repository LICENSE and the jar's mods.toml. MDK
  leftovers such as `license="All rights reserved"` in an LGPL project are common. CurseForge's
  `allowModDistribution` flag only covers its own launcher API.
- Keep the jar byte-identical to the upstream file and record the file id and SHA-1 in its `.md`.
  The exception is a backport we build ourselves from `ports/<name>/` (see below).
- If a mod is on Modrinth, put it in the index instead (even when All Rights Reserved), because the
  file then comes from the Modrinth CDN.
- All Rights Reserved without written permission stays out (e.g. Twilight Spellbooks, see the
  [TF backport](twilightforest-backport.md#pending-permission)).

The TF addons Twilight Construct, Twilight Tweaks and The King of Twilight are bundled this way. The
other TF addons are index entries; see the [TF backport](twilightforest-backport.md#in-the-pack-2026-11-01).

A backport built from source (a 1.21.1-only mod ported to 1.20.1) cannot be byte-identical to an upstream
file. Its license must allow redistributing a modified build. Keep its source in `ports/<mod>/` with the
upstream license file and a NOTICE (upstream URL, commit, changes), ship both in the jar, make the jar
build reproducible, and record the upstream commit, the build command and the jar SHA-1 in its
`modrinth-permissions/` file instead. Bundled this way: Twilight Mobs Loots (`ports/twilight-mobs-loots/`,
MIT), Spiced Twilight (`ports/spiced-twilight/`, LGPL-3.0-or-later, so the modified source stays public
in that directory) and Alternative Twilight (Remastered) (`ports/alternative-twilight-remastered/`,
GPL-3.0-or-later, same rule) and Twilight Crossbow (`ports/twilight-crossbow/`, MIT code with CC BY-NC-SA 4.0 item textures adapted from
Twilight Forest's bows, credited in its NOTICE; its jar build does not
fix file times yet, so its file records the SHA-1 of the committed jar). All four need the fork TF jar
(`ports/twilightforest/`), which `local-mods/` carries instead of the stock `4.3` jar; see the
[TF backport](twilightforest-backport.md#backported-1211-only-addons).

## ⚠️ Known gap
A few non-Modrinth jars are referenced but not yet bundled in `local-mods/` nor given a manual index
entry: `alltheleaks`, `ba_bt`, `cataclysm_spellbooks`. A fresh clone won't have them until they
are added to `local-mods/`. Fix before relying on a clean rebuild.

Note: the former `skytekx-worldgen` mod is retired. Skylands worldgen is
[pure vanilla datapack](../content/skylands-worldgen.md) now; the slim `skytekx-compat` jar in
`local-mods/` only carries compat mixins (Birds Nests loot crash guard).

Related: [AlmostUnified](almostunified.md), [publish](publish.md), [Arclight](../server/arclight.md).
