# Expanded Delight (1.20.1 Forge port by SkyTekx)

Bundled file: `expandeddelight-0.1.4-skytekx.1.jar`
Status: OK

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://github.com/ianm1647/expandeddelight
- License: MIT
- Explanation:

  expandeddelight-0.1.4-skytekx.1.jar is an unofficial port of Expanded Delight 0.1.4 by ianm1647 from NeoForge 1.21.1 to Forge 1.20.1, made for this pack. It is built from https://github.com/ianm1647/expandeddelight, branch 1.21-neo at commit ea826400654702587e86c9e94544bfd74f065083, and the source of the port is public in this pack's repository (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/expandeddelight). Expanded Delight declares the MIT License in its mods.toml, on Modrinth and on CurseForge, and its 1.20 branch carries a CC0 1.0 LICENSE. The jar ships the author's MIT text (Copyright (c) 2022 ianm1647) as LICENSE-MIT_expandeddelight, the CC0 text as LICENSE_expandeddelight, the MIT text of Farmer's Delight (Copyright (c) 2020 vectorwing) as LICENSE_farmersdelight, because the mod derives art and code from Farmer's Delight, the NeoForged MDK template license, and a NOTICE_expandeddelight that says it is an unofficial modified build, names the upstream commits and lists what was left out. Material of Farmer's Respite, which is All Rights Reserved, is not in the jar or in the repository: the upstream juicer GUI sheets, a bottle slot icon and a menu class decompiled from Farmer's Respite were left out and replaced by the author's own 2022 juicer sheet and a menu written from Farmer's Delight's MIT code. The mod id stays expandeddelight, and the version carries the suffix -skytekx.1.

## Reference

- Redistribution allowed: yes, under MIT, once master carries its source (see Notes)
- On Modrinth: 1.21.1 NeoForge and Fabric builds only (https://modrinth.com/mod/expanded-delight, project e9V6wFcR, newest 0.1.4-neoforge as version RTRYPIJp, built from 28d9561 on the same branch)
- Author: ianm1647 (Expanded Delight), port by SkyTekx. Parts follow Farmer's Delight by vectorwing
- Notes: SHA-1 45b288dd96173e620db5a6d1065250a96b584949, 486,426 bytes, the build of 2428130 with the review fixes (the first bundled build of d9eb6cc had SHA-1 3e156033dbf6a5a09d199960614776023e15e69a). Built with Java 17 from `ports/expandeddelight/` (`./gradlew build`), which fetches Farmer's Delight 1.20.1-1.3.4 from the Modrinth CDN and checks its SHA-1 against the pack's index. Two clean builds gave the same SHA-1. It requires Farmer's Delight 1.20.1-1.3.4 or newer on both sides and has no mixin. The license review of 2026-11-05 set these conditions, and the build meets each of them:
  1. The Farmer's Respite files stay out of git from the first vendoring commit (eb734ad) and out of the jar: `textures/item/empty_container_slot_bottle.png` (SHA-1 64863cf1), `textures/gui/juicer.png` and `textures/gui/jei/juicer.png` (one file, SHA-1 2a2bffad, built on Farmer's Respite's kettle sheet). The juicer screen and the JEI category draw `textures/gui/juicer_gui.png` of the 1.20 branch (commit 50e9c10, 2022, SHA-1 94706b23) instead, and the bottle slot needs no icon, since that sheet draws one. A check of every file in the jar found neither SHA-1.
  2. Upstream's `JuicerMenu.java`, decompiled from Farmer's Respite's KettleMenu, is not taken. The port's JuicerMenu is written from Farmer's Delight's CookingPotMenu (MIT) with slot positions of the 1.20 sheet.
  3. The jar holds no copy of a vanilla texture: upstream's unused `item/glass_bottle.png` and `item/sweet_berries.png` were left out, no model needed them.
  4. Farmer's Delight's MIT text ships in the jar and in `licenses/`, and the NOTICE names the derived art and code. Upstream's byte copy `block/rice_bag_bottom.png` was left out, the cranberry bag model already points at `farmersdelight:block/rice_bag_bottom`.
  5. The jar also carries the CC0 text of the 1.20 branch, the MIT text with the author's copyright from branch 1.19 (commit 6c73c98) and the MDK template license.
  6. No Fabric library of the 1.20 branch and no Farmer's Delight Refabricated code is used, and no translation, tag or recipe of the 1.20 branch, whose outside contributors sent them through pull requests while fabric.mod.json declared MIT.

  The license texts and the NOTICE also travel in `licenses/expandeddelight-0.1.4-skytekx.1/` (the review named `local-mods/licenses/`, but the pack builder ships only jars from `local-mods/`, and license texts from `licenses/`). Publishing the source is no duty under MIT or CC0. The pack's own gate wants it: `tools/mrpack_permissions.py` refuses the jar until `origin/master` holds `ports/expandeddelight/` and this build in `local-mods/`. Not published on Modrinth or CurseForge by SkyTekx.
