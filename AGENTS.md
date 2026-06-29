# AGENTS.md — working in `skytekx3-1201-server`

Guidance for AI agents (and humans) contributing to the SkyTekx 3 server/modpack. Read this first,
then browse the [OKF wiki](okf/index.md) for the per-subsystem deep dives.

## What this repo is

A **Minecraft 1.20.1 / Forge 47 / Arclight** hybrid server *and* the modpack that runs on it. It is
both the deployable server (config, deploy units, world datapacks) and the source of truth for the
published Modrinth pack (`modrinth.index.json` + `local-mods/` overrides). See [`README.md`](README.md)
for the stack and self-hosting steps.

## Golden rules

- **Java 17 only.** Arclight 1.20.1 dies on Java 21 (`IInventoryBridge NoClassDefFound`). Never bump it.
- **Don't commit secrets or runtime state.** `server.properties`, `unit.conf`, `.vault_pass.txt`,
  `world/` (except `world/datapacks/`), `mods/`, `client-only-mods/`, `logs/`, `modernfix/`,
  `.arclight/` are gitignored for a reason. Real secrets live in the **ansible vault**.
- **`mods/` is derived, not source.** It is rebuilt from `modrinth.index.json` via
  `tools/resolve_all.py`. To add/remove a Modrinth mod, edit the **index**, not `mods/`.
  A mod that is **not on Modrinth** goes in `local-mods/` (tracked) instead.
- **Materialization stays internal.** Unify materials through `config/almostunified/` and tags, not by
  hand-editing recipes per mod.
- **Test changes against a real boot.** A clean boot = `Done` reached, **0 FATAL / crash / watchdog**.
  Many pre-existing mod `ERROR` lines in the log are benign; only regressions you introduced matter.

## Where things live

| Task | Touch |
|---|---|
| Add/remove a Modrinth mod | `modrinth.index.json` (then `tools/resolve_all.py`) |
| Add a non-Modrinth mod | drop the jar in `local-mods/` (it's tracked) |
| Material unification / dup recipes | `config/almostunified/unify.json` (`modPriorities`) |
| Recipe / tag / loot tweaks | `kubejs/server_scripts/{recipe_fixes,tag_fixes,gameplay}.js` |
| Combat feel (1.7 swing, no cooldown) | `kubejs/server_scripts/combat_1_7.js` |
| Skylands fall-to-overworld | `kubejs/server_scripts/skylands_fall.js` |
| Skylands structures / worldgen | `skylands-datapack/` **and** mirror into `world/datapacks/` |
| Deploy | `systemd/skytekx3.service`, `ServerLinux.sh`, `ansible/` |
| Publish the pack | `tools/build_mrpack.py` |

> The Skylands custom structure-placement type `skytekx:surface_spread` is provided by a
> **source-built worldgen mod** (Mixin + a `StructurePlacementType` that gates on heightmap surface Y).
> If you change structure sets, keep `skylands-datapack/` and the runtime `world/datapacks/` **in sync** —
> `build_mrpack.py` ships `skylands-datapack/` into the `.mrpack`.

## Server operations (RCON)

```bash
mcrcon -H localhost -P 65535 -p "$RCON_PW" "<cmd>"
```

- Graceful stop: `save-all flush` → `stop`. Shutdown can hang ~60 s ("threads not shutting down…");
  if it does, SIGKILL the java pid as a fallback, then confirm it's gone before relaunching.
- Pregen: Chunky — `chunky radius 1000` → `chunky start`; do this after any world wipe.
- Full world regen procedure is in [`README.md`](README.md#maintenance) and
  [`okf/server/world-reset.md`](okf/server/world-reset.md).

## Git conventions

- **Author commits as the human driving the work** (the project owner / whoever is running you), **not**
  as the AI — and don't add `Co-Authored-By` trailers crediting the agent.
- **Conventional Commits / Commitizen**, multi-line, with a body explaining the *why*:
  - `feat(scope): …`, `fix(scope): …`, `chore(scope): …`, `docs(scope): …`, `refactor(scope): …`.
  - Scopes used here: `mods`, `worldgen`, `skylands`, `combat`, `recipes`, `config`, `deploy`, `tools`, `okf`.
- Keep commits **granular** — one concern per commit (a mod group, a script, a config area), not one
  giant "update everything".
- `master` is the default branch.

## Gotchas learned the hard way

- A heightmap-gated structure can still leak to the y0 void floor if only the **start-chunk center** is
  checked — gate on a `min_surface_y` high enough that the whole footprint sits on real terrain.
- KubeJS uses native **Mojmap** APIs here (see `combat_1_7.js` for the attribute pattern); cross-dim
  teleports (`skylands_fall.js`) only fire on a real void-fall, so load-verify, then test in-world.
- The `-neoforge` suffix on some jars (e.g. Aether) is a filename, not a loader — they declare
  `javafml` + `forge` and load fine on Forge.
- After a bulk delete/regen, **always pregen** or the first players pay the worldgen cost and the edge
  throttles.
