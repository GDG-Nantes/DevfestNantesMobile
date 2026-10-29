---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 14
subsystem: android-di
tags: [android, koin, adoption, bridge]
requires:
  - phase: 07-10
    provides: Koin graph tests and androidModules wiring
  - phase: 07-13
    provides: SessionFiltersStore interface in core:data and the temporary full-graph extra type
provides:
  - SessionFiltersService as a SessionFiltersStore (bridge until 07-16)
  - agendaModule binding of the single service instance under both types
  - AgendaViewModel and HomeViewModel consuming SessionFiltersStore
affects: [07-15, 07-16]
tech-stack:
  added: []
  patterns: ["one Koin definition bound under two types (binds arrayOf) as a no-second-holder bridge"]
key-files:
  created: []
  modified:
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersService.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModel.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModuleTest.kt
    - feature/agenda/src/androidTest/java/com/gdgnantes/devfest/feature/agenda/AgendaSmokeTest.kt
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModelTest.kt
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt
key-decisions:
  - "Bridge via a supertype plus a secondary Koin binding type on the existing single definition; no second SessionFiltersStore instance or production binding (T-07-30)"
  - "Smoke test re-binds SessionFiltersStore to the real service in its last module so the androidTestDoubles fake does not win"
requirements-completed: [SHARED-06, SHARED-05]
status: complete
duration: 10 min
completed: 2026-10-29
commits: 1
plan_head_before: 8ab97399c67ab68fc95a4da98f428f6f22a3dfa2
plan_head_after: ecea6a295f6864e5267fcaa17ee6385e1809c755
actuals:
  tokens: 6000
  tasks: 1
  commits: 1
---

# Phase 7 Plan 14: SessionFiltersStore bridge Summary

**The v1.0 `SessionFiltersService` now extends `SessionFiltersStore` and is bound under both types from one Koin definition, and `AgendaViewModel` and `HomeViewModel` depend on the shared `SessionFiltersStore` interface with unchanged persistence.**

## Accomplishments

- `interface SessionFiltersService : SessionFiltersStore` (members inherited; `SessionFiltersServiceImpl` body, preference key, JSON and Timber lines untouched).
- `agendaModule`: `single { SessionFiltersServiceImpl(get()) } binds arrayOf(SessionFiltersService::class, SessionFiltersStore::class)`; exactly one production `SessionFiltersStore::class` binding, one holder of `SHARED_PREFERENCES_KEY_AGENDA_FILTERS`.
- `AgendaViewModel(..., sessionFiltersStore: SessionFiltersStore, ...)` and `HomeViewModel(sessionFiltersStore: SessionFiltersStore)`; no remaining `SessionFiltersService` reference in `androidApp/src/main` or `AgendaViewModel`.
- Tests, wiring only (D-18): `FakeFilters` and the `AgendaModuleTest` double implement `SessionFiltersStore`; `AgendaSmokeTest` re-binds `SessionFiltersStore` to the real service in its last module; `AndroidGraphVerifyTest` lists `SessionFiltersStore::class` instead of `SessionFiltersService::class` in the upstream types and no longer carries the temporary 07-13 full-graph extra type.

## Task Commits

1. Task 1: `ecea6a2` refactor(07-14): put the agenda and home ViewModels on the SessionFiltersStore interface

## Verification

- `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :feature:agenda:compileDebugAndroidTestKotlin`: pass (AndroidGraphVerifyTest 5/5, AgendaModuleTest 3/3, HomeViewModelTest and KoinStartupTest pass).
- `bash scripts/di-gate.sh`: DI-GATE-OK. `bash scripts/kmp-nc-version-gate.sh`: KMPNC-VERSION-OK 1.0.6.
- `./gradlew detekt lint` and `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`: pass.
- Single production binding check: `git grep -c 'SessionFiltersStore::class' -- feature/agenda/src/main core/data/src/androidMain` returns only `AgendaModule.kt:1`.
- Acceptance criteria:
  - D-18 diff check (commit `ecea6a2`): test diffs contain only imports, fake supertypes, Koin test bindings, the verify extra-type list and a comment; no assertion, key literal, JSON literal or ordering change.
  - `AgendaViewModelTest.kt` is not in the commit (the `git show --stat` lists no file); the 07-06 characterizations are untouched.
  - `git grep -n 'class SessionFiltersServiceImpl' -- feature/agenda/src/main` returns one line.
- The androidTest smoke test was compiled but not run on a device (the plan only requires compilation).

## Deviations from Plan

None - plan executed exactly as written. The infix `binds` call is wrapped on two lines to respect the 120-column limit.

## Known Stubs

None.

## Threat Flags

None. T-07-30 mitigated: one definition, two types, `allowOverride(false)` unchanged, graph tests and DI gate green.

## Next Phase Readiness

Ready for 07-15 (`ObserveAgendaUseCase` can resolve a `SessionFiltersStore` on Android) and 07-16 (the atomic switch only removes the service and its binding and adds the `:core:data` binding; no consumer edit left).

## Self-Check: PASSED

- All 8 modified files exist; commit `ecea6a2` is an ancestor of HEAD.
