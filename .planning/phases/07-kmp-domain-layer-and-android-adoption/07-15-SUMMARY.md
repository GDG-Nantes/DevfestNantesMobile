---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 15
subsystem: android-agenda
tags: [android, viewmodel, compose, adoption]
requires:
  - phase: 07-13
    provides: ObserveAgendaUseCase, DaySchedule/TimeSlot, domainModule and domainDispatcher
  - phase: 07-14
    provides: AgendaViewModel on the SessionFiltersStore interface
provides:
  - AgendaViewModel on ObserveAgendaUseCase with days as List<DaySchedule>
  - Agenda UI (Agenda, AgendaPager, AgendaColumn) rendering DaySchedule/TimeSlot with the v1.0 label, order and scroll index
  - Removal of groupSessionsByStartTime, FakeSessionFiltersService and the IO_DISPATCHER binding
affects: [07-16, 07-17, 07-19]
tech-stack:
  added: []
  patterns: ["ViewModel collects a shared use case with an explicit TimeZone.currentSystemDefault()"]
key-files:
  created: []
  modified:
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/Agenda.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaPager.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaColumn.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/di/CoreUiModule.kt
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModule.kt
    - documentation/ARCHITECTURE.md
    - feature/agenda/src/test (AgendaViewModelTest, CurrentStartTimeIndexTest, AgendaModuleTest, three characterization tests)
    - feature/agenda/src/androidTest/java/com/gdgnantes/devfest/feature/agenda/AgendaSmokeTest.kt
  deleted:
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/FakeSessionFiltersService.kt
key-decisions:
  - "Slots without a label (no system locale) are dropped in AgendaColumn, as v1.0 dropped every session then; formatSlotTime returns an empty string in that case"
  - "currentStartTimeIndex takes the end date of the first session by room name of each slot so the scroll index equals v1.0 (shared slot order is sortIndex then name)"
  - "agenda ViewModel runs on the domainDispatcher (Dispatchers.Default); IO_DISPATCHER removed because nothing consumes it"
requirements-completed: [SHARED-06, SHARED-02, SHARED-03]
status: complete
duration: 25 min
completed: 2026-10-29
commits: 1
plan_head_before: 2daee096a9e26bb024c6038dd3a1ca5c824c493c
plan_head_after: 70589637314c88128e999de6602f5336f9c20e86
actuals:
  tokens: 40000
  tasks: 1
  commits: 1
---

# Phase 7 Plan 15: Android agenda on the shared domain Summary

**`AgendaViewModel` now collects `ObserveAgendaUseCase` (UtcDayWindows for the two 2025 days, device zone passed explicitly) and the agenda composables render `DaySchedule`/`TimeSlot` with the v1.0 time label, room order and scroll index; the v1.0 grouping, the feature fake and the dead `IO_DISPATCHER` binding are gone.**

## Accomplishments

- `AgendaViewModel(observeAgenda, store, sessionFiltersStore, performanceMonitoring)`: `days: StateFlow<List<DaySchedule>>` starting with two empty days, `onRefresh` cancel-and-restart inside `traceDataLoading`, `uiState`, `rooms` and `sessionFilters` unchanged, store failures still uncaught (D-04). `filterSessions`, `updateFilteredDays` and the IO dispatcher parameter deleted.
- `Agenda`/`AgendaPager` take `List<DaySchedule>` (page `n` shows `dayIndex == n + 1`); `AgendaColumn(slots, onSessionClick)` with `formatSlotTime` (v1.0 short-time formatter, device zone and locale), header key `slot.start.toEpochMilliseconds()`, rows in the shared order, and `currentStartTimeIndex(slots, now)` returning the v1.0 index.
- `agendaModule`: `viewModel { AgendaViewModel(get(), get(), get(), get()) }`; `coreUiModule` keeps only `BookmarksViewModel`.
- Tests, wiring only (D-18): constructor wiring, `FakeSessionFiltersStore` from `:core:testing`, day selectors over `List<DaySchedule>`, `TimeSlot` lists in `CurrentStartTimeIndexTest`, Koin lists in `AgendaModuleTest` (`production_io_dispatcher_binding_is_dispatchers_io` deleted with its subject, resolution test renamed `agenda_view_model_resolves_on_the_domain_module`), `labelGroups` selector helper in `AgendaGroupingCharacterizationTest`, `AgendaSmokeTest` adds `domainModule`.

## Task Commits

1. Task 1: `70589637` refactor(07-15): move the Android agenda onto the shared domain flow

## Verification

- `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :core:ui:testDebugUnitTest :feature:agenda:compileDebugAndroidTestKotlin`: pass. Agenda results: AgendaViewModelTest 20/20, AgendaViewModelStartupTest 2/2, CurrentStartTimeIndexTest 5/5, AgendaModuleTest 2/2, AgendaGroupingCharacterizationTest 3/3, AgendaFilteringCharacterizationTest 4/4, SessionFilterEqualityCharacterizationTest 2/2, SessionFiltersDrawer/SessionClassification characterizations and SessionFiltersServiceImplTest pass.
- `bash scripts/di-gate.sh`: DI-GATE-OK. `bash scripts/kmp-nc-version-gate.sh`: KMPNC-VERSION-OK 1.0.6.
- `./gradlew detekt lint` and `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`: pass.
- `git grep` for `IO_DISPATCHER`, `groupSessionsByStartTime` and `FakeSessionFiltersService` in `*.kt`: no match after the comment fix below.
- Acceptance criteria:
  - D-18 diff check (commit `70589637`): `AgendaViewModelTest.kt` diff touches no `idsOf(` or expected `assertEquals(` argument; the only changed assertion lines are the `days.value.keys` selector (now `map { it.dayIndex }.toSet()`). No assertion added or removed in the three characterization files (every `assert*(` call is a context line of the diff; the changes are imports, constructor wiring and selector helpers). No file under `core/domain/testdata` in the commit; goldens passed unchanged.
  - `class SessionFiltersServiceImpl` still declared once in `feature/agenda/src/main` (holder unchanged until 07-16).
- The androidTest smoke test was compiled, not run on a device.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Remaining `IO_DISPATCHER` mention in a comment outside the plan file list**
- **Found during:** Task 1 verify (the `git grep -e IO_DISPATCHER -- '*.kt'` check would fail)
- **Issue:** `DomainModule.kt` (07-13) had a KDoc line naming `IO_DISPATCHER`.
- **Fix:** reworded the comment; no code change.
- **Files modified:** `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModule.kt`
- **Commit:** `70589637`

**2. [Rule 2 - Docs] ARCHITECTURE.md described the removed dispatcher and the old VM inputs**
- **Found during:** Task 1 (AGENTS.md: update docs in the same change)
- **Fix:** updated the `:core:ui`, agenda data-flow and dispatcher paragraphs of `documentation/ARCHITECTURE.md`.
- **Commit:** `70589637`

**3. [Plan detail] `AgendaModuleTest` verify extra types**
- `BookmarksStore` and `CoroutineDispatcher` were removed from the extra-type list (the module no longer needs them) and `ObserveAgendaUseCase` added.

**Total deviations:** 3 minor (1 blocking comment fix, 1 docs, 1 test list tidy). **Impact:** none on behavior.

`.planning/codebase/ARCHITECTURE.md` still mentions `IO_DISPATCHER` (codebase map, left for the orchestrator's map refresh).

## Known Stubs

None.

## Threat Flags

None.

## Self-Check: PASSED

- Commit `70589637` is on the branch; `FakeSessionFiltersService.kt` deleted; modified files present.
