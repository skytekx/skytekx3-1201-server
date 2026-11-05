# Upstream: Create: Integrated Farming (1.20.1 Forge port base)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Create: Integrated
Farming, the base of the SkyTekx port of the mod from NeoForge 1.21.1 down to Forge 1.20.1.

| Field | Value |
|---|---|
| Repository | https://github.com/DragonsPlusMinecraft/CreateIntegratedFarming |
| Ref | Branch `1.21.1/6.0.0-dev`, the only branch. The repository has no tags, and `HEAD` points at this branch |
| Commit | `1f63780108d1d51f3b2c9ebdf4bb0cd5cb3f5906` ("[version] Release 1.4.3") |
| Commit date | 2026-09-21T14:16:01+08:00 |
| Upstream version | `mod_version = 1.4.3` (Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Create: Dragons Plus 1.11.9) |
| Matching release | Modrinth version `EWfVwbHy`, `create-integrated-farming-1.4.3.jar`, SHA-1 `27cbb530828aa375450d005426e609d96cb254e9`. All 285 Java files of its sources jar are byte-identical to this commit |

Every file in the snapshot is byte-identical to `git archive 1f63780` of the upstream repository,
including the upstream `.github/`, `.idea/` and `codeformat/` files. The changes made on top of the
snapshot are separate commits in this repository's history (`git log -- ports/createintegratedfarming`).

## License

- Code: GNU Lesser General Public License version 3 or later. `LICENSE.txt` is the verbatim LGPL 3.0
  text followed by the GPL 3.0 text, with no additional terms. `gradle.properties` says
  `mod_license = LGPL-3.0-or-later`, and every Java file carries the header of
  `codeformat/HEADER.txt`: "Copyright (C) 2025 DragonsPlus" and
  "SPDX-License-Identifier: LGPL-3.0-or-later". The Modrinth project (`9k1pAsfR`) lists
  LGPL-3.0-only. The fork meets the GPL 3.0 terms for conveying a modified work, which satisfies
  every reading of the labels.
- Textures from Farmer's Delight: `textures/block/farmersdelight/` (the basket and the safety net)
  are copies of Farmer's Delight textures under the MIT License, Copyright (c) 2020 vectorwing.
  `FARMERS_DELIGHT_LICENSE.txt` next to them stays in the source and in the jar.
- Parts from Create: `CustomHarvestBehaviour#harvestBlock` adapts Create's
  `BlockHelper.destroyBlockAs` (marked `@CodeReference(... license = "mit")`), and
  `vacuum_harvester.png` contains pixel blocks of Create's `cam_linkage.png` and
  `rotation_speed_controller.png`. Both Create blobs predate Create commit `9dcc21950`
  (2025-09-21), which made Create's assets All Rights Reserved, so they came under Create's earlier
  "MIT License, Copyright (c) 2019 simibubi". The fork ships that notice as `LICENSE-CREATE.txt`.
- Not verified: `textures/block/chicken.png` looks like pixel art derived from the vanilla chicken,
  a Mojang asset that the Minecraft usage guidelines govern like other mod textures. Nobody compared
  it with the vanilla files.
