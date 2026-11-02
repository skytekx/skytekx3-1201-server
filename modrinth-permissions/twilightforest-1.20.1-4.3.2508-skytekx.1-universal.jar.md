# The Twilight Forest (SkyTekx 1.20.1 fork with TF 4.8 backport)

**Bundled file:** `twilightforest-1.20.1-4.3.2508-skytekx.1-universal.jar`
**Status:** OK (author allows)

## Paste into the Modrinth attribution form

- **Permission reason:** Special permission
- **Link to work:** https://www.curseforge.com/minecraft/mc-mods/the-twilight-forest
- **License:** LGPL-2.1 (code), CC BY-NC-SA 4.0 (non-code/non-sound assets), All Rights Reserved (sounds and structures); MIT for the code derived from Mystic Crown Compat
- **Explanation:**

  The Twilight Forest uses LGPL-2.1 for code, CC BY-NC-SA 4.0 for most non-code assets, and All Rights Reserved for sounds and structure assets. The author (Benimatic / TeamTwilight) explicitly grants modpack permission: you must link back to the original mod page, credit the author, not monetize the modpack, and remove the mod if the author requests it. Free redistribution on Modrinth satisfies these conditions. This jar is a modified build: the SkyTekx fork of Twilight Forest 4.3 for Minecraft 1.20.1 Forge, with content from Twilight Forest 4.8 backported. The LGPL-2.1 allows distributing a modified version when it stays under the LGPL, carries a notice of the changes and its source is available. The jar ships the LGPL-2.1 text, the CC BY-NC-SA 4.0 text and a NOTICE that names the upstream commits, the modifier and date, and the changes (`LICENSE_twilightforest`, `ASSET_LICENSE_twilightforest`, `NOTICE_twilightforest`). The modified source is public in the SkyTekx 3 repository under ports/twilightforest (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/twilightforest). Part of the code derives from Mystic Crown Compat by 741741 (MIT); its licence and a notice of the derived files ship in the jar under `META-INF/`.

## Reference

- **Redistribution allowed:** yes
- **On Modrinth:** no
- **Author:** Benimatic / TeamTwilight (original mod); 1.20.1 fork and 4.8 backport by Florian Kleber / SkyTekx; Mystic Crown Compat parts by 741741
- **Notes:**
  - The official TeamTwilight mod is not published on Modrinth; only unofficial Fabric ports and unrelated companion mods appear there. The canonical release is on CurseForge and GitHub (https://github.com/TeamTwilight/twilightforest). The "no commercial use" condition in the author's modpack permission mirrors the CC BY-NC-SA 4.0 asset license; ensure the modpack is distributed free of charge. CurseForge lists the license as "Custom License" due to the mixed LGPL/CC/ARR structure.
  - Base: TeamTwilight/twilightforest branch `1.20.1` at `1bc3a4c21213bc443967e92125613cb9ef47891e` (TF 4.3). Its `src/` was imported unchanged as the start of `ports/twilightforest/` (commit `3fee28e`).
  - Backported from branch `1.21.1` at `9e40a75721c716b2fdf80c16d1058fe718825e9f` (TF 4.8, 2026-10-13).
  - Mod version `4.3.2508-skytekx.1`. Built from `ports/twilightforest/` with `./gradlew build` at commit `5bbde50`: SHA-1 `9417577d44548e730e958066ffe14662bea1db4a`, 27307362 bytes. The jar task fixes entry dates and order, so rebuilds are byte-identical (checked with two clean builds). The git history of `ports/twilightforest/` is the complete change history.
  - Licence files in the jar (checked with `unzip -l`): `LICENSE_twilightforest` (LGPL-2.1), `ASSET_LICENSE_twilightforest` (CC BY-NC-SA 4.0) and `NOTICE_twilightforest` (identical to `ports/twilightforest/NOTICE`) at the root, plus `META-INF/LICENSE-mystic-crown-compat.txt` and `META-INF/NOTICE-mystic-crown-compat.txt`. The mods.toml `license` field names the three root files and the MIT part; `credits` names Mystic Crown Compat and its licence file. The manifest says `Implementation-Vendor: TeamTwilight (modified build: SkyTekx)`.
  - Mystic Crown Compat notice: part of the code derives from Mystic Crown Compat by 741741, https://github.com/741741-741/mystic-crown-compat, commit `10ce4a4f14bdae2d455f6b76a150174cde2ac5b3` (v1.1.0), https://modrinth.com/mod/mystic-crown-compat. MIT License, Copyright (c) 2026 741741. The MIT terms cover only that derived code, which is re-implemented in the fork's own classes; no addon jar is bundled.
  - All Rights Reserved files: the jar contains 229 sounds (`.ogg`) and 274 structure templates (`.nbt`). Each was compared by git blob id (`git hash-object`) with every blob in the upstream history of branches `1.20.1` and `1.21.1`. All 229 sounds are unmodified upstream files. 266 templates are unmodified upstream files: 228 match 4.8 HEAD `9e40a757` (all 182 Lich Tower and 35 camp templates, 8 courtyard and 3 `final_castle/temp`), 37 match 4.3 HEAD `1bc3a4c` (24 courtyard, 5 graveyard, 3 well, 2 druid hut, 2 ruins, `quest_grove.nbt`), and `feature/well/simple_well_top.nbt` matches upstream commit `788aa3ee9` (the well bucket on a rope). 8 templates are modified: they are the 4.3 files with upstream 4.8 edits replayed for 1.20.1, so they match no upstream blob:
    - `data/twilightforest/structures/feature/druid_hut/{basement_gallery,basement_gallery_trap,basement_shelves,basement_shelves_trap,basement_study,basement_study_trap,druid_doubledeck}.nbt`: the junk-loot and barrel markers of upstream `f7a9b9dcc`, replayed in `f381912`
    - `data/twilightforest/structures/feature/ruins/stone_circle.nbt`: the longer stone circle pillars of upstream `81a9771c8` (#1993), replayed in `eddb69f`

    Whether TeamTwilight's modpack permission also covers these 8 modified All Rights Reserved templates is the pack owner's call.
