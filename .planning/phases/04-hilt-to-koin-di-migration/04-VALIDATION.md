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
| 4-xx | TBD | TBD | DI-01 | — | N/A | gate + build | `bash scripts/di-gate.sh && ./gradlew clean assembleDebug assembleRelease` | ❌ W0 | ⬜ pending |
| 4-xx | TBD | TBD | DI-02 | — | N/A | unit (per-module + composite `verify()`) | `./gradlew testDebugUnitTest jvmTest` | ❌ W0 | ⬜ pending |
| 4-xx | TBD | TBD | DI-03 | — | N/A | unit (full-graph verify with `extraTypes`) + iOS start test | `./gradlew :androidApp:testDebugUnitTest :shared:iosSimulatorArm64Test` | ❌ W0 | ⬜ pending |
| 4-xx | TBD | TBD | DI-04 | — | N/A | script gate | `bash scripts/di-gate.sh` | ❌ W0 | ⬜ pending |
| 4-xx | TBD | TBD | DI-05 | — | N/A | unit | `./gradlew :core:testing:jvmTest` | partial (`FakeStoresTest.kt`) | ⬜ pending |
| 4-xx | TBD | TBD | DI-06 | — | N/A | CI wiring | `testDebugUnitTest jvmTest` in android.yml; `:shared:iosSimulatorArm64Test` in ios.yml | ❌ W0 | ⬜ pending |

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
