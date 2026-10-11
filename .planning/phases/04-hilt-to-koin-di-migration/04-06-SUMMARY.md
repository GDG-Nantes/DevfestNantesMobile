---
phase: 04-hilt-to-koin-di-migration
plan: 06
subsystem: infra
tags: [koin, di, androidApp, application-initializers, robolectric, koin-startup-test, firebase]

requires:
  - phase: 04-hilt-to-koin-di-migration
    provides: "androidModules list and app-shell appModule (04-05), testDoublesModule/androidTestDoubles (04-02), initKoin composition root (04-01), di-gate with shrink-only Hilt allowlist"
provides:
  - "DevFestNantesApplication and MainActivity on Koin through by inject() only; no Hilt annotation left on them"
  - "appModule: CoroutinesDispatcherProvider single, Timber.Tree single, initializer factories, single(named(\"applicationInitializers\")) ordered list, activity-parameterized ExternalContentService factory, viewModelOf(::HomeViewModel)"
  - "KoinStartupTest: D-12 layer-2 runtime start-up test under Robolectric, resolves every ViewModel and entry-point dependency and asserts the consent-first initializer order"
  - "Robolectric 4.17 as a testImplementation-only dependency"
affects: [04-07]

actuals:
  tokens: 5400
  tasks: 3
  commits: 2

tech-stack:
  added: ["org.robolectric:robolectric 4.17 (testImplementation only)"]
  patterns:
    - "Ordered application initializers as an explicit single(named(...)) listOf(...), never an unordered multibinding"
    - "Activity-scoped helper as factory { (activity: Activity) -> ... } resolved by inject { parametersOf(this) }"
    - "Robolectric test with @Config(application = Application::class) plus FirebaseApp.initializeApp(dummy options) to exercise production Firebase definitions on the JVM"

key-files:
  created:
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt
  modified:
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/DevFestNantesApplication.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModel.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AppModule.kt
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt
    - androidApp/build.gradle.kts
    - gradle/libs.versions.toml
    - scripts/di-gate-hilt-allowlist.txt

key-decisions:
  - "Task 2 decision: robolectric. Robolectric 4.17 exercises the real production Firebase definitions unchanged under a real Application/Context and gives Phase 5 a reusable Robolectric test base. Chosen over mockito (a mocking library in a fakes-first codebase that overrides rather than exercises the definitions), exclude (a documented D-12 gap) and seam (a production refactor beyond D-14's seam list)"
  - "Application initializers are an explicit single(named(\"applicationInitializers\")) listOf(DataSharing, OpenFeedback, Performance); getAll is not used"
  - "ExternalContentService is a factory taking the Activity as a Koin parameter: one instance per MainActivity, Koin keeps no reference"
  - "KoinStartupTest runs with a plain Application via @Config(application = Application::class) so the manifest class does not start Koin and Firebase Performance before the test controls them"

patterns-established:
  - "Test-local fakes only for DataCollectionSettingsService and Timber.Tree; every other Firebase-typed definition runs for real against a dummy FirebaseApp"

requirements-completed: []  # DI-01, DI-02, DI-04, DI-05, DI-06 are also declared by 04-07; marked complete by the last declaring plan (shared-ID gate)

coverage:
  - id: D1
    description: "Application and MainActivity on Koin through by inject() only; the initializer order is fixed by an explicit named list; entry points no longer carry Hilt annotations"
    requirement: DI-04
    verification:
      - kind: other
        ref: "bash scripts/di-gate.sh = DI-GATE-OK; STARTUP-ORDER-OK (startAppStartupTrace L27, initKoin L30, super.onCreate L32)"
        status: pass
      - kind: unit
        ref: "androidApp/src/test/.../di/AndroidGraphVerifyTest.kt (full graph + appModule in isolation)"
        status: pass
      - kind: other
        ref: "debug app on emulator launched through adb install + explicit start, adb exec-out screencap, zero FATAL EXCEPTION"
        status: pass
    human_judgment: true
    rationale: "Consent-before-performance behavior at real process start was not observed with a fresh install and stored consent"
  - id: D2
    description: "KoinStartupTest starts Koin through initKoin under Robolectric and resolves every ViewModel (parameterized ones included, FeedbackFormViewModel against a dummy FirebaseApp) and every entry-point dependency"
    requirement: DI-06
    verification:
      - kind: unit
        ref: "androidApp/src/test/.../di/KoinStartupTest.kt (5 tests: every_view_model_resolves, parameterized_view_models_resolve, feedback_form_view_model_resolves_with_real_remote_config, entry_point_dependencies_resolve, application_initializers_run_in_consent_first_order)"
        status: pass
    human_judgment: false
  - id: D3
    description: "Initializer order DataSharingInitializer, OpenFeedbackInitializer, PerformanceInitializer is asserted (DI-02 edge, T-04-19)"
    requirement: DI-02
    verification:
      - kind: unit
        ref: "androidApp/src/test/.../di/KoinStartupTest.kt#application_initializers_run_in_consent_first_order"
        status: pass
    human_judgment: false
  - id: D4
    description: "Both CI workflows green on the final code HEAD, including the instrumentation job launching MainActivity with Koin as the only live DI"
    requirement: DI-06
    verification:
      - kind: e2e
        ref: "gh runs 38167121229 (Android CI) and 38167121186 (iOS CI) on c37d51d"
        status: pass
    human_judgment: false

duration: ~1h (Task 1 run plus Task 3 run; decision wait excluded)
completed: 2026-10-11
status: complete
plan_head_before: a58cfe0a188e1a7e6324cb03f8c1abc08fb5f7b8
commits: 2
---

# Phase 4 Plan 6: :androidApp shell on Koin and runtime start-up test Summary

**The :androidApp shell, the last Hilt consumer, runs on Koin through `by inject()` with an explicitly ordered `applicationInitializers` list, and a Robolectric-backed `KoinStartupTest` proves the whole Android graph (every ViewModel, parameterized ones, the real FirebaseRemoteConfig definition, and the consent-first initializer order) at runtime in CI; both workflows green on c37d51d.**

## Performance

- **Duration:** ~1h across the two executor runs (decision wait excluded)
- **Tasks:** 3 (2 auto, 1 decision)
- **Files modified:** 19 across both code commits (`git diff --name-only a58cfe0..HEAD`, before this SUMMARY)

## Accomplishments

- `DevFestNantesApplication` and `MainActivity` carry no Hilt annotation. The Application injects the `Timber.Tree`, `CoroutinesDispatcherProvider` and the named initializer list; `MainActivity` injects `AnalyticsService` and `ExternalContentService` with `parametersOf(this)`. `onCreate` order is unchanged: `startAppStartupTrace` (L27), `initKoin` (L30), `super.onCreate()` (L32), Timber planted after, initializer list first touched inside the main-dispatcher coroutine.
- `appModule` now binds the dispatcher provider, Timber tree (debug vs release), the three initializer factories, the ordered `single(named("applicationInitializers"))` list, the activity-parameterized `ExternalContentService` factory, `viewModelOf(::HomeViewModel)` and the existing `OpenFeedbackConfig` factory. `Home` uses `koinViewModel()`.
- Gate allowlist shrank from 23 to 14 lines; `MainActivity.kt` and the four 04-07 deletion targets remain.
- `KoinStartupTest` (5 tests) starts Koin via the production `initKoin(platformModules = androidModules + androidTestDoubles + overrides) { allowOverride(true); androidContext(app) }`, resolves 10 ViewModels, the entry-point dependencies and asserts the initializer order.
- Robolectric 4.17 added, `testImplementation` only (`TEST-DEPS-SCOPED-OK`); Maven Central metadata confirmed 4.17 as the release before it entered the catalog (T-04-20).

## Task Commits

1. **Task 1: :androidApp shell on Koin (Application and MainActivity by inject, ordered initializers, HomeViewModel, app-shell bindings)** - `8db4b29` (feat, pushed)
2. **Task 2: Firebase strategy decision for KoinStartupTest** - no commit (decision only). User chose `robolectric`; rationale in key-decisions above.
3. **Task 3: Runtime start-up test (D-12 layer 2) and initializer-order test** - `c37d51d` (test)

**Plan metadata:** committed with this SUMMARY (docs: complete plan).

## Task 1 verification facts

- Local verify: `assembleDebug`, `assembleRelease` (R8), `testDebugUnitTest`, detekt all BUILD SUCCESSFUL; `DI-GATE-OK`; `STARTUP-ORDER-OK`; acceptance greps pass.
- Emulator launch OK via `adb install` plus explicit activity start and `adb exec-out screencap`, replacing `android run` / `android screen capture` (as in 04-03..04-05).
- Allowlist 23 -> 14 lines.

## Decisions Made

See `key-decisions`. The Task 2 decision was `robolectric` (user-selected). Rejected: mockito, exclude, seam, for the reasons recorded above. Under robolectric, FirebaseApp initialized with dummy `FirebaseOptions` without trouble, so the resume condition "stop if FirebaseApp fails to initialize" never triggered.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] androidRuntimeTypes added to the new appModule isolation test**
- **Found during:** Task 1
- **Issue:** `OpenFeedbackInitializer` needs `Application`, which `androidContext()` supplies at runtime, so the isolation verify flagged it.
- **Fix:** passed `androidRuntimeTypes` as extra types in `app_module_is_complete_given_its_upstream_types`.
- **Files modified:** `androidApp/src/test/.../di/AndroidGraphVerifyTest.kt`
- **Committed in:** `8db4b29`

**2. [Rule 3 - Blocking] Robolectric instantiated the manifest Application**
- **Found during:** Task 3
- **Issue:** the first run failed in all 5 tests with `Default FirebaseApp is not initialized` thrown from `DevFestNantesApplication.onCreate` (`PerformanceInitializer.startAppStartupTrace`), because Robolectric builds the manifest application class. This is a test setup matter, not a Firebase-under-Robolectric failure.
- **Fix:** `@Config(sdk = [34], application = Application::class)` so the test controls Koin and the FirebaseApp itself.
- **Files modified:** `KoinStartupTest.kt`
- **Committed in:** `c37d51d`

**3. [Rule 3 - Blocking] detekt ImportOrdering in KoinStartupTest.kt**
- **Found during:** Task 3
- **Fix:** sorted the import block (java/javax/kotlin last).
- **Committed in:** `c37d51d`

**4. [Rule 3 - Blocking] Wrong ViewModel packages in the first draft of the test**
- **Found during:** Task 3 compile
- **Fix:** corrected imports for `BookmarksViewModel`, `PartnersViewModel`, `SpeakersViewModel`.
- **Committed in:** `c37d51d`

---

**Total deviations:** 4 auto-fixed (1 bug-level test config, 3 blocking). **Impact:** none on scope; the Hilt contingency (deleting the four Hilt leftovers early) was not needed.

### TDD note

Both `tdd="true"` tasks produced tests and implementation together, so there is no separate failing-test commit, same as 04-01..04-05. Flagged rather than fabricated.

## Issues Encountered

- The CI run for `8db4b29` was superseded by the next push (concurrency group); the CI gate is evidenced on the final code HEAD `c37d51d` only (both workflows).
- `android run` and `android screen capture` were replaced by `adb` equivalents on the emulator.

## Authentication Gates

None.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None.

## Threat Flags

None. T-04-19 mitigated (ordered list plus asserted order), T-04-20 mitigated (decision checkpoint, Maven Central metadata read, testImplementation-only), T-04-21 mitigated (emulator launch, KoinStartupTest, CI instrumentation job green, assembleRelease), T-04-22 accepted.

## Next Phase Readiness

- 04-07 can delete the Hilt build plumbing, `core/injection/AppModule.kt`, `AppScope.kt`, `AssistedViewModelUtils.kt`, the empty `ViewModelFactoryProvider`, and the remaining allowlist entries; Koin is the only live DI.
- Robolectric is available for the Phase 5 test retrofit.

## Self-Check: PASSED

- `KoinStartupTest.kt` present; commits `8db4b29` and `c37d51d` present on the branch and pushed.
- `DI-GATE-OK`, `TEST-DEPS-SCOPED-OK`, `CI-GREEN-BOTH c37d51d`.

---
*Phase: 04-hilt-to-koin-di-migration*
*Completed: 2026-10-11*
