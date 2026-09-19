---
phase: 02-dependency-build-tooling-upgrade
plan: 05
subsystem: build
tags: [gradle, declarative-dsl, dcl, settings-gradle, documentation]

# Dependency graph
requires:
  - phase: 02-dependency-build-tooling-upgrade
    plan: 04
    provides: Firebase BOM 34.19.0, kotlinx-coroutines 1.11.0, kotlinx-serialization 1.11.0, kotlinx-datetime 0.8.0 — the full Phase 2 toolchain baseline this plan's pilot and consolidation build on
provides:
  - settings.gradle.kts converted to settings.gradle.dcl (Gradle Declarative DSL), building and CI-green — the project's one production DCL file
  - Live-verified, dated evidence that Declarative Gradle remains experimental/unready for full-module (AGP/KMP) adoption, recorded for Phase 3 to re-check rather than re-derive
  - A single consolidated `.planning/STATE.md` record of every BUILD-01..BUILD-07 outcome for the whole phase, keyed by requirement id
  - `.planning/PROJECT.md` Key Decisions row for Phase 2's staged-upgrade outcome
affects: [phase-3-multi-module]

# Actuals (#2632)
actuals:
  tokens: 4100
  tasks: 2
  commits: 2

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "A settings file whose content is limited to core Gradle settings APIs (pluginManagement/dependencyResolutionManagement repositories, rootProject.name, include()) converts to Declarative Gradle (.dcl) cleanly on stable Gradle 9.7.1 with zero experimental flags — DCL's still-unready 'Software Types' surface (javaApplication/androidApplication/etc., requiring experimental ecosystem plugins and nightly Gradle builds) is a separate, much less mature concern that only applies to full project/module build files, not to a plain settings file"

key-files:
  created:
    - settings.gradle.dcl
  modified:
    - .planning/STATE.md
    - .planning/PROJECT.md
  deleted:
    - settings.gradle.kts

key-decisions:
  - "settings.gradle.kts -> settings.gradle.dcl succeeded on the primary attempt; the buildSrc fallback was never needed"
  - "enableFeaturePreview(\"TYPESAFE_PROJECT_ACCESSORS\") and @file:Suppress(\"UnstableApiUsage\") dropped as behavior-preserving simplifications, confirmed (not assumed) via a project-wide grep for typesafe accessor usage (zero hits) and a full clean build"
  - "androidApp/shared build files deliberately left on Kotlin DSL per D-08's scope boundary — corroborated by a live 2026-09-19 re-check of Declarative Gradle's own docs, which still call the project 'not ready for adoption by plugin authors, build engineers or software engineers' with no update since an April 2025 EAP3 snapshot"

patterns-established:
  - "Settings-file DCL conversion is a distinct, much lower-risk case than build-file (module) DCL conversion — worth re-testing per-settings-file even when a module's own DCL conversion is known to be blocked"

requirements-completed: [BUILD-07]

coverage:
  - id: D1
    description: "settings.gradle.kts converted to settings.gradle.dcl; project topology (root DevFest_Nantes, :androidApp, :shared) intact; full clean build (assemble/test/detekt/lint, both variants) and the iOS Kotlin/Native compile target both succeed through the DCL settings file; both CI workflows green on the pilot commit"
    requirement: "BUILD-07"
    verification:
      - kind: other
        ref: "./gradlew --no-daemon projects (topology intact) + ./gradlew --no-daemon clean :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest detekt lint + :shared:compileKotlinIosSimulatorArm64 — all BUILD SUCCESSFUL"
        status: pass
      - kind: other
        ref: "gh run watch 35434892941 (android.yml, commit e8eb6a7, 4/4 jobs) + gh run watch 35434892938 (ios.yml, commit e8eb6a7)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Every BUILD-01..BUILD-07 outcome consolidated in .planning/STATE.md, keyed by requirement id, with the version-target policy, pinned version, matched/superset verdict, and forcing error where applicable; PROJECT.md Key Decisions table carries a matching Phase 2 row"
    requirement: "BUILD-07"
    verification:
      - kind: other
        ref: "grep for '## Phase 02 version deviations', all seven BUILD-0[1-7] ids, and '## Phase 02 DCL pilot outcome' in STATE.md; grep for 'Phase 1 shipped' + a new Phase 2 row in PROJECT.md; gh run watch 35435373397 (android.yml, commit 1e497a0, 4/4 jobs) + gh run watch 35435373388 (ios.yml, commit 1e497a0)"
        status: pass
    human_judgment: false

duration: ~50min
completed: 2026-09-19
status: complete
---

# Phase 2 Plan 5: Gradle Declarative DSL Pilot + Version-Outcome Consolidation Summary

**`settings.gradle.kts` converted to `settings.gradle.dcl` on the first attempt (building and CI-green), with `androidApp`/`shared` deliberately staying on Kotlin DSL per a live-verified, dated finding that Declarative Gradle's module-level "Software Types" surface remains experimental and unready as of 2026-09-19 — plus a full per-requirement consolidation of every BUILD-01..BUILD-07 outcome across the phase into `.planning/STATE.md` and `.planning/PROJECT.md`.**

## Performance

- **Duration:** ~50 min
- **Started:** ~2026-09-19T08:55:00Z (approximate)
- **Completed:** 2026-09-19T09:41:00Z
- **Tasks:** 2 (Task 1: DCL pilot; Task 2: version-outcome consolidation)
- **Files modified:** 3 (1 created/renamed, 2 docs edited)

## Accomplishments
- `settings.gradle.kts` converted 1:1 to `settings.gradle.dcl` on the **primary attempt** — the buildSrc fallback in the plan's ordering was never needed. Repository lists (Google, Maven Central, Gradle Plugin Portal, in both `pluginManagement` and `dependencyResolutionManagement`), `rootProject.name`, and both `include(...)` calls preserved exactly.
- Confirmed, via a live 2026-09-19 fetch of `docs.gradle.org`, `blog.gradle.org`, and `github.com/gradle/declarative-gradle`'s `main` branch (not training-data recall), that Declarative Gradle's full "Software Types" model is still explicitly documented as "not ready for adoption by plugin authors, build engineers or software engineers," unchanged since an April 2025 EAP3 snapshot — corroborating and dating 02-RESEARCH.md's MEDIUM-confidence assessment rather than assuming it still held.
- Discovered, empirically, that a settings file limited to core Gradle settings APIs (no software types) is a materially different — and already-working — case: Gradle 9.7.1 stable parses `settings.gradle.dcl` with this content with zero experimental flags or nightly builds required, contradicting the more pessimistic prior expectation implied by the DCL ecosystem's overall immaturity.
- `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")` and `@file:Suppress("UnstableApiUsage")` dropped as behavior-preserving simplifications — verified, not assumed: a project-wide grep for typesafe project-accessor usage (`projects.shared`/`projects.androidApp`) returned zero hits, and a full clean build (both Android variants, both test suites, detekt, lint, and the iOS Kotlin/Native compile target) passed with the flag removed.
- Full per-requirement version-outcome record written to `.planning/STATE.md`'s new `## Phase 02 version deviations` section — the version-target policy, pinned version, REQUIREMENTS.md target, matched/superset verdict, and forcing error (where applicable) for every one of BUILD-01 through BUILD-07 — discharging D-03's documentation obligation for the whole phase in one place.
- `.planning/PROJECT.md`'s Key Decisions table gained a Phase 2 row matching the Phase 1 row's shape; the Phase 1 row was left untouched.
- Both commits (`build` for the DCL pilot, `docs` for the consolidation) pushed and driven through CI to green — `android.yml` 4/4 jobs on both commits, `ios.yml` green on both.

## Task Commits

Each task was committed atomically:

1. **Task 1: Gradle Declarative DSL pilot** - `e8eb6a7` (build) — `settings.gradle.kts` → `settings.gradle.dcl`; `.planning/STATE.md` new `## Phase 02 DCL pilot outcome` section
2. **Task 2: Version-outcome consolidation** - `1e497a0` (docs) — `.planning/STATE.md` new `## Phase 02 version deviations` section + discharged Blockers/Concerns entry; `.planning/PROJECT.md` new Key Decisions row

**Plan metadata:** commit pending (this SUMMARY + STATE.md + ROADMAP.md + REQUIREMENTS.md)

## Files Created/Modified
- `settings.gradle.dcl` (created, renamed from `settings.gradle.kts`) - Gradle Declarative DSL pilot conversion; `@file:Suppress`/`enableFeaturePreview` dropped, repositories/name/includes preserved
- `.planning/STATE.md` - `## Phase 02 DCL pilot outcome` section (Task 1); `## Phase 02 version deviations` section + discharged Blockers/Concerns entry (Task 2)
- `.planning/PROJECT.md` - new Key Decisions row for the Phase 2 staged-upgrade outcome
- `.planning/REQUIREMENTS.md` - BUILD-07 marked complete, traceability table updated (via `requirements mark-complete`, part of this plan's metadata commit)

## Decisions Made
- **DCL pilot target and order (D-08/D-09):** attempted `settings.gradle.kts` first per 02-RESEARCH.md Open Question 2's recommendation — it succeeded, so the `buildSrc/build.gradle.kts` fallback was never attempted and remains untouched.
- **Dropped constructs treated as simplifications, not losses:** the file annotation had no Gradle-level meaning to preserve; the feature-preview flag's effect (typesafe project accessors) was verified unused anywhere in the codebase before dropping it.
- **`androidApp`/`shared` scope boundary held exactly as D-08 specifies:** neither module's build file was attempted, and the live re-check found this is not just a conservative scoping choice but currently the only viable choice — AGP 9's KMP-library plugin, Detekt, Hilt, Firebase Crashlytics/Performance, and Compose (the plugins these two files apply) would require Declarative Gradle's "Software Types" ecosystem plugins, which remain explicitly unready.

## Deviations from Plan

None — plan executed exactly as written. The primary DCL attempt succeeded on the first try (no fallback needed, no revert needed), and both tasks' verification gates passed without requiring an auto-fix.

## Issues Encountered
None. The one operational hiccup — a `run_in_background` Bash call whose completion notification arrived out of order relative to a duplicate foreground retry — was a tooling/environment timing quirk, not a build or plan issue; the actual build output was captured correctly once located.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 2 (Dependency & Build Tooling Upgrade) is now fully complete: all 5 plans landed, all 7 BUILD requirements (BUILD-01 through BUILD-07) satisfied per `.planning/REQUIREMENTS.md`'s traceability table, and the full per-requirement version-outcome record lives in `.planning/STATE.md` for Phase 3+ to consult instead of re-deriving.
- Phase 3 (multi-module split) should re-run the same live Declarative Gradle status check before assuming the ecosystem has matured enough to attempt DCL on any new `core-*`/`feature-*` module's build file — the settings-file success in this plan does not generalize to full module build files, per the documented reasoning above.
- No blockers for Phase 3.

## Self-Check: PASSED

- FOUND: settings.gradle.dcl
- CONFIRMED: settings.gradle.kts no longer present (`ls settings.gradle.kts` fails)
- FOUND: .planning/STATE.md contains `## Phase 02 DCL pilot outcome` and `## Phase 02 version deviations` sections, all seven BUILD-0[1-7] ids present
- FOUND: .planning/PROJECT.md contains a new Phase 2 Key Decisions row; Phase 1 row unchanged
- FOUND commit e8eb6a756f0f2390175c0ef27a83df39f67feac8 in git log
- FOUND commit 1e497a0f30ee3ad621b8ce10eb60d1fea5fd6363 in git log
- CONFIRMED: android.yml run 35434892941 conclusion=success on commit e8eb6a7 (4/4 jobs)
- CONFIRMED: ios.yml run 35434892938 conclusion=success on commit e8eb6a7
- CONFIRMED: android.yml run 35435373397 conclusion=success on commit 1e497a0 (4/4 jobs)
- CONFIRMED: ios.yml run 35435373388 conclusion=success on commit 1e497a0

---
*Phase: 02-dependency-build-tooling-upgrade*
*Completed: 2026-09-19*
