# Phase 1: CI Pipeline Fixed & Optimized - Pattern Map

**Mapped:** 2026-09-17
**Files analyzed:** 2 (modified only — this phase creates no new files)
**Analogs found:** 2 / 2 (both self-referential: each modified workflow is also the best analog for the other's equivalent sections; the composite action supplies the D-05 reuse target)

## Note on scope

This phase is CI-configuration-only. No new files are created; both files under change
(`.github/workflows/ios.yml`, `.github/workflows/android.yml`) already exist and already
contain the closest available patterns for most of the required edits (existing
`actions/cache@v4` blocks, existing `android-setup` composite-action call sites). Where no
codebase analog exists (dynamic `simctl`/Xcode shell resolution, the `concurrency:` block),
RESEARCH.md's Code Examples section is the canonical source — flagged explicitly below
rather than invented from a false analog.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|--------------------|------|-----------|-----------------|----------------|
| `.github/workflows/ios.yml` | workflow/config | event-driven | `.github/workflows/android.yml` (cache/composite-action pattern) + `.github/actions/android-setup/action.yml` (D-05 target) + itself (existing `actions/cache@v4` blocks, D-01/D-02 insertion point) | role-match (cache/composite patterns); no analog (D-01/D-02 shell logic, D-07 block — use RESEARCH.md) |
| `.github/workflows/android.yml` | workflow/config | event-driven | `.github/actions/android-setup/action.yml` (existing call-site pattern, 4 instances) + `.github/workflows/ios.yml` (D-07 sibling block) | role-match (cache-read-only call-site pattern already exists 4x in this file); no analog for `concurrency:` block itself |
| `.github/actions/android-setup/action.yml` | provider/composite-action | request-response (inputs→outputs) | n/a — **unchanged by this phase**; it is itself the analog other files copy from | exact (used as-is) |

No files are created. `.github/actions/get-avd-info/action.yml` is unaffected by any decision — noted only as a general composite-action style reference (small `inputs:`/`outputs:`/bash-steps shape), not a direct analog for anything in scope.

## Pattern Assignments

### `.github/workflows/ios.yml` (workflow/config, event-driven)

**Analogs:** itself (existing cache blocks, lines 39-85), `.github/actions/android-setup/action.yml` (D-05 target), `.github/workflows/android.yml` (D-04 call-site expression pattern, lines 18-21/34-38/52-56/84-88)

#### D-05 — Replace inline Java/Gradle setup with the `android-setup` composite action

**Current inline block to remove** (`ios.yml` lines 23-37):
```yaml
- name: Setup Java
  uses: actions/setup-java@v4
  with:
    distribution: 'zulu'
    java-version: ${{ env.JAVA_VERSION }}

- name: Setup Gradle
  uses: gradle/actions/setup-gradle@v4
  with:
    gradle-version: wrapper
    cache-read-only: 'true'
    cache-cleanup: on-success

- name: Create local.properties
  run: echo "sdk.dir=$ANDROID_HOME" > ./local.properties
```

**Replacement — copy the call pattern from `android.yml` lines 17-21** (identical shape used 4x already):
```yaml
- uses: actions/checkout@v4
- uses: ./.github/actions/android-setup
  with:
    java-version: ${{ env.JAVA_VERSION }}
    cache-read-only: 'true'
```
The composite action itself (`.github/actions/android-setup/action.yml` lines 13-29) already performs `setup-java` (zulu, `inputs.java-version`) → `setup-gradle` (`gradle-version: wrapper`, `cache-overwrite-existing: true`, `cache-read-only: ${{ inputs.cache-read-only }}`) → `local.properties` creation — a strict superset of the inline block being removed, so no behavior is lost. Note the composite action does not set `cache-cleanup: on-success` (the inline block did); this is a pre-existing minor divergence, not something this phase's decisions ask to reconcile — leave as-is unless the planner decides otherwise.

#### D-04 — Branch-aware `cache-read-only` at the `android-setup` call site

Per RESEARCH.md Pattern 1 (composite-action `default:` cannot hold `${{ }}` expressions — the value **must** be computed at the call site), replace the static `'true'` shown above with:
```yaml
- uses: ./.github/actions/android-setup
  with:
    java-version: ${{ env.JAVA_VERSION }}
    cache-read-only: ${{ github.event_name == 'pull_request' }}
```
This collapses D-05's replacement step and D-04's policy change into the same edit in `ios.yml`.

#### D-01 — Dynamic simulator resolution (no codebase analog — synthesize from RESEARCH.md)

**No existing file in this repo resolves `simctl` output or filters JSON via `jq`.** The closest structural analog is the existing keyed-cache step pattern (a `run:`/`id:`-based step producing an output later steps consume — see `steps.avd-info.outputs.*` in `.github/actions/get-avd-info/action.yml` lines 17-27, which is the one place in this repo that already does "shell step → `$GITHUB_OUTPUT` → consumed by a later step's `${{ steps.X.outputs.Y }}` expression"). Follow that output-plumbing shape; use RESEARCH.md's Code Examples → "D-01: Resolve the newest plain iPhone simulator name" for the actual `jq`/`simctl` script body (already vetted against Common Pitfall 1 stale-record filtering and Common Pitfall 4 loose-regex filtering).

**Target line to replace** (`ios.yml` line 192, inside "Build iOS App for Simulator", currently lines 185-198):
```yaml
-destination 'platform=iOS Simulator,name=iPhone 16' \
```
becomes:
```yaml
-destination "platform=iOS Simulator,name=${{ steps.sim.outputs.device_name }}" \
```
with a new preceding step (`id: sim`) inserted before this build step, placed logically near the existing "Show available simulators" step (`ios.yml` lines 93-94) since that step already demonstrates `xcrun simctl list devices available` is the right diagnostic anchor point.

#### D-02 — Dynamic Xcode selection (no codebase analog — synthesize from RESEARCH.md)

**Target to replace** (`ios.yml` line 88):
```yaml
- name: Select Xcode version
  run: sudo xcode-select -s /Applications/Xcode_26.0.1.app/Contents/Developer
```
Replace with the enumerate-`/Applications/Xcode_*.app`-then-`sort -V` shell block from RESEARCH.md's "D-02: Resolve and select the latest installed Xcode" code example. Keep the following `- name: Show Xcode version` step (`ios.yml` lines 90-91, `run: xcodebuild -version`) unchanged — it already serves as the verification step RESEARCH.md's example also recommends.

#### D-07 — Concurrency block (no codebase analog — synthesize from RESEARCH.md)

Insert a top-level `concurrency:` key after `on:` (`ios.yml` lines 3-8) and before `env:` (`ios.yml` lines 10-11):
```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```
Neither workflow file currently has a `concurrency:` block, so there is no in-repo analog; this is copied verbatim from RESEARCH.md's D-07 Code Example, which itself cites `docs.github.com/en/actions/using-jobs/using-concurrency` directly. Apply the identical block shape to `android.yml` (see below) — `github.workflow` differs per file ("iOS CI" vs "Android CI") so the two groups never collide.

---

### `.github/workflows/android.yml` (workflow/config, event-driven)

**Analog:** itself — the `cache-read-only` call-site pattern already exists 4 times in this exact file; `.github/workflows/ios.yml`'s D-07 block (once added) is the sibling pattern for consistency.

#### D-04 — Branch-aware `cache-read-only` at all 4 call sites

**Current 4 call sites** (`android.yml` lines 18-21, 34-38, 52-56, 84-88):
```yaml
- uses: ./.github/actions/android-setup
  with:
    java-version: ${{ env.JAVA_VERSION }}
    cache-read-only: 'true'    # 'false' only in build-debug, line 88
```
**Replacement, identical at all 4 sites** (collapses the existing `'true'`×3 / `'false'`×1 split into one expression, per RESEARCH.md Pattern 1):
```yaml
- uses: ./.github/actions/android-setup
  with:
    java-version: ${{ env.JAVA_VERSION }}
    cache-read-only: ${{ github.event_name == 'pull_request' }}
```
Applies to jobs `checks` (line 18-21), `unit-tests` (line 34-38), `instrumentation-tests` (line 52-56), `build-debug` (line 84-88) — same edit, four locations, no per-job variation needed since the previous `build-debug`-only `'false'` is now subsumed by the shared expression (push events, including to `main`, evaluate to `false`/write-enabled across all four jobs).

#### D-07 — Concurrency block

Insert after `on:` (`android.yml` lines 3-7) and before `env:` (`android.yml` lines 8-9):
```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```
Same block shape as `ios.yml`'s D-07 addition — copy verbatim, only `github.workflow` context value differs at runtime ("Android CI" per `android.yml` line 1).

---

## Shared Patterns

### Composite-action reuse for Java/Gradle setup
**Source:** `.github/actions/android-setup/action.yml` (unchanged), called from `.github/workflows/android.yml` lines 18-21 (and 3 more identical sites)
**Apply to:** `ios.yml`'s D-05 replacement of its inline `setup-java`/`setup-gradle`/`local.properties` block
```yaml
- uses: ./.github/actions/android-setup
  with:
    java-version: ${{ env.JAVA_VERSION }}
    cache-read-only: ${{ github.event_name == 'pull_request' }}
```

### Branch-aware Gradle cache policy (call-site expression, never composite-action default)
**Source:** RESEARCH.md Pattern 1 / Pitfall 3 — platform constraint, not an existing codebase pattern (the codebase currently hardcodes `'true'`/`'false'` strings, which is exactly what's being replaced)
**Apply to:** every `- uses: ./.github/actions/android-setup` call site in both `android.yml` (4 sites) and `ios.yml` (1 site, new after D-05)
```yaml
cache-read-only: ${{ github.event_name == 'pull_request' }}
```
**Do not** attempt to move this into `.github/actions/android-setup/action.yml`'s `inputs.cache-read-only.default:` (currently `'false'`, lines 4-7 of that file) — composite-action `default:` fields are static strings, not evaluated expressions.

### Keyed `actions/cache@v4` blocks (existing convention, unaffected by this phase but must not be broken by D-01/D-02/D-05 reshuffling)
**Source:** `.github/workflows/ios.yml` lines 39-85 (Konan/KMP, Homebrew, SwiftGen caches) and lines 112-120 (SwiftGen-generated)
**Apply to:** No new cache block is required by any locked decision (D-06 explicitly keeps the Konan cache as-is); this pattern is documented here only so the planner/executor preserves the existing `key:`/`restore-keys:` shape and step ordering relative to the Xcode-selection/simulator-resolution steps being inserted by D-01/D-02, and relative to the D-05 setup-step replacement — none of these existing cache blocks should move or be reordered.
```yaml
- name: Cache Kotlin Multiplatform builds
  uses: actions/cache@v4
  with:
    path: |
      ~/.konan
      shared/build
      shared/.gradle
    key: ${{ runner.os }}-kmp-${{ hashFiles('shared/**/*.kt', 'shared/build.gradle.kts', 'gradle/libs.versions.toml') }}
    restore-keys: |
      ${{ runner.os }}-kmp-
```

### Concurrency block (top-level, once per workflow file)
**Source:** RESEARCH.md D-07 Code Example (no codebase analog — first use in this repo), citing `docs.github.com/en/actions/using-jobs/using-concurrency`
**Apply to:** both `android.yml` and `ios.yml`, each as its own top-level block (not shared — `github.workflow` differs)
```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```
**Anti-pattern to avoid:** do not add a `queue:` key alongside `cancel-in-progress: true` — GitHub documents this combination as a hard validation error (RESEARCH.md Pitfall 5).

### Shell-output plumbing (`id:` step → `$GITHUB_OUTPUT` → later `${{ steps.X.outputs.Y }}`)
**Source:** `.github/actions/get-avd-info/action.yml` lines 17-27 (only existing example of this shape in the repo, e.g. `echo "arch=x86_64" >> $GITHUB_OUTPUT` then consumed as `steps.avd-info.outputs.arch` in `android.yml` line 75)
**Apply to:** the new D-01 simulator-resolution step (`id: sim`, outputs `device_name`) and optionally D-02's Xcode-selection step if its result needs to be referenced later (RESEARCH.md's D-02 example does not plumb an output, since `xcode-select` itself is the side effect — only D-01 needs the output-passing shape)
```bash
echo "device_name=$DEVICE_NAME" >> "$GITHUB_OUTPUT"
```
consumed later as `${{ steps.sim.outputs.device_name }}`.

## No Analog Found

| File / Section | Role | Data Flow | Reason |
|------|------|-----------|--------|
| D-01 `simctl`/`jq` simulator-resolution script (new step in `ios.yml`) | shell script (workflow step) | transform (JSON in → string out) | No existing file in this repo parses `simctl`/JSON output; RESEARCH.md's directly-provided Code Example is the source of truth (cross-checked against Pitfall 1 and Pitfall 4) |
| D-02 Xcode-enumeration script (new step in `ios.yml`) | shell script (workflow step) | transform (filesystem listing → selected path) | No existing file enumerates `/Applications/Xcode_*.app`; RESEARCH.md's Code Example is the source, corroborated by the directly-fetched `macos-15-Readme.md` version-range finding |
| D-07 `concurrency:` block (new top-level key in both workflow files) | workflow/config | event-driven (cancellation policy) | Neither workflow currently has a `concurrency:` block; copied verbatim from RESEARCH.md, sourced from official GitHub docs |

## Metadata

**Analog search scope:** `.github/workflows/`, `.github/actions/` (entire directory tree — only 4 files exist in this repo's CI surface, all read in full)
**Files scanned:** 4 (`ios.yml`, `android.yml`, `android-setup/action.yml`, `get-avd-info/action.yml`) — all git-tracked, verified via `git ls-files`
**Pattern extraction date:** 2026-09-17
