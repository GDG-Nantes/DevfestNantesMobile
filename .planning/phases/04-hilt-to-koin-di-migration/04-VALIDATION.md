---
phase: "4"
slug: "hilt-to-koin-di-migration"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-04"
---

# Phase 4 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit4 (Android `testDebugUnitTest`), `kotlin.test` (KMP common/jvm/ios), Koin `koin-test` 4.2.2 `verify()`, optional `koin-test-junit4` `KoinTestRule` |
| **Config file** | none — convention plugins (`AndroidCommon.kt`, `KmpLibraryConventionPlugin.kt`); Wave 0 adds Koin test deps |
| **Quick run command** | `./gradlew :feature:venue:testDebugUnitTest :shared:jvmTest` |
| **Full suite command** | `./gradlew testDebugUnitTest jvmTest detekt lint` (+ `./gradlew :shared:iosSimulatorArm64Test` on macOS) |
| **Estimated runtime** | ~300 seconds (full), ~60 seconds (quick) |

---

## Sampling Rate

- **After every task commit:** the touched module's `testDebugUnitTest`/`jvmTest` + `bash scripts/di-gate.sh` (once it exists)
- **After every plan wave:** `./gradlew testDebugUnitTest jvmTest detekt lint`, `:shared:linkDebugFrameworkIosSimulatorArm64` + `swift-names-gate.sh check`
- **Before `/gsd-verify-work`:** Full suite must be green on both CI workflows, clean build, `assembleRelease`, Android + iOS smoke checkpoints
- **Max feedback latency:** 300 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 4-01-T1 | 04-01 | 1 | DI-01, DI-02, DI-03 | T-04-SC, T-04-01, T-04-02 | Koin coordinates verified; no Koin names in shared.h; both apps launch | tracer build + header gate + launch smokes | `./gradlew --no-daemon :feature:venue:assembleDebug :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64 detekt`; swift-names-gate `check`; `android run …`; `ios-sim-smoke.sh` | ❌ W0 (created in task) | ⬜ pending |
| 4-01-T2 | 04-01 | 1 | DI-02, DI-03, DI-06 | T-04-03 | duplicate definitions fail start-up | unit (module verify, full-graph verify + negative proof, JVM + iOS start-up) | `./gradlew --no-daemon :core:network:jvmTest :core:data:jvmTest :shared:jvmTest :shared:iosSimulatorArm64Test :feature:venue:testDebugUnitTest :androidApp:testDebugUnitTest detekt` | ❌ W0 (created in task) | ⬜ pending |
| 4-01-T3 | 04-01 | 1 | DI-06 | — | N/A | CI wiring | `CI-GREEN-BOTH` + `CI-RUNS-KOIN-TESTS` (gh run logs show `:shared:jvmTest` / `:shared:iosSimulatorArm64Test`) | ❌ W0 | ⬜ pending |
| 4-02-T1..T2 | 04-02 | 2 | DI-02, DI-03, DI-04 | T-04-05, T-04-07, T-04-08 | same SharedPreferences file and keys | unit + header gate + key diff | `./gradlew … :androidApp:testDebugUnitTest :shared:linkDebugFrameworkIosSimulatorArm64`; `PREFS-KEYS-OK` | ❌ W0 | ⬜ pending |
| 4-02-T3 | 04-02 | 2 | DI-05 | T-04-06 | fakes unreachable from production | unit | `./gradlew --no-daemon :core:testing:jvmTest …`; `TESTING-ISOLATED-OK` | partial (`FakeStoresTest.kt`) | ⬜ pending |
| 4-03-T1 | 04-03 | 3 | DI-01, DI-04 | T-04-09, T-04-11 | locator calls and new Hilt usage rejected | script gate + selftest | `bash scripts/di-gate.sh selftest && bash scripts/di-gate.sh` | ❌ W0 (created in task) | ⬜ pending |
| 4-03-T2..04-05-T2 | 04-03..04-05 | 3-5 | DI-01, DI-02, DI-04, DI-05 | T-04-10, T-04-12..18 | consent/filters/bookmarks single instances, keys unchanged | per-module `verify()` + full graph + gate | `./gradlew --no-daemon :feature:<x>:testDebugUnitTest … :androidApp:testDebugUnitTest detekt && bash scripts/di-gate.sh` | ❌ W0 (created per task) | ⬜ pending |
| 4-06-T1 | 04-06 | 6 | DI-01, DI-04 | T-04-19, T-04-21 | consent-first start order | build + order check + launch | `STARTUP-ORDER-OK`, `ANDROID-LAUNCH-OK` | ❌ W0 | ⬜ pending |
| 4-06-T3 | 04-06 | 6 | DI-02, DI-05, DI-06 | T-04-19, T-04-20 | initializer order asserted; test deps test-scoped | unit (runtime start-up) | `./gradlew --no-daemon :androidApp:testDebugUnitTest detekt`; `TEST-DEPS-SCOPED-OK` | ❌ W0 (created in task) | ⬜ pending |
| 4-07-T1 | 04-07 | 7 | DI-01 | T-04-24, T-04-25 | strict gate | gate + clean full build | `bash scripts/di-gate.sh`; `./gradlew --no-daemon clean && ./gradlew --no-daemon assembleDebug assembleRelease testDebugUnitTest jvmTest detekt lint :shared:iosSimulatorArm64Test …`; `NO-HILT-ON-CLASSPATH` | ✅ (from 04-03) | ⬜ pending |
| 4-07-T2 | 04-07 | 7 | DI-01 | T-04-23, T-04-26 | release (R8) launch | release smoke + docs + CI | `android-release-smoke.sh`; `DOCS-KOIN-OK`; `CI-GREEN-BOTH` | ❌ W0 (created in task) | ⬜ pending |

*Task IDs filled in by the planner. Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] Catalog + convention: `koin-*` entries, Koin convention wiring, `koin-test` for test source sets
- [ ] `feature/<x>/src/test/.../di/<X>ModuleTest.kt` for each of 6 features; `core/ui` module test
- [ ] `shared/src/jvmTest` (verify + start) and `shared/src/iosTest` (start + store via `IosDependencies`)
- [ ] `androidApp/src/test` full-graph `verify()` (+ negative proof) and runtime start-up test
- [ ] `core:testing` fakes + Koin helper
- [ ] `scripts/di-gate.sh` + workflow step; `android.yml` command change; `ios.yml` test step

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Android app behaves identically (all tabs, session/speaker detail, settings, bookmark toggle) | DI-01..DI-04 | Runtime UI behavior after DI swap | `android run` + `android layout` / `android screen capture` after tracer and after Hilt removal |
| iOS app launches and loads data via Koin store | DI-02, DI-03 | Simulator UI smoke | Run iosApp in simulator after tracer and at phase end |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 300s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
