---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 05
subsystem: ios
tags: [ios, kmp, agenda, classification, swift]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: "IosDependencies.classifySession() and ClassifySessionUseCase exported to Swift (09-01..09-04)"
provides:
  - "AgendaSource.isTalk(_:) and isNavigable(_:), live adapter over ClassifySessionUseCase"
  - "AgendaContent.Session with stored isATalk and isNavigable plus init(from:isATalk:isNavigable:)"
  - "AgendaView without its own classification table"
affects: [09-06]

actuals:
  tokens: 6500
  tasks: 1
  commits: 1

tech-stack:
  added: []
  patterns:
    - "Display model carries plain stored flags filled at mapping time; only the live source adapter calls Kotlin (D-07, D-08)"

key-files:
  created: []
  modified:
    - iosApp/iosApp/Agenda/AgendaSource.swift
    - iosApp/iosApp/Model/Data/AgendaContent.swift
    - iosApp/iosApp/Agenda/AgendaViewModel.swift
    - iosApp/iosApp/Agenda/AgendaView.swift
    - iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift
    - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt

key-decisions:
  - "Transitional one-argument AgendaContent.Session.init(from:) kept (v1.0 rules, now a switch on session.type) so SpeakerDetailsView and the characterization tests compile unchanged; 09-06 deletes it"
  - "FakeAgendaSource delegates to a real ClassifySessionUseCase by default, with optional closures to script the flags"

patterns-established:
  - "Agenda port exposes classification; views read stored flags only"

requirements-completed: [IOSDATA-01]

coverage:
  - id: D1
    description: "Agenda rows are classified by ClassifySessionUseCase through the live adapter; the table inlined in AgendaView is gone"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "DevFestNantesTests/AgendaViewModelTests + AgendaCharacterizationTests (18/18 passed); full iosApp plan 62/62, XCRESULT-OK"
        status: pass
      - kind: unit
        ref: "AgendaViewModelTests/rows_take_their_talk_and_navigable_flags_from_the_source (fails with the implementation reverted, passes with it)"
        status: pass
      - kind: other
        ref: "swift-names check/selftest, swift-observation, kmp-nc-version gates; ./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest"
        status: pass
    human_judgment: false

duration: 75min
completed: 2026-11-07
status: complete
plan_head_before: 7c510b1ae31afc9717b7cbbf315e716846adbb34
---

# Phase 9 Plan 05: Agenda classification on ClassifySessionUseCase Summary

**Agenda rows take isATalk and isNavigable from ClassifySessionUseCase through LiveAgendaSource, stored as plain Bools on AgendaContent.Session, with the table inlined in AgendaView removed.**

The first pass could not run Swift tests because CoreSimulator was out of date. After the toolchain was fixed, the Swift verification was completed in a second pass (below). The toolchain moved from Xcode 27.1 to 27.2 between the two passes, with no code impact.

## Performance

- **Duration:** about 75 min over two passes
- **Tasks:** 1 (tracer)
- **Files modified:** 8

## Accomplishments

- `AgendaSource` gained `isTalk(_:)` and `isNavigable(_:)`; `LiveAgendaSource` resolves `ClassifySessionUseCase` lazily through a new `classify:` closure (D-08). No model, view or ViewModel references `IosDependencies`.
- `AgendaContent.Session.isATalk` is now stored and `isNavigable` was added; new `init(from:isATalk:isNavigable:)`. The one-argument initializer stays for `SpeakerDetailsView` and the characterization tests and fills both flags with the v1.0 rules (D-07).
- `AgendaViewModel.sessionsChanged` builds rows from the source's flags; `AgendaView.agendaRow` reads `session.isNavigable` for both the row content and the `PushLink` choice (UI-SPEC rule 7).
- `FakeAgendaSource` implements both methods (real use case by default, scriptable); `live_source_...` passes `classify: { ClassifySessionUseCase() }`.
- swift-names baselines regenerated in the same commit (`ClassifySessionUseCase` is now referenced); `shared-h-baseline.txt` is unchanged.

## Task Commits

1. **Task 1 (tracer): agenda rows classified by ClassifySessionUseCase** - `ae79a651` (feat)

**Plan metadata:** SUMMARY commits (docs).

## Verification run

| Check | Result |
|-------|--------|
| `xcodebuild build-for-testing`, generic iOS Simulator destination | PASS |
| `swift-names-gate.sh check` / `selftest` | SWIFT-NAMES-OK / SWIFT-GATE-SELFTEST-OK |
| `swift-observation-gate.sh`, `kmp-nc-version-gate.sh` | SWIFT-OBS-GATE-OK, KMPNC-VERSION-OK 1.0.6 |
| `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` | BUILD SUCCESSFUL |
| Acceptance greps (no `sessionType == .conference` in AgendaView, `let isNavigable: Bool` present, no `IosDependencies` in model/view/ViewModel, `ClassifySessionUseCase` in referenced-names.txt) | PASS |
| AgendaViewModelTests + AgendaCharacterizationTests (iPhone 17, iOS 27.0) | 18 total, 18 passed, XCRESULT-OK |
| Full iosApp test plan | 62 total, 62 passed, 0 failed, 0 skipped, XCRESULT-OK (plan floor was 61) |

## TDD Gate Compliance

**Deviation: no separate `test(...)` commit.** The test `rows_take_their_talk_and_navigable_flags_from_the_source`, the fake change and the production change are all in the single `feat` commit `ae79a651`, because the simulator was unavailable during the first pass. History was not rewritten afterwards.

**RED evidence, produced after the fact.** In the working copy only (not committed, then restored file by file; the tree was clean afterwards), the call in `AgendaViewModel.sessionsChanged` was changed back to `AgendaContent.Session(from: $0)`, which fills the flags from the transitional v1.0 rules and ignores the source. The project compiled and `AgendaViewModelTests` ran 12 tests: 11 passed and exactly one failed.

- Target: `AgendaViewModelTests/rows_take_their_talk_and_navigable_flags_from_the_source()`
- Failure message read from the xcresult with xcresulttool (`testFailures`): `Expectation failed: rows.allSatisfy { !$0.isATalk }`
- Semantic assessment: the test executed and failed on the planned assertion (the source scripts `isTalk` to false, yet the rows still came out as talks). It is not a compile, load or fixture failure.

With the implementation restored the same suite passes. `gsd_run check tdd-red-evidence` was not run because an xcresult is not one of its supported report formats.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking, resolved] Simulator unavailable in the first pass**
- **Found during:** Task 1 (RED run)
- **Issue:** CoreSimulator was older than Xcode; no simulator destination resolved.
- **Fix:** none in code. The first pass checked compilation with `build-for-testing` on a generic destination and ran every non-simulator gate. The Swift suites were run in a second pass once the machine was fixed (Xcode 27.1 to 27.2, CoreSimulator 1174.21).
- **Verification:** see the table above.

**2. [Added test] One test added to AgendaViewModelTests**
- The plan expected no test added. One RED test was added, and it also checks that `isNavigable` matches the v1.0 table (quickie, conference, codelab). The suite is 62 tests; the plan floor was 61.

---

**Total deviations:** 2, plus the missing separate test commit recorded under TDD Gate Compliance. **Impact:** none on behavior; TDD order is evidenced after the fact rather than by commit order.

## Execution notes

- The plan's `<automated>` blocks were run as their component steps (plain commands in the worktree, `/usr/bin/git`, `/usr/bin/grep`), per the executor brief, because the worktree command guard refuses compound shell naming git. The xcodebuild test blocks were run with `-collect-test-diagnostics never`, one at a time, on the iPhone 17 simulator.
- A first filtered run with a single-test `-only-testing` identifier matched zero tests (exit 0, `totalTestCount` 0) and was discarded; the RED run used the suite identifier.
- Gitignored build inputs (`local.properties`, `GoogleService-Info.plist`, `build/SourcePackages`) were copied into the worktree and not staged.

## Issues Encountered

- First pass blocked by an out-of-date CoreSimulator; resolved by the machine owner. No code change was needed afterwards.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None.

## Next Phase Readiness

09-06 can build on the display shape (stored flags, three-argument initializer) and must delete the transitional one-argument initializer together with its last app call site in `SpeakerDetailsView.swift`. This plan's Swift suite is green (62/62).

## Self-Check: PASSED

- Commit `ae79a651` is an ancestor of HEAD; the 8 modified files exist and are in it.
- All acceptance criteria re-run: no table in AgendaView, `let isNavigable: Bool` present, no `IosDependencies` in model/view/ViewModel, `ClassifySessionUseCase` in referenced-names.txt, full suite XCRESULT-OK with 62 >= 61.

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-07*
