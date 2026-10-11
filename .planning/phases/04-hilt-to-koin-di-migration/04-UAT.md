---
status: testing
phase: 04-hilt-to-koin-di-migration
source: [04-VERIFICATION.md]
started: 2026-10-11T20:01:01Z
updated: 2026-10-11T20:01:01Z
---

## Current Test

number: 1
name: Full Android app walkthrough (debug and release APK)
expected: |
  Every screen renders real data, ViewModels resolve, consent toggle persists, fresh install shows the agreement dialog, no crash at launch. Speaker photos may be missing on networks that block raw.githubusercontent.com (pre-existing, diagnosed in 04-01).
awaiting: user response

## Tests

### 1. Full Android app walkthrough (debug and release APK): all tabs, agenda filters, bookmarks, session detail, speaker detail, settings consent toggle, fresh-install data-collection agreement dialog
expected: Every screen renders real data, ViewModels resolve, consent toggle persists, fresh install shows the agreement dialog, no crash at launch
result: [pending]

### 2. Release APK (R8/minified) launch by a human
expected: App starts, graph resolves under minification, no ClassNotFound/Koin resolution error
result: [pending]

### 3. iOS Agenda / Speakers / Venue / About on simulator or device
expected: Screens load data through IosDependencies.start()/store() with a single shared store
result: [pending]

## Summary

total: 3
passed: 0
issues: 0
pending: 3
skipped: 0
blocked: 0

## Gaps
