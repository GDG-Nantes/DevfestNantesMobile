#!/bin/bash

set -e

echo "🏗️  Building DevFest Nantes iOS for App Store..."

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

# Build shared framework first
echo -e "${BLUE}🔧 Building shared Kotlin framework...${NC}"
cd "$PROJECT_ROOT"
./gradlew :shared:embedAndSignAppleFrameworkForXcode
cd "$IOS_DIR"

# Verify build settings
echo -e "${BLUE}🔍 Verifying build settings...${NC}"
DEBUG_FORMAT=$(xcodebuild -project iosApp.xcodeproj -scheme iosApp -configuration Release -destination generic/platform=iOS -showBuildSettings | grep DEBUG_INFORMATION_FORMAT | awk '{print $3}')

if [ "$DEBUG_FORMAT" != "dwarf-with-dsym" ]; then
    echo -e "${RED}❌ ERROR: DEBUG_INFORMATION_FORMAT is not set to 'dwarf-with-dsym'. Current value: $DEBUG_FORMAT${NC}"
    exit 1
else
    echo -e "${GREEN}✅ DEBUG_INFORMATION_FORMAT correctly set to 'dwarf-with-dsym'${NC}"
fi

# Archive
echo -e "${BLUE}📦 Creating archive...${NC}"
ARCHIVE_PATH="$IOS_DIR/DevFestNantes.xcarchive"
rm -rf "$ARCHIVE_PATH"

xcodebuild -project iosApp.xcodeproj \
           -scheme iosApp \
           -configuration Release \
           -destination generic/platform=iOS \
           -archivePath "$ARCHIVE_PATH" \
           archive

# Verify dSYMs
echo -e "${BLUE}🔍 Verifying dSYMs in archive...${NC}"
DSYM_DIR="$ARCHIVE_PATH/dSYMs"

if [ -d "$DSYM_DIR" ]; then
    echo -e "${GREEN}✅ dSYMs found:${NC}"
    ls -la "$DSYM_DIR"
    
    # Check for specific dSYMs that were causing issues
    MAIN_DSYM="$DSYM_DIR/DevFest Nantes.app.dSYM"
    FIREBASE_ANALYTICS_DSYM="$DSYM_DIR/FirebaseAnalytics.framework.dSYM"
    GOOGLE_APP_MEASUREMENT_DSYM="$DSYM_DIR/GoogleAppMeasurement.framework.dSYM"
    
    if [ -d "$MAIN_DSYM" ]; then
        echo -e "${GREEN}✅ Main app dSYM found${NC}"
        echo -e "   UUID: $(dwarfdump --uuid "$MAIN_DSYM" 2>/dev/null | head -1 || echo 'Could not read UUID')"
    else
        echo -e "${YELLOW}⚠️  Main app dSYM not found${NC}"
    fi
    
    if [ -d "$FIREBASE_ANALYTICS_DSYM" ]; then
        echo -e "${GREEN}✅ FirebaseAnalytics dSYM found${NC}"
        echo -e "   UUID: $(dwarfdump --uuid "$FIREBASE_ANALYTICS_DSYM" 2>/dev/null | head -1 || echo 'Could not read UUID')"
    else
        echo -e "${YELLOW}⚠️  FirebaseAnalytics dSYM not found (may be expected if using SPM)${NC}"
    fi
    
    if [ -d "$GOOGLE_APP_MEASUREMENT_DSYM" ]; then
        echo -e "${GREEN}✅ GoogleAppMeasurement dSYM found${NC}"
        echo -e "   UUID: $(dwarfdump --uuid "$GOOGLE_APP_MEASUREMENT_DSYM" 2>/dev/null | head -1 || echo 'Could not read UUID')"
    else
        echo -e "${YELLOW}⚠️  GoogleAppMeasurement dSYM not found (may be expected if using SPM)${NC}"
    fi
    
else
    echo -e "${RED}❌ No dSYMs found in archive!${NC}"
    echo -e "${RED}This will cause App Store Connect upload to fail.${NC}"
    exit 1
fi

# Export for App Store
echo -e "${BLUE}📤 Exporting for App Store...${NC}"
EXPORT_PATH="$IOS_DIR/DevFestNantes-AppStore"
rm -rf "$EXPORT_PATH"

if [ ! -f "$IOS_DIR/exportOptions.plist" ]; then
    echo -e "${RED}❌ exportOptions.plist not found. Creating default...${NC}"
    cat > "$IOS_DIR/exportOptions.plist" << EOF
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
    <key>teamID</key>
    <string>ULPJX5LPF7</string>
    <key>destination</key>
    <string>upload</string>
    <key>stripSwiftSymbols</key>
    <true/>
</dict>
</plist>
EOF
fi

xcodebuild -exportArchive \
           -archivePath "$ARCHIVE_PATH" \
           -exportPath "$EXPORT_PATH" \
           -exportOptionsPlist "$IOS_DIR/exportOptions.plist"

# Final verification
IPA_PATH="$EXPORT_PATH/DevFest Nantes.ipa"
if [ -f "$IPA_PATH" ]; then
    echo -e "${GREEN}✅ Build complete! Ready for App Store upload.${NC}"
    echo -e "${BLUE}📁 Archive: $ARCHIVE_PATH${NC}"
    echo -e "${BLUE}📱 IPA: $IPA_PATH${NC}"
    echo ""
    echo -e "${YELLOW}📤 To upload to App Store Connect:${NC}"
    echo -e "   1. Open Xcode and use the Organizer (Window > Organizer)"
    echo -e "   2. Select the archive and click 'Distribute App'"
    echo -e "   3. Or use command line:"
    echo -e "      xcrun altool --upload-app --type ios --file \"$IPA_PATH\" --username YOUR_APPLE_ID --password APP_SPECIFIC_PASSWORD"
else
    echo -e "${RED}❌ Export failed! IPA not found.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 Build process completed successfully!${NC}"
echo -e "${BLUE}The app should now upload to App Store Connect without dSYM issues.${NC}"
