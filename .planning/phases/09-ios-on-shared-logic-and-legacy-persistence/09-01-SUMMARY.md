---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 01
subsystem: ios-kmp-bridge
tags: [ios, kmp, koin, spike, tracer, kotlin-native-coroutines]

requires:
  - phase: 07-shared-agenda-domain
    provides: core:domain use cases (ObserveAgenda, SpeakerIndex, VenueLanguage, ClassifySession) bound as Koin factories
  - phase: 08-ios-architecture
    provides: KotlinStreamBridge, TestHost, hosted Swift Testing target
provides:
  - Six plain IosDependencies accessors (D-08), each self-starting, no Koin type in the Swift API
  - Proof that a real StateFlow-backed Kotlin flow delivers a second element through KotlinStreamBridge (research flag closed locally)
  - Proof of the D-05 premise on the real stores (setFilters and setBookmarked visible synchronously)
  - Regenerated swift-names baselines (Phase 7 core:domain staleness closed)
affects: [09-02, 09-03, agenda ViewModel rewrite, bookmarks and filters adapters]

actuals:
  tokens: 5600
  tasks: 2
  commits: 2
plan_head_before: 80c98c9b874a83d1d1eefc4337ca7b18058d2fdc
plan_head_after: 15b9f671

tech-stack:
  added: []
  patterns:
    - "IosDependencies accessors go through one private inline reified resolve() helper that starts Koin first"
    - "Continuous-flow spike test shape: iterator form (makeAsyncIterator, two next() calls), write, assert synchronous visibility, assert second element, restore in defer"

key-files:
  created:
    - iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift
  modified:
    - shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt
    - shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt
    - iosApp/iosApp.xcodeproj/project.pbxproj
    - scripts/swift-names/shared-h-baseline.txt
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt

key-decisions:
  - "The reified private resolve() helper compiles on Kotlin/Native and stays out of the ObjC header (RESEARCH A2 holds), so the explicit six-body fallback was not needed"
  - "KotlinStreamBridge needed no change: a continuous StateFlow-backed flow delivers its second element (RESEARCH A4 holds)"

patterns-established:
  - "store() is now expressed as resolve() too; behavior unchanged (still self-starting)"

requirements-completed: [IOSDATA-01, IOSDATA-03]

coverage:
  - id: D1
    description: "Six IosDependencies accessors (observeAgenda, bookmarksStore, sessionFiltersStore, speakerIndex, venueLanguage, classifySession), self-starting, concrete return types"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt (10 tests, 6 new)"
        status: pass
    human_judgment: false
  - id: D2
    description: "A real Kotlin continuous flow (filters store, bookmarks store) delivers a second element through KotlinStreamBridge after a write, in a hosted Swift test"
    requirement: IOSDATA-03
    verification:
      - kind: integration
        ref: "iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift#filters_flow_delivers_a_second_value_after_a_write"
        status: pass
      - kind: integration
        ref: "iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift#bookmarks_flow_delivers_a_second_value_after_a_write"
        status: pass
    human_judgment: false
  - id: D3
    description: "swift-names baselines regenerated with the first Swift call sites; check, selftest and observation gate pass; names-exclude.txt untouched"
    verification:
      - kind: other
        ref: "bash scripts/swift-names-gate.sh check / selftest; bash scripts/swift-observation-gate.sh"
        status: pass
    human_judgment: false

duration: 55min
completed: 2026-11-06
status: complete
---

# Phase 9 Plan 01: IosDependencies accessors and continuous-flow bridge spike Summary

**Six self-starting `IosDependencies` accessors (D-08) plus a hosted Swift spike proving that real StateFlow-backed store flows deliver a second element through `KotlinStreamBridge` unchanged**

## Performance

- **Duration:** about 55 min
- **Started:** 2026-11-06T19:35Z
- **Completed:** 2026-11-06T20:30Z
- **Tasks:** 2 of 2
- **Files modified:** 7 (1 created, 6 modified)

## Accomplishments

- `IosDependencies` now exposes `observeAgenda()`, `bookmarksStore()`, `sessionFiltersStore()`, `speakerIndex()`, `venueLanguage()` and `classifySession()` next to `store()`, all through one private `inline reified resolve()` that starts Koin first. No Koin type reaches Swift.
- Research flag closed on the local toolchain: `filters_flow_delivers_a_second_value_after_a_write` and `bookmarks_flow_delivers_a_second_value_after_a_write` both pass, on the real Koin stores, with no network and no bridge change. The D-05 premise also holds: `filters` and `isBookmarked(id:)` reflect a write synchronously, before the second element arrives. Each test restores the store in `defer`.
- swift-names baselines regenerated in the commit that adds the first Swift call sites: `shared-h-baseline.txt` gained the Phase 7 `core:domain` names (229 lines, including `DaySchedule`), `members-baseline.txt` gained the six accessors and the `SessionFiltersStore` members, `referenced-names.txt` gained `SessionFiltersStore`. `names-exclude.txt` untouched, so `SessionFilter` stays excluded.
- Six new snake_case Koin tests, one per accessor (`assertSame` for the two stores, `assertNotNull` for the four use cases).

## Task Commits

1. **Task 1 (tracer): six IosDependencies accessors, first real continuous flow through the bridge** - `8b34ca42` (feat)
2. **Task 2: bookmarks flow through the bridge, one Koin test per accessor, Android gate** - `15b9f671` (test)

**Plan metadata:** committed with this SUMMARY (docs).

## Files Created/Modified

- `shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt` - the six accessors, KDoc updated, `store()` routed through `resolve()`
- `iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift` - the two spike tests (`@MainActor @Suite(.serialized, .timeLimit(.minutes(1)))`)
- `shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt` - six resolve tests (10 `@Test` total)
- `iosApp/iosApp.xcodeproj/project.pbxproj` - registers the new test file in the `DevFestNantesTests` target
- `scripts/swift-names/*.txt` - regenerated baselines

## Decisions Made

- Reified private helper kept (RESEARCH A2): it compiles on Kotlin/Native and does not appear in `shared.h`.
- No `KotlinStreamBridge` change needed (RESEARCH A4): the bridge's `next()` after each `yield` is enough for a continuous flow.

## Deviations from Plan

None - plan executed exactly as written.

Execution notes (environment, not deviations): the worktree command guard refuses compound shell that names git or uses computed arguments, so each plan `<automated>` block was run as its component steps (plain commands with the simulator UDID `F59F5246-2D70-49BD-B6CF-82DD9A67BF48` inlined, which is what the plan's jq expression selects: iPhone 17, iOS 27.0), with identical checks and outcomes. `grep` was invoked as `/usr/bin/grep`. Gitignored inputs (`local.properties`, `GoogleService-Info.plist`, `build/SourcePackages`) were copied from the primary checkout and never staged.

## Verification Results

- `plutil -lint` on `project.pbxproj`: OK; one PBXBuildFile and one PBXFileReference entry for the new file.
- `KotlinContinuousFlowTests` alone: 1 of 1 passed after Task 1 (XCRESULT-OK), suite listed as passed.
- Full Swift suite: `XCRESULT-TESTS total=59 passed=59 failed=0 skipped=0`, XCRESULT-OK (57 before the plan plus 2 spike tests).
- `./gradlew :shared:iosSimulatorArm64Test`: BUILD SUCCESSFUL, `IosKoinTest` 10 tests, 0 failures.
- `bash scripts/di-gate.sh`: DI-GATE-OK. `swift-names-gate.sh check`: SWIFT-NAMES-OK. `selftest`: SWIFT-GATE-SELFTEST-OK. `swift-observation-gate.sh`: SWIFT-OBS-GATE-OK.
- `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest`: BUILD SUCCESSFUL.
- Acceptance criteria: six accessor signatures present (grep count 6), `IosDependencies` and `sessionFiltersStore` in members baseline, `DaySchedule` in shared-h baseline, `names-exclude.txt` unchanged, `@Test` count in `IosKoinTest.kt` is 10.

## Issues Encountered

None. The CI toolchain replay of the spike is 09-02's job, as planned; the ViewModel work has not started.

## Known Stubs

None.

## Threat Flags

None. No new network endpoint, auth path or trust-boundary file access beyond the plan's threat model (T-09-01 mitigated by plain return types; T-09-02 mitigated by `defer` restore and `spike-` values).

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- The bridge is proven on continuous flows locally; 09-02 replays the spike on the CI toolchain before any ViewModel work starts.
- Swift can reach every shared use case and both iOS stores through `IosDependencies.shared`.
- Until 09-12 deletes the Swift `SessionFilter` struct, test files importing both must write `shared.SessionFilter` and `DevFest_Nantes.SessionFilter`.

## Self-Check: PASSED

- FOUND: iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift
- FOUND commit 8b34ca42 and 15b9f671 on this branch

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-06*
