---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 17
subsystem: android-adoption
tags: [android, adoption, classification, koin, compose]
requires:
  - phase: 07-01
    provides: ClassifySessionUseCase in :core:domain
  - phase: 07-15
    provides: AgendaViewModel on ObserveAgendaUseCase
  - phase: 07-16
    provides: Android SessionFiltersStore in :core:data
provides:
  - AgendaViewModel.isTalk(session) backed by ClassifySessionUseCase
  - AgendaRow(isTalk) bookmark star driven by the shared classification
  - MainActivity session navigation gated by ClassifySessionUseCase.isNavigable
affects: [07-18, 07-19]
tech-stack:
  added: []
  patterns:
    - "Shared use case reaches composables through ViewModel method + lambda, never koinInject"
key-files:
  created: []
  modified:
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/Agenda.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaPager.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaColumn.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRow.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - documentation/ARCHITECTURE.md
  deleted:
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/utils/SessionTypeUtils.kt
key-decisions:
  - "isTalk is a synchronous ViewModel method passed as a lambda down to AgendaRow(isTalk: Boolean); no composable resolves a use case from Koin"
  - "MainActivity resolves ClassifySessionUseCase with by inject() (allowlisted locator site); eventSessionOpened stays outside the navigation condition"
requirements-completed: [SHARED-04, SHARED-06]
duration: 20 min
completed: 2026-10-29
status: complete
actuals:
  tokens: 6000
  tasks: 1
  commits: 1
plan_head_before: 58f13630c9e1ba1a7eb6b883115161da7966c833
plan_head_after: 3ff92ad050ddd821e8daef5b5e0070c963184c84
---

# Phase 7 Plan 17: Classification through the shared use case Summary

The agenda bookmark star and the session-detail navigation in MainActivity now come from `ClassifySessionUseCase`, and the Android-only `SessionType.isService()` copy (`SessionTypeUtils.kt`) is deleted. Behavior is unchanged.

## Accomplishments

- `AgendaViewModel` gains `classifySession: ClassifySessionUseCase` (fifth constructor parameter, `viewModel { AgendaViewModel(get(), get(), get(), get(), get()) }`) and `fun isTalk(session)`.
- `isTalk` is threaded as a lambda through `Agenda` -> `AgendaLayout` -> `AgendaPager` -> `AgendaColumn` -> `AgendaRow(isTalk: Boolean)`; the innermost row replaces `session.type?.isService() == false` with `isTalk`; previews pass `isTalk = true`.
- `MainActivity` gets `classifySession by inject()` and navigates only when `isNavigable(session)`; `analyticsService.eventSessionOpened(session.id)` is still called for every click (one line, outside the condition).
- Tests (wiring-only, D-18): `ClassifySessionUseCase()` added to every `AgendaViewModel` construction (AgendaViewModelTest x2, three VM-based characterizations), `ClassifySessionUseCase` added to `AgendaModuleTest` extra types, and `SessionClassificationCharacterizationTest` now evaluates the shared use case; its expected sets and assertions are unchanged.
- `documentation/ARCHITECTURE.md`: one bullet extended to say how Android consumes the classification.

## Verification

- `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :feature:agenda:compileDebugAndroidTestKotlin`: pass
- `bash scripts/di-gate.sh`: `DI-GATE-OK`; `bash scripts/kmp-nc-version-gate.sh`: `KMPNC-VERSION-OK 1.0.6`
- `./gradlew detekt lint`: exit 0
- `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`: pass
- Acceptance: `SessionTypeUtils.kt` absent; no `.isService()` / deleted-extension import in feature or androidApp main sources; `classifySession.isNavigable` present in MainActivity; `eventSessionOpened` appears once.

## Deviations from Plan

None - plan executed exactly as written. (`androidApp` reaches `:core:domain` transitively through `:shared`'s `api(project(":core:domain"))`, so no build file change was needed. `documentation/ARCHITECTURE.md` was edited per the AGENTS.md rule to update docs with the change, confined to the classification bullet.)

**Total deviations:** 0.

## Known Stubs

None.

## Threat Flags

None. No new network, auth or trust-boundary surface; T-07-25 (behavior drift) mitigated by the unchanged classification table, T-07-26 by constructor injection and lambdas only (DI gate green).

## Task Commits

1. Task 1: `3ff92ad0` refactor(07-17): drive the agenda star and session navigation from the shared classification

## Next Phase Readiness

Ready for 07-19 (D-12 screenshots cover the star and navigation). 07-18 (speakers/venue) is independent.

## Self-Check: PASSED

- All modified files exist; `SessionTypeUtils.kt` confirmed deleted.
- Commit `3ff92ad0` is an ancestor of HEAD; `git rev-list --count` from the plan base = 1.
