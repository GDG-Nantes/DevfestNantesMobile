---
phase: 05-test-coverage-retrofit
plan: 11
subsystem: testing
tags: [kotlinx-datetime, openfeedback, r8, dependency-resolution, regression-guard, kotlin-time]
gap_closure: true
gap_ids: [G-05-3]

requires:
  - phase: 05-test-coverage-retrofit
    provides: 05-10 smoke tests and the 05-08 FeedbackFlags seam that UAT test 3 exercised
  - phase: 02-dependency-build-tooling-upgrade
    provides: the kotlinx-datetime 0.8.0 bump (736c843) and the R8 suppressions this plan retires
provides:
  - OpenFeedbackDatetimeLinkageTest, a CI guard that runs OpenFeedback's real timestamp, comment-posting and comment-display code on the :androidApp unit-test runtime classpath
  - kotlinx-datetime pinned to the 0.8.0-0.6.x-compat twin on every app runtime classpath
  - first-party code on kotlin.time.Instant only, with the iOS umbrella header byte-identical
  - androidApp/proguard-rules.pro without the kotlinx-datetime suppressions
  - STATE.md record of the compat bridge and its 0.8.x end
affects: [phase-05-verification, uat-test-3, any-future-kotlinx-datetime-or-openfeedback-bump]

plan_head_before: 205b22142abe22b1620b7ec5dbc8c229aaaa0a21
plan_head_after: 62a8857fc85becaf063afc1e2fa52a23c61b8462

actuals:
  tokens: 2800
  tasks: 3
  commits: 5

tech-stack:
  added: ["org.jetbrains.kotlinx:kotlinx-datetime:0.8.0-0.6.x-compat (replaces 0.8.0, same publisher)"]
  patterns:
    - "Reflection-based linkage guard for prebuilt third-party bytecode whose signatures name types the compile classpath no longer has"
    - "Deprecated compat types stay a runtime-only bridge; first-party source names only kotlin.time types"

key-files:
  created:
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt
  modified:
    - gradle/libs.versions.toml
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Agenda.kt
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/ScheduleSlot.kt
    - core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/ScheduleSlotDateParsingTest.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/Agenda.kt
    - androidApp/proguard-rules.pro
    - .planning/STATE.md

key-decisions:
  - "Resolve kotlinx-datetime to 0.8.0-0.6.x-compat instead of downgrading to 0.6.2: keeps the newest library and its kotlin.time API while restoring the classes OpenFeedback links against"
  - "Swap the core:model commonMain imports in the same commit as the catalog pin so no pushed tip changes the Swift-visible API"
  - "Remove the R8 suppressions entirely rather than narrowing them: with the classes present R8 reports nothing missing, so a future regression fails the release build loudly"

patterns-established:
  - "Pin comment in the version catalog states the reason, the end-of-line constraint (compat ends with 0.8.x) and which test rejects a plain bump"
  - "Pre-push gate for changes that can move the iOS API: relink the framework and diff shared.h against a baseline captured before the first edit"

requirements-completed: [TEST-03]

coverage:
  - id: D1
    description: "OpenFeedback's Firestore timestamp mapper (the crash frame), comment posting (Clock.System) and comment display mapping (toLocalDateTime) run on the :androidApp unit-test runtime classpath in CI's testDebugUnitTest, independent of OPEN_FEEDBACK_ENABLED and Remote Config; red on plain 0.8.0, green on the compat artifact"
    requirement: "TEST-03"
    verification:
      - kind: unit
        ref: "androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt#firestore_timestamp_maps_to_an_instant"
        status: pass
      - kind: unit
        ref: "androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt#posting_comments_stamps_and_sorts_them_with_the_system_clock"
        status: pass
      - kind: unit
        ref: "androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt#posted_comment_is_formatted_for_display"
        status: pass
    human_judgment: false
  - id: D2
    description: "debugRuntimeClasspath, releaseRuntimeClasspath and debugUnitTestRuntimeClasspath of :androidApp all resolve every kotlinx-datetime request to 0.8.0-0.6.x-compat"
    requirement: "TEST-03"
    verification:
      - kind: other
        ref: "./gradlew -q :androidApp:dependencies --configuration {debug,release,debugUnitTest}RuntimeClasspath (Task 1 verify loop) printed DATETIME-COMPAT-GUARD-GREEN"
        status: pass
    human_judgment: false
  - id: D3
    description: "First-party Kotlin names only kotlin.time.Instant; the 05-02 date characterization tests stay green on JVM, Android and iOS; the iOS umbrella header shared.h is byte-identical to the pre-change header and swift-names-gate passes"
    requirement: "TEST-03"
    verification:
      - kind: other
        ref: "Task 2 verify command printed INSTANT-MIGRATED-HEADER-UNCHANGED (testDebugUnitTest jvmTest :core:model:iosSimulatorArm64Test, empty shared.h diff, SWIFT-NAMES-OK, empty git grep for the deprecated types, DI-GATE-OK)"
        status: pass
    human_judgment: false
  - id: D4
    description: "The R8 release build passes with the three kotlinx-datetime suppression rules and their false 'unreachable' comment removed, and R8 writes no missing_rules.txt"
    requirement: "TEST-03"
    verification:
      - kind: other
        ref: "Task 3 gate 1 printed R8-CLEAN-WITHOUT-SUPPRESSION (:androidApp:assembleRelease)"
        status: pass
    human_judgment: false
  - id: D5
    description: "Full CI-equivalent suite is green locally and both workflows are green on HEAD 62a8857, with the guard running inside the Android unit-test job"
    requirement: "TEST-03"
    verification:
      - kind: other
        ref: "Task 3 gate 2 (assembleDebug testDebugUnitTest jvmTest testAndroidHostTest detekt lint iOS tests, DI-GATE-OK) and gate 3 printed CI-GREEN-BOTH 62a8857fc85becaf063afc1e2fa52a23c61b8462"
        status: pass
    human_judgment: false
  - id: D6
    description: "On the dev debug build with real Remote Config, after the consent dialog, session detail shows the feedback area with its comments, a comment can be posted, and turning the flag off hides the area again, with no crash (UAT test 3 re-check)"
    verification: []
    human_judgment: true
    rationale: "Real Remote Config values, the populated OpenFeedback secrets and the production Firestore exist only on a developer device, and posting writes a real comment. The JVM guard covers the same OpenFeedback code paths in CI but cannot stand in for the device run."

duration: 30min
completed: 2026-10-16
status: complete
---

# Phase 5 Plan 11: OpenFeedback kotlinx-datetime Crash Closure (G-05-3) Summary

**kotlinx-datetime pinned to `0.8.0-0.6.x-compat` so OpenFeedback's prebuilt Instant/Clock links resolve again, guarded in CI by a reflection test over OpenFeedback's own code, with first-party code on `kotlin.time.Instant`, the iOS header unchanged and the R8 suppressions gone**

## Performance

- **Duration:** about 30 min (most of it Gradle runs and the CI wait)
- **Started:** 2026-10-16T14:13:00Z (orchestrator phase-start commit 205b221)
- **Completed:** 2026-10-16T14:43:00Z
- **Tasks:** 3
- **Files modified:** 7 (1 created, 6 modified; plus `.planning/STATE.md` counted among them)

## Accomplishments

- **Crash fixed in the dependency graph.** OpenFeedback 1.0.0-alpha.3 links against `kotlinx.datetime.Instant`, `Instant.Companion`, `Clock.System` and `toLocalDateTime(kotlinx.datetime.Instant, TimeZone)`, which plain 0.8.0 no longer ships. The catalog now resolves the compat twin, and all three app runtime classpaths show a single `0.8.0-0.6.x-compat`.
- **CI guard that reaches the broken path.** `OpenFeedbackDatetimeLinkageTest` runs the timestamp mapper (the crash frame), comment posting and comment display mapping through reflection. It needs neither OPEN_FEEDBACK_ENABLED nor Remote Config, runs in `testDebugUnitTest`, and was shown red on plain 0.8.0 before the fix.
- **First-party code off the deprecated type.** All four first-party files that imported `kotlinx.datetime.Instant` now import `kotlin.time.Instant`. `git grep` finds the deprecated names nowhere outside the guard. `shared.h` is byte-identical before and after.
- **No hidden missing classes.** The three `-dontwarn kotlinx.datetime.*` rules and their false premise are deleted, `assembleRelease` passes, and R8 writes no `missing_rules.txt`.
- **Bridge documented.** The catalog comment and a STATE.md follow-up state that compat releases end with 0.8.x and that moving on needs an OpenFeedback release built on `kotlin.time.Instant`.

## Task Commits

Base commit of the plan (`plan_head_before`): `205b22142abe22b1620b7ec5dbc8c229aaaa0a21`.

1. **Task 1: Tracer, linkage guard red then green**
   - RED: `7ad6a62` (test)
   - GREEN: `2a8c617` (fix)
2. **Task 2: Remaining first-party Instant imports on kotlin.time** - `a13a383` (refactor)
3. **Task 3: Remove R8 suppressions, document the bridge**
   - `f2b81b5` (fix), proguard rules
   - `62a8857` (docs), STATE.md follow-up and amendment

**Plan metadata:** the docs commit that adds this SUMMARY and the STATE.md/ROADMAP.md updates (follows `62a8857`).

## RED Evidence (Task 1, plain kotlinx-datetime 0.8.0)

Run: `./gradlew --no-daemon :androidApp:testDebugUnitTest --tests '...OpenFeedbackDatetimeLinkageTest'` on the unchanged catalog, from `androidApp/build/test-results/testDebugUnitTest/TEST-com.gdgnantes.devfest.androidapp.feedback.OpenFeedbackDatetimeLinkageTest.xml` (`tests="3" skipped="0" failures="3" errors="0"`):

```
posting_comments_stamps_and_sorts_them_with_the_system_clock
  java.lang.NoClassDefFoundError: kotlinx/datetime/Clock$System
  Caused by: java.lang.ClassNotFoundException: kotlinx.datetime.Clock$System
firestore_timestamp_maps_to_an_instant
  java.lang.NoClassDefFoundError: kotlinx/datetime/Instant
  Caused by: java.lang.ClassNotFoundException: kotlinx.datetime.Instant
posted_comment_is_formatted_for_display
  java.lang.NoClassDefFoundError: kotlinx/datetime/Clock$System
  Caused by: java.lang.ClassNotFoundException: kotlinx.datetime.Clock$System
```

Semantic assessment: all three target tests ran and failed at the OpenFeedback call site with exactly the linkage error the device logcat shows (`kotlinx/datetime/Instant`), through the unwrapped `InvocationTargetException`. There was no ClassNotFoundException for an OpenFeedback class, no NoSuchMethodException and no Android stub error, so the failure is the planned one and not a test-harness fault. (The first RED attempt failed to compile on a `Any?` vs `Any` type mismatch inside the test; that was fixed before the RED run reported above.)

## Resolved Classpaths (Task 1, after the pin)

| Configuration | kotlinx-datetime resolves to |
|---|---|
| `:androidApp` debugRuntimeClasspath | `0.8.0-0.6.x-compat` |
| `:androidApp` releaseRuntimeClasspath | `0.8.0-0.6.x-compat` |
| `:androidApp` debugUnitTestRuntimeClasspath | `0.8.0-0.6.x-compat` |

The `DependencyResolutionRules.kt` fallback was not needed.

## Push Ranges

**Push 1 range** (`origin/feature/reno_phase_5..HEAD` before Task 1's push, 14 commits; `dc9f3f6..2a8c617`):

```
2a8c617 fix(05-11): resolve kotlinx-datetime to 0.8.0-0.6.x-compat for OpenFeedback (G-05-3)
7ad6a62 test(05-11): OpenFeedback kotlinx-datetime linkage guard, red on 0.8.0 (G-05-3)
205b221 docs(phase-05): start gap-closure execution of 05-11 (G-05-3)
b7a1a34 fix(05): revise 05-11 so no pushed tip changes shared.h and gates run in step order
dba146c fix(05): revise 05-11 push policy and anchors per plan-checker feedback
67ad8a0 docs(05): create gap closure plan 05-11 for G-05-3 (OpenFeedback kotlinx-datetime crash)
0b3a9a2 docs(05): add root causes from diagnosis
405917f docs(05): diagnose G-05-3 openfeedback kotlinx-datetime Instant crash
c70edb6 test(05): complete UAT - 2 passed, 1 issues
9fe8760 docs(05): record code review disposition
cd821de docs(05): add code review fix report
f5ed59f fix(05): WR-03 record the pinned URL and offset defects in deferred-items
c17825e fix(05): WR-02 reset FirebaseApp between Robolectric tests
399059c fix(05): WR-01 keep KoinStartupTest offline by resolving FeedbackFormViewModel with offline flags
```

Range check (step 8): every commit outside `.planning/` was a 05-11 commit or one of 399059c, c17825e, f5ed59f (test files only). No unknown code was pushed.

**Pre-push gate result:** `./gradlew --no-daemon :androidApp:testDebugUnitTest :feature:settings:testDebugUnitTest :feature:session-detail:testDebugUnitTest :core:data:jvmTest :core:model:jvmTest :core:model:iosSimulatorArm64Test :shared:linkDebugFrameworkIosSimulatorArm64 detekt` finished `BUILD SUCCESSFUL`. `diff -u` of `shared.h` against the pre-plan baseline printed nothing (header identical, 146771 bytes). The Task 1 verify command printed `DATETIME-COMPAT-GUARD-GREEN`.

**Push 2 range** (`2a8c617..a13a383`): one commit.

```
a13a383 refactor(05-11): first-party Instant on kotlin.time under the compat artifact (G-05-3)
```

**Push 3 range** (`a13a383..62a8857`):

```
62a8857 docs(05-11): record the kotlinx-datetime compat bridge and its 0.8.x end (G-05-3)
f2b81b5 fix(05-11): drop the kotlinx-datetime R8 suppressions (G-05-3)
```

## iOS Header Result

`shared.h` was captured from the unmodified tree before the first edit and compared after Task 1's pin and again after Task 2's remaining swaps. Both diffs are empty. `swift-names-gate.sh check` printed `SWIFT-NAMES-OK`. The Task 2 verify command printed `INSTANT-MIGRATED-HEADER-UNCHANGED`. The 05-02 date characterization tests ran unchanged and green on JVM, Android and iOS (`:core:model:iosSimulatorArm64Test`).

## R8 Result

With the three suppressions and their comment removed from `androidApp/proguard-rules.pro` (old lines 30-39, deleted lines only, nothing added), `./gradlew :androidApp:assembleRelease` succeeded, `androidApp/build/outputs/mapping/release/missing_rules.txt` was not produced, and the gate printed `R8-CLEAN-WITHOUT-SUPPRESSION`. `-keep class io.openfeedback.** { *; }` is unchanged. The Phase 02 premise (OpenFeedback unreachable because OPEN_FEEDBACK_ENABLED is false) is retired: the secrets plugin sets the flag from `local.properties`, so developer and release builds could reach the code, and the suppressions only hid the dangling references.

## Gates

| Gate | Result |
|---|---|
| Task 1 verify | `DATETIME-COMPAT-GUARD-GREEN` |
| Task 2 verify | `INSTANT-MIGRATED-HEADER-UNCHANGED` (`SWIFT-NAMES-OK`, `DI-GATE-OK`) |
| Task 3 gate 1 | `R8-CLEAN-WITHOUT-SUPPRESSION` |
| Task 3 gate 2 | `assembleDebug testDebugUnitTest jvmTest testAndroidHostTest detekt lint :shared:iosSimulatorArm64Test :core:model:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test` all green, `DI-GATE-OK` |
| Task 3 gate 3 | `CI-GREEN-BOTH 62a8857fc85becaf063afc1e2fa52a23c61b8462` (Android CI run 38916073471, iOS CI run 38916073817; the Android log contains `Task :androidApp:testDebugUnitTest`) |

## Files Created/Modified

- `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt` - reflection guard over OpenFeedback's timestamp mapper, comment posting and comment display mapping
- `gradle/libs.versions.toml` - `kotlinxDatetime = "0.8.0-0.6.x-compat"` with a comment on the reason, the 0.8.x end of the compat line and the guard test
- `core/model/.../Agenda.kt`, `core/model/.../ScheduleSlot.kt` - import swap to `kotlin.time.Instant` (the files that reach `shared.h`)
- `core/model/src/commonTest/.../ScheduleSlotDateParsingTest.kt`, `feature/agenda/.../Agenda.kt` - import swap only
- `androidApp/proguard-rules.pro` - kotlinx-datetime suppressions and false comment removed
- `.planning/STATE.md` - non-blocking follow-up bullet and the G-05-3 amendment under the Phase 02 version deviations (additions only)

## Decisions Made

- Used the compat twin rather than downgrading to 0.6.2 or bumping OpenFeedback (no release is built on `kotlin.time`; alpha.6 still pins 0.6.2). Gradle ranks `0.8.0-0.6.x-compat` above both 0.8.0 and 0.6.0, so no substitution rule was needed.
- Kept the guard reflection-only so it compiles on any kotlinx-datetime version and fails at link time exactly like the device.
- No `@OptIn(ExperimentalTime::class)` was needed on any target for the `kotlin.time.Instant` imports (matches the `AgendaColumn.kt` precedent on Kotlin 2.4.20).

## Deviations from Plan

None - plan executed exactly as written.

The only departures were mechanical: a compile error in the first draft of the new test (a nullability mismatch, fixed before the RED run), and BSD `sed` mangling an import insertion that was corrected before staging. Neither changed behavior or scope.

## Issues Encountered

None. Notes for the next reader:

- `proguard-rules.pro` now ends with one blank line. The plan required the deletion to stay inside old lines 30-39, so the blank separator at line 29 was kept.
- CI waits on earlier pushed tips were superseded by `cancel-in-progress`, as the plan anticipated. Only the final HEAD (`62a8857`) was gated.

## Open Device Check (UAT test 3, not blocking this plan)

The human-check is collected for the end-of-phase UAT. Re-run UAT test 3 on a device:

1. Install the dev debug build with `android run`. It uses the developer `local.properties` with the OpenFeedback secrets.
2. Set Firebase Remote Config `openfeedback_enabled` to true. The app fetches at most every 300 s per screen, so restart the app if the change does not show.
3. Answer the data-sharing consent dialog and open a session detail page that already has feedback comments. Scroll to the feedback area.
4. Post a short comment on a session where a test comment is acceptable. Then edit it, or leave it.
5. Open two or three more sessions.
6. Set `openfeedback_enabled` back to false, restart, and open a session detail page again.

Expected: with the flag on, the feedback area renders the OpenFeedback form with its existing comments and their dates; posting shows the new comment; the app never crashes and `NoClassDefFoundError` never appears in logcat; with the flag off, the feedback area is hidden exactly as before the FeedbackFlags seam (05-08).

## User Setup Required

None - no external service configuration required.

## Known Stubs

None.

## Threat Flags

None. The only new external surface is the already-registered Maven coordinate (T-5-SC); no endpoints, auth paths or trust-boundary schema changes were added.

## Next Phase Readiness

- G-05-3 is closed in code. Only the device re-check of UAT test 3 remains for `/gsd-verify-work`.
- Follow-up carried in STATE.md: moving kotlinx-datetime past 0.8.x needs an OpenFeedback release built on `kotlin.time.Instant`. `OpenFeedbackDatetimeLinkageTest` fails CI on any bump that drops the compat classes, and should be deleted together with the compat pin once OpenFeedback no longer needs it.

---
*Phase: 05-test-coverage-retrofit*
*Completed: 2026-10-16*

## Self-Check: PASSED

- FOUND: androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt
- FOUND commits in HEAD ancestry: 7ad6a62, 2a8c617, a13a383, f2b81b5, 62a8857
- All three `<automated>` gates printed their success tokens; CI-GREEN-BOTH recorded for 62a8857.
