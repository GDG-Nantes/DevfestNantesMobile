---
phase: 01-ci-pipeline-fixed-optimized
plan: 02
subsystem: infra/ci
tags: [github-actions, android, gradle, cache, concurrency, ci]

requires: []
provides:
  - Branch-aware Gradle cache-write policy (`${{ github.event_name == 'pull_request' }}`) at all four android-setup call sites in android.yml
  - Top-level concurrency block on android.yml canceling superseded in-progress runs
affects: [01-03]

actuals:
  tokens: 412
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Branch-aware cache-write policy keyed on `github.event_name` rather than parsed `github.ref`, computed at the composite-action call site (input defaults on composite actions are static strings, not expressions)"

key-files:
  created: []
  modified:
    - .github/workflows/android.yml

key-decisions:
  - "Followed D-04 exactly: replaced three hardcoded read-only literals and one hardcoded writable literal (build-debug) with the identical branch-aware expression at all four call sites, closing the pre-existing exposure where build-debug wrote the shared Gradle cache on every pull_request run"
  - "Left .github/actions/android-setup/action.yml untouched per the plan's explicit platform constraint — the expression must live at each call site, not the composite action's input default"
  - "Duplicated the concurrency block shape from Plan 01's ios.yml change rather than sharing it, per D-03 (android.yml and ios.yml stay separate files; github.workflow differs per file so the two land in distinct concurrency groups)"

patterns-established:
  - "Uniform branch-aware cache policy across all call sites of a shared composite action, verified by a content-based (not diff-based) YAML gate that rejects any surviving hardcoded boolean"

requirements-completed: [CICD-01]

coverage:
  - id: D1
    description: "All four android-setup call sites (checks, unit-tests, instrumentation-tests, build-debug) pass the identical branch-aware cache-read-only expression; no hardcoded boolean survives"
    requirement: "CICD-01"
    verification:
      - kind: static
        ref: "ruby -ryaml gate: parses android.yml, asserts 4 jobs each call android-setup with cache-read-only referencing both github.event_name and pull_request, and java-version derived from JAVA_VERSION — printed 'android cache policy ok (4 call sites)'"
        status: pass
    human_judgment: false
  - id: D2
    description: "The composite action (.github/actions/android-setup/action.yml) is untouched: still forwards inputs.cache-read-only to setup-gradle, contains no github.event_name reference"
    requirement: "CICD-01"
    verification:
      - kind: static
        ref: "ruby -ryaml content-based gate — printed 'composite action intact'"
        status: pass
    human_judgment: false
  - id: D3
    description: "Top-level concurrency block (group: github.workflow-github.ref, cancel-in-progress: true, no queue key) added to android.yml"
    requirement: "CICD-01"
    verification:
      - kind: static
        ref: "ruby -ryaml gate — printed 'concurrency ok'"
        status: pass
    human_judgment: false
  - id: D4
    description: "Workflow still declares exactly 4 jobs, all on ubuntu-latest, on: triggers unchanged, no workflow_dispatch added"
    requirement: "CICD-01"
    verification:
      - kind: static
        ref: "ruby -ryaml gate — printed 'android jobs ok'; full-file parse also confirmed with 'yaml parses ok'"
        status: pass
    human_judgment: false
  - id: D5
    description: "Live confirmation on the phase pull request: Android CI workflow run concludes successfully across all four jobs, and the Setup Gradle step summary on that PR run reports the cache as read-only"
    verification: []
    human_judgment: true
    rationale: "No phase pull request exists yet (this is wave 1, plan 02 of 3; `gh pr list --head feature/reno_phase_1` returned empty at execution time). android.yml has no workflow_dispatch trigger, so unlike Plan 01's ios.yml this cannot be triggered directly outside a real push/pull_request event. This is the plan's own documented human-check, explicitly scoped to 'the end-of-phase pull request to main' — deferred to phase-level verification (/gsd-verify-work or /gsd-ship), which should confirm the Android CI run is green and its Setup Gradle summary reports read-only cache before merge."

duration: 2min
completed: 2026-09-17
status: complete
---

# Phase 1 Plan 2: Android Gradle Cache Policy & Concurrency Summary

**Collapsed android.yml's four per-job hardcoded Gradle cache-write flags (three read-only, one writable) onto a single branch-aware expression, closing the exposure where `build-debug` wrote the shared cache on every PR run, and added a workflow-level concurrency group to cancel superseded runs.**

## Performance
- **Duration:** ~2min
- **Started:** 2026-09-17T13:28:55Z
- **Completed:** 2026-09-17T13:30:29Z
- **Tasks:** 2 completed
- **Files modified:** 1 (`.github/workflows/android.yml`)

## Accomplishments
- All four `android-setup` call sites in `android.yml` (`checks`, `unit-tests`, `instrumentation-tests`, `build-debug`) now resolve `cache-read-only` from the identical expression `${{ github.event_name == 'pull_request' }}`, replacing three hardcoded `'true'` literals and one hardcoded `'false'` literal — the latter was the actual security-relevant fix, since `build-debug` previously wrote the shared Gradle cache from every pull_request run (D-04).
- Confirmed `.github/actions/android-setup/action.yml` is untouched: it still forwards its own `inputs.cache-read-only` to `gradle/actions/setup-gradle` and contains no `github.event_name` reference, so the branch-aware expression correctly stays at each call site rather than being coerced to an always-truthy static string inside the composite action's input default.
- Added a top-level `concurrency` block (`group: ${{ github.workflow }}-${{ github.ref }}`, `cancel-in-progress: true`, no `queue` key) so a new push to a branch cancels the previous in-progress Android CI run (D-07), duplicated independently from Plan 01's equivalent block in `ios.yml` per D-03 — the two files stay separate and land in distinct concurrency groups since `github.workflow` differs per file.

## Task Commits
1. **Task 1: Branch-aware Gradle cache policy at all four call sites (D-04)** - `89ef704` (fix)
2. **Task 2: Cancel superseded in-progress Android runs (D-07)** - `20faa55` (feat)

**Plan metadata:** (recorded in the following commit, see git_commit_metadata step)

## Files Created/Modified
- `.github/workflows/android.yml` - All four `android-setup` call sites now pass `cache-read-only: ${{ github.event_name == 'pull_request' }}`; added a top-level `concurrency` block between `on:` and `env:`.

## Decisions Made
- Kept the branch-aware expression at each of the four call sites rather than moving it into the composite action's input default, per the plan's explicit platform-constraint instruction (composite-action metadata is parsed statically; an expression there would be forwarded to `setup-gradle` as raw uninterpolated text and coerced to an always-truthy string).
- Did not add a `queue` key alongside `cancel-in-progress: true` — GitHub documents that combination as a hard validation error that rejects the entire workflow file.
- Did not add a `workflow_dispatch` trigger to `android.yml` — no decision in this phase asked for one, and the plan explicitly calls this out as unaffected in practice for this file (unlike `ios.yml`, which already has one from Plan 01).

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None. Both Ruby/YAML verification gates for each task passed on the first attempt; the full-file YAML parse and the four-jobs-on-ubuntu-latest gate also passed cleanly after both edits.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness

Task 2's `human-check` — confirming the Android CI workflow run on the phase pull request concludes successfully across all four jobs and that its `Setup Gradle` step summary reports a read-only cache — is explicitly scoped by the plan to "the end-of-phase pull request to main." No such PR exists yet (`gh pr list --head feature/reno_phase_1` returned empty at execution time), and `android.yml` has no `workflow_dispatch` trigger to dispatch it directly (a deliberate scope boundary — adding one is outside this phase's decisions). This is deferred to phase-level verification (`/gsd-verify-work` or `/gsd-ship`), which should confirm the live run is green and the cache reports read-only on that PR before merge. Recorded here so phase verification does not have to re-derive the deferral reason. Plan 01-03 (iOS caching / Konan) can proceed independently — this plan touches only `android.yml`.

---
*Phase: 01-ci-pipeline-fixed-optimized*
*Completed: 2026-09-17*

## Self-Check: PASSED
- FOUND: .planning/phases/01-ci-pipeline-fixed-optimized/01-02-SUMMARY.md
- FOUND: 89ef704 (Task 1 commit)
- FOUND: 20faa55 (Task 2 commit)
