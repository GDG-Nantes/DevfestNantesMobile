---
status: testing
phase: 02-dependency-build-tooling-upgrade
source: [02-VERIFICATION.md]
started: 2026-09-19T12:35:00Z
updated: 2026-09-19T12:35:00Z
---

## Current Test

number: 1
name: 02-01 (Kotlin 2.4.20 + KSP) sanity pass
expected: |
  App looks and behaves exactly as before the Kotlin/KSP bump — no crash, sessions render, navigation works.
awaiting: user response

## Tests

### 1. 02-01 (Kotlin 2.4.20 + KSP): install/launch the debug app after the Kotlin/KSP bump and confirm the Agenda screen lists sessions and bottom-nav works, matching pre-bump behavior.
expected: App looks and behaves exactly as before the Kotlin/KSP bump — no crash, sessions render, navigation works. Screenshots: 02-01-agenda.png, 02-01-speakers.png.
result: [pending]

### 2. 02-02 (AGP 9 KMP-library plugin swap): drive Agenda, Speakers, Venue and Bookmarks on the AGP-9-built app; confirm each renders and navigates, and that bookmarks existing before the phase survived.
expected: All four screens render populated content and navigate correctly; pre-existing bookmarks are intact after the plugin swap. Screenshots: 02-02-*.png.
result: [pending]

### 3. 02-03a (Compose BOM 2026.09.00): confirm the Agenda day-pager swipe/indicator, Material 3 theming, and screen layout are visually unchanged after the Compose BOM bump.
expected: No clipped/overlapped/missing content; day pager and theming look as before. Screenshot: 02-03-agenda-compose-bom.png.
result: [pending]

### 4. 02-03b (Apollo 5.x cache migration, D-07): with network on, populate Agenda/Speakers/Venue; confirm a repeated query re-renders from cache without a loading flash; then go offline, force-relaunch, and confirm Agenda/Speakers/Venue still show the previously loaded data (not empty/error); confirm bookmarks survive the offline relaunch.
expected: |
  Offline relaunch shows real cached data on all three screens; bookmarks persist.
  FLAGGED FOR EXTRA ATTENTION: 02-03-08-agenda-offline-fixed.png shows only one session at 17:50 (with a bookmark checkmark) and nothing else on screen, while other online screenshots show 3+ sessions at the same timeslot. Please confirm this is the expected "Favori" filter state carried over from an earlier test step (per 02-02's bookmarks-filtered flow) and not a partial-data regression.
result: [pending]

### 5. 02-04 (Firebase BOM/coroutines/serialization/datetime bump): confirm Agenda day tabs and session start/end times are unchanged, and Speakers/Venue (Firebase Remote-Config-backed) still render populated content, not an empty/default state.
expected: Date-dependent UI (day tabs, session times) unchanged; Firebase-backed screens still populated. Screenshots: 02-04-*.png.
result: [pending]

### 6. General: confirm none of the 4 deliberately-authorized deviations (targetSdk 36 vs compileSdk 37, the GraphQLStore CacheAndNetwork fix, the firebase-auth-ktx substitution, and the openfeedback R8 -dontwarn rules) introduce any user-visible change, since the phase's stated goal is zero observable behavior change.
expected: No end-user-visible behavior change from any of the four authorized deviations.
result: [pending]

## Summary

total: 6
passed: 0
issues: 0
pending: 6
skipped: 0
blocked: 0

## Gaps
