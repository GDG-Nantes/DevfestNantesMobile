---
phase: 09-ios-on-shared-logic-and-legacy-persistence
plan: 15
subsystem: ios-persistence-uat
tags: [ios, uat, maestro, persistence, checkpoint]
requires:
  - phase: 09-14
    provides: documentation and IOSDATA-01 sweep; the branch under test
provides:
  - In-place update UAT evidence: legacy keys untouched, new keys empty at start then persisted over two relaunches
  - Seed and verify Maestro flows reusable by 09-16
affects: [09-16]
tech-stack:
  added: []
  patterns: [simctl install over a seeded build, plist extraction before and after, separate seed and verify flows]
key-files:
  created:
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update-seed-flow.yaml
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update-verify-flow.yaml
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/evidence.txt
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/legacy-before.txt
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/legacy-after.txt
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/u01-main-two-stars.png
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/u02-main-favorites-filter.png
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/u03-after-update-empty.png
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/u04-filter-menu-no-clear.png
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/u05-new-values-set.png
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/u06-after-relaunch.png
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-parity/update/u07-after-second-relaunch.png
  modified: []
key-decisions:
  - "Baseline is main (Phase 8 merge 1077fd84); the iOS_1_0_3 tag pass was not requested and was not run (Open Question 4)"
  - "The seed flow uses a plain launchApp and starts from nothing through one uninstall and install of the main build before any data existed; clearState is dropped because it loses the data at the next simctl install"
requirements-completed: [IOSDATA-02]
status: complete
duration: about 1h (including a discarded first attempt)
completed: 2026-11-07
commits: 1
plan_head_before: 3af70f7abd6aed2f18d905a2cc9856495d9d29f1
plan_head_after: a98a9cbe
actuals:
  tokens: 12000
  tasks: 3
  commits: 1
---

# Phase 9 Plan 15: In-place update UAT Summary

**On a real `simctl install` of the branch over a seeded main build, the legacy `Favorites` and `AgendaSessionFilters` keys survived byte-identical and unread, the app started with no bookmarks or filters, and the new `devfest.*` keys set afterwards persisted over two kill-and-relaunch cycles. The human approved at the D-09 checkpoint.**

## Verdict

**approved**, from the human at the D-09 blocking-human checkpoint (Task 3). They reviewed the screenshots and `evidence.txt` and did not ask for the `iOS_1_0_3` tag pass, so no tag pass was run.

## What was tested

- Baseline: `main` at `1077fd84` (Phase 8 merge), built in the detached worktree `/var/folders/wd/xqr_68816cj2d8jq5m_wcdv00000gn/T/devfest-parity-base`, which stays in place for 09-16.
- Branch under test: `3af70f7a` (identical to the feature branch tip), built in this worktree with `-derivedDataPath build`.
- Simulator "DevFest Update UAT", iPhone 17, iOS 27.0, UDID `EFB89B46-6222-4A6E-8AFD-1B60D87D39C7`. Maestro 1.35.0 drove it.
- Evidence commit: `a98a9cbe`.

## Results

- Seed (main build): `Favorites` = `keynotedouverture`, `openrewriterefactorascode`; `AgendaSessionFilters` holds the bookmark filter; the new keys were absent.
- After `simctl install` of the branch over it (no uninstall, no wipe, no reset): the legacy plist was present with the same values; `cmp legacy-before.txt legacy-after.txt` is identical, so the legacy data was not read into the new keys and not removed (07 D-15).
- Verify flow (Maestro exit 0, every assertion held): u03 full agenda and no filled star, u04 menu without Clear, no alert or spinner about the reset; u05 one bookmark and the Favorites filter set; u06 and u07 both kept after kill and relaunch (idempotent).
- AFTER keys: `devfest.bookmarks.sessionIds` = `keynotedouverture`; `devfest.agenda.filters` = the shared codec's BOOKMARK string.
- Edge IOSDATA-02 idempotency: the second relaunch (u07) matches the first (u06). Edge concurrency: the tap-then-kill steps of the flow kept their writes.

## Deviations from Plan

**1. [Rule 3 - Blocker] Seed flow uses a plain `launchApp`, not `clearState: true`**
- **Found during:** Task 2, first attempt.
- **Issue:** after seeding with Maestro `clearState: true`, `simctl install` of the branch left an empty container: the old container moved to the `containermanagerd` Dead area and the legacy values were lost. This broke the plan's assumption A1 as written. I stopped and reported it, committing nothing.
- **Diagnosis (orchestrator, marker file in the data container, then `simctl install`):**

  | scenario | path | marker |
  | -------- | ---- | ------ |
  | never launched, main to branch | changed | KEPT |
  | `simctl launch`, main to same main | changed | KEPT |
  | `simctl launch`, main to branch | changed | KEPT |
  | Maestro `launchApp` (no clearState), main to branch | changed | KEPT |
  | Maestro `launchApp` with `clearState: true`, main to branch | changed | LOST |

  So A1 holds for the data: `simctl install` keeps it but moves it to a new container UUID on this runtime. `clearState` is what loses it.
- **Fix (orchestrator decision):** `update-seed-flow.yaml` now uses a plain `launchApp`. "The seed starts from nothing" comes from one `simctl terminate`, `uninstall` and `install` of the main build, done once before any data existed. After the seed there was no uninstall, wipe or state reset before the branch install, and the verify flow has no state reset (RESEARCH Pitfall 8 intact).
- **Files modified:** `update-seed-flow.yaml`. **Commit:** `a98a9cbe`.

**2. Discarded first attempt.** The first seed (with `clearState`) and its `evidence.txt` and screenshots came from a different container and were overwritten. They were never committed.

**3. Maestro driver start timeout.** The very first run on the new simulator failed with `IOSDriverTimeoutException`; one identical retry passed. No flow was edited.

**Total deviations:** 1 blocker fix (plus two notes). **Impact:** the UAT still proves what it was meant to: the legacy values are present before and byte-identical after the install, and the human judged the evidence.

## Notes for the reader

- **Container path:** the BEFORE and AFTER data container paths differ (`593D3831-…` then `7466DBEF-…`). That is expected on this runtime (iOS 27.0, Xcode 27.2); the unchanged legacy values are the proof the data was kept (Task 3 step 4). `evidence.txt` carries the diagnosis in a NOTE.
- **u01 and u02 star split:** u01 shows the Opening Keynote star filled with the OpenRewrite star just below the fold, so the two filled stars appear together only in u02 (Favorites filter). The human saw this and approved it. I did not re-seed to change it, because that would have needed a second reset.

## Execution notes

- The plan's `<automated>` blocks were run as their component steps, split into plain commands and scratchpad scripts, with identical checks and outcomes, because the worktree command guard refuses compound shell naming git or computed arguments. `git` was invoked as `/usr/bin/git`, and `/usr/bin/grep` was used for acceptance checks.
- Gitignored inputs (`local.properties`, `GoogleService-Info.plist`, `build/SourcePackages`) were copied into this worktree and the baseline worktree and never staged. The API key precondition was compared only: pass.
- The simulator "DevFest Update UAT" was deleted after approval. The iPhone 17 simulator was never touched. No build or test was left running.

## Known Stubs

None.

## Threat Flags

None. The committed evidence holds public session ids and filter names only; no secret is tracked (T-09-25 mitigated).

## Self-Check: PASSED

- `git ls-files` lists the 7 PNGs, `evidence.txt`, `legacy-before.txt` and `legacy-after.txt`.
- Both flows are tracked, the verify flow contains no `clearState`, and `cmp` of the legacy files is identical.
- Commit `a98a9cbe` is an ancestor of HEAD, and the tree was clean before this SUMMARY.
