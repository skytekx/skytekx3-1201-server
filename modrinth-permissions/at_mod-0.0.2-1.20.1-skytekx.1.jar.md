# Alternative Twilight (Remastered) (1.20.1 backport by SkyTekx)

**Bundled file:** `at_mod-0.0.2-1.20.1-skytekx.1.jar`
**Status:** OK

## Paste into the Modrinth attribution form

- **Permission reason:** Licensed
- **Link to work:** https://github.com/Lvedy114/alternative-twilight
- **License:** GPL-3.0-or-later
- **Explanation:**

  at_mod-0.0.2-1.20.1-skytekx.1.jar is a modified Minecraft 1.20.1 Forge backport of lvedy's Alternative Twilight (Remastered) (Twilight Forest boss reworks for the Lich and the Naga, spears, the Blood Lust effects and a Knight Phantom aurora shader), which upstream only publishes for 1.21.1 NeoForge. The source repository github.com/Lvedy114/alternative-twilight carries the GNU GPL v3 as LICENSE, its README states GPL v3, and the Modrinth project lists GPL-3.0-or-later. The GPL permits conveying a modified version, including inside a modpack, when it stays under the GPL, carries a dated notice of the changes and its complete source is available. The jar ships the unchanged LICENSE and a NOTICE that names the upstream commit, the modification date and every change; the modified source is public in the SkyTekx 3 repository under ports/alternative-twilight-remastered (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/alternative-twilight-remastered), and the jar's NOTICE and mods.toml credits link to it. The upstream boss music files are not part of the jar, because their provenance does not show a right to redistribute them.

## Reference

- **Redistribution allowed:** yes
- **On Modrinth:** 1.21.1 only (https://modrinth.com/mod/alternative-twilight-remastered); this 1.20.1 build is not, so it must be bundled
- **Author:** lvedy / Lvedy114 (original mod); 1.20.1 Forge backport by Florian Kleber / SkyTekx
- **Notes:** Built from `ports/alternative-twilight-remastered/` in this repository (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/alternative-twilight-remastered; upstream commit 99f43e08adfeffe3dd689835d485a66d39d3590c on the `old` branch, the Modrinth 0.0.2 release plus the aurora, config and LICENSE commits). Build `ports/twilightforest` first (`./gradlew build`), then `./gradlew build` in the port; the jar is byte-reproducible. It is a modified build, so it is not byte-identical to any upstream file: SHA-1 297b72a6b100fae059fb00c5a1497b2ed679429d, 131350 bytes. The jar's mods.toml declares `license="GPL-3.0-or-later"` (the upstream 0.0.2 jar's `All Rights Reserved` is an unedited MDK value) and requires twilightforest [4.3.2508-skytekx.1,) and forge [47,). LICENSE and NOTICE ride inside the jar as `LICENSE_at_mod` and `NOTICE_at_mod`. Not included: the eight boss music `.ogg` files (about 33 MB). Their metadata shows Bilibili video downloads of unknown authorship, which the code's GPL grant does not cover; the music system plays them only if a resource pack supplies `at_mod:music.<boss>` sound events.
