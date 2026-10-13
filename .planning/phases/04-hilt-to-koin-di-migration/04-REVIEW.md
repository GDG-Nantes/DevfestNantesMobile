---
phase: 04-hilt-to-koin-di-migration
reviewed: 2026-10-11T00:00:00Z
depth: standard
files_reviewed: 104
files_reviewed_list:
  - .github/copilot-instructions.md
  - .github/workflows/android.yml
  - .github/workflows/ios.yml
  - CONTRIBUTING.md
  - README.md
  - androidApp/build.gradle.kts
  - androidApp/src/main/assets/licenses.html
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/DataSharingInitializer.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/OpenFeedbackInitializer.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/logging/TimberTreeDebug.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/logging/TimberTreeRelease.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/performance/PerformanceInitializer.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AndroidModules.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AppModule.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/ExternalContentService.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModel.kt
  - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt
  - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt
  - build-logic/convention/build.gradle.kts
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidApplicationConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidKoinConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidLibraryConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DependencyResolutionRules.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/KmpLibraryConventionPlugin.kt
  - build.gradle.kts
  - core/analytics/build.gradle.kts
  - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/FirebaseAnalyticsService.kt
  - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/di/AnalyticsPlatformModule.android.kt
  - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/performance/FirebasePerformanceMonitoring.kt
  - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/performance/PerformanceMonitoring.kt
  - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/di/AnalyticsModule.kt
  - core/analytics/src/iosMain/kotlin/com/gdgnantes/devfest/core/analytics/di/AnalyticsPlatformModule.ios.kt
  - core/analytics/src/jvmMain/kotlin/com/gdgnantes/devfest/core/analytics/di/AnalyticsPlatformModule.jvm.kt
  - core/data/build.gradle.kts
  - core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/BookmarksStoreImpl.kt
  - core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.android.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/di/DataModule.kt
  - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.ios.kt
  - core/data/src/jvmMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.jvm.kt
  - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/di/DataModuleTest.kt
  - core/network/build.gradle.kts
  - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/di/NetworkModule.kt
  - core/network/src/jvmTest/kotlin/com/gdgnantes/devfest/core/network/di/NetworkModuleTest.kt
  - core/testing/build.gradle.kts
  - core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/NoOpPerformanceMonitoring.kt
  - core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/di/AndroidTestDoublesModule.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeAnalyticsService.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeBookmarksStore.kt
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModule.kt
  - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeBookmarksStoreTest.kt
  - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModuleTest.kt
  - core/ui/build.gradle.kts
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/BookmarksViewModel.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/di/CoreUiModule.kt
  - core/ui/src/test/java/com/gdgnantes/devfest/core/ui/di/CoreUiModuleTest.kt
  - documentation/ARCHITECTURE.md
  - documentation/PERFORMANCE_MONITORING.md
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/di/AboutModule.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/Partners.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/PartnersViewModel.kt
  - feature/about/src/test/java/com/gdgnantes/devfest/feature/about/di/AboutModuleTest.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/Agenda.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRow.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersService.kt
  - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModuleTest.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackForm.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFormViewModel.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionDetailRoute.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionLayout.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionViewModel.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/di/SessionDetailModule.kt
  - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/di/SessionDetailModuleTest.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionAgreementDialog.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionSettingsScreen.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionViewModel.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/di/SettingsModule.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsService.kt
  - feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/di/SettingsModuleTest.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakerViewModel.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakersRoute.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/di/SpeakersModule.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt
  - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/di/SpeakersModuleTest.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/Venue.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueViewModel.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/di/VenueModule.kt
  - feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/di/VenueModuleTest.kt
  - gradle/libs.versions.toml
  - iosApp/iosApp/Agenda/AgendaViewModel.swift
  - iosApp/iosApp/Model/BaseViewModel.swift
  - iosApp/iosApp/iOSApp.swift
  - scripts/di-gate.sh
  - shared/build.gradle.kts
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt
  - shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt
  - shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt
  - shared/src/jvmTest/kotlin/com/gdgnantes/devfest/shared/di/KoinGraphTest.kt
findings:
  critical: 0
  warning: 5
  info: 5
  total: 10
status: issues_found
---

# Phase 4: Code Review Report

**Reviewed:** 2026-10-11
**Depth:** standard
**Files Reviewed:** 104
**Status:** issues_found

## Summary

The Hilt to Koin migration is well-structured. I compared the Koin definitions against the pre-migration Hilt `AppModule` and found no behavior regressions:

- **Preferences file:** the default SharedPreferences file is unchanged, so bookmarks, filters and consent survive.
- **Scoping:** stateful services (`SessionFiltersServiceImpl`, `DataCollectionSettingsServiceImpl`, `BookmarksStoreImpl`, the store) stay `single`, and the Firebase SDK handles stay `factory`.
- **Initializer order:** the consent-first order is an explicit list.
- **Positional definitions:** the parameterized ViewModels are positional, so they avoid type-matching ambiguity.
- **ObjC export:** the Koin modules and `initKoin` are `@HiddenFromObjC`, so no Koin type leaks into Swift.
- **No duplicate bindings:** a manual scan of the production module set found none.

The remaining problems are mostly in test strength and defensive behavior:

- The start-up and verify tests are weaker than their documentation claims.
- The strict no-override start path is never executed by any test.
- There is one deprecated/inconsistent Koin import.
- `IosDependencies.store()` crashes if called before `start()`.

No critical issues were found.

## Warnings

### WR-01: No test starts the production graph with overriding disabled

**Files:** `shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt:28`, `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt:88-91`, `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt:47`
**Issue:**
- `initKoin` sets `allowOverride(false)` so that a duplicate definition across the per-module Koin modules fails at start-up.
- `KoinStartupTest` re-enables overriding with `allowOverride(true)` and layers `androidTestDoubles` on top. The `Module.verify()` calls in `AndroidGraphVerifyTest` use a default `koinApplication`, which also allows overriding.
- So the exact production path, `initKoin(platformModules = androidModules)` with `androidContext` and no overrides, is never executed in CI. Only the JVM-only `KoinGraphTest` exercises the strict flag, and it uses no Android modules.
- A future duplicate binding between two Android or feature modules would be silently accepted by every test and crash the app at launch (`DefinitionOverrideException` in `Application.onCreate`).
**Fix:** Add a Robolectric test that starts the unmodified production graph, with the same dummy `FirebaseApp` as `KoinStartupTest`:
```kotlin
@Test
fun production_graph_starts_without_overrides() {
    stopKoin()
    val k = initKoin(platformModules = androidModules) { androidContext(application) }.koin
    assertNotNull(k.get<List<ApplicationInitializer>>(named("applicationInitializers")))
}
```

### WR-02: `KoinStartupTest` documentation overstates what it exercises

**File:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt:53-61, 87-91, 152-153`
**Issue:**
- The KDoc claims the test resolves "every ViewModel and entry-point dependency" and that "the production Firebase definitions run unchanged".
- In fact `androidTestDoubles` replaces `DevFestNantesStore`, `BookmarksStore`, `AnalyticsService` and `PerformanceMonitoring`. The local overrides replace `DataCollectionSettingsService` and `Timber.Tree`.
- Nothing in the test ever resolves these production definitions: `Firebase.analytics`, `FirebaseCrashlytics.getInstance()`, `FirebasePerformance.getInstance()`, `FirebaseAnalyticsService`, `FirebasePerformanceMonitoring`, `BookmarksStoreImpl`, `GraphQLStore`, `DataCollectionSettingsServiceImpl`, `TimberTreeDebug` and `TimberTreeRelease`.
- Only `FirebaseRemoteConfig` and the real `SessionFiltersServiceImpl` run unfaked.
- A construction-time failure in any of those definitions (for example Crashlytics or Performance instantiation) passes this test. `verify()` only checks the static constructor graph.
**Fix:**
- Add a test that resolves the real `AnalyticsService`, `PerformanceMonitoring`, `BookmarksStore`, `DataCollectionSettingsService` and `Timber.Tree` from the non-overridden graph. WR-01's test can carry these asserts.
- Correct the KDoc to say which definitions are faked.

### WR-03: `IosDependencies.store()` throws a fatal Kotlin exception if `start()` was not called

**File:** `shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt:13-21`, consumed by `iosApp/iosApp/Model/BaseViewModel.swift:17`
**Issue:**
- `store()` calls `KoinPlatform.getKoin()`, which throws `IllegalStateException` when Koin is not started.
- `store()` has no `@Throws`, so the exception crosses into Swift as an uncatchable crash. It is not surfaced as an `NSError`.
- `BaseViewModel.init` evaluates this as a default argument, so any ViewModel created before `iOSApp.init` has run `IosDependencies.shared.start()` crashes. Examples are SwiftUI previews, test hosts, and any future entry point other than `iOSApp`.
- The pre-migration code built the store directly and had no ordering requirement.
- `start()` itself is also an unsynchronized check-then-act, which is fine today but fragile.
**Fix:** Make `store()` self-starting, and guard `start()` with a lock or `Mutex`:
```kotlin
fun store(): DevFestNantesStore {
    start()
    return KoinPlatform.getKoin().get()
}
```

### WR-04: `Home.kt` imports the legacy `org.koin.androidx.compose.koinViewModel`

**File:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt:49`
**Issue:**
- Every other call site (nine files, such as `Agenda.kt`, `Partners.kt`, `Speakers.kt`) uses `org.koin.compose.viewmodel.koinViewModel`.
- `Home.kt` uses `org.koin.androidx.compose.koinViewModel`. In Koin 4.x that package is the legacy, deprecated entry point, kept only for source compatibility. I could not verify the deprecation level against 4.2.2 from here.
- It resolves the ViewModel differently from the rest of the app and may warn or break on the next Koin bump.
- The DI gate does not flag it, because `koinViewModel` is allowed everywhere.
**Fix:** `import org.koin.compose.viewmodel.koinViewModel`. Optionally, have the gate reject the `org.koin.androidx.compose` namespace.

### WR-05: `AndroidGraphVerifyTest` whitelists broad types, which weakens `verify()`

**File:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt:25-43, 49`
**Issue:**
- `full_android_graph_is_complete` passes `extraTypes` containing `String`, `Boolean`, `Context`, `Application`, `Activity` and `CoroutineDispatcher`. It also passes every constructor parameter type of `FirebaseAnalytics`, `FirebaseCrashlytics` and `FirebasePerformance`, derived by reflection.
- This turns off `verify()` for any definition that depends on those types. A ViewModel that starts requiring a `String`, `Context` or `Executor` and forgets to bind it still passes, so it fails at runtime instead.
- The reflection-derived set also changes silently with every Firebase BoM bump.
- The extra types are applied to the whole graph, although they are only needed for `OpenFeedbackConfig`, `ExternalContentService` and the Firebase factories.
**Fix:**
- Scope the whitelists to the module that needs them. `appModule.verify` already has its own list.
- Use `Module.verify` per module, as the feature modules do.
- Keep the whole-graph test, but restrict `extraTypes` to the minimum, namely `Context`, `Application` and the Firebase handles.
- Pin the Firebase handle list explicitly instead of deriving it by reflection.

## Info

### IN-01: Stale "not-yet-migrated" comments

**Files:** `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt:15-16`, `feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/di/SettingsModule.kt:15-16`, `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AndroidModules.kt:12`
**Issue:**
- The comments refer to the "not-yet-migrated HomeViewModel (through its bridge)" and the "not-yet-migrated DataSharingInitializer (through its bridge)". They also say "later migration steps append here".
- Both consumers are now on Koin and there is no bridge.
**Fix:** Reword to the current reason, for example: "single so the agenda screen and HomeViewModel observe one instance". Drop the "later migration steps" phrase.

### IN-02: Typo `timerTree` in `DevFestNantesApplication`

**File:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt:21, 34`
**Issue:** `timerTree` is carried over from the original code and should be `timberTree`. Since the field was rewritten as `by inject()`, this is a cheap moment to fix it.
**Fix:** Rename to `timberTree`.

### IN-03: DI gate self-test is not run in CI

**Files:** `.github/workflows/android.yml:28-29`, `scripts/di-gate.sh:184-253`
**Issue:**
- CI runs only `bash scripts/di-gate.sh` (check mode). The `selftest` mode, which proves the matchers still bite, is never executed.
- A regex regression, such as a portability change in `grep -E`, would turn the gate into a permanent no-op that still prints `DI-GATE-OK`.
- Locator-rule gaps: the exemption `*/di/*` covers any file under a `di` directory, including test sources. The rule is import-based, so a fully-qualified `org.koin.mp.KoinPlatform.getKoin()` call without an import slips through.
**Fix:** Add `bash scripts/di-gate.sh selftest` as a CI step, in the same job and before the check. Optionally, also match the fully-qualified names.

### IN-04: `activity` version bump bundled into the DI migration

**File:** `gradle/libs.versions.toml:3`
**Issue:**
- `activity = "1.9.2"` became `"1.12.4"` in a DI-only phase, which makes the `PROJECT.md` "no regression" blast radius larger.
- The shipped `MainActivity` uses `enableEdgeToEdge()` and `ComponentActivity`. This is probably required by `koin-androidx-compose` or a lifecycle alignment, but the reason is not recorded next to the version.
**Fix:** Add a comment next to the version explaining which dependency requires it, or confirm the bump is intended and covered by the smoke tests.

### IN-05: iOS CI runs the new shared tests without a timeout adjustment

**File:** `.github/workflows/ios.yml:130-131`
**Issue:**
- The `:shared:iosSimulatorArm64Test` step links a Kotlin/Native test binary (`-lsqlite3`) and boots a simulator test runner, on top of the existing framework build.
- The job still has `timeout-minutes: 30`, with caches keyed only on source hashes. A cold-cache run may exceed it.
**Fix:** Raise the timeout (45 to 60 minutes), or add `~/.konan` to the cache in this job. It is already cached for Gradle, but verify the test binary path is included.

---

_Reviewed: 2026-10-11_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
