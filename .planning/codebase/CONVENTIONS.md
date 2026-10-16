---
last_mapped_commit: f6a603391f893d8f6bd0964cfcca500620b6a072
last_mapped_at: 2026-10-16
---
# Coding Conventions

**Analysis Date:** 2026-10-16

## Naming Patterns

**Files:**
- One primary declaration per file, named after it: `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt`, `.../list/Speakers.kt` (Composable screen).
- Koin module files are `<Feature>Module.kt` and expose a top-level `val <feature>Module: Module`: `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/di/SpeakersModule.kt` defines `speakersModule`.
- Test files mirror the source class name with a `Test` suffix: `SpeakersViewModelTest.kt`, `FallbackFeedbackUrlTest.kt`, `DataCollectionSettingsServiceImplTest.kt`.
- Instrumented tests use the `Smoke` suffix: `feature/settings/src/androidTest/java/com/gdgnantes/devfest/feature/settings/SettingsSmokeTest.kt`.
- Service implementations use the `Impl` suffix behind an interface: `DataCollectionSettingsServiceImpl`, `SessionFiltersServiceImpl`, `BookmarksStoreImpl`.
- Shared UI theme and composables live in `core/ui/src/main/java/com/gdgnantes/devfest/core/ui/` (`theme/`, `components/`, `components/appbars/`).

**Packages:**
- All code is under `com.gdgnantes.devfest.<layer>.<module>` (e.g. `com.gdgnantes.devfest.feature.agenda`, `com.gdgnantes.devfest.core.data`).
- Package names are lowercase, no underscores. Validated by detekt `PackageNaming` in `linters/detekt-config.yml`.

**Functions:**
- camelCase (`FunctionNaming` in `linters/detekt-config.yml`). Composables are the exception: PascalCase, annotated `@Composable`, and exempted from the naming rule (`ignoreAnnotated: ['Composable', 'Test']`).
- Composable screens are PascalCase nouns or `<Name>Screen`: `Speakers(...)` in `feature/speakers/.../list/Speakers.kt`, `DataCollectionSettingsScreen(...)`, `LegalScreen(...)`.
- Preview functions are prefixed `Preview`: `PreviewDataCollectionSettingsScreen()`, `PreviewLegalScreen()`.
- Factory helpers for ViewModels/services use `viewModel()` or `buildXxx` in tests (`buildVenueStub`).

**Variables and properties:**
- Private mutable backing state uses an underscore prefix paired with a public read-only property:
  ```kotlin
  private val _uiState = MutableStateFlow(UiState.STARTING)
  val uiState: StateFlow<UiState>
      get() = _uiState
  ```
  (`feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt`). Newer code uses `.asStateFlow()` instead of an explicit getter: `val speakers = _speakers.asStateFlow()`.
- Constructor-injected dependencies are `private val` on the class.
- Constants inside companion objects use UPPER_SNAKE_CASE (e.g. `PerformanceMonitoring.TRACE_SPEAKERS_LOAD`).

**Types:**
- Classes/interfaces PascalCase. Domain models are in `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/` (`Session`, `Speaker`, `Agenda`, `Room`).
- Store/service contracts are interfaces with the implementation suffixed `Impl` or, for the KMP graphql store, the implementation carries no suffix (`internal class GraphQLStore(...) : DevFestNantesStore` in `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStore.kt`).
- Enum constants are UPPER_SNAKE_CASE (`UiState.STARTING`, `UiState.SUCCESS`, `SessionType.CONFERENCE`).
- Generated GraphQL types come from Apollo under `com.gdgnantes.devfest.core.network.graphql` and are named `<Operation>Query` (`GetSessionsQuery`).

## Code Style

**Formatting:**
- Indentation: 4 spaces (`CONTRIBUTING.md`, "Kotlin (Shared Module)").
- Kotlin formatting follows ktlint rules via the detekt ktlint wrapper configured in `linters/detekt-config.yml` (`ktlint: active: true`, `android: false`, `autoCorrect: true`).
- Max line length 120 for argument wrapping (`ArgumentListWrapping.maxLineLength: 120`); `MaxLineLength` is enabled but excluded for test sources.
- Multi-line expression bodies use the ktlint-style wrapped form: a line break after `=` and the body indented one level:
  ```kotlin
  override val agenda: Flow<Agenda>
      get() =
          sessions.map { sessions -> ... }
  ```
- Trailing commas are not required; closing parentheses of multi-line calls sit on their own line.
- Prefer explicit imports.

**Linting:**
- Detekt 2.0.x (`dev.detekt` Gradle plugin, `detekt = "2.0.0-alpha.6"` in `gradle/libs.versions.toml`), applied by the `devfest.detekt` convention plugin (`build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DetektConventionPlugin.kt`). Also applied through `AndroidApplicationConventionPlugin.kt` and `KmpLibraryConventionPlugin.kt`.
- Config: `linters/detekt-config.yml`. Key rules:
  - `LongParameterList`: max 16 function parameters, 7 constructor parameters, default parameters ignored, data classes ignored.
  - `LongMethod`: max 160 lines.
  - `MagicNumber`: property declarations and companion-object properties are ignored.
- Detekt baselines are generated through the `DetektCreateBaselineTask` provided by the convention plugin.
- Coverage: Kover (`org.jetbrains.kotlinx.kover`, version 0.9.11) is applied to Android and KMP library modules.

## Import Organization

**Order (observed):**
1. Third-party and AndroidX (`androidx.*`, `app.cash.turbine.*`, `kotlinx.*`, `org.koin.*`).
2. Project packages (`com.gdgnantes.devfest.*`).
3. Standard Kotlin/Java (`kotlin.test.*`, `timber.log.*`) interleaved at the bottom of test files.

Imports are lexicographically sorted within each block. 

**Path Aliases:**
- None. Modules are referenced by Gradle project path (`:core:model`, `:feature:agenda`) and imported by package.

## Error Handling

**Patterns:**
- ViewModels expose state through `UiState` (`core/ui`, values include `STARTING`, `SUCCESS`, `ERROR`) and rely on the store `Flow` for data. Expected failures should map to `UiState.ERROR`. Note: `SpeakersViewModel` does not currently catch store failures (see `feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModelTest.kt`, which characterizes the gap).
- Flows from the data layer use the `Flow.catch { }` operator to absorb errors and emit a fallback value (`core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStore.kt`, `partners` property emits `emptyMap()` on `response.exception` via `mapNotNull`).
- Apollo responses are checked for `response.exception` before reading `response.data`.
- Use `requireNotNull` / `checkNotNull` for invariants in tests and fixtures (`requireNotNull(SessionFixtures.composeDeepDive.room)`).
- `runCatching` is not a dominant pattern in the data layer.

## Logging

**Framework:** Timber (`com.jakewharton.timber:timber:5.0.1`) in ViewModels and Android code; `println` in the KMP data layer.

**Patterns:**
- ViewModels log loaded state at debug level: `Timber.d("Speakers loaded: ${speakersList.size} speakers")`.
- Release trees are provided by `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/logging/TimberTreeRelease.kt`. Prefer Timber over `println` in new code.
- The KMP `GraphQLStore` currently uses `println("Apollo error: ...")`; treat this as legacy, not a pattern to copy.
- Performance tracing goes through `PerformanceMonitoring` (`core/analytics/src/commonMain/...`), not logging. Wrap data loads with `performanceMonitoring.traceDataLoading(operation = PerformanceMonitoring.TRACE_..., dataSource = "...") { ... }`.

## Comments

**When to Comment:**
- Explain non-obvious decisions and reference deferred work by ID (e.g. "deferred, D-10" in `SpeakersViewModelTest.kt`).
- KDoc on test doubles and public test helpers describing the contract they fake (`core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeBookmarksStore.kt`).
- Inline comments are used to explain Koin wiring choices (e.g. "Explicit positional definition" in `SpeakersModule.kt`).
- There are roughly one `TODO`/`FIXME` across Kotlin sources; avoid leaving new ones without a tracking reference.

**JSDoc/TSDoc (KDoc):**
- Use KDoc `/** ... */` on public classes and non-obvious functions. Single-sentence summaries are the norm.
- Backticks reference symbols: `` `SessionFixtures` ``.

## Function Design

**Size:** Keep functions well under the 160-line detekt `LongMethod` limit; ViewModel `init` blocks are typically under 20 lines.

**Parameters:**
- Constructor injection for dependencies. ViewModels take interfaces (`DevFestNantesStore`, `BookmarksStore`, `PerformanceMonitoring`) so tests can pass fakes.
- Default parameter values are common for optional UI parameters; detekt ignores them for `LongParameterList`.
- Composables take state plus lambdas (`onClick: () -> Unit`) and hoist state; screens are stateless where possible.

**Return Values:**
- Expose `Flow<T>` / `StateFlow<T>` from stores and ViewModels; never expose `MutableStateFlow`.
- Use `Map`, `List`, `Set` for collections; `Set` semantics are promised for bookmarks (`BookmarksStore.bookmarkedSessionIds: Flow<Set<String>>`).

## Module Design

**Exports:**
- Core/data implementations are `internal` (e.g. `internal class GraphQLStore`); interfaces are public and consumed by features.
- Each feature exposes a Koin module (`<feature>Module`) and a `viewModelOf(::XxxViewModel)` registration.

**Dependency injection:**
- Koin (`io.insert-koin`), not Hilt. Migration from Hilt is complete (commit `de6c891`, "Phase 4: Hilt to Koin DI Migration").
- Module definitions use `module { viewModelOf(::SpeakersViewModel) }`. Use `factoryOf`/`singleOf` for simple constructor wiring; use explicit `viewModel { ... }` / `factory { ... }` only when a parameter needs positional binding.
- Test-only bindings live in `core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModule.kt`.

**Barrel Files:**
- Not used. Import specific declarations.

**Multiplatform:**
- Shared logic goes into `commonMain` sourcesets of `core/*` and `shared` modules. Android-only implementations go in `androidMain` (e.g. `core/analytics/src/androidMain/.../FirebasePerformanceMonitoring.kt`).
- Convention plugins (`build-logic/convention`) configure module type: `KmpLibraryConventionPlugin`, `AndroidApplicationConventionPlugin`, plus the shared `DetektConventionPlugin`.

---

*Convention analysis: 2026-10-16*
