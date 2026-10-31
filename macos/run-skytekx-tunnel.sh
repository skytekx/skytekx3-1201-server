#!/bin/sh
# Runs the connector of the Cloudflare Tunnel skytekx3-mac-mini, which carries hex-tunnel.skytekx.com
# to the Minecraft server on localhost:25565. Install it as ~/.cloudflared/run-skytekx-tunnel.sh (mode
# 700) next to the tunnel token in ~/.cloudflared/skytekx.token (mode 600). The token goes to cloudflared
# through the environment, so it never shows in the process list. The LaunchDaemon
# macos/com.skytekx.cloudflared.plist starts this script at boot and again whenever it exits.
TOKEN_FILE="$(dirname "$0")/skytekx.token"
[ -r "$TOKEN_FILE" ] || { echo "tunnel token missing: $TOKEN_FILE" >&2; exit 1; }
TUNNEL_TOKEN="$(cat "$TOKEN_FILE")"
export TUNNEL_TOKEN
exec /opt/homebrew/bin/cloudflared --no-autoupdate tunnel run
