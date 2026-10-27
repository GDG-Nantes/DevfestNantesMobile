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
# is missing or not a number reads as 0, so a schema change fails closed. When the summary
# also carries an overall `result`, it must be "Passed": a run that died partway (some
# tests passed, none failed) reports another result and is rejected. Every other value is
# rejected too, including ones Xcode may use for runs that are arguably fine (for example
# a test marked with `withKnownIssue` / `XCTExpectFailure`). Unknown values fail closed on
# purpose: if a legitimate run trips XCRESULT-RESULT-NOT-PASSED, add the exact value the
# token prints to ACCEPTED_RESULTS below, with a selftest case.
#
# Output tokens (stdout, one per line; the first line is always XCRESULT-TESTS unless
# the bundle is missing or unreadable):
#   XCRESULT-TESTS total=<n> passed=<n> failed=<n> skipped=<n>
#   XCRESULT-OK                    every executed test passed, at least one ran (exit 0)
#   XCRESULT-NO-TESTS-EXECUTED     total < 1, or no test passed (all skipped) (exit 1)
#   XCRESULT-TESTS-FAILED          at least one test failed (exit 1)
#   XCRESULT-RESULT-NOT-PASSED result=<value>
#                                  counts look clean but the bundle's own overall result
#                                  is not an accepted value (exit 1)
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
# Environment: none. The summary is always read from the bundle; there is no override
# variable, so nothing in the CI environment can make the assertion pass against a fixture.
# Selftest feeds fixtures to the decision functions directly, in-process.
#
# Usage: bash scripts/xcresult-assert-tests.sh <path-to.xcresult>
#        bash scripts/xcresult-assert-tests.sh selftest

set -euo pipefail

# Byte-wise collation, consistent with the other gates in this directory.
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SELF="${SCRIPT_DIR}/$(basename "${BASH_SOURCE[0]}")"

# Overall `result` values that count as a passing run. Extend only with a value observed
# in a real bundle (copy it from the XCRESULT-RESULT-NOT-PASSED line).
ACCEPTED_RESULTS=("Passed")

if [ "$#" -ne 1 ] || [ -z "${1:-}" ]; then
  echo "Usage: $0 <path-to.xcresult>|selftest" >&2
  exit 1
fi
ARG="$1"

# count <summary-json> <key>: the key as a non-negative integer, 0 when absent or not a number.
count() {
  printf '%s' "$1" | jq -r --arg k "$2" '(.[$k] // 0) | if type == "number" then floor else 0 end'
}

# result_accepted <value>: whether an overall result is in ACCEPTED_RESULTS.
result_accepted() {
  local accepted
  for accepted in "${ACCEPTED_RESULTS[@]}"; do
    [ "$1" = "$accepted" ] && return 0
  done
  return 1
}

# assert_summary <summary-json>: print the token lines and exit with the verdict.
assert_summary() {
  local summary="$1" total passed failed skipped result
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
  # Overall verdict of the run itself, checked last: the zero-test fixture legitimately
  # reports "unknown", which the checks above already turn into NO-TESTS-EXECUTED.
  result="$(printf '%s' "$summary" | jq -r '.result // empty | if type == "string" then . else "" end')"
  if [ -n "$result" ] && ! result_accepted "$result"; then
    echo "XCRESULT-RESULT-NOT-PASSED result=$result"
    exit 1
  fi
  echo "XCRESULT-OK"
}

# check_summary <summary-json>: reject unreadable JSON, otherwise print the verdict and exit.
check_summary() {
  if ! printf '%s' "$1" | jq -e 'type == "object"' >/dev/null 2>&1; then
    echo "XCRESULT-SUMMARY-UNREADABLE"
    exit 1
  fi
  assert_summary "$1"
}

mode_assert() {
  local bundle="$1" summary
  if [ ! -d "$bundle" ]; then
    echo "XCRESULT-MISSING $bundle"
    exit 1
  fi
  summary="$(xcrun xcresulttool get test-results summary --path "$bundle" --compact 2>/dev/null)" || summary=""
  check_summary "$summary"
}

# selftest_case <label> <summary-json-or-empty> <expected-exit> <expected-token>
# Runs the decision logic on a fixture in a subshell (the functions call exit). An empty
# summary means "nonexistent bundle path" (the missing-bundle case).
selftest_case() {
  local label="$1" json="$2" want_rc="$3" want_token="$4" out rc=0
  if [ -n "$json" ]; then
    out="$(check_summary "$json" 2>&1)" || rc=$?
  else
    out="$(mode_assert "$WORK/does-not-exist.xcresult" 2>&1)" || rc=$?
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
  selftest_case "run-died-partway-result-not-passed" \
    '{"totalTestCount":3,"passedTests":2,"failedTests":0,"skippedTests":0,"result":"Failed"}' 1 "XCRESULT-RESULT-NOT-PASSED"
  selftest_case "unrecognised-result-fails-closed" \
    '{"totalTestCount":3,"passedTests":3,"failedTests":0,"skippedTests":0,"result":"Expected Failure"}' 1 "XCRESULT-RESULT-NOT-PASSED"
  selftest_case "result-non-string-ignored" \
    '{"totalTestCount":3,"passedTests":3,"failedTests":0,"skippedTests":0,"result":42}' 0 "XCRESULT-OK"
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
