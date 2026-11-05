---
phase: 08-modern-swiftui-ios-17-and-observation
plan: 01
subsystem: ios
tags: [ios, xcode, deployment-target]

requires:
  - phase: 07-shared-domain-layer
    provides: stable iOS build and Swift test plan baseline (19 tests)
provides:
  - "Every Xcode build configuration (project and target level) declares and resolves IPHONEOS_DEPLOYMENT_TARGET = 17.0"
affects: [08-02, 08-03, 08-04, 08-05, 08-06, 08-07]

actuals:
  tokens: 100
  tasks: 1
  commits: 1

tech-stack:
  added: []
  patterns: []

key-files:
  created: []
  modified:
    - iosApp/iosApp.xcodeproj/project.pbxproj

key-decisions:
  - "D-13: align the project-level default with the already-17.0 target-level configurations; the shipped minimum OS does not move"

patterns-established: []

requirements-completed: [IOSUI-01]

coverage:
  - id: D1
    description: "All six IPHONEOS_DEPLOYMENT_TARGET entries in project.pbxproj read 17.0 and all targets resolve 17.0 in Debug and Release"
    requirement: "IOSUI-01"
    verification:
      - kind: other
        ref: "xcodebuild -project iosApp/iosApp.xcodeproj -alltargets -configuration {Debug,Release} -showBuildSettings (3 targets each, all 17.0); plutil -lint OK"
        status: pass
    human_judgment: false
  - id: D2
    description: "App and hosted Swift tests build and the existing suite passes on the simulator"
    requirement: "IOSUI-01"
    verification:
      - kind: integration
        ref: "xcodebuild build-for-testing + test-without-building (testPlan iosApp, iPhone 17 / iOS 27.0); scripts/xcresult-assert-tests.sh -> XCRESULT-OK total=19"
        status: pass
    human_judgment: false

duration: 8min
completed: 2026-11-04
status: complete
plan_head_before: ed5bb45daa5688b2a8e1c3f228c6be9270277f21
plan_head_after: 2acbfb5459c16d2a907f6e81c647e5f6d30673ee
commits: 1
---

# Phase 8 Plan 01: iOS 17.0 deployment floor Summary

**Project-level Debug and Release deployment target raised from iOS 15.0 to 17.0 (two-line pbxproj diff), so all six configurations agree and the Swift suite stays green at 19/19.**

## Performance

- **Duration:** 8 min
- **Started:** 2026-11-04T22:18:50Z
- **Completed:** 2026-11-04T22:26:00Z
- **Tasks:** 1
- **Files modified:** 1

## Accomplishments
- The two project-level `IPHONEOS_DEPLOYMENT_TARGET = 15.0;` entries now read `17.0`; project.pbxproj holds exactly six entries, all `17.0`.
- `xcodebuild -alltargets -showBuildSettings` resolves `IPHONEOS_DEPLOYMENT_TARGET = 17.0` for all three targets in both Debug and Release.
- `plutil -lint` passes and the KMP-NativeCoroutines gate prints `KMPNC-VERSION-OK 1.0.6`.
- Full Swift sequence passed (build-for-testing, test-without-building, xcresult assertion): `XCRESULT-TESTS total=19 passed=19 failed=0 skipped=0`, then `XCRESULT-OK`.
- Documentation already says iOS 17.0; the `iOS 1[0-6]` grep over README.md, documentation/ and AGENTS.md prints nothing, so no doc change was needed.

## Task Commits

1. **Task 1 (tracer): project-level deployment target 15.0 to 17.0** - `2acbfb5` (build)

**Plan metadata:** committed separately (docs: complete plan)

## Files Created/Modified
- `iosApp/iosApp.xcodeproj/project.pbxproj` - project-level Debug and Release `IPHONEOS_DEPLOYMENT_TARGET` 15.0 to 17.0 (2 insertions, 2 deletions)

## Decisions Made
- None beyond D-13: targeted `sed` replacement of the two 15.0 values only; MARKETING_VERSION, CURRENT_PROJECT_VERSION, bundle ids and package pins untouched.

## Deviations from Plan

None - plan executed exactly as written.

Tracer feedback gate: the `<verify>` was automated-only (no human-check) and passed end to end, so expansion is unblocked.

Execution notes: the plan's two `<automated>` blocks were run as their component steps, split into plain commands, because the worktree command guard refuses compound shell. Simulator used: iPhone 17 (the newest plain iPhone on the newest installed iOS runtime, 27.0) rather than the 18.x runtimes the plan mentions, which have no devices on this machine. The gitignored `local.properties` and `GoogleService-Info.plist` were copied into the worktree for the build and were not staged.

## Issues Encountered
- The Xcode log shows `error: the following command failed with exit code 0 but produced no further output` during build-for-testing; it is a benign Xcode message (exit 0), the build and tests completed successfully.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- iOS 17.0 is the floor on every configuration, so plans 08-02 to 08-07 can adopt Observation (`@Observable`) without availability annotations.

## Known Stubs

None.

## Self-Check: PASSED

- `iosApp/iosApp.xcodeproj/project.pbxproj` modified: FOUND
- Commit `2acbfb5` reachable from HEAD: FOUND

---
*Phase: 08-modern-swiftui-ios-17-and-observation*
*Completed: 2026-11-04*
