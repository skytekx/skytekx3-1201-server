# Spiced Twilight (1.20.1 backport by SkyTekx)

**Bundled file:** `spiced_twilight-2.0.0-1.20.1-skytekx.1.jar`
**Status:** OK

## Paste into the Modrinth attribution form

- **Permission reason:** Licensed
- **Link to work:** https://github.com/baguchi/SpicedTwilight
- **License:** LGPL-3.0-or-later
- **Explanation:**

  spiced_twilight-2.0.0-1.20.1-skytekx.1.jar is a modified Minecraft 1.20.1 Forge backport of baguchi's Spiced Twilight (Twilight Forest mob variety: Maze Slime Beetle, hiding Helmet Crabs, kobold armor and babies, Fire Beetle Powder, Hydra Hide, Cinder Pouch), which upstream only publishes for 1.21.1 NeoForge. The source repository github.com/baguchi/SpicedTwilight ships the GNU LGPL v3 as LICENSE.md, and the Modrinth project lists LGPL-3.0-or-later. The LGPL permits conveying a modified version, including inside a modpack, when it stays under the LGPL, carries a notice of the changes and its source is available. The jar ships the LGPL and GPL texts and a NOTICE that names the upstream commit, the modification date and every change; the modified source is public in the SkyTekx 3 repository under ports/spiced-twilight (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/spiced-twilight), and the jar's NOTICE and mods.toml credits link to it.

## Reference

- **Redistribution allowed:** yes
- **On Modrinth:** 1.21.1 only (https://modrinth.com/mod/spiced-twilight); this 1.20.1 build is not, so it must be bundled
- **Author:** baguchi (original mod); 1.20.1 Forge backport by Florian Kleber / SkyTekx
- **Notes:** Built from `ports/spiced-twilight/` in this repository (https://github.com/skytekx/skytekx3-1201-server/tree/master/ports/spiced-twilight; upstream commit 92a6b60be56037d3e6785b5d8bdc35ec08d88332, the 1.21.1-2.0.0 release). Build `ports/twilightforest` first (`./gradlew build`), then `./gradlew build` in the port; the jar is byte-reproducible. It is a modified build, so it is not byte-identical to any upstream file: SHA-1 1e78363d5ab1287d902c58e72d2f76942a2e8d3a, 127314 bytes. The jar's mods.toml declares `license="LGPL-3.0-or-later"` and requires twilightforest [4.3.2508-skytekx.1,) and forge [47,). LICENSE.md, the GPL-3.0 text and NOTICE ride inside the jar as `LICENSE_spiced_twilight.md`, `COPYING_spiced_twilight` and `NOTICE_spiced_twilight`. The Naga jump attack is left out (it conflicts with Alternative Twilight Remastered).
