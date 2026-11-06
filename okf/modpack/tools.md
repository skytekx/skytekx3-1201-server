---
type: Reference
title: Tools
description: The scripts in tools/ in the order they are used, from datapack sync and mod updates through jar patches and the mrpack build to the Modrinth upload and RCON, with what each one really does and where it runs.
tags: [modpack, tools, build, modrinth]
timestamp: 2026-11-06
---

# Tools

The folder [tools/](../../tools) holds 21 scripts, not counting the Arclight build helpers in
`tools/arclight/`. Six came from the build sessions of June and July 2026. The seventh,
`update_mods.py`, appeared on 2026-10-31 at 17:50, after the owner asked at 16:58 whether the installed
mods have updates. It was committed at 18:14 (a64f045). The next three are the region scans that
checked the Halloween world on the Mac mini the same evening. The eleventh, `cf_tunnel.py`, sets up the
Cloudflare Tunnel that made the server public at 21:13. The permission check `mrpack_permissions.py`
that the build and the upload share came on 2026-11-02 (002250f). Four came on 2026-11-04:
`datapack_order.py` (c450324), the unify audit `unify_audit.py` with its dump script
`unify_audit_dump.js`, and `replace_blocks.py`, after the owner asked at 17:33 to replace the old blocks
of Chocolate Quest everywhere. The melting generator
`gen_melting.py` and its dump script `melting_dump.js` answer the request of 17:35 the same day that
everything that holds a meltable material melts, and were committed on 2026-11-05. The wire scan
`wire_scan.py` (a1a1500) and the alternator scan `alternator_scan.py` (ade8903) of the Create Crafts &
Additions backport came on 2026-11-05 too, and the alternator scan is described on the
[Create Crafts & Additions backport](createaddition-backport.md#release). Since 2026-11-06
`fetch_local_mods.py` fetches the jars that `local-mods/` names by a pointer file, see
[pointed jars](#pointed-jars). The permission check is described on [Publish](publish.md) and the
Arclight rebuild in `tools/arclight/` on [Arclight runtime](../server/arclight.md).

| Order | Tool | Lines | Job | Needs | Writes |
|---|---|---:|---|---|---|
| 1 | Sync, `sync_skylands_datapack.sh` | 9 | Mirrors the two Skylands datapack sources into the world | The tool rsync | The world copies in `world/datapacks/` |
| 2 | Update, `update_mods.py` | 151 | Lists or applies newer Modrinth builds of the index entries | Public Modrinth API | The index, `mods/` and `client-only-mods/` |
| 2 (legacy) | Resolve, `resolve_all.py` | 116 | Resolves a fixed slug list to the newest releases | Modrinth token, the old scratch folder | Jars in `mods/` and a scratch file whose folder no longer exists |
| 2 (deploy) | Fetch, `fetch_local_mods.py` | 261 | Downloads every jar that `local-mods/` names by a pointer file and checks its size and SHA-1 | Python 3, the download hosts | The jars in `local-mods/` (ignored by git) or in `--dest`, and the cache outside the tree |
| 3 | Patch, `patch_mods.py` | 777 | Re-applies the boot fixes inside mod jars and holds `PACK_RULES`, the patches the pack ships too | Python 3 | Jars in `mods/` |
| 4 | Build, `build_mrpack.py` | 219 | Zips the `.mrpack` from git HEAD with the committed index, fetches the pointed jars, patches and renames the `PACK_RULES` jars and adds the server list of the clients | A git checkout, the download hosts of the pointed jars | The folder `client-only-mods/`, `SkyTekx3-<version>.mrpack` and the cache outside the tree |
| 5 | Publish, `publish_modrinth.py` | 251 | Creates the Modrinth project if missing, uploads the version and checks its hash, or replaces the project texts | Modrinth token in `MODRINTH_TOKEN` | Modrinth (only with `--go` or `--texts`) |
| Any time | RCON, `rcon.py` | 23 | Sends one command to the server | RCON password | Nothing |
| After a mod joins | Order, `datapack_order.py` | 78 | Prints the datapack order of a saved `level.dat` and the mod packs that load after the fixes pack | Python 3 | Nothing |
| After the mod set changes | Melting dump, `melting_dump.js` | 366 | Dumps every recipe, item, fluid, tag and mod of a booted test server for the generator, as `kubejs/server_scripts/zz_melting_dump.js` there and never on the live server | KubeJS on a test server | `melting_dump_*.json` in `kubejs/` of that server |
| After the mod set changes | Melting, `gen_melting.py` | 1,785 | Writes one Tinkers melting recipe per item that holds a meltable material from the dump, audits every melting recipe of the pack for dupes and checks its own reader, see [Melting](../content/melting.md) | Python 3, standard library only | Only `world/datapacks/skytekx3_fixes/data/skytekx3/recipes/melting/`, nothing with `--check` |
| Once per world, server stopped | Replace, `replace_blocks.py` | 926 | Replaces the five old blocks of Chocolate Quest with the normal blocks in every chunk, entity and NBT file of a world, see [Old blocks](../content/old-blocks.md) | Python 3.9 or newer, standard library only | Only the region and NBT files that hold an old block, each through a temporary file |
| Once per host | Tunnel, `cf_tunnel.py` | 89 | Creates or updates the Cloudflare Tunnel, its route and the DNS records for `hex.skytekx.com` | Cloudflare credentials in the environment | Cloudflare and a token file (mode 600) |
| After a pregen | Scan, `void_scan.py` | 346 | Lists structure starts and blocks below the islands and in void columns | Python with numpy | Nothing |
| After a pregen | Scan, `void_float.py` | 160 | Separates blocks that hang from an island from blocks that float free | Python with numpy | Nothing |
| After a pregen | Scan, `structure_pieces.py` | 35 | Prints every piece of one structure with its projection and box | Python with numpy | Nothing |
| Before and after a CC&A upgrade | Scan, `wire_scan.py` | 230 | Lists the Create Crafts & Additions wire nodes of a world, the wires left at one end and how the saved wire graph differs from the world | Python 3 | Nothing, or a JSON file with `--json` |

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
  the 11 patched index jars lose their patches until `patch_mods.py` runs again. An override of the
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

The script [patch_mods.py](../../tools/patch_mods.py) re-applies the jar level fixes of 14
jars in `mods/`. It
finds its targets by glob relative to its own folder and is idempotent. Run it after any tool or person
put new jars into `mods/`. The patches are listed on [boot fixes](../content/boot-fixes.md#jar-patches).

## Build the `.mrpack`

```sh
python3 tools/build_mrpack.py
```

The script [build_mrpack.py](../../tools/build_mrpack.py) does six things:

1. It reads the mod list from the committed index (`git show HEAD:modrinth.index.json`) and ships it
   as committed. It refuses to build when `versionId` or the Forge build differ from `VERSION` and
   `FORGE`, when a path is listed twice, or when the index lists a jar that `PACK_RULES` ships from
   `local-mods/`. It has not rewritten the index since 3.1.0.
2. It moves every jar whose index entry has server env `unsupported` from `mods/` to
   `client-only-mods/`.
3. It zips `SkyTekx3-<version>.mrpack` with the index and these overrides, all taken from git HEAD with
   `git archive`: `config/` without `config/chunky/tasks/`, `kubejs/`, `defaultconfigs/`, the two
   Skylands datapack sources and `world/datapacks/skytekx3_fixes/` under `overrides/world/datapacks/`,
   and the jars of `local-mods/` under `overrides/mods/`.
4. Since cba636e it adds `client-overrides/servers.dat`, the server list of the clients, built from
   `SERVERS` as uncompressed NBT. It holds one entry, SkyTekx at `hex.skytekx.com`, in 58 bytes, and the
   NBT reader of `void_scan.py` reads it back as that entry. The client mod Modflared, an index entry
   since 44358a8, opens the Cloudflare Tunnel behind that address. An instance that is updated to a
   pack with this file loses its own server entries.
5. It loads `tools/patch_mods.py` from git HEAD, applies its `PACK_RULES` to the matching jars of
   `local-mods/` and ships each result under the name with the suffix of its rule, Simply Jetpacks 2
   as `overrides/mods/SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar`. It refuses a rule that changes
   nothing and two override files at one path, its permission guard checks the shipped names, and
   `licenses/` goes to `overrides/licenses/`.
6. Since 2026-11-06 it fetches the jars that `local-mods/` names by a pointer file and bundles them
   like the jars that git holds, see [pointed jars](#pointed-jars).

### Pointed jars

A bundled jar may have a license that allows it inside the `.mrpack` but forbids a copy of its own
on another website, and a jar in this public repository would be one. Git does not hold such a jar. It
tracks a pointer file `local-mods/<jar>.fetch.json` in its place with the keys `file`, `url`, `sha1`,
`size`, `source` and `reason`, and `.gitignore` ignores the jar path. Since 2026-11-06 that is
ZeroCore 2.1.48, CurseForge file 7344725 on `mediafilez.forgecdn.net` with SHA-1
`d60df80acecc27e20401065a252773f86b08e9de` and 1,341,342 bytes, see
[mod set](mod-set.md#bundled-jars).

- The builder reads each pointer from git HEAD, takes the jar from the cache or downloads it there,
  and bundles it under the path that a tracked copy would have, so `PACK_RULES`, the permission guard
  and the file list treat it like any bundled jar. The cache is `$SKYTEKX3_FETCH_CACHE`, else
  `$XDG_CACHE_HOME/skytekx3/local-mods/`, else `~/.cache/skytekx3/local-mods/`, with one folder per
  SHA-1. It lies outside the tree unless `$SKYTEKX3_FETCH_CACHE` points into it, which the tools
  refuse unless git ignores that path.
- A pointer that does not parse, a failed download, a size or SHA-1 that differs from the pointer and
  a jar that git tracks next to its pointer stop the build before it moves or writes anything, also
  with `--allow-unpermitted`.
- No commit reachable from HEAD may hold a pointed jar, at any path, because a push publishes every
  one of them. The builder takes the blob id of each fetched jar, the id that `git hash-object` gives
  for it, and runs `git log --find-object` with that id on HEAD. Unless the command prints nothing,
  the build names the jar and the commits and stops, and with `--allow-unpermitted` it writes only
  `SkyTekx3-<version>-UNPERMITTED.mrpack`. For ZeroCore 2 the id is
  `f94d426ce1c02a3d3cbb16ddaf09db424c5435ae`.
- The upload script refuses a `.mrpack` that lacks a pointed jar in `overrides/mods/` or ships it
  unpatched with other bytes than its pointer names. It runs the same history check before any call
  to Modrinth, with the jar from the `.mrpack` or from the cache.
- For a server, `python3 tools/fetch_local_mods.py` puts every pointed jar into `local-mods/` with the
  same checks and the same cache, and `--dest mods` puts it straight into `mods/`. Inside the
  repository it writes only to paths that git ignores, the jar in `--dest` and the cache alike, and it
  checks the same for the temporary `.fetch-*` file that a killed run leaves next to them. The file
  `.gitignore` ignores `/local-mods/.fetch-*` for that. The option `--check` only lists what is
  missing.

The test of 2026-11-06 built the pack of the branch before and after the change of ZeroCore 2 with
`--allow-unpermitted`. Both files held the same 1,063 entries in the same order with the same
content, ZeroCore 2 included. A pointer with a wrong SHA-1, a wrong size, a missing file on the CDN,
a host that does not resolve or an `http` URL refused the build, and so did the jar tracked next to
its pointer. The runs used a scratch copy of the branch and are not in the repo.

A second test of 2026-11-06 ran in a throwaway clone with every permission record set to OK on disk,
so that only the new checks could stop a build. On the branch before the history rewrite, whose
local commits still held the ZeroCore jar then, the build refused and named the two commits that add and delete it, and the upload
script refused a pack for the same reason. On a commit with the same tree and no history the build
wrote a pack with the same 1,063 entries as before, and that pack passed the preflight of the upload
script. A copy of the jar committed under `plugins/` and deleted again in the next commit refused
the build as well. The fetch script refused `--dest plugins`, a cache in `.zc-cache/`, a folder
`docs/` whose jars git ignores but whose `.fetch-*` files it does not, and a symlink outside the
tree that leads into `plugins/`, and it wrote into `local-mods/` and `mods/` and to a cache in
`mods/`.

Up to 3.0.5 it packed the working tree, not git. The 3.0.5 file therefore holds 14 untracked config
files, among them `config/avaritia-common.toml` of a removed mod, and the Spark profiler's temporary
native libraries and recordings under `config/spark/tmp/`. Commit 41d15c4 limited it to tracked files
but still read them from disk, so a first 3.0.6 build at 19:09 carried seven configs that server boots
had rewritten. Since 3a398b1 every override comes from git HEAD, so commit what should ship before a
build. It does not ship the jar patches or BrassAmber BattleTowers. To raise the version, edit
`VERSION` in this script and in `publish_modrinth.py`, and to change the server list, edit `SERVERS`.
Pack 3.0.6 was built this way on 2026-10-31 at 20:19 (67,245,989 bytes, 808 entries, no client-only jar
moved) and pack 3.0.7 at 21:54:18 (67,246,343 bytes, 809 entries).

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
1603723 the script names 3.0.7 and its changelog describes that release, as 222ee72 did for 3.0.6.
Before that it had kept the changelog of 3.0.4 through 3.0.5. With `--texts` the script replaces the
summary and the body of the existing project with the two files and stops. That is how the texts of
2026-06-27 were replaced on 2026-10-31 at 21:25, and how the section on joining the server reached the
project at about 21:56.
Since 2026-11-06 it also refuses a `.mrpack` that lacks a jar which `local-mods/` names by a pointer
file, or ships it with other bytes, see [pointed jars](#pointed-jars).
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
cut off, missing `.mcc`, unknown compression). Run them after a pregen, against a stopped server or
right after `save-all flush`. They need numpy. The Mac mini has it in a venv,
`python3 -m venv ~/.venvs/skyscan` with `pip install numpy` (numpy 2.0.2 on the system Python 3.9).
Its copies of the three scripts are the ones of b2801e0 since 2026-11-01 01:10. Before that it ran
older copies.

- The scan [void_scan.py](../../tools/void_scan.py) lists every structure start with its lowest piece
  and its biome, every non-air block below the island band (Y 24 by default, `--band-min`) and every
  non-air block in `sky_void` columns. It reads starts from every chunk, also from the chunks at the
  edge of the generated area that are not full yet, and takes the biome at the center of the first
  piece, which often lies in the next chunk. A start whose chunk holds no biome yet counts as
  `start_biome_unknown`. Floaters and a few thin island edges lie in `sky_void` columns, so blocks
  there are expected.
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

The scan [wire_scan.py](../../tools/wire_scan.py) came with the
[Create Crafts & Additions backport](createaddition-backport.md#wire-graph-test) on 2026-11-05. It reads
the region files of every dimension with the standard library only, also chunks kept in `c.X.Z.mcc`
files, and lists every connector and redstone relay with its wires. Nodes still in the old `node0` to
`node7` format count with their old slots, read as `WireVertex.fromBlockEntityTag` reads them. It
reports the wire ends whose far end does not point back, which the wire graph of the backport cuts at
its first check. With a saved graph it compares `data/createaddition_wires.dat` with the world in
three lists. A difference is a position that the graph and a block entity both hold with other
wires, or a graph vertex whose full chunk holds no wire node. Nodes that the graph has not discovered
yet, because nobody loaded their chunk and no wire reached them since the upgrade, are listed apart
and are no difference. Vertices in chunks that the scan could not read or that are not full count as
unchecked. With `--json` it writes the node lists, and `--compare` lists every node whose wires
changed since such a file.

```sh
tools/wire_scan.py world --json /tmp/wires-before.json
tools/wire_scan.py world --compare /tmp/wires-before.json
```

## Mac mini

The scripts that locate the repo from their own path work on any host: the sync script,
`update_mods.py`, `patch_mods.py`, `rcon.py`, and since 2026-10-31 also `build_mrpack.py` and
`publish_modrinth.py`. Only `resolve_all.py` still hard-codes `/home/snekmin/git/skytekx3-1201-server`.
The builder reads git, and the Mac mini's copy has no `.git` folder, so packs are built on the build
machine. The Mac mini does not need them to run the server. The server
files reach it through `macos/sync-to-mac.sh`, see [macOS host](../server/macos.md). The scripts
`replace_blocks.py` and `datapack_order.py` take the world folder or its `level.dat` as their argument
and run on the system `/usr/bin/python3` 3.9.6 of the Mac mini without a venv. Both ran there at the
deploy of 3.1.3 on 2026-11-05.

## History

Commit 73ba615 (2026-06-29) added `build_mrpack.py`, `publish_modrinth.py`, `rcon.py` and
`resolve_all.py`. Commits 518aa19 and af5860e (2026-06-30) added and extended `patch_mods.py`. Commit
b7dd95f (2026-07-12) raised the version to 3.0.5, added the index fallback and the Forge datapack to
`build_mrpack.py` and added the sync script. Before that, the chain ran
`resolve_all.py`, then `patch_mods.py`, `build_mrpack.py` and `publish_modrinth.py --go`, with the
resolver's scratch file as the hand over. On 2026-10-31 `update_mods.py` took over the resolver's job
and ran once with `--apply` (a64f045 and 0a20b8c). The same day the region scans were added, and the
version went to 3.0.6 in both pack scripts (b23a72f and 222ee72). In the evening the builder started to
write the server list of the clients (cba636e), and the version went to 3.0.7 (1603723).

Related: [mod set](mod-set.md), [publish](publish.md), [boot fixes](../content/boot-fixes.md),
[secrets](../server/secrets.md), [deploy](../server/deploy.md).
