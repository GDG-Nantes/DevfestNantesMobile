#!/usr/bin/env bash
# Kover report summariser for phase 05 (D-18). Reads the merged JaCoCo-format XML report.
#   kover-summary.sh summary <report.xml>
#   kover-summary.sh compare <baseline.xml> <final.xml>
set -euo pipefail

usage() {
  echo "usage: $0 summary <report.xml> | compare <baseline.xml> <final.xml>" >&2
  exit 2
}

[ $# -ge 1 ] || usage
MODE="$1"
shift
case "$MODE" in
  summary) [ $# -eq 1 ] || usage ;;
  compare) [ $# -eq 2 ] || usage ;;
  *) usage ;;
esac

exec python3 -I - "$MODE" "$@" <<'PY'
import sys
import xml.etree.ElementTree as ET

PREFIXES = [
    ("com/gdgnantes/devfest/androidapp", ":androidApp"),
    ("com/gdgnantes/devfest/shared", ":shared"),
    ("com/gdgnantes/devfest/core/model", ":core:model"),
    ("com/gdgnantes/devfest/core/network", ":core:network"),
    ("com/gdgnantes/devfest/core/analytics", ":core:analytics"),
    ("com/gdgnantes/devfest/core/data", ":core:data"),
    ("com/gdgnantes/devfest/core/ui", ":core:ui"),
    ("com/gdgnantes/devfest/feature/venue", ":feature:venue"),
    ("com/gdgnantes/devfest/feature/about", ":feature:about"),
    ("com/gdgnantes/devfest/feature/settings", ":feature:settings"),
    ("com/gdgnantes/devfest/feature/speakers", ":feature:speakers"),
    ("com/gdgnantes/devfest/feature/agenda", ":feature:agenda"),
    ("com/gdgnantes/devfest/feature/sessiondetail", ":feature:session-detail"),
]


def missing(path):
    print("KOVER-REPORT-MISSING " + path)
    sys.exit(1)


def counter(el, kind):
    for c in el.findall("counter"):
        if c.get("type") == kind:
            m = int(c.get("missed"))
            v = int(c.get("covered"))
            return v, m + v
    return None


def pct(cov, tot):
    return 0.0 if tot == 0 else 100.0 * cov / tot


def fmt(cov, tot):
    return "%d/%d %.2f%%" % (cov, tot, pct(cov, tot))


def load(path):
    try:
        root = ET.parse(path).getroot()
    except (OSError, ET.ParseError):
        missing(path)
    line = counter(root, "LINE")
    if line is None:
        missing(path)
    return root


def module_of(name):
    for prefix, path in PREFIXES:
        if name == prefix or name.startswith(prefix + "/"):
            return path
    return None


def per_module(root):
    mods = {path: [0, 0] for _, path in PREFIXES}
    unmapped = []
    for pkg in root.findall("package"):
        name = pkg.get("name", "")
        path = module_of(name)
        if path is None:
            unmapped.append(name)
            continue
        c = counter(pkg, "LINE")
        if c is not None:
            mods[path][0] += c[0]
            mods[path][1] += c[1]
    return mods, unmapped


mode = sys.argv[1]
if mode == "summary":
    path = sys.argv[2]
    root = load(path)
    for kind in ("LINE", "INSTRUCTION", "BRANCH"):
        c = counter(root, kind)
        if c is not None:
            print("TOTAL %s %s" % (kind, fmt(*c)))
    mods, unmapped = per_module(root)
    for p in sorted(mods):
        print("MODULE %s LINE %s" % (p, fmt(*mods[p])))
    for u in sorted(unmapped):
        print("UNMAPPED " + u)
    print("KOVER-SUMMARY-OK")
else:
    bpath, fpath = sys.argv[2], sys.argv[3]
    b = load(bpath)
    f = load(fpath)
    bl = counter(b, "LINE")
    fl = counter(f, "LINE")
    print("TOTAL LINE %.2f%% -> %.2f%%" % (pct(*bl), pct(*fl)))
    bm, _ = per_module(b)
    fm, _ = per_module(f)
    for p in sorted(fm):
        print("MODULE %s LINE %.2f%% -> %.2f%%" % (p, pct(*bm[p]), pct(*fm[p])))
    # exact integer cross-multiplication: after_cov/after_tot > before_cov/before_tot
    improved = fl[1] > 0 and bl[1] > 0 and fl[0] * bl[1] > bl[0] * fl[1]
    if improved:
        print("KOVER-IMPROVED")
    else:
        print("KOVER-NOT-IMPROVED")
        sys.exit(1)
PY
