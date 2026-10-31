#!/usr/bin/env bash
# Push the server files from this checkout to the Mac mini over SSH.
#   macos/sync-to-mac.sh user@host                  update mods, configs, scripts and datapacks
#   macos/sync-to-mac.sh user@host --with-world     first deploy: also copy world/ and the live files
# --with-world refuses to run when the Mac already has world/level.dat, because it would copy this
# checkout's world over the live one. Add --overwrite-world to do that on purpose.
# Without --with-world the live state on the Mac is never touched: world/ (but for the fixes datapack,
# see below), player and plugin data (everything in plugins/Essentials/ except config.yml),
# server.properties (it holds the RCON password), ops, whitelist, bans, caches, eula.txt, the Chunky
# task records and server-icon.png (a seasonal icon such as server-icon-halloween.png is copied over
# it on the Mac).
# mods/ and the two Skylands datapack sources are mirrored exactly (removed files disappear on the Mac).
# So is world/datapacks/skytekx3_fixes/, which has no other source, straight into the Mac's world (only
# that folder, never region files or level.dat). The server applies it after a restart or
# /minecraft:reload (the vanilla data reload, not the Bukkit /reload that also reloads plugins).
# Other deletions are done by hand. Run tools/sync_skylands_datapack.sh on the Mac after a datapack change.
# Stop the server first when mods change: macos/skytekx3ctl stop, sync, macos/skytekx3ctl start.
set -euo pipefail
USAGE="usage: $0 user@host [--with-world [--overwrite-world]]"
HOST="${1:?$USAGE}"
shift
WITH_WORLD=""
OVERWRITE_WORLD=""
for arg in "$@"; do
  case "$arg" in
    --with-world) WITH_WORLD=1 ;;
    --overwrite-world) OVERWRITE_WORLD=1 ;;
    *) echo "Unknown option $arg, $USAGE" >&2; exit 2 ;;
  esac
done
if [ -n "$OVERWRITE_WORLD" ] && [ -z "$WITH_WORLD" ]; then
  echo "--overwrite-world only goes with --with-world, $USAGE" >&2
  exit 2
fi
SRC="$(cd "$(dirname "$0")/.." && pwd)"
DEST="${DEST_DIR:-skytekx3-1201-server}"

NOT_RUNTIME=(
  --exclude .git/ --exclude okf/ --exclude docs/ --exclude scripts-legacy-reference/
  --exclude menu-assets/ --exclude client-only-mods/ --exclude disabled-mods/ --exclude patches/
  --exclude /crash-reports/ --exclude /logs/ --exclude '/*.log' --exclude '*.mrpack' --exclude '*.orig'
  --exclude '*-installer.jar' --exclude ansible/ --exclude .vault_pass.txt --exclude .vaultpw
  --exclude RuinsPositionsFile.txt --exclude spark/ --exclude debug/ --exclude .wrangler/
  --exclude .mixin.out/ --exclude __pycache__/ --exclude session.lock
)
# Essentials writes player data next to its config (usermap.bin, uuids.bin, userdata/) and keeps
# in-game changes there (kits.yml, tpr.yml, warps/), so only config.yml comes from the checkout.
LIVE_STATE=(
  --exclude /world/ --include /plugins/Essentials/config.yml --exclude '/plugins/Essentials/*'
  --exclude /server.properties
  --exclude /ops.json --exclude /whitelist.json --exclude /banned-players.json --exclude /banned-ips.json
  --exclude /usercache.json --exclude /usernamecache.json --exclude /eula.txt --exclude /local/
  --exclude /server-icon.png --exclude /config/chunky/tasks/
)
if [ -n "$WITH_WORLD" ]; then
  LIVE_STATE=()
  if [ -z "$OVERWRITE_WORLD" ]; then
    rc=0
    ssh "$HOST" test -e "$DEST/world/level.dat" || rc=$?
    case "$rc" in
      0) echo "Refusing --with-world: $HOST:$DEST/world/level.dat exists, and this checkout's world/ would be copied over it." >&2
         echo "To do that on purpose, stop the server, take a backup and add --overwrite-world." >&2
         exit 1 ;;
      1) ;;   # No world on the Mac yet
      *) echo "Could not check $HOST:$DEST/world/level.dat (ssh exit $rc)" >&2
         exit 1 ;;
    esac
  fi
fi

rsync -a "${NOT_RUNTIME[@]}" ${LIVE_STATE[@]+"${LIVE_STATE[@]}"} "$SRC/" "$HOST:$DEST/"
for d in mods skylands-datapack skylands-datapack-forge; do
  rsync -a --delete "$SRC/$d/" "$HOST:$DEST/$d/"
done
# The fixes pack exists only inside world/, which the sync above skips. A fresh world gets the folder
# before its first boot, as the docs ask.
ssh "$HOST" mkdir -p "$DEST/world/datapacks"
rsync -a --delete "$SRC/world/datapacks/skytekx3_fixes/" "$HOST:$DEST/world/datapacks/skytekx3_fixes/"
echo "Synced $SRC to $HOST:$DEST${WITH_WORLD:+ (with world)}"
echo "A change to world/datapacks/skytekx3_fixes only takes effect after a restart or /minecraft:reload."
