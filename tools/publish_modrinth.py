#!/usr/bin/env python3
"""Publish SkyTekx3 modpack to Modrinth (creates draft project if needed, uploads .mrpack version).
Usage: MODRINTH_TOKEN=<PAT> publish_modrinth.py [--go | --texts]   (neither = dry run / inspect only)
  --go     create the project if it is missing, then upload the version
  --texts  replace the summary and the body of the existing project with docs/MODPACK_SUMMARY.txt
           and docs/MODPACK_DESCRIPTION.md, then stop (a new project gets them at creation anyway)
The token can also come as the first argument, but then other local users can read it in the process list.

Before any network call it checks the .mrpack it would upload: the file exists, its index has this
VERSION, and every jar inside it has a modrinth-permissions/<jar>.md with Status OK
(tools/mrpack_permissions.py). The version changelog is read from docs/changelog/<VERSION>.md, which
must exist and not be empty, so a VERSION bump cannot reuse an old changelog. A failed check stops the
dry run too; there is no override. The dry run prints the changelog it would send.
"""
import hashlib, json, os, sys, mimetypes, urllib.request, urllib.error, uuid, zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mrpack_permissions

args = [a for a in sys.argv[1:] if not a.startswith("--")]
PAT = os.environ.get("MODRINTH_TOKEN") or (args[0] if args else None)
if not PAT:
    sys.exit("Set MODRINTH_TOKEN to a Modrinth personal access token")
GO = "--go" in sys.argv
TEXTS = "--texts" in sys.argv
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
VERSION = "3.1.2"
MRPACK = REPO + f"/SkyTekx3-{VERSION}.mrpack"
CHANGELOG = REPO + f"/docs/changelog/{VERSION}.md"
API = "https://api.modrinth.com/v2"
UA = "skytekx3-builder/1.0 (skytekx@erebos.xyz)"
SLUG = "skytekx3"

BODY = open(REPO+"/docs/MODPACK_DESCRIPTION.md").read()
SUMMARY = open(REPO+"/docs/MODPACK_SUMMARY.txt").read().strip()   # Modrinth allows 256 characters
assert len(SUMMARY) <= 256 and "<!--" not in BODY, "summary too long or a comment left in the body"

def preflight():
    """Check the .mrpack itself (not local-mods/), so an old or hand-made file is caught too."""
    if not os.path.exists(MRPACK):
        sys.exit(f"{MRPACK} is missing; build it with tools/build_mrpack.py first")
    try:
        with zipfile.ZipFile(MRPACK) as z:
            idx = json.loads(z.read("modrinth.index.json"))
            jars = mrpack_permissions.mrpack_jars(z)
    except (zipfile.BadZipFile, KeyError, ValueError) as e:
        sys.exit(f"{MRPACK} is not a readable .mrpack: {e}")
    problems = []
    if idx.get("versionId") != VERSION:
        problems.append(f"its modrinth.index.json has versionId {idx.get('versionId')!r}, not {VERSION!r}")
    for jar, why in mrpack_permissions.check(REPO, jars):
        problems.append(f"bundled {jar} -- {why}")
    if problems:
        print(f"refusing to upload {MRPACK}:")
        for p in problems:
            print("  " + p)
        sys.exit("resolve the modrinth-permissions/ blockers (see its README) and rebuild")
    print(f"preflight OK: {MRPACK} ({os.path.getsize(MRPACK)//1024} KiB), versionId {VERSION}, "
          f"{len(idx.get('files', []))} index files, {len(jars)} bundled jars, all with permission")

def read_changelog():
    """The player changelog for this VERSION; refuses a missing or empty file."""
    text = open(CHANGELOG, encoding="utf-8").read().strip() if os.path.exists(CHANGELOG) else ""
    if not text:
        sys.exit(f"{CHANGELOG} is missing or empty; write the {VERSION} changelog before uploading")
    return text

preflight()
CHANGELOG_TEXT = read_changelog()

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
    print("\n[dry run] .mrpack:", MRPACK, os.path.getsize(MRPACK))
    print(f"[dry run] changelog from {CHANGELOG}:\n" + CHANGELOG_TEXT + "\n")
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
    "changelog": CHANGELOG_TEXT,
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
