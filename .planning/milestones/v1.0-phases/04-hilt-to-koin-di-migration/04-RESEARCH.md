# Phase 4: Hilt to Koin DI Migration - Research

**Researched:** 2026-10-02
**Domain:** Dependency injection migration (Dagger Hilt to Koin 4.x plain DSL) in a Kotlin Multiplatform + Android + iOS (K/N umbrella framework) multi-module project
**Confidence:** HIGH (the riskiest claims were proven by a throwaway spike build, see "Spike evidence")

## Summary

Koin **4.2.2** (Maven Central `lastUpdated 20260615`) is the current stable release and is the right pin. It is built with Kotlin 2.3.20 but a spike in a scratch copy of this repo proved it links and runs on Kotlin **2.4.20**, AGP 9.4, Gradle 9.7.1: `:shared:linkDebugFrameworkIosSimulatorArm64`, `:shared:jvmTest`, `:shared:iosSimulatorArm64Test` (Koin started on Kotlin/Native, store resolved) and an Android-library `testDebugUnitTest` with `verify()` all passed. Koin ships `ios*` klibs, so K/N compatibility is not a risk. The research brief's "Koin BOM 4.1.1, avoid 4.2.0-RC" is superseded: 4.2.x is GA.

The plan can follow CONTEXT D-01..D-14 as written, but the spike and code reading surfaced **eleven corrections/traps the planner must build into tasks** (details in Common Pitfalls): (1) public Koin `Module` vals in an `export()`-ed KMP module leak ~30 Koin types into `shared.h` unless annotated `@HiddenFromObjC`; (2) `verify()` only inspects the *primary type's* constructors, so `single<Interface> { Impl(get()) }` is vacuously "verified"; (3) `getAll<ApplicationInitializer>()` has **no ordering guarantee** and initializer order is behaviorally significant; (4) `./gradlew testDebugUnitTest` (the only Android CI test command) does **not** run any KMP `jvmTest`/`commonTest`, and the iOS CI job runs no Gradle tests at all, so D-12's "existing CI jobs" claim needs two added steps; (5) Hilt injects the Application *before* `Application.onCreate()` body, so during coexistence Koin must start before `super.onCreate()`; (6) every `expect` needs an `actual` on **jvm** as well as android/ios; (7) `:androidApp` no longer depends on `:shared` (Phase 3) so the dependency must be re-added; (8) KSP must stay (core:data needs it for kmp-native-coroutines); (9) iOS `MockDevFestNantesStore` is fully commented out and `DevFest_NantesTests.swift` references a non-existent `DevFestViewModel`, so the "keeps working" premise of D-04 is moot; (10) D-02's rationale is slightly off: `apolloClient` is already a process-global top-level `val`, so every iOS store already shares one Apollo client and cache (the singleton change is even more invisible than assumed); (11) the runtime start-up test cannot resolve `FeedbackFormViewModel` on the plain JVM without a Firebase seam (Robolectric or a mock library), which is an open decision.

**Primary recommendation:** Use Koin 4.2.2 plain DSL, declared as `concrete class primary + bind Interface` (`singleOf(::Impl) { bind<I>() }` or `single { Impl(get()) } bind I::class`) so `verify()` is meaningful; start with a tracer on `:feature:venue` + iOS, keep Hilt applied to every module until the final removal commit, bridge Hilt `@Provides` to Koin only inside `AppModule`, and add the two missing CI test steps (`jvmTest` on Android CI, `:shared:iosSimulatorArm64Test` on iOS CI) in the tracer plan.

## User Constraints (from CONTEXT.md)

<user_constraints>
### Locked Decisions

**iOS & initKoin()**
- **D-01:** iOS **adopts Koin**: the Swift `App` init calls the Kotlin `initKoin()`, and Swift gets its dependencies from Koin instead of calling `DevFestNantesStoreBuilder()` directly. One composition root on both platforms (roadmap SC-2 "single `initKoin()`", PROJECT.md KMP constraint).
- **D-02:** iOS gets **one shared `DevFestNantesStore` singleton** (today every Swift `BaseViewModel` builds its own store, i.e. its own Apollo client + normalized cache). This aligns iOS with Android's `@AppScope` store. It's a deliberate, user-approved change with no visible effect; record it as such, not as a regression.
- **D-03:** `initKoin()` and the composed module list live in **`:shared`** (the umbrella, the only module that already sees every KMP module). Shape: `:shared` commonMain `initKoin(platformModules: List<Module> = emptyList(), appDeclaration: KoinAppDeclaration = {})` listing all KMP core modules; `:androidApp` calls it from `Application.onCreate` passing its Android-only modules (core `androidMain` platform modules are reached via expect/actual, feature modules and the app module are passed in); `:shared` iosMain provides the iOS-facing helpers. — **Reversibility:** reversible. Moving the entry point into a new `:core:di` module later is a local change.
  - **Amendment to Phase 3 D-10:** `:shared` is no longer limited to `SharedFrameworkPlaceholder.kt`; it now also holds the DI composition root (commonMain `initKoin`) and the iosMain Swift helper. Nothing else goes in `:shared`. The placeholder file can be deleted once real sources exist (planner verifies NO-SOURCE no longer applies).
- **D-04:** Swift accesses Koin through a **typed Kotlin helper** in `:shared` iosMain, e.g. `object IosDependencies : KoinComponent { fun store(): DevFestNantesStore = get() }`, plus an iOS-callable `doInitKoin()`/`initKoinIos()` wrapper. Swift `BaseViewModel` becomes `init(store: DevFestNantesStore = IosDependencies.shared.store())` so the existing `MockDevFestNantesStore` test path in `iosApp/DevFest NantesTests` keeps working. No generic Swift `inject<T>()` extension. These are **additive** Swift-visible names; Phase 3 D-11 (existing Swift names stable) still holds, and `swift-names-gate.sh`'s baseline is updated once for the added symbols. Koin/`KoinComponent` types must not leak into Swift beyond this helper, so check `shared.h` the same way Phase 3 did.

**Migration sequencing**
- **D-05:** **Tracer first, then bottom-up coexistence.** Tracer plan: add Koin to the version catalog and convention plugins, write `initKoin()` in `:shared`, start Koin in `DevFestNantesApplication` **alongside** Hilt, migrate the core bindings needed by one leaf feature plus that feature (**`:feature:venue`**) to `koinViewModel()`, and switch iOS to `initKoin()` + `IosDependencies` (D-01/D-04), proven by iOS CI green + a local simulator smoke run. If Koin in the K/N umbrella misbehaves, stop and rethink before more modules move. Then migrate bottom-up, **one commit per module**, full Android + iOS CI green per pushed step (Phase 3 D-18 style): core modules → features least-coupled first (venue → about → settings → speakers → agenda → session-detail), `:core:ui` `BookmarksViewModel` and `:androidApp` `HomeViewModel`/entry points next. The last step removes `AppModule`, `AppScope`, `AssistedViewModelUtils.kt`, the Hilt plugin/KSP/deps, `AndroidHiltConventionPlugin`, the Hilt dependency-resolution rules, and the catalog entries, then does a clean build. One phase branch/PR. — **Reversibility:** reversible. Each step is its own commit.
- **D-06:** During coexistence, **Koin owns every shared singleton** (store, `SharedPreferences`, `AnalyticsService`, `PerformanceMonitoring`, `BookmarksStore`, services). Hilt's `AppModule` `@Provides` for a migrated binding become temporary one-line bridges to Koin (`KoinPlatform.getKoin().get<T>()` or the Koin 4 equivalent), so Hilt ViewModels that haven't moved yet share the **same instance** and the app never holds two copies. Bridges shrink as features migrate and disappear with `AppModule`. These temporary `get()` calls are allowed only inside `AppModule` (it is DI wiring).
- **D-07:** **Android-only bindings live in the owning module's `androidMain`**, exposed via an expect/actual platform module (e.g. `expect val analyticsPlatformModule: Module`). `:core:analytics` androidMain binds `FirebaseAnalyticsService` → `AnalyticsService`, `PerformanceMonitoring` and the Firebase instances it needs (Analytics, Performance, Crashlytics, RemoteConfig as consumed). `:core:data` androidMain binds `BookmarksStoreImpl` → `BookmarksStore` and `SharedPreferences` (via `androidContext()`). Android-only feature modules bind their own services (`:feature:agenda` → `SessionFiltersService`; `:feature:settings` → `DataCollectionSettingsService`; `:feature:session-detail` owns its ViewModels, but its `OpenFeedbackConfig` value comes from the app). `:androidApp` keeps **only** what needs `BuildConfig` or is app-shell: `OpenFeedbackConfig`, Timber tree selection, the `ApplicationInitializer`s, `CoroutinesDispatcherProvider` if it stays in the app, `ExternalContentService`, `HomeViewModel`. iOS actuals bind the iOS equivalents, or nothing where iOS has no implementation today (don't invent new iOS features).
- **D-08:** Hilt-scope → Koin mapping is **behavior-preserving**: `@AppScope`/`@Singleton` → `single`; unscoped `@Provides` → `factory` unless the planner proves `single` is observably identical (e.g. `Firebase.analytics` is already a process singleton). Keep `Firebase.remoteConfig`'s `setConfigSettingsAsync(minimumFetchInterval = 300)` side effect timing equivalent. Produce an explicit scope-mapping table (research Pitfall 5) in the plan.

**Parameterized ViewModels**
- **D-09:** `SessionViewModel(sessionId)` and `SpeakerViewModel(speakerId)` switch from Hilt `@AssistedInject` to **Koin parameters**: `viewModel { (id: String) -> SessionViewModel(id, get(), get()) }` (or `viewModelOf` equivalent), resolved with `koinViewModel { parametersOf(sessionId) }`. The id stays a plain constructor parameter. Delete `@AssistedFactory`, the `provideFactory` companions, `MainActivity.ViewModelFactoryProvider` `@EntryPoint`, and `androidApp/.../utils/AssistedViewModelUtils.kt`. No `SavedStateHandle` nav-arg reading, because features stay navigation-agnostic (Phase 3 D-07).
- **D-10:** `koinViewModel()` is called **inside the feature `*Route`**, as every other screen already does with `hiltViewModel()` default params. `SessionDetailRoute`/`SpeakerDetailRoute` change to take `sessionId: String`/`speakerId: String` and resolve the ViewModel themselves (an optional `viewModel` param with a `koinViewModel { parametersOf(id) }` default is fine for tests and previews). `MainActivity` passes only the id and the callbacks, the Nav3-ready shape. ViewModel scoping (per `NavBackStackEntry` / `ViewModelStoreOwner`) must stay identical to today's `hiltViewModel()` behavior, including `BookmarksViewModel` being requested from `AgendaRow` and `SessionLayout`.
- **D-11:** **`by inject()` is allowed only in Android framework entry points**, `DevFestNantesApplication` and `MainActivity`, since they are framework-created. The `Set<ApplicationInitializer>` multibinding becomes individual `single { … } bind ApplicationInitializer::class` definitions (or equivalent) collected with `getKoin().getAll<ApplicationInitializer>()` in `Application`. Keep initializer execution order and the "start performance trace before `super.onCreate()`" behavior identical. No other class calls `get()`/`inject()`/implements `KoinComponent` except the iOS helper (D-04) and the temporary `AppModule` bridges (D-06).

**Graph verification & guards**
- **D-12:** Success criterion 5's "`checkModules()`" is read as **"Koin graph verification"**, not the literal API, because `checkModules()` is deprecated in Koin 4. Two layers:
  1. **Static `verify()`** (JVM unit tests): each module's own test verifies that module in isolation, with its upstream dependencies and framework types (`Context`, `Application`, `SavedStateHandle`, Firebase types) declared as `extraTypes`, plus one **full Android graph** `verify()` in `:androidApp` over the exact list `initKoin()` receives.
  2. **Runtime start-up test**: one `:androidApp` unit/Robolectric test that actually starts Koin with the Android module list (Firebase-dependent definitions overridden with `:core:testing` fakes where they can't run on the JVM) and resolves every ViewModel, including the `parametersOf` ones, and every entry-point dependency. This catches parameter/factory errors that the static check can't see.
  3. **iOS**: an `iosTest` (or `commonTest` run on iOS) in `:shared` calls `initKoin()` and resolves the store through `IosDependencies`, so iOS CI proves the umbrella graph.
  All of these run in the **existing** Android unit-test and iOS CI jobs. Add CI steps only if needed.
- **D-13:** **Scripted CI gate** (in the style of Phase 3's `swift-names-gate.sh`, run in the Android CI job) that fails on:
  - any `dagger.`, `javax.inject`, `hilt` import, annotation, plugin id or catalog alias;
  - `get()` / `inject()` / `KoinComponent` / `getKoin()` usage outside DI wiring files (`*Module.kt` / `di/` packages, as the planner defines them), `:shared`'s `IosDependencies`, `DevFestNantesApplication`, and `MainActivity`.
  This makes SC-1 and SC-4 permanently enforced. The gate lands at the end of the phase, or is introduced earlier with an allowlist that shrinks, as the planner sees fit. It must not block coexistence steps.
- **D-14:** `:core:testing` grows **DI-focused fakes only**: `FakeBookmarksStore`, `FakeAnalyticsService`, a no-op `PerformanceMonitoring` (introduce an interface or open seam if the concrete Firebase-backed class can't be faked, as a minimal behavior-neutral refactor), and a Koin test helper (e.g. a `testModule` override list / `KoinTestRule`-style helper) that swaps fakes into the graph. Keep the existing `fakeDevFestNantesStore()`. Broad ViewModel test coverage stays in Phase 5 (TEST-03).

### Claude's Discretion
- Koin version and BOM pin (research suggested Koin BOM 4.1.1 as of research date; the researcher checks the current stable Koin 4.x and its Kotlin 2.4.20 / K/N compatibility), and which artifacts go where (`koin-core`, `koin-android`, `koin-androidx-compose`, `koin-compose-viewmodel`, `koin-test`, `koin-test-junit4/5`).
- Koin DSL style (`singleOf(::X) { bind<Y>() }` / `viewModelOf` vs `single { X(get()) }`), module naming (`venueModule`, `dataModule`…), and file placement convention (`di/XxxModule.kt` per module), as long as it's consistent everywhere.
- Where the expect/actual platform modules are declared and exact expect/actual shape per core module.
- Convention-plugin changes: replacing `AndroidHiltConventionPlugin` with Koin deps in the feature/library conventions, and the order of Hilt removal from `build-logic`.
- Android manual smoke checkpoints (`android run` + `android layout`/`screen capture`: all tabs, session detail, speaker detail, settings, bookmark toggle) and iOS simulator smoke checkpoints. At minimum, after the tracer and after the final Hilt removal.
- Where `CoroutinesDispatcherProvider` lives and is bound.
- Whether the D-13 gate is introduced early with a shrinking allowlist or only at the end.

### Deferred Ideas (OUT OF SCOPE)
None from this discussion. Already-tracked related items: Koin Annotations / compiler plugin (DI-V2-01), Navigation 3 migration, `!!` nav-arg cleanup, `println`→Timber, test-coverage retrofit (Phase 5).

Also out of scope per the Phase Boundary: Koin Annotations / compiler plugin (DI-V2-01), Navigation 3 migration or route/argument format changes, broad ViewModel/Store test coverage (Phase 5, TEST-01..04), `StoreStubs` RNG seeding / `SimpleDateFormat` fixes (Phase 5), drive-by behavior fixes (`println`→Timber, `!!` nav args, `init {}` error handling).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| DI-01 | DI fully migrated from Dagger Hilt to Koin | Coexistence/bridge pattern (Pattern 6), Hilt removal inventory (Runtime State Inventory + Pitfall 8/9), D-13 gate (Pattern 10), scope-mapping table |
| DI-02 | Each `core-*`/`feature-*` module exposes its own Koin `module { }`, composed via a single `initKoin()` | Project structure + Pattern 1/2/3 (per-module `di/XxxModule.kt`, `sharedModules` list in `:shared`, `androidModules` list in `:androidApp`); Open Question 1 on `core:model`/`core:network` |
| DI-03 | Platform dependencies via `expect`/`actual` or per-platform Koin modules | Pattern 2 (expect/actual `Module` val, actuals required on android, jvm AND ios; verified by spike), `androidApplication()` / `androidContext()` source-verified |
| DI-04 | ViewModels/Stores use constructor injection; no scattered `get()` outside the wiring layer | Pattern 4 (`viewModelOf`, params), Pattern 10 gate regexes, `ExternalContentService` via `parametersOf(this)` |
| DI-05 | `core-testing` centralizes shared fakes | Pattern 9 (fakes inventory, PerformanceMonitoring seam, Koin test helper) |
| DI-06 | A Koin graph-verification test runs in CI | Pattern 8 (`verify()` semantics proven by spike, composite `includes`, CI gaps: `jvmTest`, iOS test step), Validation Architecture |
</phase_requirements>

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Composition root (`initKoin`, shared module list) | `:shared` umbrella (KMP commonMain) | `:androidApp` (passes Android modules) | Only module that sees every KMP module; both apps call it (D-03) |
| Cross-platform bindings (store, analytics interface) | KMP core modules (`core:data`, `core:analytics`) commonMain | - | Same code on Android and iOS |
| Platform bindings (`SharedPreferences`, `BookmarksStoreImpl`, Firebase) | Per-module `androidMain` via `expect val ...PlatformModule` | iOS actual = empty module | iOS has no equivalent today; do not invent |
| ViewModel construction | Android feature modules (`viewModelOf` in each module's `di/`) | `:core:ui` (`BookmarksViewModel`), `:androidApp` (`HomeViewModel`) | Features own their VMs (Phase 3 ARCH-03) |
| ViewModel resolution at call site | Feature `*Route`/screen composables (`koinViewModel()`) | - | Scoping must equal today's `hiltViewModel()` (D-10) |
| Framework-created objects (Application, Activity) | `:androidApp` (`by inject()` allowed here only) | - | D-11 |
| BuildConfig-dependent values (`OpenFeedbackConfig`, Timber tree) | `:androidApp` `appModule` | - | `BuildConfig` is app-only |
| iOS dependency access | `:shared` iosMain `IosDependencies` | Swift `BaseViewModel` default arg | Typed helper, no Koin types in Swift (D-04) |
| Graph verification | JVM unit tests (`verify()`) per module + app | iOS `iosSimulatorArm64Test` in `:shared` | Static + runtime layers (D-12) |
| Regression gate | CI shell script (git grep) | - | Cheap, no Gradle (D-13) |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `io.insert-koin:koin-core` | 4.2.2 | DSL, `startKoin`, `Module`; KMP (android/jvm/ios*) | Official KMP DI. Maven metadata: latest/release 4.2.2, lastUpdated 20260615 [VERIFIED: repo1.maven.org koin-core maven-metadata.xml]. Gradle module metadata lists `iosArm64`, `iosSimulatorArm64`, `iosX64`, `jvm`, `android` variants [VERIFIED: koin-core-4.2.2.module] |
| `io.insert-koin:koin-android` | 4.2.2 | `androidContext()`, `androidApplication()`, `inject()` for Activity/Application | Needed in Android feature modules, `core:*` `androidMain`, `:androidApp` |
| `io.insert-koin:koin-androidx-compose` | 4.2.2 | Pulls `koin-compose` + `koin-compose-viewmodel` (pom compile deps) so `org.koin.compose.viewmodel.koinViewModel` is available in Android feature modules | Compiles and runs in the spike (venue module) |
| `io.insert-koin:koin-test` | 4.2.2 | `Module.verify()` (JVM only), `KoinTest` | Official verification API; `checkModules` is `@Deprecated("Migrate to verify() API")` in 4.2.2 sources [VERIFIED: koin-test-4.2.2-sources CheckModules.kt] |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `io.insert-koin:koin-test-junit4` | 4.2.2 | `KoinTestRule.create { modules(...) }` (stops Koin after each test) | JUnit4 unit tests in `:androidApp`/features that start Koin (runtime start-up test). Not KMP |
| `org.robolectric:robolectric` | 4.17 (published 2026-09-10; supports API 37; needs `--add-opens` on JDK 17+) | Real `Application`/`SharedPreferences` for the runtime start-up test | Only if Open Question 2 resolves to Robolectric [CITED: github.com/robolectric/robolectric/releases/tag/robolectric-4.17] |
| `androidx.preference:preference` | existing catalog `preference = "1.2.1"` | `PreferenceManager.getDefaultSharedPreferences` moves from `AppModule` to `core:data` androidMain | Keeps the **same prefs file** (consent, bookmarks, filters persist) |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| `koin-androidx-compose` | `koin-compose-viewmodel` directly | Same `koinViewModel` (identical signature, verified in sources). `koin-androidx-compose` is the documented Android-only path and depends on both. Use the `org.koin.compose.viewmodel` import everywhere (canonical, KMP) |
| Koin BOM | per-artifact `version.ref = "koin"` | Recommended: single `koin = "4.2.2"` version ref, no BOM. The BOM is unproven inside KMP `commonMain` in this repo; spike used plain `version.ref` and worked |
| Koin Annotations / compiler plugin | plain DSL | Deferred by DI-V2-01 and PROJECT decision |
| `koin-dagger-bridge` (exists in BOM) | manual `AppModule` bridges (D-06) | D-06 is locked; do not add the artifact |

**Installation (catalog entries, spike-proven shape):**
```toml
[versions]
koin = "4.2.2"

[libraries]
koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-androidx-compose = { group = "io.insert-koin", name = "koin-androidx-compose", version.ref = "koin" }
koin-test = { group = "io.insert-koin", name = "koin-test", version.ref = "koin" }
koin-test-junit4 = { group = "io.insert-koin", name = "koin-test-junit4", version.ref = "koin" }
```

**Version verification:** `curl -s https://repo1.maven.org/maven2/io/insert-koin/<artifact>/maven-metadata.xml` returned `<release>4.2.2</release>` for koin-core, koin-android, koin-androidx-compose, koin-compose, koin-compose-viewmodel, koin-test, koin-test-junit4, koin-core-viewmodel, koin-bom (this session, 2026-10-02).

**Transitive uplifts (observed in the spike with `:feature:venue:dependencies`):** `androidx.activity:activity-compose` and `activity-ktx` resolve to **1.12.4** (catalog pins `activity = "1.9.2"`), `androidx.appcompat` 1.7.1 [VERIFIED: spike dependency report; koin-android pom: appcompat 1.7.1, activity-ktx 1.12.4, fragment-ktx 1.8.9, lifecycle-viewmodel-ktx 2.10.0]. The project already resolves compose to 1.12.1 via the BOM. This is expected and compiled/tested in the spike, but it changes runtime library versions: the Android smoke checkpoints are the only runtime proof.

## Package Legitimacy Audit

The `gsd-tools query package-legitimacy check` seam only supports `npm|pypi|crates` (it printed `Usage: ... --ecosystem <npm|pypi|crates>` for Maven). Maven coordinates were therefore audited manually against Maven Central metadata, the official Koin docs (insert-koin.io) and the POM `scm` block (`github.com/InsertKoinIO/koin`).

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| io.insert-koin:koin-core / koin-android / koin-androidx-compose / koin-test / koin-test-junit4 | Maven Central | multi-year line, 4.2.2 published 2026-06-15 | widely used (not measured; seam unsupported) | github.com/InsertKoinIO/koin (POM scm) | OK (manual) | Approved; groupId matches the official docs |
| org.robolectric:robolectric | Maven Central | 4.17 published 2026-09-10 | widely used (not measured) | github.com/robolectric/robolectric | OK (manual) | Conditional on Open Question 2; planner gates install behind a `checkpoint:human-verify` as it is a new test dependency |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none. Because the seam could not run for Maven, treat both rows as `[CITED]`, not `[VERIFIED: registry]`; the planner should keep a single human-verify checkpoint at the first Koin dependency add (it is the project's first new runtime dependency since Phase 2).

## Architecture Patterns

### System Architecture Diagram

```
                         iOS                                         Android
        iOSApp.init()  (Swift)                          DevFestNantesApplication.onCreate()
              |                                                       |
   IosDependencies.shared.start()                  PerformanceInitializer.startAppStartupTrace()
   (iosMain, no Koin types in Swift)                                  |
              |                                  initKoin(platformModules = androidModules,
              |                                           appDeclaration = { androidContext(this) })
              v                                                       |
   +----------------------- :shared (commonMain) ----------------------+
   |  initKoin(platformModules, appDeclaration)  @HiddenFromObjC        |
   |    startKoin { modules( sharedModules + platformModules ) }        |
   |    sharedModules = [networkModule, dataModule, dataPlatformModule, |
   |                     analyticsModule, analyticsPlatformModule]      |
   +---------------------------+----------------------------------------+
                               | (public @HiddenFromObjC Module vals)
        +----------------------+-----------------------+
        v                      v                       v
  :core:network          :core:data               :core:analytics
  networkModule     dataModule (store single)    analyticsModule(common)
                    expect dataPlatformModule     expect analyticsPlatformModule
                      android: SharedPreferences,   android: FirebaseAnalytics/Perf/
                        BookmarksStoreImpl bind       Crashlytics, FirebaseAnalyticsService
                      jvm/ios: empty module           bind AnalyticsService, PerformanceMonitoring
                                                      jvm/ios: empty module

  :androidApp passes ->  androidModules = [coreUiModule, venueModule, aboutModule, settingsModule,
                         speakersModule, agendaModule, sessionDetailModule, appModule]
        |
        v
  Composables:  XxxRoute() -> koinViewModel()  -> ViewModelStore of NavBackStackEntry (unchanged scoping)
  MainActivity: by inject()  (AnalyticsService, ExternalContentService w/ parametersOf(this))
  Application:  by inject()  (Timber.Tree, ordered initializer list, dispatcher provider)

  Tests:  per-module verify()  |  androidApp full-graph verify() + runtime start test  |  :shared iosSimulatorArm64Test
  Gate:   scripts/di-gate.sh (git grep) in android.yml `checks` job
```

### Recommended Project Structure
```
shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/
    InitKoin.kt                 # sharedModules + initKoin (@HiddenFromObjC); SharedFrameworkPlaceholder.kt deleted
shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/
    IosDependencies.kt          # object IosDependencies { start(), store() }, no KoinComponent
shared/src/iosTest | jvmTest    # start-up test (iOS) + verify test (JVM)
core/network/src/commonMain/.../core/network/di/NetworkModule.kt
core/data/src/commonMain/.../core/data/di/DataModule.kt                 # dataModule + expect dataPlatformModule
core/data/src/{androidMain,jvmMain,iosMain}/.../di/DataPlatformModule.<platform>.kt
core/analytics/src/commonMain|androidMain|jvmMain|iosMain/.../di/        # analyticsModule + expect/actual analyticsPlatformModule
core/ui/src/main/java/.../core/ui/di/CoreUiModule.kt                     # BookmarksViewModel
core/testing/src/commonMain/.../testing/                                 # FakeBookmarksStore, FakeAnalyticsService, fakeModule helper
core/testing/src/androidMain/.../testing/                                # NoOp PerformanceMonitoring (androidMain-only type)
feature/<x>/src/main/java/.../feature/<x>/di/<X>Module.kt                # viewModelOf + services
feature/<x>/src/test/java/.../di/<X>ModuleTest.kt                        # verify()
androidApp/src/main/java/.../androidapp/di/AppModule.kt (replaces core/injection/AppModule.kt)
androidApp/src/test/java/.../di/                                         # full-graph verify + runtime start-up test
scripts/di-gate.sh                                                        # D-13 (repo-scripts location, runs in CI)
```

### Pattern 1: One `module { }` per Gradle module, concrete-primary + `bind`
**What:** Declare implementations by concrete class and bind the interface, so `verify()` inspects the implementation constructor.
**When to use:** Every service/impl pair (`FirebaseAnalyticsService`->`AnalyticsService`, `BookmarksStoreImpl`->`BookmarksStore`, `SessionFiltersServiceImpl`->`SessionFiltersService`, `DataCollectionSettingsServiceImpl`->`DataCollectionSettingsService`).
**Why (proven):** In the spike, `module { single<Svc> { SvcImpl(get()) } }.verify()` **passed even though `Dep` was undefined** (primary type is the interface, no constructors to inspect), while `module { single { SvcImpl(get()) } bind Svc::class }.verify()` threw `MissingKoinDefinitionException`. Source: `Verification.verifyFactory` uses `beanDefinition.primaryType.constructors` [VERIFIED: koin-test-jvm-4.2.2-sources Verification.kt + spike VerifySemanticsTest, 4/4 green].
**Example:**
```kotlin
// Source: spike (scratchpad) + Koin 4.2.2 sources
val venueModule = module {
    viewModelOf(::VenueViewModel)                       // org.koin.core.module.dsl.viewModelOf
}
val sessionFiltersModule = module {
    single { SessionFiltersServiceImpl(get()) } bind SessionFiltersService::class   // org.koin.dsl.bind
}
```

### Pattern 2: `expect val ...PlatformModule: Module` with actuals on android, jvm AND ios
**What:** `commonMain` declares `expect val dataPlatformModule: Module`; `androidMain` binds Android types; `jvmMain` and `iosMain` return `module { }`.
**Proven constraints:** (a) removing the jvm actual fails `:core:data:compileKotlinJvm` with `NO_ACTUAL_FOR_EXPECT ... in module '<commonMain> for JVM'` because the convention declares `jvm()` [VERIFIED: spike spike7.log; convention `KmpLibraryConventionPlugin.kt:46-49` declares `jvm()`, `iosX64()`, `iosArm64()`, `iosSimulatorArm64()`]; (b) the default hierarchy template gives an `iosMain` source set (spike used `core/data/src/iosMain`); (c) an android actual using `androidApplication()` + `PreferenceManager.getDefaultSharedPreferences(...)` compiled with `koin-android` + `androidx.preference` on `androidMain` [VERIFIED: spike `:core:data:compileAndroidMain` EXIT 0].
**Example (android actual, compiled in spike):**
```kotlin
actual val dataPlatformModule: Module = module {
    single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }
    single { BookmarksStoreImpl(get()) } bind BookmarksStore::class
}
```
`BookmarksStoreImpl` must lose `@Inject constructor(` -> `constructor(` and the `javax.inject.Inject` import (done in spike).

### Pattern 3: `initKoin()` in `:shared`, hidden from ObjC
**What:** commonMain `initKoin` plus a `sharedModules` list; both `@HiddenFromObjC`. All public `Module` vals in **exported** modules (`core:data`, `core:analytics`) also get `@HiddenFromObjC` (on the `expect` and on every `actual`).
**Proven:** without the annotation on `core:data`'s `dataModule`/`dataPlatformModule`, `shared.h` contained `SharedKoin_coreModule`, `SharedKoin_coreScope`, `SharedKoin_coreKoinComponent`, ~30 Koin types in forward declarations plus full `@interface`s (header lines 9-11, 913-980). After adding `@HiddenFromObjC` the only Koin mention left was my own `doInitKoinIos()` function name [VERIFIED: spike spike3.log + `grep -c -i koin shared.h` = 1]. Required import is `kotlin.native.HiddenFromObjC` and `@OptIn(ExperimentalObjCRefinement::class)` (`kotlin.experimental`); the first attempt without the `kotlin.native` import failed with `Unresolved reference 'HiddenFromObjC'`.
**Convention tip:** the convention already does `languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")` for every source set (`KmpLibraryConventionPlugin.kt:60`); add `ExperimentalObjCRefinement` there to avoid per-file `@OptIn`.
```kotlin
// shared/src/commonMain/.../InitKoin.kt  (compiled + header-checked in spike)
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun initKoin(platformModules: List<Module> = emptyList(), appDeclaration: KoinAppDeclaration = {}): KoinApplication =
    startKoin {
        appDeclaration()
        modules(sharedModules + platformModules)
    }
```
```kotlin
// shared/src/iosMain/.../IosDependencies.kt  (no KoinComponent: avoids the Koin_coreKoinComponent protocol in shared.h)
object IosDependencies {
    fun start() { initKoin() }
    fun store(): DevFestNantesStore = KoinPlatform.getKoin().get()
}
```
**Swift naming trap:** a top-level Kotlin function whose name starts with `init` is renamed by K/N to `doInit...` (spike header: `+ (void)doInitKoinIos ... swift_name("doInitKoinIos()")`). Put the starter *inside* the `IosDependencies` object (`IosDependencies.shared.start()`) to avoid a `IosDependenciesKt.doInitKoinIos()` call site. Swift: `BaseViewModel.init(store: DevFestNantesStore = IosDependencies.shared.store())`; `iOSApp.init()` calls `IosDependencies.shared.start()` before `FirebaseApp.configure()` or right after it (before any ViewModel).
D-04's `KoinComponent` shape is only an "e.g."; the non-`KoinComponent` shape above keeps it a typed helper and is lighter on the header.

### Pattern 4: ViewModels and parameters
`viewModel {}` is `factory` under the hood [VERIFIED: koin-core-viewmodel `ModuleExt.kt`: `return factory(qualifier, definition)`], so the ViewModelStore (not Koin) owns lifetime, exactly like Hilt.
`koinViewModel` signature (verified in sources, identical in `org.koin.compose.viewmodel` and `org.koin.androidx.compose`): `koinViewModel(qualifier, viewModelStoreOwner = LocalViewModelStoreOwner.current ?: error(...), key, extras, scope = currentKoinScope(), parameters: ParametersDefinition? = null)`. Default key is the class name, so every call returns the **same instance per `ViewModelStoreOwner`**: `BookmarksViewModel` requested from many `AgendaRow`s still resolves to one instance per back-stack entry, as with `hiltViewModel()` (`getViewModelKey` returns `null` when no key/qualifier, then `provider[vmClass]`).
```kotlin
// feature/session-detail di/SessionDetailModule.kt
val sessionDetailModule = module {
    viewModel { params -> SessionViewModel(get(), get(), params.get()) }   // or (id: String) destructuring (component1 exists)
    viewModelOf(::FeedbackFormViewModel)
}
// SessionDetailRoute.kt (D-10)
@Composable
fun SessionDetailRoute(
    modifier: Modifier = Modifier,
    sessionId: String,
    viewModel: SessionViewModel = koinViewModel { parametersOf(sessionId) },
    ...
)
```
Note the ViewModel constructor order in the repo today is `store, performanceMonitoring, sessionId` (`SessionViewModel.kt:19-21`, `SpeakerViewModel.kt:21-23`); with a lambda definition the order is free, with `viewModelOf(::SessionViewModel)` Koin matches `String` from `parametersOf`. `String` is whitelisted by `verify()` by default.

### Pattern 5: Android startup order and coexistence
**Verified fact (generated Hilt code):** `Hilt_DevFestNantesApplication.onCreate()` is `hiltInternalInject(); super.onCreate();` [VERIFIED: androidApp/build/generated/hilt/component_sources/debug/.../Hilt_DevFestNantesApplication.java:40-45]. The user's `super.onCreate()` at `DevFestNantesApplication.kt:32` therefore runs the Hilt field injection (`timerTree`, dispatcher provider, initializer provider). During coexistence, bridged Hilt providers (D-06) can call into Koin during that injection, so:
```kotlin
override fun onCreate() {
    PerformanceInitializer.startAppStartupTrace()   // stays first (DevFestNantesApplication.kt:30)
    initKoin(androidModules) { androidContext(this@DevFestNantesApplication) }   // BEFORE super.onCreate() while Hilt still exists
    super.onCreate()
    ...
}
```
At the end state (no Hilt) Koin start may stay before `super.onCreate()`; behavior is identical. The `Provider<Set<ApplicationInitializer>>.get()` call inside the coroutine (line 39) maps to a lazy `by inject`.

### Pattern 6: Coexistence bridge (D-06) and Hilt kept applied until the last step
Bridge shape inside `AppModule` companion: `@Provides fun store(): DevFestNantesStore = KoinPlatform.getKoin().get()`. `@Binds` entries convert to `@Provides` bridges. Keep the Hilt plugin (convention) applied to **every** module until the final removal commit: removing annotations per module does not require removing the plugin, and `BookmarksViewModel` is requested by both `AgendaRow` and `SessionLayout` (`hiltViewModel()` at `AgendaRow.kt:43`, `SessionLayout.kt:63`), so it can only flip in a **single 3-module commit** (`core:ui` + `feature:agenda` + `feature:session-detail`) after the features that use it otherwise. D-05's order already places it after the features; this explains why.

### Pattern 7: Initializer list with deterministic order
**Do not use `getAll<ApplicationInitializer>()` for ordered execution.** `Koin.getAll` iterates `InstanceRegistry._instances = safeHashMap<IndexKey, InstanceFactory<*>>()` [VERIFIED: koin-core sources InstanceRegistry.kt:46, Scope.kt:406-409; native impl `ConcurrentMutableMap`], a hash map with no declaration order. Today's order is fixed by `setOf(dataSharingInitializer, openFeedbackInitializer, performanceInitializer)` (`AppModule.kt:82-85`) and is behaviorally significant: `DataSharingInitializer` -> `DataCollectionSettingsServiceImpl.updatesDataServicesActivationStatus()` sets `firebasePerformance.isPerformanceCollectionEnabled` from the user's consent (`DataCollectionSettingsService.kt:110`), and `PerformanceInitializer` then sets `FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true` (`PerformanceInitializer.kt:43`) last. Reordering silently changes consent behavior.
Recommended (satisfies D-11's "or equivalent"):
```kotlin
single(named("applicationInitializers")) {
    listOf<ApplicationInitializer>(get<DataSharingInitializer>(), get<OpenFeedbackInitializer>(), get<PerformanceInitializer>())
}
// DevFestNantesApplication: private val appInitializers: List<ApplicationInitializer> by inject(named("applicationInitializers"))
```
Keep the three initializer classes as `factory`/`singleOf` definitions in `appModule`; they lose `@Inject`.

### Pattern 8: Graph verification (D-12), with the facts that make it work
- `verify()` is **JVM-only** (`jvmMain` in koin-test), reflection-based, needs `kotlin-reflect` (transitive from koin-test-jvm). It lives in Android unit tests (`testDebugUnitTest`) and KMP `jvmTest`. `Module.verify(extraTypes, injections)`; `injections` and `definition<T>(...)` are `@KoinExperimentalAPI` (a warning only; spike compiled with 1 warning).
- It resolves a constructor parameter if its type string occurs in the definition index (including secondary `bind` types), is in `extraTypes`, is `String/Int/Long/Double`, is `@InjectedParam`/`@Provided`, optional, or listed in `injections` [VERIFIED: Verification.kt].
- A module verifies only itself plus its `includes(...)`. For the **full-graph** check build `module { includes(sharedModules + androidModules) }.verify(extraTypes = ...)`: `includes(List)` composite worked in the spike (isolated `b.verify()` threw, `module { includes(listOf(a, b)) }.verify()` passed).
- Android-only `actual` platform modules (e.g. Firebase bindings in `core:analytics` androidMain) are invisible to `jvmTest`. Verify them via the `:androidApp` full-graph test (runs under `testDebugUnitTest`). Opt-in alternative: AGP KMP host tests are **disabled by default**; enable with `kotlin { android { withHostTest { isIncludeAndroidResources = true } } }`, source set `androidHostTest` [CITED: developer.android.com/kotlin/multiplatform/plugin]. Not needed if the app-level test exists.
- Feature modules are plain Android libraries: add `testImplementation(libs.koin.test)` (and the existing `testImplementation(project(":core:testing"))` already comes from `AndroidFeatureConventionPlugin`). Spike: `VenueModuleTest` with `venueModule.verify(extraTypes = listOf(DevFestNantesStore::class))` ran 1/1 green under `:feature:venue:testDebugUnitTest`.
- `KoinTestRule.create { modules(...) }` (koin-test-junit4) closes any existing Koin and stops it after each test [VERIFIED: KoinTestRule.kt]. In `commonTest`/`iosTest` use `stopKoin()` in `@AfterTest` (spike pattern).
- `allowOverride` defaults to `true` [VERIFIED: KoinApplication.kt:40], so a duplicate binding across modules silently overrides and a test override list works by simply listing fakes **last**. Optional hardening: call `allowOverride(false)` in production `initKoin` before `appDeclaration()` so duplicates fail at startup, while tests re-enable it through `appDeclaration`. (`verify()` also prints `definition override detected`.)

### Pattern 9: `:core:testing` fakes (D-14)
- Location: `FakeBookmarksStore`, `FakeAnalyticsService` in **commonMain** (`BookmarksStore` and `AnalyticsService` are commonMain interfaces); `core:testing` needs `api(project(":core:analytics"))` and `api(libs.koin.core)`.
- `PerformanceMonitoring` is an **androidMain-only final Firebase-backed class** (`PerformanceMonitoring.kt:15`: `class PerformanceMonitoring @Inject constructor(private val firebasePerformance: FirebasePerformance)`), whose low-level API returns Firebase `Trace`. A fake therefore needs a seam. Recommended minimal, behavior-neutral refactor: turn `PerformanceMonitoring` into an interface exposing only what callers use (`suspend fun <T> trace(traceName, attributes, block)` and `suspend fun <T> trackDataLoad(traceName, dataSource, itemCount, block)`; the extensions `traceDataLoading` call `trackDataLoad`), keep a `FirebasePerformanceMonitoring` implementation with the current `startTrace/stopTrace/recordMetric` internals, bind it as `single { FirebasePerformanceMonitoring(get()) } bind PerformanceMonitoring::class`. Callers (`SpeakerViewModel.kt:39` uses `trace(`; `SpeakersViewModel.kt:32`, `AgendaViewModel.kt:70`, `SessionViewModel.kt:29` use `traceDataLoading(`) stay source-compatible. A `NoOpPerformanceMonitoring` goes in `core:testing` **androidMain**. (Alternative: make the class `open`; rejected because the fake must still return a Firebase `Trace`.)
- Koin helper: `val testDoublesModule: Module = module { single<AnalyticsService> { FakeAnalyticsService() }; single<BookmarksStore> { FakeBookmarksStore() }; single<DevFestNantesStore> { fakeDevFestNantesStore() } }` listed **after** production modules. Keep `fakeDevFestNantesStore()` (`FakeStores.kt:19`).

### Pattern 10: D-13 gate (git grep, no Gradle)
Put `scripts/di-gate.sh` in the repo (the Phase 3 gates live under `.planning/` and are manual-only per STATE follow-up) and call it from the `checks` job in `.github/workflows/android.yml`. Scope: tracked `*.kt *.kts *.toml *.pro *.xml *.dcl` only, so `licenses.html` (lists "Dagger"/"Hilt" at lines 15/19), docs and `.planning/` never trip it (docs still need updating, see Runtime State Inventory).
- Hilt rule (suggested regex, planner finalizes): `dagger\.|javax\.inject|dagger-hilt|androidx\.hilt|@Hilt[A-Za-z]+|HiltAndroid|com\.google\.dagger|\bhilt\b` (case-sensitive on `@Inject\b|@Provides\b|@Binds\b|@Module\b|@InstallIn\b` too).
- Service-locator rule: **import-based**, because inside `module { }` `get()` is a `Scope` member needing no import, while outside DI files `get()/inject()` cannot compile without one of: `org.koin.core.component.(KoinComponent|inject|get|getKoin)`, `org.koin.android.ext.android.(get|inject|getKoin)`, `org.koin.mp.KoinPlatform`, `org.koin.compose.koinInject`, `org.koin.java.KoinJavaComponent`, `GlobalContext`, plus `\bgetKoin\(`. Allowlist: `**/di/**`, `*Module.kt`, `shared/src/iosMain/**/IosDependencies.kt`, `DevFestNantesApplication.kt`, `MainActivity.kt`. `koinViewModel` is NOT banned (it is the Route-level resolution, D-10).
- Introduce it early with an allowlist containing `AppModule.kt` and the not-yet-migrated files and shrink it per commit; ordering vs tracer is the planner's call (D-13 discretion). The hilt rule can only be turned on at the end.

### Anti-Patterns to Avoid
- `single<Interface> { Impl(get()) }` everywhere: makes `verify()` a no-op for that binding (proven).
- Sprinkling `by inject()` / `koinInject()` in composables or ViewModels (REQUIREMENTS anti-pattern row).
- Relying on `getAll()` order.
- Moving the SharedPreferences to a "cleaner" named file: breaks persisted consent/bookmarks/filters.
- Making `Module` vals public in an exported KMP module without `@HiddenFromObjC`.
- Starting Koin in a Swift view's `init` or per ViewModel (must be once, in `App.init`).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Graph validation | A bespoke reflection scanner | `org.koin.test.verify.verify` + a started-Koin runtime test | Official, proven in spike; handles bound types, includes, parameters |
| Assisted/parameterized VMs | Custom `ViewModelProvider.Factory` (today's `provideFactory`) | `koinViewModel { parametersOf(id) }` | Deletes `AssistedViewModelUtils.kt`, `@EntryPoint`, factories |
| Test Koin lifecycle | Manual start/stop boilerplate in each test | `KoinTestRule` (JUnit4) / `stopKoin()` `@AfterTest` | Prevents "KoinApplication already started" leakage between tests |
| Swift access to DI | A generic Swift `inject<T>()` | Typed `IosDependencies` functions | D-04; avoids reified generics from Swift and Koin types in the header |
| Hiding Kotlin API from Swift | Manual wrappers/`internal` tricks across modules | `@HiddenFromObjC` | `internal` cannot cross the Gradle module boundary between `core:*` and `:shared` |
| Hilt-to-Koin bridging library | `koin-dagger-bridge` | 1-line `@Provides` bridges in `AppModule` | D-06 locked; fewer deps |
| Activity-scoped helper | Koin Android activity scopes | `factory { (activity: Activity) -> ExternalContentService(activity) }` + `by inject { parametersOf(this) }` in `MainActivity` | `activityScope()` requires the Activity to implement `AndroidScopeComponent` and the built-in archetype is `AppCompatActivity` (`AndroidScopeArchetypes.kt`), while `MainActivity` is a plain `ComponentActivity`; avoid the whole scope machinery |

**Key insight:** The migration is mostly mechanical; the risk is in the *semantics Hilt gave for free* (injection timing, set ordering, activity scope, compile-time missing-binding errors) and in *build/CI wiring that silently doesn't run the new checks*.

## Scope-mapping table (D-08, required in the plan)

| Binding | Hilt today (source) | Koin target | Notes |
|---------|---------------------|-------------|-------|
| `AnalyticsService` <- `FirebaseAnalyticsService` | `@AppScope @Binds` | `single { FirebaseAnalyticsService(get()) } bind AnalyticsService::class` | in `core:analytics` androidMain |
| `BookmarksStore` <- `BookmarksStoreImpl` | `@AppScope @Binds` | `single ... bind` | `core:data` androidMain; stateful, MUST be one instance |
| `DataCollectionSettingsService` <- Impl | `@AppScope @Binds` | `single ... bind` | `feature:settings`; stateful |
| `SessionFiltersService` <- Impl | `@AppScope @Binds` | `single ... bind` | `feature:agenda`; also used by `HomeViewModel` |
| `CoroutinesDispatcherProvider` | `@AppScope @Provides` | `single` | `appModule`; values `Dispatchers.Default/IO/Main.immediate` (`AppModule` lambda, `Dispatchers.Main.immediate` evaluated at first request in `onCreate`, same time as today) |
| `Set<ApplicationInitializer>` | `@Provides @ElementsIntoSet` (unscoped), initializers unscoped `@Inject` | ordered `List` definition (Pattern 7), initializers `factory` | one consumer |
| `FirebaseAnalytics`, `FirebasePerformance`, `FirebaseCrashlytics` | unscoped `@Provides` | `factory` (behavior-identical; D-08 default) | Believed process singletons (see A2) but keep `factory` to avoid needing proof; always consumed by `single`s or Timber tree |
| `FirebaseRemoteConfig` | unscoped `@Provides` **with side effect** (`setConfigSettingsAsync`, `REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL = 300L`, `AppModule.kt:63`) | `factory { Firebase.remoteConfig.apply { setConfigSettingsAsync(remoteConfigSettings { minimumFetchIntervalInSeconds = 300 }) } }` | **must stay `factory`**: side effect runs once per `FeedbackFormViewModel` creation as today; bind inside `feature:session-detail` (single consumer) |
| `OpenFeedbackConfig` | unscoped `@Provides` (BuildConfig) | `factory`/`single` in `appModule` (immutable data class) | `OpenFeedbackConfig` lives in `feature:session-detail`; isolation test lists it in `extraTypes` |
| `PerformanceMonitoring` | `@AppScope @Provides` + class `@Singleton` | `single` | stateless wrapper but keep single |
| `SharedPreferences` | `@AppScope @Provides` (`PreferenceManager.getDefaultSharedPreferences(application)`, `AppModule.kt:124`) | `single<SharedPreferences>` in `core:data` androidMain | same file name |
| `DevFestNantesStore` | `@AppScope @Provides` | `single` in `core:data` commonMain (`DevFestNantesStoreBuilder().setUseMockServer(false).build()`) | `GraphQLStore` is `internal` (`GraphQLStore.kt:27`), so the store definition must live in `core:data` |
| `Timber.Tree` | `@AppScope @Provides` (DEBUG ? Debug : Release) | `single<Timber.Tree> { if (BuildConfig.DEBUG) TimberTreeDebug(get()) else TimberTreeRelease(get()) }` | `appModule` |
| `ExternalContentService` | `@ActivityScoped` | `factory { (activity: Activity) -> ... }` resolved once per `MainActivity` instance | one instance per Activity instance, as `@ActivityScoped` |
| `Application` | Hilt-provided | `androidContext(app)` binds `Application` and `Context` [VERIFIED: KoinExt.kt] | `OpenFeedbackInitializer(application)` uses `androidApplication()` |
| All `@HiltViewModel`s | ViewModelStore-scoped | `viewModel { }` (= `factory` + ViewModelStore) | identical |
| `SessionViewModel`, `SpeakerViewModel` | `@AssistedInject` | `viewModel` with `parametersOf` | key = class name, one per ViewModelStoreOwner as with the current `viewModel(owner, factory=)` |

## Runtime State Inventory (migration phase)

| Category | Items Found | Action Required |
|----------|-------------|------------------|
| Stored data | Default `SharedPreferences` file (`PreferenceManager.getDefaultSharedPreferences`) holds `selected_sessions` (`BookmarksStoreImpl.PREF_SELECTED_SESSIONS`), `SHARED_PREFERENCES_KEY_AGENDA_FILTERS`, `SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS` (consent). Apollo SQLite file `apollo.db` (unchanged) | **Code edit only**: the Koin `SharedPreferences` definition must call `PreferenceManager.getDefaultSharedPreferences(androidApplication())` (same file). No data migration. Add `androidx.preference` to `core:data` androidMain |
| Live service config | Firebase console (Analytics/Perf/Crashlytics/Remote Config keys `openfeedback_enabled`, `openfeedback_fallback_requested_android`): no DI involvement | None (verified by reading `FeedbackFormViewModel.kt`) |
| OS-registered state | None (no WorkManager/alarms/launchd; `grep` for `WorkManager` found no DI usage; Application class name unchanged in the manifest) | None. Keep the Application class name `DevFestNantesApplication` |
| Secrets/env vars | `OPEN_FEEDBACK_*` BuildConfig fields from `local.properties`/secrets plugin; `google-services` config | None. Values keep being read only in `:androidApp` (`appModule` / `OpenFeedbackInitializer`) |
| Build artifacts / installed | Stale Hilt/KSP output under `*/build/generated/hilt`, `ksp` caches, `.gradle`, `.kotlin` (pre-existing Hilt generated sources exist in `androidApp/build`) | `./gradlew clean` before trusting compile errors after Hilt removal (Pitfall 5 in research); CI uses fresh checkout. **Doc strings:** README.md:39, CONTRIBUTING.md:173/178, `.github/copilot-instructions.md:23`, `documentation/ARCHITECTURE.md:89,106,163,212`, `documentation/PERFORMANCE_MONITORING.md:61,248,417,439` mention Hilt; update them in the Hilt-removal commit (SC-1 says "anywhere in the codebase"). `androidApp/src/main/assets/licenses.html` lists Dagger/Hilt as third-party libs: remove those entries and add Koin when Hilt is gone |

## Common Pitfalls

### Pitfall 1: Koin types leak into `shared.h`
**What goes wrong:** Public `val xModule: Module` in an `export()`-ed module (`core:data`, `core:analytics`) drags `Koin_coreModule` and friends into the Swift API, bloating the header and tripping `swift-names-gate.sh`.
**Why:** K/N exports every public declaration of exported modules (`shared/build.gradle.kts:10-12` exports model/data/analytics).
**How to avoid:** `@HiddenFromObjC` on `initKoin`, `sharedModules`, every public `Module` val (expect **and** actual). Re-grep `shared.h` for `Koin` after each core module step.
**Warning signs:** `grep -c -i koin shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h` returns more than the helper's own names.

### Pitfall 2: `verify()` passes vacuously
**What goes wrong:** Missing dependency not detected for `single<Interface> { Impl(get()) }`.
**How to avoid:** Pattern 1; add one deliberately-failing negative test in the app graph test (remove a binding, expect `MissingKoinDefinitionException`) so the gate is proven to bite.

### Pitfall 3: CI does not run the new tests
**What goes wrong:** `android.yml` unit-tests job runs only `./gradlew testDebugUnitTest` (`android.yml:46`). A dry-run shows it covers `:androidApp`, `:core:ui`, and `:feature:*` only; KMP modules (`core:data`, `core:model`, `core:testing`, `:shared`) have `jvmTest`/`commonTest` that are **not** triggered (so the existing `DevFestNantesStoreContractTest` is not CI-run today either) [VERIFIED: `./gradlew testDebugUnitTest --dry-run` in spike, task list]. `ios.yml` runs `:shared:embedAndSignAppleFrameworkForXcode` and `xcodebuild ... build` (`ios.yml:128`, `:208`) and no tests.
**How to avoid:** Change to `./gradlew testDebugUnitTest jvmTest` (ubuntu) and add `./gradlew :shared:iosSimulatorArm64Test` to `ios.yml` (macos). D-12's "existing jobs" holds, but each job needs one added command; this is not a new job. `iosX64Test` is disabled on Apple silicon (`w: Native task 'iosX64Test' is disabled`), irrelevant.
**Warning signs:** a green CI that never prints the Koin `verify` log lines.

### Pitfall 4: Initializer order
See Pattern 7. Also `Provider.get()` timing: resolve inside the coroutine as today.

### Pitfall 5: Koin start vs Hilt injection timing
See Pattern 5. Symptom if wrong: `KoinApplication has not been started` thrown from an `AppModule` bridge during `super.onCreate()`.

### Pitfall 6: Missing `actual` on jvm
Every `expect val ...PlatformModule` needs android + jvm + ios actuals (3 files for `core:data`, 3 for `core:analytics`, 3 for any other expect). Proven failure above. Prefer small per-target files.

### Pitfall 7: `:androidApp` has no `:shared` dependency
`androidApp/build.gradle.kts` contains no `:shared` reference (Phase 3 03-09 removed it). `initKoin` lives in `:shared`, so re-add `implementation(project(":shared"))`. Its Android variant exposes only `InitKoin.kt`; no framework link. Do it in the tracer commit. Also: `:shared` commonMain now needs `implementation(libs.koin.core)`, and any `core:*` build file that declares Koin modules needs `implementation(libs.koin.core)` (+ `koin-android` in `androidMain`).

### Pitfall 8: KSP must stay
`core/data/build.gradle.kts:3-4` applies `alias(libs.plugins.ksp)` and `alias(libs.plugins.kmp.native.coroutines)` (kmp-native-coroutines requires KSP). D-05's "removes ... the Hilt plugin/KSP/deps" must be read as removing the **Hilt** KSP *dependencies* (`ksp(dagger-hilt-compiler)`, `kspAndroidTest`, `kspTest`) and the KSP plugin application in `AndroidHiltConventionPlugin`, not the catalog `ksp` plugin or root `alias(libs.plugins.ksp) apply false`. Also remove the `kotlin-metadata-jvm` force in `DependencyResolutionRules.kt:23` (Hilt-specific, per its own comment) but **keep** the `firebase-auth-ktx` substitution in the same file.

### Pitfall 9: Dead Swift test target
`iosApp/DevFest NantesTests/MockDevFestNantesStore.swift` is entirely commented out and `DevFest_NantesTests.swift:15,24` reference `DevFestViewModel`, which exists nowhere in `iosApp/` [VERIFIED: grep]. CI builds scheme `iosApp` with `build` only. D-04's rationale ("MockDevFestNantesStore test path keeps working") does not hold; the injectable `init(store:)` is still a good seam. Do not spend effort "keeping the tests working"; flag to the user as an observation.

### Pitfall 10: D-02 premise nuance
`val apolloClient = ApolloClient.Builder()...build()` is a top-level global in `core:network` (`Apollo.kt:38`) and `GraphQLStore` just wraps it (`GraphQLStore.kt:27`, `internal class GraphQLStore(private val apolloClient: ApolloClient)`). iOS stores already share one Apollo client and one normalized cache; the singleton changes only how many thin wrapper objects exist. Record this honestly in the plan/summary; risk is lower than D-02's wording suggests. (Android already used one store.)

### Pitfall 11: Runtime start-up test and Firebase types
`FeedbackFormViewModel` calls `remoteConfig.fetchAndActivate()` in `init` (`FeedbackFormViewModel.kt:29-31`) and takes a final-ish `FirebaseRemoteConfig`; it cannot be constructed with a hand-made fake and cannot be resolved on a bare JVM without Firebase being initialized. See Open Question 2.

### Pitfall 12: Transitive androidx uplifts
Koin brings `activity 1.12.4`, `appcompat 1.7.1`, `fragment-ktx 1.8.9`, `lifecycle-viewmodel-ktx 2.10.0`; the catalog `activity = "1.9.2"` becomes misleading. Consider bumping the catalog entry to match so the pin states the truth, and run both smoke checkpoints.

### Pitfall 13: R8/minified release
Release uses `isMinifyEnabled = true` (`androidApp/build.gradle.kts`), CI builds debug only. Koin's DSL has no reflection at runtime (`verify()` reflection is test-only), so no keep rules are expected [ASSUMED]; run `./gradlew :androidApp:assembleRelease` plus a launch of the release (or at least `lintRelease`) in the final checkpoint, as Phase 2 did locally.

### Pitfall 14: `Hilt` doc/asset strings
See Runtime State Inventory.

## Code Examples

### Spike-proven `:shared` verify + start tests
```kotlin
// Source: spike (scratchpad/spike/shared/src/jvmTest ...KoinGraphTest.kt), 2/2 green on Kotlin 2.4.20
class KoinGraphTest {
    @AfterTest fun tearDown() = stopKoin()
    @Test fun dataModuleVerifies() { dataModule.verify() }
    @Test fun startsAndResolvesStore() {
        initKoin()
        assertNotNull(KoinPlatform.getKoin().get<DevFestNantesStore>())
    }
}
```
### Spike-proven iOS runtime test
```kotlin
// Source: spike shared/src/iosTest ... IosKoinTest.kt: 1 test, 0 failures on iosSimulatorArm64 (Xcode 27, local)
class IosKoinTest {
    @AfterTest fun tearDown() = stopKoin()
    @Test fun initKoinResolvesStoreThroughIosDependencies() {
        initKoinIos()   // in the final shape: IosDependencies.start()
        assertNotNull(IosDependencies.store())
    }
}
```
Needed deps in `shared/build.gradle.kts`: `commonMain: implementation(libs.koin.core)`, `iosTest: implementation(libs.koin.core)`, `jvmTest: implementation(libs.koin.test)`. The existing convention already links `-lsqlite3` for native test executables (`KmpLibraryConventionPlugin.kt` TestExecutable block) which the store's Apollo SQLite cache needs.
### Feature module test and module (Android library, 1/1 green in spike)
```kotlin
class VenueModuleTest {
    @Test fun venueModuleVerifies() {
        venueModule.verify(extraTypes = listOf(DevFestNantesStore::class))
    }
}
```
### `MainActivity` entry points (D-11)
```kotlin
class MainActivity : ComponentActivity(), NavController.OnDestinationChangedListener {
    private val analyticsService: AnalyticsService by inject()
    private val externalContentService: ExternalContentService by inject { parametersOf(this) }
```
(`org.koin.android.ext.android.inject`; `parametersOf` from `org.koin.core.parameter`.) In the isolation/full-graph `verify()` add `Activity::class` (and `Application::class`, `Context::class`, `SharedPreferences::class`, Firebase types) to `extraTypes`.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `checkModules()` | `verify()` (JVM) + runtime start test | `checkModules` deprecated (Koin 4.0, still `@Deprecated` in 4.2.2) | Use `verify()`; SC-5 wording reinterpreted by D-12 |
| `org.koin.androidx.viewmodel.dsl.viewModel` | `org.koin.core.module.dsl.viewModel/viewModelOf` (koin-core-viewmodel) | Koin 4.x | One DSL for KMP + Android |
| `koinViewModel` in `org.koin.androidx.compose` | `org.koin.compose.viewmodel.koinViewModel` | Koin 4.x (sources: the compose-viewmodel one says "Ported directly from Android side") | Both exist with identical signature; standardize on the latter |
| `KoinAndroidContext` | not needed | Koin 4.x (`@Deprecated("KoinAndroidContext is not needed anymore...")`) | Compose uses the `startKoin` context |
| Koin 4.2.0-RC (research said avoid) | 4.2.2 GA | 4.2.0 GA, 4.2.1, 4.2.2 (2026-06-15) | Safe to pin 4.2.2; compiler plugin remains out of scope (DI-V2-01) |

**Deprecated/outdated:** `org.koin.androidx.compose.KoinAndroidContext`; `checkModules`/`checkKoinModules`; hilt-navigation-compose (removed with Hilt).

## Spike evidence (scratch copy, not committed)

A copy of the repo (excluding `.git`, `.planning`, `iosApp`) under the session scratchpad was modified and built with the repo's own wrapper (Gradle 9.7.1, default JDK 25, Kotlin 2.4.20, AGP 9.4.0, Xcode 27). Results: `:shared:jvmTest` 2/2 + 4/4 (verify semantics) green; `:shared:linkDebugFrameworkIosSimulatorArm64` green; `:shared:iosSimulatorArm64Test` 1/1 green (Koin on K/N started and resolved the store); `:core:data:compileAndroidMain` and `:core:data:jvmTest` green with an android actual; `:feature:venue:testDebugUnitTest` 1/1 green with `koin-androidx-compose` and `koinViewModel()` replacing `hiltViewModel()` (Hilt plugin still applied to the module). Not exercised: `:androidApp` assemble/run, R8 release, CI runners, Robolectric.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `Firebase.analytics`, `FirebasePerformance.getInstance()`, `FirebaseCrashlytics.getInstance()` are process singletons (so `single` would also be identical) | Scope-mapping table | None if plan keeps `factory` (recommended); only matters if someone switches to `single` |
| A2 | Koin DSL needs no R8 keep rules | Pitfall 13 | Release crash. Mitigation: final `assembleRelease` + launch |
| A3 | AGP KMP host-test task is named `testAndroidHostTest` | Pattern 8 | Only if the opt-in alternative is chosen; harmless otherwise |
| A4 | `:shared:iosSimulatorArm64Test` finds/boots a simulator on the GitHub `macos-latest` image without extra config (it did locally with Xcode 27) | Pitfall 3 | iOS CI red on first push of the tracer; mitigation: reuse the existing "Resolve latest plain iPhone simulator" step output if the task needs a device name |
| A5 | `FirebaseRemoteConfig`/`FirebasePerformance`/`Trace` cannot be instantiated or faked on a plain JVM unit test without a mock library or Firebase init | Pitfall 11, Pattern 9 | If wrong, the runtime test is simpler (no Robolectric/Mockito) |
| A6 | Robolectric 4.17 can initialize `FirebaseApp` and the Firebase singletons used by the graph under API 36/37 | Open Question 2 | Runtime test falls back to Mockito-inline or a documented gap |
| A7 | The koin-androidx-compose/koin-android transitive androidx uplifts (activity 1.12.4, lifecycle 2.10.0) do not regress runtime behavior | Pitfall 12 | Caught only by the Android smoke checkpoints |
| A8 | `ActivityScopeArchetype` / `activityScope()` is unsuitable for a plain `ComponentActivity` `MainActivity` | Don't Hand-Roll | Low; the recommended `parametersOf(this)` path avoids scopes entirely |

## Open Questions

1. **Do `core:model` and `core:network` need a Koin module (DI-02 literalness)?**
   - Known: `core:model` has no bindings. `core:network` exposes only the global `val apolloClient` (`Apollo.kt:38`), consumed by the `internal` `GraphQLStore` inside `core:data`.
   - Unclear: whether DI-02 ("each module exposes its own module") must be satisfied literally.
   - Recommendation: `core:network` declares `networkModule = module { single { apolloClient } }` (cheap, literal DI-02, lets tests override the client later); `dataModule` keeps the locked `DevFestNantesStoreBuilder` wrapping. `core:model` gets none and the plan documents the exception ("no bindings"). If the user wants strictness, add an empty `modelModule`; do not.

2. **How does the runtime start-up test (D-12 layer 2) get Firebase-typed dependencies on the JVM?**
   - Known: `FeedbackFormViewModel` needs `FirebaseRemoteConfig` and calls it in `init`; `DataCollectionSettingsServiceImpl` and `FirebaseAnalyticsService` take Firebase types in constructors (only stored, not called).
   - Unclear: Robolectric + `FirebaseApp.initializeApp` vs Mockito-inline for the Firebase types vs excluding `FeedbackFormViewModel` from runtime resolution.
   - Recommendation: Wave 0 spike with Robolectric 4.17 first (already has `isIncludeAndroidResources = true` in `AndroidCommon.kt`; needs `--add-opens` JVM args). Fallback: a single test-only Mockito-inline dependency scoped to Firebase SDK types (the "no mocking framework by default" out-of-scope row is about project style, not a ban on stubbing third-party finals). Last resort: exclude that one ViewModel from the runtime test and rely on `verify()`, documented as a known gap. Put the choice behind a `checkpoint:human-verify` since both add a dependency.

3. **Where do `FirebaseCrashlytics` and `FirebaseRemoteConfig` bindings live?**
   - Recommendation: `FirebaseAnalytics`, `FirebasePerformance`, `FirebaseCrashlytics` in `core:analytics` androidMain (multiple consumers: settings service, Timber trees, analytics service, monitoring); this adds `implementation(libs.firebase.crashlytics.ktx)` to its `androidMain`. `FirebaseRemoteConfig` in `feature:session-detail` (single consumer, `OpenFeedback` flag side effect stays local). This reads D-07's "as consumed" literally.

4. **`PerformanceMonitoring` seam shape.** Recommendation in Pattern 9 (interface with `trace` and `trackDataLoad`). Needs the user's nod only because it edits `core:analytics` production code (behavior-neutral).

5. **D-13 gate timing.** Recommendation: land the Hilt rule + service-locator rule script right after the tracer with a shrinking allowlist (cheap, and it prevents regressions during the migration), and flip the Hilt rule to strict in the last commit.

6. **Swift tests and `swift-names-gate.sh` baseline.** Additive names (`IosDependencies`, its `start()`/`store()`) do not trigger `SWIFT-NAME-MISSING`, but `03-swift-referenced-names.txt` and the members baseline regenerate (`baseline` mode) once Swift references the new type. Run the gate in `check` and `baseline` as part of the iOS step.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK | Gradle | yes | OpenJDK 25.0.3 default (CI uses 17); built fine | JDK 17 as in CI |
| Gradle wrapper | build | yes | 9.7.1 (spike used it) | - |
| Xcode / iOS simulators | iOS tests, link, smoke | yes | Xcode 27.0, iOS 18-27 runtimes, iPhone 18 Pro available | - |
| Android SDK | Android compile/tests | yes | `/Users/robin/Development/Android/sdk` | - |
| `android` CLI | smoke checkpoints | yes | 1.0.16261425 (update available) | `./gradlew` + `adb` |
| Emulator | smoke checkpoints | yes | AVD `Pixel_9` | - |
| swiftgen, jq, rg, node | iOS CI parity, gate scripts | yes | node v24.15.0 | - |
| Network (Maven Central) | Koin resolution | yes | verified | - |

**Missing dependencies with no fallback:** none.
**Missing dependencies with fallback:** none.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit4 (Android `testDebugUnitTest`), `kotlin.test` (KMP common/jvm/ios), Koin `koin-test` 4.2.2 `verify()`, optional `koin-test-junit4` `KoinTestRule` |
| Config file | none (convention plugins `AndroidCommon.kt`, `KmpLibraryConventionPlugin.kt`); `isReturnDefaultValues = true`, `isIncludeAndroidResources = true` already set |
| Quick run command | `./gradlew :feature:venue:testDebugUnitTest :shared:jvmTest` |
| Full suite command | `./gradlew testDebugUnitTest jvmTest detekt lint` (+ `./gradlew :shared:iosSimulatorArm64Test` on macOS) |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| DI-01 | No Hilt anywhere; app builds | gate + build | `bash scripts/di-gate.sh && ./gradlew clean assembleDebug assembleRelease` | Wave 0 |
| DI-02 | Each module exposes its module; composed by `initKoin()` | unit (verify per module + composite) | `./gradlew testDebugUnitTest jvmTest` | Wave 0 (`feature/*/src/test`, `androidApp/src/test`, `shared/src/jvmTest`) |
| DI-03 | Platform deps provided by actuals | unit (full-graph verify with `extraTypes`) + iOS start test | `./gradlew :androidApp:testDebugUnitTest :shared:iosSimulatorArm64Test` | Wave 0 |
| DI-04 | Constructor injection only; no locator calls outside wiring | script gate | `bash scripts/di-gate.sh` | Wave 0 |
| DI-05 | `core:testing` fakes usable across modules | unit | `./gradlew :core:testing:jvmTest` (+ a feature test importing the fakes) | partial (`FakeStoresTest.kt` exists) |
| DI-06 | Koin graph verification in CI | CI wiring | workflow diff: `testDebugUnitTest jvmTest` in android.yml and `:shared:iosSimulatorArm64Test` in ios.yml | Wave 0 |

### Sampling Rate
- **Per task commit:** the touched module's `testDebugUnitTest`/`jvmTest` + `bash scripts/di-gate.sh`
- **Per wave merge:** `./gradlew testDebugUnitTest jvmTest detekt lint` and `:shared:linkDebugFrameworkIosSimulatorArm64` + `swift-names-gate.sh check`
- **Phase gate:** full suite green on both CI workflows, clean build, `assembleRelease`, Android + iOS smoke checkpoints

### Wave 0 Gaps
- [ ] Catalog + convention: `koin-*` entries, `AndroidKoinConventionPlugin` (or fold into feature/library plugins), `koin-test` for `testImplementation`
- [ ] `feature/<x>/src/test/java/.../di/<X>ModuleTest.kt` for each of 6 features; `core/ui` module test
- [ ] `shared/src/jvmTest` (verify + start) and `shared/src/iosTest` (start + store via `IosDependencies`)
- [ ] `androidApp/src/test` full-graph `verify()` (+ negative proof test) and runtime start-up test (needs Open Question 2 decision)
- [ ] `core:testing` fakes + Koin helper (+ `core:analytics` dependency, Koin dependency)
- [ ] `scripts/di-gate.sh` + workflow step; `android.yml` command change; `ios.yml` test step

## Security Domain

`security_enforcement` is enabled (absent = enabled; config shows `true`, ASVS level 1).

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V1 Architecture / V14 Config & Dependencies | yes | New third-party dependency (Koin): official groupId, single version ref, human-verify checkpoint at first add; dependabot already covers Gradle |
| V2/V3/V4 Auth, Session, Access Control | no | No auth surface touched |
| V5 Input Validation | no | No new input paths; nav args unchanged (`!!` deliberately untouched) |
| V6 Cryptography | no | None |
| V7 Error handling & logging | yes | Do not enable Koin's DEBUG logger in release builds (it logs definitions); default is no logger. If `androidLogger()` is used, gate on `BuildConfig.DEBUG` |
| V8 Data Protection | yes | User consent (`ENABLED_DATA_COLLECTION_TOOLS`) must keep gating Crashlytics/Perf/Analytics: the initializer order (Pattern 7) and same-prefs-file rule protect this |
| V10 Malicious code / supply chain | yes | `OPEN_FEEDBACK_*` secrets stay in BuildConfig read only in `:androidApp`; CI gate must not echo them |

### Known Threat Patterns for this stack
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Test fakes overriding production bindings at runtime | Tampering | Test override modules live in `core:testing` only (test deps), `initKoin` never references them; optional `allowOverride(false)` in production |
| Service-locator escape hatches exposing the whole graph (`getKoin().get()` anywhere) | Elevation | D-13 gate with a small allowlist |
| Consent bypass through changed initializer order / separate preference file | Information disclosure | Ordered list + same default SharedPreferences + test asserting order |
| Secret leakage via Koin logs or `verify()` output | Information disclosure | No logger in release; `verify()` output only in tests |

## Sources

### Primary (HIGH confidence)
- Maven Central: `maven-metadata.xml` and `*.module`/`*.pom`/`*-sources.jar` for `io.insert-koin:{koin-core, koin-android, koin-androidx-compose, koin-compose, koin-compose-viewmodel, koin-core-viewmodel, koin-test, koin-test-jvm, koin-test-junit4, koin-bom}:4.2.2` (read: `Verification.kt`, `VerifyModule.kt`, `CheckModules.kt`, `KoinTestRule.kt`, `ViewModel.kt`, `GetViewModel.kt`, `ModuleExt.kt`, `KoinExt.kt`, `ComponentActivityExt.kt`, `InstanceRegistry.kt`, `KoinApplication.kt`)
- Spike builds in a scratch copy of this repo (results above)
- In-repo files read this session (paths cited inline), including generated `Hilt_DevFestNantesApplication.java`

### Secondary (MEDIUM confidence)
- insert-koin.io docs: `docs/reference/koin-test/verify/` (verify, `checkModules` deprecated since 4.0), `docs/reference/koin-compose/compose/`, `docs/reference/koin-android/viewmodel/` [CITED]
- github.com/InsertKoinIO/koin/releases (4.2.0-4.2.2 notes: Kotlin 2.3.20, minSdk 23, ViewModel scope declaration note) [CITED]
- developer.android.com/kotlin/multiplatform/plugin (AGP KMP `withHostTest`/`withDeviceTest`, source sets `androidHostTest`/`androidDeviceTest`) [CITED]
- github.com/robolectric/robolectric/releases/tag/robolectric-4.17 (API 37 support, `--add-opens`) [CITED]

### Tertiary (LOW confidence)
- None relied on for recommendations; assumptions are enumerated in the Assumptions Log.

## Project Constraints (from CLAUDE.md)

- `CLAUDE.md` is `@AGENTS.md`. Directives that bind this phase: prefer the `android` CLI (`android run`, `android layout`, `android screen capture`, `android docs search`, `android describe`, `android emulator`) over raw `adb`/`gradlew` for Android-specific tasks (build gradle tasks are still fine for CI parity); prefer `gh` for GitHub data/actions; community Android skills are optional and not part of the repo.
- User global `RTK.md`: shell commands are transparently rewritten by `rtk`; use `rtk proxy <cmd>` for raw output when debugging (observed in this session: `grep` output is summarized, use `/usr/bin/grep`).
- Memory: commit/push finished artifacts promptly because rate limits cut agents off mid-run.
- Project constraints in PROJECT.md/REQUIREMENTS.md carried by CONTEXT: no user-visible regression, KMP rule, GitHub Actions CI, Koin `get()` service-locator calls are an explicit anti-pattern.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH, versions read from Maven Central this session; compile/link/test proven by spike on the real toolchain.
- Architecture: HIGH for the shared/iOS/KMP parts (spike), MEDIUM for Android app-level wiring (not built end to end: `:androidApp` assemble, Application bootstrap, Robolectric).
- Pitfalls: HIGH for items marked VERIFIED; MEDIUM for R8, CI simulator selection and androidx uplift runtime effects.

**Research date:** 2026-10-02
**Valid until:** 2026-11-01 (Koin 4.2.x is stable; re-check `maven-metadata.xml` if a 4.3/5.0 appears before execution)
