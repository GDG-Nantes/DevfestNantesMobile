#!/usr/bin/env bash
# Dependency-injection regression gate for the Hilt -> Koin migration (phase 4, D-13).
#
# Two independent rules, both run over tracked source files only (*.kt *.kts
# *.toml *.pro *.xml *.dcl, minus .planning/). Markdown, HTML and Swift are out
# of scope; docs and licenses are updated separately.
#
#   Hilt rule      Any line mentioning hilt, dagger or the javax.inject package,
#                  or carrying a Dagger/Hilt annotation, is a hit. A hit in a path
#                  that is NOT listed in scripts/di-gate-hilt-allowlist.txt prints
#                  DI-HILT-FOUND <path>:<line>. The allowlist is transitional and
#                  can only shrink: a listed path that is not a tracked file, or
#                  whose file no longer has a hit, prints DI-ALLOWLIST-STALE <path>.
#                  A missing allowlist file counts as empty (the end state).
#
#   Locator rule   (*.kt only) Service-locator access outside the DI wiring layer:
#                  KoinComponent, getKoin(), KoinPlatform, koinInject, GlobalContext,
#                  KoinJavaComponent and the org.koin.core.component / ext.android
#                  helper imports. A hit prints DI-LOCATOR-FOUND <path>:<line>
#                  unless the path is permanently allowed: it contains /di/, or its
#                  file name ends in Module.kt, or it is exactly one of
#                    shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt
#                    androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt
#                    androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
#                  koinViewModel is allowed everywhere (Route-level resolution, D-10).
#
# Modes:
#   check (default)  Run both rules on the repository. Prints every offending line,
#                    exits 1 if any was printed, else prints DI-GATE-OK.
#   selftest         Run the same matchers on fixture files written under a temp dir
#                    and prove each rule detects a planted violation and spares an
#                    allowlisted path. Prints DI-GATE-SELFTEST-OK on success.
#   hits             Print the sorted unique tracked paths matching the Hilt rule
#                    (used once to seed the allowlist).
#
# Usage: bash scripts/di-gate.sh [check|selftest|hits]

set -euo pipefail

# Byte-wise collation so sort/comm agree between separately-sorted lists.
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
HILT_ALLOWLIST="${SCRIPT_DIR}/di-gate-hilt-allowlist.txt"

MODE="${1:-check}"
if [ "$MODE" != "check" ] && [ "$MODE" != "selftest" ] && [ "$MODE" != "hits" ]; then
  echo "Usage: $0 [check|selftest|hits]" >&2
  exit 1
fi

WORK="$(mktemp -d "${TMPDIR:-/tmp}/di-gate.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT

# Portable word boundary: \b is not understood by every grep -E, so the trailing
# boundary of an annotation name is spelled out.
HILT_WORD_RE='hilt|dagger|javax\.inject'
HILT_ANNOTATION_RE='@(Inject|Provides|Binds|Module|InstallIn|EntryPoint|AndroidEntryPoint|HiltAndroidApp|HiltViewModel|AssistedInject|Assisted|AssistedFactory|ElementsIntoSet|Singleton|ActivityScoped|AliasOf)([^A-Za-z0-9_]|$)'
LOCATOR_RE='import org\.koin\.core\.component\.|import org\.koin\.android\.ext\.android\.|import org\.koin\.mp\.KoinPlatform|import org\.koin\.compose\.koinInject|import org\.koin\.java\.KoinJavaComponent|import org\.koin\.core\.context\.GlobalContext|KoinComponent|getKoin\('

PERMANENT_LOCATOR_PATHS='shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt
androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt
androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt'

# scan_set: tracked sources the gate looks at, one repo-relative path per line.
scan_set() {
  git -C "$REPO_ROOT" ls-files -- '*.kt' '*.kts' '*.toml' '*.pro' '*.xml' '*.dcl' | { grep -v '^\.planning/' || true; } | sort -u
}

# hilt_lines <file>: line numbers (sorted, unique) matching the Hilt rule.
hilt_lines() {
  {
    grep -n -i -E "$HILT_WORD_RE" "$1" || true
    grep -n -E "$HILT_ANNOTATION_RE" "$1" || true
  } | cut -d: -f1 | sort -n -u
}

# locator_lines <file>: line numbers matching the service-locator rule.
locator_lines() {
  { grep -n -E "$LOCATOR_RE" "$1" || true; } | cut -d: -f1 | sort -n -u
}

# locator_allowed <repo-relative path>: 0 when the path is in the permanent allowlist.
locator_allowed() {
  case "$1" in
    */di/*) return 0 ;;
    *Module.kt) return 0 ;;
  esac
  printf '%s\n' "$PERMANENT_LOCATOR_PATHS" | grep -Fxq -- "$1"
}

# run_checks <root> <file-list> <hilt-allowlist>
# Applies both rules to the files named in <file-list> (paths relative to <root>)
# and prints one token line per violation. Prints nothing when clean.
run_checks() {
  local root="$1" list="$2" allow="$3"
  local hit_paths="$WORK/hit-paths.$$"
  : > "$hit_paths"

  local path line
  while IFS= read -r path; do
    [ -n "$path" ] || continue
    [ -f "$root/$path" ] || continue

    local lines
    lines="$(hilt_lines "$root/$path")"
    if [ -n "$lines" ]; then
      printf '%s\n' "$path" >> "$hit_paths"
      if ! { [ -f "$allow" ] && grep -Fxq -- "$path" "$allow"; }; then
        for line in $lines; do
          echo "DI-HILT-FOUND $path:$line"
        done
      fi
    fi

    case "$path" in
      *.kt)
        if ! locator_allowed "$path"; then
          for line in $(locator_lines "$root/$path"); do
            echo "DI-LOCATOR-FOUND $path:$line"
          done
        fi
        ;;
    esac
  done < "$list"

  if [ -f "$allow" ]; then
    while IFS= read -r path; do
      [ -n "$path" ] || continue
      if ! grep -Fxq -- "$path" "$list" || ! grep -Fxq -- "$path" "$hit_paths"; then
        echo "DI-ALLOWLIST-STALE $path"
      fi
    done < "$allow"
  fi
}

mode_check() {
  scan_set > "$WORK/scan.txt"
  local out
  out="$(run_checks "$REPO_ROOT" "$WORK/scan.txt" "$HILT_ALLOWLIST")"
  if [ -n "$out" ]; then
    printf '%s\n' "$out"
    exit 1
  fi
  echo "DI-GATE-OK"
}

mode_hits() {
  local path
  scan_set > "$WORK/scan.txt"
  while IFS= read -r path; do
    [ -f "$REPO_ROOT/$path" ] || continue
    if [ -n "$(hilt_lines "$REPO_ROOT/$path")" ]; then
      printf '%s\n' "$path"
    fi
  done < "$WORK/scan.txt" | sort -u
}

# expect_contains <label> <haystack> <needle>: fail the selftest unless found.
expect_contains() {
  if ! printf '%s\n' "$2" | grep -Fq -- "$3"; then
    echo "SELFTEST-FAIL $1: expected '$3' in output:" >&2
    printf '%s\n' "$2" >&2
    exit 1
  fi
}

# expect_clean <label> <output>: fail the selftest unless output is empty.
expect_clean() {
  if [ -n "$2" ]; then
    echo "SELFTEST-FAIL $1: expected no output, got:" >&2
    printf '%s\n' "$2" >&2
    exit 1
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
  local feature="feature/foo/src/main/java/com/example/Foo.kt"
  local diModule="feature/foo/src/main/java/com/example/di/FooModule.kt"
  local modulePath="feature/foo/src/main/java/com/example/Other/BarModule.kt"
  local ios="shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt"
  local app="androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt"
  local activity="androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt"
  local clean="feature/foo/src/main/java/com/example/Clean.kt"
  local viewModelUser="feature/foo/src/main/java/com/example/Screen.kt"
  local noAllow="$WORK/none.txt"

  # 1. Hilt rule detects a planted dagger import and a planted annotation.
  plant "$fx/hilt" "$feature" 'import dagger.hilt.android.lifecycle.HiltViewModel'
  printf '%s\n' "$feature" > "$WORK/l1.txt"
  out="$(run_checks "$fx/hilt" "$WORK/l1.txt" "$noAllow")"
  expect_contains "hilt-import" "$out" "DI-HILT-FOUND $feature:1"

  plant "$fx/hilt2" "$feature" '@Inject constructor()'
  out="$(run_checks "$fx/hilt2" "$WORK/l1.txt" "$noAllow")"
  expect_contains "hilt-annotation" "$out" "DI-HILT-FOUND $feature:1"

  # 1b. The same file is spared once it is on the transitional allowlist.
  printf '%s\n' "$feature" > "$WORK/allow1.txt"
  out="$(run_checks "$fx/hilt" "$WORK/l1.txt" "$WORK/allow1.txt")"
  expect_clean "hilt-allowlisted" "$out"

  # 2. A word that merely contains an annotation name is not a hit.
  plant "$fx/near" "$feature" 'val injected = 1 // @InjectedValue'
  out="$(run_checks "$fx/near" "$WORK/l1.txt" "$noAllow")"
  expect_clean "hilt-annotation-boundary" "$out"

  # 3. Locator rule detects the KoinPlatform import outside the wiring layer.
  plant "$fx/loc" "$feature" 'import org.koin.mp.KoinPlatform'
  out="$(run_checks "$fx/loc" "$WORK/l1.txt" "$noAllow")"
  expect_contains "locator-import" "$out" "DI-LOCATOR-FOUND $feature:1"

  plant "$fx/loc2" "$feature" 'val k = getKoin().get<String>()'
  out="$(run_checks "$fx/loc2" "$WORK/l1.txt" "$noAllow")"
  expect_contains "locator-getKoin" "$out" "DI-LOCATOR-FOUND $feature:1"

  # 3b. The permanent allowlist spares /di/ paths, *Module.kt and the three named files.
  local p
  for p in "$diModule" "$modulePath" "$ios" "$app" "$activity"; do
    plant "$fx/perm" "$p" 'import org.koin.mp.KoinPlatform'
    printf '%s\n' "$p" > "$WORK/lp.txt"
    out="$(run_checks "$fx/perm" "$WORK/lp.txt" "$noAllow")"
    expect_clean "locator-permanent $p" "$out"
  done

  # 3c. koinViewModel is allowed everywhere (D-10).
  plant "$fx/kvm" "$viewModelUser" 'import org.koin.compose.viewmodel.koinViewModel'
  printf '%s\n' "$viewModelUser" > "$WORK/l2.txt"
  out="$(run_checks "$fx/kvm" "$WORK/l2.txt" "$noAllow")"
  expect_clean "koinViewModel" "$out"

  # 4. An allowlist entry whose file is clean is stale; so is one that is not tracked.
  plant "$fx/stale" "$clean" 'class Clean'
  printf '%s\n' "$clean" > "$WORK/l3.txt"
  printf '%s\n' "$clean" > "$WORK/allow2.txt"
  out="$(run_checks "$fx/stale" "$WORK/l3.txt" "$WORK/allow2.txt")"
  expect_contains "allowlist-stale-clean" "$out" "DI-ALLOWLIST-STALE $clean"

  printf '%s\n' "feature/foo/src/main/java/com/example/Gone.kt" > "$WORK/allow3.txt"
  out="$(run_checks "$fx/stale" "$WORK/l3.txt" "$WORK/allow3.txt")"
  expect_contains "allowlist-stale-untracked" "$out" "DI-ALLOWLIST-STALE feature/foo/src/main/java/com/example/Gone.kt"

  echo "DI-GATE-SELFTEST-OK"
}

cd "$REPO_ROOT"
case "$MODE" in
  check) mode_check ;;
  selftest) mode_selftest ;;
  hits) mode_hits ;;
esac
