# Phase 4: Hilt to Koin DI Migration - Pattern Map

**Mapped:** 2026-10-04
**Files analyzed:** 47 (new/modified/deleted, grouped where they share one pattern)
**Analogs found:** 40 / 47 (the 7 without a codebase analog are Koin-first constructs, see "No Analog Found")

Important framing: the repo contains **no Koin code yet**. "Analog" here means the existing file that is being converted (the Hilt original) or the closest structural sibling (convention plugin, test, gate script). Koin snippets come from `04-RESEARCH.md` (spike-proven) and are marked as such. All paths below were verified as git-tracked (`git ls-files`); no gitignored mirror paths are used.

## File Classification

### Build / catalog

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `gradle/libs.versions.toml` (add `koin`, `koin-*`; later remove `hilt`, `dagger*`, `androidx-hilt-*`, `dagger-hilt` plugin) | config | n/a | itself (Hilt entries lines 15, 22, 58-59, 79-81, 125) | exact |
| `build-logic/.../AndroidKoinConventionPlugin.kt` (new, or fold into feature/library plugins) | config (convention plugin) | n/a | `build-logic/.../AndroidHiltConventionPlugin.kt` | exact |
| `build-logic/convention/build.gradle.kts` (register `devfestAndroidKoin`; later remove `devfestAndroidHilt`) | config | n/a | itself lines 38-41 | exact |
| `build-logic/.../AndroidFeatureConventionPlugin.kt` (swap `devfest.android.hilt` for koin) | config | n/a | itself line 18 | exact |
| `build-logic/.../AndroidLibraryConventionPlugin.kt`, `AndroidApplicationConventionPlugin.kt` (KDoc mentions of Hilt) | config | n/a | themselves | exact |
| `build-logic/.../KmpLibraryConventionPlugin.kt` (add `ExperimentalObjCRefinement` opt-in, optionally `koin-core` to commonMain) | config | n/a | itself line 60 | exact |
| `build-logic/.../DependencyResolutionRules.kt` (remove only `kotlin-metadata-jvm` force; keep `firebase-auth-ktx` substitution) | config | n/a | itself lines 14-24 vs 25-43 | exact |
| `core/data/build.gradle.kts`, `core/analytics/build.gradle.kts`, `core/network/build.gradle.kts`, `core/testing/build.gradle.kts`, `core/ui/build.gradle.kts`, `shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `feature/*/build.gradle.kts` (Koin deps, drop `libs.dagger.hilt.android`, re-add `:shared` to androidApp) | config | n/a | themselves | exact |

### KMP DI wiring (new)

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `shared/src/commonMain/.../shared/InitKoin.kt` | config (composition root) | startup | `androidApp/.../core/injection/AppModule.kt` (what it replaces) | role-match |
| `shared/src/iosMain/.../shared/IosDependencies.kt` | utility (Swift bridge) | request-response | none in repo; shape from RESEARCH Pattern 3 | no analog |
| `core/network/src/commonMain/.../network/di/NetworkModule.kt` | config | startup | `AppModule.store()` + `Apollo.kt` global `apolloClient` | role-match |
| `core/data/src/commonMain/.../data/di/DataModule.kt` (`dataModule` + `expect val dataPlatformModule`) | config | startup | `AppModule.store()` (lines 127-133) | exact |
| `core/data/src/{androidMain,jvmMain,iosMain}/.../data/di/DataPlatformModule.<platform>.kt` | config (expect/actual) | startup | `AppModule.sharedPreferences()` + `@Binds bookmarksStore` | role-match |
| `core/analytics/src/commonMain/.../analytics/di/AnalyticsModule.kt` + expect `analyticsPlatformModule` | config | startup | `AppModule.analyticsService/analytics/crashlytics/firebasePerformance/providePerformanceMonitoring` | role-match |
| `core/analytics/src/{androidMain,jvmMain,iosMain}/.../di/AnalyticsPlatformModule.<platform>.kt` | config (expect/actual) | startup | same as above | role-match |
| `core/analytics/src/androidMain/.../performance/PerformanceMonitoring.kt` (becomes interface + `FirebasePerformanceMonitoring`) | service | request-response | itself (lines 13-17, strip `@Singleton/@Inject`) | exact |
| `core/analytics/.../FirebaseAnalyticsService.kt`, `core/data/.../BookmarksStoreImpl.kt` (drop `@Inject`) | service | CRUD | themselves | exact |
| `core/ui/src/main/java/.../core/ui/di/CoreUiModule.kt` | config | startup | `VenueViewModel` conversion (see feature modules) | role-match |
| `core/ui/.../BookmarksViewModel.kt` (drop Hilt) | viewmodel | request-response | itself | exact |

### Feature modules (6x same pattern)

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `feature/venue/.../venue/di/VenueModule.kt` | config | startup | `AppModule` (Hilt original) + RESEARCH Pattern 1/4 | role-match |
| `feature/about/.../about/di/AboutModule.kt` | config | startup | `VenueModule` (tracer, once written) | exact |
| `feature/settings/.../settings/di/SettingsModule.kt` (+ `DataCollectionSettingsServiceImpl` binding) | config | startup | `VenueModule` + `AppModule.dataSharingSettingsService` | role-match |
| `feature/speakers/.../speakers/di/SpeakersModule.kt` (incl. param `SpeakerViewModel`) | config | startup | `VenueModule` | role-match |
| `feature/agenda/.../agenda/di/AgendaModule.kt` (+ `SessionFiltersServiceImpl` binding) | config | startup | `VenueModule` + `AppModule.filtersService` | role-match |
| `feature/session-detail/.../sessiondetail/di/SessionDetailModule.kt` (param `SessionViewModel`, `FeedbackFormViewModel`, `FirebaseRemoteConfig`) | config | startup | `VenueModule` + `AppModule.config()` | role-match |
| `feature/*/.../*ViewModel.kt` (strip `@HiltViewModel/@Inject`; Session/Speaker VMs also strip Assisted factory + companion) | viewmodel | request-response | `VenueViewModel.kt` | exact |
| `feature/*` screens using `hiltViewModel()` (Venue.kt, Partners.kt, DataCollectionAgreementDialog.kt, DataCollectionSettingsScreen.kt, Speakers.kt, Agenda.kt, AgendaRow.kt, SessionLayout.kt, FeedbackForm.kt, `androidApp/.../Home.kt`) | component | request-response | `Venue.kt` line 7/15 | exact |
| `feature/session-detail/.../SessionDetailRoute.kt`, `feature/speakers/.../SpeakersRoute.kt` (`SpeakerDetailRoute`): take `sessionId`/`speakerId` | component (route) | request-response | themselves | exact |
| `feature/*/src/test/java/.../di/*ModuleTest.kt` (7 files incl. core:ui) | test | request-response | `GraphQLStoreJvmTest.kt` (style) + RESEARCH `VenueModuleTest` | role-match |

### Android app

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `androidApp/.../androidapp/di/AppModule.kt` (replaces `core/injection/AppModule.kt`; temporary Hilt bridges first) | config | startup | `core/injection/AppModule.kt` | exact |
| `androidApp/.../DevFestNantesApplication.kt` | entry point | event-driven | itself | exact |
| `androidApp/.../MainActivity.kt` | entry point | request-response | itself | exact |
| `androidApp/.../core/DataSharingInitializer.kt`, `OpenFeedbackInitializer.kt`, `performance/PerformanceInitializer.kt`, `logging/TimberTreeDebug.kt`, `TimberTreeRelease.kt`, `services/ExternalContentService.kt` (drop `@Inject`, `@ActivityScoped`) | service | event-driven | themselves | exact |
| `androidApp/.../ui/screens/home/HomeViewModel.kt` | viewmodel | request-response | `VenueViewModel.kt` | exact |
| `androidApp/src/test/.../di/*` (full-graph `verify()`, runtime start-up test) | test | request-response | none (no `androidApp/src/test` exists) | no analog |
| DELETE `core/injection/AppScope.kt`, `utils/AssistedViewModelUtils.kt`, `AndroidHiltConventionPlugin.kt`, `shared/.../SharedFrameworkPlaceholder.kt` | n/a | n/a | n/a | n/a |

### Testing, iOS, CI

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `core/testing/src/commonMain/.../testing/FakeBookmarksStore.kt`, `FakeAnalyticsService.kt`, `TestModules.kt` | test utility | CRUD | `core/testing/.../FakeStores.kt` | role-match |
| `core/testing/src/androidMain/.../NoOpPerformanceMonitoring.kt` | test utility | request-response | `FakeStores.kt` (placement) | role-match |
| `shared/src/jvmTest/.../KoinGraphTest.kt`, `shared/src/iosTest/.../IosKoinTest.kt` | test | request-response | `core/testing/.../FakeStoresTest.kt` + RESEARCH spike | role-match |
| `iosApp/iosApp/Model/BaseViewModel.swift` | viewmodel (Swift) | request-response | itself | exact |
| `iosApp/iosApp/iOSApp.swift` | entry point (Swift) | startup | itself | exact |
| `scripts/di-gate.sh` | utility (CI gate) | batch | `.planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh` | role-match |
| `.github/workflows/android.yml`, `.github/workflows/ios.yml` | config | batch | themselves | exact |
| `README.md:39`, `.github/copilot-instructions.md:23`, `CONTRIBUTING.md`, `documentation/ARCHITECTURE.md`, `documentation/PERFORMANCE_MONITORING.md`, `androidApp/src/main/assets/licenses.html` | docs | n/a | themselves | exact |

## Pattern Assignments

### Version catalog + Koin convention plugin

**Analog:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidHiltConventionPlugin.kt` (lines 14-31). Copy the shape (class, `pluginManager.apply`, `dependencies { add(...) }`, `libs.findLibrary("...").get()`):

```kotlin
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.dagger.hilt.android")
            pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                add("implementation", libs.findLibrary("dagger-hilt-android").get())
                add("ksp", libs.findLibrary("dagger-hilt-compiler").get())
                ...
                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            }
        }
    }
}
```

Koin version, no plugin, no KSP (RESEARCH "Installation"). Suggested:

```kotlin
class AndroidKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", libs.findLibrary("koin-android").get())
                add("implementation", libs.findLibrary("koin-androidx-compose").get())
                add("testImplementation", libs.findLibrary("koin-test").get())
            }
        }
    }
}
```

Registration analog (`build-logic/convention/build.gradle.kts` lines 38-41):
```kotlin
register("devfestAndroidHilt") {
    id = "devfest.android.hilt"
    implementationClass = "com.gdgnantes.devfest.buildlogic.AndroidHiltConventionPlugin"
}
```

Catalog entries: add under `[versions]` (`koin = "4.2.2"`) and `[libraries]` in the same `name = { group = "...", name = "...", version.ref = "..." }` style as lines 79-81. Libraries: `koin-core`, `koin-android`, `koin-androidx-compose`, `koin-test`, `koin-test-junit4` (RESEARCH lines 119-129). Keep `ksp` plugin entry (line 134): `core/data` needs it for kmp-native-coroutines (RESEARCH Pitfall 8).

Key constraints for the Hilt convention swap:
- `AndroidFeatureConventionPlugin.kt` line 18 `pluginManager.apply("devfest.android.hilt")` is the single place that gives every feature Hilt. During coexistence apply **both** hilt and koin in features; the Hilt line is removed only in the final step (RESEARCH Pattern 6).
- `core/ui/build.gradle.kts` line 3 and `androidApp/build.gradle.kts` line 3 apply `id("devfest.android.hilt")` directly; these need Koin added in the same way (`core/ui` is not a feature, so the feature plugin does not cover it).
- `DependencyResolutionRules.kt`: delete lines 14-23 (the `force("org.jetbrains.kotlin:kotlin-metadata-jvm:...")`, Hilt-specific per its own comment) only in the final step; keep the `dependencySubstitution` block (lines 25-43).
- `KmpLibraryConventionPlugin.kt` line 59-61 is where to add the opt-in:
  ```kotlin
  sourceSets.configureEach {
      languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
      languageSettings.optIn("kotlin.experimental.ExperimentalObjCRefinement") // add
  }
  ```
- The KMP convention declares `jvm()`, `iosX64()`, `iosArm64()`, `iosSimulatorArm64()` (lines 46-49), so every `expect` needs actuals on **android, jvm, and ios** (RESEARCH Pitfall 6).

---

### `core/data/.../data/di/DataModule.kt` + platform actuals (config, startup)

**Analog (Hilt original):** `androidApp/.../core/injection/AppModule.kt` lines 127-133 (store) and 121-125 (SharedPreferences), 48-50 (BookmarksStore bind).

```kotlin
@AppScope
@Provides
fun store(): DevFestNantesStore {
    return DevFestNantesStoreBuilder()
        .setUseMockServer(false)
        .build()
}
```

Koin target (commonMain). `GraphQLStore` is `internal` so the store definition must live in `core:data`:
```kotlin
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
val dataModule: Module = module {
    single<DevFestNantesStore> { DevFestNantesStoreBuilder().setUseMockServer(false).build() }
}
@HiddenFromObjC expect val dataPlatformModule: Module
```
(`import kotlin.native.HiddenFromObjC`; see RESEARCH Pattern 3. Put `@OptIn(ExperimentalObjCRefinement::class)` on the `expect` too unless the convention-level opt-in is added.)

Android actual (RESEARCH Pattern 2, compiled in spike). It must keep the **same default prefs file** (`PreferenceManager.getDefaultSharedPreferences`, as `AppModule.kt` line 124):
```kotlin
actual val dataPlatformModule: Module = module {
    single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }
    single { BookmarksStoreImpl(get()) } bind BookmarksStore::class
}
```
jvm/ios actuals: `actual val dataPlatformModule: Module = module { }`.

**Concrete-primary + `bind`** is mandatory so `verify()` is not vacuous (RESEARCH Pattern 1). Do not write `single<BookmarksStore> { BookmarksStoreImpl(get()) }`.

**Source edits to the impl** (`core/data/src/androidMain/.../BookmarksStoreImpl.kt`): lines 9 and 11 become
```kotlin
// remove: import javax.inject.Inject
class BookmarksStoreImpl(private val sharedPreferences: SharedPreferences) : BookmarksStore {
```
`core/data/build.gradle.kts` lines 13-16: replace `implementation(libs.dagger.hilt.android)` in `androidMain.dependencies` with `implementation(libs.koin.android)` and add `implementation(libs.androidx.preference)`; add `implementation(libs.koin.core)` to `commonMain.dependencies` (lines 9-12).

Tests: `core/data/src/jvmTest/.../graphql/GraphQLStoreJvmTest.kt` (lines 1-30) is the existing JVM test style (`kotlin.test`, `runTest`, `@BeforeTest`).

---

### `core/analytics/.../di/AnalyticsModule.kt` + platform actuals (config, startup)

**Analog (Hilt original):** `AppModule.kt` lines 44-46 (`@Binds analyticsService`), 88-89 (`Firebase.analytics`), 102-106 (Crashlytics, Performance), 115-119 (`providePerformanceMonitoring`).

Android actual:
```kotlin
actual val analyticsPlatformModule: Module = module {
    factory { Firebase.analytics }
    factory { FirebaseCrashlytics.getInstance() }
    factory { FirebasePerformance.getInstance() }
    single { FirebaseAnalyticsService(get()) } bind AnalyticsService::class
    single { FirebasePerformanceMonitoring(get()) } bind PerformanceMonitoring::class
}
```
Scope rule (D-08, RESEARCH scope table): Firebase instances stay `factory` (unscoped `@Provides` today); `AnalyticsService`/`PerformanceMonitoring` are `single` (`@AppScope`).

**Edits to existing classes:**
- `core/analytics/src/androidMain/.../FirebaseAnalyticsService.kt` lines 7 and 10: drop `import javax.inject.Inject` and `@Inject constructor(` (becomes `constructor(` or just the primary constructor).
- `core/analytics/src/androidMain/.../performance/PerformanceMonitoring.kt` lines 6-7 and 13-17: drop `@Singleton`, `@Inject`. Per RESEARCH Pattern 9, introduce an interface with `trace(...)`/`trackDataLoad(...)` plus a `FirebasePerformanceMonitoring` implementation so `core:testing` can fake it (callers `SpeakerViewModel`, `SpeakersViewModel`, `AgendaViewModel`, `SessionViewModel` stay source-compatible; the `companion object` constants `TRACE_*`/`ATTR_*` at lines 18-28 must stay reachable as `PerformanceMonitoring.TRACE_*`, so keep them on the interface's companion).
- `core/analytics/build.gradle.kts` line 15: replace `implementation(libs.dagger.hilt.android)` with `implementation(libs.koin.android)`; add `implementation(libs.firebase.crashlytics.ktx)` and `implementation(libs.firebase.config.ktx)` only if bindings land there (RESEARCH Open Question 3: crashlytics in analytics androidMain, RemoteConfig in session-detail); add `koin-core` to commonMain.

All public `Module` vals in `core:data` and `core:analytics` need `@HiddenFromObjC` (expect and every actual) because `shared/build.gradle.kts` lines 10-12 `export()` them; re-grep `shared.h` for `Koin` after each core step (RESEARCH Pitfall 1).

---

### `shared/.../InitKoin.kt` and `shared/.../IosDependencies.kt` (composition root)

**Analog for what is replaced:** whole `AppModule.kt`. No Koin analog exists; use the spike-proven shapes (RESEARCH Pattern 3):

```kotlin
// shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/InitKoin.kt
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun initKoin(platformModules: List<Module> = emptyList(), appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(sharedModules + platformModules)
    }
```
```kotlin
// shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt
object IosDependencies {
    fun start() { initKoin() }
    fun store(): DevFestNantesStore = KoinPlatform.getKoin().get()
}
```
(`sharedModules` = network, data, dataPlatform, analytics, analyticsPlatform; `@HiddenFromObjC`.) Do not make a top-level `initKoinIos()` (K/N renames `init*` to `doInit*`).

**Build file analog:** `shared/build.gradle.kts` lines 16-22 (`sourceSets { commonMain.dependencies { api(project(...)) } }`). Add `implementation(libs.koin.core)` to commonMain, `iosTest`, and `jvmTest { implementation(libs.koin.test) }`.

**`SharedFrameworkPlaceholder.kt`** (lines 1-12, `internal const val SHARED_FRAMEWORK_PLACEHOLDER = true`): its KDoc explains it exists solely so K/N does not treat the source set as NO-SOURCE. Delete it only after `InitKoin.kt` exists in commonMain and verify `:shared:linkDebugFrameworkIosSimulatorArm64` still produces `shared.framework`. Also update the "Amendment to Phase 3 D-10" note (CONTEXT D-03).

---

### Feature module `di/XxxModule.kt` (6 modules plus `core:ui`)

**Analog (conversion source):** `feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueViewModel.kt` lines 11, 18, 20-23:

```kotlin
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VenueViewModel @Inject constructor(
    private val store: DevFestNantesStore
) : ViewModel() {
```
After: remove the two imports and both annotations, keep the plain constructor. Module (RESEARCH Pattern 1, spike-proven; `feature/venue` is the tracer):

```kotlin
package com.gdgnantes.devfest.feature.venue.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val venueModule = module {
    viewModelOf(::VenueViewModel)
}
```

Call-site conversion (`feature/venue/.../Venue.kt` lines 7 and 15):
```kotlin
import androidx.hilt.navigation.compose.hiltViewModel     // remove
import org.koin.compose.viewmodel.koinViewModel            // add (canonical import, RESEARCH)
    viewModel: VenueViewModel = hiltViewModel(),           // -> = koinViewModel(),
```
Apply the identical two-line swap to every `hiltViewModel()` site: `Partners.kt:20,28`, `DataCollectionAgreementDialog.kt:13,20`, `DataCollectionSettingsScreen.kt:35,44`, `Speakers.kt:42,55`, `Agenda.kt:16,33`, `AgendaRow.kt:27,43` (BookmarksViewModel), `SessionLayout.kt:30,63` (BookmarksViewModel), `FeedbackForm.kt:12,19`, `androidApp/.../Home.kt:21,63`.

Per-module ViewModel inventory to convert (same strip-annotations recipe): `PartnersViewModel.kt:8,14,16-17`, `DataCollectionViewModel.kt:6,8,10-11`, `SpeakersViewModel.kt:10,16,18-19`, `AgendaViewModel.kt:16,30,32-33`, `FeedbackFormViewModel.kt:5,10,12-13`, `BookmarksViewModel.kt:8,12,14-15`, `HomeViewModel.kt:6,11,13-14`.

**Service bindings in feature modules** (concrete + `bind`; strip `@Inject` from the impl):
```kotlin
// feature/agenda di/AgendaModule.kt
val agendaModule = module {
    viewModelOf(::AgendaViewModel)
    single { SessionFiltersServiceImpl(get()) } bind SessionFiltersService::class
}
// feature/settings di/SettingsModule.kt
val settingsModule = module {
    viewModelOf(::DataCollectionViewModel)
    single { DataCollectionSettingsServiceImpl(get(), get(), get(), get()) } bind DataCollectionSettingsService::class
}
```
Source edits: `SessionFiltersService.kt:11,19` (`class SessionFiltersServiceImpl @Inject constructor(private val sharedPreferences: SharedPreferences)`), `DataCollectionSettingsService.kt:12,30` (4-arg constructor: `FirebaseAnalytics`, `SharedPreferences`, `FirebaseCrashlytics`, `FirebasePerformance`). Both are `@AppScope @Binds` today, so they must be `single` (stateful; `SessionFiltersService` is also used by `HomeViewModel`).

**Build files** (`feature/venue/build.gradle.kts` lines 1-10 is the template; features get Koin from the convention plugin, so no per-module Koin lines are needed there).

**Tests:** per-module verify (RESEARCH "Feature module test", 1/1 green in spike). Place at `feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/di/VenueModuleTest.kt`:
```kotlin
class VenueModuleTest {
    @Test fun venueModuleVerifies() {
        venueModule.verify(extraTypes = listOf(DevFestNantesStore::class))
    }
}
```
`AndroidFeatureConventionPlugin` already gives features `testImplementation(project(":core:testing"))` (line 25) but not `koin-test`; the Koin convention supplies it. `configureAndroidLibraryTestDefaults()` sets `failOnNoDiscoveredTests = false`, so modules without tests do not fail.

---

### Parameterized ViewModels: `SessionViewModel`, `SpeakerViewModel` + their Routes

**Analog:** the files themselves.

`SessionViewModel.kt` today (lines 10-12, 18-22, 42-58) uses `@AssistedInject`, `@Assisted sessionId`, `@AssistedFactory interface SessionViewModelFactory`, and a `companion object { provideFactory(...) }`. Convert to a plain constructor and delete lines 42-58 plus imports 4, 10-12 (`ViewModelProvider`, `dagger.assisted.*`):

```kotlin
class SessionViewModel(
    private val store: DevFestNantesStore,
    private val performanceMonitoring: PerformanceMonitoring,
    sessionId: String
) : ViewModel() {
```
`SpeakerViewModel.kt` identical (lines 3-5, 12-14, 20-24, 63-79; keep `private val speakerId`).

Module (RESEARCH Pattern 4; lambda definition keeps constructor order `store, performanceMonitoring, id`):
```kotlin
val sessionDetailModule = module {
    viewModel { (sessionId: String) -> SessionViewModel(get(), get(), sessionId) }
    viewModelOf(::FeedbackFormViewModel)
    factory { Firebase.remoteConfig.apply { setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = 300L }) } }
}
```
`FirebaseRemoteConfig` must stay `factory` and keep the `setConfigSettingsAsync` side effect (copy from `AppModule.kt` lines 91-100, constant `REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL = 300L` at line 63).

Route conversion (D-10). Today `SessionDetailRoute.kt` lines 8-15 take `viewModel: SessionViewModel` as a required param and `SpeakerDetailRoute` (`SpeakersRoute.kt` lines 22-29) likewise. Target:
```kotlin
@Composable
fun SessionDetailRoute(
    modifier: Modifier = Modifier,
    sessionId: String,
    viewModel: SessionViewModel = koinViewModel { parametersOf(sessionId) },
    onBackClick: () -> Unit,
    onSocialLinkClick: (SocialItem, Speaker) -> Unit,
    onFeedbackFormFallbackLinkClick: (String) -> Unit
)
```
(`import org.koin.core.parameter.parametersOf`.)

Corresponding `MainActivity.kt` lines 86-139 change from `assistedViewModel { SessionViewModel.provideFactory(sessionViewModelFactory(), sessionId) }` to `SessionDetailRoute(sessionId = sessionId, ...)`; keep `backStackEntry.arguments!!.getString("sessionId")!!` (lines 89, 118) unchanged (CONTEXT: not this phase's fix).

---

### `androidApp/.../di/AppModule.kt` (config, startup, replaces Hilt `AppModule`)

**Analog:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt` (whole file). Two stages:

**Stage A, coexistence bridges (D-06).** Keep the Hilt class, convert each migrated binding into a one-line bridge, only here is `get()` allowed:
```kotlin
@Provides
fun store(): DevFestNantesStore = KoinPlatform.getKoin().get()
```
`@Binds` entries (lines 44-60) become `@Provides` bridges since the impl classes lose `@Inject`. Keep the Hilt plugin on every module until the last commit (RESEARCH Pattern 6).

**Stage B, final Koin module.** Android-only app-shell bindings (everything needing `BuildConfig`):
```kotlin
val appModule = module {
    single { CoroutinesDispatcherProvider(default = Dispatchers.Default, computation = Dispatchers.Default, io = Dispatchers.IO, main = Dispatchers.Main.immediate) }
    factory { OpenFeedbackConfig(enabled = BuildConfig.OPEN_FEEDBACK_ENABLED.toBoolean(), projectId = BuildConfig.OPEN_FEEDBACK_PROJECT_ID) }
    single<Timber.Tree> { if (BuildConfig.DEBUG) TimberTreeDebug(get()) else TimberTreeRelease(get()) }
    factory { DataSharingInitializer(get()) }
    factory { OpenFeedbackInitializer(androidApplication()) }
    factory { PerformanceInitializer() }
    single(named("applicationInitializers")) {
        listOf<ApplicationInitializer>(get<DataSharingInitializer>(), get<OpenFeedbackInitializer>(), get<PerformanceInitializer>())
    }
    factory { (activity: Activity) -> ExternalContentService(activity) }
    viewModelOf(::HomeViewModel)
}
```
Initializers use an **ordered list**, not `getAll<ApplicationInitializer>()` (hash map, no order guarantee; RESEARCH Pattern 7). Order is `DataSharingInitializer`, `OpenFeedbackInitializer`, `PerformanceInitializer` exactly as `AppModule.kt` lines 82-86 (`setOf(...)`). Behavior-critical: consent first, performance collection forced on last.

Scope mapping (copy RESEARCH "Scope-mapping table" verbatim into the plan). Hilt-to-Koin from this file: `@AppScope` (lines 44, 48, 52, 58, 65, 115, 121, 127, 135) -> `single`; unscoped `@Provides` (lines 75, 88, 91, 102, 105, 108) -> `factory`.

Hilt-removal also deletes `AppScope.kt` (`@AliasOf(Singleton::class)`) and the `androidApp/build.gradle.kts` line 3 `id("devfest.android.hilt")`. `androidApp/build.gradle.kts` dependencies block (lines 54-64) has no `:shared`; add `implementation(project(":shared"))` in the tracer commit (RESEARCH Pitfall 7).

---

### `DevFestNantesApplication.kt` (entry point, event-driven)

**Analog:** itself, lines 15-43.

```kotlin
@HiltAndroidApp
class DevFestNantesApplication : Application() {
    @Inject lateinit var appInitializers: Provider<Set<ApplicationInitializer>>
    @Inject lateinit var coroutineDispatcherProvider: CoroutinesDispatcherProvider
    @Inject lateinit var timerTree: Timber.Tree
    override fun onCreate() {
        // Start performance monitoring as early as possible
        PerformanceInitializer.startAppStartupTrace()
        super.onCreate()
        Timber.plant(timerTree)
        ...
        coroutineScope.launch {
            appInitializers.get().iterator().forEach { initializer -> initializer() }
        }
```
Target (RESEARCH Pattern 5/7). Hilt injects fields *inside* `super.onCreate()` (generated `Hilt_DevFestNantesApplication.onCreate()` is `hiltInternalInject(); super.onCreate()`), so while Hilt still exists Koin must start **before** `super.onCreate()` and after the startup trace:
```kotlin
private val appInitializers: List<ApplicationInitializer> by inject(named("applicationInitializers"))
private val coroutineDispatcherProvider: CoroutinesDispatcherProvider by inject()
private val timerTree: Timber.Tree by inject()

override fun onCreate() {
    PerformanceInitializer.startAppStartupTrace()   // stays first
    initKoin(androidModules) { androidContext(this@DevFestNantesApplication) }
    super.onCreate()
    Timber.plant(timerTree)
    ...
```
Keep resolving the initializer list lazily inside the launch (equivalent of `Provider.get()` inside the coroutine, line 39). `import org.koin.android.ext.android.inject`, `org.koin.core.qualifier.named`. `by inject()` is allowed here and in `MainActivity` only (D-11).

---

### `MainActivity.kt` (entry point, request-response)

**Analog:** itself, lines 32-52 (Hilt EntryPoint + injected fields).

Remove lines 32-36 imports, `@AndroidEntryPoint`, the `@EntryPoint ViewModelFactoryProvider` interface (lines 40-46) and `@Inject lateinit var` fields (48-52). Replace with (RESEARCH "MainActivity entry points"):
```kotlin
class MainActivity : ComponentActivity(), NavController.OnDestinationChangedListener {
    private val analyticsService: AnalyticsService by inject()
    private val externalContentService: ExternalContentService by inject { parametersOf(this) }
```
(`org.koin.android.ext.android.inject`, `org.koin.core.parameter.parametersOf`.) `ExternalContentService` is `@ActivityScoped` today (one instance per Activity instance); `factory { (activity: Activity) -> ExternalContentService(activity) }` resolved once per `MainActivity` field matches this. `ExternalContentService.kt` lines 10, 14, 16-17: drop `@ActivityScoped`, `@Inject`, keep `private val activity: Activity` constructor param.

---

### Initializers and Timber trees (strip annotations)

**Analogs (themselves):** `DataSharingInitializer.kt:4,6`, `OpenFeedbackInitializer.kt:7,9-10` (uses `Application` -> `androidApplication()`), `PerformanceInitializer.kt:7,12` (`class PerformanceInitializer @Inject constructor() : ApplicationInitializer` -> `class PerformanceInitializer : ApplicationInitializer`; companion `startAppStartupTrace()` lines 14-22 untouched), `TimberTreeDebug.kt:6,8`, `TimberTreeRelease.kt:6,8` (`class TimberTreeDebug @Inject constructor(private val firebaseCrashlytics: FirebaseCrashlytics) : Timber.DebugTree()`).

---

### `core:testing` fakes (test utility)

**Analog:** `core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeStores.kt` (lines 1-20): package `com.gdgnantes.devfest.core.testing`, top-level `fun fakeDevFestNantesStore(): DevFestNantesStore = DevFestNantesStoreBuilder().setUseMockServer(true).build()`, KDoc explaining intent. Keep this function untouched.

New files, same package and style:
- `FakeBookmarksStore.kt`: implements `BookmarksStore` (`core/data/.../BookmarksStore.kt`: `bookmarkedSessionIds: Flow<Set<String>>`, `isBookmarked`, `setBookmarked`, `subscribe`) with a `MutableStateFlow<Set<String>>`.
- `FakeAnalyticsService.kt`: implements the `AnalyticsService` commonMain interface (`core/analytics/src/commonMain/.../AnalyticsService.kt`), no-op bodies.
- `TestModules.kt` (RESEARCH Pattern 9):
  ```kotlin
  val testDoublesModule: Module = module {
      single<AnalyticsService> { FakeAnalyticsService() }
      single<BookmarksStore> { FakeBookmarksStore() }
      single<DevFestNantesStore> { fakeDevFestNantesStore() }
  }
  ```
  (listed **after** production modules; `allowOverride` is `true` by default).
- `core/testing/src/androidMain/...`: `NoOpPerformanceMonitoring` (androidMain-only because `PerformanceMonitoring` is).

Build analog: `core/testing/build.gradle.kts` lines 7-12 (`commonMain.dependencies { api(project(":core:model")); api(project(":core:data")); api(libs.kotlin.test) ... }`). Add `api(project(":core:analytics"))` and `api(libs.koin.core)`.

Test style analog: `core/testing/src/commonTest/.../FakeStoresTest.kt` (7-12): `kotlin.test.Test`, `kotlinx.coroutines.test.runTest`, `assertTrue`.

---

### Graph-verification tests

**Analog for placement/style:** `core/data/src/jvmTest/.../GraphQLStoreJvmTest.kt` (`kotlin.test` annotations) and `FakeStoresTest.kt`. RESEARCH provides spike-proven bodies:

`shared/src/jvmTest/.../KoinGraphTest.kt` (RESEARCH "Code Examples"):
```kotlin
class KoinGraphTest {
    @AfterTest fun tearDown() = stopKoin()
    @Test fun dataModuleVerifies() { dataModule.verify() }
    @Test fun startsAndResolvesStore() {
        initKoin()
        assertNotNull(KoinPlatform.getKoin().get<DevFestNantesStore>())
    }
}
```
`shared/src/iosTest/.../IosKoinTest.kt`: `IosDependencies.start()` then `assertNotNull(IosDependencies.store())`, `@AfterTest stopKoin()`.

`androidApp/src/test/.../di/*` (no analog; directory does not exist today): full graph via `module { includes(sharedModules + androidModules) }.verify(extraTypes = listOf(Context::class, Application::class, Activity::class, SharedPreferences::class, FirebaseAnalytics::class, ...))` plus a runtime start-up test and a deliberately failing negative test (RESEARCH Pitfall 2). The Firebase-on-JVM runtime strategy is RESEARCH Open Question 2 (human checkpoint).

---

### iOS: `BaseViewModel.swift` and `iOSApp.swift`

**Analog:** themselves.

`iosApp/iosApp/Model/BaseViewModel.swift` lines 12-19:
```swift
@MainActor
class BaseViewModel: ObservableObject {
    let store: DevFestNantesStore

    init() {
        self.store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: false).build()
    }
}
```
Target (D-04): `init(store: DevFestNantesStore = IosDependencies.shared.store()) { self.store = store }`. Subclasses (`AgendaViewModel`, `SpeakersViewModel`, `SpeakerDetailsViewModel`, `VenueViewModel`, `AboutViewModel`) must still compile with their implicit `init()`; check each for an explicit `super.init()` call.

`iosApp/iosApp/iOSApp.swift` lines 17-28: `init()` calls `FirebaseApp.configure()` then perf trace calls. Insert `IosDependencies.shared.start()` once, in `init()` (RESEARCH: never in a view's `init`), before any ViewModel is created (e.g. right after `FirebaseApp.configure()`).

Gate: after the Swift change, run `swift-names-gate.sh baseline` then `check` (RESEARCH Open Question 6), and `grep -c -i koin shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h` should be only the helper's own names. Note RESEARCH Pitfall 9: `iosApp/DevFest NantesTests/MockDevFestNantesStore.swift` is fully commented out (verified, every line prefixed `//`), so there is no live test path to preserve.

---

### `scripts/di-gate.sh` (CI gate, batch)

**Analog:** `.planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh` (tracked). Copy its structure: bash shebang, header comment documenting modes and tokens, `set -euo pipefail`, `export LC_ALL=C`, `SCRIPT_DIR`/`REPO_ROOT` resolution, `cd "$REPO_ROOT"`, `MODE` validation with a `Usage:` line, `WORK="$(mktemp -d ...)"` plus `trap 'rm -rf "$WORK"' EXIT`, then greppable fail tokens (`SWIFT-NAME-MISSING` style, e.g. `DI-HILT-FOUND`, `DI-LOCATOR-FOUND`) and a final `...-OK` line.

```bash
set -euo pipefail
export LC_ALL=C
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"   # for scripts/ this becomes /..
```
(Adjust the `REPO_ROOT` relative depth: `scripts/` is one level below the root, not three.) Use `git ls-files` scoped to `*.kt *.kts *.toml *.pro *.xml *.dcl` and exclude `.planning/`, `licenses.html`, and docs. Regexes and allowlist: RESEARCH Pattern 10 (import-based service-locator rule; allowlist `**/di/**`, `*Module.kt`, `shared/src/iosMain/**/IosDependencies.kt`, `DevFestNantesApplication.kt`, `MainActivity.kt`; `koinViewModel` is not banned).

Existing scripts in `scripts/` (`build-ios-development.sh`, `extract-firebase-dsyms.sh`, etc.) are release helpers and not gate-style; use Phase 3's gate as the template.

---

### CI workflows

**Analog:** `.github/workflows/android.yml` lines 15-23 (`checks` job) and 27-39 (`unit-tests` job).

```yaml
      - name: Unit tests
        run: ./gradlew testDebugUnitTest
```
Change to `./gradlew testDebugUnitTest jvmTest` (RESEARCH Pitfall 3: `testDebugUnitTest` alone does not run KMP `jvmTest`/`commonTest`). Add the gate to the `checks` job following the existing step style:
```yaml
      - name: DI gate
        run: bash scripts/di-gate.sh
```
`.github/workflows/ios.yml` (build step around line 128, "Build iOS App for Simulator" step near 200-215 uses `xcodebuild ... build`): add a `./gradlew :shared:iosSimulatorArm64Test` step. Reuse the existing "Resolve latest plain iPhone simulator" output (`steps.sim.outputs.device_name`) if the task needs a device name (RESEARCH A4). The "Build shared framework" step (run block lines ~110-125) is the Gradle-run style to copy.

## Shared Patterns

### Hilt-to-Koin scope mapping (applies to every binding)
**Source:** `androidApp/.../core/injection/AppModule.kt` (annotations) and `04-RESEARCH.md` "Scope-mapping table".
- `@AppScope`/`@Singleton` -> `single`
- unscoped `@Provides` -> `factory` (Firebase instances, `FirebaseRemoteConfig` with side effect, `OpenFeedbackConfig`)
- `@HiltViewModel` -> `viewModelOf(::X)` (`viewModel {}` is `factory` + ViewModelStore, same lifetime semantics)
- `@AssistedInject` -> `viewModel { (id: String) -> X(get(), get(), id) }` + `koinViewModel { parametersOf(id) }`
- `@ActivityScoped` -> `factory { (activity: Activity) -> ... }` resolved via `by inject { parametersOf(this) }` in `MainActivity`
- `@Binds` interface binding -> `single { Impl(get()) } bind Interface::class` (concrete primary, else `verify()` is vacuous)

### Per-module `di/` placement and naming
Package `<module namespace>.di`, file `XxxModule.kt`, one public `val xxxModule: Module`. For KMP modules, add `@HiddenFromObjC` on every public `Module` val when the module is `export()`-ed from `:shared` (`core:data`, `core:analytics`, `core:model`, see `shared/build.gradle.kts` 10-12). Android-only feature modules (`feature/*`, `core/ui`) are plain Android libraries (`src/main/java/...`), not KMP, so no `expect/actual` and no `@HiddenFromObjC` there.

### Hilt-annotation stripping recipe
For every class in the inventory: delete `import dagger.*`/`import javax.inject.*`, delete `@HiltViewModel`, delete `@Inject` before `constructor` (and the `constructor` keyword if no modifiers remain), delete `@Singleton`/`@ActivityScoped`. Do this in the same commit as the module that registers the class, and keep Hilt applied until the final removal commit (a migrated Hilt-provided type must have a Koin binding and a temporary `AppModule` bridge so unmigrated Hilt consumers still see the same instance, D-06).

### Service-locator ban
**Apply to:** all non-wiring Kotlin. `get()`/`inject()`/`KoinComponent`/`getKoin()` only inside `*Module.kt`, `di/`, `IosDependencies.kt`, `DevFestNantesApplication.kt`, `MainActivity.kt`, and temporarily `core/injection/AppModule.kt` (bridges). Composables resolve ViewModels only via default params `viewModel: X = koinViewModel()`, the same shape as today's `hiltViewModel()` default params (e.g. `Venue.kt` line 15).

### Verification (`verify()`) rules
**Apply to:** every `*ModuleTest.kt`. Import `org.koin.test.verify.verify`; list upstream and framework types in `extraTypes` (e.g. `DevFestNantesStore::class`, `AnalyticsService::class`, `BookmarksStore::class`, `PerformanceMonitoring::class`, `SharedPreferences::class`, `Context::class`, `Application::class`). `String`/`Int`/`Long`/`Double` are whitelisted by default so `parametersOf(id)` VMs verify. `verify()` is JVM-only (`jvmTest` / `testDebugUnitTest`).

### Commit/CI cadence
One commit per module, Android and iOS CI green per push (Phase 3 D-18 style), Hilt plugin kept on every module until the last commit. Tracer order: `:feature:venue` first, then about, settings, speakers, agenda, session-detail, then `core:ui` + agenda + session-detail `BookmarksViewModel` flip (single 3-module commit because `AgendaRow.kt:43` and `SessionLayout.kt:63` both request it; RESEARCH Pattern 6), then `HomeViewModel`/entry points, then Hilt removal.

## No Analog Found

| File | Role | Data Flow | Reason |
|------|------|-----------|--------|
| `shared/src/commonMain/.../InitKoin.kt` | composition root | startup | No Koin or `startKoin` anywhere; use RESEARCH Pattern 3 (spike-proven) |
| `shared/src/iosMain/.../IosDependencies.kt` | Swift bridge | request-response | No `iosMain` source set exists in `:shared`; RESEARCH Pattern 3 |
| `core/*/src/{androidMain,jvmMain,iosMain}/.../*PlatformModule.<platform>.kt` | expect/actual | startup | Repo has no `expect/actual` declarations or `jvmMain`/`iosMain` source sets; use RESEARCH Pattern 2 |
| `androidApp/src/test/.../di/*` | test | request-response | `androidApp` has no `src/test` dir (only `androidTest/.../MainActivityTest.kt`); use RESEARCH Pattern 8 and Open Question 2 |
| `shared/src/iosTest/.../IosKoinTest.kt` | test | request-response | No `iosTest` anywhere; RESEARCH "Spike-proven iOS runtime test" |
| `core/testing/src/androidMain/.../NoOpPerformanceMonitoring.kt` | test utility | request-response | `core:testing` has only `commonMain`; needs an `androidMain` source set (and depends on the `PerformanceMonitoring` interface seam, RESEARCH Open Question 4) |
| `feature/*/src/test/...` module tests | test | request-response | No existing JVM unit tests in any feature/UI module; use RESEARCH "Feature module test" |

## Metadata

**Analog search scope:** `androidApp/`, `build-logic/`, `core/*`, `feature/*`, `shared/`, `iosApp/`, `gradle/`, `scripts/`, `.github/workflows/`, `.planning/phases/03-*` (gate scripts)
**Files scanned:** ~210 tracked source files listed; ~45 read in full or in part
**Tracked-source gate:** all cited analog paths come from `git ls-files` output (no `.gsd/` or other mirror paths involved)
**Pattern extraction date:** 2026-10-04
