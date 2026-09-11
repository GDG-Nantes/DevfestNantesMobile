<!-- refreshed: 2026-09-11 -->
# Architecture

**Analysis Date:** 2026-09-11

## System Overview

```text
┌──────────────────────────────────────────────────────────────────────┐
│                    Android UI (Jetpack Compose)                      │
├──────────────────┬──────────────────┬──────────────────┬─────────────┤
│  MainActivity    │  Home (Nested)   │  Session Detail  │  Speaker    │
│  Navigation      │  Screens         │  Settings        │  Details    │
│  (NavHost)       │  (Agenda, etc.)  │  Legal           │  Venue      │
└────────┬─────────┴────────┬─────────┴────────┬────────┴───────┬──────┘
         │                  │                   │                │
         ▼                  ▼                   ▼                ▼
┌──────────────────────────────────────────────────────────────────────┐
│              Presentation & Application Layer (androidApp)            │
│                   ViewModels with Hilt DI                            │
│  SessionViewModel, SpeakerViewModel, AgendaViewModel, etc.           │
│  Services: AnalyticsService, BookmarksStore, SessionFiltersService  │
│  Initializers: DataSharingInitializer, OpenFeedbackInitializer      │
│  Core: CoroutinesDispatcherProvider, PerformanceMonitoring          │
└─────────────────────────────────────────┬───────────────────────────┘
                                          │
                                          ▼
┌──────────────────────────────────────────────────────────────────────┐
│              Shared Module - Data & Domain Layer (KMP)                │
│                 Package: com.gdgnantes.devfest                        │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ Store Layer                                                     │ │
│  │ - DevFestNantesStore (interface)                               │ │
│  │ - GraphQLStore (Apollo implementation)                         │ │
│  │ - BookmarksStore (local bookmarks)                             │ │
│  │ - DevFestNantesStoreMocked (for testing)                       │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                          │                                             │
│                          ▼                                             │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ Domain & Model Layer                                            │ │
│  │ - Session, Speaker, Room, Agenda, Partner, Venue models       │ │
│  │ - Enums: SessionType, Category, Complexity, ContentLanguage   │ │
│  │ - Extensions & Mappers (GraphQL → Model)                      │ │
│  └─────────────────────────────────────────────────────────────────┘ │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐ │
│  │ Analytics Layer                                                 │ │
│  │ - AnalyticsService (abstract interface)                        │ │
│  │ - AnalyticsPage, AnalyticsEvent, AnalyticsParam enums         │ │
│  └─────────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────┬───────────────────────────┘
                                           │
                                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│         External Data Source - GraphQL API via Apollo Client         │
│  Endpoint: https://confetti-app.dev/graphql                          │
│  Queries: GetSessionsQuery, GetRoomsQuery, GetPartnersQuery, etc.   │
│  Caching: Normalized cache with custom CacheKeyGenerator            │
└──────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────┐
│         External Services                                             │
│  - Firebase Analytics, Crashlytics, Performance, Remote Config      │
│  - OpenFeedback integration (conditional)                           │
│  - Browser for external links                                       │
└──────────────────────────────────────────────────────────────────────┘
```

## Component Responsibilities

| Component | Responsibility | File |
|-----------|----------------|------|
| MainActivity | App entry point, main navigation setup, screen routing, analytics page tracking | `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt` |
| DevFestNantesApplication | App initialization, Timber setup, ApplicationInitializer orchestration | `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt` |
| Home Screen | Root Compose screen with nested navigation (Agenda, Speakers, Venue, About tabs) | `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt` |
| SessionViewModel | Loads and exposes session details; uses assisted injection for sessionId parameter | `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/session/SessionViewModel.kt` |
| AgendaViewModel | Manages agenda state, session filtering, and workshop/talk categorization | `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/AgendaViewModel.kt` |
| DevFestNantesStore | Interface abstracting data access; provides Flows for sessions, speakers, rooms, partners | `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/DevFestNantesStore.kt` |
| GraphQLStore | Store implementation using Apollo client; maps GraphQL responses to domain models | `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt` |
| BookmarksStore | Local bookmarking state; tracks user favorites across sessions | `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/BookmarksStore.kt` |
| FirebaseAnalyticsService | Analytics backend implementation using Firebase; events, page tracking | `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt` |
| AppModule | Dagger Hilt dependency injection configuration at SingletonComponent | `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt` |

## Pattern Overview

**Overall:** Clean Architecture with Kotlin Multiplatform (KMP) code sharing

**Key Characteristics:**
- **Layered separation**: Presentation (androidApp) → Domain/Data (shared) → External APIs
- **Reactive data flow**: Flows from Store propagate through ViewModels to Compose state
- **Dependency injection**: Dagger Hilt for constructor injection; assisted factories for parameterized ViewModels
- **KMP multiplatform**: Shared business logic in `shared` module; platform-specific implementations in `androidApp` (Android) and `iosApp` (iOS)
- **Compose-first UI**: Entire Android UI written with Jetpack Compose; no XML layouts

## Layers

**Presentation Layer:**
- Purpose: Render UI and respond to user interactions
- Location: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/`
- Contains: Compose screens, ViewModels, theme, components
- Depends on: Shared module (models, store, analytics)
- Used by: MainActivity and nested navigation

**Application/Service Layer:**
- Purpose: Coordinate features (analytics, bookmarks, settings); initialize subsystems
- Location: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/` and `core/`
- Contains: Service implementations, Initializers, performance monitoring, logging
- Depends on: Shared module, Firebase, Hilt, Timber
- Used by: UI screens, ViewModels, Application class

**Data/Store Layer:**
- Purpose: Abstract data access behind interfaces; provide reactive data streams
- Location: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/`
- Contains: Store interfaces (DevFestNantesStore, BookmarksStore), GraphQL implementation
- Depends on: Apollo client, domain models
- Used by: ViewModels in presentation layer

**Domain/Model Layer:**
- Purpose: Define business objects and operations
- Location: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/` and `domain/`
- Contains: Session, Speaker, Room, Agenda, Partner, Venue; extensions and utilities
- Depends on: Kotlin standard library, kotlinx.serialization
- Used by: All other layers

**Analytics Layer:**
- Purpose: Abstract analytics backend; provide event/page tracking interface
- Location: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/analytics/`
- Contains: AnalyticsService interface, event enums
- Depends on: Model layer (for domain objects)
- Used by: Screens, ViewModels; implemented by FirebaseAnalyticsService in androidApp

## Data Flow

### Primary Request Path: Loading a Session Detail

1. User taps a session in agenda list → `Home.onSessionClick()` called
2. MainActivity receives callback, logs analytics event `eventSessionOpened()`
3. MainActivity navigates to `"${Screen.Session.route}/{sessionId}"` route
4. SessionLayout Composable is rendered; extracts sessionId from nav args
5. SessionLayout creates SessionViewModel via assisted factory with sessionId
6. SessionViewModel.init() calls `store.getSession(sessionId)` (suspend function)
7. GraphQLStore executes `GetSessionQuery(sessionId)` via Apollo client
8. Apollo caches response using normalized cache; mappers convert GraphQL response to `Session` model
9. SessionViewModel emits session into `_session` StateFlow
10. Compose recomposes SessionLayout with session data
11. Analytics event logged via `analyticsService.pageEvent(AnalyticsPage.SESSION_DETAILS, route)` in MainActivity's OnDestinationChangedListener

**State Management:**
- UI state is MutableStateFlow in ViewModels, collected as State in Compose
- Store data is shared Flow; cached at Apollo client level
- BookmarksStore maintains local Set<String> of bookmarked session IDs

### Secondary Flow: Booking/Unbooking a Session

1. User toggles bookmark icon in SessionLayout
2. SessionLayout calls `bookmarksViewModel.setBookmarked(sessionId, true)`
3. BookmarksViewModel:
   - Calls `bookmarksStore.setBookmarked()` to update local state
   - Logs analytics event via `analyticsService.eventBookmark()`
4. BookmarksStore saves to SharedPreferences and updates internal StateFlow
5. All subscribers (Compose) recompose with updated state

### Navigation & Initialization Flow

1. DevFestNantesApplication.onCreate() called at app startup
2. Hilt instantiates AppModule dependencies
3. Timber.Tree is planted
4. ApplicationInitializer set is created (DataSharingInitializer, OpenFeedbackInitializer, PerformanceInitializer)
5. Each initializer is invoked sequentially in coroutineScope
6. MainActivity is launched after initialization
7. MainNavController added as destination listener for top-level page tracking

## Key Abstractions

**DevFestNantesStore:**
- Purpose: Provide reactive access to conference data
- Examples: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/DevFestNantesStore.kt`
- Pattern: Abstract interface with Flow-based API; builder pattern allows mock vs. real implementation

**AnalyticsService:**
- Purpose: Provide platform-agnostic analytics API
- Examples: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/analytics/AnalyticsService.kt`, `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt`
- Pattern: Interface in shared module; concrete Firebase implementation in androidApp

**Initializer:**
- Purpose: Allow subsystems to run setup code on app launch
- Examples: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/DataSharingInitializer.kt`, `OpenFeedbackInitializer.kt`, `PerformanceInitializer.kt`
- Pattern: Functional interface (suspend () -> Unit); collected into Set and orchestrated by Application

**ViewModelFactory:**
- Purpose: Provide parameters to ViewModels (e.g., sessionId)
- Examples: `SessionViewModelFactory`, `SpeakerViewModelFactory`
- Pattern: Dagger assisted injection with @AssistedFactory; enables testable ViewModel creation

## Entry Points

**MainActivity:**
- Location: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt`
- Triggers: App launch by Android system
- Responsibilities: Set up root NavHost, create main navigation graph, track top-level page analytics

**DevFestNantesApplication:**
- Location: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt`
- Triggers: Before MainActivity; app process creation
- Responsibilities: Hilt DI setup, Timber initialization, ApplicationInitializer orchestration

**SessionViewModel (and other parameterized ViewModels):**
- Location: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/session/SessionViewModel.kt`
- Triggers: When screen is navigated to with parameters
- Responsibilities: Load data for a specific session; expose data to UI via StateFlow

## Architectural Constraints

- **Threading:** Single-threaded event loop for Compose UI; coroutines with Dispatchers.Main.immediate for UI updates. IO and computation on Dispatchers.IO and Dispatchers.Default respectively (managed by CoroutinesDispatcherProvider).
- **Global state:** DevFestNantesStore (via DI), BookmarksStore (via DI), Firebase services (singleton). Application.coroutineScope is app-scoped for Initializers.
- **Circular imports:** None detected. Shared module does not depend on androidApp; androidApp imports from shared but not vice versa.
- **Platform-specific code:** GraphQL queries in `shared/src/commonMain/graphql/`; platform-specific initializers and services in androidApp or iosApp.
- **Navigation:** Only NavigationCompose used; no Fragment-based navigation. Nested NavControllers in Home screen for tab structure.

## Anti-Patterns

### Println for Error Logging

**What happens:** GraphQL errors printed to stdout via `println()` in `GraphQLStore.kt`
**Why it's wrong:** Println is invisible in Release builds; no stack traces; hard to debug in production
**Do this instead:** Use Timber logging with proper log levels: `Timber.e(exception, "Apollo error: %s", exception.message)`

### Direct Firebase References in ViewModels

**What happens:** Some ViewModels directly reference Firebase objects instead of injected abstractions
**Why it's wrong:** Couples code to Firebase implementation; hard to test; not KMP-compatible
**Do this instead:** Inject `AnalyticsService` (interface from shared) which abstracts Firebase; implement in androidApp

### State Management in ViewModel Init Block

**What happens:** SessionViewModel loads data in `init {}` block without error handling
**Why it's wrong:** No try-catch; if loading fails, UI shows null indefinitely with no error state
**Do this instead:** Add sealed class `UiState` (Loading, Success, Error) and emit state to StateFlow

## Error Handling

**Strategy:** Permissive with fallbacks; no user-visible error dialogs

**Patterns:**
- Store methods return `null` or empty collections on Apollo errors (see `GraphQLStore.getSession()`)
- Flows catch exceptions and emit empty collections or emptyMap()
- ViewModels don't expose error states; rely on null-safety in Compose
- Firebase operations don't throw; use Firebase's built-in error handling

## Cross-Cutting Concerns

**Logging:** Timber configured in Application.onCreate(). Debug builds use TimberTreeDebug; release uses TimberTreeRelease. See `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/logging/`.

**Validation:** Session filtering logic in `SessionFiltersService`. Date/time conversions in `DateUtils`. No input validation layer; assumes GraphQL backend enforces constraints.

**Authentication:** None required; public GraphQL API. OpenFeedback uses credentials stored in BuildConfig (see `OpenFeedbackInitializer`).

---

*Architecture analysis: 2026-09-11*
