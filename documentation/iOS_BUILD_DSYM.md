# iOS Release Builds and dSYMs

How to build the DevFest Nantes iOS app for App Store Connect with the debug symbols (dSYMs) that
Firebase Crashlytics and App Store crash reports need.

## Overview

Debug symbols are needed for:
- Firebase Crashlytics crash reports
- App Store Connect crash analysis
- Symbolicated crash logs in general

When Firebase comes from Swift Package Manager, App Store Connect can report missing dSYMs for
`FirebaseAnalytics.framework` and `GoogleAppMeasurement.framework`, because those binary frameworks
do not ship separate dSYM files. `scripts/extract-firebase-dsyms.sh` works around this.

## Xcode Project Setup

### Build settings (Release)

```
DEBUG_INFORMATION_FORMAT = dwarf-with-dsym
GENERATE_DEBUG_SYMBOLS = YES
STRIP_DEBUG_SYMBOLS_DURING_COPY = NO
```

### Build phases (`DevFest Nantes` target, scheme `iosApp`)

1. **SwiftGen**: generates `SwiftGen/Assets.swift` and `SwiftGen/Strings.swift` (needs `swiftgen`)
2. **Shell script**: `./gradlew :shared:embedAndSignAppleFrameworkForXcode` builds the Kotlin framework
3. **Sources**, **Resources**, **Frameworks**
4. **Firebase Crashlytics**: uploads dSYMs to Crashlytics

## Building for the App Store

### Recommended: `scripts/build-ios-release.sh`

```bash
./scripts/build-ios-release.sh
```

The script:
- cleans previous build artifacts
- builds the shared Kotlin framework
- bumps the build number (`CURRENT_PROJECT_VERSION`) with `agvtool`
- checks that `DEBUG_INFORMATION_FORMAT` is `dwarf-with-dsym`
- archives the app and verifies that dSYMs are present, extracting missing Firebase dSYMs
- exports with `iosApp/exportOptions.plist`, which uses `destination = upload`, so the export
  uploads directly to App Store Connect

Requirements: an Apple Distribution certificate, Xcode signed in to the team's App Store Connect
account, and network access. `scripts/build-ios-development.sh` does the same with development
signing and produces a local IPA without uploading. See `scripts/README.md` for all scripts.

### Manual process

```bash
# 1. Archive
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp clean
xcodebuild -project iosApp/iosApp.xcodeproj \
           -scheme iosApp \
           -configuration Release \
           -destination generic/platform=iOS \
           -archivePath "DevFestNantes.xcarchive" \
           archive

# 2. Extract missing Firebase dSYMs, if needed
./scripts/extract-firebase-dsyms.sh ./DevFestNantes.xcarchive

# 3. Verify the dSYMs
ls -la DevFestNantes.xcarchive/dSYMs/
find DevFestNantes.xcarchive/dSYMs/ -name "*.dSYM" -exec dwarfdump --uuid {} \;

# 4. Export (uploads to App Store Connect, per exportOptions.plist)
xcodebuild -exportArchive \
           -archivePath "DevFestNantes.xcarchive" \
           -exportPath "DevFestNantes-AppStore" \
           -exportOptionsPlist iosApp/exportOptions.plist
```

## Build Numbers

`MARKETING_VERSION` follows CalVer (see [VERSIONING.md](VERSIONING.md)) and is changed by hand.
The build number must be unique for each App Store Connect upload; the release script increments it.
To manage it yourself:

```bash
cd iosApp
agvtool what-version        # current build number
agvtool next-version -all   # increment by 1
agvtool new-version -all 42 # set a specific value
```

Commit the build number change after a successful upload.

## Troubleshooting

**No dSYMs in the archive**
Check `DEBUG_INFORMATION_FORMAT = dwarf-with-dsym` for Release, clean the build folder and archive
again.

**Firebase framework dSYMs missing**
Run `./scripts/extract-firebase-dsyms.sh <archive>`. If that is not enough, update the Firebase SDK
package and clear DerivedData (`rm -rf ~/Library/Developer/Xcode/DerivedData`).

**Crashlytics build phase fails**
The phase runs the Crashlytics `run` script from the SPM checkout:

```bash
ls "${BUILD_DIR%/Build/*}/SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
```

If it is missing, resolve packages again (File > Packages > Resolve Package Versions).

**Provisioning profile errors**
Run `./scripts/refresh-provisioning-profiles.sh`.

## Manual dSYM Upload (Fallback)

1. In App Store Connect, open the app, then TestFlight > Builds, select the build and download the
   dSYMs.
2. Upload them to Crashlytics:

```bash
find . -name "*.dSYM" -exec /path/to/firebase-crashlytics-upload-symbols \
  -gsp iosApp/iosApp/GoogleService-Info.plist -p ios {} \;
```

## Related Documentation

- [DEVELOPMENT.md](DEVELOPMENT.md) - Development setup
- [VERSIONING.md](VERSIONING.md) - Version numbers
- [Apple: Building your app to include debugging information](https://developer.apple.com/documentation/xcode/building-your-app-to-include-debugging-information)
