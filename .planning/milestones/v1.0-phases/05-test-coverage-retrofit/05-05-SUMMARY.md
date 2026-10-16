---
phase: 05-test-coverage-retrofit
plan: 05
subsystem: testing
tags: [contract-test, apollo-mockserver, robolectric, sharedpreferences, firebase, kmp]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-03 fixtures, 05-04 RecordedResponses and MockServer pattern, androidHostTest source set"
provides:
  - "Abstract DevFestNantesStoreContractTest run against DevFestNantesStoreMocked and GraphQLStore on Apollo MockServer, on JVM, Android host and iOS simulator"
  - "RecordedResponsesHandler: MockServerHandler routing GraphQL operationName to recorded responses, 500 for unknown operations"
  - "Robolectric persistence tests pinning the keys and formats of bookmarks, agenda filters and data-collection consent"
  - "Test-only Crashlytics mapping-file id in :feature:settings src/test/res, absent from packaged resources"
affects: [05-06, 05-10]

actuals:
  tokens: 8000
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Abstract commonTest contract with per-implementation subclasses; value assertions live in the subclasses"
    - "Routing MockServerHandler (operation name to recording) instead of an ordered response queue"
    - "Persisted keys and formats written as literals in tests, never through the production constant"

key-files:
  created:
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/MockedStoreContractTest.kt
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreContractTest.kt
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/RecordedResponsesHandler.kt
    - core/data/src/androidHostTest/kotlin/com/gdgnantes/devfest/core/data/BookmarksStoreImplTest.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersServiceImplTest.kt
    - feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsServiceImplTest.kt
    - feature/settings/src/test/res/values/crashlytics_test_mapping_file_id.xml
  modified:
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreContractTest.kt
    - .planning/phases/05-test-coverage-retrofit/deferred-items.md

key-decisions:
  - "MockServer.Builder().handler(...).build() instead of the plan's MockServer(handler): the function form is a DEPRECATION_ERROR in apollo-mockserver 0.3.1 under Kotlin 2.4"
  - "Feature-module prefs tests use context.getSharedPreferences (androidx.preference is not on those classpaths); the :core:data bookmarks test uses PreferenceManager default prefs, the production file"
  - "GraphQL venue contract values are asserted with startsWith on the recorded English and French descriptions, so the contract does not depend on mapper whitespace handling"

patterns-established:
  - "Mutation check for key pins: renaming selected_sessions in BookmarksStoreImpl makes BookmarksStoreImplTest fail; production file restored"

requirements-completed: [TEST-02]

coverage:
  - id: D1
    description: "One store contract (11 invariants) holds for both the mocked store and GraphQLStore over MockServer on JVM, Android host and iOS simulator, with implementation values pinned in the subclasses"
    requirement: TEST-02
    verification:
      - kind: unit
        ref: "core/data MockedStoreContractTest (16 tests) and GraphQLStoreContractTest (20 tests, includes the inherited contract) present in jvmTest, testAndroidHostTest and iosSimulatorArm64Test results; inherited getSession_of_an_unknown_id_is_null present in the iOS results"
        status: pass
    human_judgment: false
  - id: D2
    description: "Bookmarks, agenda filters and consent persist under the same keys and formats, verified on real SharedPreferences under Robolectric; consent defaults and Firebase flag propagation pinned"
    requirement: TEST-02
    verification:
      - kind: unit
        ref: "BookmarksStoreImplTest (5), SessionFiltersServiceImplTest (4), DataCollectionSettingsServiceImplTest (5); PREFS-KEYS diff of production string literals against origin/main is empty"
        status: pass
    human_judgment: false
  - id: D3
    description: "The test-only Crashlytics id never reaches the library's packaged resources"
    requirement: TEST-02
    verification:
      - kind: other
        ref: "packageDebugResources and packageReleaseResources for :feature:settings, grep of packaged_res/debug and packaged_res/release finds no crashlytics.mapping_file_id (TEST-RES-CONFINED)"
        status: pass
    human_judgment: false
  - id: D4
    description: "CI green on the final code HEAD"
    requirement: TEST-02
    verification:
      - kind: other
        ref: "android.yml run 38867678502 and ios.yml run 38867678368 on 39cf00b, both success"
        status: pass
    human_judgment: false

duration: 25min
completed: 2026-10-16
status: complete
plan_head_before: 5d0e2bc9488cbcdb3b7809920a6abd377716978b
plan_head_after: 39cf00b7ea2bfc4364bf5405ad750c2b315d8fb1
commits: 2
---

# Phase 5 Plan 05: Store Contract and Persistence Tests Summary

**The mocked store and the real GraphQLStore (on Apollo MockServer with recorded responses) now satisfy one abstract contract on JVM, Android host and iOS, and the persisted keys and formats of bookmarks, agenda filters and data-collection consent are pinned by Robolectric tests on real SharedPreferences.**

## Performance

- **Duration:** about 25 min (about 8 min waiting for CI)
- **Tasks:** 2 (both auto/tdd characterization)
- **Files:** 8 test/resource files changed or added, plus deferred-items.md

## Accomplishments

- `DevFestNantesStoreContractTest` is abstract (`createStore()`, `knownSessionId`, `knownSpeakerId`, `knownRoomId`, `releaseStore()` from `@AfterTest`) with 11 structural invariants: both agenda days, agenda sessions within the sessions flow, unique ids, rooms in sortIndex order, `getX(known)` equals the flow element, unknown ids give null or empty, `getSpeakerSessions` equals the filtered sessions, non-blank venue in both languages.
- `MockedStoreContractTest` keeps the former fixture-value assertions and adds `mocked_ids_equal_the_fixture_ids`. `GraphQLStoreContractTest` adds literal recorded values (first session title, speaker name, 8 rooms, 5 partner categories, venue descriptions). Each GraphQL test owns its server and client and closes both in `releaseStore()`.
- `RecordedResponsesHandler` routes by `operationName`, so any call order works; `GetSession` answers only for the recorded id, `GetVenue` picks the French recording on `"language":"fr"`, anything else gets a 500.
- `BookmarksStoreImplTest`, `SessionFiltersServiceImplTest` and `DataCollectionSettingsServiceImplTest` pin `selected_sessions`, `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` (one `{"type":"LANGUAGE","value":"FRENCH"}` string per filter, malformed entries skipped) and `SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS` (enum-name set, nothing enabled and no agreement by default, consent-all and disallow-all drive the Crashlytics and Performance flags). The consent test lives in `:feature:settings` and gets its Crashlytics build id from `src/test/res`.
- No production file and no build file changed.

## Task Commits

1. Task 1: `c4ce747` test(05-05): two-implementation store contract (D-16)
2. Task 2: `39cf00b` test(05-05): pin persisted keys and formats for bookmarks, filters and consent (D-17)

## Verification

- CONTRACT-BOTH-IMPLS-OK: both contract classes present in jvmTest, testAndroidHostTest and iosSimulatorArm64Test results (16 and 20 tests each, 0 failures); inherited test present on iOS; detekt clean.
- PREFS-PINNED: all three persistence test classes ran (5, 4 and 5 tests); `:feature:settings:lintDebug` and detekt clean; DI-GATE-OK.
- TEST-RES-CONFINED, PREFS-KEYS-OK (string-literal diff against origin/main empty for all three production files), empty `git status` for production source and `feature/settings/build.gradle.kts`, no `DataCollectionSettingsServiceImplTest` under `androidApp/`.
- Idempotency: `:core:data:jvmTest --rerun` twice gave identical counts and no failures.
- Mutation check: renaming `selected_sessions` in `BookmarksStoreImpl` failed `BookmarksStoreImplTest`; file restored.
- CI-GREEN-BOTH on `39cf00b`: android.yml run 38867678502 (log contains `testAndroidHostTest`) and ios.yml run 38867678368. Runs on `c4ce747` were cancelled by the next push, not failed.

## TDD Notes

Both tasks characterize existing behavior, so tests passed on first run and there is no separate RED commit. The key-rename mutation check stands in for RED.

## Deviations from Plan

**1. [Rule 3 - Blocking] `MockServer(handler)` is a compile error**
- **Found during:** Task 1
- **Issue:** The plan's `MockServer(RecordedResponsesHandler())` fails with `DEPRECATION_ERROR: Use MockServer.Builder() instead` in apollo-mockserver 0.3.1 on Kotlin 2.4.
- **Fix:** `MockServer.Builder().handler(RecordedResponsesHandler()).build()`. The key_link pattern `MockServer\(RecordedResponsesHandler` therefore no longer matches literally; the link still exists in `GraphQLStoreContractTest.createStore()`.
- **Files modified:** GraphQLStoreContractTest.kt
- **Commit:** c4ce747

**Total deviations:** 1 auto-fixed (1 blocking). **Impact:** none on scope.

## Issues Encountered

None beyond the deviation. The Crashlytics test resource from the revised plan worked as probed: FirebaseApp initialized and the flags toggled on the first run.

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None new. T-5-10 and T-5-11 mitigated by literal-key tests and the string-literal diff; T-5-22 mitigated and proven by TEST-RES-CONFINED; T-5-12 accepted (dummy Firebase project id and key, Robolectric only); T-5-SC no dependency added.

## Deferred Items

Items 6 to 8 appended to `deferred-items.md`: double `apply()` in the two `save()` functions, `SessionFilter` identity equality, and the deprecated `MockServer(handler)` form.

## Next Phase Readiness

Ready for 05-06. TEST-02 is complete: the store contract spans both implementations and every persisted preference format is pinned.

## Self-Check: PASSED

All created files exist on disk; commits c4ce747 and 39cf00b are on the branch (2 measured from the plan ledger); CONTRACT-BOTH-IMPLS-OK, PREFS-PINNED, DI-GATE-OK, TEST-RES-CONFINED, PREFS-KEYS-OK and CI-GREEN-BOTH observed.
