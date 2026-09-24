#!/usr/bin/env bash
set -euo pipefail

# resources-gate.sh — proves Android resource moves during Phase 3 (D-17,
# ARCH-03) never change a string/color VALUE, never drop a resource, and
# never duplicate a resource name across modules.
#
# For values/strings.xml, values-fr/strings.xml, values/colors.xml and
# values-night/colors.xml, this builds the sorted multiset of single-line
# <string ...>...</string> / <color ...>...</color> elements from
# origin/main's androidApp/src/main/res/<path> and from the concatenation of
# every git-tracked */src/main/res/<path> in the current working tree, and
# diffs them. It also diffs the sorted multiset of file basenames under every
# drawable*/mipmap* qualifier directory (per qualifier) against origin/main's
# androidApp set. Multiset equality with origin proves: nothing lost, no
# value edited, no name duplicated across modules.
#
# Any diff -> exit 1 with RESOURCES-DRIFT (details on stderr).
# Success -> prints RESOURCES-OK.
#
# Reused unmodified by 03-06..03-08 (feature extraction plans).

REPO_ROOT="$(git rev-parse --show-toplevel)"
cd "$REPO_ROOT"

BASE_REF="origin/main"
FAIL=0

extract_elements() {
  # Single-line <string ...>...</string> / <color ...>...</color> elements only.
  # (No multi-line string/color elements exist in origin/main's androidApp
  # resources as of this gate's authoring — verified before relying on this.)
  grep -oE '<(string|color)[^>]*>.*</(string|color)>' | LC_ALL=C sort
}

diff_values_file() {
  local rel_path="$1"
  local rel_path_re
  rel_path_re=$(printf '%s' "$rel_path" | sed 's/\./\\./g')

  local baseline current
  baseline=$(git show "${BASE_REF}:androidApp/src/main/res/${rel_path}" 2>/dev/null | extract_elements || true)

  current=""
  while IFS= read -r f; do
    current+="$(cat "$f")"$'\n'
  done < <(git ls-files | grep -E "src/main/res/${rel_path_re}\$" || true)
  current=$(printf '%s' "$current" | extract_elements || true)

  if [ "$baseline" != "$current" ]; then
    echo "RESOURCES-DRIFT: ${rel_path} — value multiset differs from origin/main" >&2
    diff <(printf '%s\n' "$baseline") <(printf '%s\n' "$current") >&2 || true
    FAIL=1
  fi
}

for rel_path in values/strings.xml values-fr/strings.xml values/colors.xml values-night/colors.xml; do
  diff_values_file "$rel_path"
done

diff_drawable_qualifier() {
  local qualifier="$1"

  local baseline current
  baseline=$(git ls-tree -r --name-only "${BASE_REF}" -- "androidApp/src/main/res/${qualifier}/" \
    | sed -E "s#.*/${qualifier}/##" | LC_ALL=C sort)

  current=$(git ls-files | grep -E "src/main/res/${qualifier}/[^/]+\$" \
    | sed -E "s#.*/${qualifier}/##" | LC_ALL=C sort)

  if [ "$baseline" != "$current" ]; then
    echo "RESOURCES-DRIFT: drawable/mipmap qualifier ${qualifier} — basename set differs from origin/main" >&2
    diff <(printf '%s\n' "$baseline") <(printf '%s\n' "$current") >&2 || true
    FAIL=1
  fi
}

QUALIFIERS=$(git ls-tree -r --name-only "${BASE_REF}" -- androidApp/src/main/res \
  | grep -E '/(drawable|mipmap)[^/]*/' \
  | sed -E 's#androidApp/src/main/res/([^/]+)/.*#\1#' \
  | LC_ALL=C sort -u)

for qualifier in $QUALIFIERS; do
  diff_drawable_qualifier "$qualifier"
done

if [ "$FAIL" -ne 0 ]; then
  exit 1
fi

echo "RESOURCES-OK"
