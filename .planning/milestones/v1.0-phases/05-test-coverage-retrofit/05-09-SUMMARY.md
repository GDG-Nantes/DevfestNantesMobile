---
phase: 05-test-coverage-retrofit
plan: 09
subsystem: testing
tags: [compose-ui-test, koin-test, androidTest, smoke-tests, coil]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-03 fixtures, 05-02 :core:testing fakes and androidTestDoubles, 05-08 FeedbackFlags seam"
provides:
  - "Feature convention adds androidTest dependencies to every :feature:* module (:core:testing, compose ui-test-junit4, test runner, ext-junit, koin-test-junit4)"
  - "Compose smoke tests for Speakers (list + detail), Venue, About and Settings on the production Koin modules plus androidTestDoubles"
  - "Pattern: KoinTestRule (order 0) + createComposeRule (order 1), real *Route resolving its ViewModel with koinViewModel()"
affects: [05-10]

actuals:
  tokens: 4000
  tasks: 2
  commits: 2

plan_head_before: fdc71fe49a2b970b5b7ca98fce0e5bb15bdc96dd
plan_head_after: f2bcdd95b77c4cc938df2c0e75417ab94c3344c0

tech-stack:
  added: []
  patterns:
    - "Library-module Compose smoke test: KoinTestRule.create { modules(featureModule, coreUiModule); modules(androidTestDoubles) } then setContent { XRoute(...) }"
    - "Strings read through InstrumentationRegistry targetContext so assertions do not depend on the emulator locale"
    - "Coil.setImageLoader with an in-memory interceptor when a control depends on image load success (Venue floor plan)"

key-files:
  created:
    - feature/speakers/src/androidTest/java/com/gdgnantes/devfest/feature/speakers/SpeakersSmokeTest.kt
    - feature/venue/src/androidTest/java/com/gdgnantes/devfest/feature/venue/VenueSmokeTest.kt
    - feature/about/src/androidTest/java/com/gdgnantes/devfest/feature/about/AboutSmokeTest.kt
    - feature/settings/src/androidTest/java/com/gdgnantes/devfest/feature/settings/SettingsSmokeTest.kt
  modified:
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt

key-decisions:
  - "No packaging excludes were needed: assembleDebugAndroidTest and connectedDebugAndroidTest built without touching configureAndroidCommon, so AndroidCommon.kt is unchanged"
  - "VenueSmokeTest serves every image from memory through a Coil interceptor, because VenueFloorPlanButton removes itself when its image fails to load and the fixture URL is a real network address"
  - "SettingsSmokeTest overrides DataCollectionSettingsService with a private recording fake and allowOverride(true); the production impl needs Firebase handles"
  - "No production code changed and no test tags were added; every node is found by fixture text or resource contentDescription"

patterns-established:
  - "Smoke tests find the first toggleable node for the Accept all switch (it is the first Switch on the screen) because Text and Switch are unmerged siblings"

requirements-completed: [TEST-04]

coverage:
  - id: D1
    description: "Feature convention gives every feature module the androidTest dependency set; :core:testing stays androidTestImplementation only"
    requirement: TEST-04
    verification:
      - kind: other
        ref: "TESTING-ISOLATED-OK (git grep on build files and production source sets); assembleDebugAndroidTest for :feature:speakers"
        status: pass
    human_judgment: false
  - id: D2
    description: "Speakers list renders fixture speakers and reports the clicked speaker; detail renders Alice Moreau with her sessions and reports the clicked session"
    requirement: TEST-04
    verification:
      - kind: automated_ui
        ref: "SpeakersSmokeTest (2 tests); SPEAKERS-SMOKE-GREEN; CI android.yml run 38879583912 and 38880725984"
        status: pass
    human_judgment: false
  - id: D3
    description: "Venue renders the fixture venue and reports the floor-plan URL; About reports the clicked partner; Settings Accept all records consent and turns every switch on, and the data-sharing item reports navigation"
    requirement: TEST-04
    verification:
      - kind: automated_ui
        ref: "VenueSmokeTest (1), AboutSmokeTest (1), SettingsSmokeTest (2); FEATURE-SMOKES-GREEN; CI android.yml run 38880725984"
        status: pass
    human_judgment: false

duration: 30min
completed: 2026-10-16
status: complete
---

# Phase 5 Plan 09: Feature Smoke Tests (Speakers, Venue, About, Settings) Summary

**Compose smoke tests for four feature modules run on the production Koin modules plus `androidTestDoubles` with `KoinTestRule`, green on a Pixel_9 emulator and on the CI API 34 instrumentation job.**

## Performance

- **Duration:** about 30 min
- **Completed:** 2026-10-16
- **Tasks:** 2 (1 tracer, 1 auto)
- **Files:** 5 (4 created, 1 modified)

## Accomplishments

- `AndroidFeatureConventionPlugin` now adds `:core:testing`, `ui-test-junit4`, `androidx-test-ext-junit`, `androidx-test-runner` and `koin-test-junit4` as `androidTestImplementation` to every feature module.
- `SpeakersSmokeTest`: the list shows the fixture speakers and a click on Alice Moreau reports `SpeakerFixtures.alice`; the detail route shows Alice with her sessions and a click on "Compose deep dive" reports `SessionFixtures.composeDeepDive`.
- `VenueSmokeTest`: the fixture venue name renders and the floor-plan control reports `buildVenueStub(language).floorPlanUrl`, with the language taken from the system locale the way `VenueViewModel` does.
- `AboutSmokeTest`: the Pixel Studio logo is found by its formatted contentDescription, scrolled to, clicked, and reports the fixture partner.
- `SettingsSmokeTest`: Accept all calls `consentToAllServices` once and all four switches end on; the data-sharing item calls `onOpenDataSharing` once.
- The tracer gate held: Task 1 was verified end to end locally and in CI before Task 2 started.

## Task Commits

1. **Task 1 (tracer): feature androidTest wiring and the speakers smoke test** - `95d2db6`
2. **Task 2: venue, about and settings smoke tests** - `f2bcdd9`

Plan metadata (SUMMARY, STATE, ROADMAP, REQUIREMENTS) is committed separately after these.

## CI evidence

- Task 1 HEAD `95d2db6`: android.yml run 38879583912 green (Instrumentation tests (34) 8 min 19 s), iOS CI green. The log contains `Task :feature:speakers:connectedDebugAndroidTest` and no `OutOfMemoryError`.
- Task 2 HEAD `f2bcdd9` (plan final HEAD): android.yml run 38880725984 green, iOS CI run 38880726108 green. Instrumentation tests (34) took 6 min 9 s. The log runs `connectedDebugAndroidTest` for speakers, venue, about, settings, agenda and session-detail, and contains no `OutOfMemoryError`. The D8 dex-merging OOM (deferred item 12) did not return.
- Packaging excludes (`/META-INF/AL2.0`, `/META-INF/LGPL2.1`) were not needed.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug in my test] Duplicate "Alice Moreau" node in the detail test**
- **Found during:** Task 1, first emulator run
- **Issue:** `onNodeWithText("Alice Moreau")` matched two nodes: the top bar title and the speaker line on the session card, so `assertExists` failed.
- **Fix:** `onAllNodesWithText(...).onFirst().assertExists()` with a comment.
- **Files modified:** `SpeakersSmokeTest.kt`
- **Commit:** `95d2db6` (fixed before the commit)

**2. [Rule 2 - Missing robustness] Deterministic images in the Venue smoke test**
- **Found during:** Task 2, reading `VenueFloorPlanButton`
- **Issue:** the floor-plan card sets `displayVenue = false` in `onError`, so on an offline CI emulator the control could vanish before the click. The fixture URL is a real network address.
- **Fix:** `VenueSmokeTest` installs a `Coil` `ImageLoader` whose interceptor returns a `ColorDrawable` immediately, and resets Coil afterwards. No production change.
- **Files modified:** `VenueSmokeTest.kt`
- **Commit:** `f2bcdd9`

**Total deviations:** 2 (both inside test code). **Impact:** none on scope; no production file touched.

## Issues Encountered

None blocking. `MainActivityTest` is untouched (`git diff` on `androidApp/src/androidTest` is empty).

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None new. T-5-18 mitigated: `:core:testing` is added only as `androidTestImplementation`, TESTING-ISOLATED-OK printed. T-5-19 mitigated: log scanned, no `OutOfMemoryError`, no sharding and no dropped tests. T-5-SC: no new dependency coordinate (all five were already in the catalog).

## Next Phase Readiness

Ready for 05-10 (Agenda and Session detail smoke tests). The convention already gives those two modules the androidTest dependencies, and the same rule order and `androidTestDoubles` pattern applies. Agenda and Session detail already run `connectedDebugAndroidTest` in CI (they have no tests yet).

## Self-Check: PASSED

All five key files exist on disk; commits 95d2db6 and f2bcdd9 are on the branch (2 measured from the plan ledger, base fdc71fe); SPEAKERS-SMOKE-GREEN, FEATURE-SMOKES-GREEN, DI-GATE-OK, TESTING-ISOLATED-OK, CI-GREEN-BOTH and CI-RUNS-SPEAKERS-SMOKE observed.
