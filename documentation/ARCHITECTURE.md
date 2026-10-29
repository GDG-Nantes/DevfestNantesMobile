# DevFest Nantes - Architecture Guide

This document describes how the DevFest Nantes app is structured: the Gradle modules, how data flows
from the GraphQL API to the screens, and the conventions that keep the modules independent.

## Table of Contents

- [Overview](#overview)
- [Module Graph](#module-graph)
- [Layers](#layers)
- [Data Flow](#data-flow)
- [Dependency Injection (Koin)](#dependency-injection-koin)
- [Navigation](#navigation)
- [State Management](#state-management)
- [iOS App](#ios-app)
- [Privacy and Data Collection](#privacy-and-data-collection)
- [Conventions and Constraints](#conventions-and-constraints)

## Overview

The app is a **Kotlin Multiplatform** project. Data, domain, network and analytics code lives in
KMP modules (`commonMain`) and is shared with iOS through the `shared` framework. The Android UI is
built with Jetpack Compose and split into one Gradle module per feature. The iOS UI is written in
SwiftUI and consumes the shared framework.

```
┌──────────────────────────────────────────────────────────────────────────┐
│ :androidApp  MainActivity (NavHost), DevFestNantesApplication, app Koin  │
├────────────┬────────────┬────────────────┬──────────┬────────┬───────────┤
│ :feature:  │ :feature:  │ :feature:      │ :feature:│:feature│ :feature: │
│ agenda     │ speakers   │ session-detail │ venue    │ about  │ settings  │
└─────┬──────┴─────┬──────┴───────┬────────┴────┬─────┴───┬────┴─────┬─────┘
      ▼            ▼              ▼             ▼         ▼          ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ :core:ui  Compose theme, shared components, BookmarksViewModel (Android) │
└───────────────────────────────┬──────────────────────────────────────────┘
                                ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ :core:domain  Shared agenda policies and use cases (KMP), domainModule   │
└───────────────────────────────┬──────────────────────────────────────────┘
                                ▼
┌──────────────────────────────────────┐   ┌─────────────────────────────┐
│ :core:data  DevFestNantesStore (KMP) │   │ :core:analytics (KMP)       │
│  GraphQLStore / mocked store,        │   │  AnalyticsService,          │
│  BookmarksStore                      │   │  PerformanceMonitoring      │
└──────────────────┬───────────────────┘   └─────────────────────────────┘
                   ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ :core:network  Apollo GraphQL client + schema (KMP)                      │
│ :core:model    Plain domain types + fixtures (KMP)                       │
└──────────────────────────────────────────────────────────────────────────┘

 :shared  KMP framework for iOS, single Koin entry point `initKoin()`
 iosApp/  SwiftUI app consuming the `shared` framework
```

## Module Graph

Modules are declared in `settings.gradle.dcl`:

| Module | Type | Purpose |
|---|---|---|
| `:androidApp` | Android app | Activity, `NavHost`, application initializers, app-level Koin modules |
| `:shared` | KMP (exported to iOS) | `initKoin()` composition root, `IosDependencies` for Swift |
| `:core:model` | KMP | Domain types (`Session`, `Speaker`, `Room`, `Agenda`, ...), `stubs/` fixtures, `WebLinks` |
| `:core:network` | KMP | Apollo client, normalized cache, GraphQL schema and operations |
| `:core:domain` | KMP | Shared agenda, speaker and venue use cases (classify, filter, group, observe, speaker index, venue language), `domainModule`; exported by `:shared` |
| `:core:data` | KMP | `DevFestNantesStore` facade, `GraphQLStore`, mocked store, `BookmarksStore` |
| `:core:analytics` | KMP | `AnalyticsService`; Android Firebase analytics and performance implementations |
| `:core:testing` | KMP | Fakes, `MainDispatcherRule`, test doubles Koin modules |
| `:core:ui` | Android library | Compose theme, shared components, app bars, `BookmarksViewModel` |
| `:feature:agenda` | Android feature | Agenda pager, session filters drawer |
| `:feature:speakers` | Android feature | Speaker list and details |
| `:feature:session-detail` | Android feature | Session details, OpenFeedback form |
| `:feature:venue` | Android feature | Venue info and floor plan |
| `:feature:about` | Android feature | About screen, partners |
| `:feature:settings` | Android feature | Settings, legal, data-collection consent |

Build configuration is shared through convention plugins in the included build `build-logic/`
(`devfest.android.application`, `devfest.android.feature`, `devfest.android.library`,
`devfest.android.koin`, `devfest.kmp.library`, `devfest.detekt`).

Dependency direction is one-way and acyclic:
`androidApp → feature:* → core:ui → core:domain → core:data → core:network / core:model`.
Feature modules never depend on each other.

## Layers

### App shell (`:androidApp`)

`androidApp/src/main/java/com/gdgnantes/devfest/androidapp/`

- `MainActivity.kt`: the single activity. Installs the splash screen, enables edge-to-edge, sets the
  theme and hosts the Navigation Compose `NavHost`.
- `DevFestNantesApplication.kt`: starts Koin and runs the application initializers.
- `ui/screens/Screen.kt`: sealed class of typed destinations; `ui/screens/Home.kt` +
  `home/HomeViewModel.kt` for the bottom-bar home.
- `di/AppModule.kt`, `di/AndroidModules.kt`: app-level Koin bindings and the list of Android modules.
- `core/`: `ApplicationInitializer` and its implementations, `CoroutinesDispatcherProvider`,
  Timber trees under `core/logging/`.
- `services/ExternalContentService.kt`: opens URLs (Custom Tabs).

### Feature modules (`:feature:*`)

Each feature lives in `feature/<name>/src/main/java/com/gdgnantes/devfest/feature/<name>/` (the
package drops hyphens: `feature/sessiondetail`) and follows the same layout:

- `<Name>Route.kt`: the public entry composable, which resolves the ViewModel with `koinViewModel()`.
- Screen composables, `<Name>ViewModel.kt`, and optional `components/`, `services/`, `utils/`.
- `di/<Name>Module.kt`: the feature's Koin module.

Side effects that need the app shell (opening URLs, cross-feature navigation, analytics on
navigation) are passed in as lambdas, for example `SessionDetailRoute(onSocialLinkClick = ...)`.

### Shared UI (`:core:ui`)

Material 3 theme (`theme/`), reusable components (`components/`, `components/appbars/`), Compose
helpers (`utils/`), the cross-feature `BookmarksViewModel`, and `di/CoreUiModule.kt`, its Koin module.

### Domain (`:core:domain`)

`core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/`

Business logic shared by Android and iOS, in packages `agenda`, `speakers`, `venue` and `di`. It
depends on `:core:model` and `:core:data` and is exported to Swift through `:shared`.

Use cases:

- `ClassifySessionUseCase`: `isService`, `isTalk`, `isNavigable` for a session.
- `FilterSessionsUseCase.filter(sessions, filters, bookmarkedSessionIds)`: OR within a filter type,
  AND across types, output in store order.
- `GroupSessionsUseCase.group(sessions, dayAssignment, timeZone)`: days, then time slots keyed by the
  start `Instant`, then sessions in room order (`room.sortIndex`, then name).
- `ObserveAgendaUseCase.observe(dayAssignment, timeZone)`: combines `DevFestNantesStore.sessions`,
  `SessionFiltersStore.filters` and `BookmarksStore.bookmarkedSessionIds` into a
  `Flow<List<DaySchedule>>` (`@NativeCoroutines`).
- `SpeakerIndexUseCase`: the A-Z speaker index (`letterGroups` for the iOS list, `firstIndexes` for
  the Android scroller). `SpeakersViewModel` takes it by constructor injection and exposes
  `firstLetterIndexes(speakers)`, which the speakers list computes with `remember(speakers)`; a
  speaker with an empty name is skipped and reported to Crashlytics as a non-fatal exception
  through `Timber.w` (D-20, see the deviations table).
- `VenueLanguageUseCase.languageFor(languageCode)`: French for `"fr"`, English otherwise.
  `VenueViewModel` takes it by constructor injection and calls
  `languageFor(locale.language)`; it still emits nothing when the system locale is null.

`DayAssignment` selects how a session is assigned to a conference day: `UtcDayWindows` (Android, fixed
24 hour UTC windows from the configured day starts, exactly like `Agenda.Builder`) or `LocalDateDays`
(iOS, the calendar date in the time zone passed to `group`).

Rules:

- Policies and use cases are plain classes with named methods, not `operator invoke` objects.
- They are bound as Koin `factory` definitions in `di/DomainModule.kt`. `domainModule` is
  `@HiddenFromObjC` and is part of `sharedModules`; iOS resolves use cases through
  `IosDependencies`. The module also binds the `CoroutineDispatcher` named `domainDispatcher`
  (`Dispatchers.Default`) that `ObserveAgendaUseCase` runs its upstream work on.
- The use cases return data only. Loading and error state, tracing, refresh and cancellation stay
  in the ViewModels. `ObserveAgendaUseCase` builds its flow inside each `observe` call, so a new
  collection restarts the cold store flow (refresh = cancel and collect again), and a store failure
  reaches the collector unchanged, as in v1.0.
- The time zone is an explicit parameter on every platform: callers pass
  `TimeZone.currentSystemDefault()`.
- Methods that return a `Flow` carry `@NativeCoroutines` so Swift sees `async`/`AsyncSequence`.
- No public extension functions on exported model types: Swift cannot call them as members.

### Data (`:core:data`)

`core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/`

- `DevFestNantesStore`: the single read facade over conference data. Collections are exposed as
  `Flow`s (`agenda`, `sessions`, `speakers`, `rooms`, `partners`), lookups as `suspend` functions
  (`getSession`, `getSpeaker`, `getRoom` return `null` when not found; `getSpeakerSessions`,
  `getVenue(language)`).
- `DevFestNantesStoreBuilder`: picks `GraphQLStore` or `DevFestNantesStoreMocked`
  (`setUseMockServer(true)`).
- `graphql/GraphQLStore.kt` + `graphql/Mappers.kt`: run Apollo queries and map them to `core:model`
  types. Apollo types never leave this module.
- `BookmarksStore` and `SessionFiltersStore`: common interfaces for the persisted bookmarked session
  ids and the selected agenda filters. Android keeps its existing storage (`selected_sessions` for
  bookmarks through `BookmarksStoreImpl`, `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` for filters
  through `SharedPreferencesSessionFiltersStore`, both in `androidMain` and bound in the Android
  platform Koin module of this module; keys and value format are unchanged since v1.0). iOS
  implements both in `iosMain` on `NSUserDefaults` under new keys,
  `devfest.bookmarks.sessionIds` (array of session ids) and `devfest.agenda.filters` (array of JSON
  strings written with the shared filter codec, the same bytes as Android). The iOS stores never
  read or delete the legacy v1.0 keys (D-15: legacy data is intentionally not migrated), and the iOS
  app starts using them in Phase 9.

### Network (`:core:network`)

- `Apollo.kt`: the process-wide `apolloClient`, pointing at the
  [Confetti](https://github.com/joreilly/Confetti) backend (`https://confetti-app.dev/graphql`) with
  the `conference: devfestnantes2025` header. It is exposed to Koin as a `single`.
- `ApolloCache.kt`: a 10 MB memory cache chained with a SQLite normalized cache (`apollo.db`), which
  is what gives the app offline access to previously loaded data.
- `src/commonMain/graphql/`: `schema.graphqls`, `extra.graphqls`, `operations.graphql`. Generated
  code lands in `com.gdgnantes.devfest.core.network.graphql`, with data builders enabled for tests.

### Model (`:core:model`)

Plain Kotlin domain types with no internal dependency. `stubs/` holds the canonical fixtures
(`SessionFixtures`, `SpeakerFixtures`, ...) used by previews and tests. `SessionFilter` (a
`@Serializable` data class) and the top-level `SessionFilterType` describe the agenda filters that
both platforms persist through the shared filter codec.

### Analytics (`:core:analytics`)

- `commonMain`: `AnalyticsService`, `AnalyticsEvent`, `AnalyticsPage`, `AnalyticsParam`.
- `androidMain`: `FirebaseAnalyticsService`, and the `performance/` package
  (`PerformanceMonitoring` interface, `FirebasePerformanceMonitoring`, tracing extensions).
  See [PERFORMANCE_MONITORING.md](PERFORMANCE_MONITORING.md).
- `iosMain` binds nothing: iOS analytics and performance are implemented in Swift.

## Data Flow

```mermaid
graph LR
    UI[Route / Composable] --> VM[ViewModel]
    VM --> Store[DevFestNantesStore]
    Store --> GQL[GraphQLStore]
    GQL --> Apollo[apolloClient + normalized cache]
    Apollo --> API[Confetti GraphQL API]
    GQL --> Map[Mappers] --> Model[core:model types]
    Model --> Store --> VM --> UI
```

Example, the agenda:

1. `MainActivity` registers `Screen.Home`; `Home` shows the agenda tab.
2. `AgendaRoute` gets `AgendaViewModel` from Koin. It receives `ObserveAgendaUseCase`
   (`:core:domain`), `DevFestNantesStore` (rooms), the `SessionFiltersStore` and
   `PerformanceMonitoring`.
3. The ViewModel collects `ObserveAgendaUseCase`, which combines sessions, filters and bookmarks into
   the filtered, grouped `DaySchedule`/`TimeSlot` list exposed as UI state. The use case runs its
   upstream work on the domain's `domainDispatcher`.
4. `GraphQLStore` runs the Apollo query and maps the response through `graphql/Mappers.kt`.
5. `Agenda.kt`, `AgendaPager.kt`, `AgendaColumn.kt` render the day schedules; labels and the
   scroll-to-now index stay in `AgendaColumn` (slot headers use the device zone and locale).

Session details: a click navigates to `"${Screen.Session.route}/{sessionId}"`;
`SessionDetailRoute` builds `SessionViewModel` with that id. The OpenFeedback form is shown only when
`BuildConfig.OPEN_FEEDBACK_ENABLED` is true (`OpenFeedbackConfig` is built in the app shell);
otherwise `FallbackFeedbackForm` links to the web form.

## Dependency Injection (Koin)

Every Gradle module owns one Koin module in `di/<Name>Module.kt`. `initKoin()` in
`shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt` is the single
composition root:

- It starts Koin with `sharedModules` (network, data, analytics) plus the platform modules it is
  given.
- Android passes `androidModules` (core:ui, the feature modules and the app shell) and
  `androidContext` from `DevFestNantesApplication`.
- iOS calls it with no arguments through `IosDependencies.shared.start()`.
- Overriding is disabled (`allowOverride(false)`): a binding declared twice fails at start-up.

```kotlin
// feature/venue/.../di/VenueModule.kt
val venueModule: Module = module {
    viewModelOf(::VenueViewModel)
}

// androidApp: started once from the Application
initKoin(platformModules = androidModules) { androidContext(this@DevFestNantesApplication) }

// Route composables resolve their ViewModel
val viewModel: VenueViewModel = koinViewModel()
```

Rules:

- ViewModels are plain classes with constructor parameters; parameterized ones use
  `viewModel { (id) -> ... }`.
- Service-locator access (`by inject()`, `KoinComponent`, ...) is limited to `di/` packages,
  `*Module.kt` files, `DevFestNantesApplication`, `MainActivity` and `IosDependencies`.
  `koinViewModel()` is allowed everywhere.
- `scripts/di-gate.sh` enforces this in CI, and forbids any reintroduction of Hilt/Dagger.
- Graph verification runs in CI: `KoinGraphTest` (`:shared`), `*ModuleTest` per module, and
  `KoinStartupTest` / `ProductionGraphStartupTest` (Robolectric) in `:androidApp`.

## Navigation

The Android app uses Navigation Compose with string routes defined in `Screen.kt`
(`home`, `agenda`, `speakers`, `venue`, `about`, `settings`, `session`, `speaker`,
`datacollection`, `legal`). Detail routes append an id: `"${Screen.Speaker.route}/${speaker.id}"`.
To add a screen, add a `Screen` subclass and a `composable(...)` entry in `MainActivity`'s `NavHost`.

## State Management

- ViewModels expose `StateFlow`s; mutable state stays private (`_uiState` + `asStateFlow()`).
- `UiState` (`STARTING`, `SUCCESS`, `ERROR`) lives in `:core:ui`.
- `SharedPreferencesSessionFiltersStore` is the single `SessionFiltersStore` of the Android graph: a
  Koin `single` bound in `:core:data`, so the agenda and `HomeViewModel` share the same filters.
- Bookmarks are persisted by `BookmarksStore` and shared through `BookmarksViewModel`.
- Coroutine dispatchers are injected (`CoroutinesDispatcherProvider`, the `domainDispatcher` of
  `:core:domain`) so tests can replace them.

### Behavior deviations (v1.1)

| Behavior | Android v1.0 | iOS v1.0 | Shared (v1.1) | Why acceptable / where it stays per platform |
|---|---|---|---|---|
| `SessionFilter` equality | Not a data class: equal-content sets re-emitted through `StateFlow` | n/a | Data class: equal-content sets do not re-emit | No visible effect (drawer and equality characterizations unchanged); Android adopted it in 07-10 |
| Order inside a time slot (`RoomSortIndex`) | Unspecified (`HashSet`, then room name and `sortIndex` sorts) | Room name only | `room.sortIndex`, then room name; a session without a room first; ties keep store order | One deterministic order covering both; the iOS order changes when it adopts the shared grouping (Phase 9) |
| Room filter | Compares the filter value with `room.id` | Compares it with the room name | Compares with `room.id` | Equal on real data because `room.id == room.name` for every room |
| Slot key | Localized short time in the device zone: starts within the same minute share a slot | Exact `Date` | Exact start `Instant` | Every real session starts on a whole minute; the shared key is the stricter of the two |
| Filter output order | Unspecified (`HashSet`) | Store order | Store order | A deterministic subset of the old Android behavior |
| Start with fractional seconds | Parsed | Falls back to "now" (pinned by `AgendaDateParsingTests`) | Parsed | Never occurs in real data; the shared parsing is the correct one |
| Start up to 24 h before a day start | Belongs to that day (`(start - dayStart).inWholeDays == 0`) | n/a (local calendar date) | `UtcDayWindows` copies the `Agenda.Builder` rule, so the same quirk remains | Strict parity; real sessions are inside their day |
| Speaker index: empty name | `first()` throws `NoSuchElementException` | Adds an empty letter | Empty letter `""`; Android skips it and reports a non-fatal exception to Crashlytics through Timber | Never present in the data; an input that crashed v1.0 Android no longer does (accepted behavior change, D-20, implemented in 07-18); `SpeakerIndexEmptyNameTest` pins the skip and the report |
| Speaker index: initial outside the Basic Multilingual Plane | One UTF-16 unit | One grapheme | One UTF-16 unit | Absent from the data; the platforms could still differ for such a letter |
| Day time zone | Fixed UTC windows (zone ignored); the device zone only formats labels | Local calendar date in the device zone | The API takes the zone on every platform (D-05, D-19); `UtcDayWindows` ignores it by design, `LocalDateDays` reads it | The day assignment strategy is the per-platform choice; the Android label formatter keeps using the device zone |
| Scroll to the current time slot | `currentStartTimeIndex` in `AgendaColumn` | Own logic in SwiftUI | Not shared | Stays per platform (D-08): wall-clock behavior tied to each UI |
| Partner categories | Five fixed groups, ignores `PARTNERS` | Every category sorted by ordinal | Not shared | Stays per platform (D-08): the semantics differ |

## iOS App

`iosApp/iosApp/` is a SwiftUI app (deployment target iOS 17.0):

- `iOSApp.swift` configures Firebase, then calls `IosDependencies.shared.start()` to start Koin.
- `Model/BaseViewModel.swift` gets the store from `IosDependencies.shared.store()`. Flows are
  consumed through KMP-NativeCoroutines (Combine / async sequences).
- Feature code is in `Agenda/` and `UI/` (`Speakers/`, `Venue/`, `About/`, `Common/`).
- `Analytics/FirebaseAnalyticsService.swift` and `Performance/PerformanceMonitoring.swift` call the
  Firebase iOS SDK (Swift Package Manager) directly.
- `SwiftGen/` generates typed assets and strings; `Resources/` holds `en.lproj` and `fr.lproj`.

The Kotlin framework is built by the Xcode build phase
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`.

## Privacy and Data Collection

On Android, Firebase Analytics, Crashlytics and Performance are under user control:

- `DataCollectionSettingsService` (`feature/settings/.../services/`) stores the enabled services in
  `SharedPreferences` and toggles `setAnalyticsCollectionEnabled`,
  `isCrashlyticsCollectionEnabled` and `isPerformanceCollectionEnabled`.
- `DataCollectionAgreementDialog` asks for consent on first launch; `DataCollectionSettingsScreen`
  lets the user change it at any time.
- At start-up, the initializers run in a fixed order (named Koin list in `di/AppModule.kt`):
  `DataSharingInitializer` (applies stored consent), `OpenFeedbackInitializer`,
  `PerformanceInitializer`. Note that `PerformanceInitializer` currently re-enables performance
  collection unconditionally (see [PERFORMANCE_MONITORING.md](PERFORMANCE_MONITORING.md)).

iOS does not have a consent screen yet.

## Conventions and Constraints

- **Adding a store method:** add it to `DevFestNantesStore` and implement it in both
  `GraphQLStore.kt` and `DevFestNantesStoreMocked.kt` in the same change. The shared
  `DevFestNantesStoreContractTest` runs against both.
- **Global state:** `apolloClient` is a process-wide value in `:core:network`, not created per graph.
- **Errors:** missing entities are `null` from the store; Flow errors are caught in the data layer and
  surface as `UiState.ERROR` where handled. Logging uses Timber on Android
  (`TimberTreeDebug` / `TimberTreeRelease`).
- **No authentication:** the API is public and the app has no user accounts.

## Related Documentation

- [DEVELOPMENT.md](DEVELOPMENT.md) - Environment setup, build, test and tooling
- [PERFORMANCE_MONITORING.md](PERFORMANCE_MONITORING.md) - Firebase Performance on Android and iOS
- [VERSIONING.md](VERSIONING.md) - Calendar versioning
- [iOS_BUILD_DSYM.md](iOS_BUILD_DSYM.md) - iOS release builds and dSYMs
