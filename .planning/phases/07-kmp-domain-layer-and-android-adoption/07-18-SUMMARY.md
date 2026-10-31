---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 18
subsystem: android-adoption
tags: [android, adoption, speakers, venue, koin, timber]
requires:
  - phase: 07-13
    provides: SpeakerIndexUseCase and VenueLanguageUseCase in :core:domain
  - phase: 07-16
    provides: domainModule available to Android feature modules
provides:
  - SpeakersViewModel.firstLetterIndexes(speakers) on SpeakerIndexUseCase (empty name skipped, Timber non-fatal)
  - VenueViewModel on VenueLanguageUseCase
  - SpeakerIndexEmptyNameTest (D-20)
affects: [07-19]
tech-stack:
  added: []
  patterns: ["use cases constructor-injected into ViewModels through viewModelOf; composable computes remember(speakers) { viewModel.x(speakers) } and passes the result down"]
key-files:
  created:
    - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexEmptyNameTest.kt
  modified:
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt
    - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModelTest.kt
    - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexCharacterizationTest.kt
    - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/di/SpeakersModuleTest.kt
    - feature/speakers/src/androidTest/java/com/gdgnantes/devfest/feature/speakers/SpeakersSmokeTest.kt
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueViewModel.kt
    - feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/VenueViewModelTest.kt
    - feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/utils/VenueLanguageCharacterizationTest.kt
    - feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/di/VenueModuleTest.kt
    - feature/venue/src/androidTest/java/com/gdgnantes/devfest/feature/venue/VenueSmokeTest.kt
    - documentation/ARCHITECTURE.md
  removed:
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/utils/LocalUtils.kt
key-decisions:
  - "SpeakersList also reads the row flag with firstOrNull so an empty speaker name cannot crash the list rendering either (the v1.0 first() lived there as well as in the map)"
  - "The D-20 test file has a third test (no report for regular names) beyond the two the plan names"
requirements-completed: [SHARED-04, SHARED-06]
status: complete
commits: 1
plan_head_before: 58f13630c9e1ba1a7eb6b883115161da7966c833
plan_head_after: 162e76b97ac53e369c4e6aced040011ee9f3f91f
actuals:
  tokens: 9000
  tasks: 1
  commits: 1
duration: 25 min
completed: 2026-10-29
---

# Phase 7 Plan 18: Speakers and venue on the shared use cases Summary

`SpeakersViewModel` now builds the A-Z scroller map through `SpeakerIndexUseCase` and `VenueViewModel` picks the venue language through `VenueLanguageUseCase`; Android's own copies (`getMapOfFirstUniqueCharIndex`, `LocalUtils.kt`) are deleted, and an empty speaker name is skipped and reported to Crashlytics as a non-fatal instead of crashing.

## Accomplishments

- Task 1 (commit 162e76b, sha to cite for the acceptance-criteria diff check): speakers and venue switched in one commit.
  - Speakers: `SpeakersViewModel(store, performanceMonitoring, speakerIndex)` with `firstLetterIndexes(speakers)` = `firstIndexes` filtered on non-empty letters, keyed by `letter.lowercase().first()` (insertion order kept). The first `Speakers` overload (the one holding the ViewModel) computes `remember(speakers.value) { viewModel.firstLetterIndexes(...) }` and passes the map through the other overloads to `SpeakersListWithScroller`, which no longer computes it.
  - D-20: when an empty letter is present, one `Timber.w(IllegalStateException("Speaker A-Z index: speaker with an empty name skipped (ids: ...)"), ...)` is emitted per call; only speaker ids are in the message. `DevFestNantesApplication`'s existing Timber trees forward it to `FirebaseCrashlytics.recordException`.
  - Venue: `VenueViewModel(store, venueLanguage)` emits `store.getVenue(venueLanguage.languageFor(locale.language))`; the null-locale guard is unchanged.
  - Tests (wiring only, D-18): constructor arguments, module-test extra types (`SpeakerIndexUseCase`, `VenueLanguageUseCase`), smoke tests load `domainModule`, `VenueSmokeTest` derives the expected language from `VenueLanguageUseCase`. The speaker golden test and the fixture/case tables keep every expected literal; the venue locale table is unchanged.
  - New `SpeakerIndexEmptyNameTest` (3 tests): the empty name is skipped without a crash (`a`, `b` keys with shared-index positions), exactly one WARN with an `IllegalStateException` naming the empty-name speaker id (no names in the message), and no throwable report for regular names.
  - `documentation/ARCHITECTURE.md`: the `SpeakerIndexUseCase` and `VenueLanguageUseCase` bullets describe the ViewModel adoption and the Crashlytics non-fatal; the empty-name deviations row notes `SpeakerIndexEmptyNameTest`. Only speakers/venue lines were touched (07-17 edits the classification lines).
- Verification (all green): `:feature:speakers:testDebugUnitTest`, `:feature:venue:testDebugUnitTest`, `compileDebugAndroidTestKotlin` for both modules, `./gradlew detekt lint`, `scripts/di-gate.sh` (DI-GATE-OK), `scripts/kmp-nc-version-gate.sh` (KMPNC-VERSION-OK 1.0.6), `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`. No old helper remains in feature or androidApp main sources; `core/domain/testdata` untouched. The instrumentation smoke tests were compiled only (as the plan requires), not run on a device; D-12 screenshots in 07-19 cover the scroller and the venue screen.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Row flag in SpeakersList still crashed on an empty name**
- **Found during:** Task 1
- **Issue:** `SpeakersList` computed `firstLetterIndexes[speaker.name.lowercase().first()]`; with an empty name that `first()` throws `NoSuchElementException`, so the plan's goal (no crash on an empty speaker name) would not hold at render time even with the map fixed.
- **Fix:** `speaker.name.lowercase().firstOrNull()?.let { firstLetterIndexes[it] } == index` (identical result for non-empty names, `false` for an empty one). `SpeakerRow` only calls `name.first()` when that flag is true, so it is not reachable for an empty name.
- **Files modified:** feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt
- **Commit:** 162e76b

**2. [Rule 3 - Blocking] detekt ArgumentListWrapping on a long assertion line**
- **Found during:** Task 1 (detekt run)
- **Issue:** the re-pointed `assertEquals(...)` in `VenueLanguageCharacterizationTest` exceeded the line width.
- **Fix:** the actual value moved to a local `val actual`; expected literals unchanged.
- **Commit:** 162e76b

**Other small choices:** the venue characterization test function was renamed `languageFor_maps_every_locale_of_the_table` (the old name referenced the deleted `toContentLanguage`); the characterization KDocs state the oracle was recorded against the removed v1.0 helpers.

**Total deviations:** 2 auto-fixed (1 bug, 1 blocking). **Impact:** none on scope; the first one completes the D-20 intent.

## Known Stubs

None.

## Threat Flags

None. The D-20 report carries speaker ids only and goes through the existing Timber trees (T-07-35).

## Self-Check: PASSED

- LocalUtils.kt removed, `SpeakerIndexEmptyNameTest.kt` present, commit 162e76b is an ancestor of HEAD.
- `SpeakerIndexEmptyNameTest`: 3 tests, 0 failures; `SpeakerIndexCharacterizationTest`: 3 tests, 0 failures (golden unchanged).
