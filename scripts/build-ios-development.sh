#!/bin/bash

set -e

echo "🏗️  Building DevFest Nantes iOS for Development/Testing..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
IOS_DIR="$PROJECT_ROOT/iosApp"

cd "$IOS_DIR"

# Clean
echo -e "${BLUE}🧹 Cleaning build folder...${NC}"
xcodebuild -project iosApp.xcodeproj -scheme iosApp clean

# Clean up previous build artifacts
echo -e "${BLUE}🗑️  Cleaning previous build artifacts...${NC}"
rm -rf "$IOS_DIR"/DevFestNantes.xcarchive
rm -rf "$IOS_DIR"/DevFestNantes-Development

# Build shared framework first
echo -e "${BLUE}🔧 Building shared Kotlin framework...${NC}"
cd "$PROJECT_ROOT"
./gradlew :shared:linkReleaseFrameworkIosFat
cd "$IOS_DIR"

# Bump CURRENT_PROJECT_VERSION
echo -e "${BLUE}📊 Bumping build version...${NC}"
CURRENT_VERSION=$(xcodebuild -project iosApp.xcodeproj -scheme iosApp -showBuildSettings | grep CURRENT_PROJECT_VERSION | awk '{print $3}')
NEW_VERSION=$((CURRENT_VERSION + 1))

echo -e "${BLUE}🔄 Updating CURRENT_PROJECT_VERSION from $CURRENT_VERSION to $NEW_VERSION${NC}"

# Update the project file with new version
xcrun agvtool new-version -all "$NEW_VERSION"

# Verify the version was updated
UPDATED_VERSION=$(xcodebuild -project iosApp.xcodeproj -scheme iosApp -showBuildSettings | grep CURRENT_PROJECT_VERSION | awk '{print $3}')
if [ "$UPDATED_VERSION" = "$NEW_VERSION" ]; then
    echo -e "${GREEN}✅ Build version successfully updated to $NEW_VERSION${NC}"
else
    echo -e "${YELLOW}⚠️  Version update verification failed. Expected: $NEW_VERSION, Got: $UPDATED_VERSION${NC}"
fi

# Verify build settings
echo -e "${BLUE}🔍 Verifying build settings...${NC}"
DEBUG_FORMAT=$(xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Release -destination generic/platform=iOS -showBuildSettings | grep DEBUG_INFORMATION_FORMAT | awk '{print $3}')

if [ "$DEBUG_FORMAT" != "dwarf-with-dsym" ]; then
    echo -e "${RED}❌ ERROR: DEBUG_INFORMATION_FORMAT is not set to 'dwarf-with-dsym'. Current value: $DEBUG_FORMAT${NC}"
    exit 1
else
    echo -e "${GREEN}✅ DEBUG_INFORMATION_FORMAT correctly set to 'dwarf-with-dsym'${NC}"
fi

# Archive with development signing
echo -e "${BLUE}📦 Creating archive with development signing...${NC}"
ARCHIVE_PATH="$IOS_DIR/DevFestNantes.xcarchive"
rm -rf "$ARCHIVE_PATH"

xcodebuild -project iosApp.xcodeproj \
           -scheme iosApp \
           -configuration Release \
           -destination generic/platform=iOS \
           -archivePath "$ARCHIVE_PATH" \
           CODE_SIGN_IDENTITY="Apple Development" \
           CODE_SIGN_STYLE=Automatic \
           DEVELOPMENT_TEAM=ULPJX5LPF7 \
           archive

# Verify dSYMs
echo -e "${BLUE}🔍 Verifying dSYMs in archive...${NC}"
DSYM_DIR="$ARCHIVE_PATH/dSYMs"

if [ -d "$DSYM_DIR" ]; then
    echo -e "${GREEN}✅ dSYMs found:${NC}"
    ls -la "$DSYM_DIR"
    
    # Check for specific dSYMs
    MAIN_DSYM="$DSYM_DIR/DevFest Nantes.app.dSYM"
    
    if [ -d "$MAIN_DSYM" ]; then
        echo -e "${GREEN}✅ Main app dSYM found${NC}"
        echo -e "   UUID: $(dwarfdump --uuid "$MAIN_DSYM" 2>/dev/null | head -1 || echo 'Could not read UUID')"
    else
        echo -e "${YELLOW}⚠️  Main app dSYM not found${NC}"
    fi
else
    echo -e "${RED}❌ No dSYMs found in archive!${NC}"
    exit 1
fi

# Create exportOptions.plist for development
echo -e "${BLUE}📝 Creating development export options...${NC}"
cat > "$IOS_DIR/exportOptions-development.plist" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>development</string>
    <key>uploadBitcode</key>
    <false/>
    <key>uploadSymbols</key>
    <true/>
    <key>compileBitcode</key>
    <false/>
    <key>teamID</key>
    <string>ULPJX5LPF7</string>
    <key>signingStyle</key>
    <string>automatic</string>
    <key>stripSwiftSymbols</key>
    <true/>
</dict>
</plist>
EOF

# Export for Development
echo -e "${BLUE}📤 Exporting for Development...${NC}"
EXPORT_PATH="$IOS_DIR/DevFestNantes-Development"
rm -rf "$EXPORT_PATH"

xcodebuild -exportArchive \
           -archivePath "$ARCHIVE_PATH" \
           -exportPath "$EXPORT_PATH" \
           -exportOptionsPlist "$IOS_DIR/exportOptions-development.plist"

# Check for IPA file
IPA_PATH="$EXPORT_PATH/DevFest Nantes.ipa"
if [ -f "$IPA_PATH" ]; then
    echo -e "${GREEN}✅ Build complete! Development IPA created.${NC}"
    echo -e "${BLUE}📁 Archive: $ARCHIVE_PATH${NC}"
    echo -e "${BLUE}📱 IPA: $IPA_PATH${NC}"
    echo ""
    echo -e "${YELLOW}💡 This is a DEVELOPMENT build. To upload to App Store:${NC}"
    echo -e "   1. Install an Apple Distribution certificate"
    echo -e "   2. Run the production build script: ./scripts/build-ios-release.sh"
else
    echo -e "${RED}❌ Export failed! IPA not found.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}📋 dSYM Summary:${NC}"
find "$DSYM_DIR" -name "*.dSYM" -type d | while read dsym; do
    dsym_name=$(basename "$dsym")
    echo -e "${GREEN}  ✅ $dsym_name${NC}"
done

echo ""
echo -e "${GREEN}🎉 Development build completed successfully!${NC}"
