#!/usr/bin/env python3
"""Publish SkyTekx3 modpack to Modrinth (creates draft project if needed, uploads .mrpack version).
Usage: publish_modrinth.py <PAT> [--go]   (without --go = dry run / inspect only)
"""
import json, os, sys, mimetypes, urllib.request, urllib.error, uuid

PAT = sys.argv[1]
GO = "--go" in sys.argv
REPO = "/home/snekmin/git/skytekx3-1201-server"
VERSION = "3.0.4"
MRPACK = REPO + f"/SkyTekx3-{VERSION}.mrpack"
API = "https://api.modrinth.com/v2"
UA = "skytekx3-builder/1.0 (skytekx@erebos.xyz)"
SLUG = "skytekx3"

BODY = open(REPO+"/docs/MODPACK_DESCRIPTION.md").read() if os.path.exists(REPO+"/docs/MODPACK_DESCRIPTION.md") else "SkyTekx3 — gen-3 rebuild on 1.20.1."

def req(method, path, headers=None, data=None):
    h = {"Authorization": PAT, "User-Agent": UA}
    if headers: h.update(headers)
    r = urllib.request.Request(API+path, data=data, headers=h, method=method)
    return urllib.request.urlopen(r, timeout=60)

def get_json(path):
    return json.load(req("GET", path))

def multipart(fields, file_field=None, file_path=None):
    """Build multipart/form-data body. fields: dict of name->str. file_field/file_path optional."""
    boundary = "----skytekx3"+uuid.uuid4().hex
    nl = b"\r\n"
    body = b""
    for k, v in fields.items():
        body += b"--"+boundary.encode()+nl
        body += f'Content-Disposition: form-data; name="{k}"'.encode()+nl+nl
        body += v.encode()+nl
    if file_field and file_path:
        fn = os.path.basename(file_path)
        ctype = "application/x-modrinth-modpack+zip"
        body += b"--"+boundary.encode()+nl
        body += f'Content-Disposition: form-data; name="{file_field}"; filename="{fn}"'.encode()+nl
        body += f"Content-Type: {ctype}".encode()+nl+nl
        body += open(file_path, "rb").read()+nl
    body += b"--"+boundary.encode()+b"--"+nl
    return body, "multipart/form-data; boundary="+boundary

# 1. who am I + existing project?
me = get_json("/user")
print("authed:", me["username"], me["id"])
try:
    p = get_json(f"/project/{SLUG}")
    print("project exists:", p["id"], p["slug"], "status=", p.get("status"))
    project_id = p["id"]
except urllib.error.HTTPError as e:
    project_id = None
    print("project not found (will create):", e.code)

if not GO:
    print("\n[dry run] .mrpack:", MRPACK, os.path.getsize(MRPACK) if os.path.exists(MRPACK) else "MISSING")
    print("[dry run] re-run with --go to create/upload.")
    sys.exit(0)

# 2. create project if needed
if not project_id:
    data = {
        "slug": SLUG,
        "title": "SkyTekx3",
        "description": "Hexxit + Tekkit + SkyFactory-style tech/magic/adventure kitchen-sink on 1.20.1 (gen-3 SkyTekx).",
        "body": BODY,
        "categories": ["adventure", "magic", "technology"],
        "client_side": "required",
        "server_side": "required",
        "project_type": "modpack",
        "is_draft": True,
        "initial_versions": [],
        "license_id": "LicenseRef-All-Rights-Reserved",
    }
    body, ctype = multipart({"data": json.dumps(data)})
    try:
        resp = req("POST", "/project", headers={"Content-Type": ctype}, data=body)
        p = json.load(resp); project_id = p["id"]
        print("CREATED project:", project_id, p["slug"])
    except urllib.error.HTTPError as e:
        print("CREATE FAILED", e.code, e.read().decode()[:600]); sys.exit(1)

# 3. upload version
vdata = {
    "name": f"SkyTekx3 {VERSION}",
    "version_number": VERSION,
    "project_id": project_id,
    "changelog": "Full **Nyx** port (night/moon mechanics, meteors, lunar water) shipped server+client (env required/required). Skylands worldgen overhaul: fixed the island floor to y0 and added many new structures (villages, swamp huts, Dungeons Arise, Cataclysm, Botania mystical flowers). Added an Aether-style void-fall (drop off the Skylands islands and free-fall back into the overworld). Full all-dimension reset/regen so the new structure-sets, biome-tags and Nyx worldgen appear everywhere. Retuned AlmostUnified priorities.",
    "dependencies": [],
    "game_versions": ["1.20.1"],
    "version_type": "beta",
    "loaders": ["forge"],
    "featured": True,
    "status": "listed",
    "file_parts": ["file"],
}
body, ctype = multipart({"data": json.dumps(vdata)}, file_field="file", file_path=MRPACK)
try:
    resp = req("POST", "/version", headers={"Content-Type": ctype}, data=body)
    v = json.load(resp)
    print("UPLOADED version:", v["id"], v["version_number"])
    print("project page: https://modrinth.com/modpack/"+SLUG)
except urllib.error.HTTPError as e:
    print("VERSION UPLOAD FAILED", e.code, e.read().decode()[:800]); sys.exit(1)
