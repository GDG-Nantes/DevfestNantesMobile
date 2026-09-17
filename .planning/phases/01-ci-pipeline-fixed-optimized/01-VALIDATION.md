---
phase: "1"
slug: "ci-pipeline-fixed-optimized"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-09-17"
---

# Phase 1 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | None. The artifacts under test are GitHub Actions workflow files, not code a test framework consumes. Validation is two-tier: structural assertions over the parsed YAML (fast, local, deterministic) plus live workflow runs (the only true end-to-end proof). |
| **Config file** | none — `.github/workflows/*.yml` and `.github/actions/*/action.yml` are the artifacts under test |
| **Quick run command** | `ruby -ryaml -e 'YAML.load_file(".github/workflows/ios.yml"); YAML.load_file(".github/workflows/android.yml")'` plus the per-task structural gates in each PLAN.md |
| **Full suite command** | `gh run list --branch "$(git branch --show-current)" --limit 2 --json name,conclusion` — both `Android CI` and `iOS CI` must read `success` |
| **Estimated runtime** | structural gates ~1 second; a live iOS run is 20-30 minutes wall-clock (it exceeds a single foreground command budget — watch it in the background or poll `gh run view --json status`) |

Ruby's bundled Psych YAML parser is the structural-gate substrate. It was chosen because it is
present on this machine and on macOS by default, whereas PyYAML is absent and `actionlint` is not
installed and is not required by any locked decision.

---

## Sampling Rate

- **After every task commit:** run that task's structural gate (sub-second).
- **After every plan wave:** for any plan touching `ios.yml`, dispatch a live run with
  `gh workflow run ios.yml --ref "$(git branch --show-current)"` and assert `completed:success`
  plus a `BUILD SUCCEEDED` banner in the log.
- **Before `/gsd-verify-work`:** both workflows green on the phase pull request to `main`.
- **Max feedback latency:** ~1 second for structural gates; ~30 minutes for live-run confirmation.

`android.yml` has no `workflow_dispatch` trigger and cannot be dispatched directly, so its live
confirmation rides the phase pull request. Adding such a trigger is outside this phase's decisions.

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 1-01-01 | 01 | 1 | CI-01 | T-01-01 / T-01-02 / T-01-03 | Only workflow-computed `steps.*.outputs.*` values are interpolated into `run:` blocks; every shell expansion is double-quoted | structural + live e2e | Plan 01 Task 1 static gate, then `gh workflow run` + `completed:success` + `BUILD SUCCEEDED` log assertion | ✅ | ✅ green |
| 1-01-02 | 01 | 1 | — (D-07) | — | n/a | structural | Plan 01 Task 2 Ruby concurrency gate | ✅ | ✅ green |
| 1-02-01 | 02 | 1 | CICD-01 | T-01-04 / T-01-09 | No `pull_request` run may write the shared Gradle cache; the expression stays at the call site | structural | Plan 02 Task 1 per-job cache-policy gate + composite-action integrity gate | ✅ | ✅ green |
| 1-02-02 | 02 | 1 | — (D-07) | T-01-05 | n/a | structural | Plan 02 Task 2 Ruby concurrency gate + four-jobs-on-ubuntu gate | ✅ | ✅ green |
| 1-03-01 | 03 | 2 | CICD-01, CICD-02, CICD-03 | T-01-06 / T-01-07 / T-01-08 / T-01-09 | iOS job cache is read-only on PRs; a swallowed Gradle-setup failure cannot produce a green job | structural + live e2e | Plan 03 composite-reuse/Konan-ordering gate, job-separation gate, composite-action integrity gate, then `gh workflow run` + `completed:success` + `BUILD SUCCEEDED` | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

Every gate in this table was executed during planning against a deliberately wrong input — the
pre-change file, a fixture with an injected operating-system matrix, or a fixture with the cache
expression relocated into the composite action — and confirmed to fail. None can pass vacuously.

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements. No test scaffold needs creating: Ruby's
bundled YAML parser, `jq` 1.7.1 and `gh` 2.100.0 are all present locally, and every structural gate
in the plan set was run in this session.

RESEARCH lists `actionlint` as an optional static YAML linter. It is not installed, no locked
decision requires it, and no existing project convention uses it, so it is deliberately not adopted.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Android CI green across all four jobs | CICD-01 | `android.yml` has no `workflow_dispatch` trigger, so it cannot be dispatched on a branch; adding one is outside this phase's decisions | On the phase pull request to `main`, confirm the `Android CI` run concludes successfully for `Checks Linters`, `Unit tests`, `Instrumentation tests` and `Build debug`. Embedded as a `<human-check>` on Plan 02 Task 2. |
| Gradle cache reported active and read-only on PRs | CICD-01 | Cache behaviour is reported in the `gradle/actions/setup-gradle` job summary, which is read rather than asserted | On the PR run, open the `Setup Gradle` step summary and confirm it reports the cache in use and read-only for a `pull_request` event. Embedded as a `<human-check>` on Plan 02 Task 2 and Plan 03 Task 1. |
| Konan cache reported hit or saved | CICD-02 | `actions/cache` reports restore/save state in its own step output | On Plan 03's live run, confirm the `Cache Kotlin Multiplatform builds` step reports a hit or a save rather than an error. Embedded as a `<human-check>` on Plan 03 Task 1. The *ordering* half of CICD-02 is automated (index comparison against the shared-framework build step). |

No before/after cache-timing measurement is expected. CONTEXT.md explicitly declined visible
timing proof, so whatever `gradle/actions/setup-gradle` and `actions/cache` report in their own job
summaries is the intended evidence.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies — all five tasks carry at least one runnable `<automated>` command, each with a `<fails_when>` naming an observable failure signal
- [x] Sampling continuity: no 3 consecutive tasks without automated verify — zero tasks lack one
- [x] Wave 0 covers all MISSING references — there are none; no task carries a `MISSING — Wave 0` sentinel
- [x] No watch-mode flags — `gh run watch` is used with `--exit-status` as a bounded background wait, never as an interactive watcher, and the assertions themselves are single-shot
- [x] Feedback latency < 1s for structural gates (live-run latency is inherent to CI and is bounded by the workflow's own 30-minute job timeout)
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** signed off — `/gsd-validate-phase` confirmed all five per-task gates green and both
manual-only checks (Android CI four-job green run, Gradle cache read-only on PRs) satisfied via
UAT against real `pull_request` run logs (`gh run view` on run 35259554036 / log bundle
`logs_95495542942`: all four jobs `BUILD SUCCESSFUL`, `Cache is read-only: will not save state for
use in subsequent builds.` in each job's Setup Gradle post-action step, no `Saved cache entry`
lines anywhere in the logs).

## Validation Audit 2026-09-17

| Metric | Count |
|--------|-------|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |
