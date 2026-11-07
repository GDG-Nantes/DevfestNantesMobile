---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 06
subsystem: ios
tags: [ios, kmp, speakers, classification, swift]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: "AgendaContent.Session with stored isATalk/isNavigable and init(from:isATalk:isNavigable:), ClassifySessionUseCase exported to Swift (09-05)"
provides:
  - "SpeakerDetailsSource.isTalk(_:) and isNavigable(_:), live adapter over ClassifySessionUseCase"
  - "SpeakerDetailsViewModel.content(for:) used by SpeakerDetailsView"
  - "No Swift classification rules left in the app target"
  - "Both columns (isTalk, isNavigable) of the shared table asserted in Swift"
affects: [09-07]

requirements-completed: [IOSDATA-01]

actuals:
  tokens: 5500
  tasks: 1
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Every display-model construction in the app goes through a Source port that wraps ClassifySessionUseCase; only Live adapters call IosDependencies (D-07, D-08)"
    - "Test-only convenience initializer lives in CharacterizationSupport.swift, not in the app"

key-files:
  created: []
  modified:
    - iosApp/iosApp/UI/Speakers/SpeakerDetailsSource.swift
    - iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift
    - iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift
    - iosApp/iosApp/Model/Data/AgendaContent.swift
    - iosApp/DevFestNantesTests/Fakes/FakeSpeakerDetailsSource.swift
    - iosApp/DevFestNantesTests/CharacterizationSupport.swift
    - iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift
    - iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift

key-decisions:
  - "FakeSpeakerDetailsSource delegates to a real ClassifySessionUseCase by default with optional @Sendable closures to script the flags, like FakeAgendaSource; the scripted path is what makes the new test discriminate"
  - "The day label literal passed by SpeakerDetailsView to AgendaDetailView stays untouched (08 WR-04 remains logged debt)"

patterns-established:
  - "Speakers > speaker > session path classified like the agenda path"

coverage:
  - id: D1
    description: "Speaker detail sessions take isATalk and isNavigable from ClassifySessionUseCase through SpeakerDetailsViewModel.content(for:)"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "DevFestNantesTests/SpeakerDetailsViewModelTests#content_for_a_session_carries_the_shared_classification"
        status: pass
    human_judgment: false
  - id: D2
    description: "Navigable and talk columns of the shared classification table asserted as literal tables in Swift (keynote is a talk but not navigable)"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "DevFestNantesTests/AgendaCharacterizationTests#is_navigable_matches_the_shared_table and #is_a_talk_matches_the_shared_table"
        status: pass
    human_judgment: false
  - id: D3
    description: "Last Swift classification rules deleted; SpeakerDetailsView no longer builds the display model itself"
    requirement: IOSDATA-01
    verification:
      - kind: other
        ref: "acceptance greps (no Session(from in SpeakerDetailsView, no case .lunch in AgendaContent, no IosDependencies in model/view/ViewModel), swift-names, observation, KMP-NC and DI gates"
        status: pass
    human_judgment: false

duration: 25min
completed: 2026-11-07
status: complete
plan_head_before: 75a8c76052c647063d04d8636455d65b323bc648
---

# Phase 9 Plan 06: Speaker sessions on ClassifySessionUseCase Summary

**The Speakers > speaker > session path now takes isATalk and isNavigable from ClassifySessionUseCase through SpeakerDetailsViewModel.content(for:), the last Swift classification rules are deleted, and both columns of the shared table are asserted in Swift.**

## Performance

- **Duration:** about 25 min
- **Tasks:** 1 (tracer, TDD)
- **Files modified:** 8 (5 app and test-support, 3 test)

## Accomplishments

- `SpeakerDetailsSource` gained `isTalk(_:)` and `isNavigable(_:)`; `LiveSpeakerDetailsSource` takes a lazy `classify:` closure like `LiveAgendaSource` (D-08). Nothing outside the live adapters references `IosDependencies`.
- `SpeakerDetailsViewModel.content(for:)` maps a session with the source's flags; `SpeakerDetailsView` binds `let content = viewModel.content(for: session)` and passes it to both `AgendaDetailView` and `AgendaCellView`. The `day: "day"` literal is unchanged (08 WR-04 stays debt). The view diff is confined to the `ForEach`.
- The one-argument `AgendaContent.Session.init(from:)` and the v1.0 `switch` it held are deleted from the app. A test-only overload in `CharacterizationSupport.swift` classifies through a real `ClassifySessionUseCase()` so `AgendaViewModelTests` and `AgendaCharacterizationTests` call sites compile unchanged.
- New tests: `is_navigable_matches_the_shared_table` (BREAK false, CODELAB true, CONFERENCE true, KEYNOTE false, LUNCH false, OPENING false, QUICKIE true, nil false) and `content_for_a_session_carries_the_shared_classification`. `AgendaCharacterizationTests` doc comment updated (the code under test is no longer a Swift computed property).

## Task Commits

1. **Task 1 RED:** `75878e81` (test) - failing test, scripted fake, `content(for:)` stub ignoring the source
2. **Task 1 GREEN:** `998332ed` (feat) - shared classification wired through the port, Swift rules deleted

**Plan metadata:** SUMMARY commit (docs).

## TDD Gate Compliance

RED and GREEN commits are present in order (`test(09-06)` then `feat(09-06)`). No refactor commit was needed.

**RED evidence.** Run on the iPhone 17 simulator (iOS 27.0) over the suites `SpeakerDetailsViewModelTests` and `AgendaCharacterizationTests` (15 tests: 14 passed, 1 failed). The xcresult was read with `xcresulttool` (`testFailures`).

- Target: `SpeakerDetailsViewModelTests/content_for_a_session_carries_the_shared_classification()`
- Failures: `Expectation failed: scripted.content(for: talk).isATalk == false` (observed `true`) and `Expectation failed: scripted.content(for: talk).isNavigable == false` (observed `true`)
- Semantic assessment: the target compiled, executed and failed on the planned assertion. The RED commit carried a stub `content(for:)` that ignored the source and used the v1.0 rules, so a port scripted to the opposite flags was not honored. Not a compile, load or fixture failure.
- `is_navigable_matches_the_shared_table` passed already in the RED run. This is expected: it is a characterization of unchanged behavior (the v1.0 rules still ran behind the transitional initializer), and its job is to keep the navigable column pinned while the rules are replaced by the use case.
- `gsd_run check tdd-red-evidence` was not run: an xcresult is not one of its supported report formats.

GREEN: the same suites pass, and the full plan passes (below).

## Verification run

| Check | Result |
|-------|--------|
| Full iosApp test plan, iPhone 17 iOS 27.0 | 64 total, 64 passed, 0 failed, 0 skipped, XCRESULT-OK (floor was 63) |
| New tests present in xcresult (`content_for_a_session_carries_the_shared_classification`, `is_navigable_matches_the_shared_table`) | PASS |
| Acceptance greps: no `AgendaContent.Session(from` in SpeakerDetailsView, no `case .lunch` in AgendaContent, no `IosDependencies` in AgendaContent/SpeakerDetailsView/SpeakerDetailsViewModel, `viewModel.content(for: session)` and `day: "day"` present | PASS |
| `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` | PASS |
| `swift-names-gate.sh check` / `selftest` | SWIFT-NAMES-OK / SWIFT-GATE-SELFTEST-OK (no baseline regenerated) |
| `swift-observation-gate.sh`, `kmp-nc-version-gate.sh`, `di-gate.sh` | SWIFT-OBS-GATE-OK, KMPNC-VERSION-OK 1.0.6, DI-GATE-OK |
| `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` | BUILD SUCCESSFUL |

## Deviations from Plan

None - plan executed exactly as written. Two refinements inside the plan's scope: the fake's scripted overrides (needed so the new test can fail meaningfully in RED), and a small private helper in `AgendaCharacterizationTests` shared by the two table tests.

## Execution notes

- The plan's `<automated>` blocks were run as their component steps (plain commands in the worktree, `/usr/bin/git`, `/usr/bin/grep`, one script in the scratchpad for the build-for-testing and test-without-building pair), per the executor brief, because the worktree command guard refuses compound shell naming git. xcodebuild ran in the foreground, one at a time, with `-collect-test-diagnostics never`, on UDID F59F5246-2D70-49BD-B6CF-82DD9A67BF48. No xcodebuild process was left running.
- `-only-testing` was used with suite identifiers only (RED run); the GREEN run was the full plan.
- The gitignored inputs (`local.properties`, `GoogleService-Info.plist`, `build/SourcePackages`) were copied in and not staged.
- The plan commit ledger file under the shared git directory could not be written from the worktree; `plan_head_before` is the dispatch base 75a8c760, and `commits: 2` is the measured count of `75a8c760..HEAD` before the SUMMARY commit.

## Issues Encountered

None.

## Known Stubs

None. The RED stub of `content(for:)` was replaced in the GREEN commit.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Every display model construction in the app goes through the shared classification; no Swift classification table remains. 09-07 can proceed.

## Self-Check: PASSED

- Commits `75878e81` and `998332ed` are ancestors of HEAD; the 8 listed files exist.
- All acceptance criteria re-run: full suite XCRESULT-OK with 64 >= 63, the two new tests present and passing, `SpeakerDetailsView.swift` diff limited to the `ForEach`, gates green.

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-07*
