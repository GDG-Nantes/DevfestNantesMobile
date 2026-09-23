#!/usr/bin/env bash
# Swift-visible-name gate for the :shared umbrella framework (D-16, ARCH-04).
#
# Modes:
#   baseline  - regenerate the current shared.h swift_name set, write it to
#               03-shared-h-baseline.txt, then intersect it with the
#               capitalized identifiers actually referenced from iosApp's
#               Swift sources and write that to 03-swift-referenced-names.txt.
#   check     - regenerate the current shared.h swift_name set and fail if any
#               name in 03-swift-referenced-names.txt is now missing, or if
#               the collision-disambiguated name count (names ending in "_")
#               differs from the baseline. Prints an informational diff.
#
# Usage: bash swift-names-gate.sh <baseline|check>

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
BASELINE_FILE="${SCRIPT_DIR}/03-shared-h-baseline.txt"
REFERENCED_FILE="${SCRIPT_DIR}/03-swift-referenced-names.txt"
SHARED_H="${REPO_ROOT}/shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h"

MODE="${1:-}"
if [ "$MODE" != "baseline" ] && [ "$MODE" != "check" ]; then
  echo "Usage: $0 <baseline|check>" >&2
  exit 1
fi

cd "$REPO_ROOT"

echo "Linking :shared debug framework for iosSimulatorArm64..." >&2
./gradlew --no-daemon -q :shared:linkDebugFrameworkIosSimulatorArm64

if [ ! -f "$SHARED_H" ]; then
  echo "SWIFT-NAME-MISSING <shared.h not found at $SHARED_H>" >&2
  exit 1
fi

# Extract the sorted unique set of names inside swift_name("...") attributes.
CURRENT_NAMES=$(grep -oE 'swift_name\("[^"]+"\)' "$SHARED_H" | sed -E 's/swift_name\("([^"]+)"\)/\1/' | sort -u)

if [ "$MODE" = "baseline" ]; then
  printf '%s\n' "$CURRENT_NAMES" > "$BASELINE_FILE"

  # Sorted unique set of capitalized identifiers referenced from all
  # git-tracked iosApp/**/*.swift files (git ls-files -z + xargs -0 because
  # one iosApp folder name contains a space).
  SWIFT_IDENTIFIERS=$(git ls-files -z -- 'iosApp/**/*.swift' | xargs -0 grep -hoE '\b[A-Z][A-Za-z0-9_]*\b' -- | sort -u)

  comm -12 <(printf '%s\n' "$CURRENT_NAMES") <(printf '%s\n' "$SWIFT_IDENTIFIERS") > "$REFERENCED_FILE"

  echo "Baseline written to $BASELINE_FILE ($(printf '%s\n' "$CURRENT_NAMES" | sed '/^$/d' | wc -l | tr -d ' ') names)" >&2
  echo "Referenced names written to $REFERENCED_FILE ($(sed '/^$/d' "$REFERENCED_FILE" | wc -l | tr -d ' ') names)" >&2
  exit 0
fi

# MODE = check
if [ ! -f "$BASELINE_FILE" ] || [ ! -f "$REFERENCED_FILE" ]; then
  echo "SWIFT-NAME-MISSING <baseline files not found — run 'baseline' mode first>" >&2
  exit 1
fi

MISSING=0
while IFS= read -r name; do
  [ -z "$name" ] && continue
  if ! printf '%s\n' "$CURRENT_NAMES" | grep -qxF "$name"; then
    echo "SWIFT-NAME-MISSING $name"
    MISSING=1
  fi
done < "$REFERENCED_FILE"

if [ "$MISSING" -eq 1 ]; then
  exit 1
fi

BASELINE_COLLISION_COUNT=$(grep -cE '_$' "$BASELINE_FILE" || true)
CURRENT_COLLISION_COUNT=$(printf '%s\n' "$CURRENT_NAMES" | grep -cE '_$' || true)

if [ "$BASELINE_COLLISION_COUNT" != "$CURRENT_COLLISION_COUNT" ]; then
  echo "SWIFT-NAME-COLLISION baseline=$BASELINE_COLLISION_COUNT current=$CURRENT_COLLISION_COUNT"
  exit 1
fi

echo "--- comm -3 diff (baseline vs current, informational) ---"
comm -3 "$BASELINE_FILE" <(printf '%s\n' "$CURRENT_NAMES") || true
echo "--- end diff ---"

echo "SWIFT-NAMES-OK"
