# Simply Jetpacks 2 (guide book patched by SkyTekx)

**Bundled file:** `SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar`
**Status:** OK

## Paste into the Modrinth attribution form

- **Permission reason:** Licensed
- **Link to work:** https://modrinth.com/mod/simply-jetpacks-2
- **License:** MIT
- **Explanation:**

  Simply Jetpacks 2 by Tomson124 and StormedPanda, based on the original mod by Tonius11, is published under the MIT License, which permits copying, modifying and redistributing the mod. The mods.toml inside the jar says "license = The MIT License (MIT)", and Modrinth lists the project as MIT. SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar is version 2.11.0.1 for Forge 1.20.1 with one change: its Patchouli guide book is moved to the layout that Patchouli 1.20 requires. The license text travels with the pack as licenses/SimplyJetpacks2-1.20.1-2.11.0.1-LICENSE.txt.

## Reference

- **Redistribution allowed:** yes
- **On Modrinth:** upstream only, https://modrinth.com/mod/simply-jetpacks-2 (project ny77zKza, version HD6Ykq4K), not this patched build
- **Author:** Tomson124 and StormedPanda, original mod by Tonius11
- **Notes:** The folder `local-mods/` holds the unchanged Modrinth file as `SimplyJetpacks2-1.20.1-2.11.0.1.jar` (SHA-1 bb687cca1be2d7145f6450348c245f350cb232fb). The script `tools/build_mrpack.py` applies the `PACK_RULES` of `tools/patch_mods.py` to it while it writes the `.mrpack` and ships the result as `mods/SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar` (SHA-1 f41d2aa6a92fd4cdb7e677a5f109bde7000eeae9, 689446 bytes, the same bytes as the server's patched copy). The patch sets `use_resource_pack: true` in `book.json` and copies the book contents from `data/` to `assets/`. Without it Patchouli 1.20 refuses the book, and the guide item reads "Invalid book". The own file name keeps the patched jar apart from the Modrinth file at `mods/SimplyJetpacks2-1.20.1-2.11.0.1.jar` that packs up to 3.1.2 installed and that a launcher removes when it updates an instance. Source: https://github.com/Tomson124/SimplyJetpacks2 (branch 1.20.1), license file `LICENSE.txt` there.
