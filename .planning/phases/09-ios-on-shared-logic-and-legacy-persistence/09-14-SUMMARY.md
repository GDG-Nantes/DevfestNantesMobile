---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 14
subsystem: ios-docs
tags: [ios, docs, kmp, deviations]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: shared agenda flow, shared stores and accessors (09-01 to 09-13)
provides:
  - ARCHITECTURE.md iOS data flow on the shared use cases and stores
  - the four accepted deviations of 09-CONTEXT in the deviations table
  - DEVELOPMENT.md and AGENTS.md aligned with the final Swift test suite and accessor rule
  - IOSDATA-01 sweep and all gates green on the final tree
affects: [phase-10 explicit DI, phase-12 verification]

actuals:
  tokens: 4500
  tasks: 2
  commits: 2
plan_head_before: dc0405f2cabafa17a7ccc64300d8f4523d4d7dad
plan_head_after: f6f2fc0b1d4625978c5f1b8a8b635d846f4f4cff

tech-stack:
  added: []
  patterns: ["docs move with the code: deviations table holds only user-accepted rows"]

key-files:
  created: []
  modified:
    - documentation/ARCHITECTURE.md
    - documentation/DEVELOPMENT.md
    - AGENTS.md
    - iosApp/iosApp/Model/KotlinStreamBridge.swift

key-decisions:
  - "Deviations table gets exactly the accepted rows (scroll target, day labels, seed window) plus the fractional-seconds row cleaned of the deleted test name; no other row"

requirements-completed: [IOSDATA-01]

coverage:
  - id: D1
    description: "ARCHITECTURE.md describes observeAgenda, LocalDateDays, AgendaContent mapping, the mirror and the IosDependencies accessors"
    requirement: IOSDATA-01
    verification:
      - kind: other
        ref: "grep observeAgenda documentation/ARCHITECTURE.md; scripts/swift-observation-gate.sh -> SWIFT-OBS-GATE-OK"
        status: pass
    human_judgment: true
    rationale: "Prose accuracy against the shipped code needs a reader's judgment"
  - id: D2
    description: "Deviations table holds the four accepted deviations and no other new row"
    requirement: IOSDATA-01
    verification:
      - kind: other
        ref: "grep 'latest visible slot' / 'non-Gregorian' / 'seed window' documentation/ARCHITECTURE.md"
        status: pass
    human_judgment: true
    rationale: "That no unaccepted row was added is a judgment against 09-CONTEXT"
  - id: D3
    description: "IOSDATA-01 sweep clean and every gate and the full test suite green on the final tree"
    requirement: IOSDATA-01
    verification:
      - kind: integration
        ref: "xcresult-assert-tests.sh -> XCRESULT-OK total=72; swift-names, observation, KMP-NC, DI gates OK; detekt lint testDebugUnitTest jvmTest testAndroidHostTest rc=0"
        status: pass
    human_judgment: false

duration: 25min
completed: 2026-11-07
status: complete
---

# Phase 09 Plan 14: Documentation and IOSDATA-01 sweep Summary

**Architecture, developer and agent docs now describe the shipped iOS data flow on the shared use cases and stores, the deviations table carries the four user-accepted deviations, and the IOSDATA-01 sweep, every gate and the 72-test Swift suite are green.**

## Performance

- **Duration:** 25 min
- **Started:** 2026-11-07T18:00:00Z
- **Completed:** 2026-11-07T18:26:21Z
- **Tasks:** 2
- **Files modified:** 4

## Accomplishments

- `ARCHITECTURE.md` iOS App section: `AgendaViewModel.observeAgenda()` continuous observation from `AgendaView`'s `.task` (cancelled on disappear, resubscribed on appear, trace on first emission of each subscription, previous content kept); `LiveAgendaSource` passing `LocalDateDays` from `AgendaDays` and the device zone; `AgendaContent` as a pure display mapping; stars, checkmarks and Clear reading the ViewModel mirror while the list comes only from the flow; the plain `IosDependencies` accessors used by the Live adapters; speakers, venue and classification on the shared use cases; the bridge sentence now covers the continuous agenda flow.
- Deviations table: new rows for the initial scroll with filters active (latest visible slot), day labels on a non-Gregorian device calendar, and the iOS bookmark mirror seed window; the fractional-seconds row no longer names the test deleted in 09-12. No other row.
- `DEVELOPMENT.md`: the characterization paragraph now says the suites assert the shared output against the same v1.0 goldens; the two sentences about writing `DevFest_Nantes.SessionFilter` (false since 09-12 and 09-13) are gone, as are `toggleFavorite` and the `sessionFilters` assignment from the avoid list (replaced by the real intent names); the Swift test row cites `AgendaViewModelTests`, `KotlinContinuousFlowTests`. The page states no specific Xcode version ("Latest stable"), so no toolchain number needed correcting.
- `AGENTS.md`: the iOS rule on the accessors and on persistence.
- `KotlinStreamBridge.swift`: trade-off comment updated (comment-only change).

## Task Commits

1. **Task 1 (tracer): architecture document and bridge comment** - `5baa5481` (docs)
2. **Task 2: developer and agent docs, then the sweep** - `f6f2fc0b` (docs)

**Plan metadata:** committed right after this file (docs: complete plan).

## Verification results

- Task 1 `<automated>`: all five greps and `swift-observation-gate.sh` -> `SWIFT-OBS-GATE-OK` (re-run end to end before the tracer commit, in the default end-of-phase mode).
- Task 2 doc greps: pass.
- IOSDATA-01 sweep: no `SessionFilter.swift`, no `AgendaSessionFilters` / `forKey: "Favorites"` / `FilterType` in Swift sources, no `Dictionary(grouping` in `Agenda` or `Model/Data`: clean.
- Full Swift suite on iPhone 17 (F59F5246-...), iOS 27.0: `XCRESULT-TESTS total=72 passed=72 failed=0 skipped=0`, last line `XCRESULT-OK`. 72 is the true count (floor was 71; the plan text says 71, the actual suite is 72).
- Gates: `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK`, `SWIFT-OBS-GATE-OK`, `KMPNC-VERSION-OK 1.0.6`, `DI-GATE-OK`.
- `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest`: exit 0 (Robolectric prints logged SQLite stack traces from a Firebase transport thread; the build is green).

## Deviations from Plan

None - plan executed exactly as written.

Notes: the plan's fixed "71 tests" was a floor; the true count is 72, reported as such. The plan's `<automated>` blocks were run as their component plain commands (the worktree command guard refuses compound shell naming git or computed arguments), with identical checks and outcomes; the simulator UDID was taken from the brief instead of computed with `simctl | jq`. The `-collect-test-diagnostics never` flag was added to the test run per the brief. The per-plan commit ledger file could not be written under the shared `.git` directory from the worktree, so `plan_head_before` is the base SHA verified at startup; `git rev-list --count dc0405f2..HEAD` returned 2 before this SUMMARY commit.

## Issues Encountered

None.

## Known Stubs

None.

## Threat Flags

None.

## Self-Check: PASSED

- documentation/ARCHITECTURE.md, documentation/DEVELOPMENT.md, AGENTS.md, iosApp/iosApp/Model/KotlinStreamBridge.swift modified and committed.
- Commits `5baa5481` and `f6f2fc0b` exist on the branch.
- No xcodebuild or Gradle process left running.
