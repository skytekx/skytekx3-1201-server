# Upstream: Actually Additions (1.20.1 backport base)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Actually Additions,
the base of the SkyTekx 1.20.1 Forge backport of the mod's newer NeoForge lines.

| | |
|---|---|
| Repository | https://github.com/Ellpeck/ActuallyAdditions |
| Ref | Branch `1.20.1` |
| Commit | `226a439154a8f103424783ad7afc1e33fe59cbd7` ("Update .gitignore") |
| Commit date | 2024-03-04T23:05:21+01:00 |
| Upstream version | `mod_version=1.1.52` (Minecraft 1.20.1, Forge 47.1.3, Parchment 2023.09.03, JEI 15.3.0.4) |
| Released | Never. No 1.20.1 build exists on Modrinth or CurseForge |
| Port source | Branch `main` @ `cd9d9c63254f471f966ee51b078afdf47af5f852` (2026-06-19, `mod_version=1.3.26`, NeoForge 1.21.1, release 1.3.26) |
| Intermediate steps | Branch `1.20.4` @ `0ebbd811fd511e83fa50eea36f62df38b1e267c8` (2024-06-27, 1.2.14, NeoForge 20.4) and branch `1.21` @ `d9b0a714d1303569f913f855260376309484ba00` (2024-10-23, 1.3.6, NeoForge 21.1) |

The branches `1.20.4`, `1.21` and `main` fork from the base branch at `97532ff` (2024-03-04, "Mark
tiny torch in port.md"). The base has four commits after that fork point: `0e3fec7` (a filter
tweak that `1.20.4` carries as `00418d2`), the merge `59459b1`, the relicensing `33f5541` (its
twin on the other branches is `717199e`) and `226a439`. Branch `main` is 591 commits ahead of the
fork point.

Every file in the snapshot is byte-identical to `git archive 226a439` of the upstream repository
(blob hashes and file modes checked), with the four exclusions listed below. Changes made on top
of the snapshot are separate commits in this repository's history
(`git log -- ports/actuallyadditions`).

## Files left out of the snapshot

| Upstream path | Why it is left out |
|---|---|
| `src/main/java/de/ellpeck/actuallyadditions/mod/util/crafting/RecipeHandler.java` | The 36 lines are all commented out and compile to nothing, and nothing else refers to the class. Its header says the code is an edited copy of a Biomes O' Plenty class under CC BY-NC-ND 4.0, which forbids sharing adaptations. |
| `libs/FastWorkbench-1.12.2-1.3.2.jar` | A third party binary for Minecraft 1.12.2 that `build.gradle` never references. |
| `logs/debug.log`, `logs/latest.log` | Two empty log files. |

## License

- Code and assets: MIT License (`LICENSE`, kept unchanged with its 2019 copyright line). The
  `mods.toml` field `license` says `MIT`. Upstream commit `33f5541` (2024-03-04, "Change license
  to MIT") replaced the old custom license `LICENSE.md` with this file on branch `1.20.1`, and
  `717199e` did the same on the newer lines. Both cite upstream issue #1391, "Change license to
  MIT on all relevant (active) branches", which the repository owner opened on 2024-03-04 and
  closed as completed on 2024-03-06. The original author had already moved the 1.16.4 rewrite
  branch `clean-start` to MIT in `9d6118e` (2020-09-06).
- The same MIT `LICENSE` sits on branch `main` at `cd9d9c6`. The Modrinth project
  (`actually-additions`) declares MIT, and its team lists the owner, a maintainer and the artist
  of the 1.20+ textures. The CurseForge page (project 228404) shows the field "Custom License",
  whose text was not retrieved (not verified). The `LICENSE.md` that `33f5541` removed was itself
  permissive enough for this use: it allowed forking and modifying the code and the use in
  modpacks.
- No separate license covers the textures, models, sounds or translations at either ref. Every
  asset came into the repository by commits and pull requests to it. The original author added
  the four sounds in upstream `a86389d` (2015-12-02) and `6fe1948` (2015-12-13), and no commit
  names a third party source for them (not verified beyond the history).
- 402 Java files at the base still carry the old per file header that points to the "Actually
  Additions License" at `http://ellpeck.de/actaddlicense`. The relicensing above supersedes it,
  and the snapshot keeps the headers unchanged.
- Code excerpts that upstream marks as taken from other mods, all of them kept:
  `TileEntityXPSolidifier` (experience formulas excerpted from Ender IO with permission),
  `misc/BannerHelper` (a banner pattern helper excerpted from Additional Banners with permission,
  called from `ActuallyAdditions#setup`), `ClientRegistryHandler` (an empty method whose body is
  commented out, excerpted from Tinkers' Construct with permission) and
  `booklet/misc/GuiAAAchievements` (a 1.12 class inside a block comment, partially excerpted from
  Botania with permission). Two methods are marked as copied from vanilla Minecraft code
  (`AssetUtil`, `AwfulUtil`).
- Not part of this base, and not to be ported: `data/StructureUpdater.java` on branches `1.21` and
  `main`, a data generation helper under "Blu's License of Common Sense" that upstream uses with
  its authors' permission. That permission does not extend to this fork.
- Upstream jars carry no LICENSE file. The SkyTekx build adds `LICENSE` and the SkyTekx `NOTICE`
  to the jar, as MIT asks for the notice in every copy.
