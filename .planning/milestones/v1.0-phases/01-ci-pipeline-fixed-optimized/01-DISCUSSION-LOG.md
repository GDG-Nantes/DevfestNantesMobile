# Phase 1: CI Pipeline Fixed & Optimized - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-09-17
**Phase:** 1-ci-pipeline-fixed-optimized
**Areas discussed:** iOS simulator fix strategy, Workflow structure, Gradle/Konan cache write policy, Composite-action reuse, Concurrency control

---

## iOS simulator fix strategy

| Option | Description | Selected |
|--------|-------------|----------|
| Dynamic simctl lookup | Run `xcrun simctl list devices available`, pick first/newest matching iPhone, feed into `-destination` | ✓ |
| Pin macOS/Xcode runner image + device explicitly | Hardcode device name but pin exact runner image so simulator set is frozen | |
| Hybrid: dynamic lookup with documented fallback | Dynamic lookup, but fail with a clear diagnostic message if no match found | |

**User's choice:** Dynamic simctl lookup (recommended option), no special fallback messaging requested.

### Follow-up: which simulator to pick when multiple iPhones are available

| Option | Description | Selected |
|--------|-------------|----------|
| Newest iPhone runtime available | Sort by iOS runtime + device name, pick latest plain "iPhone \<N\>" | ✓ |
| First 'iPhone' match in simctl output | Simplest grep/awk, first line containing "iPhone" | |

**User's choice:** Newest iPhone runtime available.

### Follow-up: keep the explicit Xcode version pin, or make dynamic too?

| Option | Description | Selected |
|--------|-------------|----------|
| Keep the explicit Xcode pin | Xcode pinning ≠ simulator device naming; keep xcode-select as-is | |
| Make Xcode selection dynamic too | Query available Xcode versions on runner, select latest, remove hardcoded path | ✓ |

**User's choice:** Make Xcode selection dynamic too (against the recommended/conservative option).
**Notes:** User explicitly chose the broader "root cause fix" scope, removing both the simulator-name pin and the Xcode-version pin, despite being offered the narrower option that only touched the literally-broken line.

---

## Workflow structure

| Option | Description | Selected |
|--------|-------------|----------|
| Keep separate files | android.yml + ios.yml stay as two OS-specific files; already satisfies CICD-03 | ✓ |
| Merge into one workflow with os matrix | Single workflow, `strategy.matrix: os: [...]`, conditional steps per OS | |

**User's choice:** Keep separate files (recommended option).
**Notes:** Rejected merging as unnecessary risk to the already-passing Android pipeline for no functional gain — the two-file structure already satisfies the "separate matrix entries" success criterion.

---

## Gradle/Konan cache write policy

| Option | Description | Selected |
|--------|-------------|----------|
| Write on main push, read-only on PR | `cache-read-only: false` on push to main, `true` on PR | ✓ |
| Leave current policy as-is | Read-only everywhere except build-debug | |

**User's choice:** Write on main push, read-only on PR (recommended option).

### Follow-up: more questions on cache write policy?

User moved directly to next area — no additional cache-policy questions raised. Konan cache (via `actions/cache@v4`, not `setup-gradle`) was noted as already correctly behaved (writes on cache-miss regardless of branch; GH scopes cache visibility per-branch/PR already) and needs no change.

---

## Composite-action reuse (surfaced area)

| Option | Description | Selected |
|--------|-------------|----------|
| Reuse android-setup in ios.yml | Single source of truth for Java/Gradle setup + new cache policy | ✓ |
| Keep iOS setup steps inline/separate | iOS workflow stays self-contained | |

**User's choice:** Yes, reuse `android-setup` in `ios.yml` (recommended option).

---

## Concurrency control (surfaced area)

| Option | Description | Selected |
|--------|-------------|----------|
| Yes, cancel outdated runs | Add `concurrency` group with `cancel-in-progress: true` to both workflows | ✓ |
| No, leave out of scope | Not explicitly required by success criteria; skip to stay tightly scoped | |

**User's choice:** Yes, cancel outdated runs (recommended option).

---

## Claude's Discretion

- Exact `xcrun simctl` parsing mechanism (grep/awk/jq) for simulator resolution.
- Exact mechanism for detecting "latest available Xcode" on the runner.

## Deferred Ideas

None raised outside Phase 1 scope during this discussion.
