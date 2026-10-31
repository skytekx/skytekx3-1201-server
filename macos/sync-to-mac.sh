#!/usr/bin/env bash
# Push the server files from this checkout to the Mac mini over SSH.
#   macos/sync-to-mac.sh user@host                  update mods, configs, scripts and datapacks
#   macos/sync-to-mac.sh user@host --with-world     first deploy: also copy world/ and the live files
# Without --with-world the live state on the Mac is never touched: world/, player and plugin user data,
# server.properties (it holds the RCON password), ops, whitelist, bans, caches, eula.txt, the Chunky
# task records and server-icon.png (a seasonal icon such as server-icon-halloween.png is copied over
# it on the Mac).
# mods/ and the two Skylands datapack sources are mirrored exactly (removed files disappear on the Mac).
# Other deletions are done by hand. Run tools/sync_skylands_datapack.sh on the Mac after a datapack change.
# Stop the server first when mods change: macos/skytekx3ctl stop, sync, macos/skytekx3ctl start.
set -euo pipefail
HOST="${1:?usage: $0 user@host [--with-world]}"
MODE="${2:-}"
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
LIVE_STATE=(
  --exclude /world/ --exclude /plugins/Essentials/userdata/ --exclude /server.properties
  --exclude /ops.json --exclude /whitelist.json --exclude /banned-players.json --exclude /banned-ips.json
  --exclude /usercache.json --exclude /usernamecache.json --exclude /eula.txt --exclude /local/
  --exclude /server-icon.png --exclude /config/chunky/tasks/
)
if [ "$MODE" = --with-world ]; then
  LIVE_STATE=()
fi

rsync -a "${NOT_RUNTIME[@]}" ${LIVE_STATE[@]+"${LIVE_STATE[@]}"} "$SRC/" "$HOST:$DEST/"
for d in mods skylands-datapack skylands-datapack-forge; do
  rsync -a --delete "$SRC/$d/" "$HOST:$DEST/$d/"
done
echo "Synced $SRC to $HOST:$DEST${MODE:+ ($MODE)}"
