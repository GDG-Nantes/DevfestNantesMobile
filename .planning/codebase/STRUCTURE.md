# Codebase Structure

**Analysis Date:** 2026-09-11

## Directory Layout

```
DevFest_Nantes/
├── androidApp/                                    # Android app module
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── java/com/gdgnantes/devfest/androidapp/
│       │   │   ├── MainActivity.kt                # App entry point, root navigation
│       │   │   ├── DevFestNantesApplication.kt   # App initialization
│       │   │   ├── core/                          # Core infrastructure
│       │   │   │   ├── injection/                 # Dagger Hilt modules
│       │   │   │   │   ├── AppModule.kt           # Dependency injection setup
│       │   │   │   │   └── AppScope.kt            # Custom scope annotation
│       │   │   │   ├── logging/                   # Timber tree implementations
│       │   │   │   ├── performance/               # Firebase Performance monitoring
│       │   │   │   ├── CoroutinesDispatcherProvider.kt
│       │   │   │   ├── ApplicationInitializer.kt  # Interface for app startup
│       │   │   │   ├── DataSharingInitializer.kt
│       │   │   │   └── OpenFeedbackInitializer.kt
│       │   │   ├── services/                      # Business services
│       │   │   │   ├── FirebaseAnalyticsService.kt  # Analytics implementation
│       │   │   │   ├── BookmarksStoreImpl.kt       # Bookmarks persistence
│       │   │   │   ├── SessionFiltersService.kt
│       │   │   │   └── DataCollectionSettingsService.kt
│       │   │   ├── ui/                            # Presentation layer
│       │   │   │   ├── BookmarksViewModel.kt
│       │   │   │   ├── UiState.kt
│       │   │   │   ├── screens/
│       │   │   │   │   ├── Home.kt                # Root screen with tabs
│       │   │   │   │   ├── Screen.kt              # Route definitions
│       │   │   │   │   ├── agenda/                # Agenda tab
│       │   │   │   │   │   ├── Agenda.kt
│       │   │   │   │   │   ├── AgendaViewModel.kt
│       │   │   │   │   │   ├── AgendaRow.kt
│       │   │   │   │   │   ├── AgendaColumn.kt
│       │   │   │   │   │   ├── AgendaPager.kt
│       │   │   │   │   │   └── SessionFiltersDrawer.kt
│       │   │   │   │   ├── session/               # Session detail screen
│       │   │   │   │   │   ├── SessionLayout.kt
│       │   │   │   │   │   └── SessionViewModel.kt
│       │   │   │   │   ├── speakers/              # Speakers tab & details
│       │   │   │   │   │   ├── list/
│       │   │   │   │   │   │   └── Speakers.kt
│       │   │   │   │   │   ├── details/
│       │   │   │   │   │   │   ├── SpeakerLayout.kt
│       │   │   │   │   │   │   └── SpeakerSession.kt
│       │   │   │   │   │   └── SpeakerViewModel.kt
│       │   │   │   │   ├── venue/                 # Venue tab
│       │   │   │   │   │   ├── Venue.kt
│       │   │   │   │   │   ├── VenueDetails.kt
│       │   │   │   │   │   └── plan/
│       │   │   │   │   ├── about/                 # About tab (partners, sponsors)
│       │   │   │   │   │   ├── About.kt
│       │   │   │   │   │   └── partners/
│       │   │   │   │   │       └── Partners.kt
│       │   │   │   │   ├── settings/              # Settings screen
│       │   │   │   │   │   └── Settings.kt
│       │   │   │   │   ├── datacollection/        # Data sharing/GDPR
│       │   │   │   │   │   ├── DataCollectionSettingsScreen.kt
│       │   │   │   │   │   └── DataCollectionAgreementDialog.kt
│       │   │   │   │   └── legal/                 # Legal screen
│       │   │   │   │       └── LegalScreen.kt
│       │   │   │   ├── components/                # Reusable UI components
│       │   │   │   │   ├── SocialIcon.kt
│       │   │   │   │   ├── GithubCard.kt
│       │   │   │   │   ├── SessionComplexity.kt
│       │   │   │   │   ├── LoadingLayout.kt
│       │   │   │   │   ├── SessionCategory.kt
│       │   │   │   │   └── appbars/
│       │   │   │   │       ├── TopAppBar.kt
│       │   │   │   │       └── BottomAppBar.kt
│       │   │   │   └── theme/                     # Theming
│       │   │   │       └── DevFestNantesTheme.kt
│       │   │   └── utils/                         # Utilities
│       │   │       ├── DateUtils.kt
│       │   │       ├── PagerTab.kt
│       │   │       ├── NavigationUtils.kt
│       │   │       ├── SessionFilter.kt
│       │   │       ├── StringExtensions.kt
│       │   │       └── AssistedViewModelUtils.kt
│       │   ├── res/                               # Android resources
│       │   │   ├── drawable/                      # Vector drawables
│       │   │   ├── values/                        # Strings, colors, dimens
│       │   │   ├── values-fr/                     # French translations
│       │   │   ├── mipmap-*/                      # App icons
│       │   │   └── xml/                           # Preferences, backup
│       │   ├── assets/                            # Raw assets
│       │   ├── AndroidManifest.xml
│       │   ├── test/                              # Unit tests
│       │   └── androidTest/                       # Instrumented tests
│       │
├── shared/                                        # KMP shared module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/gdgnantes/devfest/
│       │   ├── model/                             # Domain models
│       │   │   ├── Session.kt                     # Conference session
│       │   │   ├── Speaker.kt                     # Session speaker
│       │   │   ├── Room.kt                        # Venue room
│       │   │   ├── Agenda.kt                      # Day agenda
│       │   │   ├── Partner.kt                     # Sponsors/partners
│       │   │   ├── Venue.kt                       # Venue details
│       │   │   ├── ScheduleSlot.kt                # Time slot
│       │   │   ├── Category.kt                    # Session category enum
│       │   │   ├── SessionType.kt                 # Session type enum
│       │   │   ├── SessionLanguage.kt             # Language enum
│       │   │   ├── Complexity.kt                  # Level enum
│       │   │   ├── ContentLanguage.kt             # Content language enum
│       │   │   ├── WebLinks.kt                    # External URLs
│       │   │   ├── PartnerCategory.kt
│       │   │   ├── SocialItem.kt
│       │   │   ├── stubs/                         # Test data
│       │   │   │   ├── RoomStubs.kt
│       │   │   │   ├── SessionStubs.kt
│       │   │   │   ├── SpeakerStubs.kt
│       │   │   │   ├── CategoryStubs.kt
│       │   │   │   └── StoreStubs.kt
│       │   │   └── buildVenueStub()               # Helper functions
│       │   │
│       │   ├── domain/                            # Domain layer
│       │   │   └── RoomSortIndex.kt
│       │   │
│       │   ├── store/                             # Data access layer
│       │   │   ├── DevFestNantesStore.kt          # Main data interface
│       │   │   ├── DevFestNantesStoreBuilder.kt   # Builder for store creation
│       │   │   ├── DevFestNantesStoreMocked.kt    # Mock implementation for testing
│       │   │   ├── BookmarksStore.kt              # Local bookmarks interface
│       │   │   └── graphql/
│       │   │       ├── GraphQLStore.kt            # Apollo GraphQL implementation
│       │   │       ├── Apollo.kt                  # Apollo client config
│       │   │       ├── ApolloCache.kt             # Cache setup
│       │   │       └── Mappers.kt                 # GraphQL response mappers
│       │   │
│       │   ├── analytics/                         # Analytics layer
│       │   │   ├── AnalyticsService.kt            # Service interface
│       │   │   ├── AnalyticsPage.kt               # Page tracking enum
│       │   │   ├── AnalyticsEvent.kt              # Event enum
│       │   │   └── AnalyticsParam.kt              # Parameter enum
│       │   │
│       │   ├── utils/                             # Utilities
│       │   │   └── SessionExtensions.kt
│       │   │
│       │   └── graphql/                           # Generated GraphQL code
│       │       ├── GetSessionQuery.kt
│       │       ├── GetSessionsQuery.kt
│       │       ├── GetRoomsQuery.kt
│       │       ├── GetSpeakersQuery.kt
│       │       ├── GetPartnerGroupsQuery.kt
│       │       ├── GetVenueQuery.kt
│       │       └── ...other generated files
│       │
│       ├── androidMain/                           # Android-specific code
│       │   ├── kotlin/com/gdgnantes/devfest/...
│       │   └── AndroidManifest.xml
│       │
│       ├── iosMain/                               # iOS-specific code
│       │   ├── kotlin/com/gdgnantes/devfest/...
│       │   └── AndroidManifest.xml
│       │
│       ├── commonTest/kotlin/                     # Shared tests
│       │   └── com/gdgnantes/devfest/store/
│       │       └── ...test classes
│       │
│       └── jvmTest/kotlin/                        # JVM-specific tests
│           └── com/gdgnantes/devfest/store/graphql/
│               └── ...GraphQL tests
│
├── shared-ui/                                     # Shared UI module (placeholder)
│   ├── build.gradle.kts
│   └── src/                                       # Currently empty
│
├── buildSrc/                                      # Build logic
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── AndroidSdk.kt                          # SDK versions
│       └── ...other build utilities
│
├── linters/                                       # Linting configuration
│   └── detekt-config.yml                          # Detekt rules
│
├── scripts/                                       # Build & provisioning scripts
│   ├── build_provisioning_profiles.sh
│   └── ...other scripts
│
├── documentation/                                 # Project documentation
│
├── gradle/                                        # Gradle wrapper files
│
├── iosApp/                                        # iOS app module (not in scope)
│
├── build.gradle.kts                              # Root build file
├── settings.gradle.kts                           # Module inclusion
├── gradle.properties
├── gradlew                                        # Gradle wrapper
│
├── AGENTS.md                                      # AI agent instructions
├── CLAUDE.md                                      # Project AI configuration
├── CONTRIBUTING.md                                # Contribution guidelines
├── README.md
├── CHANGELOG.md
└── LICENSE
```

## Directory Purposes

**androidApp:**
- Purpose: Android app implementation; UI, services, dependency injection
- Contains: Jetpack Compose screens, ViewModels, services, theme, resources
- Key files: `MainActivity.kt`, `DevFestNantesApplication.kt`, `AppModule.kt`

**shared:**
- Purpose: Kotlin Multiplatform module shared by Android and iOS
- Contains: Data models, store interfaces/implementations, domain logic, analytics, GraphQL queries
- Key files: `DevFestNantesStore.kt`, `GraphQLStore.kt`, `Session.kt`, `Speaker.kt`

**shared-ui:**
- Purpose: Shared UI components for multiple platforms (not currently populated)
- Contains: Currently empty placeholder
- Key files: None

**buildSrc:**
- Purpose: Gradle build logic and shared version constants
- Contains: `AndroidSdk.kt` (SDK versions), plugin configuration
- Key files: `AndroidSdk.kt`

**linters:**
- Purpose: Static analysis and linting configuration
- Contains: Detekt rules configuration
- Key files: `detekt-config.yml`

**scripts:**
- Purpose: Build and provisioning automation
- Contains: Shell scripts for iOS provisioning, build tasks
- Key files: `build_provisioning_profiles.sh`

## Key File Locations

**Entry Points:**
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt`: Main Activity; sets up root NavHost and navigation graph
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt`: App class; initializes Hilt, Timber, ApplicationInitializers

**Configuration:**
- `build.gradle.kts`: Root build file
- `settings.gradle.kts`: Module inclusion (androidApp, shared)
- `buildSrc/src/main/kotlin/AndroidSdk.kt`: SDK version constants
- `gradle.properties`: Gradle properties

**Core Logic (Shared):**
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/DevFestNantesStore.kt`: Data access interface
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt`: GraphQL implementation
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/analytics/AnalyticsService.kt`: Analytics interface

**Testing:**
- `androidApp/src/test/`: JUnit unit tests
- `androidApp/src/androidTest/`: Espresso instrumented tests
- `shared/src/commonTest/`: Shared Kotlin tests
- `shared/src/jvmTest/`: JVM-specific tests (GraphQL mocking)

## Naming Conventions

**Files:**
- Screens: `{ScreenName}Screen.kt` or `{ScreenName}.kt` (e.g., `SessionLayout.kt`, `Home.kt`)
- ViewModels: `{Feature}ViewModel.kt` (e.g., `SessionViewModel.kt`, `AgendaViewModel.kt`)
- Services: `{Feature}Service.kt` or `{Feature}ServiceImpl.kt` (e.g., `FirebaseAnalyticsService.kt`)
- Models: Singular noun, PascalCase (e.g., `Session.kt`, `Speaker.kt`)
- Utilities: `{Feature}Utils.kt` or `{Feature}Extensions.kt` (e.g., `DateUtils.kt`, `SessionExtensions.kt`)

**Directories:**
- Feature screens: Under `ui/screens/{featureName}/` (e.g., `ui/screens/agenda/`, `ui/screens/session/`)
- Services: Under `services/`
- Core infrastructure: Under `core/`
- Models: Under `model/`
- Store/data access: Under `store/`
- Utilities: Under `utils/`

**Types:**
- Enums: PascalCase, singular (e.g., `SessionType`, `Category`)
- Sealed classes for state: `{Feature}State` (not currently used; uses null-safety instead)
- Interfaces: No prefix or "I" (e.g., `DevFestNantesStore`, `AnalyticsService`)

## Where to Add New Code

**New Feature Screen (e.g., Partnerships):**
- Primary code: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/partnerships/` 
  - Create `Partnerships.kt` (Compose screen)
  - Create `PartnershipsViewModel.kt` if needed
- Add route to `Screen.kt`
- Add navigation in `MainActivity.kt`
- Tests: `androidApp/src/androidTest/java/com/gdgnantes/devfest/android/ui/screens/partnerships/`

**New Data Model (e.g., Sponsor):**
- Definition: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/Sponsor.kt`
- Add to store interface: `DevFestNantesStore.kt` (add property or method)
- Update `GraphQLStore.kt` to fetch from API
- Add GraphQL query: `shared/src/commonMain/graphql/GetSponsors.graphql`
- Generated code will appear in `shared/src/commonMain/kotlin/com/gdgnantes/devfest/graphql/`

**New Service (e.g., Notification service):**
- Interface: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/.../{Feature}Service.kt`
- Android Implementation: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/{Feature}ServiceImpl.kt`
- iOS Implementation: `iosApp/.../{Feature}ServiceImpl.kt`
- DI Binding: Add to `androidApp/.../core/injection/AppModule.kt` using `@Binds`

**New ViewModel:**
- Location: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/{featureName}/{Feature}ViewModel.kt`
- Annotate with `@HiltViewModel` if no parameters; use `@AssistedInject` + `@AssistedFactory` if requires parameters
- Inject `DevFestNantesStore`, services, and `AnalyticsService`
- Expose data via `Flow` or `StateFlow`

**Utilities:**
- Reusable functions: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/utils/{Feature}Utils.kt`
- Extensions (non-Android): `shared/src/commonMain/kotlin/com/gdgnantes/devfest/utils/{Feature}Extensions.kt`
- Extensions (Android-specific): `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/utils/{Feature}Extensions.kt`

**Components:**
- Reusable Compose components: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/components/{ComponentName}.kt`
- App bars, buttons, cards, dialogs go here if used across multiple screens

**Test Data (Stubs):**
- Shared test models: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/stubs/{Feature}Stubs.kt`
- Factory functions that return realistic data for testing

## Special Directories

**generated (GraphQL):**
- Purpose: Apollo code generation output
- Location: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/graphql/`
- Generated: Yes (by Apollo plugin from `.graphql` files)
- Committed: Yes (checked in; do not manually edit)
- Regenerate: Run `./gradlew :shared:generateApolloSources`

**res/ (Android resources):**
- Purpose: Strings, colors, drawable assets, preferences XML
- Location: `androidApp/src/main/res/`
- Contains: `values/`, `drawable/`, `mipmap-*/`, `xml/`
- Committed: Yes (except generated previews)

**.gradle / build/ (Build artifacts):**
- Purpose: Gradle cache and compiled outputs
- Generated: Yes
- Committed: No (in .gitignore)
- Safe to delete: Yes; rebuild will regenerate

**iosApp (iOS module):**
- Purpose: iOS app; mirrors androidApp structure but uses SwiftUI instead of Compose
- Not in scope for this architecture analysis but follows same layer pattern

---

*Structure analysis: 2026-09-11*
