# Twilight Mobs Loots (1.20.1 backport by SkyTekx)

**Bundled file:** `twilight_mobs_loots-1.1.1-1.20.1-skytekx.1.jar`
**Status:** OK

## Paste into the Modrinth attribution form

- **Permission reason:** Licensed
- **Link to work:** https://github.com/wdd1118/Twilight-Mobs-Loots
- **License:** MIT
- **Explanation:**

  twilight_mobs_loots-1.1.1-1.20.1-skytekx.1.jar is a modified Minecraft 1.20.1 / Forge backport of Twilight Mobs Loots 1.1.1 by wdd1118, built from source in this repository (`ports/twilight-mobs-loots/`). The upstream repository ships an MIT LICENSE ("Copyright (c) 2026 wdd1118"), and the Modrinth project and the upstream mods.toml also declare MIT. MIT permits modifying and redistributing the software, including inside a modpack, as long as the copyright and permission notice are kept. The jar carries that notice as `META-INF/LICENSE_twilight_mobs_loots`, plus `META-INF/NOTICE_twilight_mobs_loots` naming the upstream commit and the changes.

## Reference

- **Redistribution allowed:** yes
- **On Modrinth:** yes for 1.21.1 NeoForge only (https://modrinth.com/mod/twilight-mobs-loots), so the 1.20.1 build has to be bundled.
- **Author:** wdd1118 (original mod); 1.20.1 backport by Florian Kleber / SkyTekx
- **Notes:** Built from upstream commit 41e447dea8f1815815a5fdf066ec8fe9bb91eecd (mod version 1.1.1, 1.21.1 NeoForge, Twilight Forest [4.8,5.0)). The port changes the mod loader and Curios API, targets the pack's Twilight Forest fork (requires `twilightforest` [4.3.2508-skytekx.1,)) and drops the Twilight Moonshine compat; see `ports/twilight-mobs-loots/NOTICE`. The build is byte-reproducible: `cd ports/twilightforest && ./gradlew build`, then `cd ports/twilight-mobs-loots && ./gradlew build` gives SHA-1 79d224cdd69963daf429e775e848c386bcd33e30 (137168 bytes).
