---
phase: 04-hilt-to-koin-di-migration
fixed_at: 2026-10-13T11:15:00Z
review_path: /Users/robin/Development/Android/DevFestNantes/.planning/phases/04-hilt-to-koin-di-migration/04-REVIEW.md
iteration: 1
findings_in_scope: 5
fixed: 5
skipped: 0
status: all_fixed
---

# Phase 4: Code Review Fix Report

**Fixed at:** 2026-10-13T11:15:00Z
**Source review:** /Users/robin/Development/Android/DevFestNantes/.planning/phases/04-hilt-to-koin-di-migration/04-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 5 (fix_scope: critical_warning; Info findings IN-01..IN-05 out of scope)
- Fixed: 5
- Skipped: 0

**Verification environment:** isolated git worktree (`.claude/worktrees/rf-04-...`, with `local.properties` copied in), Gradle `--offline`. Run per fix: `:androidApp:compileDebugKotlin`, `:androidApp:testDebugUnitTest` (targeted tests, then the full module task once at the end, all green), `:shared:compileKotlinIosSimulatorArm64` and `:shared:iosSimulatorArm64Test` for WR-03.

## Fixed Issues

### WR-04: `Home.kt` imports the legacy `org.koin.androidx.compose.koinViewModel`

**Files modified:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt`
**Commit:** 6323bf4
**Applied fix:** Import changed to `org.koin.compose.viewmodel.koinViewModel`, matching the nine other call sites. The `androidApp` module already gets the dependency via the `devfest.android.koin` convention plugin. `:androidApp:compileDebugKotlin` passes. The optional gate rule rejecting `org.koin.androidx.compose` was not added.

### WR-01: No test starts the production graph with overriding disabled

**Files modified:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/ProductionGraphStartupTest.kt` (new)
**Commit:** 0eb9bde
**Applied fix:** New Robolectric test `production_graph_starts_without_overrides` calls `initKoin(platformModules = androidModules) { androidContext(application) }` with no `allowOverride(true)` and no test doubles, using the same dummy `FirebaseApp` as `KoinStartupTest`, and resolves the ordered `applicationInitializers` list (3 entries). The new file was required by the fix.

### WR-02: `KoinStartupTest` documentation overstates what it exercises

**Files modified:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/ProductionGraphStartupTest.kt`, `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt`
**Commit:** 885f4de
**Applied fix:** `KoinStartupTest` KDoc now lists which definitions are faked (store, bookmarks, analytics, performance via `:core:testing`; consent service and Timber tree locally) and points to `ProductionGraphStartupTest`. That test gained `real_service_definitions_resolve`, which resolves the real `AnalyticsService`, `PerformanceMonitoring`, `BookmarksStore`, `DevFestNantesStore`, `DataCollectionSettingsService` and `Timber.Tree`. The first run showed that `ApolloInitializer.context` (normally set by androidx.startup) is not set under Robolectric, so `setUp` sets it by reflection. Reflection keeps `androidx.startup` off the test compile classpath.

### WR-03: `IosDependencies.store()` throws a fatal Kotlin exception if `start()` was not called

**Files modified:** `shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt`
**Commit:** 43915bd
**Applied fix:** `store()` now calls `start()` first, so it is self-starting. `start()` is guarded by `KoinPlatformTools.synchronized(Lockable())`, which removes the check-then-act race. Compiles for `iosSimulatorArm64`, and `:shared:iosSimulatorArm64Test` passes. The Swift API (`start`, `store`) is unchanged.

### WR-05: `AndroidGraphVerifyTest` whitelists broad types, which weakens `verify()`

**Files modified:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt`
**Commit:** 6db9ac1
**Applied fix:** `full_android_graph_is_complete` now verifies `sharedModules + (androidModules - appModule)`. `appModule` keeps its own narrow whitelist in `app_module_is_complete_given_its_upstream_types`. The whole-graph whitelist drops `String`, `Boolean`, `Activity` and `CoroutineDispatcher`. It is now `Context`, `Application`, the Firebase handle constructor types and `OpenFeedbackConfig`, which is the one binding `appModule` supplies to `:feature:session-detail`. Not applied: pinning the Firebase handle types by name. `FirebaseAnalytics` takes an obfuscated `zzez` class, so naming it would be more brittle than deriving it. The reflection-derived set is kept (limited to the three handle classes) and the comment explains why. Review the remaining whitelist if you want a stricter approach.

---

_Fixed: 2026-10-13T11:15:00Z_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
