# Phase 3: Multi-Module Architecture Extraction - Pattern Map

**Mapped:** 2026-09-22
**Files analyzed:** 12 new Gradle modules + ~5 build-logic convention-plugin files + ~90 moved/repackaged Kotlin files (grouped by pattern below) + 3 modified integration files
**Analogs found:** 6 categories with strong in-repo analogs / 3 categories with no analog (build-logic tooling is genuinely new to this repo)

**How to read this file:** Phase 3 is a structural move+repackage, not new feature code, so file-by-file mapping (one row per one of ~90 moved files) would be noise. Instead this map groups the new-file types the planner must actually author (convention plugins, module build files, manifests) and the moved-file *shapes* (ViewModel, service impl, store, composable, test) that repeat across the module split, each with one concrete in-repo analog. The planner should apply each pattern block to every file in its file-list group (see `## File Classification`).

## File Classification

| New/Modified File (or group) | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `build-logic/settings.gradle.kts` (new) | config | batch (build config) | `settings.gradle.dcl` (root) | role-match (DCL vs KTS, but same job: repositories + includes) |
| `build-logic/convention/build.gradle.kts` (new) | config | batch | `shared/build.gradle.kts` plugin block shape | role-match |
| `build-logic/convention/src/.../*ConventionPlugin.kt` (5 new: Kmp, AndroidLibrary, AndroidFeature, AndroidApplication, Detekt) | config (Gradle plugin) | transform (declarative config → applied plugins) | **No analog** — `buildSrc/src/main/java/Dependencies.kt` is a plain `object`, not a `Plugin<Project>` | no analog |
| `core/model/build.gradle.kts`, `core/network/build.gradle.kts`, `core/data/build.gradle.kts`, `core/analytics/build.gradle.kts`, `core/testing/build.gradle.kts` (5 new, KMP-typed) | config | batch | `shared/build.gradle.kts` | exact (same `com.android.kotlin.multiplatform.library` + KMP plugin block being split apart) |
| `core/ui/build.gradle.kts` (new, plain Android library + Compose) | config | batch | `androidApp/build.gradle.kts` (Compose/detekt block only — namespace/applicationId parts don't apply) | role-match |
| `feature/{agenda,speakers,venue,session-detail,about,settings}/build.gradle.kts` (6 new, Android library + Compose + Hilt) | config | batch | `androidApp/build.gradle.kts` (Compose + Hilt + KSP + detekt blocks) | role-match |
| `core/*/src/androidMain/AndroidManifest.xml` (new, minimal) | config | — | `shared/src/androidMain/AndroidManifest.xml` | exact |
| `core/ui/src/main/AndroidManifest.xml`, `feature/*/src/main/AndroidManifest.xml` (new, plain Android library manifests) | config | — | **No analog** — only an application manifest (`androidApp/src/main/AndroidManifest.xml`) and a manifest-less KMP-library manifest (`shared/src/androidMain/AndroidManifest.xml`, empty `<manifest />`) exist; no plain-library manifest with a namespace-scoped `<application>`-less shape exists yet | no analog (use `shared/src/androidMain/AndroidManifest.xml`'s empty-manifest shape as the floor) |
| `settings.gradle.dcl` (modified: `+includeBuild("build-logic")`, `+12 include(...)`) | config | batch | itself, current `include(":androidApp")` / `include(":shared")` lines | exact (pure extension of existing file) |
| `shared/build.gradle.kts` (modified → thin umbrella) | config | batch | itself (current full version, before thinning) | exact |
| `androidApp/build.gradle.kts` (modified → thin shell, drop Hilt-viewmodel-only pieces if any) | config | batch | itself (current full version) | exact |
| `.github/workflows/ios.yml` (modified: widen 3 `hashFiles()` cache keys) | config (CI) | batch | itself, lines 39/75/76 (current `shared/**` glob) | exact |
| `core/data/.../BookmarksStoreImpl.kt`, `core/analytics/.../FirebaseAnalyticsService.kt` (moved to androidMain) | service (Android-only impl of a commonMain interface) | CRUD (SharedPreferences) / event-driven (Firebase) | `androidApp/.../services/BookmarksStoreImpl.kt`, `.../services/FirebaseAnalyticsService.kt` (themselves, pre-move) | exact (pure move) |
| `feature/agenda/.../SessionFiltersServiceImpl.kt`, `feature/settings/.../DataCollectionSettingsServiceImpl.kt` (moved, per D-03 discretion + RESEARCH Open Question 1) | service | CRUD (SharedPreferences) | `androidApp/.../services/SessionFiltersService.kt`, `.../services/DataCollectionSettingsService.kt` (themselves, pre-move) | exact (pure move) |
| `androidApp/.../services/ExternalContentService.kt` (stays in `:androidApp`, NOT moved) | service | event-driven (Activity-scoped) | itself — no move needed, see Shared Patterns | exact (no-op) |
| `feature:*` `@HiltViewModel` classes (e.g. `AgendaViewModel`, `SpeakersViewModel`, `VenueViewModel`, `SessionViewModel`, `PartnersViewModel`, `DataCollectionViewModel`) | hook/store (Compose ViewModel) | request-response (StateFlow exposure over a Store/Service) | `androidApp/.../ui/screens/agenda/AgendaViewModel.kt` | exact |
| `core/ui/.../BookmarksViewModel.kt` (moved) | hook/store | request-response | `androidApp/.../ui/BookmarksViewModel.kt` (itself, pre-move) | exact (pure move) |
| `feature:*` route-entry composables (new thin wrappers, e.g. `AgendaRoute`, `VenueRoute`) | component (screen entry point) | request-response (callback-driven) | `androidApp/.../ui/screens/Home.kt` lines 146-210 (existing `composable(...) { Agenda(onSessionClick = ...) }` call sites) | exact (already the target shape, per D-07) |
| `feature:*` non-entry composables (e.g. `AgendaColumn`, `AgendaRow`, `SpeakerLayout`, `VenueDetails`, `About*`, `Settings*`) | component | transform (state → UI) | same files, pre-move (pure `git mv` per D-16 commit 1) | exact (pure move) |
| `core/ui` shared components (`SessionCategory`, `SessionComplexity`, `SessionType` chips, `SpeakerPicture`, `SocialIcon`, `LoadingLayout`, `appbars/*`, `theme/*`, `UiState.kt`) | component / utility | transform | `androidApp/.../ui/components/SessionCategory.kt` (itself, pre-move) | exact (pure move) |
| `core/model/.../*.kt` (domain classes + `model/stubs/*`) | model | transform | `shared/src/commonMain/kotlin/.../model/*.kt` (themselves, pre-move) | exact (pure move) |
| `core/network/.../Apollo.kt`, `ApolloCache.kt`, `graphql/*.graphqls` | service (API client config) | request-response (GraphQL) | `shared/src/commonMain/kotlin/.../store/graphql/Apollo.kt`, `ApolloCache.kt` (themselves, pre-move) | exact (pure move) |
| `core/data/.../GraphQLStore.kt`, `Mappers.kt`, `DevFestNantesStore.kt`, `DevFestNantesStoreBuilder.kt`, `BookmarksStore.kt` | service (store abstraction) | CRUD / streaming (Flow) | themselves in `shared/src/commonMain/kotlin/.../store/*.kt` (pre-move) | exact (pure move) |
| `core/testing/.../DevFestNantesStoreMocked.kt`, `model/stubs/*Stubs.kt` | test fixture | transform (fake data generation) | themselves in `shared/src/commonMain/kotlin/.../store/DevFestNantesStoreMocked.kt`, `.../model/stubs/*.kt` (pre-move) | exact (pure move) |
| Moved test files (`DevFestNantesStoreContractTest`, `ScheduleSlotDateParsingTest`, `GraphQLStoreJvmTest`, `MainActivityTest`, `ScheduleSlotDateFormatAndroidTest`) | test | transform | themselves, pre-move | exact (pure move; only the module hosting `commonTest`/`jvmTest`/`androidTest` changes) |

## Pattern Assignments

### 1. KMP core module `build.gradle.kts` (`core/model`, `core/network`, `core/data`, `core/analytics`, `core/testing`)

**Analog:** `shared/build.gradle.kts` (full file, 42 lines — small enough to have been read in one pass)

**Current full plugin + target + apollo shape** (whole analog file, `shared/build.gradle.kts:1-84`):
```kotlin
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
    alias(libs.plugins.appollo)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    config.setFrom("$rootDir/linters/detekt-config.yml")
    source.setFrom("src/commonMain/kotlin", "src/androidMain/kotlin", "src/iosMain/kotlin")
}

kotlin {
    android {
        namespace = "com.gdgnantes.devfest"
        compileSdk = AndroidSdk.compile
        minSdk = AndroidSdk.min
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    jvm()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework { baseName = "shared"; isStatic = true }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.appollo)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlin.test)
        }
    }
}

apollo {
    service("service") {
        packageName.set("com.gdgnantes.devfest.graphql")
        plugin("com.apollographql.cache:normalized-cache-apollo-compiler-plugin:${libs.versions.appolloCache.get()}")
        pluginArgument("com.apollographql.cache.packageName", packageName.get())
    }
}

dependencies {
    detektPlugins(libs.detekt.fomatting)
}
```

**How to split it per D-13/RESEARCH's `KmpLibraryConventionPlugin`:** the `detekt {}` block, the `android {}` block's `namespace`/`compileSdk`/`minSdk`/`compilerOptions`, and the `iosTarget.binaries.framework { }` shell (minus `export(...)` — see Pattern 5) all move into the convention plugin (`devfest.kmp.library`), applied identically to `core/model`/`core/network`/`core/data`/`core/analytics`/`core/testing`. Each new leaf `build.gradle.kts` becomes tiny — the "Consuming a convention plugin" shape from `03-RESEARCH.md`'s Code Examples section (`plugins { id("devfest.kmp.library") } kotlin { sourceSets { commonMain.dependencies { ... module-specific deps only ... } } }`). Apollo config (`apollo { service(...) }`) is `core/network`-only, not part of the shared convention plugin. `jvm()` + `commonTest`/`jvmTest` source sets land on whichever module owns `GraphQLStoreJvmTest`'s subject (recommended: `core:data`, per RESEARCH Assumption A4) — do not declare `jvm()` on every KMP module.

**Detekt pattern to consolidate** (also present near-identically at `androidApp/build.gradle.kts:47-52`):
```kotlin
detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    config.setFrom("$rootDir/linters/detekt-config.yml")
}
```
This exact duplicated block (currently in both `shared/build.gradle.kts:14-24` and `androidApp/build.gradle.kts:47-52`) is what the `DetektConventionPlugin` (D-13) must consolidate — every new module's tiny `build.gradle.kts` should get this via `id("devfest.detekt")` (or bundled into each type-specific convention plugin), never re-declared per module.

---

### 2. `core/ui` and `feature/*` `build.gradle.kts` (plain Android library + Compose, + Hilt for feature modules)

**Analog:** `androidApp/build.gradle.kts` (Compose/Hilt/detekt portions only — `applicationId`/`versionCode`/Firebase/secrets/OpenFeedback parts stay app-only, see Shared Patterns)

**Compose + Hilt + KSP plugin block** (`androidApp/build.gradle.kts:1-11`):
```kotlin
plugins {
    alias(libs.plugins.android.application)   // → devfest.android.feature applies "com.android.library" instead
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)       // app-only, not in feature convention plugin
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.crashlytics)           // app-only
    alias(libs.plugins.firebase.perf)         // app-only
    alias(libs.plugins.secrets)               // app-only
}
```

**Hilt dependency wiring to replicate per feature module** (`androidApp/build.gradle.kts` dependencies block):
```kotlin
implementation(libs.dagger.hilt.android)
ksp(libs.dagger.hilt.compiler)
androidTestImplementation(libs.dagger.hilt.android.testing)
kspAndroidTest(libs.dagger.hilt.compiler)
testImplementation(libs.dagger.hilt.android.testing)
kspTest(libs.dagger.hilt.compiler)
```
Bake this into `AndroidFeatureConventionPlugin` (`devfest.android.feature`) so every `feature:*` module gets it uniformly (per D-02, Common Pitfall 3 in RESEARCH.md) — `core/ui` does NOT need Hilt (it hosts `BookmarksViewModel`, but that's still `@HiltViewModel` — confirm at planning time whether `core:ui` also needs the Hilt convention plugin, since it hosts one `@HiltViewModel` class per D-06).

**`android { }` block portions that generalize** (`androidApp/build.gradle.kts` android block): `compileOptions { sourceCompatibility/targetCompatibility = JavaVersion.VERSION_17 }`, `buildFeatures { compose = true }`, `testOptions { unitTests { isReturnDefaultValues = true; isIncludeAndroidResources = true } }` — these three must be preserved verbatim by the convention plugin (RESEARCH.md's Validation Architecture flags `testOptions` explicitly as "must be preserved by the `AndroidApplicationConventionPlugin`"; the equivalent applies to `AndroidFeatureConventionPlugin` for feature modules with tests).

---

### 3. Convention plugin classes themselves (no analog — new pattern for this repo)

**No analog found.** `buildSrc/src/main/java/Dependencies.kt` is the only existing `buildSrc`/build-tooling Kotlin file, and it is a plain `object` (data holder), not a `Plugin<Project>` implementation — there is no existing Gradle plugin-authoring code in this repository to copy from.

**Use RESEARCH.md's Code Examples section instead** (`03-RESEARCH.md` lines ~360-419, "Convention plugin skeleton" and "Registering a convention plugin"), which is itself synthesized from `shared/build.gradle.kts`'s working plugin block converted to the convention-plugin idiom. Key excerpt to follow:
```kotlin
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
            pluginManager.apply("dev.detekt")
            // AndroidSdk constants moved from buildSrc (D-13)
        }
    }
}
```
Flagged risk (RESEARCH Assumption A5): the exact AGP 9.4.0 extension type name (`KotlinMultiplatformAndroidLibraryExtension`) is unverified against AGP 9.4.0's actual KDoc — confirm at implementation time, don't assume the skeleton compiles as literally written.

---

### 4. Android-only service impl moving into a `core:*` module's `androidMain` (D-03)

**Analog:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/BookmarksStoreImpl.kt` (full file, 65 lines)

**Full pattern** (`BookmarksStoreImpl.kt:1-64`):
```kotlin
package com.gdgnantes.devfest.androidapp.services

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdgnantes.devfest.store.BookmarksStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookmarksStoreImpl @Inject constructor(private val sharedPreferences: SharedPreferences) :
    BookmarksStore {
    // ... MutableStateFlow-backed SharedPreferences-persisted implementation ...

    companion object {
        const val PREF_SELECTED_SESSIONS = "selected_sessions"
    }
}
```
**Move target:** `core/data/src/androidMain/kotlin/com/gdgnantes/devfest/data/BookmarksStoreImpl.kt` (package rename in commit 2 only, per D-16). **Critical:** `PREF_SELECTED_SESSIONS = "selected_sessions"` is a runtime `SharedPreferences` key already on real devices — this literal string must diff byte-identical before/after the repackage commit (RESEARCH.md Pitfall 4 / Runtime State Inventory).

**Same shape applies to** `FirebaseAnalyticsService.kt` → `core/analytics/src/androidMain/...` (interface `AnalyticsService` stays `commonMain` in `core/analytics`, impl moves to `androidMain`), and to `SessionFiltersServiceImpl`/`DataCollectionSettingsServiceImpl` → their respective feature module's Android source set (no `commonMain` interface exists for these two, per RESEARCH Open Question 1 — they're plain Android-only classes, not multiplatform).

**Binding pattern that must keep working from `:androidApp`'s `AppModule`** (`androidApp/.../core/injection/AppModule.kt:40-59`):
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @AppScope
    @Binds
    abstract fun analyticsService(firebaseAnalyticsService: FirebaseAnalyticsService): AnalyticsService

    @AppScope
    @Binds
    abstract fun bookmarksStore(bookmarksStoreImpl: BookmarksStoreImpl): BookmarksStore
    // ...
}
```
Per D-02, this `@Module` stays entirely in `:androidApp` — only its import lines change (now pointing at `com.gdgnantes.devfest.data.BookmarksStoreImpl`, `com.gdgnantes.devfest.analytics.FirebaseAnalyticsService`, etc. from the new `core:*` packages). `:androidApp` must add `implementation(project(":core:data"))`, `implementation(project(":core:analytics"))`, etc. as needed to resolve these imports.

---

### 5. `@HiltViewModel` classes moving into `feature:*` modules

**Analog:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/AgendaViewModel.kt` (full file, 171 lines — read via one call)

**Constructor-injection + StateFlow-exposure shape** (`AgendaViewModel.kt:32-64`):
```kotlin
@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val store: DevFestNantesStore,
    private val bookmarksStore: BookmarksStore,
    private val performanceMonitoring: PerformanceMonitoring,
    private val sessionFiltersService: SessionFiltersService
) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState.STARTING)
    val uiState: StateFlow<UiState> get() = _uiState
    // ...
}
```
This is the pattern every moved `@HiltViewModel` (`SpeakersViewModel`, `VenueViewModel`, `SessionViewModel`, `PartnersViewModel`, `DataCollectionViewModel`, `FeedbackFormViewModel`) already follows — pure `git mv` + later package rename, no structural change. `core/ui`'s `BookmarksViewModel` follows the identical shape (`androidApp/.../ui/BookmarksViewModel.kt:14-27`, shown in full above in File Classification).

---

### 6. Feature entry-point composable wrapper (D-07's Nav3-ready shape)

**Analog:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt` lines 146-210 (existing callback-driven `composable { ... }` call sites — already the target shape, confirmed by RESEARCH.md's Pattern 1)

**Existing callback-only call site** (`Home.kt:146-151`):
```kotlin
composable(Screen.Agenda.route) {
    Agenda(
        agendaFilterDrawerState = agendaFilterDrawerState,
        onSessionClick = onSessionClick
    )
}
```
**New wrapper to add per feature** (from `03-RESEARCH.md`'s Code Examples, matching this exact existing shape):
```kotlin
// feature/agenda/src/main/.../AgendaRoute.kt
@Composable
fun AgendaRoute(
    onSessionClick: (Session) -> Unit,
    viewModel: AgendaViewModel = hiltViewModel(),
) {
    // existing Agenda() composable body, unchanged — only the wrapper name/entry point is new
}
```
`:androidApp`'s `Home.kt`/`MainActivity.kt` call `AgendaRoute(onSessionClick = ...)` etc. instead of the bare composable — **no `NavController` is ever passed to a feature module** (verified already true today: `MainActivity.kt:38` is the only `NavController.OnDestinationChangedListener`, and `Home.kt`'s `Agenda(...)`/`Speakers(...)`/`Venue(...)`/`About(...)` calls already take zero `NavController` args).

---

### 7. `core/network` — Apollo client config (moved, unchanged content)

**Analog:** `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt` (full file, 48 lines)

Pure move — `apolloClient`, `cacheKeyGenerator`, `cacheResolver` top-level vals move verbatim into `core/network`'s `commonMain`; `apollo { service("service") { packageName.set("com.gdgnantes.devfest.graphql") ... } }` config block moves from `shared/build.gradle.kts` into `core/network/build.gradle.kts` (Apollo-generated GraphQL types keep the same `packageName`, per D-15's package-rename scope only covering hand-written code, not generated Apollo classes — confirm this reading at planning time since D-15 doesn't explicitly exempt generated code).

**Consumer that must keep resolving:** `GraphQLStore.kt` (`shared/src/commonMain/kotlin/.../store/graphql/GraphQLStore.kt:1-27`) imports `com.gdgnantes.devfest.graphql.Get*Query` (Apollo-generated) and implements `DevFestNantesStore` — this file moves to `core/data`, which then needs `implementation(project(":core:network"))` (internal-only per D-11, not `api`/`export()`-ed).

---

### 8. `:shared` umbrella thinning (D-10/D-11) and iOS CI cache-key widening (Pitfall 5)

**Analog:** `shared/build.gradle.kts` itself, before/after — see RESEARCH.md's Pattern 3 code block (`kotlin { ... iosTarget.binaries.framework { export(project(":core:model")); export(project(":core:data")); export(project(":core:analytics")) } ... commonMain.dependencies { api(project(":core:model")); api(project(":core:data")); api(project(":core:analytics")); implementation(project(":core:network")) } }`) for the exact target end-state — already fully worked out in RESEARCH.md, do not re-derive.

**CI cache key widening** (`.github/workflows/ios.yml:39`, current):
```yaml
key: ${{ runner.os }}-kmp-${{ hashFiles('shared/**/*.kt', 'shared/build.gradle.kts', 'gradle/libs.versions.toml') }}
```
and (`.github/workflows/ios.yml:75-76`, current):
```yaml
key: ${{ runner.os }}-xcode-deriveddata-${{ hashFiles('iosApp/iosApp.xcodeproj/project.pbxproj') }}-${{ hashFiles('shared/**/*.kt') }}
restore-keys: |
  ${{ runner.os }}-xcode-deriveddata-${{ hashFiles('iosApp/iosApp.xcodeproj/project.pbxproj') }}-
  ${{ runner.os }}-xcode-deriveddata-
```
**Required edit** (per Pitfall 5): widen each `hashFiles('shared/**/*.kt', ...)` to also include `'core/**/*.kt', 'core/**/build.gradle.kts'`, ideally in the same commit as the `core:model` tracer (D-09) since that's the first commit that moves code out of `shared/`. This is a cache-efficiency-only fix, not a correctness gate — do not treat a missed widening as a build failure, just a slower/stale-cache CI run.

## Shared Patterns

### Detekt config wiring
**Source:** `linters/detekt-config.yml` (unchanged single config file) + the duplicated `detekt {}` block currently in `shared/build.gradle.kts:14-24` and `androidApp/build.gradle.kts:47-52`
**Apply to:** every new module via a `DetektConventionPlugin` (`devfest.detekt`) applied by every other convention plugin (Kmp/AndroidLibrary/AndroidFeature/AndroidApplication) — see Pattern 1 above for the exact block to consolidate.

### Hilt-per-feature-module wiring
**Source:** `androidApp/build.gradle.kts` dependencies block (`implementation(libs.dagger.hilt.android)`, `ksp(libs.dagger.hilt.compiler)`, plus `androidTest`/`test` variants) + `AppModule.kt`'s `@Module @InstallIn(SingletonComponent::class)` shape
**Apply to:** every `feature:*` module's convention plugin (`devfest.android.feature`), per D-02. The single `AppModule` itself never moves or splits — only its imports change as bound classes relocate to `core:*` androidMain source sets.

### AndroidSdk constants (`buildSrc` → `build-logic`)
**Source:** `buildSrc/src/main/java/Dependencies.kt` (full file, 10 lines):
```kotlin
object AndroidSdk {
    const val min = 26
    const val compile = 37
    // Deliberately NOT coupled to `compile` anymore: ... (keep this comment's rationale, D-13)
    const val target = 36
}
```
**Apply to:** every convention plugin that configures `compileSdk`/`minSdk`/`targetSdk` — move this object (or an equivalent) into `build-logic/convention/src/main/kotlin/`, preserving the `target` decoupling comment verbatim (D-13 explicitly calls this out), and repoint `shared/build.gradle.kts:26-27` + `androidApp/build.gradle.kts`'s `compileSdk`/`minSdk`/`targetSdk` references in the same commit `buildSrc` is deleted (Runtime State Inventory: "hard compile-time dependency, must land in the same commit").

### Empty/minimal AndroidManifest.xml for KMP-typed Android targets
**Source:** `shared/src/androidMain/AndroidManifest.xml` (exact full content):
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest />
```
**Apply to:** `core/model`, `core/network`, `core/data`, `core/analytics`, `core/testing`'s `androidMain` source sets (all KMP-typed via `com.android.kotlin.multiplatform.library`) — none of them need permissions/components, so this empty shape is the correct floor. `core/data`'s androidMain may need `<uses-permission android:name="android.permission.INTERNET" />` moved from `androidApp/src/main/AndroidManifest.xml:6` if `core:network`'s Apollo client is the actual internet-using code (confirm which module needs the permission at planning time — AGP manifest merging means it only needs to be declared once, in whichever module is lowest in the dependency graph that needs it, or it can stay on `:androidApp` since manifests merge upward regardless).

### SharedPreferences key-string preservation (Pitfall 4)
**Source:** literal constants in `BookmarksStoreImpl.kt:62` (`PREF_SELECTED_SESSIONS = "selected_sessions"`), `SessionFiltersService.kt` (`SHARED_PREFERENCES_KEY_AGENDA_FILTERS`), `DataCollectionSettingsService.kt:129-130` (`SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS`)
**Apply to:** every repackage commit (D-16 commit 2) touching these three files — diff the literal string values explicitly before/after, never rely on "compiles" as sufficient verification.

## No Analog Found

| File/Group | Role | Data Flow | Reason |
|---|---|---|---|
| `build-logic/convention/src/main/kotlin/*ConventionPlugin.kt` (5 files) | config (Gradle plugin) | transform | No existing `Plugin<Project>` implementation in this repo; `buildSrc` only holds a plain `object` constants holder. Use RESEARCH.md's Code Examples section (synthesized from `shared/build.gradle.kts`, NIA-pattern-cited) instead of an in-repo analog. |
| `core/ui/src/main/AndroidManifest.xml`, `feature/*/src/main/AndroidManifest.xml` (7 files, plain `com.android.library`-typed) | config | — | No existing plain Android-library manifest in this repo (only an application manifest and a KMP-library's empty manifest exist). Use `shared/src/androidMain/AndroidManifest.xml`'s minimal `<manifest />` shape as the floor, adding only what each module's own resources/no components require (library manifests rarely need more than a namespace, which AGP derives from `build.gradle.kts`'s `namespace = "..."` — not the manifest itself, for AGP 8+/9). |
| `core/*/build.gradle.kts`'s convention-plugin-authored Kotlin-DSL extension calls (e.g. `KotlinMultiplatformAndroidLibraryExtension`) | config | — | AGP 9.4.0's exact extension type name for `com.android.kotlin.multiplatform.library` inside a *convention plugin* (as opposed to a leaf `build.gradle.kts` applying it via `alias(...)`, which is what `shared/build.gradle.kts` does today) is unverified against AGP 9.4.0's KDoc this session (RESEARCH Assumption A5) — no in-repo analog exists because no convention plugin exists yet. Confirm against AGP 9.4.0 API at implementation time. |

## Metadata

**Analog search scope:** `shared/src/**`, `androidApp/src/**`, `shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `buildSrc/src/**`, `settings.gradle.dcl`, `.github/workflows/{android,ios}.yml`, `linters/detekt-config.yml`, `gradle/libs.versions.toml` — i.e. the entire current two-module codebase, since Phase 3 moves *all* of it.
**Files scanned:** ~100 Kotlin source files (full `find` listing of `shared/src` and `androidApp/src/main/java`), 2 module build files, 1 buildSrc file, 1 settings file, 2 CI workflow files, 1 detekt config, 1 version catalog.
**Pattern extraction date:** 2026-09-22
**Note on git-tracked-source gate:** all analog paths cited above (`shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `settings.gradle.dcl`, `buildSrc/src/main/java/Dependencies.kt`, `androidApp/.../MainActivity.kt`, `androidApp/.../AppModule.kt`, `.github/workflows/ios.yml`, and all other cited files) were spot-checked with `git ls-files` this session and confirmed tracked — none are gitignored mirrors (this repo has no plugin/capability-sync directories of that kind; the only stray untracked directory found, `shared-ui/`, is explicitly excluded from `settings.gradle.dcl` and is not cited as an analog anywhere above).
