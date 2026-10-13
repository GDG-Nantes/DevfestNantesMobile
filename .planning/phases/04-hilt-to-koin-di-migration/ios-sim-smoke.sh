#!/usr/bin/env bash
# Local iOS simulator launch smoke for the Hilt -> Koin migration (Phase 4, D-05 tracer).
#
# Builds the iosApp scheme for the newest plain iPhone simulator, installs and
# launches it, waits 20 s and fails with IOS-SIM-CRASHED unless the app process
# is still alive. Saves a screenshot under ${TMPDIR:-/tmp}. Prints
# IOS-SIM-SMOKE-OK on success.
#
# Usage: bash ios-sim-smoke.sh

set -euo pipefail
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
BUNDLE_ID="com.gdgnantes.devfest.iosApp"
DERIVED="${TMPDIR:-/tmp}/devfest-ios-smoke"
SHOT="${TMPDIR:-/tmp}/devfest-ios-smoke.png"

cd "$REPO_ROOT"

# Same selection as ios.yml's "Resolve latest plain iPhone simulator" step.
DEVICE_NAME=$(xcrun simctl list devices available --json \
  | jq -r '
      .devices
      | to_entries[]
      | select(.key | test("com.apple.CoreSimulator.SimRuntime.iOS"))
      | .value[]
      | select(.isAvailable == true)
      | select(.name | test("^iPhone [0-9]+$"))
      | .name
    ' \
  | sort -t' ' -k2 -n -u \
  | tail -1)

if [ -z "$DEVICE_NAME" ]; then
  echo "IOS-SIM-NO-DEVICE" >&2
  exit 1
fi
echo "Simulator: ${DEVICE_NAME}"

xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -destination "platform=iOS Simulator,name=${DEVICE_NAME}" \
  -derivedDataPath "$DERIVED" \
  build \
  CODE_SIGNING_ALLOWED=NO

# The scheme "iosApp" produces the product "DevFest Nantes.app".
APP="${DERIVED}/Build/Products/Debug-iphonesimulator/DevFest Nantes.app"
if [ ! -d "$APP" ]; then
  echo "IOS-SIM-NO-APP $APP" >&2
  exit 1
fi

# Boot (an already-booted device is fine) and wait until it is usable.
xcrun simctl boot "$DEVICE_NAME" 2>/dev/null || true
xcrun simctl bootstatus "$DEVICE_NAME" -b

xcrun simctl install "$DEVICE_NAME" "$APP"
xcrun simctl terminate "$DEVICE_NAME" "$BUNDLE_ID" 2>/dev/null || true
xcrun simctl launch "$DEVICE_NAME" "$BUNDLE_ID"

sleep 20

if ! xcrun simctl spawn "$DEVICE_NAME" launchctl list \
  | grep -E "^[0-9]+[[:space:]].*UIKitApplication:${BUNDLE_ID}" >/dev/null; then
  echo "IOS-SIM-CRASHED" >&2
  exit 1
fi

xcrun simctl io "$DEVICE_NAME" screenshot "$SHOT"
echo "Screenshot: ${SHOT}"
echo "IOS-SIM-SMOKE-OK"
