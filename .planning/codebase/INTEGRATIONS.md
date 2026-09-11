# External Integrations

**Analysis Date:** 2026-09-11

## APIs & External Services

**GraphQL:**
- Confetti App - DevFest Nantes conference data backend
  - SDK/Client: Apollo GraphQL 4.3.3 (`appollo-runtime:4.3.3`)
  - Server URL: `https://confetti-app.dev/graphql`
  - Header: `conference: devfestnantes2025`
  - Cached: Yes (normalized cache with SQLite backend)
  - GraphQL schema: `shared/src/commonMain/graphql/schema.graphqls`
  - Operations: `shared/src/commonMain/graphql/operations.graphql`

**Feedback System:**
- OpenFeedback - Session feedback collection
  - SDK: `openfeedback-m3`, `openfeedback-viewmodel` (v1.0.0-alpha.3)
  - Enabled: Via Firebase Remote Config flag `openfeedback_enabled`
  - Implementation: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/session/FeedbackForm.kt`
  - ViewModel: `FeedbackFormViewModel.kt`
  - Fallback flag: `openfeedback_fallback_requested_android`

## Data Storage

**Databases:**
- Apollo Normalized Cache (SQLite) - Multiplatform
  - Connection: Managed by Apollo client
  - Cache strategy: ID-based caching with custom `CacheKeyGenerator` and `CacheResolver`
  - Location: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt`

**File Storage:**
- Local filesystem only - No cloud file storage detected
- Image caching: Coil 2.7.0 (`coil-compose`)

**Caching:**
- Apollo Normalized Cache: In-memory + SQLite persistence
- Compose state: Runtime in-app state management

## Authentication & Identity

**Auth Provider:**
- Custom (if used) - Not explicitly detected in primary code paths
- Firebase Authentication integration available via Firebase SDK (33.16.0 BOM)
- Session-based: GraphQL server accepts conference header only

**Implementation:**
- GraphQL header-based: `conference: devfestnantes2025` header passed to all requests
- No explicit OAuth/OIDC detected

## Monitoring & Observability

**Error Tracking:**
- Firebase Crashlytics - Crash and exception reporting
  - SDK: `firebase-crashlytics-ktx`
  - Plugin: `com.google.firebase.crashlytics:3.0.6`
  - Implementation: Integrated via Firebase BOM

**Logs:**
- Timber 5.0.1 - Logging framework
- Firebase Analytics 33.16.0 - Event tracking
- Firebase Performance Monitoring 33.16.0 (`firebase-perf-ktx`)
  - Plugin: `com.google.firebase.firebase-perf:2.0.1`

**Remote Configuration:**
- Firebase Remote Config 33.16.0 (`firebase-config-ktx`)
  - Used for: Feature flags (`openfeedback_enabled`, `openfeedback_fallback_requested_android`)
  - Implementation: `FeedbackFormViewModel.kt` fetches and activates remote config

## CI/CD & Deployment

**Hosting:**
- Not explicitly configured in codebase (would be Firebase Hosting or similar)

**CI Pipeline:**
- GitHub Actions
  - Android workflow: `.github/workflows/android.yml`
  - iOS workflow: `.github/workflows/ios.yml`
- Dependabot: Enabled for Gradle updates (`.github/dependabot.yml`)

**Build Secrets:**
- Secrets Gradle Plugin 2.0.1 - Manages `google-services.json` API keys
- BuildConfig fields generated from `secrets.properties` or GitHub Actions secrets

## Environment Configuration

**Required env vars/files:**
- `google-services.json` - Firebase configuration for Android
  - Location: `androidApp/google-services.json`
  - Provides: Firebase project ID (451587929709), API keys, and service configuration
  - Multiple entries for: release and debug builds, production and dev app IDs

**Secrets location:**
- Android: `local.properties` (not committed), `secrets.properties` (not committed)
- Build config: Generated at compile time via Secrets Gradle Plugin

**Firebase Configuration:**
- Project: `devfest-nantes-2022`
- Project Number: 451587929709
- Storage Bucket: `devfest-nantes-2022.firebasestorage.app`
- Multiple package configurations:
  - `com.gdgnantes.devfest.mobile.androidapp` (production)
  - `com.gdgnantes.devfest.mobile.androidapp.dev` (debug)
  - Legacy: `com.gdgnantes.devfest.androidapp` and `.dev` variant

## Webhooks & Callbacks

**Incoming:**
- OpenFeedback callbacks - Session feedback submission
- Firebase Remote Config fetch triggers

**Outgoing:**
- None explicitly detected
- Potential: Analytics events to Firebase, crash reports to Crashlytics

---

*Integration audit: 2026-09-11*
