---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 03
subsystem: ios
tags: [ios, kmp, speakers, swift-testing, tdd]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: "IosDependencies.speakerIndex() accessor and the Phase 7 SpeakerIndexUseCase (09-01/09-02 tracer chain)"
provides:
  - "Speakers A-Z index served by the shared SpeakerIndexUseCase through the Speakers port"
  - "SpeakersSource.letterGroups(speakers:) and LiveSpeakersSource(speakerIndex:)"
  - "SpeakersViewModel.letterGroups, computed once per emission"
affects: [09-04, 09-05, ios-speakers]

actuals:
  tokens: 4200
  tasks: 1
  commits: 2
plan_head_before: 3bc5ef8d0f214f8d3241983da1e1389ce4ae5be4
plan_head_after: 1eefd67e29ff39ee421f2b873b3c9658f8720d18

tech-stack:
  added: []
  patterns:
    - "Port method for a Kotlin use case, with the Live adapter taking a lazy closure over IosDependencies"
    - "didSet on an @Observable property to derive grouped data once per change"

key-files:
  created: []
  modified:
    - iosApp/iosApp/UI/Speakers/SpeakersSource.swift
    - iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift
    - iosApp/DevFestNantesTests/Fakes/FakeSpeakersSource.swift
    - iosApp/DevFestNantesTests/SpeakersViewModelTests.swift
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt

key-decisions:
  - "Group once in speakersContent.didSet and read letterGroups from getAlphabets()/speakers(for:), so SpeakersView stays untouched and the characterization test, which assigns speakersContent directly, needs no edit"
  - "FakeSpeakersSource delegates to the real SpeakerIndexUseCase and only counts requests: it fakes the port, not the grouping rule"

patterns-established:
  - "Live adapter closure per Kotlin dependency (store, speakerIndex), both resolved inside methods, never in an initializer"

requirements-completed: [IOSDATA-01]

coverage:
  - id: D1
    description: "Speaker A-Z letters and per-letter speakers come from SpeakerIndexUseCase.letterGroups; no first-letter rule left in Swift"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakersVenueCharacterizationTests.swift#speaker_alphabet_matches_the_golden (ios-speaker-index.json, unedited)"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakersViewModelTests.swift#letter_groups_come_from_the_source_once_per_emission"
        status: pass
    human_judgment: false
  - id: D2
    description: "Loading, error, empty and cancellation behaviour of the Speakers screen unchanged (08 D-09/D-10/D-11)"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakersViewModelTests.swift (8 tests, all pass)"
        status: pass
    human_judgment: false
  - id: D3
    description: "SpeakersView layout and rows are visually unchanged (the file is not touched)"
    verification: []
    human_judgment: true
    rationale: "No snapshot test covers the rendered list; SpeakersView.swift is byte-identical to the merge base, which is the evidence"

duration: 45min
completed: 2026-11-06
status: complete
---

# Phase 9 Plan 03: Speakers A-Z on SpeakerIndexUseCase Summary

**The iOS speaker A-Z index now comes from the shared `SpeakerIndexUseCase` through the Speakers port, proven on the unedited v1.0 golden, with `SpeakersView` untouched.**

## Performance

- **Duration:** about 45 min (mostly Xcode and Gradle build time)
- **Tasks:** 1 (tracer, TDD: RED then GREEN)
- **Files modified:** 6

## Accomplishments

- `SpeakersSource` gains `letterGroups(speakers:)`. `LiveSpeakersSource` takes a second lazy closure `speakerIndex`, resolved inside the method, so the Live adapter is the only caller of `IosDependencies` for the new use case.
- `SpeakersViewModel` fills `letterGroups` in a `didSet` on `speakersContent` (once per emission). `getAlphabets()` and `speakers(for:)` keep their signatures and read the groups; the `prefix(1).uppercased()` rule is gone from Swift.
- The golden `ios-speaker-index.json` passes unedited, which proves Swift string order and Kotlin `sortedBy` agree on the snapshot letters.
- Full Swift suite: 60 tests, 60 passed (59 before plus 1 new). All gates and the Android gate green.

## Task Commits

1. **Task 1 (tracer, TDD): speakers A-Z through SpeakerIndexUseCase**
   - RED: `6425fbce` (test) - port method, live adapter, fake, new test, regenerated swift-names baselines
   - GREEN: `1eefd67e` (feat) - ViewModel reads the shared use case

**Plan metadata:** the docs commit that adds this SUMMARY.

## TDD Gate Compliance

- **RED:** `letter_groups_come_from_the_source_once_per_emission` was written together with the compile-level scaffolding (protocol method, live adapter, fake with `letterGroupRequestCount`) so the target test could fail on its assertion rather than on a compile error. Run: `xcodebuild ... -only-testing:DevFestNantesTests/SpeakersViewModelTests -only-testing:DevFestNantesTests/SpeakersVenueCharacterizationTests test`, exit 65, result bundle total=10 passed=9 failed=1. The failing test is the target, on `#expect(source.letterGroupRequestCount == 1)` with the observed value `source.letterGroupRequestCount : 0` and expected `1`. The other 9 tests (including the golden) passed. Semantic assessment: the test executed, failed on the planned assertion for the intended reason (the ViewModel still did the grouping in Swift and never asked the source). The `xcresult` format is not one of the formats `gsd_run check tdd-red-evidence` supports (TAP, JUnit XML, swift-testing console, unittest), so the machine classifier was not run; evidence was read from the xcresult test details.
- **GREEN:** same command, exit 0, total=10 passed=10.
- **REFACTOR:** none needed.
- Gate commits present: `test(09-03)` precedes `feat(09-03)`.

## Files Created/Modified

- `iosApp/iosApp/UI/Speakers/SpeakersSource.swift` - port method and `speakerIndex` closure on the live adapter
- `iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift` - `letterGroups`, `didSet` grouping, accessors rewritten
- `iosApp/DevFestNantesTests/Fakes/FakeSpeakersSource.swift` - delegates to the real use case, counts requests
- `iosApp/DevFestNantesTests/SpeakersViewModelTests.swift` - new test; the live-adapter test passes an explicit `speakerIndex` closure so it stays Koin-free
- `scripts/swift-names/referenced-names.txt`, `scripts/swift-names/members-baseline.txt` - regenerated in the commit that adds the Swift call sites

## Decisions Made

- Grouping happens in `didSet`, not per call: `SpeakersView` calls the accessors several times per render, and the characterization test assigns `speakersContent` directly. No edit to that test or the view was needed.
- STATE.md note "revisit in Phase 9" (an `ı` speaker before an `i` speaker): that limit concerns `firstIndexes`, used by the Android scroller. iOS uses `letterGroups`, which keeps both speakers in one `I` group in input order, the same result as v1.0 `speakers(for:)`. No iOS change is needed.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] RED commit carries scaffolding, not only the test**
- **Found during:** Task 1 (RED)
- **Issue:** A test calling `FakeSpeakersSource.letterGroupRequestCount` cannot compile before the port method and fake exist, and a compile failure is INVALID_RED, not RED.
- **Fix:** The RED commit holds the port method, live adapter, fake and test; only the ViewModel change is left for GREEN. The test then fails on its assertion (count 0, expected 1).
- **Files modified:** the five files listed under the RED commit
- **Committed in:** `6425fbce`

**2. [Rule 3 - Blocking] swift-names baseline regeneration also picked up `BookmarksStore`**
- **Found during:** Task 1 (baseline regeneration)
- **Issue:** `referenced-names.txt` and `members-baseline.txt` gained `BookmarksStore` lines. `KotlinContinuousFlowTests.swift` (from 09-02) already references that type, and its baselines were not regenerated then. The check only fails on missing names, so it passed without them.
- **Fix:** Kept the regenerated output as is; it is the correct current state and `check` and `selftest` pass.
- **Committed in:** `6425fbce`

---

**Total deviations:** 2 auto-fixed (2 blocking). **Impact on plan:** none on scope or behavior.

## Issues Encountered

- Two foreground waits around the first RED run were moved to the background and overlapping `xcodebuild` runs interleaved their logs and corrupted one result bundle. Re-ran a single clean run; the RED evidence above comes from that clean run.

## Execution Notes

- The worktree command guard refuses compound shell that names git or computed arguments, so each plan `<automated>` block was run as its component steps (a scratchpad script for the simulator, `xcodebuild`, `xcresult-assert-tests.sh`, the gates and Gradle; plain commands for the `git diff --quiet`, `grep` and port-call checks, with `git merge-base main HEAD` resolved first to `1077fd84`). Checks and expected outcomes are the same as written.
- Simulator: iPhone 17, UDID F59F5246-2D70-49BD-B6CF-82DD9A67BF48. Gitignored build inputs were copied in and not staged.
- Results: `SpeakersViewModelTests` + `SpeakersVenueCharacterizationTests` 10/10 (both suites passed); full suite 60/60 `XCRESULT-OK`; `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK`, `SWIFT-OBS-GATE-OK`, `KMPNC-VERSION-OK 1.0.6`, `DI-GATE-OK`; `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` BUILD SUCCESSFUL.
- Acceptance criteria: `SpeakerIndexUseCase` in the source file, `IosDependencies` count in the ViewModel 0, `SpeakersView.swift` and `core/domain/testdata` unchanged vs the merge base, no `prefix(1)` left, `source.letterGroups` present, `SpeakerIndexUseCase` in `referenced-names.txt`, full suite total at least 60. All pass.
- Tracer gate: the tracer `<verify>` (carries only `<automated>`) was re-run end to end after the commits and passed, so expansion was not blocked (this is the only task).

## Known Stubs

None.

## Threat Flags

None. T-09-05 mitigated: `core/domain/testdata` has no change.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Speakers no longer duplicates the first-letter rule. The Live-adapter closure pattern (`store`, `speakerIndex`) is ready for the agenda and filter adapters in the following plans.

## Self-Check: PASSED

- Modified files exist; commits `6425fbce` and `1eefd67e` are ancestors of HEAD; `commits: 2` measured from the ledger base `3bc5ef8d`.

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-06*
