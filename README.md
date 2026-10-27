# DevFest Nantes Mobile App

The official app of the [DevFest Nantes](https://devfest.gdgnantes.com/) conference, for Android and
iOS: schedule, speakers, venue, bookmarks and session feedback.

## 📱 Download

* **Android:** [Play Store](https://play.google.com/store/apps/details?id=com.gdgnantes.devfest.mobile.androidapp)
* **iOS:** [App Store](https://apps.apple.com/fr/app/devfest-nantes/id6443489706)

## ✨ Features

- **📅 Agenda:** sessions by day, with filters (language, complexity, room, session type, bookmarks)
- **🎤 Speakers:** profiles, their sessions and social links
- **📍 Venue:** venue information and floor plan
- **⭐ Bookmarks:** save sessions you want to attend
- **💬 Feedback:** rate sessions with [OpenFeedback](https://openfeedback.io/)
- **🔄 Offline:** data already loaded stays available through a persistent GraphQL cache
- **🌐 French and English**
- **🔒 Consent:** analytics, crash reporting and performance monitoring can be turned on or off in
  the Android app

## 🏗️ Architecture

The project is **Kotlin Multiplatform**. Data, network, domain and analytics code is shared between
Android and iOS; each platform has its own native UI (Jetpack Compose on Android, SwiftUI on iOS).

```
DevfestNantesMobile/
├── androidApp/          # Android app shell: MainActivity, NavHost, app-level Koin setup
├── feature/             # One Android module per screen area
│   ├── agenda/  speakers/  session-detail/  venue/  about/  settings/
├── core/
│   ├── model/           # Domain types and test fixtures (KMP)
│   ├── network/         # Apollo GraphQL client, schema, cache (KMP)
│   ├── data/            # DevFestNantesStore facade, bookmarks (KMP)
│   ├── analytics/       # Analytics and performance monitoring (KMP)
│   ├── testing/         # Shared fakes and test helpers (KMP)
│   └── ui/              # Compose theme and shared components (Android)
├── shared/              # KMP framework consumed by iOS, Koin entry point
├── iosApp/              # SwiftUI app (Xcode project)
├── build-logic/         # Gradle convention plugins
└── gradle/              # Wrapper and version catalog (libs.versions.toml)
```

Dependencies flow one way: `androidApp → feature:* → core:ui → core:data → core:network /
core:model`. Feature modules never depend on each other. See
[documentation/ARCHITECTURE.md](documentation/ARCHITECTURE.md) for details.

### Technology Stack

**Shared (Kotlin Multiplatform)**
- Kotlin 2.4, Coroutines, Serialization
- [Apollo Kotlin](https://www.apollographql.com/docs/kotlin) 5 with a memory + SQLite normalized cache
- [Koin](https://insert-koin.io/) for dependency injection, started once by `initKoin()` in `:shared`
- [KMP-NativeCoroutines](https://github.com/rickclephas/KMP-NativeCoroutines) to expose Flows to Swift

**Android**
- Jetpack Compose with Material 3, Navigation Compose
- Koin (`koinViewModel()` in each feature's `*Route` composable)
- Coil for images, Timber for logging
- Firebase Analytics, Crashlytics, Performance and Remote Config
- minSdk 26, compileSdk 37

**iOS**
- SwiftUI, iOS 17.0+
- Firebase iOS SDK via Swift Package Manager
- SwiftGen for typed resources

**Backend**
- GraphQL API from [Confetti](https://github.com/joreilly/Confetti)
- Session feedback from [OpenFeedback](https://openfeedback.io/)

## 🚀 Quick Start

### Prerequisites

- JDK 17
- [Android Studio](https://developer.android.com/studio) (latest stable)
- For iOS: macOS with the latest stable [Xcode](https://developer.apple.com/xcode/) and
  `brew install swiftgen`

### Build and run

```bash
git clone https://github.com/GDG-Nantes/DevfestNantesMobile.git
cd DevfestNantesMobile

# Android
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug

# iOS: Xcode builds the Kotlin framework itself through a Gradle build phase
open iosApp/iosApp.xcodeproj
```

The iOS app needs a `GoogleService-Info.plist` that is not committed; ask a maintainer. Full setup
instructions are in [documentation/DEVELOPMENT.md](documentation/DEVELOPMENT.md).

## 🧪 Testing and Code Quality

```bash
# Unit tests, as run in CI
./gradlew testDebugUnitTest jvmTest testAndroidHostTest

# Instrumented smoke tests (device or emulator)
./gradlew connectedDebugAndroidTest

# iOS Swift tests (macOS, Xcode)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp \
  -destination 'platform=iOS Simulator,name=<simulator name>' test

# Static analysis and DI gate
./gradlew detekt lint
bash scripts/di-gate.sh

# Coverage report (Kover)
./gradlew koverHtmlReport
```

Android and shared tests use JUnit 4, Turbine, Robolectric and hand-written fakes from
`core/testing`; iOS tests use Swift Testing. CI runs on GitHub
Actions (`.github/workflows/android.yml` and `ios.yml`). See
[documentation/DEVELOPMENT.md](documentation/DEVELOPMENT.md#testing) for test layout and
conventions.

## 🔢 Versioning

The app uses calendar versioning, `YYYY.0M.MICRO` (for example `2025.10.00`). Version numbers are
managed by maintainers only; do not change them in pull requests. See
[documentation/VERSIONING.md](documentation/VERSIONING.md).

## 🤝 Contributing

Contributions are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) first. In short:

- Follow the coding conventions (Detekt must pass)
- Add tests for new behavior
- Update the documentation when you change something it describes
- Use conventional commit messages

## 📚 Documentation

- **[Architecture](documentation/ARCHITECTURE.md)** - Modules, data flow, dependency injection,
  navigation
- **[Development](documentation/DEVELOPMENT.md)** - Setup, build, testing, GraphQL schema updates,
  common issues
- **[Performance Monitoring](documentation/PERFORMANCE_MONITORING.md)** - Firebase Performance and user
  consent
- **[Versioning](documentation/VERSIONING.md)** - Calendar versioning
- **[iOS Release Builds](documentation/iOS_BUILD_DSYM.md)** - App Store builds and dSYMs

## 👩‍⚖️ License

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

This project is adapted from the Android Makers app:
- [Android Makers - Android app](https://github.com/paug/AndroidMakersApp)
- [Android Makers - iOS app](https://github.com/paug/AndroidMakersApp-iOS)

Special thanks to the GDG Nantes community and all contributors who make this project possible.
