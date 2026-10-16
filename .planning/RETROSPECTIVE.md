# Project Retrospective

*A living document updated after each milestone. Lessons feed forward into future planning.*

## Milestone: v1.0 — Modernization

**Shipped:** 2026-10-16
**Phases:** 5 | **Plans:** 37 | **Sessions:** not tracked

### What Was Built
- iOS CI fixed at the root cause (Xcode and the simulator resolved at run time), branch-aware Gradle cache, Konan cache, and separate Android and iOS jobs.
- Toolchain upgraded to Kotlin 2.4.20, AGP 9.4.0 with the KMP library plugin, Gradle 9.7.1, Compose BOM 2026.09.00 and Apollo 5.2.0, plus a `settings.gradle.dcl` pilot.
- `build-logic` convention plugins, six `core-*` and six `feature-*` modules, and `:shared` as a source-less iOS umbrella framework.
- Hilt replaced by Koin 4.2.2 with one module per Gradle module, `initKoin()` on both platforms, a CI DI gate and `verify()` graph tests.
- Test coverage from 15.96 % to 30.57 % (Kover): fixtures, Apollo MockServer store and mapper tests, all ten ViewModels, and a Compose smoke test in each feature module.

### What Worked
- Ordering the work CI → toolchain → modules → DI → tests: every phase started from a green, trustworthy signal, and Koin's constructor injection made the phase 5 tests cheap.
- Upgrading one dependency group per revertible commit with a green CI run in between made the forced sibling bumps (minSdk 26, compileSdk 37, Detekt/Hilt/Firebase-perf for AGP 9) easy to diagnose.
- Characterization tests that pin behavior first, then a change, kept the "no behavior change" rule enforceable. Known bugs were pinned and deferred, not silently fixed.
- Human UAT on a device after each phase caught what static checks could not, such as the OpenFeedback `NoClassDefFoundError` (G-05-3).

### What Was Inefficient
- Phase 3 halted at 03-01 on a Kotlin/Native Swift name collision (Apollo types against domain types). It needed two inserted plans (03-10, 03-11) before the rest of the phase could run.
- The OpenFeedback/kotlinx-datetime linkage break went in during phase 2 (datetime 0.8.0) and only surfaced in phase 5 UAT, because nothing exercised that path on a device or the JVM in between.
- Some planning metadata drifted: BUILD-01 was never listed in any SUMMARY's `requirements-completed`, the 02-05 and 03-08 SUMMARY frontmatter is not parseable YAML, and the debug session stayed `diagnosed` after its fix.
- Rate limits cut agents off mid-run, so finished artifacts had to be committed and pushed right away.
- Testing with a shifted device clock broke SSL. Data had to be cached first, and the Play image needed the Settings UI to change the date.

### Patterns Established
- Convention plugins `devfest.kmp.library`, `devfest.android.{library,feature,application,koin}` and `devfest.detekt`, with `:core:testing` wired into every feature module's tests.
- Each Gradle module owns a `xxxModule` Koin module, and each has a `verify()` test. Only Android framework entry points use `by inject()`.
- Feature modules expose callback-only `*Route` entry points, and navigation stays in `:androidApp`.
- Swift-name and DI gates run as scripts in CI (`swift-names-gate.sh`, `di-gate.sh`).
- Linkage guards (JVM tests) protect dependency pins that only fail at run time.

### Key Lessons
1. When a dependency bump changes binary classes (kotlinx-datetime `Instant`), add a run-time linkage test in the same phase. A green compile and CI are not proof.
2. Run a Kotlin/Native export or Swift-surface check at the start of any module split that feeds an iOS umbrella framework.
3. Validate SUMMARY frontmatter (parseable YAML, `requirements-completed` filled in) at plan close, not at the milestone audit.
4. Live-data checks (110 speakers, room ids) find mapping gaps that recorded fixtures hide. Schedule a live-API diff whenever fixtures are recorded.

### Cost Observations
- Model mix: adaptive profile (Opus orchestration, Haiku for the integration checker). Exact split not tracked.
- Sessions: not tracked.
- Notable: usage limits interrupted agents mid-run. Committing each finished artifact immediately kept the cost of those interruptions low.

---

## Cross-Milestone Trends

### Process Evolution

| Milestone | Sessions | Phases | Key Change |
|-----------|----------|--------|------------|
| v1.0 | n/a | 5 | First GSD milestone: phased modernization with per-phase verification, UAT, security and code review |

### Cumulative Quality

| Milestone | Tests | Coverage | Zero-Dep Additions |
|-----------|-------|----------|-------------------|
| v1.0 | 264 `@Test` (Kotlin) | 30.57 % lines (Kover) | n/a |

### Top Lessons (Verified Across Milestones)

1. (To be confirmed by later milestones.) Stage risky upgrades as revertible, individually green steps.
