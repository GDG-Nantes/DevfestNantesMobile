---
phase: 06-ios-test-foundation-and-ci-gates
plan: 01
subsystem: testing
tags: [ios, xcode, swift-testing, test-plan, xctestplan]

requires:
  - phase: 05
    provides: existing iosApp Xcode project and shared iosApp scheme
provides:
  - Hosted Swift Testing target DevFestNantesTests (iOS 17, TEST_HOST = app)
  - Committed test plan iosApp/iosApp.xctestplan wired as the default plan of the shared iosApp scheme
  - iOSApp.isRunningTests guard that keeps the host app inert under XCTest
  - First real iOS test (SessionFilterTests, legacy AgendaSessionFilters payload)
  - Removal of the broken space-named test target and its CocoaPods phase
affects: [06-02, 06-03, phase-08, phase-09, phase-10, phase-11, phase-12]

actuals:
  tokens: 3500
  tasks: 2
  commits: 2
plan_head_before: f757d3f66e0144e182891e65914de318fc606d91
plan_head_after: 2cc496933d9a9e46d892270e5cae510322e26b3c

tech-stack:
  added: [Swift Testing (toolchain), Xcode test plan]
  patterns: [hosted test bundle with under-test guard in the app entry point, scheme -> xctestplan -> target chain]

key-files:
  created:
    - iosApp/iosApp.xctestplan
    - iosApp/DevFestNantesTests/SessionFilterTests.swift
  modified:
    - iosApp/iosApp/iOSApp.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj
    - iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme
  deleted:
    - "iosApp/DevFest NantesTests/DevFest_NantesTests.swift"
    - "iosApp/DevFest NantesTests/MockDevFestNantesStore.swift"

key-decisions:
  - "Under-test detection keys on XCTestConfigurationFilePath env OR NSClassFromString(\"XCTestCase\"), both injected only by the XCTest runner"
  - "Test plan has codeCoverage false and the scheme-level codeCoverageEnabled attribute was removed (plan is the single source of truth)"
  - "New target created with fresh object IDs and the old one deleted in a separate commit (D-12), not an in-place rename"

requirements-completed: [IOSTEST-03]

duration: 25 min
completed: 2026-10-27
status: complete
---

# Phase 6 Plan 01: iOS test target replacement Summary

**Hosted Swift Testing target `DevFestNantesTests` run through a committed `iosApp.xctestplan`, with the host app neutralised under XCTest and the broken space-named target removed.**

## Performance

- **Duration:** 25 min
- **Tasks:** 2 (1 tracer, 1 auto)
- **Files modified:** 5 (plus 2 deleted, 2 created)

## Accomplishments

- New unit-test target `DevFestNantesTests` (bundle id `com.gdgnantes.devfest.iosApp.DevFestNantesTests`, iOS 17.0, Swift 5.0, hosted by "DevFest Nantes") created by direct project surgery with fresh object IDs.
- Shared `iosApp` scheme TestAction now holds a `TestPlanReference` to `container:iosApp.xctestplan` (single target, coverage off); the old `Testables` element and `codeCoverageEnabled` attribute are gone.
- `iOSApp` returns early from `init()` and renders `Color.clear` when `isRunningTests`; outside XCTest the five start-up calls run in the same order and `ContentView` keeps the dark color scheme.
- `SessionFilterTests.decodes_the_legacy_agenda_session_filters_payload` passes through `build-for-testing` + `test-without-building -testPlan iosApp` (.xcresult: at least one test, 0 failures, named test among the passed cases).
- Obsolete target `DevFest NantesTests`, its 2 sources, its CocoaPods "Check Pods Manifest.lock" phase (which exited 1) and its private `SessionFilter.swift` build file removed; project now has exactly two targets and the version-line multiset is back to the pre-phase one.

## Task Commits

1. **Task 1 (tracer): hosted target, test plan, scheme wiring, guard, first test** - `a2759cc` (test)
2. **Task 2: remove obsolete space-named target** - `2cc4969` (chore)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] jq precedence error in Task 1 first `<automated>` check**
- **Found during:** Task 1 verification
- **Issue:** `jq -e '.testTargets | length == 1 and ... and .defaultOptions.codeCoverage == false'` evaluates `.defaultOptions` against the `testTargets` array and errors ("Cannot index array with string").
- **Fix:** Ran the equivalent expression `(.testTargets | length == 1 and .[0].target.name == "DevFestNantesTests") and .defaultOptions.codeCoverage == false`, which returns true. The test plan file itself needed no change.
- **Files modified:** none (verification command only)
- **Commit:** n/a

**2. [Rule 3 - Blocking] Gitignored local files missing in the worktree**
- **Found during:** Task 1 local lane
- **Issue:** The worktree lacks the gitignored `iosApp/iosApp/GoogleService-Info.plist` and `local.properties` (the latter makes the Gradle configuration of the embed-framework build phase fail).
- **Fix:** Created the CI mock `GoogleService-Info.plist` (from `.github/workflows/ios.yml`) and copied `local.properties` from the main checkout. Both are gitignored and were not committed.
- **Files modified:** none tracked
- **Commit:** n/a

### Deletions

- `iosApp/DevFest NantesTests/DevFest_NantesTests.swift` (declared in plan `files_deleted`)
- `iosApp/DevFest NantesTests/MockDevFestNantesStore.swift` (declared in plan `files_deleted`)

No undeclared deletion.

**Total deviations:** 2 auto-fixed (1 verify-command bug, 1 blocking environment setup). **Impact:** none on deliverables.

## Verification Results

- `plutil -lint` OK after both commits; `xcodebuild -list` lists exactly `DevFest Nantes` and `DevFestNantesTests`.
- LOCAL LANE green after Task 1 and again after Task 2 (summary `totalTestCount >= 1`, `failedTests == 0`; named test passed).
- All Task 1 and Task 2 grep/awk acceptance criteria pass (version-line multiset after Task 1: 37 x6, 1.0 x4, 2025.10.00 x2; after Task 2: 37 x4, 1.0 x2, 2025.10.00 x2; start-up call order preserved).
- Assumption A2 (Xcode 26.6 on CI sets `XCTestConfigurationFilePath`) is untested here (local Xcode 27.1); the `NSClassFromString` OR-branch covers it and 06-03 will confirm on CI.

## Known Stubs

None.

## Threat Flags

None.

## Self-Check: PASSED

- iosApp/iosApp.xctestplan, iosApp/DevFestNantesTests/SessionFilterTests.swift: present.
- Commits a2759cc and 2cc4969 are ancestors of HEAD; Task 1 commit lists exactly its 5 files, Task 2 commit lists M project.pbxproj and the 2 planned deletions.
