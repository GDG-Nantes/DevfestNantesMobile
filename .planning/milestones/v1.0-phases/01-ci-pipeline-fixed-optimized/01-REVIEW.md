---
phase: 01-ci-pipeline-fixed-optimized
reviewed: 2026-09-17T00:00:00Z
depth: standard
files_reviewed: 2
files_reviewed_list:
  - .github/workflows/android.yml
  - .github/workflows/ios.yml
findings:
  critical: 0
  warning: 3
  info: 1
  total: 4
status: issues_found
---

# Phase 01: Code Review Report

**Reviewed:** 2026-09-17T00:00:00Z
**Depth:** standard
**Files Reviewed:** 2
**Status:** issues_found

## Summary

Reviewed `.github/workflows/android.yml` and `.github/workflows/ios.yml` against the prior
commit (`b11198c`), focusing on the four stated risk areas: GitHub Actions expression syntax,
uniform/correct branch-aware Gradle cache policy, concurrency cancellation interacting with
cache writes, and correct step ordering after routing the iOS job through the shared
`android-setup` composite action.

The core security-relevant fix is correct: previously `build-debug` in `android.yml` hardcoded
`cache-read-only: 'false'` (writing the shared cache even from PR runs — the actual risk called
out in the task brief), while `checks`/`unit-tests`/`instrumentation-tests` hardcoded
`cache-read-only: 'true'` (never writing, even on `main`). Both files now uniformly use
`cache-read-only: ${{ github.event_name == 'pull_request' }}` at every `android-setup` call
site in both workflows, which correctly gates cache writes to non-PR events (push to `main`,
`workflow_dispatch`) and forces read-only on all `pull_request` events. No call site was missed
and no expression-syntax error was found — this is a real improvement over the prior state.

Composite-action step ordering in `ios.yml` is also correct: `android-setup` (Java/Gradle/
`local.properties`) runs immediately after checkout and before the Konan cache restore, which
in turn runs before the `Build shared framework` step that invokes `gradlew
:shared:embedAndSignAppleFrameworkForXcode`. No `gradlew` invocation precedes setup.

Three residual issues remain, primarily around the new `concurrency` blocks and the
newly-introduced dynamic Xcode/simulator resolution scripts.

## Warnings

### WR-01: `cancel-in-progress: true` can interrupt an in-progress cache-write run on `main`

**File:** `.github/workflows/android.yml:9-11`, `.github/workflows/ios.yml:10-12`
**Issue:** Both workflows define
```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true
```
For `pull_request` events this is safe (each run is already `cache-read-only: true`, so a
cancelled run never attempts to write the shared cache). But for `push` events to `main`,
`github.ref` resolves to the same value (`refs/heads/main`) for every push, so two pushes to
`main` in quick succession (e.g. two PRs merged back-to-back) will cancel the earlier run —
which is exactly the run configured with `cache-read-only: false` and therefore expected to
populate the shared Gradle/Konan/Xcode caches. `setup-gradle`'s cache-save step (and
`actions/cache@v4`'s save step) run as post-job/cleanup actions that can still fire on a
cancelled run, meaning a run that was killed mid-build can persist an incomplete or stale cache
entry that later runs (including PR runs, which read the `main` cache) will restore from.
**Fix:** Scope cancellation to non-default-branch runs only, e.g.:
```yaml
concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: ${{ github.event_name == 'pull_request' }}
```
This keeps the fast-cancel behavior for superseded PR pushes while letting `main` cache-writing
runs complete to a finish (success or clean failure) without being torn down mid-write.

### WR-02: Explicit `cache-cleanup: on-success` silently dropped when `ios.yml` moved to the shared composite action

**File:** `.github/workflows/ios.yml:27-30` (vs. prior inline steps removed in this diff)
**Issue:** Before this phase, `ios.yml` configured `gradle/actions/setup-gradle@v4` directly
with `cache-cleanup: on-success`. The refactor now routes through
`./.github/actions/android-setup`, whose `setup-gradle` invocation does not expose or set
`cache-cleanup` at all. This is a real, diff-visible config regression — the explicit setting
is gone and iOS's Gradle cache cleanup behavior now silently falls back to whatever the pinned
`gradle/actions/setup-gradle@v4` default is, whatever that happens to be at the pinned version.
Whether or not the default matches `on-success` is not verifiable from these two files alone,
which is itself the problem: the intent that was explicit before is now implicit and
unreviewable from the workflow file.
**Fix:** Either add a `cache-cleanup` input to `./.github/actions/android-setup` (defaulting to
`on-success` to preserve prior behavior) and pass it through from `ios.yml`, or confirm the
composite action's current hardcoded behavior is intentional and acceptable for both consumers
(`android.yml` and `ios.yml`) and drop this note with a comment explaining why it was dropped.

### WR-03: No validation/fallback when the dynamic simulator resolution yields an empty name

**File:** `.github/workflows/ios.yml:95-111` (used at `:209`)
**Issue:** `Resolve latest plain iPhone simulator` filters `xcrun simctl list devices` output to
names matching `^iPhone [0-9]+$` (i.e., "iPhone 16" but not "iPhone 16 Pro" or unnumbered names
such as "iPhone Air"), sorts numerically, and takes the last line. If no available simulator
matches that exact pattern — e.g. a future Xcode image where the default runtime only ships
"Pro"/"Air"-style names, or `jq`/`simctl` output changes shape — `DEVICE_NAME` (and therefore
`steps.sim.outputs.device_name`) is silently empty. The value is echoed and written to
`GITHUB_OUTPUT` without any check, and is then interpolated unguarded into
`-destination "platform=iOS Simulator,name=${{ steps.sim.outputs.device_name }}"` at line 209,
producing an obscure `xcodebuild` "unable to find a destination matching the provided
destination specifier" failure many steps later, far from the actual root cause.
**Fix:** Fail fast with a clear message at the resolution step:
```bash
if [ -z "$DEVICE_NAME" ]; then
  echo "::error::No available iPhone simulator matching '^iPhone [0-9]+$' was found" >&2
  exit 1
fi
```

## Info

### IN-01: Xcode version extraction has no match/empty-result guard

**File:** `.github/workflows/ios.yml:80-87`
**Issue:** `LATEST_XCODE` is derived via `ls -d /Applications/Xcode_*.app | sed -E
's#.*/Xcode_([0-9.]+)\.app#\1#' | sort -V | tail -1`. If the glob matches nothing (unlikely but
possible if the runner image changes its install layout) or an installed Xcode path doesn't fit
the plain `Xcode_<digits.dots>.app` shape (e.g. a beta build named `Xcode_16.0_beta.app`), the
`sed` substitution fails to match and passes the line through unchanged, or `LATEST_XCODE` ends
up empty. The subsequent `sudo xcode-select -s "/Applications/Xcode_${LATEST_XCODE}.app/..."`
then targets a non-existent path with only `xcode-select`'s own error message to go on. Low risk
given current GitHub-hosted macOS runner conventions, but worth a one-line guard for the same
reason as WR-03 (fail with a clear message instead of a derived, malformed path).
**Fix:** Add `[ -n "$LATEST_XCODE" ] || { echo "::error::No Xcode_*.app found under /Applications"; exit 1; }` before the `xcode-select` call.

---

_Reviewed: 2026-09-17T00:00:00Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
