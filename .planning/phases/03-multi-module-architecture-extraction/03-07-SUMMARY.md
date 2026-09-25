---
phase: 03-multi-module-architecture-extraction
plan: 07
subsystem: android-architecture
tags: [gradle, multi-module, jetpack-compose, hilt, feature-module, resources, navigation]

# Dependency graph
requires:
  - phase: 03-multi-module-architecture-extraction (03-06)
    provides: "proven feature-module extraction pattern (convention plugin, Hilt ViewModel in a leaf, own R, callbacks-only Route entry point), resources-gate.sh"
provides:
  - ":feature:settings module (com.gdgnantes.devfest.feature.settings.*): Settings (+ SettingsItem/SettingsTile*), DataCollection (DataCollectionSettingsScreen, DataCollectionAgreementDialog, DataCollectionViewModel), LegalScreen and consumer-owned DataCollectionSettingsService/Impl (D-03); callback-only SettingsRoute/DataCollectionSettingsRoute/LegalRoute entry points"
  - ":feature:speakers module (com.gdgnantes.devfest.feature.speakers.*): SpeakerViewModel (+ assisted factory), Speakers list, SpeakerLayout/SpeakerDetails/SpeakerSession detail; callback-only SpeakersRoute/SpeakerDetailRoute entry points, assisted-injection factory stays wired in :androidApp"
  - "screen_settings/screen_data_collection/settings_legal/screen_speaker promoted to :core:ui as 2+ consumer resources (D-17), discovered via Screen.kt's shared title references"
affects: [03-08-feature-agenda-session-detail, 03-09-umbrella-thinning]

# Actuals (#2632)
actuals:
  tokens: 12588
  tasks: 2
  commits: 4

# Plan commit ledger (#3968) — measured, not narrated
commits: 4
plan_head_before: 4569a035f4cd18bdea3f1e6e5e246240ff3099eb

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Two-commit module extraction (D-16) reused for feature modules with a root NavHost / assisted-injection touchpoint: pure git-mv with packages unchanged + module wiring/compile-enabling edits, then package-rename-only commit + new callback-only Route entry point(s)"
    - "BuildConfig lift pattern (D-07/D-15) reused: Settings' version display takes versionName/versionCode plain parameters instead of importing the app's BuildConfig; MainActivity supplies BuildConfig.VERSION_NAME/VERSION_CODE at the call site"
    - "App-owned assisted-injection factory across a feature boundary (D-07): MainActivity keeps ViewModelFactoryProvider + assistedViewModel { SpeakerViewModel.provideFactory(...) }; the constructed SpeakerViewModel instance is passed into SpeakerDetailRoute as a plain parameter, so the feature module never sees NavController or the EntryPoint"
    - "D-17 2+ consumer resources discovered via Screen.kt's shared title reference: screen_settings/screen_data_collection/settings_legal/screen_speaker all promoted to :core:ui because Screen.kt (androidApp) and the extracting feature both reference the same string id"

key-files:
  created:
    - feature/settings/build.gradle.kts
    - feature/settings/src/main/AndroidManifest.xml
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/Settings.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsItem.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsTileIcon.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsTileSubtitle.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsTileTexts.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsTileTitle.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsRoute.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionAgreementDialog.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionSettingsScreen.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionViewModel.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/legal/LegalScreen.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsService.kt
    - feature/settings/src/main/res/values/strings.xml
    - feature/settings/src/main/res/values-fr/strings.xml
    - feature/speakers/build.gradle.kts
    - feature/speakers/src/main/AndroidManifest.xml
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakerViewModel.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakersRoute.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/details/SpeakerDetails.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/details/SpeakerLayout.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/details/SpeakerSession.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt
  modified:
    - settings.gradle.dcl
    - androidApp/build.gradle.kts
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Screen.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/DataSharingInitializer.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - androidApp/src/main/res/values/strings.xml
    - androidApp/src/main/res/values-fr/strings.xml
    - core/ui/src/main/res/values/strings.xml
    - core/ui/src/main/res/values-fr/strings.xml

key-decisions:
  - "DataCollectionSettingsService/Impl placed in :feature:settings.services (consumer-owned, D-03): consumers are DataCollectionViewModel/DataCollectionSettingsScreen (settings) and DataSharingInitializer + AppModule (app) — lowest module both can reach is :feature:settings (app -> feature is allowed)"
  - "screen_settings, screen_data_collection, settings_legal and screen_speaker promoted to :core:ui mid-extraction (D-17 2+ consumer rule) because Screen.kt (androidApp) keeps referencing the same string ids the extracted screens display in their own top bars"
  - "DataCollectionAgreementDialog left as a plain public composable (no *Route wrapper) per plan instruction — it is invoked directly by MainActivity outside the NavHost, not as a composable() destination"
  - "SpeakerDetailRoute takes the already-constructed SpeakerViewModel as a parameter rather than owning ViewModel creation, so MainActivity's ViewModelFactoryProvider EntryPoint and assistedViewModel {} factory call stay entirely inside :androidApp (D-07 — no NavController or Hilt EntryPoint crosses into the feature)"

patterns-established:
  - "Assisted-injection ViewModel construction stays app-owned even when the destination screen moves to a feature module: the Route wrapper's viewModel parameter is the seam, not a factory function"

requirements-completed: []  # ARCH-02/ARCH-03 are phase-spanning; requirements.ready-ids reports 0/2 ready (sibling plan 03-08 not yet finished) — not marked complete here.

coverage:
  - id: D1
    description: ":feature:settings extracted as a devfest.android.feature leaf (Settings/SettingsItem/SettingsTile*, DataCollection screen+dialog+viewmodel, LegalScreen, DataCollectionSettingsService/Impl), repackaged to com.gdgnantes.devfest.feature.settings.*, with callback-only SettingsRoute/DataCollectionSettingsRoute/LegalRoute entry points wired into MainActivity; DataCollectionAgreementDialog stays a plain composable called directly by MainActivity"
    requirement: "ARCH-02"
    verification:
      - kind: automated_ui
        ref: "gradlew :feature:settings:assembleDebug :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:testDebugUnitTest detekt lint (Task 1 <verify>)"
        status: pass
      - kind: other
        ref: "git grep -n -E 'com\\.gdgnantes\\.devfest\\.androidapp|NavController|BuildConfig|(^|[^A-Za-z])Screen\\.' -- feature/settings -> empty"
        status: pass
      - kind: other
        ref: "diff of DataCollectionSettingsService.kt string literals vs origin/main -> PREFS-KEYS-OK"
        status: pass
    human_judgment: false
  - id: D2
    description: ":feature:speakers extracted as a devfest.android.feature leaf (SpeakerViewModel + assisted factory, Speakers list, SpeakerLayout/SpeakerDetails/SpeakerSession), repackaged to com.gdgnantes.devfest.feature.speakers.*, with callback-only SpeakersRoute/SpeakerDetailRoute entry points; MainActivity keeps the ViewModelFactoryProvider EntryPoint and assistedViewModel {} factory call, only switching the call site to SpeakerDetailRoute"
    requirement: "ARCH-02"
    verification:
      - kind: automated_ui
        ref: "gradlew :feature:speakers:assembleDebug :feature:settings:assembleDebug :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:testDebugUnitTest detekt lint (Task 2 <verify>)"
        status: pass
      - kind: other
        ref: "git grep -n -E 'com\\.gdgnantes\\.devfest\\.androidapp|NavController|BuildConfig' -- feature/speakers -> empty"
        status: pass
      - kind: other
        ref: "./gradlew :feature:speakers:dependencies --configuration releaseRuntimeClasspath -> no project :feature: entry"
        status: pass
    human_judgment: false
  - id: D3
    description: "Resources (screen_settings/screen_data_collection/settings_legal/screen_speaker as 2+ consumer -> :core:ui; remaining settings/datacollection/legal strings -> :feature:settings) partitioned by ownership rule (D-17), byte-identical to origin/main, no duplicate names across modules"
    requirement: "ARCH-03"
    verification:
      - kind: other
        ref: "resources-gate.sh -> RESOURCES-OK (run after each of the 4 commits)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Both feature extractions pushed and CI green on both workflows (android.yml, ios.yml)"
    requirement: "ARCH-02"
    verification:
      - kind: other
        ref: "CI-GREEN-BOTH — android.yml run 36149435733, ios.yml run 36149435522, HEAD c0a119e"
        status: pass
    human_judgment: false

# Metrics
duration: 43min
completed: 2026-09-25
status: complete
---

# Phase 3 Plan 7: Settings + Speakers Feature Extraction Summary

**Extracted `:feature:settings` (Settings/DataCollection/Legal + consumer-owned consent service) and `:feature:speakers` (list + detail, app-owned assisted factory) as devfest.android.feature leaves with callback-only Route entry points; both CI workflows green.**

## Performance

- **Duration:** 43 min
- **Started:** 2026-09-25T14:10:32Z
- **Completed:** 2026-09-25T14:53:33Z
- **Tasks:** 2
- **Files modified:** 36 (across 4 commits since 4569a03)

## Accomplishments
- `:feature:settings` created (Settings + SettingsItem/SettingsTile*, DataCollection screen/dialog/viewmodel, LegalScreen, DataCollectionSettingsService/Impl consumer-owned per D-03), repackaged to `com.gdgnantes.devfest.feature.settings.*`, with new callback-only `SettingsRoute`/`DataCollectionSettingsRoute`/`LegalRoute` entry points; MainActivity now calls all three, and `DataCollectionAgreementDialog` stays a plain composable invoked directly outside the NavHost
- `:feature:speakers` created (SpeakerViewModel + assisted factory, Speakers list, SpeakerLayout/SpeakerDetails/SpeakerSession), repackaged to `com.gdgnantes.devfest.feature.speakers.*`, with new callback-only `SpeakersRoute`/`SpeakerDetailRoute` entry points; Home.kt's Speakers tab calls `SpeakersRoute(...)` and MainActivity's speaker-detail destination calls `SpeakerDetailRoute(...)`, still owning `ViewModelFactoryProvider` and the `assistedViewModel { SpeakerViewModel.provideFactory(...) }` call
- Settings' version display (`Settings.kt`) takes `versionName`/`versionCode` plain parameters instead of importing the app's `BuildConfig`, matching the D-07/D-15 BuildConfig-lift pattern from 03-06
- `screen_settings`, `screen_data_collection`, `settings_legal` and `screen_speaker` promoted to `:core:ui` (D-17, 2+ consumer resources — `Screen.kt` in `:androidApp` and the extracting feature's own top bar both reference the same string id); the remaining settings/datacollection/legal-only strings moved to `feature/settings/res`
- Neither feature module depends on the other or on `:androidApp` (`ARCH-02`); both `./gradlew :feature:<name>:dependencies --configuration releaseRuntimeClasspath` checks show no `project :feature:` entry
- Both CI workflows (android.yml, ios.yml) green on the pushed HEAD (`c0a119e`)

## Task Commits

Each task was committed atomically (2 tasks, 4 commits — D-16 two-commit pattern per feature):

1. **Task 1: :feature:settings pure move (packages unchanged) + compile-enabling edits** - `4e58e78` (feat)
2. **Task 1: repackage :feature:settings to com.gdgnantes.devfest.feature.settings.*** - `e7ee844` (feat)
3. **Task 2: :feature:speakers pure move (packages unchanged)** - `a1dca84` (feat)
4. **Task 2: repackage :feature:speakers to com.gdgnantes.devfest.feature.speakers.*** - `c0a119e` (feat)

**Plan metadata:** committed as part of this SUMMARY commit (docs(03-07))

_Note: this plan had no TDD tasks — all four commits are `feat`._

## Files Created/Modified
- `feature/settings/build.gradle.kts` - devfest.android.feature leaf; firebase-bom/analytics/crashlytics/perf ktx (consent service deps), compose-material-icons-extended
- `feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/**` - Settings, SettingsItem, SettingsTile*, SettingsRoute (new), datacollection/{DataCollectionAgreementDialog,DataCollectionSettingsScreen,DataCollectionViewModel}, legal/LegalScreen, services/DataCollectionSettingsService
- `feature/settings/src/main/res/**` - settings/datacollection/legal single-consumer strings (values + values-fr)
- `feature/speakers/build.gradle.kts` - devfest.android.feature leaf; timber, compose-material-icons-extended
- `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/**` - SpeakerViewModel, SpeakersRoute (new), details/{SpeakerDetails,SpeakerLayout,SpeakerSession}, list/{Speakers,SpeakersViewModel}
- `core/ui/src/main/res/values/strings.xml`, `values-fr/strings.xml` - `screen_settings`/`screen_data_collection`/`settings_legal`/`screen_speaker` added (2+ consumer promotion, D-17)
- `androidApp/build.gradle.kts` - `implementation(project(":feature:settings"))`, `implementation(project(":feature:speakers"))`
- `androidApp/src/main/java/.../MainActivity.kt` - imports + call sites switched to `SettingsRoute`/`DataCollectionSettingsRoute`/`LegalRoute`/`SpeakerDetailRoute`; `BuildConfig` import added for the version values
- `androidApp/src/main/java/.../ui/screens/Home.kt` - Speakers tab call site switched to `SpeakersRoute`
- `androidApp/src/main/java/.../ui/screens/Screen.kt` - Settings/DataCollection/Legal/Speaker `title` fields repointed to `CoreUiR.string.*`
- `androidApp/src/main/java/.../core/DataSharingInitializer.kt`, `core/injection/AppModule.kt` - import-only updates to the new `feature.settings.services` package
- `androidApp/src/main/res/values/strings.xml`, `values-fr/strings.xml` - settings/datacollection/legal/speaker strings removed (moved out)
- `settings.gradle.dcl` - `include(":feature:settings")`, `include(":feature:speakers")`

## Decisions Made
- `DataCollectionSettingsService`/`Impl` placed in `:feature:settings.services` (D-03 consumer-owned placement) — lowest module reachable by both its app consumers (`DataSharingInitializer`, `AppModule`) and its feature consumers (`DataCollectionViewModel`, `DataCollectionSettingsScreen`)
- `screen_settings`/`screen_data_collection`/`settings_legal`/`screen_speaker` promoted to `:core:ui` — each discovered via `git grep` to be referenced both by `Screen.kt` (androidApp) and by the extracting feature's own composable, matching D-17's 2+ consumer rule exactly as `app_version` did in 03-06
- `SpeakerDetailRoute` takes the already-constructed `SpeakerViewModel` as a parameter (not a factory) so MainActivity's `ViewModelFactoryProvider` EntryPoint and `assistedViewModel {}` call stay entirely inside `:androidApp` — no Hilt EntryPoint or NavController crosses the feature boundary (D-07)
- `DataCollectionAgreementDialog` left as a plain public composable, not wrapped in a `*Route` function — it is invoked directly by MainActivity outside the `NavHost` (agreement dialog overlay), not as a `composable()` destination

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added missing Firebase Analytics/Crashlytics/Performance dependencies to feature/settings**
- **Found during:** Task 1 (`:feature:settings:kspDebugKotlin`)
- **Issue:** `DataCollectionSettingsServiceImpl`'s constructor references `FirebaseAnalytics`/`FirebaseCrashlytics`/`FirebasePerformance` — the `devfest.android.feature` convention plugin only wires core-module + material3 dependencies, not Firebase; these were previously reached transitively inside the monolithic `androidApp` module
- **Fix:** Added `platform(libs.firebase.bom)`, `libs.firebase.analytics.ktx`, `libs.firebase.crashlytics.ktx`, `libs.firebase.perf.ktx` to `feature/settings/build.gradle.kts`
- **Files modified:** `feature/settings/build.gradle.kts`
- **Verification:** `:feature:settings:kspDebugKotlin`/`kspReleaseKotlin` succeed; full verify (assembleDebug/Release/testDebugUnitTest/detekt/lint) green
- **Committed in:** `4e58e78` (Task 1 commit 1)

**2. [Rule 3 - Blocking] Added compose-material-icons-extended to feature/settings and feature/speakers**
- **Found during:** Task 1 and Task 2 (initial `:feature:settings:compileDebugKotlin` / `:feature:speakers:compileDebugKotlin`)
- **Issue:** `androidx.compose.material.icons.Icons.Filled.ArrowBack`/`Lock`/`Clear` (core icons set) failed to resolve — the `devfest.android.feature` convention plugin does not bundle `material-icons-core` or `material-icons-extended` by default, and the version catalog has no standalone `material-icons-core` alias (matches the 03-06 finding for `:feature:venue`)
- **Fix:** Added `libs.androidx.compose.material.icons.extended` to `feature/settings/build.gradle.kts` and `feature/speakers/build.gradle.kts`
- **Files modified:** `feature/settings/build.gradle.kts`, `feature/speakers/build.gradle.kts`
- **Verification:** Both modules compile cleanly; full verify green
- **Committed in:** `4e58e78` (settings), `a1dca84` (speakers)

**3. [Rule 1 - Bug] Fixed import-ordering (detekt ImportOrdering) after R-import edits**
- **Found during:** Task 1 (`:feature:settings:detekt`) and Task 2 (repackage commit, `SpeakerLayout.kt`)
- **Issue:** Manually inserting/reordering R-class and cross-package imports broke detekt's required lexicographic import order in `DataCollectionSettingsScreen.kt`, `Settings.kt` and `SpeakerLayout.kt`
- **Fix:** Re-sorted the affected import blocks alphabetically (aliases last)
- **Files modified:** `feature/settings/src/main/java/.../datacollection/DataCollectionSettingsScreen.kt`, `feature/settings/src/main/java/.../Settings.kt`, `feature/speakers/src/main/java/.../details/SpeakerLayout.kt`
- **Verification:** `:feature:settings:detekt` / full verify passes
- **Committed in:** `4e58e78`, `c0a119e`

---

**Total deviations:** 3 auto-fixed (2 blocking dependencies, 1 bug/lint)
**Impact on plan:** All fixes are mechanical compile/lint corrections required by the module split itself; no scope creep, no behavior change.

## Issues Encountered
None.

## Authentication Gates
None.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Feature-module extraction pattern proven a third time, including two touchpoints not exercised by 03-06: a root-`NavHost` screen invoked outside the `NavHost` (the consent dialog) and an assisted-injection `ViewModel` constructed in `:androidApp` and passed as a plain parameter into the feature's Route function
- `screen_settings`/`screen_data_collection`/`settings_legal`/`screen_speaker` now live in `:core:ui`; 03-08's agenda/session-detail extraction should check for any remaining shared title strings before assuming `Screen.kt`'s title fields are all resolved
- ARCH-02/ARCH-03 remain open (phase-spanning); `requirements.ready-ids` confirms 0/2 ready at this point (03-08 not yet finished), correctly deferred
- No blockers for 03-08

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-25*

## Self-Check: PASSED

- FOUND: feature/settings/build.gradle.kts
- FOUND: feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsRoute.kt
- FOUND: feature/speakers/build.gradle.kts
- FOUND: feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakersRoute.kt
- FOUND commit 4e58e78 in git log
- FOUND commit e7ee844 in git log
- FOUND commit a1dca84 in git log
- FOUND commit c0a119e in git log
- Plan commit ledger: plan_head_before 4569a03, commits measured = 4 (git rev-list --count 4569a03..HEAD)
- CI-GREEN-BOTH confirmed: android.yml run 36149435733 (success), ios.yml run 36149435522 (success), HEAD c0a119e
