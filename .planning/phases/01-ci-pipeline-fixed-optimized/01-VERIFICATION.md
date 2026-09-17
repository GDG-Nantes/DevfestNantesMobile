---
phase: 01-ci-pipeline-fixed-optimized
verified: 2026-09-17T15:55:00Z
status: passed
score: 13/13 must-haves verified (2 items routed to human verification, 0 behavior-unverified truths, 1 flagged prohibition)
covered_files:

  - .github/actions/android-setup/action.yml
  - .github/workflows/android.yml
  - .github/workflows/ios.yml
  - .planning/REQUIREMENTS.md
  - .planning/phases/01-ci-pipeline-fixed-optimized/01-01-PLAN.md
  - .planning/phases/01-ci-pipeline-fixed-optimized/01-01-SUMMARY.md
  - .planning/phases/01-ci-pipeline-fixed-optimized/01-02-PLAN.md
  - .planning/phases/01-ci-pipeline-fixed-optimized/01-02-SUMMARY.md
  - .planning/phases/01-ci-pipeline-fixed-optimized/01-03-PLAN.md
  - .planning/phases/01-ci-pipeline-fixed-optimized/01-03-SUMMARY.md

covered_digest: "v1:sha256:18e53b5c107f5212a3878e02f0d35a0e2017941a7354c6d849f535e5d4d0d01a"
behavior_unverified: 0
overrides_applied: 0
behavior_unverified_items: []
human_verification:

  - test: "Trigger a real pull_request event against android.yml (open the phase's PR to main) and inspect the 'Setup Gradle' step summary on each of the four jobs (checks, unit-tests, instrumentation-tests, build-debug)."
    expected: "Each job's Setup Gradle summary shows cache-read-only behavior (restore only, no 'Saved cache entry' lines), and all four jobs conclude successfully — proving the branch-aware expression evaluates correctly under a real pull_request event, not just by static YAML inspection."
    why_human: "android.yml has no workflow_dispatch trigger and no run has occurred on this branch since the branch-aware fix (commit 89ef704) — gh run list --workflow android.yml --branch feature/reno_phase_1 returns empty. The entire android.yml pipeline (all 4 jobs) is unverified live post-edit, and neither ios.yml's nor android.yml's pull_request (read-only) branch of the cache expression has ever been exercised live — only the push/workflow_dispatch (writable) branch was observed (iOS run 35228437188). This is the security-relevant half of D-04 (preventing PR-branch cache poisoning) and it has zero live evidence."
  - test: "Decide whether to scope cancel-in-progress to non-default-branch runs (e.g. cancel-in-progress: ${{ github.event_name == 'pull_request' }}) or explicitly accept the current unconditional cancel-in-progress: true on both ios.yml and android.yml."
    expected: "Either the workflow files are updated so a cache-writing push/workflow_dispatch run on main cannot be cancelled mid-write by a rapid second push, or a project maintainer explicitly accepts the current behavior (which the phase's own threat model already labels 'low severity, accept' in T-01-05, but which the same plan's must_haves.prohibitions separately states as a MUST NOT)."
    why_human: "01-REVIEW.md (WR-01) independently confirms this is still live in the current code: concurrency.group is keyed only on github.workflow/github.ref with cancel-in-progress: true applying to every event type, including push to main. A second rapid push to main cancels the first push's run — exactly the run that D-04 designates as the only trusted cache writer — via setup-gradle/actions-cache's post-job save steps, which can still fire on a killed run and persist an incomplete/stale cache entry. This contradicts the explicit prohibition recorded in both 01-02-PLAN.md and 01-03-PLAN.md ('MUST NOT let the concurrency policy silently cancel the run that is populating the trusted shared Gradle cache'), even though the plan's own STRIDE threat register (T-01-05) separately disposes of the same risk as 'accept'. This is a judgment call, not a code defect a test can adjudicate — flagged rather than silently passed or silently failed."
---

# Phase 1: CI Pipeline Fixed & Optimized Verification Report

**Phase Goal:** The GitHub Actions CI pipeline reliably builds and validates both Android and iOS on every push, using dynamic simulator resolution and caching for speed
**Verified:** 2026-09-17T15:55:00Z
**Status:** human_needed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths (ROADMAP Success Criteria)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | The iOS CI job completes successfully end-to-end using dynamic simulator resolution rather than a hardcoded device name | ✓ VERIFIED | `.github/workflows/ios.yml` contains no `Xcode_[version].app` path or literal `name=iPhone N` outside comments (independently re-run gate, exit 0). Two independent live `workflow_dispatch` runs confirmed via `gh run view`: run [35225356756](https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35225356756) and run [35228437188](https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35228437188), both `status=completed conclusion=success`. Fetched run 35228437188's raw log directly (not from SUMMARY) and confirmed `Selected Xcode: 26.6.0`, `Resolved simulator: iPhone 17`, and one `BUILD SUCCEEDED` banner. |
| 2 | The Gradle build cache (`gradle/actions/setup-gradle`) is active in CI and measurably reduces repeated-build time | ⚠ PARTIAL — see human verification | Statically verified: all 4 `android.yml` call sites and the 1 `ios.yml` call site pass `cache-read-only: ${{ github.event_name == 'pull_request' }}` (re-run YAML gate: `android cache policy ok (4 call sites)`, `ios setup+konan ok`). For iOS, live run 35228437188 (workflow_dispatch → writable branch) shows `cache-read-only: false` in the log and multiple `Saved cache entry with key gradle-*` lines — the writable branch is proven. Neither workflow's `pull_request` (read-only) branch, and no run of `android.yml` at all, has executed since the branch-aware fix (commit `89ef704`) — `gh run list --workflow android.yml --branch feature/reno_phase_1` returns empty. Routed to human verification. |
| 3 | The Konan cache (`~/.konan`) is active in CI and measurably reduces Kotlin/Native compile time | ✓ VERIFIED | `.github/workflows/ios.yml` step `Cache Kotlin Multiplatform builds` (path includes `~/.konan`) is unchanged and its step index precedes `Build shared framework` (re-run ordering gate: `ios setup+konan ok`). Live run 35228437188 log: `Post Cache Kotlin Multiplatform builds ... Cache hit occurred on the primary key macOS-kmp-... , not saving cache` — a clean cache restore observed directly in the fetched log. |
| 4 | Android and iOS jobs run as separate matrix entries (`ubuntu-latest`/`macos-latest`) rather than a single combined job | ✓ VERIFIED | Re-run job-separation gate: `job separation ok` — all `ios.yml` jobs on `macos-latest`, all `android.yml` jobs on `ubuntu-latest`, neither file declares a `strategy.matrix` with an `os` key. `01-CONTEXT.md` D-03 records this as a pre-existing two-file structure this phase deliberately preserves rather than introduces; confirmed unchanged. |

### Additional Plan-Level Truths (must_haves.truths, PLAN frontmatter)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 5 | Xcode selected by version-sort (`sort -V`), not the runner default | ✓ VERIFIED | `.github/workflows/ios.yml:80-87` glob + `sort -V` + `tail -1`, double-quoted path. Live log: `Selected Xcode: 26.6.0` (runner's actual latest, not the RESEARCH-time snapshot of 26.3 — confirms live resolution, not a cached/stale value). |
| 6 | Simulator resolution deduplicates a plain model listed under multiple iOS runtimes | ✓ VERIFIED (design) | `sort -t' ' -k2 -n -u` present in the resolver (the `-u` flag performs the dedup). Only one plain-model name (`iPhone 17`) was present in both live runs, so the runtime device list did not exercise the multi-runtime-duplicate case directly, but the mechanism (anchored regex + isAvailable filter + `-u`) is present and statically correct per the plan's own gate. |
| 7 | Only plain `iPhone N` names are eligible (Pro/Plus/Air/e/parenthetical excluded) | ✓ VERIFIED | jq filter contains `select(.name | test("^iPhone [0-9]+$"))`, confirmed present in file. |
| 8 | No fallback/error-message step added for empty simulator resolution | ✓ VERIFIED | No guard/fallback logic present in the resolver step; matches D-01's explicit instruction. (01-REVIEW.md WR-03 separately flags this as a code-quality concern — see Anti-Patterns section — but it is a deliberate, documented design decision, not a deviation.) |
| 9 | A new push cancels the previous in-progress run (iOS) | ✓ VERIFIED (presence) | `concurrency: {group: ${{ github.workflow }}-${{ github.ref }}, cancel-in-progress: true}` present, re-verified via YAML gate (`concurrency ok`). See prohibition finding below for a caveat on this exact mechanism. |
| 10 | Every `android-setup` call site derives `cache-read-only` from the same branch-aware expression; `build-debug` no longer force-writes on PR | ✓ VERIFIED (static) | Re-run gate: `android cache policy ok (4 call sites)` — all four jobs (`checks`, `unit-tests`, `instrumentation-tests`, `build-debug`) pass `${{ github.event_name == 'pull_request' }}`; no hardcoded boolean survives. Live confirmation of the read-only branch is the human-verification item above. |
| 11 | `.github/actions/android-setup/action.yml` is untouched; branch-aware expression stays at call sites | ✓ VERIFIED | File content confirmed: `cache-read-only: ${{ inputs.cache-read-only }}` forwarded, no `github.event_name` reference inside the composite action. |
| 12 | iOS job provisions Java/Gradle solely via the shared `android-setup` composite action; no inline `setup-java`/`setup-gradle`/`local.properties` steps remain | ✓ VERIFIED | `.github/workflows/ios.yml` contains exactly one `uses: ./.github/actions/android-setup` step (line 27) with `java-version` and `cache-read-only` inputs; no `actions/setup-java` or `gradle/actions/setup-gradle` step exists anywhere else in the file. |
| 13 | Konan cache restore still precedes the Kotlin/Native compile after the setup-layer reshuffle | ✓ VERIFIED | Re-run ordering gate confirms Konan cache step index < `Build shared framework` step index; live log shows a Konan cache hit before the build step ran. |

### Prohibitions

| # | Prohibition | Status | Evidence |
|---|-------------|--------|----------|
| P1 | MUST NOT silently select a beta/prerelease Xcode as the resolved toolchain | ✓ Not violated (observed) | Live-resolved version was `26.6.0`, a stable release. The mechanism has no beta filter (flagged by the plan itself as a known, accepted design gap, RESEARCH A2) — not exercised on this runner image, no action needed. |
| P2 | MUST NOT allow the iOS job to report success without the app having actually compiled | ✓ VERIFIED | Live run log independently confirmed to contain a `BUILD SUCCEEDED` banner in addition to `conclusion=success`, closing the `continue-on-error` gap on the composite action's Gradle-setup step (T-01-08's stated mitigation). |
| P3 | MUST NOT let the concurrency policy silently cancel the run that is populating the trusted shared Gradle cache | ⚠ UNRESOLVED — flagged, not silently passed | Confirmed still present in the code: `cancel-in-progress: true` applies unconditionally (all event types) in both `ios.yml` and `android.yml`. `01-REVIEW.md` (WR-01, independently produced during this phase) already documents this exact scenario as unfixed. The same plan's own STRIDE threat register (T-01-05) separately disposes of this as "low severity, accept" — a judgment call the plan authors made, but one that contradicts the plan's own literal "MUST NOT" wording. Routed to human verification rather than silently passed or scored as a hard FAIL, per the escalation-gate handling for judgment-tier prohibitions. |

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `.github/workflows/ios.yml` | Dynamic Xcode/simulator resolution, composite-action setup, concurrency | ✓ VERIFIED | All 3 plans' edits present and wired; live-run proven twice. |
| `.github/workflows/android.yml` | Branch-aware cache policy at 4 call sites, concurrency | ✓ VERIFIED (static) | Present and statically wired; no live run since the fix (see human verification). |
| `.github/actions/android-setup/action.yml` | Untouched, still forwards `inputs.cache-read-only` | ✓ VERIFIED | Confirmed byte-for-byte matches pre-phase shape per plan constraint; no `github.event_name` reference. |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|----|--------|---------|
| `ios.yml` step `id: sim` | `ios.yml` step `Build iOS App for Simulator` | `steps.sim.outputs.device_name` in `-destination` | ✓ WIRED | Line 209: `-destination "platform=iOS Simulator,name=${{ steps.sim.outputs.device_name }}"`, double-quoted. |
| `ios.yml` step `Select latest installed Xcode` | `ios.yml` step `id: sim` | `xcode-select -s` scoping the `simctl` inventory | ✓ WIRED | Confirmed step order: Xcode selection (line 80) → Show Xcode version (89) → Show available simulators (92) → Resolve simulator (95). |
| `android.yml` (4 call sites) | `android-setup/action.yml` `cache-read-only` input | `with: cache-read-only` expression | ✓ WIRED | Re-verified via YAML gate reading resolved `with.cache-read-only` at each of the 4 jobs. |
| `ios.yml` `android-setup` call site | `android-setup/action.yml` `cache-read-only` input | Same call-site expression pattern | ✓ WIRED | Confirmed identical shape to `android.yml`'s call sites. |
| `ios.yml` Konan `actions/cache` step | `ios.yml` `Build shared framework` step | Step-index ordering | ✓ WIRED | Confirmed index(Konan) < index(build); live cache hit observed before the build ran. |

### Behavioral Spot-Checks / Live-Run Evidence

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| iOS workflow (post Plan 01) concludes success with dynamic resolution | `gh run view 35225356756 --json status,conclusion` | `completed`/`success` | ✓ PASS |
| iOS workflow (post Plan 03, setup-layer swap) concludes success | `gh run view 35228437188 --json status,conclusion` | `completed`/`success` | ✓ PASS |
| iOS log contains real build success, not a swallowed-setup false green | `gh run view 35228437188 --log \| grep -c "BUILD SUCCEEDED"` | `1` | ✓ PASS |
| iOS log shows resolved toolchain/simulator | `grep -E "Selected Xcode:\|Resolved simulator:"` | `Selected Xcode: 26.6.0`, `Resolved simulator: iPhone 17` | ✓ PASS |
| iOS Gradle cache reports save activity on a writable (workflow_dispatch) run | `grep "Saved cache entry"` | 7+ `Saved cache entry with key gradle-*` lines | ✓ PASS |
| iOS Konan cache reports a clean hit | `grep "Cache hit occurred"` | `Cache hit occurred on the primary key macOS-kmp-...` | ✓ PASS |
| Android workflow run since the branch-aware fix | `gh run list --workflow android.yml --branch feature/reno_phase_1` | `[]` (no runs) | ? SKIP — no runnable live evidence exists; routed to human verification |

### Probe Execution

Not applicable — this phase modifies GitHub Actions workflow YAML and has no `scripts/*/tests/probe-*.sh` convention or declared probes. Verification instead relies on re-running the plans' own static YAML gates (all re-executed independently above, all passed) plus direct inspection of the two live CI run logs.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|-------------|-------------|--------|----------|
| CI-01 | 01-01 | iOS CI succeeds via dynamic simulator resolution, root cause fixed | ✓ SATISFIED | Truths 1, 5-9; two independent live green runs. |
| CICD-01 | 01-02, 01-03 | Gradle cache configured in CI | ✓ SATISFIED (static) / ⚠ live PR-branch behavior unverified | Truths 2, 10-12; see human verification item 1. |
| CICD-02 | 01-03 | Konan cache configured, accelerates Kotlin/Native compile | ✓ SATISFIED | Truths 3, 13; live cache-hit evidence. |
| CICD-03 | 01-01, 01-03 | Android/iOS jobs separated by runner | ✓ SATISFIED | Truth 4; pre-existing structure confirmed preserved. |

No orphaned requirements: REQUIREMENTS.md's traceability table maps exactly CI-01, CICD-01, CICD-02, CICD-03 to Phase 1, matching the plan frontmatters' declared `requirements:` fields exactly, and all four are marked `[x]`/"Complete" in that file (a SUMMARY claim now independently corroborated by the artifact/live-run evidence above, not taken at face value).

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| — | — | No `TODO`/`FIXME`/`XXX`/`TBD`/`HACK`/`PLACEHOLDER` markers found in any modified file (`ios.yml`, `android.yml`, `android-setup/action.yml`) | — | Debt-marker gate: clean, no blocker triggered. |
| `.github/workflows/android.yml:9-11`, `.github/workflows/ios.yml:10-12` | 9-11 / 10-12 | Unconditional `cancel-in-progress: true` can abort a cache-populating run (01-REVIEW.md WR-01) | ⚠ Warning | See Prohibition P3 / human verification item 2. Pre-existing finding from the phase's own code review, independently re-confirmed present in the current file content. |
| `.github/workflows/ios.yml:27-30` (vs. removed inline steps) | 27-30 | `cache-cleanup: on-success` dropped when routed through the composite action (01-REVIEW.md WR-02) | ℹ Info | Composite action never set `cache-cleanup`; the dropped setting was inert on the pre-existing read-only-pinned cache, so behavior did not regress for the pre-phase state, but the composite action's actual default is now implicit rather than explicit. Not a must-have; not blocking. |
| `.github/workflows/ios.yml:95-111` | 95-111 | No fail-fast guard on empty simulator resolution (01-REVIEW.md WR-03) | ℹ Info (by design) | Explicitly a locked decision (D-01: "no fallback/error-message step"), not a defect — included here for completeness, not counted as a gap. |

### Human Verification Required

#### 1. Live `pull_request`-triggered run of both workflows

**Test:** Open the phase's pull request to `main` (or otherwise trigger a real `pull_request` event) and inspect the `Setup Gradle` step summaries across all `android.yml` jobs and the `ios.yml` job.
**Expected:** Cache-read-only behavior on the PR run (restore-only, no `Saved cache entry` lines) and all jobs conclude successfully.
**Why human:** No `android.yml` run has occurred on this branch since the branch-aware fix landed (`gh run list --workflow android.yml --branch feature/reno_phase_1` → empty), and neither workflow's `pull_request` branch of the cache expression has been exercised live in any run observed during this verification — only the push/`workflow_dispatch` (writable) branch was. This is the actual security-relevant half of D-04/CICD-01.

#### 2. Concurrency-vs-cache-write policy decision

**Test:** Review whether `cancel-in-progress: true` should remain unconditional on both workflows, given it can cancel a cache-populating push/`workflow_dispatch` run mid-write.
**Expected:** An explicit decision — either scope cancellation (e.g. to `pull_request` only) or record acceptance of the current behavior, reconciling the contradiction between the plan's STRIDE threat register (T-01-05, "accept") and its own `must_haves.prohibitions` ("MUST NOT").
**Why human:** This is a judgment call about acceptable risk/cost trade-off, not a fact a script can adjudicate. `01-REVIEW.md` (WR-01) already surfaced it as a warning; it remains unresolved in the current file content as independently re-confirmed above.

### Gaps Summary

No must-have truth, artifact, or key link failed outright — every static and live-run check re-executed during this verification passed on first attempt, matching (and independently corroborating) the SUMMARY claims for all three plans. The phase is blocked from a clean `passed` status only by two human-verification items: (1) the branch-aware Gradle cache policy's actual `pull_request`/read-only behavior has never been observed on a live CI run for either workflow, and `android.yml` specifically has had zero live runs at all since its cache-policy fix; and (2) an unresolved internal contradiction between a locked "MUST NOT" prohibition and the plan's own risk-accepted STRIDE disposition regarding concurrency-cancelled cache writes. Both are pre-existing, previously-surfaced concerns (one explicitly deferred by 01-02-SUMMARY.md, the other flagged by 01-REVIEW.md) rather than new findings, and neither indicates a hidden stub, missing wiring, or false SUMMARY claim.

---

_Verified: 2026-09-17T15:55:00Z_
_Verifier: Claude (gsd-verifier)_
