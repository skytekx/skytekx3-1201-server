---
type: Reference
title: Related repos and services
description: The sibling repos, the skytekx GitHub organisation, the skytekx.com website and the Modrinth project around SkyTekx 3, with their state on 2026-10-31.
tags: [related, github, website, modrinth, repos]
timestamp: 2026-10-31
---

# Related repos and services

SkyTekx 3 lives in this repo, but its website, two mod forks, the source of its compat mod and the
SkyTekx 2 audits that started it live elsewhere. This page lists them with their state on 2026-10-31.
The history behind them is on [History](history.md).

## Overview

| Name | Role | Where | State on 2026-10-31 |
|---|---|---|---|
| This repo, skytekx3-1201-server | Server and modpack | GitHub `skytekx/skytekx3-1201-server`, local `~/git/skytekx3-1201-server` | Remote master is 940f07e (2026-07-12). Local master carries the commits of 2026-10-31 on top of it, none of them pushed |
| Repo skytekx-worldgen | Source of the SkyTekx Compat mod | Only `~/git/skytekx-worldgen`, no remote | Three commits of 2026-07-12, see [SkyTekx Compat](content/compat-mod.md) |
| Repo skytekx-web | The website skytekx.com | GitHub `skytekx/skytekx-web`, local `~/git/skytekx-web` | Branch main at ef50cc2 (2026-06-30 18:14), equal to the live site |
| Repo skytekx2-1122-server | The SkyTekx 2 server on 1.12.2 | GitHub `skytekx/skytekx2-1122-server`, local `~/git/skytekx2-1122-server` | Branch main at ee2cf4f (2024-12-25), plus four untracked audit files of 2026-06-26 and 2026-06-27 |
| Fork skytekx/nyx | The Nyx port to 1.20.1 | GitHub | Port on branch `1.20.1`, no releases |
| Fork skytekx/Cataclysm_Spellbooks | The Spellbooks port to Cataclysm 3.31 | GitHub | Port on branch `1.20.1`, no releases |
| Modrinth project skytekx3 | The pack listing | Web page `modrinth.com/modpack/skytekx3` | A draft with versions 3.0.0 to 3.0.4, 3.0.6 and 3.0.7. An anonymous API call answers 404 |
| Prism instance SkyTekx3-test | The owner's game client since 2026-10-31. The older instance `SkyTekx3` was last launched on 2026-07-12 | The build machine | See [Client](modpack/client.md#test-instance) |

## This repo on GitHub

- The command `git ls-remote origin` returned `refs/heads/master` 940f07e on 2026-10-31 at 18:24, 18:56
  and 20:00. The local master adds the commits of 2026-10-31 on top, starting with 257d4c1 at 18:14.
- These commits carry the author and committer address `skytekx@erebos.xyz`, which the repo's
  local git config sets. The 40 commits before them all use one other address. Which address the new
  commits keep is an owner decision before the push, see [Open items](open-items.md#owner-decisions).
- The local tracking ref `origin/master` still points at bd5f18b of 2026-06-30, so `git status` also
  counts the six commits from 62ada38 to 940f07e as ahead. GitHub has everything up to 940f07e. A
  plain `git fetch` corrects the display.
- GitHub's history was rewritten twice by force push on 2026-06-29. The first push at 18:53 replaced the
  original commits with the rebuilt granular history. The second at 22:51 followed a `git filter-branch`
  that removed dashes and semicolons from the commit messages. The pre-rewrite states survive only in
  the local branches `backup-pre-reconstruct` and `backup-pre-scrub`.
- The repository description on GitHub still says "kitchen-sink" and "Published:
  modrinth.com/modpack/skytekx3" (GitHub API, 2026-10-31). The project is not public, and the owner had
  the phrase "kitchen sink" removed from the website on 2026-06-29 11:02.

## The GitHub organisation skytekx

The organisation holds all three generations. Read with `gh repo list skytekx` on 2026-10-31.

| Repo | Visibility | Created | Last push | Role |
|---|---|---|---|---|
| Repo skytekx-1710 | Public | 2019-02-15 | 2019-05-28 | SkyTekx 1 on 1.7.10, described as "THE MINECRAFT MODPACK OF THE 21st CENTURY" |
| Repo skytekx-1710-server | Public | 2019-02-15 | 2019-05-28 | SkyTekx 1 server |
| Repo skytekx-1710-server-ts3pp-backup | Public | 2019-04-13 | 2019-05-28 | Backup of the ts3.party server of that time |
| Repo skytekx-1710-server-pro | Private | 2020-04-30 | 2020-04-30 | Same description as skytekx-1710-server |
| Repo skytekx2-1122-server | Public | 2023-01-03 | 2024-12-25 | SkyTekx 2 server on 1.12.2 |
| Repo skytekx2-1122 | Private | 2023-01-08 | 2023-01-08 | SkyTekx 2, content not checked |
| Repo skytekx2-1122-server-xmas-backup | Private | 2024-12-25 | 2025-01-01 | Backup of the SkyTekx 2 server |
| Repo skytekx3-1201-server | Public | 2026-06-27 | 2026-07-12 | SkyTekx 3, this repo |
| Fork Cataclysm_Spellbooks | Public | 2026-06-29 | 2026-06-29 | Port of Cataclysm Spellbooks |
| Repo skytekx-web | Public | 2026-06-29 | 2026-06-30 | Website |
| Fork nyx | Public | 2026-06-29 | 2026-06-29 | Port of Nyx |
| Repos test, Supi-s-Adventure-Modpack-Client and Alex-SkyFactory-Backup | Public | 2019 to 2021 | 2019 to 2021 | Unrelated older projects |

## The two forks

Both forks keep the upstream default branch. The port lives on a branch named `1.20.1`, and neither
fork has a release or a tag.

- The fork [skytekx/nyx](https://github.com/skytekx/nyx) of Ellpeck/Nyx was created on 2026-06-29 at
  14:25 after the owner asked at 14:23 to fork and port Nyx. Branch `1.20.1` holds 7e5714f (14:49, the
  client side night atmosphere) and 5ef1fdb (15:50, the full content mod). The default branch is still
  upstream's code of 2021. Up to 74faf22 the pack index downloaded Nyx from a release `v1.20.1-1.0.0`
  of this fork, which does not exist (HTTP 404). The pack bundles the jar instead, see
  [Nyx](content/nyx.md). The fork shows the upstream description
  "Commission for Drakallen". Up to 1.0.0 the permission file wrongly read it as a commission for this
  pack. The file for 1.0.1, `modrinth-permissions/nyx-1.20.1-1.0.1.jar.md`, says it is unrelated.
- The fork [skytekx/Cataclysm_Spellbooks](https://github.com/skytekx/Cataclysm_Spellbooks) of the
  archived AceTheEldritchKing/Cataclysm_Spellbooks was created on 2026-06-29 at 06:26, right after the
  owner ruled at 06:25 to patch Spellbooks instead of Cataclysm. Branch `1.20.1` holds one commit,
  36d1cee (06:54), "Port to stock L_Ender's Cataclysm 3.31". The jar built from it keeps the upstream
  name `cataclysm_spellbooks-1.2.9-1.20.1.jar` and exists only in the server's `mods/` and the owner's
  client, see [Mod set](modpack/mod-set.md#jars-outside-the-index).

## The compat mod source

The repo `~/git/skytekx-worldgen` builds `skytekx-compat-2.0.0.jar`, the pack's own Forge mod. It has no
git remote and is not in the GitHub organisation, so the only copy of the source is on the build
machine. Its history and build steps are on [SkyTekx Compat](content/compat-mod.md).

## The website skytekx.com

The owner asked for a small website on the newly bought domain skytekx.com on 2026-06-29 at 06:42
(`~/.claude/history.jsonl`, session 5bd3e1de).

- The site is static HTML in `~/git/skytekx-web`: `index.html`, one credits page per generation
  (`credits-1710.html`, `credits-1122.html`, `credits-1201.html`), `impressum.html`, `imprint.html` and
  `assets/`. There is no build step, no `package.json` and no `wrangler.toml`.
- It is deployed to the Cloudflare Pages project `skytekx` with wrangler, straight from the local tree
  (`.wrangler/cache/pages.json`). The deploy token is not in any repo. The owner put it into the `.env`
  file in the build machine's home directory (2026-06-29 09:18). That file also holds a token of another
  person, which must not be used (2026-06-30 04:43).
- For website work the owner ruled on 2026-06-30 10:13: do not ask questions, deploy straight to
  Cloudflare when done, and commit and push everything.
- The repo has 52 commits from f5a097a (2026-06-29 11:41) to ef50cc2 (2026-06-30 18:14). On 2026-10-31
  the live `index.html` was byte identical to ef50cc2.
- The site has an English and a German version, a history timeline with the mod count of each
  generation, community credits by GitHub handle only, one credits page per pack with links to the mod
  authors, and an Impressum and imprint for the operator Netsnek e.U., as Austrian law requires.
- The footer reads "SkyTekx, a modpack by ts4.party, the TeamSpeak for party, formerly known as
  ts3.party" and "Supported by netsnek.com", as the owner ruled on 2026-06-29 (see
  [Decisions](decisions.md)).

### Claims on the site that are wrong on 2026-10-31

| Claim | Fact | Evidence |
|---|---|---|
| "Join the public server", "join at skytekx.com" | No Minecraft server answers at skytekx.com. The domain points at Cloudflare proxy addresses, there is no `_minecraft._tcp` SRV record and no `mc.` or `play.` host, and TCP 25565 timed out. Since 2026-10-31 21:13 the server is public as `hex.skytekx.com`, through a Cloudflare Tunnel and only for clients with Modflared | DNS over HTTPS and a TCP check on 2026-10-31, see [Deploy](server/deploy.md#dns) |
| The button "Play on Modrinth" | The project page answers "Project not found" | Modrinth API and web page, 2026-10-31 |
| The guide step `ln -s arclight-forge-1.20.1-1.0.6-6de9fec.jar arclight.jar` | A clone already holds `arclight.jar` as a tracked symlink to the hardened jar. The step fails with "File exists", and forcing it drops the crash fix | Commit 88552a9, see [Arclight runtime](server/arclight.md) |
| The guide itself | It never fills `mods/`, so a server built from it has no mods | Website `index.html:376-430` |
| Java 21 crashes Arclight with `IInventoryBridge` | Unproven. The only Java 21 boot failed on a missing mod, see [Arclight runtime](server/arclight.md#java-17-only) | Files `boot.log` and `boot6.log` |
| Mod counts 232 and 245+ | The badge and the credits page say 232, the text says 245+. The pack of 3.0.7 has 217 index entries and 35 bundled jars | Website `index.html:457-483`, `credits-1201.html` |
| "The rest of the ts3.party community", "the ts3.party crew" | The community is ts4.party since the owner's ruling of 2026-06-29 10:00 | Website i18n keys `server.lead` and `credits.lead` |
| Link previews "Hexxit × Tekkit × SkyFactory, reborn on Minecraft 1.20.1" | On 2026-06-30 04:54 the owner shortened the subtitle to "Hexxit × Tekkit × SkyFactory". The `og:description` and `twitter:description` tags kept the long form | Website `index.html` meta tags |

## Modrinth

- The project has the slug `skytekx3` and the id `KqKLuzBj` (memory `project_skytekx2_modpack.md:31`).
  The script `tools/publish_modrinth.py` created it as a draft and uploads versions as betas.
- On 2026-10-31 the API returned 404 for the slug and the id, a search for "skytekx" found nothing, and
  the web page showed "Project not found". The project is not public.
- With the owner's token the API shows the draft with versions 3.0.0 to 3.0.4 (2026-06-27 to
  2026-06-29), 3.0.6 (2026-10-31 20:27) and 3.0.7 (21:55). Version 3.0.5 never went up.
- What blocks a public release is on [Publish](modpack/publish.md). Every public pointer to the
  project is dead until then: the website button, the GitHub repo description and, until 2026-10-31, the
  README.

## SkyTekx 2 audit files

Four untracked files in `~/git/skytekx2-1122-server` came out of the first build session on 2026-06-26
and 2026-06-27. They exist only on the build machine.

| File | Written | Content |
|---|---|---|
| File `MODPACK_UPDATE_AUDIT.md` | 2026-06-26 17:03 | SkyTekx 2 runs 209 mod jars on Forge 14.23.5.2860 and Mohist 1.12.2-320. 45 mods have a newer 1.12.2 build |
| File `PORT_1_21_MAP.md` | 2026-06-26 17:59 | 154 of 209 mods have a path to 1.21, 97 as the same mod or a direct successor. Tinkers' Construct is lost |
| File `PORT_1_20_1_MAP.md` | 2026-06-26 18:31 | 170 of 209 mods have a path to 1.20.1, 128 as the same mod or a direct successor |
| File `RECOMMENDED_ADDITIONS_1_20_1.md` | 2026-06-27 10:22 | 65 suggested additions taken from Hexxit Revived and Tekxit 4. 42 of them are in pack 3.0.5 |

## Community and supporter

- SkyTekx is "a modpack by ts4.party, the TeamSpeak for party, formerly known as ts3.party" (owner,
  2026-06-29 10:00 and 10:10). The first pack of 2019 was the ts3.party community build, see
  [History](history.md).
- The company netsnek.com supports the project. The website links it in the footer and uses its
  Impressum.

Related: [OKF index](index.md), [History](history.md), [Publish](modpack/publish.md),
[Nyx](content/nyx.md), [SkyTekx Compat](content/compat-mod.md), [Deploy](server/deploy.md).
