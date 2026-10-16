---
status: testing
phase: 05-test-coverage-retrofit
source: [05-VERIFICATION.md]
started: 2026-10-16T11:51:07Z
updated: 2026-10-16T11:51:07Z
---

## Current Test

number: 1
name: English-locale dates and agenda auto-scroll on a device
expected: |
  With the device in English (en-US), the agenda day tabs and the session date range read in English in the same formats as before the parser swap (French was already checked on the emulator). On opening the agenda during the event, auto-scroll lands on the same slot as a pre-change build.
awaiting: user response

## Tests

### 1. English-locale dates and agenda auto-scroll on a device
expected: Day tabs and session date range render correctly in English; agenda auto-scroll position matches a pre-change build (05-02).
result: [pending]

### 2. Agenda interaction walkthrough after dispatcher injection
expected: On the debug app, open Agenda, switch days, apply then clear a filter from the drawer, toggle a bookmark and pull to refresh. Behavior is unchanged and filtering shows no jank (05-06).
result: [pending]

### 3. Feedback area on session detail with real Remote Config
expected: After answering the data-sharing consent dialog, open a session detail page. The feedback area shows or hides exactly as before the FeedbackFlags seam, driven by the real Remote Config values (05-08).
result: [pending]

## Summary

total: 3
passed: 0
issues: 0
pending: 3
skipped: 0
blocked: 0

## Gaps
