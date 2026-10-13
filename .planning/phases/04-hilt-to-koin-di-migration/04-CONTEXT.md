# Phase 4: Hilt to Koin DI Migration - Context

**Gathered:** 2026-09-28
**Status:** Ready for planning

<domain>
## Phase Boundary

Dagger Hilt is replaced by Koin as the sole DI framework, on **both Android and iOS**. Each `:core:*`/`:feature:*` Gradle module declares its own Koin `module { }` (plain DSL), composed through a single `initKoin()` entry point living in the `:shared` umbrella and called by both `:androidApp` and the Swift app. Platform-specific bindings (Android `Context`, `SharedPreferences`, Firebase) come from per-module platform modules. ViewModels and Stores use constructor injection only. `:core:testing` gains the DI-focused shared fakes, Koin graph verification runs in CI, and a scripted CI gate keeps Hilt and service-locator calls from coming back.

Pure technical refactor: zero user-visible behavior change on Android and iOS (one deliberate, invisible exception: iOS store becomes a singleton, see D-02).

**Not in this phase:** Koin Annotations / compiler plugin (DI-V2-01), Navigation 3 migration or route/argument format changes, broad ViewModel/Store test coverage (Phase 5, TEST-01..04), `StoreStubs` RNG seeding / `SimpleDateFormat` fixes (Phase 5), drive-by behavior fixes (`println`→Timber, `!!` nav args, `init {}` error handling).

</domain>

<decisions>
## Implementation Decisions

### iOS & initKoin()
- **D-01:** iOS **adopts Koin**: the Swift `App` init calls the Kotlin `initKoin()`, and Swift gets its dependencies from Koin instead of calling `DevFestNantesStoreBuilder()` directly. One composition root on both platforms (roadmap SC-2 "single `initKoin()`", PROJECT.md KMP constraint).
- **D-02:** iOS gets **one shared `DevFestNantesStore` singleton** (today every Swift `BaseViewModel` builds its own store, i.e. its own Apollo client + normalized cache). This aligns iOS with Android's `@AppScope` store. It's a deliberate, user-approved change with no visible effect; record it as such, not as a regression.
- **D-03:** `initKoin()` and the composed module list live in **`:shared`** (the umbrella, the only module that already sees every KMP module). Shape: `:shared` commonMain `initKoin(platformModules: List<Module> = emptyList(), appDeclaration: KoinAppDeclaration = {})` listing all KMP core modules; `:androidApp` calls it from `Application.onCreate` passing its Android-only modules (core `androidMain` platform modules are reached via expect/actual, feature modules and the app module are passed in); `:shared` iosMain provides the iOS-facing helpers. — **Reversibility:** reversible. Moving the entry point into a new `:core:di` module later is a local change.
  - **Amendment to Phase 3 D-10:** `:shared` is no longer limited to `SharedFrameworkPlaceholder.kt`; it now also holds the DI composition root (commonMain `initKoin`) and the iosMain Swift helper. Nothing else goes in `:shared`. The placeholder file can be deleted once real sources exist (planner verifies NO-SOURCE no longer applies).
- **D-04:** Swift accesses Koin through a **typed Kotlin helper** in `:shared` iosMain, e.g. `object IosDependencies : KoinComponent { fun store(): DevFestNantesStore = get() }`, plus an iOS-callable `doInitKoin()`/`initKoinIos()` wrapper. Swift `BaseViewModel` becomes `init(store: DevFestNantesStore = IosDependencies.shared.store())` so the existing `MockDevFestNantesStore` test path in `iosApp/DevFest NantesTests` keeps working. No generic Swift `inject<T>()` extension. These are **additive** Swift-visible names; Phase 3 D-11 (existing Swift names stable) still holds, and `swift-names-gate.sh`'s baseline is updated once for the added symbols. Koin/`KoinComponent` types must not leak into Swift beyond this helper, so check `shared.h` the same way Phase 3 did.

### Migration sequencing
- **D-05:** **Tracer first, then bottom-up coexistence.** Tracer plan: add Koin to the version catalog and convention plugins, write `initKoin()` in `:shared`, start Koin in `DevFestNantesApplication` **alongside** Hilt, migrate the core bindings needed by one leaf feature plus that feature (**`:feature:venue`**) to `koinViewModel()`, and switch iOS to `initKoin()` + `IosDependencies` (D-01/D-04), proven by iOS CI green + a local simulator smoke run. If Koin in the K/N umbrella misbehaves, stop and rethink before more modules move. Then migrate bottom-up, **one commit per module**, full Android + iOS CI green per pushed step (Phase 3 D-18 style): core modules → features least-coupled first (venue → about → settings → speakers → agenda → session-detail), `:core:ui` `BookmarksViewModel` and `:androidApp` `HomeViewModel`/entry points next. The last step removes `AppModule`, `AppScope`, `AssistedViewModelUtils.kt`, the Hilt plugin/KSP/deps, `AndroidHiltConventionPlugin`, the Hilt dependency-resolution rules, and the catalog entries, then does a clean build. One phase branch/PR. — **Reversibility:** reversible. Each step is its own commit.
- **D-06:** During coexistence, **Koin owns every shared singleton** (store, `SharedPreferences`, `AnalyticsService`, `PerformanceMonitoring`, `BookmarksStore`, services). Hilt's `AppModule` `@Provides` for a migrated binding become temporary one-line bridges to Koin (`KoinPlatform.getKoin().get<T>()` or the Koin 4 equivalent), so Hilt ViewModels that haven't moved yet share the **same instance** and the app never holds two copies. Bridges shrink as features migrate and disappear with `AppModule`. These temporary `get()` calls are allowed only inside `AppModule` (it is DI wiring).
- **D-07:** **Android-only bindings live in the owning module's `androidMain`**, exposed via an expect/actual platform module (e.g. `expect val analyticsPlatformModule: Module`). `:core:analytics` androidMain binds `FirebaseAnalyticsService` → `AnalyticsService`, `PerformanceMonitoring` and the Firebase instances it needs (Analytics, Performance, Crashlytics, RemoteConfig as consumed). `:core:data` androidMain binds `BookmarksStoreImpl` → `BookmarksStore` and `SharedPreferences` (via `androidContext()`). Android-only feature modules bind their own services (`:feature:agenda` → `SessionFiltersService`; `:feature:settings` → `DataCollectionSettingsService`; `:feature:session-detail` owns its ViewModels, but its `OpenFeedbackConfig` value comes from the app). `:androidApp` keeps **only** what needs `BuildConfig` or is app-shell: `OpenFeedbackConfig`, Timber tree selection, the `ApplicationInitializer`s, `CoroutinesDispatcherProvider` if it stays in the app, `ExternalContentService`, `HomeViewModel`. iOS actuals bind the iOS equivalents, or nothing where iOS has no implementation today (don't invent new iOS features).
- **D-08:** Hilt-scope → Koin mapping is **behavior-preserving**: `@AppScope`/`@Singleton` → `single`; unscoped `@Provides` → `factory` unless the planner proves `single` is observably identical (e.g. `Firebase.analytics` is already a process singleton). Keep `Firebase.remoteConfig`'s `setConfigSettingsAsync(minimumFetchInterval = 300)` side effect timing equivalent. Produce an explicit scope-mapping table (research Pitfall 5) in the plan.

### Parameterized ViewModels
- **D-09:** `SessionViewModel(sessionId)` and `SpeakerViewModel(speakerId)` switch from Hilt `@AssistedInject` to **Koin parameters**: `viewModel { (id: String) -> SessionViewModel(id, get(), get()) }` (or `viewModelOf` equivalent), resolved with `koinViewModel { parametersOf(sessionId) }`. The id stays a plain constructor parameter. Delete `@AssistedFactory`, the `provideFactory` companions, `MainActivity.ViewModelFactoryProvider` `@EntryPoint`, and `androidApp/.../utils/AssistedViewModelUtils.kt`. No `SavedStateHandle` nav-arg reading, because features stay navigation-agnostic (Phase 3 D-07).
- **D-10:** `koinViewModel()` is called **inside the feature `*Route`**, as every other screen already does with `hiltViewModel()` default params. `SessionDetailRoute`/`SpeakerDetailRoute` change to take `sessionId: String`/`speakerId: String` and resolve the ViewModel themselves (an optional `viewModel` param with a `koinViewModel { parametersOf(id) }` default is fine for tests and previews). `MainActivity` passes only the id and the callbacks, the Nav3-ready shape. ViewModel scoping (per `NavBackStackEntry` / `ViewModelStoreOwner`) must stay identical to today's `hiltViewModel()` behavior, including `BookmarksViewModel` being requested from `AgendaRow` and `SessionLayout`.
- **D-11:** **`by inject()` is allowed only in Android framework entry points**, `DevFestNantesApplication` and `MainActivity`, since they are framework-created. The `Set<ApplicationInitializer>` multibinding becomes individual `single { … } bind ApplicationInitializer::class` definitions (or equivalent) collected with `getKoin().getAll<ApplicationInitializer>()` in `Application`. Keep initializer execution order and the "start performance trace before `super.onCreate()`" behavior identical. No other class calls `get()`/`inject()`/implements `KoinComponent` except the iOS helper (D-04) and the temporary `AppModule` bridges (D-06).

### Graph verification & guards
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

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Scope & requirements
- `.planning/ROADMAP.md` §"Phase 4: Hilt to Koin DI Migration": goal and 5 success criteria (SC-5 wording reinterpreted by D-12)
- `.planning/REQUIREMENTS.md` §"DI (Migration Hilt → Koin)": DI-01..DI-06; §v2 DI-V2-01 (annotations deferred); anti-pattern table row "Appels Koin `get()` en style service-locator"
- `.planning/PROJECT.md` §Constraints: `android` CLI usage, no-regression rule, KMP rule, GitHub Actions

### Prior phase decisions that constrain this phase
- `.planning/phases/03-multi-module-architecture-extraction/03-CONTEXT.md`: D-03 (service placement), D-07 (callback-only feature routes, no NavController in features), D-10 + amendment (`:shared` umbrella; amended again by this phase's D-03), D-11 + amendment (Swift names stable, export rules), D-18 (one PR, one commit per step, CI green per step), D-19 (`:core:testing` scope)
- `.planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh`: Swift/ObjC header gate, which must stay green; its baseline gets updated for the additive iOS helper symbols (D-04)

### Research
- `.planning/research/SUMMARY.md`: Phase 4 rationale; "plain Koin `module { }` DSL, not Annotations/KSP"
- `.planning/research/PITFALLS.md`: Pitfall 5 (stale Hilt code / scope-semantics gaps → clean build + scope-mapping table), Pitfall 6 (runtime-only wiring failures → graph verification gate)
- `.planning/research/ARCHITECTURE.md`: Pattern 2 (Koin per-module wiring)
- `.planning/research/STACK.md`: Koin setup pattern and version notes (re-verify versions)

### Codebase maps
- `.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/CONVENTIONS.md`, `.planning/codebase/TESTING.md`: pre-Phase-3 snapshot, so module paths are outdated but patterns still apply

</canonical_refs>

<code_context>
## Existing Code Insights

### Hilt surface to dismantle
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt`: the only Hilt `@Module`. It has `@Binds` for AnalyticsService, BookmarksStore, DataCollectionSettingsService and SessionFiltersService, and `@Provides` for the dispatcher provider, the initializer set (`@ElementsIntoSet`), the Firebase Analytics/RemoteConfig/Crashlytics/Performance instances, `OpenFeedbackConfig` (BuildConfig), PerformanceMonitoring, SharedPreferences (`PreferenceManager.getDefaultSharedPreferences`), the store (`DevFestNantesStoreBuilder().setUseMockServer(false).build()`) and the Timber tree (BuildConfig.DEBUG).
- `androidApp/.../core/injection/AppScope.kt`: `@AliasOf(Singleton::class)` custom scope, which maps to `single`.
- `androidApp/.../MainActivity.kt`: `@AndroidEntryPoint`, `@EntryPoint ViewModelFactoryProvider` (session/speaker assisted factories), `@Inject` AnalyticsService + ExternalContentService; builds `SessionDetailRoute`/`SpeakerDetailRoute` via `assistedViewModel { provideFactory(...) }` from `backStackEntry.arguments!!` (keep the `!!`; it's not this phase's fix).
- `androidApp/.../DevFestNantesApplication.kt`: `@HiltAndroidApp`; `@Inject Provider<Set<ApplicationInitializer>>`, `CoroutinesDispatcherProvider`, `Timber.Tree`; `PerformanceInitializer.startAppStartupTrace()` before `super.onCreate()`.
- `androidApp/.../utils/AssistedViewModelUtils.kt`: Hilt `EntryPointAccessors` helper, to be deleted.
- `@HiltViewModel` classes: `core/ui` BookmarksViewModel; `feature/venue` VenueViewModel; `feature/about` PartnersViewModel; `feature/settings` DataCollectionViewModel; `feature/speakers` SpeakersViewModel (+ assisted SpeakerViewModel); `feature/agenda` AgendaViewModel; `feature/session-detail` FeedbackFormViewModel (+ assisted SessionViewModel); `androidApp` HomeViewModel.
- `hiltViewModel()` call sites: Venue.kt, Partners.kt, DataCollectionAgreementDialog.kt, DataCollectionSettingsScreen.kt, Speakers.kt, Agenda.kt, AgendaRow.kt (BookmarksViewModel), SessionLayout.kt (BookmarksViewModel), FeedbackForm.kt, Home.kt.
- `@Inject` constructors: BookmarksStoreImpl, FirebaseAnalyticsService, PerformanceMonitoring (`@Singleton`), SessionFiltersServiceImpl, DataCollectionSettingsServiceImpl, ExternalContentService, DataSharingInitializer, OpenFeedbackInitializer, PerformanceInitializer, TimberTreeDebug/Release.
- Build: `build-logic/convention/.../AndroidHiltConventionPlugin.kt`, Hilt bits in `AndroidApplicationConventionPlugin.kt`, `AndroidLibraryConventionPlugin.kt`, `DependencyResolutionRules.kt`; catalog `gradle/libs.versions.toml` entries `hilt`, `dagger`, `androidx-hilt-*`, `dagger-hilt-*`, plugin `dagger-hilt`. No Koin entries exist yet.

### iOS
- `iosApp/iosApp/Model/BaseViewModel.swift`: `self.store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: false).build()`, one per ViewModel. Subclasses: Agenda, Speakers, SpeakerDetails, Venue, About ViewModels. `ContentView.swift` also references the store.
- `iosApp/DevFest NantesTests/MockDevFestNantesStore.swift`: existing Swift mock, kept usable via BaseViewModel's injectable init (D-04).
- iOS has no `BookmarksStore` implementation today, so don't add one.

### Reusable assets
- `core/testing/src/commonMain/.../FakeStores.kt`: `fakeDevFestNantesStore()` (wraps the public builder in mock mode), the base for Koin test overrides.
- `core/data/.../DevFestNantesStoreBuilder.kt` / `DevFestNantesStoreMocked.kt`: the store factory that Koin's `single { }` wraps.
- `.planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh`: template for the D-13 gate script.

### Established patterns
- Features expose `*Route` composables with callbacks only (D-07 Phase 3). ViewModels are obtained via default params inside feature composables.
- Convention plugins in `build-logic` wire per-module deps, so Koin deps go there instead of per-module build files.
- Two-step, CI-green-per-commit rollouts with tracer first (Phases 1–3).

### Integration points
- `DevFestNantesApplication.onCreate` → `initKoin(androidContext + Android modules)`.
- Swift `App` init → `initKoin` iOS wrapper; `BaseViewModel` default init arg → `IosDependencies`.
- `MainActivity` NavHost composables → feature `*Route(id, callbacks)`.

</code_context>

<specifics>
## Specific Ideas

- Tracer feature is `:feature:venue` (least coupled, same order as Phase 3 D-18).
- Koin-owns-singletons bridge (D-06): `AppModule` `@Provides fun store(): DevFestNantesStore = KoinPlatform.getKoin().get()` style while Hilt ViewModels remain.
- Swift shape: `init(store: DevFestNantesStore = IosDependencies.shared.store())`.

</specifics>

<deferred>
## Deferred Ideas

None from this discussion. Already-tracked related items: Koin Annotations / compiler plugin (DI-V2-01), Navigation 3 migration, `!!` nav-arg cleanup, `println`→Timber, test-coverage retrofit (Phase 5).

</deferred>

---

*Phase: 04-hilt-to-koin-di-migration*
*Context gathered: 2026-09-28*
