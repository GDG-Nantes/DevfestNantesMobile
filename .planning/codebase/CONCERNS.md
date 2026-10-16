---
last_mapped_commit: f6a603391f893d8f6bd0964cfcca500620b6a072
last_mapped_at: 2026-10-16
---
<!-- refreshed: 2026-10-16 -->

# Codebase Concerns

**Analysis Date:** 2026-10-16

## Tech Debt

**Unfinished event filtering in analytics:**
- Issue: Event filter is a placeholder that only logs via Timber; no real filtering is applied.
- Files: `core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/FirebaseAnalyticsService.kt` (line 26)
- Impact: All analytics events pass through unfiltered; the data-collection settings screen may imply control that is not enforced at this layer.
- Fix approach: Implement the filter (or remove the stub) and verify it is applied before events reach Firebase, consistent with the consent flow in `feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionSettingsScreen.kt`.

**Documentation improvement backlog:**
- Issue: A standalone doc-improvement list lives at the repository root and has not been folded into the project docs.
- Files: `DOC_IMPROVEMENTS.md`
- Impact: Known documentation gaps remain tracked outside the planning system.
- Fix approach: Triage the list into GSD phases or close the items.

## Known Bugs

Not detected from static exploration. No open bug tracker was inspected; the analysis did not run the app or the test suite.

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
- Risk: Build and log output (`ios.log`, `xcodebuild.log`, `logo-long.afdesign`) and a `local.properties` file sit at the repository root. They are not tracked in git, but their presence increases the chance of accidental commits of local paths or logs.
- Files: `ios.log`, `xcodebuild.log`, `local.properties`
- Current mitigation: Not verified against `.gitignore` in this analysis.
- Recommendations: Confirm these are ignored by `.gitignore` and delete stale logs.

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

Not assessed in detail. Versions are managed in the Gradle version catalog (`gradle/libs.versions.toml` if present); a full outdated-dependency audit was not performed.

## Missing Critical Features

Not detected from static exploration.

## Test Coverage Gaps

**Feature composables and Android-specific code:**
- What's not tested: Most Compose screens under `feature/*` and `androidApp/src/main/java/.../ui/screens/` lack confirmed tests; only a subset of the roughly 55 test-referencing Kotlin files were inspected.
- Files: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt`, `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt`
- Risk: Layout and navigation regressions go unnoticed until manual QA.
- Priority: Medium

**Analytics filtering:**
- What's not tested: The placeholder event filter in `FirebaseAnalyticsService.kt` has no coverage.
- Files: `core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/FirebaseAnalyticsService.kt`
- Risk: Privacy-relevant behavior changes silently.
- Priority: High

---

*Concerns audit: 2026-10-16*
