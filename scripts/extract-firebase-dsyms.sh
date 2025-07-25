#!/bin/bash

# Firebase dSYM Extraction Script
# This script extracts dSYMs from Firebase frameworks for App Store Connect uploads

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔧 Firebase dSYM Extraction Tool${NC}"
echo "This script helps extract dSYMs from Firebase frameworks when they're missing from the archive."
echo ""

# Check if archive path is provided
if [ $# -eq 0 ]; then
    echo -e "${YELLOW}Usage: $0 <path-to-xcarchive>${NC}"
    echo "Example: $0 ./DevFestNantes.xcarchive"
    exit 1
fi

ARCHIVE_PATH="$1"

if [ ! -d "$ARCHIVE_PATH" ]; then
    echo -e "${RED}❌ Archive not found: $ARCHIVE_PATH${NC}"
    exit 1
fi

echo -e "${BLUE}📦 Processing archive: $ARCHIVE_PATH${NC}"

# Paths
DSYM_DIR="$ARCHIVE_PATH/dSYMs"
APP_PATH="$ARCHIVE_PATH/Products/Applications"
APP_NAME=$(find "$APP_PATH" -name "*.app" -type d | head -1 | xargs basename 2>/dev/null || echo "")

if [ -z "$APP_NAME" ]; then
    echo -e "${RED}❌ Could not find app in archive${NC}"
    exit 1
fi

FULL_APP_PATH="$APP_PATH/$APP_NAME"
FRAMEWORKS_PATH="$FULL_APP_PATH/Frameworks"

echo -e "${BLUE}📱 Found app: $APP_NAME${NC}"
echo -e "${BLUE}📂 App path: $FULL_APP_PATH${NC}"
echo -e "${BLUE}🔧 Frameworks path: $FRAMEWORKS_PATH${NC}"

# Create dSYMs directory if it doesn't exist
mkdir -p "$DSYM_DIR"

# Function to extract dSYM from framework
extract_dsym() {
    local framework_path="$1"
    local framework_name="$2"
    
    echo ""
    echo -e "${BLUE}🔍 Processing: $framework_name${NC}"
    
    local framework_binary="$framework_path/$framework_name"
    local output_dsym="$DSYM_DIR/${framework_name}.framework.dSYM"
    
    # Check if dSYM already exists
    if [ -d "$output_dsym" ]; then
        echo -e "${GREEN}✅ dSYM already exists: $output_dsym${NC}"
        # Verify UUID
        local uuid=$(dwarfdump --uuid "$output_dsym" 2>/dev/null | head -1 || echo "Could not read UUID")
        echo -e "   UUID: $uuid"
        return 0
    fi
    
    # Check if framework binary exists
    if [ ! -f "$framework_binary" ]; then
        echo -e "${RED}❌ Framework binary not found: $framework_binary${NC}"
        return 1
    fi
    
    echo -e "${BLUE}📋 Framework info:${NC}"
    file "$framework_binary" || echo "Could not determine file type"
    
    # Check if binary has debug symbols
    if ! nm "$framework_binary" >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Binary appears to be stripped or does not contain debug symbols${NC}"
    fi
    
    # Try to extract dSYM using dsymutil
    echo -e "${BLUE}🛠️  Extracting dSYM...${NC}"
    
    if dsymutil "$framework_binary" -o "$output_dsym" 2>/dev/null; then
        echo -e "${GREEN}✅ Successfully extracted dSYM: $output_dsym${NC}"
        
        # Verify the extracted dSYM
        if [ -d "$output_dsym" ]; then
            local uuid=$(dwarfdump --uuid "$output_dsym" 2>/dev/null | head -1 || echo "Could not read UUID")
            echo -e "${GREEN}   UUID: $uuid${NC}"
            
            # Check dSYM size
            local dsym_size=$(du -sh "$output_dsym" | cut -f1)
            echo -e "${GREEN}   Size: $dsym_size${NC}"
        fi
        
        return 0
    else
        echo -e "${RED}❌ Failed to extract dSYM for $framework_name${NC}"
        echo -e "${YELLOW}   This framework may not contain debug symbols${NC}"
        return 1
    fi
}

# List of Firebase frameworks that commonly cause dSYM issues
FIREBASE_FRAMEWORKS=(
    "FirebaseAnalytics"
    "GoogleAppMeasurement"
    "FirebaseCrashlytics"
    "FirebaseCore"
    "FirebaseInstallations"
    "FirebaseRemoteConfig"
    "GoogleUtilities"
    "GoogleDataTransport"
    "nanopb"
)

echo ""
echo -e "${BLUE}🔍 Checking for Firebase frameworks...${NC}"

if [ ! -d "$FRAMEWORKS_PATH" ]; then
    echo -e "${RED}❌ Frameworks directory not found: $FRAMEWORKS_PATH${NC}"
    exit 1
fi

extracted_count=0
failed_count=0

# Process each Firebase framework
for framework_name in "${FIREBASE_FRAMEWORKS[@]}"; do
    framework_path="$FRAMEWORKS_PATH/${framework_name}.framework"
    
    if [ -d "$framework_path" ]; then
        if extract_dsym "$framework_path" "$framework_name"; then
            ((extracted_count++))
        else
            ((failed_count++))
        fi
    else
        echo -e "${YELLOW}⚠️  Framework not found: $framework_name (not included in this build)${NC}"
    fi
done

# Also check for any other frameworks that might be Firebase-related
echo ""
echo -e "${BLUE}🔍 Checking for additional frameworks...${NC}"

find "$FRAMEWORKS_PATH" -name "*.framework" -type d | while read -r framework_path; do
    framework_name=$(basename "$framework_path" .framework)
    
    # Skip if already processed
    if [[ " ${FIREBASE_FRAMEWORKS[@]} " =~ " ${framework_name} " ]]; then
        continue
    fi
    
    # Process if it looks like a Firebase/Google framework
    if [[ "$framework_name" == *Firebase* ]] || [[ "$framework_name" == *Google* ]]; then
        echo -e "${BLUE}🔍 Found additional framework: $framework_name${NC}"
        if extract_dsym "$framework_path" "$framework_name"; then
            ((extracted_count++))
        else
            ((failed_count++))
        fi
    fi
done

# Summary
echo ""
echo -e "${BLUE}📋 Extraction Summary:${NC}"
echo -e "${GREEN}✅ Successfully extracted: $extracted_count dSYMs${NC}"

if [ $failed_count -gt 0 ]; then
    echo -e "${YELLOW}⚠️  Failed to extract: $failed_count dSYMs${NC}"
fi

echo ""
echo -e "${BLUE}📂 Final dSYM directory contents:${NC}"
ls -la "$DSYM_DIR"

echo ""
echo -e "${BLUE}🔍 dSYM verification:${NC}"
find "$DSYM_DIR" -name "*.dSYM" -type d | while read dsym; do
    dsym_name=$(basename "$dsym")
    uuid=$(dwarfdump --uuid "$dsym" 2>/dev/null | head -1 || echo "Could not read UUID")
    echo -e "${GREEN}  ✅ $dsym_name${NC}"
    echo -e "     UUID: $uuid"
done

echo ""
if [ $extracted_count -gt 0 ]; then
    echo -e "${GREEN}🎉 dSYM extraction completed! The archive should now be ready for App Store Connect upload.${NC}"
else
    echo -e "${YELLOW}⚠️  No dSYMs were extracted. This might be normal if:${NC}"
    echo -e "   - Firebase frameworks don't contain debug symbols (release builds)"
    echo -e "   - Frameworks are already stripped"
    echo -e "   - Using a different Firebase distribution method"
fi

echo ""
echo -e "${BLUE}💡 Tips:${NC}"
echo -e "   - If upload still fails, the frameworks might not contain debug symbols"
echo -e "   - Consider using CocoaPods instead of SPM for Firebase if issues persist"
echo -e "   - Check Firebase documentation for the latest dSYM handling recommendations"
