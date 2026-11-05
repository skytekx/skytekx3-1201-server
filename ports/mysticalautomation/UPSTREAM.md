# Upstream: Mystical Automation (1.20.1 port base)

This directory started as a plain source snapshot (`git archive`, no nested `.git`) of Mystical
Automation, an addon for Mystical Agriculture that adds machines for infusion, crafting, farming,
fertilizing and the altars. Upstream only builds it for NeoForge 1.21.1 and newer. SkyTekx ports it
to Forge 1.20.1, against the Mystical Agriculture and Cucumber builds that the pack already ships.

| Field | Value |
|---|---|
| Repository | https://github.com/BlakeBr0/MysticalAutomation |
| Ref | Branch `1.21` (upstream has no git tags) |
| Commit | `4149601b9c443eb7de5ebab6ecc9b6bdd7d6462d` ("Release 1.0.8") |
| Commit date | 2026-10-02T20:10:48-05:00 |
| Upstream version | `1.21.1-1.0.8` (NeoForge 21.1.18, Parchment 2024.07.28, Cucumber 8.0.16, Mystical Agriculture 8.0.19, Mystical Agradditions 8.0.7, JEI 19.16.1.156) |
| Released as | Modrinth version `HdlBF75n`, file `MysticalAutomation-1.21.1-1.0.8.jar`, SHA-1 `586be8e7f3fedc700e4bbf845a55c4758f12db7e` |
| Not used | Branch `26.1` (Minecraft 26.1, release line 2.0.x) |

The commit is the head of branch `1.21`. Every resource in the released 1.0.8 jar is byte identical
to `src/main/resources` at this commit, apart from the expanded `neoforge.mods.toml` (license review
of 2026-11-05).

Apart from this file, every file of the first snapshot commit is byte identical to `git archive
4149601` of the upstream repository (187 files, blob hashes and file modes checked). Changes on top of it are separate commits
in this repository's history (`git log -- ports/mysticalautomation`).

## License

- Code and assets: MIT License, `LICENSE` at the ref ("Copyright (c) 2025 BlakeBr0"), kept
  unchanged. Git history shows the file only in the initial commit `b9ae7d9` (2025-05-14). The
  README links it, `neoforge.mods.toml` says `license="MIT"`, and the Modrinth project
  `mystical-automation` (id `wtZsRhqk`) declares MIT.
- No Java file carries a license header or a third party attribution. There is no NOTICE, no
  separate asset license and no contributor agreement.
- The only commits by others are three translations merged by pull request (`zh_cn` in `f3237f9`,
  `pt_br` in `cb997b5`, `ja_jp` in `24b75e1`). They fall under the repository's MIT license through
  GitHub's inbound equals outbound terms (not separately confirmed).
- The 21 PNG textures carry Photoshop metadata from 2025 and no rights fields. No 16x16 block
  texture matches a vanilla texture. The side textures are references to
  `mysticalagriculture:block/machine_side`, which Mystical Agriculture 7.0.24 has as well. The GUI
  backgrounds were not compared pixel by pixel with vanilla (not verified).
- The CurseForge license field (project 1265613) could not be read, because CurseForge answered 403
  (not verified). Every other source says MIT.
- The Gradle wrapper scripts `gradlew` and `gradlew.bat` carry their Apache 2.0 headers. They do not
  go into the mod jar.

The dependencies Cucumber, Mystical Agriculture and Mystical Agradditions are MIT ("Copyright (c)
2018 BlakeBr0") and stay CDN entries of `modrinth.index.json`.
