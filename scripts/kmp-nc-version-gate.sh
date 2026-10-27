#!/usr/bin/env bash
# KMP-NativeCoroutines version-skew gate (phase 6, D-10 / D-12, CONC-03).
#
# The KMP-NativeCoroutines README requires the Gradle plugin and the Swift package to
# be on the same version. Gradle is the source of truth (kmpNativeCoroutines in
# gradle/libs.versions.toml, bumped by Dependabot); the Swift side is an exact-version
# Swift Package Manager requirement in iosApp/iosApp.xcodeproj/project.pbxproj, which
# Dependabot does not watch. This gate fails when the two differ, so a Gradle-only bump
# cannot reach main with a skewed iOS app. Pure text check: runs anywhere (no Xcode, no
# Gradle), it is the first step of .github/workflows/ios.yml.
#
# What it compares (exact string equality, so "v1.0.6" against "1.0.6" is a skew):
#   Gradle side  the quoted value of the `kmpNativeCoroutines = "<v>"` line.
#   SPM side     the `kind` and `version` of the XCRemoteSwiftPackageReference block
#                whose repositoryURL contains KMP-NativeCoroutines. Other package
#                references (e.g. firebase-ios-sdk) are never read.
#
# Modes:
#   check (default)  Prints one token line per failure and exits 1 if any was printed,
#                    else prints KMPNC-VERSION-OK <version> and exits 0.
#   selftest         Runs check on fixture files written under a temp dir (env overrides
#                    below) and proves each decision. Prints KMPNC-SELFTEST-OK, or
#                    KMPNC-SELFTEST-FAILED <case> and exits 1.
#
# Tokens (stdout):
#   KMPNC-VERSION-OK <v>                        both sides equal, SPM requirement exact
#   KMPNC-VERSION-SKEW gradle=<v> spm=<v>       the two versions differ
#   KMPNC-SPM-NOT-EXACT kind=<k> (expected exactVersion)
#                                               the SPM requirement is a range, branch
#                                               or revision, not an exact version
#   KMPNC-GRADLE-VERSION-MISSING <path>         the Gradle version could not be read
#   KMPNC-SPM-PIN-MISSING <path>                the SPM pin could not be read
#
# Environment (fixture-only overrides, used by selftest; CI sets neither):
#   KMP_NC_TOML     version catalog (default gradle/libs.versions.toml)
#   KMP_NC_PBXPROJ  Xcode project   (default iosApp/iosApp.xcodeproj/project.pbxproj)
#
# Extending: the SPM side is a list of reader functions (SPM_READERS). Each prints
# "<kind> <version>" for its source and returns non-zero when the pin is missing, so a
# Package.swift reader (phase 11) is one more function in the list; the tokens and the
# exit codes do not change.
#
# Usage: bash scripts/kmp-nc-version-gate.sh [check|selftest]
#
# After bumping kmpNativeCoroutines (see documentation/DEVELOPMENT.md): edit the pbxproj
# pin to the same version by hand (Xcode > Package Dependencies > KMP-NativeCoroutines >
# Exact Version) in the same commit, then re-run this gate.

set -euo pipefail

# Byte-wise collation and character handling independent of the caller's locale.
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SELF="${SCRIPT_DIR}/$(basename "${BASH_SOURCE[0]}")"

MODE="${1:-check}"
if [ "$MODE" != "check" ] && [ "$MODE" != "selftest" ]; then
  echo "Usage: $0 [check|selftest]" >&2
  exit 1
fi

TOML_FILE="${KMP_NC_TOML:-${REPO_ROOT}/gradle/libs.versions.toml}"
PBXPROJ_FILE="${KMP_NC_PBXPROJ:-${REPO_ROOT}/iosApp/iosApp.xcodeproj/project.pbxproj}"

# gradle_version: prints the kmpNativeCoroutines version, nothing when absent.
gradle_version() {
  [ -r "$TOML_FILE" ] || return 0
  sed -n -E 's/^kmpNativeCoroutines[[:space:]]*=[[:space:]]*"([^"]*)".*$/\1/p' "$TOML_FILE" | head -n 1
}

# pbxproj_block: prints the XCRemoteSwiftPackageReference block of KMP-NativeCoroutines.
# Blocks are delimited by brace depth, not by indentation.
pbxproj_block() {
  awk '
    !inblock && /XCRemoteSwiftPackageReference "[^"]*" \*\/ = \{[[:space:]]*$/ {
      inblock = 1; depth = 0; buf = ""
    }
    inblock {
      buf = buf $0 "\n"
      line = $0
      depth += gsub(/\{/, "{", line)
      line = $0
      depth -= gsub(/\}/, "}", line)
      if (depth <= 0) {
        if (buf ~ /repositoryURL = "[^"]*KMP-NativeCoroutines[^"]*"/) { printf "%s", buf; exit }
        inblock = 0
      }
    }
  ' "$PBXPROJ_FILE"
}

# spm_read_pbxproj: prints "<kind> <version>" of the pbxproj pin. Returns 1 when the
# pin cannot be read. A non-exact requirement has no `version` key: the version part
# is then "-".
spm_read_pbxproj() {
  [ -r "$PBXPROJ_FILE" ] || return 1
  local block kind version
  block="$(pbxproj_block)"
  [ -n "$block" ] || return 1
  kind="$(printf '%s\n' "$block" | sed -n -E 's/^[[:space:]]*kind = ([A-Za-z]+);.*$/\1/p' | head -n 1)"
  version="$(printf '%s\n' "$block" | sed -n -E 's/^[[:space:]]*version = "([^"]*)";.*$/\1/p' | head -n 1)"
  [ -n "$kind" ] || return 1
  printf '%s %s\n' "$kind" "${version:--}"
}

# SPM sources, one reader function each (see "Extending" above).
SPM_READERS="spm_read_pbxproj"

mode_check() {
  local failed=0 gradle reader spm kind version

  gradle="$(gradle_version)"
  if [ -z "$gradle" ]; then
    echo "KMPNC-GRADLE-VERSION-MISSING ${TOML_FILE}"
    failed=1
  fi

  for reader in $SPM_READERS; do
    if ! spm="$("$reader")"; then
      echo "KMPNC-SPM-PIN-MISSING ${PBXPROJ_FILE}"
      failed=1
      continue
    fi
    kind="${spm%% *}"
    version="${spm#* }"
    if [ "$kind" != "exactVersion" ]; then
      echo "KMPNC-SPM-NOT-EXACT kind=${kind} (expected exactVersion)"
      failed=1
      continue
    fi
    if [ -n "$gradle" ] && [ "$gradle" != "$version" ]; then
      echo "KMPNC-VERSION-SKEW gradle=${gradle} spm=${version}"
      failed=1
    fi
  done

  if [ "$failed" -ne 0 ]; then
    exit 1
  fi
  echo "KMPNC-VERSION-OK ${gradle}"
}

# --- selftest -----------------------------------------------------------------

# write_toml <path> <version or "-" for no key>
write_toml() {
  {
    echo '[versions]'
    echo 'kotlin = "2.4.20"'
    if [ "$2" != "-" ]; then
      printf 'kmpNativeCoroutines = "%s"\n' "$2"
    fi
    echo 'koin = "4.2.2"'
  } > "$1"
}

# write_pbxproj <path> <kmp-requirement or "none"> <firebase-first yes|no>
# kmp-requirement is the body of the `requirement = { ... };` object of the
# KMP-NativeCoroutines reference, tab-indented by the caller.
write_pbxproj() {
  local path="$1" kmp="$2" firebase_first="$3"
  local firebase kmp_block
  firebase='		003EC8D02DF8D7E1000E5689 /* XCRemoteSwiftPackageReference "firebase-ios-sdk" */ = {
			isa = XCRemoteSwiftPackageReference;
			repositoryURL = "https://github.com/firebase/firebase-ios-sdk";
			requirement = {
				kind = upToNextMajorVersion;
				minimumVersion = 11.14.0;
			};
		};'
  kmp_block=""
  if [ "$kmp" != "none" ]; then
    kmp_block="		003EC8D72DF8D84B000E5689 /* XCRemoteSwiftPackageReference \"KMP-NativeCoroutines\" */ = {
			isa = XCRemoteSwiftPackageReference;
			repositoryURL = \"https://github.com/rickclephas/KMP-NativeCoroutines.git\";
			requirement = {
${kmp}
			};
		};"
  fi
  {
    echo '/* Begin XCRemoteSwiftPackageReference section */'
    if [ "$firebase_first" = "yes" ]; then
      printf '%s\n' "$firebase"
      [ -z "$kmp_block" ] || printf '%s\n' "$kmp_block"
    else
      [ -z "$kmp_block" ] || printf '%s\n' "$kmp_block"
      printf '%s\n' "$firebase"
    fi
    echo '/* End XCRemoteSwiftPackageReference section */'
  } > "$path"
}

# run_case <name> <toml> <pbxproj> <expected exit> <expected output fragment or "">
run_case() {
  local name="$1" toml="$2" pbx="$3" want_rc="$4" want_out="$5" out rc=0
  out="$(KMP_NC_TOML="$toml" KMP_NC_PBXPROJ="$pbx" bash "$SELF" check 2>&1)" || rc=$?
  if [ "$rc" -ne "$want_rc" ]; then
    echo "KMPNC-SELFTEST-FAILED ${name} (exit ${rc}, expected ${want_rc})"
    printf '%s\n' "$out" >&2
    exit 1
  fi
  if [ -n "$want_out" ] && ! printf '%s\n' "$out" | grep -Fxq -- "$want_out"; then
    echo "KMPNC-SELFTEST-FAILED ${name} (expected line: ${want_out})"
    printf '%s\n' "$out" >&2
    exit 1
  fi
}

mode_selftest() {
  # Global on purpose: the EXIT trap runs after this function's locals are gone.
  work="$(mktemp -d "${TMPDIR:-/tmp}/kmpnc-gate.XXXXXX")"
  trap 'rm -rf "$work"' EXIT

  local exact106 exact105 exactV106 range
  exact106='				kind = exactVersion;
				version = "1.0.6";'
  exact105='				kind = exactVersion;
				version = "1.0.5";'
  exactV106='				kind = exactVersion;
				version = "v1.0.6";'
  range='				kind = upToNextMajorVersion;
				minimumVersion = 1.0.6;'

  write_toml "$work/ok.toml" "1.0.6"

  # 1. Equal exact pins -> OK (both orders of the two package references: the
  #    non-exact firebase reference must never be read).
  write_pbxproj "$work/exact.pbxproj" "$exact106" no
  run_case "equal-exact" "$work/ok.toml" "$work/exact.pbxproj" 0 "KMPNC-VERSION-OK 1.0.6"
  write_pbxproj "$work/exact-fb-first.pbxproj" "$exact106" yes
  run_case "firebase-non-exact-ignored" "$work/ok.toml" "$work/exact-fb-first.pbxproj" 0 "KMPNC-VERSION-OK 1.0.6"

  # 2. Different versions -> SKEW.
  write_pbxproj "$work/skew.pbxproj" "$exact105" no
  run_case "skew" "$work/ok.toml" "$work/skew.pbxproj" 1 "KMPNC-VERSION-SKEW gradle=1.0.6 spm=1.0.5"

  # 3. Range requirement -> NOT-EXACT even when the lower bound equals the Gradle version.
  write_pbxproj "$work/range.pbxproj" "$range" no
  run_case "not-exact" "$work/ok.toml" "$work/range.pbxproj" 1 "KMPNC-SPM-NOT-EXACT kind=upToNextMajorVersion (expected exactVersion)"

  # 4. Exact string equality: a v-prefix is a skew.
  write_pbxproj "$work/vprefix.pbxproj" "$exactV106" no
  run_case "v-prefix-is-skew" "$work/ok.toml" "$work/vprefix.pbxproj" 1 "KMPNC-VERSION-SKEW gradle=1.0.6 spm=v1.0.6"

  # 5. Missing sources.
  write_toml "$work/nokey.toml" "-"
  run_case "gradle-missing" "$work/nokey.toml" "$work/exact.pbxproj" 1 "KMPNC-GRADLE-VERSION-MISSING $work/nokey.toml"
  run_case "gradle-file-missing" "$work/absent.toml" "$work/exact.pbxproj" 1 "KMPNC-GRADLE-VERSION-MISSING $work/absent.toml"
  write_pbxproj "$work/nopin.pbxproj" none no
  run_case "spm-missing" "$work/ok.toml" "$work/nopin.pbxproj" 1 "KMPNC-SPM-PIN-MISSING $work/nopin.pbxproj"
  run_case "spm-file-missing" "$work/ok.toml" "$work/absent.pbxproj" 1 "KMPNC-SPM-PIN-MISSING $work/absent.pbxproj"

  echo "KMPNC-SELFTEST-OK"
}

cd "$REPO_ROOT"
case "$MODE" in
  check) mode_check ;;
  selftest) mode_selftest ;;
esac
