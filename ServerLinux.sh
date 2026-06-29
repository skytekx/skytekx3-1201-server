#!/usr/bin/env bash
# SkyTekx3 (MC 1.20.1 / Forge / Arclight) — local/dev launcher.
# Production uses systemd (systemd/skytekx3.service). Java 21.
set -euo pipefail
cd "$(dirname "$0")"

# Arclight 1.20.1 requires Java 17 (it crashes on 21 — IInventoryBridge NoClassDefFound).
JAVA="${JAVA:-/usr/lib/jvm/java-17-openjdk/bin/java}"
JAR="${JAR:-arclight.jar}"          # symlink -> arclight-forge-1.20.1-<ver>.jar
MEM="${MEM:-11G}"                   # tuned for a 16GB host (e.g. M4 Mac mini); leaves ~5G for the OS

# Aikar's flags (G1GC, tuned for Minecraft servers)
exec "$JAVA" \
  -Xms"$MEM" -Xmx"$MEM" \
  -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 \
  -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch \
  -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M \
  -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 \
  -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 \
  -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 \
  -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 \
  -Dusing.aikars.flags=https://mcflags.emc.gs -Daikars.new.flags=true \
  -Dfile.encoding=UTF-8 \
  -jar "$JAR" nogui
