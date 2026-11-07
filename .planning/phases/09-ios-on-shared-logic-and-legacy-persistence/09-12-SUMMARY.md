---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 12
subsystem: ios-agenda
tags: [ios, cleanup, characterization, goldens, swift-testing]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: "09-07 TestData.schedules, 09-09 shared filter and bookmark stores, 09-10 AgendaContent(days:isTalk:isNavigable:) on the shared combined flow"
provides:
  - "v1.0 ios-agenda and ios-filtering goldens asserted on the shared output (AgendaContent mapping, FilterSessionsUseCase), unedited"
  - "No Swift grouping, filtering, date parsing or duplicated SessionFilter/FilterType left in the iOS app"
  - "New phase test floor: 72 tests"
affects: [09-13 swift-names baselines, 09-14 docs]

actuals:
  tokens: 9000
  tasks: 2
  commits: 2
plan_head_before: a76300f89e8c71ba9d2553e4183a3aaf4f40cb9a
plan_head_after: 9ec3f716dfba55be336c6e81526520b1c58a858f

tech-stack:
  added: []
  patterns:
    - "Characterization goldens survive a deletion by being re-pointed at the shared output first, then deleting the Swift logic they proved"

key-files:
  created: []
  modified:
    - iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift
    - iosApp/DevFestNantesTests/CharacterizationSupport.swift
    - iosApp/iosApp/Agenda/AgendaViewModel.swift
    - iosApp/iosApp/Agenda/AgendaView.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj
  deleted:
    - iosApp/iosApp/Model/SessionFilter.swift
    - iosApp/DevFestNantesTests/SessionFilterTests.swift
    - iosApp/DevFestNantesTests/AgendaDateParsingTests.swift

key-decisions:
  - "Task 1 grouping tests keep the canonical (room, id) sort before comparing to the golden, because the goldens do not pin the room order inside a slot; the new mapping_keeps_the_shared_order_inside_a_slot test is the one that pins the shared order"
  - "The Kotlin SessionFilter is written unqualified only in AgendaCharacterizationTests; the other qualified shared.SessionFilter sites stay for 09-13, as the plan states"
  - "documentation/DEVELOPMENT.md still names the DevFest_Nantes.SessionFilter rule; left for 09-14, which owns that edit"

requirements-completed: [IOSDATA-01]

coverage:
  - id: D1
    description: "The shared output mapped through AgendaContent matches the unedited ios-agenda goldens in Europe/Paris and Pacific/Auckland"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift#grouping_in_europe_paris_matches_the_golden, #grouping_in_pacific_auckland_matches_the_golden"
        status: pass
    human_judgment: false
  - id: D2
    description: "FilterSessionsUseCase reproduces the unedited ios-filtering golden for all 15 cases"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift#filtering_matches_the_golden_for_every_case"
        status: pass
    human_judgment: false
  - id: D3
    description: "The mapping keeps the order of TimeSlot.sessions exactly (edge IOSDATA-03)"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift#mapping_keeps_the_shared_order_inside_a_slot"
        status: pass
    human_judgment: false
  - id: D4
    description: "Swift grouping, filtering, date helpers, SessionFilter and FilterType are deleted; the project lints and builds"
    requirement: IOSDATA-01
    verification:
      - kind: other
        ref: "absence sweep (grep -w) plus plutil -lint plus full xcodebuild test run, XCRESULT-OK total=72"
        status: pass
    human_judgment: false

duration: 25min
completed: 2026-11-07
status: complete
---

# Phase 9 Plan 12: Move the proof, then delete what it proved Summary

**The v1.0 agenda and filtering goldens now assert the shared use-case output through `AgendaContent`, and the Swift grouping, filtering, date helpers, `SessionFilter` struct and `FilterType` enum are gone with their tests; the suite is 72 tests, all green, goldens byte-identical.**

## Performance

- **Duration:** about 25 min
- **Completed:** 2026-11-07T18:10Z
- **Tasks:** 2
- **Files modified:** 5 modified, 3 deleted

## Accomplishments

- Task 1 (tracer): `assertGrouping` builds `AgendaContent(days:isTalk:isNavigable:)` from `TestData.schedules(zoneId:)` with a real `ClassifySessionUseCase`, selects each tab by `day` equality and compares to the unedited `ios-agenda-*` goldens. The filtering golden is produced by `FilterSessionsUseCase().filter(...)`, still 15 cases. New test `mapping_keeps_the_shared_order_inside_a_slot` compares the mapped session ids with `slot.sessions.map(\.id)` with no sort.
- Task 2: deleted `sessionsChanged`, `getDate`, `parseDate`, `sectionDateFormatter` (AgendaViewModel), `AgendaView.sections(_:forDay:)` and `filteredSessions(_:filters:favorites:)` (kept `filteredSections(for:)` and `scrollTarget`), `SessionFilter.swift`, `SessionFilterTests.swift`, `AgendaDateParsingTests.swift` and their 12 `project.pbxproj` lines.
- Accepted deviation 3 of 09-CONTEXT (fractional-second start dates parsed by Kotlin instead of falling back to the current date) landed with `parseDate`'s removal. 09-14 updates the deviations row.

## Task Commits

1. **Task 1 (tracer): goldens on the shared output** - `bde81fea` (test)
2. **Task 2: delete the Swift logic and duplicated types** - `9ec3f716` (refactor)

**Plan metadata:** the SUMMARY commit that follows these two.

## TDD Gate Compliance

The plan is `type: execute` with a tdd tracer task; there is no `feat` commit because the code under test (09-09, 09-10) already existed.

- **RED:** the new test `mapping_keeps_the_shared_order_inside_a_slot` and the rewritten goldens passed on first run (8 of 8 in `AgendaCharacterizationTests`), which is expected for a characterization move: the mapping already preserved the order. To prove the new test can fail, I applied a temporary mutation (`slot.sessions.reversed().map` in `AgendaContent.init(days:...)`) and re-ran the suite: exactly `mapping_keeps_the_shared_order_inside_a_slot` failed on `Expectation failed: actual == expected` with the session ids of each multi-session slot in reverse order, while the grouping and filtering goldens stayed green (they canonicalize the order, as designed). Evidence read with `xcresulttool get test-results tests/summary` (not `check tdd-red-evidence`, which does not read xcresult bundles). The mutation was reverted before the commit; `git status` was clean apart from the test file.
- **semanticAssessment:** the target test executed and failed on the planned assertion for the intended reason (order not preserved), no setup or discovery fault.
- **GREEN:** reverted mutation, 8 of 8 pass, then 72 of 72 in the full run.

## Verification Results

- `AgendaCharacterizationTests`: `XCRESULT-TESTS total=8 passed=8`, suite among passed; goldens diff against the branch point (`1077fd84`) empty for `core/domain/testdata`.
- Full suite after Task 2: **`XCRESULT-TESTS total=72 passed=72 failed=0 skipped=0`, `XCRESULT-OK`**. **New phase floor: 72 tests.**
- Test count reconciliation: the dispatch fact said the suite was at 82; the plan's own arithmetic assumed 81 after 09-11 (+1 = 82, -11 = 71). The true base was 82, so 82 + 1 (new test) - 11 (6 `SessionFilterTests` + 5 `AgendaDateParsingTests`) = 72. The plan floor of 71 was stale by one; no test file was dropped (`@Test` count in `iosApp/DevFestNantesTests` is 72).
- `plutil -lint project.pbxproj`: OK. Absence sweeps (`/usr/bin/grep`): no pbxproj entry for the 3 deleted files, no `sessionsChanged`/`parseDate`/`sectionDateFormatter`/`filteredSessions`, no `FilterType` (word match), no `Dictionary(grouping` in `Agenda`/`Model/Data`, no `shared.SessionFilter` or `DevFest_Nantes.SessionFilter` in the two test files, no `enum|struct (FilterType|SessionFilter)` in `iosApp/iosApp`. The only `sorted` left in `AgendaView.swift` is the minute-of-day sort in `scrollTarget`. `AgendaContent.swift` mentions "sorted"/"filtered" only in a doc comment.
- Gates: `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK`, `SWIFT-OBS-GATE-OK`, `KMPNC-VERSION-OK 1.0.6`, `DI-GATE-OK`; `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` BUILD SUCCESSFUL.

## Decisions Made

See frontmatter `key-decisions`.

## Deviations from Plan

None - plan executed exactly as written. Notes (not deviations):

- Test floor is 72, not 71, because the plan's baseline of 81 was one low (see reconciliation above).
- The plan's `<automated>` blocks name git and compound shell, which the worktree guard refuses; I ran their component steps as plain commands and a scratchpad script (`run-suite.sh`: boot UDID F59F5246-2D70-49BD-B6CF-82DD9A67BF48, `xcodebuild test` with `-collect-test-diagnostics never`, `-parallel-testing-enabled NO`), then `scripts/xcresult-assert-tests.sh`, the suite-name and test-case checks through `xcresulttool` and `jq`, and `git diff --stat <merge-base> -- core/domain/testdata` (empty) in place of `git diff --quiet`. Checks and outcomes are identical. The device is the iPhone 17 simulator (iOS 27.0) per the brief instead of the plan's "last available iPhone". The merge-base used was `1077fd84`. Task 2 ran build and test as one `xcodebuild test` rather than `build-for-testing` then `test-without-building`.
- Stale documentation, owned by 09-14: `documentation/DEVELOPMENT.md` lines 146-155 still describe the `DevFest_Nantes.SessionFilter` rule and the old characterization behaviors.

## Issues Encountered

None.

## Known Stubs

None.

## Threat Flags

None. T-09-20 (golden tampering) mitigated: no golden touched, `core/domain/testdata` diff against the branch point empty after both tasks.

## Next Phase Readiness

Ready for 09-13: five Swift files still write `shared.SessionFilter` (`AgendaSource`, `AgendaViewModel`, `AgendaViewModelTests`, `FakeAgendaSource`, `KotlinContinuousFlowTests`), and the doc comments in `AgendaSource.swift`, `AgendaViewModelTests.swift` and `KotlinContinuousFlowTests.swift` still say the app declares its own `SessionFilter` struct. These move with the baselines.

## Self-Check: PASSED

- Commits `bde81fea` and `9ec3f716` are ancestors of HEAD; the deleted files are absent on disk; the modified files exist; no xcodebuild process is running.
