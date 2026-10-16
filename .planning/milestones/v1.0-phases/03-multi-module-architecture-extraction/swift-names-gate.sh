#!/usr/bin/env bash
# Swift-visible-name gate for the :shared umbrella framework (D-16, ARCH-04).
#
# Modes:
#   baseline  - regenerate the current shared.h derived sets (ALL_NAMES,
#               TYPE_NAMES, MEMBERS), write ALL_NAMES to
#               03-shared-h-baseline.txt, intersect it with the capitalized
#               identifiers actually referenced from iosApp's Swift sources
#               (whole-line comments dropped, exclusions from
#               03-swift-names-exclude.txt subtracted) and write that to
#               03-swift-referenced-names.txt, then write the per-type member
#               sets for every referenced type to 03-swift-members-baseline.txt.
#   check     - regenerate the current shared.h derived sets and fail if:
#                 * SWIFT-NAME-MISSING <name>    - a referenced name is gone
#                 * SWIFT-TYPE-COLLISION <name>  - a type-level Swift name has
#                   a dot-separated component ending in "_" (a NEW identity
#                   collision, not just a count change)
#                 * SWIFT-MEMBERS-CHANGED <Type> - a referenced type's member
#                   set differs from the baseline (catches an identity swap
#                   behind an unchanged type-level name)
#                 * SWIFT-NAME-COLLISION baseline=<n> current=<m> - the total
#                   collision-disambiguated ("_"-suffixed) name count changed
#               Prints every offending line, then an informational
#               comm -3 diff, then SWIFT-NAMES-OK on success.
#   selftest  - resolves the real header once, then builds two doctored
#               in-memory copies under ${TMPDIR:-/tmp} to prove the new checks
#               actually catch what the old ones missed:
#                 * a SWAP copy (two referenced types' identities exchanged)
#                   must fail with SWIFT-MEMBERS-CHANGED and must NOT fail
#                   with SWIFT-NAME-MISSING or SWIFT-NAME-COLLISION
#                 * a COLLISION copy (an unreferenced, non-"_" type renamed to
#                   end in "_") must fail with SWIFT-TYPE-COLLISION
#               Prints SWIFT-GATE-SELFTEST-OK if both doctored copies behave
#               as expected, else exits non-zero.
#
# Env override: SWIFT_GATE_HEADER - if set, use that file as the header
# instead of linking :shared's debug iosSimulatorArm64 framework and reading
# its Headers/shared.h. Used by selftest's doctored copies and by replaying a
# previously captured header (e.g. a pre-fix snapshot) without rebuilding.
#
# Usage: bash swift-names-gate.sh <baseline|check|selftest>

set -euo pipefail

# All sort/comm/grep calls below assume C-locale byte ordering (consistent
# ASCII collation between separately-sorted files feeding `comm`); a locale
# with different collation rules would make `comm` silently miss matches
# that ARE present verbatim in both inputs.
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
BASELINE_FILE="${SCRIPT_DIR}/03-shared-h-baseline.txt"
REFERENCED_FILE="${SCRIPT_DIR}/03-swift-referenced-names.txt"
MEMBERS_FILE="${SCRIPT_DIR}/03-swift-members-baseline.txt"
EXCLUDE_FILE="${SCRIPT_DIR}/03-swift-names-exclude.txt"
DEFAULT_SHARED_H="${REPO_ROOT}/shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h"

MODE="${1:-}"
if [ "$MODE" != "baseline" ] && [ "$MODE" != "check" ] && [ "$MODE" != "selftest" ]; then
  echo "Usage: $0 <baseline|check|selftest>" >&2
  exit 1
fi

cd "$REPO_ROOT"

WORK="$(mktemp -d "${TMPDIR:-/tmp}/swift-names-gate.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT

# resolve_header <out-var-name>
# Sets the named variable to the header path to use: SWIFT_GATE_HEADER if set,
# otherwise links :shared's debug iosSimulatorArm64 framework and uses its
# fixed Headers/shared.h path.
resolve_header() {
  local __outvar="$1"
  local header
  if [ -n "${SWIFT_GATE_HEADER:-}" ]; then
    header="$SWIFT_GATE_HEADER"
  else
    echo "Linking :shared debug framework for iosSimulatorArm64..." >&2
    ./gradlew --no-daemon -q :shared:linkDebugFrameworkIosSimulatorArm64
    header="$DEFAULT_SHARED_H"
  fi
  if [ ! -f "$header" ]; then
    echo "SWIFT-NAME-MISSING <shared.h not found at $header>" >&2
    exit 1
  fi
  printf -v "$__outvar" '%s' "$header"
}

# derive_sets <header-file> <all-names-out> <type-names-out> <member-pairs-out>
# Single awk pass over the header. Writes three sorted-unique files:
#   all-names-out:    every swift_name("...") value in the header
#   type-names-out:   values of attribute lines immediately followed by a
#                      line starting with "@interface" or "@protocol"
#   member-pairs-out: "Type<TAB>Member" for every swift_name("...") value
#                      found between a type's @interface/@protocol line and
#                      its matching @end
derive_sets() {
  local header="$1" all_out="$2" type_out="$3" member_out="$4"
  local raw="${WORK}/derive-raw.txt"

  grep -oE 'swift_name\("[^"]+"\)' "$header" \
    | sed -E 's/swift_name\("([^"]+)"\)/\1/' \
    | LC_ALL=C sort -u > "$all_out"

  awk '
    /^__attribute__\(\(swift_name\("[^"]+"\)\)\)$/ {
      match($0, /"[^"]+"/)
      tname = substr($0, RSTART + 1, RLENGTH - 2)
      pending_type = tname
      pending = 1
      next
    }
    /^@interface / || /^@protocol / {
      if (pending) {
        curtype = pending_type
        print "TYPE\t" curtype
      } else {
        curtype = ""
      }
      pending = 0
      next
    }
    /^@end$/ {
      curtype = ""
      pending = 0
      next
    }
    {
      pending = 0
      if (curtype != "") {
        line = $0
        while (match(line, /swift_name\("[^"]+"\)/)) {
          seg = substr(line, RSTART, RLENGTH)
          match(seg, /"[^"]+"/)
          member = substr(seg, RSTART + 1, RLENGTH - 2)
          print "MEMBER\t" curtype "\t" member
          line = substr(line, RSTART + RLENGTH)
        }
      }
    }
  ' "$header" > "$raw"

  grep '^TYPE\t' "$raw" | cut -f2 | LC_ALL=C sort -u > "$type_out"
  grep '^MEMBER\t' "$raw" | cut -f2,3 | LC_ALL=C sort -u > "$member_out"
}

# sed_escape <string>
# Escapes basic-regex metacharacters for safe use inside a sed pattern or
# replacement built with '/' delimiters.
sed_escape() {
  printf '%s' "$1" | sed -e 's/[.[\*^$\/]/\\&/g'
}

if [ "$MODE" = "baseline" ]; then
  resolve_header HEADER
  ALL_NAMES_FILE="${WORK}/all-names.txt"
  TYPE_NAMES_FILE="${WORK}/type-names.txt"
  MEMBER_PAIRS_FILE="${WORK}/member-pairs.txt"
  derive_sets "$HEADER" "$ALL_NAMES_FILE" "$TYPE_NAMES_FILE" "$MEMBER_PAIRS_FILE"

  cp "$ALL_NAMES_FILE" "$BASELINE_FILE"

  # Sorted unique set of capitalized identifiers referenced from all
  # git-tracked iosApp/**/*.swift files (git ls-files -z + xargs -0 because
  # one iosApp folder name contains a space), with whole-line comments
  # (lines whose first non-blank characters are "//") dropped first so a
  # commented-out reference doesn't count.
  SWIFT_IDENTIFIERS_RAW="${WORK}/swift-identifiers-raw.txt"
  git ls-files -z -- 'iosApp/**/*.swift' \
    | xargs -0 cat -- 2>/dev/null \
    | grep -vE '^[[:space:]]*//' \
    | grep -hoE '\b[A-Z][A-Za-z0-9_]*\b' -- \
    | LC_ALL=C sort -u > "$SWIFT_IDENTIFIERS_RAW"

  REFERENCED_RAW="${WORK}/referenced-raw.txt"
  comm -12 "$ALL_NAMES_FILE" "$SWIFT_IDENTIFIERS_RAW" > "$REFERENCED_RAW"

  EXCLUDE_NAMES="${WORK}/exclude-names.txt"
  if [ -f "$EXCLUDE_FILE" ]; then
    grep -v '^#' "$EXCLUDE_FILE" | cut -f1 | grep -v '^$' | LC_ALL=C sort -u > "$EXCLUDE_NAMES"
  else
    : > "$EXCLUDE_NAMES"
  fi

  comm -23 "$REFERENCED_RAW" "$EXCLUDE_NAMES" > "$REFERENCED_FILE"

  # Members baseline: only for types that are BOTH Swift-referenced AND
  # genuinely type-level (present in TYPE_NAMES) - excludes referenced
  # identifiers that happen to be member names, not types.
  REFERENCED_TYPES="${WORK}/referenced-types.txt"
  comm -12 "$REFERENCED_FILE" "$TYPE_NAMES_FILE" > "$REFERENCED_TYPES"
  awk -F'\t' 'NR==FNR{keep[$1]=1; next} ($1 in keep)' "$REFERENCED_TYPES" "$MEMBER_PAIRS_FILE" \
    | LC_ALL=C sort -u > "$MEMBERS_FILE"

  echo "Baseline written to $BASELINE_FILE ($(sed '/^$/d' "$BASELINE_FILE" | wc -l | tr -d ' ') names)" >&2
  echo "Referenced names written to $REFERENCED_FILE ($(sed '/^$/d' "$REFERENCED_FILE" | wc -l | tr -d ' ') names)" >&2
  echo "Member pairs written to $MEMBERS_FILE ($(sed '/^$/d' "$MEMBERS_FILE" | wc -l | tr -d ' ') pairs)" >&2
  exit 0
fi

if [ "$MODE" = "check" ]; then
  resolve_header HEADER
  ALL_NAMES_FILE="${WORK}/all-names.txt"
  TYPE_NAMES_FILE="${WORK}/type-names.txt"
  MEMBER_PAIRS_FILE="${WORK}/member-pairs.txt"
  derive_sets "$HEADER" "$ALL_NAMES_FILE" "$TYPE_NAMES_FILE" "$MEMBER_PAIRS_FILE"

  if [ ! -f "$BASELINE_FILE" ] || [ ! -f "$REFERENCED_FILE" ] || [ ! -f "$MEMBERS_FILE" ]; then
    echo "SWIFT-NAME-MISSING <baseline files not found — run 'baseline' mode first>" >&2
    exit 1
  fi

  FAIL=0

  # Check 1: every referenced name still present.
  while IFS= read -r name; do
    [ -z "$name" ] && continue
    if ! grep -qxF "$name" "$ALL_NAMES_FILE"; then
      echo "SWIFT-NAME-MISSING $name"
      FAIL=1
    fi
  done < "$REFERENCED_FILE"

  # Check 2: no type-level name has a dot-separated component ending in "_"
  # (a NEW identity collision, e.g. "Venue_" or "Venue_.Companion", but not
  # "Kotlinx_coroutines_coreFlow" or "GetVenueQuery_ResponseAdapter.Venue").
  while IFS= read -r tname; do
    [ -z "$tname" ] && continue
    if printf '%s\n' "$tname" | grep -qE '(^|\.)[A-Za-z0-9]+_(\.|$)'; then
      echo "SWIFT-TYPE-COLLISION $tname"
      FAIL=1
    fi
  done < "$TYPE_NAMES_FILE"

  # Check 3: every baselined type's member set is unchanged.
  BASELINE_TYPES="${WORK}/baseline-types.txt"
  cut -f1 "$MEMBERS_FILE" | LC_ALL=C sort -u > "$BASELINE_TYPES"
  while IFS= read -r btype; do
    [ -z "$btype" ] && continue
    BASE_MEMBERS="${WORK}/base-members-$$.txt"
    CUR_MEMBERS="${WORK}/cur-members-$$.txt"
    awk -F'\t' -v t="$btype" '$1==t {print $2}' "$MEMBERS_FILE" | LC_ALL=C sort -u > "$BASE_MEMBERS"
    awk -F'\t' -v t="$btype" '$1==t {print $2}' "$MEMBER_PAIRS_FILE" | LC_ALL=C sort -u > "$CUR_MEMBERS"
    if ! cmp -s "$BASE_MEMBERS" "$CUR_MEMBERS"; then
      echo "SWIFT-MEMBERS-CHANGED $btype"
      comm -3 "$BASE_MEMBERS" "$CUR_MEMBERS" | sed 's/^\t/+ /; s/^\([^+]\)/- \1/' || true
      FAIL=1
    fi
    rm -f "$BASE_MEMBERS" "$CUR_MEMBERS"
  done < "$BASELINE_TYPES"

  # Check 4: the total collision-disambiguated ("_"-suffixed) name count.
  BASELINE_COLLISION_COUNT=$(grep -cE '_$' "$BASELINE_FILE" || true)
  CURRENT_COLLISION_COUNT=$(grep -cE '_$' "$ALL_NAMES_FILE" || true)
  if [ "$BASELINE_COLLISION_COUNT" != "$CURRENT_COLLISION_COUNT" ]; then
    echo "SWIFT-NAME-COLLISION baseline=$BASELINE_COLLISION_COUNT current=$CURRENT_COLLISION_COUNT"
    FAIL=1
  fi

  echo "--- comm -3 diff (baseline vs current, informational) ---"
  comm -3 "$BASELINE_FILE" "$ALL_NAMES_FILE" || true
  echo "--- end diff ---"

  if [ "$FAIL" -eq 1 ]; then
    exit 1
  fi

  echo "SWIFT-NAMES-OK"
  exit 0
fi

# MODE = selftest
resolve_header HEADER
ALL_NAMES_FILE="${WORK}/all-names.txt"
TYPE_NAMES_FILE="${WORK}/type-names.txt"
MEMBER_PAIRS_FILE="${WORK}/member-pairs.txt"
derive_sets "$HEADER" "$ALL_NAMES_FILE" "$TYPE_NAMES_FILE" "$MEMBER_PAIRS_FILE"

if [ ! -f "$MEMBERS_FILE" ] || [ ! -f "$REFERENCED_FILE" ]; then
  echo "SWIFT-NAME-MISSING <baseline files not found — run 'baseline' mode first>" >&2
  exit 1
fi

SELFTEST_FAIL=0

# --- SWAP copy: exchange two referenced types' type-level identities. ---
member_set_of() {
  awk -F'\t' -v t="$1" '$1==t {print $2}' "$MEMBERS_FILE" | LC_ALL=C sort -u
}

TYPES_FILE="${WORK}/members-types.txt"
cut -f1 "$MEMBERS_FILE" | LC_ALL=C sort -u > "$TYPES_FILE"

TYPE_A=""
TYPE_B=""
NTYPES=$(wc -l < "$TYPES_FILE" | tr -d ' ')
for ((i = 1; i <= NTYPES; i++)); do
  TI=$(sed -n "${i}p" "$TYPES_FILE")
  [ -z "$TI" ] && continue
  for ((j = i + 1; j <= NTYPES; j++)); do
    TJ=$(sed -n "${j}p" "$TYPES_FILE")
    [ -z "$TJ" ] && continue
    SET_I="${WORK}/set-i.txt"
    SET_J="${WORK}/set-j.txt"
    member_set_of "$TI" > "$SET_I"
    member_set_of "$TJ" > "$SET_J"
    if ! cmp -s "$SET_I" "$SET_J"; then
      TYPE_A="$TI"
      TYPE_B="$TJ"
    fi
    [ -n "$TYPE_A" ] && break
  done
  [ -n "$TYPE_A" ] && break
done

if [ -z "$TYPE_A" ] || [ -z "$TYPE_B" ]; then
  echo "SWIFT-GATE-SELFTEST-SETUP-FAILED <no two referenced types with differing member sets found>" >&2
  exit 1
fi

SWAP_H="${WORK}/swap-shared.h"
cp "$HEADER" "$SWAP_H"
ESC_A="$(sed_escape "$TYPE_A")"
ESC_B="$(sed_escape "$TYPE_B")"
MARK="__GSD_SELFTEST_SWAP_MARK__"
sed -i '' "s/^__attribute__((swift_name(\"${ESC_A}\")))\$/${MARK}/" "$SWAP_H"
sed -i '' "s/^__attribute__((swift_name(\"${ESC_B}\")))\$/__attribute__((swift_name(\"${ESC_A}\")))/" "$SWAP_H"
sed -i '' "s/${MARK}/__attribute__((swift_name(\"${ESC_B}\")))/" "$SWAP_H"

SWAP_OUT="${WORK}/swap-check-out.txt"
SWAP_RC=0
SWIFT_GATE_HEADER="$SWAP_H" bash "${BASH_SOURCE[0]}" check > "$SWAP_OUT" 2>&1 || SWAP_RC=$?

echo "--- selftest SWAP ($TYPE_A <-> $TYPE_B) check output ---"
cat "$SWAP_OUT"
echo "--- end SWAP output ---"

if [ "$SWAP_RC" -eq 0 ]; then
  echo "SWIFT-GATE-SELFTEST-FAILED <SWAP copy: check exited 0, expected non-zero>" >&2
  SELFTEST_FAIL=1
fi
if ! grep -q "SWIFT-MEMBERS-CHANGED $TYPE_A" "$SWAP_OUT"; then
  echo "SWIFT-GATE-SELFTEST-FAILED <SWAP copy: missing SWIFT-MEMBERS-CHANGED $TYPE_A>" >&2
  SELFTEST_FAIL=1
fi
if grep -q "SWIFT-NAME-MISSING" "$SWAP_OUT"; then
  echo "SWIFT-GATE-SELFTEST-FAILED <SWAP copy: unexpected SWIFT-NAME-MISSING>" >&2
  SELFTEST_FAIL=1
fi
if grep -q "SWIFT-NAME-COLLISION" "$SWAP_OUT"; then
  echo "SWIFT-GATE-SELFTEST-FAILED <SWAP copy: unexpected SWIFT-NAME-COLLISION>" >&2
  SELFTEST_FAIL=1
fi

# --- COLLISION copy: rename an unreferenced, non-"_" type to end in "_". ---
CANDIDATE=$(comm -23 "$TYPE_NAMES_FILE" "$REFERENCED_FILE" | grep -vE '_$' | head -1 || true)

if [ -z "$CANDIDATE" ]; then
  echo "SWIFT-GATE-SELFTEST-SETUP-FAILED <no unreferenced, non-underscore-suffixed type found>" >&2
  exit 1
fi

COLL_H="${WORK}/collision-shared.h"
cp "$HEADER" "$COLL_H"
ESC_C="$(sed_escape "$CANDIDATE")"
sed -i '' "s/^__attribute__((swift_name(\"${ESC_C}\")))\$/__attribute__((swift_name(\"${ESC_C}_\")))/" "$COLL_H"

COLL_OUT="${WORK}/collision-check-out.txt"
COLL_RC=0
SWIFT_GATE_HEADER="$COLL_H" bash "${BASH_SOURCE[0]}" check > "$COLL_OUT" 2>&1 || COLL_RC=$?

echo "--- selftest COLLISION ($CANDIDATE -> ${CANDIDATE}_) check output ---"
cat "$COLL_OUT"
echo "--- end COLLISION output ---"

if [ "$COLL_RC" -eq 0 ]; then
  echo "SWIFT-GATE-SELFTEST-FAILED <COLLISION copy: check exited 0, expected non-zero>" >&2
  SELFTEST_FAIL=1
fi
if ! grep -q "SWIFT-TYPE-COLLISION ${CANDIDATE}_" "$COLL_OUT"; then
  echo "SWIFT-GATE-SELFTEST-FAILED <COLLISION copy: missing SWIFT-TYPE-COLLISION ${CANDIDATE}_>" >&2
  SELFTEST_FAIL=1
fi

if [ "$SELFTEST_FAIL" -eq 1 ]; then
  exit 1
fi

echo "SWIFT-GATE-SELFTEST-OK"
exit 0
