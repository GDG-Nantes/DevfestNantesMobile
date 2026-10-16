---
phase: 04-hilt-to-koin-di-migration
plan: 05
subsystem: infra
tags: [koin, di, feature-session-detail, core-ui, koin-parameters, firebase-remote-config, hilt-coexistence]

requires:
  - phase: 04-hilt-to-koin-di-migration
    provides: "di-gate with shrink-only Hilt allowlist, Koin-parameter ViewModel pattern (04-04), testDoublesModule (04-02), androidModules list"
provides:
  - "sessionDetailModule: explicit-lambda parameterized SessionViewModel, viewModelOf FeedbackFormViewModel, FirebaseRemoteConfig factory (300 s fetch interval)"
  - "SessionDetailRoute(modifier, sessionId: String, viewModel = koinViewModel { parametersOf(sessionId) }, ...)"
  - "appModule (androidApp di/AppModule.kt): OpenFeedbackConfig factory from BuildConfig"
  - "coreUiModule: viewModelOf BookmarksViewModel, resolved by koinViewModel() in AgendaRow and SessionLayout"
affects: [04-06, 04-07]

actuals:
  tokens: 6900
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "App-shell Koin appModule for bindings that need BuildConfig; only the needed values cross into the feature"
    - "Side-effecting SDK handle (FirebaseRemoteConfig) declared as factory so the side effect runs per ViewModel creation"
    - "Three-module single commit for a shared ViewModel flip (core:ui + both call sites)"

key-files:
  created:
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/di/SessionDetailModule.kt
    - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/di/SessionDetailModuleTest.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/di/CoreUiModule.kt
    - core/ui/src/test/java/com/gdgnantes/devfest/core/ui/di/CoreUiModuleTest.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AppModule.kt
  modified:
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionViewModel.kt
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionDetailRoute.kt
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFormViewModel.kt
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackForm.kt
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionLayout.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRow.kt
    - core/ui/build.gradle.kts
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/BookmarksViewModel.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AndroidModules.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/AndroidGraphVerifyTest.kt
    - scripts/di-gate-hilt-allowlist.txt

key-decisions:
  - "SessionViewModel is declared with the explicit positional lambda form, as SpeakerViewModel in 04-04"
  - "FirebaseRemoteConfig is a factory inside sessionDetailModule (not single) so setConfigSettingsAsync(300 s) runs once per FeedbackFormViewModel creation, as the unscoped Hilt provider did"
  - "OpenFeedbackConfig lives in the app-shell Koin appModule; only enabled + projectId cross into the feature, other OPEN_FEEDBACK_* stay in the app"
  - "ViewModelFactoryProvider is left as an empty interface; it is deleted with AssistedViewModelUtils.kt in 04-07"

patterns-established:
  - "Config data class built from BuildConfig constants: add Boolean/String as extraTypes in the full-graph verify test with a reason comment"

requirements-completed: []  # DI-01, DI-02, DI-04, DI-05 are also declared by 04-06/04-07; marked complete by the last declaring plan (shared-ID gate)

coverage:
  - id: D1
    description: ":feature:session-detail on Koin: plain SessionViewModel resolved from the session id by Koin parameters, SessionDetailRoute(sessionId), feedback ViewModel with RemoteConfig factory and OpenFeedbackConfig from appModule"
    requirement: DI-02
    verification:
      - kind: unit
        ref: "feature/session-detail/src/test/.../di/SessionDetailModuleTest.kt#session_detail_module_graph_is_complete_given_upstream_bindings"
        status: pass
      - kind: unit
        ref: "feature/session-detail/src/test/.../di/SessionDetailModuleTest.kt#session_view_model_resolves_with_the_session_id_parameter"
        status: pass
      - kind: unit
        ref: "androidApp/src/test/.../di/AndroidGraphVerifyTest.kt (4 tests)"
        status: pass
      - kind: other
        ref: "debug app on emulator-5554: tapping the 'La notification' agenda row opened that session's detail, zero FATAL EXCEPTION in logcat"
        status: pass
    human_judgment: true
    rationale: "Feedback section visibility (OpenFeedback disabled by BuildConfig) and the 300 s fetch interval are runtime/remote behavior; the feedback section was not scrolled into view"
  - id: D2
    description: ":core:ui BookmarksViewModel on Koin, flipped together with AgendaRow and SessionLayout in one commit; row and detail FAB share one bookmark state"
    requirement: DI-05
    verification:
      - kind: unit
        ref: "core/ui/src/test/.../di/CoreUiModuleTest.kt#core_ui_module_graph_is_complete_given_upstream_bindings"
        status: pass
      - kind: unit
        ref: "core/ui/src/test/.../di/CoreUiModuleTest.kt#bookmarks_view_model_resolves_on_the_shared_test_doubles"
        status: pass
      - kind: other
        ref: "debug app: bookmarking the row showed the checked FAB in the detail; unbookmarking with the FAB showed the plus icon on the row after back; state left unbookmarked"
        status: pass
    human_judgment: true
    rationale: "Cross-screen bookmark consistency was observed once in both directions; the favorites filter was not driven"
  - id: D3
    description: "DI gate stays green and the allowlist shrinks (29 -> 26 -> 23); every :core and :feature module resolves ViewModels through Koin; both CI workflows green on the pushed HEAD"
    requirement: DI-04
    verification:
      - kind: other
        ref: "bash scripts/di-gate.sh = DI-GATE-OK after each commit; NO-HILT-VM-IN-MODULES"
        status: pass
      - kind: e2e
        ref: "gh runs 38161899123 (Android CI) and 38161899202 (iOS CI) on 0501b86"
        status: pass
    human_judgment: false

duration: 23 min
completed: 2026-10-11
status: complete
plan_head_before: f13bfaf5cf7eb514ab6d4d58fd02d4dcc3d2e8b8
commits: 2
---

# Phase 4 Plan 5: :feature:session-detail and :core:ui on Koin Summary

**The session detail screen takes a session id and resolves its ViewModel with `koinViewModel { parametersOf(sessionId) }`, OpenFeedbackConfig comes from a new Koin `appModule`, and `BookmarksViewModel` flipped to Koin together with its agenda and session-detail call sites; both CI workflows green on 0501b86.**

## Performance

- **Duration:** 23 min (Gradle builds, one emulator walkthrough, CI watch)
- **Started:** 2026-10-11T17:46:36Z
- **Completed:** 2026-10-11T18:09Z
- **Tasks:** 2
- **Files modified:** 18 production, test and build files (`git diff --name-only f13bfaf..HEAD` before this SUMMARY)

## Accomplishments

- `SessionViewModel(store, performanceMonitoring, sessionId)` is a plain class; the assisted factory interface and the factory-building companion are gone. `sessionDetailModule` declares it by explicit lambda and `FeedbackFormViewModel` by `viewModelOf`.
- `FirebaseRemoteConfig` is a `factory` in `sessionDetailModule` applying `setConfigSettingsAsync` with a 300 s minimum fetch interval on every resolution; the Hilt `config()` and `openFeedbackConfig()` providers and the interval constant are deleted from the Hilt `AppModule`.
- `appModule` (new `androidApp/.../di/AppModule.kt`) builds `OpenFeedbackConfig` from `BuildConfig.OPEN_FEEDBACK_ENABLED.toBoolean()` and `OPEN_FEEDBACK_PROJECT_ID`; no `OPEN_FEEDBACK` reference exists under `feature/`.
- `MainActivity`'s session destination keeps the `getString("sessionId")!!` read and passes only `sessionId` and the callbacks; `ViewModelFactoryProvider` is an empty interface (removed in 04-07 with `AssistedViewModelUtils.kt`).
- `coreUiModule` declares `BookmarksViewModel`; `AgendaRow` and `SessionLayout` use `koinViewModel()`. `CoreUiModuleTest` resolves it on `:core:testing`'s `testDoublesModule` (DI-05 cross-module use).
- `androidModules = [coreUiModule, venueModule, aboutModule, settingsModule, speakersModule, agendaModule, sessionDetailModule, appModule]`. No `hiltViewModel` remains under `feature/` or `core/`.

## Allowlist line count per commit

| Commit | Allowlist lines |
|--------|-----------------|
| start (04-04 end) | 29 |
| `fd967d6` session-detail | 26 (-SessionViewModel.kt, -FeedbackFormViewModel.kt, -FeedbackForm.kt) |
| `0501b86` core:ui flip | 23 (-BookmarksViewModel.kt, -AgendaRow.kt, -SessionLayout.kt) |

## Task Commits

1. **Task 1: :feature:session-detail on Koin (Koin-parameter SessionViewModel, RemoteConfig factory, OpenFeedbackConfig from appModule)** - `fd967d6` (feat)
2. **Task 2: :core:ui BookmarksViewModel on Koin with its agenda and session-detail call sites** - `0501b86` (feat); `git show --stat` lists `core/ui/`, `feature/agenda/` and `feature/session-detail/` together

**Plan metadata:** committed with this SUMMARY (docs: complete plan).

## Decisions Made

See `key-decisions` above.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] AndroidGraphVerifyTest flagged OpenFeedbackConfig's primitive constructor parameters**
- **Found during:** Task 1 (`:androidApp:testDebugUnitTest`)
- **Issue:** `full_android_graph_is_complete` failed with `MissingKoinDefinitionException` for `enabled: Boolean` in the `OpenFeedbackConfig` factory; the plan anticipated this ("add `Boolean::class` only if verify flags it").
- **Fix:** added a `configValueTypes = listOf(Boolean::class, String::class)` list with a reason comment, appended to the full-graph test only (the negative-control test keeps the stricter list, so it still bites).
- **Files modified:** `androidApp/src/test/.../di/AndroidGraphVerifyTest.kt`
- **Verification:** all four AndroidGraphVerifyTest tests pass; detekt green.
- **Committed in:** `fd967d6`

**2. [Rule 3 - Blocking] detekt ImportOrdering in SessionLayout.kt**
- **Found during:** Task 2 (`detekt`)
- **Issue:** the new `org.koin...koinViewModel` import was placed after the `core.ui.R as CoreUiR` alias import; detekt requires aliases last.
- **Fix:** moved the koin import above the alias.
- **Files modified:** `feature/session-detail/.../SessionLayout.kt`
- **Verification:** detekt, lint, assembleDebug green; CI green.
- **Committed in:** `0501b86`

---

**Total deviations:** 2 auto-fixed (1 bug-level test config, 1 blocking lint). **Impact:** none on scope.

### TDD note

Both tasks carry `tdd="true"`. As in 04-01..04-04, tests and implementation were produced together, so there is no separate failing-test commit; flagged here rather than fabricated. The parameter-binding test exercises `parametersOf` resolution on the JVM with `Dispatchers.setMain`.

## Issues Encountered

- The `fd967d6` CI runs were superseded by the next push (concurrency group); the CI gate is evidenced on the final HEAD `0501b86` only. Task 1 has local verification (tests, detekt, DI gate).
- `android screen capture` required `android update`, so screenshots were taken with `adb exec-out screencap`; `android run` was replaced by `adb install` of the debug APK plus an explicit activity start (as in 04-03/04-04). Launch verified through `dumpsys` resumed activity and screenshots, zero `FATAL EXCEPTION`.
- Speaker photos are not loaded (network blocks raw.githubusercontent.com); not a regression.

## Authentication Gates

None.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None.

## Threat Flags

None beyond the plan's threat model. T-04-15 (only flag + project id cross, no `OPEN_FEEDBACK` under `feature/`), T-04-16 (factory + `setConfigSettingsAsync` + `300L` kept), T-04-17 (explicit positional lambda, resolution test, tapped session shown on emulator) and T-04-18 (one Koin BookmarksStore, single three-module commit, row vs FAB consistency observed) are mitigated.

## Next Phase Readiness

- Ready for 04-06: only `:androidApp` still uses Hilt (MainActivity annotation and injected fields, `HomeViewModel`, initializers, Hilt `AppModule` bridges). `appModule` is in place for the remaining app-shell bindings.
- Allowlist at 23 lines. Remember to `git add` new files before running `scripts/di-gate.sh`, and keep the removed framework's name out of comments in migrated files.
- Remaining manual check (human_judgment D1/D2): open a session's feedback section (OpenFeedback disabled by BuildConfig shows the fallback), and use the favorites filter after toggling bookmarks.

---
*Phase: 04-hilt-to-koin-di-migration*
*Completed: 2026-10-11*

## Self-Check: PASSED

- Created files exist: SessionDetailModule.kt, SessionDetailModuleTest.kt, CoreUiModule.kt, CoreUiModuleTest.kt, androidApp di/AppModule.kt.
- Commits fd967d6 and 0501b86 present and pushed; `commits: 2` measured from `plan_head_before` f13bfaf.
- DI-GATE-OK, NO-HILT-VM-IN-MODULES, emulator launch and bookmark checks, CI-GREEN-BOTH 0501b86 (runs 38161899123, 38161899202).
