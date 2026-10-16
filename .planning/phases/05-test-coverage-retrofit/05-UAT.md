---
status: complete
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
  artifacts: []
  missing: []
