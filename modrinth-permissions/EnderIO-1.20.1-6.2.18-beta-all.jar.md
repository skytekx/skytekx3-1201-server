# Ender IO

Bundled file: `EnderIO-1.20.1-6.2.18-beta-all.jar`
Status: OK (on the conditions below)

## Paste into the Modrinth attribution form

- Permission reason: Licensed
- Link to work: https://modrinth.com/mod/enderio
- License: Unlicense (Ender IO), CC BY-NC-SA 3.0 (code from Patchouli), MIT, MPL-2.0 and Apache-2.0 (code from Mekanism and Commoble, bundled libraries)
- Explanation:

  Ender IO (EnderIO-1.20.1-6.2.18-beta-all.jar) by Team Ender IO is the unmodified 6.2.18-beta release for Minecraft 1.20.1, byte identical to the Modrinth file. Its own code and assets are under the Unlicense (LICENSE.txt of the tag v6.2.18-beta of https://github.com/Team-EnderIO/EnderIO), which allows redistribution. Some parts of the jar are under other licenses with conditions. The classes GhostBuffers and GhostRenderLayer and the method initBuffers of IOConfigWidget, and the block rendering of the JEI FireCraftingCategory, come from Patchouli by Vazkii, which is CC BY-NC-SA 3.0. SoulDataReloadListener is MIT code by Commoble, and IFluidItemInteractive and the temporary file save of ConduitSavedData are adapted from Mekanism (MIT). META-INF/jarjar bundles GraphLib3 (MIT), Registrate (MPL-2.0), checker-qual (MIT) and Guava with failureaccess, listenablefuture, Error Prone annotations and JSR 305 (Apache-2.0). The outer jar carries none of these texts, and of the nested library jars only guava-32.1.2-jre.jar and checker-qual-3.33.0.jar carry their own, so the pack ships all of them with a NOTICE in licenses/EnderIO-1.20.1-6.2.18-beta/. The pack is free and earns no money, as the non-commercial term of the Patchouli parts requires.

## Conditions

- The pack stays non-commercial because of the Patchouli parts. It must be free of charge, with no paid access, no paywalled or ad-gated download, no Modrinth monetization or creator rewards on the project and no paid server perks. Earning money with the pack would first need a clean rewrite of those parts or permission from Vazkii. The Twilight Forest assets (CC BY-NC-SA 4.0) already set the same limit.
- The folder licenses/EnderIO-1.20.1-6.2.18-beta/ travels with every copy of the pack: NOTICE.txt with the credits, the Unlicense of Ender IO, the CC BY-NC-SA 3.0 legal code, the MIT notices of Commoble, Mekanism, GraphLib and checker-qual, the Apache-2.0 text and the MPL-2.0 text.
- The Patchouli parts stay under CC BY-NC-SA 3.0. Neither the record nor the pack describes the jar as purely Unlicense or CC0, although the mods.toml of the jar says license="CC0".
- Registrate is unmodified, and the NOTICE names where its Source Code Form is, as MPL-2.0 asks.
- The jar is unmodified, so it keeps its name and implies no change. A modified build needs a version suffix such as -skytekx.1, a change notice in the jar and a record of its own.

## Reference

- Redistribution allowed: yes, on the conditions above
- On Modrinth: yes, https://modrinth.com/mod/enderio (project 49ZofO4f, version 6.2.18-beta of 2026-03-08, the same SHA-1)
- Author: Team Ender IO
- Notes: The jar has SHA-1 33587f98f2d96507de0e9d2f57f5ab35328ace7d, which the Modrinth API resolves to the file EnderIO-1.20.1-6.2.18-beta-all.jar of version 6.2.18-beta. Checked at the tag v6.2.18-beta (commit 907a9c37): IOConfigWidget.java line 67 and ioconfig/notice.md (Patchouli), FireCraftingCategory.java line 151 ("Borrowed a bunch of rendering code from Patchouli$PageMultiblock"), the MIT header of SoulDataReloadListener.java, IFluidItemInteractive.java line 16, ConduitSavedData.java line 350, and README.md line 122 ("Certain parts of the source code may be under a different license due to being part of other projects"). The libraries come from META-INF/jarjar/metadata.json of the jar, their licenses from their POM files on Maven Central and the license files inside the nested jars. The jar has no .ogg files, so the CC BY 3.0 and CC BY-NC 3.0 sound credits of the README do not concern it. An earlier version of this record said that the Unlicense "unconditionally permits" redistribution, which left out the parts above. Since the file is on Modrinth, the index could list it as a CDN download instead, and the pack would then not redistribute it.
