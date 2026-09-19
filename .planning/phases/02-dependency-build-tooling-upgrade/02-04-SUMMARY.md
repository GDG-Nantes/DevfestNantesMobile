---
phase: 02-dependency-build-tooling-upgrade
plan: 04
subsystem: build
tags: [firebase, coroutines, kotlinx-serialization, kotlinx-datetime, proguard, r8]

# Dependency graph
requires:
  - phase: 02-dependency-build-tooling-upgrade
    plan: 03
    provides: Compose BOM 2026.09.00, Apollo 5.2.0 with com.apollographql.cache normalized cache
provides:
  - Firebase BOM bumped to 34.19.0, kotlinx-coroutines to 1.11.0, kotlinx-serialization-json to 1.11.0, kotlinx-datetime to 0.8.0 (BUILD-06)
  - A value-level commonTest regression signal (ScheduleSlotDateParsingTest) for ScheduleSlot/Agenda date-parsing and day-bucketing semantics, proven green both pre- and post-bump
  - Resolved RESEARCH.md assumption A2 live: kotlinx.datetime.Instant's typealias to kotlin.time.Instant survived into 0.8.0 (deprecated, not removed) — ScheduleSlot.kt/Agenda.kt needed zero code changes
  - Firebase's July-2025 KTX-module removal from the BOM handled: the four firebase-*-ktx catalog aliases repointed at their merged plain artifacts, two production files migrated off .ktx-package imports, and a transitive firebase-auth-ktx (via openfeedback) redirected via Gradle dependency substitution
  - R8 full-mode release build unblocked with three -dontwarn rules for openfeedback's stale kotlinx-datetime 0.6.x class references (openfeedback is feature-flagged off at runtime)
affects: [02-05-dcl-pilot]

# Actuals (#2632)
actuals:
  tokens: 3332
  tasks: 2
  commits: 2

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Firebase BOM 34.x dropped the -ktx artifact constraints entirely (KTX extensions were merged into main modules in Oct 2023, module publishing stopped Jul 2025). Fix: change the catalog libraries' `name` field to drop the -ktx suffix while keeping the alias key (`firebase-analytics-ktx` etc.) unchanged, so `libs.firebase.analytics.ktx` call sites keep working; then update source imports from `com.google.firebase.*.ktx.*` to the same symbol under `com.google.firebase.*` (no `.ktx` package segment) — see kb://firebase/docs/android/kotlin-migration"
    - "A transitive dependency that still requests a deprecated, no-longer-BOM-managed -ktx coordinate (here: openfeedback-viewmodel -> dev.gitlive:firebase-auth -> firebase-auth-ktx) needs a `resolutionStrategy.dependencySubstitution { substitute(module(\"...-ktx\")).using(module(\"...:<version>\")) }` redirect to the plain artifact — Gradle's substitution API requires an explicit target version even though the BOM's platform() constraint ultimately governs the resolved version"
    - "kotlinx.datetime 0.8.0's typealias coverage is inconsistent by symbol: `Instant` still resolves and behaves correctly through the deprecated `kotlinx.datetime.Instant` typealias, but `Clock.System` does not resolve through `kotlinx.datetime.Clock` — any call site using `Clock.System` (not just `Instant`) needs its import swapped to `kotlin.time.Clock` directly. Files outside a plan's named scope that share the same kotlinx-datetime bump surface must still be checked (this project's UI-layer `androidApp/.../ui/screens/agenda/Agenda.kt` uses `Clock.System.todayIn(...)`, distinct from the shared-module model file of the same base name)"
    - "R8 full-mode release builds fail hard on classes referenced by a third-party library's bytecode but genuinely absent from the resolved classpath (here: kotlinx-datetime 0.7.0+ removed the real Clock/Instant .class files in favor of typealiases, but openfeedback-viewmodel was compiled against 0.6.x's real classes). R8 itself writes the exact fix to build/outputs/mapping/release/missing_rules.txt as -dontwarn lines — safe to apply verbatim when the referencing code path is confirmed unreachable (feature-flagged off)"

key-files:
  created:
    - shared/src/commonTest/kotlin/com/gdgnantes/devfest/model/ScheduleSlotDateParsingTest.kt
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-agenda-firebase-datetime-bump.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-agenda-day2-firebase-datetime-bump.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-speakers-firebase-datetime-bump.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-venue-firebase-datetime-bump.png
  modified:
    - gradle/libs.versions.toml
    - androidApp/build.gradle.kts
    - androidApp/proguard-rules.pro
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/Agenda.kt

key-decisions:
  - "kotlinx-datetime 0.8.0's Instant typealias survived (assumption A2 resolved: no import change needed in ScheduleSlot.kt/Agenda.kt) — confirmed by a zero-diff `git diff` on both files after the bump plus a green Task-1 test run"
  - "Firebase -ktx catalog aliases repointed at plain (KTX-merged) artifacts rather than pinning the frozen 23.x -ktx releases, per Firebase's own official migration guidance (kb://firebase/docs/android/kotlin-migration), preserving the `libs.firebase.*.ktx` catalog alias names so downstream build-file references needed no rename"
  - "Added a Gradle dependency substitution (not a version pin) for the transitive firebase-auth-ktx pulled in by openfeedback-viewmodel, redirecting it to the BOM-managed plain firebase-auth artifact — avoids dragging in a stale, BOM-mismatched firebase-auth transitive that a straight version pin of the frozen -ktx artifact would have caused"
  - "androidApp's UI-layer Agenda.kt (Clock.System usage) required a one-line import swap to kotlin.time.Clock — this file is outside the plan's named <files> scope (ScheduleSlot.kt/Agenda.kt in shared/model) but sits on the identical kotlinx-datetime Instant/Clock migration surface RESEARCH.md's Pitfall 4/assumption A2 flagged; treated as a forced Rule-3 blocking-issue fix, not scope creep"
  - "Added three R8 -dontwarn rules (Clock$System, Instant$Companion, Instant) verbatim from R8's own generated missing_rules.txt, scoped to openfeedback's stale kotlinx-datetime 0.6.x class references; confirmed safe because OPEN_FEEDBACK_ENABLED=\"false\" makes those code paths unreachable at runtime"

patterns-established:
  - "Version-catalog-only discipline held except where the bump itself forced source changes (the two Firebase KTX import sites, the one Clock import site) — every version value still lives solely in gradle/libs.versions.toml"

requirements-completed: [BUILD-06]

coverage:
  - id: D1
    description: "Firebase BOM/coroutines/kotlinx-serialization/kotlinx-datetime bumped to latest stable in one commit; ScheduleSlot/Agenda date-parsing test (Task 1) proven green on both the pre-bump and post-bump kotlinx-datetime version, pinning exact epoch-millisecond and day-bucketing semantics"
    requirement: "BUILD-06"
    verification:
      - kind: unit
        ref: "shared/src/commonTest/kotlin/com/gdgnantes/devfest/model/ScheduleSlotDateParsingTest.kt — ./gradlew :shared:jvmTest --tests 'com.gdgnantes.devfest.model.ScheduleSlotDateParsingTest'"
        status: pass
      - kind: other
        ref: "./gradlew clean :shared:compileKotlinIosX64 :shared:compileKotlinIosArm64 :shared:compileKotlinIosSimulatorArm64 :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest detekt lint"
        status: pass
      - kind: other
        ref: "gh run watch 35432065146 (android.yml, commit 736c843, 4/4 jobs) + gh run watch 35432065176 (ios.yml, commit 736c843) — Task 1's commit 31c8cd3 already confirmed green in the prior session (android.yml 35357043159, ios.yml 35357043188)"
        status: pass
    human_judgment: false
  - id: D2
    description: "D-02 local sanity run: debug app built on the bumped Firebase/kotlinx versions, installed and launched on a live emulator; Firebase Analytics/Crashlytics/Performance/RemoteConfig all initialize without error; Agenda day tabs and session times, Speakers list, and Venue (Remote-Config-backed) screens all render unchanged"
    requirement: "BUILD-06"
    verification:
      - kind: other
        ref: "adb logcat: FirebaseApp/FirebaseCrashlytics/PerformanceInitializer init lines with no FATAL EXCEPTION in the app process (pid 19095); android run + android layout confirming MainActivity focused and a populated Agenda UI tree"
        status: pass
      - kind: automated_ui
        ref: "screenshots 02-04-agenda-firebase-datetime-bump.png (day 1), 02-04-agenda-day2-firebase-datetime-bump.png (day 2), 02-04-speakers-firebase-datetime-bump.png, 02-04-venue-firebase-datetime-bump.png"
        status: pass
    human_judgment: true
    rationale: "Visual parity of the date-dependent Agenda screen (day tabs, session start/end times unchanged) and confirmation that Firebase-backed screens (Venue's Remote Config content) did not regress to an empty/default state are best judged by a human reviewing the attached screenshots, per D-02's sanity-run intent."

duration: ~75min (this session; excludes the prior session's Task 1 work)
completed: 2026-09-19
status: complete
---

# Phase 2 Plan 4: Firebase BOM + Coroutines + Serialization + kotlinx-datetime Bump Summary

**Firebase BOM 34.19.0, kotlinx-coroutines 1.11.0, kotlinx-serialization-json 1.11.0 and kotlinx-datetime 0.8.0 landed in one commit after a value-level date-parsing regression test, surfacing and fixing three previously-undocumented build breaks: Firebase's July-2025 KTX-module removal from the BOM, a `Clock.System` typealias gap in kotlinx-datetime 0.8.0 distinct from the `Instant` typealias, and an R8 full-mode release failure from a stale third-party dependency (openfeedback) still referencing kotlinx-datetime 0.6.x's real classes.**

## Performance

- **Duration:** ~75 min (this session, resuming after Task 1 was already committed and CI-green from a prior session)
- **Started:** 2026-09-19T~09:20:00Z (approximate, this session)
- **Completed:** 2026-09-19T~10:40:00Z
- **Tasks:** 2 (Task 1: date-parsing regression test — completed in prior session; Task 2: Firebase/coroutines/serialization/datetime bump — completed this session)
- **Files modified:** 6 (Task 2) + 1 created (Task 1, prior session)

## Accomplishments
- Task 1's `ScheduleSlotDateParsingTest` (7 `@Test` functions) reconfirmed present, unmodified, and green — this session re-ran it before touching any version, then again after the bump, with zero assertion changes
- Firebase BOM 33.16.0 → 34.19.0, kotlinx-coroutines 1.10.2 → 1.11.0, kotlinx-serialization-json 1.9.0 → 1.11.0 (1.12.0-RC explicitly excluded), kotlinx-datetime 0.6.2 → 0.8.0, googleServicesPlugin 4.4.3 → 4.5.0, firebaseCrashlyticsPlugin 3.0.6 → 3.0.8 — all re-verified live against Maven Central / Google's Maven immediately before the commit
- Resolved RESEARCH.md's open assumption A2 live: `kotlinx.datetime.Instant` is still a deprecated (not removed) typealias to `kotlin.time.Instant` in 0.8.0 — `ScheduleSlot.kt` and `Agenda.kt` compile completely unchanged (confirmed via `git diff` showing zero lines touched in either file)
- Discovered and fixed a build break RESEARCH.md did not anticipate: Firebase BOM 34.x removed the `-ktx` artifact constraints entirely (Firebase stopped publishing `-ktx` module updates in July 2025 and dropped them from the BOM's `<dependencyManagement>`). Fixed per Firebase's own official migration guide (`kb://firebase/docs/android/kotlin-migration`, fetched live via `android docs search`) — catalog aliases repointed at the merged plain artifacts, two production files (`AppModule.kt`, `FirebaseAnalyticsService.kt`) updated to import the relocated symbols, and a transitive `firebase-auth-ktx` (pulled in by `openfeedback-viewmodel` → `dev.gitlive:firebase-auth`) redirected via a Gradle dependency substitution
- Discovered that `kotlinx.datetime.Clock`'s typealias does **not** carry `.System` through to `kotlin.time.Clock` the way `Instant`'s typealias does — a third file (`androidApp/.../ui/screens/agenda/Agenda.kt`, outside the plan's named scope but on the identical migration surface) needed a one-line `import kotlin.time.Clock` swap
- Discovered and fixed an R8 full-mode release-build failure: `io.openfeedback:openfeedback-viewmodel` was compiled against kotlinx-datetime 0.6.x's real `Clock`/`Instant` classes, which no longer exist as `.class` files in 0.8.0 (typealiases generate none). Applied the exact three `-dontwarn` lines R8 itself generated in `missing_rules.txt`, safe because `OPEN_FEEDBACK_ENABLED = "false"` makes those code paths unreachable
- Full clean build green across every target: JVM, Android debug + release (R8 full mode), all three iOS Kotlin/Native targets (`iosX64`, `iosArm64`, `iosSimulatorArm64`), detekt, lint
- Both CI workflows green on this task's commit (`android.yml` 4/4 jobs, `ios.yml`), and Task 1's earlier commit's CI status reconfirmed green from the prior session
- D-02 local sanity run on a live Pixel_9 emulator: Firebase Analytics/Crashlytics/Performance/RemoteConfig all initialize without error (confirmed via `adb logcat`), Agenda day-1/day-2 tabs and session times render unchanged, Speakers list populates, Venue screen (Remote-Config-backed content) renders unchanged

## Task Commits

Each task was committed atomically:

1. **Task 1: Value-level date-parsing regression test** - `31c8cd3` (test) — committed and CI-verified green in a **prior session** (this session reconfirmed it, made no changes to it)
2. **Task 2: Firebase BOM + coroutines + serialization + datetime bump** - `736c843` (build)

**Plan metadata:** commit pending (this SUMMARY + STATE.md + ROADMAP.md + REQUIREMENTS.md)

## Files Created/Modified
- `shared/src/commonTest/kotlin/com/gdgnantes/devfest/model/ScheduleSlotDateParsingTest.kt` - Task 1 (prior session); reconfirmed unchanged and green this session
- `gradle/libs.versions.toml` - firebaseBom, kotlinxCoroutines, kotlinxSerialization, kotlinxDatetime, googleServicesPlugin, firebaseCrashlyticsPlugin bumped; four firebase-*-ktx library entries' `name` field repointed at plain (KTX-merged) artifacts
- `androidApp/build.gradle.kts` - added a `resolutionStrategy.dependencySubstitution` redirecting the transitive `firebase-auth-ktx` (via openfeedback) to the plain, BOM-managed `firebase-auth` artifact
- `androidApp/proguard-rules.pro` - three `-dontwarn` rules for openfeedback's stale kotlinx-datetime 0.6.x class references (R8-generated, verbatim)
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt` - Firebase KTX imports repointed (`com.google.firebase.ktx.Firebase` → `com.google.firebase.Firebase`, etc.)
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt` - `com.google.firebase.analytics.ktx.logEvent` → `com.google.firebase.analytics.logEvent`
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/Agenda.kt` - `import kotlinx.datetime.Clock` → `import kotlin.time.Clock` (Clock.System typealias gap, distinct from Instant)
- `.planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-*.png` - D-02 sanity-run evidence (4 screenshots)

## Decisions Made

See `key-decisions` in frontmatter for the full list. Summary:
- Assumption A2 resolved in favor of "typealias survived" for `Instant` — zero code change in the plan's named files.
- Firebase's KTX-module BOM removal (not documented in 02-RESEARCH.md, discovered live) fixed per Firebase's official migration guide, preserving catalog alias names.
- A separate, undocumented `Clock.System` typealias gap (distinct from `Instant`'s) required a one-line fix in a UI file outside the plan's named scope — treated as Rule 3 (forced blocking issue on the identical migration surface), not scope creep.
- R8 full-mode release build unblocked with R8's own generated `-dontwarn` rules, verified safe via the `OPEN_FEEDBACK_ENABLED=false` feature flag.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Firebase BOM 34.x removed the -ktx artifact constraints; four catalog aliases and two production files needed migration**
- **Found during:** Task 2, first full build attempt after the catalog version bump
- **Issue:** `:androidApp:processDebugNavigationResources` failed — `Could not find com.google.firebase:firebase-analytics-ktx:` (and config/crashlytics/perf) with no version, because firebase-bom 34.19.0's published POM no longer declares `<dependencyManagement>` entries for any `-ktx` artifact (Firebase stopped publishing new `-ktx` releases in July 2025 per its own release notes, confirmed via `android docs search` → `kb://firebase/docs/android/kotlin-migration`).
- **Fix:** Changed the four `firebase-*-ktx` catalog library `name` fields to their plain (KTX-merged) artifact names (`firebase-analytics`, `firebase-config`, `firebase-crashlytics`, `firebase-perf`) while keeping the catalog alias keys unchanged, so `libs.firebase.*.ktx` build-file references needed no rename. Updated the two production Kotlin files that imported the deprecated `.ktx` sub-packages (`AppModule.kt`: `Firebase`, `analytics`, `remoteConfig`, `remoteConfigSettings`; `FirebaseAnalyticsService.kt`: `logEvent`) to import the same symbols from the non-`.ktx` package, per Firebase's official migration guide's exact before/after example.
- **Files modified:** `gradle/libs.versions.toml`, `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt`, `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt`
- **Verification:** `:androidApp:compileDebugKotlin` and the full clean build/test/lint verification gate exit 0
- **Committed in:** `736c843`

**2. [Rule 3 - Blocking] Transitive firebase-auth-ktx (via openfeedback) also lost its BOM-managed version**
- **Found during:** Task 2, second full build attempt (after fixing deviation 1)
- **Issue:** Same root cause as deviation 1, but for a dependency this project never declares directly: `io.openfeedback:openfeedback-viewmodel` → `dev.gitlive:firebase-auth` → `com.google.firebase:firebase-auth-ktx` (no version, and no longer BOM-managed). `firebase-auth-ktx`'s last published version is 23.2.1, frozen since Firebase stopped releasing -ktx modules; the BOM-managed plain `firebase-auth` is now at 24.2.0, so pinning the stale -ktx version directly would have introduced a version mismatch against the rest of the BOM-aligned Firebase graph.
- **Fix:** Added `resolutionStrategy.dependencySubstitution { substitute(module("com.google.firebase:firebase-auth-ktx")).using(module("com.google.firebase:firebase-auth:24.2.0")) }` in `androidApp/build.gradle.kts`, alongside the existing `kotlin-metadata-jvm` force in the same `configurations.configureEach` block (same established pattern: forced sibling of a version bump, documented inline). The literal `24.2.0` matches firebase-bom 34.19.0's own managed `firebase-auth` version (verified against its published POM) and Firebase's own migration-guide example, which cites the identical version.
- **Files modified:** `androidApp/build.gradle.kts`
- **Verification:** Full clean build/test/lint verification gate exits 0
- **Committed in:** `736c843`

**3. [Rule 3 - Blocking] kotlinx.datetime.Clock's typealias does not carry `.System` through to kotlin.time.Clock**
- **Found during:** Task 2, third full build attempt (after fixing deviations 1-2)
- **Issue:** `:androidApp:compileDebugKotlin` failed with `Unresolved reference 'System'` at `androidApp/.../ui/screens/agenda/Agenda.kt:92` (`Clock.System.todayIn(...)`) — a file outside the plan's named `<files>` scope (`ScheduleSlot.kt`/`Agenda.kt` in `shared/model`) but on the identical kotlinx-datetime `Instant`/`Clock` migration surface RESEARCH.md's Pitfall 4/assumption A2 flagged. Bytecode inspection confirmed `kotlin.time.Clock` genuinely has a `System` nested object (`kotlin/time/Clock$System.class`) and `kotlinx.datetime.Clock` is still a (deprecated) typealias to it — yet the member access failed to resolve through the alias, unlike `Instant`'s typealias in the same file (which only produced a deprecation warning, not an error).
- **Fix:** Swapped `import kotlinx.datetime.Clock` to `import kotlin.time.Clock` in this one file, reordering the import to satisfy detekt's ktlint `ImportOrdering` rule (`kotlin.*` imports sort after `kotlinx.*`, not alphabetically interleaved). No other line changed.
- **Files modified:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/Agenda.kt`
- **Verification:** `:androidApp:compileDebugKotlin`, `detekt`, and the full clean build/test/lint verification gate exit 0
- **Committed in:** `736c843`

**4. [Rule 3 - Blocking] R8 full-mode release build failed on openfeedback's stale kotlinx-datetime 0.6.x class references**
- **Found during:** Task 2, fourth full build attempt (after fixing deviations 1-3), at `:androidApp:minifyReleaseWithR8`
- **Issue:** `ERROR: R8: Missing class kotlinx.datetime.Clock$System` (referenced from `io.openfeedback.extensions.SessionData_extKt.commitComment`) and `Missing class kotlinx.datetime.Instant$Companion` / `kotlinx.datetime.Instant` (referenced from `io.openfeedback.mappers.FirestoreToModelMappers_androidKt.timestampToInstant` and 16 other contexts). `openfeedback-viewmodel` was compiled against kotlinx-datetime 0.6.x, where these were real classes; in 0.8.0 they are typealiases with no `.class` files, so R8's full-mode class-presence check fails hard.
- **Fix:** Added the exact three `-dontwarn` lines R8 itself generated in `build/outputs/mapping/release/missing_rules.txt` to `androidApp/proguard-rules.pro`, with a comment explaining the cause and citing that `OPEN_FEEDBACK_ENABLED = "false"` (a `buildConfigField` already in `androidApp/build.gradle.kts`) makes these openfeedback code paths unreachable at runtime.
- **Files modified:** `androidApp/proguard-rules.pro`
- **Verification:** `:androidApp:assembleRelease` (R8 full mode) exits 0; full clean build/test/lint verification gate exits 0
- **Committed in:** `736c843`

---

**Total deviations:** 4, all Rule 3 (blocking issues directly forced by the version bump this task makes, none discretionary)
**Impact on plan:** All four fixes were strictly necessary to make the bump build at all (deviations 1-3) or to make the release variant build at all (deviation 4). None expanded scope beyond what the kotlinx-datetime/Firebase-BOM bump itself forced; deviation 3 touched one file outside the plan's named `<files>` list, but that file sits on the exact same migration surface the plan's own assumption A2 anticipated.

## Issues Encountered
None beyond what's captured in Deviations above — every blocker encountered was resolved within this plan's execution, without needing a D-03 version-pin-below-target escape valve (all four target versions from BUILD-06 landed exactly as researched).

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- `gradle/libs.versions.toml` now carries `firebaseBom = "34.19.0"`, `kotlinxCoroutines = "1.11.0"`, `kotlinxSerialization = "1.11.0"`, `kotlinxDatetime = "0.8.0"`, `googleServicesPlugin = "4.5.0"`, `firebaseCrashlyticsPlugin = "3.0.8"` — the baseline `02-05` (Gradle Declarative DSL pilot) builds on.
- The `firebase-auth-ktx` → `firebase-auth:24.2.0` dependency substitution in `androidApp/build.gradle.kts` is version-coupled to the currently-pinned `firebaseBom`; if a future stage bumps `firebaseBom` again, re-verify this literal against the new BOM's managed `firebase-auth` version.
- The `kotlinx.datetime.Clock.System` typealias gap (distinct from `Instant`, which still works) is now documented here for any future kotlinx-datetime bump or Phase 3/4/5 code touching `Clock` directly — grep for `kotlinx.datetime.Clock` before assuming a typealias covers it.
- Screenshots from the D-02 sanity run are at `.planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-*.png` for human review.
- No blockers for 02-05.

## Self-Check: PASSED

- FOUND: shared/src/commonTest/kotlin/com/gdgnantes/devfest/model/ScheduleSlotDateParsingTest.kt
- FOUND: gradle/libs.versions.toml (firebaseBom = "34.19.0", kotlinxCoroutines = "1.11.0", kotlinxSerialization = "1.11.0", kotlinxDatetime = "0.8.0")
- FOUND: androidApp/build.gradle.kts (dependencySubstitution for firebase-auth-ktx)
- FOUND: androidApp/proguard-rules.pro (three kotlinx.datetime -dontwarn rules)
- FOUND: androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt (non-.ktx Firebase imports)
- FOUND: androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt (non-.ktx logEvent import)
- FOUND: androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/Agenda.kt (kotlin.time.Clock import)
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-agenda-firebase-datetime-bump.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-agenda-day2-firebase-datetime-bump.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-speakers-firebase-datetime-bump.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-04-venue-firebase-datetime-bump.png
- FOUND commit 31c8cd3c51b6ee8ad7ba008094da9117f509579c in git log
- FOUND commit 736c8434ae1303c0cd4cb1b1d05ed5fc71cdd929 in git log
- CONFIRMED: android.yml run 35432065146 conclusion=success on commit 736c843 (4/4 jobs)
- CONFIRMED: ios.yml run 35432065176 conclusion=success on commit 736c843
- CONFIRMED: android.yml run 35357043159 conclusion=success on commit 31c8cd3 (prior session)
- CONFIRMED: ios.yml run 35357043188 conclusion=success on commit 31c8cd3 (prior session)

---
*Phase: 02-dependency-build-tooling-upgrade*
*Completed: 2026-09-19*
