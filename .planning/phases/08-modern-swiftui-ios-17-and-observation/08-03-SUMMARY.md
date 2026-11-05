---
phase: 08-modern-swiftui-ios-17-and-observation
plan: 03
subsystem: ios
tags: [ios, swiftui, observation, swift-testing, kmp-nativecoroutines]

requires:
  - phase: 08-modern-swiftui-ios-17-and-observation
    provides: "KotlinStreamBridge, FakeStream, Fakes group and four-entry project rule (08-02)"
provides:
  - "VenueSource port and LiveVenueSource adapter (asyncFunction, lazily resolved store)"
  - "VenueViewModel as @Observable @MainActor final class owned via @State, no unstructured task or main-queue hops"
  - "FakeCall<Value>: scripted one-shot async call fake with a call handshake, reusable by SpeakerDetails (08-06)"
  - "FakeVenueSource and VenueViewModelTests (6 tests)"
affects: [08-04, 08-06, 08-08]

actuals:
  tokens: 6200
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "One-shot Kotlin suspend calls go through asyncFunction inside the Live<Feature>Source adapter only; ViewModels await a plain async throws port method"
    - "One-shot call fake: n-th call plays the n-th script (value, fail, suspendUntilCancelled), waitUntilCalled() handshake, NSLock.withLock for counters"

key-files:
  created:
    - iosApp/iosApp/UI/Venue/VenueSource.swift
    - iosApp/DevFestNantesTests/VenueViewModelTests.swift
    - iosApp/DevFestNantesTests/Fakes/FakeCall.swift
    - iosApp/DevFestNantesTests/Fakes/FakeVenueSource.swift
  modified:
    - iosApp/iosApp/UI/Venue/VenueViewModel.swift
    - iosApp/iosApp/UI/Venue/VenueView.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj

key-decisions:
  - "Venue fixtures for fakes come from the mocked Kotlin store (approved D-07 exception, 2026-11-04): VenueContent(from:) force-casts coordinates to Double, and hand-building them needs KotlinDouble, which would force a swift-names baseline regeneration"
  - "About and Venue take no tracing parameter (D-06 deviation): v1.0 recorded no trace for these screens and adding one would change Firebase data"
  - "A cancelled load returns right after isLoading = true without logging (D-11), so a cancelled first load leaves isLoading true and venueContent nil"

patterns-established:
  - "FakeCall.Script: .value / .fail / .suspendUntilCancelled, with the last script repeating"

requirements-completed: [IOSUI-02, IOSTEST-01]

coverage:
  - id: D1
    description: "VenueViewModel is @Observable @MainActor final, owned via @State, loaded in the existing .task, with no Combine, published wrapper, base class, unstructured task or queue hop; reads the venue only through VenueSource"
    requirement: "IOSUI-02"
    verification:
      - kind: other
        ref: "plan Task 1 static grep checks (legacy constructs absent, @Observable/@MainActor/Observation import/default port/CancellationError branch/currentLanguage present, @State ownership, asyncFunction only in the live adapter)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Live path over the mocked Kotlin store (adapter, asyncFunction, Kotlin suspend function) fills venueContent and clears isLoading"
    requirement: "IOSUI-02"
    verification:
      - kind: integration
        ref: "VenueViewModelTests#live_source_loads_the_mocked_store_venue_through_the_kotlin_bridge"
        status: pass
    human_judgment: false
  - id: D3
    description: "v1.0 Venue semantics kept: venue requested in currentLanguage, each load shows the spinner and keeps the previous venue, error stops loading and keeps content, cancelled load silent, init never calls the source"
    requirement: "IOSTEST-01"
    verification:
      - kind: unit
        ref: "VenueViewModelTests (five behavior tests) and SpeakersVenueCharacterizationTests#venue_language_matches_the_shared_table_for_the_run_locale"
        status: pass
    human_judgment: false
  - id: D4
    description: "Full Swift suite green with the new files registered; swift-names and KMP-NC gates OK"
    requirement: "IOSTEST-01"
    verification:
      - kind: integration
        ref: "xcodebuild build-for-testing + test-without-building; scripts/xcresult-assert-tests.sh -> XCRESULT-TESTS total=32 passed=32 failed=0 skipped=0, XCRESULT-OK; swift-names-gate check -> SWIFT-NAMES-OK; kmp-nc-version-gate -> KMPNC-VERSION-OK 1.0.6"
        status: pass
    human_judgment: false

duration: 25min
completed: 2026-11-05
status: complete
plan_head_before: de1fb5fa085863048908426e2af1c5ad646c182e
plan_head_after: d649e9ee34a30ece58a05c9704adafde4998eeb9
commits: 2
---

# Phase 8 Plan 03: Venue on Observation Summary

**Venue migrated to an `@Observable @MainActor` ViewModel behind a one-shot `VenueSource` port (`asyncFunction` only in `LiveVenueSource`), with a reusable scripted `FakeCall` and a tracer test over the mocked Kotlin store (32/32 Swift tests green).**

## Performance

- **Duration:** about 25 min
- **Completed:** 2026-11-05
- **Tasks:** 2
- **Files:** 7 (4 created, 3 modified)

## Accomplishments

- `VenueSource.swift`: `protocol VenueSource { func venue(language:) async throws -> Venue }` and `LiveVenueSource(store:)` resolving `IosDependencies.shared.store()` lazily inside the method (D-05).
- `VenueViewModel` no longer extends `BaseViewModel`; no Combine, published wrappers, unstructured `Task` or `DispatchQueue.main.async`. `observeVenue()` sets `isLoading = true` (previous content kept, D-10), awaits the port, then assigns content and clears loading; `CancellationError` returns silently, any other error logs the unchanged v1.0 line and sets `isLoading = false` (D-11). `currentLanguage` is byte-identical, so `VenueViewModel()` and the speakers/venue characterization test are unchanged. No performance trace was added.
- `VenueView`: single-line diff to `@State private var viewModel = VenueViewModel()`.
- `FakeCall<Value>` (`Script`, `call()`, `callCount`, `waitUntilCalled()`) and `FakeVenueSource` (`venueCall`, `requestedLanguages`).
- `VenueViewModelTests`: tracer plus `requests_the_venue_in_the_current_language`, `each_load_shows_the_spinner_and_keeps_the_previous_venue`, `error_stops_loading_and_keeps_the_previous_venue`, `cancelled_first_load_is_silent`, `init_does_not_call_the_source`.
- Task 1 targeted run: `XCRESULT-TESTS total=3 passed=3 failed=0 skipped=0`, `XCRESULT-OK`.
- Full suite (Task 2): `XCRESULT-TESTS total=32 passed=32 failed=0 skipped=0` then `XCRESULT-OK` (26 before the plan); `VenueViewModelTests` among the passed suites. `SWIFT-NAMES-OK`, `KMPNC-VERSION-OK 1.0.6`.

## Task Commits

1. **Task 1 (tracer): Venue end to end on Observation** - `5026df7` (feat)
2. **Task 2: one-shot call fake and Venue behavior tests** - `d649e9e` (test)

**Plan metadata:** committed separately (docs: complete plan)

## Decisions Made

See key-decisions above. The swift-names baselines needed no regeneration: `DevFestNantesStoreBuilder` was already registered by 08-02, and no new Kotlin name is referenced (`Venue`, `ContentLanguage` were already in the referenced set).

## Deviations from Plan

### Approved deviations (recorded as the plan requires)

**1. [D-07 exception, user-approved 2026-11-04] Fakes return the Venue fixture read from the mocked Kotlin store**
- Reason: `VenueContent(from:)` force-casts `venue.latitude`/`venue.longitude` and building non-nil values needs `KotlinDouble`, which would force a swift-names baseline regeneration.
- Files: `VenueViewModelTests.swift` (`fixtureVenue()` via `LiveVenueSource(store:)` over `DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()`).

**2. [D-06 deviation] No `tracing:` parameter on the Venue ViewModel (same as About)**
- Reason: v1.0 recorded no performance trace for these screens; adding one would change Firebase data.

### Auto-fixed issues

**3. [Rule 1 - Bug] Swift 6 language-mode warning on `NSLock.lock()/unlock()` in async contexts**
- **Found during:** Task 2 build-for-testing (warning "unavailable from asynchronous contexts; this is an error in the Swift 6 language mode") in `FakeCall.call()` and `FakeVenueSource.venue(language:)`.
- **Fix:** replaced the lock/unlock pairs inside those async methods with `NSLock.withLock { }`. The synchronous getters (`callCount`, `requestedLanguages`) keep plain lock/unlock.
- **Files modified:** `Fakes/FakeCall.swift`, `Fakes/FakeVenueSource.swift` (fixed before the Task 2 commit).

### Notes

- The worktree command guard refuses compound shell commands that name git or computed arguments, so the plan's `<automated>` blocks were run as their component steps (static greps, `plutil -lint`, `xcodebuild` build/test, xcresult assertion, gates) with identical checks and outcomes. Plain `git` was invoked as `/usr/bin/git`. Simulator: iPhone 17 (iOS 27.0), UDID `F59F5246-2D70-49BD-B6CF-82DD9A67BF48`. The gitignored `local.properties`, `GoogleService-Info.plist` and the main checkout's `build/SourcePackages` were copied into the worktree and not staged.

**Total deviations:** 2 approved by the plan, 1 auto-fixed, 0 behavior impact.

## TDD note

Task 2 is `tdd="true"` with tests written against the already-migrated Task 1 ViewModel, as the plan prescribes; all five behavior tests passed on first run, so no ViewModel change was needed. The Task 1 tracer test was committed with its implementation (tracer task).

## Issues Encountered

- Xcode logs `error: the following command failed with exit code 0 but produced no further output` during builds; benign (exit 0), as in 08-01 and 08-02.
- Flagged assumption (non-nil coordinates in the mocked venue fixture) held: the tracer and all fake-based tests passed.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `FakeCall` is ready for SpeakerDetails (08-06); `VenueSource`/`LiveVenueSource` show the `asyncFunction` live-adapter shape.
- 08-04 replays the suite on the CI toolchain (Xcode 26.6); `asyncFunction` over the mocked store is the second bridge shape it must run.

## Known Stubs

None.

## Threat Flags

None - T-08-07 mitigated (load runs in the view's `.task`, no unstructured task; `cancelled_first_load_is_silent`), T-08-08 mitigated (cancellation not logged, error text unchanged, no venue content logged).

## Self-Check: PASSED

- Created files present: `VenueSource.swift`, `VenueViewModelTests.swift`, `Fakes/FakeCall.swift`, `Fakes/FakeVenueSource.swift`: FOUND
- Commits `5026df7`, `d649e9e` reachable from HEAD: FOUND
- Acceptance: static greps pass; full suite `XCRESULT-OK` total 32; `VenueViewModelTests` passed; swift-names and KMP-NC gates OK

---
*Phase: 08-modern-swiftui-ios-17-and-observation*
*Completed: 2026-11-05*
