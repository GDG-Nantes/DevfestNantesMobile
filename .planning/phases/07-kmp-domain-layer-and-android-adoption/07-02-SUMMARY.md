---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 02
subsystem: infra
tags: [gradle, kover, ci, docs, convention-plugin]

requires:
  - phase: 07-kmp-domain-layer-and-android-adoption
    provides: ":core:domain module, domainModule and ClassifySessionUseCase (plan 07-01)"
provides:
  - "feature convention plugin depends on :core:domain"
  - "Kover root report merges :core:domain (14 production modules)"
  - "iOS CI runs :core:domain:iosSimulatorArm64Test"
  - "AGENTS.md, README.md, documentation/ describe :core:domain and the new dependency direction"
affects: [07-15 first feature consumer, phase 07 later plans]

actuals:
  tokens: 2600
  tasks: 1
  commits: 1
plan_head_before: 0a5853b44a6fc5cf7208d8bf6b6b4c02e9c89759
plan_head_after: 4f01d9f059d7e408c4d7323e7a3f016793f8fa67
commits: 1

tech-stack:
  added: []
  patterns:
    - "Every devfest.android.feature module gets implementation(project(\":core:domain\"))"

key-files:
  created: []
  modified:
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt
    - build.gradle.kts
    - .github/workflows/ios.yml
    - AGENTS.md
    - README.md
    - documentation/ARCHITECTURE.md
    - documentation/DEVELOPMENT.md

key-decisions:
  - "Dependency direction documented as androidApp -> feature:* -> core:ui -> core:domain -> core:data -> core:network / core:model in AGENTS.md, README.md and ARCHITECTURE.md"

patterns-established:
  - "Docs change in the same commit as a module wiring change (AGENTS.md rule)"

requirements-completed: [SHARED-01]

coverage:
  - id: D1
    description: "Feature modules, Kover and iOS CI see :core:domain"
    requirement: SHARED-01
    verification:
      - kind: other
        ref: "./gradlew -q :feature:agenda:dependencies --configuration debugRuntimeClasspath (lists project ':core:domain')"
        status: pass
      - kind: other
        ref: "git grep kover(project(\":core:domain\")) build.gradle.kts; git grep :core:domain:iosSimulatorArm64Test .github/workflows/ios.yml"
        status: pass
    human_judgment: false
  - id: D2
    description: "AGENTS.md pre-merge list green with the module wired"
    requirement: SHARED-01
    verification:
      - kind: other
        ref: "./gradlew detekt lint; scripts/di-gate.sh (DI-GATE-OK); scripts/kmp-nc-version-gate.sh (KMPNC-VERSION-OK 1.0.6); ./gradlew testDebugUnitTest jvmTest testAndroidHostTest"
        status: pass
    human_judgment: false
  - id: D3
    description: "Docs describe :core:domain and the new dependency direction"
    requirement: SHARED-01
    verification: []
    human_judgment: true
    rationale: "Documentation accuracy and wording need a human read; only the dependency-direction line is grep-checked"

duration: 25min
completed: 2026-10-28
status: complete
---

# Phase 7 Plan 02: Wire :core:domain into features, Kover, iOS CI and docs Summary

**`:core:domain` is now a dependency of every feature module, merged into the Kover report, tested on the iOS simulator in CI, and documented with the new `core:ui -> core:domain -> core:data` direction.**

## Performance

- **Duration:** ~25 min (mostly Gradle detekt/lint and test runs)
- **Tasks:** 1 of 1
- **Files modified:** 7

## Accomplishments

- `AndroidFeatureConventionPlugin` adds `implementation(project(":core:domain"))`; `:feature:agenda`'s debugRuntimeClasspath lists `project ':core:domain'`. No feature code changed.
- Root `build.gradle.kts` adds `kover(project(":core:domain"))` and the comment now says 14 production modules.
- `.github/workflows/ios.yml` runs `:core:domain:iosSimulatorArm64Test` alongside the other KMP simulator tests (RESEARCH Pitfall 9, threat T-07-31).
- AGENTS.md, README.md and documentation/ARCHITECTURE.md (diagram, module table, dependency line, new "Domain (`:core:domain`)" layer section) and documentation/DEVELOPMENT.md (jvmTest and iOS simulator commands) describe the module.

## Task Commits

1. **Task 1: wire :core:domain into features, Kover, iOS CI, docs** - `4f01d9f` (chore)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocker] Verification grep for the runtime classpath used the wrong format**
- **Found during:** Task 1 verification
- **Issue:** The plan's `grep -q 'project :core:domain'` never matches: Gradle prints `project ':core:domain'` (quoted path).
- **Fix:** Checked with the quoted form; the dependency is present (`+--- project ':core:domain'`). No code change.
- **Files modified:** none

**Total deviations:** 1 (verification command format only). **Impact:** none.

No lint or detekt rule flagged the not-yet-used `:core:domain` dependency (flagged assumption held).

## Issues Encountered

None. The worktree needed `local.properties` and `iosApp/iosApp/GoogleService-Info.plist` copied from the main checkout (not committed). The iOS simulator test task itself was not run locally (CI-only per plan); `:core:domain:jvmTest` ran through the `jvmTest` aggregate.

## Known Stubs

None.

## Threat Flags

None.

## Self-Check: PASSED

- Commit 4f01d9f is on HEAD; all 7 modified files committed.
- detekt, lint, DI gate, KMP-NC gate and unit test aggregate all exited 0.
