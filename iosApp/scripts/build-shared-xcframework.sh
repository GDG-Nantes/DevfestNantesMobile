#!/bin/zsh
# Build the KMP shared module as an XCFramework and copy it to the iOS Frameworks directory
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SHARED_MODULE_PATH="$PROJECT_ROOT/shared"
IOS_FRAMEWORKS_PATH="$PROJECT_ROOT/iosApp/Frameworks"

# Build the XCFramework using Gradle
cd "$PROJECT_ROOT"
./gradlew :shared:assembleXCFramework

# Find the built XCFramework
XCFRAMEWORK_PATH="$SHARED_MODULE_PATH/build/XCFrameworks/release/shared.xcframework"

if [ ! -d "$XCFRAMEWORK_PATH" ]; then
  echo "Error: XCFramework not found at $XCFRAMEWORK_PATH"
  exit 1
fi

# Create Frameworks directory if it doesn't exist
mkdir -p "$IOS_FRAMEWORKS_PATH"

# Remove any old version
rm -rf "$IOS_FRAMEWORKS_PATH/shared.xcframework"

# Copy the new XCFramework
cp -R "$XCFRAMEWORK_PATH" "$IOS_FRAMEWORKS_PATH/"

echo "shared.xcframework built and copied to $IOS_FRAMEWORKS_PATH"
