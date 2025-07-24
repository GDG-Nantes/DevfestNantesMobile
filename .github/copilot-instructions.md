# DevFest Nantes - Coding Instructions

This is a Kotlin Multiplatform project with an androidApp and an iosApp modules.

## General Guidelines

- Always prefer Swift Package Manager (SPM) over CocoaPods for iOS dependencies
- All dependencies used in build.gradle.kts or build.gradle files must always be defined in the libs.versions.toml file

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
- **Simulator Testing**: Always test iOS builds using the iPhone 16 Pro simulator running iOS 18.3.1 to ensure compatibility with the latest iOS version and hardware configuration
- Use command: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.3.1' build`
- This ensures builds work correctly on the latest simulator hardware and iOS version