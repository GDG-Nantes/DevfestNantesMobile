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
