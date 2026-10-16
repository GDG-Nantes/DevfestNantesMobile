---
phase: 05-test-coverage-retrofit
plan: 06
subsystem: testing
tags: [turbine, viewmodel-testing, main-dispatcher-rule, fake-store, koin, coroutines-dispatcher]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-03 fixtures (SessionFixtures, SpeakerFixtures, PartnerFixtures, roomStubs, buildVenueStub), 05-05 store contract"
provides:
  - "Turbine 1.2.1 as a test-only dependency (Android test bundle and KMP commonTest convention)"
  - "MainDispatcherRule in :core:testing androidMain (caller-chosen TestDispatcher, Unconfined by default)"
  - "FakeDevFestNantesStore: MutableStateFlow-backed, fixture-seeded, with a failure switch; tested on JVM and iOS"
  - "testDoublesModule binds DevFestNantesStore to FakeDevFestNantesStore"
  - "SpeakersViewModelTest (STARTING to SUCCESS with Turbine, pushed speakers, characterized store failure)"
  - "AgendaViewModel with an injected IO dispatcher: coreUiModule binds single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }"
affects: [05-07, 05-08, 05-10]

requirements-completed: [TEST-03]

key-files:
  created:
    - core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/MainDispatcherRule.kt
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeDevFestNantesStore.kt
    - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeDevFestNantesStoreTest.kt
    - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModelTest.kt
  modified:
    - gradle/libs.versions.toml
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/KmpLibraryConventionPlugin.kt
    - core/testing/build.gradle.kts
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModule.kt
    - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModuleTest.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/di/CoreUiModule.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModuleTest.kt

key-decisions:
  - "Turbine pinned at 1.2.1 after re-checking Maven metadata (latest and release are both 1.2.1); added to the Android `test` bundle and to the KMP convention's commonTest"
  - "FakeDevFestNantesStore flows check `failure` when collection starts, getters check it on every call; flows are StateFlow-backed and never complete"
  - "The IO dispatcher is bound in :core:ui (feature-visible), not reused from :androidApp's CoroutinesDispatcherProvider"
  - "agendaModule uses an explicit positional lambda so the named dispatcher is never bound by type"

patterns-established:
  - "ViewModel tests construct the ViewModel directly with fakes, no Koin: MainDispatcherRule + FakeDevFestNantesStore + NoOpPerformanceMonitoring + Turbine"
  - "Choose StandardTestDispatcher in the rule when a test must observe the state before the init coroutine runs"

coverage:
  - id: D1
    description: "Turbine, MainDispatcherRule and FakeDevFestNantesStore drive a real ViewModel test end to end (Speakers STARTING to SUCCESS, pushed list)"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "feature/speakers SpeakersViewModelTest (3 tests) ran in testDebugUnitTest; VM-HARNESS-TRACER-OK"
        status: pass
    human_judgment: false
  - id: D2
    description: "FakeDevFestNantesStore behaves as documented on JVM and iOS, and is the default store of the shared Koin test doubles"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "core/testing FakeDevFestNantesStoreTest present in jvmTest and iosSimulatorArm64Test results; TestDoublesModuleTest asserts the fake type; FAKE-STORE-GREEN"
        status: pass
    human_judgment: false
  - id: D3
    description: "AgendaViewModel receives its IO dispatcher through Koin; production binding is Dispatchers.IO; graph, startup tests and app launch pass"
    requirement: TEST-03
    verification:
      - kind: unit
        ref: "AgendaModuleTest (3 tests incl. production_io_dispatcher_binding_is_dispatchers_io and agenda_view_model_resolves_with_the_named_io_dispatcher), CoreUiModuleTest unchanged, androidApp testDebugUnitTest, detekt, lint, DI-GATE-OK"
        status: pass
      - kind: other
        ref: "android run on emulator-5554: Agenda start destination rendered sessions, day switch and filter drawer opened, no app crash in logcat (ANDROID-LAUNCH-OK, adapted)"
        status: pass
    human_judgment: true
    rationale: "Runtime feel of filtering off the main thread (jank while filtering, bookmark toggle, pull to refresh) is only observable on a device; plan human-check"
  - id: D4
    description: "Test doubles never reach production"
    requirement: TEST-03
    verification:
      - kind: other
        ref: "TESTING-ISOLATED-OK: every project(\":core:testing\") reference is testImplementation/androidTestImplementation and no production source mentions core.testing"
        status: pass
    human_judgment: false
  - id: D5
    description: "CI green on the final code HEAD"
    requirement: TEST-03
    verification:
      - kind: other
        ref: "android.yml run 38871083415 and ios.yml run 38871083498 on 7182cc9, both success"
        status: pass
    human_judgment: false

duration: 15min
completed: 2026-10-16
status: complete
actuals:
  tokens: 16000
  tasks: 3
  commits: 3
plan_head_before: 8a14a2c181a86056d276374287a0fa3444a23bfa
plan_head_after: 7182cc982306e7794bf8988b251d78f08b797ada
commits: 3
---

# Phase 5 Plan 06: ViewModel Test Harness Summary

**Turbine 1.2.1, a `MainDispatcherRule` and a controllable StateFlow-backed `FakeDevFestNantesStore` now drive ViewModel tests (proven on `SpeakersViewModel`), the shared Koin test doubles use the new fake, and `AgendaViewModel` gets its IO dispatcher injected through a named Koin binding.**

## Performance

- **Duration:** about 15 min of work, plus about 10 min of CI waiting
- **Tasks:** 3 (1 tracer, 2 auto/tdd)
- **Files:** 13 production/test/build files changed or added

## Accomplishments

- Turbine is catalog-managed (`turbine = "1.2.1"`), in the Android `test` bundle (every Android module's `testImplementation`) and in the KMP convention's `commonTest`.
- `MainDispatcherRule(dispatcher = UnconfinedTestDispatcher())` sets and resets `Dispatchers.Main`; `:core:testing` androidMain exposes JUnit as `api` for it.
- `FakeDevFestNantesStore` exposes `sessionsState`, `speakersState`, `roomsState`, `partnersState` and a `failure` switch. `agenda` is built from the current sessions via `Agenda.Builder` (day sizes 6 and 4 with the default fixtures).
- `SpeakersViewModelTest` uses a `StandardTestDispatcher` so STARTING is observable before SUCCESS. It also covers pushed speakers and characterizes the store failure.
- `FakeDevFestNantesStoreTest` (7 tests) runs on JVM and iOS simulator. `testDoublesModule` binds the fake, and `fakeDevFestNantesStore()` stays as the mock-store entry point.
- `AgendaViewModel(store, bookmarksStore, performanceMonitoring, sessionFiltersService, ioDispatcher)` filters under `withContext(ioDispatcher)`. `agendaModule` uses an explicit positional lambda with `get(named(IO_DISPATCHER))`.
- `AgendaModuleTest` verifies with `CoroutineDispatcher` as an extra type, pins `Dispatchers.IO` as the production binding, and resolves `AgendaViewModel` on a test dispatcher. `CoreUiModuleTest` is unchanged and still green.

## Task Commits

1. Task 1 (tracer): `fc5c4d9` test(05-06): ViewModel harness tracer (Turbine, MainDispatcherRule, FakeDevFestNantesStore)
2. Task 2: `c1895a9` test(05-06): FakeDevFestNantesStore backs the shared Koin test doubles
3. Task 3: `7182cc9` refactor(05-06): inject AgendaViewModel's IO dispatcher through Koin (D-09)

## Verification

- VM-HARNESS-TRACER-OK (re-run as the tracer gate before expansion; three SpeakersViewModelTest tests in the results), detekt clean.
- FAKE-STORE-GREEN: `FakeDevFestNantesStoreTest` present in jvmTest and iosSimulatorArm64Test results; `:core:ui` and `:androidApp` unit tests green on the new doubles.
- Task 3 gradle command (`:core:ui`, `:feature:agenda` tests and assembleDebug, `:androidApp` tests and assembleDebug, detekt, lint) green; DI-GATE-OK; TESTING-ISOLATED-OK; `git grep Dispatchers.IO -- feature/agenda/src/main` prints nothing.
- App launch: debug APK installed on `emulator-5554` and started with `android run --apks ...`; the Agenda start destination rendered sessions, the second day tab and the filter drawer worked, and logcat shows no crash of the app (the only FATAL line belongs to the `android` CLI's own instrumentation server).
- CI-GREEN-BOTH on `7182cc9`: android.yml run 38871083415 and ios.yml run 38871083498. Runs on `fc5c4d9` and `c1895a9` were cancelled by the next push, not failed.

## TDD Notes

Task 1 is the tracer (production-quality harness plus its first consumer), Tasks 2 and 3 add tests around code that exists or changes behavior-neutrally, so there is no separate RED commit. The characterization test for the store failure passed on first run, which is the expected state for pinned existing behavior.

## Deviations from Plan

**1. [Rule 3 - Blocking] App launch command adapted**
- **Found during:** Task 3
- **Issue:** The plan's `android run && android layout | grep '...androidapp.dev'` assumes a `.dev` package suffix and a bare `android run`. The debug APK installs as `com.gdgnantes.devfest.mobile.androidapp` (no suffix), and `android run` needs `--apks` (05-02 finding).
- **Fix:** Ran `android run --apks androidApp/build/outputs/apk/debug/androidApp-debug.apk --device emulator-5554`, then checked the layout for the Agenda content and the logcat for crashes of the app.
- **Files modified:** none
- **Commit:** n/a

**Total deviations:** 1 auto-fixed (1 blocking). **Impact:** none on scope.

## Issues Encountered

None. `viewModel { ... }` with `get(named(IO_DISPATCHER))` resolved on the first run, in the module verify test, the three graph tests and on the device.

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None new. T-5-13 mitigated and proven by TESTING-ISOLATED-OK; T-5-14 mitigated by the module verify, the resolution tests, the full Android graph and startup tests (production `allowOverride(false)`) and the launch check; T-5-SC mitigated by the exact pin after the Maven metadata re-check, test configurations only.

## Deferred items for the log

For 05-07 Task 1 to append to `deferred-items.md` (this plan did not edit it, D-10):

9. **`SpeakersViewModel` does not handle a failing `store.speakers` flow.** The collection in `init` has no `catch`, so the error escapes `viewModelScope` as an uncaught exception (the app would crash) and `uiState` never leaves `STARTING`; `UiState.ERROR` is never used. Pinned by `SpeakersViewModelTest.store_failure_is_characterized`: with `FakeDevFestNantesStore.failure` set, `uiState` stays `STARTING`, `speakers` stays empty, and the test run fails with the original `IllegalStateException("boom")` (asserted with `assertFailsWith` around `runTest`).

## Next Phase Readiness

Ready for 05-07. The harness is in place: `MainDispatcherRule`, `FakeDevFestNantesStore` (use `mainDispatcherRule.dispatcher` as the `ioDispatcher` argument of `AgendaViewModel`), `NoOpPerformanceMonitoring` and Turbine. Human-check still open for the verifier: use the debug app, open the Agenda, switch days, apply then clear a filter, toggle a bookmark and pull to refresh; expect unchanged behavior and no jank.

## Self-Check: PASSED

All created files exist on disk; commits fc5c4d9, c1895a9 and 7182cc9 are on the branch (3 measured from the plan ledger); VM-HARNESS-TRACER-OK, FAKE-STORE-GREEN, DI-GATE-OK, TESTING-ISOLATED-OK, the adapted launch check and CI-GREEN-BOTH observed.
