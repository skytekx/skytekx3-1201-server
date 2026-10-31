#!/usr/bin/env bash
# Nightly world backup for the SkyTekx3 server on macOS (launchd job com.skytekx.backup, 05:00).
# Flushes the world over RCON with saving paused, archives world/ and keeps the newest KEEP archives.
# Skips the run unless the disk has room for an archive as large as the newest one plus MIN_FREE_GB,
# so the backup leaves at least that much free for the server. A failed run deletes its partial archive.
set -euo pipefail
DIR="$(cd "$(dirname "$0")/.." && pwd)"
DEST="${BACKUP_DIR:-$HOME/skytekx3-backups}"
KEEP="${KEEP:-3}"
MIN_FREE_GB="${MIN_FREE_GB:-4}"

prop() { sed -n "s/^$1=//p" "$DIR/server.properties" | tr -d '\r'; }
rcon() { /usr/bin/python3 "$DIR/tools/rcon.py" 127.0.0.1 "$(prop rcon.port)" "$(prop rcon.password)" "$@"; }
say() { echo "$(date '+%F %T') $*"; }

part=""     # The archive while tar writes it
paused=""   # Set from the moment save-off may have reached the server
resume_saving() {
  local i
  for i in 1 2 3 4 5 6; do
    [ "$i" = 1 ] || sleep 10
    if rcon save-on >/dev/null 2>&1; then
      paused=""
      return 0
    fi
  done
  say "ERROR: save-on failed 6 times, autosave stays off until a restart or: macos/skytekx3ctl rcon save-on"
  return 1
}
# Runs on every exit, also when set -e ends the run: removes the partial archive and turns saving back
# on. A failed save-on fails the run.
cleanup() {
  local status=$?
  if [ -n "$part" ]; then
    rm -f "$part"
    say "FAILED: removed the partial archive $part"
  fi
  if [ -n "$paused" ]; then
    resume_saving || status=1
  fi
  exit "$status"
}
trap cleanup EXIT

mkdir -p "$DEST"
# Partial archives of runs that died before their cleanup (power cut, SIGKILL). tar writes to the
# .part file of a running backup all the time, so one untouched for 30 minutes is a leftover.
find "$DEST" -maxdepth 1 -name 'world-*.tgz.part' -mmin +30 | while read -r old; do
  rm -f "$old"
  say "Removed the partial archive $old"
done

# Room for an archive as large as the newest one (before the first one, as large as world/ itself),
# plus MIN_FREE_GB left over.
last=$(ls -1t "$DEST"/world-*.tgz 2>/dev/null | head -n 1 || true)
if [ -n "$last" ]; then
  size_kb=$(du -k "$last" | cut -f1)
else
  size_kb=$(du -sk "$DIR/world" | cut -f1)
fi
need_kb=$((size_kb + MIN_FREE_GB * 1048576))
free_kb=$(df -Pk "$DEST" | awk 'NR==2 {print $4}')
if [ "$free_kb" -lt "$need_kb" ]; then
  say "SKIP: only $((free_kb / 1024)) MB free, need $((need_kb / 1024)) MB (an archive of $((size_kb / 1024)) MB plus ${MIN_FREE_GB} GB)"
  exit 1
fi

if rcon list >/dev/null 2>&1; then
  paused=1   # Before save-off: if the call fails after the server ran it, cleanup still turns saving on
  rcon save-off >/dev/null
  rcon "save-all flush" >/dev/null
fi

name="world-$(date +%F-%H%M).tgz"
part="$DEST/$name.part"
tar -czf "$part" -C "$DIR" --exclude session.lock world
mv "$part" "$DEST/$name"
part=""
say "OK: $DEST/$name ($(du -h "$DEST/$name" | cut -f1))${paused:+, saving was paused}"

# Keep the newest KEEP archives.
ls -1t "$DEST"/world-*.tgz 2>/dev/null | tail -n +"$((KEEP + 1))" | while read -r old; do
  rm -f "$old"
  say "Removed old backup $old"
done
