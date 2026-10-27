# Scripts Directory

This directory contains build and utility scripts for the DevFest Nantes iOS app.

## Available Scripts

### 🚀 `build-ios-release.sh`
**Production build script** - Builds and uploads the iOS app to App Store Connect.

```bash
./scripts/build-ios-release.sh
```

**What it does:**
- Cleans previous build artifacts
- Builds the shared Kotlin framework
- Increments build version automatically
- Creates an archive with dSYM files
- Uploads directly to App Store Connect
- Uses automatic code signing

**Requirements:**
- Apple Distribution certificate installed
- Xcode command-line tools
- Logged into App Store Connect via Xcode
- Internet connection (for provisioning profile updates)

**Exit codes:**
- `0` = Success (app uploaded)
- `1` = Failure

---

### 🧪 `build-ios-development.sh`
**Development build script** - Creates a development IPA for local testing.

```bash
./scripts/build-ios-development.sh
```

**What it does:**
- Same as release build, but uses development signing
- Creates a local IPA file (does not upload)
- Works with Apple Development certificate only

**Use for:**
- Local testing
- Internal distribution
- Development debugging

**Does NOT:**
- Upload to App Store Connect
- Require distribution certificate

---

### 🔧 `refresh-provisioning-profiles.sh`
**Provisioning profile refresh utility** - Helps resolve provisioning profile issues.

```bash
./scripts/refresh-provisioning-profiles.sh
```

**What it does:**
- Shows current certificates
- Optionally deletes old provisioning profiles
- Opens Xcode to trigger profile refresh
- Guides you through the process
- Verifies new profiles are downloaded

**Use when:**
- Getting provisioning profile errors
- After adding new certificates
- Profiles are out of sync

---

### 📦 `extract-firebase-dsyms.sh`
**dSYM extraction utility** - Manually extracts Firebase framework dSYMs.

```bash
./scripts/extract-firebase-dsyms.sh
```

**What it does:**
- Extracts dSYM files from Firebase frameworks
- Useful for debugging symbolication issues
- Typically not needed (handled automatically by build scripts)

---

### `xcresult-assert-tests.sh`
**iOS test-result assertion** - Fails when an `.xcresult` bundle executed no test.

```bash
bash scripts/xcresult-assert-tests.sh <path-to.xcresult>
bash scripts/xcresult-assert-tests.sh selftest
```

**What it does:**
- Reads the test counts of an `.xcresult` bundle (`xcrun xcresulttool get test-results summary`)
- Fails when zero tests executed, when every test was skipped, when any test failed, or when the
  bundle's overall result is present and not `Passed` (a run that died partway)
  (`xcodebuild test-without-building` exits 0 on a zero-test run, so its exit code is not enough)
- Prints `XCRESULT-TESTS total=.. passed=.. failed=.. skipped=..`, then `XCRESULT-OK` or a failure token
- `selftest` mode proves the decision logic on fixture summaries and prints `XCRESULT-SELFTEST-OK`
- Used by `.github/workflows/ios.yml`; asserts no fixed test count. It reads only the bundle: no
  environment variable can substitute the summary

**Requirements:**
- Xcode command-line tools (`xcrun xcresulttool`) and `jq`

**Exit codes:**
- `0` = at least one test ran and every executed test passed
- `1` = no test executed, a test failed, or the bundle is missing or unreadable

---

### `swift-names-gate.sh`
**Swift-visible-name gate** - Guards the Swift names of the Kotlin types exported by `shared.framework`.

```bash
bash scripts/swift-names-gate.sh check      # compare the current shared.h with the baselines
bash scripts/swift-names-gate.sh selftest   # prove check catches an identity swap and a new collision
bash scripts/swift-names-gate.sh baseline   # regenerate the baselines (see the rule below)
```

**What it does:**
- Derives the Swift names (`swift_name(...)`) from the `shared.framework` header (`shared.h`)
- `check` fails with `SWIFT-NAME-MISSING <name>` when a name referenced from `iosApp` Swift sources is
  gone or renamed, `SWIFT-MEMBERS-CHANGED <Type>` when a referenced type's member set differs
  (identity swap behind an unchanged name), `SWIFT-TYPE-COLLISION <name>` for a new `_`-suffixed
  collision, and `SWIFT-NAME-COLLISION baseline=.. current=..` when the collision count changes;
  prints `SWIFT-NAMES-OK` on success
- `selftest` builds doctored header copies and prints `SWIFT-GATE-SELFTEST-OK` when both are caught
- A missing or empty header fails (never `SWIFT-NAMES-OK`); names are compared byte-wise (`LC_ALL=C`)
- Baselines live in `scripts/swift-names/` (`shared-h-baseline.txt`, `referenced-names.txt`,
  `members-baseline.txt`, `names-exclude.txt`)
- `SWIFT_GATE_HEADER=<path to shared.h>` reuses an existing header; without it the script links
  `:shared:linkDebugFrameworkIosSimulatorArm64` itself
- CI (`.github/workflows/ios.yml`) runs `check` then `selftest` right after the shared framework build

**Re-baseline rule:** run `baseline` only in the same commit that updates the Swift call sites of a
renamed or newly referenced Kotlin export, and give the reason in the commit message. Never run it
to silence a failing `check`; CI never runs it.

**Requirements:**
- macOS (`selftest` uses BSD `sed -i ''`), `git`, and a built `shared.framework` header or Gradle

**Exit codes:**
- `0` = `check` / `selftest` passed (or `baseline` written)
- `1` = a name is missing or changed, the header is missing or empty, or `selftest` failed

---

### `kmp-nc-version-gate.sh`
**KMP-NativeCoroutines version-skew gate** - Fails when the Gradle plugin and the Swift package are not on the same version.

```bash
bash scripts/kmp-nc-version-gate.sh            # check (default)
bash scripts/kmp-nc-version-gate.sh selftest   # prove each decision on fixture files
```

**What it does:**
- Compares `kmpNativeCoroutines` in `gradle/libs.versions.toml` (source of truth) with the
  KMP-NativeCoroutines Swift package requirement in `iosApp/iosApp.xcodeproj/project.pbxproj`
  (the `XCRemoteSwiftPackageReference` whose `repositoryURL` contains `KMP-NativeCoroutines`; other
  packages are never read), as exact strings: `v1.0.6` against `1.0.6` is a skew
- Prints `KMPNC-VERSION-OK <version>` on success; otherwise one token per failure:
  `KMPNC-VERSION-SKEW gradle=.. spm=..`, `KMPNC-SPM-NOT-EXACT kind=.. (expected exactVersion)` (a range,
  branch or revision requirement), `KMPNC-GRADLE-VERSION-MISSING <path>`, `KMPNC-SPM-PIN-MISSING <path>`
- `selftest` runs the check on fixture files under a temp dir and prints `KMPNC-SELFTEST-OK`
  (or `KMPNC-SELFTEST-FAILED <case>`)
- `KMP_NC_TOML` and `KMP_NC_PBXPROJ` override the two inputs; fixture-only, used by `selftest`
  (CI sets neither)
- Pure text check: runs anywhere, no Xcode or Gradle. It is the first step of `.github/workflows/ios.yml`
- Dependabot only bumps the Gradle side: its pull request fails this gate until the SPM pin is edited by
  hand in the same commit (see
  [Bumping KMP-NativeCoroutines](../documentation/DEVELOPMENT.md#bumping-kmp-nativecoroutines))

**Exit codes:**
- `0` = versions equal and the SPM requirement is an exact version
- `1` = skew, non-exact requirement, a source that cannot be read, or `selftest` failed

---
