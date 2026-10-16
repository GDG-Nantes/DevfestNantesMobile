---
phase: 05-test-coverage-retrofit
plan: 08
subsystem: testing
tags: [viewmodel-testing, feedback-flags-seam, robolectric, turbine, fakes]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-06 ViewModel harness (MainDispatcherRule, FakeDevFestNantesStore, Turbine), 05-03 fixtures, 05-07 patterns"
provides:
  - "Tests for the six remaining ViewModels: Session, Speaker, Partners, DataCollection, FeedbackForm, Venue (all ten ViewModels now tested)"
  - "FakeDataCollectionSettingsService (settings test sources)"
  - "FeedbackFlags seam (interface) and FirebaseFeedbackFlags adapter, bound in sessionDetailModule (D-23)"
  - "deferred items 13-16 logged in deferred-items.md"
affects: [05-09, 05-10]

requirements-completed: [TEST-03]

tech-stack:
  added: []
  patterns:
    - "Adapter seam over a Firebase client so a ViewModel is tested on a plain-JVM fake (mirrors PerformanceMonitoring)"
    - "Locale-dependent Robolectric tests set the system locale with @Config(qualifiers = ...)"

key-files:
  created:
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFlags.kt
    - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/SessionViewModelTest.kt
    - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFormViewModelTest.kt
    - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/SpeakerViewModelTest.kt
    - feature/about/src/test/java/com/gdgnantes/devfest/feature/about/partners/PartnersViewModelTest.kt
    - feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/FakeDataCollectionSettingsService.kt
    - feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionViewModelTest.kt
    - feature/venue/src/test/java/com/gdgnantes/devfest/feature/venue/VenueViewModelTest.kt
  modified:
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFormViewModel.kt
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/di/SessionDetailModule.kt
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt
    - .planning/phases/05-test-coverage-retrofit/deferred-items.md

key-decisions:
  - "FeedbackFormViewModel depends on FeedbackFlags (D-23); keys, AND logic and log lines stay in the ViewModel, FirebaseFeedbackFlags is a factory binding over the existing per-ViewModel remote-config factory"
  - "The RED step of the seam is a compile failure inside one commit: a separate non-compiling commit would break CI on the shared branch"

patterns-established:
  - "A private fake inside the test file for a single-consumer seam (FakeFeedbackFlags)"
  - "Uncaught store failures are pinned with assertFailsWith around runTest, as in 05-06"

coverage:
  - id: D1
    description: "Session, Speaker, Partners and DataCollection ViewModels load, expose and forward as specified, constructed directly with fakes"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "SessionViewModelTest (4), SpeakerViewModelTest (3), PartnersViewModelTest (3), DataCollectionViewModelTest (4); PASS-THROUGH-VMS-GREEN"
        status: pass
    human_judgment: false
  - id: D2
    description: "FeedbackFormViewModel flag matrix, remote key names, one fetch at init, re-evaluation after success, no change after failure, kill switch (T-5-16), project id, on a plain-JVM fake"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "FeedbackFormViewModelTest (11 tests); FEEDBACK-SEAM-GREEN, SEAM-WIRED-OK"
        status: pass
    human_judgment: false
  - id: D3
    description: "Production wiring of the FeedbackFlags seam resolves and keeps its behavior (module verify, graph verify unedited, startup resolution against the real remote config)"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "SessionDetailModuleTest, AndroidGraphVerifyTest (unedited), KoinStartupTest.feedback_form_view_model_resolves_with_real_remote_config"
        status: pass
    human_judgment: true
    rationale: "Real remote values only arrive on a device; the feedback area on a session detail page was not exercised (see Issues Encountered)"
  - id: D4
    description: "VenueViewModel maps fr to the French venue, en and unsupported locales to the English venue, starts LOADING and ends SUCCESS (Robolectric)"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "VenueViewModelTest (5 tests)"
        status: pass
    human_judgment: false
  - id: D5
    description: "All ten ViewModels tested with fakes only, CI green on the final HEAD"
    requirement: TEST-03
    verification:
      - kind: other
        ref: "ALL-TEN-VIEWMODELS-TESTED; android.yml run 38877493825 and ios.yml run 38877493987 on 05c3b5a, both success"
        status: pass
    human_judgment: false

duration: 15min
completed: 2026-10-16
status: complete
actuals:
  tokens: 9000
  tasks: 3
  commits: 3
plan_head_before: 15f91707f2f1a76318b56db8915f3641da142524
plan_head_after: 05c3b5aba180b985a2156a94b51f297e018a6c00
commits: 3
---

# Phase 5 Plan 08: Remaining ViewModel Tests and the FeedbackFlags Seam Summary

**All ten ViewModels now have fake-based unit tests: Session, Speaker, Partners, DataCollection and Venue (Robolectric only here) plus FeedbackForm, which became testable on a plain-JVM fake through a behavior-neutral `FeedbackFlags` seam over Firebase Remote Config (D-23).**

## Performance

- **Duration:** about 15 min including the CI wait
- **Tasks:** 3 (all `tdd="true"`)
- **Files:** 12 changed or added; 3 production files, all part of the D-23 seam

## Accomplishments

- `SessionViewModelTest` (4): fixture session for a known id, null for an unknown id, null to loaded under Turbine (Standard dispatcher), store failure pinned.
- `SpeakerViewModelTest` (3): alice with `[composeDeepDive, webRendering]` and SUCCESS, farid with `[closing]`, unknown id reports SUCCESS with a null speaker and no sessions.
- `PartnersViewModelTest` (3): the five exposed categories match `PartnerFixtures.byCategory`, `PARTNERS` (Community Hub) is not exposed, pushed partners replace the lists.
- `FakeDataCollectionSettingsService` and `DataCollectionViewModelTest` (4): single toggle, accept-all, refuse-all, pass-through of agreement and status.
- `FeedbackFlags` / `FirebaseFeedbackFlags`: `FeedbackFormViewModel` takes `feedbackFlags: FeedbackFlags` in the first constructor position. Keys, AND expressions and the two Timber messages stayed in the ViewModel. The adapter uses the same single-argument `addOnCompleteListener` overload (main-thread callback) and reads the task result only on success. `sessionDetailModule` binds `factory { FirebaseFeedbackFlags(get()) } bind FeedbackFlags::class`; `viewModelOf(::FeedbackFormViewModel)` and the remote-config factory with `setConfigSettingsAsync` are unchanged.
- `FeedbackFormViewModelTest` (11): flag matrix (config off, on without fallback, both on, fallback without the openfeedback flag, missing keys), the two production key names, exactly one fetch at init, re-application after a successful fetch, no change after a failed fetch, kill switch stays off after a successful fetch (T-5-16), project id pass-through. No `FirebaseApp`, no Robolectric, no dispatcher rule.
- `KoinStartupTest` keeps its test name, adds `assertIs<FirebaseFeedbackFlags>(koin.get<FeedbackFlags>())` and a corrected comment. `SessionDetailModuleTest` and `AndroidGraphVerifyTest` are unedited and green (no `extraTypes` change).
- `VenueViewModelTest` (5, Robolectric, `@Config(sdk = [34])`): fr, en and de locales, LOADING at start, SUCCESS after load, per-access `StateFlow` of `venue` characterized. The `@Config(qualifiers = "fr-rFR" | "en-rUS" | "de-rDE")` mechanism reached `Resources.getSystem()` on the first try; no `RuntimeEnvironment.setQualifiers` fallback was needed.

## Task Commits

1. Task 1: `3c74fed` test(05-08): session, speaker, partners and data-collection ViewModel tests
2. Task 2: `e8830fe` refactor(05-08): FeedbackFormViewModel behind the FeedbackFlags seam, tested on a fake (D-23)
3. Task 3: `05c3b5a` test(05-08): venue ViewModel tests under Robolectric; all ten ViewModels tested

## Verification

- PASS-THROUGH-VMS-GREEN: four modules' `testDebugUnitTest` and `detekt` green; all four result files present (4, 3, 3, 4 tests, no failures).
- FEEDBACK-SEAM-GREEN: `:feature:session-detail:testDebugUnitTest :feature:session-detail:assembleDebug :androidApp:testDebugUnitTest :androidApp:assembleDebug detekt lint` green; `FeedbackFormViewModelTest` (11), `SessionDetailModuleTest`, `KoinStartupTest` (5) and `AndroidGraphVerifyTest` results present. SEAM-WIRED-OK printed; DI-GATE-OK printed.
- ALL-TEN-VIEWMODELS-TESTED: full `testDebugUnitTest detekt` green, all ten result files present; DI-GATE-OK; the mocking-framework grep printed nothing; `git status --porcelain -- 'feature/*/src/main'` was empty before commits 1 and 3 and listed only the three D-23 files before commit 2.
- CI-GREEN-BOTH on `05c3b5a`: android.yml run 38877493825 and ios.yml run 38877493987, both success on the first attempt.

## TDD Notes

Tasks 1 and 3 characterize existing behavior, so there is no RED commit: the tests pass on first run against unchanged production code. Task 2 was driven test-first: `FeedbackFormViewModelTest` was written before the seam and failed to compile (`Unresolved reference 'FeedbackFlags'`, argument type mismatch with `FirebaseRemoteConfig`), then went green once the interface and the constructor change existed. The compile failure was not committed on its own, because a non-compiling commit on the shared branch would break CI; the plan specifies one commit for the seam. This is a single-commit exception to the RED/GREEN split of `tdd.md`, not a gate-enforced `type: tdd` plan.

## Deviations from Plan

**1. [Scope] Deferred items logged in Task 1 instead of Task 3**
- **Issue:** the plan lists `deferred-items.md` only under Task 3, but Task 1's tests expose defects (items 13-15).
- **Fix:** items 13-15 were appended in the Task 1 commit and item 16 (Venue) in the Task 3 commit, so no finding waits on a later commit.
- **Commits:** 3c74fed, 05c3b5a

**2. [Scope] Added tests beyond the `<behavior>` list:** none beyond it for Tasks 2-3; Partners got explicit assertions that the other four lists stay empty after a push.

**Total deviations:** 1 scope adjustment. **Impact:** none on production code beyond the D-23 seam.

## Issues Encountered

- **The Task 2 `human-check` (feedback area on a session detail page) was not completed.** The debug APK built from the seam commit installed and started on emulator-5554 and the app showed the agenda behind the data-sharing consent dialog with no crash attributable to the app (the only `FATAL EXCEPTION` entries in logcat belong to the `android` CLI instrumentation server thread). Reaching a session detail page requires answering the consent dialog, which is a user choice, so it was left alone. The check remains for the end-of-phase human verification (`human_verify_mode: end-of-phase`).
- `android layout` failed with "Unrecognized response from instrumentation server"; unrelated to the project, a screenshot via `adb exec-out screencap` was used instead.

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None new. T-5-16 mitigated by `disabled_config_stays_off_after_a_successful_fetch` and `reads_the_production_remote_keys`. T-5-23 mitigated: SEAM-WIRED-OK, unchanged listener overload and factory semantics, graph tests unedited and green, startup test resolves the real `FirebaseFeedbackFlags`. T-5-SC: no dependency added.

## Deferred items logged

- 13: `SessionViewModel` does not handle a failing `store.getSession` (pinned).
- 14: `SpeakerViewModel` reports SUCCESS for an unknown speaker id and has no error state (pinned).
- 15: `PartnersViewModel` never exposes the `PARTNERS` category (pinned).
- 16: `VenueViewModel.venue` builds a new `StateFlow` on every access (pinned).

## Next Phase Readiness

Ready for 05-09. TEST-03 is complete: all ten ViewModels are tested through constructor injection with fakes and no mocking framework, Robolectric used only for `VenueViewModel`. The device check of the feedback area is open for the phase's human verification.

## Self-Check: PASSED

Created files exist on disk; commits 3c74fed, e8830fe and 05c3b5a are on the branch (3 measured from the plan ledger, base 15f9170); FEEDBACK-SEAM-GREEN, SEAM-WIRED-OK, ALL-TEN-VIEWMODELS-TESTED, DI-GATE-OK and CI-GREEN-BOTH observed.
