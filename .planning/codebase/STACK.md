---
last_mapped_commit: f6a603391f893d8f6bd0964cfcca500620b6a072
last_mapped_at: 2026-10-16
---
# Technology Stack

**Analysis Date:** 2026-10-16

## Languages

**Primary:**
- Kotlin 2.4.20 (`gradle/libs.versions.toml`, `kotlin` version) - All shared, Android, and feature modules (`core/*`, `feature/*`, `shared/`, `shared-ui/`, `androidApp/`)

**Secondary:**
- Swift / Xcode project - iOS host app (`iosApp/iosApp.xcodeproj`), consumes the `shared` Kotlin framework
- Groovy/Kotlin DSL for Gradle - Build scripts (`build.gradle.kts`, `settings.gradle.dcl`, `build-logic/`)
- GraphQL - API schema and operations (`core/network/src/commonMain/graphql/schema.graphqls`, `operations.graphql`, `extra.graphqls`)
- Bash - CI and gate scripts (`scripts/di-gate.sh`)

## Runtime

**Environment:**
- Android: minSdk 26, compileSdk 37 (`build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidSdk.kt`). targetSdk is app-only and kept at its pre-existing value (see the comment in `AndroidSdk.kt`).
- JVM target 17 for Android and Kotlin compilation (`AndroidCommon.kt`, `KmpLibraryConventionPlugin.kt`)
- iOS: Kotlin/Native framework `shared` (static, `binaries.framework`) with the experimental memory model (`shared/build.gradle.kts`)
- CI Java version: 17 (`.github/workflows/android.yml`, `JAVA_VERSION: 17`)

**Package Manager:**
- Gradle 9.7.1 via wrapper (`gradle/wrapper/gradle-wrapper.properties`), with checksum validation enabled
- Android Gradle Plugin 9.4.0 (`agp` in `gradle/libs.versions.toml`)
- Version catalog: `gradle/libs.versions.toml`
- Included build: `build-logic/` (convention plugins), wired through `settings.gradle.dcl`
- Lockfile: Not detected (no `gradle.lockfile` or `package-lock`-style file found at the root)
- Gradle JVM args: `-Xmx4096M` (`gradle.properties`)

## Frameworks

**Core:**
- Kotlin Multiplatform (`org.jetbrains.kotlin.multiplatform`) - Shared logic across Android and iOS
- Jetpack Compose via Compose BOM `2026.09.00` - Android UI (`androidx.compose.material3`, `material`, `material-icons-extended`, `ui-tooling`)
- Kotlin Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`)
- Navigation Compose `2.9.5` (`androidx.navigation:navigation-compose`)
- Koin `4.2.2` - Dependency injection (`koin-core`, `koin-android`, `koin-androidx-compose`); convention plugin `devfest.android.koin` applied in `androidApp/build.gradle.kts`
- Apollo Kotlin `5.2.0` - GraphQL client, with normalized cache `1.0.8` (`apollo-runtime`, `normalized-cache`, `normalized-cache-sqlite`) and the `com.apollographql.apollo` Gradle plugin for codegen
- Activity `1.12.4` (`activity-compose`, `activity-ktx`)
- kotlinx-coroutines `1.11.0` (`kotlinx-coroutines-core`, `kotlinx-coroutines-android`)
- kotlinx-serialization `1.11.0` (`kotlinx-serialization-json`, plugin `org.jetbrains.kotlin.plugin.serialization`)
- kotlinx-datetime `0.8.0-0.6.x-compat` - Pinned to a compat release on purpose. The catalog comment states that a plain kotlinx-datetime bump is rejected by `OpenFeedbackDatetimeLinkageTest`, and first-party code must use `kotlin.time.Instant` / `kotlin.time.Clock`.
- KMP Native Coroutines `1.0.6` (`com.rickclephas.kmp.nativecoroutines`) - Exposes Flows to Swift
- Coil `2.7.0` (`io.coil-kt:coil-compose`) - Image loading in Compose
- Accompanist `0.36.0` (pager, pager-indicators, systemuicontroller)
- OpenFeedback `1.0.0-alpha.3` (`openfeedback-m3`, `openfeedback-viewmodel`) - In-app session feedback forms

**Testing:**
- JUnit 4.13.2 (`junit`)
- Robolectric `4.17` - JVM Android tests
- kotlinx-coroutines-test `1.11.0` and Turbine `1.2.1` - Flow and coroutine tests
- Koin test `koin-test`, `koin-test-junit4` - DI graph verification
- Compose UI test (`ui-test-junit4`), Espresso `3.6.1` (core, contrib, intents), AndroidX Test `1.5.0` runner/rules/core, `junit-ktx` `1.2.1`, Truth
- Kover `0.9.11` (`org.jetbrains.kotlinx.kover`) - Coverage
- Test harness modules: `core/testing`

**Build/Dev:**
- Detekt `2.0.0-alpha.6` (`dev.detekt`, with ktlint wrapper rules) - Static analysis, run in CI via `./gradlew detekt`
- Android lint - Run in CI via `./gradlew lint`
- KSP `2.3.12` (`com.google.devtools.ksp`)
- Gradle Secrets Plugin `2.0.1` (`com.google.android.libraries.mapsplatform.secrets-gradle-plugin`) - Applied to the app module
- Google Services plugin `4.5.0`, Firebase Crashlytics plugin `3.0.8`, Firebase Performance plugin `2.0.2`
- Timber `5.0.1` - Logging

## Key Dependencies

**Critical:**
- Firebase BoM `34.19.0` (`firebase-analytics`, `firebase-config`, `firebase-crashlytics`, `firebase-perf`) - Analytics, remote config, crash reporting, performance monitoring. Wired through `core/analytics` (Android source set `AnalyticsPlatformModule.android.kt`, `FirebaseAnalyticsService.kt`, `FirebasePerformanceMonitoring.kt`) and `feature/session-detail` (`SessionDetailModule.kt`, Remote Config).
- Apollo Kotlin `5.2.0` - Sole networking path to the conference GraphQL API (`core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/Apollo.kt`)
- Koin `4.2.2` - All DI modules; there is a DI gate script at `scripts/di-gate.sh`, and the Hilt-to-Koin migration is recorded in git history
- OpenFeedback `1.0.0-alpha.3` - Depends on kotlinx-datetime 0.6.x at runtime, which explains the compat pin above

**Infrastructure:**
- `androidx.core:core-splashscreen` `1.0.1` - Splash screen (`androidApp`)
- `androidx.browser` `1.8.0`, `androidx.preference` `1.2.1`, `com.google.android.material:material` `1.13.0`
- `androidx.core:core-ktx` `1.16.0`

## Modules

Declared in `settings.gradle.dcl`:
- `:androidApp` - Android application (`com.gdgnantes.devfest.mobile.androidapp`, namespace `com.gdgnantes.devfest.androidapp`, versionName `2025.10.00`, versionCode 37)
- `:shared` - KMP framework exported to iOS (`shared.podspec` present)
- `:core:model`, `:core:network`, `:core:analytics`, `:core:data`, `:core:ui`, `:core:testing`
- `:feature:agenda`, `:feature:speakers`, `:feature:session-detail`, `:feature:venue`, `:feature:about`, `:feature:settings`
- `shared-ui/` exists as a directory but is not listed in `settings.gradle.dcl`
- `build-logic/` (included build) provides convention plugins: `devfest.android.application`, `devfest.android.koin`, `devfest.kmp.library`, and feature-level plugins

## Build Types

- `release`: `isMinifyEnabled = true`, `isShrinkResources = true`, ProGuard using `proguard-android-optimize.txt` plus `proguard-rules.pro`
- `debug`: `applicationIdSuffix = ".dev"`, `versionNameSuffix = "-DEBUG"`

## Configuration

**Environment:**
- Gradle properties: `gradle.properties` (JVM args, `android.useAndroidX=true`, `kotlin.mpp.enableCInteropCommonization=true`)
- Local machine properties: `local.properties` present at repo root (not read)
- Build-time config via `buildConfigField` in `androidApp/build.gradle.kts`. The `OPEN_FEEDBACK_*` fields are placeholders in source; real values are supplied outside the repo.
- Feature flag: `OPEN_FEEDBACK_ENABLED` defaults to `"false"` in `androidApp/build.gradle.kts`
- Firebase configuration: `google-services.json` is consumed by the Google Services plugin for the Android app (not read)

**Build:**
- Root: `build.gradle.kts`, `settings.gradle.dcl`, `gradle.properties`, `gradle/libs.versions.toml`
- Convention plugins: `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/` (`AndroidSdk.kt`, `AndroidCommon.kt`, `KmpLibraryConventionPlugin.kt`, `AndroidApplicationConventionPlugin.kt`, `AndroidFeatureConventionPlugin.kt`, `DependencyResolutionRules.kt`)
- Apollo codegen: `core/network/build.gradle.kts` (service `service`, package `com.gdgnantes.devfest.core.network.graphql`, data builders generated)
- Shared iOS framework: `shared/build.gradle.kts`, `shared/shared.podspec`
- iOS project: `iosApp/iosApp.xcodeproj`
- Dependabot: `.github/dependabot.yml`

## CI

- `.github/workflows/android.yml` - Ubuntu runners. Jobs: detekt and lint (plus DI gate), unit tests (`testDebugUnitTest jvmTest testAndroidHostTest`), instrumentation tests on API level 34
- `.github/workflows/ios.yml` - macOS runners (`macos-latest`), selects the latest installed Xcode, builds with `xcodebuild` against `iosApp/iosApp.xcodeproj`
- Shared composite action: `.github/actions/android-setup`

## Platform Requirements

**Development:**
- JDK 17
- Android SDK with compileSdk 37 platform
- Xcode (latest installed) for iOS builds, macOS host required
- Gradle wrapper 9.7.1 (`./gradlew`)

**Production:**
- Android: minSdk 26 APK/AAB, published via Google Play (applicationId `com.gdgnantes.devfest.mobile.androidapp`)
- iOS: `shared` Kotlin/Native static framework linked into `iosApp`

---

*Stack analysis: 2026-10-16*
