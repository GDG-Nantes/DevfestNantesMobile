---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 11
subsystem: ios-agenda
tags: [ios, agenda, viewmodel, continuous-flow, tdd, swift-testing]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: "09-10 agenda ViewModel on the shared combined flow (observeAgenda, FakeStream, RecordingDataLoadTracing)"
provides:
  - "D-01 to D-05 lifetime and failure behavior of the continuous agenda pinned on the scripted stream through the real KotlinStreamBridge"
  - "Agenda loading state ends on a Kotlin-side cancellation before the first emission (Open Question 3)"
  - "FakeStream.emitThenFail and .cancelUpstream scripts; RecordingDataLoadTracing.completedTraceCount and waitUntilCompleted()"
affects: [09-12, phase-09-verification, WR-01 follow-up for the other ViewModels]

estimate_pairing: "plan estimate 35000 tokens, 1 task"
actuals:
  tokens: 3000
  tasks: 1
  commits: 2
plan_head_before: a8c20ab1f469ae2255b22a3ece540f9a23e3e546
plan_head_after: 49294f56212613f4f34139ac03f174f614f026ac

tech-stack:
  added: []
  patterns:
    - "Observation-based waitUntil helper (withObservationTracking inside a continuation), no sleeps"
    - "Handshake signals (AsyncStream) for trace completion, same style as FakeStream"

key-files:
  created: []
  modified:
    - iosApp/iosApp/Agenda/AgendaViewModel.swift
    - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
    - iosApp/DevFestNantesTests/Fakes/FakeStream.swift
    - iosApp/DevFestNantesTests/Fakes/RecordingDataLoadTracing.swift

key-decisions:
  - "Open Question 3 decided as planned: observeAgenda() ends the loading state, silently, when the CancellationError arrives while the Swift task is alive and nothing was emitted; WR-01 stays open for the other ViewModels"
  - "hasEmitted local added to observeAgenda() (09-10 had omitted it because nothing read it); it is read by the new guard"

patterns-established:
  - "A script with an explicit Ending (stay open, finish, fail) in FakeStream.deliver instead of a Bool"

requirements-completed: [IOSDATA-03]

coverage:
  - id: D1
    description: "Trace rules D-02: the AGENDA_LOAD trace ends at the first emission while the stream stays open, and each subscription records one trace and one completion"
    requirement: IOSDATA-03
    verification:
      - kind: unit
        ref: "AgendaViewModelTests#trace_ends_at_the_first_emission_while_the_stream_stays_open"
        status: pass
      - kind: unit
        ref: "AgendaViewModelTests#each_subscription_records_one_trace"
        status: pass
    human_judgment: false
  - id: D2
    description: "D-03: on resubscribe the previous content stays and isLoading stays false"
    requirement: IOSDATA-03
    verification:
      - kind: unit
        ref: "AgendaViewModelTests#resubscribing_keeps_the_previous_content_and_no_spinner"
        status: pass
    human_judgment: false
  - id: D3
    description: "D-04: a failure after an emission keeps the content and stops; a cancelled collection is silent; an upstream cancellation before the first emission ends the loading state"
    requirement: IOSDATA-03
    verification:
      - kind: unit
        ref: "AgendaViewModelTests#error_after_an_emission_keeps_the_content_and_stops"
        status: pass
      - kind: unit
        ref: "AgendaViewModelTests#cancelled_collection_is_silent_and_keeps_the_content"
        status: pass
      - kind: unit
        ref: "AgendaViewModelTests#upstream_cancellation_before_the_first_emission_stops_loading_silently"
        status: pass
      - kind: unit
        ref: "AgendaViewModelTests#cancelled_agenda_load_keeps_the_spinner"
        status: pass
    human_judgment: false
  - id: D4
    description: "Edge cases: equal consecutive emissions leave the content unchanged; days without slots render an empty list"
    requirement: IOSDATA-03
    verification:
      - kind: unit
        ref: "AgendaViewModelTests#equal_consecutive_emissions_leave_the_content_unchanged"
        status: pass
      - kind: unit
        ref: "AgendaViewModelTests#days_without_slots_show_an_empty_list"
        status: pass
    human_judgment: false
  - id: D5
    description: "D-05: the bookmark mirror and write-through stay correct after the collection is cancelled"
    requirement: IOSDATA-03
    verification:
      - kind: unit
        ref: "AgendaViewModelTests#mirror_stays_correct_while_the_collection_is_cancelled"
        status: pass
    human_judgment: false

duration: 14min
completed: 2026-11-07
status: complete
---

# Phase 9 Plan 11: Agenda lifetime and failure rules Summary

**Nine tests pin the continuous agenda's trace, resubscribe, failure and cancellation rules (D-01 to D-05) through the real KotlinStreamBridge, plus a silent guard that ends the loading state when the Kotlin side cancels before the first emission.**

## Performance

- **Duration:** about 14 min (including two full Xcode builds and a full gate run)
- **Tasks:** 1 (tracer, TDD)
- **Files modified:** 4

## Accomplishments

- Nine new `AgendaViewModelTests` cover D-02 (trace ends at the first emission, one trace per subscription), D-03 (resubscribe keeps content, no spinner), D-04 (failure after an emission, silent cancellation), Open Question 3 (upstream cancellation), the two IOSDATA-03 edges (equal consecutive emissions, days without slots) and D-05 (mirror stays correct after the collection is cancelled).
- `observeAgenda()` now ends the loading state, with no log, when a `CancellationError` arrives while the Swift task is alive and nothing was emitted. This restores the v1.0 parity Phase 8 removed (WR-01) for the agenda; D-04 silence still holds, and a Swift-side cancellation still keeps the spinner (`cancelled_agenda_load_keeps_the_spinner` stays green).
- `FakeStream` gained `.emitThenFail` and `.cancelUpstream`; `RecordingDataLoadTracing` gained `completedTraceCount` and `waitUntilCompleted()`, built on an `AsyncStream` handshake (no sleeps, no polling).

## TDD Gate Compliance

- **RED** (`22b4ecc4`, `test(09-11)`): fakes and the nine tests. The suite ran 82 tests: 81 passed, 1 failed. Read with `xcresulttool` from the xcresult: the failing test is `AgendaViewModelTests/upstream_cancellation_before_the_first_emission_stops_loading_silently()`, on the planned assertion `Expectation failed: viewModel.isLoading == false; viewModel.isLoading → true`. Semantic assessment: the target test executed, the `.cancelUpstream` script ended the stream with a `CancellationError` while the task was alive, and the guard was absent, so the spinner stayed up. That is the planned reason, not a compile, setup or fixture fault. `gsd_run check tdd-red-evidence` does not read xcresult bundles, so the evidence was inspected directly (per the brief).
- The other eight new tests pass at RED by design: they pin behavior the 09-10 code already has (D-02 to D-05 and the two edges), so they are characterization tests of the existing flow; only Open Question 3 was new behavior.
- **GREEN** (`49294f56`, `feat(09-11)`): the guard in `observeAgenda()`. Full suite 82 of 82 passed; all eleven agenda lifetime tests (nine new, `cancelled_agenda_load_keeps_the_spinner`, `loads_only_the_first_emission_and_stops_the_upstream`) confirmed present and Passed in the xcresult.
- **REFACTOR:** none needed.

## Task Commits

1. **Task 1 (tracer): lifetime and failure behavior D-01 to D-05**
   - RED: `22b4ecc4` (test)
   - GREEN: `49294f56` (feat)

**Plan metadata:** the SUMMARY commit follows this file.

## Files Modified

- `iosApp/iosApp/Agenda/AgendaViewModel.swift` - `hasEmitted` local; the `CancellationError` catch ends loading when the task is alive and nothing was emitted
- `iosApp/DevFestNantesTests/AgendaViewModelTests.swift` - nine tests, the private `waitUntil` Observation helper, `import Observation`
- `iosApp/DevFestNantesTests/Fakes/FakeStream.swift` - `emitThenFail`, `cancelUpstream`, internal `Ending` enum replacing the `thenFinish` Bool in `deliver`
- `iosApp/DevFestNantesTests/Fakes/RecordingDataLoadTracing.swift` - `completedTraceCount`, `waitUntilCompleted()`

## Decisions Made

- Open Question 3 decided as the plan states (guard included, agenda only). The `!hasEmitted` condition is redundant for correctness (`isLoading` is already false after an emission) but kept, as the plan words it, so the intent reads in the code. WR-01 stays open for the other ViewModels.
- `AgendaView.swift` was not touched: no empty state, error banner, retry or spinner path was added (prohibition honored; neither commit lists the view).

## Deviations from Plan

None - plan executed exactly as written, apart from two reporting points that are not code deviations:

- **Test count floor:** the plan's `-ge 81` floor was stale (the suite was at 73 before this plan, not 72). The true count after this plan is **82** (73 + 9), and the gate passes against the stated floor of 81.
- **`hasEmitted`:** 09-10 omitted this local; it is added here because the guard reads it.

## Issues Encountered

None. The Robolectric "Failed to destroy temp directory" noise at the end of the Gradle run is pre-existing shutdown noise; the Gradle invocation exited 0.

## Execution notes

- The worktree command guard refuses compound shell that names git or uses computed arguments, so each `<automated>` block was run as its component steps with identical checks and outcomes: `build-for-testing` and `test-without-building` as two scripts (iPhone 17, UDID `F59F5246-2D70-49BD-B6CF-82DD9A67BF48`, `-collect-test-diagnostics never`, `-parallel-testing-enabled NO`), then `bash scripts/xcresult-assert-tests.sh` (printed `XCRESULT-TESTS total=82 passed=82 failed=0 skipped=0` and `XCRESULT-OK`), then the Task-creation grep (no match), the `AgendaViewModel()` count (1), the swift-names, observation, KMP-NC and DI gates (`SWIFT-NAMES-OK`, `SWIFT-OBS-GATE-OK`, `KMPNC-VERSION-OK 1.0.6`, `DI-GATE-OK`) and `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` (exit 0).
- The plan commit ledger file could not be written under `.git/worktrees/` from this worktree (the guard refuses the shared path), so `plan_head_before` is the dispatch base SHA and the commit count was measured with `rev-list --count` against it (2 code commits).
- No xcodebuild or Gradle process is left running.

## Known Stubs

None.

## Threat Flags

None. T-09-16 (denial of service on a failing or stalled agenda stream) is mitigated and pinned by tests: no path leaves the blocking overlay up while the view is alive.

## Next Phase Readiness

The agenda lifetime and failure rules are pinned. WR-01 (loading state on a Kotlin-side cancellation) remains open for the Speakers, Venue, About and details ViewModels, which this phase does not rewrite.

## Self-Check: PASSED

- Modified files exist; commits `22b4ecc4` and `49294f56` are ancestors of HEAD.
- Acceptance criteria: nine tests and `cancelled_agenda_load_keeps_the_spinner` pass; full suite `XCRESULT-OK` with 82 tests (at least 81); neither task commit lists `AgendaView.swift`.
