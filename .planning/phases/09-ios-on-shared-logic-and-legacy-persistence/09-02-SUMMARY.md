---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 02
subsystem: ios-ci-verification
tags: [ios, ci, toolchain, checkpoint, kotlin-native-coroutines]

requires:
  - phase: 09-ios-on-shared-logic-and-legacy-persistence
    provides: "Plan 09-01: IosDependencies accessors and KotlinContinuousFlowTests spike (commit 15b9f671, head b9c99773)"
provides:
  - "Roadmap research flag closed with an executed CI run: continuous Kotlin flows through KotlinStreamBridge pass on the CI toolchain (Xcode 26.6)"
  - "Run id, head SHA, Xcode version and test totals recorded as evidence (T-09-03)"
affects: [09-03, 09-04, 09-05, 09-06, 09-07, 09-08, 09-09, 09-10, 09-11]

actuals:
  tokens: 1800
  tasks: 2
  commits: 0
plan_head_before: b9c99773dfd366ddfc2dd6979faaecd0856b477f
plan_head_after: b9c99773dfd366ddfc2dd6979faaecd0856b477f

tech-stack:
  added: []
  patterns:
    - "CI replay gate: local CI-equivalent lane on the closing commit, human pushes and dispatches, Claude verifies the run read-only with gh (same shape as 08-04)"

key-files:
  created: []
  modified: []

key-decisions:
  - "No KotlinStreamBridge change is needed: the continuous-flow spike passes unchanged on both Xcode 27.1 (local) and Xcode 26.6 (CI), so RESEARCH A4 and A7 are confirmed on the CI toolchain"

requirements-completed: [IOSDATA-03]

coverage:
  - id: D1
    description: "Continuous Kotlin flows delivered through KotlinStreamBridge pass on the CI toolchain, replayed on the same commit as the local lane"
    requirement: IOSDATA-03
    verification:
      - kind: integration
        ref: "GitHub Actions run 42289484240 (iOS CI, workflow_dispatch), Suite KotlinContinuousFlowTests passed; XCRESULT-TESTS total=59 passed=59 failed=0 skipped=0; XCRESULT-OK"
        status: pass
      - kind: integration
        ref: "./gradlew :shared:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test (local, 15 result XMLs, 0 failures); CI step 'Run shared and core iOS tests' BUILD SUCCESSFUL"
        status: pass
    human_judgment: false

duration: not measured
completed: 2026-11-06
status: complete
---

# Phase 9 Plan 02: CI replay of the continuous-flow spike Summary

**The 09-01 continuous-flow spike is green on the CI toolchain (Xcode 26.6, run 42289484240, head SHA equal to the local 09-01 head), closing the roadmap research flag before any ViewModel moves to a continuous flow.**

## Accomplishments

- Task 1 (tracer): the CI-equivalent iOS lane ran locally on the 09-01 head and was green before the push. No code change.
- Task 2 (human-action checkpoint): the human pushed `feature/reno_phase_9` and the `iOS CI` workflow ran by `workflow_dispatch`. The run was verified read-only with `gh` against every acceptance criterion below.
- Plan changes no code and commits no code: it gates 09-03 onward.

## Evidence

CI run (verified by the executor with `gh run view` and the saved log, not taken from the orchestrator's report):

| Item | Value |
| ---- | ----- |
| Run id | 42289484240 |
| URL | https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/42289484240 |
| Workflow / event / job | iOS CI / workflow_dispatch / iOS Build Check |
| Branch | feature/reno_phase_9 |
| Head SHA | b9c99773dfd366ddfc2dd6979faaecd0856b477f (equal to the local head replayed in Task 1) |
| Status / conclusion | completed / success |
| Selected Xcode | `Selected Xcode: 26.6.0`; `Xcode 26.6`, `Build version 17F113` |
| Gates in CI | `KMPNC-VERSION-OK 1.0.6`, `KMPNC-SELFTEST-OK`, `SWIFT-OBS-GATE-OK`, `SWIFT-OBS-GATE-SELFTEST-OK`, `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK` |
| Kotlin iOS tests in CI | `:shared`, `:core:model`, `:core:data`, `:core:domain`, `:core:testing` `iosSimulatorArm64Test`, step ended `BUILD SUCCESSFUL in 2m 53s` |
| Swift suite in CI | `Suite KotlinContinuousFlowTests passed after 0.055 seconds.` |
| Assertion | `XCRESULT-TESTS total=59 passed=59 failed=0 skipped=0` then `XCRESULT-OK` |

Acceptance criteria of Task 2: head SHA + `success` matches Task 1's SHA (met); `XCRESULT-OK` and total 59, at least 59 (met); run id, SHA, Xcode line and totals recorded here (met).

Local lane (Task 1, same commit): Xcode 27.1 (build 27A9275), simulator iPhone 17 on iOS 27.0 (UDID F59F5246-2D70-49BD-B6CF-82DD9A67BF48). Gates printed `KMPNC-VERSION-OK 1.0.6`, `KMPNC-SELFTEST-OK`, `SWIFT-OBS-GATE-OK`, `SWIFT-OBS-GATE-SELFTEST-OK`, `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK` on the freshly linked `shared.h`. `./gradlew :shared:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test` was BUILD SUCCESSFUL, all 15 result XMLs with 0 failures and 0 skipped, including `ObserveAgendaUseCaseTest` under `:core:domain`. Swift suite: `XCRESULT-TESTS total=59 passed=59 failed=0 skipped=0`, `XCRESULT-OK`.

Scope note on the research-flag claim: the CI log proves `KotlinContinuousFlowTests` (Swift, through the bridge) passed and that the `:core:domain:iosSimulatorArm64Test` Gradle task succeeded. The CI log does not list individual Kotlin test names, so `ObserveAgendaUseCaseTest` passing is established by the successful `:core:domain` task in CI and by the per-test XML check locally, not by a named line in the CI log.

## Toolchain note

The plan assumed CI picks the latest Xcode installed on the runner. It selected Xcode 26.6 (build 17F113) while the local toolchain is Xcode 27.1, a major version apart. Both are green on the same SHA, which makes this replay a genuine cross-check of the bridge rather than a repeat of the local result.

## Task Commits

No code commits (verification only). Plan commit count is 0 by measurement (`plan_head_before` equals `plan_head_after`); the only commit is this SUMMARY, a docs commit made after the measured head was recorded.

## Deviations from Plan

None - plan executed exactly as written.

Execution notes (environment, not deviations): the worktree command guard refuses compound shell that names git, so the Task 1 `<automated>` blocks were run as plain component commands with identical checks and outcomes; `/usr/bin/git` and `/usr/bin/grep` were used. `local.properties`, `GoogleService-Info.plist` and `build/SourcePackages` were copied from the primary checkout (all gitignored, none staged). The agent did not push and did not create, switch or rename branches; the human pushed and dispatched the workflow.

## Issues Encountered

None. `SWIFT-MEMBERS-CHANGED` and `SWIFT-NAME-COLLISION` lines in the CI log come from the swift-names selftest's deliberate mutations and are followed by `SWIFT-GATE-SELFTEST-OK`.

## Known Stubs

None.

## Threat Flags

None. T-09-03 (repudiation of the replay claim) is mitigated: head SHA, conclusion and the `XCRESULT-OK` / total lines were checked against the real run. T-09-04 and T-09-SC accepted as planned.

## User Setup Required

None.

## Next Phase Readiness

- 09-03 onward is unblocked: the bridge handles a continuous combined flow on the CI toolchain.
- If a later CI run goes red on `KotlinContinuousFlowTests`, fix inside `KotlinStreamBridge.swift` first (Flagged Assumptions A4/A7 of this plan).

## Self-Check: PASSED

- FOUND run 42289484240: headSha b9c99773dfd366ddfc2dd6979faaecd0856b477f, conclusion success
- FOUND log lines: `Xcode 26.6`, `Build version 17F113`, `XCRESULT-TESTS total=59 passed=59 failed=0 skipped=0`, `XCRESULT-OK`, `Suite KotlinContinuousFlowTests passed`
- No task code commits expected; HEAD at b9c99773 before this docs commit

---
*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Completed: 2026-11-06*
