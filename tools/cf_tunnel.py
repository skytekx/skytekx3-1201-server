#!/usr/bin/env python3
"""Create or update the Cloudflare Tunnel that carries the game from the Mac mini to the players.

The Mac mini sits behind carrier grade NAT, so no port can be forwarded to it. The connector
cloudflared on the Mac mini dials out to Cloudflare instead, and players join through Modflared, a
client mod that reads a TXT record and opens the tunnel route on their side.

  CLOUDFLARE_ACCOUNT_EMAIL=... CLOUDFLARE_Global_API_Key=... python3 tools/cf_tunnel.py TOKEN_FILE
  or CLOUDFLARE_API_TOKEN=... (rights: Account, Cloudflare Tunnel, Edit and Zone, DNS, Edit)

Every step is idempotent:
  1. find or create the tunnel skytekx3-mac-mini, configured on Cloudflare's side
  2. write its connector token to TOKEN_FILE with mode 600, without printing it
  3. route hex-tunnel.skytekx.com to tcp://localhost:25565 and everything else to a 404
  4. point hex-tunnel.skytekx.com at the tunnel with a proxied CNAME
  5. set the TXT record of each player name to cloudflared-route=hex-tunnel.skytekx.com for Modflared
  6. give each player name the address 192.0.2.1, which answers nothing. Minecraft resolves the name
     before Modflared sees it, and without an address it stops at "Unknown host" (2026-10-31 22:08)
The player names are hex.skytekx.com, the one in the pack's server list, and play.skytekx.com, a
second name for the same server since 2026-10-31 22:15.
Then copy TOKEN_FILE to ~/.cloudflared/skytekx.token on the Mac mini (mode 600) and delete it here.
"""
import json, os, sys, urllib.error, urllib.request

ZONE, TUNNEL = "skytekx.com", "skytekx3-mac-mini"
ROUTE, PLAYERS = "hex-tunnel.skytekx.com", ("hex.skytekx.com", "play.skytekx.com")
SERVICE = "tcp://localhost:25565"
API = "https://api.cloudflare.com/client/v4"

if len(sys.argv) != 2:
    sys.exit(__doc__)
if os.environ.get("CLOUDFLARE_API_TOKEN"):
    AUTH = {"Authorization": "Bearer " + os.environ["CLOUDFLARE_API_TOKEN"]}
else:
    AUTH = {"X-Auth-Email": os.environ["CLOUDFLARE_ACCOUNT_EMAIL"],
            "X-Auth-Key": os.environ["CLOUDFLARE_Global_API_Key"]}


def call(method, path, body=None):
    req = urllib.request.Request(API + path, method=method,
                                 headers={**AUTH, "Content-Type": "application/json"},
                                 data=json.dumps(body).encode() if body is not None else None)
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            out = json.load(resp)
    except urllib.error.HTTPError as e:
        out = json.loads(e.read().decode() or "{}")
    if not out.get("success"):
        sys.exit(f"{method} {path.split('?')[0]} failed: {out.get('errors')}")
    return out["result"]


def ensure(zid, rtype, name, content, proxied=None, comment=""):
    body = {"type": rtype, "name": name, "content": content, "ttl": 1, "comment": comment}
    if proxied is not None:
        body["proxied"] = proxied
    cur = call("GET", f"/zones/{zid}/dns_records?type={rtype}&name={name}")
    if cur:
        call("PUT", f"/zones/{zid}/dns_records/{cur[0]['id']}", body)
    else:
        call("POST", f"/zones/{zid}/dns_records", body)
    print(rtype, name, "updated" if cur else "created")


zone = call("GET", f"/zones?name={ZONE}")[0]
zid, acc = zone["id"], zone["account"]["id"]
found = call("GET", f"/accounts/{acc}/cfd_tunnel?name={TUNNEL}&is_deleted=false")
if found:
    tid = found[0]["id"]
else:
    tid = call("POST", f"/accounts/{acc}/cfd_tunnel", {"name": TUNNEL, "config_src": "cloudflare"})["id"]
print("tunnel", TUNNEL, "found" if found else "created", tid[:8])

fd = os.open(sys.argv[1], os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
os.write(fd, call("GET", f"/accounts/{acc}/cfd_tunnel/{tid}/token").encode())
os.close(fd)
print("token written to", sys.argv[1])

call("PUT", f"/accounts/{acc}/cfd_tunnel/{tid}/configurations",
     {"config": {"ingress": [{"hostname": ROUTE, "service": SERVICE}, {"service": "http_status:404"}]}})
print("ingress", ROUTE, "to", SERVICE)

ensure(zid, "CNAME", ROUTE, f"{tid}.cfargotunnel.com", True,
       "SkyTekx3 Minecraft through the Cloudflare Tunnel " + TUNNEL)
for player in PLAYERS:
    ensure(zid, "TXT", player, f"cloudflared-route={ROUTE}", None,
           f"Modflared: join {player} through the tunnel route")
    ensure(zid, "A", player, "192.0.2.1", False,   # A documentation address (RFC 5737), never routed
           "Lets Minecraft resolve the name, then Modflared follows the TXT into the tunnel. Answers nothing.")
