---
phase: 07-kmp-domain-layer-and-android-adoption
reviewed: 2026-10-29T00:00:00Z
depth: standard
files_reviewed: 123
files_reviewed_list:
  - .github/workflows/ios.yml
  - AGENTS.md
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModel.kt
  - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt
  - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt
  - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModelTest.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt
  - build.gradle.kts
  - core/data/build.gradle.kts
  - core/data/src/androidHostTest/kotlin/com/gdgnantes/devfest/core/data/SharedPreferencesSessionFiltersStoreTest.kt
  - core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.android.kt
  - core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/SharedPreferencesSessionFiltersStore.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/SessionFilterCodec.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/SessionFiltersStore.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/SessionFilterCodecTest.kt
  - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.ios.kt
  - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStore.kt
  - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStore.kt
  - core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStoreTest.kt
  - core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStoreTest.kt
  - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/snapshot/AgendaSnapshotJson.kt
  - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/snapshot/AgendaSnapshotRecorder.kt
  - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/snapshot/AgendaSnapshotTest.kt
  - core/domain/build.gradle.kts
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/ClassifySessionUseCase.kt
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/DayAssignment.kt
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/DaySchedule.kt
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/FilterSessionsUseCase.kt
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/GroupSessionsUseCase.kt
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/ObserveAgendaUseCase.kt
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModule.kt
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/speakers/SpeakerIndexUseCase.kt
  - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/venue/VenueLanguageUseCase.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/SessionFilter.kt
  - core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/TestDataFiles.android.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModule.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeSessionFiltersStore.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/AgendaSnapshot.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/FilterCases.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/GoldenFiles.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/Goldens.kt
  - core/testing/src/jvmMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/TestDataFiles.jvm.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/di/CoreUiModule.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/Agenda.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaColumn.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaPager.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRow.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/SessionFiltersDrawer.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueViewModel.kt
  - iosApp/iosApp/Agenda/AgendaView.swift
  - iosApp/iosApp/Agenda/AgendaViewModel.swift
  - iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift
  - iosApp/DevFestNantesTests/CharacterizationSupport.swift
  - iosApp/DevFestNantesTests/SpeakersVenueCharacterizationTests.swift
  - scripts/record-agenda-snapshot.sh
  - scripts/README.md
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt
  - shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt
  - shared/src/jvmTest/kotlin/com/gdgnantes/devfest/shared/di/KoinGraphTest.kt
  - (plus the remaining test, build, doc and fixture files of the 123-file scope, skimmed for structure)
findings:
  critical: 0
  warning: 4
  info: 4
  total: 8
status: issues_found
---

# Phase 7: Code Review Report

**Reviewed:** 2026-10-29
**Depth:** standard
**Files Reviewed:** 123
**Status:** issues_found

## Summary

I diffed the Android rewiring against `254f572` (v1.0). The shared domain logic reproduces the v1.0
Android rules:

- `FilterSessionsUseCase` gives the same AND/OR result as the old `filterSessions`.
- `UtcDayWindows` copies the `Agenda.Builder` rule, including its truncation quirk for starts up to
  24 h before a day start.
- The in-slot room order, the bookmark star (`isTalk`) and the navigation gate (`isNavigable`)
  match the old code.
- The filters store keeps the same preference key and the same JSON bytes, so persisted filters
  survive the upgrade.
- `ObserveAgendaUseCase` builds a fresh cold flow per call, so refresh and cancellation behave as in v1.0.

I found no crash, data-loss or security defect on the production path. The findings are:

- one correctness inconsistency in the new speaker index adapter;
- one mismatch between the speaker index's case-folding and the row header logic;
- one fragile re-record script;
- one non-atomic persistence pattern in the new iOS stores;
- one side effect (a Crashlytics report) inside a composition-time call;
- a few stale comments and duplication.

The security surface is small: the recorder posts a fixed query to a public API with a public header
and no secrets, and the persisted values are decoded defensively on both platforms.

## Warnings

### WR-01: Speaker A-Z scroller index and row header use different case folding, so some letters break

**File:** `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt:61-63`
(consumer: `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt:~253`)
**Issue:** The shared use case derives the letter as `name.take(1).uppercase()`. The ViewModel then
turns it into the map key with `letter.lowercase().first()`. The row header check in `SpeakersList`
still uses `speaker.name.lowercase().firstOrNull()`. v1.0 used `name.lowercase().first()` on both
sides. The two pipelines agree for ASCII and common accented letters, but diverge when upper-casing
changes the length or the base character.

Examples: `ß` becomes `SS` and the key becomes `'s'`; `ﬁ` becomes `FI` and the key `'f'`; the dotless
`ı` becomes `I` and the key `'i'`. For a speaker named `ß...`:
- the map has `'s' -> index`, while the row looks up `'ß'`;
- the letter header is never shown;
- the scroller's `S` jumps to the `ß` speaker;
- `associate` silently overwrites an existing `'s'` entry (or is overwritten by it), so `S` can point
  at the wrong speaker.

None of these letters appears in the recorded data, but the old code was consistent and the new
adapter is not.
**Fix:** Derive the key and the row lookup from one function, so they cannot diverge:
```kotlin
// SpeakersViewModel
fun letterKey(name: String): Char? = name.lowercase().firstOrNull()

return indexes
    .filter { it.letter.isNotEmpty() }
    .associate { (letter, index) -> speakers[index].name.let(::letterKey)!! to index }
    // keep the first index per key
```
Or have the row use `speakerIndex.firstIndexes` semantics: compare `index ==
firstLetterIndexes[letterKey(speaker.name)]` with the same `letterKey`. Add a `ß` case to
`SpeakerIndexEmptyNameTest`.

### WR-02: `record-agenda-snapshot.sh` swallows real failures and prints a success marker

**File:** `scripts/record-agenda-snapshot.sh:17-29` (also `scripts/README.md`, "Then runs
`AgendaSnapshotTest`... tolerated")
**Issue:** The "No tests found" tolerance was written for a plan that has not yet added
`AgendaSnapshotTest`. The test now exists. Any Gradle "No tests found" result (a renamed class, a
changed `--tests` filter, an exclusion in `core/data/build.gradle.kts`) is now treated as expected.
The script then skips the neutral snapshot rewrite and still prints `AGENDA-SNAPSHOT-RECORDED` with
exit code 0. The raw responses are already overwritten at that point, so the committed neutral
snapshot silently stays stale. The next default run fails with "neutral snapshot drifted", with no
pointer to the cause.

There is a second weakness in the same flow. `--tests '*AgendaSnapshotTest*'` in record mode also
runs `recorded_snapshot_has_the_expected_shape`, which hard-codes 94 sessions, 17 roomless, 8 rooms
and 100 speakers (`AgendaSnapshotTest.kt:67-72`). The documented purpose of the script is to record a
new edition or a changed API. After any real change in the data, step 2 fails and the script exits 1
after step 1 has already overwritten `core/data/testdata/graphql/*.json`.
**Fix:** Remove the tolerance and fail on any non-zero exit:
```bash
./gradlew :core:data:jvmTest -PrecordAgendaSnapshot --tests '*AgendaSnapshotTest.neutral_snapshot_matches_the_raw_responses'
```
Run only the recording test in step 2, so the pinned-count tests do not block re-recording. Update
the pinned counts, and the README line, as an explicit manual step after reviewing the diff. Drop
the mention of a later plan from the script and from `scripts/README.md`.

### WR-03: iOS stores persist `state.value` after the update, so concurrent writers can lose a write

**File:** `core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStore.kt:27-30`
and `core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStore.kt:26-29`
**Issue:** `setBookmarked` runs `state.update { ... }` (atomic) and then
`defaults.setObject(state.value.toList(), ...)`, which is a separate non-atomic step. With two
callers on different threads the interleaving is: A updates, A reads its snapshot, B updates, B
persists the merged state, A persists its older snapshot. The in-memory state is then correct but
`NSUserDefaults` is missing B's change after the next launch. `UserDefaultsSessionFiltersStore`
has the same shape (`state.value = filters`, then `setObject(filters...)`). The stores are Koin
singles reachable from any coroutine context in Phase 9 (the Swift side calls them from arbitrary
tasks), and the Android `SharedPreferences` version has the same ordering but its writes go through
`apply()`, which serializes them.
**Fix:** Persist the value produced by the atomic update, and serialize the write:
```kotlin
override fun setBookmarked(sessionId: String, bookmarked: Boolean) {
    val next = state.updateAndGet { if (bookmarked) it + sessionId else it - sessionId }
    defaults.setObject(next.toList(), forKey = KEY_BOOKMARKS)
}
```
This narrows the window but does not close it. If the stores are to be callable from multiple
threads, guard update-plus-write with a lock (for example `kotlinx.atomicfu.locks.SynchronizedObject`)
or confine writes to a single-thread dispatcher.

### WR-04: Non-fatal report fires from a composition-time call and repeats on every recomputation

**File:** `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt:52-60`
(call site: `Speakers.kt`, `remember(speakers.value) { viewModel.firstLetterIndexes(speakers.value) }`)
**Issue:** `firstLetterIndexes` has a side effect: `Timber.w(exception, ...)` is forwarded to
`firebaseCrashlytics.recordException` by `TimberTreeRelease`. It is called inside `remember(...)` in
a composable. The `ViewModel` outlives configuration changes, but the `remember` cache does not. A
rotation, process-preserved navigation return, or a re-entry into the composition recomputes it and
re-reports the same empty-name speaker. That inflates the Crashlytics non-fatal count for one
persistent data defect. A side effect in composition also breaks the "composition is pure" contract.
The exception is allocated only to carry a stack trace, which points at the composition call and not
at the data source.
**Fix:** Compute the index once, outside the composition. For example, expose a derived state from the ViewModel:
```kotlin
val firstLetterIndexes: StateFlow<Map<Char, Int>> =
    speakers.map(::buildIndex).stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())
```
Run the report in the `map` step so it fires once per data load. This also removes the `remember`
call from `Speakers.kt`.

## Info

### IN-01: Shared "navigable" table leaves KEYNOTE sessions with a bookmark star and no detail screen

**File:** `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/ClassifySessionUseCase.kt:22-29`
**Issue:** `isTalk` is true for `KEYNOTE` (a typed non-service session), so the agenda row shows the
bookmark star. `isNavigable` is only QUICKIE, CONFERENCE and CODELAB, so tapping a keynote row does
nothing. That is faithful to v1.0 on both platforms, but the shared table, the golden tests and the
iOS table (`AgendaCharacterizationTests.is_a_talk_matches_the_shared_table`) now fix it as the cross-platform contract.
Phase 9 will inherit it. If keynotes were meant to open a detail screen, this is a latent v1.0 bug.
**Fix:** Confirm the intent with the product owner. If it is intended, add one sentence to the KDoc;
if not, add `KEYNOTE` to `NAVIGABLE_TYPES` together with a table row in the Kotlin and Swift tests.

### IN-02: Stale phase-planning comments left in code and docs

**File:** `core/data/build.gradle.kts` (comment "plan 07-04 creates the neutral snapshots later"),
`feature/agenda/src/test/.../AgendaGroupingCharacterizationTest.kt:38-47` ("written against the
unchanged code"),
`core/domain/src/commonMain/.../ObserveAgendaUseCase.kt:29-30` ("iOS will pass LocalDateDays"),
`core/domain/src/commonMain/.../di/DomainModule.kt:39-40` (positional-argument comment that restates the
constructor)
**Issue:** The comments describe a state of the repo that no longer holds. The grouping test
runs against the new shared code, not the v1.0 code, so its claim about the oracle's provenance is
misleading to the next reader.
**Fix:** Reword to the current state and remove plan references; keep the "goldens captured from
v1.0" note on the golden files only.

### IN-03: Duplicate `TestDataFiles` implementations for android and jvm

**File:** `core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/TestDataFiles.android.kt`
and `core/testing/src/jvmMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/TestDataFiles.jvm.kt`
**Issue:** The two files are byte-identical apart from the file name. Any fix (for example making
`writeFile` atomic, or guarding `..` in `relativePath` in `GoldenFiles.join`) has to be applied twice.
**Fix:** Introduce a shared `jvmAndAndroid` intermediate source set, or put the implementation in a
common `jvmCommon` helper and have both actuals delegate to it.

### IN-04: Every day-label parse repeats inside the per-session loop

**File:** `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/DayAssignment.kt:46-49`
**Issue:** `dayPosition` re-parses every configured `dayStart` string for each session on each
emission. This is not a correctness problem (it is only O(sessions x days)), but `toInstantOrNull`
is called inside a lambda that could be hoisted, and the unparseable-day-start case ("matches
nothing") is silently invisible. A typo in `DAY_ONE_ISO` would drop every session with no log line.
**Fix:** Parse `dayStarts` once per `group` call, and consider a single `Timber`/log hook in the
caller when a configured day start fails to parse.

---

_Reviewed: 2026-10-29_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
