# DevFest Nantes Mobile App

A Kotlin Multiplatform Mobile (KMM) application for the [DevFest Nantes](https://devfest.gdgnantes.com/) conference, providing attendees with schedules, speaker information, venue details, and more.

## 📱 Download

Get the app from the official app stores:

* **Android:
  ** [Play Store](https://play.google.com/store/apps/details?id=com.gdgnantes.devfest.mobile.androidapp)
* **iOS:** [App Store](https://apps.apple.com/fr/app/devfest-nantes/id6443489706)

## 🏗️ Project Architecture

This project uses **Kotlin Multiplatform Mobile (KMM)** to share business logic between Android and iOS while maintaining platform-specific UI implementations.

### Module Structure

```
DevFestNantes/
├── shared/                 # Shared business logic, models, and API clients
├── shared-ui/             # Shared UI components and resources (experimental)
├── androidApp/            # Android-specific UI and platform implementations
├── iosApp/               # iOS-specific UI and platform implementations
├── gradle/               # Gradle version catalog and wrapper
└── buildSrc/             # Build configuration and plugins
```

### Technology Stack

#### Shared (Kotlin Multiplatform)
- **Kotlin Multiplatform** for shared business logic
- **Apollo GraphQL** for API communication
- **Kotlinx Coroutines** for asynchronous programming
- **Kotlinx Serialization** for JSON handling
- **KMP-NativeCoroutines** for iOS coroutines integration

#### Android
- **Jetpack Compose** for modern UI
- **Dagger Hilt** for dependency injection
- **Navigation Component** for app navigation
- **Material 3** design system
- **Firebase** for analytics, crash reporting, and performance monitoring (with user consent)

#### iOS
- **SwiftUI** for native iOS UI
- **Swift Package Manager** for dependencies
- **iOS 15.0+** minimum deployment target

## 🚀 Quick Start

### Prerequisites

1. **Android Development:**
   - [Android Studio](https://developer.android.com/studio) (latest stable)
   - JDK 17
   - Android SDK 36

2. **iOS Development:**
   - [Xcode](https://developer.apple.com/xcode/) 15.0+
   - macOS 13.0+ (for iOS development)
   - iOS 15.0+ deployment target

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/GDG-Nantes/DevfestNantesMobile.git
   cd DevfestNantesMobile
   ```

2. **Android Setup:**
   ```bash
   # Open in Android Studio
   # The project should sync automatically
   
   # Or build from command line
   ./gradlew :androidApp:assembleDebug
   ```

3. **iOS Setup:**
   ```bash
   # Generate the Kotlin framework for iOS
   ./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
   
   # Open iOS project in Xcode
   open iosApp/iosApp.xcodeproj
   
   # Build and run in Xcode or from command line
   xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug build
   ```

### Development Workflow

1. **Shared Module Development:** Make changes to business logic in the `shared/` module
2. **Android UI:** Implement Android-specific UI in `androidApp/` using Jetpack Compose
3. **iOS UI:** Implement iOS-specific UI in `iosApp/` using SwiftUI
4. **Testing:** Run platform-specific tests and shared module tests

For detailed setup instructions, see [documentation/DEVELOPMENT.md](documentation/DEVELOPMENT.md).

## 🏛️ Architecture Overview

The app follows a clean architecture pattern with clear separation of concerns:

- **Data Layer** (`shared/`): GraphQL client, repositories, and data models
- **Domain Layer** (`shared/`): Business logic and use cases
- **Presentation Layer** (platform-specific): ViewModels and UI components

### Key Features

- **📅 Event Schedule:** Browse conference sessions by day, track, and time
- **🎤 Speaker Profiles:** Detailed speaker information and social links
- **📍 Venue Information:** Interactive venue maps and navigation
- **⭐ Bookmarks:** Save favorite sessions for easy access
- **🔄 Offline Support:** Cached data for offline viewing
- **🌐 Multi-language:** Support for French and English
- **🔒 Privacy-First:** User consent system for analytics and performance monitoring

## 🔧 Dependencies & Tooling

### Core Dependencies
- **GraphQL Server:** Powered by [Confetti](https://github.com/joreilly/Confetti)
- **Feedback System:** [OpenFeedback.io](https://openfeedback.io/) integration
- **Analytics:** Firebase Analytics, Crashlytics, and Performance Monitoring (respects user consent)
- **Version Management:** Gradle version catalog (`gradle/libs.versions.toml`)

### Versioning Strategy
The project uses **Calendar Versioning (CalVer)** with the `YYYY.0M.MICRO` format:
- **YYYY**: Full year (e.g., 2025)
- **0M**: Zero-padded month (e.g., 07 for July)
- **MICRO**: Patch version (e.g., 00)

This approach aligns releases with the DevFest Nantes conference schedule and provides clear timeline visibility. Version management is handled by project maintainers through GitHub releases.

### Development Tools
- **Code Quality:** Detekt for static analysis
- **Build System:** Gradle with Kotlin DSL
- **Testing:** JUnit, KMM test framework
- **CI/CD:** GitHub Actions (configuration in `.github/`)

## 🔄 Updating GraphQL Schema

See [documentation/DEVELOPMENT.md](documentation/DEVELOPMENT.md#4-updating-graphql-schema) for
instructions on updating your local `schema.graphqls` file using the Apollo Gradle plugin and custom
headers.

## 🧪 Testing

The project includes comprehensive testing at multiple levels:

- **Unit Tests:** Business logic testing in the shared module
- **Platform Tests:** Android and iOS specific testing
- **Integration Tests:** End-to-end feature testing

Run tests with:
```bash
# All tests
./gradlew test

# Android tests
./gradlew :androidApp:testDebugUnitTest

# Shared module tests
./gradlew :shared:test
```

## 🤝 Contributing

We welcome contributions! Please see our [contribution guidelines](CONTRIBUTING.md) for details on:

- Setting up your development environment
- Code style and conventions
- Submitting pull requests
- Reporting issues

### Quick Contribution Checklist
- [ ] Follow the established coding standards (see `.github/copilot-instructions.md`)
- [ ] Add tests for new features
- [ ] Update documentation as needed
- [ ] Use conventional commit messages

## 📚 Documentation

- **[Architecture Guide](documentation/ARCHITECTURE.md)** - Detailed architecture overview
- **[Development Setup](documentation/DEVELOPMENT.md)** - Complete setup instructions
- **[Performance Monitoring](documentation/PERFORMANCE_MONITORING.md)** - Firebase Performance implementation and usage
- **[Versioning Strategy](documentation/VERSIONING.md)** - Calendar versioning implementation and guidelines
- **[API Documentation](documentation/API.md)** - GraphQL API integration details
- **[Testing Guide](documentation/TESTING.md)** - Testing strategies and best practices
- **[Troubleshooting](documentation/TROUBLESHOOTING.md)** - Common issues and solutions
- **[Build System](documentation/BUILD.md)** - Gradle and build configuration details

## � License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

This project is adapted from the Android Makers app:
- [Android Makers - Android app](https://github.com/paug/AndroidMakersApp)
- [Android Makers - iOS app](https://github.com/paug/AndroidMakersApp-iOS)

Special thanks to the GDG Nantes community and all contributors who make this project possible.
