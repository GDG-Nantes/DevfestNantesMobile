---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 07
subsystem: ios-agenda
tags: [ios, agenda, scroll, swift-testing, shared-grouping]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: "GroupSessionsUseCase and LocalDateDays exported to Swift (09-02), TestData snapshot loading (Phase 7)"
provides:
  - "AgendaDays single-source conference dates (day1, day2, all)"
  - "AgendaView.scrollTarget(slotDates:now:calendar:) pure static scroll-target selection"
  - "TestData.schedules(zoneId:) helper grouping the snapshot through the shared use case"
  - "AgendaScrollTargetTests: five D-11 tests, both days, injected now, Paris calendar"
affects: [09-10, 09-14]

actuals:
  tokens: 5000
  tasks: 1
  commits: 2
plan_head_before: 3f32d740d438dfa867aea6910f6040ef1714c94e
plan_head_after: d4390ef1dbd86e84639a976de47f4f214b627439

tech-stack:
  added: []
  patterns:
    - "Pure static function on the View for logic that stays in Swift, tested with injected now and explicit Europe/Paris calendar"
    - "Test slot dates taken from the shared grouping of the recorded snapshot"

key-files:
  created:
    - iosApp/DevFestNantesTests/AgendaScrollTargetTests.swift
  modified:
    - iosApp/iosApp/Model/Data/AgendaContent.swift
    - iosApp/iosApp/Agenda/AgendaView.swift
    - iosApp/DevFestNantesTests/CharacterizationSupport.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt

key-decisions:
  - "scrollTarget moves verbatim (sort by minute of day, keep the last not after now, break on the first later one); no fix for filtered lists (D-11 strict parity)"
  - "Baselines regenerated in the RED commit, the commit that first references DaySchedule, GroupSessionsUseCase, Kotlinx_datetimeTimeZone and LocalDateDays from Swift; the GREEN commit adds no new shared name"

patterns-established:
  - "RED for a pure-function extraction: a compiling stub returning nil, so the failing tests are real assertion failures and not load faults"

requirements-completed: [IOSDATA-03]

coverage:
  - id: D1
    description: "Scroll-target selection is a pure static function, tested on the snapshot for both days with an injected now and a Paris calendar"
    requirement: IOSDATA-03
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaScrollTargetTests.swift#AgendaScrollTargetTests (5 tests)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Conference dates have a single source (AgendaDays) read by the day picker, the initial day and the scroll guard"
    requirement: IOSDATA-03
    verification:
      - kind: other
        ref: "grep -nE '2025-10-1[67]' iosApp/iosApp/Agenda/AgendaView.swift prints nothing"
        status: pass
    human_judgment: false
  - id: D3
    description: "Behavior preserved: the app scrolls to the same slot as before on the legacy data path"
    verification: []
    human_judgment: true
    rationale: "The move is verbatim and unit-tested, but the in-app scroll on a device clock is not asserted by any test; no shifted-clock UAT per D-11"

duration: 25min
completed: 2026-11-07
status: complete
---

# Phase 9 Plan 07: Agenda scroll target and day constants Summary

**Scroll-to-current-slot extracted as the pure `AgendaView.scrollTarget(slotDates:now:calendar:)`, proven on the recorded snapshot for both days, with the conference dates single-sourced in `AgendaDays`**

## Performance

- **Duration:** 25 min
- **Tasks:** 1 (tracer, TDD)
- **Files modified:** 7 (1 created)

## Accomplishments

- `AgendaDays` (`day1`, `day2`, `all`) replaces the eight date literals of `AgendaView.swift`; `LocalDateDays` takes `AgendaDays.all`.
- `scrollTarget` holds the selection moved verbatim out of `handleInitialScrollIfNeeded`, which keeps the `isLoading` guard, the `currentDay == day && firstAppear` guard, `scrollToSectionID` and `firstAppear = false`.
- Five tests on the snapshot: before the first slot, exactly on a slot, between slots, after the last slot, second day. Full Swift suite: 69 tests passed (64 before plus 5; the plan assumed 63), above the required 68.

## Task Commits

1. **Task 1 (tracer), RED:** `dae5546b` test(09-07): failing tests, `AgendaDays`, nil stub, `TestData.schedules(zoneId:)`, pbxproj entries, regenerated baselines
2. **Task 1 (tracer), GREEN:** `d4390ef1` feat(09-07): real `scrollTarget`, handler wiring, literals replaced by `AgendaDays`

**Plan metadata:** committed with this SUMMARY (docs: complete plan).

## TDD Gate Compliance

- **RED:** `AgendaScrollTargetTests` ran 5 tests, 1 passed and 4 failed (xcresult: passed 1, failed 4, total 5). `gsd_run check tdd-red-evidence` returned `RED_EVIDENCE_OK` (format swift-testing, target `now_exactly_on_a_slot_targets_that_slot()`). Semantic assessment: the stub returns nil, so the four tests expecting a slot fail on their planned equality assertions; `now_before_the_first_slot_has_no_target` passes by construction since nil is its expected value. No compile, load or fixture fault. The `<not evaluated>` annotation on the failures is how swift-testing prints a `try` expression and does not indicate a thrown error.
- **GREEN:** same suite, all 5 passed; full suite `XCRESULT-OK`, total 69, passed 69, `AgendaScrollTargetTests` among the passed suites.
- **REFACTOR:** none.

## Decisions Made

- A3 settled: `Kotlinx_datetimeTimeZone.companion.of(zoneId:)` compiles as spelled in RESEARCH; 09-10 can use it.
- The nil stub lives only in the RED commit; GREEN replaced it, so no stub remains.

## Deviations from Plan

None - plan executed exactly as written. The plan has one task, delivered as the RED and GREEN commits that the TDD protocol requires.

## Execution notes

- The worktree command guard refuses compound shell naming git or computed arguments, so each `<automated>` block ran as its component steps with identical checks: pbxproj lint and registration greps, `AgendaDays` and date-literal greps, the `-only-testing` suite run (RED, 5 tests), `build-for-testing` plus `test-without-building` of the full plan with `xcresult-assert-tests.sh`, then `swift-names-gate.sh check` and `selftest` with `SWIFT_GATE_HEADER`, the observation, KMP-NC and DI gates, and `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest`. All passed: `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK`, `SWIFT-OBS-GATE-OK`, `KMPNC-VERSION-OK 1.0.6`, `DI-GATE-OK`, `BUILD SUCCESSFUL`.
- Simulator iPhone 17 (UDID F59F5246-2D70-49BD-B6CF-82DD9A67BF48); no `xcodebuild` left running; the gitignored build inputs were copied in and not staged.
- `gsd-plan-head-before-09-07` ledger written under the worktree git dir with the base `3f32d740`; `commits: 2` is `git rev-list --count` over `3f32d740..HEAD` before this SUMMARY commit.

## Issues Encountered

None.

## Known Stubs

None.

## Threat Flags

None. T-09-29 (device clock driving the initial scroll) accepted as planned: the selection is pure and tested with an injected now.

## Next Phase Readiness

Ready for 09-08. `AgendaDays` is the single source for `LocalDateDays` in 09-10, and `scrollTarget` reads `viewModel.agendaContent.sections`, which become filtered sessions after 09-10 (first item of 09-CONTEXT "Accepted deviations", recorded by 09-14).

## Self-Check: PASSED

- FOUND: iosApp/DevFestNantesTests/AgendaScrollTargetTests.swift
- FOUND commits: dae5546b, d4390ef1
