---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 08
subsystem: testing
tags: [ios, swift-testing, characterization, golden]
requires:
  - phase: 07-04
    provides: neutral agenda snapshot and filter cases
  - phase: 07-07
    provides: D-10 filter statics and sessionsChanged access seam
provides:
  - Swift Testing characterization of the v1.0 iOS agenda grouping, filtering, isATalk, speaker alphabet and venue language
  - iOS goldens (agenda in two zones, filtering, speaker index) for the Kotlin side (07-12, 07-13)
affects: [07-12, 07-13, phase-09]
tech-stack:
  added: []
  patterns: ["golden record mode via TEST_RUNNER_DEVFEST_RECORD_GOLDENS", "DevFest_Nantes.SessionFilter qualification in tests importing shared"]
key-files:
  created:
    - iosApp/DevFestNantesTests/CharacterizationSupport.swift
    - iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift
    - iosApp/DevFestNantesTests/SpeakersVenueCharacterizationTests.swift
    - core/domain/testdata/agenda/golden/ios-agenda-Europe_Paris.json
    - core/domain/testdata/agenda/golden/ios-agenda-Pacific_Auckland.json
    - core/domain/testdata/agenda/golden/ios-filtering.json
    - core/domain/testdata/agenda/golden/ios-speaker-index.json
  modified:
    - iosApp/iosApp.xcodeproj/project.pbxproj
    - scripts/swift-names/shared-h-baseline.txt
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt
    - documentation/DEVELOPMENT.md
key-decisions:
  - "Goldens recorded by the tests themselves in explicit record mode; a default run never writes and fails on an absent golden"
patterns-established:
  - "Hosted tests start Koin through TestHost.start() (IosDependencies.shared.start(), idempotent) before creating a ViewModel"
requirements-completed: [SHARED-07, SHARED-03, SHARED-04]
status: complete
duration: multi-session (interrupted by limits)
completed: 2026-10-29
actuals:
  tokens: 30000
  tasks: 2
  commits: 2
plan_head_before: 0196959d0844eb28ea2b65970652ee50eb786be4
plan_head_after: 1b73bab9a84abe7b9444899e05fb607ed07808c8
---

# Phase 7 Plan 08: iOS characterization Summary

**Swift Testing pins on the recorded snapshot of the unchanged v1.0 iOS code (grouping in Europe/Paris and Pacific/Auckland, 15 filter cases, isATalk, speaker alphabet, venue language), with four iOS goldens recorded by the tests.**

## Accomplishments
- `import shared` works in the test target with no fallback: neither `FRAMEWORK_SEARCH_PATHS` nor `CLANG_MODULES_AUTOLINK` was needed, and no "is implemented in both" warning appeared.
- Default run with goldens absent failed with `golden missing` and wrote nothing (checked with `git status`); record mode wrote the goldens; the following default run is green (19/19 tests, `XCRESULT-OK`).
- Goldens: Paris 17 + 16 slots for the two tabs; Auckland tab 2025-10-17 starts on 2025-10-16T12:00Z (day-1 evening sessions moved over); 15 filter cases; 19 speaker letters.
- swift-names gate: `check` and `selftest` pass; baseline diff is additions only (`ScheduleSlot` referenced name and member block, and the `ClassifySessionUseCase` entries already present in the header).
- No file under `iosApp/iosApp` changed.

## Task Commits
1. Task 1 (grouping, filtering, classification, goldens, baselines): 3c86433
2. Task 2 (speaker alphabet, venue language, docs): 1b73bab

## Deviations from Plan

**1. [Rule 3 - Blocking] `#require` with a module-qualified initializer**
- **Issue:** `#require(DevFest_Nantes.FilterType(rawValue:))` does not compile (the macro parses `DevFest_Nantes` as a module value).
- **Fix:** `guard let ... else { throw TestDataError.unknownName }`, same semantics.
- **Files:** `AgendaCharacterizationTests.swift` (before the first commit).

**2. [Docs] DEVELOPMENT.md sentence updated**
- The old text said tests must not create a ViewModel; it now says to call `IosDependencies.shared.start()` first, as these suites do (Debug app skips Koin under XCTest).

None other: the plan was executed as written.

## Notes
- A failing Swift test run (golden missing or recorded) takes about 10 minutes at the end because Xcode times out collecting simulator diagnostics; green runs are fast.
- `shared-h-baseline.txt` gained 4 names that come from the header already built by earlier plans (07-02); regeneration is mechanical from the header.
- The venue test only checks the row for the run's `Locale.current.languageCode`; other rows are pinned on the Kotlin side.

## Known Stubs
None.

## Self-Check: PASSED
- Files found: three test files, four goldens, SUMMARY.
- Commits 3c86433 and 1b73bab are ancestors of HEAD.
