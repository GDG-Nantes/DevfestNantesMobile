# Development Guide

How to set up, build, test and run the DevFest Nantes app locally.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Project Setup](#project-setup)
- [Android](#android)
- [iOS](#ios)
- [Testing](#testing)
- [Code Quality](#code-quality)
- [Build Configuration](#build-configuration)
- [Updating the GraphQL Schema](#updating-the-graphql-schema)
- [Common Issues](#common-issues)

## Prerequisites

| Tool | Version |
|---|---|
| JDK | 17 |
| Android Studio | Latest stable, with the Android SDK platform for compileSdk 37 |
| Gradle | Wrapper (`./gradlew`, Gradle 9.7.1), nothing to install |
| Xcode (iOS only, macOS) | Latest stable; the app targets iOS 17.0 |
| SwiftGen (iOS only) | `brew install swiftgen` |

Key versions live in `gradle/libs.versions.toml` (Kotlin 2.4.20, AGP 9.4.0, Compose BOM,
Koin 4.2.2, Apollo 5.2.0, ...). Android `minSdk` is 26; SDK levels are set in
`build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidSdk.kt`.

## Project Setup

```bash
git clone https://github.com/GDG-Nantes/DevfestNantesMobile.git
cd DevfestNantesMobile
./gradlew --version
```

Repository layout:

```
DevfestNantesMobile/
├── androidApp/        # Android app shell (MainActivity, NavHost, app Koin modules)
├── shared/            # KMP framework for iOS, Koin entry point initKoin()
├── core/              # model, network, data, analytics, testing (KMP) and ui (Android)
├── feature/           # agenda, speakers, session-detail, venue, about, settings (Android)
├── iosApp/            # SwiftUI app (Xcode project)
├── build-logic/       # Included build with convention plugins
├── gradle/            # Wrapper and version catalog (libs.versions.toml)
├── linters/           # Detekt configuration
├── scripts/           # DI gate and iOS build scripts (see scripts/README.md)
├── documentation/     # This documentation
├── build.gradle.kts   # Root build (plugins, Kover aggregation)
└── settings.gradle.dcl
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for what each module does.

## Android

Open the root directory in Android Studio and let Gradle sync, or use the command line:

```bash
./gradlew :androidApp:assembleDebug     # Build the debug APK
./gradlew :androidApp:installDebug      # Install on a connected device/emulator
```

The debug build uses the application id suffix `.dev` and the version name suffix `-DEBUG`, so it
can be installed next to the Play Store version.

If you use the [`android` CLI](https://developer.android.com/), `android run` builds, installs and
launches the app (see `AGENTS.md`).

## iOS

The Xcode project builds the Kotlin framework itself: a build phase runs
`./gradlew :shared:embedAndSignAppleFrameworkForXcode`.

```bash
open iosApp/iosApp.xcodeproj
```

Select the `iosApp` scheme and a simulator, then build and run. From the command line:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -destination 'platform=iOS Simulator,name=<simulator name>' build
```

To only check that the Kotlin code compiles for iOS:

```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Firebase on iOS needs `iosApp/iosApp/GoogleService-Info.plist`, which is not committed: ask a
maintainer for it (CI generates a mock one, see `.github/workflows/ios.yml`). Release builds and App Store uploads
are covered in [iOS_BUILD_DSYM.md](iOS_BUILD_DSYM.md) and `scripts/README.md`.

### Swift tests

The iOS unit tests use Swift Testing (shipped with the Xcode toolchain), live in
`iosApp/DevFestNantesTests/` and run hosted in the app (`@testable import DevFest_Nantes`). The
`iosApp` scheme runs them through the committed `iosApp/iosApp.xctestplan`. In Debug builds the app skips Firebase
and Koin start-up when launched by XCTest (`XCTestConfigurationFilePath` is set; the switch is
compiled out of Release), so tests must not create a ViewModel (its initializer starts Koin): test
static or pure helpers instead. A future UI-test target must not rely on this switch, since it would
render an empty window. Pick a simulator with
`xcrun simctl list devices available`, then:

```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp \
  -destination 'platform=iOS Simulator,id=<udid>' -derivedDataPath build \
  build-for-testing CODE_SIGNING_ALLOWED=NO
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp \
  -destination 'platform=iOS Simulator,id=<udid>' -derivedDataPath build \
  -parallel-testing-enabled NO test-without-building
```

For a quick local run, the single `xcodebuild ... -testPlan iosApp test` form works too. Test
names are snake_case sentences, as on Android.

## Testing

Tests use JUnit 4, `kotlin.test` assertions, kotlinx-coroutines-test, Turbine and Robolectric. There
is no mocking library: use the hand-written fakes in `core/testing` (`FakeDevFestNantesStore`,
`FakeBookmarksStore`, `FakeAnalyticsService`, `NoOpPerformanceMonitoring`, `MainDispatcherRule`) and
the fixtures in `core/model/.../stubs/` (`SessionFixtures`, `SpeakerFixtures`).

```bash
# What CI runs: Android unit tests, KMP JVM tests and KMP Android host tests
./gradlew testDebugUnitTest jvmTest testAndroidHostTest

# A single module
./gradlew :feature:agenda:testDebugUnitTest
./gradlew :core:data:jvmTest
./gradlew :core:domain:jvmTest

# KMP tests on the iOS simulator (macOS)
./gradlew :shared:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test

# Instrumented smoke tests (device or emulator required)
./gradlew connectedDebugAndroidTest

# Merged coverage report (Kover, report-only, no threshold)
./gradlew koverHtmlReport
```

Where tests live:

| Kind | Location | Example |
|---|---|---|
| ViewModel / service unit tests | `feature/<name>/src/test/` | `AgendaViewModelTest`, `SessionFiltersServiceImplTest` |
| Koin module wiring | `*/src/test/.../di/`, `shared/src/jvmTest/` | `SettingsModuleTest`, `KoinGraphTest` |
| App start-up graph | `androidApp/src/test/.../di/` | `KoinStartupTest`, `ProductionGraphStartupTest` |
| Store contract (fake and real) | `core/data/src/commonTest/` | `DevFestNantesStoreContractTest`, `GraphQLStoreMockServerTest` |
| Live API (manual) | `core/data/src/jvmTest/` | `GraphQLStoreLiveNetworkTest` |
| Instrumented smoke tests | `feature/<name>/src/androidTest/` | `SettingsSmokeTest` |
| iOS Swift unit tests | `iosApp/DevFestNantesTests/` | `SessionFilterTests`, `AgendaDateParsingTests` |

Conventions: test names are snake_case sentences (`french_filter_keeps_the_four_french_sessions`),
expected values reference fixtures rather than retyped literals, and `*CharacterizationTest` classes
pin current behavior, including known defects.

## Code Quality

```bash
./gradlew detekt               # Detekt + ktlint rules (config: linters/detekt-config.yml)
./gradlew lint                 # Android lint
bash scripts/di-gate.sh        # DI gate: no Hilt/Dagger, no service locator outside DI wiring
bash scripts/swift-names-gate.sh check   # macOS: Swift names of exported Kotlin types unchanged
                               # (links the shared framework unless SWIFT_GATE_HEADER is set)
bash scripts/kmp-nc-version-gate.sh      # KMP-NativeCoroutines: Gradle and SPM versions identical
                               # (pure text check, runs anywhere; first step of ios.yml)
```

The detekt, lint and DI checks run in CI (`.github/workflows/android.yml`) together with unit tests,
instrumented tests on API 34 and a debug build. `.github/workflows/ios.yml` starts with the
KMP-NativeCoroutines version gate (`check` then `selftest`), builds the shared framework, runs the Swift
names gate (`check` then `selftest`, reusing the header just linked), runs the KMP
iOS tests, then builds the app and its Swift tests once (`build-for-testing`) and runs them with
`test-without-building` through `iosApp.xctestplan` on the newest iPhone simulator (selected and
booted by UDID, tests not parallelised, no retries). The job fails when the result bundle reports
zero executed tests, only skipped tests or any failure (`scripts/xcresult-assert-tests.sh`, which
has a `selftest` mode), because `xcodebuild` itself exits 0 on a zero-test run. The DerivedData
cache is keyed on the selected Xcode version, the job summary lists the Xcode version, the
simulator and the test counts, and the `.xcresult` is uploaded as an artifact when the job fails.

### Bumping KMP-NativeCoroutines

The version must be identical in `gradle/libs.versions.toml` (`kmpNativeCoroutines`) and in the
KMP-NativeCoroutines Swift package of `iosApp/iosApp.xcodeproj` (requirement "Exact Version").
Dependabot only bumps the Gradle side, so its pull request fails the iOS gate
(`KMPNC-VERSION-SKEW`) until the pin is updated by hand: Xcode > Package Dependencies >
KMP-NativeCoroutines > Exact Version, or the `version` line of its `XCRemoteSwiftPackageReference`
in `project.pbxproj`. Then re-run `bash scripts/kmp-nc-version-gate.sh`. A range, branch or
revision requirement is rejected too (`KMPNC-SPM-NOT-EXACT`).

## Build Configuration

- **Convention plugins** (`build-logic/convention`): `devfest.android.application`,
  `devfest.android.feature`, `devfest.android.library`, `devfest.android.koin`,
  `devfest.kmp.library`, `devfest.detekt`. New modules should apply one of these instead of
  configuring Android/Kotlin by hand.
- **Build types:** `release` is minified and resource-shrunk with R8 (`proguard-rules.pro`);
  `debug` adds the `.dev` suffix.
- **OpenFeedback:** the `OPEN_FEEDBACK_*` `buildConfigField`s in `androidApp/build.gradle.kts` are
  placeholders, and `OPEN_FEEDBACK_ENABLED` defaults to `false`. With the flag off, the session
  screen falls back to a link to the web form. Real values are not committed.
- **Firebase:** `androidApp/google-services.json` (committed) and
  `iosApp/iosApp/GoogleService-Info.plist` (not committed).
- **Secrets:** the Gradle Secrets plugin is applied to `:androidApp`; keep local values in
  `local.properties`, which is not committed.

### Adding a feature module

1. Create `feature/<name>/build.gradle.kts` applying `id("devfest.android.feature")`.
2. Add `include(":feature:<name>")` to `settings.gradle.dcl`.
3. Add `<Name>Route.kt`, `<Name>ViewModel.kt` and `di/<Name>Module.kt` under
   `com.gdgnantes.devfest.feature.<name>`.
4. Add the Koin module to `androidModules` (`androidApp/.../di/AndroidModules.kt`), and the route to
   `Screen.kt` and the `NavHost` in `MainActivity`.
5. Add a `<Name>ModuleTest` and, if relevant, a ViewModel test and a smoke test.

## Updating the GraphQL Schema

The schema lives in `core/network/src/commonMain/graphql/schema.graphqls`. To refresh it from the
server with the Apollo Gradle plugin:

```bash
./gradlew :core:network:downloadApolloSchema \
  --endpoint="https://confetti-app.dev/graphql" \
  --schema="core/network/src/commonMain/graphql/schema.graphqls" \
  --header="conference:devfestnantes2025"
```

The endpoint and the `conference` header must match `core/network/.../Apollo.kt`. Queries go in
`operations.graphql`; client-side schema extensions in `extra.graphqls`.

## Common Issues

**Gradle sync fails after a dependency bump**
Run `./gradlew clean build --refresh-dependencies`. Check that the KSP version matches the Kotlin
version in `gradle/libs.versions.toml`.

**kotlinx-datetime cannot be bumped**
This is intentional. OpenFeedback depends on kotlinx-datetime 0.6.x at runtime, so the catalog pins
a `0.6.x-compat` release and `OpenFeedbackDatetimeLinkageTest` fails on a plain bump. First-party
code uses `kotlin.time.Instant` / `kotlin.time.Clock`.

**Koin fails at start-up with a duplicate definition**
Overriding is disabled on purpose. Remove the second binding instead of enabling overrides; tests
that need overrides enable them in their own `appDeclaration`.

**Instrumented tests fail with `NoSuchMethodError: registerDefaultInstance`**
A protobuf-lite version from Espresso clashes with Firebase Performance. Exclude
`com.google.protobuf:protobuf-lite` from the Espresso dependencies.

**iOS build cannot find the `shared` module**
Build once from Xcode so the Gradle build phase produces the framework, or run
`./gradlew :shared:embedAndSignAppleFrameworkForXcode` with the Xcode environment set (see the
"Build shared framework" step in `.github/workflows/ios.yml`).

**Swift Package Manager failures**
Product > Clean Build Folder, then File > Packages > Reset Package Caches.

## Getting Help

Search the [GitHub issues](https://github.com/GDG-Nantes/DevfestNantesMobile/issues) or open a new
one with the error output.
