# Twilight Crossbow (1.20.1 backport by SkyTekx)

**Bundled file:** `twilight_crossbow-1.5.0-1.20.1-skytekx.1.jar`
**Status:** OK

## Paste into the Modrinth attribution form

- **Permission reason:** Licensed
- **Link to work:** https://github.com/baguchi/TwilightCrossbow
- **License:** MIT (code); CC BY-NC-SA 4.0 (textures adapted from The Twilight Forest)
- **Explanation:**

  twilight_crossbow-1.5.0-1.20.1-skytekx.1.jar is a modified Minecraft 1.20.1 / Forge backport of baguchi's Twilight Crossbow (crossbow versions of the Twilight Forest bows: Triple, Seeker, Ice and Ender Crossbow, plus the ice and seeker fireworks), which upstream only publishes for 1.21.1 NeoForge. It is built from source in this repository (`ports/twilight-crossbow/`). The upstream repository ships an MIT LICENSE ("Copyright (c) 2025 baguchi") for the code and data. MIT permits modifying and redistributing the software, including inside a modpack, as long as the copyright and permission notice are kept. The 24 crossbow item textures are baguchi's adaptations of The Twilight Forest's bow textures by TeamTwilight (https://github.com/TeamTwilight/twilightforest), which are licensed CC BY-NC-SA 4.0; under ShareAlike the adapted textures are distributed under CC BY-NC-SA 4.0 too, which allows redistribution with attribution, the licence and a note of changes, for non-commercial use only. This pack is distributed free of charge, as for The Twilight Forest itself. The jar carries the unchanged MIT licence as `LICENSE_twilight_crossbow`, the CC BY-NC-SA 4.0 text as `ASSET_LICENSE_twilight_crossbow`, and `NOTICE_twilight_crossbow`, which names the upstream commit, the changes and the adapted textures with their Twilight Forest source. Its mods.toml declares `MIT (code); CC BY-NC-SA 4.0 (item textures adapted from The Twilight Forest)` and credits TeamTwilight's bow textures.

## Reference

- **Redistribution allowed:** yes
- **On Modrinth:** yes for 1.21.1 NeoForge only (https://modrinth.com/mod/twilight-crossbow), so the 1.20.1 build has to be bundled.
- **Author:** baguchi (original mod); 1.20.1 backport by Florian Kleber / SkyTekx
- **Notes:** Built from upstream commit 00085635b09c2b3072dec3de7feb917685d1f9a9 ("ender crossbow", 2026-08-28: the 1.21.1-1.5.0 release plus the Ender Crossbow). The port changes the mod loader and the crossbow loading and shooting code, targets the pack's Twilight Forest fork (requires `twilightforest` [4.3.2508-skytekx.1,); JEI [15.20,) is optional), fixes the smithing template loot and the Ice Firework blast, and leaves out the unreleased Knight Crossbow; see `ports/twilight-crossbow/NOTICE`. Build: `cd ports/twilightforest && ./gradlew build`, then `cd ports/twilight-crossbow && ./gradlew build`. `tf_version` in `ports/twilight-crossbow/gradle.properties` must equal the fork's `mod_version`, because the build finds the fork jar by its file name. The bundled jar has SHA-1 ee22819bd3baa5d166640ee239bbcdb0dd22d887 (99296 bytes). This build is not byte-reproducible: the zip entries carry build times, so a rebuild gives a different SHA-1. Textures: no file in the jar is byte-identical to a Twilight Forest asset, but the 24 crossbow item textures (`{triple,seeker,ice,ender}_crossbow` with their `_pulling_0..2`, `_arrow` and `_firework` variants) reuse the limbs of TF's `triple_bow`, `seeker_bow`, `ice_bow_clear`/`ice_bow_solid` and `ender_bow` textures pixel for pixel (a pixel compare matches 19-57% of their opaque pixels at the same or a one-pixel-shifted position), so they are CC BY-NC-SA 4.0 adaptations. They are upstream's files, unchanged by the backport. `crossbow_upgrade_smithing_template.png` and `empty_slot_bow.png` are not TF-derived.
