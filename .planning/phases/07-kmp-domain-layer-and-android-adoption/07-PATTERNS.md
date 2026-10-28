# Phase 7: KMP Domain Layer and Android Adoption - Pattern Map

**Mapped:** 2026-10-28
**Files analyzed:** 20 (new/modified groups)
**Analogs found:** 18 / 20
(All analog paths verified git-tracked via `git ls-files`/Glob; none are gitignored mirrors.)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `core/domain/build.gradle.kts` | config | n/a | `core/data/build.gradle.kts` | exact |
| `settings.gradle.dcl`, root `build.gradle.kts` (Kover list) | config | n/a | existing `:core:data` entries | exact |
| `core/domain/.../di/DomainModule.kt` | provider (Koin) | DI | `core/data/.../di/DataModule.kt` | exact |
| `core/domain/.../agenda/ObserveAgendaUseCase.kt` | service (use case) | streaming (combine) | `feature/agenda/.../AgendaViewModel.kt` (lines 64-167, logic being moved) | role-match |
| `core/domain/.../agenda/FilterSessionsUseCase.kt` | service (policy) | transform | `AgendaViewModel.filterSessions` | exact (moved) |
| `core/domain/.../agenda/GroupSessionsUseCase.kt` (+ `DaySchedule`, `TimeSlot`, `DayAssignment`) | service/model | transform | `AgendaColumn.groupSessionsByStartTime` + `core/model/.../Agenda.kt` Builder | exact (moved) |
| `ClassifySessionUseCase`, `SpeakerIndexUseCase`, `VenueLanguageUseCase` | service (policy) | transform | `SessionTypeUtils.kt`, `Speakers.kt:166-175`, venue `toContentLanguage` | exact (moved) |
| `core/model/.../SessionFilter.kt` (data class + top-level `SessionFilterType`) | model | n/a | `feature/agenda/.../utils/SessionFilter.kt` | exact (moved) |
| `core/data/commonMain/.../SessionFiltersStore.kt` + `SessionFilterCodec.kt` | store interface | CRUD / state | `core/data/.../BookmarksStore.kt` | exact |
| `core/data/androidMain/.../SharedPreferencesSessionFiltersStore.kt` | store impl | CRUD | `SessionFiltersServiceImpl` (SessionFiltersService.kt) | exact (moved) |
| `core/data/iosMain/.../UserDefaultsBookmarksStore.kt`, `UserDefaultsSessionFiltersStore.kt` | store impl | CRUD | `core/data/androidMain/.../BookmarksStoreImpl.kt` | role-match |
| `DataPlatformModule.android.kt` / `.ios.kt` | provider | DI | themselves | exact |
| `core/testing/.../FakeSessionFiltersStore.kt` (+ `testDoublesModule`) | test double | state | `core/testing/.../FakeBookmarksStore.kt` | exact |
| `shared/.../di/InitKoin.kt`, `shared/build.gradle.kts` | config | DI | themselves | exact |
| `feature/agenda/.../AgendaViewModel.kt`, `di/AgendaModule.kt`, `AgendaColumn.kt` | controller/provider | request-response | themselves | modify |
| `core/domain/src/commonTest` and `jvmTest` (policy + goldens + `DomainModuleTest`) | test | transform | `DataModuleTest.kt`, `GraphQLStoreMockServerTest.kt` | role-match |
| `core/data` androidHostTest `SharedPreferencesSessionFiltersStoreTest` | test | CRUD | `BookmarksStoreImplTest.kt` | exact |
| `shared/src/jvmTest/.../KoinGraphTest.kt` | test | n/a | itself | modify |
| `iosApp/DevFestNantesTests/*Characterization*.swift` | test | transform | `iosApp/DevFestNantesTests/SessionFilterTests.swift` | role-match |
| `readTestData` expect/actual, recorded snapshot JSON, golden files | test util | file-I/O | none | no analog (spike in RESEARCH Pitfall 1) |
| `scripts/swift-names/*`, `.github/workflows/ios.yml` | config | n/a | themselves | modify |

## Pattern Assignments

### `core/domain/build.gradle.kts` (config)
**Analog:** `core/data/build.gradle.kts` (lines 1-33)
```kotlin
plugins {
    id("devfest.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            implementation(project(":core:network"))   // domain: api(project(":core:data")) instead
            implementation(libs.koin.core)
        }
        jvmTest.dependencies { implementation(libs.koin.test) }
    }
}
```
Domain uses `api(":core:data")` (use-case constructors expose store types). Add `include(":core:domain")` to `settings.gradle.dcl`, `kover(project(":core:domain"))` to the root list, `implementation(project(":core:domain"))` to the `devfest.android.feature` convention plugin, and `export(project(":core:domain"))` + `api(...)` in `shared/build.gradle.kts` (mirrors lines 9-20 there). Add `:core:domain:iosSimulatorArm64Test` to `.github/workflows/ios.yml` (the `./gradlew :shared:iosSimulatorArm64Test :core:model:... :core:data:... :core:testing:...` line).

### `core/domain/.../di/DomainModule.kt` (Koin provider)
**Analog:** `core/data/src/commonMain/.../di/DataModule.kt`
```kotlin
@HiddenFromObjC
val dataModule: Module =
    module {
        includes(dataPlatformModule)
        single { GraphQLStore(get()) } bind DevFestNantesStore::class
    }
```
Copy: `@HiddenFromObjC val domainModule`, `factory { ObserveAgendaUseCase(get(), get(), get(), get(named(DOMAIN_DISPATCHER))) }`, and `single<CoroutineDispatcher>(named("domainDispatcher")) { Dispatchers.Default }`. Append `domainModule` to `sharedModules` in `shared/.../di/InitKoin.kt` line 13: `listOf(networkModule, dataModule, analyticsModule)`. No duplicate bindings (allowOverride(false)).

### `ObserveAgendaUseCase` (streaming)
**Analog:** `feature/agenda/.../AgendaViewModel.kt` combine chain (lines 64-84):
```kotlin
store.agenda
    .map { agenda -> agenda.days }
    .onEach { ... }
    .combine(sessionFilters) { _, _ -> }
    .combine(bookmarksStore.bookmarkedSessionIds) { _, _ -> }
    .collect { updateFilteredDays() }
```
Replace with `combine(store.sessions, filtersStore.filters, bookmarksStore.bookmarkedSessionIds) {...}.flowOn(dispatcher)` as in RESEARCH "Combined flow skeleton". Annotate with `@NativeCoroutines` (same import as `BookmarksStore.kt`: `com.rickclephas.kmp.nativecoroutines.NativeCoroutines`). Read `store.sessions` inside `observe()`. The ViewModel keeps `autoRefreshJob?.cancel()`, `traceDataLoading`, `_uiState`.

### `FilterSessionsUseCase` (transform)
**Analog:** `AgendaViewModel.kt:112-167` `filterSessions`. Match rules to copy verbatim: BOOKMARK -> membership in bookmarked set, LANGUAGE -> `filter.value == session.language?.name`, COMPLEXITY -> `complexity?.name`, ROOM -> `room?.id`, TYPE -> `type?.name`. Semantics: OR within type, AND across types (`retainAll`); empty filters -> all sessions. Use the RESEARCH "Filter semantics" sketch (`byType.all { group.any {...} }`). Keep store order (old HashSet order was unspecified). Move the `AgendaViewModelTest` filter matrix to cover it; characterize before moving (D-11).

### Grouping / `DayAssignment` / models
**Analog 1:** `feature/agenda/.../AgendaColumn.kt:104-128` `groupSessionsByStartTime` (private; make `internal` in the characterization commit). In-slot order: `sortedBy room.name` then `sortedBy room.sortIndex`, nulls first. Shared version: `compareBy<Session>({ it.room?.sortIndex }, { it.room?.name })`, slot key `Instant`.
**Analog 2:** `core/model/.../Agenda.kt` Builder (24 h windows: `startInstant.minus(DAY_ONE).inWholeDays == 0L`; constants `DAY_ONE_ISO`/`DAY_TWO_ISO` at lines ~31-32): `DayAssignment.utcWindows` must reuse these constants (single source). `DayAssignment.localDate(timeZone)` mirrors `AgendaViewModel.swift` `sessionsChanged`.
Names: `DaySchedule(dayIndex, date, slots)`, `TimeSlot(start: Instant, sessions)` as data classes. Do NOT reuse `AgendaDay` (exists in `:core:model` and Swift header). Use `kotlin.time.Instant`. Avoid default args and `Map`/`Pair` in public API. LazyColumn header key must use `slot.start` (not the formatted label).

### Classification, speaker index, venue language
**Analogs:** `feature/agenda/.../utils/SessionTypeUtils.kt` (`fun SessionType.isService() = this == OPENING || BREAK || LUNCH`); navigable = QUICKIE/CONFERENCE/CODELAB (`MainActivity.kt` ~151-157); `Speakers.kt:166-175` `getMapOfFirstUniqueCharIndex`; venue `Locale.toContentLanguage` (`"fr"->FRENCH`, else ENGLISH). Null type -> neither service nor navigable.

### `core/model/.../SessionFilter.kt`
**Analog:** `feature/agenda/.../utils/SessionFilter.kt`
```kotlin
@Serializable
class SessionFilter(val type: FilterType, val value: String) {
    enum class FilterType { BOOKMARK, COMPLEXITY, LANGUAGE, ROOM, TYPE }
}
```
Becomes `@Serializable data class SessionFilter(val type: SessionFilterType, val value: String)` plus top-level `enum class SessionFilterType` (same entry order; JSON stays `{"type":"LANGUAGE","value":"FRENCH"}`). Check `:core:model` has the serialization plugin. Update Swift-names exclusion (see Shared Patterns).

### `SessionFiltersStore` (interface, `core/data/commonMain`)
**Analog:** `core/data/.../BookmarksStore.kt` (interface in `:core:data` commonMain with `@NativeCoroutines`). Use `@NativeCoroutinesState val filters: StateFlow<Set<SessionFilter>>` and `fun setFilters(filters: Set<SessionFilter>)`. `SessionFilterCodec` (commonMain) holds `Json.encodeToString(SessionFilter.serializer(), it)` / `Json.decodeFromString<SessionFilter>` and skips malformed entries, catching `SerializationException` and `IllegalArgumentException` (no Timber in common).

### `SharedPreferencesSessionFiltersStore` (androidMain)
**Analog:** `SessionFiltersServiceImpl` in `feature/agenda/.../services/SessionFiltersService.kt` (lines 17-69): ctor `(SharedPreferences)`, key `"SHARED_PREFERENCES_KEY_AGENDA_FILTERS"`, `MutableStateFlow(loadFilters())`, `sharedPreferences.edit { putStringSet(...) }`. Move the body, keep ctor and key. Bind in `DataPlatformModule.android.kt`:
```kotlin
single { BookmarksStoreImpl(get()) } bind BookmarksStore::class
// add:
single { SharedPreferencesSessionFiltersStore(get()) } bind SessionFiltersStore::class
```
Delete `SessionFiltersService`/`Impl` and the `AgendaModule.kt` binding (`single { SessionFiltersServiceImpl(get()) } bind SessionFiltersService::class`) in the SAME commit that switches all consumers (AgendaViewModel, HomeViewModel bridge, tests).

### iOS stores (`core/data/iosMain`)
**Analog:** `BookmarksStoreImpl.kt` (androidMain) for the surface (`bookmarkedSessionIds`, `isBookmarked`, `setBookmarked`, `subscribe` = `_selectedSessionIds.asStateFlow().map { it.contains(id) }`, key `selected_sessions`). Differences: take `NSUserDefaults` in the ctor, use `MutableStateFlow.update {}` (not the unsafe `HashSet` mutation), new keys (e.g. `devfest.bookmarks.sessionIds`, `devfest.agenda.filters`), never the legacy `Favorites`/`AgendaSessionFilters`. Bind in `DataPlatformModule.ios.kt` (currently `module { }`):
```kotlin
internal actual val dataPlatformModule: Module = module { }
```

### `FakeSessionFiltersStore` (test double)
**Analog:** `core/testing/.../FakeBookmarksStore.kt`
```kotlin
class FakeBookmarksStore : BookmarksStore {
    private val bookmarks = MutableStateFlow<Set<String>>(emptySet())
    override val bookmarkedSessionIds: Flow<Set<String>> = bookmarks
```
Same shape with `MutableStateFlow<Set<SessionFilter>>`; register in `TestDoublesModule`.

### `AgendaViewModel.kt` / `AgendaModule.kt` (modify)
Current ctor: `(store, bookmarksStore, performanceMonitoring, sessionFiltersService, ioDispatcher)`. New: `(observeAgenda, filtersStore, performanceMonitoring, ...)`. Module line to update: `viewModel { AgendaViewModel(get(), get(), get(), get(), get(named(IO_DISPATCHER))) }`. Keep `_uiState`, refresh/cancel, `rooms`, `sessionFilters`. `IO_DISPATCHER` (in `CoreUiModule.kt:19`) may become unused; planner decides.

### Tests

**`DomainModuleTest` (jvmTest)** - analog `core/data/src/jvmTest/.../di/DataModuleTest.kt`:
```kotlin
@OptIn(KoinExperimentalAPI::class)
class DataModuleTest {
    @Test fun data_module_graph_is_complete_given_the_apollo_client() {
        dataModule.verify(extraTypes = listOf(ApolloClient::class))
    }
    @Test fun data_module_verify_inspects_the_graphql_store_to_apollo_client_edge() {
        assertFailsWith<MissingKoinDefinitionException> { dataModule.verify() }
    }
}
```
Use `extraTypes = listOf(DevFestNantesStore::class, BookmarksStore::class, SessionFiltersStore::class)`.

**`KoinGraphTest.shared_modules_graph_is_complete`** (`shared/src/jvmTest/.../KoinGraphTest.kt`): `module { includes(sharedModules) }.verify()` will fail once domain needs `BookmarksStore`/`SessionFiltersStore` (no JVM binding). Add them as `extraTypes`. Also update `AndroidGraphVerifyTest` and `AgendaModuleTest` (`SessionFiltersService::class` -> `SessionFiltersStore::class`).

**Android store test** - analog `core/data/src/androidHostTest/.../BookmarksStoreImplTest.kt`:
```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BookmarksStoreImplTest { private lateinit var prefs: SharedPreferences
    @Before fun setUp() { val context = ApplicationProvider.getApplicationContext<Context>() ... PreferenceManager ... }
```
Move `SessionFiltersServiceImplTest` here, changing only class under test/imports; keep literal key and JSON asserts.

**Snapshot replay** - analog `core/data/src/commonTest/.../graphql/GraphQLStoreMockServerTest.kt`: `withStore { store, server -> }` builds `MockServer()`, `ApolloClient.Builder().serverUrl(server.url())`, `GraphQLStore(client)`, `server.serve(json)` = `enqueueString(json, contentType = JSON)`. `GraphQLStore` is internal, so raw-JSON replay lives only in `:core:data` tests; dump a neutral model snapshot for `:core:domain` tests.

**Swift characterization** - analog `iosApp/DevFestNantesTests/SessionFilterTests.swift`:
```swift
import Testing
import Foundation
@testable import DevFest_Nantes

@Suite struct SessionFilterTests {
    @Test func decodes_the_legacy_agenda_session_filters_payload() throws { ... #expect(decoded == [...]) }
```
Test pure/static helpers only (never construct a ViewModel; Koin starts in `init`). Every new Swift file needs explicit `project.pbxproj` entries.

## Shared Patterns

### Koin rules
**Source:** `DataModule.kt`, `InitKoin.kt` (`allowOverride(false)`). One module per Gradle module, remove the old binding in the same commit the new one appears; `bash scripts/di-gate.sh` must pass and cover the new module.

### Swift export hygiene
**Source:** `scripts/swift-names/names-exclude.txt` (format `NAME<TAB>reason`, with per-name evidence). Add `SessionFilter` with evidence `iosApp/iosApp/Model/SessionFilter.swift defines struct SessionFilter`. Use `@HiddenFromObjC` on `domainModule`. Regenerate baselines and run `bash scripts/swift-names-gate.sh check` in the same commit as export changes, and run `xcodebuild ... build` to prove the Swift struct still wins lookup.

### NativeCoroutines
**Source:** `BookmarksStore.kt`. Every public Flow member gets `@NativeCoroutines` (or `@NativeCoroutinesState` for `StateFlow`).

### Characterization first (D-11)
Characterization commit is green on old code; the move commit changes only imports, type names, constructor wiring and selectors (no expected value, key or JSON literal edited).

## No Analog Found

| File | Role | Data Flow | Reason |
|---|---|---|---|
| `readTestData` expect/actual (jvmTest env/system property, iosTest `NSProcessInfo.environment`) + Gradle `SIMCTL_CHILD_` env wiring with `inputs.dir(testdata)` | test util | file-I/O | No test currently reads repo files; mechanism from RESEARCH Pitfall 1 spike |
| Recorded snapshot JSON, golden files, re-record script | test asset | file-I/O | `RecordedResponses.kt` constants exist but are too small/inline (213 KB sessions); use files |

## Metadata

**Analog search scope:** `core/data`, `core/testing`, `core/model`, `feature/agenda`, `shared`, `iosApp/DevFestNantesTests`, `scripts/swift-names`
**Files scanned:** about 20 read, rest via `git ls-files`
**Pattern extraction date:** 2026-10-28
**Note:** line numbers beyond those quoted come from RESEARCH.md citations; RESEARCH lines 429-631 (later sections) were not read.
