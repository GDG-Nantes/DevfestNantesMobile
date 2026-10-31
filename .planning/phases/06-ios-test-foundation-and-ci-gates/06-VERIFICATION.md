---
phase: 06-ios-test-foundation-and-ci-gates
verified: 2026-10-31T19:20:00Z
status: passed
score: 4/4 must-haves verified
covered_files:
  - .github/workflows/ios.yml
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-01-PLAN.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-01-SUMMARY.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-02-PLAN.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-02-SUMMARY.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-03-PLAN.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-03-SUMMARY.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-04-PLAN.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-04-SUMMARY.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-05-PLAN.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-05-SUMMARY.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-06-PLAN.md
  - .planning/phases/06-ios-test-foundation-and-ci-gates/06-06-SUMMARY.md
  - iosApp/DevFestNantesTests/AgendaDateParsingTests.swift
  - iosApp/DevFestNantesTests/SessionFilterTests.swift
  - iosApp/iosApp.xcodeproj/project.pbxproj
  - iosApp/iosApp.xctestplan
  - iosApp/iosApp/iOSApp.swift
  - scripts/kmp-nc-version-gate.sh
  - scripts/swift-names-gate.sh
  - scripts/xcresult-assert-tests.sh
covered_digest: "v3:sha256:9ec140401c783016f33668a30c50166025b427855705ffd26c6c6ec57d99a23c"
behavior_unverified: 0
overrides_applied: 0
re_verification:
  previous_status: passed
  previous_score: 4/4
  gaps_closed: []
  gaps_remaining: []
  regressions: []
---

# Phase 6: iOS Test Foundation and CI Gates Verification Report

**Phase Goal:** Every PR actually runs Swift tests on iOS CI and the Kotlin/Swift interop gates are active, so that every subsequent iOS rework has a reliable signal
**Verified:** 2026-10-27
**Status:** passed
**Re-verification:** Yes, after code-review fixes WR-01/WR-02/WR-03 changed `iOSApp.swift`, `AgendaDateParsingTests.swift` and `scripts/xcresult-assert-tests.sh` (commits `fcef4d3`, `43c0456`, `094717d`, `4195159`)

## Re-verification Scope

**2026-10-31 (post-Phase 7):** Phase 7 (`5896f573`) touched two covered files, both additively: `ios.yml` adds `:core:domain:iosSimulatorArm64Test` to the KMP iOS test step, and the pbxproj registers three new test files (`CharacterizationSupport.swift`, `AgendaCharacterizationTests.swift`, `SpeakersVenueCharacterizationTests.swift`) in `DevFestNantesTests`. No Phase 6 step, gate or target was removed or changed. Re-ran on HEAD: `xcodebuild ... -testPlan iosApp test` (iPhone 18 Pro) -> `** TEST SUCCEEDED **`, `xcresult-assert-tests.sh` -> `XCRESULT-TESTS total=19 passed=19 failed=0`, `swift-names-gate.sh check` -> `SWIFT-NAMES-OK`, `selftest` -> `SWIFT-GATE-SELFTEST-OK`, `kmp-nc-version-gate.sh` -> `KMPNC-VERSION-OK 1.0.6`. The digest was regenerated with `verification.fingerprint`. All 4 truths still hold.

**2026-10-27:** The previous report (14:48/15:00) was written before the review fixes. The diff `aac1c81..HEAD` over `iosApp`, `.github`, `scripts`, `gradle`, `shared`, `core` touches exactly four files: the three named above plus `scripts/README.md`. `ios.yml`, the pbxproj, the test plan, the scheme and the other two gate scripts are unchanged since the last real CI run. Those three files were re-read in full and re-executed; the unchanged artifacts got a regression sanity check (existence, wiring, self-tests).

## Goal Achievement

### Observable Truths (ROADMAP Success Criteria)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | On every PR, iOS CI runs build-for-testing then test-without-building through a `.xctestplan`; a working Swift Testing target (replacing `DevFest NantesTests`) runs at least one real test green | VERIFIED | `ios.yml` triggers on `pull_request` (main), `push` (main), `workflow_dispatch`. Steps "Build app and tests for testing" (`-testPlan iosApp ... build-for-testing`) and "Run Swift tests" (`... test-without-building`) are present. `iosApp.xctestplan` targets `DevFestNantesTests` (id `48271312CBCC986AA4EA1DCC`); the shared scheme has a `TestPlanReference`. Old target is gone (`DevFest NantesTests`: 0 matches in pbxproj, `DevFestViewModel`: 0 matches in `iosApp`). **I ran the real lane on current HEAD** (`xcodebuild ... -testPlan iosApp -configuration Debug -destination iPhone 17 test`): `** TEST SUCCEEDED **`, suites `AgendaDateParsingTests` and `SessionFilterTests` passed, including the renamed `matches_the_default_iso8601_formatter_for_fractional_seconds`. `iOSApp.swift` (with `#if DEBUG` test-host switch) compiles and the hosted run works. |
| 2 | Job fails when the `.xcresult` counts zero executed tests; the iOS cache key includes the Xcode version | VERIFIED | `scripts/xcresult-assert-tests.sh` re-read in full. Reads the summary only from the bundle (`xcrun xcresulttool get test-results summary`); the `XCRESULT_SUMMARY_JSON` override is gone (0 occurrences in scripts, `.github`, documentation). Exits 1 on total<1, passed<1, failures, missing bundle, unreadable or schema-drifted summary, and now on a string `result` other than `Passed`. `bash scripts/xcresult-assert-tests.sh selftest` -> `XCRESULT-SELFTEST-OK` (13 cases incl. the 2 new ones). Against the real bundle from my run: `XCRESULT-TESTS total=11 passed=11 failed=0 skipped=0` then `XCRESULT-OK`, so the new result check does not reject a genuine pass. Wired in `ios.yml` "Assert tests executed" (`shell: bash`, so `pipefail` applies to the `| tee`). DerivedData cache key contains `xcode-${{ steps.xcode.outputs.version }}`, step placed after Xcode selection. |
| 3 | `scripts/swift-names-gate.sh` restored and runs in CI; a change to the Swift name of an exported Kotlin type fails the job | VERIFIED | Script (14.9K) and `scripts/swift-names/` baselines (shared-h-baseline, members-baseline, referenced-names, names-exclude) present and unchanged since the last CI run. `ios.yml` "Swift names gate" runs `check` and `selftest` against the header just linked. `bash scripts/swift-names-gate.sh selftest` -> `SWIFT-GATE-SELFTEST-OK` locally. CI run 40501276582 logged `SWIFT-NAMES-OK`. |
| 4 | KMP-NativeCoroutines at 1.0.6 on SPM side as on Gradle side, CI gate fails on divergence, iOS app behaves as in v1.0 | VERIFIED | pbxproj `KMP-NativeCoroutines` reference: `kind = exactVersion; version = "1.0.6"`. No `ALPHA` string left in `iosApp`, `.github`, `scripts`, `gradle`. `bash scripts/kmp-nc-version-gate.sh` -> `KMPNC-VERSION-OK 1.0.6`; `selftest` -> `KMPNC-SELFTEST-OK`. Gate is the first step of `ios.yml`. v1.0 parity: blocking human smoke (06-06) was approved by the user over 8 flows with screenshots (recorded in 06-06-SUMMARY.md); the review fixes do not touch that surface (Release behavior of `iOSApp.swift` is unchanged: the switch is a constant `false` there). |

**Score:** 4/4 truths verified (0 present, behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `iosApp/DevFestNantesTests/SessionFilterTests.swift` | Swift Testing suite on legacy filter format | VERIFIED | Unchanged; passes in my run |
| `iosApp/DevFestNantesTests/AgendaDateParsingTests.swift` | Tests of `AgendaViewModel.parseDate` | VERIFIED | 5 `@Test`s, real `#expect` assertions on the production type; fractional-second test now derives its expectation from `ISO8601DateFormatter()` (weaker than the old strict pin by design, still asserts fallback-to-now or the parsed instant) |
| `iosApp/iosApp/iOSApp.swift` | Hermetic test host without weakening the shipped app | VERIFIED | `isRunningTests` is `#if DEBUG` env-var check, constant `false` in Release; `init()` and `body` both branch on it |
| `iosApp/iosApp.xctestplan` | Test plan with the test target | VERIFIED | Target id present in pbxproj and scheme |
| `scripts/xcresult-assert-tests.sh` | Zero-test failure | VERIFIED | See truth 2 |
| `scripts/swift-names-gate.sh` + baselines | Restored gate | VERIFIED | See truth 3 |
| `scripts/kmp-nc-version-gate.sh` | Version skew gate | VERIFIED | See truth 4 |
| `.github/workflows/ios.yml` | Test lane, gates, Xcode-aware cache | VERIFIED | All steps present, unchanged since green CI |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `ios.yml` | `iosApp.xctestplan` | `-testPlan iosApp` on both xcodebuild phases | WIRED | Scheme `TestPlanReference` present |
| `ios.yml` | `xcresult-assert-tests.sh` | step on `$RUNNER_TEMP/ios-tests.xcresult` | WIRED | Same path as `-resultBundlePath` |
| `ios.yml` | `swift-names-gate.sh` | check + selftest with `SWIFT_GATE_HEADER` | WIRED | |
| `ios.yml` | `kmp-nc-version-gate.sh` | first step | WIRED | |
| pbxproj SPM pin | `libs.versions.toml` | gate string equality | WIRED | Both 1.0.6 |
| `iOSApp.swift` | test host | `isRunningTests` short-circuits Firebase/Koin in Debug | WIRED | CI builds `-configuration Debug`, so the switch is active where tests run |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Full Swift test lane on HEAD | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -configuration Debug -destination 'platform=iOS Simulator,id=<iPhone 17>' test` | `** TEST SUCCEEDED **`, 11 tests, 2 suites | PASS |
| Zero-test assertion on that real bundle | `bash scripts/xcresult-assert-tests.sh <bundle>` | `XCRESULT-TESTS total=11 passed=11 failed=0 skipped=0`, `XCRESULT-OK`, exit 0 | PASS |
| Zero-test assertion self-test | `bash scripts/xcresult-assert-tests.sh selftest` | `XCRESULT-SELFTEST-OK` | PASS |
| Version gate + self-test | `bash scripts/kmp-nc-version-gate.sh` / `selftest` | `KMPNC-VERSION-OK 1.0.6` / `KMPNC-SELFTEST-OK` | PASS |
| Swift names gate self-test | `bash scripts/swift-names-gate.sh selftest` | `SWIFT-GATE-SELFTEST-OK` | PASS |

### Probe Execution

SKIPPED: the phase declares no `probe-*.sh` scripts; the gate self-tests above serve as the runnable checks.

### Requirements Coverage

| Requirement | Source Plans | Description | Status | Evidence |
|-------------|--------------|-------------|--------|----------|
| IOSTEST-03 | 06-01, 06-02, 06-03 | CI runs `xcodebuild test` via test plan, fails if no test run, Xcode version in cache key | SATISFIED | Truths 1 and 2. The clause "including the package tests" is explicitly deferred to Phase 11 (REQUIREMENTS.md note; no packages exist yet) |
| IOSTEST-04 | 06-04 | `swift-names-gate.sh` restored in `scripts/` and run in CI | SATISFIED | Truth 3 |
| CONC-03 | 06-05, 06-06 | KMP-NativeCoroutines same version (1.0.6) on SPM and Gradle, CI gate | SATISFIED | Truth 4 |

No orphaned requirements: REQUIREMENTS.md maps exactly IOSTEST-03, IOSTEST-04, CONC-03 to Phase 6, and all three are `[x]` / `Complete` there (the earlier bookkeeping warning is resolved).

### Anti-Patterns Found

No TBD/FIXME/XXX debt markers in the files changed by the phase (the three `XXXXXX` hits are `mktemp` templates). No stubs: the assertions in both test files exercise production types.

Review findings: WR-01, WR-02, WR-03 are `fixed` in `06-REVIEW-DISPOSITION.md` and the fixes are confirmed in code (see truths 1 and 2). IN-01..IN-05 remain `open` (info only, do not falsify any must-have).

### Advisory Notes (not blocking, not human-verification gates)

1. **CI has not run on the post-fix HEAD.** The branch is 9 commits ahead of `origin`; the last green iOS CI run (40501276582) is on `aac1c81`, before the fixes. I closed the gap locally by running the same test lane and assertion script on HEAD (table above), but a push or `workflow_dispatch` run will give the first CI confirmation. The fixes touch no workflow, Gradle or Kotlin file, so the remaining CI steps are unaffected.
2. **Policy choices recorded by the fixer (06-REVIEW-FIX.md):** (a) any non-`Passed` overall `result` fails the gate (fail-closed; extend `ACCEPTED_RESULTS` if a legitimate run, e.g. with `withKnownIssue`, ever trips it); (b) the fractional-seconds test is deliberately less strict. These are maintainer decisions about future flexibility; neither weakens a ROADMAP success criterion.
3. Stale names in planning artifacts: `deferred-items.md`, `06-02-PLAN.md`, `06-02-SUMMARY.md` still mention the old test name `falls_back_to_now_for_fractional_seconds`. Documentation drift only.
4. Release-configuration compile of the `#else` branch in `iOSApp.swift` was not exercised by anyone (trivial constant); the CI lane is Debug.

### Human Verification Required

None outstanding. The only human-judgment item (v1.0 parity after the 1.0.6 bump) was a blocking checkpoint approved by the user in 06-06, with screenshot evidence.

### Gaps Summary

No gaps. All four ROADMAP success criteria hold on the current code, including after the review fixes: the three changed files compile, the real test lane passes 11/11 on HEAD, and the hardened assertion script accepts a genuine pass and rejects every degenerate fixture.

---

_Verified: 2026-10-27_
_Verifier: Claude (gsd-verifier)_
