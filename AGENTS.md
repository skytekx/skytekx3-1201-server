# AGENTS.md for skytekx3-1201-server

Guidance for AI agents and humans who work on the SkyTekx 3 server and modpack. Read this first, then
the [OKF wiki](okf/index.md) for the details of each part. The open work is on
[Open items](okf/open-items.md), the owner's rulings on [Decisions](okf/decisions.md).

## What this repo is

The repo is both the server and the modpack that runs on it. The server is Minecraft 1.20.1 with Forge
47 on Arclight, a hybrid that also loads Bukkit plugins. The repo holds the server config, the deploy
files and the world datapacks, and it is the source of the Modrinth pack (`modrinth.index.json` plus the
bundled jars in `local-mods/`). The stack and the self-hosting steps are in the [README](README.md).

## Golden rules

- Run the server on Java 17 only. Every boot that reached `Done` ran Temurin 17.0.19 on the build
  machine or 17.0.21 on the Mac mini. The one try with Java 21 failed, so do not move to 21 without a
  full boot test. See
  [Arclight runtime](okf/server/arclight.md#java-17-only).
- Keep `arclight.jar` pointed at the hardened jar
  `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar`, which git tracks together with its patch in
  `patches/`. A stock Arclight build brings back the shutdown crash of 2026-06-29.
- Do not commit secrets or runtime state. Git ignores all of `world/`, and only the three datapacks in
  `world/datapacks/` were force-added, so new files there need `git add -f`. It also ignores
  `server.properties`, `unit.conf`, every `.vault_pass.txt` (the vault expects
  `ansible/.vault_pass.txt`), `mods/`, `client-only-mods/`, `logs/`, `modernfix/` and `.arclight/`. The
  folder `plugins/` is not ignored, and it holds player data in `plugins/Essentials/userdata/`, so read
  `git status` before every commit. Real secrets live in the ansible vault.
- Treat `mods/` as state that the repo cannot rebuild. It holds the index jars, eleven of them patched
  in place, the bundled jars from `local-mods/` (three of them patched too, and the pack ships Simply
  Jetpacks 2 patched as `SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar`), BrassAmber BattleTowers and
  Falling Meteors Reimagined, which are in no pack. Change the set in `modrinth.index.json` or `local-mods/` and mirror the change
  into `mods/` by hand. Git holds no copy of a jar that a pointer file `local-mods/<jar>.fetch.json`
  names, ZeroCore 2 for now, because its license forbids one in this public repository. Fetch it with
  `tools/fetch_local_mods.py` before you mirror `local-mods/`, and never commit it. Update mods with
  `tools/update_mods.py`, never with `tools/resolve_all.py`. See
  [Mod set](okf/modpack/mod-set.md).
- Unify materials through `config/almostunified/` and tags, not by editing recipes mod by mod.
- Test every change against a real boot. A clean boot reaches `Done` with 0 FATAL lines, no crash
  report and no watchdog, and keeps the baseline of 2 accepted ERROR lines: the AllTheLeaks reflection
  into Citadel and the Essentials warning about the hybrid server. A test copy without EssentialsX shows
  the AllTheLeaks line alone. See
  [Logs and crashes](okf/server/logs-and-crashes.md#boot-error-baseline).
- Stop the server over RCON and wait until the java process has exited. Never stop it with a signal.
- Change server configs only while the java process is gone, because Forge writes them back from memory
  at every stop. They are `world/serverconfig/` and the ones some mods keep in `config/`: `Mekanism/`,
  `MekanismExtras/`, `ars_nouveau/`, `ars_nouveau-server.toml`, `colytra-server.toml`,
  `comforts-server.toml` and the two `veinmining-*.toml`. See
  [Logs and crashes](okf/server/logs-and-crashes.md#mekanisms-ore-switch-lost-at-a-stop-2026-11-02-172012).

## Where things live

| Task | Touch |
|---|---|
| Add or remove a Modrinth mod | The index `modrinth.index.json`, then the same jar in `mods/` by hand |
| Update mods | Script `tools/update_mods.py`, then `tools/patch_mods.py` and a boot test |
| Add a mod that is not on Modrinth | Folder `local-mods/` (tracked) and `mods/`, plus a file in `modrinth-permissions/` for the review. A jar whose license forbids a copy in this repository gets a pointer file `local-mods/<jar>.fetch.json` and a `.gitignore` line instead, see [Mod set](okf/modpack/mod-set.md#changing-the-mod-set) |
| Fetch the jars that pointer files name | Script `tools/fetch_local_mods.py`, then copy them into `mods/` |
| Unify materials and duplicate recipes | File `config/almostunified/unify.json` (`modPriorities`, `ignoredItems`), and script `kubejs/server_scripts/unify_inventory.js` for the items players carry |
| Recipe and tag tweaks | Scripts `kubejs/server_scripts/recipe_fixes.js`, `tag_fixes.js` and `gameplay.js`. No loot code exists yet |
| Combat feel (no attack cooldown) | Script `kubejs/server_scripts/combat_1_7.js` |
| Broken or unwanted data of other mods, such as duplicate ores | The override datapack `world/datapacks/skytekx3_fixes/`, which has no other source. Git ignores `world/`, so add its files with `git add -f` |
| Fixes inside mod jars | Script `tools/patch_mods.py`. Its `PACK_RULES` reach the `.mrpack` too, see [Boot fixes](okf/content/boot-fixes.md#jar-patches) |
| Skylands travel | Functions in `skylands-datapack/data/skytekx3/functions/travel/` |
| Skylands terrain and structures | Sources `skylands-datapack/` (shared, vanilla safe) and `skylands-datapack-forge/` (mod content), then `tools/sync_skylands_datapack.sh` |
| Deploy on Linux | Unit `systemd/skytekx3.service` and `ServerLinux.sh`, see [Deploy on Debian](okf/server/deploy.md) |
| Deploy on the Mac mini | See [Mac mini](okf/server/macos.md) |
| Build and publish the pack | Scripts `tools/build_mrpack.py`, which fetches the pointed jars, then `tools/publish_modrinth.py` |

### The Skylands in short

Since 2026-07-12 the Skylands are a pure datapack. An island mask, routed into the `continents` climate
channel, gives every column over the void the featureless biome `skytekx3:sky_void`. That biome sits in
no `has_structure` tag, so vanilla rejects every structure start over the void, modded ones included.
The only tags that list it are `#skytekx3:has_structure/sky_floaters` and the Dungeons Arise tags for
Keep Kayra and the mechanical nest, which only hold structures with a fixed start height in the air.

The shared pack registers only `minecraft` and `skytekx3` ids and boots on unmodded vanilla and Spigot.
Everything that names a mod lives in `skylands-datapack-forge/`. The shared pack also carries five
Forge biome modifier files for chocobos, which vanilla and Spigot ignore. After editing either source,
run `tools/sync_skylands_datapack.sh` and restart. The build script ships both packs in the `.mrpack`.
The design is on [Skylands worldgen](okf/content/skylands-worldgen.md).

Check a worldgen change in a pregenerated copy of the dimension, not by eye. The script
`tools/void_scan.py` lists structure starts and blocks below the islands, `tools/void_float.py`
separates what hangs from an island from what floats free, and `tools/structure_pieces.py` shows where
each piece of one structure landed. They read region files only and need numpy.

## Server operations (RCON)

```bash
mcrcon -H localhost -P 65535 -p "$RCON_PW" "<command>"
python3 tools/rcon.py 127.0.0.1 65535 "$RCON_PW" <command>    # Standard library alternative
macos/skytekx3ctl rcon "<command>"                            # On the Mac mini, reads the password itself
```

- To stop the server, send `save-all flush` and `stop`. Saving takes another 5 to 9 seconds after the
  `stop` reply. Wait until the java process is gone before you start it again or touch `world/`.
- Match the process with `pgrep -f 'java .*arclight[.]jar nogui'`. The plain pattern
  `'arclight.jar nogui'` also matches the shell that runs `pgrep` or `pkill`.
- Every logged signal stop went wrong, see [Stop path](okf/server/logs-and-crashes.md#stop-path).
- Pregenerate after every world wipe, one dimension at a time:
  `chunky world minecraft:overworld`, `chunky radius 1000`, `chunky start`, then the same for
  `skytekx3:skylands`.
- Keep `/locate` sweeps off the live server. A sweep over every modded structure tripped the watchdog
  on 2026-06-29.
- The full regen procedure is in the [README](README.md#maintenance) and on
  [World reset and pregen](okf/server/world-reset.md).

## Git conventions

- Commits are authored by the human who drives the work, not by the AI. Add no `Co-Authored-By`
  trailers for the agent.
- Use Conventional Commits in the Commitizen style, multi-line, with a body that explains why.
  - Types in use are `feat`, `fix`, `perf`, `chore` and `docs`.
  - Scopes in use are `skylands`, `worldgen`, `mods`, `pack`, `server`, `macos`, `content`, `recipes`,
    `combat`, `config`, `tools`, `deploy`, `release` and `okf`.
- Commit messages carry no em dashes, no en dashes, no hyphens used as dashes and no semicolons.
- Keep commits granular, one concern per commit, such as a mod group, a script or a config area.
- Never let a jar that a pointer file `local-mods/<jar>.fetch.json` names into any commit, at any path,
  because a push publishes the whole history and its license forbids that copy. Before every push,
  `git log --oneline --find-object=f94d426ce1c02a3d3cbb16ddaf09db424c5435ae HEAD` must print nothing
  for ZeroCore 2. For another pointed jar, look for the id that `git hash-object` gives for its
  fetched copy. If such a jar got into a commit, rewrite the history without it before the push.
  The scripts `tools/build_mrpack.py` and `tools/publish_modrinth.py` refuse until then.
- The default branch is `master`.

## Writing docs

These rules hold for the README, this file and the OKF wiki.

- Write in English.
- Use no em dashes, no en dashes and no hyphens as dashes, no semicolons in prose and no exclamation
  marks. Use no emoji and no warning signs.
- Start every sentence, list item and table cell with a capital letter. Rephrase rather than start with
  a code identifier or a lowercase name. Do not open a paragraph or list item with a bold label.
- Do not number phases or steps in headings.
- Give every OKF page the front matter keys `type`, `title`, `description`, `tags` and `timestamp`.
- Cite the evidence for a claim: a file and line, a commit, a log or the owner's prompt with its time.
  Mark what nobody verified.
- Move retired designs into a short History section at the end of a page.
- Name no real persons. Credit the community as ts4.party.

## Gotchas learned the hard way

- Never add `skytekx3:sky_void` to `#minecraft:is_overworld`, `#forge:is_overworld` or another mod's
  `has_structure` tag. Tag chains bring structures back onto the Y 0 floor over the void.
- Structure sets are global. A set tuned for the Skylands also places in the Overworld wherever the
  structures' biome tags match, see
  [Skylands worldgen](okf/content/skylands-worldgen.md#structure-sets-are-global).
- Pack order decides which datapack wins. A world datapack that sits before a mod's own pack loses to
  it, as the ChocoCraft overrides do in the current world. Check the order with `/datapack list enabled`.
- Worldgen registries only rebind on a restart, not on `/reload`, and edits only reach chunks that do
  not exist yet.
- Rhino can resolve no-argument accessors such as `level()` or `dimension()` to objects. The first fall
  script threw a `TypeError` every tick and never teleported anyone. Load classes with
  `Java.loadClass` and resolve constants at load time, as `combat_1_7.js` does, then test in game.
- The suffix `-neoforge` in a jar name, as in the Aether Genesis jar, is part of the file name and not
  the loader. The jar declares `javafml` and loads on Forge.
- The update script takes betas by default. Pass `--channels release` for release builds only, and keep
  Cataclysm at 3.31, the version the Cataclysm Spellbooks build on the server was ported to.
- The pack build takes every override from git HEAD, so commit what should ship before a build.
  Runtime rewrites that are not committed stay out of the pack. Test a new pack in a fresh instance and
  stay in the world until EMI has reloaded before an upload.
- Client-only mods in the server's `mods/` stop the boot. They belong in `client-only-mods/`.
- After a wipe, always pregenerate, or the first players pay the worldgen cost.
- Retired designs stay retired: the `skytekx:surface_spread` structure placement of the old
  skytekx-worldgen mod, the KubeJS fall script and the Java fall handler. SkyTekx Compat 2.0.0 only
  guards Bird's Nests now, see [SkyTekx Compat](okf/content/compat-mod.md).
