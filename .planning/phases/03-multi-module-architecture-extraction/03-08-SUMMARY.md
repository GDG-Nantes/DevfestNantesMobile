---
phase: 03-multi-module-architecture-extraction
plan: 08
subsystem: architecture
tags: [android, gradle, multi-module, compose, hilt, dagger, kotlin]

# Dependency graph
requires:
  - phase: 03-multi-module-architecture-extraction (03-05, 03-06, 03-07)
    provides: ":core:ui" (BookmarksViewModel, theme, shared components, promoted resources), ":feature:venue" / ":feature:about" / ":feature:settings" / ":feature:speakers" (D-16 two-commit extraction pattern, D-17 resource-promotion rule, resources-gate.sh gate)
provides:
  - ":feature:agenda" module (Agenda/AgendaColumn/AgendaPager/AgendaRow/AgendaViewModel/EmptyLayout/SessionFiltersDrawer, utils/{PagerTab,SessionFilter,SessionTypeUtils}, services/SessionFiltersService(Impl), callback-only AgendaRoute entry point)
  - ":feature:session-detail" module (SessionViewModel + assisted factory, SessionLayout/SessionDetails/SessionSpeaker, FeedbackForm/FallbackFeedbackForm/FeedbackFormViewModel, SessionComplexity/SessionType chips, ScheduleSlotUtils, callback-only SessionDetailRoute entry point)
  - OpenFeedbackConfig data class + AppModule @Provides wiring OpenFeedback build-config values into the feature without leaking BuildConfig/OPEN_FEEDBACK_* symbols across the module boundary
  - ":androidApp" reduced to the 18-file D-03 thin app shell (all six ARCH-03 feature modules now extracted)
affects: [03-09 (umbrella iOS/shared thinning, still pending — ARCH-02/ARCH-03 stay blocked until it lands)]

# Actuals (#2632)
actuals:
  tokens: 13548
  tasks: 3
  commits: 4

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "D-16 two-commit-per-module extraction: commit 1 = git mv + new build.gradle.kts + module wiring, packages unchanged (pure move, easy diff review); commit 2 = repackage to the feature's final package + new callback-only Route entry point"
    - "D-02 config-crossing pattern: a small data class (OpenFeedbackConfig) plus a single @Provides in the app's one Hilt AppModule is the only way build-config/secrets values reach a feature module — no BuildConfig import crosses the boundary"
    - "D-03 consumer-owned service placement: SessionFiltersService lives in :feature:agenda (its primary consumer) even though :androidApp's HomeViewModel/AppModule also consume it — app-to-feature dependency is allowed, feature-to-feature is not"
    - "D-07 callback-only Route entry points: AgendaRoute/SessionDetailRoute take only callbacks/state/ViewModel — no NavController, no app symbol imports"
    - "D-17 resource promotion: a resource used by 2+ consumers (app + feature, or feature + feature) moves to :core:ui; single-consumer resources stay local to their feature"

key-files:
  created:
    - feature/agenda/build.gradle.kts
    - feature/agenda/src/main/AndroidManifest.xml
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRoute.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersService.kt
    - feature/session-detail/build.gradle.kts
    - feature/session-detail/src/main/AndroidManifest.xml
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionDetailRoute.kt
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/OpenFeedbackConfig.kt
  modified:
    - settings.gradle.dcl
    - androidApp/build.gradle.kts
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModel.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - core/ui/src/main/res/values/strings.xml
    - core/ui/src/main/res/values-fr/strings.xml

key-decisions:
  - "SessionFiltersService placed in :feature:agenda under package .services per D-03 discretion (app -> feature dependency permitted per RESEARCH Open Question 1)"
  - "SessionComplexity/SessionType chips stayed single-consumer, moved into :feature:session-detail.components per D-08 (only SessionDetails consumes them)"
  - "OpenFeedbackConfig data class + single AppModule @Provides is the sole crossing point for OpenFeedback's two build-config fields (D-02); OpenFeedbackInitializer stays in :androidApp unchanged"

requirements-completed: []  # ARCH-02/ARCH-03 stay BLOCKED — 03-09 (final umbrella-thinning plan) has not produced a SUMMARY yet; requirements.ready-ids reported both blocked, see below

coverage:
  - id: D1
    description: ":feature:agenda extracted with AgendaViewModel and all agenda screens, SessionFiltersService(Impl) consumer-owned, package com.gdgnantes.devfest.feature.agenda; bookmark toggle/favorites filter still route through :core:ui/:core:data"
    requirement: "ARCH-02"
    verification:
      - kind: integration
        ref: ":feature:agenda:assembleDebug + :androidApp:assembleDebug/:assembleRelease + :androidApp:testDebugUnitTest + detekt + lint (Task 1 <verify>)"
        status: pass
      - kind: other
        ref: "PREFS-KEYS-OK (byte-identical SharedPreferences key diff vs origin/main) + RESOURCES-OK (resources-gate.sh)"
        status: pass
    human_judgment: false
  - id: D2
    description: ":feature:session-detail extracted with SessionViewModel + assisted factory, SessionLayout/SessionDetails/SessionSpeaker, FeedbackForm/FallbackFeedbackForm/FeedbackFormViewModel, SessionComplexity/SessionType chips, package com.gdgnantes.devfest.feature.sessiondetail"
    requirement: "ARCH-02"
    verification:
      - kind: integration
        ref: ":feature:session-detail:assembleDebug + :feature:agenda:assembleDebug + :androidApp:assembleDebug/:assembleRelease + :androidApp:testDebugUnitTest + detekt + lint (Task 2 <verify>)"
        status: pass
      - kind: e2e
        ref: "CI-GREEN-BOTH: android.yml run 36154645700, ios.yml run 36154645615 (PR #419) on pushed HEAD da6063d"
        status: pass
    human_judgment: false
  - id: D3
    description: "OpenFeedback build-config values reach :feature:session-detail only through OpenFeedbackConfig provided by :androidApp's single AppModule; no OPEN_FEEDBACK/BuildConfig symbol crosses into feature code (T-03-20 mitigation)"
    requirement: "ARCH-03"
    verification:
      - kind: other
        ref: "git grep -n 'OPEN_FEEDBACK' -- 'feature/*' -> empty (Task 2 acceptance criteria)"
        status: pass
    human_judgment: false
  - id: D4
    description: ":androidApp's Kotlin sources reduced to exactly the 18-file D-03 thin app shell; no feature imports another feature or any com.gdgnantes.devfest.androidapp symbol / NavController type (ARCH-02, D-07)"
    requirement: "ARCH-02"
    verification:
      - kind: other
        ref: "git ls-files androidApp/src/main/java | grep -c '.kt$' -> 18; git grep for com.gdgnantes.devfest.androidapp|NavController|BuildConfig under feature/agenda and feature/session-detail -> empty"
        status: pass
    human_judgment: false
  - id: D5
    description: "Android smoke checkpoint 3 (D-18): Agenda ordering/filters/bookmarks, session detail, Speakers ordering, Venue, About, Settings (incl. Legal, Data collection toggle persistence) and fresh-install consent dialog all match pre-phase behavior"
    requirement: "ARCH-03"
    verification: []
    human_judgment: true
    rationale: "Backstop truth (ordering identity, persisted-state continuity across relaunch, consent dialog timing) requires a human to observe the running app on-device — no automated test asserts this. Approved by the user 2026-09-26 (see Decisions Made)."
duration: not separately timed (continuation of an interrupted session; original executor's elapsed time not recoverable, work spans 2026-09-25 17:11 to 2026-09-26 checkpoint approval)
completed: 2026-09-26
status: complete
---

# Phase 3 Plan 08: Extract :feature:agenda and :feature:session-detail (final two feature modules) Summary

**All six ARCH-03 feature modules now exist — :feature:agenda (with consumer-owned SessionFiltersService) and :feature:session-detail (with OpenFeedbackConfig crossing the build-config boundary via AppModule) extracted last per D-18 ordering, reducing :androidApp to its 18-file D-03 thin shell.**

## Performance

- **Tasks:** 3 (2 `auto` extraction tasks + 1 `checkpoint:human-verify` smoke test)
- **Files modified:** 46 (across the 4 task commits, ledger base `9ad6793`..`da6063d`)
- **Commits:** 4 task commits (2 per feature: pure move, then repackage) + this metadata commit

## Accomplishments

- **`:feature:agenda`** extracted as a `devfest.android.feature` leaf: `Agenda`/`AgendaColumn`/`AgendaPager`/`AgendaRow`/`AgendaViewModel`/`EmptyLayout`/`SessionFiltersDrawer`, `utils/{PagerTab,SessionFilter,SessionTypeUtils}`, and the consumer-owned `services/SessionFiltersService(Impl)` (D-03 discretion: app -> feature dependency permitted). New `AgendaRoute.kt` callback-only entry point; `Home.kt` now calls `AgendaRoute(...)`.
- **`:feature:session-detail`** extracted as a `devfest.android.feature` leaf: `SessionViewModel` (+ assisted factory, factory itself stays in `:androidApp`'s `MainActivity`), `SessionLayout`/`SessionDetails`/`SessionSpeaker`, `FeedbackForm`/`FallbackFeedbackForm`/`FeedbackFormViewModel`, `SessionComplexity`/`SessionType` chips, `ScheduleSlotUtils`. New `SessionDetailRoute.kt` callback-only entry point; `MainActivity.kt` now calls `SessionDetailRoute(viewModel, ...)`.
- **OpenFeedback config crossing (D-02):** new `OpenFeedbackConfig(enabled, projectId)` data class provided by a single `@Provides` in `:androidApp`'s `AppModule`, computed from the app's two `OPEN_FEEDBACK_*` `BuildConfig` fields with the same `toBoolean()` conversion used before the move. `FeedbackFormViewModel` injects it instead of reading `BuildConfig` directly; `FeedbackForm` gets the project id through the ViewModel. `OpenFeedbackInitializer` untouched in the app. `git grep OPEN_FEEDBACK -- feature/*` returns empty (T-03-20 mitigation confirmed).
- **Resource promotion (D-17):** `bookmarked`, `complexity_beginner/intermediate/advanced`, `session_type_conference/quickie/codelab` promoted to `:core:ui` (2+ consumers: agenda + session-detail's chips/layout); `empty_day`, `session_filters_*`, `language_french/english` stayed single-consumer in `:feature:agenda`; `placeholder_session_details`, `session_feedback_label` stayed single-consumer in `:feature:session-detail`. `resources-gate.sh` printed `RESOURCES-OK` after every commit.
- **`:androidApp` reached the D-03 thin-shell target:** `git ls-files androidApp/src/main/java | grep -c '\.kt$'` = 18 — exactly the app-shell files of the module map (Application, MainActivity, Home shell, HomeViewModel, Screen, BottomAppBar, AppModule/AppScope, initializers, logging, ExternalContentService, AssistedViewModelUtils, CoroutinesDispatcherProvider, SuspendUseCase). No feature depends on another feature; no feature references any `com.gdgnantes.devfest.androidapp` symbol or a Navigation Compose controller type.
- **CI green on pushed HEAD `da6063d`:** `android.yml` run [36154645700](https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/36154645700), `ios.yml` run [36154645615](https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/36154645615), PR [#419](https://github.com/GDG-Nantes/DevfestNantesMobile/pull/419) — resolving the ARCH-02 concurrency-edge acceptance criterion.
- **Android smoke checkpoint 3 (D-18) approved** by the user 2026-09-26, covering Agenda (day ordering, filter drawer + bookmark persistence across relaunch, favorites filter), session detail, Speakers (ordering), Venue, About, Settings (Legal, Data collection toggle preserved), and the fresh-install consent dialog — confirming the must-haves backstop ordering truth.

## Task Commits

Each task was committed atomically (D-16 two-commit-per-module pattern):

1. **Task 1: Extract `:feature:agenda` (with SessionFiltersService, D-03)** - `fc887d5` (feat, pure move) then `63a8361` (feat, repackage)
2. **Task 2: Extract `:feature:session-detail` (OpenFeedbackConfig via AppModule, D-02), push, CI, launch sanity** - `0d28780` (feat, pure move) then `da6063d` (feat, repackage)
3. **Task 3: D-18 Android smoke checkpoint 3** - checkpoint task, no code commit (verification only); user approved 2026-09-26

**Plan metadata:** committed alongside this SUMMARY.

_Note: extraction tasks use the project's D-16 two-commit convention (pure move, then repackage) rather than TDD's RED/GREEN/REFACTOR._

## Files Created/Modified

- `feature/agenda/build.gradle.kts` - devfest.android.feature leaf, module-specific deps (accompanist bundle, kotlinx.datetime/serialization.json, M2 material for pull-refresh, material-icons-extended, timber)
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRoute.kt` - callback-only entry point (drawer state + onSessionClick)
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersService.kt` - consumer-owned filter persistence, SharedPreferences keys byte-identical to origin/main
- `feature/session-detail/build.gradle.kts` - devfest.android.feature leaf, Firebase BOM/config-ktx + openfeedback-m3/-viewmodel + material-icons-extended + timber
- `feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionDetailRoute.kt` - callback-only entry point (viewModel, onBackClick, onSocialLinkClick, onFeedbackFormFallbackLinkClick)
- `feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/OpenFeedbackConfig.kt` - data class carrying the two OpenFeedback build-config values across the module boundary
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt` - `@Provides fun openFeedbackConfig(): OpenFeedbackConfig`; imports repointed to `feature.agenda.services` / `feature.sessiondetail`
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt`, `.../home/HomeViewModel.kt` - switched to `AgendaRoute`/feature imports, otherwise unchanged
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt` - switched to `SessionDetailRoute`, kept `ViewModelFactoryProvider` EntryPoint and `assistedViewModel { SessionViewModel.provideFactory(...) }`
- `core/ui/src/main/res/values*/strings.xml` - promoted 2+-consumer strings; `settings.gradle.dcl` - `include(":feature:agenda")`, `include(":feature:session-detail")`

## Decisions Made

- `SessionFiltersService` placed in `:feature:agenda` (D-03 discretion) since its only consumers are `AgendaViewModel` (agenda) and `HomeViewModel`/`AppModule` (app) — app-to-feature dependency is allowed, feature-to-feature is not.
- `SessionComplexity`/`SessionType` chips stayed single-consumer, moved into `:feature:session-detail.components` (D-08) since only `SessionDetails` consumes them.
- OpenFeedback config crossing implemented exactly as scoped: one `OpenFeedbackConfig` data class + one `AppModule` `@Provides`, no `BuildConfig` import in feature code.

## Deviations from Plan

None - plan executed exactly as written. All acceptance criteria for Tasks 1 and 2 passed on the first attempt per the prior executor's session notes; the mid-plan API-rate-limit interruption required no rework (all four production commits and the checkpoint approval were intact on resume).

## Issues Encountered

- The executing session hit an API rate limit mid-plan (after Task 2's CI/launch-sanity verification and the Task 3 checkpoint approval, before SUMMARY creation). No code or verification rework was needed on resume — HEAD, the four commits, and the recorded checkpoint approval were all confirmed intact before continuing.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- All six ARCH-03 feature modules now exist (`:feature:venue`, `:feature:about`, `:feature:settings`, `:feature:speakers`, `:feature:agenda`, `:feature:session-detail`); `:androidApp` is the 18-file navigation/DI thin shell; CI green on both workflows; Android smoke checkpoint 3 approved.
- **ARCH-02 and ARCH-03 are NOT marked complete by this plan.** `gsd-tools query requirements.ready-ids` reports both IDs `blocked`: `03-09-PLAN.md` (the final umbrella/shared-thinning plan) also declares these requirement IDs and has not yet produced a `03-09-SUMMARY.md`. Per the shared-ID gate (#2388), both requirements stay open until 03-09 finishes; do not mark them from this plan.
- `03-09` (iOS/shared umbrella thinning) is the next plan — module-boundary extraction work this plan depended on (D-16 pattern, resources-gate.sh, PREFS-KEYS-OK convention) is complete and reusable.

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-26*
