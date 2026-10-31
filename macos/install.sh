#!/usr/bin/env bash
# Install the SkyTekx3 launchd jobs on macOS: the server, the RCON guard and the nightly backup.
# Run once from the server directory with sudo:   sudo macos/install.sh
# Running it again reloads all three jobs, which restarts a running server and starts a stopped one.
set -euo pipefail
[ "$(id -u)" = 0 ] || { echo "Run with sudo: sudo $0"; exit 1; }

DIR="$(cd "$(dirname "$0")/.." && pwd)"
RUN_USER="${SUDO_USER:-$(stat -f %Su "$DIR")}"
RUN_HOME="$(dscl . -read "/Users/$RUN_USER" NFSHomeDirectory | awk '{print $2}')"
[ -x "$(/usr/libexec/java_home -F -v 17 2>/dev/null)/bin/java" ] || { echo "Java 17 is missing (brew install --cask temurin@17)"; exit 1; }
# The clean stop (ServerMac.sh) and the backup reach RCON through /usr/bin/python3, which only runs
# once the Command Line Tools are installed. Without it every stop ends in the SIGTERM fallback.
/usr/bin/python3 -c pass >/dev/null 2>&1 || { echo "python3 is missing (xcode-select --install, it brings /usr/bin/python3)"; exit 1; }
[ -f "$DIR/server.properties" ] || { echo "server.properties is missing (copy server.properties.example)"; exit 1; }
mkdir -p "$DIR/logs" && chown "$RUN_USER":staff "$DIR/logs"

# A server must never sleep, and it must boot by itself after a power cut.
pmset -a sleep 0 disksleep 0 powernap 0 autorestart 1 womp 1

install -o root -g wheel -m 644 "$DIR/macos/pf-rcon-guard.conf" /etc/pf.anchors/com.skytekx

for job in com.skytekx.rcon-guard com.skytekx.server com.skytekx.backup; do
  dst=/Library/LaunchDaemons/$job.plist
  sed -e "s#__SERVER_DIR__#$DIR#g" -e "s#__USER__#$RUN_USER#g" -e "s#__HOME__#$RUN_HOME#g" \
    "$DIR/macos/$job.plist" > "$dst"
  chown root:wheel "$dst"
  chmod 644 "$dst"
  plutil -lint "$dst" >/dev/null
  if launchctl print "system/$job" >/dev/null 2>&1; then
    # bootout only starts the stop and returns early (the server's RCON stop takes a few seconds).
    # Wait until launchd has removed the job, or the bootstrap below fails with error 5.
    launchctl bootout "system/$job" 2>/dev/null || true
    for _ in $(seq 1 100); do
      launchctl print "system/$job" >/dev/null 2>&1 || break
      sleep 2
    done
  fi
  # skytekx3ctl stop disables the server job so that it stays down across reboots. A disabled job
  # cannot be loaded, so enable each job first.
  launchctl enable "system/$job"
  launchctl bootstrap system "$dst"
  echo "Loaded $job"
done
echo "Done. Watch the boot with: tail -f $DIR/logs/latest.log"
