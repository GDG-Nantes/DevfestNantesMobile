---
last_mapped_commit: f6a603391f893d8f6bd0964cfcca500620b6a072
last_mapped_at: 2026-10-16
---
# Testing Patterns

**Analysis Date:** 2026-10-16

## Test Framework

**Runner:**
- JUnit 4 (`junit:junit:4.13.2`, `gradle/libs.versions.toml`), with `kotlin-test` / `kotlin-test-junit` assertions.
- Robolectric `4.17` for JVM tests needing Android framework classes, run with `@RunWith(RobolectricTestRunner::class)` (e.g. `core/ui/src/test/java/com/gdgnantes/devfest/core/ui/utils/DateUtilsCharacterizationTest.kt`, `core/data/src/androidHostTest/kotlin/com/gdgnantes/devfest/core/data/BookmarksStoreImplTest.kt`).
- Config: no standalone test config file. Test tasks are configured by the Gradle convention plugins in `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/`.

**Coroutines and Flows:**
- `kotlinx-coroutines-test` (`runTest`, `StandardTestDispatcher`).
- Turbine `1.2.1` (`app.cash.turbine:turbine`) for Flow assertions via `flow.test { awaitItem() }`.
- `MainDispatcherRule` (`core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/MainDispatcherRule.kt`) replaces `Dispatchers.Main` for ViewModel tests.

**Dependency injection in tests:**
- `koin-test` and `koin-test-junit4` for graph verification.

**Instrumented/UI:**
- Compose UI test (`androidx.compose.ui:ui-test-junit4`), Espresso (`espresso-core`, `espresso-intents`, `espresso-contrib`), AndroidX Test rules and runner (`androidx-test-runner 1.6.2`, `androidx-test-rules`).

**Assertion Library:**
- `kotlin.test` (`assertEquals`, `assertTrue`, `assertFailsWith`, `requireNotNull`). No Truth or AssertJ in active use (the `androidx-test-truth` artifact is declared only for `android-test`).

**Run Commands:**

```bash
./gradlew test                              # Run all unit tests (CONTRIBUTING.md)
./gradlew :androidApp:testDebugUnitTest     # Android app unit tests
./gradlew :shared:test                      # Shared KMP module tests (JVM)
./gradlew :feature:agenda:testDebugUnitTest # Single feature module
./gradlew :core:data:jvmTest                # KMP JVM target tests for core/data
./gradlew :feature:speakers:connectedDebugAndroidTest  # Instrumented smoke tests (device/emulator)
./gradlew <module>:koverHtmlReport          # Coverage report (Kover 0.9.11)
```

Watch mode is not configured. Use `./gradlew <module>:testDebugUnitTest --continuous` if needed.

## Test File Organization

**Location:**
- Co-located by source set, not by package layer. Unit tests in `src/test/java/...` for Android modules; in `commonTest`, `jvmTest`, `androidHostTest`, `iosTest` for KMP modules; instrumented tests in `src/androidTest/java/...`.
- Examples:
  - `feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModelTest.kt`
  - `feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersServiceImplTest.kt`
  - `core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreContractTest.kt`
  - `core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreLiveNetworkTest.kt`
  - `shared/src/jvmTest/kotlin/com/gdgnantes/devfest/shared/di/KoinGraphTest.kt`
  - `androidApp/src/androidTest/java/com/gdgnantes/devfest/android/MainActivityTest.kt`

**Naming:**
- Unit test classes: `<Subject>Test` (`SpeakersViewModelTest`, `FeedbackFormViewModelTest`).
- Contract tests: `<Interface>ContractTest` run one shared contract against several implementations (`DevFestNantesStoreContractTest`, `GraphQLStoreContractTest`, `MockedStoreContractTest`).
- Module wiring tests: `<Feature>ModuleTest` in `src/test/.../di/` (e.g. `feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/di/SettingsModuleTest.kt`).
- Smoke tests (instrumented): `<Feature>SmokeTest`.
- Characterization tests: `*CharacterizationTest` pin current behavior, including known defects (`DateUtilsCharacterizationTest`).
- Test function names are snake_case sentences with no backticks, describing behavior:
  - `loads_the_fixture_agenda_into_two_days_and_reports_success`
  - `french_filter_keeps_the_four_french_sessions`
  - `ui_state_goes_from_starting_to_success_with_the_fixture_speakers`
  - `accept_all_records_consent_and_turns_every_switch_on` (instrumented)

**Structure:**

```
feature/<name>/src/test/java/com/gdgnantes/devfest/feature/<name>/
├── <Name>ViewModelTest.kt
├── di/<Name>ModuleTest.kt
├── services/<Name>ServiceImplTest.kt
└── utils/<Helper>Test.kt
feature/<name>/src/androidTest/java/com/gdgnantes/devfest/feature/<name>/
└── <Name>SmokeTest.kt
```

## Test Structure

**Suite Organization:**

```kotlin
class SpeakersViewModelTest {
    // A standard dispatcher lets the test observe STARTING before the init block's collection runs.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val store = FakeDevFestNantesStore()

    private fun viewModel() = SpeakersViewModel(store, NoOpPerformanceMonitoring())

    @Test
    fun ui_state_goes_from_starting_to_success_with_the_fixture_speakers() =
        runTest {
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertEquals(UiState.STARTING, awaitItem())
                assertEquals(UiState.SUCCESS, awaitItem())
            }

            assertEquals(SpeakerFixtures.all, viewModel.speakers.value)
        }
}
```

Source: `feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModelTest.kt`.

**Patterns:**
- Setup: a private factory function (`private fun viewModel() = ...`) builds a fresh subject per test; state lives in private `val` fakes on the class. JUnit creates a new instance per test method, so no `@Before` is needed.
- Dispatcher: `@get:Rule val mainDispatcherRule = MainDispatcherRule()`. Pass `StandardTestDispatcher()` when the test must observe an intermediate state.
- Teardown: handled by the rule; no explicit `@After` is used.
- Assertion: `assertEquals(expected, actual)` with fixture IDs (`SessionFixtures.welcome.id`) rather than string literals.
- Test-body wrapping: multi-line tests put `runTest { ... }` on the line after `=` (ktlint expression-body style).
- Test helpers for domain data: `idsOf(vararg sessions: Session): Set<String>` and extension helpers like `AgendaViewModel.ids(day: Int)` in `AgendaViewModelTest.kt`.

## Mocking

**Framework:** None library-based. The project uses hand-written fakes. `mockk` is not referenced in any Kotlin source. Mokkery is not used.

**Fakes (preferred):**
- Live in `core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/`:
  - `FakeDevFestNantesStore.kt`: exposes `speakersState: MutableStateFlow<List<Speaker>>`, `failure: Throwable?` for error injection.
  - `FakeBookmarksStore.kt`: in-memory `Set<String>` backed by `MutableStateFlow`.
  - `FakeStores.kt`, `FakeAnalyticsService.kt`.
  - `NoOpPerformanceMonitoring` for `PerformanceMonitoring`.
- Feature-local fakes are declared in the test file: `FakeSessionFiltersService()` inside `AgendaViewModelTest.kt`.

**Pattern:**

```kotlin
private val store = FakeDevFestNantesStore()
private val bookmarks = FakeBookmarksStore()

private fun viewModel() =
    AgendaViewModel(store, bookmarks, NoOpPerformanceMonitoring(), filters, mainDispatcherRule.dispatcher)

// drive state through the fake
store.speakersState.value = listOf(SpeakerFixtures.alice)
filters.setFilters(setOf(french))
```

**Test doubles DI:**
- `core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModule.kt` and `core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/di/AndroidTestDoublesModule.kt` provide fakes in place of production bindings for instrumented/UI and graph tests.

**What to Mock:**
- Repository/store contracts (`DevFestNantesStore`, `BookmarksStore`) and services behind interfaces: use the existing fakes; extend them rather than creating parallel ones.
- Analytics and performance monitoring: use `NoOpPerformanceMonitoring` or `FakeAnalyticsService`.
- Network: use Apollo mock server in `GraphQLStoreMockServerTest.kt` rather than faking the client.

**What NOT to Mock:**
- Pure domain models and fixtures in `core/model` (use `SessionFixtures`, `SpeakerFixtures` from `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/`).
- Coroutine dispatchers: use `MainDispatcherRule` and `StandardTestDispatcher`.
- Koin graph: verify real wiring with `KoinGraphTest` / `*ModuleTest` instead of mocking the container.

## Fixtures and Factories

**Test Data:**
- Canonical fixtures live in `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/`:
  - `SessionFixtures` (named vals such as `welcome`, `composeDeepDive`, `kubernetesLab`, `all`), `SpeakerFixtures` (`alice`, `all`), `roomStubs`, `buildVenueStub(...)`.
  - Fixture integrity is itself tested in `core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/stubs/FixturesTest.kt`.
- Expected values in tests reference fixture constants by name, never by re-typed literals (documented in the `AgendaViewModelTest` KDoc).

**Location:**
- Model fixtures: `core/model/src/commonMain/.../stubs/`.
- Store fakes: `core/testing/src/commonMain/.../`.
- Feature-specific data is built inline in the test file.

## Coverage

**Requirements:** None enforced by a threshold in the build. Kover is applied for reporting (`org.jetbrains.kotlinx.kover` 0.9.11 via `AndroidApplicationConventionPlugin.kt` and `KmpLibraryConventionPlugin.kt`).

**View Coverage:**

```bash
./gradlew koverHtmlReport
./gradlew :feature:agenda:koverHtmlReport
```

## Test Types

**Unit Tests:**
- ViewModels: state transitions via Turbine plus direct `.value` reads (`feature/*/src/test/.../*ViewModelTest.kt`).
- Services and utils: pure logic (`SessionFiltersServiceImplTest`, `ScheduleSlotFormattedRangeTest`, `IsoDateTest`).
- DI wiring: `*ModuleTest` and `KoinGraphTest` verify that every module resolves.
- Contract tests: shared test body run against fake and real store (`core/data/src/commonTest/.../DevFestNantesStoreContractTest.kt`).

**Integration Tests:**
- Store against Apollo mock server: `core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreMockServerTest.kt`.
- Android graph startup: `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/ProductionGraphStartupTest.kt`, `KoinStartupTest.kt`, `AndroidGraphVerifyTest.kt`.
- Feature-visibility wiring: `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt`.
- iOS graph: `shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt`.

**Live network tests:**
- `core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreLiveNetworkTest.kt` hits the real API. It lives in `jvmTest` rather than `commonTest` so it is not run as a shared contract.

**E2E / Instrumented Smoke Tests:**
- Present per feature: `feature/{agenda,speakers,about,settings,session-detail,venue}/src/androidTest/.../*SmokeTest.kt`, `androidApp/src/androidTest/.../MainActivityTest.kt`.
- Compose UI tests use the Compose test rule (`androidx.compose.ui:ui-test-junit4`); Espresso is also declared for Activity-level flows.
- Not run by `./gradlew test`. Requires a device or emulator.

**Compose previews:**
- Preview functions (`Preview*`) double as visual checks in Compose screens (e.g. `PreviewLegalScreen`).

## Common Patterns

**Async Testing (Flow):**

```kotlin
@Test
fun pushed_speakers_replace_the_list() =
    runTest {
        val viewModel = viewModel()

        viewModel.speakers.test {
            assertEquals(emptyList(), awaitItem())
            assertEquals(SpeakerFixtures.all, awaitItem())

            store.speakersState.value = listOf(SpeakerFixtures.alice)

            assertEquals(listOf(SpeakerFixtures.alice), awaitItem())
        }
    }
```

**Coroutine Testing (plain suspend):**

```kotlin
@Test
fun some_behavior() = runTest { /* ... */ }
```

**Error Testing:**

```kotlin
@Test
fun store_failure_is_characterized() {
    store.failure = IllegalStateException("boom")
    val thrown = assertFailsWith<IllegalStateException> { /* trigger */ }
    assertEquals("boom", thrown.message)
}
```

Source pattern: `SpeakersViewModelTest.kt`, `AgendaViewModelTest.kt` (`assertFailsWith`). Characterization tests document known defects with a comment referencing the deferred ticket rather than asserting the desired behavior.

**Known gaps:**
- No mutation testing or property-based testing configured.
- `SpeakersViewModel` does not handle store errors (characterized, not fixed).
- Several smoke tests are instrumented only and are not part of the default unit test run.

---

*Testing analysis: 2026-10-16*
