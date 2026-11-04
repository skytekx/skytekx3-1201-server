# Upstream: Create Crafts & Additions (1.20.1 backport base)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Create Crafts &
Additions, the base of the SkyTekx 1.20.1 Forge backport of the mod's 1.21.1 line.

| | |
|---|---|
| Repository | https://github.com/mrh0/createaddition |
| Ref | branch `1.20.1` |
| Commit | `42b6f2d06f0412a7cee787d767449d5b1b3b81b8` ("Merge pull request #1200 from Texaliuz/patch-1") |
| Commit date | 2026-11-02T23:02:03+01:00 |
| Upstream version | `mod_version = 1.3.3` (Minecraft 1.20.1, Forge 47.4.0, Create 6.0.8-289) |
| Port source (later commits) | branch `1.21.1` @ `658a390b8ff4daa17013e9447b29949330c8a42a` (2026-11-02, `mod_version = 1.7.3`; release 1.7.2 on 2026-10-01) |

Every file in the snapshot is byte-identical to `git archive 42b6f2d` of the upstream repository,
including the upstream `.github/`, `.vs/` and screenshot files. Changes made on top of the snapshot
are separate commits in this repository's history (`git log -- ports/createaddition`).

## What the base contains beyond the shipped 1.3.3 jar

The pack shipped `createaddition-1.20.1-1.3.3.jar` from Modrinth (built 2025-11-10). Its resources
are identical (line endings aside) to upstream commit `838cfab` (2025-11-10, "Merge pull request
#1088 ... Fixed PEI visual in Forge"), which is the 1.3.3 release point. The base commit
`42b6f2d` adds these merged but unreleased upstream changes, all of them wanted:

- `8e996ec` (PR #1189, merged in `a82f60e`): the Liquid Blaze Burner needs a Straw when a
  Schematicannon prints it (`LiquidBlazeBurnerBlockEntity#getRequiredItems`).
- `7ce2204` (PR #1167, merged in `0077170`): new barbed wire block model and texture.
- Translations: `ae01f10` ru_ru (PR #1061), `2fc2bcf` new vi_vn, `d246b82` / `3c4b57e` /
  `8bf7647` zh_cn (PR #1106), `257b673` es_ar (PR #1200).

No registry id, block state, NBT key or config key changed between 1.3.3 and the base.

## License

- Code and assets: MIT License, `Copyright (c) 2024 MRH` (`LICENSE`, kept unchanged; `mods.toml`
  says `MIT License, Copyright 2022 MRH0`). `LICENSE` also notes that the sounds come from
  pixabay.com (Pixabay Content License) and freesound.org (CC0). The Pixabay sounds may only ship
  inside the mod jar, never as standalone files.
- `RollingMillBlockEntity` contains code "taken and adapted from the Create repo", and a few
  textures (rolling mill casing, digital adapter, portable energy interface, copper wire casing, the
  seed oil and bioethanol buckets) are edited copies of Create textures. All of them were added to
  CC&A between 2021 and 2023-05, when Create was MIT-licensed as a whole (`Copyright (c) 2019
  simibubi`). Create's assets are All Rights Reserved since Create commit `9dcc21950`
  (2025-09-21): the backport must not copy any Create asset dated 2025-09-21 or later.
- Upstream jars carry no LICENSE file. The SkyTekx build adds the licence texts and a NOTICE to the
  jar (see the build changes below).

## Build changes

None in the snapshot itself. Build changes are listed here as they are made.
