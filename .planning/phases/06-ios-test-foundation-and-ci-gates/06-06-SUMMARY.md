---
phase: 06-ios-test-foundation-and-ci-gates
plan: 06
subsystem: testing
tags: [ios, uat, smoke, kmp-nativecoroutines, simulator]

requires:
  - phase: 06-05
    provides: KMP-NativeCoroutines SPM pin 1.0.6 plus the version-skew gate (commit 21b58bc)
provides:
  - Human-approved v1.0 behavior parity of the iOS app on KMP-NativeCoroutines 1.0.6 (CONC-03)
  - Nine smoke screenshots under .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/
affects: [phase-09, phase-11, phase-12]

actuals:
  tokens: 3000
  tasks: 2
  commits: 0
plan_head_before: 642a559ecd488ba812a45cba277c0e58c3885f2e
plan_head_after: 642a559ecd488ba812a45cba277c0e58c3885f2e

tech-stack:
  added: []
  patterns: [guided blocking-human parity smoke with one screenshot per step as the manual-UAT evidence format until automated UI tests exist (Phase 12)]

key-files:
  created:
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/00-launch.png
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/01-agenda.png
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/02-filters.png
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/03-favorites.png
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/04-relaunch.png
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/05-session-detail.png
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/06-speakers.png
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/07-venue.png
    - .planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/08-about.png
  modified:
    - .planning/phases/06-ios-test-foundation-and-ci-gates/deferred-items.md

key-decisions:
  - "The KMP-NativeCoroutines 1.0.6 bump commit 21b58bc is kept: the human reported no behavior difference against v1.0 on any of the eight smoke steps (D-11, D-12)"
  - "The Xcode GUI build failure (x86_64-only default JDK, no Rosetta) is a developer-environment issue, logged as debt and not fixed in this phase"

requirements-completed: [CONC-03]

coverage:
  - id: D1
    description: "The Debug app built against KMP-NativeCoroutines 1.0.6 is installed and launched on an iPhone simulator with the real Firebase configuration (version gate KMPNC-VERSION-OK 1.0.6, xcodebuild -list shows 1.0.6, BUILD SUCCEEDED, launch screenshot)"
    requirement: CONC-03
    verification:
      - kind: command
        ref: "bash scripts/kmp-nc-version-gate.sh -> KMPNC-VERSION-OK 1.0.6; xcodebuild -list -> KMPNativeCoroutines @ 1.0.6; xcodebuild Debug build -> ** BUILD SUCCEEDED **; simctl launch + 06-smoke/00-launch.png"
        status: pass
    human_judgment: false
  - id: D2
    description: "A human confirmed that Agenda (sessions per day, filters, favorite add/remove, favorites and filters kept after kill and relaunch), session detail, Speakers, Venue and About behave as in v1.0"
    requirement: CONC-03
    verification:
      - kind: manual_procedural
        ref: "06-06-PLAN.md Task 2 steps 1-8; screenshots 01-agenda.png to 08-about.png; human reply 'approved'"
        status: pass
    human_judgment: true
    rationale: "Behavior parity with v1.0 is a visual and interaction judgment; no automated UI test exists before Phase 12"

duration: 95min
completed: 2026-10-27
status: complete
---

# Phase 6 Plan 06: Guided v1.0 parity smoke after the KMP-NativeCoroutines 1.0.6 bump Summary

**The iOS Debug app built on KMP-NativeCoroutines 1.0.6 was smoke-tested by a human on an iPhone 18 Pro simulator across agenda, filters, favorites (including persistence after kill and relaunch), session detail, Speakers, Venue and About; every step matched v1.0, so the bump commit 21b58bc is kept.**

## Performance

- **Duration:** about 95 min including the human smoke and the Xcode environment troubleshooting
- **Completed:** 2026-10-27T13:25Z
- **Tasks:** 2 (1 auto, 1 blocking-human checkpoint)
- **Files modified:** 11 (nine screenshots and the SUMMARY created, deferred-items.md appended)

## Accomplishments

- Task 1 (automated): real, non-mock `GoogleService-Info.plist` confirmed (value never printed); `bash scripts/kmp-nc-version-gate.sh` printed `KMPNC-VERSION-OK 1.0.6`; `xcodebuild -list` showed `KMPNativeCoroutines @ 1.0.6`; Debug build ended `** BUILD SUCCEEDED **`; the app launched on the iPhone 17 simulator (F59F5246-2D70-49BD-B6CF-82DD9A67BF48) and `00-launch.png` was captured.
- Task 2 (human): the same 1.0.6 Debug build, built from the CLI (`xcodebuild ... -configuration Debug build`, BUILD SUCCEEDED) and installed and launched by the orchestrator through simctl, was exercised by the human on the iPhone 18 Pro simulator (6D9E4FF3-1D57-4277-BA27-F91A0776E1BB, iOS 27.0). All eight steps of the checklist were performed and the human replied "approved" with no difference reported against v1.0:
  1. Agenda sessions per day (`01-agenda.png`)
  2. Room and language filters, clearing them (`02-filters.png`)
  3. Favorites add, favorites filter, remove without relaunch (`03-favorites.png`)
  4. Favorite and filter kept after kill and relaunch (`04-relaunch.png`)
  5. Session detail, favorite toggle reflected in the agenda (`05-session-detail.png`)
  6. Speakers list and speaker detail (`06-speakers.png`)
  7. Venue information and map (`07-venue.png`)
  8. About partners and links (`08-about.png`)
- This exercises the one Swift-runtime change of the bump (AsyncSequence now cancels Flow collection on deallocation, Pitfall 11) on exactly the flows a user sees; no regression was observed.
- Decision D-12: no bump-caused difference, so the 06-05 Task 1 commit (pin and gate together) is not reverted.

## Task Commits

1. **Task 1: build, install and launch the 1.0.6 app, capture the launch screenshot** - no commit by plan (the screenshot is committed with this SUMMARY after approval)
2. **Task 2: guided v1.0 parity smoke (checkpoint:human-verify, blocking-human)** - approved by the human; no code change

**Plan metadata:** the docs commit that carries this SUMMARY, the nine screenshots and the tracking updates. `commits: 0` above counts code commits made by the plan before the SUMMARY (docs-only plan, nothing to commit during Tasks 1 and 2).

## Files Created/Modified

- `.planning/phases/06-ios-test-foundation-and-ci-gates/06-smoke/00-launch.png` to `08-about.png` - smoke evidence, public conference data only (T-06-16)
- `.planning/phases/06-ios-test-foundation-and-ci-gates/deferred-items.md` - appended the "From 06-06" developer-environment entry

## Decisions Made

- Kept the 1.0.6 bump (commit 21b58bc) on the strength of the human approval; the revert path of D-12 was not needed.
- The Xcode GUI build failure was classified as an environment issue and logged as debt rather than fixed here (see below).

## Deviations from Plan

None to the deliverable: plan executed as written.

### Environment note (not a deviation)

The human's first attempt to build from the Xcode GUI failed for an environment reason, not a code regression. Xcode launched from the Dock does not inherit the shell `JAVA_HOME`, so the existing `./gradlew :shared:embedAndSignAppleFrameworkForXcode` Run Script fell back to `/usr/libexec/java_home`, whose default is an x86_64-only Corretto 11. Rosetta is not installed, hence "Bad CPU type in executable". The user fixed it with `launchctl setenv JAVA_HOME "/Applications/Android Studio.app/Contents/jbr/Contents/Home"` (not persistent across reboot). A durable fix (registering an ARM64 JDK as the system default, or making the Run Script resolve `JAVA_HOME`) is out of scope for Phase 6 and is logged in `deferred-items.md`. The smoke itself ran on the CLI-built app, so this issue does not affect the parity evidence.

### Deletions

None.

## Issues Encountered

- Xcode GUI build failed on the x86_64-only default JDK (see the environment note); resolved for the session with `launchctl setenv`, deferred as a developer-environment follow-up.
- The smoke ran on iPhone 18 Pro (iOS 27.0) rather than the iPhone 17 simulator of Task 1; the build is identical (1.0.6 Debug). The plan's flagged assumption (iOS 27 locally versus iOS 26.x on CI) still holds.

## User Setup Required

None - no external service configuration required. (Optional: re-run `launchctl setenv JAVA_HOME ...` after a reboot if building from the Xcode GUI; see deferred-items.md.)

## Known Stubs

None.

## Threat Flags

None. T-06-15 mitigated: blocking-human checklist with one screenshot per step, never auto-approved, approval recorded. T-06-16 accepted: screenshots show public conference data only; the real `GoogleService-Info.plist` remains gitignored and is not committed. T-06-SC: no package or action added in this plan.

## Next Phase Readiness

- CONC-03 is complete: the SPM pin is 1.0.6, guarded by the version-skew gate, and behavior parity with v1.0 is human-approved.
- Phase 6 has no remaining plan; phase verification can run. Manual-only coverage (no automated UI test before Phase 12) is recorded under D2 as human judgment.

## Self-Check: PASSED

- Nine PNGs present under `06-smoke/` (00-launch to 08-about).
- Plan base `642a559` is an ancestor of HEAD; bump commit `21b58bc` is an ancestor of HEAD and remains in history (not reverted).
