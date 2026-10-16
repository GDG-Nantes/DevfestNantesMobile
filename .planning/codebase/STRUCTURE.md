---
last_mapped_commit: f6a603391f893d8f6bd0964cfcca500620b6a072
last_mapped_at: 2026-10-16
---
# Codebase Structure

**Analysis Date:** 2026-10-16

## Directory Layout

```
DevFestNantes/
├── androidApp/              # Android application module (Compose shell, NavHost, Koin setup)
├── shared/                  # KMP framework consumed by iOS; initKoin composition root
├── shared-ui/               # Placeholder directory (no tracked sources)
├── core/                    # Shared KMP and Android libraries
│   ├── model/               # Domain models (KMP commonMain)
│   ├── network/             # Apollo GraphQL client, schema and operations (KMP)
│   ├── data/                # DevFestNantesStore, GraphQL store, bookmarks (KMP)
│   ├── analytics/           # AnalyticsService + platform impls (KMP)
│   ├── testing/             # Shared test utilities (KMP)
│   └── ui/                  # Compose theme, components, BookmarksViewModel (Android)
├── feature/                 # Feature modules, one per screen area (Android)
│   ├── agenda/
│   ├── speakers/
│   ├── session-detail/
│   ├── venue/
│   ├── about/
│   └── settings/
├── build-logic/             # Included build: convention plugins
│   └── convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/
├── buildSrc/                # Additional Gradle build sources
├── gradle/                  # Gradle wrapper and version catalog
├── iosApp/                  # iOS Xcode project (Swift)
│   └── iosApp/              # Swift sources: Agenda/, UI/, Model/, Analytics/, Extension/, SwiftGen/, Resources/
├── documentation/           # Project documentation
├── linters/                 # Lint configuration
├── scripts/                 # Helper scripts
├── .github/                 # Workflows and issue templates
├── .planning/               # GSD planning artifacts (codebase maps, roadmap, phases)
├── settings.gradle.dcl      # Gradle settings: included modules and plugin management
└── build.gradle.kts         # Root build script
```

## Directory Purposes

**`androidApp/`:**
- Purpose: The launcher application module.
- Contains: `src/main/java/com/gdgnantes/devfest/androidapp/` (activity, application class, Koin modules, initializers, screens), `src/main/res/` (app strings, `values-night`, `mipmap-anydpi-v26`), `src/test/java/.../di/` (Koin graph tests).
- Key files: `MainActivity.kt`, `DevFestNantesApplication.kt`, `ui/screens/Screen.kt`, `ui/screens/Home.kt`, `di/AppModule.kt`, `di/AndroidModules.kt`.

**`shared/`:**
- Purpose: KMP module producing the iOS framework; holds the shared Koin composition root.
- Contains: `src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt`, `src/iosMain/`, `src/androidMain/`, test source sets `src/jvmTest/`, `src/iosTest/`.

**`core/model/`:**
- Purpose: Pure domain types.
- Contains: `src/commonMain/kotlin/com/gdgnantes/devfest/core/model/` (types, `stubs/` for previews and tests, `utils/`).

**`core/network/`:**
- Purpose: GraphQL transport.
- Contains: `src/commonMain/kotlin/com/gdgnantes/devfest/core/network/` (Apollo client, `di/`), `src/commonMain/graphql/` (`schema.graphqls`, `operations.graphql`, `extra.graphqls`).

**`core/data/`:**
- Purpose: Data facade over network and local state.
- Contains: `src/commonMain/kotlin/com/gdgnantes/devfest/core/data/` (store interface, builder, GraphQL store, mocked store, bookmarks, `graphql/Mappers.kt`, `domain/`, `di/`), `src/commonTest/kotlin/.../graphql/` (mapper and store tests).

**`core/analytics/`:**
- Purpose: Analytics and performance abstraction.
- Contains: `src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/` (incl. `di/`), `src/androidMain/kotlin/.../performance/`.

**`core/testing/`:**
- Purpose: Test doubles and helpers shared across modules.
- Contains: `src/commonMain/kotlin/.../core/testing/`, `src/commonTest/kotlin/.../core/testing/`, `src/androidMain/kotlin/.../core/testing/`.

**`core/ui/`:**
- Purpose: Android Compose design system and shared UI state.
- Contains: `src/main/java/com/gdgnantes/devfest/core/ui/` with `theme/`, `components/`, `components/appbars/`, `utils/`, `di/`; `src/main/res/drawable/`.

**`feature/<name>/`:**
- Purpose: One screen area each. Same internal layout in every feature.
- Contains: `src/main/java/com/gdgnantes/devfest/feature/<name>/` with `*Route.kt`, screens, `*ViewModel.kt`, `di/`, and optional `components/`, `services/`, `utils/`, sub-screens; `src/test/java/.../` unit tests; `src/main/res/` for feature drawables and strings.
- Subpackages found:
  - `feature/agenda/`: `Agenda*.kt`, `SessionFiltersDrawer.kt`, `di/`, `services/`, `utils/`
  - `feature/speakers/`: `list/`, `details/`, `di/`
  - `feature/session-detail/`: `components/`, `utils/`, `di/`, feedback files (`FeedbackForm.kt`, `FallbackFeedbackForm.kt`, `FeedbackFlags.kt`, `OpenFeedbackConfig.kt`)
  - `feature/venue/`: `plan/`, `utils/`, `di/`
  - `feature/about/`: `components/`, `partners/`, `di/`
  - `feature/settings/`: `datacollection/`, `legal/`, `services/`, `di/`

**`build-logic/convention/`:**
- Purpose: Convention plugins used by every module's `build.gradle.kts`.
- Contains: `src/main/kotlin/com/gdgnantes/devfest/buildlogic/` (10 plugin/config sources).

**`iosApp/iosApp/`:**
- Purpose: iOS application code.
- Contains: `Agenda/`, `UI/` (with `Common/`, `Speakers/`, `Venue/`, `About/`), `Model/Data/`, `Analytics/`, `Extension/`, `SwiftGen/`, `Resources/` (including `fr.lproj`). Tests in `iosApp/DevFest NantesTests/`.

## Key File Locations

**Entry Points:**
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt`: Android launcher activity and NavHost.
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt`: Application class, Koin startup.
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt`: shared Koin entry point.

**Configuration:**
- `settings.gradle.dcl`: included modules.
- `build.gradle.kts`: root build script.
- `gradle.properties`: Gradle properties.
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/`: convention plugins.

**Core Logic:**
- `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStore.kt`: data facade interface.
- `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStore.kt`: production store.
- `core/network/src/commonMain/graphql/operations.graphql`: GraphQL queries.
- `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/`: domain types.

**Testing:**
- `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/`: Koin graph tests.
- `feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/`: agenda tests.
- `feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/`: session detail tests.
- `core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/`: data layer tests.
- `core/testing/`: shared test helpers.
- `iosApp/DevFest NantesTests/`: iOS tests.

## Naming Conventions

**Files:**
- PascalCase Kotlin files named after their primary declaration: `AgendaViewModel.kt`, `SessionFiltersDrawer.kt`.
- Entry composables suffixed `Route`: `AgendaRoute.kt`, `SettingsRoute.kt`, `VenueRoute.kt`, `SpeakersRoute.kt`.
- ViewModels suffixed `ViewModel`; Koin modules suffixed `Module` in a `di/` folder: `feature/agenda/.../di/AgendaModule.kt`.
- Service implementations suffixed `Impl` next to their interface: `SessionFiltersService.kt` / `SessionFiltersServiceImpl`.
- Platform-specific source sets use `androidMain`, `iosMain`, `commonMain`, `jvmTest`, `iosTest` naming.

**Directories:**
- Reverse-domain package layout: `com/gdgnantes/devfest/<layer-or-feature>/...`.
- Modules are kebab-case in Gradle paths (`:feature:session-detail`) and directories (`feature/session-detail`), but Kotlin packages drop the hyphen (`feature/sessiondetail`).
- Feature packages use lowercase single words (`feature/agenda`, `feature/speakers`).

## Where to Add New Code

**New feature screen:**
- Module: create `feature/<name>/` with `build.gradle.kts` using the convention plugin, and register it in `settings.gradle.dcl` as `include(":feature:<name>")`.
- Package: `feature/<name>/src/main/java/com/gdgnantes/devfest/feature/<name>/`.
- Entry point: `<Name>Route.kt` composable; ViewModel `<Name>ViewModel.kt`; Koin module `di/<Name>Module.kt`.
- Wire it: add the route to `MainActivity`'s `NavHost` and a `Screen` subclass in `androidApp/.../ui/screens/Screen.kt` if it is a top-level tab; add its module to `androidModules` in `androidApp/.../di/AndroidModules.kt`.
- Tests: `feature/<name>/src/test/java/com/gdgnantes/devfest/feature/<name>/`.

**New feature sub-screen or component:**
- Implementation: `feature/<name>/src/main/java/com/gdgnantes/devfest/feature/<name>/components/` (or the closest existing subpackage).

**New data access method:**
- Interface: `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStore.kt`.
- Implementations: `GraphQLStore.kt` and `DevFestNantesStoreMocked.kt` in the same package.
- Mapping: `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/Mappers.kt`.
- GraphQL: add operations to `core/network/src/commonMain/graphql/operations.graphql`.

**New domain model:**
- `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/`; add a stub to `stubs/` for previews and tests.

**New shared Compose component:**
- `core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/` (app bars go under `components/appbars/`).

**New Compose theme token:**
- `core/ui/src/main/java/com/gdgnantes/devfest/core/ui/theme/`.

**New analytics event:**
- Interface and common code: `core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/`; Android implementation under `core/analytics/src/androidMain/`.

**Utilities:**
- Shared Compose helpers: `core/ui/src/main/java/com/gdgnantes/devfest/core/ui/utils/`.
- Feature-local helpers: `feature/<name>/src/main/java/com/gdgnantes/devfest/feature/<name>/utils/`.
- Shared domain helpers: `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/utils/`.

## Special Directories

**`.planning/`:**
- Purpose: GSD planning artifacts, including `codebase/` maps.
- Generated: Yes (by GSD commands).
- Committed: Yes.

**`build-logic/`:**
- Purpose: Included Gradle build with convention plugins.
- Generated: No.
- Committed: Yes.

**`.gradle/`, `.kotlin/`, build output directories:**
- Purpose: Build caches.
- Generated: Yes.
- Committed: No.

**`core/ui/src/main/res/`, `androidApp/src/main/res/`, `feature/*/src/main/res/`:**
- Purpose: Android resources (drawables, strings, values-night).
- Generated: No.
- Committed: Yes.

---

*Structure analysis: 2026-10-16*
