---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 16
subsystem: ios-parity-gate
tags: [ios, parity, uat, maestro, checkpoint]
requires:
  - phase: 09-15
    provides: in-place update UAT approved; baseline worktree at main
provides:
  - 31 paired main-vs-branch screenshots (D-10), default size, accessibility size and French
  - Full phase gate green on the branch head
  - Human verdict at the D-10 checkpoint, with every difference classified
affects: [verify-phase]
tech-stack:
  added: []
  patterns: [two identical simulators with status-bar override, paired Maestro captures, pixel diff then visual review]
key-files:
  created:
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/parity-flow.yaml
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/parity-a11y-flow.yaml
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/parity-fr-flow.yaml
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/base/ (31 PNGs)
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/branch/ (31 PNGs)
  modified:
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-CONTEXT.md
    - documentation/ARCHITECTURE.md
key-decisions:
  - "The agenda position offsets after a bookmark change in a detail (pairs 06, 07, 22, 27) are a new user decision, accepted at the checkpoint and recorded in 09-CONTEXT and the ARCHITECTURE.md deviations table (D-10)"
requirements-completed: [IOSDATA-01, IOSDATA-03]
status: complete
completed: 2026-11-07
commits: 2
plan_head_before: 0058136f03035a7cf4096def1570ae27c6325e5a
plan_head_after: b55823d4ea8912512c820fff05e6dcbd62d4c7f8
---

# Phase 9 Plan 16: Parity and Phase Gate Summary

**The same 31 screens were captured on `main` and on the branch on two identical simulators. 20 pairs are pixel-identical. The 11 that differ were classified by the human: in-slot room order, or small layout offsets the human accepted as a new decision. The full gate is green on the branch head, with 72/72 Swift tests, and `ios.yml` and `android.yml` are green on the pushed head SHA.**

## Verdict

**approved**, typed by the human in the orchestrator session at the D-10 blocking-human checkpoint, after comparing the pairs: "Some positions differ a bit but branch looks better than base."

The executor finished Tasks 1 and 2 and returned the checkpoint. It declined to act on the approval because it came as a relayed message, which is correct for a blocking-human gate. So the orchestrator, which received the approval directly, wrote this SUMMARY and the two records of the new accepted deviation.

## What was tested

- Baseline: `main` at `1077fd84`, built in the detached worktree from 09-15. The worktree was removed at the end of Task 2.
- Branch under test: `0058136f`. The evidence commits `461bf14d` and `b55823d4` add files under `09-parity/` only.
- Simulators, both iPhone 17 on iOS 27.0, status bar fixed at 9:41:
  - "DevFest Parity Base" `06CEA786-09B5-4FF6-BFAB-8FAFD9D63DA4`
  - "DevFest Parity Branch" `40D5EC6A-0D40-4EED-9266-ED51D9A9B0DB`
- Maestro 1.35.0. The first driver start on the base simulator needed two identical retries.

## Classification of the differing pairs

| Pairs | Difference | Classification |
|---|---|---|
| 02, 12, 23 | Different talks at the top of a slot | (a) In-slot room order, `RoomSortIndex`, accepted |
| 03, 08, 09, 14 | A different second talk is starred or filtered, because the flow stars the first two talks it sees | Knock-on of (a), accepted |
| 06, 07, 22 | After a bookmark change in a detail, the list on return sits about 35 points higher than on `main` | New user decision, accepted at this checkpoint |
| 07, 27 | Filter menu panel a few points further left, same contents and order | New user decision, accepted at this checkpoint |

- Pixel-identical pairs: 01, 04, 05, 10, 11, 13, 15, 16, 17, 18, 19, 20, 21, 24, 25, 26, 28, 29, 30 and 31. Pair 31 is the French day picker, showing `16 Octobre 2025` on both builds.
- The Rooms submenu order did not differ in these runs.
- The likely cause of the offsets, not confirmed: the agenda now observes the combined flow, so a bookmark change re-emits it and the list redraws.
- The new decision is recorded in 09-CONTEXT under "Accepted deviation (user decision at the 09-16 D-10 checkpoint)" and as a row of the `Behavior deviations (v1.1)` table in `documentation/ARCHITECTURE.md`.

## Gate on the branch head (Task 2)

- `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest :shared:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test`: BUILD SUCCESSFUL.
- Gate tokens:
  - `DI-GATE-OK`
  - `KMPNC-VERSION-OK 1.0.6`
  - `SWIFT-OBS-GATE-OK` and `SWIFT-OBS-GATE-SELFTEST-OK`
  - `SWIFT-NAMES-OK` and `SWIFT-GATE-SELFTEST-OK`
- Sweeps, all clean:
  - IOSUI-02: no unstructured `Task` in the app sources.
  - IOSDATA-01: `SessionFilter.swift` is deleted, and no `AgendaSessionFilters`, `forKey: "Favorites"` or Swift `FilterType` remains.
- Text-layout check:
  - `SpeakersView.swift` and `VenueView.swift` are unchanged since the merge-base.
  - `AgendaDetailView.swift` has the same layout modifiers as the branch point.
- Swift suite on iPhone 17 (F59F5246): `XCRESULT-TESTS total=72 passed=72 failed=0 skipped=0`, `XCRESULT-OK`.

## CI: green on the pushed head

- The human pushed `feature/reno_phase_9` at `d5f5efb61b716780e6dd6ccc61d73df26e0f1ff9`, and the orchestrator opened PR #433 to `main` at the human's request.
- `ios.yml` run `42419842682`: `gh run view` prints `d5f5efb61b716780e6dd6ccc61d73df26e0f1ff9 success`. The iOS log shows `XCRESULT-TESTS total=72 passed=72 failed=0 skipped=0` and `XCRESULT-OK`.
- `android.yml` run `42419842760`: `d5f5efb61b716780e6dd6ccc61d73df26e0f1ff9 success`.
- This SUMMARY update is docs-only and comes after the verified SHA.

## Deviations from Plan

1. **Step 10 target.** The Phase 8 text target `OpenRewrite: Refactor as code` is not on screen on the branch, because a different second talk is starred. The flow taps the single remaining Favorites card by position (`40%,37%`), and the screenshots show which talk it is.
2. **Accessibility flow dismissal.** `50%,3%` did not close the Rooms menu at accessibility-extra-large, so the flow taps `50%,80%`.
3. **Pixel diff tool.** ImageMagick on this machine is a broken x86 binary, so the pairs were diffed with a small Swift tool in the scratchpad.
4. **SUMMARY author.** The orchestrator wrote this file, for the reason given under Verdict.

No flow edit hides a difference: both full flows passed on both builds, and every differing pair was shown to the human.

## Execution notes

- **Split commands.** The plan's `<automated>` blocks were run as their component steps, because the worktree command guard refuses compound shell.
- **Secrets.** The API key precondition was a comparison only. No secret, `local.properties` or `build/` file is tracked.
- **Simulators.** The two parity simulators were deleted after CI was verified. The iPhone 17 simulator is kept.

## Self-Check: PASSED

- All 31 screen names exist in both `base/` and `branch/`, and are tracked (checked by the executor before its checkpoint).
- `b55823d4` is an ancestor of HEAD after the merge.
- The new accepted deviation is in 09-CONTEXT and `ARCHITECTURE.md`.
