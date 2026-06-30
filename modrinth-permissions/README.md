# SkyTekx 3 — Modrinth distribution permissions

Modrinth requires distribution-permission attribution for every mod BUNDLED in the modpack
(the `overrides/mods/` jars that are not pulled from Modrinth's own CDN). This folder has one
file per bundled mod with the exact fields to paste into
`Settings -> Permissions` on the project, plus this overview.

Researched automatically across all 34 bundled mods. **Verify the blockers yourself before trusting them.**

## TL;DR

- **28** mods are fine to bundle (open license or author allows modpacks).
- **0** need a quick manual check (CurseForge modpack flag unconfirmed).
- **6** are BLOCKERS — All-Rights-Reserved with no modpack permission. These will fail Modrinth review and must be resolved (contact author / get permission / replace / remove) before the pack can be public.
- **4** are on Modrinth and could be CDN-linked in `modrinth.index.json` instead of bundled, which removes them from this list entirely.

## BLOCKERS (resolve before publishing)

These cannot legally be bundled in a public modpack as-is:

- **The Aether: Genesis** (`aether_genesis-1.20.1-0.0.1-neoforge.jar`) — LGPL-3.0 (code) / All Rights Reserved (assets). Version 0.0.1 is a bleeding-edge CircleCI build from the 1.20.1-develop branch — there are no official GitHub releases. The mod is not on Modrinth or CurseForge, so CDN-linking is not an option. The Aether Team must be contacted via Discord (Oz#1986 on the Aether Community Discord) for modpack redistribution permission.
- **Connectivity** (`connectivity-1.20.1-7.6.jar`) — All Rights Reserved. The author (someaddon) has explicitly disabled the CurseForge third-party distribution flag, which covers modpack redistribution. The GitHub repository (https://github.com/someaddons/connectivity) contains no LICENSE file and no modpack permission statement. Contact someaddon directly via CurseForge or GitHub before including this mod in any redistributed modpack.
- **Cupboard** (`cupboard-1.20.1-3.7.jar`) — All Rights Reserved. The exact matching file (cupboard-1.20.1-3.7.jar) is at https://www.curseforge.com/minecraft/mc-mods/cupboard/files/7905744. CurseForge's Curse Maven integration explicitly states third-party sharing is disabled for this mod. The GitHub repo (https://github.com/someaddons/cupboard) has no license file. The mod is not present on Modrinth at all (Modrinth API search returns zero hits for "cupboard someaddon").
- **Deadly World** (`deadlyworld-1.20.1-1.2.3.jar`) — All Rights Reserved. Not on Modrinth — must be bundled, requiring explicit permission. CurseForge project ID 60098. The GitHub repo (FatherToast/DeadlyWorld, default branch 1.16.5, also has a 1.20.X branch) carries no LICENSE file (GitHub API returns null). Version 1.2.3 for MC 1.20.1 confirmed via gradle.properties in the 1.20.X branch. The CurseForge "Allow distribution" (allowModDistribution) flag status is unconfirmed without an API key — this is the key thing to verify before bundling; if the flag is true, reason upgrades to "Special permission".
- **🟨⬛!!Fireflies!!🟨⬛** (`fireflies-1.0.0-forge-1.20.1.jar`) — All Rights Reserved. The mod has only one file (version 1.0.0 for Forge 1.20.1, released 2024-10-15). It is exclusively on CurseForge and is not available on Modrinth, so CDN-linking is not an option. The CurseForge modpack permission flag could not be confirmed via web scraping (JavaScript-rendered); if supermj767 has enabled "Allow modpack use" on the CurseForge project settings, the reason would upgrade to "Special permission" and redistribution would be OK — verify manually on the CurseForge mod page or by contacting the author.
- **Hexxit Additions** (`hexxit_additions-1.0.2-forge-1.20.1.jar`) — All Rights Reserved. The CurseForge "Allow 3rd party / modpack distribution" flag is set to disabled by the author for all file versions. This mod does not appear on Modrinth under any matching slug. Contact starboiluke directly (CurseForge profile) to request explicit modpack inclusion permission before distributing this jar.

## Could be CDN-linked instead of bundled

On Modrinth, so you can add them to `modrinth.index.json` as CDN downloads and drop the bundled jar (no attribution needed):

- **ElevatorMod (OpenBlocks Elevator)** (`elevatorid-1.20.1-1.9.1-forge.jar`) — https://modrinth.com/mod/elevatormod
- **Ender IO** (`EnderIO-1.20.1-6.2.18-beta-all.jar`) — https://modrinth.com/mod/enderio
- **OfflineSkins** (`offlineskins-1.20.1-v1.jar`) — https://modrinth.com/mod/offlineskins
- **Tumbleweed** (`Tumbleweed-forge-1.20.1-0.5.5.jar`) — https://modrinth.com/mod/tumbleweed

> Note: Tumbleweed is on Modrinth but only for older MC versions; its 1.20.1 Forge build is CurseForge-only, so it must stay bundled (license is open, so that is fine).

## Full table

| File | Mod | License | Permission reason | Redistribute? | On Modrinth? |
|---|---|---|---|---|---|
| `aether_genesis-1.20.1-0.0.1-neoforge.jar` | The Aether: Genesis | LGPL-3.0 (code) / All Rights Reserved (assets) | No permission | **NO** | - |
| `connectivity-1.20.1-7.6.jar` | Connectivity | All Rights Reserved | No permission | **NO** | - |
| `cupboard-1.20.1-3.7.jar` | Cupboard | All Rights Reserved | No permission | **NO** | - |
| `deadlyworld-1.20.1-1.2.3.jar` | Deadly World | All Rights Reserved | Unknown | **NO** | - |
| `fireflies-1.0.0-forge-1.20.1.jar` | 🟨⬛!!Fireflies!!🟨⬛ | All Rights Reserved | No permission | **NO** | - |
| `hexxit_additions-1.0.2-forge-1.20.1.jar` | Hexxit Additions | All Rights Reserved | No permission | **NO** | - |
| `bibliocraft-1.20.1-1.3.1.jar` | BiblioCraft Legacy (Expanded) | MIT | Licensed | yes | - |
| `birdsnests-1.20.1-2.1.5.jar` | Birds Nests | LGPL-3.0 | Licensed | yes | - |
| `ChunkOpt-1.0.2.jar` | Chunk Optimization (ChunkOpt) | MIT | Licensed | yes | - |
| `cleanswing-1.20-1.8.jar` | Clean Swing Through Grass | LGPL-3.0 | Licensed | yes | - |
| `cosmeticarmorreworked-1.20.1-v1a.jar` | Cosmetic Armor Reworked | MMPL-1.0.1 | Licensed | yes | - |
| `elevatorid-1.20.1-1.9.1-forge.jar` | ElevatorMod (OpenBlocks Elevator) | MIT | Licensed | yes | yes |
| `EnchantingPlus-1.20.1-1.2.0.jar` | Enchanting Plus (Classic) | All Rights Reserved | Special permission | yes | - |
| `EnderIO-1.20.1-6.2.18-beta-all.jar` | Ender IO | Unlicense | Licensed | yes | yes |
| `Exchangers-1.20.1-3.5.1.jar` | Exchangers | Jacky's Minecraft Mods License (custom) | Special permission | yes | - |
| `EyesInTheDarkness-1.20.1-1.3.10.jar` | Eyes in the Darkness | BSD-3-Clause | Licensed | yes | - |
| `fairylights-7.0.0-1.20.1.jar` | Fairy Lights | MIT | Licensed | yes | - |
| `FastLeafDecay-32.jar` | Fast Leaf Decay | All Rights Reserved | Special permission | yes | - |
| `framework-forge-1.20.1-0.8.0-signed.jar` | Framework | LGPL-2.1 | Licensed | yes | - |
| `GunpowderLib-1.20-2.2.jar` | GunpowderLib | Jacky's Minecraft Mods License (custom) | Special permission | yes | - |
| `infernalmobs-1.20.1.11.jar` | AtomicStryker's Infernal Mobs | Custom License (All Rights Reserved with stated modpack exception) | Special permission | yes | - |
| `inventorysorter-1.20.1-23.0.11.jar` | Inventory Sorter | GPL-3.0 | Licensed | yes | - |
| `nvlforcefields-1.20.1-10.jar` | NVL Force Fields | Public Domain | Licensed | yes | - |
| `nyx-1.20.1-1.0.0.jar` | Nyx (1.20.1 port by SkyTekx) | MIT | Licensed | yes | - |
| `offlineskins-1.20.1-v1.jar` | OfflineSkins | MIT | Licensed | yes | yes |
| `parry-2.5.0.jar` | Shield Parry | LGPL-2.1 | Licensed | yes | - |
| `refurbished_furniture-forge-1.20.1-1.0.20.jar` | MrCrayfish's Furniture Mod: Refurbished | Custom (MIT for source code; All Rights Reserved for assets) | Special permission | yes | - |
| `ruins-1.20.1.2.jar` | Ruins (Structure Spawning System) | Custom License | Special permission | yes | - |
| `SkewersReflavored-1.20.1-1.0.0.jar` | Skewers Reflavored | MIT | Licensed | yes | - |
| `skytekx-worldgen-1.0.0.jar` | SkyTekx Worldgen | MIT | Licensed | yes | - |
| `traverse-forge-7.0.12.jar` | Traverse Reforged | LGPL-3.0-only | Licensed | yes | - |
| `trumpet_skeleton-1.2-forge-1.20.1.jar` | Trumpet Skeleton: Reforged | MIT | Licensed | yes | - |
| `Tumbleweed-forge-1.20.1-0.5.5.jar` | Tumbleweed | LGPL-3.0-only | Licensed | yes | yes |
| `twilightforest-1.20.1-4.3-universal.jar` | The Twilight Forest | LGPL-2.1 (code), CC BY-NC-SA 4.0 (non-code/non-sound assets), All Rights Reserved (sounds and structures) | Special permission | yes | - |

## How to fill the form

For each mod marked *Pending* under `Settings -> Permissions`:

1. Open the matching `<file>.jar.md` in this folder.
2. Set **Permission reason** to the listed value.
3. Paste **Link to work**, pick the **License** in the dropdown, paste the **Explanation**.
4. For *Special permission* mods, the proof is the author's stated modpack permission (linked in the file) — screenshot it into *Proof images* if asked.

