# Architecture Research

**Domain:** KMP mobile app modularization (Android + iOS, Now-in-Android-style)
**Researched:** 2026-09-12
**Confidence:** MEDIUM (module-boundary theory is HIGH-confidence, sourced from the official NIA doc and official Kotlin migration docs; the exact incremental order and Koin wiring recommendations are a synthesis adapted to this specific codebase, so treat step ordering as a strong recommendation, not gospel — validate each step against `android run` / `android layout` before proceeding to the next)

## Standard Architecture

### System Overview

The target end state adapts Now in Android's module graph to the reality that **this app does not share UI with iOS** (iOS stays SwiftUI, not Compose Multiplatform). That single fact is the whole ballgame: only data/domain modules can be pure `commonMain` KMP; every Compose-UI module is Android-only. `shared-ui` (currently an empty placeholder in the repo) should either become the umbrella iOS-export module or be deleted — it must not be mistaken for a cross-platform Compose UI layer.

```text
┌─────────────────────────────────────────────────────────────────────────┐
│  app  (androidApp)  — MainActivity, DevFestNantesApplication, NavHost,  │
│  Koin startKoin{}, theme root, initializers                             │
└───────┬─────────┬─────────┬─────────┬─────────┬─────────┬───────────────┘
        │         │         │         │         │         │
        ▼         ▼         ▼         ▼         ▼         ▼
   feature-  feature-  feature-  feature-  feature-  feature-
   agenda    speakers  venue     bookmarks session-  settings
                                            detail
   (Android-only Gradle modules: Compose UI + ViewModel + feature Koin module)
        │         │         │         │         │         │
        └─────────┴─────────┴────┬────┴─────────┴─────────┘
                                  ▼
                         ┌──────────────────┐
                         │     core-ui       │  (Android-only: theme,
                         │  (design system)  │   TopAppBar, cards, icons)
                         └────────┬─────────┘
                                  │
                                  ▼
        ╔═════════════════════════════════════════════════════════╗
        ║   KMP commonMain modules (built once, consumed by both   ║
        ║   Android feature modules AND the iOS Cocoapods framework)║
        ║                                                           ║
        ║   core-data ──depends on──▶ core-network                  ║
        ║      │                          │                          ║
        ║      └──────────┬───────────────┘                          ║
        ║                 ▼                                          ║
        ║             core-model  ◀── core-analytics (interface)     ║
        ║                 ▲                                          ║
        ║             core-testing (fixtures/mocks, depends on all)  ║
        ╚═════════════════════════════════════════════════════════╝
```

### Component Responsibilities

| Component | Responsibility | Typical Implementation |
|-----------|----------------|-------------------------|
| `core-model` | Domain data classes/enums (Session, Speaker, Room, Agenda, Partner, Venue, Category, SessionType, Complexity, ContentLanguage) | Pure KMP `commonMain` module, `kotlinx.serialization` only, zero deps on other core modules — the graph's root |
| `core-network` | Apollo GraphQL client config, generated query code (`.graphql` files + codegen output), normalized cache, `CacheKeyGenerator` | KMP `commonMain` module; depends on `core-model` for mapping targets; owns the Apollo Gradle plugin config |
| `core-data` | `DevFestNantesStore` interface + `GraphQLStore` impl + `Mappers` (GraphQL→model) + `BookmarksStore` (local persistence) | KMP module with `commonMain`/`androidMain`/`iosMain`; local persistence via `expect`/`actual` or a KMP settings library (e.g. multiplatform-settings) instead of raw `SharedPreferences` |
| `core-analytics` | `AnalyticsService` interface + `AnalyticsPage`/`AnalyticsEvent`/`AnalyticsParam` enums | Interface lives in `commonMain`; Firebase-backed `FirebaseAnalyticsService` implementation lives in the module's `androidMain` source set (Firebase Android SDK is Android-only) or is bound at the `app` layer — either works, but keep the interface in the KMP module so iOS can implement its own backend later |
| `core-testing` | Test fixtures/stubs (`SessionStubs`, `SpeakerStubs`, `RoomStubs`, `CategoryStubs`, `DevFestNantesStoreMocked`) + shared coroutine/turbine test helpers | KMP module, `commonMain` (exposed as `api`, not `testImplementation`, so both `commonTest` in KMP modules and Android `test`/`androidTest` in feature modules can use it) |
| `core-ui` | Compose design system: `DevFestNantesTheme`, `TopAppBar`/`BottomAppBar`, `SocialIcon`, `GithubCard`, `SessionComplexity`, `SessionCategory`, `LoadingLayout` | Android library module (`com.android.library` + Compose), depends on `core-model` only |
| `feature-agenda`, `feature-speakers`, `feature-venue`, `feature-bookmarks`, `feature-session-detail`, `feature-settings` | One user journey each: Compose screens + ViewModel + feature-local Koin module | Android library modules, depend on `core-ui`, `core-data`, `core-model`, `core-analytics`; **never depend on each other's Gradle module** |
| `app` (androidApp) | Wires everything: `MainActivity`, `NavHost`/route strings, `DevFestNantesApplication`, `startKoin{}`, `ApplicationInitializer`s | Depends on all feature modules + all core modules; only module that knows the full nav graph |
| `shared` (umbrella, iOS-only concern) | Aggregates `core-model` + `core-network` + `core-data` + `core-analytics` into the single Cocoapods framework iOS links against | Thin KMP module with `cocoapods {}` config; exports nested modules explicitly (see Pitfalls) |

## Recommended Project Structure

```
DevFest_Nantes/
├── build-logic/                     # convention plugins (new) — kmp-common, android-feature, android-compose
│   └── convention/src/main/kotlin/
│       ├── KmpLibraryConventionPlugin.kt
│       ├── AndroidFeatureConventionPlugin.kt
│       └── AndroidComposeConventionPlugin.kt
├── core-model/                      # KMP, commonMain only
├── core-network/                    # KMP, Apollo + generated GraphQL code
├── core-data/                       # KMP, Store layer (commonMain/androidMain/iosMain)
├── core-analytics/                  # KMP, AnalyticsService interface + enums
├── core-testing/                    # KMP, stubs/fixtures/mocks
├── core-ui/                         # Android-only, Compose design system
├── feature-agenda/                  # Android-only, Compose + ViewModel + Koin module
├── feature-speakers/
├── feature-venue/
├── feature-bookmarks/
├── feature-session-detail/
├── feature-settings/                # Settings + Legal + DataCollection (About/Partners: see open question below)
├── androidApp/                      # renamed conceptually to "app": MainActivity, NavHost, Koin startup
├── shared/                          # thin umbrella KMP module, cocoapods export target for iOS only
├── iosApp/                          # unchanged, out of scope
└── settings.gradle.kts              # include() grows one module at a time during migration
```

### Structure Rationale

- **`core-*` before `feature-*`:** feature modules are Android-only consumers; core modules (except `core-ui`) are KMP producers. This mirrors NIA's rule that core modules never depend on feature/app modules — it also means core modules can be extracted first without touching any Android/Compose/Hilt code.
- **`core-ui` is Android-only, not `core-*` KMP:** unlike NIA (where `core:ui`/`core:designsystem` participate in Compose but the app is Android-only end to end), this project's Compose code has no iOS counterpart. Do not put Compose code in a module that also has an `iosMain` source set — it will not compile for iOS and adds confusion about what's actually shared.
- **`shared` becomes a thin re-export shim, not a fat module:** once `core-model`/`core-network`/`core-data`/`core-analytics` exist, `shared`'s only job is being the Cocoapods export surface for iOS. Android code should depend on the fine-grained `core-*` modules directly, not on `shared`.
- **No `feature-*-api`/`feature-*-impl` split (yet):** NIA's own doc explicitly warns against overmodularizing a relatively small app. With 6 features and no evidence of deep cross-feature navigation coupling (navigation today is centralized in `MainActivity`'s `NavHost`, driven by string routes and callback lambdas passed down into composables), a flat one-module-per-feature structure is sufficient. Revisit the api/impl split only if a feature module needs to expose a public surface consumed by another feature module.

## Architectural Patterns

### Pattern 1: Callback-driven, route-agnostic feature modules

**What:** Feature modules expose composables that take `onNavigateToX: (id: String) -> Unit` lambdas rather than importing another feature's `NavController` route or Gradle module. `Screen.kt` (route string constants) stays in `app` (or a tiny shared `core-ui`/`core-navigation` object), and `MainActivity`'s `NavHost` is the only place that knows how routes stitch together.
**When to use:** Always, for this migration — it's how the current code already behaves (`MainActivity` passes `onSessionClick` into `Home`), so it costs nothing to preserve and it's what makes feature modules independently buildable.
**Trade-offs:** `app`'s `NavHost` grows large and is a single point of coordination; acceptable at this app's scale (6 features) and avoids introducing NIA's heavier `api`/`impl` navigation-key pattern prematurely.

**Example:**
```kotlin
// feature-agenda module — no dependency on feature-session-detail
@Composable
fun AgendaScreen(
    onSessionClick: (sessionId: String) -> Unit,
    viewModel: AgendaViewModel = koinViewModel(),
) { /* ... */ }

// app module — owns the wiring
NavHost(navController, startDestination = Screen.Agenda.route) {
    composable(Screen.Agenda.route) {
        AgendaScreen(onSessionClick = { id -> navController.navigate("${Screen.Session.route}/$id") })
    }
}
```

### Pattern 2: Decentralized Koin module per Gradle module

**What:** Every Gradle module (KMP `core-*` and Android `feature-*`) declares its own `val xModule = module { ... }` Koin module in its own source set. `app` collects all of them into one `startKoin { modules(coreModelModule, coreDataModule, coreAnalyticsModule, agendaModule, speakersModule, ...) }` call. KMP modules use plain `koin-core`; Android modules add `koin-android`/`koin-androidx-compose` for `koinViewModel()`.
**When to use:** This is the target DI pattern per `PROJECT.md`'s decision to replace Hilt with Koin. It is orthogonal to modularization — it can be introduced *after* the module split is stable (Hilt still works fine across multiple Android Gradle modules as long as `app` transitively depends on all of them, which it already will).
**Trade-offs:** Plain `module { }` DSL is more verbose than Koin Annotations, but Koin Annotations/KSP has a known limitation: it cannot resolve an interface declared in one Gradle module against an implementation declared in another. Since `core-data`'s `DevFestNantesStore` interface and `GraphQLStore` implementation are likely to stay in the same module (avoiding the cross-module interface/impl case), Annotations remain an option later — but plain DSL is the safer default while modules are still being carved out.

**Example:**
```kotlin
// core-data/src/commonMain/.../di/DataModule.kt
val dataModule = module {
    single<DevFestNantesStore> { GraphQLStore(get(), get()) }
    single { BookmarksStoreImpl(get()) }
}

// feature-agenda/src/main/.../di/AgendaModule.kt
val agendaModule = module {
    viewModel { AgendaViewModel(get(), get(), get()) }
}

// app/.../DevFestNantesApplication.kt
startKoin {
    androidContext(this@DevFestNantesApplication)
    modules(coreModelModule, coreNetworkModule, dataModule, analyticsModule,
             agendaModule, speakersModule, venueModule, bookmarksModule,
             sessionDetailModule, settingsModule)
}
```

### Pattern 3: KMP dependency direction enforced by Gradle, not convention alone

**What:** `core-model` has zero module dependencies; `core-network` and `core-analytics` depend only on `core-model`; `core-data` depends on `core-model` + `core-network`; `core-testing` depends on `core-model` + `core-data`. No core module ever depends on a `feature-*` module (Gradle would reject a cycle if this were violated the other way, but nothing stops a core module from *accidentally* importing a feature type — code review must catch this).
**When to use:** Always — this is the same rule NIA documents for its own core modules, unchanged by the KMP/iOS context.
**Trade-offs:** Requires discipline; consider a `Dependency-Analysis` Gradle plugin or a simple `./gradlew` module-graph lint if drift becomes a problem later (not needed for MVP of the migration).

## Data Flow

### Request Flow (unchanged behaviorally, now crossing module boundaries)

```
[User taps session] (feature-agenda: AgendaScreen)
    ↓ onSessionClick(id) callback
[app: NavHost navigates] → [feature-session-detail: SessionScreen + SessionViewModel]
    ↓ viewModel.init { store.getSession(id) }
[core-data: GraphQLStore] → [core-network: Apollo client executes GetSessionQuery]
    ↓ maps response → Session (core-model)
[SessionViewModel emits StateFlow<Session>] → [Compose recomposes] → [core-analytics: pageEvent logged]
```

### State Management

- Unchanged from current codebase: `MutableStateFlow` in ViewModels, collected as Compose `State`. Module boundaries do not change this — they only change *where the code lives*, not *how it flows*.
- `BookmarksStore` (in `core-data`) remains the single source of truth for bookmark state; `feature-bookmarks` owns the `BookmarksViewModel` that both `feature-agenda` and `feature-session-detail` trigger via callback (not via direct module dependency — see Pitfall below on cross-feature bookmark coupling).

### Key Data Flows

1. **Session browsing:** `feature-agenda` → `core-data` (via injected `DevFestNantesStore` interface) → `core-network` (Apollo) → `core-model` (mapped domain objects) — flows back up as `Flow`/`StateFlow`.
2. **Bookmarking:** UI event in `feature-agenda` or `feature-session-detail` → local `BookmarksViewModel`-equivalent call (injected from `core-data`'s `BookmarksStore`, not from `feature-bookmarks`'s Gradle module directly, to avoid two feature modules depending on each other — see Anti-Pattern 2) → `core-analytics` event.
3. **iOS reuse:** Swift code links the `shared` Cocoapods framework, which re-exports `core-model`, `core-network`, `core-data`, `core-analytics` — the exact same `DevFestNantesStore`/`GraphQLStore`/models used by Android feature modules, with SwiftUI as the sole consumer.

## Migration / Build Order

**Guiding principle (from Kotlin's own migration docs):** never big-bang; extract bottom-up (leaf/foundational dependencies first); every commit must leave the app in a buildable, runnable state on both Android and iOS. Verify each step with `android run` + `android layout`/`android screen capture` before moving to the next.

**Sequencing relative to the other workstreams in `PROJECT.md`:** do this *after* the Gradle Declarative DSL and dependency-upgrade phases (so you're not modularizing on top of a build you're also rewriting), and *before* the Hilt→Koin migration (module boundaries and DI framework are orthogonal — Hilt keeps working fine across multiple Android Gradle modules as long as `app` transitively depends on all of them, which it will by construction).

1. **Foundation (no runtime risk):** introduce `build-logic` convention plugins for the module types you're about to create (`kmp-library`, `android-feature`, `android-compose`) so each subsequent extraction is copy-a-template, not hand-rolled Gradle. Zero behavior change; verify with a clean build.
2. **`core-model`:** extract `model/`, `domain/` from `shared`. Zero internal dependencies — the safest possible first cut. Update `shared`'s remaining code to depend on it. Rebuild Android + iOS.
3. **`core-network`:** extract `graphql/` generated sources, `Apollo.kt`, `ApolloCache.kt`. Depends only on `core-model`. Move the Apollo Gradle plugin config here.
4. **`core-analytics`:** extract `analytics/` (interface + enums). Depends only on `core-model`. Leave the Firebase-backed impl in `androidApp` for now — it can move into this module's `androidMain` later without blocking anything.
5. **`core-data`:** extract `store/` (`DevFestNantesStore`, `GraphQLStore`, `Mappers`, `BookmarksStore`). Depends on `core-model` + `core-network`. At this point `shared` should be nearly empty except for the Cocoapods `cocoapods {}` block — turn it into the thin umbrella described above, `api`-depending on and `export()`-ing `core-model`/`core-network`/`core-data`/`core-analytics` for the iOS framework (see Pitfall on Cocoapods export below). **Checkpoint: full Android app run + iOS build should be identical in behavior to before step 2.**
6. **`core-testing`:** extract `model/stubs/` + `DevFestNantesStoreMocked`. Depends on `core-model` + `core-data`. Update existing `commonTest`/`jvmTest` to consume it as `api`, not duplicate fixtures.
7. **`core-ui`:** extract the Android Compose design system (`theme/`, `components/`) out of `androidApp` into its own Android library module. Depends on `core-model` only. This is the last "shared infrastructure" extraction before touching features — verify all existing screens still render (`android screen capture`) since every screen will re-point its imports here.
8. **Feature extraction, ordered from most isolated to most coupled** (each step: move Compose screen(s) + ViewModel + tests into the new module, repoint `MainActivity`'s `NavHost` to the new module's composable, verify with `android run`/`android layout` before continuing):
   - `feature-venue` (self-contained, no bookmark/session interaction)
   - `feature-settings` (Settings + Legal + DataCollection screens; self-contained)
   - `feature-speakers` (depends on `core-data`, `core-ui`; navigates to session detail via callback only)
   - `feature-bookmarks` (small, but needed by the next two — extract before them)
   - `feature-agenda` (depends on `core-ui`, bookmark toggle via `core-data`'s `BookmarksStore`, not via `feature-bookmarks`'s Gradle module)
   - `feature-session-detail` last — it's the most cross-cutting (reached from both agenda and speakers, needs bookmark toggle and analytics)
9. **Only after the module skeleton is stable and green:** start the separate Hilt→Koin migration phase, module by module, following Pattern 2 above. Do not interleave DI-framework changes with module extraction — each step should change exactly one thing (either "where code lives" or "how it's injected"), never both, so a regression is always attributable to a single cause.

**Decide before step 8, not during it:** where do About/Partners screens go? The requested feature list (`feature-agenda`, `feature-speakers`, `feature-venue`, `feature-bookmarks`, `feature-session-detail`, `feature-settings`) has no explicit home for the current `About`/`Partners` screens. Recommend folding them into `feature-settings` (both are "app info" surfaces) unless the roadmap wants a 7th `feature-about` module — flag this as a roadmap decision point, not a research gap.

## Anti-Patterns

### Anti-Pattern 1: Compose code in a module with an `iosMain` source set

**What people do:** Add Jetpack Compose dependencies to a KMP module that also targets iOS, assuming "shared" means "shared UI too."
**Why it's wrong:** This project's iOS app is SwiftUI, not Compose Multiplatform — Compose code in a KMP module either fails to compile for the iOS target or sits there unused, and it misleads future contributors into thinking UI is actually shared.
**Instead:** Keep Compose exclusively in Android-only library modules (`core-ui`, `feature-*`). If Compose Multiplatform for iOS is ever adopted, that's a separate, explicit future decision — not a side effect of modularization.

### Anti-Pattern 2: Feature modules depending on each other directly

**What people do:** Have `feature-agenda` add a Gradle dependency on `feature-bookmarks` (or `feature-session-detail`) because "it needs the bookmark toggle logic."
**Why it's wrong:** Reintroduces the exact coupling modularization is meant to remove; two features depending on each other's Gradle module means neither can be built, tested, or changed independently, and it's how flat module graphs silently become a ball of mud.
**Instead:** Push shared state (bookmark on/off) down into `core-data`'s `BookmarksStore`, which every feature module already depends on. Cross-feature *navigation* goes through callbacks owned by `app`, never through a direct Gradle dependency.

### Anti-Pattern 3: Forgetting to `export()` nested KMP modules from the Cocoapods framework

**What people do:** Make `shared` an umbrella module with `api(project(":core-data"))` etc., assume Swift can see `core-data`'s public types through transitivity, and move on.
**Why it's wrong:** Kotlin/Native's Cocoapods framework packaging does not automatically re-export transitive module APIs to Objective-C/Swift — types from `core-model`/`core-network`/`core-data`/`core-analytics` will not be visible in Swift unless each is explicitly listed in the framework's `export()` block (inside `binaries.framework { export(project(":core-model")) ... }` in `shared`'s Gradle config). This is a known, easy-to-miss KMP gotcha and will silently break the iOS build (or worse, appear to build but crash/miscompile at the Swift call site) right at the step where `shared` becomes a thin umbrella (migration step 5).
**Instead:** When collapsing `shared` into an umbrella module, explicitly `export()` every KMP module that iOS Swift code touches, and verify with an actual iOS build (not just `./gradlew build`) before considering step 5 complete.

### Anti-Pattern 4: Doing the module split and the Hilt→Koin migration in the same commit/step

**What people do:** Since you're touching a feature's files anyway to move them into a new module, also swap `@HiltViewModel`/`@AssistedInject` for Koin's `module { viewModel { } }` in the same pass.
**Why it's wrong:** Two independent variables change at once (module boundary + DI mechanism); if the feature breaks, you can't tell which change caused it, and it makes each migration PR/commit much larger and harder to review or revert.
**Instead:** Follow `PROJECT.md`'s own declared order — finish the multi-module split with Hilt still working (Hilt handles multi-module Android fine as long as `app` transitively sees every `@Module`), then do a separate, dedicated Koin migration pass afterward, one module at a time.

## Integration Points

### External Services

| Service | Integration Pattern | Notes |
|---------|---------------------|-------|
| Apollo GraphQL (`confetti-app.dev`) | Lives entirely in `core-network`; `core-data` consumes it via the `DevFestNantesStore` interface | Codegen (`generateApolloSources`) must be reconfigured to run in `core-network`'s Gradle build, not `shared`'s |
| Firebase (Analytics/Crashlytics/Performance/Remote Config) | Interface (`AnalyticsService`) in `core-analytics` `commonMain`; Firebase Android SDK implementation in that module's `androidMain` or bound at `app` layer | Firebase has no meaningful iOS Kotlin binding here since iOS is native SwiftUI — iOS Firebase usage (if any) stays entirely in `iosApp`, outside this module graph |
| Cocoapods framework export (iOS) | `shared` umbrella module packages `core-model`/`core-network`/`core-data`/`core-analytics` via `export()` | See Anti-Pattern 3 — this is the highest-risk integration point in the whole migration |
| OpenFeedback | Currently an Android-only `Initializer` in `androidApp/core/` | Stays in `app` (or moves into `feature-settings` if it's UI-adjacent) — not KMP-shareable as-is, low priority to move |

### Internal Boundaries

| Boundary | Communication | Notes |
|----------|---------------|-------|
| `feature-*` ↔ `feature-*` | None directly — via `app`'s `NavHost` callbacks only | Enforced by Gradle (don't add the dependency), not by a runtime mechanism |
| `feature-*` ↔ `core-data` | Constructor/Koin-injected interface (`DevFestNantesStore`, `BookmarksStore`) | Same pattern as today, just resolved via Koin instead of Hilt post-migration |
| `core-data` ↔ `core-network` | Direct dependency, `core-data` owns the mapping | `GraphQLStore` + `Mappers` stay together in `core-data` |
| Android (`app`+`feature-*`+`core-ui`) ↔ KMP (`core-model`+`core-network`+`core-data`+`core-analytics`) | Regular Gradle module dependency (`implementation(project(":core-data"))`), Android modules consume the KMP module's Android target | No `expect`/`actual` needed on the Android consumption side — that machinery lives inside the KMP modules themselves |
| iOS (`iosApp`) ↔ KMP | Cocoapods-generated Objective-C/Swift framework from the `shared` umbrella module | Out of scope to change `iosApp` itself, but the umbrella module's `export()` config is very much in scope |

## Sources

- [Now in Android — ModularizationLearningJourney.md (official, main branch)](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md) — fetched raw and cross-checked; confidence MEDIUM per this project's provider-tiering (webfetch/websearch tier), but content is a direct primary-source read, treat module/dependency rules as reliable
- [Kotlin official docs — Migrating a Jetpack Compose app to Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform/migrate-from-android.html) — incremental migration sequencing
- [Koin — Kotlin Multiplatform Setup (official docs)](https://insert-koin.io/docs/reference/koin-core/kmp-setup/)
- [Santiago Mattiauda — Optimizing Android Modularization: A Guide to Decoupling Koin Modules](https://medium.com/@santimattius/optimizing-android-modularization-a-guide-to-decoupling-koin-modules-968f7194c261)
- [Ahmad Jailani — Modularization in a Kotlin Multiplatform Project](https://ajailani.medium.com/modularization-in-a-kotlin-multiplatform-project-81e06d2170b6)
- [Ahmed Nassar — Building a Fully Modular Kotlin Multiplatform Architecture for Android, iOS, Web, Desktop & Server](https://medium.com/@ranger163/building-a-fully-modular-kotlin-multiplatform-architecture-for-android-ios-web-desktop-server-5933ed62c0a3)
- Project-internal: `.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/STRUCTURE.md`, `.planning/PROJECT.md`

---
*Architecture research for: KMP app modularization (Android + iOS)*
*Researched: 2026-09-12*
