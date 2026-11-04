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
"""
import os
import re

STATUS = re.compile(r"(?:\*\*Status:\*\*|Status:)(.*)")
_OK = re.compile(r"OK(\s|\(|$)")
_NOT_OK = re.compile(r"BLOCKER|VERIFY")


def jar_problem(repo, jar):
    """None when `jar` (a file name) may be bundled, else the reason it may not."""
    pf = os.path.join(repo, "modrinth-permissions", jar + ".md")
    if not os.path.isfile(pf):
        return "no modrinth-permissions/%s.md" % jar
    with open(pf, encoding="utf-8") as fh:
        statuses = [m.group(1).strip() for m in map(STATUS.match, fh) if m]
    if not statuses:
        return "no Status line"
    if len(statuses) > 1:
        return "%d Status lines: %s" % (len(statuses), " | ".join(statuses))
    status = statuses[0]
    if not _OK.match(status) or _NOT_OK.search(status):
        return "Status: %s" % status
    return None


def check(repo, jars):
    """[(jar, reason)] for every jar name in `jars` that may not be bundled."""
    bad = []
    for jar in sorted(set(jars)):
        why = jar_problem(repo, jar)
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
