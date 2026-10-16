# Phase 5 deferred items (D-10)

Production behavior the Phase 5 tests expose. Each entry is characterized by a test and is not fixed in this phase.

## From 05-04 (GraphQLStore and mappers)

1. **`GraphQLStore` logs Apollo errors with `println` instead of Timber.** Every error branch and `getSpeakerSessions` ("Found N sessions...") call `println`. The behavior around those branches is pinned by `GraphQLStoreMockServerTest.getters_return_null_or_empty_on_error`, `getVenue_falls_back_to_the_offline_stub_on_error` and `network_error_after_cached_success_emits_only_cached_*`.
2. **The store reads the deprecated `speakers` query field, which returns only the first 100 of 110 speakers** (live check 2026-10-16: `speakers` 100 vs `speakersPage(first: 200)` 110). Speakers beyond the first 100 are missing from the speaker list and `getSpeaker`, although they appear on sessions (for example `xavier_charef`, a speaker of the recorded codelab). Pinned by `MapperTest.recorded_speakers_map_profile_and_socials` (7 of the 8 speakers of the recorded sessions are present). The Kotlin compiler also warns about the deprecated field in `GraphQLStore.kt`.
3. **`RoomSortIndex` does not know the live room ids `Tour Bretagne` and `Unconf`.** It lists `Tour de Bretagne`, so both rooms get `Int.MAX_VALUE` and sort last in the rooms list. Pinned by `GraphQLStoreMockServerTest.rooms_flow_maps_and_sorts_recorded_rooms` and `MapperTest.recorded_rooms_get_their_sort_index`.
4. **`SocialItem.Builder` produces double-slash links for bare handles** (`https://github.com//alice-moreau`, found in 05-03): `baseUrl` already ends with `/` and the builder adds another. Recorded socials carry full URLs, so the recorded tests do not hit it.
5. **A social whose `name` is unknown and whose `url` is empty (recorded `bluesky` with `url: ""`) becomes a `WEBSITE` item with an empty link**, so the UI may render an empty link. Pinned by `MapperTest.recorded_unknown_social_name_and_empty_url_map_to_a_website_item`.

## From 05-05 (store contract and persistence tests)

6. **`BookmarksStoreImpl.save` and `DataCollectionSettingsServiceImpl.save` call `apply()` inside `edit { }`**, which already applies on exit, so every save applies twice. Harmless today; the persisted result is pinned by `BookmarksStoreImplTest` and `DataCollectionSettingsServiceImplTest`.
7. **`SessionFilter` is a plain class (no `equals`)**, so `SessionFiltersService.filters` compares by identity and `setFilters(sameContent)` always looks like a change to a `StateFlow`. `SessionFiltersServiceImplTest` compares `(type, value)` pairs instead.
8. **`MockServer(handler)` is deprecated in apollo-mockserver 0.3.1** (DEPRECATION_ERROR under Kotlin 2.4); tests use `MockServer.Builder().handler(...).build()`.

## From 05-06 (ViewModel harness), logged by 05-07

9. **`SpeakersViewModel` does not handle a failing `store.speakers` flow.** The collection in `init` has no `catch`, so the error escapes `viewModelScope` as an uncaught exception (the app would crash) and `uiState` never leaves `STARTING`; `UiState.ERROR` is never used. Pinned by `SpeakersViewModelTest.store_failure_is_characterized`: with `FakeDevFestNantesStore.failure` set, `uiState` stays `STARTING`, `speakers` stays empty, and the test run fails with the original `IllegalStateException("boom")` (asserted with `assertFailsWith` around `runTest`).

## From 05-07 (agenda, home and bookmarks ViewModels)

10. **`AgendaViewModel` does not handle a failing `store.agenda` flow.** `onRefresh` collects the flow inside `viewModelScope.launch` without a `catch`, so a store error escapes uncaught (the app would crash), `uiState` stays `STARTING` and `days` stays empty; `UiState.ERROR` is never used. Same defect as item 9. Pinned by `AgendaViewModelTest.store_failure_is_characterized`.
11. **Sessions that share a start time come out of `AgendaViewModel` in an unspecified order.** The filter builds a `HashSet<Session>` and then `sortedBy { startDate }`, which is stable but starts from hash order, so ties (for example the three 10:00 sessions of day 1) can reorder between runs of the same data. Characterized by `AgendaViewModelTest.sessions_within_each_day_are_sorted_by_start_date`, which compares ties as a set instead of asserting an order.

## From 05-07 (CI)

12. **`./gradlew connectedDebugAndroidTest` / `assembleDebugAndroidTest` intermittently fails with `java.lang.OutOfMemoryError: Java heap space` in D8** (`mergeExtDexDebugAndroidTest`, then `packageDebugAndroidTest` of `:feature:session-detail`) with `org.gradle.jvmargs=-Xmx4096M`. Not caused by Phase 5 code: it reproduced locally on the 05-06 commit `7182cc9` (first run failed, second passed) and on 05-07 HEAD, and the instrumentation job passed after a rerun (android.yml run 38873089664, attempts 1 and 2 red, attempt 3 green). A larger Gradle heap in `gradle.properties` or a lower worker count in the instrumentation job would likely stop it; left alone because it is build configuration outside this plan.

## From 05-08 (session, speaker, partners, data-collection, feedback and venue ViewModels)

13. **`SessionViewModel` does not handle a failing `store.getSession`.** The load in `init` has no `catch`, so a store error escapes `viewModelScope` uncaught (the app would crash) and `session` stays `null`. Same defect as items 9 and 10. Pinned by `SessionViewModelTest.store_failure_is_characterized`.
14. **`SpeakerViewModel` reports `UiState.SUCCESS` for an unknown speaker id** (null speaker, empty sessions), and has no error state; it also has no `catch` around `getSpeaker` / `getSpeakerSessions`. Pinned by `SpeakerViewModelTest.unknown_speaker_still_reports_success_with_null_speaker`.
15. **`PartnersViewModel` never exposes the `PARTNERS` category** (no flow for it), so partners filed under it are not shown by the screen. Pinned by `PartnersViewModelTest.partners_category_is_not_exposed`.
16. **`VenueViewModel.venue` is a getter that builds a new `StateFlow` (`flow { ... }.stateIn(viewModelScope, Lazily, null)`) on every access**, so each recomposition or read starts a new collection and a new store call, and the locale is re-read each time. Characterized by `VenueViewModelTest.venue_builds_a_new_state_flow_on_every_access` (`assertNotSame`). Also, when the system locale list is empty the flow emits nothing and `uiState` stays `LOADING` (not covered: Robolectric always provides a locale). The tests set the locale with `@Config(qualifiers = "fr-rFR" | "en-rUS" | "de-rDE")`, which does reach `Resources.getSystem()`.
