#!/usr/bin/env python3
"""Publish SkyTekx3 modpack to Modrinth (creates draft project if needed, uploads .mrpack version).
Usage: MODRINTH_TOKEN=<PAT> publish_modrinth.py [--go | --texts]   (neither = dry run / inspect only)
  --go     create the project if it is missing, then upload the version
  --texts  replace the summary and the body of the existing project with docs/MODPACK_SUMMARY.txt
           and docs/MODPACK_DESCRIPTION.md, then stop (a new project gets them at creation anyway)
The token can also come as the first argument, but then other local users can read it in the process list.
"""
import hashlib, json, os, sys, mimetypes, urllib.request, urllib.error, uuid

args = [a for a in sys.argv[1:] if not a.startswith("--")]
PAT = os.environ.get("MODRINTH_TOKEN") or (args[0] if args else None)
if not PAT:
    sys.exit("Set MODRINTH_TOKEN to a Modrinth personal access token")
GO = "--go" in sys.argv
TEXTS = "--texts" in sys.argv
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
VERSION = "3.0.8"
MRPACK = REPO + f"/SkyTekx3-{VERSION}.mrpack"
API = "https://api.modrinth.com/v2"
UA = "skytekx3-builder/1.0 (skytekx@erebos.xyz)"
SLUG = "skytekx3"

BODY = open(REPO+"/docs/MODPACK_DESCRIPTION.md").read()
SUMMARY = open(REPO+"/docs/MODPACK_SUMMARY.txt").read().strip()   # Modrinth allows 256 characters
assert len(SUMMARY) <= 256 and "<!--" not in BODY, "summary too long or a comment left in the body"

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

if TEXTS:
    if not project_id: sys.exit("no project to update, create it with --go")
    resp = req("PATCH", f"/project/{project_id}", headers={"Content-Type": "application/json"},
               data=json.dumps({"description": SUMMARY, "body": BODY}).encode())
    p = get_json(f"/project/{project_id}")
    print("PATCH", resp.status, "| summary matches", p["description"] == SUMMARY, "| body matches", p["body"] == BODY)
    sys.exit(0)

if not GO:
    print("\n[dry run] .mrpack:", MRPACK, os.path.getsize(MRPACK) if os.path.exists(MRPACK) else "MISSING")
    print("[dry run] re-run with --go to create/upload.")
    sys.exit(0)

# 2. create project if needed
if not project_id:
    data = {
        "slug": SLUG,
        "title": "SkyTekx3",
        "description": SUMMARY,
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
    "changelog": "Every mod in the pack is required now. Version 3.0.7 marked 36 mods as optional, among them libraries like Cloth Config, JEI, Placebo and Balm, and an instance without them did not start. Sophisticated Backpacks and Sophisticated Core are gone, a port of the Backpack mod takes their place and its sources ship in the licenses folder. Falling Meteors Reimagined, Elementary Staffs and BattleTowers are new, downloaded from GitHub releases.",
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
    local = hashlib.sha512(open(MRPACK, "rb").read()).hexdigest()
    for f in v["files"]:
        print("file:", f["filename"], f["size"], "sha512 matches local build:", f["hashes"]["sha512"] == local)
    print("project page: https://modrinth.com/modpack/"+SLUG)
except urllib.error.HTTPError as e:
    print("VERSION UPLOAD FAILED", e.code, e.read().decode()[:800]); sys.exit(1)
