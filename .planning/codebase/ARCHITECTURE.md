---
last_mapped_commit: f6a603391f893d8f6bd0964cfcca500620b6a072
last_mapped_at: 2026-10-16
---
<!-- refreshed: 2026-10-16 -->

# Architecture

**Analysis Date:** 2026-10-16

## System Overview

```text
┌──────────────────────────────────────────────────────────────────────┐
│                     Android app shell (`:androidApp`)                 │
│  `androidApp/.../MainActivity.kt` (NavHost, Screen routes, Koin)      │
├───────────────┬──────────────┬──────────────┬────────────┬───────────┤
│ `:feature:agenda` │ `:feature:speakers` │ `:feature:session-detail` │ `:feature:venue` │ `:feature:about` / `:feature:settings` │
│  AgendaViewModel │ SpeakerViewModel │ SessionViewModel / FeedbackFormViewModel │ VenueViewModel │ (no ViewModel) │
└───────┬───────┴──────┬───────┴──────┬───────┴─────┬──────┴─────┬─────┘
        │              │              │             │            │
        ▼              ▼              ▼             ▼            ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Shared UI + Android UI kit: `:core:ui` (theme, components, BookmarksViewModel) │
└──────────────────────────────────────────────────────────────────────┘
        │                                             │
        ▼                                             ▼
┌───────────────────────────────┐        ┌──────────────────────────────┐
│ `:core:data` (DevFestNantesStore) │ ───▶  │ `:core:analytics` (AnalyticsService) │
│  GraphQLStore / Mocked store   │        │  Android + iOS platform impls  │
└───────────────┬───────────────┘        └──────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────────────────────────────────┐
│ `:core:network` (Apollo GraphQL client, `apolloClient` global)        │
│ `:core:model` (pure Kotlin domain models, KMP commonMain)             │
└──────────────────────────────────────────────────────────────────────┘
```

Kotlin Multiplatform modules (`commonMain`) hold the data and domain layers and are shared with the iOS app via the `shared` framework. Compose UI modules are Android-only (`src/main/java`).

## Module Graph

Declared in `settings.gradle.dcl` (`include(...)`):

- `:androidApp` (Android application, Compose, Koin, Navigation Compose)
- `:shared` (KMP framework for iOS; composition root `initKoin`)
- `:core:model`, `:core:network`, `:core:analytics`, `:core:data`, `:core:testing`, `:core:ui`
- `:feature:venue`, `:feature:about`, `:feature:settings`, `:feature:speakers`, `:feature:agenda`, `:feature:session-detail`
- `build-logic/` (included build with convention plugins under `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic`)

Dependency direction is one-way: `androidApp` → `feature:*` → `core:ui` → `core:data` → `core:network` / `core:model`. `core:model` depends on nothing internal.

## Pattern Overview

**Overall:** Modular, feature-sliced MVVM with a repository-style data facade ("Store") and Koin dependency injection. Single-activity Compose app with a Navigation Compose `NavHost` in the app shell.

**Key Characteristics:**
- One `ComponentActivity` (`androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt`) hosts all screens; screens are composables registered in its `NavHost`.
- Each feature module exposes a public `*Route` composable (entry point) and keeps its screen composables, ViewModel and Koin module `internal`/package-private by convention (e.g. `feature/agenda/.../AgendaRoute.kt`, `feature/session-detail/.../SessionDetailRoute.kt`).
- Feature modules never depend on each other directly; cross-feature wiring happens in `androidApp` (e.g. `OpenFeedbackConfig` in `feature/session-detail` is constructed in `androidApp/.../di/AppModule.kt` so only a flag and project id cross modules).
- Data access goes through the `DevFestNantesStore` interface (`core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStore.kt`), which exposes `Flow`s for lists and `suspend` functions for single lookups.
- Analytics and performance monitoring are behind `AnalyticsService` (`core/analytics`) with platform `actual` implementations in `core/analytics/src/androidMain` and iOS.

## Layers

**Composition root / app shell (`:androidApp`):**
- Purpose: Hosts the activity, navigation graph, application initializers, and the top-level Koin setup.
- Location: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/`
- Contains: `MainActivity.kt`, `DevFestNantesApplication.kt`, `ui/screens/Screen.kt` (sealed route definitions), `ui/screens/Home.kt` + `ui/screens/home/HomeViewModel.kt`, `di/AppModule.kt`, `di/AndroidModules.kt`, `core/*Initializer.kt`, `services/ExternalContentService.kt`.
- Depends on: all `feature:*` and `core:*` modules.
- Used by: Android launcher only.

**Feature layer (`:feature:*`):**
- Purpose: Self-contained screens with their own ViewModel, state, and Koin module.
- Location: `feature/<name>/src/main/java/com/gdgnantes/devfest/feature/<name>/`
- Contains: `*Route.kt` (entry composable), screen composables, `*ViewModel.kt`, `di/<Name>Module.kt`, optional `services/` and `utils/`.
- Depends on: `:core:ui`, `:core:data`, `:core:model`, `:core:analytics`.
- Used by: `androidApp` navigation graph.

**Shared UI layer (`:core:ui`):**
- Purpose: Material theme, reusable composables, app bars, and the cross-feature `BookmarksViewModel`.
- Location: `core/ui/src/main/java/com/gdgnantes/devfest/core/ui/`
- Contains: `theme/`, `components/`, `components/appbars/`, `utils/`, `di/CoreUiModule.kt` (defines `IO_DISPATCHER` qualifier).
- Depends on: `:core:data`, `:core:model`.
- Used by: all feature modules and `androidApp`.

**Data layer (`:core:data`, KMP commonMain):**
- Purpose: Exposes domain data through `DevFestNantesStore`; maps GraphQL responses to domain models.
- Location: `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/`
- Contains: `DevFestNantesStore.kt` (interface), `DevFestNantesStoreBuilder.kt` (selects GraphQL vs mocked store), `GraphQLStore.kt` and `graphql/Mappers.kt` (in `graphql/`), `DevFestNantesStoreMocked.kt`, `BookmarksStore.kt`, `domain/RoomSortIndex.kt`, `di/DataModule.kt`.
- Depends on: `:core:network`, `:core:model`.
- Used by: feature ViewModels (injected via Koin `get()`), iOS via `shared`.

**Network layer (`:core:network`, KMP commonMain):**
- Purpose: Apollo GraphQL client and schema.
- Location: `core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/`
- Contains: process-global `apolloClient` (exported as a Koin `single<ApolloClient>` in `di/NetworkModule.kt`), GraphQL operations and schema under `core/network/src/commonMain/graphql/` (`schema.graphqls`, `operations.graphql`, `extra.graphqls`).
- Depends on: nothing internal.
- Used by: `:core:data`.

**Domain model (`:core:model`, KMP commonMain):**
- Purpose: Plain domain types shared across layers and platforms.
- Location: `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/`
- Contains: `Session`, `Speaker`, `Room`, `Venue`, `Partner`, `Agenda`, etc.; `stubs/` for test/preview data; `utils/` helpers.
- Depends on: nothing internal.
- Used by: every other module.

**Analytics (`:core:analytics`, KMP):**
- Purpose: Event and page tracking, performance hooks, consent-aware collection.
- Location: `core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/`, with `androidMain/.../performance/` and `di/`.
- Used by: `androidApp` (`MainActivity`) and feature ViewModels.

**Shared KMP composition (`:shared`):**
- Purpose: iOS-facing framework and single Koin entry point.
- Location: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt`
- Contains: `sharedModules = listOf(networkModule, dataModule, analyticsModule)` and `initKoin(platformModules, appDeclaration)`. Overriding is disabled (`allowOverride(false)`) so duplicate bindings fail fast.
- Used by: iOS app (`iosApp/`) through `IosDependencies`; Android passes its platform modules and `androidContext`.

## Data Flow

### Primary Request Path (Agenda screen)

1. `Screen.Home` is registered in `MainActivity` NavHost; `Home` renders the agenda tab (`androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt`, `androidApp/.../ui/screens/Home.kt`).
2. `AgendaRoute` (`feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRoute.kt`) obtains `AgendaViewModel` through Koin (`feature/agenda/.../di/AgendaModule.kt`), which receives `DevFestNantesStore`, bookmarks store, performance monitoring, `SessionFiltersService`, and the named IO dispatcher.
3. `AgendaViewModel` collects `store.agenda` / `store.sessions` Flows and combines them with bookmarks and filters into UI state.
4. `GraphQLStore` (`core/data/.../graphql/GraphQLStore.kt`) runs Apollo queries on `apolloClient` and maps responses via `graphql/Mappers.kt` to `core/model` types.
5. Composables (`Agenda.kt`, `AgendaPager.kt`, `AgendaColumn.kt`) render state; user toggles (bookmarks, filters) call back into the ViewModel.

### Session Detail Flow

1. Home or agenda row click invokes `onSessionClick`, which navigates to `"${Screen.Session.route}/{sessionId}"` (`MainActivity.kt`).
2. `SessionDetailRoute(sessionId, onBackClick, onSocialLinkClick, ...)` (`feature/session-detail/.../SessionDetailRoute.kt`) builds `SessionViewModel` with the session id.
3. Social link clicks are routed back to the app shell: `MainActivity` calls `externalContentService.openUrl(link)` and `analyticsService.eventSpeakerSocialLinkOpened(...)`. Feature modules do not open URLs themselves.

### Feedback Flow (feature-flagged)

1. `OpenFeedbackInitializer` (`androidApp/.../core/OpenFeedbackInitializer.kt`) and `OpenFeedbackConfig` (`feature/session-detail/.../OpenFeedbackConfig.kt`) supply the flag and project id.
2. `FeedbackForm` / `FeedbackFormViewModel` render; `FallbackFeedbackForm.kt` is used when the feature is disabled.

### Application Startup

1. `DevFestNantesApplication` starts Koin via `initKoin(platformModules = androidModules, appDeclaration = { androidContext(...) })` (`shared/.../InitKoin.kt`, `androidApp/.../di/AndroidModules.kt`).
2. `applicationInitializers` (named Koin list in `androidApp/.../di/AppModule.kt`) runs in a fixed order: `DataSharingInitializer`, `OpenFeedbackInitializer`, `PerformanceInitializer`. The order is a consent contract: stored data-collection consent must be applied before performance collection is enabled.
3. `MainActivity` installs the splash screen, enables edge-to-edge, and sets the `DevFestNantesTheme` plus `NavHost`.

**State Management:**
- ViewModels expose `StateFlow`-based UI state; one-off effects use event channels/flows inside each ViewModel (see `feature/agenda/.../AgendaViewModel.kt`).
- Session filters are a single shared stateful service (`SessionFiltersServiceImpl`, registered as Koin `single`) so the agenda screen and `HomeViewModel` observe the same instance.
- Bookmarks are persisted via `core/data/.../BookmarksStore.kt` and shared through `core/ui`'s `BookmarksViewModel`.

## Key Abstractions

**`DevFestNantesStore`:**
- Purpose: Single read-facade over conference data (agenda, sessions, speakers, rooms, partners, venue).
- Examples: `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStore.kt`, `GraphQLStore.kt`, `DevFestNantesStoreMocked.kt`
- Pattern: Interface with `Flow` for observable collections and `suspend` lookups; implementations are chosen by `DevFestNantesStoreBuilder` (`setUseMockServer` switches to the mock).

**`AnalyticsService`:**
- Purpose: Typed event methods (`eventSpeakerOpened`, `eventSpeakerSocialLinkOpened`, ...) and page tracking.
- Examples: `core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/`
- Pattern: Interface in commonMain, platform implementations in `androidMain` / iOS.

**`Screen` (sealed class):**
- Purpose: Typed navigation destinations for bottom-bar tabs and detail routes (route string, title resource, filled/outlined icons).
- Examples: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Screen.kt`
- Pattern: `data object` subclasses; routes are plain strings joined with ids (`"${Screen.Speaker.route}/${speaker.id}"`).

**`ApplicationInitializer`:**
- Purpose: Start-up hooks run in an explicit, ordered list.
- Examples: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/ApplicationInitializer.kt` and its implementations in the same package.
- Pattern: Named Koin `listOf(...)`, not an unordered multibinding.

**`SuspendUseCase`:**
- Purpose: Small use-case wrapper for suspend work in the app shell.
- Examples: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/SuspendUseCase.kt`
- Pattern: Invoked from ViewModels; `CoroutinesDispatcherProvider` (same package) supplies injectable dispatchers.

## Entry Points

**Android application:**
- Location: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt` (launcher activity) and `DevFestNantesApplication.kt` (Koin + initializers).
- Triggers: Android launcher intent.
- Responsibilities: NavHost, theme, edge-to-edge, splash screen, cross-feature navigation callbacks.

**iOS application:**
- Location: `iosApp/iosApp/` (Swift); consumes the `shared` framework and calls `initKoin` through `IosDependencies`.
- Triggers: iOS app launch.
- Responsibilities: SwiftUI/UIKit presentation over shared data and analytics.

**Mock data entry:**
- Location: `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreMocked.kt`, selected via `DevFestNantesStoreBuilder.setUseMockServer(true)`.
- Triggers: Build or test configuration.

## Architectural Constraints

- **Threading:** Coroutines throughout. Production dispatchers are `Dispatchers.Default` / `Dispatchers.IO` / `Dispatchers.Main.immediate` via `CoroutinesDispatcherProvider` (`androidApp/.../core/CoroutinesDispatcherProvider.kt`). The IO dispatcher is injected under the named qualifier `IO_DISPATCHER` (`core/ui/.../di/CoreUiModule.kt`) so tests can substitute it.
- **Global state:** `apolloClient` is a process-global in `core/network` (exported to Koin as a single, not created per graph). `Timber` trees are installed as a Koin `single<Timber.Tree>`.
- **Koin overrides disabled:** `startKoin { allowOverride(false) }` in `shared/.../InitKoin.kt`; tests must re-enable overriding in `appDeclaration`. Adding a duplicate binding in two modules is a start-up failure by design.
- **Hilt-to-Koin migration in progress:** Git history records a completed Hilt-to-Koin DI migration; a few comments still refer to "not-yet-migrated" components (for example the `HomeViewModel` bridge comment in `feature/agenda/.../di/AgendaModule.kt`).
- **Circular imports:** None observed between Gradle modules; the dependency graph is acyclic as listed above.

## Anti-Patterns

### Feature modules reaching into the app shell

**What happens:** Some behaviour that needs the Android `Activity` or Android services (URL opening, analytics on navigation) is wired in `MainActivity` callbacks instead of inside feature modules.
**Why it's wrong:** Feature screens stay coupled to the app's navigation and cannot be previewed or reused without the shell.
**Do this instead:** Keep the pattern used by `SessionDetailRoute(onSocialLinkClick = ...)` in `androidApp/.../MainActivity.kt`: pass lambdas into `*Route` composables and let the shell handle the side effect.

### Parallel mock and real store selection

**What happens:** `DevFestNantesStoreBuilder` picks between `GraphQLStore` and `DevFestNantesStoreMocked` with a boolean flag.
**Why it's wrong:** Mock and real implementations of `DevFestNantesStore` must be kept in sync manually; drift is only caught when a feature is exercised against the mock.
**Do this instead:** Add any new store method to the `DevFestNantesStore` interface and implement it in both `GraphQLStore.kt` and `DevFestNantesStoreMocked.kt` in the same change.

## Error Handling

**Strategy:** Errors are surfaced through ViewModel UI state and logged with Timber; data-layer functions return nullable results (`getSession`, `getRoom`, `getSpeaker` return `T?`) for not-found cases.

**Patterns:**
- Nullable returns for missing entities in `DevFestNantesStore`.
- `Timber` logging through the trees in `androidApp/.../core/logging/`.

## Cross-Cutting Concerns

**Logging:** Timber; `TimberTreeDebug` / `TimberTreeRelease` chosen in `di/AppModule.kt` by `BuildConfig.DEBUG`.
**Validation:** Not centralized; mapping is performed in `core/data/.../graphql/Mappers.kt`.
**Authentication:** Not detected (no user accounts in the app).
**Analytics and consent:** `DataSharingInitializer` applies stored data-collection consent from `feature/settings/.../datacollection/` before `PerformanceInitializer` enables performance collection.

---

*Architecture analysis: 2026-10-16*
