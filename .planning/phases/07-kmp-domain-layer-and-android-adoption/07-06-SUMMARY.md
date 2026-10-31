---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 06
subsystem: testing
tags: [characterization, android, robolectric, golden]
status: complete

requires: ["07-05"]
provides:
  - "golden/android-filtering.json: v1.0 Android filter oracle for the 15 shared filter cases"
  - "golden/android-speaker-index.json: v1.0 Android speaker A-Z index oracle"
  - "Characterization of the filter drawer and equal-content filters (D-14), session classification and venue language"
affects: [07-12, 07-13, 07-14, 07-17, 07-18]

tech-stack:
  added: []
  patterns:
    - "Characterization tests drive the real v1.0 AgendaViewModel on the recorded snapshot; goldens are recorded once with -PrecordGoldens and committed with the tests"
    - "Drawer pinned through Robolectric Compose and asserted as (type, value) pair sets only, so the assertions survive the D-14 data-class change"

key-files:
  created:
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/characterization/AgendaFilteringCharacterizationTest.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/characterization/SessionFiltersDrawerCharacterizationTest.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/characterization/SessionFilterEqualityCharacterizationTest.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/characterization/SessionClassificationCharacterizationTest.kt
    - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexCharacterizationTest.kt
    - feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/utils/VenueLanguageCharacterizationTest.kt
    - core/domain/testdata/agenda/golden/android-filtering.json
    - core/domain/testdata/agenda/golden/android-speaker-index.json
  modified:
    - feature/speakers/build.gradle.kts

key-decisions:
  - "The drawer checkbox has no semantics (onCheckedChange = null), so the checked state is pinned through what a click emits (add when unchecked, remove every equal-content instance when checked)"
  - "Tests that build an AgendaViewModel must do it per test (function or lazy), never in a property initializer: viewModelScope needs Dispatchers.Main, which MainDispatcherRule installs after the test class is constructed"

requirements-completed: [SHARED-07, SHARED-02, SHARED-04]

duration: 1 session
completed: 2026-10-29

commits: 2
plan_head_before: 7ad0f9aa86f5881229efccc858b3beae266e1e57
plan_head_after: 21bacd2c58d21bb37253a2192db8088cd4f5df24

actuals:
  tokens: 14000
  tasks: 2
  commits: 2

coverage:
  deliverables:
    - name: "Android filtering golden for the 15 shared cases"
      human_judgment: false
      verification:
        - kind: command
          ref: "./gradlew :feature:agenda:testDebugUnitTest (AgendaFilteringCharacterizationTest, default run after record run)"
          status: pass
    - name: "Filter drawer, equal-content filters and classification characterization"
      human_judgment: false
      verification:
        - kind: command
          ref: "./gradlew :feature:agenda:testDebugUnitTest (SessionFiltersDrawer, SessionFilterEquality, SessionClassification characterization tests)"
          status: pass
    - name: "Speaker A-Z index golden and venue language characterization"
      human_judgment: false
      verification:
        - kind: command
          ref: "./gradlew :feature:speakers:testDebugUnitTest :feature:venue:testDebugUnitTest"
          status: pass
---

# Phase 7 Plan 06: Android filtering, drawer, classification, speaker index and venue language characterization Summary

**Characterization of the unchanged v1.0 Android code: the real AgendaViewModel on the 15 shared filter cases (golden/android-filtering.json), the Robolectric-driven filter drawer, equal-content filters, session classification, the speaker A-Z index (golden/android-speaker-index.json) and the venue language mapping.**

## Accomplishments

- `golden/android-filtering.json` (source `android-v1.0`, 15 cases in `filter-cases.json` order): `no_filter` lists all 94 snapshot sessions, `room_titan` 14 (only Titan sessions), `bookmark` 3, `language_french_complexity_advanced` 0. Every case keeps days 1 and 2 present; an empty result still exposes both days with no session (separate test with an impossible room filter).
- `SessionFiltersDrawerCharacterizationTest` (9 tests, Robolectric Compose): French, English, bookmark (empty value), every room (room id), every complexity and type (enum name) toggle on and off by `(type, value)`; toggles accumulate without duplicates; the clear button emits an empty set; one click on a checked box removes both equal-content instances.
- `SessionFilterEqualityCharacterizationTest`: re-applying an equal-content set (new instances) leaves `allIds()` and the `(type, value)` pairs unchanged. KDoc records the only post-D-14 difference (no re-emission through the StateFlow, no visible effect).
- `SessionClassificationCharacterizationTest`: `isService()` true only for OPENING, BREAK, LUNCH, star condition false for null and service types, `every_session_type_is_covered` guard.
- `golden/android-speaker-index.json`: 19 letters `a..t` (no `i`) in non-decreasing first-index order starting at 0, plus a literal-map fixture test (a..f at 0..5) and a repeated-letter/case test. Empty-name `NoSuchElementException` documented in KDoc as v1.0 debt (D-20, 07-18).
- `VenueLanguageCharacterizationTest`: fr, fr-FR, fr-CA give FRENCH; en, en-US, de-DE and the root locale give ENGLISH.
- `feature/speakers/build.gradle.kts`: same `devfest.domainTestdata` / `inputs.dir` / `-PrecordGoldens` test-task block as `feature/agenda`.

## Task Commits

1. **Task 1: filtering, drawer, equal-content filters, classification** - `47ecff9` (test)
2. **Task 2: speaker index and venue language** - `21bacd2` (test)

## TDD note

Characterization plan: the RED state is the absent golden (`golden missing` / `golden recorded, re-run` by design). Each golden was recorded once with `-PrecordGoldens` (the only expected failure), then the default run was green and wrote nothing. No production code changed, so there is no GREEN commit; `git diff --stat 7ad0f9a HEAD -- feature/*/src/main` is empty.

## Deviations from Plan

None - plan executed as written. One test-authoring fix, not a deviation: the first version of `SessionFilterEqualityCharacterizationTest` built the ViewModel in a property initializer (before `MainDispatcherRule` installs Main) and failed; it is now built `by lazy`. A detekt `ArgumentListWrapping` issue on one long `assertEquals` was reformatted.

## Verification

- `./gradlew :feature:agenda:testDebugUnitTest :feature:speakers:testDebugUnitTest :feature:venue:testDebugUnitTest`: green; characterization suites: agenda 4+3+3+2+9 tests, speakers 3, venue 2, 0 failures.
- `jq -e '.source == "android-v1.0" and (.cases | length == 15)'` on the filtering golden, and `.firstIndexes[0].index == 0` on the speaker golden: true.
- CI list: `./gradlew detekt lint`, `bash scripts/di-gate.sh` (DI-GATE-OK), `bash scripts/kmp-nc-version-gate.sh` (KMPNC-VERSION-OK 1.0.6), `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`: all green.
- Goldens committed, working tree clean for them after the default runs.

## Known Stubs

None.

## Threat Flags

None.

## Notes for downstream plans

- 07-12 / 07-13 read `agenda/golden/android-filtering.json` and `android-speaker-index.json` through `GoldenFiles.read`; iOS passes `DOMAIN_TESTDATA` with the `SIMCTL_CHILD_` prefix.
- From 07-14 on, update these tests only for imports, type renames, constructor wiring and selector helpers (D-18); never an expected value.
- Within a test class, build any `AgendaViewModel` per test, not in a property initializer.

## Self-Check: PASSED

- All created files exist; commits `47ecff9` and `21bacd2` are ancestors of HEAD.
