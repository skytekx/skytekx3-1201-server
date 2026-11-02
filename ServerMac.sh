#!/usr/bin/env bash
# SkyTekx3 (MC 1.20.1 / Forge / Arclight) launcher for macOS on Apple Silicon.
# launchd starts it at boot through macos/com.skytekx.server.plist (see macos/install.sh).
# It also runs by hand in a terminal for a foreground test. Stop that one by typing "stop", not Ctrl+C.
set -euo pipefail
cd "$(dirname "$0")"

# Java 17 is the only version with a successful boot on record (okf/server/arclight.md).
# -F makes java_home fail when Java 17 is missing instead of falling back to another installed Java.
JAVA="${JAVA:-$(/usr/libexec/java_home -F -v 17)/bin/java}"
JAR="${JAR:-arclight.jar}"   # Symlink to the hardened arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar
# Heap for the 16 GB M4 Mac mini. macOS with nobody logged in uses about 3 GB, and the JVM needs
# roughly 2 to 2.5 GB on top of the heap for metaspace, code cache and threads of ~270 mods.
MEM="${MEM:-9G}"

# A macOS shell starts with a soft limit of 256 open files. Mod jars and region files need more.
# Under launchd the plist sets 65536, this covers a manual start. Only a lower soft limit is raised:
# a plain "ulimit -n" in bash sets the hard limit as well and would cut the plist's 65536 down.
soft_files=$(ulimit -Sn)
if [ "$soft_files" != unlimited ] && [ "$soft_files" -lt 10240 ]; then
  ulimit -Sn 10240 2>/dev/null || true
fi

# Aikar's G1 flags (the same set as ServerLinux.sh and the systemd unit), plus three macOS additions:
# ReservedCodeCacheSize keeps the JIT from running out of code cache with this many mods,
# java.awt.headless stops any mod from reaching for the window server (there is none under launchd),
# and the file limit above.
JVM=(
  -Xms"$MEM" -Xmx"$MEM"
  -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200
  -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch
  -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M
  -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4
  -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90
  -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32
  -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1
  -XX:ReservedCodeCacheSize=512M
  -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true
  -Djava.awt.headless=true
  -Dfile.encoding=UTF-8
  -jar "$JAR" nogui
)

# In a terminal the console stays attached to the JVM.
if [ -t 0 ]; then
  exec "$JAVA" "${JVM[@]}"
fi

# Without a terminal (launchd) no signal may reach the JVM. SIGTERM sends Arclight down its shutdown
# thread, which skips the stop handlers and saved badly every time it happened
# (okf/server/logs-and-crashes.md, stop path). launchd sends SIGTERM on "launchctl bootout" and on
# every shutdown or reboot. So the JVM runs as a child, and SIGTERM or SIGINT to this script becomes
# the RCON "stop" of a clean stop.
prop() { sed -n "s/^$1=//p" server.properties | tr -d '\r'; }

pid=""
stop_requested=""
stop_server() {
  # A signal before "pid=$!" below only records the stop. The line after the JVM start then makes it.
  if [ -z "$pid" ]; then
    stop_requested=1
    return 0
  fi
  trap '' TERM INT
  local sent=0 i
  for i in $(seq 1 45); do
    kill -0 "$pid" 2>/dev/null || return 0
    if /usr/bin/python3 tools/rcon.py 127.0.0.1 "$(prop rcon.port)" "$(prop rcon.password)" stop >/dev/null 2>&1; then
      sent=1
      break
    fi
    sleep 2   # RCON is not up yet, the server is still booting
  done
  # RCON never answered within 90 s, so fall back to the signal.
  if [ "$sent" = 0 ]; then
    kill -TERM "$pid" 2>/dev/null || true
  fi
}
# Set before the JVM starts, so no signal can end this script and leave the JVM running without it.
trap stop_server TERM INT

"$JAVA" "${JVM[@]}" </dev/null &
pid=$!
[ -z "$stop_requested" ] || stop_server

set +e
wait "$pid"
status=$?
# A trapped signal ends "wait" early. Keep waiting until the JVM has really exited.
while kill -0 "$pid" 2>/dev/null; do
  wait "$pid"
  status=$?
done
exit "$status"
