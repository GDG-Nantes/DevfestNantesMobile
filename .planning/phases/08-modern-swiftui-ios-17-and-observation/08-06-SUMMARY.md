---
phase: 08-modern-swiftui-ios-17-and-observation
plan: 06
subsystem: ios-ui
tags: [ios, swiftui, observation, swift-testing, performance-tracing]

requires:
  - phase: 08-modern-swiftui-ios-17-and-observation
    provides: "FakeCall (08-03), DataLoadTracing seam, RecordingDataLoadTracing and LiveSpeakersSource (08-05)"
provides:
  - "SpeakerDetailsSource port and LiveSpeakerDetailsSource (lazy store, one-shot async calls)"
  - "SpeakerDetailsViewModel on Observation with load() and no work in init"
  - "FakeSpeakerDetailsSource and SpeakerDetailsViewModelTests (7 tests)"
affects: [08-07 Agenda migration, 08-08 gate, 08-09 parity pass]

plan_head_before: 82c5717966b404bc7ea9a908a47f9d7463415389
plan_head_after: f241c84c52767fd0d680ccb4c6bb1520e6e01c86

actuals:
  tokens: 9000
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "One-shot port with two calls (speaker, sessions) faked by two FakeCall instances"
    - "ViewModel owned by State(initialValue:) in the view init and loaded from .task"

key-files:
  created:
    - iosApp/iosApp/UI/Speakers/SpeakerDetailsSource.swift
    - iosApp/DevFestNantesTests/Fakes/FakeSpeakerDetailsSource.swift
    - iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift
  modified:
    - iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift
    - iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj

key-decisions:
  - "SpeakerDetailsViewModel.init stays a single line so the plan's static init-signature check matches"
  - "getSpeaker and getSpeakerSession both catch CancellationError silently; load() returns after a cancelled speaker call so the sessions call is not made"
  - "An extra test (speaker_is_set_while_the_sessions_are_still_loading) pins that isLoading only turns false once the sessions call returns"

patterns-established:
  - "Sequenced two-call load: fail on the first call does not stop the second (v1.0 parity), cancellation does"

requirements-completed: [IOSUI-02, IOSTEST-01]

coverage:
  - id: D1
    description: "SpeakerDetailsViewModel is @Observable @MainActor behind a one-shot port, init only stores, load() runs in the view's .task"
    requirement: IOSUI-02
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift#init_starts_no_load"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift#load_sets_the_speaker_then_the_sessions_and_stops_loading"
        status: pass
    human_judgment: false
  - id: D2
    description: "Live adapter over the mocked Kotlin store loads a speaker and their sessions and records one (speaker_details_load, graphql) trace"
    requirement: IOSTEST-01
    verification:
      - kind: integration
        ref: "iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift#live_source_loads_a_mocked_speaker_and_their_sessions"
        status: pass
    human_judgment: false
  - id: D3
    description: "Error handling and silent cancellation keep v1.0 semantics"
    requirement: IOSUI-02
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift#cancellation_during_the_speaker_call_is_silent"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift#speaker_error_stops_loading_and_still_requests_the_sessions"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift#sessions_error_stops_loading_and_keeps_the_speaker"
        status: pass
    human_judgment: false
  - id: D4
    description: "On-device behavior of the .task reload when the detail reappears (D-10) is unchanged for the user"
    verification: []
    human_judgment: true
    rationale: "Visual parity of the reload on reappear is confirmed in the 08-09 parity pass; no unit test observes the view"

duration: 17min
completed: 2026-11-05
status: complete
---

# Phase 08 Plan 06: SpeakerDetails Observation Summary

**SpeakerDetails now runs on Observation behind a SpeakerDetailsSource port: the ViewModel only stores its arguments, loads speaker then sessions from the view's .task, traces the speaker call as speaker_details_load/graphql, and treats cancellation silently.**

## Performance

- **Duration:** 17 min
- **Started:** 2026-11-05T10:17:00Z
- **Completed:** 2026-11-05T10:34:39Z
- **Tasks:** 2
- **Files modified:** 6 (3 created, 3 modified)

## Accomplishments
- `SpeakerDetailsSource` port plus `LiveSpeakerDetailsSource` (store resolved lazily inside the methods, only place that calls `asyncFunction`).
- `SpeakerDetailsViewModel` is `@Observable @MainActor final class` with no base class, no Combine, no main-queue hops, no unstructured task; `load()` runs `getSpeaker` then `getSpeakerSession` as v1.0 did; the speaker call goes through `DataLoadTracing`, the sessions call stays untraced.
- `SpeakerDetails` owns the ViewModel with `State(initialValue:)` and loads in `.task { await viewModel.load() }`.
- Tests: tracer over the mocked Kotlin store plus six behavior tests on `FakeSpeakerDetailsSource` (two `FakeCall`s).
- Full suite: `XCRESULT-TESTS total=46 passed=46 failed=0 skipped=0`, `XCRESULT-OK`; `SWIFT-NAMES-OK`; `KMPNC-VERSION-OK 1.0.6`.

## Task Commits

1. **Task 1 (tracer): SpeakerDetails end to end on Observation** - `b0fe74a` (feat)
2. **Task 2: source fake and behavior tests** - `f241c84` (test)

**Plan metadata:** committed separately (docs).

## Files Created/Modified
- `iosApp/iosApp/UI/Speakers/SpeakerDetailsSource.swift` - port and live adapter
- `iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift` - Observation ViewModel with `load()`
- `iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift` - `State(initialValue:)` ownership and `.task` load
- `iosApp/DevFestNantesTests/Fakes/FakeSpeakerDetailsSource.swift` - fake with `speakerCall`, `sessionsCall`, requested ids
- `iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift` - 7 tests
- `iosApp/iosApp.xcodeproj/project.pbxproj` - four entries per new Swift file

## Decisions Made
- Single-line `init(speakerId:source:tracing:)` so the plan's static grep matches.
- Cancellation returns silently from both load steps; `load()` also checks `Task.isCancelled` between them.
- Added one test beyond the plan's six (speaker visible while sessions pending, `isLoading` still true) to pin the "loading stops only after sessions" parity; total new tests 7, suite 46 (plan floor 45).

## Deviations from Plan

None - plan executed exactly as written (one additional test, within the plan's total floor). TDD note: per the task text the behavior tests were written against Task 1's ViewModel and passed on first run, so no RED commit exists for Task 2; this follows the plan's own instruction ("watch them pass against Task 1's ViewModel").

## Issues Encountered
- The worktree sandbox guard refuses shell constructs it cannot verify; the plan's `<automated>` commands were run as separate plain commands with the same checks (static greps, `xcodebuild build-for-testing` then `test-without-building`, `xcresult-assert-tests.sh`, suite-name check, swift-names and KMP-NC gates) instead of as one chained line.
- `swift-names-gate.sh check` prints a diff section for the known stale `shared-h-baseline.txt` but ends with `SWIFT-NAMES-OK`; no baseline was regenerated.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
Every ViewModel except Agenda is on Observation; 08-07 migrates Agenda (and the `AgendaCellView`/`AgendaDetailView` call site in `SpeakerDetails`).

## Self-Check: PASSED

- Files exist: SpeakerDetailsSource.swift, SpeakerDetailsViewModel.swift, FakeSpeakerDetailsSource.swift, SpeakerDetailsViewModelTests.swift
- Commits `b0fe74a` and `f241c84` are on the branch.
- Full suite 46/46 passed, both speaker suites among the passed suites.

---
*Phase: 08-modern-swiftui-ios-17-and-observation*
*Completed: 2026-11-05*
