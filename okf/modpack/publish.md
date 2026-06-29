---
type: Runbook
title: Publish the modpack
description: Build the .mrpack and upload it to Modrinth.
tags: [modpack, release, modrinth]
timestamp: 2026-06-29
---

# Publish the modpack

```bash
python3 tools/build_mrpack.py     # builds SkyTekx3-<ver>.mrpack from the index + overrides
```

- Bundles every [Modrinth-resolvable mod](mod-set.md) from `modrinth.index.json` plus the
  `local-mods/` overrides and the [`skylands-datapack/`](../content/skylands-worldgen.md).
- Uploads a new version to Modrinth project **`skytekx3`** using the PAT from the
  [vault](../server/secrets.md) (`vault_modrinth_pat`). Loader Forge, MC 1.20.1.

## State
- Current version: **3.0.4** (213 index files + overrides; `.mrpack` ≈ 50 MB).
- The Modrinth **project itself is still in `draft`** — uploaded versions aren't publicly listed until
  someone publishes the project.

Related: [mod set](mod-set.md), [secrets](../server/secrets.md).
