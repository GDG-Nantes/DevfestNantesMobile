---
phase: 04-hilt-to-koin-di-migration
plan: 04
subsystem: infra
tags: [koin, di, feature-speakers, feature-agenda, koin-parameters, hilt-coexistence]

requires:
  - phase: 04-hilt-to-koin-di-migration
    provides: "di-gate with shrink-only Hilt allowlist, feature-migration recipe, Hilt-to-Koin bridge pattern, aboutModule/settingsModule/venueModule"
provides:
  - "speakersModule: viewModelOf(SpeakersViewModel) plus parameterized SpeakerViewModel via explicit lambda definition"
  - "SpeakerDetailRoute(modifier, speakerId: String, viewModel = koinViewModel { parametersOf(speakerId) }, ...)"
  - "agendaModule: viewModelOf(AgendaViewModel) plus single SessionFiltersServiceImpl bound to SessionFiltersService"
  - "Hilt AppModule bridge for SessionFiltersService so the still-Hilt HomeViewModel shares the Koin instance"
affects: [04-05, 04-06, 04-07]

actuals:
  tokens: 9000
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Parameterized ViewModel: viewModel { (id: String) -> VM(get(), get(), id) } resolved with koinViewModel { parametersOf(id) } as a route default parameter"
    - "Assisted factory and provideFactory companion deleted; the nav-argument read stays in MainActivity"

key-files:
  created:
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/di/SpeakersModule.kt
    - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/di/SpeakersModuleTest.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModule.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/di/AgendaModuleTest.kt
  modified:
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakerViewModel.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakersRoute.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt
    - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/Agenda.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersService.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AndroidModules.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - scripts/di-gate-hilt-allowlist.txt

key-decisions:
  - "SpeakerViewModel is declared with the explicit positional lambda form, so the speaker id can never bind to a wrong parameter by type"
  - "AppModule becomes a Kotlin object once its only abstract binding (SessionFiltersService) turned into a bridge; Hilt supports @Module object and all provider semantics are unchanged"
  - "AgendaRow keeps its hiltViewModel BookmarksViewModel resolver until the 04-05 three-module flip"

patterns-established:
  - "A route that needs a nav-argument ViewModel takes the id and resolves it as a default parameter with koinViewModel { parametersOf(id) }; the owner stays the back-stack entry"

requirements-completed: []  # DI-01, DI-02, DI-04 are also declared by 04-05..04-07; marked complete by the last declaring plan (shared-ID gate)

coverage:
  - id: D1
    description: ":feature:speakers on Koin: plain SpeakerViewModel resolved from the id by Koin parameters, speakers list on viewModelOf, MainActivity passes only the id"
    requirement: DI-02
    verification:
      - kind: unit
        ref: "feature/speakers/src/test/.../di/SpeakersModuleTest.kt#speakers_module_graph_is_complete_given_upstream_bindings"
        status: pass
      - kind: unit
        ref: "feature/speakers/src/test/.../di/SpeakersModuleTest.kt#speaker_view_model_resolves_with_the_speaker_id_parameter"
        status: pass
      - kind: other
        ref: "debug app on emulator-5554: tapping Benjamin Cavy in the Speakers list opened Benjamin Cavy's detail with his own talk, zero FATAL EXCEPTION in logcat"
        status: pass
    human_judgment: true
    rationale: "Automation checked one speaker end to end; tapping a session from the detail and going back twice was not driven"
  - id: D2
    description: ":feature:agenda on Koin with one SessionFiltersService single shared with the still-Hilt HomeViewModel through a KoinPlatform bridge; persisted preference key literals unchanged"
    requirement: DI-04
    verification:
      - kind: unit
        ref: "feature/agenda/src/test/.../di/AgendaModuleTest.kt#agenda_module_graph_is_complete_given_upstream_bindings"
        status: pass
      - kind: unit
        ref: "androidApp/src/test/.../di/AndroidGraphVerifyTest.kt (4 tests)"
        status: pass
      - kind: other
        ref: "string-literal diff of SessionFiltersService.kt vs origin/main = PREFS-KEYS-OK"
        status: pass
      - kind: other
        ref: "debug app: selected the anglais filter, force-stopped, relaunched; checkbox still set and the toolbar filter icon shown filled, zero FATAL EXCEPTION"
        status: pass
    human_judgment: true
    rationale: "Persistence and indicator were observed once on the emulator; bookmark toggling from an agenda row was not driven"
  - id: D3
    description: "DI gate stays green and the allowlist shrinks (35 -> 32 -> 29); both CI workflows green on the pushed HEAD"
    requirement: DI-04
    verification:
      - kind: other
        ref: "bash scripts/di-gate.sh = DI-GATE-OK after each commit"
        status: pass
      - kind: e2e
        ref: "gh runs 38158730120 (Android CI) and 38158730169 (iOS CI) on 53a95b4"
        status: pass
    human_judgment: false

duration: 1h 35m
completed: 2026-10-11
status: complete
plan_head_before: 158c7fb1ac67b7cfd58adf9864f1351431f68570
commits: 2
---

# Phase 4 Plan 4: :feature:speakers and :feature:agenda on Koin Summary

**The first assisted-injection ViewModel now uses Koin parameters (`SpeakerDetailRoute(speakerId)` resolves `koinViewModel { parametersOf(speakerId) }`), and the agenda runs on Koin with its filters service a `single` bridged into the still-Hilt `HomeViewModel`; both CI workflows green on 53a95b4.**

## Performance

- **Duration:** 1h 35m (mostly Gradle builds and CI waits on a loaded host)
- **Started:** 2026-10-11T15:45:36Z
- **Completed:** 2026-10-11T17:20Z
- **Tasks:** 2
- **Files modified:** 15 production and test files plus the allowlist (`git diff --name-only 158c7fb..HEAD`: 18 before this SUMMARY)

## Accomplishments

- `SpeakerViewModel(store, performanceMonitoring, speakerId)` is a plain class: the assisted factory interface, `provideFactory` companion and the Dagger imports are gone; `MainActivity.ViewModelFactoryProvider` no longer declares a speaker factory and the speaker destination passes only `speakerId` and the callbacks (the `getString("speakerId")!!` read is unchanged).
- `speakersModule` declares the speaker detail ViewModel by explicit lambda `viewModel { (speakerId: String) -> SpeakerViewModel(get(), get(), speakerId) }`; `SpeakersViewModel` uses `viewModelOf`.
- `agendaModule` declares `viewModelOf(::AgendaViewModel)` and `single { SessionFiltersServiceImpl(get()) } bind SessionFiltersService::class`; the Hilt `AppModule` provider for `SessionFiltersService` is a `KoinPlatform` bridge, so Agenda (Koin) and Home (Hilt) observe one filters flow. `AgendaRow.kt` untouched.
- `androidModules = [venueModule, aboutModule, settingsModule, speakersModule, agendaModule]`.

## Allowlist line count per commit

| Commit | Allowlist lines |
|--------|-----------------|
| start (04-03 end) | 35 |
| `d0b7011` speakers | 32 (-SpeakerViewModel.kt, -SpeakersViewModel.kt, -Speakers.kt) |
| `53a95b4` agenda | 29 (-AgendaViewModel.kt, -Agenda.kt, -SessionFiltersService.kt) |

## Task Commits

1. **Task 1: :feature:speakers on Koin (Koin-parameter SpeakerViewModel)** - `d0b7011` (feat)
2. **Task 2: :feature:agenda on Koin** - `53a95b4` (feat)

**Plan metadata:** committed with this SUMMARY (docs: complete plan).

## Decisions Made

See `key-decisions` above.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] detekt UtilityClassWithPublicConstructor on AppModule**
- **Found during:** Task 2 (`detekt` in the plan's verify)
- **Issue:** removing the only `@Binds` left `AppModule` an abstract class whose body is just a companion of providers; detekt failed `:androidApp:detekt`.
- **Fix:** converted `abstract class AppModule { companion object { ... } }` to `object AppModule { ... }` (providers unchanged, one indent level removed). No other file references `AppModule.Companion`.
- **Files modified:** `androidApp/.../core/injection/AppModule.kt`
- **Verification:** detekt, lint, `:androidApp:assembleDebug`, `AndroidGraphVerifyTest` and CI green.
- **Committed in:** `53a95b4`

---

**Total deviations:** 1 auto-fixed (1 blocking). **Impact on plan:** none on scope.

### TDD note

Both tasks carry `tdd="true"`. As in 04-01..04-03, tests and implementation were produced together, so there is no separate failing-test commit; flagged here rather than fabricated. The parameter-binding test (`speaker_view_model_resolves_with_the_speaker_id_parameter`) exercises `parametersOf` resolution on the JVM with `Dispatchers.setMain`.

## Issues Encountered

- `android run` with no arguments fails; the debug APK was installed with `--apks androidApp/build/outputs/apk/debug/androidApp-debug.apk` and the plan's `grep` on `android layout` (no package field) was replaced by checking the resumed activity through `dumpsys` plus screenshots (as in 04-03).
- The d0b7011 CI runs were cancelled by the next push (concurrency group); the plan's CI gate is evidenced on the final HEAD 53a95b4 only. Task 1's own commit therefore has local verification (tests, detekt, DI gate) but no separate CI result.
- Speaker photos are not loaded (network blocks raw.githubusercontent.com); not a regression.
- Emulator state: app data was cleared at the start (consent dialog shown on fresh install), consent was granted, and the language filter set during the check was cleared afterwards.

## Authentication Gates

None.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None.

## Threat Flags

None beyond the plan's threat model. T-04-12 (explicit positional parameter binding, resolution test, tapped speaker shown), T-04-13 (single plus bridge, PREFS-KEYS-OK, filter persistence observed) and T-04-14 (Agenda start destination launched on emulator, CI green) are mitigated.

## Next Phase Readiness

- Ready for 04-05 (bookmarks and `:core:ui` three-module flip): `AgendaRow` still resolves `BookmarksViewModel` through Hilt and must flip together with the other bookmark consumers.
- Allowlist at 29 lines. Remember to `git add` new files before running `scripts/di-gate.sh`, and keep the removed framework's name out of comments in migrated files.
- Remaining manual check (human_judgment D1/D2): tap a session from a speaker detail and go back twice; toggle a bookmark from an agenda row.

---
*Phase: 04-hilt-to-koin-di-migration*
*Completed: 2026-10-11*

## Self-Check: PASSED

- Created files exist: SpeakersModule.kt, SpeakersModuleTest.kt, AgendaModule.kt, AgendaModuleTest.kt.
- Commits d0b7011 and 53a95b4 present and pushed; `commits: 2` measured from `plan_head_before` 158c7fb.
- DI-GATE-OK, PREFS-KEYS-OK, emulator launch/speaker/filter checks, CI-GREEN-BOTH 53a95b4 (runs 38158730120, 38158730169).
