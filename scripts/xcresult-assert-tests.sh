#!/usr/bin/env bash
# iOS test-result assertion for CI (phase 6, D-05).
#
# `xcodebuild test-without-building` exits 0 and prints "** TEST EXECUTE SUCCEEDED **"
# even when it executed no test at all (target dropped from the test plan, broken
# discovery, a mistyped -only-testing selector). This script reads the .xcresult
# bundle instead and fails the job in those cases. It never trusts xcodebuild's own
# exit code or its stdout.
#
# What it reads: `xcrun xcresulttool get test-results summary --path <bundle> --compact`
# (covers XCTest and Swift Testing; a parameterised Swift Testing test counts as one
# test) and the keys totalTestCount, passedTests, failedTests, skippedTests. A key that
# is missing or not a number reads as 0, so a schema change fails closed.
#
# Output tokens (stdout, one per line; the first line is always XCRESULT-TESTS unless
# the bundle is missing or unreadable):
#   XCRESULT-TESTS total=<n> passed=<n> failed=<n> skipped=<n>
#   XCRESULT-OK                    every executed test passed, at least one ran (exit 0)
#   XCRESULT-NO-TESTS-EXECUTED     total < 1, or no test passed (all skipped) (exit 1)
#   XCRESULT-TESTS-FAILED          at least one test failed (exit 1)
#   XCRESULT-MISSING <path>        the bundle directory does not exist (exit 1)
#   XCRESULT-SUMMARY-UNREADABLE    xcresulttool failed or its output is not JSON (exit 1)
#   XCRESULT-SELFTEST-OK / XCRESULT-SELFTEST-FAILED <case>   selftest mode
#
# No fixed test count is asserted: tests are added every phase, the count is only
# reported (job summary), never compared.
#
# Modes:
#   <path-to.xcresult>  Assert on a real result bundle.
#   selftest            Run the decision logic on fixture summaries (zero tests, all
#                       skipped, a failure, schema drift, unreadable summary, a missing
#                       bundle, a passing run). Prints XCRESULT-SELFTEST-OK on success.
#
# Environment:
#   XCRESULT_SUMMARY_JSON  When set, the summary JSON is read from that file instead of
#                          calling xcresulttool and the bundle directory is not checked.
#                          Used by selftest. Never set it in CI.
#
# Usage: bash scripts/xcresult-assert-tests.sh <path-to.xcresult>
#        bash scripts/xcresult-assert-tests.sh selftest

set -euo pipefail

# Byte-wise collation, consistent with the other gates in this directory.
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SELF="${SCRIPT_DIR}/$(basename "${BASH_SOURCE[0]}")"

if [ "$#" -ne 1 ] || [ -z "${1:-}" ]; then
  echo "Usage: $0 <path-to.xcresult>|selftest" >&2
  exit 1
fi
ARG="$1"

# count <summary-json> <key>: the key as a non-negative integer, 0 when absent or not a number.
count() {
  printf '%s' "$1" | jq -r --arg k "$2" '(.[$k] // 0) | if type == "number" then floor else 0 end'
}

# assert_summary <summary-json>: print the token lines and exit with the verdict.
assert_summary() {
  local summary="$1" total passed failed skipped
  total="$(count "$summary" totalTestCount)"
  passed="$(count "$summary" passedTests)"
  failed="$(count "$summary" failedTests)"
  skipped="$(count "$summary" skippedTests)"
  echo "XCRESULT-TESTS total=$total passed=$passed failed=$failed skipped=$skipped"
  if [ "$total" -lt 1 ]; then
    echo "XCRESULT-NO-TESTS-EXECUTED"
    exit 1
  fi
  if [ "$failed" -gt 0 ]; then
    echo "XCRESULT-TESTS-FAILED"
    exit 1
  fi
  if [ "$passed" -lt 1 ]; then
    echo "XCRESULT-NO-TESTS-EXECUTED"
    exit 1
  fi
  echo "XCRESULT-OK"
}

mode_assert() {
  local bundle="$1" summary
  if [ -n "${XCRESULT_SUMMARY_JSON:-}" ]; then
    summary="$(cat "$XCRESULT_SUMMARY_JSON" 2>/dev/null)" || summary=""
  else
    if [ ! -d "$bundle" ]; then
      echo "XCRESULT-MISSING $bundle"
      exit 1
    fi
    summary="$(xcrun xcresulttool get test-results summary --path "$bundle" --compact 2>/dev/null)" || summary=""
  fi
  if ! printf '%s' "$summary" | jq -e 'type == "object"' >/dev/null 2>&1; then
    echo "XCRESULT-SUMMARY-UNREADABLE"
    exit 1
  fi
  assert_summary "$summary"
}

# selftest_case <label> <summary-json-or-empty> <expected-exit> <expected-token>
# Runs this script on a fixture through the XCRESULT_SUMMARY_JSON override. An empty
# summary means "no override, nonexistent bundle path" (the missing-bundle case).
selftest_case() {
  local label="$1" json="$2" want_rc="$3" want_token="$4" out rc=0
  if [ -n "$json" ]; then
    printf '%s\n' "$json" > "$WORK/summary.json"
    out="$(XCRESULT_SUMMARY_JSON="$WORK/summary.json" bash "$SELF" "$WORK/fixture.xcresult" 2>&1)" || rc=$?
  else
    out="$(env -u XCRESULT_SUMMARY_JSON bash "$SELF" "$WORK/does-not-exist.xcresult" 2>&1)" || rc=$?
  fi
  if [ "$rc" -ne "$want_rc" ] || ! printf '%s\n' "$out" | grep -Eq -- "^${want_token}( |\$)"; then
    echo "XCRESULT-SELFTEST-FAILED $label" >&2
    echo "  expected exit $want_rc and token $want_token, got exit $rc:" >&2
    printf '%s\n' "$out" >&2
    exit 1
  fi
}

mode_selftest() {
  WORK="$(mktemp -d "${TMPDIR:-/tmp}/xcresult-assert.XXXXXX")"
  trap 'rm -rf "$WORK"' EXIT

  selftest_case "passing-run" \
    '{"totalTestCount":3,"passedTests":3,"failedTests":0,"skippedTests":0,"result":"Passed"}' 0 "XCRESULT-OK"
  selftest_case "passing-run-with-some-skipped" \
    '{"totalTestCount":3,"passedTests":2,"failedTests":0,"skippedTests":1}' 0 "XCRESULT-OK"
  selftest_case "zero-tests" \
    '{"totalTestCount":0,"passedTests":0,"failedTests":0,"skippedTests":0,"result":"unknown"}' 1 "XCRESULT-NO-TESTS-EXECUTED"
  selftest_case "all-skipped" \
    '{"totalTestCount":3,"passedTests":0,"failedTests":0,"skippedTests":3}' 1 "XCRESULT-NO-TESTS-EXECUTED"
  selftest_case "one-failure" \
    '{"totalTestCount":2,"passedTests":1,"failedTests":1,"skippedTests":0}' 1 "XCRESULT-TESTS-FAILED"
  selftest_case "everything-failed" \
    '{"totalTestCount":2,"passedTests":0,"failedTests":2,"skippedTests":0}' 1 "XCRESULT-TESTS-FAILED"
  selftest_case "schema-drift-no-count-keys" \
    '{"result":"Passed","title":"Test Summary"}' 1 "XCRESULT-NO-TESTS-EXECUTED"
  selftest_case "schema-drift-non-numeric-counts" \
    '{"totalTestCount":"3","passedTests":"3"}' 1 "XCRESULT-NO-TESTS-EXECUTED"
  selftest_case "unreadable-summary" \
    'not json at all' 1 "XCRESULT-SUMMARY-UNREADABLE"
  selftest_case "missing-bundle" \
    '' 1 "XCRESULT-MISSING"

  echo "XCRESULT-SELFTEST-OK"
}

case "$ARG" in
  selftest) mode_selftest ;;
  *) mode_assert "$ARG" ;;
esac
