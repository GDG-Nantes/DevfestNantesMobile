---
phase: 05-test-coverage-retrofit
plan: 01
subsystem: testing
tags: [kover, coverage, gradle, build-logic, ios-ci, github-actions]

requires:
  - phase: 04-decentralised-di
    provides: ":core:testing fakes and the Koin test setup that later plans extend"
provides:
  - "Kover 0.9.11 applied through the three build-logic conventions, merged at the root across 13 production modules (report-only)"
  - "kover-summary.sh with summary and compare modes (KOVER-IMPROVED / KOVER-NOT-IMPROVED)"
  - "Committed deterministic pre-phase Kover baseline (report XML + summary)"
  - "GraphQLStoreLiveNetworkTest: manual-only schema-drift canary behind -PliveNetworkTests"
  - "iOS CI runs :shared, :core:model, :core:data, :core:testing iosSimulatorArm64Test"
  - "Draft phase PR #421 with both workflows green"
affects: [05-02, 05-03, 05-04, 05-05, 05-06, 05-07, 05-08, 05-09, 05-10]

actuals:
  tokens: 22000
  tasks: 3
  commits: 3

tech-stack:
  added: ["org.jetbrains.kotlinx.kover 0.9.11 (Gradle plugin)"]
  patterns:
    - "Coverage plugin applied by id in every convention; root applies it for real and aggregates via kover(project(...))"
    - "Live-backend test gated by a Gradle property plus a class-name filter on the jvmTest task"

key-files:
  created:
    - .planning/phases/05-test-coverage-retrofit/kover-summary.sh
    - .planning/phases/05-test-coverage-retrofit/kover/baseline-report.xml
    - .planning/phases/05-test-coverage-retrofit/kover/baseline-summary.txt
  modified:
    - gradle/libs.versions.toml
    - build.gradle.kts
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/KmpLibraryConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidLibraryConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidApplicationConventionPlugin.kt
    - core/data/build.gradle.kts
    - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreLiveNetworkTest.kt
    - .github/workflows/ios.yml

key-decisions:
  - "Kover pinned at 0.9.11 after a read-only Maven metadata re-check (still the newest release on 2026-10-15)"
  - "Live-canary mechanism: Gradle property liveNetworkTests plus excludeTestsMatching('*LiveNetwork*') on the jvmTest task (D-15 discretion)"
  - "Baseline measured with the canary excluded, so :core:data reads 26.25% rather than the 82.05% a network-fed run showed"

patterns-established:
  - "Pattern: kover-summary.sh compare <baseline> <final> is the single before/after instrument for 05-10"

requirements-completed: [TEST-02, TEST-04]

coverage:
  - id: D1
    description: "Kover wired through build-logic into 13 production modules and merged at the root, report-only"
    requirement: TEST-04
    verification:
      - kind: other
        ref: "./gradlew koverXmlReport :androidApp:assembleDebug && kover-summary.sh summary build/reports/kover/report.xml (13 MODULE lines, KOVER-WIRED-OK)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Live-network canary is manual-only; default :core:data:jvmTest is offline"
    requirement: TEST-02
    verification:
      - kind: other
        ref: "./gradlew :core:data:jvmTest (no LiveNetwork result) and -PliveNetworkTests --tests '*LiveNetwork*' (result present)"
        status: pass
    human_judgment: false
  - id: D3
    description: "Deterministic pre-phase baseline recorded; compare of baseline with itself yields KOVER-NOT-IMPROVED and exit 1"
    requirement: TEST-04
    verification:
      - kind: other
        ref: "kover-summary.sh compare baseline-report.xml baseline-report.xml"
        status: pass
    human_judgment: false
  - id: D4
    description: "iOS CI runs the core commonTest suites; draft PR green on both workflows"
    requirement: TEST-02
    verification:
      - kind: other
        ref: "gh run: android.yml 38795714709 and ios.yml 38795715108 on 61810da, core iOS test tasks present in the ios log"
        status: pass
    human_judgment: false

duration: 20min
completed: 2026-10-15
status: complete
plan_head_before: 68419e86d16ee3515799cc4f39fc7c9f1463ee28
plan_head_after: 61810da0f5e5193695228307a96640584afca927
commits: 3
---

# Phase 5 Plan 01: Kover Baseline, Canary Split and iOS CI Widening Summary

**Report-only Kover 0.9.11 wired through the build-logic conventions into 13 modules with a committed deterministic pre-phase baseline (15.96% line coverage), the live GraphQL test turned into a manual-only canary, and iOS CI widened to the core commonTest suites.**

## Performance

- **Duration:** about 20 min
- **Tasks:** 3 (1 tracer, 2 auto)
- **Files modified:** 11 (3 new, 8 changed or renamed)

## Accomplishments

- Kover is applied by `KmpLibraryConventionPlugin`, `AndroidLibraryConventionPlugin` and `AndroidApplicationConventionPlugin`. The root applies it for real, lists the 13 production modules with `kover(project(...))` and excludes generated Android classes, the Apollo-generated `core.network.graphql` package, `ComposableSingletons` and `@Preview` functions. No verify rule and no threshold exist (TEST-04 prohibition honoured). `:core:testing` is deliberately not aggregated.
- `kover-summary.sh` reads the merged JaCoCo XML: `summary` prints TOTAL LINE/INSTRUCTION/BRANCH, one MODULE line per production module and `UNMAPPED` lines (none occurred); `compare` uses exact integer cross-multiplication and exits 1 with `KOVER-NOT-IMPROVED` unless the final LINE ratio is strictly higher.
- `GraphQLStoreJvmTest` was renamed with `git mv` to `GraphQLStoreLiveNetworkTest`, its header moved to `devfestnantes2025` (matching production), with a KDoc giving the run command. `:core:data:jvmTest` excludes `*LiveNetwork*` unless `-PliveNetworkTests` is present.
- Baseline captured and committed under `kover/`.
- `ios.yml` step renamed `Run shared and core iOS tests` and now runs `:shared`, `:core:model`, `:core:data`, `:core:testing` `iosSimulatorArm64Test`. The CI log confirms the three core tasks ran.
- Draft PR https://github.com/GDG-Nantes/DevfestNantesMobile/pull/421 opened; android.yml (run 38795714709) and ios.yml (run 38795715108) are green on 61810da.

## Baseline (measured on 3c81576 plus the Task 2 change set, committed in 8983b66)

The baseline was measured on the Kover wiring commit 3c81576 with the Task 2 working-tree changes (canary rename and exclusion, no production or assertion change) applied, then committed as 8983b66. Canary excluded.

```
TOTAL LINE 567/3552 15.96%
TOTAL INSTRUCTION 3187/34293 9.29%
TOTAL BRANCH 45/1727 2.61%
MODULE :androidApp LINE 54/313 17.25%
MODULE :core:analytics LINE 20/154 12.99%
MODULE :core:data LINE 63/240 26.25%
MODULE :core:model LINE 183/224 81.70%
MODULE :core:network LINE 20/29 68.97%
MODULE :core:ui LINE 10/325 3.08%
MODULE :feature:about LINE 23/273 8.42%
MODULE :feature:agenda LINE 67/688 9.74%
MODULE :feature:session-detail LINE 43/359 11.98%
MODULE :feature:settings LINE 26/379 6.86%
MODULE :feature:speakers LINE 45/404 11.14%
MODULE :feature:venue LINE 6/157 3.82%
MODULE :shared LINE 7/7 100.00%
```

For reference, the Task 1 tracer run, which still executed the live canary, showed TOTAL LINE 740/3585 20.64% (`:core:data` 82.05%). That figure is not the baseline because it depended on the network.

## Task Commits

1. Task 1 (tracer): `3c81576` build(05-01): Kover coverage report through the build-logic conventions
2. Task 2: `8983b66` test(05-01): live canary manual-only; record the Kover baseline
3. Task 3: `61810da` ci(05-01): run core commonTest suites on the iOS simulator

## Decisions Made

- Kover 0.9.11, re-verified as the newest release via the Maven metadata before pinning (T-5-SC mitigation).
- The version entry sits next to `koin` in the catalog `[versions]` table, matching the file's loose ordering.
- The tracer's verify was automated-only and `human_verify_mode` is `end-of-phase`, so the gate was a re-run of the verify (passed), not a checkpoint.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None. Kover's total report ran cleanly across debug and release host tests with no variant narrowing needed.

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None. The new surface is a build-time Gradle plugin only (T-5-SC pinned) and a test-task filter (T-5-01 mitigated, verified by CANARY-EXCLUDED and CANARY-RUNNABLE).

## Next Phase Readiness

Ready for 05-02. The canary's `SimpleDateFormat` assertion is intentionally untouched here and is adapted in 05-02 (D-07). The baseline is committed before any TEST-01 code change.

## Self-Check: PASSED

Files, commits 3c81576, 8983b66 and 61810da verified present; verify tokens KOVER-WIRED-OK, CANARY-EXCLUDED, CANARY-RUNNABLE, BASELINE-RECORDED, IOS-CORE-TESTS-OK, CI-GREEN-BOTH and CI-RUNS-CORE-IOS-TESTS all observed.
