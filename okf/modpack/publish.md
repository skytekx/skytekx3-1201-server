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
  Forge, MC 1.20.1. Without `--go` it is a dry run that prints the changelog it would send.
- The version's changelog on Modrinth is the file `docs/changelog/<VERSION>.md`. `publish_modrinth.py`
  refuses to run when that file is missing or empty. (Until 3.1.0 the changelog was a string inside the
  script, and the 3.1.0 bump would have uploaded the 3.0.4 Nyx/Skylands text.)

The version is hard-coded in four places, bump them together: `VERSION` in `tools/build_mrpack.py`
(the `.mrpack` name and `versionId`), `VERSION` in `tools/publish_modrinth.py` (the file it uploads and
the Modrinth `version_number`), `versionId` in `modrinth.index.json`, and a new
`docs/changelog/<VERSION>.md` for the new version.

## Before uploading
1. **Permissions.** Every `local-mods/` jar needs a [`modrinth-permissions/`](../../modrinth-permissions/README.md)
   file with exactly one **Status** line that starts with the word `OK` (`OK`, `OK (author allows)`).
   Both scripts check this through `tools/mrpack_permissions.py`. `build_mrpack.py` checks
   `local-mods/` first; on a refusal it also deletes any `SkyTekx3-<ver>.mrpack` left from an earlier
   build. `--allow-unpermitted` writes a private/draft pack named `SkyTekx3-<ver>-UNPERMITTED.mrpack`,
   which must not be uploaded. `publish_modrinth.py` opens the `SkyTekx3-<ver>.mrpack` it would upload
   and refuses, in the dry run too and with no override, when a jar inside it lacks permission or the
   pack's `versionId` is not `VERSION`. As of 3.1.0, five BLOCKER jars and one VERIFY jar (Deadly World)
   still stop a public build.
2. **Source links.** Merge the branch that carries `ports/` into `master` and push it. The LGPL/GPL
   source offers of the three modified builds point into this repo, but not all in the same way:
   - Spiced Twilight and Alternative Twilight (Remastered) carry
     `https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/{spiced-twilight,alternative-twilight-remastered}`
     in their jar NOTICE and mods.toml credits (and in their permission files). Do not rewrite these
     URLs: they are baked into the SHA-pinned jars.
   - The TF fork's jar NOTICE names only the repo root and points at the history of
     `ports/twilightforest` at the commit that added the jar to `local-mods/` (c825572); its mods.toml
     has no SkyTekx link. The `tree/master/ports/twilightforest` URL is only in its
     `modrinth-permissions/` file, which can still be edited. Check that c825572 is on master:
     `git merge-base --is-ancestor c825572 origin/master`.

   Then check that
   `git ls-tree origin/master ports/twilightforest ports/spiced-twilight ports/alternative-twilight-remastered`
   lists all three and that the three `tree/master/ports/...` URLs open without logging in (the repo is
   public).
3. **Changelog.** Check that `docs/changelog/<VERSION>.md` exists and describes this release, for
   everything since the last upload recorded under *State* (3.1.0 also covers the never-uploaded 3.0.5).
   If a permission blocker is resolved by dropping a jar, drop it from the changelog too. Use absolute
   GitHub URLs, not relative `okf/` links, which break on Modrinth. The dry run prints the text.

## State
- Last upload recorded here: **3.0.4** (213 index files + overrides; `.mrpack` ≈ 50 MB). An upload of
  3.0.5 (master, vanilla Skylands rework) is not recorded.
- Next version: **3.1.0** (225 index files + overrides; the [TF 4.8 backport](twilightforest-backport.md)
  and its addons; changelog `docs/changelog/3.1.0.md`). Not uploaded yet; blocked by the permission and
  source-link checks above.
- The Modrinth **project itself is still in `draft`** — uploaded versions aren't publicly listed until
  someone publishes the project.

Related: [mod set](mod-set.md), [secrets](../server/secrets.md).
