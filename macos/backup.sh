#!/usr/bin/env bash
# Nightly world backup for the SkyTekx3 server on macOS (launchd job com.skytekx.backup, 05:00).
# Flushes the world over RCON with saving paused, archives world/ and keeps the newest KEEP archives.
# Skips the run when the disk is short on space, so a backup can never fill the disk under the server.
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
DEST="${BACKUP_DIR:-$HOME/skytekx3-backups}"
KEEP="${KEEP:-3}"
MIN_FREE_GB="${MIN_FREE_GB:-4}"

prop() { sed -n "s/^$1=//p" "$DIR/server.properties" | tr -d '\r'; }
rcon() { /usr/bin/python3 "$DIR/tools/rcon.py" 127.0.0.1 "$(prop rcon.port)" "$(prop rcon.password)" "$@"; }
say() { echo "$(date '+%F %T') $*"; }

free_gb=$(df -g "$DIR" | awk 'NR==2 {print $4}')
if [ "$free_gb" -lt "$MIN_FREE_GB" ]; then
  say "SKIP: only ${free_gb} GB free, need ${MIN_FREE_GB} GB"
  exit 1
fi

mkdir -p "$DEST"
paused=""
if rcon list >/dev/null 2>&1; then
  rcon save-off >/dev/null
  paused=1
  trap 'rcon save-on >/dev/null || true' EXIT
  rcon "save-all flush" >/dev/null
fi

name="world-$(date +%F-%H%M).tgz"
tar -czf "$DEST/$name.part" -C "$DIR" --exclude session.lock world
mv "$DEST/$name.part" "$DEST/$name"
say "OK: $DEST/$name ($(du -h "$DEST/$name" | cut -f1))${paused:+, saving was paused}"

# Keep the newest KEEP archives.
ls -1t "$DEST"/world-*.tgz 2>/dev/null | tail -n +"$((KEEP + 1))" | while read -r old; do
  rm -f "$old"
  say "Removed old backup $old"
done
