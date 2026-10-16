---
phase: 01-ci-pipeline-fixed-optimized
plan: 01
subsystem: infra/ci
tags: [github-actions, ios, xcode, simctl, jq, concurrency, ci]

requires: []
provides:
  - Dynamic Xcode toolchain selection in ios.yml (version-sorted, no hardcoded bundle path)
  - Dynamic plain-iPhone simulator resolution in ios.yml (simctl JSON + jq, deduplicated across iOS runtimes)
  - steps.sim.outputs.device_name wired into the xcodebuild -destination argument
  - Top-level concurrency block on ios.yml canceling superseded in-progress runs
affects: [01-02, 01-03]

actuals:
  tokens: 611
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Runner-local tool resolution (Xcode version, simulator inventory) computed at run time via shell + jq, output plumbed through $GITHUB_OUTPUT / steps.<id>.outputs.<key>, never re-pinned into the workflow file"
    - "Anchored regex filtering (^iPhone [0-9]+$) plus isAvailable==true predicate to exclude stale/variant simulator records"

key-files:
  created: []
  modified:
    - .github/workflows/ios.yml

key-decisions:
  - "Followed D-01/D-02 exactly as locked in CONTEXT.md: no fallback/error-message step for empty simulator resolution — xcodebuild's own destination error is the accepted failure mode"
  - "Xcode selection uses sort -V over /Applications/Xcode_*.app rather than the marketplace action maxim-lobanov/setup-xcode, avoiding a new unaudited dependency per RESEARCH.md's Package Legitimacy Audit"

patterns-established:
  - "Shell-output plumbing: id: sim step writes device_name to $GITHUB_OUTPUT, consumed as ${{ steps.sim.outputs.device_name }} in a later step's double-quoted -destination value"

requirements-completed: [CI-01]

coverage:
  - id: D1
    description: "Dynamic Xcode selection (version-sorted /Applications/Xcode_*.app enumeration replaces hardcoded xcode-select pin)"
    requirement: "CI-01"
    verification:
      - kind: integration
        ref: "https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35225356756 — log line 'Selected Xcode: 26.6.0'"
        status: pass
    human_judgment: false
  - id: D2
    description: "Dynamic plain iPhone simulator resolution (simctl JSON + jq, anchored ^iPhone [0-9]+$, deduplicated, highest model wins) replaces hardcoded destination device name"
    requirement: "CI-01"
    verification:
      - kind: integration
        ref: "https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35225356756 — log line 'Resolved simulator: iPhone 17'"
        status: pass
    human_judgment: false
  - id: D3
    description: "iOS app compiles end-to-end on the runner-resolved toolchain and simulator (BUILD SUCCEEDED)"
    requirement: "CI-01"
    verification:
      - kind: integration
        ref: "https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35225356756 — status completed:success, log contains BUILD SUCCEEDED"
        status: pass
    human_judgment: false
  - id: D4
    description: "Top-level concurrency block (group keyed on github.workflow + github.ref, cancel-in-progress: true) cancels superseded in-progress iOS CI runs"
    verification:
      - kind: static
        ref: "ruby -ryaml gate confirming group interpolates github.workflow/github.ref, cancel-in-progress is boolean true, no queue key present"
        status: pass
    human_judgment: false

duration: 18min
completed: 2026-09-17
status: complete
---

# Phase 1 Plan 1: iOS CI Toolchain Fix Summary

**Replaced both hardcoded iOS CI pins (Xcode bundle path, simulator device name) with runner-time resolution via shell/jq, and proved the whole path green on a live macos-latest run (Xcode 26.6.0, iPhone 17, BUILD SUCCEEDED), plus added a workflow-level concurrency group.**

## Performance
- **Duration:** ~18min (dominated by the ~9min live GitHub Actions run)
- **Started:** 2026-09-17T13:03:00Z (approx, session start)
- **Completed:** 2026-09-17T13:21:00Z
- **Tasks:** 2 completed
- **Files modified:** 1 (`.github/workflows/ios.yml`)

## Accomplishments
- iOS CI job now resolves its Xcode toolchain and simulator device dynamically at run time instead of relying on hardcoded, image-rotation-fragile pins — root cause of CI-01's failure is fixed, not re-pinned.
- A live `workflow_dispatch` run on `feature/reno_phase_1` (run 35225356756) confirmed the fix end-to-end: `Selected Xcode: 26.6.0`, `Resolved simulator: iPhone 17`, and `BUILD SUCCEEDED`, with overall run conclusion `completed:success`.
- Added a top-level `concurrency` block so a new push to the same branch/PR cancels the previous in-progress iOS CI run (D-07), verified by a Ruby/YAML gate confirming the exact `group`/`cancel-in-progress`/no-`queue` shape.

## Task Commits
1. **Task 1: End-to-end iOS build on a runner-resolved toolchain** - `e9b4121` (feat)
2. **Task 2: Cancel superseded in-progress iOS runs (D-07)** - `1b23b83` (feat)

**Plan metadata:** (recorded in the following commit, see git_commit_metadata step)

## Files Created/Modified
- `.github/workflows/ios.yml` - Dynamic Xcode selection (`Select latest installed Xcode`), dynamic simulator resolution (`Resolve latest plain iPhone simulator`, `id: sim`), the build step's `-destination` now interpolates `steps.sim.outputs.device_name`, and a top-level `concurrency` block.

## Decisions Made
- No fallback/error-message step was added for an empty simulator-resolution result, per D-01's explicit instruction — `xcodebuild` fails on its own malformed destination if resolution ever yields nothing.
- Kept the shell/`jq`-based approach over the `maxim-lobanov/setup-xcode` marketplace action alternative, avoiding a new third-party dependency this phase's decisions did not request.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None. The live `workflow_dispatch` run passed on the first attempt: resolved Xcode 26.6.0 (the runner's actual latest installed version at execution time, newer than RESEARCH.md's snapshot of 26.3, confirming version-sort correctly tracks image drift), resolved simulator `iPhone 17`, and the build succeeded.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness

Plan 01-02/01-03 (Gradle/Konan caching, `android-setup` composite-action reuse, branch-aware cache policy) can proceed against this now-green iOS baseline. Recorded for comparison: pre-existing-broken baseline run was 34716324916; this plan's proving run is 35225356756 (Xcode 26.6.0, iPhone 17, `completed:success`). Plan 01-03 should compare its own live run's Xcode/simulator resolution against these same values to confirm the caching/setup-reuse changes don't disturb toolchain resolution.

---
*Phase: 01-ci-pipeline-fixed-optimized*
*Completed: 2026-09-17*

## Self-Check: PASSED
- FOUND: .planning/phases/01-ci-pipeline-fixed-optimized/01-01-SUMMARY.md
- FOUND: e9b4121 (Task 1 commit)
- FOUND: 1b23b83 (Task 2 commit)
