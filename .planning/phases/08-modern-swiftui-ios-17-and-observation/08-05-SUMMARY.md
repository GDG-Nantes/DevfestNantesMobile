---
phase: 08-modern-swiftui-ios-17-and-observation
plan: 05
subsystem: ios
tags: [ios, swiftui, observation, swift-testing, performance-tracing]

requires:
  - phase: 08-modern-swiftui-ios-17-and-observation
    provides: "KotlinStreamBridge and FakeStream (08-02), FakeCall (08-03), CI evidence of the bridge shapes (08-04)"
provides:
  - "SpeakersViewModel on Observation (@Observable @MainActor final class) owned by SpeakersView through @State"
  - "SpeakersSource port with LiveSpeakersSource over the Task-free KotlinStreamBridge"
  - "DataLoadTracing seam (PerformanceMonitoring conforms) and RecordingDataLoadTracing fake, reusable by 08-06 and 08-07"
  - "Silent cancellation in PerformanceMonitoring.trackDataLoad (info log, no error object, nothing sent to Crashlytics)"
affects: [08-06, 08-07, 08-08]

tech-stack:
  added: []
  patterns:
    - "Tracing seam: ViewModels take tracing: DataLoadTracing = PerformanceMonitoring.shared; tests inject a recording fake"
    - "Cancellation is checked explicitly after the bridge loop (the bridge ends silently), caught as CancellationError and never logged by the ViewModel"

key-files:
  created:
    - iosApp/iosApp/Performance/DataLoadTracing.swift
    - iosApp/iosApp/UI/Speakers/SpeakersSource.swift
    - iosApp/DevFestNantesTests/Fakes/RecordingDataLoadTracing.swift
    - iosApp/DevFestNantesTests/Fakes/FakeSpeakersSource.swift
    - iosApp/DevFestNantesTests/SpeakersViewModelTests.swift
  modified:
    - iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift
    - iosApp/iosApp/UI/Speakers/SpeakersView.swift
    - iosApp/iosApp/Performance/PerformanceMonitoring.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj
    - documentation/ARCHITECTURE.md

key-decisions:
  - "The DataLoadTracing protocol uses the real label operation: (not block:) so PerformanceMonitoring conforms with an empty extension"
  - "The cancellation clause in trackDataLoad stops the trace with the same attributes as the failure path and logs info without the error object; trace names and attribute keys are untouched"
  - "Behavior tests build Speaker values directly instead of using TestData.speakers(), so they need no Koin/golden host start"

patterns-established:
  - "Recording fakes are lock-guarded @unchecked Sendable classes with an Equatable record struct"

requirements-completed: [IOSUI-02, IOSTEST-01]

actuals:
  tokens: 3500
  tasks: 2
  commits: 2
plan_head_before: 6f4d786fae49f53cb86756a26c029b5c8149d56b
plan_head_after: 9f8a0ad7f057ece250ca16aeca16ee7f028eaeab

duration: 40 min
completed: 2026-11-05
status: complete
---

# Phase 8 Plan 05: Speakers on Observation with a tracing seam Summary

SpeakersViewModel moves to `@Observable` behind a `SpeakersSource` port with a `DataLoadTracing` seam, and `PerformanceMonitoring.trackDataLoad` now logs cancelled loads at info level so they never reach Crashlytics.

## Accomplishments

- `SpeakersViewModel` is an `@Observable @MainActor final class` with no base class, no Combine, no main-queue hop and no unstructured task; `SpeakersView` owns it with `@State` and still loads in `.task`. `getAlphabets()`, `speakers(for:)`, the settable `speakersContent` and `SpeakersViewModel()` default construction are unchanged, so `speaker_alphabet_matches_the_golden` still passes.
- `SpeakersSource` / `LiveSpeakersSource`: the only adapter code, handing the store's `speakers` native flow to `KotlinStreamBridge` and resolving `IosDependencies.shared.store()` lazily inside the method.
- `DataLoadTracing` protocol (`trackDataLoad(traceName:dataSource:operation:)`) with an empty `extension PerformanceMonitoring: DataLoadTracing {}`; the ViewModel keeps trace `speakers_load` with data source `graphql`.
- `trackDataLoad` has a `catch let cancellation as CancellationError` clause: it stops the trace (`duration_ms`, `success` false, `error_type`), logs `"Cancelled data load trace: <name>"` at info level with no error object, and rethrows. Other errors keep the v1.0 path.
- Seven Swift Testing tests in `SpeakersViewModelTests`: the tracer over the mocked store plus first-emission-only, stream error, empty first emission, cancelled first load, cancelled reload keeps previous speakers, init does not subscribe.
- `documentation/ARCHITECTURE.md` iOS App section documents `DataLoadTracing` and info-level cancellation.

## Verification

- Task 1 (targeted): `XCRESULT-TESTS total=3 passed=3 failed=0 skipped=0`, `XCRESULT-OK`; `SpeakersViewModelTests` and `SpeakersVenueCharacterizationTests` among the passed suites.
- Task 2 (plan-final, full suite via build-for-testing then test-without-building, parallel testing disabled, iPhone 17 / iOS 27.0 simulator):
  `XCRESULT-TESTS total=39 passed=39 failed=0 skipped=0` then `XCRESULT-OK` (32 before the plan plus 7 Speakers tests).
- `SWIFT-NAMES-OK`, `KMPNC-VERSION-OK 1.0.6`.
- Acceptance: the `PerformanceMonitoring.swift` diff (`git diff -U0`) only adds the cancellation clause (+11 lines, no deletions, no trace name, attribute key or class declaration change).

## Deviations from Plan

None - plan executed exactly as written. The Task 1 and Task 2 automated blocks were run as component steps because the worktree command guard refuses compound shell; each component was run and passed.

## Issues Encountered

None. Only pre-existing deprecation warnings appeared (`NavigationLink(isActive:)` in `SpeakersView.swift`, `languageCode` in a test).

## Known Stubs

None.

## Threat Flags

None. T-08-12 (cancelled traces to Crashlytics) and T-08-13 (trace names) are mitigated as planned; the tracer test asserts `("speakers_load", "graphql")`.

## Next Phase Readiness

Ready for 08-06 (SpeakerDetails): `DataLoadTracing`, `RecordingDataLoadTracing` and the cancellation handling in the tracing layer are in place. 08-07 (Agenda) can reuse them too.

## Self-Check: PASSED

- All created files exist on disk; commits `5befa86` (Task 1) and `9f8a0ad` (Task 2) are on the branch; the measured commit count since `plan_head_before` is 2.
