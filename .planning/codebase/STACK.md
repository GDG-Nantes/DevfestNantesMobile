# Technology Stack

**Analysis Date:** 2026-09-11

## Languages

**Primary:**
- Kotlin 2.2.0 - Shared module, Android app, and business logic
- Swift - iOS app UI and platform-specific code

**Secondary:**
- Java/Kotlin - Gradle buildSrc conventions (`buildSrc/src/main/java/Dependencies.kt`)

## Runtime

**Environment:**
- Android Runtime (min API 23, target API 36)
- JVM 11+ for Android library
- JVM 17 for Android application
- Swift Runtime (iOS 13+)

**Package Manager:**
- Gradle 8.13.0 (AGP 8.13.0)
- Gradle Kotlin DSL (all build files are `.kts`)
- Lockfile: `gradle/libs.versions.toml` (version catalog)

## Frameworks

**Core:**
- Jetpack Compose 2025.09.01 - UI framework for Android
- Kotlin Multiplatform Mobile (KMP) - Shared code between Android and iOS
- Apollo GraphQL 4.3.3 - GraphQL client for data fetching and caching

**Dependency Injection:**
- Dagger Hilt 2.57.2 - DI framework for Android

**Navigation:**
- Jetpack Navigation Compose 2.9.5 - Navigation framework for Compose

**State Management:**
- Kotlin Coroutines 1.10.2 - Async operations and reactive flows
- Jetpack Compose State - UI state management

**Testing:**
- JUnit 4 - Unit testing framework
- Espresso 3.6.1 - Android UI testing
- Compose UI Test - Compose-specific UI testing
- Kotlin Test - Multiplatform testing

**Build/Dev:**
- Detekt 1.23.8 - Kotlin linter
- Kotlin Serialization 1.9.0 - JSON serialization
- KSP (Kotlin Symbol Processor) 2.2.20-2.0.3 - Annotation processing

## Key Dependencies

**Critical:**
- `firebase-bom:33.16.0` - Firebase services umbrella dependency
- `apollo-runtime:4.3.3` - GraphQL client runtime
- `dagger-hilt-android:2.57.2` - Dependency injection
- `androidx-compose-bom:2025.09.01` - Compose framework versioning

**Infrastructure:**
- `kotlinx-coroutines-core:1.10.2` - Async programming
- `kotlinx-serialization-json:1.9.0` - JSON parsing/serialization
- `kotlinx-datetime:0.6.2` - Multiplatform date/time handling
- `coil-compose:2.7.0` - Image loading in Compose
- `timber:5.0.1` - Logging utility
- `accompanist:0.36.0` - Jetpack Compose extensions (pager, system UI controller)
- `apollo-normalized-cache-sqlite` - SQLite caching for GraphQL

**Material Design:**
- `androidx-compose-material3` - Material Design 3 components
- `material:1.13.0` - Material Design components (fallback)
- `androidx-compose-material-icons-extended` - Extended icon set

## Configuration

**Environment:**
- Configured via `gradle.properties`:
  - Gradle JVM args: `-Xmx2048M`
  - Kotlin style: `official`
  - Android X enabled: `true`
  - MPP C-Interop: enabled

**Build:**
- Root config: `build.gradle.kts`
- Android app config: `androidApp/build.gradle.kts`
- Shared module config: `shared/build.gradle.kts`
- Version catalog: `gradle/libs.versions.toml`
- Secrets management: Secrets Gradle Plugin 2.0.1 (for API keys and build config fields)

**Build Config Fields** (set via `androidApp/build.gradle.kts`):
- `OPEN_FEEDBACK_ENABLED` - Feature flag for feedback system
- `OPEN_FEEDBACK_PROJECT_ID` - OpenFeedback Firebase project configuration
- `OPEN_FEEDBACK_FIREBASE_PROJECT_ID`, `OPEN_FEEDBACK_FIREBASE_APPLICATION_ID`, `OPEN_FEEDBACK_FIREBASE_API_KEY`, `OPEN_FEEDBACK_FIREBASE_DATABASE_URL`

## Platform Requirements

**Development:**
- Android SDK Compile Level: 36
- Android SDK Min Level: 23
- Java version: 17 for Android app, 11 for shared library
- Kotlin compiler: 2.2.0

**Production:**
- Android: Minimum API level 23 (Android 6.0)
- iOS: Generated via Cocoapods integration in shared module
- Release builds: ProGuard/R8 enabled with resource shrinking

---

*Stack analysis: 2026-09-11*
