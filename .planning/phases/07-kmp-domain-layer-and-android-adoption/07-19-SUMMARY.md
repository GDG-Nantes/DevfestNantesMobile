---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 19
subsystem: testing
tags: [android, parity, uat, r8, screenshots, android-cli, checkpoint]

requires:
  - phase: 07-kmp-domain-layer-and-android-adoption
    provides: "07-14 to 07-18 Android adoption of :core:domain and the shared stores"
provides:
  - "15 paired main/branch Android screenshots proving visual parity (D-12)"
  - "R8 release persistence smoke evidence for the moved @Serializable SessionFilter"
  - "Green full phase gate on the branch head"
  - "Recorded human approval of the Android parity comparison"
affects: [phase-08, phase-09, verify-work]

actuals:
  tokens: 450
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Parity evidence: identical android-CLI script run against a main build and a branch build on one emulator, compared pixel-wise below the status bar, then human-approved"

key-files:
  created:
    - .planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/CAPTURE-NOTES.md
    - .planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/main/ (15 PNGs, 01-day1-top to 15-venue)
    - .planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/branch/ (15 PNGs, same names)
    - .planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/release/ (01-before-relaunch, 02-after-relaunch, 03-after-relaunch-top)
  modified: []

key-decisions:
  - "Android parity with v1.0 approved by the human on 2026-10-29 from 15 paired screenshots and the R8 release smoke; no phase-caused difference, no new debt"

patterns-established:
  - "Blocking-human parity checkpoint backed by an objective pixel comparison so the human reviews a short list of exceptions"

requirements-completed: [SHARED-06, SHARED-02, SHARED-05]

coverage:
  - id: D1
    description: "Android main vs branch screens are visually identical across agenda days, slots, room order, each filter type, favorites, home indicator, session navigation, speakers scroller and venue (15 pairs)"
    requirement: SHARED-06
    verification:
      - kind: automated_ui
        ref: "07-parity/main/*.png vs 07-parity/branch/*.png (pixel rows y >= 160 identical for 01 and 03-15; 02 differs by a 2 px scroll offset only)"
        status: pass
    human_judgment: true
    rationale: "D-12 (locked) requires a human to compare main and branch screenshots; approved by the human on 2026-10-29"
  - id: D2
    description: "R8-minified release build keeps the English language filter and a bookmark across a force-stop and relaunch, with no SerializationException or ClassNotFoundException in logcat"
    requirement: SHARED-02
    verification:
      - kind: manual_procedural
        ref: "07-parity/release/01-before-relaunch.png, 02-after-relaunch.png, 03-after-relaunch-top.png; adb logcat -d grep"
        status: pass
    human_judgment: true
    rationale: "Release smoke result reviewed and approved by the human with the parity comparison"
  - id: D3
    description: "Full phase gate green on the branch head (detekt, lint, di-gate, kmp-nc gate, JVM/Android/iOS-simulator tests, swift-names check and selftest, Swift tests, Android smoke instrumentation tests)"
    requirement: SHARED-05
    verification:
      - kind: integration
        ref: "./gradlew detekt lint; scripts/di-gate.sh; scripts/kmp-nc-version-gate.sh; testDebugUnitTest jvmTest testAndroidHostTest; iosSimulatorArm64Test; scripts/swift-names-gate.sh check/selftest; xcodebuild test (19/19 Swift tests, XCRESULT-OK); connectedDebugAndroidTest agenda/speakers/venue (5/5)"
        status: pass
    human_judgment: false

duration: n/a (spans an orchestrator checkpoint)
completed: 2026-10-29
status: complete
plan_head_before: 4af25ed86b286110785b60ddf43dc7fc72d1f5e4
plan_head_after: 69d54e7f40395efa52057e63b6a1166353f55ba9
---

# Phase 7 Plan 19: Android Parity Proof and Phase Gate Summary

**Visual Android parity with v1.0 proven by 15 paired main/branch emulator screenshots (pixel-identical below the status bar except one 2 px scroll offset), an R8 release persistence smoke for the moved SessionFilter, a fully green phase gate, and the human approval of D-12.**

## Performance

- **Duration:** spans an orchestrator checkpoint (Task 1 executed in a worktree, Task 2 approved by the human in the main session)
- **Completed:** 2026-10-29
- **Tasks:** 2 (1 auto, 1 blocking-human checkpoint)
- **Files modified:** 34 evidence files (15 main + 15 branch PNGs, 3 release PNGs, CAPTURE-NOTES.md); no source file touched

## Accomplishments

- Captured the same scripted 15-screen sequence on a `main` debug build (254f5722) and on the branch debug build (4af25ed8) on one Pixel_9 emulator (Android 16 image, 1080x2424, en-US, Europe/Paris, clock never shifted) against live API data in the same sitting.
- Objective comparison: pairs 01 and 03-15 are pixel-identical below the status bar (y >= 160). Pair 02 differs by a 2 px scroll offset only (same sessions, slot headers and room order), a touch-slop artifact of the scripted drag, not a behavior difference.
- R8 release smoke (T-07-27): a release build signed with the local debug keystore kept the English language filter and a bookmark (11:10 AM, Kate Smith) after force-stop and relaunch; `adb logcat -d` held zero `SerializationException` / `ClassNotFoundException` lines.
- Full automated gate green on the branch: detekt, lint, di-gate, kmp-nc version gate, JVM/Android/iOS-simulator tests, swift-names check and selftest, 19/19 Swift tests (XCRESULT-OK), Android smoke instrumentation tests 5/5. `versionName` / `versionCode` unchanged.
- Indicative cold start (`am start -W` after force-stop, favorites filter on, emulator under host memory pressure): main 13711 ms, branch 12449 ms. Not a regression; indicative only.

## Human Approval (Task 2, checkpoint:human-verify, gate=blocking-human)

The orchestrator presented all 15 pairs and the release screenshots (with paths) to the user. The user's reply, verbatim:

> approved

Date: 2026-10-29. No difference was reported, so there is no phase-caused difference to fix and no pre-existing difference to log as debt (T-07-28 mitigated: a human compared the pairs, never auto-approved).

## Task Commits

1. **Task 1: capture main vs branch screenshots and the R8 release smoke, then run the full gate** - `e7e2a11` (test), merged into `feature/reno_phase_7` via `69d54e7f`
2. **Task 2: human comparison (checkpoint)** - no commit; approval recorded in this SUMMARY

**Plan metadata:** the docs commit that adds this SUMMARY (see git log, `docs(07-19)`)

## Files Created/Modified

- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/main/*.png` - 15 "before" screenshots (main)
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/branch/*.png` - 15 "after" screenshots (branch)
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/release/*.png` - R8 release persistence evidence (3 PNGs)
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/CAPTURE-NOTES.md` - emulator, builds and comparison method

## Decisions Made

- Android parity approved by the human on 2026-10-29; Roadmap Phase 7 criterion 5 ("Android behaves exactly as in v1.0") satisfied.
- Emulator housekeeping (not evidence): the data partition was 94% full, so updates of YouTube, YouTube Music, Photos, Gmail, Docs and Maps were uninstalled with `pm uninstall-system-updates` to be able to install the APKs.

## Deviations from Plan

None - plan executed exactly as written. (The `commits` actual of 2 is the measured `git rev-list --count 4af25ed8..HEAD`: the task commit plus the executor-worktree merge commit.)

## Issues Encountered

- Emulator storage was nearly full; resolved by removing app updates (see Decisions Made). No effect on the evidence.
- Screenshot 02 differs from its pair by 2 px of scroll offset; classified as a scripted-drag artifact, confirmed acceptable by the human.

## Known Stubs

None.

## Threat Flags

None - evidence only, screens show public conference data; the temporary main worktree (with copied local secrets) was removed.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 7 plan work is complete; ready for phase-level verification (`/gsd-verify-work`) and then Phase 8.
- No blockers; no new debt logged by this plan.

---
*Phase: 07-kmp-domain-layer-and-android-adoption*
*Completed: 2026-10-29*

## Self-Check: PASSED

- 07-parity directory tracked with 34 files (>= 32 required by the acceptance criteria)
- Task commit `e7e2a11` is an ancestor of HEAD
