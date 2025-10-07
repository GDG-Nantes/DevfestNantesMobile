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
