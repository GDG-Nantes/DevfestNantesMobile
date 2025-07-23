// This is a Kotlin Multiplatform project with an androidApp and an iosApp modules.
// Always prefer Swift Package Manager (SPM) over CocoaPods for iOS dependencies.
// All dependencies used in build.gradle.kts or build.gradle files must always be defined in the libs.versions.toml file.

## iOS issues Prevention Measures
1. **Version Alignment**: Always verify KSP compatibility when updating Kotlin versions
2. **Path Testing**: Test SwiftGen configuration manually before relying on build integration
3. **Documentation**: Maintain clear documentation of working directory contexts for build scripts