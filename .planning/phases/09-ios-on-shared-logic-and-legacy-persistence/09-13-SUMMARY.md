---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 13
subsystem: ios
tags: [ios, cleanup, swift-names]

requires:
  - phase: 09-12
    provides: Swift SessionFilter struct deleted, so the exported Kotlin type is the only one of that name
provides:
  - Kotlin SessionFilter written unqualified in all Swift sources
  - names-exclude.txt without the SessionFilter line
  - regenerated swift-names baselines that protect SessionFilter (and FilterSessionsUseCase)
affects: [09-14]

actuals:
  tokens: 3000
  tasks: 1
  commits: 1
plan_head_before: d97665e86c8c736df541e69c1a3bb9e6d6eb2bfe
plan_head_after: cf6dedd28b8c2e6ed333f35cd2fa9612ee95f8b1

tech-stack:
  added: []
  patterns:
    - "A name leaves names-exclude.txt in the same commit as the baseline regeneration and the Swift call sites"

key-files:
  created: []
  modified:
    - iosApp/iosApp/Agenda/AgendaViewModel.swift
    - iosApp/iosApp/Agenda/AgendaSource.swift
    - iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift
    - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
    - iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift
    - scripts/swift-names/names-exclude.txt
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt

key-decisions:
  - "Kept FilterSessionsUseCase in the regenerated baselines: AgendaCharacterizationTests (09-12) references it, so it is a genuine Kotlin reference the gate had not yet protected"

patterns-established:
  - "Doc comments that described the shadowing Swift type were rewritten rather than left stale"

requirements-completed: [IOSDATA-01]

coverage:
  - id: D1
    description: "Swift sources write the Kotlin SessionFilter with no module qualifier"
    requirement: IOSDATA-01
    verification:
      - kind: other
        ref: "grep -rn 'shared\\.SessionFilter' iosApp/iosApp iosApp/DevFestNantesTests (no match)"
        status: pass
      - kind: integration
        ref: "xcodebuild test, iosApp test plan, 72/72 passed (XCRESULT-OK)"
        status: pass
    human_judgment: false
  - id: D2
    description: "The swift-names gate protects the Kotlin SessionFilter and SessionFilterType"
    requirement: IOSDATA-01
    verification:
      - kind: other
        ref: "bash scripts/swift-names-gate.sh check (SWIFT-NAMES-OK) and selftest (SWIFT-GATE-SELFTEST-OK)"
        status: pass
    human_judgment: false

duration: 20min
completed: 2026-11-07
status: complete
---

# Phase 9 Plan 13: Kotlin SessionFilter stands alone Summary

**The exported Kotlin `SessionFilter` is written unqualified in every Swift source, its `names-exclude.txt` line is gone, and the regenerated swift-names baselines now protect it.**

## Performance

- **Duration:** about 20 min
- **Started:** 2026-11-07T19:05Z
- **Completed:** 2026-11-07T19:25Z
- **Tasks:** 1
- **Files modified:** 8

## Accomplishments
- Removed the `shared.` qualifier from the five remaining Swift files (`AgendaViewModel`, `AgendaSource`, `FakeAgendaSource`, `AgendaViewModelTests`, `KotlinContinuousFlowTests`) and rewrote the three doc comments that still claimed the app declares its own `SessionFilter` struct.
- Removed the `SessionFilter` line from `names-exclude.txt` (the `Link` and `SpeakerDetails` entries remain).
- Regenerated `referenced-names.txt` and `members-baseline.txt` in the same commit: `SessionFilter` (with its 8 members) and `SessionFilterType` are listed as Kotlin references. `shared-h-baseline.txt` did not change.

## Task Commits

1. **Task 1 (tracer): the Kotlin names stand alone** - `cf6dedd2` (refactor)

**Plan metadata:** committed with this SUMMARY (docs: complete plan)

## Files Created/Modified
- `iosApp/iosApp/Agenda/AgendaViewModel.swift`, `iosApp/iosApp/Agenda/AgendaSource.swift` - unqualified `SessionFilter`; stale doc comment rewritten
- `iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift`, `AgendaViewModelTests.swift`, `KotlinContinuousFlowTests.swift` - same
- `scripts/swift-names/names-exclude.txt` - `SessionFilter` line removed
- `scripts/swift-names/referenced-names.txt`, `members-baseline.txt` - regenerated

## Decisions Made
- Kept `FilterSessionsUseCase` in the regenerated baselines (`referenced-names.txt` +1 line, `members-baseline.txt` +2 pairs for it). 09-12 introduced a Swift reference to it without regenerating; it is a real Kotlin export, so the diff review accepted it. Not a silenced check: it only adds protection.

## Deviations from Plan

None - plan executed exactly as written. The baseline diff was reviewed line by line: only `SessionFilter`, its members, and `FilterSessionsUseCase` were added; nothing removed.

**Total deviations:** 0. **Impact:** none.

## Issues Encountered
- Facts differ slightly from the plan text: the suite is 72 tests (floor 71 satisfied; 72 is the new floor per 09-12).

## Execution notes
- The plan's three `<automated>` blocks were run as their component steps (worktree command guard refuses compound shell naming git or computed values), with identical checks:
  - qualifier absence: `grep -rn 'shared\.SessionFilter'` over iosApp sources returns no match; `names-exclude.txt` has no `^SessionFilter` line; `referenced-names.txt` has `SessionFilter` and `SessionFilterType` as whole lines.
  - `:shared:linkDebugFrameworkIosSimulatorArm64`, then swift-names `check` -> `SWIFT-NAMES-OK`, `selftest` -> `SWIFT-GATE-SELFTEST-OK`; observation gate -> `SWIFT-OBS-GATE-OK`; KMP-NC gate -> `KMPNC-VERSION-OK 1.0.6`; DI gate -> `DI-GATE-OK`.
  - iPhone 17 simulator F59F5246-2D70-49BD-B6CF-82DD9A67BF48: `build-for-testing`, then `test-without-building` with `-collect-test-diagnostics never`; `xcresult-assert-tests.sh` -> `XCRESULT-TESTS total=72 passed=72 failed=0 skipped=0`, `XCRESULT-OK`.
  - `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest` -> BUILD SUCCESSFUL.
- Gitignored build inputs (`local.properties`, `GoogleService-Info.plist`, `build/SourcePackages`) were copied in and never staged. No xcodebuild left running.
- `documentation/DEVELOPMENT.md` lines 146-155 are left for 09-14, as instructed.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Ready for 09-14. Real test count is 72.

## Self-Check: PASSED
- Commit `cf6dedd2` exists on this branch; `git rev-list --count` from the plan base is 1, matching `commits: 1`.
- Modified files exist; no file deletions in the commit.

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-07*
