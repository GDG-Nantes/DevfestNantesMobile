---
phase: 06-ios-test-foundation-and-ci-gates
verified: 2026-10-27T15:00:00Z
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
covered_digest: "v3:sha256:83644328fcecb4e20df7a6b6e73c3eb2fa148c7da07a9d96b55dec0802e718ae"
behavior_unverified: 0
overrides_applied: 0
---

# Phase 6: iOS Test Foundation and CI Gates Verification Report

**Phase Goal:** Every PR actually runs Swift tests on iOS CI and the Kotlin/Swift interop gates are active, so that every subsequent iOS rework has a reliable signal
**Verified:** 2026-10-27
**Status:** passed
**Re-verification:** No, initial verification

## Goal Achievement

### Observable Truths (ROADMAP Success Criteria)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | On every PR, iOS CI runs build-for-testing then test-without-building through a `.xctestplan`; a working Swift Testing target (replacing the old `DevFest NantesTests`) runs at least one real test green | VERIFIED | `ios.yml` triggers on `pull_request` (main), `push` (main) and `workflow_dispatch`. Steps "Build app and tests for testing" (`-testPlan iosApp ... build-for-testing`) and "Run Swift tests" (`... test-without-building`) are present. `iosApp/iosApp.xctestplan` lists target `DevFestNantesTests` (id `48271312CBCC986AA4EA1DCC`, which exists in pbxproj and is referenced by the shared scheme `TestPlans`). Old target `DevFest NantesTests` and directory are gone and `DevFestViewModel` is referenced nowhere in `iosApp`. New target has 2 suites with real assertions (`SessionFilterTests`, `AgendaDateParsingTests`, Swift Testing, against production types). CI run 40501276582 (head `aac1c81`, success): "Suite AgendaDateParsingTests passed", "Suite SessionFilterTests passed", `XCRESULT-TESTS total=11 passed=11 failed=0`. No iosApp/.github/scripts/shared/core/gradle change between `aac1c81` and HEAD. |
| 2 | Job fails when the `.xcresult` counts zero executed tests; the iOS cache key includes the Xcode version | VERIFIED | `scripts/xcresult-assert-tests.sh` reads `xcresulttool get test-results summary`, exits 1 on total<1, passed<1, failures, missing bundle, unreadable or schema-drifted summary (missing keys read as 0). Wired in `ios.yml` "Assert tests executed". I ran `selftest`: `XCRESULT-SELFTEST-OK` (zero-tests, all-skipped, failure, drift, missing-bundle cases all behave). CI log shows selftest OK and `XCRESULT-OK`. DerivedData cache key is `...-xcode-${{ steps.xcode.outputs.version }}-deriveddata-...` with the step placed after Xcode selection (version-build string, e.g. 26.6-build). |
| 3 | `scripts/swift-names-gate.sh` restored and runs in CI; a change to the Swift name of an exported Kotlin type fails the job | VERIFIED | `scripts/swift-names-gate.sh` (14.9K) plus baselines in `scripts/swift-names/` (shared-h-baseline, members-baseline, referenced-names, names-exclude). `ios.yml` "Swift names gate" runs `check` and `selftest` against the header of the framework just linked. Check emits SWIFT-NAME-MISSING / SWIFT-TYPE-COLLISION / SWIFT-MEMBERS-CHANGED / SWIFT-NAME-COLLISION and exits non-zero. CI run 40501276582 log: `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK` (selftest proves doctored headers are caught). Not re-executed locally (requires a linked framework); CI evidence inspected directly with `gh run view --log`. |
| 4 | KMP-NativeCoroutines at 1.0.6 on SPM side as on Gradle side, CI gate fails on divergence, iOS app behaves as in v1.0 | VERIFIED | `gradle/libs.versions.toml`: `kmpNativeCoroutines = "1.0.6"`. pbxproj `XCRemoteSwiftPackageReference "KMP-NativeCoroutines"`: `kind = exactVersion; version = "1.0.6"`. `Package.resolved` pins 1.0.6 (rev 54a45fb). No `1.0.0-ALPHA` string remains in iosApp, `.github`, scripts or gradle. I ran `bash scripts/kmp-nc-version-gate.sh` -> `KMPNC-VERSION-OK 1.0.6` and `selftest` -> `KMPNC-SELFTEST-OK`. Gate is the first step of `ios.yml` and CI log confirms both tokens. Behavior parity: blocking human smoke (06-06) approved by the user over 8 flows with 9 screenshots; I viewed `03-favorites.png` (real agenda, starred sessions, tab bar rendered). Human approval is already recorded, so no open human item. |

**Score:** 4/4 truths verified (0 present, behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `iosApp/DevFestNantesTests/SessionFilterTests.swift` | Swift Testing suite, legacy `AgendaSessionFilters` format | VERIFIED | 6 tests incl. parameterised round-trip, real decode/encode assertions |
| `iosApp/DevFestNantesTests/AgendaDateParsingTests.swift` | Tests of extracted `AgendaViewModel.parseDate` | VERIFIED | 5 tests; pins current fractional-second fallback |
| `iosApp/iosApp.xctestplan` | Test plan with the test target | VERIFIED | References target id present in pbxproj and scheme |
| `scripts/xcresult-assert-tests.sh` | Zero-test failure | VERIFIED | Selftest passes locally and in CI |
| `scripts/swift-names-gate.sh` + `scripts/swift-names/*` | Restored gate and baselines | VERIFIED | Present, wired, CI tokens observed |
| `scripts/kmp-nc-version-gate.sh` | Version skew gate | VERIFIED | Passes locally and in CI |
| `.github/workflows/ios.yml` | Test lane, gates, Xcode-aware cache | VERIFIED | All steps present, see above |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `ios.yml` | `iosApp.xctestplan` | `-testPlan iosApp` on build-for-testing and test-without-building | WIRED | Scheme `iosApp` has `TestPlanReference container:iosApp.xctestplan` |
| `ios.yml` | `xcresult-assert-tests.sh` | "Assert tests executed" step on `$RUNNER_TEMP/ios-tests.xcresult` | WIRED | Same bundle path used as `-resultBundlePath` |
| `ios.yml` | `swift-names-gate.sh` | check + selftest, `SWIFT_GATE_HEADER` set to just-built header | WIRED | |
| `ios.yml` | `kmp-nc-version-gate.sh` | first step | WIRED | |
| pbxproj SPM pin | `libs.versions.toml` | gate string equality | WIRED | Both 1.0.6 |
| `iOSApp.swift` | test host | `isRunningTests` short-circuits Firebase/Koin | WIRED | Needed for hermetic app-hosted tests (see WR-01 below) |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Version gate | `bash scripts/kmp-nc-version-gate.sh` | `KMPNC-VERSION-OK 1.0.6` | PASS |
| Version gate self-test | `bash scripts/kmp-nc-version-gate.sh selftest` | `KMPNC-SELFTEST-OK` | PASS |
| Zero-test assertion self-test | `bash scripts/xcresult-assert-tests.sh selftest` | `XCRESULT-SELFTEST-OK` | PASS |
| Real iOS CI run on branch HEAD sources | `gh run view 40501276582 --log` | success; all tokens above; 11/11 tests | PASS |

### Requirements Coverage

| Requirement | Source Plans | Description | Status | Evidence |
|-------------|--------------|-------------|--------|----------|
| IOSTEST-03 | 06-01, 06-02, 06-03 | CI runs `xcodebuild test` via test plan, fails if no test run, Xcode version in cache key | SATISFIED | Truths 1 and 2. Note: the clause "including the package tests" is explicitly deferred to Phase 11 (REQUIREMENTS.md line 127; no packages exist yet) |
| IOSTEST-04 | 06-04 | `swift-names-gate.sh` restored in `scripts/` and run in CI | SATISFIED | Truth 3 |
| CONC-03 | 06-05, 06-06 | KMP-NativeCoroutines same version (1.0.6) on SPM and Gradle, CI gate | SATISFIED | Truth 4 |

No orphaned requirements: REQUIREMENTS.md maps exactly IOSTEST-03, IOSTEST-04, CONC-03 to Phase 6.

**Bookkeeping warning:** `.planning/REQUIREMENTS.md` still shows IOSTEST-03 and IOSTEST-04 as `[ ]` / `Pending` (CONC-03 is already `[x]` / `Complete`). The code satisfies them; update the checkboxes and traceability rows when closing the phase.

### Anti-Patterns Found

No TBD/FIXME/XXX debt markers or stubs in files changed by the phase. Open advisory code-review findings (0 critical, 3 warnings, 5 info; all disposition `open`) do not falsify any must-have:

| Finding | Severity | Impact on goal |
|---------|----------|----------------|
| WR-01 `isRunningTests` branch compiled into Release builds (`iOSApp.swift`) | Warning | Could blank the real app if XCTest is ever injected; not triggered today. Recommend `#if DEBUG` guard before release |
| WR-02 `XCRESULT_SUMMARY_JSON` env var bypasses the bundle read | Warning | CI does not set it (verified in `ios.yml`); hardening only |
| WR-03 floating Xcode/simulator runtime vs a test pinning formatter behavior | Warning | CI green on Xcode 26.6 / iPhone 17; potential future flake |
| IN-01..IN-05 | Info | Coverage off in test plan, hand-enumerated `FilterType` cases, AGENTS.md stale module reference, silent gate failures, `simctl boot || true` |

Other noted debt (`deferred-items.md`): fractional-second start dates fall back to now (pinned by a test, parity); Xcode GUI Run Script needs an arm64 JDK (developer environment).

### Human Verification Required

None outstanding. The only human-judgment item (v1.0 parity after the 1.0.6 bump) was a blocking checkpoint approved by the user in 06-06, with screenshot evidence.

### Gaps Summary

No gaps. All four ROADMAP success criteria are backed by code present in the repository, wired into `ios.yml`, locally re-executable self-tests, and real green CI runs on unchanged sources. Notes: the CI runs seen were `workflow_dispatch` runs on the feature branch (no PR opened yet); the workflow declares `pull_request` and `push` triggers on `main`, which satisfies "every PR". "Executed 0 tests" in the XCTest log line is legacy XCTest only; the `.xcresult` summary (which the gate reads) counts 11 Swift Testing tests.

---

_Verified: 2026-10-27_
_Verifier: Claude (gsd-verifier)_
