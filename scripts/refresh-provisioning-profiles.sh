#!/bin/bash

set -e

echo "🔄 Refreshing iOS Provisioning Profiles..."

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

echo -e "${BLUE}📋 Current signing certificates:${NC}"
security find-identity -v -p codesigning

echo ""
echo -e "${BLUE}📁 Current provisioning profiles:${NC}"
PROFILE_COUNT=$(ls -1 ~/Library/MobileDevice/Provisioning\ Profiles/*.mobileprovision 2>/dev/null | wc -l)
echo "Found $PROFILE_COUNT provisioning profiles"

echo ""
echo -e "${YELLOW}🗑️  Would you like to delete existing provisioning profiles? (recommended)${NC}"
echo -e "${YELLOW}This will force Xcode to download fresh profiles that include your new certificate.${NC}"
read -p "Delete profiles? (y/n): " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${BLUE}Deleting old provisioning profiles...${NC}"
    rm -f ~/Library/MobileDevice/Provisioning\ Profiles/*.mobileprovision
    echo -e "${GREEN}✅ Old profiles deleted${NC}"
else
    echo -e "${YELLOW}⚠️  Skipping profile deletion${NC}"
fi

echo ""
echo -e "${BLUE}🔧 Opening Xcode to trigger provisioning profile refresh...${NC}"
echo -e "${YELLOW}In Xcode, please:${NC}"
echo -e "  1. Select the 'iosApp' target"
echo -e "  2. Go to 'Signing & Capabilities' tab"
echo -e "  3. Ensure 'Automatically manage signing' is checked"
echo -e "  4. Verify team is set to 'GDG Nantes (ULPJX5LPF7)'"
echo -e "  5. Wait for Xcode to download new provisioning profiles"
echo -e "  6. Check that both Debug and Release configurations show no errors"
echo ""

# Open Xcode
cd "$IOS_DIR"
open iosApp.xcodeproj

echo ""
echo -e "${BLUE}⏳ Waiting for Xcode to update profiles (this may take a moment)...${NC}"
echo -e "${YELLOW}Press Enter when Xcode has finished updating provisioning profiles...${NC}"
read

# Check if new profiles were downloaded
NEW_PROFILE_COUNT=$(ls -1 ~/Library/MobileDevice/Provisioning\ Profiles/*.mobileprovision 2>/dev/null | wc -l)
echo ""
echo -e "${BLUE}📁 Updated provisioning profile count: $NEW_PROFILE_COUNT${NC}"

if [ "$NEW_PROFILE_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✅ Provisioning profiles have been refreshed!${NC}"
    echo ""
    echo -e "${BLUE}🔍 Profiles for com.gdgnantes.devfest.iosApp:${NC}"
    find ~/Library/MobileDevice/Provisioning\ Profiles -name "*.mobileprovision" -exec sh -c '
        PROFILE="$1"
        BUNDLE_ID=$(security cms -D -i "$PROFILE" 2>/dev/null | grep -A1 "application-identifier" | tail -1 | sed "s/.*<string>//;s/<\/string>//;s/.*\.//" | tr -d " \t\n")
        if [[ "$BUNDLE_ID" == *"devfest"* ]]; then
            NAME=$(security cms -D -i "$PROFILE" 2>/dev/null | grep -A1 "<key>Name</key>" | tail -1 | sed "s/.*<string>//;s/<\/string>//" | tr -d "\t")
            echo "  ✅ $NAME"
        fi
    ' _ {} \;
    
    echo ""
    echo -e "${GREEN}🎉 Provisioning profiles are ready!${NC}"
    echo -e "${BLUE}You can now run: ./scripts/build-ios-release.sh${NC}"
else
    echo -e "${YELLOW}⚠️  No new provisioning profiles found.${NC}"
    echo -e "${YELLOW}Please check Xcode for any signing errors.${NC}"
fi

echo ""
echo -e "${BLUE}💡 Tips:${NC}"
echo -e "  • If you still see errors, try building once in Xcode (⌘+B)"
echo -e "  • Make sure you're logged in to your Apple ID in Xcode preferences"
echo -e "  • Verify your Apple ID has access to team ULPJX5LPF7"
