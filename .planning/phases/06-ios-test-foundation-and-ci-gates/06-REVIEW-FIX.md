---
phase: 06-ios-test-foundation-and-ci-gates
fixed_at: 2026-10-27T15:10:00Z
review_path: .planning/phases/06-ios-test-foundation-and-ci-gates/06-REVIEW.md
iteration: 1
findings_in_scope: 3
fixed: 3
skipped: 0
status: all_fixed
---

# Phase 6: Code Review Fix Report

**Fixed at:** 2026-10-27
**Source review:** .planning/phases/06-ios-test-foundation-and-ci-gates/06-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 3 (fix_scope `critical_warning`: 0 Critical, 3 Warning)
- Fixed: 3
- Skipped: 0
- Not in scope (Info, left open): IN-01 to IN-05

Work was done in an isolated worktree on a temporary branch and fast-forwarded into
`feature/reno_phase_6`; the worktree, temp branch and recovery sentinel were removed afterwards.

## Fixed Issues

### WR-01: Test-host detection ships in release builds and can blank the real app

**Files modified:** `iosApp/iosApp/iOSApp.swift`, `documentation/DEVELOPMENT.md`
**Commits:** 43c0456 (code), e1c8ef7 (docs)
**Applied fix:** `isRunningTests` is wrapped in `#if DEBUG` (`false` constant otherwise), so Release and
App Store builds can never take the render-nothing path. The `NSClassFromString("XCTestCase")`
fallback was removed; detection relies only on `XCTestConfigurationFilePath`. I verified empirically
that the hosted Swift Testing run still sets that variable (see Verification). `DEVELOPMENT.md` now
states the switch is Debug-only and that a future UI-test target must not rely on it.
**Status note:** logic change; fixed, but the Release-configuration compile was not exercised (see below).

### WR-02: `xcresult-assert-tests.sh` can be bypassed by an ambient env var and ignores the bundle result

**Files modified:** `scripts/xcresult-assert-tests.sh`, `scripts/README.md`
**Commits:** fcef4d3 (script), 8e92938 (README)
**Applied fix:** Went slightly further than the suggested private variable: the `XCRESULT_SUMMARY_JSON`
override is removed entirely. The summary is now always read from the bundle, and `selftest` calls the
decision functions (`check_summary`, `mode_assert`) in-process in subshells, so no environment
variable can substitute a fixture. `assert_summary` additionally fails with `XCRESULT-TESTS-FAILED`
when the summary's `result` is a string other than `Passed`; it runs after the zero-test, failed and
no-pass checks so the `"unknown"` zero-test fixture still maps to `XCRESULT-NO-TESTS-EXECUTED`. A
non-string `result` is ignored (fail-open only for that odd field; the count checks still apply).
Two selftest cases were added (`run-died-partway-result-not-passed`, `result-non-string-ignored`).
**Status note:** logic change; fixed, requires human verification that treating any non-`Passed` result
as a failure is the intended policy (for example a future run with only expected failures).

### WR-03: CI runs on a floating Xcode and simulator runtime while one test pins OS formatter behaviour

**Files modified:** `iosApp/DevFestNantesTests/AgendaDateParsingTests.swift`
**Commit:** 094717d
**Applied fix:** Took the "make the test robust" option, not the pinning option. The test was renamed
`matches_the_default_iso8601_formatter_for_fractional_seconds` and derives its expectation from
`ISO8601DateFormatter().date(from:)`: if the platform rejects fractional seconds it still asserts the
fall-back to now; if a newer runtime accepts them it asserts the parsed instant. The known parse gap
stays documented in the test comment and in `deferred-items.md`. I did not pin Xcode or the simulator
runtime in `ios.yml`: floating on the latest installed Xcode is the deliberate Phase 6 CI policy and
changing it is a maintainer decision. Note that `deferred-items.md`, `06-02-PLAN.md` and
`06-02-SUMMARY.md` still mention the old test name `falls_back_to_now_for_fractional_seconds`; these
planning artifacts were not edited.
**Status note:** the new test is weaker than the old strict pin by design; fixed, requires human
verification that this trade-off is acceptable.

## Skipped Issues

None. Info findings IN-01 to IN-05 were out of scope for `critical_warning` and remain open in
`06-REVIEW-DISPOSITION.md`.

## Verification

Gates were run in the isolated worktree (a copy of the tree with the untracked `GoogleService-Info.plist`
and `local.properties` copied in, neither committed), not the main checkout; the commits are now on
`feature/reno_phase_6`, but the xcresult was produced from the worktree and was deleted with it.

- `bash -n scripts/xcresult-assert-tests.sh` and `bash scripts/xcresult-assert-tests.sh selftest`:
  `XCRESULT-SELFTEST-OK` (including the 2 new cases). Also confirmed an ambient
  `XCRESULT_SUMMARY_JSON=/dev/null` is now ignored (`XCRESULT-MISSING` for a nonexistent bundle).
- `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -configuration Debug
  -destination 'platform=iOS Simulator,id=<iPhone 17>' test` after WR-01 and WR-03 edits:
  `** TEST SUCCEEDED **`, 11 tests in 2 suites passed, including the renamed test. The first attempt
  failed only because `local.properties` was missing from the worktree (environment, not code).
- `scripts/xcresult-assert-tests.sh` against that real bundle: `XCRESULT-TESTS total=11 passed=11
  failed=0 skipped=0` then `XCRESULT-OK`; the real summary reports `"result":"Passed"`, so the new
  result check does not reject a genuine passing run.
- Not run: a Release-configuration build (the `#else` branch of WR-01 is a trivial constant but was not
  compiled), the full CI command list (`detekt`, `lint`, Gradle tests, other gate scripts), and the
  GitHub Actions workflow itself. No Kotlin or `ios.yml` changes were made, so those gates are
  unaffected by these edits.
- Whether the tests actually ran with the Firebase/Koin start-up skipped could not be directly
  observed; the run passed with the env-var-only check, which is consistent with detection working.

---

_Fixed: 2026-10-27_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
