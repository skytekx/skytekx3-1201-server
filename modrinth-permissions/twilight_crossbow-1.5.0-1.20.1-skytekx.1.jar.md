# Twilight Crossbow (1.20.1 backport by SkyTekx)

**Bundled file:** `twilight_crossbow-1.5.0-1.20.1-skytekx.1.jar`
**Status:** OK

## Paste into the Modrinth attribution form

- **Permission reason:** Licensed
- **Link to work:** https://github.com/baguchi/TwilightCrossbow
- **License:** MIT
- **Explanation:**

  twilight_crossbow-1.5.0-1.20.1-skytekx.1.jar is a modified Minecraft 1.20.1 / Forge backport of baguchi's Twilight Crossbow (crossbow versions of the Twilight Forest bows: Triple, Seeker, Ice and Ender Crossbow, plus the ice and seeker fireworks), which upstream only publishes for 1.21.1 NeoForge. It is built from source in this repository (`ports/twilight-crossbow/`). The upstream repository ships an MIT LICENSE ("Copyright (c) 2025 baguchi"), and the jar's mods.toml declares MIT. MIT permits modifying and redistributing the software, including inside a modpack, as long as the copyright and permission notice are kept. The jar carries the unchanged licence as `LICENSE_twilight_crossbow`, plus `NOTICE_twilight_crossbow` naming the upstream commit and the changes.

## Reference

- **Redistribution allowed:** yes
- **On Modrinth:** yes for 1.21.1 NeoForge only (https://modrinth.com/mod/twilight-crossbow), so the 1.20.1 build has to be bundled.
- **Author:** baguchi (original mod); 1.20.1 backport by Florian Kleber / SkyTekx
- **Notes:** Built from upstream commit 00085635b09c2b3072dec3de7feb917685d1f9a9 ("ender crossbow", 2026-08-28: the 1.21.1-1.5.0 release plus the Ender Crossbow). The port changes the mod loader and the crossbow loading and shooting code, targets the pack's Twilight Forest fork (requires `twilightforest` [4.3.2508-skytekx.1,); JEI [15.20,) is optional), fixes the smithing template loot and the Ice Firework blast, and leaves out the unreleased Knight Crossbow; see `ports/twilight-crossbow/NOTICE`. Build: `cd ports/twilightforest && ./gradlew build`, then `cd ports/twilight-crossbow && ./gradlew build`. `tf_version` in `ports/twilight-crossbow/gradle.properties` must equal the fork's `mod_version`, because the build finds the fork jar by its file name. The bundled jar has SHA-1 7b9ba21253074027711b8274b45b738d56e371eb (92109 bytes). This build is not byte-reproducible: the zip entries carry build times, so a rebuild gives a different SHA-1. It ships no Twilight Forest asset file (no texture or sound in it is byte-identical to one in the fork jar).
