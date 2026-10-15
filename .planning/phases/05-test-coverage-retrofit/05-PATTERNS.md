# Phase 5: Test Coverage Retrofit - Pattern Map

**Mapped:** 2026-10-15
**Files analyzed:** 27 new/modified
**Analogs found:** 25 / 27 (all analog paths verified git-tracked)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match |
|---|---|---|---|---|
| `core/model/.../stubs/StoreStubs.kt` (modify, fixed fixtures; split per entity) | fixture | transform | itself + `stubs/RoomStubs.kt`, `CategoryStubs.kt`, `SocialItemStubs.kt` | exact |
| `core/model/.../utils/IsoDate.kt` (new, `String.toInstantOrNull`, `Instant.toLocalDate`) | utility | transform | `ScheduleSlot` parsing (`Instant.parse`), `Agenda.kt` `toLocalDateTime(TimeZone.currentSystemDefault())` | role-match |
| `core/model/src/commonTest/.../IsoDateParsingTest.kt`, `FixturesTest.kt` (new) | test | transform | `core/model/src/commonTest/.../ScheduleSlotDateParsingTest.kt` | exact |
| `core/data/.../DevFestNantesStoreMocked.kt` (modify) | store | CRUD | itself | exact |
| `core/data/src/commonTest/.../StoreContractTest.kt` (abstract) + Mocked/GraphQL subclasses | test | request-response | `core/data/src/commonTest/.../DevFestNantesStoreContractTest.kt` | exact |
| `core/data/src/commonTest/.../graphql/MapperTest.kt`, `RecordedResponses.kt` | test | transform | `GraphQLStoreJvmTest.kt` (setup) + `ScheduleSlotDateParsingTest.kt` (value style) | role-match |
| `core/data/src/commonTest/.../graphql/GraphQLStoreMockServerTest.kt` | test | request-response | `core/data/src/jvmTest/.../graphql/GraphQLStoreJvmTest.kt` | role-match |
| `core/data/src/jvmTest/.../graphql/GraphQLStoreLiveNetworkTest.kt` (rename of JvmTest, canary) | test | request-response | itself | exact |
| `core/data/src/androidHostTest/.../BookmarksStoreImplTest.kt` | test | CRUD (prefs) | `androidApp/src/test/.../KoinStartupTest.kt` (Robolectric setup) | role-match |
| `feature/agenda/src/test/.../services/SessionFiltersServiceImplTest.kt`, `feature/settings/src/test/.../DataCollectionSettingsServiceImplTest.kt` | test | CRUD (prefs) | `KoinStartupTest.kt` | role-match |
| `core/testing/.../FakeDevFestNantesStore.kt` (new) | fake | pub-sub (StateFlow) | `core/testing/.../FakeBookmarksStore.kt` | exact |
| `core/testing/src/androidMain/.../MainDispatcherRule.kt` (new) | test util | event-driven | `KoinStartupTest.kt` setMain/resetMain lines | partial |
| `core/testing/.../FakeStores.kt`, `di/TestDoublesModule.kt` (modify) | fake/config | n/a | themselves | exact |
| `feature/*/src/test/.../*ViewModelTest.kt` (10 files) | test | request-response/flow | `FakeBookmarksStoreTest.kt` + `KoinStartupTest.kt` | role-match |
| `feature/agenda/.../AgendaViewModel.kt` + `di/AgendaModule.kt` + `AgendaModuleTest.kt` (modify, dispatcher) | viewmodel/config | flow | `SpeakerViewModel` explicit lambda binding, `AgendaModuleTest` | exact |
| `core/ui/.../di/CoreUiModule.kt` (modify, bind named dispatcher) | config | n/a | itself | exact |
| `core/ui/.../utils/DateUtils.kt`, `feature/session-detail/.../FallbackFeedbackForm.kt` (modify) | utility | transform | themselves | exact |
| `core/ui/src/androidTest/.../ScheduleSlotDateFormatAndroidTest.kt` (adapt) | test | transform | itself | exact |
| `feature/*/src/androidTest/.../*SmokeTest.kt` (one per feature) | test | request-response | `androidApp/src/androidTest/.../MainActivityTest.kt` + `core/testing/.../AndroidTestDoublesModule.kt` | role-match |
| `build-logic/.../KmpLibraryConventionPlugin.kt`, `AndroidFeatureConventionPlugin.kt` (modify); new Kover convention | config | n/a | those files | exact |
| `gradle/libs.versions.toml` (modify) | config | n/a | itself | exact |
| `.github/workflows/android.yml:49`, `ios.yml:131` (modify) | config | n/a | themselves | exact |

## Pattern Assignments

### Fixtures: `StoreStubs.kt` (fixture, transform)
**Analog:** itself (`core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/StoreStubs.kt`)

Keep public `buildSessionStub()/buildSpeakerStub()/buildPartnerStub()/buildScheduleSlotStub()` signatures as deterministic wrappers (7 `@Preview` callers). Remove `import kotlin.random.Random` (line 12). Current offenders to replace (lines 24, 26, 30, 34, 49, 62):
```kotlin
id = Random.nextLong().toString(),
category = categoryStubs[Random.nextInt(categoryStubs.size)],
openFeedbackFormId = "", // TODO Replaces with UUID implementation
room = roomStubs[Random.nextInt(roomStubs.size)],
MutableList(Random.nextInt(1, MAX_SPEAKER_PER_SESSION)) { buildSpeakerStub() },
id = "", // TODO Replace with UUID implementation
```
Date style to keep: `Agenda.DAY_ONE` + `Duration`, `startDate = start.toString()`. Do NOT change `buildVenueStub` (production fallback in `GraphQLStore.getVenue`). Detekt scans commonMain: use named constants, `@file:Suppress("TooManyFunctions")` as in `Mappers.kt`. Sessions must start on 2025-10-16/17 UTC and be pairwise unequal.

### `DevFestNantesStoreMocked.kt` (store, CRUD)
**Analog:** itself (`core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreMocked.kt`). Replace lines 22-49 (`Random.nextInt` counts, 3 partner categories) with fixture lists; remove `import kotlin.random.Random` (l.19), `MAX_*` imports. Flow pattern stays (`flow { emit(...) }`, `firstOrNull` lookups, lines 51-89). Implement `getSpeakerSessions` (line 89, currently `emptyList()`) by filtering `sessionsCache`.

### `IsoDate.kt` and its test (utility, transform)
**Analog test:** `core/model/src/commonTest/.../ScheduleSlotDateParsingTest.kt` lines 1-57: package `com.gdgnantes.devfest.core.model`, `kotlin.test.*` only, expected values as absolute epoch millis, snake_case names:
```kotlin
val offsetSlot = ScheduleSlot(startDate = "2025-10-16T11:00:00+02:00", endDate = "2025-10-16T11:45:00+02:00")
assertEquals(1760605200000L, offsetSlot.startDateAsEpochMilliseconds)
assertEquals(1760572800000L, Instant.parse(Agenda.DAY_ONE_ISO).toEpochMilliseconds())
```
Use `kotlin.time.Instant` (not `kotlinx.datetime.Instant`/Clock; note the test file still imports `kotlinx.datetime.Instant`). Pass `TimeZone` as a parameter so tests use UTC / Europe/Paris. Add `LocalDate.toString()` ASCII assertion (A8). Characterization tests land before the swap (D-07).

### Date call-site migration
**`FallbackFeedbackForm.kt:44-52`** (replace `SimpleDateFormat` block):
```kotlin
private fun getFallbackUrlForSession(session: Session): String? {
    val openFeedbackFormId = session.openFeedbackFormId ?: return null
    return getDateFromIso8601(session.scheduleSlot.startDate)?.run {
        val pattern = "yyyy-MM-dd"
        val simpleDateFormat = SimpleDateFormat(pattern)
        simpleDateFormat.format(this)
    }?.run { "${WebLinks.OPENFEEDBACK_BASE_URL.url}/$this/$openFeedbackFormId" }
}
```
Target shape: `session.scheduleSlot.startDate.toInstantOrNull()?.toLocalDate()` then `"${WebLinks.OPENFEEDBACK_BASE_URL.url}/$date/$openFeedbackFormId"`. Extract so it is unit-testable (Robolectric test with non-Latin locale).
**`core/ui/.../utils/DateUtils.kt`**: `getDateFromIso8601` (two SimpleDateFormat patterns) -> delegate to common parser; `getDayFromIso8601` keeps `android.text.format.DateUtils.formatDateTime`. Existing characterization: `core/ui/src/androidTest/.../ScheduleSlotDateFormatAndroidTest.kt` (AndroidJUnit4 runner, loops over `"2024-10-17T08:00:00+02:00"` strings); keep the `getDateFromIso8601` test, drop/adapt the raw `SimpleDateFormat` one.

### Store contract tests (test, request-response)
**Analog:** `core/data/src/commonTest/.../DevFestNantesStoreContractTest.kt` (lines 1-71). Keep imports (`kotlinx.coroutines.flow.first`, `kotlinx.coroutines.test.runTest`, `kotlin.test.Test`) and `runTest { store.x.first() }` shape; make class `abstract` with `abstract fun createStore(): DevFestNantesStore`; replace "no crash" tests (lines 24-28, 36-40, 48-52) with `assertNull(store.getSession("unknown"))` and fixture-constant assertions. Subclasses: `MockedStoreContractTest` (`DevFestNantesStoreMocked()`), `GraphQLStoreContractTest` (MockServer).

### `GraphQLStore` MockServer test
**Analog:** `core/data/src/jvmTest/.../graphql/GraphQLStoreJvmTest.kt` lines 12-23 (client + store setup):
```kotlin
apolloClient = ApolloClient.Builder()
    .serverUrl("https://confetti-app.dev/graphql")
    .httpHeaders(listOf(HttpHeader("conference", "devfestnantes2024")))
    .build()
store = GraphQLStore(apolloClient)
```
Swap URL for `MockServer().url()` (suspend, 0.3.x), add `.normalizedCache(MemoryCacheFactory(), cacheKeyGenerator, cacheResolver)` (public vals in `:core:network` `Apollo.kt`). RESEARCH Pattern 3 snippet: `server.enqueueString("""{"data":{"rooms":[...]}}""")`. Recorded JSON as Kotlin string constants in `RecordedResponses.kt` (no resources in commonTest). Mapper parse: `Operation.parseResponse(Buffer().writeUtf8(json).jsonReader()).dataOrThrow()` (not deprecated `parseJsonResponse`). Mappers are `internal` in `graphql/Mappers.kt`: test from same module.
**Canary:** rename `GraphQLStoreJvmTest` -> `GraphQLStoreLiveNetworkTest`, exclude in `jvmTest` via `filter.excludeTestsMatching("*LiveNetwork*")` unless `-PliveNetworkTests`.

### Robolectric prefs tests (test, CRUD)
**Analog:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt` lines 1-90:
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
...
val application = ApplicationProvider.getApplicationContext<Application>()
```
Get real prefs via `application.getSharedPreferences("test", Context.MODE_PRIVATE)`. Keys to pin: `BookmarksStoreImpl` `PREF_SELECTED_SESSIONS` (putStringSet), `SessionFiltersServiceImpl` `"SHARED_PREFERENCES_KEY_AGENDA_FILTERS"` (JSON-encoded `SessionFilter` string set, `feature/agenda/.../services/SessionFiltersService.kt:16-60`), plus consent key in `DataCollectionSettingsServiceImpl`. `:core:data` needs `withHostTest { isIncludeAndroidResources = true }` (see config) and source set `androidHostTest`; task `testAndroidHostTest`.

### `FakeDevFestNantesStore` and fakes (fake, StateFlow)
**Analog:** `core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeBookmarksStore.kt` lines 1-25 (KDoc style, `MutableStateFlow` private, `override val x: Flow<...> = state`, mutators). Seed from `:core:model` fixtures; add error injection hooks. Register in `di/TestDoublesModule.kt`:
```kotlin
single<DevFestNantesStore> { fakeDevFestNantesStore() }
single<BookmarksStore> { FakeBookmarksStore() }
```
(keep `fakeDevFestNantesStore()` helper in `FakeStores.kt`). Feature-owned interfaces (`SessionFiltersService`, `DataCollectionSettingsService`) get fakes inside their feature (copy the ~15-line fake already in `KoinStartupTest`). Fake tests: `core/testing/src/commonTest/.../FakeBookmarksStoreTest.kt`.

### `MainDispatcherRule` and ViewModel tests
**Analog:** `KoinStartupTest.kt` lines 24-28, 70-90:
```kotlin
Dispatchers.setMain(UnconfinedTestDispatcher())   // @Before
Dispatchers.resetMain()                            // @After
```
Wrap in a JUnit4 `TestWatcher` (`starting`/`finished`) exposing `dispatcher`; place in `:core:testing` androidMain (needs `junit` dep). ViewModel tests: `src/test`, `runTest`, Turbine `vm.uiState.test {}`, `kotlin.test` assertions, snake_case names, direct construction with fakes (no Koin). `VenueViewModel` and `FeedbackFormViewModel` need Robolectric (`Resources.getSystem()`, final `FirebaseRemoteConfig`; see KoinStartupTest dummy `FirebaseApp` block lines 80-92 or a small seam, Open Question 1).

### Dispatcher injection (config)
**Analog:** `feature/agenda/.../di/AgendaModule.kt` currently `viewModelOf(::AgendaViewModel)`; switch to explicit lambda (as `SpeakerViewModel` does): `viewModel { AgendaViewModel(get(), get(), get(), get(), get(named(IO_DISPATCHER))) }`. `AgendaViewModel` ctor (4 params: store, bookmarksStore, performanceMonitoring, sessionFiltersService) gains `ioDispatcher: CoroutineDispatcher`; replace `withContext(Dispatchers.IO)` in `updateFilteredDays`. Bind `single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }` in `core/ui/.../di/CoreUiModule.kt` (currently only `viewModelOf(::BookmarksViewModel)`). Update `AgendaModuleTest.kt` (lines 15-26) `extraTypes` list with `CoroutineDispatcher::class`:
```kotlin
agendaModule.verify(extraTypes = listOf(DevFestNantesStore::class, BookmarksStore::class, PerformanceMonitoring::class, SharedPreferences::class))
```
Also check `coreUiModuleTest` and `AndroidGraphVerifyTest` whitelist.

### Compose smoke tests (test, request-response)
**Analog:** `androidApp/src/androidTest/java/com/gdgnantes/devfest/android/MainActivityTest.kt` (`createAndroidComposeRule`, `hasTestTag`, `assertIsDisplayed`, `waitForIdle`) plus `androidTestDoubles` list from `core/testing/src/androidMain/.../di/AndroidTestDoublesModule.kt`:
```kotlin
val androidTestDoubles: List<Module> = listOf(testDoublesModule, androidTestDoublesModule)
```
Skeleton (RESEARCH): `KoinTestRule` + `createComposeRule()`, `modules(featureModule + coreUiModule + androidTestDoubles)` with `allowOverride(true)`, assert fixture title, click callback. Agenda is wall-clock dependent (Pitfall 6). Convention plugin must add `androidTestImplementation(project(":core:testing"))`, compose ui-test-junit4, runner/ext-junit, koin-test-junit4.

### Build-logic and catalog
**`KmpLibraryConventionPlugin.kt`:** add inside `targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach { ... }` (lines 36-43) `withHostTest { isIncludeAndroidResources = true }` (opt-in only for `:core:data`); extend `sourceSets.getByName("commonTest").dependencies { implementation(libs.findLibrary("kotlin-test").get()) ... }` (lines 63-66) with `turbine`. Catalog lookup idiom: `libs.findLibrary("...").get()`.
**`AndroidFeatureConventionPlugin.kt`:** existing `add("testImplementation", project(":core:testing"))` (line 24); add `androidTestImplementation` lines in the same style.
**Kover:** new convention plugin `KoverConventionPlugin` following `DetektConventionPlugin.kt` shape (applied by id string via `pluginManager.apply("devfest.detekt")`). Catalog: put `[libraries]`/`[plugins]` entries before `[bundles]`.
**CI:** `android.yml:49` `./gradlew testDebugUnitTest jvmTest` -> add `testAndroidHostTest`; `ios.yml:131` `./gradlew :shared:iosSimulatorArm64Test` -> add `:core:model`, `:core:data`, `:core:testing` `iosSimulatorArm64Test`.

## Shared Patterns

### Test conventions
Source: `DevFestNantesStoreContractTest.kt`, `ScheduleSlotDateParsingTest.kt`. `kotlin.test.Test`, `runTest`, snake_case fun names, KDoc explaining why the test exists, values asserted as constants not `assertNotNull`.

### Koin graph verification
Source: `feature/agenda/src/test/.../AgendaModuleTest.kt:11-26` (`@OptIn(KoinExperimentalAPI::class)`, `module.verify(extraTypes = ...)`). Apply to every module whose constructor changes.

### Koin test doubles
Source: `core/testing/.../di/TestDoublesModule.kt` KDoc: list after production modules, `allowOverride(true)`, never referenced from production.

### DI/Hilt gate on test code
`scripts/di-gate.sh` scans all tracked `.kt`, including tests. Forbidden: `KoinComponent`, `getKoin(`, `GlobalContext`, `KoinPlatform`, `koinInject`, and the substrings hilt/dagger/javax.inject. Allowed: `startKoin`, `stopKoin`, `koinApplication`, `KoinTestRule`.

### Detekt on commonMain
`configureKmpDetektSources()` scans commonMain/androidMain/iosMain only. Fixtures and fakes in `:core:model`/`:core:testing` main source sets must satisfy MagicNumber, LongMethod, MaxLineLength; run `./gradlew detekt`.

## No Analog Found

| File | Role | Data Flow | Reason |
|---|---|---|---|
| Kover convention plugin / baseline report | config | n/a | No coverage tooling exists; follow `DetektConventionPlugin.kt` shape only |
| `MainDispatcherRule` | test util | event-driven | No JUnit rule exists in repo; only inline setMain/resetMain in `KoinStartupTest` |
| Compose feature smoke tests with Koin | test | request-response | Only `MainActivityTest` (full activity, no Koin test rule); use RESEARCH skeleton (A4 `KoinTestRule.create` signature unverified) |
| Apollo data builders (`generateDataBuilders`) | codegen | transform | No precedent; spike first (A1), fallback to JSON variants |

## Metadata

**Analog search scope:** `core/*`, `feature/*`, `androidApp`, `shared`, `build-logic`, `.github/workflows`
**Files scanned:** ~45 tracked files via `git ls-files`
**Pattern extraction date:** 2026-10-15
