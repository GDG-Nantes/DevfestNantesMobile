# Development Setup Guide

This guide provides step-by-step instructions for setting up the DevFest Nantes development environment on your local machine.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Project Setup](#project-setup)
- [Platform-Specific Setup](#platform-specific-setup)
- [IDE Configuration](#ide-configuration)
- [Verification](#verification)
- [Common Issues](#common-issues)

## Prerequisites

### System Requirements

#### For Android Development
- **Operating System:** Windows 10+, macOS 10.14+, or Linux (Ubuntu 18.04+)
- **RAM:** 8 GB minimum, 16 GB recommended
- **Storage:** 4 GB available space minimum
- **Java:** JDK 11 or higher

#### For iOS Development (macOS only)
- **Operating System:** macOS 14.0 (Sonoma) or later
- **Xcode:** 16.0 or later
- **iOS Deployment Target:** 15.0+ (for broad device compatibility)
- **RAM:** 8 GB minimum, 16 GB recommended

### Required Tools

#### 1. Android Studio
Download and install the latest stable version of [Android Studio](https://developer.android.com/studio).

**Installation:**
1. Download Android Studio from the official website
2. Follow the installation wizard
3. Install Android SDK (API 36 minimum for latest features, API 23+ for device support)
4. Install Android SDK Build-Tools
5. Configure Android Virtual Device (AVD) for testing

#### 2. Xcode (macOS only)
Install Xcode from the Mac App Store or [Apple Developer Portal](https://developer.apple.com/xcode/).

**Installation:**
1. Install Xcode from the Mac App Store
2. Launch Xcode and accept the license agreements
3. Install additional components when prompted
4. Install Command Line Tools: `xcode-select --install`

#### 3. Git
Install Git from [git-scm.com](https://git-scm.com/) or use your system's package manager.

## Environment Setup

### 1. Java Development Kit (JDK)

#### Option A: Using SDKMAN (Recommended)
```bash
# Install SDKMAN
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 11
sdk install java 11.0.22-tem
sdk use java 11.0.22-tem
```

#### Option B: Manual Installation
Download and install JDK 11 from [Eclipse Temurin](https://adoptium.net/).

### 2. Android SDK Configuration

1. Open Android Studio
2. Go to `Settings/Preferences > Appearance & Behavior > System Settings > Android SDK`
3. Install the following:
   - Android 16 API Preview (API level 36) for latest features
   - Android 14 (API level 34) for stable development
   - Android 6.0 (API level 23) for minimum support
   - Android SDK Build-Tools 34.0.0+
   - Android Emulator
   - Android SDK Platform-Tools

### 3. Environment Variables

Add the following to your shell profile (`~/.bashrc`, `~/.zshrc`, etc.):

```bash
# Android SDK
export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
# export ANDROID_HOME=$HOME/Android/Sdk        # Linux
# export ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk  # Windows

export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools/bin

# Java (if not using SDKMAN)
export JAVA_HOME=/path/to/your/jdk11
export PATH=$PATH:$JAVA_HOME/bin
```

Reload your shell configuration:
```bash
source ~/.zshrc  # or ~/.bashrc
```

## Project Setup

### 1. Clone the Repository

```bash
git clone https://github.com/GDG-Nantes/DevfestNantesMobile.git
cd DevfestNantesMobile
```

### 2. Verify Gradle Setup

Check that Gradle can resolve dependencies:

```bash
# Check Gradle version
./gradlew --version

# Download dependencies
./gradlew build --refresh-dependencies
```

### 3. Project Structure Verification

Ensure your project structure matches:

```
DevfestNantesMobile/
├── androidApp/
├── iosApp/
├── shared/
├── shared-ui/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

## Platform-Specific Setup

### Android Setup

#### 1. Open Project in Android Studio

1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `DevfestNantesMobile` directory
4. Wait for Gradle sync to complete

#### 2. Configure Build Variants

1. Open `Build Variants` panel (View > Tool Windows > Build Variants)
2. Select `debug` build variant for development

#### 3. Create Virtual Device

1. Open AVD Manager (Tools > AVD Manager)
2. Create a new virtual device:
   - Device: Pixel 7 Pro (recommended)
   - System Image: Android 14 (API 34) or Android 16 Preview (API 36)
   - Configuration: Default settings

#### 4. Build and Run Android App

```bash
# Command line build
./gradlew :androidApp:assembleDebug

# Install on connected device
./gradlew :androidApp:installDebug

# Run tests
./gradlew :androidApp:testDebugUnitTest
```

### iOS Setup (macOS only)

#### 1. Generate Kotlin Framework

Before opening the iOS project, generate the necessary Kotlin framework:

```bash
# Generate dummy framework (required for initial setup)
./gradlew :shared:generateDummyFramework

# Or build the full framework
./gradlew :shared:syncFramework
```

#### 2. Open iOS Project

```bash
# Open in Xcode
open iosApp/iosApp.xcodeproj
```

#### 3. Configure iOS Project

1. Select the `iosApp` scheme in Xcode
2. Choose a simulator or connected device
3. Verify the minimum deployment target is set to iOS 15.0

#### 4. Recommended Simulator Setup

For consistent testing, use the iPhone 16 Pro simulator with iOS 18.3.1:

```bash
# Create simulator from command line
xcrun simctl create "iPhone 16 Pro Test" com.apple.CoreSimulator.SimDeviceType.iPhone-16-Pro com.apple.CoreSimulator.SimRuntime.iOS-18-3
```

#### 5. Build and Run iOS App

From Xcode:
1. Select Product > Build
2. Select Product > Run

From command line:
```bash
# Build for simulator
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.3.1' build

# Run tests
xcodebuild test -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'platform=iOS Simulator,name=iPhone 16 Pro,OS=18.3.1'
```

## IDE Configuration

### Android Studio Configuration

#### 1. Install Useful Plugins

Go to `Settings > Plugins` and install:
- **Kotlin Multiplatform Mobile** (if not already installed)
- **Detekt** (for code quality)
- **GitToolBox** (for Git integration enhancements)

#### 2. Code Style Configuration

1. Go to `Settings > Editor > Code Style > Kotlin`
2. Import the project's code style (if available) or configure:
   - Indentation: 4 spaces
   - Continuation indent: 8 spaces
   - Tab size: 4

#### 3. Build Configuration

Ensure these settings in `Settings > Build, Execution, Deployment > Build Tools > Gradle`:
- Use Gradle from: 'gradle-wrapper.properties' file
- Gradle JVM: Project SDK (Java 11+)

### Xcode Configuration

#### 1. Code Formatting

Set up consistent Swift formatting:
1. Xcode > Preferences > Text Editing
2. Enable "Automatic indent"
3. Configure indentation to use 4 spaces

#### 2. Simulator Configuration

For optimal development experience:
1. Simulator > Device > Manage Devices
2. Create devices for different screen sizes:
   - iPhone 16 Pro (main testing device)
   - iPhone SE (small screen testing)
   - iPad Pro (tablet testing)

## Verification

### 1. Verify Android Setup

Run these commands to ensure everything works:

```bash
# Check Android setup
./gradlew :androidApp:assembleDebug

# Verify shared module builds
./gradlew :shared:build

# Run shared module tests
./gradlew :shared:test
```

Expected output: Build successful with no errors.

### 2. Verify iOS Setup

```bash
# Verify framework generation
./gradlew :shared:syncFramework

# Build iOS project
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug build
```

Expected output: Build successful with no errors.

### 3. Test App Functionality

#### Android Test Checklist
- [ ] App launches successfully
- [ ] Agenda screen loads data
- [ ] Speaker list displays
- [ ] Navigation between screens works
- [ ] Pull-to-refresh functionality

#### iOS Test Checklist
- [ ] App launches successfully
- [ ] Agenda screen loads data
- [ ] Speaker list displays
- [ ] Navigation between screens works
- [ ] Pull-to-refresh functionality

## Common Issues

### Android Issues

#### Issue: Gradle sync fails
**Solution:**
```bash
# Clean and rebuild
./gradlew clean build --refresh-dependencies

# Clear Gradle cache
rm -rf ~/.gradle/caches/
./gradlew build
```

#### Issue: Android SDK not found
**Solution:**
1. Verify `ANDROID_HOME` environment variable
2. Check Android Studio SDK location in Preferences
3. Ensure Android SDK is properly installed

#### Issue: Build tools version mismatch
**Solution:**
Update `build.gradle.kts` files to use consistent versions defined in `gradle/libs.versions.toml`.

### iOS Issues

#### Issue: "Kotlin framework doesn't exist"
**Solution:**
```bash
# Generate the framework first
./gradlew :shared:generateDummyFramework

# Then open Xcode project
open iosApp/iosApp.xcodeproj
```

#### Issue: Swift Package Manager build failures
**Solution:**
1. Clean build folder: Product > Clean Build Folder
2. Reset package caches: File > Packages > Reset Package Caches
3. Update to latest package versions: File > Packages > Update to Latest Package Versions

#### Issue: KSP compatibility issues
**Solution:**
Verify Kotlin and KSP versions are compatible in `gradle/libs.versions.toml`:
```toml
kotlin = "2.2.0"
ksp = "2.2.0-2.0.2"  # Should match Kotlin version
```

### Cross-Platform Issues

#### Issue: Version catalog sync issues
**Solution:**
1. Check `gradle/libs.versions.toml` for syntax errors
2. Ensure all versions are properly defined
3. Run `./gradlew build --refresh-dependencies`

#### Issue: KMP-NativeCoroutines integration
**Solution:**
Ensure proper dependency versions:
```toml
kmpNativeCoroutines = "1.0.0-ALPHA-45"
```

## Next Steps

After successful setup:

1. **Read the [Architecture Guide](documentation/ARCHITECTURE.md)** to understand the codebase structure
2. **Review [Contributing Guidelines](CONTRIBUTING.md)** for development workflow
3. **Check [API Documentation](documentation/API.md)** for GraphQL integration details
4. **Run the test suites** to ensure everything works correctly
5. **Start with small changes** to familiarize yourself with the codebase

## Getting Help

If you encounter issues not covered in this guide:

1. Check the [Troubleshooting Guide](documentation/TROUBLESHOOTING.md)
2. Search existing [GitHub Issues](https://github.com/GDG-Nantes/DevfestNantesMobile/issues)
3. Create a new issue with detailed error information
4. Ask for help in the project's discussion forum

---

**Estimated Setup Time:**
- Android only: 30-45 minutes
- Android + iOS: 60-90 minutes

Happy coding! 🚀
