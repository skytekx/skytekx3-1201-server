# SkyTekx 3: Hexxit × Tekkit × SkyFactory

SkyTekx 3 is the third generation of SkyTekx, a modpack by ts4.party, the TeamSpeak for party, formerly
known as ts3.party. It follows SkyTekx on Minecraft 1.7.10
([`skytekx-1710-server`](https://github.com/skytekx/skytekx-1710-server)) and SkyTekx 2 on 1.12.2
([`skytekx2-1122-server`](https://github.com/skytekx/skytekx2-1122-server)) and brings the pack to
Minecraft 1.20.1.

It mixes tech, magic and adventure and runs as a Forge and Bukkit hybrid on Arclight, so Forge mods and
the EssentialsX plugin share one server. Its own Skylands dimension of floating islands replaces the
skyblock plugin of SkyTekx 2.

The website is <https://skytekx.com>. The Modrinth project `skytekx3` is not public yet, and
skytekx.com does not point at a game server yet.

## Stack

| Part | Version |
|---|---|
| Minecraft | 1.20.1 |
| Loader | Forge 47.4.18 inside Arclight, the same build the pack index asks clients for |
| Server | Arclight 1.20.1 1.0.6 in a hardened build, `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar`, started through the symlink `arclight.jar` |
| Java | Java 17. The boots that reached `Done` ran Temurin 17.0.19 on the build machine and 17.0.21 on the Mac mini. The one try with Java 21 failed on a missing mod and was never repeated |
| Plugins | EssentialsX 2.22.0 |
| Mods | 217 Modrinth entries in `modrinth.index.json` plus 35 bundled jars in `local-mods/` |
| Recipe layers | AlmostUnified for one item per material, Polymorph for grid conflicts, KubeJS for scripts |
| Recipe viewers | EMI and JEI |
| Pregen and profiling | Chunky and Spark |

## Repository layout

| Path | What |
|---|---|
| File `modrinth.index.json` | The pack manifest with 217 entries, all from the Modrinth CDN. Git ignores `mods/`, and no script rebuilds it from the index, see [Provide the mods](#provide-the-mods) |
| Folder `local-mods/` | The 35 jars the pack bundles because they are built from source or not on Modrinth, such as Twilight Forest, Nyx and SkyTekx Compat. Forge does not load this folder, the jars have to be in `mods/` |
| Folder `config/` | Mod configuration, with `config/almostunified/` for material unification |
| Folder `kubejs/server_scripts/` | The four server scripts `recipe_fixes.js`, `tag_fixes.js`, `gameplay.js` and `combat_1_7.js` |
| Folder `skylands-datapack/` | Source of the shared Skylands datapack. It only uses vanilla ids and also runs on unmodded Spigot |
| Folder `skylands-datapack-forge/` | Source of the Forge only Skylands content: Dungeons Arise sets, floaters from mod pools, Botania features |
| Folder `world/datapacks/` | The three datapacks the world loads. Git ignores `world/`, these three were force-added. Two are copies of the sources above, `skytekx3_fixes` has no other source |
| Symlink `arclight.jar` and folder `patches/` | The hardened Arclight jar with its symlink, both tracked, and the source patch behind it |
| Folder `modrinth-permissions/` | One file per bundled jar for the Modrinth permission review |
| Folder `scripts-legacy-reference/` | The CraftTweaker scripts of SkyTekx 2, a read-only reference for the KubeJS port |
| Folder `tools/` | Eleven scripts for datapack sync, mod updates, jar patches, the pack build, the Modrinth upload, RCON, region scans of the Skylands and the Cloudflare Tunnel, see [Maintenance](#maintenance) |
| Folder `docs/` | The recipe port notes `RECIPE_PORT.md` and the Modrinth texts, the body `MODPACK_DESCRIPTION.md` and the summary `MODPACK_SUMMARY.txt` |
| Folder `okf/` | The project wiki in Open Knowledge Format. Start at [okf/index.md](okf/index.md) |
| File `AGENTS.md` | Rules and pitfalls for anyone, human or agent, who edits this repo |
| Folder `macos/` and `ServerMac.sh` | The macOS deployment of the production server: launcher, launchd jobs, RCON firewall, backup, control script and the sync from the build machine |
| Folder `systemd/` and `ServerLinux.sh` | The Linux deployment |
| Folder `ansible/` | The encrypted vault with the Modrinth token and the RCON password |

## Self-hosting the server

This guide sets up a fresh Debian or Ubuntu host. The server runs under systemd without `screen` and is
controlled over RCON. The systemd unit has not run on a logged host yet, so check each step. Details
are on [Deploy on Debian (systemd)](okf/server/deploy.md). The production server runs on a Mac mini
under launchd instead, see [Self-hosting on macOS](#self-hosting-on-macos).

### Prepare the host

```bash
apt-get update && apt-get dist-upgrade -y
apt-get install -y openjdk-17-jre-headless ca-certificates-java build-essential git ufw rsync python3
```

Arclight 1.20.1 needs Java 17. Debian and Ubuntu install it under `/usr/lib/jvm/java-17-openjdk-<arch>`,
for example `java-17-openjdk-amd64`, while the unit and `ServerLinux.sh` call
`/usr/lib/jvm/java-17-openjdk/bin/java`. Fix the path in the `ExecStart` line of
`systemd/skytekx3.service` and pass `JAVA=` to `ServerLinux.sh`. If the distribution no longer ships
Java 17, install Temurin 17 from Eclipse Adoptium, the build that every logged run used.

### Create a dedicated user

```bash
adduser minecraft --system --group --home /opt/minecraft-server --disabled-login
mkdir -p /opt/minecraft-server/{backup/server,build/mcrcon,server}
```

### Build mcrcon

The systemd unit stops the server through mcrcon.

```bash
cd /opt/minecraft-server/build/mcrcon
git clone https://github.com/Tiiffi/mcrcon.git code && cd code
gcc mcrcon.c -o mcrcon && mv mcrcon /usr/local/bin/
```

### Clone the repo

```bash
git clone https://github.com/skytekx/skytekx3-1201-server.git \
  /opt/minecraft-server/server/skytekx3-1201-server
cd /opt/minecraft-server/server/skytekx3-1201-server
```

The clone brings the hardened Arclight jar and the symlink `arclight.jar` that points at it. Keep the
symlink on that jar. A stock Arclight release brings back the crash at shutdown of 2026-06-29, see
[Arclight runtime](okf/server/arclight.md#the-hardened-jar).

### Provide the mods

Git does not hold `mods/`. Copy the folder from a running install as it is. It holds 235 jars, among
them jars patched in place, BrassAmber BattleTowers and Falling Meteors Reimagined, which exist nowhere
else in the repo, see
[Mod set](okf/modpack/mod-set.md#jars-outside-the-index).

No script restores `mods/` from the pinned index yet. Do not use `tools/resolve_all.py` for it, because
it ignores the index and installs whatever is newest, including Radium, which crashed Arclight on
2026-06-27. A rebuild from the index takes four steps.

1. Download every index entry whose `env.server` is not `unsupported` and check its hash against the
   index.
2. Copy the jars from `local-mods/` into `mods/`.
3. Run `python3 tools/patch_mods.py`.
4. Add BrassAmber BattleTowers and Falling Meteors Reimagined, which are in no pack.

The plugin jar `plugins/EssentialsX-2.22.0.jar` and its folder `plugins/Essentials/` are not in git
either. Copy them along, or the server has no `/gm` and no other Essentials command.

### Configure the server

```bash
cp server.properties.example server.properties
# Set rcon.password to a new strong password. The example already sets enable-rcon=true,
# rcon.port=65535, server-port=25565 and max-players=39.
echo "eula=true" > eula.txt
# Bring the world copies of the Skylands datapacks in line with their sources
tools/sync_skylands_datapack.sh
```

The example leaves `level-seed` empty, so a new world gets a random seed. A new world also needs its
datapack order checked, see [World reset and pregen](okf/server/world-reset.md#fresh-world).

### Open the firewall

```bash
ufw allow 22/tcp
ufw allow 25565/tcp     # Game
ufw allow 24454/udp     # Simple Voice Chat
ufw enable              # The default policy denies the rest, RCON 65535 included
chown -R minecraft:minecraft /opt/minecraft-server/
```

RCON listens on every interface (`server-ip=0.0.0.0`) and sends its password in clear text. Never open
port 65535.

### Launch

For a foreground test, start the launcher with the Java 17 path of the host. It uses Aikar's G1 flags
and an 11 GB heap, sized for a 16 GB host. The variables `JAVA`, `JAR` and `MEM` override its defaults.
Stop it by typing `stop` into the console, not with Ctrl+C.

```bash
JAVA=/usr/lib/jvm/java-17-openjdk-amd64/bin/java ./ServerLinux.sh
```

For production, the systemd unit reads `RCON_PW` from `unit.conf` in the repo root. Write it with the
same password as `rcon.password` and keep it private. Git ignores the file.

```bash
echo "RCON_PW=<the rcon.password from server.properties>" > unit.conf
chmod 600 unit.conf && chown minecraft:minecraft unit.conf
ln -s "$PWD/systemd/skytekx3.service" /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now skytekx3.service
systemctl status skytekx3.service
```

The unit restarts the server after a crash (`Restart=always`). It stops the server with
`mcrcon ... stop`, which returns before the save is done, and with `KillMode=none` systemd does not wait
for the JVM. Before you start the unit again or touch `world/`, wait until the java process is gone.

```bash
while pgrep -f 'java .*arclight[.]jar nogui' >/dev/null; do sleep 1; done
```

Never stop the server with a signal. Every logged signal stop went wrong, see
[Logs and crashes](okf/server/logs-and-crashes.md#stop-path).

## Self-hosting on macOS

The production server runs on an Apple M4 Mac mini with 16 GB under launchd, which starts it at boot
with nobody logged in, starts it again after a crash and stops it over RCON. Details and measured
numbers are on [Mac mini (macOS, launchd) deployment](okf/server/macos.md).

Install Java 17 on the Mac and keep FileVault off, or the Mac waits at the unlock screen after a power
cut and the server stays down.

```bash
brew install --cask temurin@17
xcode-select --install     # Brings /usr/bin/python3, which the clean stop and the backup need for RCON
```

The first copy comes from the build machine, which holds the checkout and `mods/`. With `--with-world`
it also brings `world/` and the live files such as `server.properties`. It refuses when the Mac already
has a world, unless `--overwrite-world` is given too.

```bash
macos/sync-to-mac.sh user@mac-mini --with-world
```

Then install the launchd jobs once on the Mac.

```bash
cd ~/skytekx3-1201-server
sudo macos/install.sh
```

The script turns sleep off and the automatic restart after a power cut on, installs pf rules that keep
RCON (65535/tcp) on the loopback interface, and loads three jobs: the server, the RCON guard and a
world backup every day at 05:00. Run the server and its tools through `macos/skytekx3ctl`.

```bash
macos/skytekx3ctl status            # launchd state, players and tick times
macos/skytekx3ctl restart           # RCON stop, launchd starts the server again
macos/skytekx3ctl stop              # Stops it over RCON and keeps it down, also across reboots (sudo)
macos/skytekx3ctl start             # Enables the job and starts it again (sudo)
macos/skytekx3ctl rcon "list"       # One console command
```

Updates also come from the build machine. The commands `stop` and `start` call sudo, so the SSH
sessions need `-t`.

```bash
ssh -t user@mac-mini 'cd ~/skytekx3-1201-server && macos/skytekx3ctl stop'
macos/sync-to-mac.sh user@mac-mini
ssh -t user@mac-mini 'cd ~/skytekx3-1201-server && tools/sync_skylands_datapack.sh && macos/skytekx3ctl start'
```

Without `--with-world` the sync never touches the world (but for the fixes datapack), the player
data, `server.properties` or the other live files. It does copy `config/`, and since the mod update of 2026-10-31 the Mac's configs
differ from the checkout. Check every sync with a checksum dry run first, see
[Mac mini](okf/server/macos.md#deploy-and-update).

## Maintenance

### Regenerate the world

Worldgen changes only reach chunks that do not exist yet. To regenerate every dimension while keeping
the seed, the datapacks, the server configs and the player data:

```bash
# Stop over RCON and wait until the java process has exited
mcrcon -H localhost -P 65535 -p "$RCON_PW" "save-all flush" stop
while pgrep -f 'java .*arclight[.]jar nogui' >/dev/null; do sleep 1; done
# Back up, then wipe the generated chunk data only
tar czf ~/skytekx3-world-$(date +%F).tgz world
rm -rf world/region world/entities world/poi world/DIM-1 world/DIM1 world/dimensions
systemctl start skytekx3.service
# Pregenerate the Overworld, and when Chunky reports it done, the Skylands
mcrcon -H localhost -P 65535 -p "$RCON_PW" "chunky world minecraft:overworld" "chunky radius 1000" "chunky start"
mcrcon -H localhost -P 65535 -p "$RCON_PW" "chunky world skytekx3:skylands" "chunky radius 1000" "chunky start"
```

Always pregenerate after a wipe, so players do not pay the worldgen cost. On the Mac mini use
`macos/skytekx3ctl stop`, `start` and `rcon` for the server steps. A reset of the Skylands alone and the
measured pregen rates are on [World reset and pregen](okf/server/world-reset.md).

### Change the Skylands

Edit `skylands-datapack/` or `skylands-datapack-forge/`, never the copies in `world/datapacks/`, then
run `tools/sync_skylands_datapack.sh` and restart the server. Worldgen does not reload with `/reload`.
The design and its rules are on [Skylands worldgen](okf/content/skylands-worldgen.md).

### Update mods

```bash
python3 tools/update_mods.py                      # Dry run, lists the updates
python3 tools/update_mods.py --channels release   # Releases only, the default also takes betas
python3 tools/update_mods.py --apply              # Rewrites the index and swaps the jars in mods/
python3 tools/patch_mods.py                       # Puts the jar patches back on updated jars
```

Then boot the server and compare the log with the baseline of 3 ERROR lines, see
[Logs and crashes](okf/server/logs-and-crashes.md#boot-error-baseline). Keep Cataclysm at 3.31, the
Cataclysm Spellbooks build on the server was ported to exactly that version.

### Build and publish the pack

```bash
python3 tools/build_mrpack.py             # Writes SkyTekx3-<version>.mrpack from git HEAD
export MODRINTH_TOKEN="$MODRINTH_PAT"     # The token from the vault, see Secrets
python3 tools/publish_modrinth.py         # Dry run, checks the token and the project
python3 tools/publish_modrinth.py --go    # Uploads the version and checks its hash
```

The build script only builds. It takes every override from git HEAD, so commit what should ship
first, and it rewrites `modrinth.index.json` with the version, the summary and the Forge build, which
you commit after the build. Raise `VERSION` in `tools/build_mrpack.py` and `tools/publish_modrinth.py`
before a new build. The upload only happens with `--go`. Test the pack in a fresh instance before the
upload. What blocks a public release is on [Publish](okf/modpack/publish.md).

### Tools

| Script | Job |
|---|---|
| Sync, `tools/sync_skylands_datapack.sh` | Mirrors the two Skylands datapack sources into `world/datapacks/` |
| Update, `tools/update_mods.py` | Lists or applies newer Modrinth builds of the index entries, no token needed |
| Patch, `tools/patch_mods.py` | Puts the boot fixes back into mod jars in `mods/` |
| Build, `tools/build_mrpack.py` | Builds the `.mrpack` from the index, the configs, the scripts, the datapacks and `local-mods/` |
| Publish, `tools/publish_modrinth.py` | Uploads the `.mrpack` to Modrinth, only with `--go` |
| RCON, `tools/rcon.py` | Sends one RCON command, for example `python3 tools/rcon.py 127.0.0.1 65535 "$RCON_PW" list` |
| Scan, `tools/void_scan.py` | Lists structure starts and blocks below the Skylands islands and in void columns, read only |
| Scan, `tools/void_float.py` | Tells blocks that hang from an island apart from blocks that float free in the void |
| Scan, `tools/structure_pieces.py` | Prints where every piece of one structure ended up, with its projection and box |
| Legacy, `tools/resolve_all.py` | Legacy resolver of a fixed slug list. Do not use it |

More on each script is on [Tools](okf/modpack/tools.md).

## Secrets

The file `ansible/group_vars/all/vault.yml` is encrypted with ansible-vault (AES256). It holds the
Modrinth token as `vault_modrinth_pat` and the RCON password as `vault_rcon_password`.

- No tool reads the vault. The scripts `tools/rcon.py` and `tools/resolve_all.py` take the password
  or the token as a command line argument, and `tools/publish_modrinth.py` reads the token from
  `MODRINTH_TOKEN`.
- The vault password is never committed. The config `ansible/ansible.cfg` expects it in
  `ansible/.vault_pass.txt`, which git ignores. Until that file exists, `--ask-vault-pass` prompts for
  it.
- The live `server.properties` and `unit.conf` carry the RCON password, and git ignores both. Commit only
  `server.properties.example`.

```bash
ansible-vault view --ask-vault-pass ansible/group_vars/all/vault.yml
```

More on secrets, and which values must be rotated, is on [Secrets](okf/server/secrets.md).

## Design goals

SkyTekx 3 should feel as much like the original SkyTekx as possible. It keeps as many of the original
mods as it can, adds a small set of new ones, and keeps recipes free of conflicts with the same layers
the 1.12.2 pack used: AlmostUnified in place of UniDict, Polymorph in place of NoMoreRecipeConflict and
KubeJS in place of CraftTweaker.

For the architecture, the Skylands, the Nyx port, deployment and publishing, see the
[OKF wiki](okf/index.md) and [AGENTS.md](AGENTS.md).
