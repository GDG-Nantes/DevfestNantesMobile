---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 12
subsystem: domain
tags: [kmp, domain, golden, timezone]
requires:
  - phase: 07-09
    provides: SessionFilter / SessionFilterType in :core:model
  - phase: 07-05
    provides: GoldenFiles / Goldens / FilterCases in :core:testing
provides:
  - FilterSessionsUseCase (shared filter rule)
  - DayAssignment (UtcDayWindows, LocalDateDays), DaySchedule, TimeSlot, GroupSessionsUseCase
  - Test-data wiring for :core:domain (jvmTest property, SIMCTL_CHILD_DOMAIN_TESTDATA)
affects: [07-13, 07-15, phase-09]
tech-stack:
  added: []
  patterns: ["explicit TimeZone parameter on the shared grouping API", "golden parity tests on JVM and iOS simulator"]
key-files:
  created:
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/FilterSessionsUseCase.kt
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/DayAssignment.kt
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/DaySchedule.kt
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/GroupSessionsUseCase.kt
    - core/domain/src/commonTest/kotlin/com/gdgnantes/devfest/core/domain/agenda/FilterSessionsUseCaseTest.kt
    - core/domain/src/commonTest/kotlin/com/gdgnantes/devfest/core/domain/agenda/GroupSessionsUseCaseTest.kt
    - core/domain/src/commonTest/kotlin/com/gdgnantes/devfest/core/domain/agenda/DayAssignmentTest.kt
  modified:
    - core/domain/build.gradle.kts
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModule.kt
key-decisions:
  - "DayAssignment strategies are top-level classes (UtcDayWindows, LocalDateDays), so Swift sees plain names"
  - "UtcDayWindows keeps the exact Agenda.Builder predicate, including its truncation quirk (a start less than 24 h before a day start counts as that day)"
requirements-completed: [SHARED-02, SHARED-03, SHARED-07]
status: complete
duration: ~1h (interrupted once by a network outage)
completed: 2026-10-29
actuals:
  tokens: 40000
  tasks: 2
  commits: 2
commits: 2
plan_head_before: a709d8a8b9a07d0aec0539e4621e3fba25b3160e
plan_head_after: 6a5f5d74eab7d468355be371f67cc8fb4d7ef41d
---

# Phase 7 Plan 12: Shared filter rule and day/time-slot grouping Summary

Shared `FilterSessionsUseCase` and `GroupSessionsUseCase` in `:core:domain`, proven against the v1.0 Android matrix, `Agenda.Builder`, the recorded snapshot and the Android and iOS goldens, on the JVM and the iOS simulator.

## What was built

- **FilterSessionsUseCase.filter(sessions, filters, bookmarkedSessionIds)**: OR within a filter type, AND across types; BOOKMARK by id membership whatever the value; LANGUAGE/COMPLEXITY/TYPE by enum name, ROOM by `room.id`; null never matches; input order kept; no filter returns the input list itself.
- **DayAssignment** (`UtcDayWindows`, `LocalDateDays`), internal `dayPosition` / `dayLabels`; invalid day starts parse with `toInstantOrNull()` and match nothing (never throws).
- **DaySchedule / TimeSlot** data classes, **GroupSessionsUseCase.group(sessions, dayAssignment, timeZone)**: slots keyed by start Instant, chronological; in-slot order by (room.sortIndex, room.name), roomless first, stable; every configured day emitted.
- `domainModule` factories for both use cases; `core/domain/build.gradle.kts` wires `devfest.domainTestdata` (jvmTest) and `SIMCTL_CHILD_DOMAIN_TESTDATA` (KotlinNativeSimulatorTest), both with the data directory as task input. `-PrecordGoldens` is not forwarded, so no golden can be written from this module.

## Verification

- `FilterSessionsUseCaseTest`: 19 tests on JVM and iOS simulator (v1.0 matrix, edge rules, 15 shared cases vs both filtering goldens).
- `GroupSessionsUseCaseTest` (12) and `DayAssignmentTest` (11): pass on both runtimes, including the Android golden for Europe/Paris, Pacific/Auckland and UTC, and both iOS goldens (membership and slot keys equal; order inside a slot is the documented RoomSortIndex deviation, asserted).
- `:shared:linkDebugFrameworkIosSimulatorArm64`: header exports `GroupSessionsUseCase`; `swift-names-gate.sh check` ends `SWIFT-NAMES-OK` (new exports only, baselines untouched).
- Xcode build-for-testing and Swift tests: XCRESULT-OK (19 passed).
- `detekt lint`, `di-gate`, `kmp-nc-version-gate`, `testDebugUnitTest jvmTest testAndroidHostTest`: green. `core/domain/testdata` untouched.

## TDD note

The plan is `type: execute` with `tdd="true"` tasks and one commit per task. Tests and implementation were written together and committed together per task; no separate RED commit exists.

## Deviations from Plan

### Auto-fixed Issues

None in production code. One test expectation was corrected during authoring (not a golden): `an_unparseable_day_start...` initially placed a session 16 h before the day-two start, which `Agenda.Builder`'s predicate (`inWholeDays == 0` truncates toward zero) assigns to day two. The session was moved to 2025-10-14. This quirk is intentionally preserved for strict parity and is worth noting in the 07-13 deviations table.

### Known v1.0 behavior carried over

- `UtcDayWindows` keeps `(start - dayStart).inWholeDays == 0`, so an instant up to 24 h before a day start belongs to that day (same as `Agenda.Builder`).

## Known Stubs

None.

## Threat Flags

None. T-07-17 mitigated (no throw on invalid day starts or empty lists, tested); T-07-18 mitigated (golden parity on both runtimes, no golden edited).

## Environment notes

Copied gitignored `local.properties` and `GoogleService-Info.plist` from the main checkout into the worktree for the builds; neither is committed. A network outage interrupted the run once after Task 1 files were written; state was intact and work resumed.

## Self-Check: PASSED

All created files exist; commits dbc4d7e (Task 1) and 6a5f5d7 (Task 2) are on the branch.
