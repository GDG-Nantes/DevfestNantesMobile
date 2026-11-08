---
phase: 09-ios-on-shared-logic-and-legacy-persistence
reviewed: 2026-11-08T00:00:00Z
depth: standard
files_reviewed: 42
files_reviewed_list:
  - AGENTS.md
  - documentation/ARCHITECTURE.md
  - documentation/DEVELOPMENT.md
  - iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift
  - iosApp/DevFestNantesTests/AgendaScrollTargetTests.swift
  - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
  - iosApp/DevFestNantesTests/CharacterizationSupport.swift
  - iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift
  - iosApp/DevFestNantesTests/Fakes/FakeSpeakerDetailsSource.swift
  - iosApp/DevFestNantesTests/Fakes/FakeSpeakersSource.swift
  - iosApp/DevFestNantesTests/Fakes/FakeStream.swift
  - iosApp/DevFestNantesTests/Fakes/FakeVenueSource.swift
  - iosApp/DevFestNantesTests/Fakes/RecordingDataLoadTracing.swift
  - iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift
  - iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift
  - iosApp/DevFestNantesTests/SpeakersViewModelTests.swift
  - iosApp/DevFestNantesTests/VenueViewModelTests.swift
  - iosApp/iosApp.xcodeproj/project.pbxproj
  - iosApp/iosApp/Agenda/AgendaDetailView.swift
  - iosApp/iosApp/Agenda/AgendaSource.swift
  - iosApp/iosApp/Agenda/AgendaView.swift
  - iosApp/iosApp/Agenda/AgendaViewModel.swift
  - iosApp/iosApp/Agenda/ComplexityFilterMenu.swift
  - iosApp/iosApp/Agenda/FavoritesFilterMenu.swift
  - iosApp/iosApp/Agenda/LanguageFilterMenu.swift
  - iosApp/iosApp/Agenda/RoomFilterMenu.swift
  - iosApp/iosApp/Agenda/SessionTypeFilterMenu.swift
  - iosApp/iosApp/Model/Data/AgendaContent.swift
  - iosApp/iosApp/Model/KotlinStreamBridge.swift
  - iosApp/iosApp/UI/Speakers/SpeakerDetailsSource.swift
  - iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift
  - iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift
  - iosApp/iosApp/UI/Speakers/SpeakersSource.swift
  - iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift
  - iosApp/iosApp/UI/Venue/VenueSource.swift
  - iosApp/iosApp/UI/Venue/VenueViewModel.swift
  - scripts/swift-names/members-baseline.txt
  - scripts/swift-names/names-exclude.txt
  - scripts/swift-names/referenced-names.txt
  - scripts/swift-names/shared-h-baseline.txt
  - shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt
  - shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt
findings:
  critical: 0
  warning: 1
  info: 5
  total: 6
status: issues_found
---

# Phase 9: Code Review Report

**Reviewed:** 2026-11-08
**Depth:** standard
**Files Reviewed:** 42
**Status:** issues_found

## Summary

The phase moves the iOS agenda onto the shared `ObserveAgendaUseCase`, the shared bookmark and filter stores, and the shared classification, speaker index and venue language use cases. I read each Swift and Kotlin source in full and traced the data flow from the Kotlin stores, through `KotlinStreamBridge` and the `Live...Source` adapters, into the ViewModels and views. I also compared the changed files against `main` (diff base 1077fd84).

I found no security problems. I found no crash or data-loss defects. The cancellation, error and loading-state handling in `AgendaViewModel.observeAgenda` is consistent with the recorded decisions (D-01, D-03, D-04). The Koin accessors in `IosDependencies` are self-starting and idempotent under a lock. The test fakes drive the real `KotlinStreamBridge`. The `.pbxproj` and `scripts/swift-names` edits match the deleted and added Swift files.

One warning: the bookmark mirror is seeded exactly once, and nothing recovers it if that seed fails. The remaining items are minor.

## Warnings

### WR-01: Bookmark mirror is never reconciled with the store after a failed or skipped seed

**File:** `iosApp/iosApp/Agenda/AgendaViewModel.swift:77-92` (consumed at `:58-73`)
**Issue:** `bookmarkedIds` is an in-memory mirror filled once, from the first emission of `bookmarkedSessionIds`, inside `seedBookmarksIfNeeded()`. The call is not tied to the agenda load. `observeAgenda()` calls it first and carries on whether or not the seed worked:

- On a non-cancellation error the seed only logs and leaves `bookmarksSeeded == false`.
- `observeAgenda()` then subscribes to the agenda anyway and shows rows.
- The shared use case reads the store's real bookmarks, so the "Favorites" filter works from the real data.
- Stars and `toggleBookmark` read the empty mirror.

Result: a session that is already bookmarked in the store shows an empty star. Tapping it computes `bookmarked = true`, writes `setBookmarked(id, true)` (a no-op in the store) and sends the `eventBookmark(... bookmarked: true)` analytics event, although the user meant to un-bookmark. The same drift happens if the Agenda task is cancelled before the first bookmark emission arrives and the user opens `AgendaDetailView` from the Speakers tab. The detail view uses the same environment ViewModel and its star is read from the unseeded mirror. In practice the store is a `StateFlow`, so the first emission is immediate and the window is narrow. The mirror still depends on a single fallible read and has no fallback.

**Fix:** Do not let a failed seed pass silently. Either retry or fall back to a per-id store read:

```swift
// AgendaSource: add a snapshot read the mirror can fall back on
func isBookmarked(_ sessionId: String) -> Bool   // wraps BookmarksStore.isBookmarked(id:)

// AgendaViewModel
func isBookmarked(_ sessionId: String) -> Bool {
    bookmarksSeeded ? bookmarkedIds.contains(sessionId) : source.isBookmarked(sessionId)
}
func toggleBookmark(sessionId: String) -> Bool {
    let bookmarked = !isBookmarked(sessionId)
    ...
}
```

`BookmarksStore.isBookmarked(id:)` already exists; `KotlinContinuousFlowTests` uses it. Alternatively, keep collecting the bookmark flow for the whole life of `observeAgenda()` so the mirror cannot go stale.

## Info

### IN-01: `AgendaViewModel.init` now calls into Kotlin and starts Koin from a `@State` initializer

**File:** `iosApp/iosApp/Agenda/AgendaViewModel.swift:30-34`, `iosApp/iosApp/UI/ContentView.swift:7`
**Issue:** `ContentView` declares `@State private var agendaViewModel = AgendaViewModel()`. SwiftUI evaluates that initializer expression again each time the `ContentView` struct is rebuilt, and keeps only the first instance. Before this phase the init read `UserDefaults`. It now runs `LiveAgendaSource().currentFilters()`, which resolves Koin (starting it if needed) and reads the filters store. Every discarded instance repeats the Kotlin call. This is harmless today (the call is cheap and idempotent), but the init has a side effect that the surrounding code does not expect.
**Fix:** Make the seed lazy, or create the ViewModel with `State(initialValue:)` in an `init`. Alternatively, read `currentFilters()` on first use.

### IN-02: Duplicate tests in `IosKoinTest`

**File:** `shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt:34-45, 57-61, 91-96`
**Issue:** `domain_use_cases_resolve_after_start` and `classify_session_accessor_resolves_after_start` assert the same thing. So do `observe_agenda_use_case_resolves_after_start` and `observe_agenda_accessor_resolves_after_start`, except that one goes through the accessor and the other through `KoinPlatform.getKoin()`. Each pair covers one binding.
**Fix:** Keep the accessor variants, since they are what Swift calls. Drop the direct-Koin duplicates, or fold them into one test per binding.

### IN-03: `KotlinContinuousFlowTests` write to the real stores and are not isolated from other suites

**File:** `iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift:14-49`
**Issue:** The suite uses the host's `standardUserDefaults`-backed stores and restores them in `defer`. `.serialized` only orders tests inside this suite. Other suites run in parallel and read the same stores: `AgendaViewModelTests.live_source_loads_the_mocked_store_rooms` calls `currentFilters()`. If the test process dies between the write and the `defer` (a crash or a hard timeout), the `spike-room` filter stays persisted in the simulator app container. A developer running the app on that simulator would then see an agenda filtered by a room that does not exist. The `first == previous` assertion could also flake if another suite ever writes to the store.
**Fix:** Run these tests against a store built on a throwaway `NSUserDefaults(suiteName:)`, or at least clear the spike entry at the start of each test. Cleaning up at the start is robust against a previous aborted run.

### IN-04: Bookmark subscription teardown after the seed is not asserted

**File:** `iosApp/iosApp/Agenda/AgendaViewModel.swift:80-81`, `iosApp/DevFestNantesTests/AgendaViewModelTests.swift:398-414`
**Issue:** `seedBookmarksIfNeeded()` takes the first element and then drops the iterator. It relies on stream deallocation to cancel the Kotlin collection of `bookmarkedSessionIds`. `bookmarks_are_seeded_once_from_the_first_emission` scripts `thenFinish: false`, so a leaked subscription would stay open. The test checks only `subscriptionCount == 1` and never `terminationCount`. A regression that kept the collection alive (a flow that keeps running for the whole process) would pass.
**Fix:** After the first `observeAgenda()`, wait for or assert `bookmarks.terminationCount == 1`, as the speakers tests already do.

### IN-05: Pre-existing items on touched lines (not phase defects)

**File:** `iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift:72`, `iosApp/iosApp/Model/Data/AgendaContent.swift:47`, `iosApp/iosApp/Agenda/AgendaViewModel.swift:149`
**Issue:** These behaviors also exist on `main` (v1.0). They are listed so they are not mistaken for regressions:

- `AgendaDetailView(session:day: "day")` passes the literal string `"day"`. The OpenFeedback link built from it becomes `.../day/<sessionId>`, which is wrong for sessions opened from the Speaker details screen.
- `ISO8601DateFormatter().date(from:) ?? Date()` puts a session with an unparseable start date at "now" instead of failing visibly. A new `ISO8601DateFormatter` is also allocated per session.
- `roomsContent = Array(rooms)` turns an unordered `Set<Room>` into an array, so the room-filter menu order can differ between runs.

**Fix:** None required for this phase; track as v1.0 debt.

---

_Reviewed: 2026-11-08_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
