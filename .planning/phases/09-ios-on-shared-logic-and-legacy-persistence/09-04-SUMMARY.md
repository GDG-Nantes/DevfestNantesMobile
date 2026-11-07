---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 04
subsystem: ios
tags: [ios, kmp, venue, swift-testing, tdd]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: "IosDependencies.venueLanguage() accessor and the Phase 7 VenueLanguageUseCase (09-01 tracer chain); Live adapter closure pattern from 09-03"
provides:
  - "Venue content language chosen by the shared VenueLanguageUseCase through the Venue port"
  - "VenueSource.language(forCode:) and LiveVenueSource(venueLanguage:)"
affects: [09-05, ios-venue]

actuals:
  tokens: 2600
  tasks: 1
  commits: 2
plan_head_before: ffc022c05c17a65c39690eb6771f43afb127c15a
plan_head_after: 0ad7c22a

tech-stack:
  added: []
  patterns:
    - "Port method for a Kotlin use case, Live adapter takes a lazy closure over IosDependencies, resolved inside the method"
    - "Fake delegates to the real shared use case and only records requests: it fakes the port, not the rule"

key-files:
  created: []
  modified:
    - iosApp/iosApp/UI/Venue/VenueSource.swift
    - iosApp/iosApp/UI/Venue/VenueViewModel.swift
    - iosApp/DevFestNantesTests/Fakes/FakeVenueSource.swift
    - iosApp/DevFestNantesTests/VenueViewModelTests.swift
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt

key-decisions:
  - "currentLanguage stays a computed property (read only during a load) so init_does_not_call_the_source and the characterization test that builds VenueViewModel() stay untouched"
  - "The deprecated Locale.current.languageCode read is kept on purpose: Phase 12 owns deprecations"

patterns-established:
  - "Live adapter closure per Kotlin dependency (store, venueLanguage), both resolved inside methods"

requirements-completed: [IOSDATA-01]

coverage:
  - id: D1
    description: "Venue content language comes from VenueLanguageUseCase.languageFor(languageCode:) through the Venue port; no language rule left in Swift"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/VenueViewModelTests.swift#language_comes_from_the_source_with_the_locale_code"
        status: pass
      - kind: unit
        ref: "iosApp/DevFestNantesTests/SpeakersVenueCharacterizationTests.swift#venue_language_matches_the_shared_table_for_the_run_locale (unedited)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Loading, error, empty and cancellation behaviour of the Venue screen unchanged (08 D-09/D-10/D-11)"
    requirement: IOSDATA-01
    verification:
      - kind: unit
        ref: "iosApp/DevFestNantesTests/VenueViewModelTests.swift (6 tests, all pass)"
        status: pass
    human_judgment: false
  - id: D3
    description: "VenueView layout and photo detail are visually unchanged (VenueView.swift is not touched)"
    verification: []
    human_judgment: true
    rationale: "No snapshot test covers the rendered screen; VenueView.swift is byte-identical to the merge base, which is the evidence"

duration: 70min
completed: 2026-11-07
status: complete
---

# Phase 9 Plan 04: Venue language on VenueLanguageUseCase Summary

**The iOS venue language (French for "fr", English otherwise) now comes from the shared `VenueLanguageUseCase` through the Venue port, with `VenueView` untouched and the v1.0 locale test passing unedited.**

## Performance

- **Duration:** about 70 min (mostly the 18 min Android gate plus Xcode and Gradle build time)
- **Tasks:** 1 (tracer, TDD: RED then GREEN)
- **Files modified:** 6

## Accomplishments

- `VenueSource` gains `language(forCode:)`. `LiveVenueSource` takes a second lazy closure `venueLanguage`, resolved inside the method, so the Live adapter is the only `IosDependencies` caller for the new use case (`IosDependencies` count in `VenueViewModel.swift` is 0).
- `VenueViewModel.currentLanguage` returns `source.language(forCode: Locale.current.languageCode)`; the Swift `"fr"` comparison is gone. `observeVenue()` is unchanged.
- `FakeVenueSource` records the language codes and delegates to a real `VenueLanguageUseCase()`.
- Full Swift suite: 61 tests, 61 passed (60 before plus 1 new), `XCRESULT-OK`. All gates and the Android gate green.

## Task Commits

1. **Task 1 (tracer, TDD): venue language through VenueLanguageUseCase**
   - RED: `8d2fd229` (test) - port method, live adapter, fake, new test, regenerated swift-names baselines
   - GREEN: `0ad7c22a` (feat) - ViewModel reads the language from the source

**Plan metadata:** the docs commit that adds this SUMMARY.

## TDD Gate Compliance

- **RED:** `language_comes_from_the_source_with_the_locale_code` was committed with the compile-level scaffolding (protocol method, live adapter, fake with `requestedLanguageCodes`) so the target could fail on its assertion rather than on a compile error. Run: `-only-testing:DevFestNantesTests/VenueViewModelTests -only-testing:DevFestNantesTests/SpeakersVenueCharacterizationTests`, exit 65, result bundle total=9 passed=8 failed=1. The failing test is the target, on `#expect(source.requestedLanguageCodes == [code])` with observed `[]` and expected `[Optional("en")]`. Semantic assessment: the test executed and failed on the planned assertion for the intended reason (the ViewModel still decided the language in Swift and never asked the source). The `xcresult` format is not one of the formats `gsd_run check tdd-red-evidence` supports, so the machine classifier was not run; the evidence was read from `xcresulttool get test-results tests`.
- **GREEN:** full suite, exit 0, total=61 passed=61.
- **REFACTOR:** none needed.
- Gate commits present: `test(09-04)` precedes `feat(09-04)`.

## Files Created/Modified

- `iosApp/iosApp/UI/Venue/VenueSource.swift` - port method and `venueLanguage` closure on the live adapter
- `iosApp/iosApp/UI/Venue/VenueViewModel.swift` - `currentLanguage` reads the source
- `iosApp/DevFestNantesTests/Fakes/FakeVenueSource.swift` - records codes, delegates to the real use case
- `iosApp/DevFestNantesTests/VenueViewModelTests.swift` - new test; the live-adapter test passes `venueLanguage: { VenueLanguageUseCase() }` so it stays Koin-free
- `scripts/swift-names/referenced-names.txt`, `scripts/swift-names/members-baseline.txt` - regenerated in the commit that adds the Swift call sites (`shared-h-baseline.txt` unchanged)

## Decisions Made

- `currentLanguage` stays computed (not cached): it is read once per load and the `init_does_not_call_the_source` test pins that init does nothing.
- Kept the deprecated `Locale.current.languageCode` read; Phase 12 owns deprecations.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] RED commit carries scaffolding, not only the test**
- **Found during:** Task 1 (RED)
- **Issue:** A test calling `FakeVenueSource.requestedLanguageCodes` cannot compile before the port method and fake exist, and a compile failure is INVALID_RED, not RED.
- **Fix:** The RED commit holds the port method, live adapter, fake and test; only the ViewModel change is left for GREEN.
- **Committed in:** `8d2fd229`

---

**Total deviations:** 1 auto-fixed (1 blocking). **Impact:** none on scope or behavior.

## Issues Encountered

None.

## Execution Notes

- The worktree command guard refuses compound shell that names git or computed arguments, so each plan `<automated>` block was run as its component steps: a scratchpad script for the simulator boot, `build-for-testing`, `test-without-building` and `xcresult-assert-tests.sh`; plain commands for the `grep` and `git diff --quiet` checks (with `git merge-base main HEAD` resolved first to `1077fd84`), the framework link, the four gates and Gradle. Checks and expected outcomes are the same as written. The UDID was fixed to the iPhone 17 simulator instead of the `jq` lookup.
- Results: full suite 61/61 `XCRESULT-OK`; `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK`, `SWIFT-OBS-GATE-OK`, `KMPNC-VERSION-OK 1.0.6`, `DI-GATE-OK`; `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` BUILD SUCCESSFUL.
- Acceptance criteria: `VenueLanguageUseCase` in `VenueSource.swift` (2 hits), `IosDependencies` count in `VenueViewModel.swift` 0, no `"fr"` left in the ViewModel, `source.language(forCode:` present, `VenueView.swift` unchanged vs the merge base, `venue_language_matches_the_shared_table_for_the_run_locale` passes unedited, full suite total 61 (at least 61). All pass.
- Tracer gate: the tracer `<verify>` carries only `<automated>`, and was run end to end after the commits and passed, so expansion was not blocked (this is the only task).
- Gitignored build inputs (`local.properties`, `GoogleService-Info.plist`, `build/SourcePackages`) were copied in and not staged. No xcodebuild process was left running.

## Known Stubs

None.

## Threat Flags

None. T-09-28 accepted as planned: the locale code is local input, and the use case maps any value to French or English.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Venue no longer duplicates the language rule. Ready for 09-05.

## Self-Check: PASSED

- Modified files exist; commits `8d2fd229` and `0ad7c22a` are ancestors of HEAD; `commits: 2` measured from the ledger base `ffc022c0`.

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-07*
