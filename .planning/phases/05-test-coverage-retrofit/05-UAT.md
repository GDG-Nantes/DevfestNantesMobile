---
status: diagnosed
phase: 05-test-coverage-retrofit
source: [05-VERIFICATION.md]
started: 2026-10-16T11:51:07Z
updated: 2026-10-16T13:19:57.423Z
---

## Current Test

[testing complete]

## Tests

### 1. English-locale dates and agenda auto-scroll on a device
expected: Day tabs and session date range render correctly in English; agenda auto-scroll position matches a pre-change build (05-02).
result: pass

### 2. Agenda interaction walkthrough after dispatcher injection
expected: On the debug app, open Agenda, switch days, apply then clear a filter from the drawer, toggle a bookmark and pull to refresh. Behavior is unchanged and filtering shows no jank (05-06).
result: pass
note: "Shifting the device date/time breaks SSL validation, so data may not load; pre-fetch data on the real clock before testing with a shifted date."

### 3. Feedback area on session detail with real Remote Config
expected: After answering the data-sharing consent dialog, open a session detail page. The feedback area shows or hides exactly as before the FeedbackFlags seam, driven by the real Remote Config values (05-08).
result: issue
reported: "crashed. I changed the flag on Firebase Remote Config and got the error FATAL EXCEPTION: main Process: com.gdgnantes.devfest.mobile.androidapp.dev, PID: 26012 java.lang.NoClassDefFoundError: Failed resolution of: Lkotlinx/datetime/Instant;"
severity: blocker

## Summary

total: 3
passed: 2
issues: 1
pending: 0
skipped: 0
blocked: 0

## Gaps

- gap_id: G-05-3
  truth: "After the consent dialog, the session detail feedback area shows or hides exactly as before the FeedbackFlags seam, driven by real Remote Config values"
  status: failed
  reason: "User reported: crashed. I changed the flag on Firebase Remote Config and got the error FATAL EXCEPTION: main Process: com.gdgnantes.devfest.mobile.androidapp.dev, PID: 26012 java.lang.NoClassDefFoundError: Failed resolution of: Lkotlinx/datetime/Instant;"
  severity: blocker
  test: 3
  root_cause: "OpenFeedback 1.0.0-alpha.3 bytecode references kotlinx.datetime.Instant / Clock$System (compiled against kotlinx-datetime 0.6.x); the app resolves kotlinx-datetime 0.8.0 (bumped in Phase 02, commit 736c843) where those classes no longer exist, so the first Firestore comment mapping throws NoClassDefFoundError. Phase 02 masked it with -dontwarn rules on a false 'unreachable' assumption (local.properties secrets set OPEN_FEEDBACK_ENABLED=true; Remote Config is the only gate). Latest OpenFeedback (1.0.0-alpha.6) still pins 0.6.2, so upgrading does not help."
  artifacts:
    - path: "gradle/libs.versions.toml"
      issue: "kotlinxDatetime = 0.8.0 incompatible with openfeedback 1.0.0-alpha.3 runtime references"
    - path: "androidApp/proguard-rules.pro"
      issue: "three -dontwarn kotlinx.datetime.* rules (lines 30-39) with false 'unreachable' comment hide the missing classes"
    - path: "build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DependencyResolutionRules.kt"
      issue: "natural place for a kotlinx-datetime compat pin/substitution"
    - path: "core/model/.../ScheduleSlot.kt, core/model/.../Agenda.kt, feature/agenda/.../Agenda.kt, core/model/src/commonTest/.../ScheduleSlotDateParsingTest.kt"
      issue: "still import kotlinx.datetime.Instant; become a distinct type from kotlin.time.Instant under the compat artifact"
  missing:
    - "Resolve kotlinx-datetime to 0.8.0-0.6.x-compat on runtime classpaths (verify with dependencyInsight)"
    - "Migrate remaining kotlinx.datetime.Instant imports to kotlin.time.Instant"
    - "Remove the -dontwarn kotlinx.datetime rules and the false comment"
    - "Regression test exercising OpenFeedback's timestampToInstant / Clock.System path on the app runtime classpath"
    - "Re-run UAT test 3 on device including posting a comment"
    - "Document that compat releases end at 0.8.x; long-term fix is an OpenFeedback release on kotlin.time"
  debug_session: .planning/debug/feedback-kotlinx-datetime-instant-crash.md
