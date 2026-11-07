---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 08
subsystem: ios-agenda
tags: [ios, agenda, viewmodel, swiftui, tdd]

requires:
  - phase: 09-07
    provides: agenda scroll-target selection and single-sourced day constants
provides:
  - "AgendaViewModel.isBookmarked(_:), toggleBookmark(sessionId:) and toggleFilter(type:value:) intents"
  - "Five filter menus, the agenda list row and the detail star reading and writing only through the intents"
affects: [09-09]

actuals:
  tokens: 9000
  tasks: 2
  commits: 4
plan_head_before: 2cb1d6f2a03022a006ba47b875b3e56de6309fd3
plan_head_after: 93a39c277777b76660796721a19ce45cc39dc183

tech-stack:
  added: []
  patterns:
    - "ViewModel owns every write: favorites and sessionFilters are private(set), views call intents"
    - "Filter intent takes (type:value:) so menus do not change when 09-09 swaps the parameter type"

key-files:
  created: []
  modified:
    - iosApp/iosApp/Agenda/AgendaViewModel.swift
    - iosApp/iosApp/Agenda/AgendaView.swift
    - iosApp/iosApp/Agenda/AgendaDetailView.swift
    - iosApp/iosApp/Agenda/RoomFilterMenu.swift
    - iosApp/iosApp/Agenda/LanguageFilterMenu.swift
    - iosApp/iosApp/Agenda/ComplexityFilterMenu.swift
    - iosApp/iosApp/Agenda/SessionTypeFilterMenu.swift
    - iosApp/iosApp/Agenda/FavoritesFilterMenu.swift
    - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
    - iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift

key-decisions:
  - "toggleBookmark is @discardableResult: the two analytics call sites bind the post-toggle value, the legacy-key test ignores it"
  - "toggleFilter mutates sessionFilters in place (one didSet, one save per toggle) instead of copying and reassigning the set"
  - "AgendaView.filteredSessionsForSection keeps reading viewModel.favorites (read-only, private(set) allows it): it is not a line named by the plan (D-07)"

patterns-established:
  - "Compile-only stub in the RED commit (signature returning false / doing nothing) so a Swift TDD RED fails on the planned assertion, not on a missing symbol"

requirements-completed: [IOSDATA-01, IOSDATA-03]

coverage:
  - id: D1
    description: "Every bookmark write goes through one ViewModel intent that returns the post-toggle value, and both analytics call sites pass it"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#toggle_bookmark_returns_the_post_toggle_value"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#toggling_a_favorite_writes_the_legacy_key_and_notifies_observers"
        status: pass
    human_judgment: false
  - id: D2
    description: "Every filter write goes through toggleFilter(type:value:) in all five menus, legacy JSON persistence unchanged"
    requirement: IOSDATA-03
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#toggling_a_filter_twice_restores_the_previous_set"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#filters_are_saved_as_json_under_the_legacy_key"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#clear_filters_empties_the_set_and_removes_the_key"
        status: pass
    human_judgment: false
  - id: D3
    description: "The filter menus and list/detail bookmark controls look and behave exactly as in v1.0 (behavior-preserving, no visual check run)"
    verification: []
    human_judgment: true
    rationale: "Only the Button action bodies changed; no screenshot parity was captured in this plan. The D-10 checkpoint in 09-09 is the on-device proof."

duration: 25min
completed: 2026-11-07
status: complete
---

# Phase 9 Plan 08: Agenda intents Summary

**Bookmark and filter writes now live behind `isBookmarked`, `toggleBookmark(sessionId:)` and `toggleFilter(type:value:)` on `AgendaViewModel`, with the list row, detail star and all five filter menus calling them, still on the legacy UserDefaults keys.**

## Performance

- **Duration:** about 25 min
- **Started:** 2026-11-07T16:00:00Z (approx.)
- **Completed:** 2026-11-07T16:20:00Z (approx.)
- **Tasks:** 2 (both TDD: RED, GREEN, no REFACTOR)
- **Files modified:** 10

## Accomplishments

- `toggleBookmark(sessionId:)` returns the value after the toggle; `AgendaView.agendaRow` and the `AgendaDetailView` toolbar bind it and pass it to `eventBookmark`, so the analytics event keeps reporting the post-toggle value (D-05, T-09-10).
- `toggleFilter(type:value:)` replaced the copy-mutate-reassign block in Room, Language, Complexity, SessionType and Favorites menus; each menu changed only inside its `Button` action. 09-09 can now change the parameter type without touching a menu.
- `favorites` and `sessionFilters` are `private(set)`; `toggleFavorite(ofSession:)` and its two helpers are gone. Persistence code (keys, formats, `didSet`) is untouched (T-09-09, prohibition on persistence changes respected).

## Task Commits

1. **Task 1 (tracer): bookmark intents** - RED `f1a2d3ef` (test), GREEN `0850df86` (feat)
2. **Task 2: filter intent, five menus, full suite** - RED `41bb070e` (test), GREEN `93a39c27` (feat)

**Plan metadata:** the docs commit that adds this file.

## TDD Gate Compliance

- **Task 1 RED:** `AgendaViewModelTests` run on the iPhone 17 simulator, 13 tests, 2 failed. `toggle_bookmark_returns_the_post_toggle_value` failed on `toggleBookmark(sessionId: "s1") == true` (got false) and `isBookmarked("s1") == true`; `toggling_a_favorite_writes_the_legacy_key_and_notifies_observers` failed on the notification and on the `Favorites` key being nil. Read from the xcresult with `xcresulttool` (target tests present, failing on the planned assertions, other 11 passed). Semantic assessment: valid RED, the target tests executed and failed for the intended reason.
- **Task 1 GREEN:** same suite, 13 of 13 passed, `XCRESULT-OK`. Tracer gate: interactive mode, `end-of-phase`, automated-only verify, re-run passed, expansion started.
- **Task 2 RED:** 14 tests, 3 failed: `toggling_a_filter_twice_restores_the_previous_set` (set stayed empty), `filters_are_saved_as_json_under_the_legacy_key` and `clear_filters_empties_the_set_and_removes_the_key` (nothing written). The other 11 passed. Valid RED.
- **Task 2 GREEN:** full suite, 71 of 71 passed, `XCRESULT-OK` (plan floor 70).
- No REFACTOR commit: nothing worth restructuring.

## Files Created/Modified

- `AgendaViewModel.swift` - three intents, `private(set)` on both state properties, old toggle removed
- `AgendaView.swift`, `AgendaDetailView.swift` - read `isBookmarked`, bind `toggleBookmark` for analytics
- `RoomFilterMenu.swift`, `LanguageFilterMenu.swift`, `ComplexityFilterMenu.swift`, `SessionTypeFilterMenu.swift`, `FavoritesFilterMenu.swift` - Button action is one `toggleFilter` call; lists, labels, order, `isSelected` untouched; Rooms still built from `Array(rooms)` of a Swift `Set` (Open Question 2, kept for parity)
- `AgendaViewModelTests.swift` - updated 3 tests, added 2
- `AgendaCharacterizationTests.swift` - doc comment only (see deviations)

## Decisions Made

See `key-decisions` in the frontmatter. No decision departs from the plan.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Compile-only stubs in the RED commits**
- **Found during:** Task 1 and Task 2 RED
- **Issue:** the new tests call symbols that do not exist yet; in Swift that is a build failure, which the TDD reference classes as invalid RED (not a failure on the planned assertion).
- **Fix:** each RED commit adds the signature as a stub (`isBookmarked`/`toggleBookmark` returning false, `toggleFilter` empty), so the tests compile and fail on the assertion. The GREEN commits replace the stubs.
- **Files modified:** `AgendaViewModel.swift`
- **Committed in:** `f1a2d3ef`, `41bb070e`

**2. [Rule 3 - Blocking] Doc comment in `AgendaCharacterizationTests.swift` named `toggleFavorite`**
- **Found during:** Task 1 acceptance check
- **Issue:** the plan's verify (`! grep -rn 'toggleFavorite' iosApp/iosApp iosApp/DevFestNantesTests`) matched a comment in a file the plan does not list.
- **Fix:** comment reworded to name `toggleBookmark`, `toggleFilter` and `clearFilters`. No code change.
- **Committed in:** `0850df86`

---

**Total deviations:** 2 auto-fixed (both Rule 3, no behavior impact)
**Impact on plan:** none on scope or behavior.

## Issues Encountered

- The worktree command guard refused the plan's compound `<automated>` shell. Each block was run as its component steps (a scratchpad script for the simulator boot and `xcodebuild ... test` with `-collect-test-diagnostics never`, then `scripts/xcresult-assert-tests.sh`, `xcresulttool` + `jq` suite and test-name checks, and the greps with `/usr/bin/grep`), with identical checks and outcomes. Task 2's separate `build-for-testing` / `test-without-building` pair was run as a single `test` invocation on the same destination and derived data. The Gradle gates ran one per command.
- `xcodebuild` prints `error: the following command failed with exit code 0 but produced no further output` lines on every build, also on passing runs. Known noise, exit code 0 and `XCRESULT-OK` are the signal.

## Verification Results

| Check | Result |
|-------|--------|
| `AgendaViewModelTests` after Task 1 | 13/13, suite listed as Passed |
| Full iOS suite after Task 2 | 71/71, `XCRESULT-OK` |
| No `toggleFavorite`, no `viewModel.favorites` in detail view, one `toggleBookmark(sessionId:` call in each view | pass |
| No `viewModel.sessionFilters = `, no `newFilters` in `Agenda/`, 5 menus use `toggleFilter(type:` | pass |
| `swift-names-gate check` / `selftest` | `SWIFT-NAMES-OK` / `SWIFT-GATE-SELFTEST-OK` |
| `swift-observation-gate` | `SWIFT-OBS-GATE-OK` |
| `kmp-nc-version-gate` | `KMPNC-VERSION-OK 1.0.6` |
| `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` | BUILD SUCCESSFUL |

## Known Stubs

None. The RED-commit stubs were replaced in the GREEN commits.

## Threat Flags

None. No new network, auth or storage surface.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Ready for 09-09: bookmark and filter writes cross only the three intents, so the store switch changes the ViewModel internals and the `toggleFilter` parameter type, not any menu body. `AgendaView.filteredSessionsForSection` still reads `viewModel.favorites` and `viewModel.sessionFilters` directly, to be handled in 09-09.

## Self-Check: PASSED

- Commits `f1a2d3ef`, `0850df86`, `41bb070e`, `93a39c27` exist on the branch.
- All ten modified files exist; no new files created.
- Working tree clean before this file; no `xcodebuild` process left running.

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-07*
