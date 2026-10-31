---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 13
subsystem: domain
tags: [kmp, domain, flow, koin, nativecoroutines]
requires:
  - phase: 07-11
    provides: iOS BookmarksStore and SessionFiltersStore bindings
  - phase: 07-12
    provides: FilterSessionsUseCase, GroupSessionsUseCase, DayAssignment
provides:
  - SpeakerIndexUseCase (letterGroups, firstIndexes) matching both v1.0 goldens
  - VenueLanguageUseCase.languageFor
  - ObserveAgendaUseCase.observe(dayAssignment, timeZone) (@NativeCoroutines)
  - final domainModule with the named domainDispatcher
  - domain layer and behavior deviations documentation
affects: [07-14, 07-15, 07-16, 07-18, phase-09]
tech-stack:
  added: []
  patterns: ["combine(three sources) built per call + flowOn(injected dispatcher)", "named CoroutineDispatcher single in domainModule"]
key-files:
  created:
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/speakers/SpeakerIndexUseCase.kt
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/venue/VenueLanguageUseCase.kt
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/ObserveAgendaUseCase.kt
    - core/domain/src/commonTest/kotlin/com/gdgnantes/devfest/core/domain/speakers/SpeakerIndexUseCaseTest.kt
    - core/domain/src/commonTest/kotlin/com/gdgnantes/devfest/core/domain/venue/VenueLanguageUseCaseTest.kt
    - core/domain/src/commonTest/kotlin/com/gdgnantes/devfest/core/domain/agenda/ObserveAgendaUseCaseTest.kt
  modified:
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModule.kt
    - core/domain/src/jvmTest/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModuleTest.kt
    - shared/src/jvmTest/kotlin/com/gdgnantes/devfest/shared/di/KoinGraphTest.kt
    - shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt
    - documentation/ARCHITECTURE.md
    - documentation/DEVELOPMENT.md
    - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.ios.kt
key-decisions:
  - "Speaker index shared: both goldens matched on the real snapshot, so the decision gate did not trip"
  - "Dispatchers.Default bound as domainDispatcher inside domainModule (not IO_DISPATCHER)"
  - "No distinctUntilChanged, shareIn or cache in the combined flow; the ViewModel keeps UiState, tracing, refresh and errors (D-04)"
requirements-completed: [SHARED-01, SHARED-04, SHARED-03, SHARED-07]
duration: ~50 min
completed: 2026-10-29
status: complete
actuals:
  tokens: 30000
  tasks: 2
  commits: 3
plan_head_before: 6d5756dd7b6423d8991acf0c180eb67e463fda80
plan_head_after: 60bd0a8e280258a71a74ec655e93103538fe8c05
commits: 3
---

# Phase 7 Plan 13: Shared domain completion Summary

Speaker A-Z index and venue language shared and proven against both v1.0 platforms, the combined `ObserveAgendaUseCase` flow with a named dispatcher in the final `domainModule`, Koin graph proofs on JVM/Android/iOS, and the domain layer plus the v1.1 behavior deviations documented.

## What was built

- `SpeakerIndexUseCase`: `letterGroups` (letter = `name.take(1).uppercase()`, sorted letters, store-ordered speakers) reproduces `golden/ios-speaker-index.json`; `firstIndexes` (first-appearance order) reproduces `golden/android-speaker-index.json` once letters are lowercased. Both pass on JVM and iOS simulator on the recorded snapshot; the decision gate (checkpoint) was not needed.
- `VenueLanguageUseCase.languageFor(code)`: `"fr"` is FRENCH, everything else (including null, empty, `"FR"`, `"fr-FR"`) ENGLISH.
- `ObserveAgendaUseCase.observe(dayAssignment, timeZone)`: `combine(store.sessions, filters, bookmarkedSessionIds)` then `group(filter(...))`, `flowOn(dispatcher)`, built inside each call. 8 tests (JVM + iOS): first emission, time zone pass-through (Paris vs Auckland), filter change, bookmark toggle under a BOOKMARK filter, store replacement, re-subscription after cancel (counting store wrapper), injected dispatcher gating emission, store failure propagation.
- `domainModule`: `DOMAIN_DISPATCHER` single (`Dispatchers.Default`), factories for all six use cases. `DomainModuleTest` pins verify with and without store extra types and the production dispatcher binding. `KoinGraphTest` (JVM extra types), `AndroidGraphVerifyTest` (temporary `SessionFiltersStore` extra type until 07-14) and `IosKoinTest.observe_agenda_use_case_resolves_after_start` pass.
- Docs: ARCHITECTURE.md Domain section (six use cases, `DayAssignment`, dispatcher, data-only contract) and the "Behavior deviations (v1.1)" table (12 rows); DEVELOPMENT.md domain test data / golden rule / test table rows.

## Verification

- `:core:domain` jvmTest and iosSimulatorArm64Test, `:shared` jvmTest and iosSimulatorArm64Test, `AndroidGraphVerifyTest`: green.
- `./gradlew detekt lint`, `di-gate.sh` (DI-GATE-OK), `kmp-nc-version-gate.sh` (KMPNC-VERSION-OK 1.0.6), `testDebugUnitTest jvmTest testAndroidHostTest`: green.
- `ObserveAgendaUseCase` is in `shared.h`; `swift-names-gate.sh check` ends with `SWIFT-NAMES-OK`.
- Xcode build-for-testing and test-without-building: `XCRESULT-OK` (19 passed, 0 failed). No golden file was written or modified.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] detekt failure in an earlier plan's file**
- **Found during:** Task 1 verification (`./gradlew detekt lint`)
- **Issue:** `core/data/.../DataPlatformModule.ios.kt` (merged by 07-11) failed `MultilineExpressionWrapping`, which fails the CI detekt task required by this plan's verify step.
- **Fix:** Wrapped the `module { ... }` expression onto a new line (formatting only).
- **Files modified:** `core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.ios.kt`
- **Commit:** a534acb

Otherwise none: the plan executed as written. Flagged assumption A4 held (Koin `verify()` accepts the named dispatcher in the shared and Android graphs); A8 held (both speaker goldens matched).

## Auth gates

None.

## Known Stubs

None.

## Threat Flags

None. T-07-19 (store failure kept as v1.0 debt) is pinned by `a_store_failure_propagates_to_the_collector`.

## Notes for later plans

- 07-14 must bind `SessionFiltersStore` on Android and remove the `SessionFiltersStore::class` extra type (and its comment) from `AndroidGraphVerifyTest.full_android_graph_is_complete`.
- 07-18: Android must skip the empty letter of `firstIndexes` and report a Crashlytics non-fatal through Timber (D-20).

## Self-Check: PASSED

All created files exist and commits a534acb, 5b4bec4, 60bd0a8 are in the branch history.
