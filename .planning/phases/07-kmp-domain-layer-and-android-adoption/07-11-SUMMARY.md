---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 11
subsystem: ios-persistence
tags: [ios, kmp, nsuserdefaults, persistence, koin]
status: complete

requires:
  - phase: 07-09
    provides: SessionFilter model, SessionFiltersStore interface, internal SessionFilterCodec
provides:
  - Internal iOS BookmarksStore on NSUserDefaults under devfest.bookmarks.sessionIds
  - Internal iOS SessionFiltersStore on NSUserDefaults under devfest.agenda.filters (shared Android codec)
  - iOS dataPlatformModule bindings for both stores
affects: [07-13, phase-09]

tech-stack:
  added: []
  patterns:
    - "NSUserDefaults-backed store: state loaded once into a MutableStateFlow, safe casts on load, write persists state.value"

key-files:
  created:
    - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStore.kt
    - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStore.kt
    - core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStoreTest.kt
    - core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStoreTest.kt
  modified:
    - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.ios.kt
    - shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt
    - documentation/ARCHITECTURE.md

key-decisions:
  - "D-15 implemented as planned: new NSUserDefaults keys, legacy Favorites and AgendaSessionFilters never read, migrated or deleted"
  - "Both store classes are internal, so the Swift header and swift-names baselines are unchanged"

requirements-completed: [SHARED-05]

duration: multi-session (interrupted by a network outage)
completed: 2026-10-29
commits: 2
plan_head_before: a709d8a8b9a07d0aec0539e4621e3fba25b3160e
plan_head_after: 3bd413730e920c0f4977c53b039e13a859101908
actuals:
  tokens: 9000
  tasks: 2
  commits: 2
---

# Phase 7 Plan 11: iOS Bookmarks and Session Filters Stores Summary

**iOS BookmarksStore and SessionFiltersStore on hand-written NSUserDefaults under new keys, writing filters with the shared Android codec, bound in the iOS Koin module and tested on the simulator, with the legacy v1.0 keys left untouched (D-15).**

## What was built

- `UserDefaultsBookmarksStore`: persists session ids as a string array under `devfest.bookmarks.sessionIds`. State is a `MutableStateFlow` loaded once; `setBookmarked` uses `update {}` then persists `state.value`; `subscribe(id)` is `state.map { id in it }`.
- `UserDefaultsSessionFiltersStore`: persists one `SessionFilterCodec` JSON string per filter under `devfest.agenda.filters` (bytes identical to Android, e.g. `{"type":"LANGUAGE","value":"FRENCH"}`). Load uses `as? String` plus `decodeOrNull`; `setFilters(emptySet())` stores an empty array.
- iOS `dataPlatformModule` binds both as Koin singles. The JVM and Android actuals are unchanged.
- `IosKoinTest.platform_stores_resolve_as_singles_after_start` proves each store resolves as one instance.
- `documentation/ARCHITECTURE.md` documents the Android and iOS persistence keys and D-15.

## Tests

- `UserDefaultsBookmarksStoreTest`: 7 tests, all passed on iosSimulatorArm64.
- `UserDefaultsSessionFiltersStoreTest`: 7 tests, all passed on iosSimulatorArm64.
- `IosKoinTest`: 3 tests, all passed, including the new one.
- Covered: persistence round trip, second-instance restore, malformed JSON, non-string elements, non-array values, empty array, legacy keys unchanged after writes, no re-emission on equal content.

## Verification

- `./gradlew detekt lint` and `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest`: green.
- `bash scripts/di-gate.sh`: DI-GATE-OK. `bash scripts/kmp-nc-version-gate.sh`: KMPNC-VERSION-OK 1.0.6.
- `:core:data:jvmTest :shared:jvmTest :shared:linkDebugFrameworkIosSimulatorArm64`: green; no `UserDefaults*Store` symbol in `shared.h`; `swift-names-gate.sh check` ends with SWIFT-NAMES-OK.
- `git status --porcelain -- iosApp` is empty of tracked changes (no Swift change).

## Deviations from Plan

None - plan executed as written. Task 1 and Task 2 each have a single commit as the plan specifies; RED-first was not split into a separate commit because the plan is `type: execute` with one commit per task.

## Auth gates

None.

## Known Stubs

None. The stores are intentionally not called by the iOS app yet; wiring is Phase 9.

## Threat Flags

None. No new network endpoint, auth path or trust boundary beyond the planned NSUserDefaults read (T-07-14 mitigated by safe casts and `decodeOrNull`, tested).

## Commits

- 84275d5: feat(07-11): add the iOS bookmarks store on NSUserDefaults
- 3bd4137: feat(07-11): add the iOS session filters store and bind both iOS stores
