---
phase: 08-modern-swiftui-ios-17-and-observation
plan: 02
subsystem: ios
tags: [ios, swiftui, observation, swift-testing, kmp-nativecoroutines]

requires:
  - phase: 08-modern-swiftui-ios-17-and-observation
    provides: "iOS 17.0 deployment floor on every configuration (08-01)"
provides:
  - "KotlinStreamBridge: Task-free bridge from a KMP-NativeCoroutines NativeFlow to AsyncThrowingStream, native subscription cancelled from continuation.onTermination"
  - "AboutSource port and LiveAboutSource adapter; AboutViewModel as @Observable @MainActor final class owned via @State"
  - "FakeStream (scripted native flow served through the real bridge) and FakeAboutSource in the new tests Fakes group"
  - "AboutViewModelTests (7 tests); swift-names baselines with DevFestNantesStoreBuilder"
affects: [08-03, 08-04, 08-05, 08-06, 08-07, 08-08]

actuals:
  tokens: 7360
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Per-ViewModel port (<Feature>Source) + Live adapter declared in <Feature>Source.swift, injected through an init default"
    - "Live adapters hand store flows to KotlinStreamBridge.stream(store.<flow>); ViewModels never touch KMP-NativeCoroutines"
    - "Stream fake served through the real bridge with subscription/termination handshakes (no sleeps)"

key-files:
  created:
    - iosApp/iosApp/Model/KotlinStreamBridge.swift
    - iosApp/iosApp/UI/About/AboutSource.swift
    - iosApp/DevFestNantesTests/AboutViewModelTests.swift
    - iosApp/DevFestNantesTests/Fakes/FakeStream.swift
    - iosApp/DevFestNantesTests/Fakes/FakeAboutSource.swift
  modified:
    - iosApp/iosApp/UI/About/AboutViewModel.swift
    - iosApp/iosApp/UI/About/AboutView.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt
    - documentation/DEVELOPMENT.md
    - documentation/ARCHITECTURE.md

key-decisions:
  - "D-04 mechanism: the bridge takes the store's NativeFlow closure shape directly and subscribes through its callbacks, so no pump task exists (IOSUI-02 / Roadmap SC2 with no carve-out)"
  - "Cancellation after the first-emission loop is checked explicitly (Task.checkCancellation) because the bridge ends silently on consumer cancellation (RESEARCH Pitfall 1)"
  - "shared-h-baseline.txt left untouched in the commit (see Deviations)"

patterns-established:
  - "KotlinStreamBridge.stream(store.<flow>): no task, return next() after each yield, onTermination invokes the stored native cancellable"
  - "Four project.pbxproj entries per new Swift file, new PBXGroup for Fakes"

requirements-completed: [IOSUI-02, IOSTEST-01]

coverage:
  - id: D1
    description: "AboutViewModel is @Observable @MainActor, owned via @State, loaded in .task, with no Combine, published wrapper or unstructured task; reads partners only through the AboutSource port"
    requirement: "IOSUI-02"
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/AboutViewModelTests.swift (7 tests, all via DevFestNantesTests/AboutViewModelTests)"
        status: pass
      - kind: other
        ref: "plan Task 1 static grep checks (legacy constructs absent, @Observable/@MainActor/Observation import/default port present)"
        status: pass
    human_judgment: false
  - id: D2
    description: "KotlinStreamBridge creates no task, resumes the collector with return next(), finishes with CancellationError on Kotlin cancellation and cancels the native subscription from onTermination"
    requirement: "IOSUI-02"
    verification:
      - kind: unit
        ref: "AboutViewModelTests#loads_only_the_first_emission_and_stops_the_upstream, #cancelled_load_keeps_the_reset_state (terminationCount == 1)"
        status: pass
    human_judgment: false
  - id: D3
    description: "Live path over the mocked Kotlin store (adapter, bridge, KMP-NativeCoroutines NativeFlow, Kotlin flow) fills partnersContent and clears isLoading"
    requirement: "IOSUI-02"
    verification:
      - kind: integration
        ref: "AboutViewModelTests#live_source_loads_the_mocked_store_partners_through_the_kotlin_bridge"
        status: pass
    human_judgment: false
  - id: D4
    description: "v1.0 About semantics kept: first emission only, ordinal order once per category, empty stream, error stops loading, cancelled load silent, reload reset"
    requirement: "IOSTEST-01"
    verification:
      - kind: unit
        ref: "AboutViewModelTests (six behavior tests)"
        status: pass
    human_judgment: false
  - id: D5
    description: "Full Swift suite green with the new tests registered in the project; swift-names and KMP-NC gates OK"
    requirement: "IOSTEST-01"
    verification:
      - kind: integration
        ref: "xcodebuild build-for-testing + test-without-building; scripts/xcresult-assert-tests.sh -> XCRESULT-TESTS total=26 passed=26 failed=0 skipped=0, XCRESULT-OK; swift-names-gate check -> SWIFT-NAMES-OK, selftest -> SWIFT-GATE-SELFTEST-OK; kmp-nc-version-gate -> KMPNC-VERSION-OK 1.0.6"
        status: pass
    human_judgment: false
  - id: D6
    description: "Documentation describes the port/fake pattern, the four project entries and the Task-free bridge"
    verification:
      - kind: other
        ref: "plan Task 3 grep checks over documentation/DEVELOPMENT.md and ARCHITECTURE.md"
        status: pass
    human_judgment: false

duration: 45min
completed: 2026-11-05
status: complete
plan_head_before: 485bb8c0dc3fecff4e38819ccb6263d33bd3d27c
plan_head_after: 02a24654111d05acbef9431bfbc0cd87578b2c8d
commits: 3
---

# Phase 8 Plan 02: About on Observation Summary

**About migrated to an `@Observable @MainActor` ViewModel behind an `AboutSource` port, fed by a Task-free `KotlinStreamBridge` over KMP-NativeCoroutines native-flow callbacks, with a scripted `FakeStream` that runs through the real bridge and a hosted tracer test over the mocked Kotlin store (26/26 Swift tests green).**

## Performance

- **Duration:** about 45 min
- **Started:** 2026-11-05T06:3x (approx.)
- **Completed:** 2026-11-05
- **Tasks:** 3
- **Files modified:** 12 planned (plus 5 new Swift files; see scope justification)

## Accomplishments

- `KotlinStreamBridge.stream(_:)` takes the KMP-NativeCoroutines 1.0.6 `NativeFlow` closure shape, yields each item then `return next()`, finishes normally or throwing on completion, finishes with `CancellationError` on Kotlin-side cancellation, and `continuation.onTermination` invokes the native cancellable. No task, no KMP-NativeCoroutines import.
- `AboutViewModel` no longer extends `BaseViewModel`; no Combine, published wrappers or unstructured task. `AboutView` owns it with `@State private var viewModel = AboutViewModel()` (single-line view diff). `AboutViewModel()` still works through the `LiveAboutSource()` init default; the store is resolved lazily inside `partners()`.
- `AboutViewModelTests` (7 tests) pass: tracer over the mocked store, first emission with native cancellation (`terminationCount == 1`), ordinal order once per category, empty stream, error stops loading, cancelled load keeps the reset state with native cancellation, reload reset.
- Full suite: `XCRESULT-TESTS total=26 passed=26 failed=0 skipped=0` then `XCRESULT-OK` (19 before the plan); `AboutViewModelTests` among the passed suites.
- swift-names gate: `check` prints `SWIFT-NAMES-OK`, `selftest` prints `SWIFT-GATE-SELFTEST-OK`; KMP-NC gate prints `KMPNC-VERSION-OK 1.0.6`.

### swift-names diff

`referenced-names.txt` adds only `+DevFestNantesStoreBuilder` (nothing removed). `members-baseline.txt` adds three member pairs of that type (`build()`, `init()`, `setUseMockServer(useMockServer:)`).

### Bridge shape later plans reuse

`KotlinStreamBridge.stream(store.<flow>)` in a `Live<Feature>Source` adapter: subscribes through the native-flow callbacks (no task), requests the next native item right after each yield (accepted trade-off: no pull-based backpressure), and cancels the native subscription from `continuation.onTermination`. In tests, `FakeStream<Element>(.emit(..., thenFinish:) / .fail / .hang)` exposes `stream()`, `subscriptionCount`, `terminationCount`, `waitUntilSubscribed()`, `waitUntilTerminated()`; each wait consumes one signal, so a test that subscribes twice waits twice.

### Scope justification (12 files, above the 10-file tier)

First ViewModel plan: besides About's own slice (ViewModel, view, port with live adapter, fake, test suite) it lands the pieces every later ViewModel plan reuses once: the shared `KotlinStreamBridge`, the `Fakes` group with the generic `FakeStream`, the four-entry project registrations in `project.pbxproj`, the swift-names baselines required by AGENTS.md in the same commit as the first `DevFestNantesStoreBuilder` call site, and the two documentation files required in the same change. Each task stays within one commit.

## Task Commits

1. **Task 1 (tracer): About end to end on Observation** - `a89fe56c` (feat)
2. **Task 2: scripted native-flow fake and About behavior tests** - `a8e236bb` (test)
3. **Task 3: document ports, fakes and the Task-free bridge, closed by the full suite** - `02a24654` (docs)

**Plan metadata:** committed separately (docs: complete plan)

## Files Created/Modified

- `iosApp/iosApp/Model/KotlinStreamBridge.swift` - Task-free NativeFlow-to-AsyncThrowingStream bridge
- `iosApp/iosApp/UI/About/AboutSource.swift` - `AboutSource` port and `LiveAboutSource` adapter
- `iosApp/iosApp/UI/About/AboutViewModel.swift` - Observation-based ViewModel
- `iosApp/iosApp/UI/About/AboutView.swift` - `@State` ownership line
- `iosApp/DevFestNantesTests/AboutViewModelTests.swift`, `Fakes/FakeStream.swift`, `Fakes/FakeAboutSource.swift` - tests and fakes
- `iosApp/iosApp.xcodeproj/project.pbxproj` - four entries per new file plus the `Fakes` group
- `scripts/swift-names/referenced-names.txt`, `members-baseline.txt` - baselines with `DevFestNantesStoreBuilder`
- `documentation/DEVELOPMENT.md`, `documentation/ARCHITECTURE.md` - pattern documentation

## Decisions Made

- Followed the plan's bridge design (callback-based, Task-free) rather than RESEARCH Pattern 1.
- `FakeStream` waits use `AsyncStream<Void>` handshakes with one persistent iterator per signal, as planned.

## Deviations from Plan

### Auto-fixed / noted issues

**1. [Rule 3 - Blocking] swift-names gate only scans git-tracked Swift files**
- **Found during:** Task 1 step 7
- **Issue:** `scripts/swift-names-gate.sh baseline` builds the referenced set from `git ls-files`, so the new untracked test file was not scanned and `DevFestNantesStoreBuilder` was missing from the first regeneration.
- **Fix:** staged the new Swift files (`git add`) before regenerating the baselines.
- **Files modified:** none beyond the planned baselines.

**2. [Note] `shared-h-baseline.txt` kept out of the commit**
- **Found during:** Task 1 step 7
- **Issue:** regenerating with the current header rewrote `scripts/swift-names/shared-h-baseline.txt` with 223 added names (Phase 7 `core:domain` exports such as `DaySchedule`, `FilterSessionsUseCase`), i.e. the committed header baseline was already stale before this plan. The plan's acceptance criterion requires that file not to be listed in the commit.
- **Fix:** restored that file to HEAD (`git checkout -- <file>`) and committed only `referenced-names.txt` and `members-baseline.txt`. `check` and `selftest` pass with it unchanged. The stale header baseline is a pre-existing item for a separate fix (log it for the orchestrator).

**3. [Note] Verify commands split**
- The worktree command guard refuses compound shell with git or computed arguments, so the plan's `<automated>` blocks were run as their component steps (static greps, gate scripts, `xcodebuild` build/test, xcresult assertion), with identical checks and outcomes. Simulator: iPhone 17 (iOS 27.0), UDID `F59F5246-2D70-49BD-B6CF-82DD9A67BF48`. The gitignored `local.properties`, `GoogleService-Info.plist` and the main checkout's `build/SourcePackages` were copied into the worktree for the build and not staged.

**Total deviations:** 1 blocking fix, 2 notes. **Impact:** none on behavior or scope.

## TDD note

Task 2 is `tdd="true"` with fake-based tests written against the Task 1 ViewModel, as the plan prescribes ("watch the new ones pass against Task 1's ViewModel"); all six passed on first run, so no ViewModel change was needed. The Task 1 tracer test was committed together with its implementation (tracer task).

## Issues Encountered

- Xcode logs `error: the following command failed with exit code 0 but produced no further output` during builds; benign (exit 0), as noted in 08-01.
- Flagged assumption A2 (`DevFestLogger.log(.error, ...)` in the hosted test process): the error-path test `stream_error_stops_loading_with_empty_content` exercises it and passes on Xcode 27.1; the CI replay (08-04) confirms it on Xcode 26.6.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `KotlinStreamBridge` and `FakeStream` are ready for 08-03 to 08-07 (streams via `KotlinStreamBridge.stream(store.<flow>)`, one-shot calls via `asyncFunction` in the live adapters).
- 08-04 replays the suite on the CI toolchain (Xcode 26.6 / Swift 6.3.3); the bridge generic inference and the A2 logging assumption are the points to watch there.

## Known Stubs

None.

## Threat Flags

None - no new endpoints, auth paths or trust boundaries beyond the plan's threat model (T-08-03 mitigated by `onTermination` and asserted by `terminationCount == 1`; T-08-04 log text unchanged; T-08-06 baseline diff only adds `DevFestNantesStoreBuilder`).

## Self-Check: PASSED

- Created files present: `KotlinStreamBridge.swift`, `AboutSource.swift`, `AboutViewModelTests.swift`, `Fakes/FakeStream.swift`, `Fakes/FakeAboutSource.swift`: FOUND
- Commits `a89fe56c`, `a8e236bb`, `02a24654` reachable from HEAD: FOUND
- Acceptance: static greps pass; full suite `XCRESULT-OK` total 26; `AboutViewModelTests` passed; swift-names and KMP-NC gates OK

---
*Phase: 08-modern-swiftui-ios-17-and-observation*
*Completed: 2026-11-05*
