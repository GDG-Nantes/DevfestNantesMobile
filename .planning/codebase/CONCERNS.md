---
last_mapped_commit: f6a603391f893d8f6bd0964cfcca500620b6a072
last_mapped_at: 2026-10-16
---
<!-- refreshed: 2026-10-16 -->

# Codebase Concerns

**Analysis Date:** 2026-10-16

## Tech Debt

**Unimplemented "filter" analytics event:**
- Issue: `AnalyticsService.eventFilter()` is meant to log that the user applied an agenda filter. The Android implementation is a stub (`Timber.d("TODO: eventFilter")`) and nothing on Android calls it. It is not a privacy filter: analytics consent is applied separately through `setAnalyticsCollectionEnabled` in `DataCollectionSettingsServiceImpl`.
- Files: `core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsService.kt` (line 9), `core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/FirebaseAnalyticsService.kt` (lines 25-27); iOS counterpart in `iosApp/iosApp/Analytics/FirebaseAnalyticsService.swift`
- Impact: Agenda filter usage is not measured on Android.
- Fix approach: Log a Firebase event (with the filter type as a parameter) and call it from the agenda filters, or remove the method from the interface, the fakes and the iOS service.

## Known Bugs

**`PerformanceInitializer` overrides the user's performance-monitoring consent:**
- Symptoms: Firebase Performance collection is enabled at every app start, even when the user turned "Firebase Performance" off (or never consented) in the data-collection settings.
- Files: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/performance/PerformanceInitializer.kt` (line 42), `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AppModule.kt` (lines 45-50), `feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsService.kt` (lines 108-110)
- Trigger: `applicationInitializers` run in order `DataSharingInitializer`, `OpenFeedbackInitializer`, `PerformanceInitializer`. `DataSharingInitializer` applies the stored consent (`isPerformanceCollectionEnabled = <consent>`), then `PerformanceInitializer.invoke()` unconditionally sets `FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true`. The Firebase setting is persisted by the SDK, so it stays on until the user toggles the setting again in the same session.
- Impact: Privacy/GDPR: performance data is collected without consent, contradicting the consent dialog and settings screen. The documented intent ("stored consent must be applied before performance collection is enabled") is defeated rather than enforced.
- Fix approach: Remove the `isPerformanceCollectionEnabled = true` line from `PerformanceInitializer` (it should only stop the `app_startup` trace), leaving `DataCollectionSettingsService` as the single writer of that flag. Add a test in `androidApp/src/test/.../di/` that runs the initializers with performance consent off and asserts collection stays disabled.
- Test coverage: None. `KoinStartupTest` and `ProductionGraphStartupTest` check the initializer order, not the resulting Firebase flag.

## Security Considerations

**Firebase configuration committed to the repository:**
- Risk (low): `google-services.json` is tracked by git (verified). Firebase Android API keys are client identifiers that ship inside the APK anyway, so this is a hygiene issue rather than a leak, as long as the key is restricted.
- Files: `androidApp/google-services.json`
- Current mitigation: Secrets plugin is applied in `androidApp/build.gradle.kts` and root `build.gradle.kts` (`alias(libs.plugins.secrets)`), indicating a secrets-management intent.
- Recommendations: Confirm the Firebase API key is restricted in the Google Cloud console (Android app package and SHA-1 restrictions). Consider moving the file out of git and injecting it in CI.

**Exported components in the manifest (verified, low risk):**
- The only `android:exported="true"` component is `.MainActivity` (`androidApp/src/main/AndroidManifest.xml`, line 44), the `MAIN`/`LAUNCHER` entry point, which must be exported. No other exported activities, services or receivers are declared.
- `android:allowBackup="false"` is set (line 14).
- Recommendations: If deep links are added later, validate incoming intent data before use.

**Working-tree artifacts at repository root:**
- Risk (low): Build and log output (`ios.log`, `xcodebuild.log`), a design file (`logo-long.afdesign`) and `local.properties` sit at the repository root. They are not tracked in git.
- Files: `ios.log`, `xcodebuild.log`, `logo-long.afdesign`, `local.properties`
- Current mitigation: All four are ignored by `.gitignore` (`*.log`, `local.properties`, `*.afdesign`; verified with `git check-ignore`).
- Recommendations: Delete the stale logs; no further action needed.

## Performance Bottlenecks

Not detected from static exploration. No profiling data was reviewed.

## Fragile Areas

**Largest UI files:**
- Files: `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/SessionFiltersDrawer.kt` (405 lines), `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt` (341 lines), `feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionSettingsScreen.kt` (297 lines), `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt` (217 lines)
- Why fragile: Large composables mixing layout, state and event handling are harder to change safely and to recompose efficiently.
- Safe modification: Split into smaller composables with hoisted state before adding behavior; keep changes covered by the existing agenda and settings tests.
- Test coverage: Agenda ViewModel has a unit test (`feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModelTest.kt`); the composables themselves were not confirmed to have UI tests.

**Dependency injection migration:**
- Files: `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt`, `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt`
- Why fragile: The app moved from Hilt to Koin (per recent commit history). Startup wiring errors surface at runtime rather than compile time.
- Safe modification: Keep `KoinStartupTest` green when adding or renaming modules.

## Scaling Limits

Not applicable. This is a single-event conference app with a bounded dataset served from a GraphQL backend.

## Dependencies at Risk

Not assessed in detail. Versions are managed in the Gradle version catalog (`gradle/libs.versions.toml`); a full outdated-dependency audit was not performed. Known pin: kotlinx-datetime stays on a `0.6.x-compat` release because OpenFeedback `1.0.0-alpha.3` needs 0.6.x at runtime (guarded by `OpenFeedbackDatetimeLinkageTest`).

## Missing Critical Features

Not detected from static exploration.

## Test Coverage Gaps

**Feature composables and Android-specific code:**
- What's not tested: Most Compose screens under `feature/*` and `androidApp/src/main/java/.../ui/screens/` lack confirmed tests; only a subset of the roughly 55 test-referencing Kotlin files were inspected.
- Files: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt`, `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt`
- Risk: Layout and navigation regressions go unnoticed until manual QA.
- Priority: Medium

**Consent applied at start-up:**
- What's not tested: The effective Firebase collection flags after `applicationInitializers` run. Only the initializer order is asserted.
- Files: `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt`, `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/performance/PerformanceInitializer.kt`
- Risk: Consent regressions such as the `PerformanceInitializer` bug above go unnoticed.
- Priority: High

---

*Concerns audit: 2026-10-16*
