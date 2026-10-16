#!/usr/bin/env bash
# Local release (R8-minified) launch smoke for the Hilt -> Koin migration (Phase 4, 04-07).
#
# Builds :androidApp:assembleRelease, re-signs the unsigned APK with the local debug key,
# installs it on the running emulator/device and launches MainActivity. Fails unless the app
# process is still alive 10 s after launch. Prints RELEASE-LAUNCH-OK on success, or
# RELEASE-SIGNATURE-CONFLICT (exit 1) if a differently signed build is already installed.
# It never uninstalls anything.
#
# adb is used for install/launch because the `android` CLI installs project variants only.
#
# Usage: bash android-release-smoke.sh

set -euo pipefail
export LC_ALL=C

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
PACKAGE="com.gdgnantes.devfest.mobile.androidapp"
ACTIVITY="${PACKAGE}/com.gdgnantes.devfest.androidapp.MainActivity"
UNSIGNED_APK="androidApp/build/outputs/apk/release/androidApp-release-unsigned.apk"
SIGNED_APK="${TMPDIR:-/tmp}/devfest-release-debugsigned.apk"
KEYSTORE="${HOME}/.android/debug.keystore"

cd "$REPO_ROOT"

# Locate the SDK: environment first, then local.properties, then the default location.
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
if [ -z "$SDK" ] && [ -f local.properties ]; then
  SDK="$(sed -n 's/^sdk\.dir=//p' local.properties | head -1)"
fi
if [ -z "$SDK" ] && [ -d "${HOME}/Library/Android/sdk" ]; then
  SDK="${HOME}/Library/Android/sdk"
fi
if [ -z "$SDK" ] || [ ! -d "${SDK}/build-tools" ]; then
  echo "RELEASE-NO-SDK" >&2
  exit 1
fi

# Highest installed build-tools that ships apksigner.
BUILD_TOOLS="$(ls -1 "${SDK}/build-tools" | sort -t. -k1,1n -k2,2n -k3,3n | while read -r v; do
  [ -x "${SDK}/build-tools/${v}/apksigner" ] && echo "$v"
done | tail -1)"
if [ -z "$BUILD_TOOLS" ]; then
  echo "RELEASE-NO-APKSIGNER" >&2
  exit 1
fi
APKSIGNER="${SDK}/build-tools/${BUILD_TOOLS}/apksigner"
echo "apksigner: ${APKSIGNER}"

./gradlew :androidApp:assembleRelease

if [ ! -f "$UNSIGNED_APK" ]; then
  echo "RELEASE-NO-APK ${UNSIGNED_APK}" >&2
  exit 1
fi

"$APKSIGNER" sign \
  --ks "$KEYSTORE" \
  --ks-key-alias androiddebugkey \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out "$SIGNED_APK" \
  "$UNSIGNED_APK"

INSTALL_OUT="$(adb install -r "$SIGNED_APK" 2>&1 || true)"
echo "$INSTALL_OUT"
if printf '%s' "$INSTALL_OUT" | grep -q 'INSTALL_FAILED_UPDATE_INCOMPATIBLE'; then
  echo "RELEASE-SIGNATURE-CONFLICT" >&2
  exit 1
fi
if ! printf '%s' "$INSTALL_OUT" | grep -q 'Success'; then
  echo "RELEASE-INSTALL-FAILED" >&2
  exit 1
fi

adb shell am force-stop "$PACKAGE" || true
adb shell am start -W -n "$ACTIVITY"

sleep 10

PID="$(adb shell pidof "$PACKAGE" | tr -d '\r' || true)"
if [ -z "$PID" ]; then
  echo "RELEASE-LAUNCH-CRASHED" >&2
  exit 1
fi
echo "Release process alive: pid ${PID}"
echo "RELEASE-LAUNCH-OK"
