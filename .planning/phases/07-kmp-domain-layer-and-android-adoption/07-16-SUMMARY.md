---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 16
subsystem: android-persistence
tags: [android, koin, persistence, atomic-switch]
requires:
  - phase: 07-14
    provides: AgendaViewModel and HomeViewModel depend on SessionFiltersStore
  - phase: 07-15
    provides: Android agenda collects ObserveAgendaUseCase
  - phase: 07-09
    provides: internal SessionFilterCodec in :core:data
provides:
  - SharedPreferencesSessionFiltersStore (Android SessionFiltersStore in :core:data androidMain)
  - single SessionFiltersStore binding in the Android data platform Koin module
affects: [07-17, 07-19]
tech-stack:
  added: []
  patterns: ["stateful store bound once in the :core:data platform module, never in a feature module"]
key-files:
  created:
    - core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/SharedPreferencesSessionFiltersStore.kt
    - core/data/src/androidHostTest/kotlin/com/gdgnantes/devfest/core/data/SharedPreferencesSessionFiltersStoreTest.kt
  modified:
    - core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.android.kt
    - core/data/build.gradle.kts
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModuleTest.kt
    - feature/agenda/src/androidTest/java/com/gdgnantes/devfest/feature/agenda/AgendaSmokeTest.kt
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt
    - documentation/ARCHITECTURE.md
    - documentation/DEVELOPMENT.md
  removed:
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersService.kt (git mv to the store above)
key-decisions:
  - "Android filters store is public (the agenda smoke test builds it over its own test preferences) and keeps the v1.0 Timber error lines and key constant"
  - "KoinStartupTest comment now states the test double replaces the SessionFiltersStore binding, so the production SharedPreferencesSessionFiltersStore is covered by ProductionGraphStartupTest and its own host test"
requirements-completed: [SHARED-06, SHARED-05]
status: complete
commits: 2
plan_head_before: ed4f9a89b7196e6f2006567cdae5da5f45f797b8
plan_head_after: 13ee1e07
actuals:
  tokens: 9000
  tasks: 2
  commits: 2
duration: 20 min
completed: 2026-10-29
---

# Phase 7 Plan 16: Atomic switch to the :core:data Android filters store Summary

The `SessionFiltersService` holder is replaced by `SharedPreferencesSessionFiltersStore` in `:core:data`, bound as the only `SessionFiltersStore` single in the Android platform module, in one commit with the v1.0 persistence test moved over unchanged.

## Accomplishments

- Task 1 (a28bf160): `git mv` of the service to `SharedPreferencesSessionFiltersStore` (inline `Json` replaced by `SessionFilterCodec.encode/decode`; same key `SHARED_PREFERENCES_KEY_AGENDA_FILTERS`, same catches and Timber messages); `single { SharedPreferencesSessionFiltersStore(get()) } bind SessionFiltersStore::class` in `DataPlatformModule.android.kt`; `agendaModule` now only declares the ViewModel; `timber` added to `:core:data` androidMain. The persistence test moved to `core/data/src/androidHostTest` with only package, class name, constructor and KDoc-name edits (4 tests pass; `BookmarksStoreImplTest` still green). `AgendaModuleTest` adds `SessionFiltersStore` as an upstream extra type; `AgendaSmokeTest` builds the real store over its test preferences; `KoinStartupTest` comment updated.
- Task 2 (13ee1e07): ARCHITECTURE.md (data section, agenda data flow, state management) and DEVELOPMENT.md (test table) describe the new holder; no doc names the removed service or the IO dispatcher.
- Full gate green: `./gradlew detekt lint`, `scripts/di-gate.sh` (DI-GATE-OK), `scripts/kmp-nc-version-gate.sh` (KMPNC-VERSION-OK 1.0.6), `testDebugUnitTest jvmTest testAndroidHostTest`, and `:feature:agenda:connectedDebugAndroidTest` on the Pixel_9 emulator (`AgendaSmokeTest`: 2 tests, 0 failures).

## Deviations from Plan

None - plan executed exactly as written. (The precondition for Task 2 was unmet at start, no device connected; the plan's own remedy `android emulator start Pixel_9` was applied.)

## Known Stubs

None.

## Self-Check: PASSED

- FOUND: core/data/src/androidMain/.../SharedPreferencesSessionFiltersStore.kt, core/data/src/androidHostTest/.../SharedPreferencesSessionFiltersStoreTest.kt
- FOUND commits: a28bf160, 13ee1e07
- `bind SessionFiltersStore::class` appears once under core/data/src/androidMain; no Kotlin file references the removed service; di-gate OK; core/domain/testdata untouched.
