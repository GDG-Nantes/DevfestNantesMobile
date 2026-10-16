# Phase 1: CI Pipeline Fixed & Optimized - Context

**Gathered:** 2026-09-17
**Status:** Ready for planning

<domain>
## Phase Boundary

The GitHub Actions CI pipeline reliably builds and validates both Android and iOS on every push. This phase fixes the iOS simulator resolution at its root cause (dynamic lookup, not a re-pin) and adds Gradle/Konan caching so repeated builds are measurably faster, while Android and iOS keep running as separate, OS-appropriate jobs. No new user-facing behavior, no architecture/DI/test-coverage work — that's Phases 2-5.

</domain>

<decisions>
## Implementation Decisions

### iOS simulator & Xcode resolution
- **D-01:** Replace the hardcoded `-destination 'platform=iOS Simulator,name=iPhone 16'` in `.github/workflows/ios.yml` with a dynamic lookup: run `xcrun simctl list devices available`, parse it, and pick the newest available iPhone runtime (plain "iPhone \<N\>" naming, not Plus/Pro Max), then feed that resolved device name into `-destination`. No special-cased fallback/error-message step was requested — plain dynamic lookup only. — **Reversibility:** reversible — it's a shell parsing step inside one workflow file; can be swapped back to a hardcoded pin with a one-line diff.
- **D-02:** Also make Xcode version selection dynamic — remove the hardcoded `xcode-select -s /Applications/Xcode_26.0.1.app/Contents/Developer` and instead query available Xcode installations on the runner and select the latest. User explicitly chose this over keeping the pin, despite the pin not being the currently-broken part. — **Reversibility:** reversible — a shell step; can be reverted to an explicit `xcode-select -s` pin easily. Note for planner/researcher: this widens the blast radius of a "root cause fix" phase into also removing Xcode reproducibility — flag if `xcodebuild` behavior varies unexpectedly across runner image updates once this ships.

### Workflow structure
- **D-03:** Keep `.github/workflows/android.yml` and `.github/workflows/ios.yml` as two separate workflow files — do NOT merge into a single workflow with a `strategy.matrix: os: [...]`. The existing two-file/two-OS-runner structure already satisfies CICD-03 ("Android and iOS jobs run as separate matrix entries"); merging was rejected as unnecessary risk to the already-passing Android pipeline for no functional gain. — **Reversibility:** reversible — nothing structural depends on file count.

### Gradle/Konan cache write policy
- **D-04:** Change Gradle cache policy from "read-only almost everywhere, write-only in `build-debug`" to branch-aware: `cache-read-only: false` when the trigger is a push to `main` (populates a trusted, shared cache), `cache-read-only: true` on `pull_request` (PRs only ever read, never write — no risk of a bad branch poisoning the shared cache). Applies to every job currently calling `gradle/actions/setup-gradle` (via the `android-setup` composite action, and inline in `ios.yml` before D-05 folds it in). — **Reversibility:** reversible — a policy flag per job/composite-action input.
- **D-05:** `ios.yml` should stop duplicating inline `setup-java`/`setup-gradle` steps and instead reuse the shared `.github/actions/android-setup` composite action that `android.yml` already uses — single source of truth for Java/Gradle setup and for the new branch-aware cache policy (D-04). ~15 lines of duplicated YAML removed. — **Reversibility:** reversible — composite action extraction, no behavior contract beyond CI itself.
- **D-06:** Konan cache (`~/.konan`, keyed on `hashFiles('shared/**/*.kt', 'shared/build.gradle.kts', 'gradle/libs.versions.toml')`) already exists in `ios.yml` via `actions/cache@v4` and needs no branch-aware policy change — `actions/cache` writes on cache-miss regardless of branch, and GitHub Actions already scopes cache visibility per-branch/PR, so no discussion action item follows from this. Kept as-is.

### CI efficiency (surfaced during discussion, in scope)
- **D-07:** Add a `concurrency` group to both workflows (`group: ${{ github.workflow }}-${{ github.ref }}`, `cancel-in-progress: true`) so a new push to the same branch/PR cancels the previous in-progress run. Not one of the 4 literal success criteria but fits the "optimize build times" intent of this phase and was explicitly requested. — **Reversibility:** reversible — a top-level YAML block, trivially removable.

### Claude's Discretion
- Exact `xcrun simctl` parsing approach (grep/awk/jq, whatever is most robust) — user cared about the outcome (newest iPhone runtime, no fallback messaging), not the parsing mechanism.
- Exact mechanism for detecting "latest available Xcode" on the runner for D-02 (e.g. `ls /Applications | grep Xcode` vs `xcode-select --print-path` enumeration) — left to implementation/research.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project-level context
- `.planning/PROJECT.md` — core value, constraints (GitHub Actions stays the CI platform; `android` CLI must be used for Android-specific tasks), out-of-scope list
- `.planning/REQUIREMENTS.md` — CI-01, CICD-01, CICD-02, CICD-03 (Phase 1 requirement definitions), plus deferred CICD-V2-01/02
- `.planning/ROADMAP.md` §Phase 1 — goal, success criteria, dependencies (none — first phase)
- `.planning/STATE.md` — accumulated decisions/blockers; notes that AGP 9.x/KGP/KSP2 versions need re-verification via `android docs search` before Phase 2 (not this phase, but downstream awareness)

### Codebase maps
- `.planning/codebase/STACK.md` — current dependency/tooling versions this phase's CI must keep building
- `.planning/codebase/ARCHITECTURE.md` — known anti-patterns to avoid reintroducing (not directly relevant to CI, but shared context)

### Existing CI files this phase modifies
- `.github/workflows/ios.yml` — target of D-01, D-02, D-04, D-05
- `.github/workflows/android.yml` — target of D-04, D-07
- `.github/actions/android-setup/action.yml` — target of D-04 (branch-aware `cache-read-only` input), consumed by both workflows after D-05
- `.github/actions/get-avd-info/action.yml` — used by `android.yml`'s instrumentation-tests job; unaffected by this phase's decisions but worth reviewing for pattern consistency

No external specs beyond the above — requirements fully captured in decisions above.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `.github/actions/android-setup/action.yml` — composite action already wraps `setup-java` + `gradle/actions/setup-gradle` + `local.properties` creation with a `cache-read-only` input. D-05 extends its use to `ios.yml`; D-04 changes how callers set that input based on branch/event.
- `.github/actions/get-avd-info/action.yml` — existing pattern for a small composite action feeding matrix outputs; can serve as a style reference if a new composite action is needed for simulator/Xcode resolution.

### Established Patterns
- `ios.yml` already has multiple `actions/cache@v4` blocks (Konan, Homebrew, SwiftGen, SwiftGen-generated, Xcode DerivedData) keyed by `hashFiles(...)` on relevant inputs — any new caching should follow this same keyed-cache pattern, not `setup-gradle`'s built-in cache for non-Gradle assets.
- `android.yml` jobs are already split by concern (checks/unit-tests/instrumentation-tests/build-debug), each calling the shared `android-setup` composite action — D-04's branch-aware cache policy should be threaded through this composite action's input rather than hardcoded per-job.

### Integration Points
- The `-destination` flag inside the "Build iOS App for Simulator" step (`ios.yml` line ~192) is the literal root-cause line for CI-01 — D-01 replaces its hardcoded value with a variable populated by a new preceding step.
- The `xcode-select -s ...` step (`ios.yml` line ~88) is the target of D-02.
- `cache-read-only: 'true'` appears in `ios.yml`'s inline `setup-gradle` step (line ~33) and is passed as an input in every `android-setup` call across `android.yml`'s four jobs — all of these are D-04's blast radius, consolidated to one call site by D-05.

</code_context>

<specifics>
## Specific Ideas

- User wants the Xcode version itself resolved dynamically (D-02), not just the simulator device — a stronger read of "root cause fix" than the roadmap's literal wording, applied deliberately after being offered the more conservative "keep the pin" option.
- No interest in visible before/after timing proof for the cache speed-up (that gray area was explicitly not selected for discussion) — implementers should not build extra telemetry/reporting for this unless it falls out naturally from `gradle/actions/setup-gradle`'s own step summary.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within Phase 1 scope. (Path-filtered triggers and cross-module dependency lint are already tracked as CICD-V2-01/02 in REQUIREMENTS.md, not raised fresh here.)

</deferred>

---

*Phase: 1-ci-pipeline-fixed-optimized*
*Context gathered: 2026-09-17*
