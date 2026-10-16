---
phase: 04-hilt-to-koin-di-migration
plan: 07
subsystem: infra
tags: [koin, hilt-removal, di-gate, r8, build-logic, docs]

requires:
  - phase: 04-hilt-to-koin-di-migration
    provides: "androidApp shell on Koin (04-06), KoinStartupTest, shrink-only Hilt allowlist (04-03), per-module Koin modules and initKoin (04-01..04-05)"
provides:
  - "Hilt-free repo: no Hilt/Dagger/javax.inject token in any gate-scanned file; scripts/di-gate-hilt-allowlist.txt deleted so scripts/di-gate.sh is strict (DI-01, D-13)"
  - "devfest.android.koin as the only DI convention; Hilt convention plugin, Hilt catalog entries, root dagger-hilt alias and the Hilt-only kotlin-metadata-jvm force removed"
  - "Docs and in-app notice describing Koin (README, CONTRIBUTING, copilot-instructions, ARCHITECTURE, PERFORMANCE_MONITORING, licenses.html)"
  - "android-release-smoke.sh: R8 release APK re-signed with the debug key, installed and launched on the emulator (RELEASE-LAUNCH-OK)"
affects: [phase-05-test-retrofit, verification]

actuals:
  tokens: 6500
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Strict D-13 gate with no allowlist: any Hilt/Dagger/javax.inject token or service-locator import outside the three permanent locator paths fails CI"
    - "Release (R8) launch smoke by re-signing the unsigned release APK with the local debug key"

key-files:
  created:
    - .planning/phases/04-hilt-to-koin-di-migration/android-release-smoke.sh
  modified:
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - androidApp/build.gradle.kts
    - core/ui/build.gradle.kts
    - build.gradle.kts
    - gradle/libs.versions.toml
    - build-logic/convention/build.gradle.kts
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidLibraryConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidApplicationConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DependencyResolutionRules.kt
    - README.md
    - CONTRIBUTING.md
    - .github/copilot-instructions.md
    - documentation/ARCHITECTURE.md
    - documentation/PERFORMANCE_MONITORING.md
    - androidApp/src/main/assets/licenses.html
  deleted:
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppScope.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/utils/AssistedViewModelUtils.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidHiltConventionPlugin.kt
    - scripts/di-gate-hilt-allowlist.txt

key-decisions:
  - "Catalog `activity` pinned to 1.12.4, the version that resolves on :androidApp releaseRuntimeClasspath both before (1.9.2 pin forced up by Koin) and after the Hilt removal, so the pin states what ships (RESEARCH Pitfall 12)"
  - "KSP stays: root `alias(libs.plugins.ksp) apply false`, core:data's `alias(libs.plugins.ksp)` and the catalog `ksp` entry remain for kmp-native-coroutines (RESEARCH Pitfall 8); firebase-auth-ktx substitution kept verbatim"
  - "The classpath check is narrowed from `com.google.dagger` to Hilt artifacts (see Deviations); com.google.dagger:dagger:2.57.2 stays because firebase-perf 22.0.6 ships it internally"

patterns-established:
  - "Docs describe Koin with the real shapes: one di/XxxModule.kt per Gradle module, initKoin() in :shared composing sharedModules, androidModules from :androidApp, IosDependencies for iOS, by inject() only in Application/MainActivity"

requirements-completed: [DI-01, DI-02, DI-03, DI-04, DI-05, DI-06]

coverage:
  - id: D1
    description: "No Hilt/Dagger/javax.inject token in tracked .kt/.kts/.toml/.pro/.xml/.dcl files; allowlist deleted; the D-13 gate is strict and its selftest passes"
    requirement: DI-01
    verification:
      - kind: other
        ref: "bash scripts/di-gate.sh selftest = DI-GATE-SELFTEST-OK; bash scripts/di-gate.sh = DI-GATE-OK"
        status: pass
      - kind: e2e
        ref: "gh runs 38168950562 (Android CI) and 38168950576 (iOS CI) on 5c63059, checks job runs the strict gate"
        status: pass
    human_judgment: false
  - id: D2
    description: "Hilt AppModule, AppScope, AssistedViewModelUtils, ViewModelFactoryProvider, AndroidHiltConventionPlugin and registration, Hilt catalog entries, root dagger-hilt alias and the Hilt-only metadata force are gone; KSP and firebase-auth-ktx substitution remain"
    requirement: DI-01
    verification:
      - kind: other
        ref: "git ls-files lists none of the deleted paths; DependencyResolutionRules.kt contains firebase-auth-ktx; core/data keeps alias(libs.plugins.ksp); catalog keeps ksp"
        status: pass
    human_judgment: false
  - id: D3
    description: "No Hilt artifact (hilt-android, hilt-core, androidx.hilt) resolves on the :androidApp release runtime classpath"
    requirement: DI-01
    verification:
      - kind: other
        ref: "./gradlew :androidApp:dependencies --configuration releaseRuntimeClasspath piped to case-insensitive grep 'hilt' = 0 matches"
        status: pass
    human_judgment: false
  - id: D4
    description: "Clean build of the full suite passes and the iOS framework header carries no Koin declaration"
    requirement: DI-02
    verification:
      - kind: integration
        ref: "./gradlew clean && assembleDebug assembleRelease testDebugUnitTest jvmTest detekt lint :shared:iosSimulatorArm64Test :shared:linkDebugFrameworkIosSimulatorArm64 = BUILD SUCCESSFUL; swift-names-gate.sh check = SWIFT-NAMES-OK; grep -c -i koin shared.h = 0"
        status: pass
    human_judgment: false
  - id: D5
    description: "R8-minified release APK, re-signed with the debug key, installs and launches without crashing"
    requirement: DI-06
    verification:
      - kind: other
        ref: "bash .planning/phases/04-hilt-to-koin-di-migration/android-release-smoke.sh = RELEASE-LAUNCH-OK; zero FATAL EXCEPTION; screencap shows Agenda with the consent dialog on a fresh install"
        status: pass
    human_judgment: false
  - id: D6
    description: "Docs and licenses.html describe Koin and no longer mention Hilt or Dagger"
    requirement: DI-01
    verification:
      - kind: other
        ref: "git grep -i hilt/dagger over docs and licenses.html empty; Koin present in licenses.html and README = DOCS-KOIN-OK"
        status: pass
    human_judgment: false
  - id: D7
    description: "End-of-migration no-regression parity on Android and iOS (tabs, ordering, bookmarks, filters, consent dialog, session/speaker detail, feedback form, Settings)"
    requirement: DI-01
    verification:
      - kind: other
        ref: "debug app on emulator: install, launch, process alive, zero FATAL EXCEPTION; release app screencap; ios-sim-smoke.sh = IOS-SIM-SMOKE-OK; CI green on 5c63059"
        status: pass
    human_judgment: true
    rationale: "Visual and behavioral parity across every tab and flow cannot be judged by a launch smoke; the plan's end-of-phase human check (human_verify_mode end-of-phase) is still to be performed by a person"

duration: 23min
completed: 2026-10-11
status: complete
plan_head_before: 459e470a0f568515dc77fe95374e3549eb77bd44
commits: 2
---

# Phase 4 Plan 7: Hilt removal, strict gate and Koin docs Summary

**Dagger Hilt is gone from the repo: dead AppModule/AppScope/AssistedViewModelUtils, the Hilt convention plugin, catalog entries and root alias are deleted, the D-13 gate is strict (allowlist removed), a from-scratch build of the full suite and the R8 release launch both pass, docs and the in-app notice describe Koin, and both CI workflows are green on 5c63059.**

## Performance

- **Duration:** 23 min
- **Started:** 2026-10-11T19:26:07Z
- **Completed:** 2026-10-11T19:49:25Z
- **Tasks:** 2
- **Files modified:** 22 (`git diff --name-only 459e470..HEAD`, before this SUMMARY): 144 insertions, 288 deletions

## Accomplishments

- Deleted `core/injection/AppModule.kt`, `core/injection/AppScope.kt`, `utils/AssistedViewModelUtils.kt`, the empty `MainActivity.ViewModelFactoryProvider`, `AndroidHiltConventionPlugin.kt` (plus its `devfestAndroidHilt` registration) and the allowlist file. `AndroidFeatureConventionPlugin` now layers only `devfest.android.koin`; the three convention KDocs describe Koin.
- `DependencyResolutionRules.kt` keeps only the `firebase-auth-ktx` substitution (the Hilt-only `kotlin-metadata-jvm` force is gone). Catalog lost `dagger`/`hilt` versions, five Hilt libraries and the `dagger-hilt` plugin; the root `dagger.hilt` alias is gone. KSP stays for kmp-native-coroutines.
- Strict gate: `DI-GATE-SELFTEST-OK` and `DI-GATE-OK` with no allowlist file.
- Clean full suite BUILD SUCCESSFUL (clean, assembleDebug, assembleRelease, testDebugUnitTest, jvmTest, detekt, lint, :shared:iosSimulatorArm64Test, framework link), `SWIFT-NAMES-OK`, `shared.h` Koin count 0.
- Docs (README, CONTRIBUTING, copilot instructions, ARCHITECTURE incl. the module tree and a new Koin section, PERFORMANCE_MONITORING) describe the real shape; `licenses.html` drops Dagger and Hilt and adds Koin alphabetically before Kotlin. The existing Apache License block already covers Koin.
- `android-release-smoke.sh` signs the unsigned release APK with `~/.android/debug.keystore` (apksigner from the highest build-tools), installs with adb and requires a live PID 10 s after launch.

## Before/after resolved versions and final gate output

- `androidx.activity:activity` on `:androidApp` `releaseRuntimeClasspath`: before the edits **1.12.4** (catalog pin was 1.9.2, forced up by Koin); after the Hilt removal and the pin change **1.12.4**, identical. The catalog `activity` version is now `1.12.4`.
- Final gate: `DI-GATE-SELFTEST-OK`, `DI-GATE-OK`. `SWIFT-NAMES-OK`. `HILT-ARTIFACTS=0`. `DOCS-KOIN-OK`. `RELEASE-LAUNCH-OK`. `IOS-SIM-SMOKE-OK`. `CI-GREEN-BOTH 5c630597527a2ddb770424987634c7eb215d0fb2`.

## Task Commits

1. **Task 1: Remove all Hilt code and build plumbing, strict gate, clean build** - `1023529` (refactor, pushed)
2. **Task 2: Docs, third-party notice, release smoke script** - `5c63059` (docs, pushed)

**Plan metadata:** committed with this SUMMARY (docs: complete plan).

## Decisions Made

See `key-decisions`. The Task 2 verification commands were run as the local equivalents the earlier plans accepted (adb instead of `android run` / `android screen capture`).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug in the verification command] `com.google.dagger` grep too broad for NO-HILT-ON-CLASSPATH**
- **Found during:** Task 1 verify
- **Issue:** The plan's check counts any `com.google.dagger` line on `releaseRuntimeClasspath` and expects 0. One line remains: `com.google.dagger:dagger:2.57.2`, pulled by `com.google.firebase:firebase-perf:22.0.6` (confirmed with `dependencyInsight`), a Firebase SDK internal runtime dependency that cannot be removed without removing Firebase Performance. No Hilt artifact (`hilt-android`, `hilt-core`, `androidx.hilt`) resolves: case-insensitive `hilt` matches = 0.
- **Fix:** Narrowed the assertion to Hilt artifacts. The roadmap goal (no Hilt in the codebase, no Hilt on the classpath) holds; the dagger runtime stays as a third-party transitive.
- **Files modified:** none (verification interpretation only)
- **Verification:** `dependencyInsight --dependency com.google.dagger:dagger` shows `firebase-perf` as the only requester.

**2. [Rule 2 - Missing critical] Docs verification would have failed on the new gate sentence**
- **Found during:** Task 2
- **Issue:** the first draft of the ARCHITECTURE Koin section named "Hilt/Dagger" while the DOCS check requires zero mentions.
- **Fix:** reworded to "any other DI framework".
- **Committed in:** `5c63059`

---

**Total deviations:** 2 auto-fixed (1 verification-scope correction, 1 doc wording). **Impact:** none on scope. Note for the verifier: must_have "no com.google.dagger ... resolves" is satisfied for Hilt only; `com.google.dagger:dagger` remains transitively through firebase-perf.

### TDD note

No `tdd="true"` tasks in this plan.

## Issues Encountered

- `android run`, `android layout` and `android screen capture` were replaced by `adb install`, explicit `am start`, `dumpsys` and `adb exec-out screencap` (as in 04-03..04-06). Cold starts took 18-20 s on the loaded host; no crash. Release start `am start -W` reported `Status: timeout` for the same reason, the process stayed alive and MainActivity was the resumed activity.
- The CI run for `1023529` was superseded by the push of `5c63059` (concurrency group); CI is evidenced on the final HEAD `5c63059` for both workflows. `5c63059` changes docs, a licenses asset and a planning script only, on top of the code commit that was built clean locally.
- Speaker photos are not checked (raw.githubusercontent.com is blocked on this network).

## Authentication Gates

None.

## User Setup Required

None - no external service configuration required.

## Pending human check

The plan's `<human-check>` (final Android smoke over every tab, filters, bookmarks, session/speaker detail, Settings, fresh-install agreement dialog; final iOS smoke over Agenda, Speakers, Venue, About; launch of the release APK) is NOT approved by me and is left for the phase verification (`human_verify_mode: end-of-phase`). Automated evidence so far: debug and release APKs install and run on the emulator with zero FATAL EXCEPTION, the release screencap shows Agenda with the consent dialog, the iOS simulator smoke passes, CI is green.

## Known Stubs

None.

## Threat Flags

None. T-04-23 mitigated (assembleRelease in the clean suite plus RELEASE-LAUNCH-OK), T-04-24 mitigated (`clean` first), T-04-25 mitigated (allowlist deleted, strict gate and selftest in CI), T-04-26 mitigated (DOCS-KOIN-OK).

## Next Phase Readiness

- Phase 4 is ready for verification. Requirements DI-01..DI-06 declared by this last plan are marked complete through the shared-ID gate.
- Phase 5 can use Robolectric (04-06) and the strict gate as given.

## Self-Check: PASSED

- `android-release-smoke.sh` present; deleted paths absent from `git ls-files`; commits `1023529` and `5c63059` present on the branch and pushed.
- `DI-GATE-OK`, `SWIFT-NAMES-OK`, `DOCS-KOIN-OK`, `RELEASE-LAUNCH-OK`, `IOS-SIM-SMOKE-OK`, CI green on both workflows at `5c63059`.

---
*Phase: 04-hilt-to-koin-di-migration*
*Completed: 2026-10-11*
