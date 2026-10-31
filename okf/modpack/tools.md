---
type: Reference
title: Tools
description: The scripts in tools/ in the order they are used, from datapack sync and mod updates through jar patches and the mrpack build to the Modrinth upload and RCON, with what each one really does and where it runs.
tags: [modpack, tools, build, modrinth]
timestamp: 2026-10-31
---

# Tools

The folder [tools/](../../tools) holds eleven scripts. Six came from the build sessions of June and July
2026. The seventh, `update_mods.py`, appeared on 2026-10-31 at 17:50, after the owner asked at 16:58
whether the installed mods have updates. It was committed at 18:14 (a64f045). The last three are the
region scans that checked the Halloween world on the Mac mini the same evening. The eleventh,
`cf_tunnel.py`, sets up the Cloudflare Tunnel that made the server public at 21:13.

| Order | Tool | Lines | Job | Needs | Writes |
|---|---|---:|---|---|---|
| 1 | Sync, `sync_skylands_datapack.sh` | 9 | Mirrors the two Skylands datapack sources into the world | The tool rsync | The world copies in `world/datapacks/` |
| 2 | Update, `update_mods.py` | 151 | Lists or applies newer Modrinth builds of the index entries | Public Modrinth API | The index, `mods/` and `client-only-mods/` |
| 2 (legacy) | Resolve, `resolve_all.py` | 108 | Resolves a fixed slug list to the newest releases | Modrinth token, the old scratch folder | Jars in `mods/` and a scratch file whose folder no longer exists |
| 3 | Patch, `patch_mods.py` | 218 | Re-applies the boot fixes inside mod jars | Python 3 | Jars in `mods/` |
| 4 | Build, `build_mrpack.py` | 84 | Rewrites the index and zips the `.mrpack` from git HEAD | A git checkout | The index, `client-only-mods/` and `SkyTekx3-<version>.mrpack` |
| 5 | Publish, `publish_modrinth.py` | 127 | Creates the Modrinth project if missing, uploads the version and checks its hash, or replaces the project texts | Modrinth token in `MODRINTH_TOKEN` | Modrinth (only with `--go` or `--texts`) |
| Any time | RCON, `rcon.py` | 23 | Sends one command to the server | RCON password | Nothing |
| Once per host | Tunnel, `cf_tunnel.py` | 82 | Creates or updates the Cloudflare Tunnel, its route and the DNS records for `hex.skytekx.com` | Cloudflare credentials in the environment | Cloudflare and a token file (mode 600) |
| After a pregen | Scan, `void_scan.py` | 346 | Lists structure starts and blocks below the islands and in void columns | Python with numpy | Nothing |
| After a pregen | Scan, `void_float.py` | 160 | Separates blocks that hang from an island from blocks that float free | Python with numpy | Nothing |
| After a pregen | Scan, `structure_pieces.py` | 35 | Prints every piece of one structure with its projection and box | Python with numpy | Nothing |

The Modrinth token and the RCON password live in the ansible vault as `vault_modrinth_pat` and
`vault_rcon_password`, see [secrets](../server/secrets.md). The RCON password is passed as a command
line argument, so it ends up in the shell history and in the process list while the script runs.
The script `publish_modrinth.py` reads the token from `MODRINTH_TOKEN` since 7f5ae03 and only falls
back to an argument.

## Datapack sync

The script [sync_skylands_datapack.sh](../../tools/sync_skylands_datapack.sh) changes into the repo
root and runs `rsync -a --delete` from `skylands-datapack/` to `world/datapacks/skytekx3_skylands/` and
from `skylands-datapack-forge/` to `world/datapacks/skytekx3_skylands_forge/`, then prints the file
counts.
Run it after every datapack edit and restart the server. It leaves `world/datapacks/skytekx3_fixes/`
alone. See [Skylands worldgen](../content/skylands-worldgen.md#edit-workflow).

## Mod updates

### The new update script

The script `tools/update_mods.py` (commit a64f045, 2026-10-31) works from the pinned index. It sends
the SHA-1 of every index entry to the public Modrinth endpoints `/version_files` and
`/version_files/update` with the loader Forge and game version 1.20.1, and needs no token.

```sh
python3 tools/update_mods.py                      # dry run, lists the updates
python3 tools/update_mods.py --channels release   # only release builds (default release,beta,alpha)
python3 tools/update_mods.py --apply              # rewrite the index entries and swap the jars
python3 tools/update_mods.py --apply --add-deps   # also add new required dependencies
```

With `--apply` it downloads each new jar, checks its SHA-512, puts it into `mods/` (or
`client-only-mods/` if the server side is unsupported), deletes the old jar and updates path, hashes,
URL and size in the index. The env of an entry stays. Then run `patch_mods.py` and a boot test, as its
own docstring says (line 11). On 2026-10-31 it ran with `--apply` and `--add-deps` between 17:50 and
17:53, and `patch_mods.py` ran right after. The result is on [mod set](mod-set.md#update-of-2026-10-31)
(commit 0a20b8c). This server folder has not booted since.

Points to watch with `--apply`:

- It updates every entry with a newer build, by default including beta and alpha builds. The owner
  asked for the most stable configuration on 2026-06-27 10:34. The run of 2026-10-31 brought in 12
  betas.
- Cataclysm must stay at 3.31, because the Cataclysm Spellbooks fork build on the server was ported to
  exactly that version (see [mod set](mod-set.md#jars-outside-the-index)).
- Every update can bring back boot errors that the [boot fixes](../content/boot-fixes.md) silenced, and
  the 9 patched index jars lose their patches until `patch_mods.py` runs again. An override of the
  fixes pack can also empty a loot table that a new build repaired.
- It only touches index entries, so the bundled jars and BrassAmber BattleTowers, which is in no pack,
  stay as they are.
- It does not restore `mods/` from the index. A jar that is missing from `mods/` is not downloaded unless
  it has an update.

### The legacy resolver

Do not use [resolve_all.py](../../tools/resolve_all.py). Despite what the docstring of `patch_mods.py`
says, it never reads `modrinth.index.json`:

- It takes the token as its first argument (line 5) and hard-codes the repo path (line 6).
- It walks a list of 100 slugs (lines 12 to 34) plus their required dependencies, and takes the first
  release of each for Forge 1.20.1 (lines 42 to 46). So it pulls whatever is newest that day.
- The list still names Radium, Rubidium, memoryleakfix and Better Combat. Radium broke Arclight on
  2026-06-27, see [mod set](mod-set.md#parked-and-removed-mods).
- It writes its result to the scratch folder of the old build session (line 103), which no longer
  exists, so the run now ends with an error after the downloads.

## Jar patches

The script [patch_mods.py](../../tools/patch_mods.py) re-applies ten jar level fixes in `mods/`. It
finds its targets by glob relative to its own folder and is idempotent. Run it after any tool or person
put new jars into `mods/`. The patches are listed on [boot fixes](../content/boot-fixes.md#jar-patches).

## Build the `.mrpack`

```sh
python3 tools/build_mrpack.py
```

The script [build_mrpack.py](../../tools/build_mrpack.py) does three things:

1. It reads the mod list from the committed index (`git show HEAD:modrinth.index.json`) and writes
   `modrinth.index.json` back with its `VERSION`, its `FORGE` build and its summary: `versionId` 3.0.6,
   Forge 47.4.18 and the tagline of skytekx.com. The file ends with a newline, as `update_mods.py`
   writes it.
2. It moves every jar whose index entry has server env `unsupported` from `mods/` to
   `client-only-mods/`.
3. It zips `SkyTekx3-<version>.mrpack` with the index and these overrides, all taken from git HEAD with
   `git archive`: `config/` without `config/chunky/tasks/`, `kubejs/`, `defaultconfigs/`, the two
   Skylands datapack sources and `world/datapacks/skytekx3_fixes/` under `overrides/world/datapacks/`,
   and the jars of `local-mods/` under `overrides/mods/`.

Up to 3.0.5 it packed the working tree, not git. The 3.0.5 file therefore holds 14 untracked config
files, among them `config/avaritia-common.toml` of a removed mod, and the Spark profiler's temporary
native libraries and recordings under `config/spark/tmp/`. Commit 41d15c4 limited it to tracked files
but still read them from disk, so a first 3.0.6 build at 19:09 carried seven configs that server boots
had rewritten. Since 3a398b1 every override comes from git HEAD, so commit what should ship before a
build. It does not ship the jar patches or BrassAmber BattleTowers. To raise the version, edit
`VERSION` in this script and in `publish_modrinth.py`. Pack 3.0.6 was built this way on 2026-10-31 at
20:19 (67,245,989 bytes, 808 entries, no client-only jar moved).

## Publish to Modrinth

```sh
export MODRINTH_TOKEN="$MODRINTH_PAT"     # the token from the vault
python3 tools/publish_modrinth.py         # dry run: checks the token and the project
python3 tools/publish_modrinth.py --go    # creates the project if missing, uploads the version
```

The script [publish_modrinth.py](../../tools/publish_modrinth.py) reads the token from
`MODRINTH_TOKEN`. A token as the first argument still works, but other local users can read it in the
process list. It takes the body from `docs/MODPACK_DESCRIPTION.md` and the summary from
`docs/MODPACK_SUMMARY.txt`. A new project is created as a
draft with the slug `skytekx3`, the categories adventure, magic and technology and the license All
Rights Reserved (lines 80 to 101). The version goes up as a featured beta with status listed, and the
script then compares the SHA-512 of the uploaded file with the local build (lines 103 to 127). Since
222ee72 the script names 3.0.6 and its changelog describes that release. It had kept the changelog of
3.0.4 through 3.0.5. With `--texts` the script replaces the summary and the body of the existing
project with the two files and stops. That is how the texts of 2026-06-27 were replaced on 2026-10-31.
The state of the project and its blockers are on [publish](publish.md).

## RCON

```sh
python3 tools/rcon.py 127.0.0.1 65535 "$RCON_PW" list
```

The script [rcon.py](../../tools/rcon.py) is a minimal Source RCON client: host, port, password, then
the command words. It prints the reply or "(empty response)", and exits with "AUTH FAILED" on a wrong
password. The port is `rcon.port` in `server.properties`, 65535 on this server
(`server.properties:43`). Output of `say` does not come back over RCON, so use `execute if ...` probes
to test things (see [Skylands worldgen](../content/skylands-worldgen.md#checking-a-change)). For the
stop path and mcrcon see [deploy](../server/deploy.md#rcon-and-mcrcon).

To find the server process, match `'java .*arclight[.]jar nogui'`. The plain pattern
`-f 'arclight.jar nogui'` also matches the shell that runs `pkill` or `pgrep` (memory
`project_skytekx3_vanilla_skylands.md:24`).

## Region scans

```sh
tools/void_scan.py world/dimensions/skytekx3/skylands/region
tools/void_float.py world/dimensions/skytekx3/skylands/region
tools/structure_pieces.py world/dimensions/skytekx3/skylands/region skytekx3:sky_windmill
```

The three scripts read region files with their own NBT reader and change nothing. Since b2801e0 the
reader works like the one of Minecraft 1.20.1: it also reads chunks kept in `c.X.Z.mcc` files, and
each script ends with a list of every chunk or region file it could not read and why (torn by a write,
cut off, missing `.mcc`, unknown compression). Run them after a
pregen, against a stopped server or right after `save-all flush`. They need numpy. The Mac mini has it in
a venv, `python3 -m venv ~/.venvs/skyscan` with `pip install numpy` (numpy 2.0.2 on the system Python
3.9).

- The scan [void_scan.py](../../tools/void_scan.py) lists every structure start with its lowest piece
  and its biome, every non-air block below the island band (Y 24 by default, `--band-min`) and every
  non-air block in `sky_void` columns. It reads starts from every chunk, also from the chunks at the
  edge of the generated area that are not full yet, and takes the biome at the center of the first
  piece, which often lies in the next chunk. A start whose chunk holds no biome yet counts as
  `start_biome_unknown`. Island rims are `sky_void` by design, so terrain there is expected.
- The scan [void_float.py](../../tools/void_float.py) flood fills from each block below the band. A
  group that reaches Y 40 (`--attach-y`) hangs from an island, for example an island bottom or a geode.
  A group that never reaches it floats in the void and is a leak. Since 00519e5 fills that meet are
  joined into one group, so a body that hangs from an island is no longer split into a false floating
  part, and a floating group next to an unreadable chunk is marked as such.
- The scan [structure_pieces.py](../../tools/structure_pieces.py) prints the template, projection and
  box of every piece of one structure and lists the unreadable chunks at the end. It showed that the floating windmill dropped its terrain
  matching fields onto an island below, see
  [Skylands worldgen](../content/skylands-worldgen.md#changes-of-2026-10-31).

A clean result has no structure start with a piece below the band, and no floating group except the
small islands that the terrain places low on purpose.

## Mac mini

The scripts that locate the repo from their own path work on any host: the sync script,
`update_mods.py`, `patch_mods.py`, `rcon.py`, and since 2026-10-31 also `build_mrpack.py` and
`publish_modrinth.py`. Only `resolve_all.py` still hard-codes `/home/snekmin/git/skytekx3-1201-server`.
The builder reads git, and the Mac mini's copy has no `.git` folder, so packs are built on the build
machine. The Mac mini does not need them to run the server. The server
files reach it through `macos/sync-to-mac.sh`, see [macOS host](../server/macos.md).

## History

Commit 73ba615 (2026-06-29) added `build_mrpack.py`, `publish_modrinth.py`, `rcon.py` and
`resolve_all.py`. Commits 518aa19 and af5860e (2026-06-30) added and extended `patch_mods.py`. Commit
b7dd95f (2026-07-12) raised the version to 3.0.5, added the index fallback and the Forge datapack to
`build_mrpack.py` and added the sync script. Before that, the chain ran
`resolve_all.py`, then `patch_mods.py`, `build_mrpack.py` and `publish_modrinth.py --go`, with the
resolver's scratch file as the hand over. On 2026-10-31 `update_mods.py` took over the resolver's job
and ran once with `--apply` (a64f045 and 0a20b8c). The same day the region scans were added, and the
version went to 3.0.6 in both pack scripts (b23a72f and 222ee72).

Related: [mod set](mod-set.md), [publish](publish.md), [boot fixes](../content/boot-fixes.md),
[secrets](../server/secrets.md), [deploy](../server/deploy.md).
