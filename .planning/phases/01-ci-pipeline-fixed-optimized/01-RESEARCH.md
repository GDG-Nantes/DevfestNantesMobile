# Phase 1: CI Pipeline Fixed & Optimized - Research

**Researched:** 2026-09-17
**Domain:** GitHub Actions CI/CD (macOS/Xcode simulator resolution, Gradle/Konan caching, workflow concurrency)
**Confidence:** MEDIUM

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01:** Replace the hardcoded `-destination 'platform=iOS Simulator,name=iPhone 16'` in `.github/workflows/ios.yml` with a dynamic lookup: run `xcrun simctl list devices available`, parse it, and pick the newest available iPhone runtime (plain "iPhone \<N\>" naming, not Plus/Pro Max variants), then feed that resolved device name into `-destination`. No special-cased fallback/error-message step was requested — plain dynamic lookup only. — **Reversibility:** reversible — it's a shell parsing step inside one workflow file; can be swapped back to a hardcoded pin with a one-line diff.
- **D-02:** Also make Xcode version selection dynamic — remove the hardcoded `xcode-select -s /Applications/Xcode_26.0.1.app/Contents/Developer` and instead query available Xcode installations on the runner and select the latest. User explicitly chose this over keeping the pin, despite the pin not being the currently-broken part. — **Reversibility:** reversible — a shell step; can be reverted to an explicit `xcode-select -s` pin easily. Note for planner/researcher: this widens the blast radius of a "root cause fix" phase into also removing Xcode reproducibility — flag if `xcodebuild` behavior varies unexpectedly across runner image updates once this ships.
- **D-03:** Keep `.github/workflows/android.yml` and `.github/workflows/ios.yml` as two separate workflow files — do NOT merge into a single workflow with a `strategy.matrix: os: [...]`. The existing two-file/two-OS-runner structure already satisfies CICD-03 ("Android and iOS jobs run as separate matrix entries"); merging was rejected as unnecessary risk to the already-passing Android pipeline for no functional gain. — **Reversibility:** reversible — nothing structural depends on file count.
- **D-04:** Change Gradle cache policy from "read-only almost everywhere, write-only in `build-debug`" to branch-aware: `cache-read-only: false` when the trigger is a push to `main` (populates a trusted, shared cache), `cache-read-only: true` on `pull_request` (PRs only ever read, never write — no risk of a bad branch poisoning the shared cache). Applies to every job currently calling `gradle/actions/setup-gradle` (via the `android-setup` composite action, and inline in `ios.yml` before D-05 folds it in). — **Reversibility:** reversible — a policy flag per job/composite-action input.
- **D-05:** `ios.yml` should stop duplicating inline `setup-java`/`setup-gradle` steps and instead reuse the shared `.github/actions/android-setup` composite action that `android.yml` already uses — single source of truth for Java/Gradle setup and for the new branch-aware cache policy (D-04). ~15 lines of duplicated YAML removed. — **Reversibility:** reversible — composite action extraction, no behavior contract beyond CI itself.
- **D-06:** Konan cache (`~/.konan`, keyed on `hashFiles('shared/**/*.kt', 'shared/build.gradle.kts', 'gradle/libs.versions.toml')`) already exists in `ios.yml` via `actions/cache@v4` and needs no branch-aware policy change — `actions/cache` writes on cache-miss regardless of branch, and GitHub Actions already scopes cache visibility per-branch/PR, so no discussion action item follows from this. Kept as-is.
- **D-07:** Add a `concurrency` group to both workflows (`group: ${{ github.workflow }}-${{ github.ref }}`, `cancel-in-progress: true`) so a new push to the same branch/PR cancels the previous in-progress run. Not one of the 4 literal success criteria but fits the "optimize build times" intent of this phase and was explicitly requested. — **Reversibility:** reversible — a top-level YAML block, trivially removable.

### Claude's Discretion

- Exact `xcrun simctl` parsing approach (grep/awk/jq, whatever is most robust) — user cared about the outcome (newest iPhone runtime, no fallback messaging), not the parsing mechanism.
- Exact mechanism for detecting "latest available Xcode" on the runner for D-02 (e.g. `ls /Applications | grep Xcode` vs `xcode-select --print-path` enumeration) — left to implementation/research.

### Deferred Ideas (OUT OF SCOPE)

None — discussion stayed within Phase 1 scope. (Path-filtered triggers and cross-module dependency lint are already tracked as CICD-V2-01/02 in REQUIREMENTS.md, not raised fresh here.)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| CI-01 | iOS CI completes end-to-end via dynamic simulator resolution rather than a hardcoded device name | See D-01 code example (jq-based `simctl` parsing), Common Pitfall 1 & 4 (stale/variant device names), Environment Availability (confirmed simulator inventory on `macos-15` image) |
| CICD-01 | Gradle cache (`gradle/actions/setup-gradle`) configured in CI | See `cache-read-only` semantics research, D-04/D-05 code example, Validation Architecture (cache-hit verification via job logs) |
| CICD-02 | Konan cache (`~/.konan`) configured in CI | Confirmed already correctly implemented (D-06) — no code change, verification command only |
| CICD-03 | Android and iOS jobs separated in CI matrix (`ubuntu-latest`/`macos-latest`) | Confirmed already satisfied structurally (D-03) — no code change |
</phase_requirements>

## Summary

This phase is a CI-configuration-only change confined to two workflow files (`.github/workflows/ios.yml`, `.github/workflows/android.yml`) and one composite action (`.github/actions/android-setup/action.yml`). All four success criteria map cleanly onto the seven locked decisions in CONTEXT.md — the primary research task was not "what stack to use" (no new dependency is required) but "what is the correct, current syntax and semantics" for: (1) parsing `xcrun simctl list devices available --json` to select the newest plain "iPhone N" simulator, (2) enumerating installed Xcode versions on a GitHub-hosted macOS runner to pick the latest, (3) `gradle/actions/setup-gradle`'s `cache-read-only` input semantics, and (4) GitHub Actions' `concurrency` block syntax.

A directly-fetched, dated confirmation from `actions/runner-images`' own `macos-15-Readme.md` is the most load-bearing finding: the current `macos-latest` image ships Xcode versions **16.0 through 26.3**, with **16.4 as the runner's own `xcode-select` default** — meaning the pipeline's already-broken hardcoded `xcode-select -s .../Xcode_26.0.1.app` line was pointing at a specific build that may not even exist on every image rotation, while the *actual* newest available Xcode (26.3) sits unused unless explicitly selected. This directly validates D-02's premise and gives a concrete, non-hypothetical target for the "select the latest installed Xcode" logic. The same source confirms the iPhone simulator naming set (`iPhone 16`, `iPhone 16 Plus`, `iPhone 16 Pro`, `iPhone 16 Pro Max`, `iPhone 16e`, `iPhone 17`, `iPhone 17 Pro`, `iPhone 17 Pro Max`, `iPhone Air`, `iPhone SE (3rd generation)`), which the D-01 filter regex must exclude everything except the plain `iPhone <N>` forms from.

No new external package is required for either D-01 or D-02: `jq` is confirmed pre-installed on the `macos-15` image (the current `macos-latest`), so a dependency-free `jq`-based parsing script is both robust and adds zero new supply-chain surface — consistent with this phase's "reversible, minimal blast radius" framing in CONTEXT.md. The alternative (`maxim-lobanov/setup-xcode`, a widely-used marketplace action) is documented as a fallback option only, since introducing it would add a new third-party dependency that this phase's decisions did not ask for and that would need its own SHA-pinning/legitimacy verification pass.

**Primary recommendation:** Implement D-01 and D-02 as inline `run:` shell steps using `jq` (already present on the runner) rather than a new marketplace action; implement D-04 by moving the `cache-read-only` value from a hardcoded string to the expression `${{ github.event_name == 'pull_request' }}` at every `android-setup`/`setup-gradle` call site (not inside the composite action's static `default:`, which cannot evaluate `${{ }}` expressions); implement D-07 with the canonical `group: ${{ github.workflow }}-${{ github.ref }}` / `cancel-in-progress: true` block, added once per workflow file (two separate top-level blocks, since `github.workflow` differs between "Android CI" and "iOS CI" and group names must be unique per-workflow to avoid unrelated runs cancelling each other).

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Dynamic iOS simulator selection (D-01) | Runner shell script (`run:` step in `ios.yml`) | — | The accepted simulator inventory only exists on the macOS runner at execution time; resolution must happen there, immediately before the `xcodebuild` step that consumes it |
| Dynamic Xcode version selection (D-02) | Runner shell script (`run:` step in `ios.yml`) | Marketplace action (optional alternative) | Same reasoning as above; a marketplace action (`maxim-lobanov/setup-xcode`) is a viable secondary but adds a new pinned dependency this phase doesn't require |
| Gradle cache branch policy (D-04) | Workflow YAML (job-level `with:` input at each call site) | Composite action (`android-setup/action.yml`, already accepts the input) | `github.event_name`/`github.ref` context is only naturally available where the job is defined; composite action `default:` fields are static strings and cannot hold `${{ }}` expressions |
| `android-setup` reuse in `ios.yml` (D-05) | Composite action (`.github/actions/android-setup`) | Workflow YAML (call site in `ios.yml`) | Single source of truth for `setup-java`+`setup-gradle`+`local.properties`; already exists and is already consumed this way by `android.yml` |
| Konan cache (D-06) | Workflow YAML (`actions/cache@v4` block in `ios.yml`) | — | Already correctly implemented; no tier change needed |
| Job separation (D-03) | Workflow file structure (two files, two `runs-on` targets) | — | Already satisfied; CICD-03 requires no change |
| Concurrency control (D-07) | Workflow YAML (top-level `concurrency:` key, once per file) | — | Applies per-workflow (keyed by `github.workflow`), not per-job — must be added to both `android.yml` and `ios.yml` independently |

## Standard Stack

### Core

| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| `gradle/actions/setup-gradle` | `v4` (already pinned in both workflows) [VERIFIED: .github/workflows/ios.yml:30, .github/actions/android-setup/action.yml:20] | Gradle setup + build cache | Official Gradle-org action, the current maintained successor to the deprecated `gradle/gradle-build-action`; already in use, no version change needed |
| `actions/cache` | `v4` (already pinned in `ios.yml`) [VERIFIED: .github/workflows/ios.yml:40,51,61,77,113] | Keyed caching for Konan/Homebrew/SwiftGen/DerivedData | GitHub's own official generic cache action; the correct tool for non-Gradle cache targets per the existing established pattern in this repo |
| `actions/setup-java` | `v4` (already pinned) [VERIFIED: .github/workflows/ios.yml:24, .github/actions/android-setup/action.yml:16] | JDK provisioning | Official action; required by `setup-gradle` |
| `jq` | pre-installed on runner (1.8.2 confirmed on `macos-15` image) [CITED: github.com/actions/runner-images macos-15-Readme.md] | JSON parsing of `xcrun simctl list --json` output for D-01 | No install step needed; robust structured parsing beats regex-on-plain-text for the nested `devices`/`SimRuntime` JSON shape |

### Supporting

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `sort -V` / `sort -n` (coreutils, pre-installed) | n/a | Numeric/version sort to pick "newest" Xcode path or iPhone model number | Always — needed by both D-01 (device name) and D-02 (Xcode path) resolution scripts |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Shell-based `/Applications/Xcode_*.app` enumeration + `xcode-select -s` (D-02) | `maxim-lobanov/setup-xcode@v1` with `xcode-version: latest-stable` [ASSUMED — websearch, not fetched from an official source] | Marketplace action is more declarative and handles `latest-stable` vs `latest` (beta) distinction for you, but it is a **new third-party dependency** this phase's decisions did not request — would need SHA-pinning + legitimacy verification (see Package Legitimacy Audit) before use, and only switches among already-installed versions (installs nothing), same constraint as the shell approach |
| `jq`-based JSON parsing of `simctl list --json` (D-01) | `grep`/`awk` on plain-text `simctl list devices available` output [ASSUMED — websearch] | Plain-text parsing avoids a `jq` dependency, but `jq` is already confirmed present on the runner so there's no dependency benefit; plain-text parsing is also more fragile to Apple's occasional device-name string format changes (e.g. the iPhone XS/XS Max/XR renames referenced in community reports) |
| Composite-action `default:` holding a branch-aware expression (D-04) | Hardcoding the expression at each call site | GitHub Actions composite action `input.default` fields are evaluated as static strings at parse time and cannot contain `${{ github.* }}` expressions — this is not a stylistic choice, it is a hard platform constraint, so the expression **must** live at the call site, not inside `android-setup/action.yml`'s `default:` |

**Installation:** None. No `npm install`/`pip install`/`cargo add` equivalent applies — every tool used (`jq`, `sort`, the pinned Actions) is already present in the repo's workflow files or on the GitHub-hosted runner image.

**Version verification:** `gradle/actions/setup-gradle@v4`, `actions/cache@v4`, and `actions/setup-java@v4` version pins were read directly from the current workflow/composite-action files this session (`.github/workflows/ios.yml`, `.github/actions/android-setup/action.yml`) — see line citations above. These are GitHub Marketplace Actions, not registry packages, so `npm view`/`pip index versions` do not apply; no newer major version of any of the three was surfaced during research and none is required by the locked decisions.

## Package Legitimacy Audit

This phase does not install any new npm/PyPI/crates package, and per the locked decisions and the Standard Stack recommendation above, it does not need to add any new GitHub Marketplace Action either — D-01, D-02, D-04, D-05, and D-07 are all implementable as inline shell steps or YAML-only changes using Actions already present and pinned in the repository (`gradle/actions/setup-gradle@v4`, `actions/cache@v4`, `actions/setup-java@v4`, `actions/checkout@v4`, `reactivecircus/android-emulator-runner@v2`). None of these are newly introduced by this phase, so the standard `package-legitimacy check` protocol (scoped to `npm`/`pypi`/`crates` ecosystems) does not apply — there is nothing new to run it against.

**Conditional note for the planner:** If the shell-based Xcode enumeration in the Code Examples section below is rejected during planning in favor of the `maxim-lobanov/setup-xcode` marketplace-action alternative (see Alternatives Considered), that action must be treated as a **new dependency**: it has not been verified through the `package-legitimacy check` protocol (no npm/PyPI/crates ecosystem applies to GitHub Marketplace Actions), was discovered only via WebSearch [ASSUMED], and should be pinned to a full commit SHA (not the `@v1` tag) per the supply-chain hardening findings below, gated behind a `checkpoint:human-verify` task before first use.

**Packages removed due to [SLOP] verdict:** none (nothing new introduced)
**Packages flagged as suspicious [SUS]:** none (nothing new introduced)

## Architecture Patterns

### System Architecture Diagram

```
Push / PR to main
        │
        ▼
┌───────────────────────────┐        ┌───────────────────────────┐
│   android.yml (ubuntu)    │        │     ios.yml (macos)        │
│                            │        │                            │
│  concurrency: android-CI-  │        │  concurrency: iOS-CI-      │
│  ${{ ref }} [D-07]         │        │  ${{ ref }} [D-07]         │
│                            │        │                            │
│  jobs: checks, unit-tests, │        │  job: ios-build             │
│  instrumentation-tests,    │        │   ├─ android-setup [D-05]   │
│  build-debug                │        │   │   ├─ setup-java         │
│   each ──▶ android-setup   │        │   │   └─ setup-gradle        │
│   composite action          │        │   │       cache-read-only:  │
│      ├─ setup-java          │        │   │       event=='pull_    │
│      └─ setup-gradle        │        │   │        request' [D-04] │
│          cache-read-only:   │        │   ├─ actions/cache: Konan   │
│          event=='pull_      │        │   │   (unchanged) [D-06]   │
│           request' [D-04]   │        │   ├─ resolve Xcode version  │
└───────────────────────────┘        │   │   (enumerate /Applica-  │
                                       │   │    tions/Xcode_*.app,   │
                                       │   │    sort -V, xcode-      │
                                       │   │    select -s) [D-02]    │
                                       │   ├─ resolve simulator name │
                                       │   │   (xcrun simctl list    │
                                       │   │    --json | jq filter   │
                                       │   │    isAvailable + regex  │
                                       │   │    ^iPhone [0-9]+$,     │
                                       │   │    pick highest) [D-01] │
                                       │   └─ xcodebuild build       │
                                       │        -destination         │
                                       │        name=$RESOLVED [D-01]│
                                       └───────────────────────────┘
```

### Recommended Project Structure

No new files/directories. Existing structure is retained:
```
.github/
├── workflows/
│   ├── android.yml          # D-04 (call-site cache-read-only expr), D-07 (concurrency)
│   └── ios.yml               # D-01, D-02, D-04, D-05, D-07
├── actions/
│   ├── android-setup/        # target of D-05 reuse; no internal logic change needed for D-04
│   │   └── action.yml
│   └── get-avd-info/         # unaffected; style reference only
│       └── action.yml
└── dependabot.yml             # unaffected — [VERIFIED: .github/dependabot.yml:6-13] only covers `package-ecosystem: gradle`, not `github-actions` — see Common Pitfall 7
```

### Pattern 1: Branch-aware cache policy via call-site expression, not composite-action default

**What:** Instead of the composite action's `cache-read-only` input `default:` trying to encode "read-only on PRs", every call site passes an explicit expression.
**When to use:** Any composite-action input whose correct value depends on `github.event_name`/`github.ref` — those contexts are available at the call site but a composite action's own `default:` field cannot reference them.
**Example:**
```yaml
# Source: pattern synthesized from official concurrency/cache-read-only docs (see Sources)
# .github/workflows/android.yml (every job) and .github/workflows/ios.yml (after D-05 reuse)
steps:
  - uses: ./.github/actions/android-setup
    with:
      java-version: ${{ env.JAVA_VERSION }}
      cache-read-only: ${{ github.event_name == 'pull_request' }}
```
This makes `build-debug`'s previous hardcoded `cache-read-only: 'false'` and the other three jobs' hardcoded `'true'` [VERIFIED: .github/workflows/android.yml:21,38,56,88 — `cache-read-only: 'true'` on lines 21/38/56, `cache-read-only: 'false'` on line 88] both collapse into the same single expression, since `push` (including push-to-main) and `workflow_dispatch` both evaluate the expression to `false` (write-enabled), and `pull_request` evaluates to `true` (read-only) — matching D-04's literal "push to main → false, pull_request → true" wording. See Open Questions for the `workflow_dispatch` edge case on `ios.yml` specifically.

### Pattern 2: Composite-action reuse across OS-heterogeneous workflows

**What:** `ios.yml`'s inline `setup-java`+`setup-gradle` steps (currently lines 23-34) [VERIFIED: .github/workflows/ios.yml:23-34] are replaced by a single `uses: ./.github/actions/android-setup` step, identical to how `android.yml` already calls it four times.
**When to use:** Whenever two OS-specific workflows share identical tool-provisioning logic; the composite action already runs fine on `macos-latest` since `setup-java`/`setup-gradle` are both cross-platform actions.
**Example:**
```yaml
# Source: .github/actions/android-setup/action.yml (read this session) + .github/workflows/android.yml call pattern
- uses: actions/checkout@v4
- uses: ./.github/actions/android-setup
  with:
    java-version: ${{ env.JAVA_VERSION }}
    cache-read-only: ${{ github.event_name == 'pull_request' }}
```

### Anti-Patterns to Avoid

- **Putting a `${{ github.* }}` expression inside a composite action's `inputs.<name>.default:`** — GitHub Actions does not evaluate expressions in composite-action input defaults; they are static strings only. This would silently produce the literal string `"${{ github.event_name == 'pull_request' }}"` rather than `true`/`false`. Always compute the expression at the call site.
- **Filtering `simctl` JSON only on the top-level `available` list without also checking `isAvailable` per-device** — a documented bug pattern where stale simulator records for a since-removed runtime persist with `isAvailable: false` inside the "available" grouping; an unfiltered script can select one and fail to boot it. [ASSUMED — websearch, see Common Pitfall 1]
- **Hardcoding a specific Xcode `.app` bundle path** (the current bug) — GitHub silently rotates which exact Xcode point-release is the runner's default, and per the directly-fetched `macos-15-Readme.md`, the default (16.4) is not even the newest installed version (26.3) — any hardcoded `xcode-select -s /Applications/Xcode_X.Y.Z.app` will eventually reference a bundle that no longer exists on the image. [CITED: github.com/actions/runner-images macos-15-Readme.md]

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Gradle remote build cache | A custom S3/GCS cache backend or manual `~/.gradle/caches` tar+upload script | `gradle/actions/setup-gradle@v4`'s built-in GitHub Actions cache integration (already in use) | Already solved, already pinned in this repo; handles cache key hashing, restore-key fallback chains, and (per the researched default behavior) already defaults to a safe branch-aware write policy even without D-04's explicit override |
| Detecting "is this the default branch" across `push`/`pull_request`/`workflow_dispatch` | A bespoke bash `if` chain parsing `$GITHUB_REF`/`$GITHUB_EVENT_NAME` env vars manually | The `github.event_name`/`github.ref` expression contexts directly in YAML `with:`/`if:` fields | GitHub Actions already exposes these as first-class expression contexts; manual env-var string parsing duplicates logic GitHub already computes and exposes, and is a common source of the exact `head_ref` vs `ref` confusion documented in Common Pitfall 6 |

**Key insight:** Every problem this phase needs to solve (branch-aware caching, dynamic tool-version selection, workflow concurrency) already has a first-class GitHub Actions primitive or an already-pinned action in this repo — no phase in this project needed less "build vs buy" judgment than this one. The risk here is not under-building, it's over-building (e.g., reaching for a new marketplace action or writing a custom fallback-resolution script) when CONTEXT.md explicitly scoped this to "plain dynamic lookup only."

## Common Pitfalls

### Pitfall 1: Stale/unavailable simulator runtime records leak through `simctl list devices available`
**What goes wrong:** A script that filters only on the top-level "available" grouping (rather than also checking each device's `isAvailable: true` field) can select a device record for a runtime Xcode has since removed, then fail to boot it.
**Why it happens:** `simctl`'s JSON can retain stale device entries with `isAvailable: false` inside data that still gets returned by `devices available`, per a documented community-reported issue. [ASSUMED — websearch, not independently verified against a live `simctl` invocation in this sandbox — see Assumptions Log A1]
**How to avoid:** Explicitly filter `select(.isAvailable == true)` in the `jq` pipeline (see Code Examples), don't rely solely on the CLI's own `available` filter.
**Warning signs:** `xcodebuild` failing with "Unable to find a destination matching the provided destination specifier" despite `xcrun simctl list devices available` appearing to show a matching device.

### Pitfall 2: The runner's default `xcode-select` path is not the newest installed Xcode
**What goes wrong:** Assuming `xcode-select --print-path` (unset/default) already points at the latest Xcode, and skipping explicit selection.
**Why it happens:** Confirmed directly from the official runner-images README for the current `macos-latest` (`macos-15`) image: the image ships Xcode 16.0 through 26.3, but the pre-selected default is **16.4** — nine minor/major versions behind the newest installed. [CITED: github.com/actions/runner-images macos-15-Readme.md]
**How to avoid:** Explicitly enumerate `/Applications/Xcode_*.app`, sort by version, and `xcode-select -s` the highest one (D-02) — do not trust the ambient default.
**Warning signs:** `xcodebuild -version` in CI logs reporting an older Xcode than expected even after a runner-image update.

### Pitfall 3: Composite-action `default:` cannot hold `${{ }}` expressions
**What goes wrong:** Attempting to make `android-setup/action.yml`'s `cache-read-only` input branch-aware by changing its `default: 'false'` [VERIFIED: .github/actions/android-setup/action.yml:4-7 — `cache-read-only:\n    description: ...\n    required: false\n    default: 'false'`] to something like `default: ${{ github.event_name == 'pull_request' }}`.
**Why it happens:** Composite action metadata (`action.yml`) is parsed statically; `inputs.*.default` values are literal strings, not evaluated expressions — unlike workflow-level `env:`/`with:` fields.
**How to avoid:** Keep the composite action's `default:` as-is (or drop it) and pass the computed expression explicitly from every call site instead (Pattern 1 above).
**Warning signs:** The Gradle cache silently behaving as if `cache-read-only` were the literal string `"${{ github.event_name == 'pull_request' }}"` (which Actions would likely coerce to a non-empty/truthy string, always read-only) rather than the intended boolean.

### Pitfall 4: Non-plain iPhone simulator names slipping through a loose filter
**What goes wrong:** A regex like `iPhone` (substring match) or `iPhone*` (glob without anchoring) also matches `iPhone 16 Plus`, `iPhone 16 Pro`, `iPhone 16 Pro Max`, `iPhone 16e`, `iPhone Air`, and `iPhone SE (3rd generation)` — all confirmed present on the current runner image alongside the plain `iPhone 16`/`iPhone 17` entries D-01 wants. [CITED: github.com/actions/runner-images macos-15-Readme.md]
**Why it happens:** Apple's simulator device-type names are not zero-padded or consistently delimited; `Pro`, `Pro Max`, `Plus`, `e`, `Air`, and parenthetical generation suffixes all share the `iPhone ` prefix.
**How to avoid:** Anchor the filter to `^iPhone [0-9]+$` (end-of-string immediately after the digits) so `iPhone 17` matches but `iPhone 17 Pro` and `iPhone Air` do not.
**Warning signs:** CI intermittently resolving to a Pro/Pro Max/Plus device, which is still a valid simulator but violates the plain-model-only requirement in D-01.

### Pitfall 5: `queue: max` + `cancel-in-progress: true` is a validation error
**What goes wrong:** Adding a `queue:` property alongside `cancel-in-progress: true` in the new D-07 `concurrency` block.
**Why it happens:** GitHub documents this combination as explicitly disallowed. [CITED: docs.github.com/en/actions/using-jobs/using-concurrency]
**How to avoid:** Not applicable unless a future phase adds `queue:` — for D-07's simple `group`+`cancel-in-progress: true` pair, this is a non-issue; flagged here only because it's the one documented hard validation error in this API surface.
**Warning signs:** Workflow file fails GitHub's YAML validation entirely (red X before any job runs) if this combination is ever introduced later.

### Pitfall 6: `github.ref` means different things on `push` vs `pull_request`
**What goes wrong:** Writing a branch-detection expression assuming `github.ref` always reflects "the branch this run is happening on."
**Why it happens:** On `push`, `github.ref` is `refs/heads/<branch>`. On `pull_request`, `github.ref` is the PR's merge ref (`refs/pull/<n>/merge`), not the source branch — `github.head_ref` is needed for that. [ASSUMED — websearch synthesis of community discussions, not an official-docs direct fetch]
**How to avoid:** D-04's required expression, `github.event_name == 'pull_request'`, sidesteps this entirely by keying off `event_name` rather than parsing `ref` — recommended over any `ref`-string-matching approach for this reason.
**Warning signs:** A `cache-read-only` expression that appears correct on `push` events but behaves unexpectedly on `pull_request` runs.

### Pitfall 7: GitHub Actions dependencies are not covered by this repo's Dependabot config
**What goes wrong:** Assuming any Action version pin added or changed by this phase (`@v4`, `@v1`, etc.) will be kept current automatically.
**Why it happens:** `.github/dependabot.yml` only configures `package-ecosystem: gradle`; there is no `package-ecosystem: github-actions` entry. [VERIFIED: .github/dependabot.yml:6-13 — `version: 2\nupdates:\n- package-ecosystem: gradle\n  directory: "/"\n  schedule:\n    interval: weekly\n  open-pull-requests-limit: 100`]
**How to avoid:** Not a blocking issue for this phase's locked decisions (no decision requires adding Dependabot config), but the planner should be aware that any newly-pinned Action version (or a future SHA-pin) will need manual bumps until/unless a `github-actions` Dependabot ecosystem entry is added — out of scope here, flagged for awareness only.
**Warning signs:** None immediate; this is a latent maintenance gap, not a functional bug in this phase's deliverables.

## Code Examples

### D-01: Resolve the newest plain iPhone simulator name

```yaml
# Source: synthesized from community jq patterns + confirmed device-name set
# (github.com/actions/runner-images macos-15-Readme.md) — the jq/simctl JSON
# shape itself is ASSUMED (training knowledge / websearch), not independently
# verified against a live `simctl` invocation in this sandbox (no Xcode
# license accepted on this machine — see Assumptions Log A1).
- name: Resolve latest plain iPhone simulator
  id: sim
  run: |
    DEVICE_NAME=$(xcrun simctl list devices available --json \
      | jq -r '
          .devices
          | to_entries[]
          | select(.key | test("com.apple.CoreSimulator.SimRuntime.iOS"))
          | .value[]
          | select(.isAvailable == true)
          | select(.name | test("^iPhone [0-9]+$"))
          | .name
        ' \
      | sort -t' ' -k2 -n -u \
      | tail -1)
    echo "device_name=$DEVICE_NAME" >> "$GITHUB_OUTPUT"

# .github/workflows/ios.yml "Build iOS App for Simulator" step (was line ~192):
- name: Build iOS App for Simulator
  run: |
    xcodebuild \
      -project iosApp/iosApp.xcodeproj \
      -scheme "iosApp" \
      -configuration Debug \
      -destination "platform=iOS Simulator,name=${{ steps.sim.outputs.device_name }}" \
      -derivedDataPath build \
      build \
      CODE_SIGNING_ALLOWED=NO \
      CODE_SIGNING_REQUIRED=NO \
      CODE_SIGN_IDENTITY="" \
      PROVISIONING_PROFILE=""
```
Per CONTEXT.md's explicit discretion note ("No special-cased fallback/error-message step was requested — plain dynamic lookup only"), this intentionally omits a guard clause for an empty `$DEVICE_NAME`; if resolution fails, `xcodebuild` will fail naturally on the malformed/empty `-destination` value with its own error message, which is an acceptable natural failure mode rather than a custom fallback.

### D-02: Resolve and select the latest installed Xcode

```yaml
# Source: synthesized from community xcode-select enumeration patterns
# [ASSUMED — websearch]; confirmed against the actual installed-version set
# on the current runner image [CITED: github.com/actions/runner-images
# macos-15-Readme.md — Xcode 16.0 through 26.3 installed, default is 16.4]
- name: Select latest installed Xcode
  run: |
    LATEST_XCODE=$(ls -d /Applications/Xcode_*.app 2>/dev/null \
      | sed -E 's#.*/Xcode_([0-9.]+)\.app#\1#' \
      | sort -V \
      | tail -1)
    sudo xcode-select -s "/Applications/Xcode_${LATEST_XCODE}.app/Contents/Developer"

- name: Show Xcode version
  run: xcodebuild -version
```

### D-04 + D-05: Branch-aware cache, `android-setup` reuse in `ios.yml`

```yaml
# Source: .github/actions/android-setup/action.yml (read this session, unchanged)
# .github/workflows/ios.yml — replaces the current inline setup-java/setup-gradle
# block (lines 23-34) [VERIFIED: .github/workflows/ios.yml:23-34]
- name: Checkout code
  uses: actions/checkout@v4

- uses: ./.github/actions/android-setup
  with:
    java-version: ${{ env.JAVA_VERSION }}
    cache-read-only: ${{ github.event_name == 'pull_request' }}
```
```yaml
# .github/workflows/android.yml — all four jobs (checks, unit-tests,
# instrumentation-tests, build-debug) replace their current hardcoded
# cache-read-only: 'true'/'false' [VERIFIED: .github/workflows/android.yml:21,38,56,88]
# with the same expression:
- uses: ./.github/actions/android-setup
  with:
    java-version: ${{ env.JAVA_VERSION }}
    cache-read-only: ${{ github.event_name == 'pull_request' }}
```

### D-07: Concurrency group (both workflow files)

```yaml
# Source: docs.github.com/en/actions/using-jobs/using-concurrency (fetched this session)
name: iOS CI   # or "Android CI" — github.workflow differs per file, keeping groups distinct

on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]
  workflow_dispatch:   # ios.yml only — see Open Questions

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

env:
  JAVA_VERSION: 17
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| `gradle-build-action` | `gradle/actions/setup-gradle` | `gradle-build-action` is the predecessor action; `gradle/actions/setup-gradle@v4` is the current maintained action and is already what this repo uses [VERIFIED: .github/actions/android-setup/action.yml:20] | No migration needed — already on the current action |
| Manual `cache-read-only` policy replicating "write only on default branch" | `setup-gradle`'s own built-in default already implements this automatically when `cache-read-only` is left unset | Introduced as the recommended default behavior in the action's history [ASSUMED — websearch, not independently fetched from a changelog] | D-04's explicit branch-aware expression is not strictly novel behavior — it recreates, more explicitly and auditably, what the action's own unset-default already does; the explicit version is still preferred here because the codebase currently *overrides* the smart default with hardcoded `'true'`/`'false'` per job, which must be replaced regardless |

**Deprecated/outdated:**
- Hardcoded `-destination 'platform=iOS Simulator,name=iPhone 16'`: breaks whenever a runner-image rotation stops shipping a device literally named "iPhone 16" (e.g. if only "iPhone 17"/"iPhone 18" remain) — this is the literal root cause CI-01 requires fixing.
- Hardcoded `xcode-select -s /Applications/Xcode_26.0.1.app/Contents/Developer`: breaks whenever that exact point-release bundle is not present on the image (point releases like `26.0.1` are not guaranteed to persist across image updates the way major.minor lines are).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | The exact JSON shape of `xcrun simctl list devices available --json` (`{"devices": {"<SimRuntime key>": [{"name", "udid", "isAvailable", ...}]}}`) and the claim that stale/unavailable records can leak through the `available` filter | Code Examples (D-01), Common Pitfall 1 | If the JSON shape differs from what's documented here, the `jq` filter in the D-01 code example will fail or silently return nothing, causing the resolution step (and therefore the whole iOS job) to fail at `xcodebuild`. Not verified in this session — `xcrun simctl` was attempted locally on this darwin research sandbox but blocked by an unaccepted Xcode license agreement, not run on an actual GitHub-hosted macOS runner. **Recommend the planner add a verification step early in execution: run the exact `jq` pipeline against real `xcrun simctl list devices available --json` output on a live `macos-latest` runner (e.g., via a throwaway workflow_dispatch run) before finalizing the script.** |
| A2 | `maxim-lobanov/setup-xcode`'s exact input semantics (`latest`, `latest-stable`, exact SemVer, `<semver>-beta`) | Standard Stack (Alternatives Considered), Package Legitimacy Audit | Only relevant if the planner chooses this action over the recommended shell-based approach; if input semantics are misremembered, the action could select a beta or wrong-version Xcode. Not used in the primary recommendation, so this is low-impact unless the alternative path is taken. |
| A3 | `github.ref` vs `github.head_ref` semantics differing between `push` and `pull_request` events, and that a PR merge produces a `push` event on the base branch | Common Pitfall 6 | Low risk to this phase's specific recommendation (D-04's expression uses `event_name`, not `ref`, specifically to avoid this pitfall), but if wrong, could affect the planner's understanding of other `ref`-based logic elsewhere in the two workflows. |
| A4 | `jq` is pre-installed (confirmed version 1.8.2) specifically on the `macos-15` runner image, which is the image currently backing the `macos-latest` label as of this research date | Standard Stack, Environment Availability | If `macos-latest` has since rotated past `macos-15` by execution time, `jq`'s presence should be re-verified — GitHub's own runner-images repo is the authoritative, fast-moving source and was directly fetched this session, but runner image labels roll forward on GitHub's own schedule, independent of this repo. |
| A5 | `gradle/actions/setup-gradle`'s own *unset-default* cache-write behavior (write on default branch only) predates and is separate from this phase's explicit D-04 expression | State of the Art | Low risk — this is context/rationale only, not something the plan depends on; D-04's explicit expression is required regardless of what the action's own default does, since the codebase currently overrides that default everywhere. |

**If this table is empty:** N/A — see entries above. Every `[ASSUMED]`-tagged claim in the Common Pitfalls, Code Examples, and State of the Art sections traces to one of these five rows.

## Open Questions

1. **What should `cache-read-only` evaluate to on `ios.yml`'s `workflow_dispatch` trigger?**
   - What we know: `ios.yml` has a `workflow_dispatch` trigger [VERIFIED: .github/workflows/ios.yml:8] that `android.yml` does not have [VERIFIED: .github/workflows/android.yml:1-7 — only `push`/`pull_request` listed]. D-04's literal wording only specifies behavior for `push`-to-`main` and `pull_request`.
   - What's unclear: Whether a manually-triggered `workflow_dispatch` run (which could target any branch, not just `main`) should write to the shared cache.
   - Recommendation: The recommended expression `github.event_name == 'pull_request'` evaluates `workflow_dispatch` to `false` (cache-write enabled), which is a reasonable default since `workflow_dispatch` is manually/trust-gated — but the planner should confirm this matches user intent, since it's a case CONTEXT.md's decisions didn't explicitly cover.

2. **Should "newest available iPhone runtime" (D-01's wording) be interpreted as newest iPhone *model number* or newest iOS *runtime version* hosting a plain iPhone device?**
   - What we know: The parenthetical in D-01 — "(plain \"iPhone \<N\>\" naming, not Plus/Pro Max variants)" — is about device *model* naming, which suggests the intent is "pick the highest N in `iPhone <N>`", not "pick the newest iOS runtime".
   - What's unclear: Apple/`simctl` terminology reserves "runtime" specifically for the OS version (`SimRuntime`), which CONTEXT.md's wording conflates with "device". The plain reading favors model-number interpretation, but this is a terminology mismatch worth a one-line confirmation.
   - Recommendation: Proceed with "highest plain iPhone model number, any runtime" (as implemented in the D-01 code example) since it's the interpretation consistent with the parenthetical and with what gets fed into `-destination name=`; flag for a quick confirmation during planning if there's any doubt.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| GitHub-hosted `macos-latest` runner (currently `macos-15` image) | D-01, D-02, iOS job overall | Not directly probeable from this local sandbox — confirmed via official `actions/runner-images` repo README instead [CITED: github.com/actions/runner-images macos-15-Readme.md] | `macos-15`, Xcode 16.0–26.3 installed, default 16.4 | None needed — this is the target execution environment, not a local dependency |
| `jq` (on the GitHub-hosted macOS runner) | D-01 `jq` filter script | Confirmed present on the runner image (1.8.2) [CITED: github.com/actions/runner-images macos-15-Readme.md] | 1.8.2 (on `macos-15` image) | Plain-text `grep`/`awk` parsing (see Alternatives Considered) if `jq` is ever removed from a future image |
| `xcrun`/`simctl` (local darwin sandbox, for research verification only) | Verifying the D-01 JSON shape locally before trusting the code example | ✗ — `xcrun` binary present but blocked by an unaccepted Xcode license agreement on this research machine | — | None — this only affects local research verification, not CI execution; recommend the planner verify the `jq` pipeline against live output on an actual `macos-latest` runner instead (see Assumptions Log A1) |
| `actionlint` (optional local/CI static YAML lint) | Not required by any locked decision; optional Wave 0 quality gate | ✗ — not installed locally [confirmed via `command -v actionlint`] | — | Skip — no decision requires it; `gh run watch` on a real push is sufficient validation per this phase's scope |
| `gh` CLI | Validation Architecture (watching CI runs) | ✓ | 2.100.0 [confirmed via `gh --version`] | — |

**Missing dependencies with no fallback:** none — every dependency this phase's locked decisions actually require is either already present on the GitHub-hosted runner (confirmed via official docs) or already pinned in the repo.

**Missing dependencies with fallback:** `jq`'s absence (hypothetical future image change) has a documented `grep`/`awk` fallback; `actionlint`'s absence has no impact since it's optional and not required by any locked decision.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | None (CI-workflow-configuration phase — validation is "does the real GitHub Actions run succeed", not a unit-test framework) |
| Config file | none — `.github/workflows/*.yml` are the artifacts under test, not files a framework consumes |
| Quick run command | `gh run watch $(gh run list --branch <branch> --limit 1 --json databaseId --jq '.[0].databaseId')` after pushing to a working branch |
| Full suite command | `gh run list --branch <branch> --limit 2 --json name,conclusion` — checks both `Android CI` and `iOS CI` workflow runs conclude `success` |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|---------------------|-------------|
| CI-01 | iOS job completes end-to-end with dynamically resolved destination | manual-only (real CI run observation — no local iOS simulator harness in this repo) | `gh run watch <run-id>` then inspect the "Build iOS App for Simulator" step log for the resolved device name and a `** BUILD SUCCEEDED **` line | ❌ — no test file; this is a live-run observation by design for a CI-config phase |
| CICD-01 | Gradle cache active and reduces repeated-build time | manual-only (compare two consecutive run durations / cache-hit log lines) | `gh run view <run-id> --log \| grep -i "cache"` — `setup-gradle` posts its own cache-summary lines | ❌ — same as above |
| CICD-02 | Konan cache active and reduces Kotlin/Native compile time | manual-only (unchanged from current behavior — verify it still works after D-05's reshuffling of preceding steps) | `gh run view <run-id> --log \| grep -i "Cache restored from key"` (from the `actions/cache@v4` step) | ❌ — same as above |
| CICD-03 | Android/iOS run as separate `ubuntu-latest`/`macos-latest` jobs | automated static check | `grep -n "runs-on" .github/workflows/*.yml` | ✅ — already true today, no code change required by D-03 |

### Sampling Rate
- **Per task commit:** No local quick-run equivalent exists for GitHub Actions YAML — the fastest feedback loop is a `git push` to a scratch branch and `gh run watch`.
- **Per wave merge:** Full suite = both `android.yml` and `ios.yml` runs green on the integration branch before proceeding.
- **Phase gate:** Both workflows green on a real push to the phase's working branch before `/gsd-verify-work`.

### Wave 0 Gaps
- None blocking. Optional (not required by any locked decision): install `actionlint` (`brew install actionlint`) for pre-push static YAML validation — not currently installed locally [confirmed via `command -v actionlint`] and no existing project convention uses it.

*(No existing test infrastructure gap applies — this phase's "tests" are, by its own nature, live CI runs, which is the correct and sufficient validation surface for CI-configuration changes.)*

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | No | Not touched by this phase (no app auth code) |
| V3 Session Management | No | Not touched by this phase |
| V4 Access Control | No | `GITHUB_TOKEN` permissions are not modified by any locked decision; out of scope |
| V5 Input Validation | Partial | The new D-01/D-02 shell steps parse runner-local tool output (`simctl`, `/Applications` listing) — not attacker-controlled external input, but shell-quoting hygiene around the resolved `$DEVICE_NAME`/`$LATEST_XCODE` values (see Code Examples — always double-quote expansions) is the relevant control |
| V6 Cryptography | No | Not touched by this phase |

### Known Threat Patterns for GitHub Actions CI

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Cache poisoning via untrusted PR-branch cache writes | Tampering | This is exactly what D-04 addresses: `cache-read-only: true` on `pull_request` prevents a malicious/broken PR branch from writing entries a subsequent `main`-branch build would read. Already the locked, in-scope fix. |
| Third-party Action tag mutability (a moved/compromised `@v4`/`@v2`/`@v1` tag) | Tampering / Spoofing | Standard mitigation is pinning to a full commit SHA [CITED: docs.github.com/en/actions/reference/security/secure-use]. **Not currently done** in this repo — `actions/checkout@v4`, `gradle/actions/setup-gradle@v4`, `reactivecircus/android-emulator-runner@v2`, etc. are all tag-pinned, not SHA-pinned [VERIFIED: .github/workflows/android.yml, .github/workflows/ios.yml — all `uses:` lines reference `@v4`/`@v2` tags]. This is a pre-existing gap, **not** something any of D-01–D-07 asks to fix — flagged here for awareness per the Security Domain requirement, not as a blocking task for this phase. |
| Script injection via untrusted context interpolated directly into `run:` blocks | Tampering / Injection | Neither current workflow interpolates PR titles/branch names/issue bodies directly into a `run:` script [VERIFIED: .github/workflows/ios.yml, .github/workflows/android.yml — no `github.event.pull_request.title` or similar found]. The new D-01/D-02 steps must preserve this by only interpolating `steps.*.outputs.*` (script-computed, not attacker-supplied) values, which the Code Examples above already do correctly. |

## Sources

### Primary (HIGH confidence)
- None — no `npm view`/`pip index versions`/package-legitimacy-check-backed findings apply to this phase (no new package ecosystem dependency), and no MCP `context7` server was available in this session for `gradle/actions` docs.

### Secondary (MEDIUM confidence)
- `github.com/actions/runner-images/blob/main/images/macos/macos-15-Readme.md` — directly fetched this session; source of the confirmed installed Xcode range (16.0–26.3, default 16.4) and iPhone simulator device-name set.
- `docs.github.com/en/actions/using-jobs/using-concurrency` — directly fetched this session; source of the `concurrency`/`cancel-in-progress` syntax and the `queue:max`+`cancel-in-progress` validation-error caveat.
- `docs.github.com/en/actions/reference/security/secure-use` — directly fetched this session; source of the SHA-pinning supply-chain guidance.
- `github.com/gradle/actions/blob/main/docs/setup-gradle.md` — directly fetched this session; source of `cache-read-only`'s documented default behavior (write on default branch only, when unset) and its interaction with `cache-cleanup`.
- `.github/workflows/ios.yml`, `.github/workflows/android.yml`, `.github/actions/android-setup/action.yml`, `.github/actions/get-avd-info/action.yml`, `.github/dependabot.yml` — all read directly this session; source of every `[VERIFIED: <path>:<lines>]` claim above.

### Tertiary (LOW confidence)
- WebSearch synthesis on `xcrun simctl list --json` parsing patterns, stale/unavailable-record filtering, `maxim-lobanov/setup-xcode` input semantics, `xcode-select` enumeration approaches, `github.ref`/`github.head_ref` event-semantics differences, and `jq`'s general presence on GitHub-hosted runners — all marked `[ASSUMED]` and logged in the Assumptions Log; none independently fetched from a single authoritative page in this session (the `jq`-presence claim specifically *is* corroborated by the directly-fetched `macos-15-Readme.md` and is therefore upgraded to `[CITED]` in Standard Stack).

## Metadata

**Confidence breakdown:**
- Standard stack: MEDIUM — no new dependency required; existing pins verified by direct file read, `jq` presence confirmed via directly-fetched official runner-images README, but the exact `simctl --json` schema itself is unverified in this session (Assumptions Log A1)
- Architecture: HIGH — the composite-action `default:`-cannot-hold-expressions constraint and the call-site expression pattern are platform mechanics, not opinion, and are consistent with directly-read source files
- Pitfalls: MEDIUM — several pitfalls (stale runtime records, `ref` vs `head_ref` semantics) are websearch-sourced and unverified; the Xcode-default-version pitfall and the iPhone-naming-variant pitfall are both `[CITED]` against the directly-fetched runner-images README

**Research date:** 2026-09-17
**Valid until:** 7 days — GitHub-hosted macOS runner image contents (installed Xcode versions, default Xcode, simulator device sets) rotate on GitHub's own schedule and are explicitly called out in `actions/runner-images` as changing frequently; re-verify the `macos-15-Readme.md` snapshot (or whatever image backs `macos-latest` at execution time) immediately before implementing D-01/D-02 if more than a few days have passed.
