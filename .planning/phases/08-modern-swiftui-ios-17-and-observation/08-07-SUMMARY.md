---
phase: 08-modern-swiftui-ios-17-and-observation
plan: 07
subsystem: ios-ui
tags: [ios, swiftui, observation, environment, swift-testing, persistence]

requires:
  - phase: 08-modern-swiftui-ios-17-and-observation
    provides: "KotlinStreamBridge and FakeStream (08-02), DataLoadTracing and RecordingDataLoadTracing (08-05), SpeakerDetails call site (08-06)"
provides:
  - "AgendaSource port and LiveAgendaSource (sessions and rooms over KotlinStreamBridge, lazy store)"
  - "AgendaViewModel on Observation: one shared instance owned by ContentView and injected through the environment"
  - "Rooms collected in AgendaView's own .task, sessions in a separate .task"
  - "FakeAgendaSource and AgendaViewModelTests (11 tests)"
affects: [08-08 gate and docs, 08-09 parity pass, Phase 9]

plan_head_before: 82e40e58fa82316b22416cc9b6f3bb6854bbeae8
plan_head_after: 33562bf4f58f794f70d1b0a3883620ef3e464a3c

actuals:
  tokens: 16000
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Shared @Observable ViewModel owned with @State at the root and read with @Environment(Type.self); explicit re-injection into navigationBarItems content"
    - "Distinct non-shadowing @Bindable local for bindings on an environment ViewModel"
    - "Persistence behind injected UserDefaults with didSet write-through; init only reads"

key-files:
  created:
    - iosApp/iosApp/Agenda/AgendaSource.swift
    - iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift
    - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
  modified:
    - iosApp/iosApp/Agenda/AgendaViewModel.swift
    - iosApp/iosApp/Agenda/AgendaView.swift
    - iosApp/iosApp/Agenda/AgendaDetailView.swift
    - iosApp/iosApp/Agenda/AgendaCellView.swift
    - iosApp/iosApp/Agenda/AgendaNavigationBarItems.swift
    - iosApp/iosApp/Agenda/ClearFiltersButton.swift
    - iosApp/iosApp/Agenda/ComplexityFilterMenu.swift
    - iosApp/iosApp/Agenda/FavoritesFilterMenu.swift
    - iosApp/iosApp/Agenda/LanguageFilterMenu.swift
    - iosApp/iosApp/Agenda/RoomFilterMenu.swift
    - iosApp/iosApp/Agenda/SessionTypeFilterMenu.swift
    - iosApp/iosApp/UI/ContentView.swift
    - iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj

key-decisions:
  - "defaults became a private @ObservationIgnored stored property (nothing outside the ViewModel used it)"
  - "AgendaCellView keeps its Spacer after the bookmark button removal so the row layout is unchanged"
  - "@Bindable local declared at the top of body without an explicit return, so the ViewBuilder still applies"

patterns-established:
  - "Every persistence test builds a unique UserDefaults(suiteName:) and removes the domain in a defer"
  - "withObservationTracking with a lock-guarded flag (OSAllocatedUnfairLock) to assert observers are notified"

requirements-completed: [IOSUI-02, IOSUI-03, IOSTEST-01]

coverage:
  - id: D1
    description: "Agenda sessions and rooms load through the AgendaSource port; the live adapter over the mocked store loads both and records one (agenda_load, graphql) trace"
    requirement: IOSUI-02
    verification:
      - kind: integration
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#live_source_loads_the_mocked_store_sessions_and_rooms"
        status: pass
    human_judgment: false
  - id: D2
    description: "Sessions keep first-emission semantics with silent cancellation; rooms follow every emission and cancel silently"
    requirement: IOSUI-02
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#loads_only_the_first_sessions_emission_and_records_the_agenda_trace"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#cancelled_sessions_load_keeps_the_spinner"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#rooms_follow_every_emission_until_the_stream_ends"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#cancelled_rooms_collection_is_silent_and_keeps_the_rooms"
        status: pass
    human_judgment: false
  - id: D3
    description: "Favorites and filters persist on the legacy keys with byte-identical encodings; init writes nothing"
    requirement: IOSUI-02
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#favorites_and_filters_are_read_from_the_legacy_keys_and_init_writes_nothing"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#filters_are_saved_as_json_under_the_legacy_key"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaViewModelTests.swift#toggling_a_favorite_writes_the_legacy_key_and_notifies_observers"
        status: pass
    human_judgment: false
  - id: D4
    description: "Grouping and in-slot order unchanged (goldens and filter cases)"
    requirement: IOSUI-02
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AgendaCharacterizationTests.swift (unchanged)"
        status: pass
    human_judgment: false
  - id: D5
    description: "Single shared AgendaViewModel through the environment on every path (Agenda tab, filter menus, session detail, Speakers > speaker > session), initial scroll without the main-queue hop, and the D-03 immediate refresh"
    verification: []
    human_judgment: true
    rationale: "Launch check only proves no start-up trap on the Agenda tab; the remaining navigation paths and the shifted-clock scroll (A4) are visited in the 08-09 parity pass"

duration: 45min
completed: 2026-11-05
status: complete
---

# Phase 08 Plan 07: Agenda Observation Summary

**One shared `@Observable` AgendaViewModel owned by ContentView and read from the environment by every agenda view, with sessions and rooms behind an AgendaSource port, rooms collected in their own `.task`, and favorites/filters persisted on the legacy keys behind injected defaults.**

## Performance

- **Duration:** about 45 min
- **Tasks:** 3
- **Files:** 17 touched (3 created, 14 modified)

## Accomplishments
- `AgendaSource` port plus `LiveAgendaSource` (store resolved lazily, the bridge is the only adapter code).
- `AgendaViewModel` is `@Observable @MainActor final class` with no base class, no Combine, no published wrappers, no will-change calls, no main-queue hops and no unstructured task. `init(source:tracing:defaults:)` only reads the legacy keys; `favorites` is stored with a `didSet` write-through; `sessionFilters` keeps its save on change.
- `observeSessions()` keeps v1.0 first-emission semantics through `DataLoadTracing` (`agenda_load` / `graphql`); `observeRooms()` is a structured loop that ends with the stream or the view's `.task`. Cancellation is silent in both.
- `ContentView` owns the instance (`@State`) and injects it on the `TabView`; `AgendaView`, `AgendaDetailView`, the bar items, `ClearFiltersButton` and the five filter menus read `@Environment(AgendaViewModel.self)`; `AgendaView` re-injects it into `AgendaNavigationBarItems` and binds `isLoading` through `@Bindable var bindable`.
- `AgendaCellView` lost its ViewModel and the unused bookmark button; `SpeakerDetailsView` passes only `session:`.
- Initial-scroll main-queue hop replaced with a direct assignment (D-12).
- IOSUI-02 / SC2 app-wide: zero matches for an unstructured or detached task under `iosApp/iosApp` (bridge included, comments included).

## Verification
- Task 1 targeted run: `XCRESULT-TESTS total=12 passed=12 failed=0 skipped=0`, `XCRESULT-OK`; `AgendaViewModelTests`, `AgendaCharacterizationTests`, `AgendaDateParsingTests` among passed suites.
- Task 2 targeted run: same line (`total=12`, `XCRESULT-OK`). Launch check: built app installed on the iPhone 17 / iOS 27 simulator, `simctl launch` printed a pid, the process was still listed by `launchctl` 6 seconds later, and a screenshot shows the Agenda tab leaving its spinner with the schedule rendered (no environment trap).
- Task 3 full suite (build-for-testing then test-without-building, parallel testing disabled): `XCRESULT-TESTS total=57 passed=57 failed=0 skipped=0`, `XCRESULT-OK` (46 before the plan plus 11 new; plan floor 56). All nine suites passed, `AgendaViewModelTests` included.
- `SWIFT-NAMES-OK` (no baseline regenerated, `shared-h-baseline.txt` untouched), `KMPNC-VERSION-OK 1.0.6`.

## Task Commits
1. **Task 1 (tracer): agenda data path on ports** - `4a512cb` (feat)
2. **Task 2: Observation with one shared instance in the environment** - `247f123` (feat)
3. **Task 3: fake and behavior tests** - `33562bf` (test)

## Scope justification and budget acceptance
- The plan lists 17 files, above the 15-file threshold: 13 are Task 2's single compile unit (no file can keep the legacy wrapper once the class leaves the legacy observation model; nine edits are one-line wrapper swaps) and 4 are the port, the test suite, the fake and `project.pbxproj`, which D-14 requires in the same plan as the ViewModel. Accepted by the orchestrator at plan-check iteration 2.
- The calibrated 140000-token estimate exceeded the 100000 budget; also accepted at plan-check iteration 2. Each task was committed as soon as its verify passed.

## Documentation hand-off to 08-08 Task 3
No documentation was changed in this plan. 08-08 Task 3 must document: (1) one shared `AgendaViewModel` owned by `ContentView` with `@State` and read with `@Environment(AgendaViewModel.self)`, the bar items re-injected explicitly; (2) favorites and filters stay on the legacy `UserDefaults` keys `Favorites` and `AgendaSessionFilters` until Phase 9; (3) the test rule that every persistence test uses its own `UserDefaults(suiteName:)` removed afterwards and never `.standard`.

## Deviations from Plan

None - plan executed exactly as written. Notes:
- TDD note for Task 3: per the task text the behavior tests were written against the already migrated ViewModel and passed on first run, so no RED commit exists (same as 08-06).
- One small fix inside Task 3: the observation test first warned about mutating a captured var in a concurrent closure; replaced with an `OSAllocatedUnfairLock` flag before the commit.

## Issues Encountered
- The worktree command guard refuses compound shell and `rtk`-rewritten git; the plan's `<automated>` blocks were run as separate plain commands (same checks) and git was invoked as `/usr/bin/git`.
- Xcode prints "command failed with exit code 0" noise lines; exit codes and result bundles were all clean.

## Known Stubs
None.

## Threat Flags
None. T-08-14 (legacy keys, init writes nothing: tests decode a literal v1.0 blob), T-08-15 (environment injected on the TabView and re-injected on the bar items; launch check passed), T-08-16 (two separate `.task` modifiers; tracer loads both) and T-08-17 (per-test suites, `.standard` absent from the test file) are mitigated as planned.

## Next Phase Readiness
Every ViewModel is now Observation-based; 08-08 removes the remaining Combine leftovers (`BaseViewModel`, ContentView's `@StateObject` and Combine import) and carries the documentation hand-off above. 08-09 should visit Speakers > speaker > session and the filter menus, check the shifted-clock initial scroll (A4), and record the D-03 immediate-refresh deviation.

## Self-Check: PASSED

- Files exist: AgendaSource.swift, FakeAgendaSource.swift, AgendaViewModelTests.swift, AgendaViewModel.swift.
- Commits `4a512cb`, `247f123`, `33562bf` are on the branch; measured count since `plan_head_before` is 3.
- Full suite 57/57 passed.
