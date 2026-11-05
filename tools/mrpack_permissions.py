#!/usr/bin/env python3
"""Distribution-permission check for the jars bundled in the .mrpack (overrides/mods/).

Side-effect free, so both tools can import it:
- build_mrpack.py checks the jars of local-mods/ under the names they ship as (pack_jar_name of
  patch_mods.py) before it builds anything,
- publish_modrinth.py checks every jar inside the .mrpack it is about to upload, so an old or
  hand-made file at the public name is caught too.

A jar passes only when modrinth-permissions/<jar>.md exists and has exactly one status line
that starts with the word OK ("OK", "OK (author allows)") and does not mention BLOCKER or VERIFY. A
status line starts with `**Status:**` or with a plain `Status:`, since the files use both.

A jar whose file points its source at tree/master/ports/<dir> of this repository (our own builds,
whose licenses demand public source) passes only when origin/master already holds that directory
and the same build of the jar in local-mods/. Until the branch with the source is merged and pushed,
those links lead nowhere. The check reads the local origin/master, so fetch before a build.
"""
import hashlib
import os
import re
import subprocess

STATUS = re.compile(r"(?:\*\*Status:\*\*|Status:)(.*)")
_OK = re.compile(r"OK(\s|\(|$)")
_NOT_OK = re.compile(r"BLOCKER|VERIFY")
SOURCE_URL = re.compile(r"https://github\.com/skytekx/skytekx3-1201-server/tree/master/(ports/[A-Za-z0-9._-]+)")
MASTER = "origin/master"


def _git(repo, *args):
    """Stripped stdout of a git command, or None when it fails."""
    r = subprocess.run(["git", "-C", repo, *args], capture_output=True)
    return r.stdout.decode("utf-8").strip() if r.returncode == 0 else None


def _blob_id(data):
    """The git blob id of `data`, as git hash-object gives it."""
    return hashlib.sha1(b"blob %d\0" % len(data) + data).hexdigest()


def source_problem(repo, jar, text, data=None):
    """None unless the permission file `text` of `jar` names source in tree/master/ports/ that
    origin/master does not hold yet. `data` are the bytes that ship, by default HEAD:local-mods/<jar>."""
    dirs = sorted(set(SOURCE_URL.findall(text)))
    if not dirs:
        return None
    if _git(repo, "rev-parse", "--verify", "--quiet", MASTER + "^{commit}") is None:
        return "its source points at master, but %s is unknown here (fetch it first)" % MASTER
    for d in dirs:
        if not _git(repo, "ls-tree", MASTER, "--", d):
            return "its source points at %s on master, which %s does not hold yet (merge and push first)" % (d, MASTER)
    listed = _git(repo, "ls-tree", MASTER, "--", "local-mods/" + jar)
    if not listed:
        return "local-mods/%s is not on %s yet, so the source there is not this build" % (jar, MASTER)
    shipped = _blob_id(data) if data is not None else _git(repo, "rev-parse", "HEAD:local-mods/" + jar)
    if shipped != listed.split()[2]:
        return "the build that ships is not the local-mods/%s on %s, whose source the links name" % (jar, MASTER)
    return None


def jar_problem(repo, jar, data=None):
    """None when `jar` (a file name) may be bundled, else the reason it may not."""
    pf = os.path.join(repo, "modrinth-permissions", jar + ".md")
    if not os.path.isfile(pf):
        return "no modrinth-permissions/%s.md" % jar
    with open(pf, encoding="utf-8") as fh:
        text = fh.read()
    statuses = [m.group(1).strip() for m in map(STATUS.match, text.splitlines()) if m]
    if not statuses:
        return "no Status line"
    if len(statuses) > 1:
        return "%d Status lines: %s" % (len(statuses), " | ".join(statuses))
    status = statuses[0]
    if not _OK.match(status) or _NOT_OK.search(status):
        return "Status: %s" % status
    return source_problem(repo, jar, text, data)


def check(repo, jars, data=None):
    """[(jar, reason)] for every jar name in `jars` that may not be bundled. `data` maps a jar name
    to the bytes that ship, for the source check (default: the jar in HEAD:local-mods/)."""
    bad = []
    for jar in sorted(set(jars)):
        why = jar_problem(repo, jar, (data or {}).get(jar))
        if why:
            bad.append((jar, why))
    return bad


def local_mod_jars(repo):
    """File names of the jars in local-mods/ (what build_mrpack.py bundles)."""
    lm = os.path.join(repo, "local-mods")
    if not os.path.isdir(lm):
        return []
    return sorted(fn for fn in os.listdir(lm) if fn.endswith(".jar"))


def mrpack_jars(zf):
    """File names of every jar inside an open .mrpack ZipFile, wherever it sits."""
    return sorted({os.path.basename(n) for n in zf.namelist() if n.lower().endswith(".jar")})


def mrpack_jar_data(zf, repo):
    """{file name: bytes} of the jars in overrides/mods/ of an open .mrpack ZipFile whose permission
    file names source on master, for the source check. Other jars are not read."""
    data = {}
    for n in zf.namelist():
        jar = os.path.basename(n)
        pf = os.path.join(repo, "modrinth-permissions", jar + ".md")
        if n.lower().endswith(".jar") and os.path.dirname(n) == "overrides/mods" and os.path.isfile(pf):
            with open(pf, encoding="utf-8") as fh:
                if SOURCE_URL.search(fh.read()):
                    data[jar] = zf.read(n)
    return data
