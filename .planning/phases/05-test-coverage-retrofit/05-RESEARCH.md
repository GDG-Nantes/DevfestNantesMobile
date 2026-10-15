# Phase 5: Test Coverage Retrofit - Research

**Researched:** 2026-10-15
**Domain:** Kotlin Multiplatform + Android test infrastructure (fixtures, Apollo GraphQL store tests, ViewModel tests, Robolectric, Compose UI tests, Kover)
**Confidence:** HIGH on build/tooling feasibility (verified by running a throwaway copy of the repo), MEDIUM on Compose-instrumented wiring (not run), MEDIUM on Kover for the pure-Android modules (not run)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Fixture determinism (TEST-01)**
- **D-01:** **Fully fixed, hand-written fixture data** replaces all randomness. Not a seeded `Random`: the user challenged that option because seeded data doesn't reveal intent ("why 3?") and shifts silently when someone adds or reorders a `random.next*()` call. `StoreStubs.kt` loses `kotlin.random.Random`, and so does `DevFestNantesStoreMocked` (its `Random.nextInt` session/partner counts).
- **D-02:** The fixed dataset **replaces `StoreStubs` in `:core:model`** (same location, no new module edges). It is the **single source** for `DevFestNantesStoreMocked`, Compose previews, `:core:testing` fakes and test assertions. Tests reference fixture constants (ids, titles, counts) instead of hard-coding magic numbers.
- **D-03:** The dataset is **deliberately varied** to make up for the lost randomness and cover the edge cases tests need: sessions on both days, every room, mixed languages/complexities/types/categories, a session with no room, null language/complexity, a speaker shared by two sessions, a session with several speakers, and partners across all categories. The planner/researcher derives the exact edge-case list from the filter logic (`AgendaViewModel.filterSessions`) and from `Mappers.kt` nullability.
- **D-04:** **Stable, unique ids** for sessions and speakers, plus a non-empty `openFeedbackFormId` where relevant. This resolves the `// TODO Replace with UUID implementation` stubs, so `getSession`/`getSpeaker`/`getSpeakerSessions` become testable on mock data. Mock mode never ships to users (production uses `setUseMockServer(false)`), so changing mock data is not a user-visible change.

**Date handling (TEST-01)**
- **D-05:** Replace `SimpleDateFormat` in production code with **Kotlin types (kotlinx-datetime / `kotlin.time.Instant`)**, not `java.time`. The user chose this for KMP consistency: the model already uses `kotlin.time.Instant`. **The OpenFeedback fallback URL date is built with `LocalDate.toString()`**, which is always ASCII `yyyy-MM-dd`. This fixes today's device-default-locale bug in `FallbackFeedbackForm.kt` (non-Latin-digit locales produce a broken URL). The output is identical for fr/en users. This is a **deliberate, user-approved behavior fix**, recorded the same way as Phase 4's D-02.
- **D-06:** **ISO-8601 parsing moves to `commonMain` in `:core:model`** (e.g. a `ScheduleSlot`/String extension returning `kotlin.time.Instant?`) and is tested in `commonTest`, so it runs on JVM and iOS. It must accept both formats the current parser handles: offset (`2024-10-17T08:00:00+02:00`) and legacy UTC (`…'Z'`). It returns null on unparseable input, as today. **Localized display stays Android-only** in `:core:ui` (`android.text.format.DateUtils.formatDateTime(context, instant.toEpochMilliseconds(), …)`), because kotlinx-datetime has no localized formatting. Timezone parity is kept by using `TimeZone.currentSystemDefault()` wherever the old code relied on the default-timezone `SimpleDateFormat`.
- **D-07:** **Characterization tests are written before the swap**: they pin today's parse results and formatted outputs for real confetti date strings, and they must pass both before and after the migration. Test-only `SimpleDateFormat` usages (`GraphQLStoreJvmTest`, `ScheduleSlotDateFormatAndroidTest`) are reviewed. Remove or adapt them where they only existed to guard the old parser. Reversibility: reversible.

**ViewModel test harness (TEST-03)**
- **D-08:** **Turbine** is added as a test-only dependency (version catalog + convention plugin) for asserting flow emission sequences (e.g. `UiState.STARTING → SUCCESS`, filter updates).
- **D-09:** **Inject the dispatcher** where a ViewModel hard-codes one: `AgendaViewModel.updateFilteredDays` uses `withContext(Dispatchers.IO)`, so it gets a constructor `CoroutineDispatcher` param bound in Koin. Tests pass the test dispatcher. Also check `AgendaColumn.kt:131` `withContext(Dispatchers.IO)` and handle it if it sits in a tested path. Add a shared **`MainDispatcherRule`** (or equivalent) to `:core:testing` for `viewModelScope`. These are behavior-neutral refactors. Update the Koin module `verify()` tests to match.
- **D-10:** **When a test exposes a real bug, characterize it, don't fix it.** Examples: no error handling in ViewModel `init {}`, a flow stuck in `STARTING` on store error, `println` logging. The test pins the current behavior (or is `@Ignore`d with a TODO pointing to the deferred item), and the bug goes into the phase's deferred list. This keeps the no-regression rule; fixes come later with feature work.
- **D-11:** Add a **controllable `FakeDevFestNantesStore`** to `:core:testing`, backed by `MutableStateFlow`s and seeded with the D-02 fixed dataset by default. Tests can push new values or make calls throw or emit errors. The existing `fakeDevFestNantesStore()` helper stays. Add other fakes as ViewModels need them (e.g. `FakeSessionFiltersService`, `FakeDataCollectionSettingsService`), also in `:core:testing` when they are shared, or in the feature's test source set when only one feature uses them. **Fakes only, no mocking framework.**
- **D-12:** **All 10 ViewModels are in scope**: `AgendaViewModel`, `SessionViewModel`, `FeedbackFormViewModel`, `SpeakersViewModel`, `SpeakerViewModel`, `VenueViewModel`, `PartnersViewModel`, `DataCollectionViewModel`, `BookmarksViewModel` (`:core:ui`), `HomeViewModel` (`:androidApp`). Test depth is proportional to logic: Agenda filtering (each `FilterType`, combined filters as intersection across types, bookmark filter, sort by start date) gets the most. Thin pass-through ViewModels get load/success plus their key action. ViewModels are constructed directly with fakes in JVM unit tests (`src/test`).

**Store & mapper tests (TEST-02)**
- **D-13:** GraphQL→model **mapper tests use both input styles**. **Recorded JSON responses** cover the happy path: trimmed real confetti-app.dev responses committed as test resources, parsed with Apollo's `parseJsonResponse`, then mapped. **Apollo data builders** (`generateDataBuilders`) cover edge cases (nulls, missing room/speakers, unknown enums). Tests live in `:core:data` `commonTest` where Apollo test APIs allow; otherwise in `jvmTest`.
- **D-14:** **`GraphQLStore` is tested with Apollo MockServer**: a real `ApolloClient` with a memory cache, served the same recorded JSON. The tests cover CacheAndNetwork, cache hits, and network failure after a cached success. That last case pins Phase 2's user-authorized `map → mapNotNull` offline-cache fix, so a network error must not overwrite cached data.
- **D-15:** The existing **live-network `GraphQLStoreJvmTest` stays as a manual-only canary**, excluded from the default `testDebugUnitTest jvmTest` run and runnable through a dedicated Gradle task/property (the planner picks the mechanism). It serves as a schema-drift check. The default CI run becomes offline and deterministic. No new CI workflow.
- **D-16:** **`DevFestNantesStoreContractTest` becomes a real contract**: an abstract contract class asserting concrete values against the fixed dataset, with explicit `getX(unknownId) → null` checks instead of "no crash" comments. It runs against **both** `DevFestNantesStoreMocked` and `GraphQLStore`-on-MockServer, wherever both can serve equivalent data. Where they can't, the shared contract asserts structural invariants and the implementation-specific value assertions go in per-implementation subclasses.
- **D-17:** **The SharedPreferences-backed implementations are tested with Robolectric**, reusing Phase 4's Robolectric 4.17 base: `BookmarksStoreImpl` (`:core:data` androidMain), `SessionFiltersServiceImpl` (`:feature:agenda`) and `DataCollectionSettingsServiceImpl` (`:feature:settings`). The tests run against real `SharedPreferences` and pin the persisted keys and format, so users' bookmarks, filters and consent survive any later refactor.

**Compose smoke tests & measurement (TEST-04)**
- **D-18:** **Measure with a Kover report and no threshold.** Add Kover through `build-logic` (convention plugin) and capture a **baseline report before the coverage work** and a final report after. Record both in VERIFICATION. No CI coverage gate, consistent with "no strict % target".
- **D-19:** **Compose smoke tests run as `androidTest` on the emulator**, in the existing CI instrumentation job (API 34, `connectedDebugAndroidTest`), one test class per feature module. Keep `MainActivityTest`. Watch for the Phase 3 D8 dex-merging OOM with more androidTest APKs (daemon heap is 4 GB). If it comes back, say so; don't silently shard or drop tests.
- **D-20:** **Smoke depth: render plus one key interaction per screen.** The feature `*Route` renders with fake fixed data (assert a known title/name from the D-02 dataset). One critical interaction per screen: bookmark toggle, applying a filter, a speaker/session click firing its callback, a consent toggle, and similar.
- **D-21:** **UI tests are wired through Koin test modules.** Each feature androidTest loads Koin with the feature's module plus `:core:testing`'s `testDoublesModule` (`FakeDevFestNantesStore` with the fixed dataset, fake analytics, no-op performance). The real `*Route` resolves its ViewModel via `koinViewModel()`, so the production wiring is exercised too. The planner handles Koin start/stop isolation between tests and the app's own `initKoin()` (e.g. a test `Application`/runner or `loadKoinModules` with overrides). Note that Phase 4 set `allowOverride(false)` in production `initKoin()`.

**iOS**
- **D-22:** **No Swift/XCTest work.** iOS coverage comes from `commonTest` running on `iosSimulatorArm64`. **Required CI adjustment:** iOS CI currently runs only `:shared:iosSimulatorArm64Test` (`.github/workflows/ios.yml:131`), so the new `commonTest` suites in `:core:model`/`:core:data` (and `:core:testing`) must be added to the iOS CI test step, or the step widened, so they actually run on iOS.

### Claude's Discretion
- Exact fixture content (names, counts) within D-03's coverage goals; the naming of fixture constants.
- Turbine, Kover, Apollo MockServer and kotlinx-datetime versions (re-verify the newest stable at plan time, per the Phase 2 newest-verified policy) and their placement in the catalog and convention plugins.
- Name and placement of the common ISO parser (D-06), as long as it is in `:core:model` commonMain.
- Mechanism for excluding the live canary (D-15): Gradle task, system property, or source set.
- Plan sequencing. Hard constraint: TEST-01 (fixtures + dates) lands first, and the Kover baseline is captured before the coverage additions. Otherwise follow the established pattern of one commit per logical step with CI green per pushed step (Phase 3 D-18 style).
- Test naming style (current tests use `snake_case` `fun agenda_flow_emits_agenda()`; stay consistent).

### Deferred Ideas (OUT OF SCOPE)
- Bugs that the new tests uncover (D-10): logged here during execution, fixed in a later feature-work phase. Known candidates: missing error handling in ViewModel `init {}` blocks, `println` instead of Timber for GraphQL errors.
- Running the live-network canary on a schedule in GitHub Actions (D-15 kept it manual-only).
- Swift/XCTest coverage of iOS ViewModels (D-22).
- iOS reusing the shared ISO parser from D-06 (Swift side unchanged this phase).
- Coverage non-regression gate in CI (D-18 chose report-only).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| TEST-01 | Known blocking fragilities fixed before extending coverage (unseeded RNG in `StoreStubs.kt`, `SimpleDateFormat` thread-safety in tested paths) | Fixture design (§Fixture Dataset), date migration surface and characterization-first order (§Date Migration), detekt-on-commonMain pitfall, `buildVenueStub` production-fallback pitfall |
| TEST-02 | `commonTest` coverage of Store/repository and GraphQL→model mappers extended into `core-data`/feature modules following the `DevFestNantesStoreContractTest` pattern | Verified MockServer + `GraphQLStore` on JVM, Android host test and iOS simulator; recorded JSON as Kotlin string constants; deprecated `parseJsonResponse` replacement; data-builder output placement; CI task additions |
| TEST-03 | ViewModel test coverage extended, enabled by constructor injection | Per-ViewModel testability table, `MainDispatcherRule`, Turbine, dispatcher binding, `FirebaseRemoteConfig` and `Resources.getSystem()` obstacles |
| TEST-04 | Compose smoke tests per feature module (`androidTest`) on `core-testing` | Library-module androidTest has no app `Application`; Koin wiring; time-dependent Agenda scroll pitfall; D8 OOM watch; dependencies missing from feature modules today |
</phase_requirements>

## Summary

The build tooling for this phase is feasible as locked. In a throwaway copy of the repo (JDK 17 via `~/.gradle/gradle.properties`), I enabled the AGP 9 KMP host test, added Kover 0.9.11, Turbine 1.2.1 and `com.apollographql.mockserver:apollo-mockserver:0.3.1`, and ran `:core:data:testAndroidHostTest :core:data:jvmTest :core:data:koverXmlReport :core:data:iosSimulatorArm64Test`. All passed: a Robolectric 4.17 test of `BookmarksStoreImpl` on the Android host test, and an Apollo `MockServer` + real `ApolloClient` + `GraphQLStore.rooms` test running on JVM, Android host and **iOS simulator**. Kover produced one merged report covering both JVM and Android-host tests (it showed `BookmarksStoreImpl` covered by the Robolectric test). So D-13/D-14/D-16/D-17/D-18/D-22 are technically sound.

Six findings change how the plan must be written. (1) `:core:data` has **no Android host test today**; Robolectric tests for `BookmarksStoreImpl` need `withHostTest { isIncludeAndroidResources = true }` in the KMP convention and a new CI task name (`testAndroidHostTest`), because `testDebugUnitTest jvmTest` does not run it. (2) `parseJsonResponse` is **deprecated** in Apollo 5.2.0; use `parseResponse(jsonReader)`. (3) Apollo's default data-builder output goes to the module's **own `commonTest`**, which `:core:data` cannot see; it must be redirected to `commonMain`. (4) `commonTest` has no portable resource loading (iOS), so "recorded JSON as test resources" must be Kotlin string constants. (5) `FeedbackFormViewModel` takes the **final** `FirebaseRemoteConfig`, so it cannot be faked without a small seam, and `VenueViewModel` calls `Resources.getSystem()`, so it needs Robolectric. (6) The `Agenda` screen auto-scrolls by wall-clock time against hard-coded 2025 dates, so a Compose smoke test of it is time-dependent.

**Primary recommendation:** Sequence as (0) Kover wiring + baseline at the unmodified pre-phase HEAD with the live canary excluded, (1) characterization tests then fixture + date migration (TEST-01), (2) harness (Turbine, `MainDispatcherRule`, `FakeDevFestNantesStore`, host-test enablement, CI task widening), (3) store/mapper tests, (4) ViewModel tests, (5) Compose androidTests, (6) final Kover report. Use the verified commands below as the per-task automated checks.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Fixed fixture dataset | `:core:model` commonMain (`stubs/`) | `:core:testing`, `:core:data` Mocked, feature previews | D-02: single source with no new module edges |
| ISO-8601 parsing to `Instant?` | `:core:model` commonMain | `:core:ui` (display), `:feature:*` callers | D-06: shared and testable on JVM and iOS |
| Localized date display | `:core:ui` (Android) | `:feature:agenda`, `:feature:session-detail` | kotlinx-datetime has no localized formatting |
| GraphQL → model mappers | `:core:data` commonMain (`internal`) | `:core:network` (generated types) | Must stay `internal` (iOS header leak), tested from the same module |
| Store contract tests | `:core:data` commonTest | `:core:testing` (fakes) | Runs on JVM, Android host, iOS sim |
| SharedPreferences persistence tests | `:core:data` androidHostTest, `:feature:agenda` / `:feature:settings` `src/test` | — | Robolectric needs an Android host test environment |
| ViewModel tests | feature module `src/test` (JVM) | `:core:testing` (fakes, `MainDispatcherRule`) | D-12; fakes live in `:core:testing` unless feature-owned interfaces |
| Compose smoke tests | feature module `src/androidTest` | `:core:testing` (`androidTestDoubles`) | D-19/D-21 |
| Coverage measurement | `build-logic` convention + root aggregation | CI (report only) | D-18 |

## Standard Stack

### Core (new, test-only)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kover Gradle plugin `org.jetbrains.kotlinx.kover` | 0.9.11 | Coverage report, no threshold | JetBrains' official KMP coverage tool. Verified end to end with AGP 9.4 + `com.android.kotlin.multiplatform.library` |
| `app.cash.turbine:turbine` | 1.2.1 | Flow emission assertions | Official, KMP, current release |
| `com.apollographql.mockserver:apollo-mockserver` | 0.3.1 | In-process HTTP server for `ApolloClient` | Apollo's own multiplatform mock server; JVM + iOS variants published |
| Robolectric | 4.17 (already in catalog) | Real `SharedPreferences`, `Resources.getSystem()` | Phase 4 base (`KoinStartupTest`) |

### Already present and reused
`kotlin-test` + `kotlinx-coroutines-test` (added to every KMP `commonTest` by `KmpLibraryConventionPlugin`), JUnit 4.13.2, `koin-test` + `koin-test-junit4`, `androidx-test-core`, `androidx-compose-ui-test-junit4` (in the `test` and `android-test` bundles), `androidx-compose-ui-test-manifest` (in the `debug` bundle). Apollo 5.2.0 runtime/gradle plugin, kotlinx-datetime 0.8.0.

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Apollo MockServer | OkHttp MockWebServer | JVM only, would not run on iOS. Rejected by D-14 |
| Kover | JaCoCo | No KMP/iOS story. Not considered |
| Data builders | Hand-built generated classes / trimmed JSON variants | Simpler, no codegen change. Use as fallback if the builder spike fails (see Open Question 2) |

**Installation (catalog additions):**
```toml
# [versions]
kover = "0.9.11"
turbine = "1.2.1"
apolloMockserver = "0.3.1"
# [libraries]  (NOT after [bundles] - a table there fails with "Unexpected type for bundle")
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
apollo-mockserver = { group = "com.apollographql.mockserver", name = "apollo-mockserver", version.ref = "apolloMockserver" }
# [plugins]
kover = { id = "org.jetbrains.kotlinx.kover", version.ref = "kover" }
```

**Version verification** `[VERIFIED: repo1.maven.org maven-metadata.xml, 2026-10-15]`: Kover plugin `0.9.11` (lastUpdated 2026-09-29), Turbine `1.2.1` (2025-06-11), apollo-mockserver `0.3.1` (2025-11-17), Robolectric `4.17` (2026-09-10), Apollo `5.2.0` and normalized-cache `1.0.9` (project is on `1.0.8`; no need to bump). Re-verify at plan time per the Phase 2 newest-verified policy.

## Package Legitimacy Audit

The `package-legitimacy` seam only supports npm/pypi/crates; Maven coordinates cannot be checked with it. Each coordinate was instead confirmed against an authoritative source this session.

| Package | Registry | Age | Source Repo | Verdict | Disposition |
|---------|----------|-----|-------------|---------|-------------|
| `org.jetbrains.kotlinx.kover` 0.9.11 | Maven Central / Plugin Portal | multi-year | github.com/Kotlin/kotlinx-kover (README read) | OK (manual) | Approved |
| `app.cash.turbine:turbine` 1.2.1 | Maven Central | multi-year | github.com/cashapp/turbine (README read: `app.cash.turbine:turbine:1.2.1`) | OK (manual) | Approved |
| `com.apollographql.mockserver:apollo-mockserver` 0.3.1 | Maven Central | ~1 yr | github.com/apollographql/apollo-kotlin-mockserver (pom scm; Apollo docs name the same group/artifact) | OK (manual) | Approved |

**Packages removed ([SLOP]):** none. **Packages flagged ([SUS]):** none. No `postinstall` concept applies (Gradle). The Apollo docs page shows `apollo-mockserver:0.0.1`; the published artifact line is 0.3.1, which is what compiled and ran. `[VERIFIED: experiment]`

## Architecture Patterns

### System Architecture Diagram

```
                         D-02 fixed dataset (:core:model commonMain, stubs/)
                                      |
        +-----------------------------+------------------------------+
        v                             v                              v
DevFestNantesStoreMocked    FakeDevFestNantesStore            Compose @Preview
(:core:data, mock mode)     (:core:testing, MutableStateFlow) (buildXStub() wrappers)
        |                             |
        |   contract tests (commonTest, JVM + Android host + iOS sim)
        v                             v
  Abstract store contract <--- GraphQLStore + ApolloClient(MemoryCache)
                                      ^
                                      | HTTP (loopback)
                              Apollo MockServer  <-- recorded JSON (Kotlin string consts)
                                      |
                       parseResponse(jsonReader) -> internal Mappers.kt -> domain models

ViewModel (JVM unit test, src/test)
  <- FakeDevFestNantesStore / FakeBookmarksStore / NoOpPerformanceMonitoring / fake services
  <- MainDispatcherRule (Dispatchers.setMain) + injected CoroutineDispatcher (Agenda)
  -> Turbine asserts uiState / days sequences

Compose smoke test (feature src/androidTest, emulator API 34)
  KoinTestRule { modules(featureModule + coreUiModule + androidTestDoubles + test overrides) }
  -> real *Route -> koinViewModel() -> fakes with fixture data -> assert fixture title + 1 click

Kover: each module applies plugin; root aggregates -> koverXmlReport (JVM + Android host tests only)
```

### Recommended structure (additions)
```
core/model/src/commonMain/.../stubs/        # fixed fixtures (replace Random), keep buildXStub() as thin deterministic wrappers
core/model/src/commonMain/.../utils/        # IsoDate.kt: String.toInstantOrNull(), Instant.toLocalDate(tz)
core/model/src/commonTest/...               # IsoDateParsingTest, fixture sanity tests
core/data/src/commonTest/...                # abstract StoreContractTest + Mocked/GraphQL subclasses, MapperTest, RecordedResponses.kt
core/data/src/androidHostTest/...           # BookmarksStoreImplTest (Robolectric)
core/testing/src/commonMain/...             # FakeDevFestNantesStore
core/testing/src/androidMain/...            # MainDispatcherRule (JUnit4, JVM/Android only)
feature/*/src/test/...                      # ViewModel tests (+ Robolectric where noted)
feature/*/src/androidTest/...               # one Compose smoke class per feature
```

### Pattern 1: Android host test in the KMP convention (verified)
`KmpLibraryConventionPlugin` already configures `KotlinMultiplatformAndroidLibraryTarget`; adding one line compiled and ran `[VERIFIED: experiment, core/data]`:
```kotlin
// inside targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach { ... }
withHostTest { isIncludeAndroidResources = true }
```
Source set is `androidHostTest` (`src/androidHostTest/kotlin`), task is `:core:data:testAndroidHostTest`. Declare deps with `getByName("androidHostTest").dependencies { implementation(libs.robolectric); implementation(libs.junit); implementation(libs.androidx.test.core) }`. Side effect: `commonTest` now also runs on the Android host (3 runtimes: jvm, androidHost, iosSimulatorArm64). `[CITED: developer.android.com/kotlin/multiplatform/plugin]` for the DSL and source-set names.

### Pattern 2: Mapper tests from recorded JSON (verified API)
`parseJsonResponse` is deprecated (`@Deprecated("Use parseResponse or jsonReader.toApolloResponse() instead")`). Use:
```kotlin
// Source: apollo-api 5.2.0 sources, Operations.kt (read this session)
import com.apollographql.apollo.api.json.jsonReader
import com.apollographql.apollo.api.parseResponse
import okio.Buffer

internal fun <D : Operation.Data> Operation<D>.parseRecorded(json: String): D =
    parseResponse(Buffer().writeUtf8(json).jsonReader()).dataOrThrow()
// usage: GetSessionsQuery().parseRecorded(RecordedResponses.SESSIONS).sessions.nodes.map { it.sessionDetails.toSession() }
```
`okio` is an `api` dependency of `apollo-api`, so it is visible from `:core:data` tests. `[VERIFIED: apollo-api-5.2.0.module metadataApiElements]`. Fragment mappers (`SessionDetails.toSession()`, `SpeakerDetails.toSpeaker()`, `RoomDetails.toRoom()`) are reached through the query data.

### Pattern 3: GraphQLStore on MockServer (verified for the simple case)
```kotlin
// Source: experiment, passed on jvm + androidHost + iosSimulatorArm64
val server = MockServer()
server.enqueueString("""{"data":{"rooms":[{"__typename":"Room","id":"Titan","name":"Titan"}]}}""")
val client = ApolloClient.Builder().serverUrl(server.url()).build()
val rooms = GraphQLStore(client).rooms.first()
```
For D-14 add `.normalizedCache(MemoryCacheFactory(), cacheKeyGenerator, cacheResolver)` using `com.apollographql.cache.normalized.memory.MemoryCacheFactory` (import path from STATE.md Phase 02) and the public `cacheKeyGenerator`/`cacheResolver` vals of `:core:network` `Apollo.kt`. `server.url()` is a suspend function in 0.3.x (it compiled inside `runTest`); `enqueue(MockResponse...)` for the error case was not exercised, so verify the `MockResponse` builder shape in the first task. `[ASSUMED]`

### Pattern 4: ViewModel test skeleton
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AgendaViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()   // new, :core:testing androidMain
    @Test fun filters_intersect_across_types() = runTest {
        val vm = AgendaViewModel(FakeDevFestNantesStore(), FakeBookmarksStore(), NoOpPerformanceMonitoring(),
            FakeSessionFiltersService(), ioDispatcher = mainDispatcher.dispatcher)
        vm.days.test { /* skipItems / awaitItem, assert on fixture ids */ cancelAndIgnoreRemainingEvents() }
    }
}
```
Turbine: call `test {}` before pushing values into shared flows; unconsumed events fail the test unless `cancelAndIgnoreRemainingEvents()`. `[CITED: github.com/cashapp/turbine]`. Turbine depends on `UnconfinedTestDispatcher` semantics, which the coroutines-test docs flag as unstable, so keep the test dispatcher choice in one rule.

### Anti-Patterns to Avoid
- **Resource files in `commonTest`:** no portable resource loading on Kotlin/Native. Use Kotlin string constants (mind the 65535-byte JVM constant limit: use `trimIndent()` raw strings in a `val`, or split per query).
- **Reading the live network in the default run:** `GraphQLStoreJvmTest` hits `confetti-app.dev` (and uses conference `devfestnantes2024`, while production uses `devfestnantes2025`). It also feeds the Kover numbers (it covers `GraphQLStore` and `Mappers` today), so a baseline taken with it is network-dependent.
- **Fakes of final SDK classes:** do not reach for a mocking framework (Out of Scope).
- **Service-locator in tests:** the DI gate scans test code too (see Pitfalls).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Flow sequence assertions | `delay`/`yield` polling, manual collect jobs | Turbine `test {}` | Timeouts, unconsumed-event detection |
| HTTP stubbing for Apollo | A custom Ktor/okhttp interceptor fake | Apollo MockServer | Real HTTP path, multiplatform, queue ordering |
| Android `SharedPreferences` | A hand-written in-memory `SharedPreferences` | Robolectric's real one | D-17 requires pinning real persisted keys/format |
| Coverage aggregation | Parsing JaCoCo/XML by hand | `koverXmlReport` (root) | Merges JVM + Android host |
| ISO-8601 offset/`Z` parsing | A regex/`SimpleDateFormat` replacement | `kotlin.time.Instant.parse` (already accepts both, see `ScheduleSlotDateParsingTest.kt:47-57`) | Already used by `ScheduleSlot` |
| Local date for the OpenFeedback URL | `String.format("%d-%02d-%02d")` | `LocalDate.toString()` | D-05 |

**Key insight:** every hard part (HTTP, prefs, flows, coverage, dates) has an official, already-multiplatform tool; the work is wiring and deterministic data, not new infrastructure.

## Common Pitfalls

### Pitfall 1: `testDebugUnitTest jvmTest` silently skips Android host tests
**What goes wrong:** Robolectric tests added under `androidHostTest` pass locally but never run in CI.
**Why:** `.github/workflows/android.yml` unit job runs only `testDebugUnitTest jvmTest`. The KMP task is `testAndroidHostTest`.
**How to avoid:** change the CI line to `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`; keep per-step CI green (D-18 of Phase 3).
**Warning signs:** a new `*HostTest` directory with no corresponding CI task.

### Pitfall 2: Data builders land where `:core:data` cannot see them
**What goes wrong:** `generateDataBuilders.set(true)` generates into `:core:network`'s `commonTest` (the plugin code uses `connectToKotlinSourceSet("commonTest")` for KMP, although the KDoc says commonMain). `[VERIFIED: apollo-gradle-plugin 5.2.0 sources, DefaultApolloExtension.kt defaultDataBuildersOutputDirAction]`.
**How to avoid:** `dataBuildersOutputDirConnection { connectToKotlinSourceSet("commonMain") }` (API exists: `Service.dataBuildersOutputDirConnection(Action<DirectoryConnection>)`). The builders then compile into the production klib of `:core:network` (not exported to Swift, so no header leak, but run `swift-names-gate.sh` anyway). The feature is `@ApolloExperimental`. `[ASSUMED]` that this combination works; make it the first spike. Fallback: build edge cases from trimmed JSON variants, which needs no codegen change.

### Pitfall 3: Detekt scans fixtures in `commonMain`
**What goes wrong:** the new fixture file in `:core:model` commonMain is analysed (`configureKmpDetektSources()` restricts to `src/commonMain|androidMain|iosMain`; the `**/test/**` excludes do not apply), so `MagicNumber`, `LongMethod` (160), `MaxLineLength`, `TooManyFunctions` can fail CI.
**How to avoid:** split fixtures by entity, use named constants, add a targeted `@file:Suppress(...)` as the repo already does (`Mappers.kt`: `@file:Suppress("TooManyFunctions")`). Run `./gradlew detekt` per commit.

### Pitfall 4: `buildVenueStub` is production code, not just mock data
`GraphQLStore.getVenue` returns `buildVenueStub(language)` on error and on null data (`GraphQLStore.kt:168,170`). Do not change its content when replacing the random stubs. Only the random builders (`buildSessionStub`, `buildSpeakerStub`, `buildPartnerStub`, `buildScheduleSlotStub`) are mock-only. Previews call these (`AgendaRow.kt:198`, `SpeakerSession.kt:123`, `SessionDetails.kt:99`, `PartnerCard.kt:78`, `SpeakerDetails.kt:97`, `SessionSpeaker.kt:93`, `VenueDetails.kt:137`), so keep `buildXStub()` as deterministic wrappers and the previews stay untouched.

### Pitfall 5: Agenda day buckets are hard-coded to 2025-10-16/17
`Agenda.DAY_ONE_ISO = "2025-10-16T00:00:00Z"`, `DAY_TWO_ISO = "2025-10-17T00:00:00Z"`; `Agenda.Builder` silently drops sessions outside those two UTC days. Fixture sessions must start on those days (the current stub's `STUB_ENDING_HOUR = 30` even overflows into day two). Local-time bucketing uses `inWholeDays == 0` on the UTC offset, so a session at `2025-10-16T23:30:00Z` still counts as day one.

### Pitfall 6: Agenda smoke test is wall-clock dependent
`AgendaColumn` runs `getCurrentStartTimeIndex` (compares `Date()` now with each group's end) and `scrollToItem(index)`; `Agenda.todayPageIndex()` uses `Clock.System`. With fixtures in October 2025 and a current date after that, every group is "past" and the list scrolls to the end, and the initial pager page is 0. Assert on content that is deterministic under that behavior (e.g. the last time group, or scroll explicitly with `performScrollToNode`), or introduce an injectable clock as part of the `Date()` → `Instant` migration (D-05). Do not assert the first row blindly.

### Pitfall 7: `FeedbackFormViewModel` cannot be faked
It takes `FirebaseRemoteConfig` (final, `fetchAndActivate().addOnCompleteListener` in `init`). `KoinStartupTest.kt` already notes "takes the final FirebaseRemoteConfig". Options: (a) behavior-neutral seam (a small `interface FeedbackFlags { getBoolean(key); fetchAndActivate(onComplete) }` adapter bound in `sessionDetailModule`), (b) Robolectric with a dummy `FirebaseApp` as `KoinStartupTest` does, asserting only the config-derived flags. Recommend (a) for the unit test of the flag logic plus the existing (b) startup coverage; this is a refactor beyond D-09, so confirm with the user (Open Question 1).

### Pitfall 8: `VenueViewModel` and `getDateFromIso8601` call `Resources.getSystem()`
Under `isReturnDefaultValues = true` this yields null and an NPE in a plain JVM test. Use Robolectric for `VenueViewModel` (and set `@Config(qualifiers = "fr")` / `"en"` to check `toContentLanguage()`). Also `VenueViewModel.venue` is a getter that builds a new `stateIn` on every access (candidate for the D-10 deferred list).

### Pitfall 9: DI gate and Hilt-word gate apply to test code
`scripts/di-gate.sh` scans every tracked `*.kt` (no test exclusion). Tests must not use `KoinComponent`, `getKoin(`, `GlobalContext`, `KoinPlatform`, `koinInject`; `KoinTestRule`, `startKoin`, `stopKoin`, `loadKoinModules`, `koinApplication` are fine. The Hilt rule is a case-insensitive substring match on `hilt`/`dagger`/`javax.inject` plus annotation names such as `@Module`, `@Provides`, `@Singleton`, `@Inject`; avoid those words in test names and comments. Run `bash scripts/di-gate.sh` after `git add` (it reads tracked files only).

### Pitfall 10: Library-module androidTest has no app `Application`; deps are missing
Feature modules' androidTest APKs run against AGP's generated test app, so there is no `DevFestNantesApplication` and no conflict with `initKoin()`; start Koin per test with `KoinTestRule` (koin-test-junit4). But feature modules currently declare **no** `androidTestImplementation` deps (only `:core:ui` has `test.ext.junit` and `test.runner`); `AndroidFeatureConventionPlugin` only adds `testImplementation(project(":core:testing"))`. Add `androidTestImplementation` for `:core:testing`, the compose ui-test-junit4, the runner/ext-junit and koin-test-junit4, in the convention plugin.

### Pitfall 11: Feature-owned interfaces cannot have fakes in `:core:testing`
`SessionFiltersService` lives in `:feature:agenda` and `DataCollectionSettingsService` in `:feature:settings`, and core modules never depend on features (ARCH-02). Their fakes belong to the feature. `src/test` and `src/androidTest` do not share sources. Recommended: unit tests use a fake in `src/test`; the androidTest uses the real `SessionFiltersServiceImpl` against a private prefs file cleared in `@Before`, and a small androidTest-local `DataCollectionSettingsService` fake (the fake is ~15 lines, `KoinStartupTest` already contains one to copy). Alternative: one shared `src/sharedTest` dir wired into both source sets (unverified under AGP 9).

### Pitfall 12: Koin `verify()` tests must change with the dispatcher param
`AgendaModuleTest` lists `extraTypes` for upstream bindings; a new `CoroutineDispatcher` constructor parameter must be bound (named) and added to `extraTypes` or `verify()` fails. `AndroidGraphVerifyTest` already whitelists `CoroutineDispatcher::class` only for `appModule`, so bind the Agenda dispatcher in a core module included by `sharedModules`/platform modules (recommend `coreUiModule` in `:core:ui`, which every feature depends on), keep `KoinStartupTest`/`ProductionGraphStartupTest` green.

### Pitfall 13: Kover scope
Kover merges JVM and Android host tests only; `androidTest` (instrumented Compose smoke tests) do **not** count `[CITED: github.com/Kotlin/kotlinx-kover README via search summary]`. Exclude generated/noise from the measurement so the before/after is meaningful: Apollo generated `com.gdgnantes.devfest.core.network.graphql.*`, `R`, `BuildConfig`, `ComposableSingletons*`, `@Preview` functions. Kover with the pure `com.android.library` / application modules was not run here (`[ASSUMED]` to work, README lists AGP >= 7.0); make "Kover applied to all 13 modules and one root report generated" the first plan's acceptance criterion.

### Pitfall 14: Gradle daemon heap and CI time
`connectedDebugAndroidTest` with up to six feature androidTest APKs previously triggered the D8 OOM at 2 GB (now 4 GB, `gradle.properties`). Per D-19, report it if it returns. The iOS job (30 min timeout) will also gain `:core:model`, `:core:data`, `:core:testing` `iosSimulatorArm64Test` (MockServer + Apollo on K/N ran locally in under a minute).

## Code Examples

### Common ISO parser (D-06), shape only
```kotlin
// :core:model commonMain. Instant.parse accepts "Z" and "+02:00" forms (ScheduleSlotDateParsingTest.kt:47-57 pins both).
fun String.toInstantOrNull(): Instant? = try { Instant.parse(this) } catch (_: IllegalArgumentException) { null }

fun Instant.toLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    toLocalDateTime(timeZone).date   // tz is a parameter so commonTest can pass UTC / Europe/Paris
// FallbackFeedbackForm: "${WebLinks.OPENFEEDBACK_BASE_URL.url}/${start.toLocalDate()}/$formId"
```
Behavior differences to pin in characterization tests (D-07): the old parser tried `yyyy-MM-dd'T'HH:mm:ssXXX` then `'Z'`-literal UTC and returned null otherwise; `Instant.parse` additionally accepts fractional seconds, and rejects offset-less strings like the old one. `[ASSUMED]` for fractional-seconds acceptance by SimpleDateFormat vs Instant; the characterization test settles it before the swap. `kotlin.time.Instant.toLocalDateTime` is already used in `Agenda.kt` (`startOfDay.toLocalDateTime(TimeZone.currentSystemDefault()).date`). `[VERIFIED: Agenda.kt read]`. `LocalDate.toString()` producing ASCII ISO is documented kotlinx-datetime behavior `[ASSUMED]`; add a `commonTest` assertion.

### Dispatcher binding (D-09)
```kotlin
// :core:ui CoreUiModule (suggested home). Qualifier constant is a plain String const to keep the DI gate quiet.
const val IO_DISPATCHER = "io"
single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
// :feature:agenda AgendaModule
viewModel { AgendaViewModel(get(), get(), get(), get(), get(named(IO_DISPATCHER))) }
```
`AgendaViewModel` currently uses `viewModelOf(::AgendaViewModel)`; with a qualified parameter switch to the explicit lambda like `SpeakerViewModel` already does.

### Minimal Compose smoke shape (D-20/D-21)
```kotlin
@RunWith(AndroidJUnit4::class)
class SpeakersSmokeTest {
    @get:Rule(order = 0) val koin = KoinTestRule.create { androidContext(ApplicationProvider.getApplicationContext()); allowOverride(true)
        modules(speakersModule + coreUiModule + androidTestDoubles) }
    @get:Rule(order = 1) val compose = createComposeRule()
    @Test fun renders_fixture_speaker_and_click_fires_callback() { /* setContent { SpeakersRoute(onSpeakerClick = ...) } ; onNodeWithText(Fixtures.speakerShared.name) ... */ }
}
```
`androidTestDoubles` (`testDoublesModule` + `androidTestDoublesModule`) exists in `:core:testing` androidMain. `[VERIFIED: AndroidTestDoublesModule.kt read]`. `KoinTestRule.create` signature `[ASSUMED]` (koin-test-junit4 4.2.2); `Speakers`/screens read ViewModels with `koinViewModel()` so a `ViewModelStoreOwner` is provided by the compose rule's `ComponentActivity`.

## Fixture Dataset (D-01..D-04)

Edge cases derived from `AgendaViewModel.filterSessions` (`FilterType` BOOKMARK, COMPLEXITY, LANGUAGE, ROOM, TYPE: OR within a type, intersection across types, result built from a `HashSet<Session>`) and `Mappers.kt` nullability:
- Sessions on **day 1 and day 2** (UTC days above), at least two sharing a start time (grouping), one with the **latest** start so sorting by `startDate` string is observable.
- **Every room** in `roomStubs` (7 rooms, ids `jules_verne`, `titan`, `belem`, `tour_de_bretagne`, `les_machines`, `Hangar`, `l_atelier`) used at least once; **one session with `room = null`**.
- `language`: FRENCH, ENGLISH and **null**; `complexity`: BEGINNER, INTERMEDIATE, ADVANCED and **null**; `type`: each of the 7 `SessionType` values used plus **null**; categories mixed and **null**.
- A filter combination that selects exactly one named session (specification-style tests: "FR + ADVANCED returns only X"), and one combination that yields an empty result (type present but nothing matches).
- Sessions must be **pairwise unequal** (the filter builds `HashSet<Session>`; equal data classes collapse).
- A **speaker shared by two sessions**, a **session with several speakers**, a speaker with `socials` of each `SocialType`, one with null `company`/`bio`/`photoUrl`.
- `openFeedbackFormId` non-empty on most, **null on one** (the fallback URL returns null when null).
- Partners across all six `PartnerCategory` values (the old mock only had PLATINIUM/GOLD/VIRTUAL); `PartnersViewModel` maps PXL, PLATINIUM, GOLD, VIRTUAL, VELOTYPE (PARTNERS is not exposed, a characterization point).
- `Mocked.getSpeakerSessions` currently returns `emptyList()`; implement it from the fixture (D-04 says it becomes testable). Mock mode is never shipped, but iOS mock builds are affected only by data content.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `parseJsonResponse(jsonReader)` | `Operation.parseResponse(jsonReader)` / `JsonReader.toApolloResponse(op)` | Apollo 5.x | D-13's wording is stale; use the non-deprecated call |
| `androidUnitTest` / `src/test` in KMP Android | `androidHostTest` via `withHostTest {}` (opt-in) | AGP 9 KMP library plugin | New CI task `testAndroidHostTest` |
| `com.apollographql.apollo:apollo-mockserver` (4.x line, last 4.0.1) | `com.apollographql.mockserver:apollo-mockserver` 0.3.x | Apollo 5 | New group id; do not use the old one |
| Kover < 0.9.4 | 0.9.4+ supports AGP 9; 0.9.8 fixes Android KMP library with project isolation | 2025-26 | 0.9.11 is the one verified |

**Deprecated/outdated:** `kotlinx.datetime.Instant` typealias (still compiles; see STATE.md), `Clock.System` must come from `kotlin.time.Clock`.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `dataBuildersOutputDirConnection { connectToKotlinSourceSet("commonMain") }` produces builders usable from `:core:data` tests | Pitfall 2 | Fall back to JSON variants; D-13's "data builders" half degrades |
| A2 | MockServer `MockResponse` builder API for HTTP 500 / network failure in 0.3.1 | Pattern 3 | Small API adjustment in the CacheAndNetwork-after-failure test |
| A3 | Kover works on the pure `com.android.library` / application modules with the same plugin id | Pitfall 13 | Baseline plan needs per-module workaround |
| A4 | `KoinTestRule.create { }` signature in koin-test-junit4 4.2.2 | Code Examples | Use `startKoin`/`stopKoin` in `@Before/@After` |
| A5 | `SimpleDateFormat` rejects fractional seconds while `Instant.parse` accepts them (superset) | Code Examples | Characterization test settles it; behavior is benign either way |
| A6 | Excluding the canary via a test filter keyed on a `-P` property works for `KotlinJvmTest` | Open Question 3 | Use a separate source set/task |
| A7 | Kover exclusion class patterns (`annotatedBy` Preview, generated packages) work as listed | Pitfall 13 | Noisy baseline; adjust filters |
| A8 | `LocalDate.toString()` is always ASCII `yyyy-MM-dd` | Code Examples | Add commonTest assertion (cheap) |

## Open Questions

1. **`FeedbackFormViewModel` seam (D-12 vs D-09 scope)**
   - Known: depends on final `FirebaseRemoteConfig`; real instance only works with a dummy `FirebaseApp` under Robolectric.
   - Unclear: whether the user accepts a tiny adapter interface in `:feature:session-detail`.
   - Recommendation: add the seam (behavior-neutral, same pattern as Phase 4's `PerformanceMonitoring`), bind the Firebase-backed adapter in `sessionDetailModule`, keep the Robolectric startup test. Confirm at plan time.
2. **Data builders placement** - see Pitfall 2. Recommendation: one spike task early in the store/mapper plan; fall back to JSON variants and note the deviation.
3. **Live canary exclusion mechanism (D-15)** - Recommend renaming to `GraphQLStoreLiveNetworkTest` and, in `:core:data`, configuring the `jvmTest` task with `filter.excludeTestsMatching("*LiveNetwork*")` unless `-PliveNetworkTests` is passed; document the one-line command in the test KDoc. Also use the correct conference header, or keep `devfestnantes2024` as is (it is a canary, not production).
4. **Baseline definition** - CONTEXT says TEST-01 lands first and the baseline precedes the coverage additions. Recommended reading: Kover wiring is infrastructure, so land it first and record the baseline on the unmodified pre-phase code with the canary excluded (a deterministic "before"), then TEST-01. If the user prefers the baseline after TEST-01, the report will already include the characterization tests; say so in VERIFICATION either way.
5. **Contract equivalence (D-16)** - Hand-writing recorded JSON that mirrors the fixture dataset duplicates data and can drift. Recommended: the abstract contract asserts structural invariants plus `unknownId -> null`; value assertions live in `MockedStoreContractTest` (fixture constants) and `GraphQLStoreContractTest` (constants of the recorded JSON). Add one test that the Mocked store's ids equal `Fixtures` ids.
6. **Where the shared `Fake*` for feature-owned interfaces live** - see Pitfall 11.
7. **`testAndroidHostTest` scope** - enabling it in the convention enables it for all five KMP modules and re-runs their `commonTest` on a third runtime. Recommend a convention switch (e.g. an extension property) so only `:core:data` (and `:core:model` if it gains Android-only tests) opt in.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK 17 | Gradle (pinned via `org.gradle.java.home` in `~/.gradle/gradle.properties`) | ✓ | 17 (system `java` is 25, Gradle uses 17) | CI uses Zulu 17 |
| Gradle wrapper | all builds | ✓ | 9.7.1 | — |
| Network (Maven Central, Plugin Portal) | new coordinates, Robolectric android-all jars | ✓ | — | `--offline` fails for new deps |
| Xcode / iOS simulator | `iosSimulatorArm64Test` | ✓ (ran locally) | — | CI macOS runner |
| Android emulator / `adb` / `android` CLI | `connectedDebugAndroidTest`, `android run` | ✓ binaries on PATH (no AVD booted/checked) | — | CI emulator API 34 |
| `gsd-tools` package-legitimacy for Maven | audit | ✗ (npm/pypi/crates only) | — | Manual verification (done above) |

**Missing with no fallback:** none.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Frameworks | `kotlin.test` + JUnit 4.13.2, kotlinx-coroutines-test 1.11.0, Turbine 1.2.1, Robolectric 4.17, Compose ui-test-junit4 (BOM 2026.09.00), Apollo MockServer 0.3.1 |
| Config | Convention plugins in `build-logic/convention`; no separate test config files |
| Quick run (per module) | `./gradlew :core:model:jvmTest :core:data:jvmTest` |
| ViewModel run | `./gradlew :feature:agenda:testDebugUnitTest` (per feature) |
| Host/Robolectric (KMP) | `./gradlew :core:data:testAndroidHostTest` |
| iOS | `./gradlew :core:model:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test :core:testing:iosSimulatorArm64Test :shared:iosSimulatorArm64Test` |
| Full CI-equivalent | `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest && bash scripts/di-gate.sh` |
| Instrumented | `./gradlew connectedDebugAndroidTest` (emulator API 34) |
| Coverage | `./gradlew koverXmlReport` (root, merged) |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| TEST-01 | No `Random` in stubs/Mocked; fixtures deterministic and unique ids | unit (common) | `./gradlew :core:model:jvmTest :core:data:jvmTest` | ❌ Wave 0 (`FixturesTest`) |
| TEST-01 | Old/new date parse equivalence, offset + `Z`, null on garbage; fallback URL ASCII date in a non-Latin locale | unit (common) + Robolectric | `./gradlew :core:model:jvmTest :feature:session-detail:testDebugUnitTest` | ❌ Wave 0 (`IsoDateParsingTest`, `FallbackUrlTest`) |
| TEST-02 | Mappers from recorded JSON + edge cases | unit (common) | `./gradlew :core:data:jvmTest :core:data:iosSimulatorArm64Test` | ❌ Wave 0 |
| TEST-02 | `GraphQLStore` CacheAndNetwork, cache hit, error after cached success | integration (MockServer) | same | ❌ Wave 0 |
| TEST-02 | Store contract on Mocked and GraphQL | contract (common) | same | ✅ partial (`DevFestNantesStoreContractTest`, weak) |
| TEST-02 | Prefs persistence (bookmarks, filters, consent) | Robolectric | `./gradlew :core:data:testAndroidHostTest :feature:agenda:testDebugUnitTest :feature:settings:testDebugUnitTest` | ❌ Wave 0 |
| TEST-03 | 10 ViewModels (Agenda filter matrix deepest) | unit (JVM) | `./gradlew testDebugUnitTest` | ❌ Wave 0 (`MainDispatcherRule`, `FakeDevFestNantesStore`) |
| TEST-04 | One smoke class per feature (render + 1 interaction) | instrumented | `./gradlew connectedDebugAndroidTest` | ❌ Wave 0 (deps + Koin wiring) |
| D-18 | Before/after Kover report | report | `./gradlew koverXmlReport` | ❌ Wave 0 |

### Sampling Rate
- **Per task commit:** the module-scoped quick command plus `./gradlew detekt` and `bash scripts/di-gate.sh`.
- **Per wave merge:** the full CI-equivalent line above, then push and require both workflows green.
- **Phase gate:** full suite green, instrumented job green, baseline and final Kover reports attached to VERIFICATION.

### Wave 0 Gaps
- [ ] Catalog entries (kover, turbine, apollo-mockserver) and convention-plugin wiring (Kover on all modules, `withHostTest` opt-in, test deps for feature modules incl. `androidTestImplementation`).
- [ ] `MainDispatcherRule` (`:core:testing` androidMain, add `junit` as `api`/`implementation` there), `FakeDevFestNantesStore`, extended `FakeAnalyticsService` if recording is needed.
- [ ] Recorded JSON constants `RecordedResponses.kt` (trim real confetti responses, strip nothing sensitive: all data is public conference data).
- [ ] CI: add `testAndroidHostTest` to `android.yml`; widen `ios.yml:131`.

## Security Domain

Applicable `security_enforcement` is enabled (ASVS level 1). This phase changes test code, build config and mock data; one production change touches input handling (date parsing).

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2/V3/V4 Auth/Session/Access | no | No auth or session surface touched |
| V5 Input Validation | yes | `toInstantOrNull()` returns null on malformed server dates instead of throwing; tests cover garbage, empty and out-of-range input |
| V6 Cryptography | no | None |
| V8/V14 Data protection and config | yes | Persisted consent/bookmark/filter keys pinned by D-17 tests; recorded JSON must contain only public conference data (no tokens, no API keys); `BuildConfig` secrets stay out of fixtures |

| Threat Pattern | STRIDE | Standard Mitigation |
|----------------|--------|---------------------|
| Test-only module/binding leaking into production | Elevation/Tampering | Keep `testDoublesModule` referenced only from test source sets (DI gate already scans); `:core:testing` stays `testImplementation`/`androidTestImplementation` |
| Supply chain via new test deps | Tampering | Three vetted first-party coordinates, pinned in the catalog; no `+` versions |
| Cleartext MockServer | Information disclosure | Loopback only in tests; production `network_security` config untouched |
| Malformed backend dates crashing UI | Denial of service | Null-returning parser; `ScheduleSlot` still throws on bad data (pin as characterization, defer fix per D-10) |

## Project Constraints (from CLAUDE.md)

- Prefer the `android` CLI for Android-specific tasks (`android run`, `android layout`, `android screen capture`, `android emulator`); `gh` over raw GitHub HTTP.
- No new user-visible behavior; fakes only (no MockK/Mockative); no screenshot tests; no coverage percentage target.
- Community Android skills are optional and machine-local (not part of the repo).
- User memory: commit and push finished artifacts promptly; rate limits cut agents off mid-run.
- Commit attribution line: `Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>`.

## Sources

### Primary (HIGH confidence)
- Repository files read this session: `StoreStubs.kt`, `DevFestNantesStoreMocked.kt`, `GraphQLStore.kt`, `Mappers.kt`, all 10 ViewModels, `SessionFiltersService.kt`, `DataCollectionSettingsService.kt`, `BookmarksStoreImpl.kt`, `DateUtils.kt`, `FallbackFeedbackForm.kt`, `AgendaColumn.kt`, `Agenda.kt`, build-logic convention plugins, `libs.versions.toml`, `android.yml`, `ios.yml`, `scripts/di-gate.sh`, `core:testing` sources, `KoinStartupTest.kt`, `ProductionGraphStartupTest.kt`.
- Executed experiment (throwaway copy under the session scratchpad): `:core:data:testAndroidHostTest :core:data:jvmTest :core:data:koverXmlReport :core:data:iosSimulatorArm64Test` BUILD SUCCESSFUL; MockServer test and Robolectric test green; Kover `report.xml` generated.
- Maven Central `maven-metadata.xml` and `.module` files for Kover, Turbine, apollo-mockserver (variants: jvm + iOS, no android variant), Robolectric, Apollo.
- Apollo 5.2.0 sources: `apollo-api` `Operations.kt` (deprecation, `parseResponse`), `apollo-gradle-plugin` `DefaultApolloExtension.kt`/`Service.kt` (data-builder default `commonTest`, `dataBuildersOutputDirConnection`).

### Secondary (MEDIUM confidence)
- developer.android.com/kotlin/multiplatform/plugin (host/device test opt-in, `androidHostTest` source set).
- github.com/Kotlin/kotlinx-kover releases and README (AGP 9 / KMP library support history, JVM + Android host tests only).
- github.com/cashapp/turbine README; apollographql.com/docs/kotlin testing pages (MockServer, data builders).

### Tertiary (LOW confidence)
- None relied on without verification; see Assumptions Log.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - versions from registry metadata and a green build of the exact combination.
- Architecture: HIGH for store/mapper/host-test wiring (executed); MEDIUM for Compose androidTest wiring (not executed on an emulator).
- Pitfalls: HIGH for items traced to source lines; MEDIUM for Kover on non-KMP modules and data-builder redirection.

**Research date:** 2026-10-15
**Valid until:** 2026-11-14 (stable tooling; re-check Kover/Apollo/Turbine versions at plan time)
