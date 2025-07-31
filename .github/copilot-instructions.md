# DevFest Nantes - Coding Instructions

This is a Kotlin Multiplatform project with an androidApp and an iosApp modules.

## Project Documentation

Before making changes, please review the comprehensive project documentation:

- **[README.md](../README.md)** - Project overview, setup instructions, and quick start guide
- **[ARCHITECTURE.md](../documentation/ARCHITECTURE.md)** - Complete architecture overview, module structure, and design patterns
- **[DEVELOPMENT.md](../documentation/DEVELOPMENT.md)** - Detailed development setup guide for both Android and iOS
- **[CONTRIBUTING.md](../CONTRIBUTING.md)** - Contribution guidelines, coding standards, and development workflow

These documents provide essential context for understanding the project structure, development patterns, and best practices.

## Architecture Context

This project follows Clean Architecture with Kotlin Multiplatform Mobile (KMM):

### Module Structure
- **`shared/`** - Core business logic, data models, GraphQL client, and store pattern implementation
- **`shared-ui/`** - Shared UI components (experimental)
- **`androidApp/`** - Android-specific UI using Jetpack Compose, Hilt DI, and MVVM pattern
- **`iosApp/`** - iOS-specific UI using SwiftUI, native patterns, and KMP-NativeCoroutines

### Key Patterns
- **Repository Pattern**: `DevFestNantesStore` interface for data access abstraction
- **Builder Pattern**: `DevFestNantesStoreBuilder` for configuration
- **MVVM**: ViewModels for business logic, reactive state management
- **Reactive Streams**: StateFlow (Android) and Combine (iOS) for data flow

## General Guidelines

- Always prefer Swift Package Manager (SPM) over CocoaPods for iOS dependencies
- All dependencies used in build.gradle.kts or build.gradle files must always be defined in the libs.versions.toml file
- Follow the architectural patterns documented in documentation/ARCHITECTURE.md
- Refer to CONTRIBUTING.md for detailed coding standards and conventions

## iOS Development Standards

### Logging
- Use the modern `Logger` API instead of deprecated `os_log`
- Follow this pattern for consistent logging across ViewModels:
  ```swift
  Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "CategoryName").error("Error message: \(error.localizedDescription)")
  ```
- Use appropriate category names that match the ViewModel or feature area (e.g., "Speakers", "About", "Schedule")

### Issues Prevention Measures
1. **Version Alignment**: Always verify KSP compatibility when updating Kotlin versions
2. **Path Testing**: Test SwiftGen configuration manually before relying on build integration
3. **Documentation**: Maintain clear documentation of working directory contexts for build scripts

### Build Testing
- **Shared Framework Generation**: Before building iOS, generate the Kotlin framework with `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`
- **Simulator Testing**: Always test iOS builds using the iPhone 16 Pro simulator running iOS 18.3.1 to ensure compatibility with the latest iOS version and hardware configuration
- Use command: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.3.1' build`
- This ensures builds work correctly on the latest simulator hardware and iOS version

## Development Workflow

### GitHub CLI Usage
- **Prefer GitHub CLI** if it is installed on the developer's computer for creating pull requests and managing repository interactions
- Use `gh pr create` to create pull requests from the command line
- Use `gh pr view` to review existing pull requests
- Use `gh repo clone` for cloning repositories when available

### Pull Request Guidelines
- When creating a pull request, follow the pull request template located in `.github/pull_request_template.md`
- **Only use relevant sections** from the template - not all sections need to be filled out for every pull request
- Focus on the sections that make sense for your specific changes:
  - Always include: Description, Type of Change, Changes Made
  - Include Platform Testing sections only if your changes affect Android/iOS
  - Include Screenshots/Videos only for UI changes
  - Include Breaking Changes section only if applicable
- Use meaningful commit messages following conventional commit format
- Ensure your branch is up-to-date with the main branch before creating the pull request