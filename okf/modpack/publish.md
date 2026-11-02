---
type: Runbook
title: Publish the modpack
description: Build the .mrpack and upload it to Modrinth.
tags: [modpack, release, modrinth]
timestamp: 2026-11-02
---

# Publish the modpack

```bash
python3 tools/build_mrpack.py     # builds SkyTekx3-<ver>.mrpack from the index + overrides
```

- Bundles every [Modrinth-resolvable mod](mod-set.md) from `modrinth.index.json` plus the
  `local-mods/` overrides and the [`skylands-datapack/`](../content/skylands-worldgen.md).
- Ships `modrinth.index.json` exactly as committed and never rewrites it. It refuses to build when the
  index's `versionId` or Forge version differs from `VERSION`/`FORGE` in the script, or when a path is
  listed twice. The `WROTE` line must report the index's file count (225 for 3.1.0). Before 3.1.0 the
  script preferred a `tools/resolve_all.py` scratch list when one existed and wrote it over the index,
  which silently dropped every index-only mod (such as the TF addons).
- `python3 tools/publish_modrinth.py <PAT> --go` uploads it as a new version to Modrinth project
  **`skytekx3`** using the PAT from the [vault](../server/secrets.md) (`vault_modrinth_pat`). Loader
  Forge, MC 1.20.1. Without `--go` it is a dry run.

The version is hard-coded in three places, bump them together: `VERSION` in `tools/build_mrpack.py`
(the `.mrpack` name and `versionId`), `VERSION` in `tools/publish_modrinth.py` (the file it uploads and
the Modrinth `version_number`) and `versionId` in `modrinth.index.json`.

## Before uploading
1. **Permissions.** Every `local-mods/` jar needs a [`modrinth-permissions/`](../../modrinth-permissions/README.md)
   file whose **Status** starts with `OK`. `build_mrpack.py` checks this first and refuses to build
   otherwise; `--allow-unpermitted` builds a private/draft pack that must not be uploaded. As of 3.1.0,
   five BLOCKER jars and one VERIFY jar (Deadly World) still stop a public build.
2. **Source links.** Merge the branch that carries `ports/` into `master` and push it. The LGPL/GPL
   source offers of the modified builds (the TF fork, Spiced Twilight, Alternative Twilight (Remastered))
   point at `https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/...` from their
   permission files, NOTICEs and mods.toml credits. Then check that
   `git ls-tree origin/master ports/twilightforest ports/spiced-twilight ports/alternative-twilight-remastered`
   lists all three and that the three `tree/master/ports/...` URLs open without logging in (the repo is
   public). Do not rewrite the URLs: they are baked into the SHA-pinned jars.

## State
- Last upload recorded here: **3.0.4** (213 index files + overrides; `.mrpack` ≈ 50 MB). An upload of
  3.0.5 (master, vanilla Skylands rework) is not recorded.
- Next version: **3.1.0** (225 index files + overrides; the [TF 4.8 backport](twilightforest-backport.md)
  and its addons). Not uploaded yet; blocked by the two checks above.
- The Modrinth **project itself is still in `draft`** — uploaded versions aren't publicly listed until
  someone publishes the project.

Related: [mod set](mod-set.md), [secrets](../server/secrets.md).
