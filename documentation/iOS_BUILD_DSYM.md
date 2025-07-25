# iOS dSYM and App Store Upload Guide

This document explains how to properly build and upload the DevFest Nantes iOS app to App Store Connect with correct debug symbols (dSYMs).

## Overview

Debug symbols (dSYMs) are essential for:
- Firebase Crashlytics crash reporting
- App Store crash analysis and debugging
- Symbolicated crash reports

## Common Firebase dSYM Issues

When using Firebase via Swift Package Manager (SPM), you may encounter these upload errors:
- Missing dSYM for FirebaseAnalytics.framework
- Missing dSYM for GoogleAppMeasurement.framework

This is because SPM-distributed Firebase frameworks often don't include separate dSYM files or have debug symbols embedded differently.

## Build Configuration

### Xcode Project Settings

The project is configured with the following build settings for Release configuration:

```
DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym"
GENERATE_DEBUG_SYMBOLS = YES
STRIP_DEBUG_SYMBOLS_DURING_COPY = NO
```

This ensures that debug symbols are generated and preserved during Release builds.

### Build Phases

The project includes these important build phases:

1. **SwiftGen** - Generates Swift code from resources
2. **Gradle Shared Framework** - Builds the Kotlin Multiplatform shared framework
3. **Sources** - Compiles Swift source files
4. **Resources** - Processes app resources
5. **Frameworks** - Links frameworks and libraries
6. **Extract Firebase dSYMs** - Extracts dSYMs from Firebase frameworks (NEW)
7. **Firebase Crashlytics** - Uploads dSYMs to Firebase Crashlytics

## Building for App Store

### Method 1: Automated Build Script (Recommended)

Use the enhanced build script that automatically handles dSYM extraction:

```bash
./scripts/build-ios-release.sh
```

This script will:
- Clean previous build artifacts
- Build the shared Kotlin framework
- **Automatically bump the build version** (CURRENT_PROJECT_VERSION)
- Build the archive with proper settings
- Verify dSYMs are present
- Automatically extract missing Firebase dSYMs
- Export for App Store with validation
- Upload directly to App Store Connect

### Method 2: Manual Build Process

#### 1. Archive the App

```bash
# Clean build folder first
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp clean

# Create archive for App Store
xcodebuild -project iosApp/iosApp.xcodeproj \
           -scheme iosApp \
           -configuration Release \
           -destination generic/platform=iOS \
           -archivePath "DevFestNantes.xcarchive" \
           archive
```

#### 2. Extract Missing Firebase dSYMs

If Firebase dSYMs are missing, use the extraction script:

```bash
./scripts/extract-firebase-dsyms.sh ./DevFestNantes.xcarchive
```

#### 3. Export for App Store

```bash
# Export the archive
xcodebuild -exportArchive \
           -archivePath "DevFestNantes.xcarchive" \
           -exportPath "DevFestNantes-AppStore" \
           -exportOptionsPlist exportOptions.plist
```

### 3. Verify dSYMs are Present

Before uploading, verify that dSYMs are included in the archive:

```bash
# Check archive contents
ls -la DevFestNantes.xcarchive/dSYMs/

# Should show files like:
# DevFest Nantes.app.dSYM/
# FirebaseAnalytics.framework.dSYM/         (if successfully extracted)
# GoogleAppMeasurement.framework.dSYM/      (if successfully extracted)

# Verify UUIDs
find DevFestNantes.xcarchive/dSYMs/ -name "*.dSYM" -exec dwarfdump --uuid {} \;
```

### 4. Upload to App Store Connect

```bash
# Upload using Xcode command line tools
xcrun altool --upload-app \
             --type ios \
             --file "DevFestNantes-AppStore/DevFest Nantes.ipa" \
             --username "your-apple-id@example.com" \
             --password "app-specific-password"
```

## Version Management

### Automatic Build Version Bumping

The build script automatically increments the `CURRENT_PROJECT_VERSION` for each build to ensure unique version numbers for App Store Connect.

**How it works:**
- Reads the current `CURRENT_PROJECT_VERSION` from Xcode project
- Increments the version by 1
- Updates the project using `xcrun agvtool new-version`
- Verifies the update was successful

**Manual version management:**
```bash
# Check current build version
agvtool what-version

# Set a specific build version
agvtool new-version -all 42

# Increment build version by 1
agvtool next-version -all
```

**Important Notes:**
- Marketing version (`CFBundleShortVersionString`) remains unchanged
- Only build version (`CFBundleVersion`) is auto-incremented
- Version changes are included in the git commit after successful build
- Each App Store submission requires a unique build version

## Troubleshooting dSYM Issues

### Issue: Missing dSYMs in Archive

**Solution:**
1. Verify `DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym"` is set for Release configuration
2. Clean build folder: Product → Clean Build Folder
3. Archive again: Product → Archive

### Issue: Firebase Framework dSYMs Missing

**Solution:**
Firebase frameworks should automatically include dSYMs when using Swift Package Manager. If missing:

1. Update Firebase SDK to latest version
2. Clean derived data: `rm -rf ~/Library/Developer/Xcode/DerivedData`
3. Rebuild the project

### Issue: Firebase Crashlytics Script Fails

**Solution:**
The Firebase Crashlytics build script path may need adjustment:

```bash
# Check if the script exists
ls "${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"

# Alternative path (if using CocoaPods):
"${PODS_ROOT}/FirebaseCrashlytics/run"
```

## Manual dSYM Upload (Fallback)

If automatic upload fails, you can manually upload dSYMs:

### 1. Download dSYMs from App Store Connect

1. Go to App Store Connect
2. Select your app
3. Go to TestFlight → Builds
4. Select your build
5. Download dSYMs

### 2. Upload to Firebase Crashlytics

```bash
# Upload to Firebase Crashlytics manually
find . -name "*.dSYM" -exec /path/to/firebase-crashlytics-upload-symbols -gsp GoogleService-Info.plist -p ios {} \;
```

## Build Script Automation

### Create exportOptions.plist

Create `iosApp/exportOptions.plist`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>app-store</string>
    <key>uploadBitcode</key>
    <false/>
    <key>uploadSymbols</key>
    <true/>
    <key>compileBitcode</key>
    <false/>
</dict>
</plist>
```

### Automated Build Script

Create `scripts/build-ios-release.sh`:

```bash
#!/bin/bash

set -e

echo "🏗️  Building DevFest Nantes iOS for App Store..."

# Clean
echo "🧹 Cleaning build folder..."
xcodebuild -project iosApp/iosApp.xcodeproj -scheme "DevFest Nantes" clean

# Build shared framework
echo "🔧 Building shared Kotlin framework..."
cd ..
./gradlew :shared:embedAndSignAppleFrameworkForXcode
cd iosApp

# Archive
echo "📦 Creating archive..."
xcodebuild -project iosApp.xcodeproj \
           -scheme "DevFest Nantes" \
           -configuration Release \
           -destination generic/platform=iOS \
           -archivePath "DevFestNantes.xcarchive" \
           archive

# Verify dSYMs
echo "🔍 Verifying dSYMs..."
if [ -d "DevFestNantes.xcarchive/dSYMs" ]; then
    echo "✅ dSYMs found:"
    ls -la DevFestNantes.xcarchive/dSYMs/
else
    echo "❌ No dSYMs found in archive!"
    exit 1
fi

# Export
echo "📤 Exporting for App Store..."
xcodebuild -exportArchive \
           -archivePath "DevFestNantes.xcarchive" \
           -exportPath "DevFestNantes-AppStore" \
           -exportOptionsPlist exportOptions.plist

echo "✅ Build complete! Ready for App Store upload."
echo "📁 Archive: DevFestNantes.xcarchive"
echo "📱 IPA: DevFestNantes-AppStore/DevFest Nantes.ipa"
```

## Validation

### Before Uploading

Always validate your build before uploading:

```bash
# Validate the IPA
xcrun altool --validate-app \
             --type ios \
             --file "DevFestNantes-AppStore/DevFest Nantes.ipa" \
             --username "your-apple-id@example.com" \
             --password "app-specific-password"
```

### Check dSYM UUIDs

```bash
# Check dSYM UUIDs
dwarfdump --uuid DevFestNantes.xcarchive/dSYMs/DevFest\ Nantes.app.dSYM
dwarfdump --uuid DevFestNantes.xcarchive/dSYMs/FirebaseAnalytics.framework.dSYM
```

## Common Build Settings

### Required Settings for Release

```
DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym"
GENERATE_DEBUG_SYMBOLS = YES (default)
STRIP_DEBUG_SYMBOLS_DURING_COPY = NO (for frameworks)
DEPLOYMENT_POSTPROCESSING = YES (for Release)
```

### Firebase-Specific Settings

```
FIREBASE_CRASHLYTICS_UPLOAD_SYMBOLS = YES
```

## CI/CD Integration

For GitHub Actions or other CI systems, ensure:

1. Proper code signing setup
2. Build settings include dSYM generation
3. Archive and export steps are included
4. dSYM verification before upload

## Related Documentation

- [DEVELOPMENT.md](DEVELOPMENT.md) - Development setup
- [ARCHITECTURE.md](ARCHITECTURE.md) - Project architecture
- [Apple Developer Documentation](https://developer.apple.com/documentation/xcode/building-your-app-to-include-debugging-information)

---

**Note:** This configuration should resolve the dSYM upload issues encountered with App Store Connect. The Firebase Crashlytics build phase will automatically upload symbols for crash reporting.
