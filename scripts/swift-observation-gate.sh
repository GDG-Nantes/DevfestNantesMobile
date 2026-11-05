#!/usr/bin/env bash
# Swift observation regression gate (phase 8, D-15, IOSUI-03).
#
# Phase 8 moved every iOS ViewModel to the Observation framework (@Observable, @State,
# @Environment) and removed Combine. This gate keeps it that way: it fails when Combine or
# a legacy observation API comes back into the iOS Swift sources, or when the
# KMP-NativeCoroutines Combine product comes back into the Xcode project. Pure text check:
# runs anywhere (no Xcode, no Gradle), it runs early in .github/workflows/ios.yml.
#
# Two rules:
#
#   Swift rule     Any line of a tracked iOS Swift file (the app and DevFestNantesTests)
#                  that contains one of the following is a hit. Comments are scanned too,
#                  so a comment must not name them either; there is no allowlist.
#                    - an import of the Combine module (whole word)
#                    - the KMP-NativeCoroutines Combine module name
#                    - the legacy observable-object protocol name
#                    - the published property wrapper (with its at-sign)
#                    - the will-change publisher name
#                    - a main-queue async hop: DispatchQueue, .main, then .async, with
#                      optional spaces around the dots (async-after is caught too)
#                  The only excluded paths are the generated SwiftGen files under
#                  iosApp/iosApp/SwiftGen/.
#
#   Project rule   Any line of iosApp/iosApp.xcodeproj/project.pbxproj that names the
#                  KMP-NativeCoroutines Combine product. The package reference and the
#                  Async product are not touched by the rule.
#
# Scan set: git ls-files -z -- 'iosApp/*.swift', minus the SwiftGen directory. The scan
# set can never be empty: an empty set is reported as a failure, never a silent pass.
#
# Modes:
#   check (default)  Runs both rules on the repository. Prints one token line per
#                    violation and exits 1 if any was printed, else prints
#                    SWIFT-OBS-GATE-OK and exits 0.
#   selftest         Runs the same matchers on fixture files written under a temp dir and
#                    proves each pattern, the clean case, the SwiftGen exclusion, both
#                    project cases and the empty scan set. Prints
#                    SWIFT-OBS-GATE-SELFTEST-OK, or
#                    SWIFT-OBS-GATE-SELFTEST-FAILED <case> and exits 1.
#
# Tokens (stdout):
#   SWIFT-OBS-FOUND <path>:<line>           a banned construct in a Swift source
#   SWIFT-OBS-PBXPROJ-FOUND <path>:<line>   the Combine product in the Xcode project
#   SWIFT-OBS-PBXPROJ-MISSING <path>        the Xcode project could not be read
#   SWIFT-OBS-SCAN-EMPTY                    no Swift file was scanned
#   SWIFT-OBS-GATE-OK                       check passed
#   SWIFT-OBS-GATE-SELFTEST-OK              selftest passed
#   SWIFT-OBS-GATE-SELFTEST-FAILED <case>   selftest failed on <case>
#
# Exit codes: 0 success, 1 violation or selftest failure, 2 unknown mode.
#
# Usage: bash scripts/swift-observation-gate.sh [check|selftest]

set -euo pipefail

# Byte-wise collation and character handling independent of the caller's locale.
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

MODE="${1:-check}"
if [ "$MODE" != "check" ] && [ "$MODE" != "selftest" ]; then
  echo "Usage: $0 [check|selftest]" >&2
  exit 2
fi

WORK="$(mktemp -d "${TMPDIR:-/tmp}/swift-observation-gate.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT

PBXPROJ_PATH="iosApp/iosApp.xcodeproj/project.pbxproj"
SWIFTGEN_PREFIX="iosApp/iosApp/SwiftGen/"

# Portable word boundaries: \b is not understood by every grep -E, so they are spelled out.
SWIFT_RE='(^|[^A-Za-z0-9_])import[[:space:]]+Combine([^A-Za-z0-9_]|$)|KMPNativeCoroutinesCombine|ObservableObject|@Published|objectWillChange|DispatchQueue[[:space:]]*\.[[:space:]]*main[[:space:]]*\.[[:space:]]*async'
PBXPROJ_RE='KMPNativeCoroutinesCombine'

# filter_scan: reads repo-relative paths (one per line) on stdin, drops the generated
# SwiftGen output. This is the only exclusion of the gate.
filter_scan() {
  local path
  while IFS= read -r path; do
    [ -n "$path" ] || continue
    case "$path" in
      "$SWIFTGEN_PREFIX"*) continue ;;
    esac
    printf '%s\n' "$path"
  done
}

# scan_set: tracked iOS Swift sources, one repo-relative path per line. The listing is
# NUL-separated so unusual file names (spaces) survive until the filter.
scan_set() {
  git -C "$REPO_ROOT" ls-files -z -- 'iosApp/*.swift' | tr '\0' '\n' | filter_scan | sort -u
}

# hit_lines <file>: line numbers (sorted, unique) matching a pattern.
hit_lines() {
  { grep -n -E "$2" "$1" || true; } | cut -d: -f1 | sort -n -u
}

# run_checks <root> <file-list> <pbxproj-path-relative-to-root>
# Applies both rules and prints one token line per violation. Prints nothing when clean.
run_checks() {
  local root="$1" list="$2" pbx="$3"
  local path line scanned=0

  while IFS= read -r path; do
    [ -n "$path" ] || continue
    [ -f "$root/$path" ] || continue
    scanned=$((scanned + 1))
    for line in $(hit_lines "$root/$path" "$SWIFT_RE"); do
      echo "SWIFT-OBS-FOUND $path:$line"
    done
  done < "$list"

  if [ "$scanned" -eq 0 ]; then
    echo "SWIFT-OBS-SCAN-EMPTY"
  fi

  if [ -f "$root/$pbx" ]; then
    for line in $(hit_lines "$root/$pbx" "$PBXPROJ_RE"); do
      echo "SWIFT-OBS-PBXPROJ-FOUND $pbx:$line"
    done
  else
    echo "SWIFT-OBS-PBXPROJ-MISSING $pbx"
  fi
}

mode_check() {
  scan_set > "$WORK/scan.txt"
  local out
  out="$(run_checks "$REPO_ROOT" "$WORK/scan.txt" "$PBXPROJ_PATH")"
  if [ -n "$out" ]; then
    printf '%s\n' "$out"
    exit 1
  fi
  echo "SWIFT-OBS-GATE-OK"
}

selftest_fail() {
  echo "SWIFT-OBS-GATE-SELFTEST-FAILED $1"
  if [ -n "${2:-}" ]; then
    printf '%s\n' "$2" >&2
  fi
  exit 1
}

# expect_contains <case> <output> <needle>
expect_contains() {
  if ! printf '%s\n' "$2" | grep -Fq -- "$3"; then
    selftest_fail "$1" "expected '$3' in output:
$2"
  fi
}

# expect_clean <case> <output>
expect_clean() {
  if [ -n "$2" ]; then
    selftest_fail "$1" "expected no output, got:
$2"
  fi
}

# plant <root> <repo-relative path> <content>
plant() {
  mkdir -p "$(dirname "$1/$2")"
  printf '%s\n' "$3" > "$1/$2"
}

mode_selftest() {
  local fx="$WORK/fx" out
  mkdir -p "$fx"
  local swift="iosApp/iosApp/UI/Foo.swift"
  local gen="iosApp/iosApp/SwiftGen/Strings.swift"
  local pbx="$PBXPROJ_PATH"
  local cleanPbx='		003EC8D92DF8D84B000E5689 /* KMPNativeCoroutinesAsync in Frameworks */ = {isa = PBXBuildFile; };'
  local list="$WORK/list.txt"
  printf '%s\n' "$swift" > "$list"

  # 1. One fixture per banned Swift pattern, each yields a token for its line.
  local i=0 body
  while IFS= read -r body; do
    [ -n "$body" ] || continue
    i=$((i + 1))
    plant "$fx/p$i" "$swift" "// header
$body"
    plant "$fx/p$i" "$pbx" "$cleanPbx"
    out="$(run_checks "$fx/p$i" "$list" "$pbx")"
    expect_contains "swift-pattern-$i" "$out" "SWIFT-OBS-FOUND $swift:2"
  done <<'EOF_PATTERNS'
import Combine
@preconcurrency import Combine
import KMPNativeCoroutinesCombine
final class Foo: ObservableObject {}
@Published var items: [String] = []
objectWillChange.send()
DispatchQueue.main.async { }
DispatchQueue.main.asyncAfter(deadline: .now() + 1) { }
DispatchQueue . main . async { }
// do not use ObservableObject here
EOF_PATTERNS

  # 2. A clean Swift fixture (Observation, state, task, global-queue dispatch) is not a hit.
  plant "$fx/clean" "$swift" 'import SwiftUI
import Observation
@Observable @MainActor final class Foo { var items: [String] = [] }
struct Bar: View {
  @State private var model = Foo()
  var body: some View { Text("x").task { DispatchQueue.global().async { } } }
}
let combineLatest = 1
let importCombined = 2'
  plant "$fx/clean" "$pbx" "$cleanPbx"
  out="$(run_checks "$fx/clean" "$list" "$pbx")"
  expect_clean "swift-clean" "$out"

  # 3. A violation under iosApp/iosApp/SwiftGen/ is spared; the same line elsewhere is not.
  plant "$fx/gen" "$swift" 'struct Foo {}'
  plant "$fx/gen" "$gen" 'import Combine'
  plant "$fx/gen" "$pbx" "$cleanPbx"
  printf '%s\n%s\n' "$swift" "$gen" | filter_scan > "$WORK/gen-list.txt"
  out="$(run_checks "$fx/gen" "$WORK/gen-list.txt" "$pbx")"
  expect_clean "swiftgen-excluded" "$out"
  printf '%s\n' "$gen" > "$WORK/gen-raw.txt"
  out="$(run_checks "$fx/gen" "$WORK/gen-raw.txt" "$pbx")"
  expect_contains "swiftgen-raw-hit" "$out" "SWIFT-OBS-FOUND $gen:1"

  # 4. A project naming the Combine product is a hit; only the Async product is clean.
  plant "$fx/pbx" "$swift" 'struct Foo {}'
  plant "$fx/pbx" "$pbx" "$cleanPbx
		003EC8DB2DF8D84B000E5689 /* KMPNativeCoroutinesCombine in Frameworks */ = {isa = PBXBuildFile; };"
  out="$(run_checks "$fx/pbx" "$list" "$pbx")"
  expect_contains "pbxproj-combine" "$out" "SWIFT-OBS-PBXPROJ-FOUND $pbx:2"
  plant "$fx/pbx" "$pbx" "$cleanPbx"
  out="$(run_checks "$fx/pbx" "$list" "$pbx")"
  expect_clean "pbxproj-async-only" "$out"

  # 5. A missing project file is reported, not skipped.
  plant "$fx/nopbx" "$swift" 'struct Foo {}'
  out="$(run_checks "$fx/nopbx" "$list" "$pbx")"
  expect_contains "pbxproj-missing" "$out" "SWIFT-OBS-PBXPROJ-MISSING $pbx"

  # 6. An empty scan list is reported, never a silent pass.
  : > "$WORK/empty.txt"
  out="$(run_checks "$fx/clean" "$WORK/empty.txt" "$pbx")"
  expect_contains "scan-empty" "$out" "SWIFT-OBS-SCAN-EMPTY"

  echo "SWIFT-OBS-GATE-SELFTEST-OK"
}

cd "$REPO_ROOT"
case "$MODE" in
  check) mode_check ;;
  selftest) mode_selftest ;;
esac
