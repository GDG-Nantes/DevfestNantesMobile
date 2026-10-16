---
last_mapped_commit: f6a603391f893d8f6bd0964cfcca500620b6a072
last_mapped_at: 2026-10-16
---
# External Integrations

**Analysis Date:** 2026-10-16

## APIs & External Services

**Conference GraphQL API (primary data source):**
- Confetti GraphQL backend at `https://confetti-app.dev/graphql`, set in `core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/Apollo.kt`
  - SDK/Client: Apollo Kotlin `5.2.0` (`ApolloClient.Builder`), with normalized cache
  - Request header: `conference: devfestnantes2025` (set in `Apollo.kt`), which selects the conference edition
  - Schema: `core/network/src/commonMain/graphql/schema.graphqls`, with `extra.graphqls` for client-side extensions
  - Operations: `core/network/src/commonMain/graphql/operations.graphql`
  - Generated code: `core/network/build/generated/...` (package `com.gdgnantes.devfest.core.network.graphql`)
  - Auth: None (public, unauthenticated queries)
- Repository layer consumes it through `core/data` (Apollo types are kept behind the network module)

**Feedback forms (OpenFeedback):**
- Library: `io.openfeedback:openfeedback-m3` and `openfeedback-viewmodel` `1.0.0-alpha.3`
- Used by `feature/session-detail`, with fallback URL logic in `feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FallbackFeedbackForm.kt`
- Initialized in `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/OpenFeedbackInitializer.kt`
- Backed by a Firebase Realtime Database (`databaseUrl` is passed from `BuildConfig.OPEN_FEEDBACK_FIREBASE_DATABASE_URL`)
- Web form base: `https://openfeedback.io/devfestnantes2025/` (`core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/WebLinks.kt`, `OPENFEEDBACK_BASE_URL`)
- Gate: `OPEN_FEEDBACK_ENABLED` build config flag, default `"false"` in `androidApp/build.gradle.kts`
- Auth/config: `OPEN_FEEDBACK_PROJECT_ID`, `OPEN_FEEDBACK_FIREBASE_PROJECT_ID`, `OPEN_FEEDBACK_FIREBASE_APPLICATION_ID`, `OPEN_FEEDBACK_FIREBASE_API_KEY`, `OPEN_FEEDBACK_FIREBASE_DATABASE_URL` are `buildConfigField` entries declared with placeholder values in `androidApp/build.gradle.kts`. Real values are supplied outside the repo.

**Images and media:**
- Coil `2.7.0` loads remote images. Speaker and partner assets are hosted on GitHub raw content from `GDG-Nantes/Devfest2025` (`raw.githubusercontent.com/GDG-Nantes/Devfest2025/main/src/...`). These URLs are referenced from the data and UI layers.

**Web links opened by the app:**
- Defined in `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/WebLinks.kt`:
  - Code of conduct: `https://devfest.gdgnantes.com/code-of-conduct`
  - Website: `https://devfest.gdgnantes.com/`
  - Project repository and support: `https://github.com/GDG-Nantes/DevfestNantesMobile` and `/issues`
  - Social: Facebook, Twitter, LinkedIn, YouTube (`core/model/src/commonMain/.../SocialItem.kt`)
  - Local community: `https://nantes.community/`
- Opened through `androidx.browser` Custom Tabs

## Data Storage

**Databases:**
- No app-owned database detected. No Room, SQLDelight, or Realm dependency in `gradle/libs.versions.toml`.
- Apollo normalized cache: `normalized-cache` and `normalized-cache-sqlite` `1.0.8` are declared. The cache is configured in `core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/ApolloCache.kt` and `Apollo.kt`. Whether the SQLite-backed store is wired in at runtime was not confirmed in this pass.
- Remote: Firebase Realtime Database, used only by OpenFeedback (see above)

**Local preferences:**
- AndroidX Preference `1.2.1` (`androidx.preference`) for settings in `feature/settings`

**File Storage:**
- Local filesystem only (app-internal; no cloud storage SDK detected)

**Caching:**
- Apollo normalized in-memory and SQLite-backed cache (see above)
- Firebase Remote Config defaults and fetch results, held by the Firebase SDK

## Authentication & Identity

**Auth Provider:**
- None. The conference API and the app are anonymous. No login, OAuth, or Firebase Auth dependency found.
- Firebase is initialized for analytics, config, and crash reporting, not for user accounts.

## Analytics, Monitoring & Observability

**Firebase Analytics:**
- `com.google.firebase:firebase-analytics` via `core/analytics`
- Abstracted behind `AnalyticsService` (interface in `core/analytics`), implemented by `FirebaseAnalyticsService` in `core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/FirebaseAnalyticsService.kt`
- Bound in `core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/di/AnalyticsPlatformModule.android.kt`
- Consent gate: `DataCollectionSettingsService` (referenced in `androidApp/src/test/.../KoinStartupTest.kt`)

**Crash Reporting:**
- Firebase Crashlytics `firebase-crashlytics`, with `com.google.firebase.crashlytics` Gradle plugin `3.0.8`
- Obtained via `FirebaseCrashlytics.getInstance()` in `AnalyticsPlatformModule.android.kt`

**Performance Monitoring:**
- Firebase Performance `firebase-perf`, with `com.google.firebase.firebase-perf` Gradle plugin `2.0.2`
- Implementation: `core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/performance/FirebasePerformanceMonitoring.kt`

**Remote Config:**
- Firebase Remote Config `firebase-config`, initialized in `feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/di/SessionDetailModule.kt` (`Firebase.remoteConfig`, `remoteConfigSettings`)

**Logs:**
- Timber `5.0.1`. Planting of trees happens at app start; the test suite verifies a `Timber.Tree` is planted in `androidApp/src/test/.../KoinStartupTest.kt`.

## CI/CD & Deployment

**Hosting:**
- Google Play for Android (applicationId `com.gdgnantes.devfest.mobile.androidapp`)
- iOS distribution: the Xcode project in `iosApp/` (distribution channel not detected in the workflows reviewed)

**CI Pipeline:**
- GitHub Actions
  - `.github/workflows/android.yml`: detekt, lint, DI gate, unit tests, instrumentation tests on API 34
  - `.github/workflows/ios.yml`: macOS build via `xcodebuild`
- Shared setup action: `.github/actions/android-setup`
- Dependabot: `.github/dependabot.yml`
- Issue and PR templates: `.github/ISSUE_TEMPLATE/`, `.github/pull_request_template.md`

## Environment Configuration

**Required env vars / build config:**
- `OPEN_FEEDBACK_ENABLED` (defaults to `"false"`)
- `OPEN_FEEDBACK_PROJECT_ID`
- `OPEN_FEEDBACK_FIREBASE_PROJECT_ID`
- `OPEN_FEEDBACK_FIREBASE_APPLICATION_ID`
- `OPEN_FEEDBACK_FIREBASE_API_KEY`
- `OPEN_FEEDBACK_FIREBASE_DATABASE_URL`

Names only. Values are not recorded in this document.

**Secrets location:**
- Gradle Secrets Plugin (`com.google.android.libraries.mapsplatform.secrets-gradle-plugin`) is applied in `androidApp/build.gradle.kts`. Its source file (conventionally `local.properties` or `secrets.properties`) is local and not committed.
- `local.properties` exists at the repo root. Contents were not read.
- Firebase config is supplied by `google-services.json` for the Android app module. Not read.

## Webhooks & Callbacks

**Incoming:**
- None detected

**Outgoing:**
- None detected (the app only issues GraphQL queries and Firebase SDK calls)

---

*Integration audit: 2026-10-16*
