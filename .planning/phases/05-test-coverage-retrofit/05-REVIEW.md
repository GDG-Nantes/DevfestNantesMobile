---
phase: 05-test-coverage-retrofit
reviewed: 2026-10-16T00:00:00Z
depth: standard
files_reviewed: 75
files_reviewed_list:
  - .github/workflows/android.yml
  - .github/workflows/ios.yml
  - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt
  - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModelTest.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidApplicationConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidLibraryConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/KmpLibraryConventionPlugin.kt
  - build.gradle.kts
  - core/data/build.gradle.kts
  - core/data/src/androidHostTest/kotlin/com/gdgnantes/devfest/core/data/BookmarksStoreImplTest.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreMocked.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreContractTest.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreContractTest.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreMockServerTest.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/MapperTest.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/RecordedResponses.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/RecordedResponsesHandler.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/MockedStoreContractTest.kt
  - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreLiveNetworkTest.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/PartnerFixtures.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/SessionFixtures.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/SpeakerFixtures.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/StoreStubs.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/utils/IsoDate.kt
  - core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/stubs/FixturesTest.kt
  - core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/utils/IsoDateTest.kt
  - core/network/build.gradle.kts
  - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/Apollo.kt
  - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/ApolloCacheKeys.kt
  - core/testing/build.gradle.kts
  - core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/MainDispatcherRule.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModule.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeAnalyticsService.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeDevFestNantesStore.kt
  - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModuleTest.kt
  - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeDevFestNantesStoreTest.kt
  - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeStoresTest.kt
  - core/ui/src/androidTest/java/com/gdgnantes/devfest/core/ui/utils/ScheduleSlotDateFormatAndroidTest.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/di/CoreUiModule.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/utils/DateUtils.kt
  - core/ui/src/test/java/com/gdgnantes/devfest/core/ui/BookmarksViewModelTest.kt
  - core/ui/src/test/java/com/gdgnantes/devfest/core/ui/utils/DateUtilsCharacterizationTest.kt
  - feature/about/src/androidTest/java/com/gdgnantes/devfest/feature/about/AboutSmokeTest.kt
  - feature/about/src/test/java/com/gdgnantes/devfest/feature/about/partners/PartnersViewModelTest.kt
  - feature/agenda/src/androidTest/java/com/gdgnantes/devfest/feature/agenda/AgendaSmokeTest.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaColumn.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
  - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModelTest.kt
  - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/CurrentStartTimeIndexTest.kt
  - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModuleTest.kt
  - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/FakeSessionFiltersService.kt
  - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersServiceImplTest.kt
  - feature/session-detail/src/androidTest/java/com/gdgnantes/devfest/feature/sessiondetail/SessionDetailSmokeTest.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/di/SessionDetailModule.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FallbackFeedbackForm.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFlags.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFormViewModel.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/utils/ScheduleSlotUtils.kt
  - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/FallbackFeedbackUrlTest.kt
  - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFormViewModelTest.kt
  - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/SessionViewModelTest.kt
  - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/utils/ScheduleSlotFormattedRangeTest.kt
  - feature/settings/src/androidTest/java/com/gdgnantes/devfest/feature/settings/SettingsSmokeTest.kt
  - feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionViewModelTest.kt
  - feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/FakeDataCollectionSettingsService.kt
  - feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsServiceImplTest.kt
  - feature/settings/src/test/res/values/crashlytics_test_mapping_file_id.xml
  - feature/speakers/src/androidTest/java/com/gdgnantes/devfest/feature/speakers/SpeakersSmokeTest.kt
  - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModelTest.kt
  - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/SpeakerViewModelTest.kt
  - feature/venue/src/androidTest/java/com/gdgnantes/devfest/feature/venue/VenueSmokeTest.kt
  - feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/VenueViewModelTest.kt
  - gradle/libs.versions.toml
findings:
  critical: 0
  warning: 3
  info: 6
  total: 9
status: issues_found
---

# Phase 5: Code Review Report

**Reviewed:** 2026-10-16
**Depth:** standard
**Files Reviewed:** 75
**Status:** issues_found

## Summary

I reviewed the Phase 5 production changes (shared ISO parser, `FeedbackFlags` seam, `AgendaViewModel` dispatcher injection, `ApolloCacheKeys` extraction, `DevFestNantesStoreMocked`) and the new tests, fakes, fixtures, Kover, build-logic and CI changes. I diffed the production files against `68419e8` to look for behavior regressions.

I found no production regression. The shared parser accepts a superset of what the old two-pattern `SimpleDateFormat` accepted. The ASCII `LocalDate.toString()` fixes the locale-digit bug in the feedback URL. The Koin wiring is consistent: the `FeedbackFlags` factory with `bind`, and the positional `AgendaViewModel` definition with the named IO dispatcher. `MainDispatcherRule` resets `Dispatchers.Main` in `finished`, and the Venue smoke test resets the Coil singleton in `@After`, so I found no leaked global dispatcher or Coil state. The Kover module list matches the 13 production modules, and every module's convention plugin applies Kover.

The findings are all about test hermeticity and tracking of pinned defects. None is a crash or data-loss risk, so none is rated BLOCKER.

Two coverage limits: I read `RecordedResponses.kt` (about 36 KB of recorded JSON) only for its header and for secret/PII patterns, not line by line, and found none. I judged the behavior of Robolectric, Compose and Apollo test APIs by reading the code. I did not run the build.

## Warnings

### WR-01: `KoinStartupTest` performs a real Firebase Remote Config fetch

**File:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt:127-131`
**Issue:** `feedback_form_view_model_resolves_with_real_remote_config` resolves `FeedbackFormViewModel` against the production `FirebaseFeedbackFlags` and the real `FirebaseRemoteConfig`. The `FeedbackFormViewModel` init block calls `fetchAndActivate()`, which starts a background network fetch against `firebaseremoteconfig.googleapis.com` with the dummy API key `test-api-key`. Robolectric runs on the host JVM, so the request can really leave the machine. Its outcome and timing are non-deterministic: a callback may land after the test ends, and the fetch adds log noise and slows the run. The same test also creates the ViewModel without ever clearing it, so the listener keeps a reference to it. The CI unit-test job is meant to be offline and deterministic. The `FeedbackFlags` seam was added for exactly this reason, but this test still bypasses it.
**Fix:** Keep the DI-resolution check, but stop the fetch from running. For example, override `FeedbackFlags` for the ViewModel test and assert the production binding without constructing the ViewModel.
```kotlin
@Test
fun feedback_form_view_model_resolves_with_real_remote_config() {
    // Resolve the production binding without triggering the fetch in FeedbackFormViewModel.init.
    assertIs<FirebaseFeedbackFlags>(koin.get<FeedbackFlags>())
}
// ...and cover FeedbackFormViewModel resolution in a separate test that overrides FeedbackFlags with a fake.
```

### WR-02: Firebase singletons are shared across Robolectric tests and are never reset

**File:** `feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsServiceImplTest.kt:37-60` (same pattern in `KoinStartupTest.kt:77-99`)
**Issue:** `setUp` initializes the default `FirebaseApp` only `if (FirebaseApp.getApps(application).isEmpty())`. `FirebaseApp` instances, `FirebaseCrashlytics`, `FirebasePerformance` and `FirebaseAnalytics` are process-wide statics. Robolectric reuses the instrumenting classloader for tests with the same SDK, so the statics survive from one test to the next. Two consequences:
1. In the second and later tests, `FirebaseApp` keeps the `Application` of the first test, while `prefs` and `FirebaseAnalytics.getInstance(...)` use the current one.
2. The `consentToAllServices`/`disallowAllServices` tests change the global Crashlytics and Performance collection flags. Nothing resets them, so the result depends on test order. The assertions on `isCrashlyticsCollectionEnabled` and `isPerformanceCollectionEnabled` in `consentToAllServices_stores_the_three_names_and_enables_collection` only hold because that test sets the flags itself first.

No `@After` calls `FirebaseApp.clearInstancesForTest()` or deletes the app.
**Fix:** Reset the Firebase state after every test:
```kotlin
@After
fun tearDown() {
    FirebaseApp.clearInstancesForTest()
}
```
Then initialize `FirebaseApp` unconditionally in `setUp`. `clearInstancesForTest` is `@VisibleForTesting`; if it is not visible from this module, call `FirebaseApp.getApps(ctx).forEach { it.delete() }` instead.

### WR-03: Two pinned production defects are not recorded in `deferred-items.md`

**File:** `feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/FallbackFeedbackUrlTest.kt:55-105`, `core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/MapperTest.kt:321-341`
**Issue:** `deferred-items.md` closes with "every characterized bug is pinned". Two defects are pinned by assertions here but have no entry.
1. The fallback feedback URL contains `//`. `WebLinks.OPENFEEDBACK_BASE_URL` already ends with `/`, and `getFallbackUrlForSession` appends another. Every `FallbackFeedbackUrlTest` case asserts `https://openfeedback.io/devfestnantes2025//2025-10-16/form-1`. The file header calls this "today's URL" and says the tests "MUST pass unchanged". The test therefore makes the fix a failing change, and nobody has tracked it. The URL is user-facing: it opens the OpenFeedback form.
2. The mapper appends a fixed `+02:00` to zone-less backend timestamps (`start_dates_get_the_paris_offset_appended`). That is wrong for any date outside Paris summer time (DST ends 2026-10-25). The tests lock in the fixed offset without a note.

Production behavior here is unchanged by Phase 5, so these are not regressions. They are test-enshrined defects missing from the ledger.
**Fix:** Add both to `deferred-items.md` (the deferred ledger entries 4 and 17 style). For the URL, consider changing the test to assert the intended single-slash URL once fixed, or add a comment pointing at the new entry.

## Info

### IN-01: Stale and redundant text in the characterization tests

**File:** `core/ui/src/test/java/com/gdgnantes/devfest/core/ui/utils/DateUtilsCharacterizationTest.kt:18-24`, `feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/utils/ScheduleSlotFormattedRangeTest.kt:16-22`, `core/ui/src/androidTest/java/com/gdgnantes/devfest/core/ui/utils/ScheduleSlotDateFormatAndroidTest.kt:4,16-21`
**Issue:**
- The KDocs say the tests "MUST pass unchanged after the swap" and "were written against the `SimpleDateFormat` implementation". The swap is done, so these now describe history rather than the code.
- `ScheduleSlotDateFormatAndroidTest` imports `getDateFromIso8601` from its own package, so the import is redundant. Its `try/catch (e: Exception)` wraps an `AssertionError`, which is an `Error` and is never caught, so the wrapping only adds noise. The test is also a strict subset of `DateUtilsCharacterizationTest`.
- `ScheduleSlotFormattedRangeTest` hard-codes U+2009 and U+202F in its expected strings. These come from the CLDR data inside the Robolectric android-all 34 jar, so changing `@Config(sdk=...)` will break the test.

**Fix:** Reword the KDocs in the present tense. Delete the redundant import and the try/catch, or drop `ScheduleSlotDateFormatAndroidTest`. Note the SDK dependence in the KDoc of `ScheduleSlotFormattedRangeTest`.

### IN-02: Characterization tests are blind to a non-Paris device time zone

**File:** `core/ui/src/test/java/com/gdgnantes/devfest/core/ui/utils/DateUtilsCharacterizationTest.kt:35,86-97`
**Issue:** `getDayFromIso8601(Agenda.DAY_ONE_ISO, ...)` formats an instant at midnight UTC in the device zone. The tests pin `Europe/Paris`, so they pass. For a device in a zone west of UTC, such as New York, the day tab for day one would read "October 15". The production behavior is unchanged by Phase 5 (the old code did the same), but the new tests lock the time zone and so cannot see it.
**Fix:** Add one case with `TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))` that documents the behavior, or log it in `deferred-items.md`.

### IN-03: Live-network canary tests assert nothing

**File:** `core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreLiveNetworkTest.kt:45-85`
**Issue:** Several tests have no assertion (`getRoom_returns_room_or_null`, `getSession_returns_session_or_null`, `getSpeaker_returns_speaker_or_null`). Others call `assertNotNull` on non-nullable types (`agenda`, `rooms`, `sessions`, `venue`), which is always true. Only the added `scheduleSlot_dates_are_parseable_by_the_shared_parser` and the non-empty speakers test can fail. A canary that cannot detect schema drift gives false confidence. The file is excluded from the default run, so the cost is small.
**Fix:** Assert something observable, for example `assertTrue(rooms.isNotEmpty())` and `assertTrue(sessions.isNotEmpty())`.

### IN-04: Mock server tests depend on the exact number of requests the store makes

**File:** `core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreMockServerTest.kt:173-187,297-327`, `GraphQLStoreContractTest.kt:22-38`, `RecordedResponsesHandler.kt:27`
**Issue:**
- `getSpeakerSessions_filters_by_speaker_id` enqueues exactly 3 responses, `getters_return_null_or_empty_on_error` enqueues 4 errors, and the venue fallback test enqueues 2. If `GraphQLStore` adds a retry or an extra request, responses shift between calls and the tests fail with confusing assertions rather than a clear message.
- `GraphQLStoreContractTest.createStore()` overwrites the `server` and `client` fields without closing the previous ones. A contract test that calls `createStore()` twice would leak the first server and client (only the second is closed in `releaseStore`).
- `RecordedResponsesHandler` matches `"language":"fr"` with a literal substring, which breaks if Apollo ever changes its JSON whitespace.

**Fix:** Close any previous `server` and `client` at the top of `createStore()`. Prefer `RecordedResponsesHandler` (keyed by operation) over request-order queues for the `getSpeakerSessions` and error tests where possible. Parse the request body as JSON before reading `variables.language`.

### IN-05: `VenueViewModelTest` builds the ViewModel before the dispatcher rule starts

**File:** `feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/VenueViewModelTest.kt:25-28`
**Issue:** `private val viewModel = VenueViewModel(...)` runs during instance creation, before `MainDispatcherRule.starting` calls `Dispatchers.setMain`. Today this is safe because `viewModelScope` is created lazily on the first `venue` access, inside the test body. If a later change touches `viewModelScope` in the ViewModel's init block, the scope would be built against the real Main dispatcher. The tests also read the locale only through `@Config(qualifiers)`, so the class-level instance depends on Robolectric configuring the environment first.
**Fix:** Build it per test, for example `private fun viewModel() = VenueViewModel(FakeDevFestNantesStore())`, or use `by lazy`.

### IN-06: Test-only code ships in production source sets

**File:** `core/network/build.gradle.kts:23-27`, `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/*.kt`
**Issue:** `generateDataBuilders` plus `connectToKotlinSourceSet("commonMain")` compiles the Apollo data builders (`buildGraphQLSession`, ...) into the production `:core:network` artifact, and the fixtures live in `:core:model` `commonMain`. The build comment explains why: `:core:data` tests cannot see `:core:network`'s own `commonTest`. R8 strips the unused builders from release builds, so the user impact is small. The generated package is excluded from Kover, but the builders still enlarge the public API and the iOS framework surface.
**Fix:** Acceptable as a deliberate trade-off. If it becomes a problem, move the builders to a `java-test-fixtures`-style or dedicated `:core:network-testing` module that only test source sets depend on.

---

_Reviewed: 2026-10-16_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
