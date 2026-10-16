---
phase: 01-ci-pipeline-fixed-optimized
plan: 03
subsystem: infra/ci
tags: [github-actions, ios, gradle, cache, composite-action, konan, ci]

requires:
  - phase: 01-01
    provides: Dynamic Xcode/simulator resolution in ios.yml (Xcode 26.6.0, iPhone 17 baseline for comparison)
  - phase: 01-02
    provides: Branch-aware Gradle cache-write policy pattern at android-setup call sites, applied identically here
provides:
  - Single ./.github/actions/android-setup call site in ios.yml replacing inline Setup Java / Setup Gradle / Create local.properties steps (D-05)
  - Branch-aware cache-read-only expression (github.event_name == 'pull_request') at that call site (D-04)
  - Live-run proof that the Konan cache and macos-latest/ubuntu-latest job separation survived the reshuffle (D-06, D-03)
affects: []

actuals:
  tokens: 436
  tasks: 1
  commits: 1

tech-stack:
  added: []
  patterns:
    - "Shared composite-action call site reused identically across both platform workflows (android.yml's four call sites and now ios.yml's one), with the branch-aware cache expression kept at each call site rather than the composite action's static input default"

key-files:
  created: []
  modified:
    - .github/workflows/ios.yml

key-decisions:
  - "Followed D-05 exactly: removed the three inline Java/Gradle/local.properties steps (formerly lines 27-41) and replaced them with a single `uses: ./.github/actions/android-setup` step, byte-for-byte the shape android.yml uses at its four call sites"
  - "Followed D-04 exactly: cache-read-only set to `${{ github.event_name == 'pull_request' }}` at the ios.yml call site, matching the Android jobs; .github/actions/android-setup/action.yml left untouched since composite-action input defaults are static strings that cannot evaluate an expression"
  - "Left every other step (Konan cache, Homebrew/SwiftGen/DerivedData caches, Xcode selection, simulator resolution, concurrency block) untouched and unreordered, per the plan's explicit scope boundary"

patterns-established:
  - "Cross-platform composite-action adoption: a single shared action (android-setup) now provisions Java+Gradle identically for both the ubuntu-latest Android jobs and the macos-latest iOS job, with only the cache-policy expression varying per call site"

requirements-completed: [CICD-01, CICD-02, CICD-03]

coverage:
  - id: D1
    description: "ios.yml provisions Java and Gradle solely through ./.github/actions/android-setup, with no inline actions/setup-java or gradle/actions/setup-gradle steps and no separate local.properties step remaining"
    requirement: "CICD-01"
    verification:
      - kind: static
        ref: "ruby -ryaml gate: parses ios.yml, confirms one android-setup call, no inline setup-java/setup-gradle steps, Konan cache present/correctly keyed, and both android-setup and the Konan cache precede 'Build shared framework' — printed 'ios setup+konan ok'"
        status: pass
    human_judgment: false
  - id: D2
    description: "The android-setup call site carries the same branch-aware cache-read-only expression (github.event_name == 'pull_request') as the four android.yml call sites"
    requirement: "CICD-01"
    verification:
      - kind: static
        ref: "same gate as D1 — asserts cache-read-only references both github.event_name and pull_request"
        status: pass
    human_judgment: false
  - id: D3
    description: "The ~/.konan cache is present, unchanged, and still restores before the Kotlin/Native compile (Build shared framework)"
    requirement: "CICD-02"
    verification:
      - kind: static
        ref: "same gate as D1 — Konan cache step index confirmed lower than 'Build shared framework' step index; key still references hashFiles, shared/**/*.kt, gradle/libs.versions.toml"
        status: pass
    human_judgment: false
  - id: D4
    description: "Android runs on ubuntu-latest and iOS on macos-latest in two separate workflow files, with no operating-system matrix introduced in either"
    requirement: "CICD-03"
    verification:
      - kind: static
        ref: "ruby -ryaml gate across ios.yml and android.yml — printed 'job separation ok'"
        status: pass
    human_judgment: false
  - id: D5
    description: ".github/actions/android-setup/action.yml still forwards its own inputs.cache-read-only to setup-gradle and contains no github.event_name reference; android.yml is read-only in this plan"
    requirement: "CICD-01"
    verification:
      - kind: static
        ref: "ruby -ryaml content-based gate — printed 'composite action intact'"
        status: pass
    human_judgment: false
  - id: D6
    description: "A live workflow_dispatch run of ios.yml on this branch, after the setup-layer swap, concludes completed:success with a BUILD SUCCEEDED banner and an unchanged Resolved simulator / Selected Xcode line, and the Setup Gradle / Konan cache step summaries show sane cache behaviour (writable + saved for Gradle, cache hit for Konan)"
    requirement: "CICD-02"
    verification:
      - kind: integration
        ref: "https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35228437188 — status completed:success; log contains 1x 'BUILD SUCCEEDED', 'Selected Xcode: 26.6.0', 'Resolved simulator: iPhone 17' (both match Plan 01's baseline run 35225356756); Setup Gradle showed cache-read-only: false and multiple 'Saved cache entry with key gradle-*' lines on job-post cleanup (this run wrote the shared cache since it is workflow_dispatch, not pull_request); 'Cache Kotlin Multiplatform builds' (Konan) reported a cache hit on its primary key rather than erroring or missing"
        status: pass
    human_judgment: false

duration: 21min
completed: 2026-09-17
status: complete
---

# Phase 1 Plan 3: iOS Shared Gradle Setup Summary

**Collapsed ios.yml's inline Java/Gradle/local.properties provisioning onto the shared `android-setup` composite action with the same branch-aware cache policy the Android jobs use, and proved on a live macos-latest run (Xcode 26.6.0, iPhone 17, BUILD SUCCEEDED, cache-writable on workflow_dispatch with a Konan cache hit) that the Konan cache and platform job separation survived the reshuffle.**

## Performance
- **Duration:** ~21min (dominated by the ~11min live GitHub Actions run)
- **Started:** 2026-09-17T13:29:00Z (approx, session start after reading required files)
- **Completed:** 2026-09-17T14:00:05Z
- **Tasks:** 1 completed
- **Files modified:** 1 (`.github/workflows/ios.yml`)

## Accomplishments
- Removed the three inline `Setup Java`, `Setup Gradle` and `Create local.properties` steps from `ios.yml` and replaced them with a single `uses: ./.github/actions/android-setup` step — the identical shape used at all four `android.yml` call sites (D-05).
- That call site now carries `cache-read-only: ${{ github.event_name == 'pull_request' }}`, the same branch-aware expression as the Android jobs, replacing the old permanent `cache-read-only: 'true'` pin (D-04).
- Confirmed via static YAML gates that: no inline `actions/setup-java` / `gradle/actions/setup-gradle` steps survive; the `~/.konan` cache step is unchanged and still precedes `Build shared framework`; every iOS job runs on `macos-latest` and every Android job on `ubuntu-latest` with no `os` matrix in either file; and `.github/actions/android-setup/action.yml` still forwards its own `inputs.cache-read-only` and contains no `github.event_name` reference.
- Proved the change end-to-end on a live `workflow_dispatch` run (run id `35228437188`, https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35228437188): `completed:success`, one `BUILD SUCCEEDED` banner, `Selected Xcode: 26.6.0` and `Resolved simulator: iPhone 17` — both identical to Plan 01's baseline run (`35225356756`), confirming the setup-layer swap did not disturb toolchain resolution.
- Confirmed the cache evidence needed for CICD-01/CICD-02: the `Setup Gradle` step reported `cache-read-only: false` on this `workflow_dispatch` run (correctly cache-writable per the branch-aware policy) and its post-job cleanup logged multiple `Saved cache entry with key gradle-*` lines (dependencies, transforms, wrapper, home, etc.); the `Cache Kotlin Multiplatform builds` (Konan) step reported "Cache hit occurred on the primary key ..., not saving cache" — a clean restore, not an error or miss.

## Task Commits
1. **Task 1: Route iOS setup through the shared composite action with branch-aware caching (D-05, D-04)** - `95a7ad5` (feat)

**Plan metadata:** (recorded in the following commit, see git_commit_metadata step)

## Files Created/Modified
- `.github/workflows/ios.yml` - Replaced inline `Setup Java` / `Setup Gradle` / `Create local.properties` steps with a single `uses: ./.github/actions/android-setup` step carrying `java-version: ${{ env.JAVA_VERSION }}` and `cache-read-only: ${{ github.event_name == 'pull_request' }}`. No other step touched or reordered.

## Decisions Made
- Kept the branch-aware cache expression at the `ios.yml` call site rather than moving it into the composite action's input default, per the plan's explicit platform constraint (composite-action input defaults are static strings and cannot evaluate an expression at parse time).
- Did not attempt to reconcile the composite action's `continue-on-error: true` on its `setup-gradle` step (pre-existing, shared with all four Android jobs) — outside this phase's locked decisions. Verified the live run's downstream `BUILD SUCCEEDED` banner instead of trusting the job conclusion alone, per the threat register's T-01-08 mitigation.
- Did not "fix" the loss of `cache-cleanup: on-success` from the removed inline step — assessed as inert per the plan's flagged assumption, since that setting only has an effect on a writable cache and the inline step was pinned permanently read-only.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None. The static YAML gates passed on the first attempt after the single edit. The live `workflow_dispatch` run (id `35228437188`) took slightly longer to complete (~11 minutes for the run itself, from 13:39:20Z dispatch to 13:50:09Z completion) than Plan 01's baseline run but passed cleanly on the first attempt: `completed:success`, `BUILD SUCCEEDED`, `Selected Xcode: 26.6.0`, `Resolved simulator: iPhone 17` — both toolchain values unchanged from Plan 01's recorded baseline, confirming this setup-layer swap did not disturb toolchain resolution.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness

Phase 01 (CI Pipeline Fixed & Optimized) is now complete across all 3 plans:
- Plan 01: Dynamic iOS Xcode/simulator resolution + concurrency (CI-01)
- Plan 02: Branch-aware Gradle cache policy + concurrency on `android.yml` (CICD-01)
- Plan 03 (this plan): Shared composite-action setup + branch-aware cache policy on `ios.yml`, with Konan cache and job separation proven intact (CICD-01, CICD-02, CICD-03)

One item is still deferred to phase-level verification per Plan 02's SUMMARY: the Android CI workflow's live run on the eventual phase pull request to `main` (no PR exists yet as of this plan; `android.yml` has no `workflow_dispatch` trigger to dispatch directly). `/gsd-verify-work` or `/gsd-ship` should confirm that run is green with a read-only cache before merge. This plan's own iOS live-run evidence is already captured above and needs no further phase-level re-verification.

One flagged assumption from this plan's threat model is worth a one-line confirmation at phase verification: the `workflow_dispatch` cache-write policy (cache-writable, since D-04's expression only names `push`/`pull_request` and evaluates `workflow_dispatch` to `false`/writable) was inferred as the researched, defensible default rather than explicitly decided in CONTEXT.md.

---
*Phase: 01-ci-pipeline-fixed-optimized*
*Completed: 2026-09-17*

## Self-Check: PASSED
- FOUND: .github/workflows/ios.yml
- FOUND: .planning/phases/01-ci-pipeline-fixed-optimized/01-03-SUMMARY.md
- FOUND: 95a7ad5 (Task 1 commit)
