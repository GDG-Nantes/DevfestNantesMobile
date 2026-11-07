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
      - kind: other
        ref: "xcodebuild build-for-testing (generic iOS Simulator) compiles app and test targets"
        status: pass
      - kind: other
        ref: "swift-names check/selftest, swift-observation, kmp-nc-version gates; ./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest"
        status: pass
      - kind: unit
        ref: "DevFestNantesTests/AgendaViewModelTests, AgendaCharacterizationTests, full suite (>= 61)"
        status: unknown
    human_judgment: true
    rationale: "The Swift test suites could not be executed in this environment (CoreSimulator out of date), so the parity of starrable and tappable rows is not machine-proven"

duration: 45min
completed: 2026-11-07
status: halted
plan_head_before: 7c510b1ae31afc9717b7cbbf315e716846adbb34
---

# Phase 9 Plan 05: Agenda classification on ClassifySessionUseCase Summary

**Agenda rows take isATalk and isNavigable from ClassifySessionUseCase through LiveAgendaSource, stored as plain Bools on AgendaContent.Session, with the table inlined in AgendaView removed.**

**Status: halted before verification.** The implementation is committed and compiles, but no Swift test could be run because CoreSimulator is broken on this machine. See Issues Encountered. The plan must not be treated as verified until the Swift suite has been run.

## Performance

- **Duration:** about 45 min
- **Tasks:** 1 (tracer), code complete, Swift tests not run
- **Files modified:** 8

## Accomplishments

- `AgendaSource` gained `isTalk(_:)` and `isNavigable(_:)`; `LiveAgendaSource` resolves `ClassifySessionUseCase` lazily through a new `classify:` closure (D-08). No model, view or ViewModel references `IosDependencies`.
- `AgendaContent.Session.isATalk` is now stored and `isNavigable` was added; new `init(from:isATalk:isNavigable:)`. The one-argument initializer stays for `SpeakerDetailsView` and the characterization tests and fills both flags with the v1.0 rules (D-07).
- `AgendaViewModel.sessionsChanged` builds rows from the source's flags; `AgendaView.agendaRow` reads `session.isNavigable` for both the row content and the `PushLink` choice (UI-SPEC rule 7).
- `FakeAgendaSource` implements both methods (real use case by default, scriptable); `live_source_...` passes `classify: { ClassifySessionUseCase() }`.
- swift-names baselines regenerated in the same commit (`ClassifySessionUseCase` is now referenced); `shared-h-baseline.txt` is unchanged.

## Task Commits

1. **Task 1 (tracer): agenda rows classified by ClassifySessionUseCase** - `ae79a651` (feat)

## Verification run

| Check | Result |
|-------|--------|
| `xcodebuild build-for-testing`, `generic/platform=iOS Simulator` (app and DevFestNantesTests compile) | PASS |
| `swift-names-gate.sh check` / `selftest` (header from `linkDebug...` build) | SWIFT-NAMES-OK / SWIFT-GATE-SELFTEST-OK |
| `swift-observation-gate.sh`, `kmp-nc-version-gate.sh` | SWIFT-OBS-GATE-OK, KMPNC-VERSION-OK 1.0.6 |
| `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` | BUILD SUCCESSFUL |
| Acceptance greps: no `sessionType == .conference` in AgendaView, `let isNavigable: Bool` present, no `IosDependencies` in model/view/ViewModel, `ClassifySessionUseCase` in referenced-names.txt | PASS |
| Swift test suites (AgendaViewModelTests, AgendaCharacterizationTests, full suite >= 61, `XCRESULT-OK`) | NOT RUN |

## TDD Gate Compliance

**Violation: no RED commit, and no RED evidence.** `xcodebuild test` failed before compiling anything: `CoreSimulator is out of date. Current version (1174.9.2) is older than build version (1174.21.0)`, and `xcrun simctl list devices` hangs. The planned RED test `rows_take_their_talk_and_navigable_flags_from_the_source` (fake scripts `isTalk` to false; expects no row to be a talk) was written, but could not be run, so it was not committed as a separate `test(...)` commit and `check tdd-red-evidence` was not run. Test, fake and production change are in the single `feat` commit. Neither GREEN nor the parity characterization tests were observed passing.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking, partially handled] Simulator unavailable**
- **Found during:** Task 1 (RED run)
- **Issue:** CoreSimulator service is older than the Xcode in use; no simulator destination resolves; `simctl` hangs.
- **Fix:** none possible within scope. Did not kill or restart the shared CoreSimulator service (it affects the whole machine and any other session). Verified compilation of app and tests via `build-for-testing` with a generic destination, and ran every non-simulator gate.
- **Verification:** see table above.

**2. [Added test] One test added to AgendaViewModelTests**
- The plan expected no test added; one RED test was added (suite becomes 62 tests, still satisfying `>= 61`). It also checks that `isNavigable` matches the v1.0 table (quickie, conference, codelab).

**Total deviations:** 2. **Impact:** the code is complete, but the plan's own Swift verification is outstanding.

## Execution notes

- The plan's `<automated>` blocks were run as their component steps (plain commands in the worktree, `/usr/bin/git`, `/usr/bin/grep`), per the executor brief, because the worktree command guard refuses compound shell naming git. The xcodebuild test blocks could not be run (see above); the Gradle, gate and grep blocks were run with identical checks.
- Gitignored build inputs (`local.properties`, `GoogleService-Info.plist`, `build/SourcePackages`) were copied into the worktree and not staged.

## Issues Encountered

- **Blocker for verification:** CoreSimulator out of date (1174.9.2 running vs 1174.21.0 expected by Xcode). Suggested remedy for whoever owns the machine: restart the CoreSimulator service or the machine, then run `AgendaViewModelTests`, `AgendaCharacterizationTests`, and the full suite (expect `XCRESULT-OK`, total >= 62 with the added test). If they pass, this plan can be flipped to `status: complete`.

## Known Stubs

None.

## Next Phase Readiness

09-06 can build on the display shape (stored flags, three-argument initializer) and must delete the transitional one-argument initializer together with its last app call site in `SpeakerDetailsView.swift`. 09-06 should not be treated as safe until this plan's Swift suite has been run.

## Self-Check: PASSED (files and commit exist); Swift test execution FAILED to run

- `ae79a651` is an ancestor of HEAD; all 8 modified files are in the commit.

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-07*
