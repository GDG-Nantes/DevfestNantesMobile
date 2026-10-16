---
phase: 05-test-coverage-retrofit
plan: 07
subsystem: testing
tags: [viewmodel-testing, turbine, agenda-filters, specification-tests, bookmarks, analytics-fake]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-03 named session fixtures, 05-06 ViewModel harness (MainDispatcherRule, FakeDevFestNantesStore, Turbine, injected AgendaViewModel IO dispatcher)"
provides:
  - "FakeSessionFiltersService (agenda test sources)"
  - "AgendaViewModelTest and AgendaViewModelStartupTest: filter matrix and reactivity as specifications against fixtures"
  - "BookmarksViewModelTest and HomeViewModelTest"
  - "FakeAnalyticsService.bookmarkEvents / BookmarkEvent recording in :core:testing (common code)"
  - "deferred items 9-12 logged in deferred-items.md"
affects: [05-08, 05-10]

requirements-completed: [TEST-03]

key-files:
  created:
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/FakeSessionFiltersService.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModelTest.kt
    - core/ui/src/test/java/com/gdgnantes/devfest/core/ui/BookmarksViewModelTest.kt
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModelTest.kt
  modified:
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeAnalyticsService.kt
    - .planning/phases/05-test-coverage-retrofit/deferred-items.md

key-decisions:
  - "Ties on start time are asserted as a set, never as an order, because the filter goes through a HashSet before sortedBy"
  - "rooms emission order (emptySet, then rooms) is asserted in a StandardTestDispatcher class; with the default Unconfined rule the lazy stateIn already holds the rooms at first collection"
  - "The store failure of AgendaViewModel is pinned (uncaught IllegalStateException surfaced by runTest) and logged, mirroring SpeakersViewModel from 05-06"

patterns-established:
  - "Filter rules are written as named specifications: fixture constants for every expected id, rule stated in the test name"
  - "A feature-owned service fake stays in the feature's test sources; other modules write a private local fake"

coverage:
  - id: D1
    description: "Agenda filter rules (each FilterType alone, OR within a type, intersection across types, missing type/room/language never matching, bookmark filter, day sorting) are executable specifications against named fixtures"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "feature/agenda AgendaViewModelTest (20 tests); AGENDA-SPEC-GREEN"
        status: pass
    human_judgment: false
  - id: D2
    description: "Agenda state reacts to store updates, filter changes (Turbine order), bookmark changes, onSessionFiltersChanged forwarding, rooms and refresh; store failure characterized"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "AgendaViewModelTest.filter_change_emits_the_filtered_days, new_store_sessions_replace_the_agenda, on_refresh_keeps_the_loaded_agenda, store_failure_is_characterized; AgendaViewModelStartupTest (2 tests)"
        status: pass
    human_judgment: false
  - id: D3
    description: "HomeViewModel.isAgendaFiltered follows the filters and pins WhileSubscribed(5000)'s initial value; BookmarksViewModel mirrors the store and records one bookmark event per call"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "androidApp HomeViewModelTest (3 tests), core/ui BookmarksViewModelTest (4 tests); FILTER-BOOKMARK-VMS-GREEN"
        status: pass
    human_judgment: false
  - id: D4
    description: "No production file changed; exposed bugs pinned and deferred (D-10)"
    requirement: TEST-03
    verification:
      - kind: other
        ref: "git status --porcelain on feature/agenda/src/main, core/ui/src/main, androidApp/src/main printed nothing before each commit; deferred-items.md items 9-12"
        status: pass
    human_judgment: false
  - id: D5
    description: "CI green on the final code HEAD"
    requirement: TEST-03
    verification:
      - kind: other
        ref: "android.yml run 38873089664 (green on the third attempt, see Issues Encountered) and ios.yml run 38873089492 on b1e7c8d, both success"
        status: pass
    human_judgment: false

duration: 45min
completed: 2026-10-16
status: complete
actuals:
  tokens: 7000
  tasks: 2
  commits: 3
plan_head_before: 59847f8698b21c3d494fea8138334f715c0012b1
plan_head_after: b1e7c8de8a5e27d9c81a5cd1852a8e92f8888bb5
commits: 3
---

# Phase 5 Plan 07: Agenda Filter Matrix and Home/Bookmarks ViewModel Tests Summary

**The agenda filtering rules (the app's main business logic, previously untested) now read as 20 specification tests against named session fixtures, plus tests for `HomeViewModel` and `BookmarksViewModel`, with bookmark-event recording added to the shared `FakeAnalyticsService`.**

## Performance

- **Duration:** about 45 min including three CI attempts
- **Tasks:** 2 (both `tdd="true"`, characterization of existing behavior)
- **Files:** 6 changed or added, no production file

## Accomplishments

- `AgendaViewModelTest` (20 tests) covers: the loaded two-day agenda and SUCCESS state, no filter, French, BEGINNER, CONFERENCE (excludes the session without a type), titan room, two rooms ORed, two languages ORed, French + ADVANCED (only `Les secrets du rendu web`), CODELAB + French (both days present and empty), the bookmark filter (and adding or removing a bookmark afterwards), bookmarks having no effect without a BOOKMARK filter, sessions without a language never matching LANGUAGE, start-date ordering per day, Turbine-asserted filter change emission, `onSessionFiltersChanged` forwarding, new store sessions replacing the agenda, `onRefresh` keeping the agenda, and the store failure characterization.
- `AgendaViewModelStartupTest` (StandardTestDispatcher) asserts `STARTING` then `SUCCESS` and the `rooms` emissions `emptySet`, store rooms, then a pushed update.
- `FakeSessionFiltersService(initial)` lives in the agenda test sources (feature-owned interface, RESEARCH Pitfall 11).
- `FakeAnalyticsService` now records `BookmarkEvent(page, sessionId, bookmarked)` in `bookmarkEvents`; every other method stays a no-op. `:core:testing` jvmTest and iosSimulatorArm64Test pass.
- `BookmarksViewModelTest` (4 tests): `subscribe` starts false and follows the store (and ignores other sessions), `setBookmarked` updates the store and records exactly one event with page, id and state, unbookmarking records a second event with `false`.
- `HomeViewModelTest` (3 tests, local `SessionFiltersService` fake because `:androidApp` cannot see the agenda test sources): not filtered by default, false/true/false under Turbine, and `.value` stays false with no subscriber (pins `WhileSubscribed(5000)`).
- `deferred-items.md` now carries the 05-06 hand-over (item 9, `SpeakersViewModel` store failure) and the new items 10-12.

## Task Commits

1. Task 1: `f9fa661` test(05-07): agenda filter matrix as specifications against fixtures
2. Task 1 follow-up: `30a3277` test(05-07): name the web rendering fixture id in the french+advanced spec (makes the plan's `SessionFixtures.webRendering.id` acceptance literal true)
3. Task 2: `b1e7c8d` test(05-07): home and bookmarks ViewModel tests

## Verification

- AGENDA-SPEC-GREEN: `:feature:agenda:testDebugUnitTest detekt` green; `french_and_advanced_returns_only_les_secrets_du_rendu_web` and `bookmark_filter_keeps_only_bookmarked_sessions` present in the results; `SpeakersViewModel` present in `deferred-items.md`. Acceptance: 22 `@Test` in `AgendaViewModelTest.kt` (at least 15), `SessionFixtures.webRendering.id` referenced.
- FILTER-BOOKMARK-VMS-GREEN: `:core:testing:jvmTest :core:testing:iosSimulatorArm64Test :core:ui:testDebugUnitTest :androidApp:testDebugUnitTest :feature:agenda:testDebugUnitTest detekt` green, DI-GATE-OK, `BookmarksViewModelTest` and `HomeViewModelTest` present in the results.
- `git status --porcelain` on the three main source sets printed nothing before the commits (T-5-15).
- CI-GREEN-BOTH on `b1e7c8d`: android.yml run 38873089664 (after reruns) and ios.yml run 38873089492.

## TDD Notes

Both tasks characterize existing behavior, so there is no RED commit: the tests pass on first run against unchanged production code, the expected state for pinned behavior. The only red moment was the `rooms_exposes_the_store_rooms` test, whose expected emission order was wrong for the Unconfined rule (see Deviations).

## Deviations from Plan

**1. [Rule 1 - Bug in test] `rooms_exposes_the_store_rooms` moved to the StandardTestDispatcher class**
- **Found during:** Task 1
- **Issue:** With the default Unconfined `MainDispatcherRule`, the Lazily started `stateIn` has already collected the store rooms when Turbine subscribes, so the first item is the full set, not `emptySet()`.
- **Fix:** Moved the test to `AgendaViewModelStartupTest` (Standard dispatcher), where `emptySet`, then the store rooms, is observable; added a pushed-update assertion.
- **Files modified:** AgendaViewModelTest.kt
- **Commit:** f9fa661

**2. [Plan acceptance] Extra commit for the `SessionFixtures.webRendering.id` literal**
- **Issue:** The first version compared against `idsOf(SessionFixtures.webRendering)`, which does not contain the literal the acceptance criterion greps for.
- **Fix:** `french_and_advanced...` now asserts `setOf(SessionFixtures.webRendering.id)`.
- **Commit:** 30a3277

**3. [Scope] Added tests beyond the `<behavior>` list:** `two_language_filters_are_ored`, `unbookmarking_removes_the_session_under_a_bookmark_filter`, `bookmarks_do_not_affect_the_agenda_without_a_bookmark_filter` and `subscribe_ignores_bookmarks_of_other_sessions`, all specifications of rules the plan describes in prose.

**Total deviations:** 1 auto-fixed test defect, 1 acceptance adjustment, 1 scope addition. **Impact:** none on production code.

## Issues Encountered

- **CI instrumentation job flaked on D8 `OutOfMemoryError: Java heap space`** (android.yml run 38873089664, attempts 1 and 2; attempt 3 green after `gh run rerun --failed`). The failure is not caused by this plan: `assembleDebugAndroidTest` reproduced it locally on the 05-06 commit `7182cc9` (first run red, second run green) and on this HEAD (first red, second green). Logged as deferred item 12; no build configuration was changed.

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None new. T-5-15 mitigated: no production file was modified and the `git status --porcelain` checks on the main source sets were empty; T-5-SC: no dependency added.

## Deferred items logged

- 9: `SpeakersViewModel` does not handle a failing `store.speakers` flow (pinned by `SpeakersViewModelTest.store_failure_is_characterized`).
- 10: `AgendaViewModel` does not handle a failing `store.agenda` flow (pinned by `AgendaViewModelTest.store_failure_is_characterized`).
- 11: sessions sharing a start time come out in an unspecified order (characterized by `AgendaViewModelTest.sessions_within_each_day_are_sorted_by_start_date`).
- 12: intermittent D8 OOM in the instrumentation build (CI flake).

## Next Phase Readiness

Ready for 05-08. Three of the ten ViewModels are covered with the deepest logic (agenda filters) fully specified. `FakeAnalyticsService.bookmarkEvents` and the `FakeSessionFiltersService` pattern are available to later plans.

## Self-Check: PASSED

Created files exist on disk; commits f9fa661, 30a3277 and b1e7c8d are on the branch (3 measured from the plan ledger, base 59847f8); AGENDA-SPEC-GREEN, FILTER-BOOKMARK-VMS-GREEN, DI-GATE-OK and CI-GREEN-BOTH observed.
