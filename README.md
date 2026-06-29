# SkyTekx 3 — Minecraft 1.20.1 / Forge / Arclight

Gen-3 rebuild of **SkyTekx**, the ts3.party community modpack: the 1.7.10 original
([`skytekx-1710-server`](https://github.com/skytekx/skytekx-1710-server)) → 1.12.2
([`skytekx2-1122-server`](https://github.com/skytekx/skytekx2-1122-server)) → **this**, on 1.20.1.

A Hexxit × Tekkit × SkyFactory kitchen-sink of tech, magic and adventure, run as a **Forge + Bukkit
hybrid** (Arclight) so the EssentialsX / LuckPerms / Multiverse / WorldEdit plugin layer carries over
alongside the Forge mods. Ships a bespoke **custom Skylands floating-islands dimension**.

Public server: **`skytekx.com`** · Site: <https://skytekx.com> · Modpack: <https://modrinth.com/modpack/skytekx3>

---

## Stack

| | |
|---|---|
| Minecraft | 1.20.1 |
| Loader | Forge 47.x |
| Server | **Arclight** (forge-1.20.1) hybrid — Mohist 1.20.1 is the documented fallback |
| Java | **17** — Arclight 1.20.1 crashes on Java 21 (`IInventoryBridge` `NoClassDefFound`). Do **not** use 21. |
| Recipe engine | AlmostUnified (unify) + Polymorph (in-grid conflicts) + KubeJS (scripts) |
| Recipe viewer | EMI |
| Pregen / profiling | Chunky · Spark |

---

## Repository layout

| Path | What |
|---|---|
| `modrinth.index.json` | The pack manifest — every Modrinth-resolvable mod (213 files). `mods/` is rebuilt from this, so `mods/` is **gitignored**. |
| `local-mods/` | Mods built from source / not on Modrinth (Twilight Forest, Nyx, …). **Tracked** — these are part of the pack. |
| `config/` | All mod configuration, incl. `config/almostunified/` (material unification). |
| `kubejs/server_scripts/` | Gameplay scripts: `recipe_fixes.js`, `tag_fixes.js`, `gameplay.js`, `combat_1_7.js`, `skylands_fall.js`. |
| `skylands-datapack/` | **Source** datapack for the custom Skylands worldgen (structure sets + biome tags). |
| `world/datapacks/` | The deployed copy of the datapack (built into the `.mrpack`; `world/` itself is gitignored except this). |
| `scripts-legacy-reference/` | Original 1.12.2 CraftTweaker `.zs` — read-only reference for the KubeJS port. |
| `tools/` | `resolve_all.py` (fetch mods from the index), `build_mrpack.py` (build + publish the modpack). |
| `docs/` | `RECIPE_PORT.md`, `MODPACK_DESCRIPTION.md`. |
| `okf/` | Open Knowledge Format wiki for AI agents working on the pack — start at `okf/index.md`. |
| `AGENTS.md` | Conventions and gotchas for anyone (human or agent) editing this repo. |
| `systemd/skytekx3.service`, `ServerLinux.sh`, `ansible/` | Deploy. |

---

## Self-hosting the server

A from-scratch guide for a fresh **Debian / Ubuntu** box. The setup runs **without `screen`** — the
server runs under **systemd** and is controlled over **RCON**.

> ⚠️ **Java 17 only.** Arclight 1.20.1 crashes on Java 21. Install `openjdk-17`.

### 1 — Prepare the host

```bash
apt-get update && apt-get dist-upgrade -y
apt-get install -y openjdk-17-jre-headless ca-certificates-java build-essential git ufw
```

### 2 — Create a dedicated, unprivileged user under `/opt`

```bash
adduser minecraft --system --group --home /opt/minecraft-server --disabled-login
mkdir -p /opt/minecraft-server/{backup/server,build/mcrcon,server}
```

### 3 — Build `mcrcon` (used by systemd to stop the server cleanly)

```bash
cd /opt/minecraft-server/build/mcrcon
git clone https://github.com/Tiiffi/mcrcon.git code && cd code
gcc mcrcon.c -o mcrcon && mv mcrcon /usr/local/bin/
```

### 4 — Clone the pack and provide the Arclight jar

```bash
git clone https://github.com/skytekx/skytekx3-1201-server.git \
  /opt/minecraft-server/server/skytekx3-1201-server
cd /opt/minecraft-server/server/skytekx3-1201-server

# Download an Arclight forge-1.20.1 build and symlink it (the versioned jar is gitignored):
#   https://github.com/IzzelAliz/Arclight/releases
ln -sf arclight-forge-1.20.1-<ver>.jar arclight.jar
```

Fetch the Modrinth-resolvable mods into `mods/` from the manifest (needs a Modrinth token, see
[Secrets](#secrets)). `local-mods/` is already in the repo and is loaded too:

```bash
python3 tools/resolve_all.py            # downloads mods/ from modrinth.index.json
```

### 5 — EULA, RCON, firewall

```bash
cp server.properties.example server.properties
# edit server.properties: set rcon.port=65535 and a strong rcon.password
# (server-port 25565, max-players 39, enable-rcon=true)
echo "eula=true" > eula.txt

sed -i 's/IPV6=yes/IPV6=no/g' /etc/default/ufw
ufw allow 22/tcp && ufw allow 25565/tcp && ufw enable     # RCON 65535 stays loopback-only
chown -Rv minecraft:minecraft /opt/minecraft-server/
```

### 6 — Launch

**Dev / quick start** (foreground, Aikar G1GC flags, 11 G heap):

```bash
./ServerLinux.sh        # JAVA=, JAR=, MEM= are overridable env vars
```

**Production (systemd):** the unit reads `${RCON_PW}` from an `unit.conf` `EnvironmentFile`
(gitignored — create it next to the repo with `RCON_PW=<your rcon password>`).

```bash
echo "RCON_PW=<your rcon password>" > unit.conf
ln -s "$PWD/systemd/skytekx3.service" /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now skytekx3.service
systemctl status skytekx3.service
```

The unit stops the server gracefully via `mcrcon … stop` and `Restart=always`.

---

## Maintenance

**Regenerate the world** (e.g. after worldgen/datapack changes — surfaces new structures everywhere):

```bash
# stop, back up, wipe generated chunk data (keep level.dat + datapacks), restart, pregen
mcrcon -H localhost -P 65535 -p "$RCON_PW" "save-all flush" stop
tar czf ~/skytekx3-world-backup.tgz -C . world
rm -rf world/region world/entities world/poi world/DIM-1 world/DIM1 world/dimensions
systemctl start skytekx3.service
mcrcon -H localhost -P 65535 -p "$RCON_PW" "chunky radius 1000" "chunky start"
```

**Rebuild / publish the modpack** (`.mrpack`):

```bash
python3 tools/build_mrpack.py           # builds SkyTekx3-<ver>.mrpack from the index + overrides
# publish: uploads to Modrinth (needs the Modrinth PAT from the vault)
```

> ⚠️ Always `VACUUM`-equivalent housekeeping after big deletes is N/A here, but **always pregen**
> after a wipe so worldgen cost is baked out of play.

---

## Secrets

`ansible/group_vars/all/vault.yml` is ansible-vault encrypted (AES256) and holds `vault_modrinth_pat`
(the Modrinth PAT used by `tools/` to fetch mods and publish the pack).

- The vault password is **never committed**. Put it in `.vault_pass.txt` (gitignored); `ansible/ansible.cfg` points to it.
- `server.properties` and `unit.conf` are gitignored (they carry the RCON password). Commit only `server.properties.example`.

```bash
ansible-vault view ansible/group_vars/all/vault.yml
```

---

## Design goals

Feel as much like the original SkyTekx as possible — keep the maximum set of original mods, add only a
curated handful of new ones, and keep recipes conflict-free using the same layered prevention the
1.12.2 pack used (UniDict → **AlmostUnified**, NoMoreRecipeConflict → **Polymorph**, CraftTweaker → **KubeJS**).

For deeper context — architecture, the Nyx port, the Skylands worldgen, the Aether-style fall, deploy
and the publish pipeline — see **[`okf/`](okf/index.md)** and **[`AGENTS.md`](AGENTS.md)**.
